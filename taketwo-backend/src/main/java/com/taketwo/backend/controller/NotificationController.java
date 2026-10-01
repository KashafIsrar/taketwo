// controller/NotificationController.java
package com.taketwo.backend.controller;

import com.taketwo.backend.dto.NotificationResponse;
import com.taketwo.backend.service.CurrentUserService;
import com.taketwo.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public List<NotificationResponse> getNotifications(
            @RequestParam(required = false) Integer limit,
            Authentication authentication
    ) {
        var user = currentUserService.getCurrentUser(authentication);
        return notificationService.getNotifications(user.getId(), limit);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(Authentication authentication) {
        var user = currentUserService.getCurrentUser(authentication);
        return Map.of("count", notificationService.getUnreadCount(user.getId()));
    }

    @PostMapping("/mark-read")
    public void markAllAsRead(Authentication authentication) {
        var user = currentUserService.getCurrentUser(authentication);
        notificationService.markAllAsRead(user.getId());
    }
}