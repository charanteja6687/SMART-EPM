package com.smartepm.repository;

import com.smartepm.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipient_IdOrderByCreatedAtDesc(Long employeeId);

    long countByRecipient_IdAndReadFalse(Long employeeId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient.id = :employeeId AND n.read = false")
    void markAllAsRead(@Param("employeeId") Long employeeId);
}
