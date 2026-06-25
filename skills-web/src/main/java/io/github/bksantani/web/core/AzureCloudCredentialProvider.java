package io.github.bksantani.web.core;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import io.github.bksantani.web.request.PullRequestProvider;
import io.github.bksantani.web.utils.AzureCloudPatContext;

@Component
public class AzureCloudCredentialProvider implements CredentialProvider {

    private static final String AZURE_CLOUD_PAT_HEADER = "X-Azure-DevOps-PAT";

    @Override
    public PullRequestProvider provider() {
        return PullRequestProvider.AZURE_CLOUD;
    }

    @Override
    public CredentialBinding bind(HttpHeaders headers) {
        AzureCloudPatContext.set(headers.getFirst(AZURE_CLOUD_PAT_HEADER));
        return AzureCloudPatContext::clear;
    }
}
