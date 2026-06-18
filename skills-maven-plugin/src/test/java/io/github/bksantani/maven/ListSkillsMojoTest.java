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

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class ListSkillsMojoTest {

    @Test
    void testExecute() throws Exception {
        ListSkillsMojo mojo = new ListSkillsMojo();
        Log mockLog = mock(Log.class);
        mojo.setLog(mockLog);

        mojo.execute();

        verify(mockLog).info("Scanning skills registry...");
        verify(mockLog).info("Available skills in registry:");
        verify(mockLog).info(contains("test-skill-1"));
        verify(mockLog).info(contains("test-skill-2"));
    }
}
