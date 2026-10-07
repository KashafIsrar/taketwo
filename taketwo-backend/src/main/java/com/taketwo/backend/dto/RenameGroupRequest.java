// dto/RenameGroupRequest.java
package com.taketwo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameGroupRequest(
        @NotBlank @Size(max = 100) String name
) {}