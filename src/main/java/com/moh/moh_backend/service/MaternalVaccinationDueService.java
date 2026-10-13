package com.moh.moh_backend.service;

import com.moh.moh_backend.Enum.PregnancyStatus;
import com.moh.moh_backend.Enum.TargetGroup;
import com.moh.moh_backend.dto.MaternalVaccinationDtos;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.MotherVaccination;
import com.moh.moh_backend.model.PhmArea;
import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.model.VaccineSchedule;
import com.moh.moh_backend.repository.DoctorRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.MotherVaccinationRepository;
import com.moh.moh_backend.repository.PregnancyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tetanus toxoid (TT / Td / aTd) protection for pregnant mothers (client request 3.3).
 *
 * >>> These rules must be verified and signed off by the MOH / Medical Officer. <<<
 * They follow the WHO schedule for women of childbearing age as used in Sri Lanka:
 *   - doses before this pregnancy = earlier history entered by the midwife + doses recorded here;
 *   - 0 earlier doses: 2 doses this pregnancy; 1 earlier dose: 1 dose;
 *   - 2-4 earlier doses: 1 booster unless the last dose still protects at the EDD
 *     (protection after the 2nd dose 3 years, 3rd 5 years, 4th 10 years);
 *   - 5 doses: protected for all childbearing years, no more doses;
 *   - first dose from 12 weeks of gestation, a 2nd dose in the same pregnancy 6 weeks later
 *     (never less than 4 weeks after any earlier dose), and finished 2 weeks before the EDD.
 * Other maternal vaccines (e.g. influenza) are recorded and listed but have no due dates.
 */
@Service
public class MaternalVaccinationDueService {

    public static final int FULL_COURSE_DOSES = 5;
    public static final int FIRST_DOSE_FROM_WEEK = 12;
    /** Recommended gap between two doses in the same pregnancy. */
    public static final int SAME_PREGNANCY_INTERVAL_DAYS = 42;
    /** A dose given sooner than this after the previous one does not count (WHO minimum). */
    public static final int MIN_INTERVAL_DAYS = 28;
    /** The course should be complete this long before the EDD for the baby to be protected. */
    public static final int FINISH_BEFORE_EDD_DAYS = 14;
    public static final int DUE_SOON_DAYS = VaccinationDueService.DUE_SOON_DAYS;
    public static final int GRACE_DAYS = VaccinationDueService.GRACE_DAYS;

    private final MotherVaccinationRepository motherVaccinationRepository;
    private final PregnancyRepository pregnancyRepository;
    private final MotherRepository motherRepository;
    private final VaccineScheduleService vaccineScheduleService;
    private final MidwifeRepository midwifeRepository;
    private final DoctorRepository doctorRepository;

    public MaternalVaccinationDueService(MotherVaccinationRepository motherVaccinationRepository,
                                         PregnancyRepository pregnancyRepository,
                                         MotherRepository motherRepository,
                                         VaccineScheduleService vaccineScheduleService,
                                         MidwifeRepository midwifeRepository,
                                         DoctorRepository doctorRepository) {
        this.motherVaccinationRepository = motherVaccinationRepository;
        this.pregnancyRepository = pregnancyRepository;
        this.motherRepository = motherRepository;
        this.vaccineScheduleService = vaccineScheduleService;
        this.midwifeRepository = midwifeRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional(readOnly = true)
    public MaternalVaccinationDtos.PregnancyStatus forPregnancy(Pregnancy pregnancy) {
        Mother mother = pregnancy.getMother();
        return evaluate(pregnancy, mother, motherVaccinationRepository.findByMotherId(mother.getMotherId()),
                motherSchedules(), LocalDate.now());
    }

    /** Active pregnancies in the caller's PHM area whose tetanus dose is due or overdue. */
    @Transactional(readOnly = true)
    public List<MaternalVaccinationDtos.Defaulter> defaultersForArea(Integer requestedAreaId, Integer userId, String role) {
        Integer areaId = resolveArea(requestedAreaId, userId, role);
        List<Pregnancy> active = pregnancyRepository.findByMother_PhmArea_PhmAreaIdAndPregnancyStatus(areaId, PregnancyStatus.ACTIVE)
                .stream()
                .filter(p -> p.getMother() != null && !Boolean.FALSE.equals(p.getMother().getActive()))
                .toList();
        if (active.isEmpty()) return List.of();

        Map<Integer, List<MotherVaccination>> byMother = motherVaccinationRepository
                .findByMotherIdIn(active.stream().map(p -> p.getMother().getMotherId()).distinct().toList())
                .stream()
                .collect(Collectors.groupingBy(v -> v.getPregnancy().getMother().getMotherId()));
        List<VaccineSchedule> schedules = motherSchedules();
        LocalDate today = LocalDate.now();

        List<MaternalVaccinationDtos.Defaulter> result = new ArrayList<>();
        for (Pregnancy p : active) {
            Mother m = p.getMother();
            MaternalVaccinationDtos.PregnancyStatus s =
                    evaluate(p, m, byMother.getOrDefault(m.getMotherId(), List.of()), schedules, today);
            if (!"OVERDUE".equals(s.status) && !"DUE".equals(s.status)) continue;
            MaternalVaccinationDtos.Defaulter d = new MaternalVaccinationDtos.Defaulter();
            d.pregnancyId = p.getPregnancyId();
            d.pregnancyNumber = p.getPregnancyNumber();
            d.motherId = m.getMotherId();
            d.motherName = m.getName();
            d.contactNumber = m.getContactNumber();
            d.address = m.getAddress();
            d.gestationWeeks = s.gestationWeeks;
            d.eddDate = p.getEddDate();
            d.historyRecorded = s.historyRecorded;
            d.status = s.status;
            d.nextLifetimeDoseNumber = s.nextLifetimeDoseNumber;
            d.nextDueDate = s.nextDueDate;
            d.daysOverdue = s.daysOverdue;
            result.add(d);
        }
        result.sort(Comparator.comparingInt((MaternalVaccinationDtos.Defaulter d) -> "OVERDUE".equals(d.status) ? 0 : 1)
                .thenComparing(d -> d.daysOverdue == null ? 0 : -d.daysOverdue));
        return result;
    }

    /** Records how many tetanus doses the mother had before this system (0-5) and when the last one was. */
    @Transactional
    public Mother saveHistory(Mother mother, MaternalVaccinationDtos.TtHistoryRequest req) {
        if (req == null || req.priorDoses == null) {
            throw new IllegalArgumentException("Number of earlier tetanus doses is required (0 if none)");
        }
        if (req.priorDoses < 0 || req.priorDoses > FULL_COURSE_DOSES) {
            throw new IllegalArgumentException("Earlier tetanus doses must be between 0 and " + FULL_COURSE_DOSES);
        }
        if (req.lastDoseDate != null) {
            if (req.priorDoses == 0) {
                throw new IllegalArgumentException("Leave the last dose date empty when there were no earlier doses");
            }
            if (req.lastDoseDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Last dose date cannot be in the future");
            }
            if (mother.getDateOfBirth() != null && req.lastDoseDate.isBefore(mother.getDateOfBirth())) {
                throw new IllegalArgumentException("Last dose date cannot be before the mother's date of birth");
            }
        }
        mother.setPriorTtDoses(req.priorDoses);
        mother.setPriorTtLastDate(req.lastDoseDate);
        return motherRepository.save(mother);
    }

    /** Pure evaluation; {@code allMotherVaccinations} are every maternal vaccine recorded for this mother. */
    public MaternalVaccinationDtos.PregnancyStatus evaluate(Pregnancy pregnancy, Mother mother,
                                                           List<MotherVaccination> allMotherVaccinations,
                                                           List<VaccineSchedule> motherSchedules,
                                                           LocalDate today) {
        MaternalVaccinationDtos.PregnancyStatus s = new MaternalVaccinationDtos.PregnancyStatus();
        LocalDate lmp = pregnancy.getLmpDate();
        LocalDate edd = pregnancy.getEddDate() != null ? pregnancy.getEddDate() : (lmp != null ? lmp.plusDays(280) : null);
        s.pregnancyId = pregnancy.getPregnancyId();
        s.pregnancyNumber = pregnancy.getPregnancyNumber();
        s.pregnancyStatus = pregnancy.getPregnancyStatus() != null ? pregnancy.getPregnancyStatus().name() : null;
        s.motherId = mother.getMotherId();
        s.motherName = mother.getName();
        s.lmpDate = lmp;
        s.eddDate = edd;
        if (lmp != null) s.gestationWeeks = (int) (ChronoUnit.DAYS.between(lmp, today) / 7);
        s.historyRecorded = mother.getPriorTtDoses() != null;
        s.priorTtDoses = mother.getPriorTtDoses();
        s.priorTtLastDate = mother.getPriorTtLastDate();

        List<MotherVaccination> all = new ArrayList<>(allMotherVaccinations == null ? List.of() : allMotherVaccinations);
        all.sort(Comparator.comparing(MotherVaccination::getVaccinationDate, Comparator.nullsLast(Comparator.naturalOrder())));
        List<MotherVaccination> thisPregnancy = all.stream()
                .filter(v -> v.getPregnancy() != null && pregnancy.getPregnancyId() != null
                        && pregnancy.getPregnancyId().equals(v.getPregnancy().getPregnancyId()))
                .toList();
        List<MotherVaccination> tetanusEarlier = all.stream()
                .filter(v -> isTetanus(v.getSchedule()) && !thisPregnancy.contains(v))
                .filter(v -> lmp == null || v.getVaccinationDate() == null || v.getVaccinationDate().isBefore(lmp))
                .toList();
        List<MotherVaccination> tetanusThis = thisPregnancy.stream().filter(v -> isTetanus(v.getSchedule())).toList();

        int prior = mother.getPriorTtDoses() == null ? 0 : mother.getPriorTtDoses();
        s.dosesBeforePregnancy = Math.min(FULL_COURSE_DOSES, prior + tetanusEarlier.size());
        s.lastDoseBeforePregnancy = latest(mother.getPriorTtLastDate(),
                tetanusEarlier.isEmpty() ? null : tetanusEarlier.get(tetanusEarlier.size() - 1).getVaccinationDate());

        int ordinal = prior;
        for (MotherVaccination v : tetanusEarlier) s.earlierDoses.add(toDose(v, ++ordinal));
        ordinal = s.dosesBeforePregnancy;
        for (MotherVaccination v : thisPregnancy) {
            s.doses.add(toDose(v, isTetanus(v.getSchedule()) ? ++ordinal : null));
        }

        s.dosesNeeded = dosesNeeded(s.dosesBeforePregnancy, s.lastDoseBeforePregnancy, edd);
        s.dosesGivenThisPregnancy = tetanusThis.size();
        s.lifetimeDoses = Math.min(FULL_COURSE_DOSES, s.dosesBeforePregnancy + s.dosesGivenThisPregnancy);
        if (edd != null) s.latestDate = edd.minusDays(FINISH_BEFORE_EDD_DAYS);

        if (pregnancy.getPregnancyStatus() != null && pregnancy.getPregnancyStatus() != PregnancyStatus.ACTIVE) {
            s.status = "CLOSED";
            return s;
        }
        int remaining = Math.max(0, s.dosesNeeded - s.dosesGivenThisPregnancy);
        if (remaining == 0 || lmp == null) {
            s.status = remaining == 0 ? "PROTECTED" : "NOT_YET";
            return s;
        }

        s.nextLifetimeDoseNumber = s.lifetimeDoses + 1;
        s.nextScheduleId = suggestSchedule(motherSchedules, s.nextLifetimeDoseNumber);

        LocalDate lastAny = latest(s.lastDoseBeforePregnancy,
                tetanusThis.isEmpty() ? null : tetanusThis.get(tetanusThis.size() - 1).getVaccinationDate());
        LocalDate due;
        if (tetanusThis.isEmpty()) {
            due = lmp.plusWeeks(FIRST_DOSE_FROM_WEEK);
            if (lastAny != null && lastAny.plusDays(MIN_INTERVAL_DAYS).isAfter(due)) {
                due = lastAny.plusDays(MIN_INTERVAL_DAYS);
            }
        } else {
            LocalDate lastThis = tetanusThis.get(tetanusThis.size() - 1).getVaccinationDate();
            due = lastThis.plusDays(SAME_PREGNANCY_INTERVAL_DAYS);
            // Bring it forward if waiting 6 weeks would miss the EDD - 14 day cut-off (but never under 4 weeks)
            if (s.latestDate != null && due.isAfter(s.latestDate)) {
                LocalDate earliest = lastThis.plusDays(MIN_INTERVAL_DAYS);
                due = earliest.isAfter(s.latestDate) ? earliest : s.latestDate;
            }
        }
        s.nextDueDate = due;

        long daysFromDue = ChronoUnit.DAYS.between(due, today);
        boolean pastCutOff = s.latestDate != null && today.isAfter(s.latestDate);
        if (daysFromDue > GRACE_DAYS || (pastCutOff && daysFromDue >= 0)) {
            s.status = "OVERDUE";
            s.daysOverdue = (int) Math.max(1, daysFromDue);
        } else if (daysFromDue >= 0) {
            s.status = "DUE";
        } else if (daysFromDue >= -DUE_SOON_DAYS) {
            s.status = "DUE_SOON";
        } else {
            s.status = "NOT_YET";
        }
        return s;
    }

    /** How many tetanus doses a pregnancy needs, from the doses before it (WHO protection periods). */
    static int dosesNeeded(int dosesBefore, LocalDate lastDoseBefore, LocalDate edd) {
        if (dosesBefore >= FULL_COURSE_DOSES) return 0;
        if (dosesBefore == 0) return 2;
        if (dosesBefore == 1) return 1;
        int protectionYears = switch (dosesBefore) {
            case 2 -> 3;
            case 3 -> 5;
            default -> 10; // 4 doses
        };
        boolean stillProtected = lastDoseBefore != null && edd != null
                && !lastDoseBefore.plusYears(protectionYears).isBefore(edd);
        return stillProtected ? 0 : 1;
    }

    /** True for tetanus toxoid and tetanus-diphtheria vaccines given to mothers. */
    public static boolean isTetanus(VaccineSchedule s) {
        if (s == null || s.getVaccineName() == null) return false;
        if (s.getTargetGroup() != null && s.getTargetGroup() != TargetGroup.MOTHER) return false;
        String n = s.getVaccineName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return n.contains("tetanus") || n.startsWith("tt") || n.equals("td") || n.contains("atd");
    }

    private List<VaccineSchedule> motherSchedules() {
        return vaccineScheduleService.getSchedulesByTargetGroup(TargetGroup.MOTHER);
    }

    private static Integer suggestSchedule(List<VaccineSchedule> schedules, int lifetimeDose) {
        List<VaccineSchedule> tetanus = (schedules == null ? List.<VaccineSchedule>of() : schedules).stream()
                .filter(MaternalVaccinationDueService::isTetanus)
                .filter(VaccineSchedule::isCurrent)
                .sorted(Comparator.comparing(VaccineSchedule::getDoseNumber, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        if (tetanus.isEmpty()) return null;
        return tetanus.stream()
                .filter(v -> v.getDoseNumber() != null && v.getDoseNumber() == lifetimeDose)
                .findFirst()
                .orElse(tetanus.get(tetanus.size() - 1))
                .getScheduleId();
    }

    private static MaternalVaccinationDtos.Dose toDose(MotherVaccination v, Integer lifetimeDoseNumber) {
        MaternalVaccinationDtos.Dose d = new MaternalVaccinationDtos.Dose();
        d.vaccinationId = v.getVaccinationId();
        d.pregnancyId = v.getPregnancy() != null ? v.getPregnancy().getPregnancyId() : null;
        if (v.getSchedule() != null) {
            d.scheduleId = v.getSchedule().getScheduleId();
            d.vaccineName = v.getSchedule().getVaccineName();
            d.scheduleDoseNumber = v.getSchedule().getDoseNumber();
        }
        d.tetanus = isTetanus(v.getSchedule());
        d.lifetimeDoseNumber = lifetimeDoseNumber;
        d.vaccinationDate = v.getVaccinationDate();
        d.batchNumber = v.getBatchNumber();
        d.manufacturer = v.getManufacturer();
        d.adverseReaction = v.getAdverseReaction();
        d.givenBy = v.getMidwife() != null ? v.getMidwife().getName() : null;
        return d;
    }

    private static LocalDate latest(LocalDate a, LocalDate b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    private Integer resolveArea(Integer requestedAreaId, Integer userId, String role) {
        if ("MIDWIFE".equalsIgnoreCase(role)) {
            PhmArea area = midwifeRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Midwife profile not found for this account"))
                    .getPhmArea();
            if (area == null) throw new IllegalStateException("Midwife has no PHM area assigned");
            return area.getPhmAreaId();
        }
        if ("DOCTOR".equalsIgnoreCase(role)) {
            PhmArea area = doctorRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Doctor profile not found for this account"))
                    .getPhmArea();
            if (area == null) throw new IllegalStateException("Doctor has no PHM area assigned");
            return area.getPhmAreaId();
        }
        if ("ADMIN".equalsIgnoreCase(role)) {
            if (requestedAreaId == null) throw new IllegalArgumentException("phmAreaId is required");
            return requestedAreaId;
        }
        throw new IllegalStateException("Not allowed to view area vaccination lists");
    }
}
