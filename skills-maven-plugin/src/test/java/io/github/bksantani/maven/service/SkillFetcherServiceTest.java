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
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class SkillFetcherServiceTest {

    private final SkillFetcherService fetcherService = new SkillFetcherService();

    @Test
    void testFetchSkillSuccess(@TempDir Path tempDest) throws Exception {
        File outputDir = tempDest.toFile();
        
        fetcherService.fetchSkill("test-skill-1", outputDir);

        Path destDummy = tempDest.resolve("dummy.txt");
        assertTrue(Files.exists(destDummy));
        assertEquals("This is a dummy test skill resource for test-skill-1.", Files.readString(destDummy).trim());
    }

    @Test
    void testFetchSkillValidation() {
        assertThrows(SkillFetchException.class, () -> {
            fetcherService.fetchSkill("", new File("dest"));
        });

        assertThrows(SkillFetchException.class, () -> {
            fetcherService.fetchSkill(null, new File("dest"));
        });

        assertThrows(SkillFetchException.class, () -> {
            fetcherService.fetchSkill("../outside", new File("dest"));
        });

        assertThrows(SkillFetchException.class, () -> {
            fetcherService.fetchSkill("nested/skill", new File("dest"));
        });
    }

    @Test
    void testFetchSkillNotFound() {
        assertThrows(SkillFetchException.class, () -> {
            fetcherService.fetchSkill("non-existent-skill-id-xyz", new File("dest"));
        });
    }
}
