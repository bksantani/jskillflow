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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SkillListerServiceTest {

    private final SkillListerService listerService = new SkillListerService();

    @Test
    void testListSkillIds() throws Exception {
        List<String> skillIds = listerService.listSkillIds();
        
        assertNotNull(skillIds);
        assertFalse(skillIds.isEmpty());
        // Verify that our test skills from target/test-classes/skills are found
        assertTrue(skillIds.contains("test-skill-1"));
        assertTrue(skillIds.contains("test-skill-2"));
    }
}
