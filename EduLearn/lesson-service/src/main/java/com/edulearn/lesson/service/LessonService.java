package com.edulearn.lesson.service;

import com.edulearn.lesson.entity.Lesson;

import java.util.List;
import java.util.Optional;

/**
 * Service interface defining operations for Lesson management,
 * including the "Preview Free Lessons" feature.
 */
public interface LessonService {

    // ── Core CRUD ──────────────────────────────────────────────────────────

    List<Lesson> getLessonsByCourseId(Long courseId);

    Optional<Lesson> getLessonById(Long id);

    Lesson createLesson(Lesson lesson);

    Lesson updateLesson(Long id, Lesson lesson);

    void deleteLesson(Long id);

    // ── Preview Free Lessons ───────────────────────────────────────────────

    /**
     * Returns all lessons for {@code courseId} that are marked as free-preview.
     * This is publicly accessible — no enrollment required.
     *
     * @param courseId the course whose preview lessons are requested
     * @return ordered list of previewable lessons (may be empty)
     */
    List<Lesson> getPreviewLessons(Long courseId);

    /**
     * Returns a single lesson by {@code id} only when it is marked as previewable.
     * Returns {@link Optional#empty()} if the lesson does not exist or is not
     * publicly previewable, preventing unenrolled users from accessing paid content.
     *
     * @param id the lesson identifier
     * @return the previewable lesson, or empty
     */
    Optional<Lesson> getPreviewLessonById(Long id);
}
