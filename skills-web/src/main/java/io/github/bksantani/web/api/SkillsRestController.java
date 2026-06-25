package io.github.bksantani.web.api;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.bksantani.web.request.CreateSkillRequest;
import io.github.bksantani.web.request.PullRequestProvider;
import io.github.bksantani.web.response.CreateSkillResponse;
import io.github.bksantani.web.core.SkillsService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillsRestController {

    private final SkillsService skillsService;

    @PostMapping("/pull-requests")
    public CreateSkillResponse createSkill(@Valid @RequestBody CreateSkillRequest request, @RequestHeader("X-Provider-Name") String provider) {
        return skillsService.createSkill(request, PullRequestProvider.valueOf(provider));
    }

    @PostMapping("/pull-requests/markdown")
    public String createSkillMarkdown(@Valid @RequestBody CreateSkillRequest request, @RequestHeader("X-Provider-Name") String provider) {
        var response = skillsService.createSkill(request, PullRequestProvider.valueOf(provider));
        return response.content();
    }

}
