package com.edulearn.lesson;

import com.edulearn.lesson.entity.Lesson;
import com.edulearn.lesson.repository.LessonRepository;
import com.edulearn.lesson.service.impl.LessonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LessonServiceImpl}.
 *
 * <p>Tests are grouped by feature area:
 * <ul>
 *   <li>{@link CoreCrudTests} – standard CRUD operations</li>
 *   <li>{@link PreviewLessonTests} – "Preview Free Lessons" feature</li>
 * </ul>
 */
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private LessonServiceImpl lessonService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper factory
    // ─────────────────────────────────────────────────────────────────────────

    private Lesson buildLesson(long id, String title, long courseId, boolean previewable) {
        Lesson l = new Lesson();
        l.setId(id);
        l.setTitle(title);
        l.setCourseId(courseId);
        l.setOrderIndex((int) id);
        l.setPreviewable(previewable);
        return l;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Core CRUD
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Core CRUD operations")
    class CoreCrudTests {

        @Test
        @DisplayName("getLessonsByCourseId – returns ordered lessons")
        void testGetLessonsByCourseId() {
            Lesson l1 = buildLesson(1, "Intro to Java", 10, false);
            Lesson l2 = buildLesson(2, "OOP Concepts", 10, false);

            when(lessonRepository.findByCourseIdOrderByOrderIndexAsc(10L))
                    .thenReturn(Arrays.asList(l1, l2));

            List<Lesson> result = lessonService.getLessonsByCourseId(10L);

            assertEquals(2, result.size());
            assertEquals("Intro to Java", result.get(0).getTitle());
            verify(lessonRepository, times(1)).findByCourseIdOrderByOrderIndexAsc(10L);
        }

        @Test
        @DisplayName("getLessonById – found")
        void testGetLessonById_Found() {
            Lesson lesson = buildLesson(1, "Intro to Java", 10, false);
            when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));

            Optional<Lesson> result = lessonService.getLessonById(1L);

            assertTrue(result.isPresent());
            assertEquals("Intro to Java", result.get().getTitle());
        }

        @Test
        @DisplayName("getLessonById – not found returns empty")
        void testGetLessonById_NotFound() {
            when(lessonRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Lesson> result = lessonService.getLessonById(99L);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("createLesson – persists and returns saved entity")
        void testCreateLesson() {
            Lesson lesson = buildLesson(0, "New Lesson", 5, false);
            when(lessonRepository.save(lesson)).thenReturn(lesson);

            Lesson created = lessonService.createLesson(lesson);

            assertNotNull(created);
            assertEquals("New Lesson", created.getTitle());
            verify(lessonRepository, times(1)).save(lesson);
        }

        @Test
        @DisplayName("updateLesson – updates all fields including previewable flag")
        void testUpdateLesson_Success() {
            Lesson existing = buildLesson(1, "Old Title", 10, false);
            Lesson updated  = buildLesson(1, "Updated Title", 10, true);
            updated.setDescription("Updated desc");
            updated.setDuration(45);

            when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

            Lesson result = lessonService.updateLesson(1L, updated);

            assertEquals("Updated Title", result.getTitle());
            assertEquals("Updated desc", result.getDescription());
            assertEquals(45, result.getDuration());
            assertTrue(result.isPreviewable(), "previewable flag should be updated");
            verify(lessonRepository, times(1)).save(existing);
        }

        @Test
        @DisplayName("updateLesson – throws when lesson id does not exist")
        void testUpdateLesson_NotFound() {
            when(lessonRepository.findById(99L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> lessonService.updateLesson(99L, new Lesson()));

            assertTrue(ex.getMessage().contains("99"));
        }

        @Test
        @DisplayName("deleteLesson – delegates to repository")
        void testDeleteLesson() {
            doNothing().when(lessonRepository).deleteById(1L);

            lessonService.deleteLesson(1L);

            verify(lessonRepository, times(1)).deleteById(1L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Preview Free Lessons feature
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Preview Free Lessons feature")
    class PreviewLessonTests {

        @Test
        @DisplayName("getPreviewLessons – returns only previewable lessons for a course")
        void testGetPreviewLessons_ReturnsPreviewableOnly() {
            Lesson preview1 = buildLesson(1, "What is Java?", 10, true);
            Lesson preview2 = buildLesson(3, "Setting Up IDE", 10, true);
            // lesson 2 (paid) should never appear – repository filters it out

            when(lessonRepository.findByCourseIdAndPreviewableTrueOrderByOrderIndexAsc(10L))
                    .thenReturn(Arrays.asList(preview1, preview2));

            List<Lesson> result = lessonService.getPreviewLessons(10L);

            assertEquals(2, result.size());
            result.forEach(l -> assertTrue(l.isPreviewable(),
                    "All returned lessons must be previewable"));
            verify(lessonRepository, times(1))
                    .findByCourseIdAndPreviewableTrueOrderByOrderIndexAsc(10L);
        }

        @Test
        @DisplayName("getPreviewLessons – returns empty list when course has no preview lessons")
        void testGetPreviewLessons_EmptyWhenNoneMarked() {
            when(lessonRepository.findByCourseIdAndPreviewableTrueOrderByOrderIndexAsc(42L))
                    .thenReturn(Collections.emptyList());

            List<Lesson> result = lessonService.getPreviewLessons(42L);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("getPreviewLessons – returns empty list for unknown courseId")
        void testGetPreviewLessons_UnknownCourse() {
            when(lessonRepository.findByCourseIdAndPreviewableTrueOrderByOrderIndexAsc(999L))
                    .thenReturn(Collections.emptyList());

            List<Lesson> result = lessonService.getPreviewLessons(999L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("getPreviewLessonById – returns lesson when it is previewable")
        void testGetPreviewLessonById_Found() {
            Lesson preview = buildLesson(5, "Java Basics Preview", 10, true);
            when(lessonRepository.findByIdAndPreviewableTrue(5L))
                    .thenReturn(Optional.of(preview));

            Optional<Lesson> result = lessonService.getPreviewLessonById(5L);

            assertTrue(result.isPresent());
            assertEquals("Java Basics Preview", result.get().getTitle());
            assertTrue(result.get().isPreviewable());
        }

        @Test
        @DisplayName("getPreviewLessonById – returns empty for non-previewable lesson (paid content guard)")
        void testGetPreviewLessonById_NotPreviewable() {
            // The repository returns empty when the lesson exists but previewable=false
            when(lessonRepository.findByIdAndPreviewableTrue(7L))
                    .thenReturn(Optional.empty());

            Optional<Lesson> result = lessonService.getPreviewLessonById(7L);

            assertFalse(result.isPresent(),
                    "Should not expose a non-previewable (paid) lesson through the preview API");
        }

        @Test
        @DisplayName("getPreviewLessonById – returns empty for completely unknown lesson id")
        void testGetPreviewLessonById_UnknownId() {
            when(lessonRepository.findByIdAndPreviewableTrue(999L))
                    .thenReturn(Optional.empty());

            Optional<Lesson> result = lessonService.getPreviewLessonById(999L);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("getPreviewLessons – repository is called with correct courseId parameter")
        void testGetPreviewLessons_CorrectRepositoryInteraction() {
            when(lessonRepository.findByCourseIdAndPreviewableTrueOrderByOrderIndexAsc(anyLong()))
                    .thenReturn(Collections.emptyList());

            lessonService.getPreviewLessons(77L);

            verify(lessonRepository, times(1))
                    .findByCourseIdAndPreviewableTrueOrderByOrderIndexAsc(77L);
            verify(lessonRepository, never())
                    .findByCourseIdOrderByOrderIndexAsc(anyLong());
        }

        @Test
        @DisplayName("createLesson – previewable flag is persisted correctly")
        void testCreateLesson_WithPreviewableTrue() {
            Lesson lesson = buildLesson(0, "Free Intro", 10, true);
            when(lessonRepository.save(lesson)).thenReturn(lesson);

            Lesson created = lessonService.createLesson(lesson);

            assertTrue(created.isPreviewable(),
                    "Newly created lesson should retain previewable=true");
            verify(lessonRepository).save(lesson);
        }

        @Test
        @DisplayName("createLesson – previewable defaults to false when not set")
        void testCreateLesson_PreviewableDefaultsFalse() {
            Lesson lesson = new Lesson(); // previewable not set → defaults false
            lesson.setTitle("Paid Lesson");
            lesson.setCourseId(10L);
            when(lessonRepository.save(lesson)).thenReturn(lesson);

            Lesson created = lessonService.createLesson(lesson);

            assertFalse(created.isPreviewable(),
                    "Lesson should default to previewable=false");
        }
    }
}
