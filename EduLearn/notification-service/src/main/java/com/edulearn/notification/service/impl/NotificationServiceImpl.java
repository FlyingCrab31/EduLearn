package com.edulearn.notification.service.impl;

import com.edulearn.notification.entity.Notification;
import com.edulearn.notification.exception.NotificationNotFoundException;
import com.edulearn.notification.repository.NotificationRepository;
import com.edulearn.notification.service.NotificationService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender javaMailSender;

    public NotificationServiceImpl(NotificationRepository notificationRepository, JavaMailSender javaMailSender) {
        this.notificationRepository = notificationRepository;
        this.javaMailSender = javaMailSender;
    }

    @Override
    @Transactional
    public Notification sendNotification(Notification notification) {
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public List<Notification> sendBulkNotification(List<Notification> notifications) {
        notifications.forEach(notification -> {
            notification.setCreatedAt(LocalDateTime.now());
            notification.setIsRead(false);
        });
        return notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + notificationId));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsRead(userId, false);
        unreadNotifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    public List<Notification> getByUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new NotificationNotFoundException("Notification not found with ID: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public void sendEmailAlert(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("rishabhpan25@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
        } catch (Exception e) {
            // Log the exception but do not break the transaction or flow, just fallback
            System.err.println("Failed to send email alert to " + to + ": " + e.getMessage());
        }
    }
}
