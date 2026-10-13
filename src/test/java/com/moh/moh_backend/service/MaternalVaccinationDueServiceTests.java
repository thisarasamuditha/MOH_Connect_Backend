package com.moh.moh_backend.service;

import com.moh.moh_backend.Enum.PregnancyStatus;
import com.moh.moh_backend.Enum.TargetGroup;
import com.moh.moh_backend.dto.MaternalVaccinationDtos;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.MotherVaccination;
import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.model.VaccineSchedule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaternalVaccinationDueServiceTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 14);

    private final MaternalVaccinationDueService service =
            new MaternalVaccinationDueService(null, null, null, null, null, null);

    private static final VaccineSchedule TT1 = schedule(1, "Tetanus Toxoid (TT)", 1);
    private static final VaccineSchedule TT2 = schedule(2, "Tetanus Toxoid (TT)", 2);
    private static final VaccineSchedule TT3 = schedule(3, "Tetanus Toxoid (TT)", 3);
    private static final VaccineSchedule FLU = schedule(4, "Influenza", 1);
    private static final List<VaccineSchedule> SCHEDULES = List.of(TT1, TT2, TT3, FLU);

    private static VaccineSchedule schedule(int id, String name, int dose) {
        VaccineSchedule s = new VaccineSchedule();
        s.setScheduleId(id);
        s.setVaccineName(name);
        s.setDoseNumber(dose);
        s.setTargetGroup(TargetGroup.MOTHER);
        s.setIsActive(true);
        return s;
    }

    private static Mother mother(Integer priorDoses, LocalDate priorLast) {
        Mother m = new Mother();
        m.setMotherId(7);
        m.setName("Test Mother");
        m.setPriorTtDoses(priorDoses);
        m.setPriorTtLastDate(priorLast);
        return m;
    }

    private static Pregnancy pregnancy(int id, Mother m, LocalDate lmp) {
        Pregnancy p = new Pregnancy();
        p.setPregnancyId(id);
        p.setMother(m);
        p.setLmpDate(lmp);
        p.setEddDate(lmp.plusDays(280));
        p.setPregnancyStatus(PregnancyStatus.ACTIVE);
        return p;
    }

    private static MotherVaccination dose(int id, Pregnancy p, VaccineSchedule s, LocalDate date) {
        MotherVaccination v = new MotherVaccination();
        v.setVaccinationId(id);
        v.setPregnancy(p);
        v.setSchedule(s);
        v.setVaccinationDate(date);
        return v;
    }

    private MaternalVaccinationDtos.PregnancyStatus eval(Pregnancy p, List<MotherVaccination> doses) {
        return service.evaluate(p, p.getMother(), doses, SCHEDULES, TODAY);
    }

    @Test
    void noHistoryNeedsTwoDosesFromTwelveWeeks() {
        Pregnancy p = pregnancy(1, mother(null, null), TODAY.minusWeeks(8));
        MaternalVaccinationDtos.PregnancyStatus s = eval(p, List.of());
        assertFalse(s.historyRecorded);
        assertEquals(2, s.dosesNeeded);
        assertEquals(p.getLmpDate().plusWeeks(12), s.nextDueDate);
        assertEquals("NOT_YET", s.status);
        assertEquals(1, s.nextLifetimeDoseNumber);
        assertEquals(1, s.nextScheduleId);
    }

    @Test
    void statusWindowsAroundTheDueDate() {
        assertEquals("DUE_SOON", eval(pregnancy(1, mother(0, null), TODAY.minusWeeks(11)), List.of()).status);
        assertEquals("DUE", eval(pregnancy(1, mother(0, null), TODAY.minusWeeks(13)), List.of()).status);
        MaternalVaccinationDtos.PregnancyStatus late = eval(pregnancy(1, mother(0, null), TODAY.minusWeeks(16)), List.of());
        assertEquals("OVERDUE", late.status);
        assertEquals(28, late.daysOverdue);
    }

    @Test
    void secondDoseSixWeeksAfterTheFirst() {
        Pregnancy p = pregnancy(1, mother(0, null), TODAY.minusWeeks(20));
        LocalDate first = TODAY.minusWeeks(2);
        MaternalVaccinationDtos.PregnancyStatus s = eval(p, List.of(dose(10, p, TT1, first)));
        assertEquals(first.plusDays(42), s.nextDueDate);
        assertEquals(2, s.nextLifetimeDoseNumber);
        assertEquals(2, s.nextScheduleId);
        assertEquals("NOT_YET", s.status);
        assertEquals(1, s.doses.get(0).lifetimeDoseNumber);
    }

    @Test
    void twoDosesThisPregnancyIsProtected() {
        Pregnancy p = pregnancy(1, mother(0, null), TODAY.minusWeeks(30));
        MaternalVaccinationDtos.PregnancyStatus s = eval(p, List.of(
                dose(10, p, TT1, TODAY.minusWeeks(16)), dose(11, p, TT2, TODAY.minusWeeks(10))));
        assertEquals("PROTECTED", s.status);
        assertEquals(2, s.lifetimeDoses);
        assertNull(s.nextDueDate);
    }

    @Test
    void boosterDependsOnHowLongTheLastDoseProtects() {
        LocalDate lmp = TODAY.minusWeeks(14);
        LocalDate edd = lmp.plusDays(280);
        // 2 earlier doses protect for 3 years
        assertEquals("PROTECTED", eval(pregnancy(1, mother(2, edd.minusYears(2)), lmp), List.of()).status);
        MaternalVaccinationDtos.PregnancyStatus old = eval(pregnancy(1, mother(2, edd.minusYears(4)), lmp), List.of());
        assertEquals(1, old.dosesNeeded);
        assertEquals(3, old.nextLifetimeDoseNumber);
        assertEquals(3, old.nextScheduleId);
        // Count known but date unknown: give the booster
        assertEquals(1, eval(pregnancy(1, mother(3, null), lmp), List.of()).dosesNeeded);
        // Full course
        assertEquals("PROTECTED", eval(pregnancy(1, mother(5, null), lmp), List.of()).status);
    }

    @Test
    void dosesFromAnEarlierPregnancyCount() {
        Mother m = mother(0, null);
        Pregnancy previous = pregnancy(1, m, TODAY.minusYears(2));
        previous.setPregnancyStatus(PregnancyStatus.COMPLETED);
        Pregnancy current = pregnancy(2, m, TODAY.minusWeeks(14));
        List<MotherVaccination> doses = List.of(
                dose(10, previous, TT1, TODAY.minusYears(2).plusWeeks(13)),
                dose(11, previous, TT2, TODAY.minusYears(2).plusWeeks(19)));
        MaternalVaccinationDtos.PregnancyStatus s = eval(current, doses);
        assertEquals(2, s.dosesBeforePregnancy);
        assertEquals(2, s.earlierDoses.size());
        assertEquals("PROTECTED", s.status);
    }

    @Test
    void lateRegistrationIsOverdueAndSecondDoseIsBroughtForward() {
        Pregnancy late = pregnancy(1, mother(0, null), TODAY.minusWeeks(37));
        assertEquals("OVERDUE", eval(late, List.of()).status);

        Pregnancy p = pregnancy(2, mother(0, null), TODAY.minusWeeks(35));
        LocalDate first = TODAY.minusWeeks(1);
        MaternalVaccinationDtos.PregnancyStatus s = eval(p, List.of(dose(10, p, TT1, first)));
        // 6 weeks would be after EDD - 14 days, so the 2nd dose moves to the cut-off (still >= 4 weeks)
        assertEquals(first.plusDays(28).isAfter(s.latestDate) ? first.plusDays(28) : s.latestDate, s.nextDueDate);
        assertTrue(!s.nextDueDate.isBefore(first.plusDays(28)));
    }

    @Test
    void closedPregnancyAndOtherVaccines() {
        Pregnancy p = pregnancy(1, mother(0, null), TODAY.minusWeeks(20));
        MaternalVaccinationDtos.PregnancyStatus s = eval(p, List.of(dose(10, p, FLU, TODAY.minusWeeks(1))));
        assertEquals(0, s.dosesGivenThisPregnancy);
        assertEquals(1, s.doses.size());
        assertFalse(s.doses.get(0).tetanus);

        p.setPregnancyStatus(PregnancyStatus.COMPLETED);
        assertEquals("CLOSED", eval(p, List.of()).status);
    }

    @Test
    void recognisesTetanusVaccines() {
        assertTrue(MaternalVaccinationDueService.isTetanus(TT1));
        assertTrue(MaternalVaccinationDueService.isTetanus(schedule(9, "aTd", 1)));
        assertTrue(MaternalVaccinationDueService.isTetanus(schedule(9, "TT", 1)));
        assertFalse(MaternalVaccinationDueService.isTetanus(FLU));
        assertFalse(MaternalVaccinationDueService.isTetanus(schedule(9, "COVID-19", 1)));
        VaccineSchedule babyDt = schedule(9, "DT", 5);
        babyDt.setTargetGroup(TargetGroup.BABY);
        assertFalse(MaternalVaccinationDueService.isTetanus(babyDt));
    }

    // ── Recording rules (MotherVaccinationService.validateDose) ──

    @Test
    void recordingRejectsBadDates() {
        Pregnancy p = pregnancy(1, mother(0, null), TODAY.minusWeeks(20));
        assertThrows(IllegalArgumentException.class, () -> MotherVaccinationService.validateDose(null, TT1, p, List.of(), TODAY));
        assertThrows(IllegalArgumentException.class, () -> MotherVaccinationService.validateDose(TODAY.plusDays(1), TT1, p, List.of(), TODAY));
        assertThrows(IllegalArgumentException.class, () -> MotherVaccinationService.validateDose(p.getLmpDate().minusDays(1), TT1, p, List.of(), TODAY));
        assertDoesNotThrow(() -> MotherVaccinationService.validateDose(TODAY, TT1, p, List.of(), TODAY));
    }

    @Test
    void recordingRejectsDuplicatesShortIntervalsAndExtraDoses() {
        Pregnancy p = pregnancy(1, mother(0, null), TODAY.minusWeeks(20));
        List<MotherVaccination> given = new ArrayList<>(List.of(dose(10, p, TT1, TODAY.minusWeeks(3))));
        // Same schedule entry twice in one pregnancy
        assertThrows(IllegalArgumentException.class, () -> MotherVaccinationService.validateDose(TODAY, TT1, p, given, TODAY));
        // Less than 4 weeks after the previous tetanus dose
        assertThrows(IllegalArgumentException.class, () -> MotherVaccinationService.validateDose(TODAY, TT2, p, given, TODAY));
        // Influenza is not limited by the tetanus interval
        assertDoesNotThrow(() -> MotherVaccinationService.validateDose(TODAY, FLU, p, given, TODAY));

        Pregnancy full = pregnancy(2, mother(5, null), TODAY.minusWeeks(20));
        assertThrows(IllegalArgumentException.class, () -> MotherVaccinationService.validateDose(TODAY, TT1, full, List.of(), TODAY));

        Pregnancy recentHistory = pregnancy(3, mother(2, TODAY.minusWeeks(2)), TODAY.minusWeeks(20));
        assertThrows(IllegalArgumentException.class, () -> MotherVaccinationService.validateDose(TODAY, TT3, recentHistory, List.of(), TODAY));
    }
}
