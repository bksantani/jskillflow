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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileSystemSkillExtractorTest {

    private final FileSystemSkillExtractor extractor = new FileSystemSkillExtractor();

    @Test
    void testSupports() throws Exception {
        assertTrue(extractor.supports(new URL("file:/some/path")));
        assertFalse(extractor.supports(new URL("http://some/path")));
        assertFalse(extractor.supports(new URL("jar:file:/some/jar!/path")));
    }

    @Test
    void testExtract(@TempDir Path tempSrc, @TempDir Path tempDest) throws Exception {
        Path subDir = tempSrc.resolve("sub");
        Files.createDirectory(subDir);
        
        Path file1 = tempSrc.resolve("file1.txt");
        Path file2 = subDir.resolve("file2.txt");
        
        Files.writeString(file1, "content1");
        Files.writeString(file2, "content2");

        extractor.extract(tempSrc.toUri().toURL(), tempDest.toFile());

        Path destFile1 = tempDest.resolve("file1.txt");
        Path destFile2 = tempDest.resolve("sub/file2.txt");

        assertTrue(Files.exists(destFile1));
        assertTrue(Files.exists(destFile2));
        assertEquals("content1", Files.readString(destFile1));
        assertEquals("content2", Files.readString(destFile2));
    }

    @Test
    void testExtractFailure() throws Exception {
        assertThrows(SkillFetchException.class, () -> {
            extractor.extract(new URL("file:/non/existent/path"), new File("dest"));
        });
    }
}
