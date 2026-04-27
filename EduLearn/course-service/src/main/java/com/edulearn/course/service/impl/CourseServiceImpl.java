package com.edulearn.course.service.impl;

import com.edulearn.course.entity.Course;
import com.edulearn.course.repository.CourseRepository;
import com.edulearn.course.service.CourseService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public List<Course> searchCourses(String keyword, String category, String level, BigDecimal maxPrice) {
        List<Course> courses;

        if (keyword != null && !keyword.trim().isEmpty()) {
            courses = courseRepository.searchByKeyword(keyword.trim());
        } else {
            courses = courseRepository.findByIsPublishedTrue();
        }

        return courses.stream()
                .filter(c -> category == null || category.equalsIgnoreCase(c.getCategory()))
                .filter(c -> level == null || level.equalsIgnoreCase(c.getLevel()))
                .filter(c -> maxPrice == null || (c.getPrice() != null && c.getPrice().compareTo(maxPrice) <= 0))
                .collect(Collectors.toList());
    }

    @Override
    public List<Course> getAllPublishedCourses() {
        return courseRepository.findByIsPublishedTrue();
    }
}
