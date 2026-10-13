package com.moh.moh_backend.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Requests and responses for closing a pregnancy (delivery, miscarriage, termination). */
public class PregnancyOutcomeDtos {

    public static class NewbornRequest {
        public String name;
        public String gender;          // MALE | FEMALE
        public Float birthWeight;      // kg
        public Float birthHeight;      // cm
        public String apgarScore;
        public Boolean isAlive = true; // false = stillbirth
        public String birthComplications;
    }

    public static class DeliveryRequest {
        public LocalDate deliveryDate;
        public String deliveryType;    // NORMAL | C_SECTION | ASSISTED | OTHER
        public String deliveryPlace;   // hospital / home
        public String notes;
        public List<NewbornRequest> babies = new ArrayList<>();
    }

    public static class CloseRequest {
        public String status;          // MISCARRIAGE | TERMINATED
        public LocalDate outcomeDate;
        public String reason;
    }

    public static class NewbornSummary {
        public Integer babyId;
        public String name;
        public String gender;
        public Float birthWeight;
        public Boolean isAlive;
        public Integer birthOrder;
    }

    public static class DeliveryResponse {
        public PregnancyResponse pregnancy;
        public List<NewbornSummary> babies = new ArrayList<>();
        public Integer gestationWeeks;
        public boolean paraIncremented;
    }
}
