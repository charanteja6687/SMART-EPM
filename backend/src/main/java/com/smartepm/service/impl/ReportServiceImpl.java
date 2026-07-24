package com.smartepm.service.impl;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.smartepm.entity.Employee;
import com.smartepm.entity.Project;
import com.smartepm.entity.Task;
import com.smartepm.entity.TaskStatus;
import com.smartepm.repository.EmployeeRepository;
import com.smartepm.repository.ProjectRepository;
import com.smartepm.repository.TaskRepository;
import com.smartepm.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Override
    public void exportEmployeeTaskReportPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=employee_task_report.pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        document.add(new com.itextpdf.layout.element.Paragraph("Employee-wise Task Report")
                .setBold().setFontSize(18));

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2, 2}))
                .useAllAvailableWidth();
        table.addHeaderCell(headerCell("Employee"));
        table.addHeaderCell(headerCell("Department"));
        table.addHeaderCell(headerCell("Total Tasks"));
        table.addHeaderCell(headerCell("Completed"));
        table.addHeaderCell(headerCell("Pending"));

        List<Employee> employees = employeeRepository.findByDeletedAtIsNull();
        for (Employee e : employees) {
            List<Task> tasks = taskRepository.findByAssignedTo_IdAndDeletedAtIsNull(e.getId());
            long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
            long pending = tasks.size() - completed;

            table.addCell(new Cell().add(new com.itextpdf.layout.element.Paragraph(e.getFullName())));
            table.addCell(new Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(e.getDepartment()))));
            table.addCell(new Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(tasks.size()))));
            table.addCell(new Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(completed))));
            table.addCell(new Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(pending))));
        }

        document.add(table);
        document.close();
    }

    @Override
    public void exportEmployeeTaskReportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=employee_task_report.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Employee Task Report");
            int rowIdx = 0;

            Row header = sheet.createRow(rowIdx++);
            String[] headers = {"Employee", "Department", "Designation", "Total Tasks", "Completed", "Pending"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            List<Employee> employees = employeeRepository.findByDeletedAtIsNull();
            for (Employee e : employees) {
                List<Task> tasks = taskRepository.findByAssignedTo_IdAndDeletedAtIsNull(e.getId());
                long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
                long pending = tasks.size() - completed;

                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(e.getFullName());
                row.createCell(1).setCellValue(String.valueOf(e.getDepartment()));
                row.createCell(2).setCellValue(String.valueOf(e.getDesignation()));
                row.createCell(3).setCellValue(tasks.size());
                row.createCell(4).setCellValue(completed);
                row.createCell(5).setCellValue(pending);
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (OutputStream os = response.getOutputStream()) {
                workbook.write(os);
            }
        }
    }

    @Override
    public void exportProjectProgressReportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=project_progress_report.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Project Progress Report");
            int rowIdx = 0;

            Row header = sheet.createRow(rowIdx++);
            String[] headers = {"Project", "Status", "Priority", "Deadline", "Total Tasks", "Completed Tasks", "Progress %"};
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);

            List<Project> projects = projectRepository.findByDeletedAtIsNull();
            for (Project p : projects) {
                long total = taskRepository.countByProject_IdAndDeletedAtIsNull(p.getId());
                long completed = taskRepository.countByProject_IdAndStatusAndDeletedAtIsNull(p.getId(), TaskStatus.COMPLETED);
                double progress = total == 0 ? 0 : (completed * 100.0) / total;

                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getName());
                row.createCell(1).setCellValue(p.getStatus().name());
                row.createCell(2).setCellValue(p.getPriority().name());
                row.createCell(3).setCellValue(p.getDeadline() != null ? p.getDeadline().toString() : "");
                row.createCell(4).setCellValue(total);
                row.createCell(5).setCellValue(completed);
                row.createCell(6).setCellValue(Math.round(progress * 100.0) / 100.0);
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (OutputStream os = response.getOutputStream()) {
                workbook.write(os);
            }
        }
    }

    @Override
    public void exportPendingTasksReportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=pending_tasks_report.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Pending Tasks Report");
            int rowIdx = 0;

            Row header = sheet.createRow(rowIdx++);
            String[] headers = {"Task", "Project", "Assigned To", "Status", "Priority", "Due Date", "Progress %"};
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);

            List<Task> tasks = taskRepository.findByDeletedAtIsNull().stream()
                    .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                    .toList();

            for (Task t : tasks) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getTitle());
                row.createCell(1).setCellValue(t.getProject() != null ? t.getProject().getName() : "");
                row.createCell(2).setCellValue(t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : "Unassigned");
                row.createCell(3).setCellValue(t.getStatus().name());
                row.createCell(4).setCellValue(t.getPriority().name());
                row.createCell(5).setCellValue(t.getDueDate() != null ? t.getDueDate().toString() : "");
                row.createCell(6).setCellValue(t.getProgress());
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (OutputStream os = response.getOutputStream()) {
                workbook.write(os);
            }
        }
    }

    private Cell headerCell(String text) {
        return new Cell().add(new com.itextpdf.layout.element.Paragraph(text).setBold());
    }
}
