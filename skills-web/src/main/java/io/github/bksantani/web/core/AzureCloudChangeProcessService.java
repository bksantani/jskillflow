package io.github.bksantani.web.core;

import static io.github.bksantani.web.utils.InMemoryDiffUtils.createDiff;
import static io.github.bksantani.web.utils.ListUtils.responseOrEmptyList;
import static io.github.bksantani.web.utils.StringUtils.valueOrEmptyString;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import io.github.bksantani.web.client.azure.AzureCloudClient;
import io.github.bksantani.web.client.azure.AzurePullRequestContext;
import io.github.bksantani.web.client.azure.model.AzurePullRequestChangesResponse;
import io.github.bksantani.web.config.AzureCloudApiProperties;
import io.github.bksantani.web.exceptions.AzureDevOpsClientException;
import io.github.bksantani.web.exceptions.FileTooLargeException;
import io.github.bksantani.web.model.DiffBlock;
import io.github.bksantani.web.model.ProcessedChangesResult;
import io.github.bksantani.web.model.PullRequestChange;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AzureCloudChangeProcessService {

    private static final String EMPTY_FILE_CONTENT = "";

    private final AzureCloudClient azureCloudClient;
    private final AzureCloudApiProperties azureCloudApiProperties;

    public ProcessedChangesResult processChanges(
            AzurePullRequestContext context,
            String repositoryId,
            AzurePullRequestChangesResponse changes
    ) {
        var warnings = new ArrayList<String>();
        var diffs = new ArrayList<DiffBlock>();

        if (changes == null) {
            warnings.add("No changes response received from Azure.");
            return new ProcessedChangesResult(diffs, warnings);
        }

        var validChanges = new ArrayList<PullRequestChange>();

        for (AzurePullRequestChangesResponse.ChangeEntry changeEntry : responseOrEmptyList(changes.changeEntries())) {
            var item = changeEntry == null ? null : changeEntry.item();
            PullRequestChange change = new PullRequestChange(
                    item == null ? "" : valueOrEmptyString(item.path()),
                    changeEntry == null ? "" : valueOrEmptyString(changeEntry.changeType()),
                    item == null ? "" : valueOrEmptyString(item.originalObjectId()),
                    item == null ? "" : valueOrEmptyString(item.objectId())
            );

            if (change.hasMissingPath()) {
                warnings.add("Skipped file due to missing path.");
                continue;
            }

            if (!change.canCreateDiff()) {
                warnings.add("Skipped file due to missing object ids: " + change.path());
                continue;
            }

            validChanges.add(change);
        }

        for (PullRequestChange change : validChanges.stream().limit(azureCloudApiProperties.getMaxFilesToProcess()).toList()) {
            try {
                String original = loadOriginalContent(context, repositoryId, change);
                String updated = loadUpdatedContent(context, repositoryId, change);
                diffs.add(createDiff(change.path(), original, updated));
            } catch (FileTooLargeException ex) {
                warnings.add(ex.getMessage());
            } catch (AzureDevOpsClientException ex) {
                warnings.add("Failed to fetch file content for: " + change.path());
            }
        }

        return new ProcessedChangesResult(diffs, warnings);
    }

    private String loadOriginalContent(AzurePullRequestContext context, String repositoryId, PullRequestChange change) {
        if (change.isAdded()) {
            return EMPTY_FILE_CONTENT;
        }
        return fetchFile(context, repositoryId, change.path(), change.originalObjectId());
    }

    private String loadUpdatedContent(AzurePullRequestContext context, String repositoryId, PullRequestChange change) {
        if (change.isDeleted()) {
            return EMPTY_FILE_CONTENT;
        }
        return fetchFile(context, repositoryId, change.path(), change.objectId());
    }

    private String fetchFile(AzurePullRequestContext context, String repositoryId, String path, String objectId) {
        byte[] bytes = azureCloudClient.getFileContent(context, repositoryId, objectId);

        if (bytes == null) {
            return EMPTY_FILE_CONTENT;
        }

        if (bytes.length > azureCloudApiProperties.getMaxFileBytes()) {
            throw new FileTooLargeException("Skipped file due to size limit: " + path + " (" + bytes.length + " bytes)");
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }

}
