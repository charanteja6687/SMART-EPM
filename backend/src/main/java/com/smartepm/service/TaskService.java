package com.smartepm.service;

import com.smartepm.dto.request.TaskProgressUpdateRequest;
import com.smartepm.dto.request.TaskRequest;
import com.smartepm.dto.response.PageResponse;
import com.smartepm.dto.response.TaskResponse;
import com.smartepm.entity.Priority;
import com.smartepm.entity.TaskStatus;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(TaskRequest request);
    TaskResponse updateTask(Long id, TaskRequest request);
    TaskResponse updateTaskProgress(Long id, TaskProgressUpdateRequest request);
    void deleteTask(Long id);
    TaskResponse restoreTask(Long id);
    List<TaskResponse> getDeletedTasks();
    TaskResponse getTaskById(Long id);
    PageResponse<TaskResponse> searchTasks(String keyword, TaskStatus status, Priority priority, Long projectId, Long employeeId, int page, int size, String sortBy, String direction);
}
