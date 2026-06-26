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

import static io.github.bksantani.web.core.CredentialProvider.CredentialBinding;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.bksantani.web.request.PullRequestProvider;
import io.github.bksantani.web.core.CredentialProvider;
import io.github.bksantani.web.exceptions.CredentialContextException;

@Component
public class CredentialProviderInterceptor implements HandlerInterceptor {

    private static final String PROVIDER_HEADER = "X-Provider-Name";
    private static final String BINDING_ATTR = "credential.binding";

    private final Map<PullRequestProvider, CredentialProvider> credentialProviders;

    public CredentialProviderInterceptor(List<CredentialProvider> providers) {
        this.credentialProviders = providers.stream()
                .collect(Collectors.toMap(CredentialProvider::provider, p -> p));
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

        String providerHeader = request.getHeader(PROVIDER_HEADER);
        if (providerHeader == null || providerHeader.isBlank()) {
            throw new CredentialContextException("Missing required provider header: " + PROVIDER_HEADER);
        }

        PullRequestProvider provider;
        try {
            provider = PullRequestProvider.valueOf(providerHeader);
        } catch (IllegalArgumentException ex) {
            throw new CredentialContextException("Unsupported provider: " + providerHeader);
        }

        CredentialProvider credentialProvider = credentialProviders.get(provider);
        if (credentialProvider == null) {
            throw new CredentialContextException("No credential provider found for: " + provider);
        }

        HttpHeaders headers = new ServletServerHttpRequest(request).getHeaders();
        CredentialBinding binding = credentialProvider.bind(headers);

        request.setAttribute(BINDING_ATTR, binding);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex) {
        Object binding = request.getAttribute(BINDING_ATTR);
        if (binding instanceof CredentialBinding cb) {
            cb.close();
        }
    }
}

