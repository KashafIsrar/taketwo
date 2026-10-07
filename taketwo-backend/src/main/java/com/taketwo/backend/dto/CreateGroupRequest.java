package com.taketwo.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateGroupRequest(
        @NotBlank @Size(max = 100) String name,
        @NotEmpty(message = "Add at least one member") 
        @JsonAlias({"memberIds", "participantIds"}) 
        List<UUID> memberUserIds
) {}