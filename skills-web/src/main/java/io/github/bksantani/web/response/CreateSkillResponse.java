package io.github.bksantani.web.response;

import java.util.List;

public record CreateSkillResponse(
    String content,
    PullRequestMetadata metadata,
    List<String> warnings
) {
}

