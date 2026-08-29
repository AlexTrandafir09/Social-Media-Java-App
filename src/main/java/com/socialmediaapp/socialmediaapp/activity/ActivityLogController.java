package com.socialmediaapp.socialmediaapp.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Open for now, intentionally - real admin-only access control needs Phase 08 (Security) to mean anything.
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public Page<ActivityLog> getActivity(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return activityLogService.getActivity(pageable);
    }
}
