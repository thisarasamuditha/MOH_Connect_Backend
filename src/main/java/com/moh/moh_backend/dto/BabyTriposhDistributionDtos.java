package com.moh.moh_backend.dto;

import lombok.*;

import java.time.LocalDate;

public class BabyTriposhDistributionDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Integer babyId;
        private Integer midwifeId;
        private Double quantityKg;
        private LocalDate distributionDate;
        private String remarks;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Integer distributionId;
        private Integer babyId;
        private String babyName;
        private Integer motherId;
        private Integer midwifeId;
        private String midwifeName;
        private Double quantityKg;
        private LocalDate distributionDate;
        private String remarks;
        private String createdAt;
    }
}
