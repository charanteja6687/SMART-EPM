package com.smartepm.dto.request;

import com.smartepm.entity.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskProgressUpdateRequest {

    @Min(0)
    @Max(100)
    private Integer progress;

    private TaskStatus status;

    private String remarks;
}
