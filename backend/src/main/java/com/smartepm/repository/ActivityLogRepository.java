package com.smartepm.repository;

import com.smartepm.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query("SELECT a FROM ActivityLog a WHERE " +
            "(:entityType IS NULL OR a.entityType = :entityType) " +
            "AND (:actorUsername IS NULL OR a.actorUsername = :actorUsername) " +
            "ORDER BY a.timestamp DESC")
    Page<ActivityLog> search(@Param("entityType") String entityType,
                              @Param("actorUsername") String actorUsername,
                              Pageable pageable);

    Page<ActivityLog> findAllByOrderByTimestampDesc(Pageable pageable);
}
