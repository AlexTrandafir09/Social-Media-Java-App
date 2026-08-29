package com.socialmediaapp.socialmediaapp.content.controller;

import com.socialmediaapp.socialmediaapp.content.dto.CommentCreateRequest;
import com.socialmediaapp.socialmediaapp.content.dto.CommentUpdateRequest;
import com.socialmediaapp.socialmediaapp.content.entity.Comment;
import com.socialmediaapp.socialmediaapp.content.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Comment> createComment(@Valid @RequestBody CommentCreateRequest request) {
        Comment comment = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/{id}")
    public Comment getComment(@PathVariable Long id) {
        return commentService.getCommentById(id);
    }

    @GetMapping("/post/{postId}")
    public Page<Comment> getCommentsForPost(@PathVariable Long postId,
                                             @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return commentService.getCommentsForPost(postId, pageable);
    }

    @PutMapping("/{id}")
    public Comment updateComment(@PathVariable Long id, @Valid @RequestBody CommentUpdateRequest request) {
        return commentService.updateComment(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
