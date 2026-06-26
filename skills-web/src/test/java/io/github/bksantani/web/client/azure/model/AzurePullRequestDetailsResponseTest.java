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

package io.github.bksantani.web.client.azure.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.bksantani.web.exceptions.SkillsWebServiceException;

class AzurePullRequestDetailsResponseTest {

    @Test
    void when_repositoryIdIsPresent_then_returnsRepositoryId() {
        AzurePullRequestDetailsResponse response = new AzurePullRequestDetailsResponse(
                "title",
                "description",
                new AzurePullRequestDetailsResponse.Repository("repo-123")
        );

        assertThat(response.repositoryIdOrThrow()).isEqualTo("repo-123");
    }

    @Test
    void when_repositoryIsNull_then_throwsServiceException() {
        AzurePullRequestDetailsResponse response = new AzurePullRequestDetailsResponse(
                "title",
                "description",
                null
        );

        assertThatThrownBy(response::repositoryIdOrThrow)
                .isInstanceOfSatisfying(SkillsWebServiceException.class, ex ->
                        assertThat(ex.getMessage()).isEqualTo("Missing repository id in Azure pull request details response."));
    }

    @ParameterizedTest
    @MethodSource("invalidRepositoryIds")
    void when_repositoryIdIsMissingOrBlank_then_throwsServiceException(String repositoryId) {
        AzurePullRequestDetailsResponse response = new AzurePullRequestDetailsResponse(
                "title",
                "description",
                new AzurePullRequestDetailsResponse.Repository(repositoryId)
        );

        assertThatThrownBy(response::repositoryIdOrThrow)
                .isInstanceOfSatisfying(SkillsWebServiceException.class, ex ->
                        assertThat(ex.getMessage()).isEqualTo("Missing repository id in Azure pull request details response."));
    }

    private static Stream<Arguments> invalidRepositoryIds() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of(""),
                Arguments.of("   ")
        );
    }
}
