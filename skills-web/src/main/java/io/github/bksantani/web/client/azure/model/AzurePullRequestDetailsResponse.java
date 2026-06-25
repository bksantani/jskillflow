package io.github.bksantani.web.client.azure.model;

import static org.springframework.util.StringUtils.hasText;

import io.github.bksantani.web.exceptions.SkillsWebServiceException;

public record AzurePullRequestDetailsResponse(
    String title,
    String description,
    Repository repository
) {
    public record Repository(String id) {
    }

    public String repositoryIdOrThrow() {
        if (repository == null || !hasText(repository.id())) {
            throw new SkillsWebServiceException("Missing repository id in Azure pull request details response.");
        }
        return repository.id();
    }
}
