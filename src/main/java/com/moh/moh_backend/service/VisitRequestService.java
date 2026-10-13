package com.moh.moh_backend.service;

import com.moh.moh_backend.Enum.VisitRequestStatus;
import com.moh.moh_backend.Enum.VisitRequestType;
import com.moh.moh_backend.Enum.VisitRequestUrgency;
import com.moh.moh_backend.dto.VisitRequestDtos.CreateRequest;
import com.moh.moh_backend.dto.VisitRequestDtos.MidwifeContact;
import com.moh.moh_backend.dto.VisitRequestDtos.OpenCount;
import com.moh.moh_backend.dto.VisitRequestDtos.RespondRequest;
import com.moh.moh_backend.dto.VisitRequestDtos.Response;
import com.moh.moh_backend.model.Midwife;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.VisitRequest;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.VisitRequestRepository;
import com.moh.moh_backend.util.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Client request 3.1 - mothers ask their PHM (the midwives of their PHM area) for a special
 * home visit or clinic consultation; URGENT requests are flagged to the midwives straight away.
 */
@Service
@RequiredArgsConstructor
public class VisitRequestService {

    static final int MAX_REASON_LENGTH = 2000;
    static final int MAX_OPEN_REQUESTS_PER_MOTHER = 5;
    private static final EnumSet<VisitRequestStatus> OPEN = EnumSet.of(
            VisitRequestStatus.PENDING, VisitRequestStatus.ACKNOWLEDGED, VisitRequestStatus.SCHEDULED);

    private final VisitRequestRepository visitRequestRepository;
    private final MotherRepository motherRepository;
    private final MidwifeRepository midwifeRepository;
    private final EmailService emailService;

    // ── Mother ────────────────────────────────────────────

    @Transactional
    public Response create(CreateRequest req, Integer motherUserId) {
        Mother mother = currentMother(motherUserId);
        if (mother.getPhmArea() == null) {
            throw new IllegalStateException("Your account has no PHM area assigned. Please contact your clinic.");
        }

        VisitRequestType type = parseEnum(VisitRequestType.class, req.getRequestType(), "request type");
        VisitRequestUrgency urgency = req.getUrgency() == null || req.getUrgency().isBlank()
                ? VisitRequestUrgency.ROUTINE
                : parseEnum(VisitRequestUrgency.class, req.getUrgency(), "urgency");
        String reason = req.getReason() == null ? "" : req.getReason().trim();
        if (reason.isEmpty()) {
            throw new IllegalArgumentException("Please describe the reason for the request");
        }
        if (reason.length() > MAX_REASON_LENGTH) {
            throw new IllegalArgumentException("Reason must be at most " + MAX_REASON_LENGTH + " characters");
        }
        if (req.getPreferredDate() != null && req.getPreferredDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Preferred date cannot be in the past");
        }
        long open = visitRequestRepository.findByMother_MotherIdOrderByCreatedAtDesc(mother.getMotherId())
                .stream().filter(r -> r.getStatus().isOpen()).count();
        // Never block an urgent request; only stop a pile-up of routine ones
        if (urgency == VisitRequestUrgency.ROUTINE && open >= MAX_OPEN_REQUESTS_PER_MOTHER) {
            throw new IllegalStateException("You already have " + open
                    + " open requests. Please wait for your PHM to respond or cancel one first.");
        }

        String contact = req.getContactNumber() != null && !req.getContactNumber().isBlank()
                ? req.getContactNumber().trim()
                : mother.getContactNumber();

        VisitRequest saved = visitRequestRepository.save(VisitRequest.builder()
                .mother(mother)
                .phmArea(mother.getPhmArea())
                .requestType(type)
                .urgency(urgency)
                .reason(reason)
                .preferredDate(req.getPreferredDate())
                .contactNumber(contact)
                .status(VisitRequestStatus.PENDING)
                .build());

        if (urgency == VisitRequestUrgency.URGENT) {
            alertAreaMidwives(saved);
        }
        return Response.from(saved);
    }

    @Transactional(readOnly = true)
    public List<Response> getMine(Integer motherUserId) {
        Mother mother = currentMother(motherUserId);
        return visitRequestRepository.findByMother_MotherIdOrderByCreatedAtDesc(mother.getMotherId())
                .stream().map(Response::from).toList();
    }

    @Transactional
    public Response cancel(Integer requestId, Integer motherUserId) {
        Mother mother = currentMother(motherUserId);
        VisitRequest r = find(requestId);
        if (!Objects.equals(r.getMother().getMotherId(), mother.getMotherId())) {
            throw new IllegalStateException("You can only cancel your own requests");
        }
        if (!r.getStatus().isOpen()) {
            throw new IllegalStateException("This request is already " + r.getStatus().name().toLowerCase(Locale.ROOT));
        }
        r.setStatus(VisitRequestStatus.CANCELLED);
        r.setClosedAt(LocalDateTime.now());
        return Response.from(visitRequestRepository.save(r));
    }

    @Transactional(readOnly = true)
    public List<MidwifeContact> getMyPhmContacts(Integer motherUserId) {
        Mother mother = currentMother(motherUserId);
        if (mother.getPhmArea() == null) return List.of();
        return activeMidwives(mother.getPhmArea().getPhmAreaId()).stream()
                .map(m -> MidwifeContact.builder()
                        .name(m.getName())
                        .contactNumber(m.getContactNumber())
                        .phmAreaName(mother.getPhmArea().getAreaName())
                        .build())
                .toList();
    }

    // ── Midwife ───────────────────────────────────────────

    /** Requests in the midwife's PHM area: open urgent first, then other open, then closed; newest first within each. */
    @Transactional(readOnly = true)
    public List<Response> getForArea(Integer midwifeUserId, boolean openOnly) {
        Integer areaId = currentMidwife(midwifeUserId).getPhmArea().getPhmAreaId();
        List<VisitRequest> requests = openOnly
                ? visitRequestRepository.findByPhmArea_PhmAreaIdAndStatusIn(areaId, OPEN)
                : visitRequestRepository.findByPhmArea_PhmAreaIdOrderByCreatedAtDesc(areaId);
        return requests.stream()
                .sorted(Comparator.comparingInt(VisitRequestService::priorityRank)
                        .thenComparing(VisitRequest::getCreatedAt, Comparator.reverseOrder()))
                .map(Response::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OpenCount getOpenCount(Integer midwifeUserId) {
        Integer areaId = currentMidwife(midwifeUserId).getPhmArea().getPhmAreaId();
        List<VisitRequest> open = visitRequestRepository.findByPhmArea_PhmAreaIdAndStatusIn(areaId, OPEN);
        long urgent = open.stream()
                .filter(r -> r.getUrgency() == VisitRequestUrgency.URGENT && r.getStatus() == VisitRequestStatus.PENDING)
                .count();
        return OpenCount.builder().open(open.size()).urgent(urgent).build();
    }

    @Transactional
    public Response respond(Integer requestId, RespondRequest req, Integer midwifeUserId) {
        Midwife midwife = currentMidwife(midwifeUserId);
        VisitRequest r = find(requestId);
        if (!Objects.equals(r.getPhmArea().getPhmAreaId(), midwife.getPhmArea().getPhmAreaId())) {
            throw new IllegalStateException("Midwives can only handle requests from their own PHM area");
        }
        VisitRequestStatus next = parseEnum(VisitRequestStatus.class, req.getStatus(), "status");
        String response = req.getResponse() == null ? null : req.getResponse().trim();
        validateTransition(r.getStatus(), next, req.getScheduledAt(), response, LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();
        r.setStatus(next);
        r.setHandledBy(midwife);
        if (r.getAcknowledgedAt() == null) r.setAcknowledgedAt(now);
        if (next == VisitRequestStatus.SCHEDULED) r.setScheduledAt(req.getScheduledAt());
        if (response != null && !response.isEmpty()) r.setMidwifeResponse(response);
        if (!next.isOpen()) r.setClosedAt(now);
        return Response.from(visitRequestRepository.save(r));
    }

    /**
     * Midwife workflow: PENDING -> ACKNOWLEDGED -> SCHEDULED -> COMPLETED, with DECLINED possible from any
     * open state and SCHEDULED -> SCHEDULED allowed for rescheduling. Closed requests cannot change.
     */
    static void validateTransition(VisitRequestStatus from, VisitRequestStatus to,
                                   LocalDateTime scheduledAt, String response, LocalDateTime now) {
        if (!from.isOpen()) {
            throw new IllegalStateException("This request is already " + from.name().toLowerCase(Locale.ROOT));
        }
        switch (to) {
            case ACKNOWLEDGED -> {
                if (from != VisitRequestStatus.PENDING) {
                    throw new IllegalStateException("Only a pending request can be acknowledged");
                }
            }
            case SCHEDULED -> {
                if (scheduledAt == null) {
                    throw new IllegalArgumentException("Choose the visit date and time");
                }
                if (scheduledAt.isBefore(now.minusMinutes(5))) {
                    throw new IllegalArgumentException("Visit time cannot be in the past");
                }
            }
            case COMPLETED -> { }
            case DECLINED -> {
                if (response == null || response.isBlank()) {
                    throw new IllegalArgumentException("Please give the mother a reason when declining");
                }
            }
            default -> throw new IllegalArgumentException("A midwife cannot set status " + to);
        }
    }

    // ── Helpers ───────────────────────────────────────────

    private static int priorityRank(VisitRequest r) {
        if (!r.getStatus().isOpen()) return 2;
        return r.getUrgency() == VisitRequestUrgency.URGENT ? 0 : 1;
    }

    private void alertAreaMidwives(VisitRequest r) {
        List<String> emails = activeMidwives(r.getPhmArea().getPhmAreaId()).stream()
                .map(Midwife::getEmail)
                .filter(e -> e != null && !e.isBlank())
                .toList();
        if (emails.isEmpty()) return;
        // Copy plain values; the email goes out on another thread so a slow mail server never delays the mother
        String motherName = r.getMother().getName();
        String type = r.getRequestType() == VisitRequestType.HOME_VISIT ? "Home visit" : "Clinic consultation";
        String reason = r.getReason();
        String contact = r.getContactNumber();
        String address = r.getMother().getAddress();
        CompletableFuture.runAsync(() ->
                emailService.sendUrgentVisitRequestAlert(emails, motherName, type, reason, contact, address));
    }

    private List<Midwife> activeMidwives(Integer phmAreaId) {
        return midwifeRepository.findByPhmArea_PhmAreaId(phmAreaId).stream()
                .filter(m -> m.getUser() == null || !Boolean.FALSE.equals(m.getUser().getIsActive()))
                .toList();
    }

    private Mother currentMother(Integer userId) {
        return motherRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mother profile not found for this account"));
    }

    private Midwife currentMidwife(Integer userId) {
        Midwife midwife = midwifeRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Midwife profile not found for this account"));
        if (midwife.getPhmArea() == null) {
            throw new IllegalStateException("Midwife has no PHM area assigned");
        }
        return midwife;
    }

    private VisitRequest find(Integer requestId) {
        return visitRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Visit request not found: " + requestId));
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing " + label);
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + label + ": " + value);
        }
    }
}
