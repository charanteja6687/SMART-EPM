package com.smartepm.service;

import com.smartepm.dto.response.DashboardResponse;

public interface DashboardService {
    DashboardResponse getAdminDashboard();
    DashboardResponse getEmployeeDashboard(Long employeeId);
}
