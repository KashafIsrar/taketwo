// controller/TasteProfileController.java
package com.taketwo.backend.controller;

import com.taketwo.backend.dto.TasteProfileResponse;
import com.taketwo.backend.service.CurrentUserService;
import com.taketwo.backend.service.TasteProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/taste-profile")
@RequiredArgsConstructor
public class TasteProfileController {

    private final TasteProfileService tasteProfileService;
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public TasteProfileResponse getMyTasteProfile(Authentication authentication) {
        var user = currentUserService.getCurrentUser(authentication);
        return tasteProfileService.getTasteProfile(user.getId());
    }
}