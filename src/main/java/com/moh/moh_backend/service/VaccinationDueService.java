package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.VaccinationDueDtos;
import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.model.BabyVaccination;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.PhmArea;
import com.moh.moh_backend.model.TargetGroup;
import com.moh.moh_backend.model.VaccineSchedule;
import com.moh.moh_backend.repository.BabyRepository;
import com.moh.moh_backend.repository.BabyVaccinationRepository;
import com.moh.moh_backend.repository.DoctorRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.MotherRepository;
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
 * Works out which childhood vaccines are given, due or overdue, from the active rows of
 * VACCINE_SCHEDULE (the current Sri Lanka EPI). Access checks are the caller's job.
 *
 * Status windows (relative to the due date = date of birth + recommended age):
 *   UPCOMING  more than 14 days before the due date
 *   DUE_SOON  within 14 days before the due date
 *   DUE       from the due date up to 14 days after it
 *   OVERDUE   more than 14 days after the due date
 *   CONDITIONAL doses (e.g. BCG 2nd dose if no scar) are never reported as due/overdue.
 */
@Service
public class VaccinationDueService {

    public static final int DUE_SOON_DAYS = 14;
    public static final int GRACE_DAYS = 14;

    private final VaccineScheduleService vaccineScheduleService;
    private final BabyVaccinationRepository babyVaccinationRepository;
    private final BabyRepository babyRepository;
    private final MotherRepository motherRepository;
    private final MidwifeRepository midwifeRepository;
    private final DoctorRepository doctorRepository;

    public VaccinationDueService(VaccineScheduleService vaccineScheduleService,
                                 BabyVaccinationRepository babyVaccinationRepository,
                                 BabyRepository babyRepository,
                                 MotherRepository motherRepository,
                                 MidwifeRepository midwifeRepository,
                                 DoctorRepository doctorRepository) {
        this.vaccineScheduleService = vaccineScheduleService;
        this.babyVaccinationRepository = babyVaccinationRepository;
        this.babyRepository = babyRepository;
        this.motherRepository = motherRepository;
        this.midwifeRepository = midwifeRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional(readOnly = true)
    public VaccinationDueDtos.ChildSchedule forBaby(Baby baby) {
        return evaluate(baby, babyVaccinationRepository.findByBaby_BabyId(baby.getBabyId()), LocalDate.now());
    }

    /**
     * Children in a PHM area with overdue or due vaccines. Midwives and doctors get their own area;
     * admins must pass phmAreaId.
     */
    @Transactional(readOnly = true)
    public List<VaccinationDueDtos.Defaulter> defaultersForArea(Integer requestedAreaId, Integer userId, String role) {
        Integer areaId = resolveArea(requestedAreaId, userId, role);
        List<Mother> mothers = motherRepository.findByPhmArea_PhmAreaId(areaId).stream()
                .filter(m -> !Boolean.FALSE.equals(m.getActive()))
                .toList();
        Map<Integer, Mother> motherById = mothers.stream().collect(Collectors.toMap(Mother::getMotherId, m -> m));

        List<Baby> babies = new ArrayList<>();
        for (Mother m : mothers) {
            babyRepository.findByMotherId(m.getMotherId()).stream()
                    .filter(b -> !Boolean.FALSE.equals(b.getIsAlive()) && b.getDateOfBirth() != null)
                    .forEach(babies::add);
        }
        if (babies.isEmpty()) return List.of();

        Map<Integer, List<BabyVaccination>> givenByBaby = babyVaccinationRepository
                .findByBaby_BabyIdIn(babies.stream().map(Baby::getBabyId).toList()).stream()
                .collect(Collectors.groupingBy(v -> v.getBaby().getBabyId()));

        LocalDate today = LocalDate.now();
        List<VaccinationDueDtos.Defaulter> result = new ArrayList<>();
        for (Baby b : babies) {
            VaccinationDueDtos.ChildSchedule cs = evaluate(b, givenByBaby.getOrDefault(b.getBabyId(), List.of()), today);
            if (cs.overdueCount == 0 && cs.dueCount == 0) continue;
            VaccinationDueDtos.Defaulter d = new VaccinationDueDtos.Defaulter();
            Mother m = motherById.get(b.getMotherId());
            d.babyId = b.getBabyId();
            d.babyName = b.getName();
            d.dateOfBirth = b.getDateOfBirth();
            d.gender = cs.gender;
            d.motherId = b.getMotherId();
            d.motherName = m != null ? m.getName() : null;
            d.contactNumber = m != null ? m.getContactNumber() : null;
            d.address = m != null ? m.getAddress() : null;
            d.overallStatus = cs.overallStatus;
            for (VaccinationDueDtos.Item it : cs.items) {
                if ("OVERDUE".equals(it.status)) d.overdue.add(it);
                else if ("DUE".equals(it.status)) d.due.add(it);
            }
            result.add(d);
        }
        // Most overdue first
        result.sort(Comparator.comparingInt((VaccinationDueDtos.Defaulter d) -> d.overdue.isEmpty() ? 1 : 0)
                .thenComparing(d -> d.overdue.isEmpty() ? 0 : -d.overdue.stream().mapToInt(i -> i.daysOverdue == null ? 0 : i.daysOverdue).max().orElse(0)));
        return result;
    }

    /** Pure evaluation, exposed for tests and for list views that already loaded the vaccinations. */
    public VaccinationDueDtos.ChildSchedule evaluate(Baby baby, List<BabyVaccination> given, LocalDate today) {
        VaccinationDueDtos.ChildSchedule cs = new VaccinationDueDtos.ChildSchedule();
        cs.babyId = baby.getBabyId();
        cs.babyName = baby.getName();
        cs.dateOfBirth = baby.getDateOfBirth();
        cs.gender = baby.getGender() != null ? baby.getGender().name() : null;

        List<VaccineSchedule> schedule = vaccineScheduleService.getSchedulesByTargetGroup(TargetGroup.BABY).stream()
                .filter(s -> appliesTo(s, baby))
                .sorted(Comparator.comparing((VaccineSchedule s) -> s.getRecommendedAgeDays() == null ? 0 : s.getRecommendedAgeDays())
                        .thenComparing(VaccineSchedule::getVaccineName))
                .toList();

        List<BabyVaccination> unmatched = new ArrayList<>(given == null ? List.of() : given);
        for (VaccineSchedule s : schedule) {
            VaccinationDueDtos.Item item = new VaccinationDueDtos.Item();
            item.scheduleId = s.getScheduleId();
            item.vaccineName = s.getVaccineName();
            item.doseNumber = s.getDoseNumber();
            item.recommendedAgeDays = s.getRecommendedAgeDays();
            item.description = s.getDescription();
            item.conditional = Boolean.TRUE.equals(s.getIsConditional());
            if (baby.getDateOfBirth() != null) {
                item.dueDate = baby.getDateOfBirth().plusDays(s.getRecommendedAgeDays() == null ? 0 : s.getRecommendedAgeDays());
            }

            BabyVaccination match = findGiven(s, unmatched);
            if (match != null) {
                unmatched.remove(match);
                item.status = "GIVEN";
                item.vaccinationId = match.getVaccinationId();
                item.vaccinationDate = match.getVaccinationDate();
                item.batchNumber = match.getBatchNumber();
                item.manufacturer = match.getManufacturer();
                item.adverseReaction = match.getAdverseReaction();
                cs.givenCount++;
            } else if (item.conditional) {
                item.status = "CONDITIONAL";
            } else if (item.dueDate == null) {
                item.status = "UPCOMING";
            } else {
                long daysFromDue = ChronoUnit.DAYS.between(item.dueDate, today);
                if (daysFromDue > GRACE_DAYS) {
                    item.status = "OVERDUE";
                    item.daysOverdue = (int) daysFromDue;
                    cs.overdueCount++;
                } else if (daysFromDue >= 0) {
                    item.status = "DUE";
                    cs.dueCount++;
                } else if (daysFromDue >= -DUE_SOON_DAYS) {
                    item.status = "DUE_SOON";
                } else {
                    item.status = "UPCOMING";
                }
            }
            if (!item.conditional && item.dueDate != null && !item.dueDate.isAfter(today)) {
                cs.requiredCount++;
            }
            if (cs.nextDue == null && !"GIVEN".equals(item.status) && !"CONDITIONAL".equals(item.status)) {
                cs.nextDue = item;
            }
            cs.items.add(item);
        }
        cs.overallStatus = cs.overdueCount > 0 ? "delayed" : cs.dueCount > 0 ? "due" : "up-to-date";
        return cs;
    }

    private static boolean appliesTo(VaccineSchedule s, Baby baby) {
        if (s.getTargetGender() == null || s.getTargetGender().isBlank()) return true;
        return baby.getGender() != null && s.getTargetGender().trim().equalsIgnoreCase(baby.getGender().name());
    }

    /**
     * A dose counts as given if it was recorded against this schedule row, or against a deactivated
     * (older schedule) row of the same vaccine family at the same age, e.g. old "DTP dose 1 at 60 days"
     * satisfies "Pentavalent dose 1 at 60 days".
     */
    private static BabyVaccination findGiven(VaccineSchedule s, List<BabyVaccination> given) {
        for (BabyVaccination v : given) {
            if (v.getSchedule() != null && s.getScheduleId() != null && s.getScheduleId().equals(v.getSchedule().getScheduleId())) {
                return v;
            }
        }
        String family = family(s.getVaccineName());
        for (BabyVaccination v : given) {
            VaccineSchedule old = v.getSchedule();
            if (old == null || old.isCurrent()) continue;
            if (family != null && family.equals(family(old.getVaccineName()))
                    && old.getRecommendedAgeDays() != null
                    && old.getRecommendedAgeDays().equals(s.getRecommendedAgeDays())) {
                return v;
            }
        }
        return null;
    }

    static String family(String vaccineName) {
        if (vaccineName == null) return null;
        String n = vaccineName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        if (n.contains("pentavalent") || n.contains("dtp") || n.contains("dpt") || n.contains("hib") || n.contains("hepatitisb")) return "DTP";
        if (n.contains("opv") || n.contains("oralpolio")) return "OPV";
        if (n.contains("ipv")) return "IPV";
        if (n.contains("measles") || n.contains("mmr")) return "MMR";
        if (n.contains("japanese") || n.contains("je")) return "JE";
        if (n.contains("bcg")) return "BCG";
        if (n.contains("hpv")) return "HPV";
        if (n.contains("atd")) return "ATD";
        if (n.startsWith("dt")) return "DT";
        return n;
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
