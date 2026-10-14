package com.moh.moh_backend.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Monthly PHM area summary (client request 3.6, PDF export). All counts are for one area. */
public class MonthlyReportDtos {

    public static class MonthlyReport {
        public Integer phmAreaId;
        public String phmAreaName;
        public String phmAreaCode;
        /** yyyy-MM */
        public String month;
        public LocalDate periodStart;
        public LocalDate periodEnd;
        public LocalDate generatedOn;

        // Mothers and pregnancies
        public int newMotherRegistrations;
        public int newPregnanciesRegistered;
        /** As of today, not of the month end (the system keeps no status history). */
        public int activePregnancies;
        public int highRiskActivePregnancies;
        public int activePregnanciesPastEdd;
        public int ancVisits;
        public int mothersSeenAtAnc;

        // Outcomes in the month
        public int deliveries;
        public int miscarriages;
        public int terminations;

        // Children
        public int births;
        public int lowBirthWeightBirths;
        public int childGrowthVisits;

        // Vaccines given in the month
        public int childVaccinesGiven;
        public int maternalVaccinesGiven;
        public List<VaccineCount> childVaccines = new ArrayList<>();
        public List<VaccineCount> maternalVaccines = new ArrayList<>();

        // Defaulters (as of today)
        public int childVaccineDefaulters;
        public int maternalTtDueOrOverdue;

        // Thriposha
        public double thriposhaMotherKg;
        public double thriposhaChildKg;
        public int thriposhaMotherIssues;
        public int thriposhaChildIssues;
        /** Stock is held centrally (not per area). */
        public double thriposhaStockBalanceKg;
        public double thriposhaExpiringIn30DaysKg;
    }

    public static class VaccineCount {
        public String vaccineName;
        public Integer doseNumber;
        public int count;

        public VaccineCount(String vaccineName, Integer doseNumber, int count) {
            this.vaccineName = vaccineName;
            this.doseNumber = doseNumber;
            this.count = count;
        }
    }
}
