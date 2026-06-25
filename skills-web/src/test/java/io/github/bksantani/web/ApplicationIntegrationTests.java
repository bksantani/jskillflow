package io.github.bksantani.web;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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


        mockMvc.perform(post("/api/skills/pull-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Provider-Name", "AZURE_CLOUD")
                        .header("X-Azure-DevOps-PAT", "test-pat")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(expectedContent()))
                .andExpect(jsonPath("$.metadata.filesAnalyzed").value(1))
                .andExpect(jsonPath("$.metadata.filesSkipped").value(0))
                .andExpect(jsonPath("$.metadata.commentsIncluded").value(1));
    }

    private String expectedContent() {
        return "## Pull Request Summary\n" +
                "Title: Refactor parser and improve validation\n" +
                "\n" +
                "Description:\n" +
                "This PR refactors parser logic and adds validation checks.\n" +
                "\n" +
                "## Review Comments\n" +
                "- Looks good overall, please add one more unit test.\n" +
                "\n" +
                "## Unified Diffs\n" +
                "\n" +
                "### 1. /src/main/java/com/example/Parser.java\n" +
                "```diff\n" +
                "--- a/src/main/java/com/example/Parser.java\n" +
                "+++ b/src/main/java/com/example/Parser.java\n" +
                "@@ -1,6 +1,6 @@\n" +
                " public class Parser {\n" +
                "     public String parse(String input) {\n" +
                "-        return input;\n" +
                "+        return input == null ? \"\" : input.trim();\n" +
                "     }\n" +
                " }\n" +
                " \n" +
                "```\n";
    }
}
