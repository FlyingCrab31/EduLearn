package com.edulearn.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * StudentController — Spring MVC controller for all student-facing views.
 *
 * <p>Integrates with the following microservices via a load-balanced RestTemplate:
 * <ul>
 *   <li>auth-service        — registration, login, profile</li>
 *   <li>course-service      — browse, search, course detail</li>
 *   <li>enrollment-service  — enroll, my courses</li>
 *   <li>lesson-service      — watch lesson</li>
 *   <li>assessment-service  — take quiz, submit quiz</li>
 *   <li>progress-service    — view progress, download certificate</li>
 *   <li>discussion-service  — forum, post thread, post reply</li>
 *   <li>notification-service — view notifications</li>
 * </ul>
 * </p>
 */
@Controller
@RequestMapping("/student")
public class StudentController {

    // ─── Service base-URLs (resolved via Eureka) ───────────────────────────
    @Value("${app.services.auth-url}")        private String authUrl;
    @Value("${app.services.course-url}")      private String courseUrl;
    @Value("${app.services.lesson-url}")      private String lessonUrl;
    @Value("${app.services.enrollment-url}")  private String enrollmentUrl;
    @Value("${app.services.assessment-url}")  private String assessmentUrl;
    @Value("${app.services.progress-url}")    private String progressUrl;
    @Value("${app.services.discussion-url}")  private String discussionUrl;
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
    // 1. Home
    // =========================================================================

    /**
     * GET /student/home
     * Renders the student home/landing page with featured courses.
     */
    @GetMapping({"/", "/home"})
    public String home(Model model, HttpSession session) {
        try {
            ResponseEntity<Object> featured = restTemplate.exchange(
                    courseUrl + "/api/v1/courses/featured",
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("featuredCourses", featured.getBody());
        } catch (Exception e) {
            model.addAttribute("featuredCourses", null);
            model.addAttribute("serviceError", "Unable to load featured courses.");
        }
        return "student/home";
    }

    // =========================================================================
    // 2. Auth — Register & Login
    // =========================================================================

    /**
     * GET /student/register — show registration form
     */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerRequest", Map.of());
        return "student/register";
    }

    /**
     * POST /student/register — submit registration to auth-service
     */
    @PostMapping("/register")
    public String register(@RequestParam Map<String, String> formData,
                           Model model,
                           HttpSession session) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    authUrl + "/api/v1/auth/register",
                    formData,
                    Map.class
            );
            model.addAttribute("successMessage", "Registration successful! Please log in.");
            return "redirect:/student/login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "student/register";
        }
    }

    /**
     * GET /student/login — show login form
     */
    @GetMapping("/login")
    public String loginForm(Model model) {
        return "student/login";
    }

    /**
     * POST /student/login — authenticate via auth-service, store JWT in session
     */
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model,
                        HttpSession session) {
        try {
            Map<String, String> credentials = Map.of("email", email, "password", password);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    authUrl + "/api/v1/auth/login",
                    credentials,
                    Map.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                session.setAttribute("jwt", response.getBody().get("token"));
                session.setAttribute("userId", response.getBody().get("userId"));
                session.setAttribute("userEmail", email);
                return "redirect:/student/home";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Login failed: " + e.getMessage());
        }
        return "student/login";
    }

    // =========================================================================
    // 3. Profile
    // =========================================================================

    /**
     * GET /student/profile — view student profile
     */
    @GetMapping("/profile")
    public String viewProfile(Model model, HttpSession session) {
        try {
            String userId = (String) session.getAttribute("userId");
            ResponseEntity<Object> profile = restTemplate.exchange(
                    authUrl + "/api/v1/users/" + userId,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("profile", profile.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load profile.");
        }
        return "student/profile";
    }

    // =========================================================================
    // 4. Course Browsing
    // =========================================================================

    /**
     * GET /student/courses — browse all published courses
     */
    @GetMapping("/courses")
    public String browseCourses(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "12") int size,
                                @RequestParam(required = false) String category,
                                Model model,
                                HttpSession session) {
        try {
            String url = courseUrl + "/api/v1/courses?page=" + page + "&size=" + size
                    + (category != null ? "&category=" + category : "");
            ResponseEntity<Object> courses = restTemplate.exchange(
                    url, HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("courses", courses.getBody());
            model.addAttribute("currentPage", page);
            model.addAttribute("category", category);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load courses.");
        }
        return "student/courses";
    }

    /**
     * GET /student/courses/search?q= — search courses by keyword
     */
    @GetMapping("/courses/search")
    public String searchCourses(@RequestParam String q,
                                @RequestParam(defaultValue = "0") int page,
                                Model model,
                                HttpSession session) {
        try {
            ResponseEntity<Object> results = restTemplate.exchange(
                    courseUrl + "/api/v1/courses/search?keyword=" + q + "&page=" + page,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("courses", results.getBody());
            model.addAttribute("query", q);
        } catch (Exception e) {
            model.addAttribute("error", "Search failed.");
        }
        return "student/courses";
    }

    /**
     * GET /student/courses/{courseId} — view course detail page
     */
    @GetMapping("/courses/{courseId}")
    public String viewCourseDetail(@PathVariable String courseId,
                                   Model model,
                                   HttpSession session) {
        try {
            ResponseEntity<Object> course = restTemplate.exchange(
                    courseUrl + "/api/v1/courses/" + courseId,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("course", course.getBody());

            // Fetch lessons overview
            ResponseEntity<Object> lessons = restTemplate.exchange(
                    lessonUrl + "/api/v1/lessons/course/" + courseId,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("lessons", lessons.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load course details.");
        }
        return "student/course-detail";
    }

    // =========================================================================
    // 5. Enrollment
    // =========================================================================

    /**
     * POST /student/courses/{courseId}/enroll — enroll student in a course
     */
    @PostMapping("/courses/{courseId}/enroll")
    public String enrollCourse(@PathVariable String courseId,
                               HttpSession session,
                               Model model) {
        try {
            String userId = (String) session.getAttribute("userId");
            Map<String, String> payload = Map.of("studentId", userId, "courseId", courseId);
            restTemplate.exchange(
                    enrollmentUrl + "/api/v1/enrollments",
                    HttpMethod.POST,
                    new HttpEntity<>(payload, authHeaders(session)),
                    Object.class
            );
            return "redirect:/student/my-courses";
        } catch (Exception e) {
            model.addAttribute("error", "Enrollment failed: " + e.getMessage());
            return "redirect:/student/courses/" + courseId;
        }
    }

    /**
     * GET /student/my-courses — list all courses the student is enrolled in
     */
    @GetMapping("/my-courses")
    public String viewMyCourses(Model model, HttpSession session) {
        try {
            String userId = (String) session.getAttribute("userId");
            ResponseEntity<Object> enrollments = restTemplate.exchange(
                    enrollmentUrl + "/api/v1/enrollments/student/" + userId,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("enrollments", enrollments.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load your courses.");
        }
        return "student/my-courses";
    }

    // =========================================================================
    // 6. Lesson
    // =========================================================================

    /**
     * GET /student/lessons/{lessonId} — watch / read a lesson
     */
    @GetMapping("/lessons/{lessonId}")
    public String watchLesson(@PathVariable String lessonId,
                              Model model,
                              HttpSession session) {
        try {
            ResponseEntity<Object> lesson = restTemplate.exchange(
                    lessonUrl + "/api/v1/lessons/" + lessonId,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("lesson", lesson.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load lesson.");
        }
        return "student/lesson";
    }

    // =========================================================================
    // 7. Quiz / Assessment
    // =========================================================================

    /**
     * GET /student/quizzes/{quizId} — start quiz attempt view
     */
    @GetMapping("/quizzes/{quizId}")
    public String takeQuiz(@PathVariable String quizId,
                           Model model,
                           HttpSession session) {
        try {
            ResponseEntity<Object> quiz = restTemplate.exchange(
                    assessmentUrl + "/api/v1/quizzes/" + quizId,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("quiz", quiz.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load quiz.");
        }
        return "student/quiz";
    }

    /**
     * POST /student/quizzes/{quizId}/submit — submit quiz answers to assessment-service
     */
    @PostMapping("/quizzes/{quizId}/submit")
    public String submitQuiz(@PathVariable String quizId,
                             @RequestParam Map<String, String> answers,
                             HttpSession session,
                             Model model) {
        try {
            String userId = (String) session.getAttribute("userId");
            Map<String, Object> payload = Map.of(
                    "studentId", userId,
                    "quizId", quizId,
                    "answers", answers
            );
            ResponseEntity<Object> result = restTemplate.exchange(
                    assessmentUrl + "/api/v1/attempts",
                    HttpMethod.POST,
                    new HttpEntity<>(payload, authHeaders(session)),
                    Object.class
            );
            model.addAttribute("result", result.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not submit quiz.");
        }
        return "student/quiz-result";
    }

    // =========================================================================
    // 8. Progress & Certificate
    // =========================================================================

    /**
     * GET /student/progress/{courseId} — view lesson-level progress for a course
     */
    @GetMapping("/progress/{courseId}")
    public String viewProgress(@PathVariable String courseId,
                               Model model,
                               HttpSession session) {
        try {
            String userId = (String) session.getAttribute("userId");
            ResponseEntity<Object> progress = restTemplate.exchange(
                    progressUrl + "/api/v1/progress/student/" + userId + "/course/" + courseId,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("progress", progress.getBody());
            model.addAttribute("courseId", courseId);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load progress.");
        }
        return "student/progress";
    }

    /**
     * GET /student/certificates/{courseId} — download PDF certificate
     */
    @GetMapping("/certificates/{courseId}")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable String courseId,
                                                       HttpSession session) {
        try {
            String userId = (String) session.getAttribute("userId");
            ResponseEntity<byte[]> cert = restTemplate.exchange(
                    progressUrl + "/api/v1/certificates/student/" + userId + "/course/" + courseId,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    byte[].class
            );
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"certificate-" + courseId + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(cert.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    // =========================================================================
    // 9. Discussion Forum
    // =========================================================================

    /**
     * GET /student/courses/{courseId}/forum — view course forum threads
     */
    @GetMapping("/courses/{courseId}/forum")
    public String viewForum(@PathVariable String courseId,
                            Model model,
                            HttpSession session) {
        try {
            ResponseEntity<Object> threads = restTemplate.exchange(
                    discussionUrl + "/api/v1/threads/course/" + courseId,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("threads", threads.getBody());
            model.addAttribute("courseId", courseId);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load forum.");
        }
        return "student/forum";
    }

    /**
     * POST /student/courses/{courseId}/forum/thread — post a new forum thread
     */
    @PostMapping("/courses/{courseId}/forum/thread")
    public String postThread(@PathVariable String courseId,
                             @RequestParam String title,
                             @RequestParam String content,
                             HttpSession session) {
        try {
            String userId = (String) session.getAttribute("userId");
            Map<String, String> payload = Map.of(
                    "courseId", courseId,
                    "authorId", userId,
                    "title", title,
                    "content", content
            );
            restTemplate.exchange(
                    discussionUrl + "/api/v1/threads",
                    HttpMethod.POST,
                    new HttpEntity<>(payload, authHeaders(session)),
                    Object.class
            );
        } catch (Exception ignored) { }
        return "redirect:/student/courses/" + courseId + "/forum";
    }

    /**
     * POST /student/threads/{threadId}/reply — post a reply to a forum thread
     */
    @PostMapping("/threads/{threadId}/reply")
    public String postReply(@PathVariable String threadId,
                            @RequestParam String content,
                            @RequestParam String courseId,
                            HttpSession session) {
        try {
            String userId = (String) session.getAttribute("userId");
            Map<String, String> payload = Map.of(
                    "threadId", threadId,
                    "authorId", userId,
                    "content", content
            );
            restTemplate.exchange(
                    discussionUrl + "/api/v1/replies",
                    HttpMethod.POST,
                    new HttpEntity<>(payload, authHeaders(session)),
                    Object.class
            );
        } catch (Exception ignored) { }
        return "redirect:/student/courses/" + courseId + "/forum";
    }

    // =========================================================================
    // 10. Notifications
    // =========================================================================

    /**
     * GET /student/notifications — view in-app notifications
     */
    @GetMapping("/notifications")
    public String viewNotifications(Model model, HttpSession session) {
        try {
            String userId = (String) session.getAttribute("userId");
            ResponseEntity<Object> notifications = restTemplate.exchange(
                    notificationUrl + "/api/v1/notifications/user/" + userId,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("notifications", notifications.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load notifications.");
        }
        return "student/notifications";
    }
}
