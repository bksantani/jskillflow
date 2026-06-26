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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.bksantani.web.client.azure.AzureCloudClient;
import io.github.bksantani.web.client.azure.AzurePullRequestContext;
import io.github.bksantani.web.client.azure.model.AzurePullRequestChangesResponse;
import io.github.bksantani.web.client.azure.model.AzurePullRequestChangesResponse.ChangeEntry;
import io.github.bksantani.web.client.azure.model.AzurePullRequestChangesResponse.Item;
import io.github.bksantani.web.config.AzureCloudApiProperties;
import io.github.bksantani.web.exceptions.AzureDevOpsClientException;
import io.github.bksantani.web.model.DiffBlock;
import io.github.bksantani.web.model.ProcessedChangesResult;

class AzureCloudChangeProcessServiceTest {

    private static final AzurePullRequestContext CONTEXT = new AzurePullRequestContext("org", "project", 42);
    private static final String REPOSITORY_ID = "repo";

    private final AzureCloudClient azureCloudClient = mock(AzureCloudClient.class);
    private final AzureCloudApiProperties azureCloudApiProperties = mock(AzureCloudApiProperties.class);
    private final AzureCloudChangeProcessService service = new AzureCloudChangeProcessService(
            azureCloudClient,
            azureCloudApiProperties
    );

    @BeforeEach
    void setUp() {
        when(azureCloudApiProperties.getMaxFilesToProcess()).thenReturn(10);
        when(azureCloudApiProperties.getMaxFileBytes()).thenReturn(1024);
    }

    @Test
    void when_changesResponseIsNull_then_returnsWarningAndNoDiffs() {
        ProcessedChangesResult result = service.processChanges(CONTEXT, REPOSITORY_ID, null);

        assertThat(result.diffs()).isEmpty();
        assertThat(result.warnings()).containsExactly("No changes response received from Azure.");
        verifyNoInteractions(azureCloudClient);
    }

    @Test
    void when_changeEntriesAreNull_then_returnsEmptyResult() {
        ProcessedChangesResult result = service.processChanges(
                CONTEXT,
                REPOSITORY_ID,
                new AzurePullRequestChangesResponse(null)
        );

        assertThat(result.diffs()).isEmpty();
        assertThat(result.warnings()).isEmpty();
        verifyNoInteractions(azureCloudClient);
    }

    @Test
    void when_changesContainInvalidEntries_then_skipsThemWithWarnings() {
        ProcessedChangesResult result = service.processChanges(
                CONTEXT,
                REPOSITORY_ID,
                response(
                        null,
                        changeWithNullItem(),
                        change("edit", null, "old-object", "new-object"),
                        change("edit", "src/skip.txt", null, null)
                )
        );

        assertThat(result.diffs()).isEmpty();
        assertThat(result.warnings()).containsExactly(
                "Skipped file due to missing path.",
                "Skipped file due to missing path.",
                "Skipped file due to missing path.",
                "Skipped file due to missing object ids: src/skip.txt"
        );
        verifyNoInteractions(azureCloudClient);
    }

    @Test
    void when_fileIsAdded_then_fetchesOnlyUpdatedContent() {
        when(azureCloudClient.getFileContent(CONTEXT, REPOSITORY_ID, "new-object"))
                .thenReturn(bytes("new file"));

        ProcessedChangesResult result = service.processChanges(
                CONTEXT,
                REPOSITORY_ID,
                response(change("add", "src/new.txt", null, "new-object"))
        );

        assertThat(result.warnings()).isEmpty();
        assertThat(result.diffs()).hasSize(1);
        assertThat(result.diffs()).extracting(DiffBlock::filePath).containsExactly("src/new.txt");
        assertThat(result.diffs().getFirst().unifiedDiff()).contains("new file");

        verify(azureCloudClient).getFileContent(CONTEXT, REPOSITORY_ID, "new-object");
        verifyNoMoreInteractions(azureCloudClient);
    }

    @Test
    void when_fileIsDeleted_then_fetchesOnlyOriginalContent() {
        when(azureCloudClient.getFileContent(CONTEXT, REPOSITORY_ID, "old-object"))
                .thenReturn(bytes("old file"));

        ProcessedChangesResult result = service.processChanges(
                CONTEXT,
                REPOSITORY_ID,
                response(change("delete", "src/old.txt", "old-object", null))
        );

        assertThat(result.warnings()).isEmpty();
        assertThat(result.diffs()).hasSize(1);
        assertThat(result.diffs()).extracting(DiffBlock::filePath).containsExactly("src/old.txt");
        assertThat(result.diffs().getFirst().unifiedDiff()).contains("old file");

        verify(azureCloudClient).getFileContent(CONTEXT, REPOSITORY_ID, "old-object");
        verifyNoMoreInteractions(azureCloudClient);
    }

    @Test
    void when_fileIsModified_then_fetchesBothVersionsAndCreatesDiff() {
        when(azureCloudClient.getFileContent(CONTEXT, REPOSITORY_ID, "old-object"))
                .thenReturn(bytes("old value"));
        when(azureCloudClient.getFileContent(CONTEXT, REPOSITORY_ID, "new-object"))
                .thenReturn(bytes("new value"));

        ProcessedChangesResult result = service.processChanges(
                CONTEXT,
                REPOSITORY_ID,
                response(change("edit", "src/config.txt", "old-object", "new-object"))
        );

        assertThat(result.warnings()).isEmpty();
        assertThat(result.diffs()).hasSize(1);
        assertThat(result.diffs().getFirst().filePath()).isEqualTo("src/config.txt");
        assertThat(result.diffs().getFirst().unifiedDiff()).contains("-old value").contains("+new value");

        verify(azureCloudClient).getFileContent(CONTEXT, REPOSITORY_ID, "old-object");
        verify(azureCloudClient).getFileContent(CONTEXT, REPOSITORY_ID, "new-object");
        verifyNoMoreInteractions(azureCloudClient);
    }

    @Test
    void when_originalBytesAreNull_then_treatsThemAsEmptyContent() {
        when(azureCloudClient.getFileContent(CONTEXT, REPOSITORY_ID, "old-object"))
                .thenReturn(null);
        when(azureCloudClient.getFileContent(CONTEXT, REPOSITORY_ID, "new-object"))
                .thenReturn(bytes("new value"));

        ProcessedChangesResult result = service.processChanges(
                CONTEXT,
                REPOSITORY_ID,
                response(change("edit", "src/null-original.txt", "old-object", "new-object"))
        );

        assertThat(result.warnings()).isEmpty();
        assertThat(result.diffs()).hasSize(1);
        assertThat(result.diffs().getFirst().filePath()).isEqualTo("src/null-original.txt");
        assertThat(result.diffs().getFirst().unifiedDiff()).contains("+new value");

        verify(azureCloudClient).getFileContent(CONTEXT, REPOSITORY_ID, "old-object");
        verify(azureCloudClient).getFileContent(CONTEXT, REPOSITORY_ID, "new-object");
        verifyNoMoreInteractions(azureCloudClient);
    }

    @Test
    void when_maxFilesToProcessIsReached_then_ignoresRemainingValidChanges() {
        when(azureCloudApiProperties.getMaxFilesToProcess()).thenReturn(2);
        when(azureCloudClient.getFileContent(CONTEXT, REPOSITORY_ID, "object-1"))
                .thenReturn(bytes("file one"));
        when(azureCloudClient.getFileContent(CONTEXT, REPOSITORY_ID, "object-2"))
                .thenReturn(bytes("file two"));

        ProcessedChangesResult result = service.processChanges(
                CONTEXT,
                REPOSITORY_ID,
                response(
                        change("add", "src/one.txt", null, "object-1"),
                        change("add", "src/two.txt", null, "object-2"),
                        change("add", "src/three.txt", null, "object-3")
                )
        );

        assertThat(result.warnings()).containsExactly(
                "Skipped file after reaching processing limit of 2 changed files: src/three.txt"
        );
        assertThat(result.diffs()).hasSize(2);
        assertThat(result.diffs()).extracting(DiffBlock::filePath).containsExactly("src/one.txt", "src/two.txt");

        verify(azureCloudClient).getFileContent(CONTEXT, REPOSITORY_ID, "object-1");
        verify(azureCloudClient).getFileContent(CONTEXT, REPOSITORY_ID, "object-2");
        verifyNoMoreInteractions(azureCloudClient);
    }

    @Test
    void when_fileTypeIsUnsupported_then_skipsWithoutFetchingContent() {
        ProcessedChangesResult result = service.processChanges(
                CONTEXT,
                REPOSITORY_ID,
                response(change("add", "assets/architecture.svg", null, "svg-object"))
        );

        assertThat(result.diffs()).isEmpty();
        assertThat(result.warnings()).containsExactly(
                "Skipped file due to unsupported file type for prompt generation: assets/architecture.svg"
        );
        verifyNoInteractions(azureCloudClient);
    }

    @Test
    void when_fileContentLooksBinary_then_skipsFileAndReportsWarning() {
        when(azureCloudClient.getFileContent(CONTEXT, REPOSITORY_ID, "binary-object"))
                .thenReturn(new byte[] { 0x50, 0x4E, 0x47, 0x00, 0x01 });

        ProcessedChangesResult result = service.processChanges(
                CONTEXT,
                REPOSITORY_ID,
                response(change("add", "src/generated/no-extension", null, "binary-object"))
        );

        assertThat(result.diffs()).isEmpty();
        assertThat(result.warnings()).containsExactly(
                "Skipped file due to unsupported or binary content: src/generated/no-extension"
        );

        verify(azureCloudClient).getFileContent(CONTEXT, REPOSITORY_ID, "binary-object");
        verifyNoMoreInteractions(azureCloudClient);
    }

    @Test
    void when_originalContentExceedsLimit_then_skipsFileAndReportsWarning() {
        when(azureCloudApiProperties.getMaxFileBytes()).thenReturn(4);
        when(azureCloudClient.getFileContent(CONTEXT, REPOSITORY_ID, "old-object"))
                .thenReturn(bytes("12345"));

        ProcessedChangesResult result = service.processChanges(
                CONTEXT,
                REPOSITORY_ID,
                response(change("edit", "src/too-large-original.txt", "old-object", "new-object"))
        );

        assertThat(result.diffs()).isEmpty();
        assertThat(result.warnings()).containsExactly(
                "Skipped file due to size limit: src/too-large-original.txt (5 bytes)"
        );

        verify(azureCloudClient).getFileContent(CONTEXT, REPOSITORY_ID, "old-object");
        verifyNoMoreInteractions(azureCloudClient);
    }

    @Test
    void when_updatedContentExceedsLimit_then_skipsFileAndReportsWarning() {
        when(azureCloudApiProperties.getMaxFileBytes()).thenReturn(4);
        when(azureCloudClient.getFileContent(CONTEXT, REPOSITORY_ID, "old-object"))
                .thenReturn(bytes("1234"));
        when(azureCloudClient.getFileContent(CONTEXT, REPOSITORY_ID, "new-object"))
                .thenReturn(bytes("12345"));

        ProcessedChangesResult result = service.processChanges(
                CONTEXT,
                REPOSITORY_ID,
                response(change("edit", "src/too-large-updated.txt", "old-object", "new-object"))
        );

        assertThat(result.diffs()).isEmpty();
        assertThat(result.warnings()).containsExactly(
                "Skipped file due to size limit: src/too-large-updated.txt (5 bytes)"
        );

        verify(azureCloudClient).getFileContent(CONTEXT, REPOSITORY_ID, "old-object");
        verify(azureCloudClient).getFileContent(CONTEXT, REPOSITORY_ID, "new-object");
        verifyNoMoreInteractions(azureCloudClient);
    }

    @Test
    void when_clientThrows_then_warnsAndContinuesWithRemainingFiles() {
        when(azureCloudClient.getFileContent(CONTEXT, REPOSITORY_ID, "broken-object"))
                .thenThrow(new AzureDevOpsClientException("Azure DevOps request failed.", 502,
                        new RuntimeException("downstream unavailable")));
        when(azureCloudClient.getFileContent(CONTEXT, REPOSITORY_ID, "good-object"))
                .thenReturn(bytes("good file"));

        ProcessedChangesResult result = service.processChanges(
                CONTEXT,
                REPOSITORY_ID,
                response(
                        change("edit", "src/broken.txt", "broken-object", "broken-new-object"),
                        change("add", "src/good.txt", null, "good-object")
                )
        );

        assertThat(result.warnings()).containsExactly("Failed to fetch file content for: src/broken.txt");
        assertThat(result.diffs()).hasSize(1);
        assertThat(result.diffs()).extracting(DiffBlock::filePath).containsExactly("src/good.txt");

        verify(azureCloudClient).getFileContent(CONTEXT, REPOSITORY_ID, "broken-object");
        verify(azureCloudClient).getFileContent(CONTEXT, REPOSITORY_ID, "good-object");
        verifyNoMoreInteractions(azureCloudClient);
    }

    private static AzurePullRequestChangesResponse response(ChangeEntry... changeEntries) {
        return new AzurePullRequestChangesResponse(Arrays.asList(changeEntries));
    }

    private static ChangeEntry change(String changeType, String path, String originalObjectId, String objectId) {
        return new ChangeEntry(changeType, new Item(path, originalObjectId, objectId));
    }

    private static ChangeEntry changeWithNullItem() {
        return new ChangeEntry("edit", null);
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
