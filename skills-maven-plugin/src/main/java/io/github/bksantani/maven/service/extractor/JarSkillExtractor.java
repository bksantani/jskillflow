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
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarSkillExtractor implements SkillExtractor {

    @Override
    public boolean supports(URL url) {
        return "jar".equalsIgnoreCase(url.getProtocol());
    }

    @Override
    public void extract(URL url, File outputDirectory) throws SkillFetchException {
        try {
            URLConnection connection = url.openConnection();
            if (!(connection instanceof JarURLConnection)) {
                throw new SkillFetchException("Expected a JarURLConnection, got: " + connection.getClass().getName());
            }

            JarURLConnection jarConnection = (JarURLConnection) connection;
            JarFile jarFile = jarConnection.getJarFile();
            String entryName = jarConnection.getEntryName();

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(entryName + "/")) {
                    String relativePath = name.substring(entryName.length() + 1);
                    File targetFile = new File(outputDirectory, relativePath);

                    if (entry.isDirectory()) {
                        if (!targetFile.exists() && !targetFile.mkdirs()) {
                            throw new SkillFetchException("Failed to create directory: " + targetFile.getAbsolutePath());
                        }
                    } else {
                        File parent = targetFile.getParentFile();
                        if (!parent.exists() && !parent.mkdirs()) {
                            throw new SkillFetchException("Failed to create directory: " + parent.getAbsolutePath());
                        }
                        try (InputStream is = jarFile.getInputStream(entry);
                             OutputStream os = new FileOutputStream(targetFile)) {
                            copyStream(is, os);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new SkillFetchException("Failed to extract skill from JAR file resource", e);
        }
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }
}
