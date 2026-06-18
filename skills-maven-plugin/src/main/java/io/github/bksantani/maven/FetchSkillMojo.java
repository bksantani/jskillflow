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

package io.github.bksantani.maven;

import io.github.bksantani.maven.exception.SkillFetchException;
import io.github.bksantani.maven.service.SkillFetcherService;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.LifecyclePhase;

import java.io.File;
import java.util.List;

@Mojo(name = "fetch", defaultPhase = LifecyclePhase.INITIALIZE)
public class FetchSkillMojo extends AbstractMojo {

    @Parameter(property = "skills", required = true)
    private List<String> skills;

    @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}/skills")
    private File outputDirectory;

    private final SkillFetcherService fetcherService = new SkillFetcherService();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skills == null || skills.isEmpty()) {
            getLog().warn("No skills specified to fetch.");
            return;
        }

        // Process potential comma-separated inputs (from command line: -Dskills=skill1,skill2)
        List<String> skillsToFetch = new java.util.ArrayList<>();
        for (String rawSkill : skills) {
            if (rawSkill != null) {
                for (String s : rawSkill.split(",")) {
                    s = s.trim();
                    if (!s.isEmpty()) {
                        skillsToFetch.add(s);
                    }
                }
            }
        }

        getLog().info("Fetching skills: " + skillsToFetch + " -> " + outputDirectory.getAbsolutePath());
        
        for (String skillName : skillsToFetch) {
            try {
                File skillOutputDir = new File(outputDirectory, skillName);
                fetcherService.fetchSkill(skillName, skillOutputDir);
                getLog().info("Successfully fetched skill: " + skillName);
            } catch (SkillFetchException e) {
                getLog().error("Failed to fetch skill '" + skillName + "': " + e.getMessage(), e);
                throw new MojoExecutionException("Failed to fetch skill '" + skillName + "': " + e.getMessage(), e);
            }
        }
    }
}
