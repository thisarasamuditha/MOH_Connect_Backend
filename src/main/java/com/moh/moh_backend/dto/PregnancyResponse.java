package com.moh.moh_backend.dto;

import com.moh.moh_backend.model.Pregnancy;
import java.time.LocalDate;

public class PregnancyResponse {
    public Integer pregnancyId;
    public Integer motherId;
    public String pregnancyNumber;
    public LocalDate lmpDate;
    public LocalDate eddDate;
    public LocalDate deliveryDate;
    public String deliveryType;
    public String pregnancyStatus;
    public Integer gravida;
    public Integer para;
    public String riskLevel;
    public String riskFactors;

    public static PregnancyResponse from(Pregnancy p) {
        PregnancyResponse dto = new PregnancyResponse();
        dto.pregnancyId = p.getPregnancyId();
        dto.motherId = p.getMother() != null ? p.getMother().getMotherId() : null;
        dto.pregnancyNumber = p.getPregnancyNumber();
        dto.lmpDate = p.getLmpDate();
        dto.eddDate = p.getEddDate();
        dto.deliveryDate = p.getDeliveryDate();
        dto.deliveryType = p.getDeliveryType() != null ? p.getDeliveryType().name() : null;
        dto.pregnancyStatus = p.getPregnancyStatus() != null ? p.getPregnancyStatus().name() : null;
        dto.gravida = p.getGravida();
        dto.para = p.getPara();
        dto.riskLevel = p.getRiskLevel() != null ? p.getRiskLevel().name() : null;
        dto.riskFactors = p.getRiskFactors();
        return dto;
    }
}
