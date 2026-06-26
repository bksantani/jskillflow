package io.github.bksantani.web.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import io.github.bksantani.web.api.error.ApiError;
import io.github.bksantani.web.exceptions.AzureDevOpsClientException;
import io.github.bksantani.web.exceptions.CredentialContextException;

@ExtendWith(OutputCaptureExtension.class)
class SkillsWebExceptionHandlerTest {

    private final SkillsWebExceptionHandler handler = new SkillsWebExceptionHandler();

    @Test
    void when_credentialContextFailure_then_returnsUnauthorizedAndLogsCause(CapturedOutput output) {
        Throwable cause = new IllegalStateException("missing PAT");
        CredentialContextException ex = new CredentialContextException("Credential context unavailable");
        ex.initCause(cause);

        ApiError apiError = handler.handleCredentialContext(ex);

        assertThat(apiError.status()).isEqualTo(401);
        assertThat(apiError.message()).isEqualTo("Credential context unavailable");
        assertThat(output).contains("Credential context failure");
        assertThat(output).contains("cause=missing PAT");
        assertThat(output).contains("CredentialContextException");
    }

    @Test
    void when_bodyValidationFails_then_returnsBadRequestAndLogsCause(CapturedOutput output) {
        FieldError fieldError = new FieldError("createSkillRequest", "pullRequestUrl", "pullRequestUrl is required");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(ex.getCause()).thenReturn(new IllegalArgumentException("invalid payload"));

        ApiError apiError = handler.handleBodyValidation(ex);

        assertThat(apiError.status()).isEqualTo(400);
        assertThat(apiError.message()).isEqualTo("pullRequestUrl is required");
        assertThat(output).contains("Request body validation failure");
        assertThat(output).contains("cause=invalid payload");
        assertThat(output).contains("MethodArgumentNotValidException");
    }

    @Test
    void when_azureClientFailureHasInvalidStatus_then_fallsBackToBadGatewayAndLogsDetails(CapturedOutput output) {
        AzureDevOpsClientException ex = new AzureDevOpsClientException(
            "Azure returned error",
            700,
            new RuntimeException("connection reset")
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        ApiError apiError = handler.handleAzure(ex, response);

        assertThat(response.getStatus()).isEqualTo(502);
        assertThat(apiError.status()).isEqualTo(502);
        assertThat(apiError.message()).isEqualTo("Azure DevOps request failed.");
        assertThat(output).contains("Azure DevOps request failure");
        assertThat(output).contains("upstreamStatus=700");
        assertThat(output).contains("responseStatus=502");
        assertThat(output).contains("cause=connection reset");
        assertThat(output).contains("AzureDevOpsClientException");
    }

    @Test
    void when_azureClientFailureHasLowInvalidStatus_then_fallsBackToBadGatewayAndLogsDetails(CapturedOutput output) {
        AzureDevOpsClientException ex = new AzureDevOpsClientException(
            "Azure returned error",
            99,
            new RuntimeException("connection reset")
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        ApiError apiError = handler.handleAzure(ex, response);

        assertThat(response.getStatus()).isEqualTo(502);
        assertThat(apiError.status()).isEqualTo(502);
        assertThat(apiError.message()).isEqualTo("Azure DevOps request failed.");
        assertThat(output).contains("Azure DevOps request failure");
        assertThat(output).contains("upstreamStatus=99");
        assertThat(output).contains("responseStatus=502");
        assertThat(output).contains("cause=connection reset");
        assertThat(output).contains("AzureDevOpsClientException");
    }

    @Test
    void when_azureClientFailureHasNullCause_then_logsNaAndUsesUpstreamStatus(CapturedOutput output) {
        AzureDevOpsClientException ex = new AzureDevOpsClientException(
            "Azure returned error",
            200,
            null
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        ApiError apiError = handler.handleAzure(ex, response);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(apiError.status()).isEqualTo(200);
        assertThat(apiError.message()).isEqualTo("Azure DevOps request failed.");
        assertThat(output).contains("Azure DevOps request failure");
        assertThat(output).contains("upstreamStatus=200");
        assertThat(output).contains("responseStatus=200");
        assertThat(output).contains("cause=n/a");
        assertThat(output).contains("AzureDevOpsClientException");
    }

    @Test
    void when_runtimeFailure_then_returnsInternalServerErrorAndLogsCause(CapturedOutput output) {
        RuntimeException ex = new RuntimeException("processing failed", new IllegalStateException("cache miss"));

        ApiError apiError = handler.handleRuntime(ex);

        assertThat(apiError.status()).isEqualTo(500);
        assertThat(apiError.message()).isEqualTo("Unexpected server error.");
        assertThat(output).contains("Runtime failure");
        assertThat(output).contains("cause=cache miss");
        assertThat(output).contains("RuntimeException");
    }

    @Test
    void when_unknownFailure_then_returnsInternalServerErrorAndLogsCause(CapturedOutput output) {
        Exception ex = new Exception("unexpected", new IllegalArgumentException("unknown source"));

        ApiError apiError = handler.handleUnknown(ex);

        assertThat(apiError.status()).isEqualTo(500);
        assertThat(apiError.message()).isEqualTo("Unexpected server error.");
        assertThat(output).contains("Unhandled failure");
        assertThat(output).contains("cause=unknown source");
        assertThat(output).contains("java.lang.Exception");
    }
}

