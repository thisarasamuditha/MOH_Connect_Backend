package com.moh.moh_backend.dto;

import lombok.*;

import java.time.LocalDate;

public class MotherTriposhDistributionDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Integer pregnancyId;
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
        private Integer pregnancyId;
        private String pregnancyNumber;
        private Integer motherId;
        private String motherName;
        private Integer midwifeId;
        private String midwifeName;
        private Double quantityKg;
        private LocalDate distributionDate;
        private String remarks;
        private String createdAt;
    }
}
