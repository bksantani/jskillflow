package io.github.bksantani.web.response;

public record PullRequestMetadata(
    int filesAnalyzed,
    int filesSkipped,
    int commentsIncluded
) {
}

