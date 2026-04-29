package com.edulearn.notification.resource;

import com.edulearn.notification.entity.Notification;
import com.edulearn.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationResource {

    private final NotificationService notificationService;

    public NotificationResource(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Notification> sendNotification(@RequestBody Notification notification) {
        return new ResponseEntity<>(notificationService.sendNotification(notification), HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Notification>> sendBulkNotification(@RequestBody List<Notification> notifications) {
        return new ResponseEntity<>(notificationService.sendBulkNotification(notifications), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getByUser(userId));
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllRead(@PathVariable Long userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
    
    // Test endpoint to trigger email
    @PostMapping("/test-email")
    public ResponseEntity<String> testEmail(@RequestParam String to, @RequestParam String subject, @RequestBody String body) {
        notificationService.sendEmailAlert(to, subject, body);
        return ResponseEntity.ok("Email send request dispatched to: " + to);
    }
}
