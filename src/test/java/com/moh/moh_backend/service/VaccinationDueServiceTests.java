package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.VaccinationDueDtos;
import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.model.BabyVaccination;
import com.moh.moh_backend.model.TargetGroup;
import com.moh.moh_backend.model.VaccineSchedule;
import com.moh.moh_backend.repository.BabyRepository;
import com.moh.moh_backend.repository.BabyVaccinationRepository;
import com.moh.moh_backend.repository.DoctorRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.MotherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VaccinationDueServiceTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 8);
    private VaccinationDueService service;
    private VaccineSchedule bcg, penta1, bcg2, hpv1;

    private static VaccineSchedule row(int id, String name, int dose, int days, boolean active) {
        VaccineSchedule s = new VaccineSchedule();
        s.setScheduleId(id);
        s.setVaccineName(name);
        s.setDoseNumber(dose);
        s.setRecommendedAgeDays(days);
        s.setTargetGroup(TargetGroup.BABY);
        s.setIsActive(active);
        s.setIsConditional(false);
        return s;
    }

    private static Baby baby(LocalDate dob, Baby.Gender gender) {
        Baby b = new Baby();
        b.setBabyId(1);
        b.setName("Test");
        b.setDateOfBirth(dob);
        b.setGender(gender);
        return b;
    }

    private static BabyVaccination given(VaccineSchedule s, LocalDate on) {
        BabyVaccination v = new BabyVaccination();
        v.setVaccinationId(s.getScheduleId() * 10);
        v.setSchedule(s);
        v.setVaccinationDate(on);
        return v;
    }

    @BeforeEach
    void setUp() {
        bcg = row(1, "BCG", 1, 0, true);
        penta1 = row(2, "Pentavalent (DTP-HepB-Hib)", 1, 60, true);
        bcg2 = row(3, "BCG", 2, 180, true);
        bcg2.setIsConditional(true);
        hpv1 = row(4, "HPV", 1, 3650, true);
        hpv1.setTargetGender("FEMALE");

        VaccineScheduleService schedules = mock(VaccineScheduleService.class);
        when(schedules.getSchedulesByTargetGroup(TargetGroup.BABY)).thenReturn(List.of(bcg, penta1, bcg2, hpv1));
        service = new VaccinationDueService(schedules, mock(BabyVaccinationRepository.class), mock(BabyRepository.class),
                mock(MotherRepository.class), mock(MidwifeRepository.class), mock(DoctorRepository.class));
    }

    private VaccinationDueDtos.Item item(VaccinationDueDtos.ChildSchedule cs, int scheduleId) {
        return cs.items.stream().filter(i -> i.scheduleId == scheduleId).findFirst().orElse(null);
    }

    @Test
    void statusWindowsAroundDueDate() {
        // Pentavalent 1 is due at 60 days
        assertEquals("UPCOMING", item(service.evaluate(baby(TODAY.minusDays(30), Baby.Gender.MALE), List.of(), TODAY), 2).status);
        assertEquals("DUE_SOON", item(service.evaluate(baby(TODAY.minusDays(50), Baby.Gender.MALE), List.of(), TODAY), 2).status);
        assertEquals("DUE", item(service.evaluate(baby(TODAY.minusDays(70), Baby.Gender.MALE), List.of(), TODAY), 2).status);
        VaccinationDueDtos.Item late = item(service.evaluate(baby(TODAY.minusDays(90), Baby.Gender.MALE), List.of(), TODAY), 2);
        assertEquals("OVERDUE", late.status);
        assertEquals(30, late.daysOverdue);
    }

    @Test
    void givenDosesAndOverallStatus() {
        Baby b = baby(TODAY.minusDays(90), Baby.Gender.MALE);
        VaccinationDueDtos.ChildSchedule cs = service.evaluate(b, List.of(given(bcg, b.getDateOfBirth())), TODAY);
        assertEquals("GIVEN", item(cs, 1).status);
        assertEquals("delayed", cs.overallStatus);
        assertEquals(1, cs.overdueCount);
        assertEquals(2, cs.nextDue.scheduleId);

        cs = service.evaluate(b, List.of(given(bcg, b.getDateOfBirth()), given(penta1, b.getDateOfBirth().plusDays(61))), TODAY);
        assertEquals("up-to-date", cs.overallStatus);
        assertEquals(2, cs.givenCount);
    }

    @Test
    void conditionalDoseIsNeverOverdue() {
        VaccinationDueDtos.ChildSchedule cs = service.evaluate(baby(TODAY.minusDays(400), Baby.Gender.MALE), List.of(), TODAY);
        assertEquals("CONDITIONAL", item(cs, 3).status);
    }

    @Test
    void genderSpecificDosesOnlyForThatSex() {
        assertNull(item(service.evaluate(baby(TODAY.minusDays(30), Baby.Gender.MALE), List.of(), TODAY), 4));
        assertNotNull(item(service.evaluate(baby(TODAY.minusDays(30), Baby.Gender.FEMALE), List.of(), TODAY), 4));
    }

    @Test
    void doseRecordedAgainstOldScheduleRowCountsForSameFamilyAndAge() {
        VaccineSchedule oldDtp = row(99, "DTP", 1, 60, false);
        Baby b = baby(TODAY.minusDays(90), Baby.Gender.MALE);
        VaccinationDueDtos.ChildSchedule cs = service.evaluate(b, List.of(given(oldDtp, b.getDateOfBirth().plusDays(60))), TODAY);
        assertEquals("GIVEN", item(cs, 2).status);
    }

    @Test
    void vaccineFamilies() {
        assertEquals("DTP", VaccinationDueService.family("Pentavalent (DTP-HepB-Hib)"));
        assertEquals("OPV", VaccinationDueService.family("OPV (Oral Polio Vaccine)"));
        assertEquals("IPV", VaccinationDueService.family("fIPV"));
        assertEquals("MMR", VaccinationDueService.family("Measles"));
        assertEquals("JE", VaccinationDueService.family("Live JE"));
        assertEquals("DT", VaccinationDueService.family("DT (Diphtheria-Tetanus)"));
        assertEquals("ATD", VaccinationDueService.family("aTd"));
    }
}
