package com.edulearn.notification;

import com.edulearn.notification.entity.Notification;
import com.edulearn.notification.exception.NotificationNotFoundException;
import com.edulearn.notification.repository.NotificationRepository;
import com.edulearn.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        sampleNotification = new Notification(1L, "TEST", "Test Title", "Test Message", false, null, 100L, "COURSE");
        sampleNotification.setNotificationId(1L);
    }

    @Test
    void testSendNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        Notification saved = notificationService.sendNotification(sampleNotification);

        assertNotNull(saved.getCreatedAt());
        assertFalse(saved.getIsRead());
        verify(notificationRepository, times(1)).save(sampleNotification);
    }

    @Test
    void testGetByUser() {
        when(notificationRepository.findByUserId(1L)).thenReturn(Arrays.asList(sampleNotification));

        List<Notification> notifications = notificationService.getByUser(1L);

        assertEquals(1, notifications.size());
        assertEquals("Test Title", notifications.get(0).getTitle());
        verify(notificationRepository, times(1)).findByUserId(1L);
    }

    @Test
    void testGetUnreadCount() {
        when(notificationRepository.countByUserIdAndIsRead(1L, false)).thenReturn(5L);

        long count = notificationService.getUnreadCount(1L);

        assertEquals(5L, count);
        verify(notificationRepository, times(1)).countByUserIdAndIsRead(1L, false);
    }

    @Test
    void testMarkAsRead() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        notificationService.markAsRead(1L);

        assertTrue(sampleNotification.getIsRead());
        verify(notificationRepository, times(1)).save(sampleNotification);
    }

    @Test
    void testMarkAsReadThrowsException() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () -> notificationService.markAsRead(1L));
    }

    @Test
    void testDeleteNotification() {
        when(notificationRepository.existsById(1L)).thenReturn(true);

        notificationService.deleteNotification(1L);

        verify(notificationRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteNotificationThrowsException() {
        when(notificationRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotificationNotFoundException.class, () -> notificationService.deleteNotification(1L));
    }

    @Test
    void testSendEmailAlert() {
        // Just verify it doesn't throw and attempts to send
        notificationService.sendEmailAlert("test@test.com", "Subject", "Body");
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
