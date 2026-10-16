package com.moh.moh_backend.service;

import com.moh.moh_backend.config.RequireRoles;
import com.moh.moh_backend.controller.AuditLogController;
import com.moh.moh_backend.dto.AuditLogDtos;
import com.moh.moh_backend.model.AuditLog;
import com.moh.moh_backend.model.User;
import com.moh.moh_backend.repository.AuditLogRepository;
import com.moh.moh_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Admin audit-log search.
 */
class AuditLogServiceTests {

    private final AuditLogRepository repository = mock(AuditLogRepository.class);
    private final UserRepository userRepo = mock(UserRepository.class);
    private final AuditLogService service = new AuditLogService(repository, userRepo);

    private static AuditLogService.SearchFilter filter(LocalDate from, LocalDate to, String username) {
        return new AuditLogService.SearchFilter(from, to, username, null, null, false, null);
    }

    @Test
    void auditLogEndpointIsAdminOnly() {
        RequireRoles roles = AuditLogController.class.getAnnotation(RequireRoles.class);
        assertNotNull(roles);
        assertArrayEquals(new String[]{"ADMIN"}, roles.value());
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchAddsUsernamesAndCapsPageSize() {
        AuditLog log = new AuditLog();
        log.setAuditId(1L);
        log.setUserId(7);
        log.setRole("MIDWIFE");
        log.setHttpMethod("POST");
        log.setRequestPath("/api/v1/moh-backend/mothers");
        log.setAction("CREATE");
        log.setResponseStatus(200);
        log.setCreatedAt(LocalDateTime.of(2026, 10, 16, 9, 30));
        AuditLog anonymous = new AuditLog();
        anonymous.setAuditId(2L);
        anonymous.setHttpMethod("POST");
        anonymous.setRequestPath("/api/v1/moh-backend/auth/login");
        anonymous.setAction("CREATE");
        anonymous.setResponseStatus(400);

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(log, anonymous), inv.getArgument(1), 2));
        User u = new User();
        u.setUserId(7);
        u.setUsername("midwife_anne");
        when(userRepo.findAllById(any())).thenReturn(List.of(u));

        AuditLogDtos.PageResponse page = service.search(filter(null, null, null), 0, 1000);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageable.capture());
        assertEquals(AuditLogService.MAX_PAGE_SIZE, pageable.getValue().getPageSize());
        assertEquals(2, page.content.size());
        assertEquals("midwife_anne", page.content.get(0).username);
        assertEquals("2026-10-16T09:30", page.content.get(0).createdAt);
        assertNull(page.content.get(1).username);
    }

    @Test
    @SuppressWarnings("unchecked")
    void unknownUsernameReturnsEmptyPageWithoutQuery() {
        when(userRepo.findByUsernameContainingIgnoreCase("ghost")).thenReturn(List.of());

        AuditLogDtos.PageResponse page = service.search(filter(null, null, "ghost"), 0, 25);

        assertEquals(0, page.totalElements);
        assertTrue(page.content.isEmpty());
        verify(repository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void fromAfterToIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.search(
                filter(LocalDate.of(2026, 10, 16), LocalDate.of(2026, 10, 1), null), 0, 25));
    }

    @Test
    @SuppressWarnings("unchecked")
    void negativePageIsTreatedAsFirstPage() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(), inv.getArgument(1), 0));

        service.search(filter(null, null, null), -3, 0);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageable.capture());
        assertEquals(PageRequest.of(0, 1).getPageNumber(), pageable.getValue().getPageNumber());
        assertEquals(1, pageable.getValue().getPageSize());
    }
}
