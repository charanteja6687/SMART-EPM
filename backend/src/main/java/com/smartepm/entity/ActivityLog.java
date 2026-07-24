package com.smartepm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Immutable audit trail entry. One row per meaningful action (create/update/delete/restore/
 * login/progress-change) taken by a user. Never updated after creation — write-once, read-many.
 */
@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username of the actor (or "SYSTEM" for automated actions). */
    @Column(nullable = false, length = 100)
    private String actorUsername;

    /** e.g. CREATE, UPDATE, DELETE, RESTORE, LOGIN, PROGRESS_UPDATE */
    @Column(nullable = false, length = 30)
    private String action;

    /** e.g. EMPLOYEE, PROJECT, TASK, AUTH */
    @Column(nullable = false, length = 30)
    private String entityType;

    private Long entityId;

    /** Human-readable summary, e.g. "Created employee 'Ravi Kumar'" */
    @Column(nullable = false, length = 500)
    private String description;

    @Column(updatable = false, nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
