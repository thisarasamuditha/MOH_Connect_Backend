package com.moh.moh_backend.dto;

import com.moh.moh_backend.model.SectionType;
import lombok.*;

public class SectionDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SectionCreateRequest {
        private Integer midwifeId;
        private Integer phmAreaId;
        private String sectionName;
        private SectionType sectionType;
        private String description;
        private String location;
        private Integer capacity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SectionUpdateRequest {
        private String sectionName;
        private SectionType sectionType;
        private String description;
        private String location;
        private Integer capacity;
        private Boolean isActive;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SectionResponse {
        private Integer sectionId;
        private Integer midwifeId;
        private String midwifeName;
        private Integer phmAreaId;
        private String phmAreaName;
        private String sectionName;
        private SectionType sectionType;
        private String description;
        private String location;
        private Integer capacity;
        private Boolean isActive;
        private String createdAt;
        private String updatedAt;
    }
}
