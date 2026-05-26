package com.blood.model;

import com.blood.model.enumformat.ActionType;
import com.blood.model.enumformat.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "action_log", indexes = {
        @Index(name = "idx_action_log_staff", columnList = "staff_id"),
        @Index(name = "idx_action_log_user", columnList = "performed_by_user_id"),
        @Index(name = "idx_action_log_entity", columnList = "entity_name, entity_id"),
        @Index(name = "idx_action_log_action_type", columnList = "action_type"),
        @Index(name = "idx_action_log_created_at", columnList = "created_at")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = true)
    private Staff staff;

    @Column(name = "performed_by_user_id")
    private Integer performedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "performed_by_role", length = 30)
    private Role performedByRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private ActionType actionType;

    @Column(name = "entity_name", nullable = false, length = 50)
    private String entityName;

    @Column(name = "entity_id", nullable = false, length = 50)
    private String entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_data", columnDefinition = "json")
    private String oldData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_data", columnDefinition = "json")
    private String newData;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    @Builder.Default
    private ActionLogStatus status = ActionLogStatus.SUCCESS;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ActionLogStatus {
        SUCCESS, FAILED
    }
}
