package io.github.bksantani.web.model;

public record DiffBlock(
    String filePath,
    String unifiedDiff
) {
}

