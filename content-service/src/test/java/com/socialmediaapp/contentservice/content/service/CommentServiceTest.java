package com.socialmediaapp.contentservice.content.service;

import com.socialmediaapp.contentservice.content.dto.CommentCreateRequest;
import com.socialmediaapp.contentservice.content.dto.CommentUpdateRequest;
import com.socialmediaapp.contentservice.content.entity.Comment;
import com.socialmediaapp.contentservice.content.entity.Post;
import com.socialmediaapp.contentservice.content.exception.CommentNotFoundException;
import com.socialmediaapp.contentservice.content.exception.PostNotFoundException;
import com.socialmediaapp.contentservice.content.repository.CommentRepository;
import com.socialmediaapp.contentservice.content.repository.PostRepository;
import com.socialmediaapp.contentservice.messaging.ActivityEventPublisher;
import com.socialmediaapp.contentservice.messaging.NotificationType;
import com.socialmediaapp.contentservice.notification.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ActivityEventPublisher activityEventPublisher;

    @Mock
    private NotificationService notificationService;

    private CommentService commentService;

    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, postRepository, activityEventPublisher, notificationService);
        post = Post.builder().id(1L).authorId(1L).content("hi").build();
        comment = Comment.builder().id(1L).authorId(2L).post(post).content("nice").build();
        authenticateAs(2L, "ROLE_USER");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Long userId, String... authorities) {
        var grantedAuthorities = List.of(authorities).stream().map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, grantedAuthorities));
    }

    @Test
    void createComment_savesWhenValid() {
        CommentCreateRequest request = new CommentCreateRequest(1L, "nice");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = commentService.createComment(request);

        assertThat(result.getContent()).isEqualTo("nice");
        assertThat(result.getAuthorId()).isEqualTo(2L);
        assertThat(result.getPost()).isEqualTo(post);
        verify(notificationService).notifyIfEnabled(1L, 2L, NotificationType.COMMENT, 1L);
    }

    @Test
    void createComment_throwsWhenPostNotFound() {
        CommentCreateRequest request = new CommentCreateRequest(1L, "nice");
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(PostNotFoundException.class);

        verify(commentRepository, never()).save(any());
    }

    @Test
    void getCommentById_returnsCommentWhenFound() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        Comment result = commentService.getCommentById(1L);

        assertThat(result).isEqualTo(comment);
    }

    @Test
    void getCommentById_throwsWhenNotFound() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getCommentById(99L))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    void getCommentsForPost_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(commentRepository.findByPostId(1L, pageable)).thenReturn(new PageImpl<>(List.of(comment), pageable, 1));

        Page<Comment> result = commentService.getCommentsForPost(1L, pageable);

        assertThat(result.getContent()).containsExactly(comment);
    }

    @Test
    void updateComment_updatesContent() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);

        Comment result = commentService.updateComment(1L, new CommentUpdateRequest("edited"));

        assertThat(result.getContent()).isEqualTo("edited");
    }

    @Test
    void updateComment_throwsWhenNotFound() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.updateComment(99L, new CommentUpdateRequest("x")))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    void updateComment_throwsWhenNotOwnerAndNotAdmin() {
        authenticateAs(3L, "ROLE_USER");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.updateComment(1L, new CommentUpdateRequest("x")))
                .isInstanceOf(AccessDeniedException.class);

        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_succeedsWhenAdminButNotOwner() {
        authenticateAs(3L, "ROLE_ADMIN");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);

        Comment result = commentService.updateComment(1L, new CommentUpdateRequest("edited"));

        assertThat(result.getContent()).isEqualTo("edited");
    }

    @Test
    void deleteComment_deletesWhenExists() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L);

        verify(commentRepository).deleteById(1L);
    }

    @Test
    void deleteComment_throwsWhenNotFound() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(99L))
                .isInstanceOf(CommentNotFoundException.class);

        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void deleteComment_throwsWhenNotOwnerAndNotAdmin() {
        authenticateAs(3L, "ROLE_USER");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void deleteComment_succeedsWhenAdminButNotOwner() {
        authenticateAs(3L, "ROLE_ADMIN");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L);

        verify(commentRepository).deleteById(1L);
    }
}
