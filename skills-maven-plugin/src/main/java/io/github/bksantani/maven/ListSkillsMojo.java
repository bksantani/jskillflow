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
import io.github.bksantani.maven.service.SkillListerService;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;

import java.util.List;

@Mojo(name = "list", defaultPhase = LifecyclePhase.INITIALIZE)
public class ListSkillsMojo extends AbstractMojo {

    private final SkillListerService listerService = new SkillListerService();

    // ANSI green code prefix & reset suffix
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Scanning skills registry...");
        
        try {
            List<String> skillIds = listerService.listSkillIds();
            
            if (skillIds.isEmpty()) {
                getLog().info("No skills found in the registry.");
                return;
            }
            
            getLog().info("Available skills in registry:");
            for (String skillId : skillIds) {
                // Output green skill ID
                getLog().info("  - " + ANSI_GREEN + skillId + ANSI_RESET);
            }
        } catch (SkillFetchException e) {
            getLog().error("Failed to list skills: " + e.getMessage(), e);
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
