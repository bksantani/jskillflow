package io.github.bksantani.web.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSkillRequest(
    @NotBlank(message = "prUrl is required")
    String prUrl
) {
}

