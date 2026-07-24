package com.smartepm.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    // Admin fields
    private Long totalEmployees;
    private Long totalProjects;
    private Long totalTasks;
    private Long activeProjects;
    private Long completedProjects;
    private Long pendingTasks;
    private Long completedTasks;

    // Chart data (Admin dashboard) — keyed by enum name, e.g. "TODO" -> 4
    private Map<String, Long> tasksByStatus;
    private Map<String, Long> tasksByPriority;
    private Map<String, Long> projectsByStatus;

    // Employee fields
    private List<TaskResponse> assignedTasks;
    private List<TaskResponse> upcomingDeadlines;
}
