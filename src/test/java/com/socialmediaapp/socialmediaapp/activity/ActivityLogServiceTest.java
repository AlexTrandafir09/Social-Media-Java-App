package com.socialmediaapp.socialmediaapp.activity;

import com.socialmediaapp.socialmediaapp.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityLogServiceTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    private ActivityLogService activityLogService;

    private User actor;

    @BeforeEach
    void setUp() {
        activityLogService = new ActivityLogService(activityLogRepository);
        actor = User.builder().id(1L).username("alice").build();
    }

    @Test
    void record_savesEntryWithActorActionAndDescription() {
        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        when(activityLogRepository.save(any(ActivityLog.class))).thenAnswer(inv -> inv.getArgument(0));

        activityLogService.record(actor, ActivityAction.POST_CREATED, "Post created: 1");

        verify(activityLogRepository).save(captor.capture());
        ActivityLog saved = captor.getValue();
        assertThat(saved.getActor()).isEqualTo(actor);
        assertThat(saved.getAction()).isEqualTo(ActivityAction.POST_CREATED);
        assertThat(saved.getDescription()).isEqualTo("Post created: 1");
    }

    @Test
    void getActivity_returnsPageFromRepository() {
        ActivityLog entry = ActivityLog.builder().id(1L).actor(actor).action(ActivityAction.USER_REGISTERED).description("x").build();
        Pageable pageable = PageRequest.of(0, 20);
        Page<ActivityLog> page = new PageImpl<>(java.util.List.of(entry), pageable, 1);
        when(activityLogRepository.findAll(pageable)).thenReturn(page);

        Page<ActivityLog> result = activityLogService.getActivity(pageable);

        assertThat(result.getContent()).containsExactly(entry);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
