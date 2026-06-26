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

package io.github.bksantani.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "skills.web.providers.azure")
public class AzureCloudApiProperties {

    @NotBlank
    private String baseUrl;

    @NotBlank
    private String apiVersion;

    @Positive
    private int maxFilesToProcess;

    @Positive
    private int maxFileBytes;

    @Valid
    @NotNull
    private Endpoints endpoints;

    @Getter
    @Setter
    public static class Endpoints {
        @NotBlank private String prDetails;
        @NotBlank private String prThreads;
        @NotBlank private String prIterations;
        @NotBlank private String prIterationChanges;
        @NotBlank private String blob;
    }
}
