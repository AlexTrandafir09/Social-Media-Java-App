package com.socialmediaapp.followservice.follow.service;

import com.socialmediaapp.followservice.follow.entity.Follow;
import com.socialmediaapp.followservice.follow.exception.DuplicateFollowException;
import com.socialmediaapp.followservice.follow.exception.FollowNotFoundException;
import com.socialmediaapp.followservice.follow.exception.SelfFollowException;
import com.socialmediaapp.followservice.follow.repository.FollowRepository;
import com.socialmediaapp.followservice.messaging.ActivityAction;
import com.socialmediaapp.followservice.messaging.ActivityEvent;
import com.socialmediaapp.followservice.messaging.ActivityEventPublisher;
import com.socialmediaapp.followservice.messaging.NotificationEvent;
import com.socialmediaapp.followservice.messaging.NotificationEventPublisher;
import com.socialmediaapp.followservice.messaging.NotificationType;
import com.socialmediaapp.followservice.security.SecurityUtils;
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
    private final ActivityEventPublisher activityEventPublisher;
    private final NotificationEventPublisher notificationEventPublisher;

    public Follow follow(Long followingId) {
        Long followerId = SecurityUtils.getCurrentUserId();
        if (followerId.equals(followingId)) {
            throw new SelfFollowException();
        }
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new DuplicateFollowException(followerId, followingId);
        }
        Follow follow = Follow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
        Follow saved = followRepository.save(follow);
        activityEventPublisher.publish(new ActivityEvent(followerId, ActivityAction.FOLLOW_CREATED, "Follow created: followingId=" + followingId));
        notificationEventPublisher.publish(new NotificationEvent(followingId, followerId, NotificationType.FOLLOW, null));
        log.debug("Follow created: followerId={}, followingId={}", followerId, followingId);
        return saved;
    }

    public void unfollow(Long followingId) {
        Long followerId = SecurityUtils.getCurrentUserId();
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new FollowNotFoundException(followerId, followingId));
        followRepository.delete(follow);
        activityEventPublisher.publish(new ActivityEvent(followerId, ActivityAction.FOLLOW_REMOVED, "Follow removed: followingId=" + followingId));
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
