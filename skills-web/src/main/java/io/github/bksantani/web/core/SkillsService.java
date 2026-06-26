/*
 * Copyright 2026 Bharat Santani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.bksantani.web.core;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.github.bksantani.web.request.CreateSkillRequest;
import io.github.bksantani.web.request.PullRequestProvider;
import io.github.bksantani.web.api.response.CreateSkillResponse;
import io.github.bksantani.web.exceptions.SkillsWebServiceException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SkillsService {

    private final Map<PullRequestProvider, PullRequestHandler> pullRequestHandlers;

    public SkillsService(List<PullRequestHandler> handlersList) {
        this.pullRequestHandlers = handlersList.stream().collect(Collectors.toMap(PullRequestHandler::provider, handler -> handler));
        log.info("Initialized pull request handlers: providers={}", pullRequestHandlers.keySet());
    }

    public CreateSkillResponse createSkill(CreateSkillRequest request, PullRequestProvider provider) {
        log.info("createSkill request received: provider={}, prUrl={}", provider, request != null ? request.prUrl() : "n/a");
        var handler = requiredHandler(provider);
        log.debug("Using pull request handler: provider={}, handler={}", provider, handler.getClass().getSimpleName());

        try {
            var response = handler.handle(request);
            var metadata = response.metadata();
            int warningsCount = response.warnings() != null ? response.warnings().size() : 0;
            log.info(
                "createSkill completed: provider={}, filesAnalyzed={}, filesSkipped={}, commentsIncluded={}, warnings={}",
                provider,
                metadata != null ? metadata.filesAnalyzed() : -1,
                metadata != null ? metadata.filesSkipped() : -1,
                metadata != null ? metadata.commentsIncluded() : -1,
                warningsCount
            );
            return response;
        } catch (RuntimeException ex) {
            log.error("createSkill failed: provider={}, message={}, cause={}", provider, ex.getMessage(), causeMessage(ex), ex);
            throw ex;
        }
    }

    private PullRequestHandler requiredHandler(PullRequestProvider provider) {
        var handler = pullRequestHandlers.get(provider);
        if (handler == null) {
            log.error("No Pull Request Handler found: provider={}, availableProviders={}", provider, pullRequestHandlers.keySet());
            throw new SkillsWebServiceException("No Pull Request Handler found for provider:" + provider);
        }
        return handler;
    }

    private String causeMessage(Throwable ex) {
        return ex.getCause() != null ? ex.getCause().getMessage() : "n/a";
    }

}
