package io.github.bksantani.web.core;

import io.github.bksantani.web.request.CreateSkillRequest;
import io.github.bksantani.web.request.PullRequestProvider;
import io.github.bksantani.web.response.CreateSkillResponse;

public interface PullRequestHandler {
    PullRequestProvider provider();
    CreateSkillResponse handle(CreateSkillRequest request);
}
