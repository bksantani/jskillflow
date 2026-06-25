package io.github.bksantani.web.client.azure.model;

import java.util.List;

public record AzurePullRequestChangesResponse(
    List<ChangeEntry> changeEntries
) {
    public record ChangeEntry(
        String changeType,
        Item item
    ) {
    }

    public record Item(
        String path,
        String originalObjectId,
        String objectId
    ) {
    }
}

