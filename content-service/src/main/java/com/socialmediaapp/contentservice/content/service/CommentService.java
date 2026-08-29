package com.socialmediaapp.contentservice.content.service;

import com.socialmediaapp.contentservice.content.dto.CommentCreateRequest;
import com.socialmediaapp.contentservice.content.dto.CommentUpdateRequest;
import com.socialmediaapp.contentservice.content.entity.Comment;
import com.socialmediaapp.contentservice.content.entity.Post;
import com.socialmediaapp.contentservice.content.exception.CommentNotFoundException;
import com.socialmediaapp.contentservice.content.exception.PostNotFoundException;
import com.socialmediaapp.contentservice.content.repository.CommentRepository;
import com.socialmediaapp.contentservice.content.repository.PostRepository;
import com.socialmediaapp.contentservice.messaging.ActivityAction;
import com.socialmediaapp.contentservice.messaging.ActivityEvent;
import com.socialmediaapp.contentservice.messaging.ActivityEventPublisher;
import com.socialmediaapp.contentservice.messaging.NotificationType;
import com.socialmediaapp.contentservice.notification.NotificationService;
import com.socialmediaapp.contentservice.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ActivityEventPublisher activityEventPublisher;
    private final NotificationService notificationService;

    public Comment createComment(CommentCreateRequest request) {
        Long authorId = SecurityUtils.getCurrentUserId();
        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new PostNotFoundException(request.postId()));
        Comment comment = Comment.builder()
                .authorId(authorId)
                .post(post)
                .content(request.content())
                .build();
        Comment saved = commentRepository.save(comment);
        activityEventPublisher.publish(new ActivityEvent(authorId, ActivityAction.COMMENT_CREATED, "Comment created: " + saved.getId()));
        notificationService.notifyIfEnabled(post.getAuthorId(), authorId, NotificationType.COMMENT, post.getId());
        log.debug("Comment created: id={}, postId={}, authorId={}", saved.getId(), post.getId(), authorId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Comment> getCommentsForPost(Long postId, Pageable pageable) {
        return commentRepository.findByPostId(postId, pageable);
    }

    public Comment updateComment(Long id, CommentUpdateRequest request) {
        Comment comment = getCommentById(id);
        if (!SecurityUtils.isCurrentUserOrAdmin(comment.getAuthorId())) {
            throw new AccessDeniedException("You can only edit your own comments");
        }
        comment.setContent(request.content());
        Comment saved = commentRepository.save(comment);
        activityEventPublisher.publish(new ActivityEvent(comment.getAuthorId(), ActivityAction.COMMENT_UPDATED, "Comment updated: " + id));
        log.debug("Comment updated: id={}", id);
        return saved;
    }

    public void deleteComment(Long id) {
        Comment comment = getCommentById(id);
        if (!SecurityUtils.isCurrentUserOrAdmin(comment.getAuthorId())) {
            throw new AccessDeniedException("You can only delete your own comments");
        }
        commentRepository.deleteById(id);
        activityEventPublisher.publish(new ActivityEvent(comment.getAuthorId(), ActivityAction.COMMENT_DELETED, "Comment deleted: " + id));
        log.debug("Comment deleted: id={}", id);
    }
}
