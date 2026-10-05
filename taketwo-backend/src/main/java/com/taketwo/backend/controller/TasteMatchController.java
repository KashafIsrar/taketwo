// controller/TasteMatchController.java
package com.taketwo.backend.controller;

import com.taketwo.backend.dto.TasteMatchResponse;
import com.taketwo.backend.service.CurrentUserService;
import com.taketwo.backend.service.TasteMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/taste-profile")
@RequiredArgsConstructor
public class TasteMatchController {

    private final TasteMatchService tasteMatchService;
    private final CurrentUserService currentUserService;

    @GetMapping("/match/{targetUserId}")
    public TasteMatchResponse getMatch(@PathVariable UUID targetUserId, Authentication authentication) {
        var currentUser = currentUserService.getCurrentUser(authentication);
        return tasteMatchService.getMatch(currentUser.getId(), targetUserId);
    }
}