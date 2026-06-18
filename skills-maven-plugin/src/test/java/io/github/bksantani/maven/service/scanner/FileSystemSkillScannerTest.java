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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileSystemSkillScannerTest {

    private final FileSystemSkillScanner scanner = new FileSystemSkillScanner();

    @Test
    void testSupports() throws Exception {
        assertTrue(scanner.supports(new URL("file:/some/path")));
        assertFalse(scanner.supports(new URL("http://some/path")));
        assertFalse(scanner.supports(new URL("jar:file:/some/jar!/path")));
    }

    @Test
    void testScan(@TempDir Path tempDir) throws Exception {
        Path skillA = tempDir.resolve("skill-a");
        Path skillB = tempDir.resolve("skill-b");
        Path regularFile = tempDir.resolve("some-file.txt");

        Files.createDirectory(skillA);
        Files.createDirectory(skillB);
        Files.createFile(regularFile);

        List<String> skills = scanner.scan(tempDir.toUri().toURL());

        assertEquals(2, skills.size());
        assertTrue(skills.contains("skill-a"));
        assertTrue(skills.contains("skill-b"));
        assertFalse(skills.contains("some-file.txt"));
    }

    @Test
    void testScanFailure() throws Exception {
        assertThrows(SkillFetchException.class, () -> {
            scanner.scan(new URL("file:/non/existent/path/at/all"));
        });
    }
}
