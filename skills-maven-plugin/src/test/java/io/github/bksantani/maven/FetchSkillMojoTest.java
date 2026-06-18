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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FetchSkillMojoTest {

    @Test
    void testExecuteSuccess(@TempDir Path tempDir) throws Exception {
        FetchSkillMojo mojo = new FetchSkillMojo();
        Log mockLog = mock(Log.class);
        mojo.setLog(mockLog);

        File outputDirectory = tempDir.toFile();
        
        // Set fields via reflection
        setField(mojo, "skills", List.of("test-skill-1"));
        setField(mojo, "outputDirectory", outputDirectory);

        mojo.execute();

        // Verify log was called
        verify(mockLog).info(contains("Fetching skills"));
        verify(mockLog).info(contains("Successfully fetched skill: test-skill-1"));

        // Verify file extracted
        Path destFile = tempDir.resolve("test-skill-1/dummy.txt");
        assertTrue(Files.exists(destFile));
    }

    @Test
    void testExecuteNoSkills() throws Exception {
        FetchSkillMojo mojo = new FetchSkillMojo();
        Log mockLog = mock(Log.class);
        mojo.setLog(mockLog);

        setField(mojo, "skills", null);
        mojo.execute();

        verify(mockLog).warn("No skills specified to fetch.");
    }

    @Test
    void testExecuteFailure(@TempDir Path tempDir) throws Exception {
        FetchSkillMojo mojo = new FetchSkillMojo();
        Log mockLog = mock(Log.class);
        mojo.setLog(mockLog);

        setField(mojo, "skills", List.of("non-existent-skill"));
        setField(mojo, "outputDirectory", tempDir.toFile());

        assertThrows(MojoExecutionException.class, mojo::execute);
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
