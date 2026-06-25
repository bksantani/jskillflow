package io.github.bksantani.web.model;

import static org.springframework.util.StringUtils.hasText;

public record PullRequestChange(
    String path,
    String changeType,
    String originalObjectId,
    String objectId
) {

    public boolean isAdded() {
        return "add".equalsIgnoreCase(defaultString(changeType));
    }

    public boolean isDeleted() {
        return "delete".equalsIgnoreCase(defaultString(changeType));
    }

    public boolean hasMissingPath() {
        return !hasText(path);
    }

    public boolean canCreateDiff() {
        return (isDeleted() || hasText(objectId))
            && (isAdded() || hasText(originalObjectId));
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }
}

