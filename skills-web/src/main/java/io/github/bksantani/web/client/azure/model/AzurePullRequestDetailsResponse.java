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

import static org.springframework.util.StringUtils.hasText;

import io.github.bksantani.web.exceptions.SkillsWebServiceException;

public record AzurePullRequestDetailsResponse(
    String title,
    String description,
    Repository repository
) {
    public record Repository(String id) {
    }

    public String repositoryIdOrThrow() {
        if (repository == null || !hasText(repository.id())) {
            throw new SkillsWebServiceException("Missing repository id in Azure pull request details response.");
        }
        return repository.id();
    }
}
