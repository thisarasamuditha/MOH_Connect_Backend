package com.moh.moh_backend.service;

import com.moh.moh_backend.Enum.RiskLevel;
import com.moh.moh_backend.model.Pregnancy;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PregnancyValidationTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 13);

    private static Pregnancy pregnancy(LocalDate lmp, LocalDate edd, Integer gravida, Integer para) {
        Pregnancy p = new Pregnancy();
        p.setLmpDate(lmp);
        p.setEddDate(edd);
        p.setGravida(gravida);
        p.setPara(para);
        p.setRiskLevel(null);
        return p;
    }

    @Test
    void fillsEddAndDefaultsWhenMissing() {
        Pregnancy p = pregnancy(TODAY.minusWeeks(10), null, null, null);
        PregnancyService.validateNewPregnancy(p, TODAY);
        assertThat(p.getEddDate()).isEqualTo(TODAY.minusWeeks(10).plusDays(280));
        assertThat(p.getGravida()).isEqualTo(1);
        assertThat(p.getPara()).isEqualTo(0);
        assertThat(p.getRiskLevel()).isEqualTo(RiskLevel.LOW);
    }

    @Test
    void acceptsEddWithin21Days() {
        LocalDate lmp = TODAY.minusWeeks(10);
        Pregnancy p = pregnancy(lmp, lmp.plusDays(280 + 21), 2, 1);
        PregnancyService.validateNewPregnancy(p, TODAY);
        assertThat(p.getEddDate()).isEqualTo(lmp.plusDays(301));
    }

    @Test
    void rejectsEddMoreThan21DaysOff() {
        LocalDate lmp = TODAY.minusWeeks(10);
        assertThatThrownBy(() -> PregnancyService.validateNewPregnancy(pregnancy(lmp, lmp.plusDays(258), 1, 0), TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EDD");
    }

    @Test
    void rejectsMissingFutureOrTooOldLmp() {
        assertThatThrownBy(() -> PregnancyService.validateNewPregnancy(pregnancy(null, null, 1, 0), TODAY))
                .hasMessageContaining("required");
        assertThatThrownBy(() -> PregnancyService.validateNewPregnancy(pregnancy(TODAY.plusDays(1), null, 1, 0), TODAY))
                .hasMessageContaining("future");
        assertThatThrownBy(() -> PregnancyService.validateNewPregnancy(pregnancy(TODAY.minusWeeks(43), null, 1, 0), TODAY))
                .hasMessageContaining("42 weeks");
    }

    @Test
    void rejectsInvalidGravidaPara() {
        LocalDate lmp = TODAY.minusWeeks(8);
        assertThatThrownBy(() -> PregnancyService.validateNewPregnancy(pregnancy(lmp, null, 0, 0), TODAY))
                .hasMessageContaining("Gravida");
        assertThatThrownBy(() -> PregnancyService.validateNewPregnancy(pregnancy(lmp, null, 2, 2), TODAY))
                .hasMessageContaining("Para");
        assertThatThrownBy(() -> PregnancyService.validateNewPregnancy(pregnancy(lmp, null, 2, -1), TODAY))
                .hasMessageContaining("Para");
    }
}
