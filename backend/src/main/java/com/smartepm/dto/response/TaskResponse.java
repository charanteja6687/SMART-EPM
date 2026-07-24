package com.smartepm.dto.response;

import com.smartepm.entity.Priority;
import com.smartepm.entity.TaskStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private Integer progress;
    private LocalDate dueDate;
    private String remarks;
    private Long projectId;
    private String projectName;
    private Long employeeId;
    private String employeeName;
}
