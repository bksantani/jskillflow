package io.github.bksantani.web.core;

import static io.github.bksantani.web.utils.StringUtils.valueOrEmptyString;

import java.util.List;

import org.springframework.stereotype.Service;

import io.github.bksantani.web.model.DiffBlock;

@Service
public class PullRequestContentService {

    public String pullRequestContent(
            String title,
            String description,
            List<String> comments,
            List<DiffBlock> diffs
    ) {
        String pullRequestTitle  =  valueOrEmptyString(title).trim();
        String pullRequestDescription = valueOrEmptyString(description).trim();

        StringBuilder sb = new StringBuilder();
        sb.append("## Pull Request Summary\n");
        sb.append("Title: ").append(pullRequestTitle).append("\n\n");
        sb.append("Description:\n").append(pullRequestDescription).append("\n\n");

        sb.append("## Review Comments\n");
        if (comments.isEmpty()) {
            sb.append("- None\n");
        } else {
            for (String comment : comments) {
                String cleaned = valueOrEmptyString(comment).trim();
                if (!cleaned.isEmpty()) {
                    sb.append("- ").append(cleaned).append("\n");
                }
            }
        }

        sb.append("\n## Unified Diffs\n");
        if (diffs.isEmpty()) {
            sb.append("- None\n");
        } else {
            int index = 1;
            for (DiffBlock diff : diffs) {
                sb.append("\n### ").append(index++).append(". ").append(diff.filePath()).append("\n");
                sb.append("```diff\n").append(diff.unifiedDiff()).append("\n```\n");
            }
        }

        return sb.toString();
    }
}
