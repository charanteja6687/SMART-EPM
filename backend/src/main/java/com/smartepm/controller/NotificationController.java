package com.smartepm.controller;

import com.smartepm.dto.response.ApiResponse;
import com.smartepm.dto.response.NotificationResponse;
import com.smartepm.security.UserPrincipal;
import com.smartepm.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Tag(name = "Notifications", description = "In-app notifications for the logged-in employee")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getEmployeeId() == null) {
            return ResponseEntity.ok(ApiResponse.success("No notifications (account not linked to an employee)", Collections.emptyList()));
        }
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched successfully",
                notificationService.getMyNotifications(principal.getEmployeeId())));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        long count = principal.getEmployeeId() == null ? 0 : notificationService.getUnreadCount(principal.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("Unread count fetched successfully", Map.of("unreadCount", count)));
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Object>> markAllAsRead(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getEmployeeId() != null) {
            notificationService.markAllAsRead(principal.getEmployeeId());
        }
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Object>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }
}
