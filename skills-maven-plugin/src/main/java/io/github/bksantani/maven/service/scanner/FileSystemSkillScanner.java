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
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FileSystemSkillScanner implements SkillScanner {

    @Override
    public boolean supports(URL url) {
        return "file".equalsIgnoreCase(url.getProtocol());
    }

    @Override
    public List<String> scan(URL url) throws SkillFetchException {
        try {
            Path sourcePath = Paths.get(url.toURI());
            Set<String> skillIds = new TreeSet<>();
            try (var stream = Files.list(sourcePath)) {
                stream.filter(Files::isDirectory)
                      .map(p -> p.getFileName().toString())
                      .forEach(skillIds::add);
            }
            return new ArrayList<>(skillIds);
        } catch (Exception e) {
            throw new SkillFetchException("Failed to scan local filesystem skills directory", e);
        }
    }
}
