package com.smartepm.repository;

import com.smartepm.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    Boolean existsByEmailAndDeletedAtIsNull(String email);

    // Active (non-deleted) lookup — use this in services instead of plain findById
    Optional<Employee> findByIdAndDeletedAtIsNull(Long id);

    List<Employee> findByDeletedAtIsNotNull();

    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL AND " +
            "(:keyword IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:department IS NULL OR e.department = :department)")
    Page<Employee> searchEmployees(@Param("keyword") String keyword,
                                    @Param("department") String department,
                                    Pageable pageable);

    long countByActiveTrueAndDeletedAtIsNull();

    long countByDeletedAtIsNull();

    List<Employee> findByDeletedAtIsNull();
}
