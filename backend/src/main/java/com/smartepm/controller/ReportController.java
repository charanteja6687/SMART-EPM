package com.smartepm.controller;

import com.smartepm.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reports", description = "PDF/Excel report downloads (ADMIN only)")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/employee-tasks/pdf")
    public void employeeTasksPdf(HttpServletResponse response) throws IOException {
        reportService.exportEmployeeTaskReportPdf(response);
    }

    @GetMapping("/employee-tasks/excel")
    public void employeeTasksExcel(HttpServletResponse response) throws IOException {
        reportService.exportEmployeeTaskReportExcel(response);
    }

    @GetMapping("/project-progress/excel")
    public void projectProgressExcel(HttpServletResponse response) throws IOException {
        reportService.exportProjectProgressReportExcel(response);
    }

    @GetMapping("/pending-tasks/excel")
    public void pendingTasksExcel(HttpServletResponse response) throws IOException {
        reportService.exportPendingTasksReportExcel(response);
    }
}
