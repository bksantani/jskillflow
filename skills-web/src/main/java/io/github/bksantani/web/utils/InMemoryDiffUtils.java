package io.github.bksantani.web.utils;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import io.github.bksantani.web.model.DiffBlock;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InMemoryDiffUtils {

    public static DiffBlock createDiff(String filePath, @Nullable String originalContent, @Nullable String revisedContent) {
        List<String> originalLines = toLines(originalContent);
        List<String> revisedLines = toLines(revisedContent);

        Patch<String> patch = DiffUtils.diff(originalLines, revisedLines);
        List<String> unified = UnifiedDiffUtils.generateUnifiedDiff(
            "a" + filePath,
            "b" + filePath,
            originalLines,
            patch,
            3
        );

        return new DiffBlock(filePath, String.join("\n", unified));
    }

    private static List<String> toLines(@Nullable String content) {
        return Arrays.asList(Objects.requireNonNullElse(content, "").split("\\R", -1));
    }
}

