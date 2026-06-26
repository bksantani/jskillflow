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

package io.github.bksantani.web.client.azure.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import io.github.bksantani.web.exceptions.AzureDevOpsClientException;
import io.github.bksantani.web.exceptions.SkillsWebServiceException;
import io.github.bksantani.web.utils.AzureCloudPatContext;

@SuppressWarnings({ "rawtypes", "unchecked" })
class AzureCloudClientWrapperTest {

    private static final String URL = "https://dev.azure.com/my-org/my-project/_apis/git/repositories/repo";

    private final RestClient azureRestClient = mock(RestClient.class);
    private final RestClient.RequestHeadersUriSpec requestSpec = mock(RestClient.RequestHeadersUriSpec.class);
    private final RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    private AzureCloudClientWrapper wrapper;

    @BeforeEach
    void setUp() {
        wrapper = new AzureCloudClientWrapper(azureRestClient);
        when(azureRestClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(URL)).thenReturn(requestSpec);
        when(requestSpec.header(eq("Authorization"), anyString())).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
    }

    @AfterEach
    void tearDown() {
        AzureCloudPatContext.clear();
    }

    @Test
    void when_azureReturnsHttpError_then_wrapsStatusAndCause() {
        AzureCloudPatContext.set("test-pat");
        RestClientResponseException cause = new RestClientResponseException(
                "Not found",
                404,
                "Not Found",
                null,
                null,
                StandardCharsets.UTF_8
        );
        when(responseSpec.body(String.class)).thenThrow(cause);

        assertThatThrownBy(() -> wrapper.get(URL, String.class))
                .isInstanceOfSatisfying(AzureDevOpsClientException.class, ex -> {
                    assertThat(ex.getMessage()).isEqualTo("Azure DevOps request failed.");
                    assertThat(ex.getStatusCode()).isEqualTo(404);
                    assertThat(ex.getCause()).isSameAs(cause);
                });
    }

    @Test
    void when_patIsMissing_then_returnsBadRequestClientException() {
        assertThatThrownBy(() -> wrapper.get(URL, String.class))
                .isInstanceOfSatisfying(AzureDevOpsClientException.class, ex -> {
                    assertThat(ex.getMessage()).isEqualTo("Missing required PAT header.");
                    assertThat(ex.getStatusCode()).isEqualTo(400);
                    assertThat(ex.getCause())
                            .isInstanceOfSatisfying(IllegalStateException.class,
                                    cause -> assertThat(cause.getMessage()).isEqualTo("Missing required PAT header."));
                });
    }

    @Test
    void when_responseBodyIsNull_then_mapsToBadGateway() {
        AzureCloudPatContext.set("test-pat");
        when(responseSpec.body(String.class)).thenReturn(null);

        assertThatThrownBy(() -> wrapper.get(URL, String.class))
                .isInstanceOfSatisfying(AzureDevOpsClientException.class, ex -> {
                    assertThat(ex.getMessage()).isEqualTo("Azure DevOps request failed.");
                    assertThat(ex.getStatusCode()).isEqualTo(502);
                    assertThat(ex.getCause())
                            .isInstanceOfSatisfying(SkillsWebServiceException.class,
                                    cause -> assertThat(cause.getMessage())
                                            .isEqualTo("Azure DevOps request returned null response for URL: " + URL));
                });
    }

    @Test
    void when_unexpectedFailureOccurs_then_wrapsAsBadGateway() {
        AzureCloudPatContext.set("test-pat");
        RuntimeException cause = new RuntimeException("connection reset");
        when(responseSpec.body(byte[].class)).thenThrow(cause);

        assertThatThrownBy(() -> wrapper.getBytes(URL))
                .isInstanceOfSatisfying(AzureDevOpsClientException.class, ex -> {
                    assertThat(ex.getMessage()).isEqualTo("Azure DevOps file request failed.");
                    assertThat(ex.getStatusCode()).isEqualTo(502);
                    assertThat(ex.getCause()).isSameAs(cause);
                });
    }
}

