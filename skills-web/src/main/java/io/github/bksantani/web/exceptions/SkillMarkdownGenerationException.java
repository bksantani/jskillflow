package io.github.bksantani.web.exceptions;

public class SkillMarkdownGenerationException extends RuntimeException {

    public SkillMarkdownGenerationException(String message) {
        super(message);
    }

    public SkillMarkdownGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

