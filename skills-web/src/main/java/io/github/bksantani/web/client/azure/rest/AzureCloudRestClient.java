package io.github.bksantani.web.client.azure.rest;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import io.github.bksantani.web.client.azure.AzureCloudClient;
import io.github.bksantani.web.client.azure.AzurePullRequestContext;
import io.github.bksantani.web.client.azure.model.AzurePullRequestChangesResponse;
import io.github.bksantani.web.client.azure.model.AzurePullRequestDetailsResponse;
import io.github.bksantani.web.client.azure.model.AzurePullRequestIterationsResponse;
import io.github.bksantani.web.client.azure.model.AzurePullRequestThreadsResponse;
import io.github.bksantani.web.config.AzureCloudApiProperties;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AzureCloudRestClient implements AzureCloudClient {

    private static final String API_VERSION_QUERY_PARAM = "api-version";

    private final AzureCloudClientWrapper azureCloudClientWrapper;
    private final AzureCloudApiProperties azureCloudApiProperties;

    @Override
    public AzurePullRequestDetailsResponse getPullRequestDetails(AzurePullRequestContext context) {
        // /{organization}/{project}/_apis/git/pullrequests/{pullRequestId}
        String url = UriComponentsBuilder.fromUriString(azureCloudApiProperties.getEndpoints().getPrDetails())
                .queryParam(API_VERSION_QUERY_PARAM, azureCloudApiProperties.getApiVersion())
                .build(
                        context.organization(),
                        context.project(),
                        context.pullRequestId()
                ).toString();

        System.out.println("Url is "+url);

        return azureCloudClientWrapper.get(url, AzurePullRequestDetailsResponse.class);
    }

    @Override
    public AzurePullRequestThreadsResponse getPullRequestThreads(AzurePullRequestContext context, String repositoryId) {
        // /{organization}/{project}/_apis/git/repositories/{repositoryId}/pullrequests/{pullRequestId}/threads
        String url = UriComponentsBuilder.fromUriString(azureCloudApiProperties.getEndpoints().getPrThreads())
                .queryParam(API_VERSION_QUERY_PARAM, azureCloudApiProperties.getApiVersion())
                .build(
                        context.organization(),
                        context.project(),
                        repositoryId,
                        context.pullRequestId()
                ).toString();

        return azureCloudClientWrapper.get(url, AzurePullRequestThreadsResponse.class);
    }

    @Override
    public AzurePullRequestIterationsResponse getPullRequestIterations(AzurePullRequestContext context, String repositoryId) {
        // /{organization}/{project}/_apis/git/repositories/{repositoryId}/pullrequests/{pullRequestId}/iterations
        String url = UriComponentsBuilder.fromUriString(azureCloudApiProperties.getEndpoints().getPrIterations()).build(
                context.organization(),
                context.project(),
                repositoryId,
                context.pullRequestId()
        ).toString();

        return azureCloudClientWrapper.get(url, AzurePullRequestIterationsResponse.class);
    }

    @Override
    public AzurePullRequestChangesResponse getPullRequestChanges(AzurePullRequestContext context, String repositoryId, long iterationId) {
        // /{organization}/{project}/_apis/git/repositories/{repositoryId}/pullrequests/{pullRequestId}/iterations/{iterationId}/changes
        String url = UriComponentsBuilder.fromUriString(azureCloudApiProperties.getEndpoints().getPrIterationChanges())
                .queryParam(API_VERSION_QUERY_PARAM, azureCloudApiProperties.getApiVersion())
                .build(
                        context.organization(),
                        context.project(),
                        repositoryId,
                        context.pullRequestId(),
                        iterationId
                ).toString();

        return azureCloudClientWrapper.get(url, AzurePullRequestChangesResponse.class);
    }

    @Override
    public byte[] getFileContent(AzurePullRequestContext context, String repositoryId, String objectId) {
        // /{organization}/{project}/_apis/git/repositories/{repositoryId}/blobs/{objectId}
        String url = UriComponentsBuilder.fromUriString(azureCloudApiProperties.getEndpoints().getBlob())
                .queryParam(API_VERSION_QUERY_PARAM, azureCloudApiProperties.getApiVersion())
                .build(
                        context.organization(),
                        context.project(),
                        repositoryId,
                        objectId
                ).toString();

        return azureCloudClientWrapper.getBytes(url);
    }
}
