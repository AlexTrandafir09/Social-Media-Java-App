package com.socialmediaapp.socialmediaapp.content.service;

import com.socialmediaapp.socialmediaapp.activity.ActivityAction;
import com.socialmediaapp.socialmediaapp.activity.ActivityLogService;
import com.socialmediaapp.socialmediaapp.content.dto.CommentCreateRequest;
import com.socialmediaapp.socialmediaapp.content.dto.CommentUpdateRequest;
import com.socialmediaapp.socialmediaapp.content.entity.Comment;
import com.socialmediaapp.socialmediaapp.content.entity.Post;
import com.socialmediaapp.socialmediaapp.content.exception.CommentNotFoundException;
import com.socialmediaapp.socialmediaapp.content.exception.PostNotFoundException;
import com.socialmediaapp.socialmediaapp.content.repository.CommentRepository;
import com.socialmediaapp.socialmediaapp.content.repository.PostRepository;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.exception.UserNotFoundException;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public Comment createComment(CommentCreateRequest request) {
        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new UserNotFoundException(request.authorId()));
        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new PostNotFoundException(request.postId()));
        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .content(request.content())
                .build();
        Comment saved = commentRepository.save(comment);
        activityLogService.record(author, ActivityAction.COMMENT_CREATED, "Comment created: " + saved.getId());
        log.debug("Comment created: id={}, postId={}, authorId={}", saved.getId(), post.getId(), author.getId());
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
        comment.setContent(request.content());
        Comment saved = commentRepository.save(comment);
        activityLogService.record(comment.getAuthor(), ActivityAction.COMMENT_UPDATED, "Comment updated: " + id);
        log.debug("Comment updated: id={}", id);
        return saved;
    }

    public void deleteComment(Long id) {
        Comment comment = getCommentById(id);
        commentRepository.deleteById(id);
        activityLogService.record(comment.getAuthor(), ActivityAction.COMMENT_DELETED, "Comment deleted: " + id);
        log.debug("Comment deleted: id={}", id);
    }
}
