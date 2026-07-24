package com.smartepm.controller;

import com.smartepm.dto.request.ProjectRequest;
import com.smartepm.dto.response.ApiResponse;
import com.smartepm.dto.response.PageResponse;
import com.smartepm.dto.response.ProjectResponse;
import com.smartepm.entity.Priority;
import com.smartepm.entity.ProjectStatus;
import com.smartepm.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Projects", description = "Project CRUD, employee assignment, soft-delete/restore. EMPLOYEE role only ever sees projects they're assigned to.")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> create(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", projectService.createProject(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", projectService.updateProject(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Project fetched successfully", projectService.getProjectById(id)));
    }

    /**
     * ADMIN sees all projects. EMPLOYEE only ever sees projects they're assigned to —
     * this is enforced server-side in ProjectServiceImpl and cannot be bypassed by the client.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        return ResponseEntity.ok(ApiResponse.success("Projects fetched successfully",
                projectService.searchProjects(keyword, status, priority, page, size, sortBy, direction)));
    }

    @PutMapping("/{id}/assign-employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> assignEmployees(@PathVariable Long id, @RequestBody Set<Long> employeeIds) {
        return ResponseEntity.ok(ApiResponse.success("Employees assigned successfully", projectService.assignEmployees(id, employeeIds)));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getDeleted() {
        return ResponseEntity.ok(ApiResponse.success("Deleted projects fetched successfully", projectService.getDeletedProjects()));
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> restore(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Project restored successfully", projectService.restoreProject(id)));
    }
}
