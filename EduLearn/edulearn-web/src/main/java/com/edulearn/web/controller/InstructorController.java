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
 * InstructorController — Spring MVC controller for all instructor-facing views.
 *
 * <p>Integrates with the following microservices via a load-balanced RestTemplate:
 * <ul>
 *   <li>course-service      — create, edit, delete, publish course</li>
 *   <li>lesson-service      — add, edit, delete lesson, add resource</li>
 *   <li>assessment-service  — create quiz, add question</li>
 *   <li>enrollment-service  — view enrollments</li>
 *   <li>progress-service    — view student progress</li>
 *   <li>discussion-service  — view / moderate forum threads</li>
 * </ul>
 * </p>
 */
@Controller
@RequestMapping("/instructor")
public class InstructorController {

    @Value("${app.services.auth-url}")        private String authUrl;
    @Value("${app.services.course-url}")      private String courseUrl;
    @Value("${app.services.lesson-url}")      private String lessonUrl;
    @Value("${app.services.enrollment-url}")  private String enrollmentUrl;
    @Value("${app.services.assessment-url}")  private String assessmentUrl;
    @Value("${app.services.progress-url}")    private String progressUrl;
    @Value("${app.services.discussion-url}")  private String discussionUrl;

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
     * GET /instructor/dashboard — instructor overview page
     */
    @GetMapping({"/", "/dashboard"})
    public String instructorDashboard(Model model, HttpSession session) {
        try {
            String instructorId = (String) session.getAttribute("userId");

            // Instructor's courses
            ResponseEntity<Object> courses = restTemplate.exchange(
                    courseUrl + "/api/v1/courses/instructor/" + instructorId,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("courses", courses.getBody());

            // Total enrollments across all courses
            ResponseEntity<Object> enrollStats = restTemplate.exchange(
                    enrollmentUrl + "/api/v1/enrollments/instructor/" + instructorId + "/stats",
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("enrollStats", enrollStats.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Could not load dashboard data.");
        }
        return "instructor/dashboard";
    }

    // =========================================================================
    // 2. Course Management
    // =========================================================================

    /**
     * GET /instructor/courses/create — show create-course form
     */
    @GetMapping("/courses/create")
    public String createCourseForm(Model model) {
        model.addAttribute("courseForm", Map.of());
        return "instructor/create-course";
    }

    /**
     * POST /instructor/courses/create — submit new course to course-service
     */
    @PostMapping("/courses/create")
    public String createCourse(@RequestParam Map<String, String> formData,
                               HttpSession session,
                               Model model) {
        try {
            String instructorId = (String) session.getAttribute("userId");
            formData.put("instructorId", instructorId);
            ResponseEntity<Map> response = restTemplate.exchange(
                    courseUrl + "/api/v1/courses",
                    HttpMethod.POST,
                    new HttpEntity<>(formData, authHeaders(session)),
                    Map.class
            );
            String courseId = String.valueOf(response.getBody().get("id"));
            return "redirect:/instructor/courses/" + courseId;
        } catch (Exception e) {
            model.addAttribute("error", "Could not create course: " + e.getMessage());
            return "instructor/create-course";
        }
    }

    /**
     * GET /instructor/courses/{courseId}/edit — show edit-course form
     */
    @GetMapping("/courses/{courseId}/edit")
    public String editCourseForm(@PathVariable String courseId,
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
            model.addAttribute("courseId", courseId);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load course.");
        }
        return "instructor/edit-course";
    }

    /**
     * POST /instructor/courses/{courseId}/edit — update course via course-service
     */
    @PostMapping("/courses/{courseId}/edit")
    public String editCourse(@PathVariable String courseId,
                             @RequestParam Map<String, String> formData,
                             HttpSession session,
                             Model model) {
        try {
            restTemplate.exchange(
                    courseUrl + "/api/v1/courses/" + courseId,
                    HttpMethod.PUT,
                    new HttpEntity<>(formData, authHeaders(session)),
                    Object.class
            );
            return "redirect:/instructor/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Could not update course.");
            return "instructor/edit-course";
        }
    }

    /**
     * POST /instructor/courses/{courseId}/delete — delete a course
     */
    @PostMapping("/courses/{courseId}/delete")
    public String deleteCourse(@PathVariable String courseId, HttpSession session) {
        try {
            restTemplate.exchange(
                    courseUrl + "/api/v1/courses/" + courseId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
        } catch (Exception ignored) { }
        return "redirect:/instructor/dashboard";
    }

    /**
     * POST /instructor/courses/{courseId}/publish — publish a draft course
     */
    @PostMapping("/courses/{courseId}/publish")
    public String publishCourse(@PathVariable String courseId, HttpSession session) {
        try {
            restTemplate.exchange(
                    courseUrl + "/api/v1/courses/" + courseId + "/publish",
                    HttpMethod.POST,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
        } catch (Exception ignored) { }
        return "redirect:/instructor/dashboard";
    }

    // =========================================================================
    // 3. Lesson Management
    // =========================================================================

    /**
     * GET /instructor/courses/{courseId}/lessons/add — show add-lesson form
     */
    @GetMapping("/courses/{courseId}/lessons/add")
    public String addLessonForm(@PathVariable String courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "instructor/add-lesson";
    }

    /**
     * POST /instructor/courses/{courseId}/lessons/add — submit new lesson to lesson-service
     */
    @PostMapping("/courses/{courseId}/lessons/add")
    public String addLesson(@PathVariable String courseId,
                            @RequestParam Map<String, String> formData,
                            HttpSession session,
                            Model model) {
        try {
            formData.put("courseId", courseId);
            restTemplate.exchange(
                    lessonUrl + "/api/v1/lessons",
                    HttpMethod.POST,
                    new HttpEntity<>(formData, authHeaders(session)),
                    Object.class
            );
            return "redirect:/instructor/courses/" + courseId + "/lessons";
        } catch (Exception e) {
            model.addAttribute("error", "Could not add lesson.");
            return "instructor/add-lesson";
        }
    }

    /**
     * POST /instructor/lessons/{lessonId}/edit — update lesson details
     */
    @PostMapping("/lessons/{lessonId}/edit")
    public String editLesson(@PathVariable String lessonId,
                             @RequestParam Map<String, String> formData,
                             @RequestParam String courseId,
                             HttpSession session) {
        try {
            restTemplate.exchange(
                    lessonUrl + "/api/v1/lessons/" + lessonId,
                    HttpMethod.PUT,
                    new HttpEntity<>(formData, authHeaders(session)),
                    Object.class
            );
        } catch (Exception ignored) { }
        return "redirect:/instructor/courses/" + courseId + "/lessons";
    }

    /**
     * POST /instructor/lessons/{lessonId}/delete — delete a lesson
     */
    @PostMapping("/lessons/{lessonId}/delete")
    public String deleteLesson(@PathVariable String lessonId,
                               @RequestParam String courseId,
                               HttpSession session) {
        try {
            restTemplate.exchange(
                    lessonUrl + "/api/v1/lessons/" + lessonId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
        } catch (Exception ignored) { }
        return "redirect:/instructor/courses/" + courseId + "/lessons";
    }

    /**
     * POST /instructor/lessons/{lessonId}/resources — upload/add a resource to a lesson
     */
    @PostMapping("/lessons/{lessonId}/resources")
    public String addResource(@PathVariable String lessonId,
                              @RequestParam Map<String, String> formData,
                              @RequestParam String courseId,
                              HttpSession session) {
        try {
            formData.put("lessonId", lessonId);
            restTemplate.exchange(
                    lessonUrl + "/api/v1/resources",
                    HttpMethod.POST,
                    new HttpEntity<>(formData, authHeaders(session)),
                    Object.class
            );
        } catch (Exception ignored) { }
        return "redirect:/instructor/courses/" + courseId + "/lessons";
    }

    // =========================================================================
    // 4. Quiz / Assessment Management
    // =========================================================================

    /**
     * GET /instructor/courses/{courseId}/quizzes/create — show create-quiz form
     */
    @GetMapping("/courses/{courseId}/quizzes/create")
    public String createQuizForm(@PathVariable String courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "instructor/create-quiz";
    }

    /**
     * POST /instructor/courses/{courseId}/quizzes/create — submit quiz to assessment-service
     */
    @PostMapping("/courses/{courseId}/quizzes/create")
    public String createQuiz(@PathVariable String courseId,
                             @RequestParam Map<String, String> formData,
                             HttpSession session,
                             Model model) {
        try {
            formData.put("courseId", courseId);
            ResponseEntity<Map> response = restTemplate.exchange(
                    assessmentUrl + "/api/v1/quizzes",
                    HttpMethod.POST,
                    new HttpEntity<>(formData, authHeaders(session)),
                    Map.class
            );
            String quizId = String.valueOf(response.getBody().get("id"));
            return "redirect:/instructor/quizzes/" + quizId + "/questions/add";
        } catch (Exception e) {
            model.addAttribute("error", "Could not create quiz.");
            return "instructor/create-quiz";
        }
    }

    /**
     * POST /instructor/quizzes/{quizId}/questions — add a question to a quiz
     */
    @PostMapping("/quizzes/{quizId}/questions")
    public String addQuestion(@PathVariable String quizId,
                              @RequestParam Map<String, String> formData,
                              HttpSession session) {
        try {
            formData.put("quizId", quizId);
            restTemplate.exchange(
                    assessmentUrl + "/api/v1/questions",
                    HttpMethod.POST,
                    new HttpEntity<>(formData, authHeaders(session)),
                    Object.class
            );
        } catch (Exception ignored) { }
        return "redirect:/instructor/quizzes/" + quizId + "/questions/add";
    }

    // =========================================================================
    // 5. Enrollments & Student Progress
    // =========================================================================

    /**
     * GET /instructor/courses/{courseId}/enrollments — view enrolled students
     */
    @GetMapping("/courses/{courseId}/enrollments")
    public String viewEnrollments(@PathVariable String courseId,
                                  Model model,
                                  HttpSession session) {
        try {
            ResponseEntity<Object> enrollments = restTemplate.exchange(
                    enrollmentUrl + "/api/v1/enrollments/course/" + courseId,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("enrollments", enrollments.getBody());
            model.addAttribute("courseId", courseId);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load enrollments.");
        }
        return "instructor/enrollments";
    }

    /**
     * GET /instructor/courses/{courseId}/students/{studentId}/progress
     *     — view a specific student's progress in this course
     */
    @GetMapping("/courses/{courseId}/students/{studentId}/progress")
    public String viewStudentProgress(@PathVariable String courseId,
                                      @PathVariable String studentId,
                                      Model model,
                                      HttpSession session) {
        try {
            ResponseEntity<Object> progress = restTemplate.exchange(
                    progressUrl + "/api/v1/progress/student/" + studentId + "/course/" + courseId,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("progress", progress.getBody());
            model.addAttribute("courseId", courseId);
            model.addAttribute("studentId", studentId);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load student progress.");
        }
        return "instructor/student-progress";
    }

    // =========================================================================
    // 6. Forum Moderation
    // =========================================================================

    /**
     * GET /instructor/courses/{courseId}/forum — view all threads for a course
     */
    @GetMapping("/courses/{courseId}/forum")
    public String viewForumThreads(@PathVariable String courseId,
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
            model.addAttribute("error", "Could not load forum threads.");
        }
        return "instructor/forum";
    }

    /**
     * POST /instructor/threads/{threadId}/moderate — hide or flag a thread
     */
    @PostMapping("/threads/{threadId}/moderate")
    public String moderateThread(@PathVariable String threadId,
                                 @RequestParam String action,
                                 @RequestParam String courseId,
                                 HttpSession session) {
        try {
            Map<String, String> payload = Map.of("action", action);
            restTemplate.exchange(
                    discussionUrl + "/api/v1/threads/" + threadId + "/moderate",
                    HttpMethod.POST,
                    new HttpEntity<>(payload, authHeaders(session)),
                    Object.class
            );
        } catch (Exception ignored) { }
        return "redirect:/instructor/courses/" + courseId + "/forum";
    }

    // =========================================================================
    // 7. Analytics
    // =========================================================================

    /**
     * GET /instructor/courses/{courseId}/analytics — course-level analytics view
     */
    @GetMapping("/courses/{courseId}/analytics")
    public String viewCourseAnalytics(@PathVariable String courseId,
                                      Model model,
                                      HttpSession session) {
        try {
            // Enrollment count
            ResponseEntity<Object> enrollStats = restTemplate.exchange(
                    enrollmentUrl + "/api/v1/enrollments/course/" + courseId + "/stats",
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("enrollStats", enrollStats.getBody());

            // Completion stats from progress-service
            ResponseEntity<Object> completionStats = restTemplate.exchange(
                    progressUrl + "/api/v1/progress/course/" + courseId + "/stats",
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(session)),
                    Object.class
            );
            model.addAttribute("completionStats", completionStats.getBody());

            model.addAttribute("courseId", courseId);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load analytics.");
        }
        return "instructor/analytics";
    }
}
