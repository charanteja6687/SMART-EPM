package com.smartepm.controller;

import com.smartepm.dto.response.ActivityLogResponse;
import com.smartepm.dto.response.ApiResponse;
import com.smartepm.dto.response.PageResponse;
import com.smartepm.service.ActivityLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Activity Log", description = "Audit trail of create/update/delete/login actions across the system (ADMIN only)")
@RestController
@RequestMapping("/api/activity-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ActivityLogResponse>>> search(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String actorUsername,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Activity log fetched successfully",
                activityLogService.search(entityType, actorUsername, page, size)));
    }
}
