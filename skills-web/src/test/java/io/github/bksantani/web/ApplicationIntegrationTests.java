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

package io.github.bksantani.web;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import io.github.bksantani.web.request.CreateSkillRequest;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@SpringBootTest
@EnableWireMock(
        @ConfigureWireMock(port = 9999)
)
class ApplicationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void when_validCreateSkillRequest_then_returnsCreateSkillResponse() throws Exception {
        var request = new CreateSkillRequest("https://dev.azure.com/my-org/my-project/_git/repo/pullrequest/12");


        var response = mockMvc.perform(post("/api/skills/pull-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Provider-Name", "AZURE_CLOUD")
                        .header("X-Azure-DevOps-PAT", "test-pat")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        var payload = objectMapper.readTree(response.getResponse().getContentAsString());
        var content = objectMapper.convertValue(payload.get("content"), String.class);

        assertThat(payload.at("/metadata/filesAnalyzed").asInt()).isEqualTo(3);
        assertThat(payload.at("/metadata/filesSkipped").asInt()).isEqualTo(1);
        assertThat(payload.at("/metadata/commentsIncluded").asInt()).isEqualTo(1);
        assertThat(payload.withArray("warnings")).hasSize(1);
        assertThat(payload.at("/warnings/0").textValue())
                .isEqualTo("Skipped file due to unsupported file type for prompt generation: /docs/architecture.svg");

        int updatedIndex = content.indexOf("### 1. /src/main/java/com/example/Parser.java");
        int deletedIndex = content.indexOf("### 2. /src/main/java/com/example/LegacyParser.java");
        int addedIndex = content.indexOf("### 3. /src/main/java/com/example/TrimUtils.java");

        assertThat(updatedIndex).isGreaterThanOrEqualTo(0);
        assertThat(deletedIndex).isGreaterThan(updatedIndex);
        assertThat(addedIndex).isGreaterThan(deletedIndex);

        assertThat(content)
                .contains("## Pull Request Summary")
                .contains("Title: Refactor parser and improve validation")
                .contains("This PR refactors parser logic and adds validation checks.")
                .contains("- Looks good overall, please add one more unit test.")
                .contains("### 1. /src/main/java/com/example/Parser.java")
                .contains("### 2. /src/main/java/com/example/LegacyParser.java")
                .contains("### 3. /src/main/java/com/example/TrimUtils.java")
                .contains("--- a/src/main/java/com/example/Parser.java")
                .contains("+++ b/src/main/java/com/example/Parser.java")
                .contains("-        return input;")
                .contains("+        return input == null ? \"\" : input.trim();")
                .contains("--- a/src/main/java/com/example/LegacyParser.java")
                .contains("+++ b/src/main/java/com/example/LegacyParser.java")
                .contains("-public class LegacyParser {")
                .contains("-        return input == null ? \"\" : input;")
                .contains("--- a/src/main/java/com/example/TrimUtils.java")
                .contains("+++ b/src/main/java/com/example/TrimUtils.java")
                .contains("+public final class TrimUtils {")
                .contains("+        return input == null ? \"\" : input.trim();")
                .doesNotContain("/docs/architecture.svg");
    }
}
