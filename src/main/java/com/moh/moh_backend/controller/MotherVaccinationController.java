package com.moh.moh_backend.controller;

import com.moh.moh_backend.Enum.PregnancyStatus;
import com.moh.moh_backend.config.RequireRoles;
import com.moh.moh_backend.dto.MaternalVaccinationDtos;
import com.moh.moh_backend.dto.MotherVaccinationCreateDto;
import com.moh.moh_backend.dto.MotherVaccinationResponseDto;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.PregnancyRepository;
import com.moh.moh_backend.service.MaternalVaccinationDueService;
import com.moh.moh_backend.service.MotherVaccinationService;
import com.moh.moh_backend.service.PregnancyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("/mother-vaccinations")
public class MotherVaccinationController {

    private final MotherVaccinationService motherVaccinationService;
    private final MaternalVaccinationDueService maternalVaccinationDueService;
    private final PregnancyService pregnancyService;
    private final PregnancyRepository pregnancyRepository;
    private final MotherRepository motherRepository;

    public MotherVaccinationController(MotherVaccinationService motherVaccinationService,
                                       MaternalVaccinationDueService maternalVaccinationDueService,
                                       PregnancyService pregnancyService,
                                       PregnancyRepository pregnancyRepository,
                                       MotherRepository motherRepository) {
        this.motherVaccinationService = motherVaccinationService;
        this.maternalVaccinationDueService = maternalVaccinationDueService;
        this.pregnancyService = pregnancyService;
        this.pregnancyRepository = pregnancyRepository;
        this.motherRepository = motherRepository;
    }

    @PostMapping
    @RequireRoles({"MIDWIFE", "DOCTOR"})
    public ResponseEntity<?> administerVaccine(@RequestBody MotherVaccinationCreateDto dto, HttpServletRequest request) {
        return handle(() -> {
            MotherVaccinationResponseDto response = motherVaccinationService.administerVaccine(dto, userId(request), role(request));
            return ResponseEntity
                    .created(URI.create("/mother-vaccinations/" + response.getVaccinationId()))
                    .body(response);
        });
    }

    @GetMapping("/{id}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getVaccinationById(@PathVariable Integer id, HttpServletRequest request) {
        return handle(() -> ResponseEntity.ok(motherVaccinationService.getVaccinationById(id, userId(request), role(request))));
    }

    @GetMapping("/by-pregnancy/{pregnancyId}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getVaccinationsByPregnancy(@PathVariable Integer pregnancyId, HttpServletRequest request) {
        return handle(() -> ResponseEntity.ok(
                motherVaccinationService.getVaccinationsByPregnancy(pregnancyId, userId(request), role(request))));
    }

    @GetMapping("/by-mother/{motherId}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getVaccinationsByMother(@PathVariable Integer motherId, HttpServletRequest request) {
        return handle(() -> ResponseEntity.ok(
                motherVaccinationService.getVaccinationsByMother(motherId, userId(request), role(request))));
    }

    /** Tetanus status and vaccines given for one pregnancy. */
    @GetMapping("/status/pregnancy/{pregnancyId}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getStatusForPregnancy(@PathVariable Integer pregnancyId, HttpServletRequest request) {
        return handle(() -> {
            Pregnancy pregnancy = pregnancyService.getPregnancyById(pregnancyId, userId(request), role(request));
            return ResponseEntity.ok(maternalVaccinationDueService.forPregnancy(pregnancy));
        });
    }

    /** Tetanus status for the mother's active pregnancy; 204 when she has none. */
    @GetMapping("/status/mother/{motherId}")
    @RequireRoles({"MIDWIFE", "DOCTOR", "MOTHER"})
    public ResponseEntity<?> getStatusForMother(@PathVariable Integer motherId, HttpServletRequest request) {
        return handle(() -> {
            findMother(motherId, request);
            List<Pregnancy> active = pregnancyRepository.findByMother_MotherIdAndPregnancyStatus(motherId, PregnancyStatus.ACTIVE);
            if (active.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(maternalVaccinationDueService.forPregnancy(active.get(0)));
        });
    }

    /** Active pregnancies in the caller's PHM area with a tetanus dose due or overdue (admins pass ?phmAreaId=). */
    @GetMapping("/due/area")
    @RequireRoles({"MIDWIFE", "DOCTOR", "ADMIN"})
    public ResponseEntity<?> getAreaDue(@RequestParam(required = false) Integer phmAreaId, HttpServletRequest request) {
        return handle(() -> ResponseEntity.ok(
                maternalVaccinationDueService.defaultersForArea(phmAreaId, userId(request), role(request))));
    }

    /** Records how many tetanus doses the mother had before they were recorded in this system. */
    @PutMapping("/tt-history/{motherId}")
    @RequireRoles({"MIDWIFE", "DOCTOR"})
    public ResponseEntity<?> saveTtHistory(@PathVariable Integer motherId,
                                           @RequestBody MaternalVaccinationDtos.TtHistoryRequest body,
                                           HttpServletRequest request) {
        return handle(() -> {
            Mother saved = maternalVaccinationDueService.saveHistory(findMother(motherId, request), body);
            return ResponseEntity.ok(Map.of(
                    "motherId", saved.getMotherId(),
                    "priorTtDoses", saved.getPriorTtDoses(),
                    "priorTtLastDate", saved.getPriorTtLastDate() == null ? "" : saved.getPriorTtLastDate().toString()));
        });
    }

    @DeleteMapping("/{id}")
    @RequireRoles("DOCTOR")
    public ResponseEntity<?> deleteVaccination(@PathVariable Integer id, HttpServletRequest request) {
        return handle(() -> {
            motherVaccinationService.deleteVaccination(id, userId(request), role(request));
            return ResponseEntity.noContent().build();
        });
    }

    private Mother findMother(Integer motherId, HttpServletRequest request) {
        Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new NotFound("Mother not found with id: " + motherId));
        pregnancyService.assertCanAccessMother(mother, userId(request), role(request));
        return mother;
    }

    /** Validation problems -> 400, access problems -> 403, missing records -> 404, all as {"error": "..."}. */
    private static ResponseEntity<?> handle(Supplier<ResponseEntity<?>> action) {
        try {
            return action.get();
        } catch (NotFound e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return error(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (RuntimeException e) {
            String msg = e.getMessage() == null ? "Request failed" : e.getMessage();
            return error(msg.toLowerCase().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR, msg);
        }
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message == null ? "Request failed" : message));
    }

    private static Integer userId(HttpServletRequest request) {
        return (Integer) request.getAttribute("moh.userId");
    }

    private static String role(HttpServletRequest request) {
        return (String) request.getAttribute("moh.role");
    }

    private static class NotFound extends RuntimeException {
        NotFound(String message) {
            super(message);
        }
    }
}
