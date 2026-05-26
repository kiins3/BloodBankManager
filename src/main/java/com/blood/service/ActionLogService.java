package com.blood.service;

import com.blood.dto.actionlog.ActionLogResponse;
import com.blood.model.ActionLog;
import com.blood.model.Staff;
import com.blood.model.Users;
import com.blood.model.enumformat.ActionType;
import com.blood.model.enumformat.Role;
import com.blood.repository.ActionLogRepository;
import com.blood.repository.StaffRepository;
import com.blood.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActionLogService {

    private final ActionLogRepository actionLogRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // =========================================================================
    // PUBLIC API — các Service chỉ cần gọi các method này
    // =========================================================================

    /**
     * Ghi log với đầy đủ thông tin (có old data và note).
     * Người thực hiện được tự động resolve từ SecurityContext.
     */
    public void log(ActionType actionType,
                    String entityName,
                    String entityId,
                    Object oldObject,
                    Object newObject,
                    String note) {

        ActionLog actionLog = buildBaseLog(actionType, entityName, entityId, note);
        actionLog.setOldData(toJson(oldObject));
        actionLog.setNewData(toJson(newObject));
        saveQuietly(actionLog);
    }

    /**
     * Overload không có note.
     */
    public void log(ActionType actionType,
                    String entityName,
                    String entityId,
                    Object oldObject,
                    Object newObject) {
        log(actionType, entityName, entityId, oldObject, newObject, null);
    }

    /**
     * Overload cho CREATE — không có oldData.
     */
    public void logCreate(ActionType actionType,
                          String entityName,
                          String entityId,
                          Object newObject,
                          String note) {
        log(actionType, entityName, entityId, null, newObject, note);
    }

    // =========================================================================
    // QUERY
    // =========================================================================

    public Page<ActionLogResponse> searchLogs(Integer staffId,
                                              ActionType actionType,
                                              String entityName,
                                              Role role,
                                              Pageable pageable) {
        Page<ActionLog> logs = actionLogRepository.searchLogs(staffId, actionType, entityName, role, pageable);
        return logs.map(this::mapToResponse);
    }

    // =========================================================================
    // PRIVATE — resolve người thực hiện từ SecurityContext
    // =========================================================================

    /**
     * Build ActionLog cơ bản: tự động đọc SecurityContext để điền
     * performedByUserId, performedByRole, staff (nếu là STAFF).
     */
    private ActionLog buildBaseLog(ActionType actionType, String entityName, String entityId, String note) {
        ActionLog.ActionLogBuilder builder = ActionLog.builder()
                .actionType(actionType)
                .entityName(entityName)
                .entityId(entityId)
                .note(note)
                .status(ActionLog.ActionLogStatus.SUCCESS);

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String email = auth.getName();
                Users user = userRepository.findByEmail(email).orElse(null);

                if (user != null) {
                    builder.performedByUserId(user.getId());
                    builder.performedByRole(user.getRole());

                    // Chỉ resolve Staff nếu role là STAFF (ADMIN không có Staff record)
                    if (user.getRole() != null && user.getRole() != Role.ADMIN) {
                        Staff staff = staffRepository.findByUser(user).orElse(null);
                        builder.staff(staff);
                    }
                    // ADMIN: staff = null, nhưng performedByUserId + performedByRole đã đủ thông tin
                }
            }
        } catch (Exception e) {
            log.warn("ActionLog: Không thể resolve người thực hiện từ SecurityContext: {}", e.getMessage());
        }

        return builder.build();
    }

    private void saveQuietly(ActionLog actionLog) {
        try {
            actionLogRepository.save(actionLog);
        } catch (Exception e) {
            log.error("ActionLog: Không thể lưu log [action={}, entity={}, id={}]: {}",
                    actionLog.getActionType(), actionLog.getEntityName(),
                    actionLog.getEntityId(), e.getMessage());
        }
    }

    // =========================================================================
    // MAPPING
    // =========================================================================

    private ActionLogResponse mapToResponse(ActionLog entity) {
        String staffName = null;
        Integer staffId = null;

        if (entity.getStaff() != null) {
            staffId = entity.getStaff().getStaffId();
            staffName = entity.getStaff().getFullName();
        }

        // Với ADMIN: hiển thị email thay vì tên staff
        String performerDisplay = staffName;
        if (performerDisplay == null && entity.getPerformedByUserId() != null) {
            performerDisplay = userRepository.findById(entity.getPerformedByUserId())
                    .map(u -> u.getEmail())
                    .orElse("Unknown");
        }

        return ActionLogResponse.builder()
                .logId(entity.getLogId())
                .staffId(staffId)
                .staffName(performerDisplay)
                .performedByUserId(entity.getPerformedByUserId())
                .performedByRole(entity.getPerformedByRole() != null
                        ? entity.getPerformedByRole().name() : null)
                .actionType(entity.getActionType() != null ? entity.getActionType().name() : null)
                .entityName(entity.getEntityName())
                .entityId(entity.getEntityId())
                .oldData(entity.getOldData())
                .newData(entity.getNewData())
                .note(entity.getNote())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("ActionLog: Không thể serialize [type={}]: {}",
                    obj.getClass().getSimpleName(), e.getMessage());
            return "{\"error\": \"serialize_failed\"}";
        }
    }
}
