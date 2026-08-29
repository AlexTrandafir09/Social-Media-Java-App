package com.socialmediaapp.socialmediaapp.user.service;

import com.socialmediaapp.socialmediaapp.activity.ActivityAction;
import com.socialmediaapp.socialmediaapp.activity.ActivityLogService;
import com.socialmediaapp.socialmediaapp.notification.entity.NotificationType;
import com.socialmediaapp.socialmediaapp.notification.service.NotificationService;
import com.socialmediaapp.socialmediaapp.user.entity.Follow;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import com.socialmediaapp.socialmediaapp.user.exception.DuplicateFollowException;
import com.socialmediaapp.socialmediaapp.user.exception.FollowNotFoundException;
import com.socialmediaapp.socialmediaapp.user.exception.SelfFollowException;
import com.socialmediaapp.socialmediaapp.user.exception.UserNotFoundException;
import com.socialmediaapp.socialmediaapp.user.repository.FollowRepository;
import com.socialmediaapp.socialmediaapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public Follow follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new SelfFollowException();
        }
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new UserNotFoundException(followerId));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new UserNotFoundException(followingId));
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new DuplicateFollowException(followerId, followingId);
        }
        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();
        Follow saved = followRepository.save(follow);
        activityLogService.record(follower, ActivityAction.FOLLOW_CREATED, "Follow created: followingId=" + followingId);
        notificationService.notifyIfEnabled(following, follower, NotificationType.FOLLOW, null);
        log.debug("Follow created: followerId={}, followingId={}", followerId, followingId);
        return saved;
    }

    public void unfollow(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new FollowNotFoundException(followerId, followingId));
        followRepository.delete(follow);
        activityLogService.record(follow.getFollower(), ActivityAction.FOLLOW_REMOVED, "Follow removed: followingId=" + followingId);
        log.debug("Follow removed: followerId={}, followingId={}", followerId, followingId);
    }

    @Transactional(readOnly = true)
    public List<Follow> getFollowing(Long userId) {
        return followRepository.findByFollowerId(userId);
    }

    @Transactional(readOnly = true)
    public List<Follow> getFollowers(Long userId) {
        return followRepository.findByFollowingId(userId);
    }
}
