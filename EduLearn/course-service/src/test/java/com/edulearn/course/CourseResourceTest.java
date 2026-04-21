package com.edulearn.course;

import com.edulearn.course.entity.Course;
import com.edulearn.course.resource.CourseResource;
import com.edulearn.course.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseResource.class)
public class CourseResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @Test
    void testGetAllPublishedCourses() throws Exception {
        Course course1 = new Course();
        course1.setCourseId(1L);
        course1.setTitle("Spring Boot API");

        Course course2 = new Course();
        course2.setCourseId(2L);
        course2.setTitle("Microservices");

        when(courseService.getAllPublishedCourses()).thenReturn(Arrays.asList(course1, course2));

        mockMvc.perform(get("/api/v1/courses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].title").value("Spring Boot API"))
                .andExpect(jsonPath("$[1].title").value("Microservices"));
    }

    @Test
    void testSearchCourses() throws Exception {
        Course course1 = new Course();
        course1.setCourseId(1L);
        course1.setTitle("Java Fundamentals");
        course1.setCategory("Programming");

        when(courseService.searchCourses("Java", "Programming", null, new BigDecimal("50.00")))
                .thenReturn(Arrays.asList(course1));

        mockMvc.perform(get("/api/v1/courses/search")
                .param("keyword", "Java")
                .param("category", "Programming")
                .param("maxPrice", "50.00")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].title").value("Java Fundamentals"))
                .andExpect(jsonPath("$[0].category").value("Programming"));
    }
}
