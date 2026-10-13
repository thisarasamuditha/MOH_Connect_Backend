package com.moh.moh_backend.service;

import com.moh.moh_backend.Enum.VisitRequestStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.moh.moh_backend.Enum.VisitRequestStatus.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VisitRequestTransitionTests {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 13, 10, 0);
    private static final LocalDateTime TOMORROW = NOW.plusDays(1);

    private static void ok(VisitRequestStatus from, VisitRequestStatus to, LocalDateTime at, String response) {
        assertThatCode(() -> VisitRequestService.validateTransition(from, to, at, response, NOW)).doesNotThrowAnyException();
    }

    @Test
    void normalFlowIsAllowed() {
        ok(PENDING, ACKNOWLEDGED, null, null);
        ok(ACKNOWLEDGED, SCHEDULED, TOMORROW, null);
        ok(SCHEDULED, SCHEDULED, TOMORROW.plusDays(1), "Rescheduled");
        ok(SCHEDULED, COMPLETED, null, null);
        ok(PENDING, COMPLETED, null, "Visited the same day");
        ok(PENDING, DECLINED, null, "Please attend Thursday's clinic instead");
    }

    @Test
    void closedRequestsCannotChange() {
        for (VisitRequestStatus closed : new VisitRequestStatus[]{COMPLETED, DECLINED, CANCELLED}) {
            assertThatThrownBy(() -> VisitRequestService.validateTransition(closed, ACKNOWLEDGED, null, null, NOW))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void schedulingNeedsAFutureTime() {
        assertThatThrownBy(() -> VisitRequestService.validateTransition(PENDING, SCHEDULED, null, null, NOW))
                .hasMessageContaining("date and time");
        assertThatThrownBy(() -> VisitRequestService.validateTransition(PENDING, SCHEDULED, NOW.minusHours(1), null, NOW))
                .hasMessageContaining("past");
    }

    @Test
    void decliningNeedsAReason() {
        assertThatThrownBy(() -> VisitRequestService.validateTransition(PENDING, DECLINED, null, "  ", NOW))
                .hasMessageContaining("reason");
    }

    @Test
    void midwifeCannotSetPendingOrCancelledOrReAcknowledge() {
        assertThatThrownBy(() -> VisitRequestService.validateTransition(ACKNOWLEDGED, PENDING, null, null, NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VisitRequestService.validateTransition(PENDING, CANCELLED, null, null, NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VisitRequestService.validateTransition(SCHEDULED, ACKNOWLEDGED, null, null, NOW))
                .isInstanceOf(IllegalStateException.class);
    }
}
