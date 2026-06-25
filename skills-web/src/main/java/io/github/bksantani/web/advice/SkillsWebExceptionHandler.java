package io.github.bksantani.web.advice;

import io.github.bksantani.web.exceptions.AzureDevOpsClientException;
import io.github.bksantani.web.exceptions.CredentialContextException;
import io.github.bksantani.web.exceptions.SkillMarkdownGenerationException;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SkillsWebExceptionHandler {

    @ExceptionHandler(CredentialContextException.class)
    public ResponseEntity<Map<String, String>> handleCredentialContext(CredentialContextException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleBodyValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(org.springframework.validation.FieldError::getDefaultMessage)
            .orElse("Invalid request body.");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    @ExceptionHandler(AzureDevOpsClientException.class)
    public ResponseEntity<Map<String, String>> handleAzure(AzureDevOpsClientException ex) {
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        if (ex.getStatusCode() == 401) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (ex.getStatusCode() == 403) {
            status = HttpStatus.FORBIDDEN;
        }
        return ResponseEntity.status(status).body(Map.of("message", "Azure DevOps request failed."));
    }

    @ExceptionHandler(SkillMarkdownGenerationException.class)
    public ResponseEntity<Map<String, String>> handleSkillMarkdownGeneration(SkillMarkdownGenerationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnknown(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", "Unexpected server error."));
    }
}

