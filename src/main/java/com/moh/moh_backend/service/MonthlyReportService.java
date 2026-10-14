package com.moh.moh_backend.service;

import com.moh.moh_backend.Enum.PregnancyStatus;
import com.moh.moh_backend.Enum.RiskLevel;
import com.moh.moh_backend.dto.MonthlyReportDtos;
import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.model.BabyTriposhDistribution;
import com.moh.moh_backend.model.BabyVaccination;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.MotherRecord;
import com.moh.moh_backend.model.MotherTriposhDistribution;
import com.moh.moh_backend.model.MotherVaccination;
import com.moh.moh_backend.model.PhmArea;
import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.model.TriposhStock;
import com.moh.moh_backend.model.VaccineSchedule;
import com.moh.moh_backend.repository.BabyRecordRepository;
import com.moh.moh_backend.repository.BabyRepository;
import com.moh.moh_backend.repository.BabyTriposhDistributionRepository;
import com.moh.moh_backend.repository.BabyVaccinationRepository;
import com.moh.moh_backend.repository.DoctorRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.MotherRecordRepository;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.MotherTriposhDistributionRepository;
import com.moh.moh_backend.repository.MotherVaccinationRepository;
import com.moh.moh_backend.repository.PhmAreaRepository;
import com.moh.moh_backend.repository.PregnancyRepository;
import com.moh.moh_backend.repository.TriposhStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Builds the monthly PHM area summary used for the printable/PDF report (client request 3.6).
 * Read-only: counts what is already recorded, nothing is stored.
 * Midwives and doctors get their own area; admins must pass the area.
 */
@Service
public class MonthlyReportService {

    /** Low birth weight cut-off (WHO), same as the delivery flow. */
    static final float LOW_BIRTH_WEIGHT_KG = 2.5f;

    private final PhmAreaRepository phmAreaRepository;
    private final MidwifeRepository midwifeRepository;
    private final DoctorRepository doctorRepository;
    private final MotherRepository motherRepository;
    private final PregnancyRepository pregnancyRepository;
    private final MotherRecordRepository motherRecordRepository;
    private final BabyRepository babyRepository;
    private final BabyRecordRepository babyRecordRepository;
    private final BabyVaccinationRepository babyVaccinationRepository;
    private final MotherVaccinationRepository motherVaccinationRepository;
    private final MotherTriposhDistributionRepository motherTriposhRepository;
    private final BabyTriposhDistributionRepository babyTriposhRepository;
    private final TriposhStockRepository stockRepository;
    private final VaccinationDueService vaccinationDueService;
    private final MaternalVaccinationDueService maternalVaccinationDueService;

    public MonthlyReportService(PhmAreaRepository phmAreaRepository,
                                MidwifeRepository midwifeRepository,
                                DoctorRepository doctorRepository,
                                MotherRepository motherRepository,
                                PregnancyRepository pregnancyRepository,
                                MotherRecordRepository motherRecordRepository,
                                BabyRepository babyRepository,
                                BabyRecordRepository babyRecordRepository,
                                BabyVaccinationRepository babyVaccinationRepository,
                                MotherVaccinationRepository motherVaccinationRepository,
                                MotherTriposhDistributionRepository motherTriposhRepository,
                                BabyTriposhDistributionRepository babyTriposhRepository,
                                TriposhStockRepository stockRepository,
                                VaccinationDueService vaccinationDueService,
                                MaternalVaccinationDueService maternalVaccinationDueService) {
        this.phmAreaRepository = phmAreaRepository;
        this.midwifeRepository = midwifeRepository;
        this.doctorRepository = doctorRepository;
        this.motherRepository = motherRepository;
        this.pregnancyRepository = pregnancyRepository;
        this.motherRecordRepository = motherRecordRepository;
        this.babyRepository = babyRepository;
        this.babyRecordRepository = babyRecordRepository;
        this.babyVaccinationRepository = babyVaccinationRepository;
        this.motherVaccinationRepository = motherVaccinationRepository;
        this.motherTriposhRepository = motherTriposhRepository;
        this.babyTriposhRepository = babyTriposhRepository;
        this.stockRepository = stockRepository;
        this.vaccinationDueService = vaccinationDueService;
        this.maternalVaccinationDueService = maternalVaccinationDueService;
    }

    @Transactional(readOnly = true)
    public MonthlyReportDtos.MonthlyReport build(String month, Integer requestedAreaId, Integer userId, String role) {
        YearMonth ym = parseMonth(month);
        Integer areaId = resolveArea(requestedAreaId, userId, role);
        PhmArea area = phmAreaRepository.findById(areaId)
                .orElseThrow(() -> new IllegalArgumentException("PHM area not found with id: " + areaId));
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        LocalDate today = LocalDate.now();

        MonthlyReportDtos.MonthlyReport r = new MonthlyReportDtos.MonthlyReport();
        r.phmAreaId = area.getPhmAreaId();
        r.phmAreaName = area.getAreaName();
        r.phmAreaCode = area.getAreaCode();
        r.month = ym.toString();
        r.periodStart = from;
        r.periodEnd = to;
        r.generatedOn = today;

        // Mothers and pregnancies
        List<Mother> mothers = motherRepository.findByPhmArea_PhmAreaId(areaId).stream()
                .filter(m -> !Boolean.FALSE.equals(m.getActive()))
                .toList();
        r.newMotherRegistrations = (int) mothers.stream()
                .filter(m -> inRange(firstNonNull(m.getRegistrationDate(), toDate(m.getCreatedAt())), from, to))
                .count();

        List<Pregnancy> pregnancies = pregnancyRepository.findByMother_PhmArea_PhmAreaId(areaId).stream()
                .filter(p -> p.getMother() != null && !Boolean.FALSE.equals(p.getMother().getActive()))
                .toList();
        List<Integer> pregnancyIds = pregnancies.stream().map(Pregnancy::getPregnancyId).toList();
        r.newPregnanciesRegistered = (int) pregnancies.stream()
                .filter(p -> inRange(toDate(p.getCreatedAt()), from, to))
                .count();
        List<Pregnancy> active = pregnancies.stream()
                .filter(p -> p.getPregnancyStatus() == PregnancyStatus.ACTIVE)
                .toList();
        r.activePregnancies = active.size();
        r.highRiskActivePregnancies = (int) active.stream().filter(p -> p.getRiskLevel() == RiskLevel.HIGH).count();
        r.activePregnanciesPastEdd = (int) active.stream()
                .filter(p -> p.getEddDate() != null && p.getEddDate().isBefore(today))
                .count();

        for (Pregnancy p : pregnancies) {
            LocalDate outcome = firstNonNull(p.getOutcomeDate(), p.getDeliveryDate());
            if (!inRange(outcome, from, to)) continue;
            if (p.getPregnancyStatus() == PregnancyStatus.COMPLETED) r.deliveries++;
            else if (p.getPregnancyStatus() == PregnancyStatus.MISCARRIAGE) r.miscarriages++;
            else if (p.getPregnancyStatus() == PregnancyStatus.TERMINATED) r.terminations++;
        }

        if (!pregnancyIds.isEmpty()) {
            List<MotherRecord> visits = motherRecordRepository
                    .findByPregnancy_PregnancyIdInAndRecordDateBetween(pregnancyIds, from, to);
            r.ancVisits = visits.size();
            r.mothersSeenAtAnc = (int) visits.stream()
                    .map(v -> v.getPregnancy().getPregnancyId())
                    .distinct()
                    .count();
        }

        // Children
        List<Integer> motherIds = mothers.stream().map(Mother::getMotherId).toList();
        List<Baby> babies = motherIds.isEmpty() ? List.of() : babyRepository.findByMotherIdIn(motherIds);
        List<Integer> babyIds = babies.stream().map(Baby::getBabyId).toList();
        for (Baby b : babies) {
            if (!inRange(b.getDateOfBirth(), from, to)) continue;
            r.births++;
            if (b.getBirthWeight() != null && b.getBirthWeight() < LOW_BIRTH_WEIGHT_KG) r.lowBirthWeightBirths++;
        }
        if (!babyIds.isEmpty()) {
            r.childGrowthVisits = babyRecordRepository
                    .findByBaby_BabyIdInAndRecordDateBetween(babyIds, from, to).size();
        }

        // Vaccines given in the month
        List<BabyVaccination> childDoses = babyIds.isEmpty() ? List.of()
                : babyVaccinationRepository.findByBaby_BabyIdIn(babyIds).stream()
                        .filter(v -> inRange(v.getVaccinationDate(), from, to))
                        .toList();
        r.childVaccinesGiven = childDoses.size();
        r.childVaccines = countBySchedule(childDoses, BabyVaccination::getSchedule);

        List<MotherVaccination> maternalDoses = motherIds.isEmpty() ? List.of()
                : motherVaccinationRepository.findByMotherIdIn(motherIds).stream()
                        .filter(v -> inRange(v.getVaccinationDate(), from, to))
                        .toList();
        r.maternalVaccinesGiven = maternalDoses.size();
        r.maternalVaccines = countBySchedule(maternalDoses, MotherVaccination::getSchedule);

        // Defaulters as of today (same lists the dashboards show)
        r.childVaccineDefaulters = vaccinationDueService.defaultersForArea(areaId, userId, role).size();
        r.maternalTtDueOrOverdue = maternalVaccinationDueService.defaultersForArea(areaId, userId, role).size();

        // Thriposha
        if (!pregnancyIds.isEmpty()) {
            List<MotherTriposhDistribution> issued = motherTriposhRepository
                    .findByPregnancy_PregnancyIdInAndDistributionDateBetween(pregnancyIds, from, to);
            r.thriposhaMotherIssues = issued.size();
            r.thriposhaMotherKg = round1(issued.stream().map(MotherTriposhDistribution::getQuantityKg)
                    .filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum());
        }
        if (!babyIds.isEmpty()) {
            List<BabyTriposhDistribution> issued = babyTriposhRepository
                    .findByBaby_BabyIdInAndDistributionDateBetween(babyIds, from, to);
            r.thriposhaChildIssues = issued.size();
            r.thriposhaChildKg = round1(issued.stream().map(BabyTriposhDistribution::getQuantityKg)
                    .filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum());
        }
        Double balance = stockRepository.findTotalAvailableQuantity();
        r.thriposhaStockBalanceKg = round1(balance == null ? 0 : balance);
        r.thriposhaExpiringIn30DaysKg = round1(stockRepository.findAvailableStockFifo().stream()
                .filter(s -> s.getExpiryDate() != null && !s.getExpiryDate().isAfter(today.plusDays(30)))
                .map(TriposhStock::getQuantityKg)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum());
        return r;
    }

    static YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) return YearMonth.now();
        try {
            YearMonth ym = YearMonth.parse(month.trim());
            if (ym.isAfter(YearMonth.now())) throw new IllegalArgumentException("Month cannot be in the future");
            return ym;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Month must be in the format YYYY-MM");
        }
    }

    static boolean inRange(LocalDate date, LocalDate from, LocalDate to) {
        return date != null && !date.isBefore(from) && !date.isAfter(to);
    }

    /** Doses grouped by vaccine and dose number, sorted by name then dose. */
    static <T> List<MonthlyReportDtos.VaccineCount> countBySchedule(List<T> doses, Function<T, VaccineSchedule> schedule) {
        Map<String, MonthlyReportDtos.VaccineCount> counts = new TreeMap<>();
        for (T dose : doses) {
            VaccineSchedule s = schedule.apply(dose);
            String name = s == null || s.getVaccineName() == null ? "Unknown vaccine" : s.getVaccineName();
            Integer doseNumber = s == null ? null : s.getDoseNumber();
            String key = name + "\u0000" + String.format("%03d", doseNumber == null ? 0 : doseNumber);
            counts.computeIfAbsent(key, k -> new MonthlyReportDtos.VaccineCount(name, doseNumber, 0)).count++;
        }
        List<MonthlyReportDtos.VaccineCount> result = new ArrayList<>(counts.values());
        result.sort(Comparator.comparing((MonthlyReportDtos.VaccineCount c) -> c.vaccineName)
                .thenComparing(c -> c.doseNumber == null ? 0 : c.doseNumber));
        return result;
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
        throw new IllegalStateException("Not allowed to view area reports");
    }

    private static LocalDate toDate(LocalDateTime dt) {
        return dt == null ? null : dt.toLocalDate();
    }

    private static LocalDate firstNonNull(LocalDate a, LocalDate b) {
        return a != null ? a : b;
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
