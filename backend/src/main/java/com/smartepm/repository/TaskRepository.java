package com.smartepm.repository;

import com.smartepm.entity.Priority;
import com.smartepm.entity.Task;
import com.smartepm.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Active (non-deleted) lookup — use this in services instead of plain findById
    Optional<Task> findByIdAndDeletedAtIsNull(Long id);

    List<Task> findByDeletedAtIsNotNull();

    @Query("SELECT t FROM Task t WHERE t.deletedAt IS NULL AND " +
            "(:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:projectId IS NULL OR t.project.id = :projectId) " +
            "AND (:employeeId IS NULL OR t.assignedTo.id = :employeeId)")
    Page<Task> searchTasks(@Param("keyword") String keyword,
                            @Param("status") TaskStatus status,
                            @Param("priority") Priority priority,
                            @Param("projectId") Long projectId,
                            @Param("employeeId") Long employeeId,
                            Pageable pageable);

    List<Task> findByAssignedTo_IdAndDeletedAtIsNull(Long employeeId);

    List<Task> findByAssignedTo_IdAndStatusAndDeletedAtIsNull(Long employeeId, TaskStatus status);

    List<Task> findByAssignedTo_IdAndDueDateBetweenAndDeletedAtIsNull(Long employeeId, LocalDate start, LocalDate end);

    List<Task> findByProject_IdAndDeletedAtIsNull(Long projectId);

    long countByStatusAndDeletedAtIsNull(TaskStatus status);

    long countByPriorityAndDeletedAtIsNull(Priority priority);

    long countByProject_IdAndDeletedAtIsNull(Long projectId);

    long countByProject_IdAndStatusAndDeletedAtIsNull(Long projectId, TaskStatus status);

    long countByDeletedAtIsNull();

    List<Task> findByDeletedAtIsNull();
}
