package com.smartepm.service.impl;

import com.smartepm.dto.request.ProjectRequest;
import com.smartepm.dto.response.EmployeeResponse;
import com.smartepm.dto.response.PageResponse;
import com.smartepm.dto.response.ProjectResponse;
import com.smartepm.entity.*;
import com.smartepm.exception.BadRequestException;
import com.smartepm.exception.ResourceNotFoundException;
import com.smartepm.repository.EmployeeRepository;
import com.smartepm.repository.ProjectRepository;
import com.smartepm.repository.TaskRepository;
import com.smartepm.security.UserPrincipal;
import com.smartepm.service.ActivityLogService;
import com.smartepm.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .startDate(request.getStartDate())
                .deadline(request.getDeadline())
                .build();

        if (request.getEmployeeIds() != null && !request.getEmployeeIds().isEmpty()) {
            project.setEmployees(resolveEmployees(request.getEmployeeIds()));
        }

        project = projectRepository.save(project);
        logger.info("Project created: id={}, name={}", project.getId(), project.getName());
        activityLogService.log("CREATE", "PROJECT", project.getId(), "Created project '" + project.getName() + "'");
        return toResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());
        project.setPriority(request.getPriority());
        project.setStartDate(request.getStartDate());
        project.setDeadline(request.getDeadline());

        if (request.getEmployeeIds() != null) {
            project.setEmployees(resolveEmployees(request.getEmployeeIds()));
        }

        project = projectRepository.save(project);
        logger.info("Project updated: id={}", project.getId());
        activityLogService.log("UPDATE", "PROJECT", project.getId(), "Updated project '" + project.getName() + "'");
        return toResponse(project);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        LocalDateTime now = LocalDateTime.now();
        project.setDeletedAt(now);
        projectRepository.save(project);

        List<Task> tasks = taskRepository.findByProject_IdAndDeletedAtIsNull(id);
        for (Task t : tasks) {
            t.setDeletedAt(now);
        }
        taskRepository.saveAll(tasks);

        logger.info("Project soft-deleted: id={} (cascaded to {} tasks)", id, tasks.size());
        activityLogService.log("DELETE", "PROJECT", id,
                "Deleted (soft) project '" + project.getName() + "' (" + tasks.size() + " tasks cascaded)");
    }

    @Override
    @Transactional
    public ProjectResponse restoreProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        if (project.getDeletedAt() == null) {
            throw new BadRequestException("Project is not deleted");
        }
        project.setDeletedAt(null);
        project = projectRepository.save(project);
        logger.info("Project restored: id={}", id);
        activityLogService.log("RESTORE", "PROJECT", id, "Restored project '" + project.getName() + "'");
        return toResponse(project);
    }

    @Override
    public List<ProjectResponse> getDeletedProjects() {
        return projectRepository.findByDeletedAtIsNotNull().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        // Employees may only view projects they're assigned to. A direct GET /projects/{id} for
        // someone else's project is treated as "not found" rather than 403, so we don't leak
        // whether the project exists at all to an unauthorized employee.
        UserPrincipal principal = currentPrincipal();
        if (principal != null && isEmployeeRole(principal)) {
            boolean isAssigned = project.getEmployees().stream()
                    .anyMatch(e -> e.getId().equals(principal.getEmployeeId()));
            if (!isAssigned) {
                throw new ResourceNotFoundException("Project", "id", id);
            }
        }

        return toResponse(project);
    }

    @Override
    public PageResponse<ProjectResponse> searchProjects(String keyword, ProjectStatus status, Priority priority, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Server-side enforcement: an EMPLOYEE's results are always scoped to their own
        // assigned projects, no matter what the client requests. This cannot be bypassed
        // by omitting or forging query parameters on the frontend.
        Long employeeId = null;
        UserPrincipal principal = currentPrincipal();
        if (principal != null && isEmployeeRole(principal)) {
            employeeId = principal.getEmployeeId(); // null if this EMPLOYEE account has no linked Employee record
        }

        Page<Project> result = projectRepository.searchProjects(
                (keyword == null || keyword.isBlank()) ? null : keyword, status, priority, employeeId, pageable);

        List<ProjectResponse> content = result.getContent().stream().map(this::toResponse).collect(Collectors.toList());

        return PageResponse.<ProjectResponse>builder()
                .content(content)
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    @Override
    @Transactional
    public ProjectResponse assignEmployees(Long projectId, Set<Long> employeeIds) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        project.setEmployees(resolveEmployees(employeeIds));
        project = projectRepository.save(project);
        logger.info("Employees assigned to project id={}: {}", projectId, employeeIds);
        activityLogService.log("UPDATE", "PROJECT", projectId,
                "Assigned employees to project '" + project.getName() + "': " + employeeIds);
        return toResponse(project);
    }

    private Set<Employee> resolveEmployees(Set<Long> employeeIds) {
        Set<Employee> employees = new HashSet<>();
        for (Long empId : employeeIds) {
            Employee employee = employeeRepository.findByIdAndDeletedAtIsNull(empId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", empId));
            employees.add(employee);
        }
        return employees;
    }

    private UserPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        return null;
    }

    private boolean isEmployeeRole(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
    }

    private ProjectResponse toResponse(Project p) {
        Set<EmployeeResponse> employeeResponses = p.getEmployees().stream()
                .map(e -> EmployeeResponse.builder()
                        .id(e.getId())
                        .fullName(e.getFullName())
                        .email(e.getEmail())
                        .department(e.getDepartment())
                        .designation(e.getDesignation())
                        .active(e.isActive())
                        .build())
                .collect(Collectors.toSet());

        long totalTasks = taskRepository.countByProject_IdAndDeletedAtIsNull(p.getId());
        long completedTasks = taskRepository.countByProject_IdAndStatusAndDeletedAtIsNull(p.getId(), TaskStatus.COMPLETED);
        double progress = totalTasks == 0 ? 0 : (completedTasks * 100.0) / totalTasks;

        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .status(p.getStatus())
                .priority(p.getPriority())
                .startDate(p.getStartDate())
                .deadline(p.getDeadline())
                .employees(employeeResponses)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .progressPercent(Math.round(progress * 100.0) / 100.0)
                .build();
    }
}
