package com.socialmediaapp.socialmediaapp.content;

import com.socialmediaapp.socialmediaapp.activity.ActivityLogService;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.exception.UserNotFoundException;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private UserRepository userRepository;

    @Mock
    private ActivityLogService activityLogService;

    private CommentService commentService;

    private User author;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, postRepository, userRepository, activityLogService);
        author = User.builder().id(2L).username("bob").build();
        post = Post.builder().id(1L).content("hi").build();
        comment = Comment.builder().id(1L).author(author).post(post).content("nice").build();
    }

    @Test
    void createComment_savesWhenValid() {
        CommentCreateRequest request = new CommentCreateRequest(2L, 1L, "nice");
        when(userRepository.findById(2L)).thenReturn(Optional.of(author));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = commentService.createComment(request);

        assertThat(result.getContent()).isEqualTo("nice");
        assertThat(result.getAuthor()).isEqualTo(author);
        assertThat(result.getPost()).isEqualTo(post);
    }

    @Test
    void createComment_throwsWhenAuthorNotFound() {
        CommentCreateRequest request = new CommentCreateRequest(2L, 1L, "nice");
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(UserNotFoundException.class);

        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_throwsWhenPostNotFound() {
        CommentCreateRequest request = new CommentCreateRequest(2L, 1L, "nice");
        when(userRepository.findById(2L)).thenReturn(Optional.of(author));
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
    void getCommentsForPost_returnsList() {
        when(commentRepository.findByPostId(1L)).thenReturn(List.of(comment));

        List<Comment> result = commentService.getCommentsForPost(1L);

        assertThat(result).containsExactly(comment);
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
}
