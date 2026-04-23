package com.edulearn.lesson.repository;

import com.edulearn.lesson.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Lesson entity.
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /** All lessons for a course, ordered by position. */
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    /**
     * Returns only the lessons that are marked as free-preview for a given course,
     * ordered by their display position.
     */
    List<Lesson> findByCourseIdAndPreviewableTrueOrderByOrderIndexAsc(Long courseId);

    /**
     * Finds a single lesson by id only if it is marked as previewable.
     * Used to guard the preview endpoint against non-preview lesson access.
     */
    Optional<Lesson> findByIdAndPreviewableTrue(Long id);
}
