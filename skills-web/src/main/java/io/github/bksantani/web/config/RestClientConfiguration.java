package io.github.bksantani.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    @Bean
    public RestClient azureRestClient(AzureCloudApiProperties azureCloudApiProperties) {
        return RestClient.builder()
                .baseUrl(azureCloudApiProperties.getBaseUrl())
                .build();
    }
}
