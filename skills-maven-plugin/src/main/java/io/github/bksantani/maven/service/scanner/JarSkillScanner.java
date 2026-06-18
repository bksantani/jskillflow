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
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarSkillScanner implements SkillScanner {

    @Override
    public boolean supports(URL url) {
        return "jar".equalsIgnoreCase(url.getProtocol());
    }

    @Override
    public List<String> scan(URL url) throws SkillFetchException {
        try {
            URLConnection connection = url.openConnection();
            if (!(connection instanceof JarURLConnection)) {
                throw new SkillFetchException("Expected a JarURLConnection, got: " + connection.getClass().getName());
            }

            JarURLConnection jarConnection = (JarURLConnection) connection;
            JarFile jarFile = jarConnection.getJarFile();
            String entryName = jarConnection.getEntryName();

            Set<String> skillIds = new TreeSet<>();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(entryName + "/") && !name.equals(entryName + "/")) {
                    String relative = name.substring(entryName.length() + 1);
                    int slashIndex = relative.indexOf('/');
                    if (slashIndex > 0) {
                        skillIds.add(relative.substring(0, slashIndex));
                    }
                }
            }
            return new ArrayList<>(skillIds);
        } catch (IOException e) {
            throw new SkillFetchException("Failed to scan skills in JAR resource", e);
        }
    }
}
