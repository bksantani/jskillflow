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

package io.github.bksantani.web.client.azure.model;

import static io.github.bksantani.web.utils.ListUtils.responseOrEmptyList;
import static io.github.bksantani.web.utils.StringUtils.valueOrEmptyString;
import static org.springframework.util.StringUtils.hasText;

import java.util.List;
import java.util.Objects;

public record AzurePullRequestThreadsResponse(
    List<Thread> value
) {
    public record Thread(List<Comment> comments) {
    }

    public record Comment(
        String content,
        String commentType
    ) {
    }

    public List<String> extractComments() {
        return responseOrEmptyList(value).stream()
                .filter(Objects::nonNull)
                .flatMap(thread -> responseOrEmptyList(thread.comments()).stream())
                .filter(Objects::nonNull)
                .filter(comment -> {
                    String content = valueOrEmptyString(comment.content()).trim();
                    String type = valueOrEmptyString(comment.commentType());
                    return !content.isEmpty() && (!hasText(type) || "text".equalsIgnoreCase(type));
                })
                .map(comment -> valueOrEmptyString(comment.content()).trim())
                .toList();
    }
}
