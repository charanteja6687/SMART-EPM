package com.smartepm.repository;

import com.smartepm.entity.Priority;
import com.smartepm.entity.Project;
import com.smartepm.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Active (non-deleted) lookup — use this in services instead of plain findById
    Optional<Project> findByIdAndDeletedAtIsNull(Long id);

    List<Project> findByDeletedAtIsNotNull();

    @Query("SELECT p FROM Project p WHERE p.deletedAt IS NULL AND " +
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:priority IS NULL OR p.priority = :priority) " +
            "AND (:employeeId IS NULL OR :employeeId IN (SELECT e.id FROM p.employees e))")
    Page<Project> searchProjects(@Param("keyword") String keyword,
                                  @Param("status") ProjectStatus status,
                                  @Param("priority") Priority priority,
                                  @Param("employeeId") Long employeeId,
                                  Pageable pageable);

    long countByStatusAndDeletedAtIsNull(ProjectStatus status);

    long countByDeletedAtIsNull();

    List<Project> findByDeletedAtIsNull();

    @Query("SELECT p FROM Project p JOIN p.employees e WHERE e.id = :employeeId AND p.deletedAt IS NULL")
    List<Project> findByEmployeeId(@Param("employeeId") Long employeeId);
}
