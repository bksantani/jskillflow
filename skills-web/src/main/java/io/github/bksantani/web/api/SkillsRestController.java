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

package io.github.bksantani.web.api;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.bksantani.web.request.CreateSkillRequest;
import io.github.bksantani.web.request.PullRequestProvider;
import io.github.bksantani.web.api.response.CreateSkillResponse;
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

}
