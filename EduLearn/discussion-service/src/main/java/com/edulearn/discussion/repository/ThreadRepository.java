package com.edulearn.discussion.repository;

import com.edulearn.discussion.entity.DiscussionThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ThreadRepository extends JpaRepository<DiscussionThread, Integer> {
    
    List<DiscussionThread> findByCourseId(int courseId);
    
    List<DiscussionThread> findByLessonId(int lessonId);
    
    List<DiscussionThread> findByAuthorId(int authorId);
    
    List<DiscussionThread> findByIsPinned(boolean isPinned);
    
    @Query("SELECT t FROM DiscussionThread t WHERE t.title LIKE %:keyword% OR t.body LIKE %:keyword%")
    List<DiscussionThread> searchByKeyword(@Param("keyword") String keyword);
}
