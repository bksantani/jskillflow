package io.github.bksantani.web.client.azure;

import io.github.bksantani.web.client.azure.model.AzurePullRequestChangesResponse;
import io.github.bksantani.web.client.azure.model.AzurePullRequestDetailsResponse;
import io.github.bksantani.web.client.azure.model.AzurePullRequestIterationsResponse;
import io.github.bksantani.web.client.azure.model.AzurePullRequestThreadsResponse;

public interface AzureCloudClient {
    AzurePullRequestDetailsResponse getPullRequestDetails(AzurePullRequestContext context);

    AzurePullRequestThreadsResponse getPullRequestThreads(AzurePullRequestContext context, String repositoryId);

    AzurePullRequestIterationsResponse getPullRequestIterations(AzurePullRequestContext context, String repositoryId);

    AzurePullRequestChangesResponse getPullRequestChanges(AzurePullRequestContext context, String repositoryId, long iterationId);

    byte[] getFileContent(AzurePullRequestContext context, String repositoryId, String objectId);
}
