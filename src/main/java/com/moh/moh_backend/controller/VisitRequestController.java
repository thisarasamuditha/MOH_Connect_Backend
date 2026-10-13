package com.moh.moh_backend.controller;

import com.moh.moh_backend.config.RequireRoles;
import com.moh.moh_backend.dto.VisitRequestDtos.CreateRequest;
import com.moh.moh_backend.dto.VisitRequestDtos.MidwifeContact;
import com.moh.moh_backend.dto.VisitRequestDtos.OpenCount;
import com.moh.moh_backend.dto.VisitRequestDtos.RespondRequest;
import com.moh.moh_backend.dto.VisitRequestDtos.Response;
import com.moh.moh_backend.service.VisitRequestService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Mother Portal special / emergency visit requests (client request 3.1). */
@RestController
@RequestMapping("/visit-requests")
@RequiredArgsConstructor
public class VisitRequestController {

    private final VisitRequestService visitRequestService;

    // ── Mother ────────────────────────────────────────────

    @PostMapping
    @RequireRoles("MOTHER")
    public ResponseEntity<Response> create(@RequestBody CreateRequest body, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitRequestService.create(body, userId(request)));
    }

    @GetMapping("/my")
    @RequireRoles("MOTHER")
    public ResponseEntity<List<Response>> getMine(HttpServletRequest request) {
        return ResponseEntity.ok(visitRequestService.getMine(userId(request)));
    }

    @PutMapping("/{id}/cancel")
    @RequireRoles("MOTHER")
    public ResponseEntity<Response> cancel(@PathVariable Integer id, HttpServletRequest request) {
        return ResponseEntity.ok(visitRequestService.cancel(id, userId(request)));
    }

    @GetMapping("/my-phm")
    @RequireRoles("MOTHER")
    public ResponseEntity<List<MidwifeContact>> getMyPhmContacts(HttpServletRequest request) {
        return ResponseEntity.ok(visitRequestService.getMyPhmContacts(userId(request)));
    }

    // ── Midwife ───────────────────────────────────────────

    /** ?status=open (default) or ?status=all */
    @GetMapping("/area")
    @RequireRoles("MIDWIFE")
    public ResponseEntity<List<Response>> getForArea(@RequestParam(defaultValue = "open") String status,
                                                     HttpServletRequest request) {
        return ResponseEntity.ok(visitRequestService.getForArea(userId(request), !"all".equalsIgnoreCase(status)));
    }

    @GetMapping("/area/open-count")
    @RequireRoles("MIDWIFE")
    public ResponseEntity<OpenCount> getOpenCount(HttpServletRequest request) {
        return ResponseEntity.ok(visitRequestService.getOpenCount(userId(request)));
    }

    @PutMapping("/{id}/respond")
    @RequireRoles("MIDWIFE")
    public ResponseEntity<Response> respond(@PathVariable Integer id, @RequestBody RespondRequest body,
                                            HttpServletRequest request) {
        return ResponseEntity.ok(visitRequestService.respond(id, body, userId(request)));
    }

    private Integer userId(HttpServletRequest request) {
        return (Integer) request.getAttribute("moh.userId");
    }
}
