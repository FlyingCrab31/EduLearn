package com.edulearn.web;

import com.edulearn.web.controller.AdminController;
import com.edulearn.web.controller.InstructorController;
import com.edulearn.web.controller.StudentController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebControllerTest — lightweight Spring MVC slice tests for all three controllers.
 *
 * <p>Uses {@link WebMvcTest} to load only the web layer; downstream RestTemplate calls
 * are mocked so that no real microservices need to be running.</p>
 */
@WebMvcTest(controllers = {StudentController.class, InstructorController.class, AdminController.class})
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /** Mock the shared RestTemplate bean so no real HTTP calls are made */
    @MockBean
    private RestTemplate restTemplate;

    // ─────────────────────────────────────────────
    // Student Controller Tests
    // ─────────────────────────────────────────────

    @Test
    @WithMockUser(username = "student@edulearn.com", roles = "STUDENT")
    void studentHome_returnsOk() throws Exception {
        mockMvc.perform(get("/student/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/home"));
    }

    @Test
    @WithMockUser(username = "student@edulearn.com", roles = "STUDENT")
    void browseCourses_returnsCoursesView() throws Exception {
        mockMvc.perform(get("/student/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/courses"));
    }

    @Test
    @WithMockUser(username = "student@edulearn.com", roles = "STUDENT")
    void searchCourses_returnsCoursesView() throws Exception {
        mockMvc.perform(get("/student/courses/search").param("q", "spring boot"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/courses"))
                .andExpect(model().attributeExists("query"));
    }

    @Test
    @WithMockUser(username = "student@edulearn.com", roles = "STUDENT")
    void viewMyCourses_returnsMyCourses() throws Exception {
        mockMvc.perform(get("/student/my-courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/my-courses"));
    }

    @Test
    @WithMockUser(username = "student@edulearn.com", roles = "STUDENT")
    void viewProgress_returnsProgressView() throws Exception {
        mockMvc.perform(get("/student/progress/101"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/progress"));
    }

    @Test
    @WithMockUser(username = "student@edulearn.com", roles = "STUDENT")
    void watchLesson_returnsLessonView() throws Exception {
        mockMvc.perform(get("/student/lessons/50"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/lesson"));
    }

    @Test
    @WithMockUser(username = "student@edulearn.com", roles = "STUDENT")
    void takeQuiz_returnsQuizView() throws Exception {
        mockMvc.perform(get("/student/quizzes/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/quiz"));
    }

    @Test
    @WithMockUser(username = "student@edulearn.com", roles = "STUDENT")
    void viewNotifications_returnsNotificationsView() throws Exception {
        mockMvc.perform(get("/student/notifications"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/notifications"));
    }

    // ─────────────────────────────────────────────
    // Instructor Controller Tests
    // ─────────────────────────────────────────────

    @Test
    @WithMockUser(username = "instructor@edulearn.com", roles = "INSTRUCTOR")
    void instructorDashboard_returnsOk() throws Exception {
        mockMvc.perform(get("/instructor/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/dashboard"));
    }

    @Test
    @WithMockUser(username = "instructor@edulearn.com", roles = "INSTRUCTOR")
    void createCourseForm_returnsForm() throws Exception {
        mockMvc.perform(get("/instructor/courses/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/create-course"));
    }

    @Test
    @WithMockUser(username = "instructor@edulearn.com", roles = "INSTRUCTOR")
    void addLessonForm_returnsForm() throws Exception {
        mockMvc.perform(get("/instructor/courses/1/lessons/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/add-lesson"));
    }

    @Test
    @WithMockUser(username = "instructor@edulearn.com", roles = "INSTRUCTOR")
    void createQuizForm_returnsForm() throws Exception {
        mockMvc.perform(get("/instructor/courses/1/quizzes/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/create-quiz"));
    }

    @Test
    @WithMockUser(username = "instructor@edulearn.com", roles = "INSTRUCTOR")
    void viewCourseAnalytics_returnsAnalyticsView() throws Exception {
        mockMvc.perform(get("/instructor/courses/1/analytics"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/analytics"));
    }

    @Test
    @WithMockUser(username = "instructor@edulearn.com", roles = "INSTRUCTOR")
    void viewEnrollments_returnsEnrollmentsView() throws Exception {
        mockMvc.perform(get("/instructor/courses/1/enrollments"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/enrollments"));
    }

    // ─────────────────────────────────────────────
    // Admin Controller Tests
    // ─────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin@edulearn.com", roles = "ADMIN")
    void adminDashboard_returnsOk() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    @WithMockUser(username = "admin@edulearn.com", roles = "ADMIN")
    void manageUsers_returnsUsersView() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/manage-users"));
    }

    @Test
    @WithMockUser(username = "admin@edulearn.com", roles = "ADMIN")
    void manageAllCourses_returnsCoursesView() throws Exception {
        mockMvc.perform(get("/admin/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/manage-courses"));
    }

    @Test
    @WithMockUser(username = "admin@edulearn.com", roles = "ADMIN")
    void viewAllPayments_returnsPaymentsView() throws Exception {
        mockMvc.perform(get("/admin/payments"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/payments"));
    }

    @Test
    @WithMockUser(username = "admin@edulearn.com", roles = "ADMIN")
    void viewPlatformAnalytics_returnsAnalyticsView() throws Exception {
        mockMvc.perform(get("/admin/analytics"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/analytics"));
    }

    @Test
    @WithMockUser(username = "admin@edulearn.com", roles = "ADMIN")
    void sendNotificationForm_returnsForm() throws Exception {
        mockMvc.perform(get("/admin/notifications/send"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/send-notification"));
    }

    @Test
    @WithMockUser(username = "admin@edulearn.com", roles = "ADMIN")
    void viewDiscussions_returnsDiscussionsView() throws Exception {
        mockMvc.perform(get("/admin/discussions"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/discussions"));
    }
}
