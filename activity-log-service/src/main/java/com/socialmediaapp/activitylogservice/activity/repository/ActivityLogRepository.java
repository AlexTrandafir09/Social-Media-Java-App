package com.socialmediaapp.activitylogservice.activity.repository;

import com.socialmediaapp.activitylogservice.activity.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
}
