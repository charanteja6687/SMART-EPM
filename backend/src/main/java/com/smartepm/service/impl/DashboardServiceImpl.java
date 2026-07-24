package com.smartepm.service.impl;

import com.smartepm.dto.response.DashboardResponse;
import com.smartepm.dto.response.TaskResponse;
import com.smartepm.entity.Priority;
import com.smartepm.entity.ProjectStatus;
import com.smartepm.entity.Task;
import com.smartepm.entity.TaskStatus;
import com.smartepm.repository.EmployeeRepository;
import com.smartepm.repository.ProjectRepository;
import com.smartepm.repository.TaskRepository;
import com.smartepm.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Override
    public DashboardResponse getAdminDashboard() {
        Map<String, Long> tasksByStatus = new LinkedHashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            tasksByStatus.put(status.name(), taskRepository.countByStatusAndDeletedAtIsNull(status));
        }

        Map<String, Long> tasksByPriority = new LinkedHashMap<>();
        for (Priority priority : Priority.values()) {
            tasksByPriority.put(priority.name(), taskRepository.countByPriorityAndDeletedAtIsNull(priority));
        }

        Map<String, Long> projectsByStatus = new LinkedHashMap<>();
        for (ProjectStatus status : ProjectStatus.values()) {
            projectsByStatus.put(status.name(), projectRepository.countByStatusAndDeletedAtIsNull(status));
        }

        return DashboardResponse.builder()
                .totalEmployees(employeeRepository.countByDeletedAtIsNull())
                .totalProjects(projectRepository.countByDeletedAtIsNull())
                .totalTasks(taskRepository.countByDeletedAtIsNull())
                .activeProjects(projectRepository.countByStatusAndDeletedAtIsNull(ProjectStatus.ACTIVE))
                .completedProjects(projectRepository.countByStatusAndDeletedAtIsNull(ProjectStatus.COMPLETED))
                .pendingTasks(taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.TODO)
                        + taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.IN_PROGRESS))
                .completedTasks(taskRepository.countByStatusAndDeletedAtIsNull(TaskStatus.COMPLETED))
                .tasksByStatus(tasksByStatus)
                .tasksByPriority(tasksByPriority)
                .projectsByStatus(projectsByStatus)
                .build();
    }

    @Override
    public DashboardResponse getEmployeeDashboard(Long employeeId) {
        List<Task> assigned = taskRepository.findByAssignedTo_IdAndDeletedAtIsNull(employeeId);
        List<Task> completed = taskRepository.findByAssignedTo_IdAndStatusAndDeletedAtIsNull(employeeId, TaskStatus.COMPLETED);
        List<Task> upcoming = taskRepository.findByAssignedTo_IdAndDueDateBetweenAndDeletedAtIsNull(
                employeeId, LocalDate.now(), LocalDate.now().plusDays(7));

        return DashboardResponse.builder()
                .assignedTasks(assigned.stream().map(this::toResponse).collect(Collectors.toList()))
                .completedTasks((long) completed.size())
                .pendingTasks((long) (assigned.size() - completed.size()))
                .upcomingDeadlines(upcoming.stream().map(this::toResponse).collect(Collectors.toList()))
                .build();
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
