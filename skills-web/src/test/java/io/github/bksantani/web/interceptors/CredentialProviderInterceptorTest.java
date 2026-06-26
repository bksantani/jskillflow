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

package io.github.bksantani.web.interceptors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import io.github.bksantani.web.core.CredentialProvider;
import io.github.bksantani.web.exceptions.CredentialContextException;
import io.github.bksantani.web.request.PullRequestProvider;

class CredentialProviderInterceptorTest {

    private static final String PROVIDER_HEADER = "X-Provider-Name";

    private CredentialProviderInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new CredentialProviderInterceptor(List.of());
    }

    @Test
    void when_providerHeaderIsMissing_then_throwsCredentialContextException() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOfSatisfying(CredentialContextException.class,
                        ex -> assertThat(ex.getMessage())
                                .isEqualTo("Missing required provider header: " + PROVIDER_HEADER));
    }

    @Test
    void when_providerHeaderIsBlank_then_throwsCredentialContextException() {
        MockHttpServletRequest request = requestWithProviderHeader("   ");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOfSatisfying(CredentialContextException.class,
                        ex -> assertThat(ex.getMessage())
                                .isEqualTo("Missing required provider header: " + PROVIDER_HEADER));
    }

    @Test
    void when_providerHeaderIsUnsupported_then_throwsCredentialContextException() {
        MockHttpServletRequest request = requestWithProviderHeader("UNKNOWN_PROVIDER");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOfSatisfying(CredentialContextException.class,
                        ex -> assertThat(ex.getMessage())
                                .isEqualTo("Unsupported provider: UNKNOWN_PROVIDER"));
    }

    @Test
    void when_providerIsValidButNotRegistered_then_throwsCredentialContextException() {
        MockHttpServletRequest request = requestWithProviderHeader(PullRequestProvider.AZURE_CLOUD.name());

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOfSatisfying(CredentialContextException.class,
                        ex -> assertThat(ex.getMessage())
                                .isEqualTo("No credential provider found for: AZURE_CLOUD"));
    }

    @Test
    void when_providerIsRegistered_then_allowsPreHandle() throws Exception {
        CredentialProvider provider = mock(CredentialProvider.class);
        when(provider.provider()).thenReturn(PullRequestProvider.AZURE_CLOUD);
        interceptor = new CredentialProviderInterceptor(List.of(provider));

        MockHttpServletRequest request = requestWithProviderHeader(PullRequestProvider.AZURE_CLOUD.name());

        assertThat(
                interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isTrue();
    }

    private static MockHttpServletRequest requestWithProviderHeader(String value) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(PROVIDER_HEADER, value);
        return request;
    }
}
