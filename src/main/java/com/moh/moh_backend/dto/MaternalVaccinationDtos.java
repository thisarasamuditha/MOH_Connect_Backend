package com.moh.moh_backend.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Maternal (pregnancy) immunization status - tetanus toxoid plus any other vaccines recorded. */
public class MaternalVaccinationDtos {

    /** One vaccine given to the mother. */
    public static class Dose {
        public Integer vaccinationId;
        public Integer pregnancyId;
        public Integer scheduleId;
        public String vaccineName;
        public Integer scheduleDoseNumber;
        /** Tetanus toxoid / Td / aTd */
        public boolean tetanus;
        /** For tetanus doses: the mother's lifetime dose number (1-5), counting her earlier history. */
        public Integer lifetimeDoseNumber;
        public LocalDate vaccinationDate;
        public String batchNumber;
        public String manufacturer;
        public String adverseReaction;
        public String givenBy;
    }

    public static class PregnancyStatus {
        public Integer pregnancyId;
        public String pregnancyNumber;
        public String pregnancyStatus;
        public Integer motherId;
        public String motherName;
        public LocalDate lmpDate;
        public LocalDate eddDate;
        public Integer gestationWeeks;

        /** False until the midwife records how many tetanus doses the mother had before (may be 0). */
        public boolean historyRecorded;
        public Integer priorTtDoses;
        public LocalDate priorTtLastDate;
        /** True if mother has completed all 6 childhood/adolescent qualifying doses (infancy, 18m, 5y, aTd). */
        public boolean childhood6Doses;
        /** True if a booster dose is indicated during pregnancy due to >= 10 years elapsed. */
        public boolean boosterIndicated;
        public boolean isBooster;

        /** Tetanus doses before this pregnancy: earlier history + doses recorded here in earlier pregnancies. */
        public int dosesBeforePregnancy;
        public LocalDate lastDoseBeforePregnancy;
        /** Tetanus doses this pregnancy needs (0, 1 or 2). */
        public int dosesNeeded;
        public int dosesGivenThisPregnancy;
        public int lifetimeDoses;

        /**
         * PROTECTED  no further tetanus dose needed in this pregnancy
         * NOT_YET    next dose is more than 14 days away
         * DUE_SOON   within 14 days before the due date
         * DUE        from the due date up to 14 days after it
         * OVERDUE    more than 14 days late, or the dose can no longer be given 2 weeks before the EDD
         * CLOSED     pregnancy is no longer active
         */
        public String status;
        public Integer nextLifetimeDoseNumber;
        /** Suggested VACCINE_SCHEDULE row for the next dose. */
        public Integer nextScheduleId;
        public LocalDate nextDueDate;
        /** The last date that still gives protection at delivery (EDD - 14 days). */
        public LocalDate latestDate;
        public Integer daysOverdue;

        /** Vaccines given in this pregnancy (tetanus and others), oldest first. */
        public List<Dose> doses = new ArrayList<>();
        /** Tetanus doses recorded in this system in earlier pregnancies. */
        public List<Dose> earlierDoses = new ArrayList<>();
    }

    /** An active pregnancy whose tetanus dose is due or overdue, for the midwife's list. */
    public static class Defaulter {
        public Integer pregnancyId;
        public String pregnancyNumber;
        public Integer motherId;
        public String motherName;
        public String contactNumber;
        public String address;
        public Integer gestationWeeks;
        public LocalDate eddDate;
        public boolean historyRecorded;
        public String status;
        public Integer nextLifetimeDoseNumber;
        public LocalDate nextDueDate;
        public Integer daysOverdue;
    }

    /** Earlier tetanus history entered by the midwife. */
    public static class TtHistoryRequest {
        public Integer priorDoses;
        public LocalDate lastDoseDate;
    }
}
