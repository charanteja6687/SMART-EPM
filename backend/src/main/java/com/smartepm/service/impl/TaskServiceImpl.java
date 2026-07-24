package com.smartepm.service.impl;

import com.smartepm.dto.request.TaskProgressUpdateRequest;
import com.smartepm.dto.request.TaskRequest;
import com.smartepm.dto.response.PageResponse;
import com.smartepm.dto.response.TaskResponse;
import com.smartepm.entity.*;
import com.smartepm.exception.BadRequestException;
import com.smartepm.exception.ResourceNotFoundException;
import com.smartepm.repository.EmployeeRepository;
import com.smartepm.repository.ProjectRepository;
import com.smartepm.repository.TaskRepository;
import com.smartepm.security.UserPrincipal;
import com.smartepm.service.ActivityLogService;
import com.smartepm.service.NotificationService;
import com.smartepm.service.TaskService;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        Employee employee = null;
        if (request.getEmployeeId() != null) {
            employee = employeeRepository.findByIdAndDeletedAtIsNull(request.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .progress(request.getProgress() != null ? request.getProgress() : 0)
                .dueDate(request.getDueDate())
                .remarks(request.getRemarks())
                .project(project)
                .assignedTo(employee)
                .build();

        task = taskRepository.save(task);
        logger.info("Task created: id={}, title={}", task.getId(), task.getTitle());
        activityLogService.log("CREATE", "TASK", task.getId(), "Created task '" + task.getTitle() + "' in project '" + project.getName() + "'");

        if (employee != null) {
            notificationService.notify(employee.getId(),
                    "You've been assigned a new task: \"" + task.getTitle() + "\" in project \"" + project.getName() + "\"",
                    "TASK_ASSIGNED", task.getId());
        }

        return toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        Project project = projectRepository.findByIdAndDeletedAtIsNull(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        Employee newEmployee = null;
        if (request.getEmployeeId() != null) {
            newEmployee = employeeRepository.findByIdAndDeletedAtIsNull(request.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));
        }

        Long previousAssigneeId = task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;
        Long newAssigneeId = newEmployee != null ? newEmployee.getId() : null;
        boolean reassigned = !Objects.equals(previousAssigneeId, newAssigneeId);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getProgress() != null) task.setProgress(request.getProgress());
        task.setDueDate(request.getDueDate());
        task.setRemarks(request.getRemarks());
        task.setProject(project);
        task.setAssignedTo(newEmployee);

        task = taskRepository.save(task);
        logger.info("Task updated: id={}", task.getId());
        activityLogService.log("UPDATE", "TASK", task.getId(), "Updated task '" + task.getTitle() + "'");

        // Only notify the newly-assigned employee if the assignment actually changed
        if (reassigned && newAssigneeId != null) {
            notificationService.notify(newAssigneeId,
                    "You've been assigned to task: \"" + task.getTitle() + "\" in project \"" + project.getName() + "\"",
                    "TASK_ASSIGNED", task.getId());
        }

        return toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTaskProgress(Long id, TaskProgressUpdateRequest request) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        if (request.getProgress() != null) {
            task.setProgress(request.getProgress());
            if (request.getProgress() == 100 && request.getStatus() == null) {
                task.setStatus(TaskStatus.COMPLETED);
            }
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            if (request.getStatus() == TaskStatus.COMPLETED) {
                task.setProgress(100);
            }
        }
        if (request.getRemarks() != null) {
            task.setRemarks(request.getRemarks());
        }

        task = taskRepository.save(task);
        logger.info("Task progress updated: id={}, progress={}, status={}", id, task.getProgress(), task.getStatus());
        activityLogService.log("PROGRESS_UPDATE", "TASK", task.getId(),
                "Updated progress on '" + task.getTitle() + "' to " + task.getProgress() + "% (" + task.getStatus() + ")");

        return toResponse(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        task.setDeletedAt(LocalDateTime.now());
        taskRepository.save(task);
        logger.info("Task soft-deleted: id={}", id);
        activityLogService.log("DELETE", "TASK", id, "Deleted (soft) task '" + task.getTitle() + "'");
    }

    @Override
    @Transactional
    public TaskResponse restoreTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        if (task.getDeletedAt() == null) {
            throw new BadRequestException("Task is not deleted");
        }
        task.setDeletedAt(null);
        task = taskRepository.save(task);
        logger.info("Task restored: id={}", id);
        activityLogService.log("RESTORE", "TASK", id, "Restored task '" + task.getTitle() + "'");
        return toResponse(task);
    }

    @Override
    public List<TaskResponse> getDeletedTasks() {
        return taskRepository.findByDeletedAtIsNotNull().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        // Employees may only view tasks assigned to them. A direct GET /tasks/{id} for
        // someone else's task is treated as "not found" rather than 403, to avoid leaking
        // whether the task exists to an unauthorized employee.
        UserPrincipal principal = currentPrincipal();
        if (principal != null && isEmployeeRole(principal)) {
            Long assignedId = task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;
            if (assignedId == null || !assignedId.equals(principal.getEmployeeId())) {
                throw new ResourceNotFoundException("Task", "id", id);
            }
        }

        return toResponse(task);
    }

    @Override
    public PageResponse<TaskResponse> searchTasks(String keyword, TaskStatus status, Priority priority, Long projectId, Long employeeId, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Server-side enforcement: an EMPLOYEE's results are always scoped to tasks assigned
        // to them, no matter what the client requests. This overrides any employeeId passed
        // in from the frontend, so it cannot be bypassed by omitting/forging query params.
        Long effectiveEmployeeId = employeeId;
        UserPrincipal principal = currentPrincipal();
        if (principal != null && isEmployeeRole(principal)) {
            effectiveEmployeeId = principal.getEmployeeId(); // null if this account has no linked Employee record
        }

        Page<Task> result = taskRepository.searchTasks(
                (keyword == null || keyword.isBlank()) ? null : keyword,
                status, priority, projectId, effectiveEmployeeId, pageable);

        List<TaskResponse> content = result.getContent().stream().map(this::toResponse).collect(Collectors.toList());

        return PageResponse.<TaskResponse>builder()
                .content(content)
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
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

    private TaskResponse toResponse(Task t) {
        return TaskResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .status(t.getStatus())
                .priority(t.getPriority())
                .progress(t.getProgress())
                .dueDate(t.getDueDate())
                .remarks(t.getRemarks())
                .projectId(t.getProject() != null ? t.getProject().getId() : null)
                .projectName(t.getProject() != null ? t.getProject().getName() : null)
                .employeeId(t.getAssignedTo() != null ? t.getAssignedTo().getId() : null)
                .employeeName(t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : null)
                .build();
    }
}
