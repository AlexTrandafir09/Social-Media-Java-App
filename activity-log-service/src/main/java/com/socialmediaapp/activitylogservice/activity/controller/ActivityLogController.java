package com.socialmediaapp.activitylogservice.activity.controller;

import com.socialmediaapp.activitylogservice.activity.entity.ActivityLog;
import com.socialmediaapp.activitylogservice.activity.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Admin-only, enforced in SecurityConfig via a path-based rule requiring ROLE_ADMIN.
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
