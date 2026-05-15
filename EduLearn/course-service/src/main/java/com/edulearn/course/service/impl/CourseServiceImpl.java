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

    @Override
    public List<Course> getFeaturedCourses() {
        // Just return the first few published courses for now
        return courseRepository.findByIsPublishedTrue().stream().limit(6).collect(Collectors.toList());
    }

    @Override
    public Course getCourseById(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found"));
    }

    @Override
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public Course updateCourse(Long id, Course courseDetails) {
        Course course = getCourseById(id);
        course.setTitle(courseDetails.getTitle());
        course.setDescription(courseDetails.getDescription());
        course.setCategory(courseDetails.getCategory());
        course.setLevel(courseDetails.getLevel());
        course.setPrice(courseDetails.getPrice());
        course.setThumbnailUrl(courseDetails.getThumbnailUrl());
        return courseRepository.save(course);
    }

    @Override
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    @Override
    public List<Course> getCoursesByInstructor(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId);
    }

    @Override
    public void publishCourse(Long id) {
        Course course = getCourseById(id);
        course.setIsPublished(true);
        courseRepository.save(course);
    }
}
