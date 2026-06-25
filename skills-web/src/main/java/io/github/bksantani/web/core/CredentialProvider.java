package io.github.bksantani.web.core;

import org.springframework.http.HttpHeaders;

import io.github.bksantani.web.request.PullRequestProvider;

public interface CredentialProvider {
    PullRequestProvider provider();
    CredentialBinding bind(HttpHeaders headers);

    @FunctionalInterface
    interface CredentialBinding extends AutoCloseable {
        @Override
        void close();
    }
}
