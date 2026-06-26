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
        sb.append("Use below PR code snippets, Generate a GitHub Copilot skill as a single markdown (.md) file. Output only the raw file content with no explanation so I can copy it directly into .github/copilot/skills/.\n\n");
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
