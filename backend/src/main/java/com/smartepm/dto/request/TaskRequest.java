package com.smartepm.dto.request;

import com.smartepm.entity.Priority;
import com.smartepm.entity.TaskStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private TaskStatus status;

    private Priority priority;

    @Min(0)
    @Max(100)
    private Integer progress;

    private LocalDate dueDate;

    private String remarks;

    @NotNull(message = "Project id is required")
    private Long projectId;

    private Long employeeId;
}
