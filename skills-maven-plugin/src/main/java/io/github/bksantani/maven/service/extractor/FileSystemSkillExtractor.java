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
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.*;

public class FileSystemSkillExtractor implements SkillExtractor {

    @Override
    public boolean supports(URL url) {
        return "file".equalsIgnoreCase(url.getProtocol());
    }

    @Override
    public void extract(URL url, File outputDirectory) throws SkillFetchException {
        try {
            Path sourcePath = Paths.get(url.toURI());
            try (var stream = Files.walk(sourcePath)) {
                stream.forEach(source -> {
                    try {
                        Path target = outputDirectory.toPath().resolve(sourcePath.relativize(source).toString());
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(target);
                        } else {
                            Files.createDirectories(target.getParent());
                            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        } catch (Exception e) {
            throw new SkillFetchException("Failed to extract skill from local filesystem resource", e);
        }
    }
}
