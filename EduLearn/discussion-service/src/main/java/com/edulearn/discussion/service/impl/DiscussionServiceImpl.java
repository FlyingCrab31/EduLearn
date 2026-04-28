package com.edulearn.discussion.service.impl;

import com.edulearn.discussion.entity.DiscussionThread;
import com.edulearn.discussion.entity.Reply;
import com.edulearn.discussion.exception.ReplyNotFoundException;
import com.edulearn.discussion.exception.ThreadNotFoundException;
import com.edulearn.discussion.repository.ReplyRepository;
import com.edulearn.discussion.repository.ThreadRepository;
import com.edulearn.discussion.service.DiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussionServiceImpl implements DiscussionService {

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @Override
    public DiscussionThread createThread(DiscussionThread thread) {
        return threadRepository.save(thread);
    }

    @Override
    public Reply postReply(int threadId, Reply reply) {
        if (!threadRepository.existsById(threadId)) {
            throw new ThreadNotFoundException("Thread not found with id: " + threadId);
        }
        reply.setThreadId(threadId);
        return replyRepository.save(reply);
    }

    @Override
    public List<DiscussionThread> getThreadsByCourse(int courseId) {
        return threadRepository.findByCourseId(courseId);
    }

    @Override
    public List<DiscussionThread> getThreadsByLesson(int lessonId) {
        return threadRepository.findByLessonId(lessonId);
    }

    @Override
    public List<Reply> getRepliesByThread(int threadId) {
        return replyRepository.findByThreadId(threadId);
    }

    @Override
    public void upvoteReply(int replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new ReplyNotFoundException("Reply not found with id: " + replyId));
        reply.setUpvotes(reply.getUpvotes() + 1);
        replyRepository.save(reply);
    }

    @Override
    public void acceptReply(int replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new ReplyNotFoundException("Reply not found with id: " + replyId));
        reply.setAccepted(true);
        replyRepository.save(reply);
    }

    @Override
    public void pinThread(int threadId) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ThreadNotFoundException("Thread not found with id: " + threadId));
        thread.setPinned(true);
        threadRepository.save(thread);
    }

    @Override
    public void closeThread(int threadId) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ThreadNotFoundException("Thread not found with id: " + threadId));
        thread.setClosed(true);
        threadRepository.save(thread);
    }

    @Override
    public void deleteThread(int threadId) {
        if (!threadRepository.existsById(threadId)) {
            throw new ThreadNotFoundException("Thread not found with id: " + threadId);
        }
        threadRepository.deleteById(threadId);
        // Optionally delete all replies associated with this thread
        List<Reply> replies = replyRepository.findByThreadId(threadId);
        replyRepository.deleteAll(replies);
    }

    @Override
    public void deleteReply(int replyId) {
        if (!replyRepository.existsById(replyId)) {
            throw new ReplyNotFoundException("Reply not found with id: " + replyId);
        }
        replyRepository.deleteById(replyId);
    }
}
