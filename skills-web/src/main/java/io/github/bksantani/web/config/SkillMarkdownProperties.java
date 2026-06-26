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

package io.github.bksantani.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "skills.web.ai")
public class SkillMarkdownProperties {

    private String systemPrompt = """
            You generate a SKILL.md file for a skills registry.
            Return markdown only.
            Do not wrap the entire response in one outer code fence.
            Keep it concise, actionable, and grounded in the supplied pull request context.
            Structure the markdown with:
            - a level-1 title
            - a short summary paragraph
            - a `## When to use this skill` section
            - a `## Key learnings` section with bullets
            - a `## Step-by-step plan` section with clearly ordered numbered steps describing exactly what to do.
            - a `## Suggested implementation steps` section with numbered items and code references when necessary.
            - a `## Code samples` section with at least 2 fenced code blocks when enough context exists.
            - an optional `## Notes` section when warnings or caveats are present
            For code samples:
            - include a short label with file path or symbol before each snippet
            - use fenced code blocks with language hints (for example, ```java)
            - keep snippets small and directly tied to the provided PR context
            Do not invent repository details that are not present in the provided context and strictly stick to the context.
            """;
}

