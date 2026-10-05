package com.taketwo.backend.controller;

import com.taketwo.backend.dto.TakeTwoRequest;
import com.taketwo.backend.dto.TakeTwoResponse;
import com.taketwo.backend.service.CurrentUserService;
import com.taketwo.backend.service.TakeTwoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/taketwo")
@RequiredArgsConstructor
public class TakeTwoController {

    private final TakeTwoService takeTwoService;
    private final CurrentUserService currentUserService;

    @PostMapping("/recommend")
    public TakeTwoResponse recommend(@Valid @RequestBody TakeTwoRequest request, Authentication authentication) {
        try {
            var user = currentUserService.getCurrentUser(authentication);
            return takeTwoService.recommend(user.getId(), request);
        } catch (Exception e) {
            e.printStackTrace(); // This will force the exact red error to print in your backend terminal!
            throw e;
        }
    }
}