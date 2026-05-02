package com.moh.moh_backend.model;

public enum SectionType {
    HEALTH_CENTER("Health Center"),
    CLINIC("Clinic"),
    MOBILE_UNIT("Mobile Unit"),
    MATERNAL_HEALTH("Maternal Health Center"),
    CHILD_HEALTH("Child Health Center"),
    IMMUNIZATION("Immunization Center"),
    COMMUNITY_HEALTH("Community Health Post"),
    SATELLITE_CLINIC("Satellite Clinic"),
    OTHER("Other");

    private final String displayName;

    SectionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
