package com.socialmediaapp.activitylogservice.activity.service;

import com.socialmediaapp.activitylogservice.activity.entity.ActivityLog;
import com.socialmediaapp.activitylogservice.activity.repository.ActivityLogRepository;
import com.socialmediaapp.activitylogservice.messaging.ActivityEvent;
import com.socialmediaapp.activitylogservice.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public void recordFromEvent(ActivityEvent event) {
        ActivityLog entry = ActivityLog.builder()
                .actorId(event.actorId())
                .action(event.action())
                .description(event.description())
                .build();
        ActivityLog saved = activityLogRepository.save(entry);
        log.debug("Activity recorded from event: id={}, actorId={}, action={}", saved.getId(), saved.getActorId(), event.action());
    }

    @Transactional(readOnly = true)
    public Page<ActivityLog> getActivity(Pageable pageable) {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Only admins can view the activity log");
        }
        return activityLogRepository.findAll(pageable);
    }
}
