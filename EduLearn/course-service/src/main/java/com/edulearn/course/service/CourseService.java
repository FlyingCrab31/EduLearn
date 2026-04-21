package com.edulearn.course.service;

import com.edulearn.course.entity.Course;
import java.math.BigDecimal;
import java.util.List;

public interface CourseService {
    List<Course> searchCourses(String keyword, String category, String level, BigDecimal maxPrice);
    List<Course> getAllPublishedCourses();
}
