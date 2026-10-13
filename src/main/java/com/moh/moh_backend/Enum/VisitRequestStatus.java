package com.moh.moh_backend.Enum;

public enum VisitRequestStatus {
    PENDING, ACKNOWLEDGED, SCHEDULED, COMPLETED, DECLINED, CANCELLED;

    /** Still waiting for the midwife to finish it. */
    public boolean isOpen() {
        return this == PENDING || this == ACKNOWLEDGED || this == SCHEDULED;
    }
}
