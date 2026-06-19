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

import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SkillRegistryService } from '../../services/skill-registry.service';

@Component({
  selector: 'app-getting-started',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './getting-started.html'
})
export class GettingStarted implements OnInit {
  private readonly registryService = inject(SkillRegistryService);

  // UI copy feedback signals
  protected readonly copiedSkillId = signal<string | null>(null);
  protected readonly registryVersion = signal<string>('1.0.0-SNAPSHOT');
  protected readonly pluginVersion = signal<string>('1.0.0-SNAPSHOT');

  ngOnInit(): void {
    this.registryService.getVersions().subscribe({
      next: (data) => {
        this.registryVersion.set(data.registryVersion);
        this.pluginVersion.set(data.pluginVersion);
      },
      error: (err) => console.error('Error fetching versions:', err)
    });
  }

  protected getGenericUsageSnippet(): string {
    return `<build>
    <plugins>
        <plugin>
            <groupId>io.github.bksantani</groupId>
            <artifactId>skills-maven-plugin</artifactId>
            <version>${this.pluginVersion()}</version> <!-- Static plugin binary version -->
            <executions>
                <execution>
                    <goals>
                        <goal>fetch</goal>
                    </goals>
                    <configuration>
                        <!-- Optional: Defaults to \${project.build.directory}/skills -->
                        <outputDirectory>\${project.build.directory}/skills</outputDirectory>
                        <skills>
                            <skill>[skill-name-1]</skill>
                            <skill>[skill-name-2]</skill>
                        </skills>
                    </configuration>
                </execution>
            </executions>
            <dependencies>
                <!-- Decoupled skill repository containing your actual prompt files -->
                <dependency>
                    <groupId>io.github.bksantani</groupId>
                    <artifactId>skills-registry</artifactId>
                    <version>${this.registryVersion()}</version> <!-- Bushed when skills change -->
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>`;
  }

  protected copyText(text: string, feedbackId: string): void {
    navigator.clipboard.writeText(text).then(() => {
      this.copiedSkillId.set(feedbackId);
      setTimeout(() => this.copiedSkillId.set(null), 2000);
    });
  }
}
