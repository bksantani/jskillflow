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
