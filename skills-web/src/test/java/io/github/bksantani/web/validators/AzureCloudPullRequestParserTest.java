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

package io.github.bksantani.web.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.bksantani.web.client.azure.AzurePullRequestContext;
import io.github.bksantani.web.exceptions.SkillsWebBadRequestException;

class AzureCloudPullRequestParserTest {

    private final AzureCloudPullRequestParser parser = new AzureCloudPullRequestParser();

    @ParameterizedTest
    @MethodSource("validPullRequestUrls")
    void when_validAzurePullRequestUrl_then_returnsParsedContext(String prUrl, String expectedOrganization,
            String expectedProject, int expectedPullRequestId) {
        AzurePullRequestContext context = parser.parse(prUrl);

        assertEquals(expectedOrganization, context.organization());
        assertEquals(expectedProject, context.project());
        assertEquals(expectedPullRequestId, context.pullRequestId());
    }

    @ParameterizedTest
    @MethodSource("invalidPullRequestUrls")
    void when_invalidAzurePullRequestUrl_then_throwsBadRequest(String prUrl, String expectedErrorMessage) {
        SkillsWebBadRequestException ex = assertThrows(SkillsWebBadRequestException.class, () -> parser.parse(prUrl));

        assertEquals(expectedErrorMessage, ex.getMessage());
    }

    private static Stream<Arguments> validPullRequestUrls() {
        return Stream.of(
                Arguments.of("https://dev.azure.com/my-org/my-project/_git/my-repo/pullrequest/12", "my-org",
                        "my-project", 12),
                Arguments.of("https://DEV.AZURE.COM/org/project/_git/repo/pullrequest/1", "org", "project", 1),
                Arguments.of("https://dev.azure.com/org-2/project-2/_git/repo/pullrequest/999?api-version=7.1",
                        "org-2", "project-2", 999)
        );
    }

    private static Stream<Arguments> invalidPullRequestUrls() {
        return Stream.of(
                Arguments.of(null, "prUrl is required"),
                Arguments.of("", "prUrl is required"),
                Arguments.of("   ", "prUrl is required"),
                Arguments.of("http://dev.azure.com/org/project/_git/repo/pullrequest/1", "Only https is allowed"),
                Arguments.of("https://example.com/org/project/_git/repo/pullrequest/1", "Only dev.azure.com is allowed"),
                Arguments.of("https://dev.azure.com/org/project/repo/pullrequest/1", "Invalid Azure PR URL path format"),
                Arguments.of("https://dev.azure.com/org/project/_git/repo/pullrequest/not-a-number",
                        "Invalid pull request id")
        );
    }
}

