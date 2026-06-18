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

package io.github.bksantani.maven.service;

import io.github.bksantani.maven.exception.SkillFetchException;
import io.github.bksantani.maven.service.scanner.FileSystemSkillScanner;
import io.github.bksantani.maven.service.scanner.JarSkillScanner;
import io.github.bksantani.maven.service.scanner.SkillScanner;

import java.net.URL;
import java.util.List;

public class SkillListerService {

    private final List<SkillScanner> scanners = List.of(
        new FileSystemSkillScanner(),
        new JarSkillScanner()
    );

    public List<String> listSkillIds() throws SkillFetchException {
        URL skillsUrl = getClass().getClassLoader().getResource("skills");
        if (skillsUrl == null) {
            throw new SkillFetchException("Skills directory not found on classpath.");
        }

        SkillScanner scanner = scanners.stream()
                .filter(s -> s.supports(skillsUrl))
                .findFirst()
                .orElseThrow(() -> new SkillFetchException("No supported scanner found for resource URL protocol: " + skillsUrl.getProtocol()));

        return scanner.scan(skillsUrl);
    }
}
