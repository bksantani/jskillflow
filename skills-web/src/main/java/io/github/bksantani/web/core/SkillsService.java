package io.github.bksantani.web.core;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.github.bksantani.web.request.CreateSkillRequest;
import io.github.bksantani.web.request.PullRequestProvider;
import io.github.bksantani.web.response.CreateSkillResponse;
import io.github.bksantani.web.exceptions.SkillsWebServiceException;

@Service
public class SkillsService {

    private final Map<PullRequestProvider, PullRequestHandler> pullRequestHandlers;

    public SkillsService(List<PullRequestHandler> handlersList) {
        this.pullRequestHandlers = handlersList.stream().collect(Collectors.toMap(PullRequestHandler::provider, handler -> handler));
    }

    public CreateSkillResponse createSkill(CreateSkillRequest request, PullRequestProvider provider) {
        var handler = requiredHandler(provider);
        return handler.handle(request);
    }

    private PullRequestHandler requiredHandler(PullRequestProvider provider) {
        var handler = pullRequestHandlers.get(provider);
        if (handler == null) {
            throw new SkillsWebServiceException("No Pull Request Handler found for provider:" + provider);
        }
        return handler;
    }


}
