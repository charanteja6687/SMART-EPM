package com.smartepm.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogResponse {
    private Long id;
    private String actorUsername;
    private String action;
    private String entityType;
    private Long entityId;
    private String description;
    private LocalDateTime timestamp;
}
