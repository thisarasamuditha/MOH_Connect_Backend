package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.MonthlyReportDtos;
import com.moh.moh_backend.model.VaccineSchedule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class MonthlyReportServiceTests {

    @Test
    void parsesMonthAndDefaultsToCurrent() {
        assertEquals(YearMonth.of(2026, 9), MonthlyReportService.parseMonth("2026-09"));
        assertEquals(YearMonth.now(), MonthlyReportService.parseMonth(null));
        assertEquals(YearMonth.now(), MonthlyReportService.parseMonth(" "));
    }

    @Test
    void rejectsBadOrFutureMonth() {
        assertThrows(IllegalArgumentException.class, () -> MonthlyReportService.parseMonth("09/2026"));
        assertThrows(IllegalArgumentException.class, () -> MonthlyReportService.parseMonth("2026-13"));
        String future = YearMonth.now().plusMonths(1).toString();
        assertThrows(IllegalArgumentException.class, () -> MonthlyReportService.parseMonth(future));
    }

    @Test
    void rangeIncludesBothEnds() {
        LocalDate from = LocalDate.of(2026, 9, 1);
        LocalDate to = LocalDate.of(2026, 9, 30);
        assertTrue(MonthlyReportService.inRange(from, from, to));
        assertTrue(MonthlyReportService.inRange(to, from, to));
        assertFalse(MonthlyReportService.inRange(LocalDate.of(2026, 8, 31), from, to));
        assertFalse(MonthlyReportService.inRange(LocalDate.of(2026, 10, 1), from, to));
        assertFalse(MonthlyReportService.inRange(null, from, to));
    }

    @Test
    void countsDosesPerVaccineAndDose() {
        VaccineSchedule bcg = schedule("BCG", 1);
        VaccineSchedule penta1 = schedule("Pentavalent", 1);
        VaccineSchedule penta2 = schedule("Pentavalent", 2);
        List<VaccineSchedule> doses = List.of(penta2, bcg, penta1, penta1, bcg, bcg);

        List<MonthlyReportDtos.VaccineCount> counts =
                MonthlyReportService.countBySchedule(doses, Function.identity());

        assertEquals(3, counts.size());
        assertEquals("BCG", counts.get(0).vaccineName);
        assertEquals(3, counts.get(0).count);
        assertEquals("Pentavalent", counts.get(1).vaccineName);
        assertEquals(1, counts.get(1).doseNumber);
        assertEquals(2, counts.get(1).count);
        assertEquals(2, counts.get(2).doseNumber);
        assertEquals(1, counts.get(2).count);
    }

    private static VaccineSchedule schedule(String name, int dose) {
        VaccineSchedule s = new VaccineSchedule();
        s.setVaccineName(name);
        s.setDoseNumber(dose);
        return s;
    }
}
