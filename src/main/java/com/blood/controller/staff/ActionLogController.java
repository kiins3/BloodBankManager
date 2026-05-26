package com.blood.controller.staff;

import com.blood.dto.actionlog.ActionLogResponse;
import com.blood.model.enumformat.ActionType;
import com.blood.model.enumformat.Role;
import com.blood.service.ActionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff/action-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF_TECH', 'STAFF_INVENTORY', 'ADMIN')")
public class ActionLogController {

    private final ActionLogService actionLogService;

    @GetMapping
    public ResponseEntity<Page<ActionLogResponse>> getLogs(
            @RequestParam(required = false) Integer staffId,
            @RequestParam(required = false) ActionType actionType,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ActionLogResponse> logs = actionLogService.searchLogs(staffId, actionType, entityName, role, pageable);
        return ResponseEntity.ok(logs);
    }
}
