package com.smartepm.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private String message;
    private String type;
    private Long relatedEntityId;
    private boolean read;
    private LocalDateTime createdAt;
}
