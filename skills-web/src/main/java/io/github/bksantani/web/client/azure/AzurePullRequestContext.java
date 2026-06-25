package io.github.bksantani.web.client.azure;

public record AzurePullRequestContext(String organization, String project, int pullRequestId) {
}
