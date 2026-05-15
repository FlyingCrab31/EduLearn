package com.edulearn.discussion.resource;

import com.edulearn.discussion.entity.DiscussionThread;
import com.edulearn.discussion.entity.Reply;
import com.edulearn.discussion.service.DiscussionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discussions")
@Tag(name = "Discussion Resource", description = "Endpoints for managing course discussion threads and replies")
public class DiscussionResource {

    @Autowired
    private DiscussionService discussionService;

    @PostMapping("/threads")
    @Operation(summary = "Create a new discussion thread")
    public ResponseEntity<DiscussionThread> createThread(@RequestBody DiscussionThread thread) {
        return new ResponseEntity<>(discussionService.createThread(thread), HttpStatus.CREATED);
    }

    @GetMapping("/threads/course/{courseId}")
    @Operation(summary = "Get all discussion threads for a course")
    public ResponseEntity<List<DiscussionThread>> getThreadsByCourse(@PathVariable int courseId) {
        return ResponseEntity.ok(discussionService.getThreadsByCourse(courseId));
    }

    @GetMapping("/threads/lesson/{lessonId}")
    @Operation(summary = "Get all discussion threads for a specific lesson")
    public ResponseEntity<List<DiscussionThread>> getThreadsByLesson(@PathVariable int lessonId) {
        return ResponseEntity.ok(discussionService.getThreadsByLesson(lessonId));
    }

    @PostMapping("/threads/{threadId}/replies")
    @Operation(summary = "Post a reply to a discussion thread")
    public ResponseEntity<Reply> postReply(@PathVariable int threadId, @RequestBody Reply reply) {
        return new ResponseEntity<>(discussionService.postReply(threadId, reply), HttpStatus.CREATED);
    }

    @GetMapping("/threads/{threadId}/replies")
    @Operation(summary = "Get all replies for a discussion thread")
    public ResponseEntity<List<Reply>> getRepliesByThread(@PathVariable int threadId) {
        return ResponseEntity.ok(discussionService.getRepliesByThread(threadId));
    }

    @PutMapping("/replies/{replyId}/upvote")
    @Operation(summary = "Upvote a reply")
    public ResponseEntity<Void> upvoteReply(@PathVariable int replyId) {
        discussionService.upvoteReply(replyId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/replies/{replyId}/accept")
    @Operation(summary = "Mark a reply as accepted (Instructor/Admin only)")
    public ResponseEntity<Void> acceptReply(@PathVariable int replyId) {
        discussionService.acceptReply(replyId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/threads/{threadId}/pin")
    @Operation(summary = "Pin a discussion thread (Instructor/Admin only)")
    public ResponseEntity<Void> pinThread(@PathVariable int threadId) {
        discussionService.pinThread(threadId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/threads/{threadId}/close")
    @Operation(summary = "Close a discussion thread (Instructor/Admin only)")
    public ResponseEntity<Void> closeThread(@PathVariable int threadId) {
        discussionService.closeThread(threadId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/threads/{threadId}")
    @Operation(summary = "Delete a discussion thread")
    public ResponseEntity<Void> deleteThread(@PathVariable int threadId) {
        discussionService.deleteThread(threadId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/replies/{replyId}")
    @Operation(summary = "Delete a reply")
    public ResponseEntity<Void> deleteReply(@PathVariable int replyId) {
        discussionService.deleteReply(replyId);
        return ResponseEntity.noContent().build();
    }
}
