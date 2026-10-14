package com.moh.moh_backend.controller;

import com.moh.moh_backend.config.RequireRoles;
import com.moh.moh_backend.service.MonthlyReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Area reports for the printable / PDF export (client request 3.6). Read-only. */
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final MonthlyReportService monthlyReportService;

    public ReportController(MonthlyReportService monthlyReportService) {
        this.monthlyReportService = monthlyReportService;
    }

    /** Monthly summary for the caller's PHM area (admins pass ?phmAreaId=). month = YYYY-MM, default this month. */
    @GetMapping("/monthly")
    @RequireRoles({"MIDWIFE", "DOCTOR", "ADMIN"})
    public ResponseEntity<?> monthly(@RequestParam(required = false) String month,
                                     @RequestParam(required = false) Integer phmAreaId,
                                     HttpServletRequest request) {
        try {
            return ResponseEntity.ok(monthlyReportService.build(month, phmAreaId,
                    (Integer) request.getAttribute("moh.userId"), (String) request.getAttribute("moh.role")));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return error(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message == null ? "Request failed" : message));
    }
}
