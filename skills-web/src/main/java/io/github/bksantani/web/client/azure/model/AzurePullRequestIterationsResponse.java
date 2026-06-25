package io.github.bksantani.web.client.azure.model;

import static io.github.bksantani.web.utils.ListUtils.responseOrEmptyList;

import java.util.List;
import java.util.Objects;

public record AzurePullRequestIterationsResponse(
    List<Iteration> value
) {
    public record Iteration(Long id) {
    }

    public long latestIterationId() {
        return responseOrEmptyList(value).stream()
                .filter(Objects::nonNull)
                .map(AzurePullRequestIterationsResponse.Iteration::id)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(1L);
    }
}

