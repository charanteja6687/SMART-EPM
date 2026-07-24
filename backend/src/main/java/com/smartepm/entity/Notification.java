package com.smartepm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * In-app notification targeted at a specific Employee (e.g. "you've been assigned a new task").
 * Simple read/unread model — no external delivery (email/push) in this build.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee recipient;

    @Column(nullable = false, length = 300)
    private String message;

    /** e.g. TASK_ASSIGNED, TASK_STATUS_CHANGED, DEADLINE_APPROACHING */
    @Column(length = 40)
    private String type;

    private Long relatedEntityId;

    @Column(name = "is_read", nullable = false)
private boolean read = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
