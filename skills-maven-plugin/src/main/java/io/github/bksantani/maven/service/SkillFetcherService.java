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
import io.github.bksantani.maven.service.extractor.FileSystemSkillExtractor;
import io.github.bksantani.maven.service.extractor.JarSkillExtractor;
import io.github.bksantani.maven.service.extractor.SkillExtractor;

import java.io.File;
import java.net.URL;
import java.util.List;

public class SkillFetcherService {

    private final List<SkillExtractor> extractors = List.of(
        new FileSystemSkillExtractor(),
        new JarSkillExtractor()
    );

    public void fetchSkill(String skillName, File outputDirectory) throws SkillFetchException {
        validateSkillName(skillName);
        URL skillUrl = locateSkillResource(skillName);
        ensureOutputDirectoryExists(outputDirectory);

        SkillExtractor extractor = extractors.stream()
                .filter(ext -> ext.supports(skillUrl))
                .findFirst()
                .orElseThrow(() -> new SkillFetchException("No supported extractor found for resource URL protocol: " + skillUrl.getProtocol()));

        extractor.extract(skillUrl, outputDirectory);
    }

    private void validateSkillName(String skillName) throws SkillFetchException {
        if (skillName == null || skillName.trim().isEmpty()) {
            throw new SkillFetchException("Skill name cannot be null or empty.");
        }
        if (skillName.contains("..") || skillName.contains("/") || skillName.contains("\\")) {
            throw new SkillFetchException("Invalid skill name containing forbidden path characters: " + skillName);
        }
    }

    private URL locateSkillResource(String skillName) throws SkillFetchException {
        String resourcePath = "skills/" + skillName;
        URL url = getClass().getClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new SkillFetchException("Skill '" + skillName + "' not found in the registry classpath.");
        }
        return url;
    }

    private void ensureOutputDirectoryExists(File directory) throws SkillFetchException {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new SkillFetchException("Failed to create output directory: " + directory.getAbsolutePath());
        }
    }
}
