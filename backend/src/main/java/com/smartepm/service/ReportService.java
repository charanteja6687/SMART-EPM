package com.smartepm.service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface ReportService {
    void exportEmployeeTaskReportPdf(HttpServletResponse response) throws IOException;
    void exportEmployeeTaskReportExcel(HttpServletResponse response) throws IOException;
    void exportProjectProgressReportExcel(HttpServletResponse response) throws IOException;
    void exportPendingTasksReportExcel(HttpServletResponse response) throws IOException;
}
