package com.edulearn.lesson.service.impl;

import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.repository.LessonRepository;
import com.edulearn.lesson.service.LessonService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link LessonService}.
 *
 * <p>Includes the "Preview Free Lessons" feature: certain lessons are flagged
 * {@code previewable = true} and can be fetched without enrollment checks.
 */
@Service
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;

    public LessonServiceImpl(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    // ── Core CRUD ──────────────────────────────────────────────────────────

    @Override
    public List<Lesson> getLessonsByCourseId(Long courseId) {
        return lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
    }

    @Override
    public Optional<Lesson> getLessonById(Long id) {
        if (id == null) return Optional.empty();
        return lessonRepository.findById(id);
    }

    @Override
    public Lesson createLesson(Lesson lesson) {
        if (lesson == null) throw new IllegalArgumentException("Lesson cannot be null");
        return lessonRepository.save(lesson);
    }

    @Override
    public Lesson updateLesson(Long id, Lesson updated) {
        return lessonRepository.findById(id).map(existing -> {
            existing.setTitle(updated.getTitle());
            existing.setDescription(updated.getDescription());
            existing.setDurationMinutes(updated.getDurationMinutes());
            existing.setOrderIndex(updated.getOrderIndex());
            existing.setCourseId(updated.getCourseId());
            existing.setIsPreview(updated.isPreview());
            existing.setContentType(updated.getContentType());
            existing.setContentUrl(updated.getContentUrl());
            return lessonRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Lesson not found with id: " + id));
    }

    @Override
    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
    }

    // ── Preview Free Lessons ───────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Delegates directly to the repository derived query so the database
     * filters on {@code isPreview = true} — no post-filter needed in Java.
     */
    @Override
    public List<Lesson> getPreviewLessons(Long courseId) {
        return lessonRepository.findByCourseIdAndIsPreviewTrueOrderByOrderIndexAsc(courseId);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link Optional#empty()} for lessons that exist but are NOT
     * previewable, so the REST layer can return 404 — intentionally hiding
     * whether paid content exists at that id.
     */
    @Override
    public Optional<Lesson> getPreviewLessonById(Long id) {
        return lessonRepository.findByIdAndIsPreviewTrue(id);
    }
}
