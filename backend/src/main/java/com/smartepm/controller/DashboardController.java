package com.smartepm.controller;

import com.smartepm.dto.response.ApiResponse;
import com.smartepm.dto.response.DashboardResponse;
import com.smartepm.security.UserPrincipal;
import com.smartepm.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Dashboard", description = "Admin and Employee dashboard summaries")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<DashboardResponse>> adminDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard fetched successfully", dashboardService.getAdminDashboard()));
    }

    @GetMapping("/employee")
    public ResponseEntity<ApiResponse<DashboardResponse>> employeeDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Employee dashboard fetched successfully",
                dashboardService.getEmployeeDashboard(principal.getEmployeeId())));
    }
}
