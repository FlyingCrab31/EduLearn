package com.edulearn.notification.service;

import com.edulearn.notification.entity.Notification;
import java.util.List;

public interface NotificationService {
    Notification sendNotification(Notification notification);
    List<Notification> sendBulkNotification(List<Notification> notifications);
    void markAsRead(Long notificationId);
    void markAllRead(Long userId);
    List<Notification> getByUser(Long userId);
    long getUnreadCount(Long userId);
    void deleteNotification(Long notificationId);
    void sendEmailAlert(String to, String subject, String body);
}
