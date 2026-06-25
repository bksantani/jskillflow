package io.github.bksantani.web.validators;

import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.bksantani.web.client.azure.AzurePullRequestContext;
import io.github.bksantani.web.exceptions.SkillsWebBadRequestException;

@Component
public class AzureCloudPullRequestParser {

    public AzurePullRequestContext parse(String prUrl) {
        if (prUrl == null || prUrl.isBlank()) {
            throw new SkillsWebBadRequestException("prUrl is required");
        }

        final URI uri;
        try {
            uri = URI.create(prUrl);
        } catch (IllegalArgumentException ex) {
            throw new SkillsWebBadRequestException("Invalid Azure Cloud Pull Request URL");
        }

        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new SkillsWebBadRequestException("Only https is allowed");
        }
        if (!"dev.azure.com".equalsIgnoreCase(uri.getHost())) {
            throw new SkillsWebBadRequestException("Only dev.azure.com is allowed");
        }

        List<String> parts = List.of(uri.getPath().split("/"));
        if (parts.size() < 7 || !"_git".equals(parts.get(3)) || !"pullrequest".equals(parts.get(5))) {
            throw new SkillsWebBadRequestException("Invalid Azure PR URL path format");
        }

        int prId;
        try {
            prId = Integer.parseInt(parts.get(6));
        } catch (NumberFormatException ex) {
            throw new SkillsWebBadRequestException("Invalid pull request id");
        }

        // example https://dev.azure.com/{organizationId}/{projectId}/_git/{repositoryName}/pullrequest/pullRequestId
        return new AzurePullRequestContext(parts.get(1), parts.get(2), prId);
    }


}
