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

package io.github.bksantani.maven.service.scanner;

import io.github.bksantani.maven.exception.SkillFetchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class JarSkillScannerTest {

    private final JarSkillScanner scanner = new JarSkillScanner();

    @Test
    void testSupports() throws Exception {
        assertTrue(scanner.supports(new URL("jar:file:/some/path.jar!/skills")));
        assertFalse(scanner.supports(new URL("file:/some/path")));
        assertFalse(scanner.supports(new URL("http://some/path")));
    }

    @Test
    void testScan(@TempDir Path tempDir) throws Exception {
        File jarFile = tempDir.resolve("test-skills.jar").toFile();
        createJar(jarFile, "skills");

        URL jarUrl = new URL("jar:file:" + jarFile.getAbsolutePath() + "!/skills");
        List<String> skills = scanner.scan(jarUrl);

        assertEquals(2, skills.size());
        assertTrue(skills.contains("skill-a"));
        assertTrue(skills.contains("skill-b"));
    }

    @Test
    void testScanFailure() throws Exception {
        assertThrows(SkillFetchException.class, () -> {
            scanner.scan(new URL("jar:file:/non/existent/path.jar!/skills"));
        });
    }

    private void createJar(File jarFile, String entryDirName) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile))) {
            jos.putNextEntry(new JarEntry(entryDirName + "/"));
            jos.closeEntry();
            
            jos.putNextEntry(new JarEntry(entryDirName + "/skill-a/"));
            jos.closeEntry();
            
            jos.putNextEntry(new JarEntry(entryDirName + "/skill-a/dummy.txt"));
            jos.write("dummy-a".getBytes());
            jos.closeEntry();
            
            jos.putNextEntry(new JarEntry(entryDirName + "/skill-b/"));
            jos.closeEntry();
            
            jos.putNextEntry(new JarEntry(entryDirName + "/skill-b/dummy.txt"));
            jos.write("dummy-b".getBytes());
            jos.closeEntry();
        }
    }
}
