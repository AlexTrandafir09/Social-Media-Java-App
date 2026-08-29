package com.socialmediaapp.socialmediaapp.activity;

import com.socialmediaapp.socialmediaapp.messaging.ActivityEvent;
import com.socialmediaapp.socialmediaapp.messaging.ActivityEventPublisher;
import com.socialmediaapp.socialmediaapp.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ActivityLogServiceTest {

    @Mock
    private ActivityEventPublisher activityEventPublisher;

    private ActivityLogService activityLogService;

    private User actor;

    @BeforeEach
    void setUp() {
        activityLogService = new ActivityLogService(activityEventPublisher);
        actor = User.builder().id(1L).username("alice").build();
    }

    @Test
    void record_publishesEventWithActorActionAndDescription() {
        activityLogService.record(actor, ActivityAction.POST_CREATED, "Post created: 1");

        verify(activityEventPublisher).publish(new ActivityEvent(1L, ActivityAction.POST_CREATED, "Post created: 1"));
    }
}
