package io.github.bksantani.web.core;

import org.springframework.stereotype.Component;

import io.github.bksantani.web.request.CreateSkillRequest;
import io.github.bksantani.web.request.PullRequestProvider;
import io.github.bksantani.web.response.CreateSkillResponse;
import io.github.bksantani.web.response.PullRequestMetadata;
import io.github.bksantani.web.client.azure.AzureCloudClient;
import io.github.bksantani.web.validators.AzureCloudPullRequestParser;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AzureCloudPullRequestHandler implements PullRequestHandler {

    private final AzureCloudPullRequestParser parser;
    private final AzureCloudClient azureCloudClient;
    private final AzureCloudChangeProcessService azureCloudChangeProcessService;
    private final PullRequestContentService pullRequestContentService;

    @Override
    public CreateSkillResponse handle(CreateSkillRequest request) {
        var context = parser.parse(request.prUrl());
        var details = azureCloudClient.getPullRequestDetails(context);

        String repositoryId = details.repositoryIdOrThrow();

        var threads = azureCloudClient.getPullRequestThreads(context, repositoryId);
        var comments = threads.extractComments();

        var iterations = azureCloudClient.getPullRequestIterations(context, repositoryId);
        long latestIterationId = iterations.latestIterationId();

        var changes = azureCloudClient.getPullRequestChanges(context, repositoryId, latestIterationId);

        var result = azureCloudChangeProcessService.processChanges(context, repositoryId, changes);
        int filesAnalyzed = result.diffs().size();
        int filesSkipped = result.warnings().size();

        PullRequestMetadata metadata = new PullRequestMetadata(
                filesAnalyzed,
                filesSkipped,
                comments.size()
        );

        String content = pullRequestContentService.pullRequestContent(
                details.title(),
                details.description(),
                comments,
                result.diffs()
        );

        return new CreateSkillResponse(content, metadata, result.warnings());
    }

    @Override
    public PullRequestProvider provider() {
        return PullRequestProvider.AZURE_CLOUD;
    }
}
