package com.edulearn.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * AdminController — Spring MVC controller for all administrator-facing views.
 *
 * <p>Integrates with the following microservices via a load-balanced RestTemplate:
 * <ul>
 *   <li>auth-service         — manage / suspend / delete users</li>
 *   <li>course-service       — manage, approve, reject courses</li>
 *   <li>enrollment-service   — view all enrollments</li>
 *   <li>payment-service      — view all payments, manage subscriptions</li>
 *   <li>progress-service     — view all certificates</li>
 *   <li>discussion-service   — moderate discussions</li>
 *   <li>notification-service — send platform-wide notifications</li>
 * </ul>
 * </p>
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Value("${app.services.auth-url}")         private String authUrl;
    @Value("${app.services.course-url}")       private String courseUrl;
    @Value("${app.services.enrollment-url}")   private String enrollmentUrl;
    @Value("${app.services.payment-url}")      private String paymentUrl;
    @Value("${app.services.progress-url}")     private String progressUrl;
    @Value("${app.services.discussion-url}")   private String discussionUrl;
    @Value("${app.services.notification-url}") private String notificationUrl;

    @Autowired
    private RestTemplate restTemplate;

    // ─── Helper: build auth header from session JWT ─────────────────────────
    private HttpHeaders authHeaders(HttpSession session) {
        HttpHeaders headers = new HttpHeaders();
        String token = (String) session.getAttribute("jwt");
        if (token != null) {
            headers.setBearerAuth(token);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // =========================================================================
    // 1. Dashboard
    // =========================================================================

    /**
     * GET /admin/dashboard — admin overview with platform-wide KPIs
     */
    @GetMapping({"/", "/dashboard"})
    public String adminDashboard(Model model, HttpSession session) {
        try {
            // Total users
            ResponseEntity<Object> userStats = restTemplate.exchange(
                    authUrl + "/api/v1/admin/users/stats",
                    HttpMethod.GET, new HttpEntity<>(authHeaders(session)), Object.class);
            model.addAttribute("userStats", userStats.getBody());

            // Total courses
            ResponseEntity<Object> courseStats = restTemplate.exchange(
                    courseUrl + "/api/v1/admin/courses/stats",
                    HttpMethod.GET, new HttpEntity<>(authHeaders(session)), Object.class);
            model.addAttribute("courseStats", courseStats.getBody());

            // Revenue summary
            ResponseEntity<Object> revenueStats = restTemplate.exchange(
                    paymentUrl + "/api/v1/admin/payments/stats",
                    HttpMethod.GET, new HttpEntity<>(authHeaders(session)), Object.class);
            model.addAttribute("revenueStats", revenueStats.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load dashboard data.");
        }
        return "admin/dashboard";
    }

    // =========================================================================
    // 2. User Management
    // =========================================================================

    /**
     * GET /admin/users — list all users (students, instructors, admins)
     */
    @GetMapping("/users")
    public String manageUsers(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(required = false) String role,
                              Model model,
                              HttpSession session) {
        try {
            String url = authUrl + "/api/v1/admin/users?page=" + page
                    + (role != null ? "&role=" + role : "");
            ResponseEntity<Object> users = restTemplate.exchange(
                    url, HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)), Object.class);
            model.addAttribute("users", users.getBody());
            model.addAttribute("currentPage", page);
            model.addAttribute("role", role);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load users.");
        }
        return "admin/manage-users";
    }

    /**
     * POST /admin/users/{userId}/suspend — suspend (disable) a user account
     */
    @PostMapping("/users/{userId}/suspend")
    public String suspendUser(@PathVariable String userId, HttpSession session) {
        try {
            restTemplate.exchange(
                    authUrl + "/api/v1/admin/users/" + userId + "/suspend",
                    HttpMethod.POST,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
        } catch (Exception ignored) { }
        return "redirect:/admin/users";
    }

    /**
     * POST /admin/users/{userId}/delete — permanently delete a user account
     */
    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable String userId, HttpSession session) {
        try {
            restTemplate.exchange(
                    authUrl + "/api/v1/admin/users/" + userId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
        } catch (Exception ignored) { }
        return "redirect:/admin/users";
    }

    // =========================================================================
    // 3. Course Management
    // =========================================================================

    /**
     * GET /admin/courses — list all courses with status filters
     */
    @GetMapping("/courses")
    public String manageAllCourses(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(required = false) String status,
                                   Model model,
                                   HttpSession session) {
        try {
            String url = courseUrl + "/api/v1/admin/courses?page=" + page
                    + (status != null ? "&status=" + status : "");
            ResponseEntity<Object> courses = restTemplate.exchange(
                    url, HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)), Object.class);
            model.addAttribute("courses", courses.getBody());
            model.addAttribute("currentPage", page);
            model.addAttribute("status", status);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load courses.");
        }
        return "admin/manage-courses";
    }

    /**
     * POST /admin/courses/{courseId}/approve — approve a pending course for publishing
     */
    @PostMapping("/courses/{courseId}/approve")
    public String approveCourse(@PathVariable String courseId, HttpSession session) {
        try {
            restTemplate.exchange(
                    courseUrl + "/api/v1/admin/courses/" + courseId + "/approve",
                    HttpMethod.POST,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
        } catch (Exception ignored) { }
        return "redirect:/admin/courses";
    }

    /**
     * POST /admin/courses/{courseId}/reject — reject a course submission
     */
    @PostMapping("/courses/{courseId}/reject")
    public String rejectCourse(@PathVariable String courseId,
                               @RequestParam(required = false) String reason,
                               HttpSession session) {
        try {
            Map<String, String> payload = Map.of("reason", reason != null ? reason : "");
            restTemplate.exchange(
                    courseUrl + "/api/v1/admin/courses/" + courseId + "/reject",
                    HttpMethod.POST,
                    new HttpEntity<>(payload, authHeaders(session)),
                    Object.class
            );
        } catch (Exception ignored) { }
        return "redirect:/admin/courses";
    }

    // =========================================================================
    // 4. Enrollment Overview
    // =========================================================================

    /**
     * GET /admin/enrollments — view all enrollments across the platform
     */
    @GetMapping("/enrollments")
    public String viewAllEnrollments(@RequestParam(defaultValue = "0") int page,
                                     Model model,
                                     HttpSession session) {
        try {
            ResponseEntity<Object> enrollments = restTemplate.exchange(
                    enrollmentUrl + "/api/v1/admin/enrollments?page=" + page,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("enrollments", enrollments.getBody());
            model.addAttribute("currentPage", page);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load enrollments.");
        }
        return "admin/enrollments";
    }

    // =========================================================================
    // 5. Platform Analytics
    // =========================================================================

    /**
     * GET /admin/analytics — platform-wide analytics (users, revenue, completions)
     */
    @GetMapping("/analytics")
    public String viewPlatformAnalytics(Model model, HttpSession session) {
        try {
            ResponseEntity<Object> userAnalytics = restTemplate.exchange(
                    authUrl + "/api/v1/admin/analytics/users",
                    HttpMethod.GET, new HttpEntity<>(authHeaders(session)), Object.class);
            model.addAttribute("userAnalytics", userAnalytics.getBody());

            ResponseEntity<Object> courseAnalytics = restTemplate.exchange(
                    courseUrl + "/api/v1/admin/analytics/courses",
                    HttpMethod.GET, new HttpEntity<>(authHeaders(session)), Object.class);
            model.addAttribute("courseAnalytics", courseAnalytics.getBody());

            ResponseEntity<Object> revenueAnalytics = restTemplate.exchange(
                    paymentUrl + "/api/v1/admin/analytics/revenue",
                    HttpMethod.GET, new HttpEntity<>(authHeaders(session)), Object.class);
            model.addAttribute("revenueAnalytics", revenueAnalytics.getBody());

            ResponseEntity<Object> completionAnalytics = restTemplate.exchange(
                    progressUrl + "/api/v1/admin/analytics/completions",
                    HttpMethod.GET, new HttpEntity<>(authHeaders(session)), Object.class);
            model.addAttribute("completionAnalytics", completionAnalytics.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load analytics.");
        }
        return "admin/analytics";
    }

    // =========================================================================
    // 6. Payments & Subscriptions
    // =========================================================================

    /**
     * GET /admin/payments — view all payment transactions
     */
    @GetMapping("/payments")
    public String viewAllPayments(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(required = false) String status,
                                  Model model,
                                  HttpSession session) {
        try {
            String url = paymentUrl + "/api/v1/admin/payments?page=" + page
                    + (status != null ? "&status=" + status : "");
            ResponseEntity<Object> payments = restTemplate.exchange(
                    url, HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)), Object.class);
            model.addAttribute("payments", payments.getBody());
            model.addAttribute("currentPage", page);
            model.addAttribute("status", status);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load payments.");
        }
        return "admin/payments";
    }

    /**
     * GET /admin/subscriptions — manage platform subscription plans
     */
    @GetMapping("/subscriptions")
    public String manageSubscriptions(Model model, HttpSession session) {
        try {
            ResponseEntity<Object> subscriptions = restTemplate.exchange(
                    paymentUrl + "/api/v1/admin/subscriptions",
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("subscriptions", subscriptions.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load subscriptions.");
        }
        return "admin/subscriptions";
    }

    // =========================================================================
    // 7. Certificates
    // =========================================================================

    /**
     * GET /admin/certificates — view all issued certificates across the platform
     */
    @GetMapping("/certificates")
    public String viewAllCertificates(@RequestParam(defaultValue = "0") int page,
                                      Model model,
                                      HttpSession session) {
        try {
            ResponseEntity<Object> certificates = restTemplate.exchange(
                    progressUrl + "/api/v1/admin/certificates?page=" + page,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("certificates", certificates.getBody());
            model.addAttribute("currentPage", page);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load certificates.");
        }
        return "admin/certificates";
    }

    // =========================================================================
    // 8. Notifications
    // =========================================================================

    /**
     * GET /admin/notifications/send — show bulk notification form
     */
    @GetMapping("/notifications/send")
    public String sendNotificationForm(Model model) {
        return "admin/send-notification";
    }

    /**
     * POST /admin/notifications/send — send platform-wide notification via notification-service
     */
    @PostMapping("/notifications/send")
    public String sendPlatformNotification(@RequestParam String title,
                                           @RequestParam String message,
                                           @RequestParam(required = false) String targetRole,
                                           HttpSession session,
                                           Model model) {
        try {
            Map<String, String> payload = Map.of(
                    "title", title,
                    "message", message,
                    "targetRole", targetRole != null ? targetRole : "ALL"
            );
            restTemplate.exchange(
                    notificationUrl + "/api/v1/notifications/bulk",
                    HttpMethod.POST,
                    new HttpEntity<>(payload, authHeaders(session)),
                    Object.class
            );
            model.addAttribute("success", "Notification sent successfully.");
        } catch (Exception e) {
            model.addAttribute("error", "Could not send notification: " + e.getMessage());
        }
        return "admin/send-notification";
    }

    // =========================================================================
    // 9. Discussion Moderation
    // =========================================================================

    /**
     * GET /admin/discussions — view all flagged / pending discussion threads
     */
    @GetMapping("/discussions")
    public String viewDiscussions(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(required = false) String filter,
                                  Model model,
                                  HttpSession session) {
        try {
            String url = discussionUrl + "/api/v1/admin/threads?page=" + page
                    + (filter != null ? "&filter=" + filter : "");
            ResponseEntity<Object> threads = restTemplate.exchange(
                    url, HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)), Object.class);
            model.addAttribute("threads", threads.getBody());
            model.addAttribute("currentPage", page);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load discussions.");
        }
        return "admin/discussions";
    }

    /**
     * POST /admin/discussions/{threadId}/moderate — take moderation action on a thread
     *     (action: HIDE | DELETE | RESTORE)
     */
    @PostMapping("/discussions/{threadId}/moderate")
    public String moderateDiscussion(@PathVariable String threadId,
                                     @RequestParam String action,
                                     HttpSession session) {
        try {
            Map<String, String> payload = Map.of("action", action);
            restTemplate.exchange(
                    discussionUrl + "/api/v1/admin/threads/" + threadId + "/moderate",
                    HttpMethod.POST,
                    new HttpEntity<>(payload, authHeaders(session)),
                    Object.class
            );
        } catch (Exception ignored) { }
        return "redirect:/admin/discussions";
    }
}
