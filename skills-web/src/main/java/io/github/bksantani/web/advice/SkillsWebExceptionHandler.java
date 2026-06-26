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

package io.github.bksantani.web.advice;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.bksantani.web.api.error.ApiError;
import io.github.bksantani.web.exceptions.AzureDevOpsClientException;
import io.github.bksantani.web.exceptions.CredentialContextException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class SkillsWebExceptionHandler {

    @ExceptionHandler(CredentialContextException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleCredentialContext(CredentialContextException ex) {
        log.error("Credential context failure: message={}, cause={}", ex.getMessage(), causeMessage(ex), ex);
        return new ApiError(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBodyValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(org.springframework.validation.FieldError::getDefaultMessage)
            .orElse("Invalid request body.");
        log.error("Request body validation failure: message={}, cause={}", message, causeMessage(ex), ex);
        return new ApiError(HttpStatus.BAD_REQUEST.value(), message);
    }

    @ExceptionHandler(AzureDevOpsClientException.class)
    public ApiError handleAzure(AzureDevOpsClientException ex, HttpServletResponse response) {
        int upstreamStatus = ex.getStatusCode();
        int responseStatus = upstreamStatus;
        if (responseStatus < 100 || responseStatus > 599) {
            responseStatus = HttpStatus.BAD_GATEWAY.value();
        }
        log.error(
            "Azure DevOps request failure: upstreamStatus={}, responseStatus={}, cause={}",
            upstreamStatus,
            responseStatus,
            causeMessage(ex),
            ex
        );
        response.setStatus(responseStatus);
        return new ApiError(responseStatus, "Azure DevOps request failed.");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleRuntime(RuntimeException ex) {
        log.error("Runtime failure: message={}, cause={}", ex.getMessage(), causeMessage(ex), ex);
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected server error.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnknown(Exception ex) {
        log.error("Unhandled failure: message={}, cause={}", ex.getMessage(), causeMessage(ex), ex);
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected server error.");
    }

    private String causeMessage(Throwable ex) {
        return ex.getCause() != null ? ex.getCause().getMessage() : "n/a";
    }
}

