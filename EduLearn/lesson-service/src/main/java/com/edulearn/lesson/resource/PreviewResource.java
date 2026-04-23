package com.edulearn.lesson.resource;

import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.service.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public REST controller for the <b>Preview Free Lessons</b> feature.
 *
 * <p>All endpoints under {@code /api/preview} are intentionally unauthenticated —
 * any visitor (enrolled or not) may call them. Only lessons explicitly marked
 * {@code previewable = true} are surfaced here, so paid content is never exposed.
 *
 * <pre>
 *   GET /api/preview/course/{courseId}   → list preview lessons for a course
 *   GET /api/preview/lessons/{id}        → fetch one preview lesson by id
 * </pre>
 */
@RestController
@RequestMapping("/api/preview")
public class PreviewResource {

    private final LessonService lessonService;

    public PreviewResource(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    /**
     * Returns all free-preview lessons for {@code courseId}, ordered by
     * {@code orderIndex}. Unenrolled visitors can use this to decide whether
     * to purchase the course.
     *
     * @param courseId the course identifier
     * @return 200 with a (possibly empty) list of previewable lessons
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Lesson>> getPreviewLessons(@PathVariable Long courseId) {
        List<Lesson> previews = lessonService.getPreviewLessons(courseId);
        return ResponseEntity.ok(previews);
    }

    /**
     * Returns a single previewable lesson by its id.
     *
     * <p>Returns {@code 404 Not Found} if the lesson does not exist <em>or</em>
     * if it exists but is not marked as previewable — this prevents information
     * leakage about paid lessons.
     *
     * @param id the lesson identifier
     * @return 200 with the lesson, or 404
     */
    @GetMapping("/lessons/{id}")
    public ResponseEntity<Lesson> getPreviewLessonById(@PathVariable Long id) {
        return lessonService.getPreviewLessonById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
