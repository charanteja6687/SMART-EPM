package com.smartepm.controller;

import com.smartepm.dto.request.TaskProgressUpdateRequest;
import com.smartepm.dto.request.TaskRequest;
import com.smartepm.dto.response.ApiResponse;
import com.smartepm.dto.response.PageResponse;
import com.smartepm.dto.response.TaskResponse;
import com.smartepm.entity.Priority;
import com.smartepm.entity.TaskStatus;
import com.smartepm.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Tasks", description = "Task CRUD, progress updates, soft-delete/restore")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaskResponse>> create(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", taskService.createTask(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaskResponse>> update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", taskService.updateTask(id, request)));
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<TaskResponse>> updateProgress(@PathVariable Long id, @Valid @RequestBody TaskProgressUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task progress updated successfully", taskService.updateTaskProgress(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Task fetched successfully", taskService.getTaskById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        return ResponseEntity.ok(ApiResponse.success("Tasks fetched successfully",
                taskService.searchTasks(keyword, status, priority, projectId, employeeId, page, size, sortBy, direction)));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<java.util.List<TaskResponse>>> getDeleted() {
        return ResponseEntity.ok(ApiResponse.success("Deleted tasks fetched successfully", taskService.getDeletedTasks()));
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaskResponse>> restore(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Task restored successfully", taskService.restoreTask(id)));
    }
}
