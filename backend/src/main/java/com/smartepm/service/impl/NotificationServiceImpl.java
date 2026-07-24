package com.smartepm.service.impl;

import com.smartepm.dto.response.NotificationResponse;
import com.smartepm.entity.Employee;
import com.smartepm.entity.Notification;
import com.smartepm.exception.ResourceNotFoundException;
import com.smartepm.repository.EmployeeRepository;
import com.smartepm.repository.NotificationRepository;
import com.smartepm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notify(Long employeeId, String message, String type, Long relatedEntityId) {
        if (employeeId == null) {
            return; // e.g. an unassigned task — nobody to notify
        }
        try {
            Employee employee = employeeRepository.findByIdAndDeletedAtIsNull(employeeId).orElse(null);
            if (employee == null) {
                return;
            }
            Notification notification = Notification.builder()
                    .recipient(employee)
                    .message(message)
                    .type(type)
                    .relatedEntityId(relatedEntityId)
                    .read(false)
                    .build();
            notificationRepository.save(notification);
    } catch (Exception ex) {
    // Audit logging is best-effort — never let a logging failure break the actual request.
}
    }

    @Override
    public List<NotificationResponse> getMyNotifications(Long employeeId) {
        return notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(employeeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(Long employeeId) {
        return notificationRepository.countByRecipient_IdAndReadFalse(employeeId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long employeeId) {
        notificationRepository.markAllAsRead(employeeId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .message(n.getMessage())
                .type(n.getType())
                .relatedEntityId(n.getRelatedEntityId())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
