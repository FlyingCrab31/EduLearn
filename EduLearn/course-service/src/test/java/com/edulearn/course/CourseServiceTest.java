package com.edulearn.course;

import com.edulearn.course.entity.Course;
import com.edulearn.course.repository.CourseRepository;
import com.edulearn.course.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course course1;
    private Course course2;
    private Course course3;

    @BeforeEach
    void setUp() {
        course1 = new Course();
        course1.setCourseId(1L);
        course1.setTitle("Java Basics");
        course1.setCategory("Programming");
        course1.setLevel("Beginner");
        course1.setPrice(new BigDecimal("10.00"));
        course1.setIsPublished(true);

        course2 = new Course();
        course2.setCourseId(2L);
        course2.setTitle("Advanced Java");
        course2.setCategory("Programming");
        course2.setLevel("Advanced");
        course2.setPrice(new BigDecimal("20.00"));
        course2.setIsPublished(true);

        course3 = new Course();
        course3.setCourseId(3L);
        course3.setTitle("Spring Boot Intro");
        course3.setCategory("Frameworks");
        course3.setLevel("Beginner");
        course3.setPrice(new BigDecimal("15.00"));
        course3.setIsPublished(true);
    }

    @Test
    void testGetAllPublishedCourses() {
        when(courseRepository.findByIsPublishedTrue()).thenReturn(Arrays.asList(course1, course2, course3));

        List<Course> result = courseService.getAllPublishedCourses();

        assertEquals(3, result.size());
        verify(courseRepository, times(1)).findByIsPublishedTrue();
    }

    @Test
    void testSearchCoursesWithKeyword() {
        when(courseRepository.searchByKeyword("Java")).thenReturn(Arrays.asList(course1, course2));

        List<Course> result = courseService.searchCourses("Java", null, null, null);

        assertEquals(2, result.size());
        assertEquals("Java Basics", result.get(0).getTitle());
        verify(courseRepository, times(1)).searchByKeyword("Java");
        verify(courseRepository, never()).findByIsPublishedTrue();
    }

    @Test
    void testSearchCoursesWithFilters() {
        when(courseRepository.findByIsPublishedTrue()).thenReturn(Arrays.asList(course1, course2, course3));

        // Filter by Category = Programming
        List<Course> resultCat = courseService.searchCourses(null, "Programming", null, null);
        assertEquals(2, resultCat.size());

        // Filter by Level = Beginner
        List<Course> resultLevel = courseService.searchCourses(null, null, "Beginner", null);
        assertEquals(2, resultLevel.size());

        // Filter by Max Price = 15.00
        List<Course> resultPrice = courseService.searchCourses(null, null, null, new BigDecimal("15.00"));
        assertEquals(2, resultPrice.size());

        // Filter by multiple: Category=Programming, Level=Advanced
        List<Course> resultMulti = courseService.searchCourses(null, "Programming", "Advanced", null);
        assertEquals(1, resultMulti.size());
        assertEquals("Advanced Java", resultMulti.get(0).getTitle());
    }
}
