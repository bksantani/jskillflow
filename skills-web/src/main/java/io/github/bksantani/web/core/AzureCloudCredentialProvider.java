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
