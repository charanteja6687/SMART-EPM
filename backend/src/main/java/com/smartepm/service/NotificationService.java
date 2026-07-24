package com.smartepm.service;

import com.smartepm.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    /** Fire-and-forget: creates a notification for the given employee. Safe to call even if employeeId is null (no-op). */
    void notify(Long employeeId, String message, String type, Long relatedEntityId);

    List<NotificationResponse> getMyNotifications(Long employeeId);

    long getUnreadCount(Long employeeId);

    void markAllAsRead(Long employeeId);

    void markAsRead(Long notificationId);
}
