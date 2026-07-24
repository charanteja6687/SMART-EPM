package com.smartepm.service;

import com.smartepm.dto.request.ProjectRequest;
import com.smartepm.dto.response.PageResponse;
import com.smartepm.dto.response.ProjectResponse;
import com.smartepm.entity.Priority;
import com.smartepm.entity.ProjectStatus;

import java.util.List;
import java.util.Set;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest request);
    ProjectResponse updateProject(Long id, ProjectRequest request);
    void deleteProject(Long id);
    ProjectResponse restoreProject(Long id);
    List<ProjectResponse> getDeletedProjects();

    /** Returns the project only if the caller is allowed to see it (ADMIN sees all; EMPLOYEE only their own). */
    ProjectResponse getProjectById(Long id);

    /**
     * Searches projects. When the caller is an EMPLOYEE, results are always restricted to
     * projects they are assigned to, regardless of any filters passed in — this scoping
     * happens server-side and cannot be bypassed from the client.
     */
    PageResponse<ProjectResponse> searchProjects(String keyword, ProjectStatus status, Priority priority, int page, int size, String sortBy, String direction);

    ProjectResponse assignEmployees(Long projectId, Set<Long> employeeIds);
}
