package com.smartepm.dto.response;

import com.smartepm.entity.Priority;
import com.smartepm.entity.ProjectStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private Priority priority;
    private LocalDate startDate;
    private LocalDate deadline;
    private Set<EmployeeResponse> employees;
    private long totalTasks;
    private long completedTasks;
    private double progressPercent;
}
