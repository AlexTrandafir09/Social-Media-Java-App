package com.socialmediaapp.contentservice.content.service;

import com.socialmediaapp.contentservice.content.entity.Like;
import com.socialmediaapp.contentservice.content.entity.Post;
import com.socialmediaapp.contentservice.content.exception.DuplicateLikeException;
import com.socialmediaapp.contentservice.content.exception.LikeNotFoundException;
import com.socialmediaapp.contentservice.content.exception.PostNotFoundException;
import com.socialmediaapp.contentservice.content.repository.LikeRepository;
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
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ActivityEventPublisher activityEventPublisher;

    @Mock
    private NotificationService notificationService;

    private LikeService likeService;

    private Post post;

    @BeforeEach
    void setUp() {
        likeService = new LikeService(likeRepository, postRepository, activityEventPublisher, notificationService);
        post = Post.builder().id(1L).authorId(1L).content("hi").build();
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
    void like_savesWhenValid() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(likeRepository.existsByUserIdAndPostId(2L, 1L)).thenReturn(false);
        Like like = Like.builder().userId(2L).post(post).build();
        when(likeRepository.save(any(Like.class))).thenReturn(like);

        Like result = likeService.like(1L);

        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.getPost()).isEqualTo(post);
        verify(notificationService).notifyIfEnabled(1L, 2L, NotificationType.LIKE, 1L);
    }

    @Test
    void like_throwsWhenPostNotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.like(1L))
                .isInstanceOf(PostNotFoundException.class);

        verify(likeRepository, never()).save(any());
    }

    @Test
    void like_throwsWhenAlreadyLiked() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(likeRepository.existsByUserIdAndPostId(2L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> likeService.like(1L))
                .isInstanceOf(DuplicateLikeException.class);

        verify(likeRepository, never()).save(any());
    }

    @Test
    void unlike_deletesWhenExists() {
        Like like = Like.builder().userId(2L).post(post).build();
        when(likeRepository.findByUserIdAndPostId(2L, 1L)).thenReturn(Optional.of(like));

        likeService.unlike(1L);

        verify(likeRepository).delete(like);
    }

    @Test
    void unlike_throwsWhenNotFound() {
        when(likeRepository.findByUserIdAndPostId(2L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.unlike(1L))
                .isInstanceOf(LikeNotFoundException.class);

        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    void getLikesForPost_returnsList() {
        Like like = Like.builder().userId(2L).post(post).build();
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
