package com.socialmediaapp.socialmediaapp.activity;

import com.socialmediaapp.socialmediaapp.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public void record(User actor, ActivityAction action, String description) {
        ActivityLog entry = ActivityLog.builder()
                .actor(actor)
                .action(action)
                .description(description)
                .build();
        activityLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public Page<ActivityLog> getActivity(Pageable pageable) {
        return activityLogRepository.findAll(pageable);
    }
}
