package com.smartepm.service.impl;

import com.smartepm.dto.request.EmployeeRequest;
import com.smartepm.dto.response.EmployeeResponse;
import com.smartepm.dto.response.PageResponse;
import com.smartepm.entity.Employee;
import com.smartepm.exception.DuplicateResourceException;
import com.smartepm.exception.ResourceNotFoundException;
import com.smartepm.repository.EmployeeRepository;
import com.smartepm.service.ActivityLogService;
import com.smartepm.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new DuplicateResourceException("Employee with email '" + request.getEmail() + "' already exists");
        }
        Employee employee = Employee.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .salary(request.getSalary())
                .dateOfJoining(request.getDateOfJoining())
                .active(request.getActive() == null || request.getActive())
                .build();
        employee = employeeRepository.save(employee);
        logger.info("Employee created: id={}, name={}", employee.getId(), employee.getFullName());
        activityLogService.log("CREATE", "EMPLOYEE", employee.getId(),
                "Created employee '" + employee.getFullName() + "'");
        return toResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        if (!employee.getEmail().equalsIgnoreCase(request.getEmail())
                && employeeRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new DuplicateResourceException("Employee with email '" + request.getEmail() + "' already exists");
        }

        employee.setFullName(request.getFullName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setSalary(request.getSalary());
        employee.setDateOfJoining(request.getDateOfJoining());
        if (request.getActive() != null) {
            employee.setActive(request.getActive());
        }

        employee = employeeRepository.save(employee);
        logger.info("Employee updated: id={}", employee.getId());
        activityLogService.log("UPDATE", "EMPLOYEE", employee.getId(),
                "Updated employee '" + employee.getFullName() + "'");
        return toResponse(employee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        // Soft delete: mark as deleted instead of removing the row, so history/reports stay intact
        employee.setDeletedAt(LocalDateTime.now());
        employeeRepository.save(employee);
        logger.info("Employee soft-deleted: id={}", id);
        activityLogService.log("DELETE", "EMPLOYEE", id,
                "Deleted (soft) employee '" + employee.getFullName() + "'");
    }

    @Override
    @Transactional
    public EmployeeResponse restoreEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        if (employee.getDeletedAt() == null) {
            throw new com.smartepm.exception.BadRequestException("Employee is not deleted");
        }
        employee.setDeletedAt(null);
        employee = employeeRepository.save(employee);
        logger.info("Employee restored: id={}", id);
        activityLogService.log("RESTORE", "EMPLOYEE", id,
                "Restored employee '" + employee.getFullName() + "'");
        return toResponse(employee);
    }

    @Override
    public List<EmployeeResponse> getDeletedEmployees() {
        return employeeRepository.findByDeletedAtIsNotNull().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return toResponse(employee);
    }

    @Override
    public PageResponse<EmployeeResponse> searchEmployees(String keyword, String department, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Employee> result = employeeRepository.searchEmployees(
                (keyword == null || keyword.isBlank()) ? null : keyword,
                (department == null || department.isBlank()) ? null : department,
                pageable);

        List<EmployeeResponse> content = result.getContent().stream().map(this::toResponse).collect(Collectors.toList());

        return PageResponse.<EmployeeResponse>builder()
                .content(content)
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    private EmployeeResponse toResponse(Employee e) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .fullName(e.getFullName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .department(e.getDepartment())
                .designation(e.getDesignation())
                .salary(e.getSalary())
                .dateOfJoining(e.getDateOfJoining())
                .active(e.isActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
