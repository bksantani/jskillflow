/*
 * Copyright 2026 Bharat Santani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.bksantani.web.core;

import static io.github.bksantani.web.utils.InMemoryDiffUtils.createDiff;
import static io.github.bksantani.web.utils.ListUtils.responseOrEmptyList;
import static io.github.bksantani.web.utils.StringUtils.valueOrEmptyString;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
    private static final Set<String> UNSUPPORTED_PROMPT_FILE_EXTENSIONS = Set.of(
            ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".ico", ".webp", ".svg",
            ".pdf", ".zip", ".gz", ".tar", ".tgz", ".7z", ".rar",
            ".jar", ".war", ".ear", ".class", ".dll", ".dylib", ".so", ".exe", ".bin",
            ".woff", ".woff2", ".ttf", ".eot", ".otf",
            ".mp3", ".wav", ".ogg", ".mp4", ".mov", ".avi", ".webm"
    );
    private static final int BINARY_DETECTION_SAMPLE_SIZE = 1024;

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

            if (isUnsupportedPromptFile(change.path())) {
                warnings.add("Skipped file due to unsupported file type for prompt generation: " + change.path());
                continue;
            }

            if (!change.canCreateDiff()) {
                warnings.add("Skipped file due to missing object ids: " + change.path());
                continue;
            }

            validChanges.add(change);
        }

        int maxFilesToProcess = azureCloudApiProperties.getMaxFilesToProcess();
        List<PullRequestChange> changesToProcess = validChanges.stream().limit(maxFilesToProcess).toList();

        for (int index = maxFilesToProcess; index < validChanges.size(); index++) {
            warnings.add("Skipped file after reaching processing limit of "
                    + maxFilesToProcess
                    + " changed files: "
                    + validChanges.get(index).path());
        }

        for (PullRequestChange change : changesToProcess) {
            try {
                String original = loadOriginalContent(context, repositoryId, change);
                String updated = loadUpdatedContent(context, repositoryId, change);
                diffs.add(createDiff(change.path(), original, updated));
            } catch (FileTooLargeException | PromptFileSkipException ex) {
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

        if (isLikelyBinary(bytes)) {
            throw new PromptFileSkipException("Skipped file due to unsupported or binary content: " + path);
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }

    private boolean isUnsupportedPromptFile(String path) {
        String normalizedPath = valueOrEmptyString(path).toLowerCase(Locale.ROOT);
        return UNSUPPORTED_PROMPT_FILE_EXTENSIONS.stream().anyMatch(normalizedPath::endsWith);
    }

    private boolean isLikelyBinary(byte[] bytes) {
        int limit = Math.min(bytes.length, BINARY_DETECTION_SAMPLE_SIZE);
        int suspiciousBytes = 0;

        for (int index = 0; index < limit; index++) {
            int value = bytes[index] & 0xFF;
            if (value == 0) {
                return true;
            }
            if (value < 0x09 || (value > 0x0D && value < 0x20)) {
                suspiciousBytes++;
            }
        }

        return limit > 0 && suspiciousBytes * 10 >= limit;
    }

    private static final class PromptFileSkipException extends RuntimeException {

        private PromptFileSkipException(String message) {
            super(message);
        }
    }

}
