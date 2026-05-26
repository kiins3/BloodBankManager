package com.blood.repository;

import com.blood.model.ActionLog;
import com.blood.model.enumformat.ActionType;
import com.blood.model.enumformat.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Integer> {

    @Query("""
        SELECT a FROM ActionLog a
        LEFT JOIN a.staff s
        WHERE (:staffId IS NULL OR s.staffId = :staffId)
          AND (:actionType IS NULL OR a.actionType = :actionType)
          AND (:entityName IS NULL OR a.entityName = :entityName)
          AND (:role IS NULL OR a.performedByRole = :role)
        ORDER BY a.createdAt DESC
    """)
    Page<ActionLog> searchLogs(
            @Param("staffId") Integer staffId,
            @Param("actionType") ActionType actionType,
            @Param("entityName") String entityName,
            @Param("role") Role role,
            Pageable pageable
    );
}
