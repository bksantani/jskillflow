package io.github.bksantani.web.exceptions;

import lombok.Getter;

@Getter
public class AzureDevOpsClientException extends RuntimeException {
    private final int statusCode;

    public AzureDevOpsClientException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

}

