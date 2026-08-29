package com.socialmediaapp.socialmediaapp.activity;

import com.socialmediaapp.socialmediaapp.messaging.ActivityEvent;
import com.socialmediaapp.socialmediaapp.messaging.ActivityEventPublisher;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityEventPublisher activityEventPublisher;

    public void record(User actor, ActivityAction action, String description) {
        activityEventPublisher.publish(new ActivityEvent(actor.getId(), action, description));
    }
}
