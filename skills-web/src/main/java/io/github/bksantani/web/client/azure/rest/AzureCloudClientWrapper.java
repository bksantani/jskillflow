package io.github.bksantani.web.client.azure.rest;


import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import io.github.bksantani.web.exceptions.AzureDevOpsClientException;
import io.github.bksantani.web.exceptions.SkillsWebServiceException;
import io.github.bksantani.web.utils.AzureCloudPatContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AzureCloudClientWrapper {

    private final RestClient azureRestClient;

    public <T> T get(String url, Class<T> responseType) {
        return getBody(url, responseType, "Azure DevOps request failed.");
    }

    public byte[] getBytes(String url) {
        return getBody(url, byte[].class, "Azure DevOps file request failed.");
    }

    private <T> T getBody(String url, Class<T> bodyType, String errorMessage) {
        try {
            var response =  azureRestClient.get()
                    .uri(url)
                    .header("Authorization", toBasicAuth())
                    .retrieve()
                    .body(bodyType);
            if (response == null) {
                throw new SkillsWebServiceException("Azure DevOps request returned null response for URL: " + url);
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new AzureDevOpsClientException(errorMessage, ex.getStatusCode().value(), ex);
        } catch (IllegalStateException ex) {
            throw new AzureDevOpsClientException(ex.getMessage(), 400, ex);
        } catch (Exception ex) {
            throw new AzureDevOpsClientException(errorMessage, 502, ex);
        }
    }

    private String toBasicAuth() {
        String pat = AzureCloudPatContext.getRequired();
        String payload = ":" + pat;
        return "Basic " + Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

}
