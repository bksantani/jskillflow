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

package io.github.bksantani.maven.service.extractor;

import io.github.bksantani.maven.exception.SkillFetchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class JarSkillExtractorTest {

    private final JarSkillExtractor extractor = new JarSkillExtractor();

    @Test
    void testSupports() throws Exception {
        assertTrue(extractor.supports(new URL("jar:file:/some/path.jar!/skills")));
        assertFalse(extractor.supports(new URL("file:/some/path")));
        assertFalse(extractor.supports(new URL("http://some/path")));
    }

    @Test
    void testExtract(@TempDir Path tempDir, @TempDir Path tempDest) throws Exception {
        File jarFile = tempDir.resolve("test-skills.jar").toFile();
        createJar(jarFile, "skills/git");

        URL jarUrl = new URL("jar:file:" + jarFile.getAbsolutePath() + "!/skills/git");
        extractor.extract(jarUrl, tempDest.toFile());

        Path destFile1 = tempDest.resolve("prompt.txt");
        Path destFile2 = tempDest.resolve("nested/config.json");

        assertTrue(Files.exists(destFile1));
        assertTrue(Files.exists(destFile2));
        assertEquals("content-prompt", Files.readString(destFile1));
        assertEquals("content-config", Files.readString(destFile2));
    }

    @Test
    void testExtractFailure() throws Exception {
        assertThrows(SkillFetchException.class, () -> {
            extractor.extract(new URL("jar:file:/non/existent/path.jar!/skills"), new File("dest"));
        });
    }

    private void createJar(File jarFile, String entryPath) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile))) {
            jos.putNextEntry(new JarEntry(entryPath + "/"));
            jos.closeEntry();
            
            jos.putNextEntry(new JarEntry(entryPath + "/prompt.txt"));
            jos.write("content-prompt".getBytes());
            jos.closeEntry();
            
            jos.putNextEntry(new JarEntry(entryPath + "/nested/"));
            jos.closeEntry();
            
            jos.putNextEntry(new JarEntry(entryPath + "/nested/config.json"));
            jos.write("content-config".getBytes());
            jos.closeEntry();
        }
    }
}
