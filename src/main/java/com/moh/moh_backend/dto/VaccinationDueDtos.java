package com.moh.moh_backend.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Child immunization status computed from the active national schedule. */
public class VaccinationDueDtos {

    /** GIVEN, OVERDUE, DUE, DUE_SOON, UPCOMING, CONDITIONAL */
    public static class Item {
        public Integer scheduleId;
        public String vaccineName;
        public Integer doseNumber;
        public Integer recommendedAgeDays;
        public String description;
        public boolean conditional;
        public LocalDate dueDate;
        public String status;
        public Integer daysOverdue;
        // Filled when GIVEN
        public Integer vaccinationId;
        public LocalDate vaccinationDate;
        public String batchNumber;
        public String manufacturer;
        public String adverseReaction;
    }

    public static class ChildSchedule {
        public Integer babyId;
        public String babyName;
        public LocalDate dateOfBirth;
        public String gender;
        /** up-to-date, due, delayed */
        public String overallStatus;
        public int givenCount;
        public int requiredCount;
        public int overdueCount;
        public int dueCount;
        public Item nextDue;
        public List<Item> items = new ArrayList<>();
    }

    /** A child with at least one overdue or due vaccine, for the midwife's defaulter list. */
    public static class Defaulter {
        public Integer babyId;
        public String babyName;
        public LocalDate dateOfBirth;
        public String gender;
        public Integer motherId;
        public String motherName;
        public String contactNumber;
        public String address;
        public String overallStatus;
        public List<Item> overdue = new ArrayList<>();
        public List<Item> due = new ArrayList<>();
    }
}
