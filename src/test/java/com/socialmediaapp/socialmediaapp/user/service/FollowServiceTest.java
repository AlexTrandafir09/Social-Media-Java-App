package com.socialmediaapp.socialmediaapp.user.service;

import com.socialmediaapp.socialmediaapp.activity.ActivityLogService;
import com.socialmediaapp.socialmediaapp.user.entity.Follow;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.exception.DuplicateFollowException;
import com.socialmediaapp.socialmediaapp.user.exception.FollowNotFoundException;
import com.socialmediaapp.socialmediaapp.user.exception.SelfFollowException;
import com.socialmediaapp.socialmediaapp.user.exception.UserNotFoundException;
import com.socialmediaapp.socialmediaapp.user.repository.FollowRepository;
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
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityLogService activityLogService;

    private FollowService followService;

    private User follower;
    private User following;

    @BeforeEach
    void setUp() {
        followService = new FollowService(followRepository, userRepository, activityLogService);
        follower = User.builder().id(2L).username("bob").build();
        following = User.builder().id(1L).username("alice").build();
    }

    @Test
    void follow_savesWhenValid() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(1L)).thenReturn(Optional.of(following));
        when(followRepository.existsByFollowerIdAndFollowingId(2L, 1L)).thenReturn(false);
        Follow follow = Follow.builder().follower(follower).following(following).build();
        when(followRepository.save(any(Follow.class))).thenReturn(follow);

        Follow result = followService.follow(2L, 1L);

        assertThat(result.getFollower()).isEqualTo(follower);
        assertThat(result.getFollowing()).isEqualTo(following);
    }

    @Test
    void follow_throwsWhenFollowingSelf() {
        assertThatThrownBy(() -> followService.follow(1L, 1L))
                .isInstanceOf(SelfFollowException.class);

        verify(followRepository, never()).save(any());
    }

    @Test
    void follow_throwsWhenFollowerNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followService.follow(2L, 1L))
                .isInstanceOf(UserNotFoundException.class);

        verify(followRepository, never()).save(any());
    }

    @Test
    void follow_throwsWhenFollowingNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followService.follow(2L, 1L))
                .isInstanceOf(UserNotFoundException.class);

        verify(followRepository, never()).save(any());
    }

    @Test
    void follow_throwsWhenAlreadyFollowing() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(1L)).thenReturn(Optional.of(following));
        when(followRepository.existsByFollowerIdAndFollowingId(2L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> followService.follow(2L, 1L))
                .isInstanceOf(DuplicateFollowException.class);

        verify(followRepository, never()).save(any());
    }

    @Test
    void unfollow_deletesWhenExists() {
        Follow follow = Follow.builder().follower(follower).following(following).build();
        when(followRepository.findByFollowerIdAndFollowingId(2L, 1L)).thenReturn(Optional.of(follow));

        followService.unfollow(2L, 1L);

        verify(followRepository).delete(follow);
    }

    @Test
    void unfollow_throwsWhenNotFound() {
        when(followRepository.findByFollowerIdAndFollowingId(2L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followService.unfollow(2L, 1L))
                .isInstanceOf(FollowNotFoundException.class);

        verify(followRepository, never()).delete(any(Follow.class));
    }

    @Test
    void getFollowing_returnsList() {
        Follow follow = Follow.builder().follower(follower).following(following).build();
        when(followRepository.findByFollowerId(2L)).thenReturn(List.of(follow));

        List<Follow> result = followService.getFollowing(2L);

        assertThat(result).containsExactly(follow);
    }

    @Test
    void getFollowers_returnsList() {
        Follow follow = Follow.builder().follower(follower).following(following).build();
        when(followRepository.findByFollowingId(1L)).thenReturn(List.of(follow));

        List<Follow> result = followService.getFollowers(1L);

        assertThat(result).containsExactly(follow);
    }
}
