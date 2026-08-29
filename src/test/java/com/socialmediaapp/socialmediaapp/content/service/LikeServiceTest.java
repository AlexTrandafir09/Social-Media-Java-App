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
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityLogService activityLogService;

    private LikeService likeService;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        likeService = new LikeService(likeRepository, postRepository, userRepository, activityLogService);
        user = User.builder().id(2L).username("bob").build();
        post = Post.builder().id(1L).content("hi").build();
    }

    @Test
    void like_savesWhenValid() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(likeRepository.existsByUserIdAndPostId(2L, 1L)).thenReturn(false);
        Like like = Like.builder().user(user).post(post).build();
        when(likeRepository.save(any(Like.class))).thenReturn(like);

        Like result = likeService.like(2L, 1L);

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getPost()).isEqualTo(post);
    }

    @Test
    void like_throwsWhenUserNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.like(2L, 1L))
                .isInstanceOf(UserNotFoundException.class);

        verify(likeRepository, never()).save(any());
    }

    @Test
    void like_throwsWhenPostNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.like(2L, 1L))
                .isInstanceOf(PostNotFoundException.class);

        verify(likeRepository, never()).save(any());
    }

    @Test
    void like_throwsWhenAlreadyLiked() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(likeRepository.existsByUserIdAndPostId(2L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> likeService.like(2L, 1L))
                .isInstanceOf(DuplicateLikeException.class);

        verify(likeRepository, never()).save(any());
    }

    @Test
    void unlike_deletesWhenExists() {
        Like like = Like.builder().user(user).post(post).build();
        when(likeRepository.findByUserIdAndPostId(2L, 1L)).thenReturn(Optional.of(like));

        likeService.unlike(2L, 1L);

        verify(likeRepository).delete(like);
    }

    @Test
    void unlike_throwsWhenNotFound() {
        when(likeRepository.findByUserIdAndPostId(2L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.unlike(2L, 1L))
                .isInstanceOf(LikeNotFoundException.class);

        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    void getLikesForPost_returnsList() {
        Like like = Like.builder().user(user).post(post).build();
        when(likeRepository.findByPostId(1L)).thenReturn(List.of(like));

        List<Like> result = likeService.getLikesForPost(1L);

        assertThat(result).containsExactly(like);
    }

    @Test
    void countLikesForPost_returnsCount() {
        when(likeRepository.countByPostId(1L)).thenReturn(5L);

        long result = likeService.countLikesForPost(1L);

        assertThat(result).isEqualTo(5L);
    }
}
