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
import { Skill, SkillRegistryService } from './services/skill-registry.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  private readonly registryService = inject(SkillRegistryService);

  // State Signals
  protected readonly skills = signal<Skill[]>([]);
  protected readonly searchQuery = signal<string>('');
  protected readonly selectedTags = signal<Set<string>>(new Set<string>());
  
  // UI copy feedback signal
  protected readonly copiedSkillId = signal<string | null>(null);

  // Derive the latest version from the skills list
  protected readonly latestVersion = computed(() => {
    const list = this.skills();
    return list.length > 0 ? list[0].version : '1.0.0-SNAPSHOT';
  });

  // Extract unique tags for filtering UI
  protected readonly allTags = computed(() => {
    const tags = new Set<string>();
    this.skills().forEach(s => s.tags.forEach(t => tags.add(t)));
    return Array.from(tags).sort();
  });

  // Filter skills by search query and tags
  protected readonly filteredSkills = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const tags = this.selectedTags();
    
    return this.skills().filter(skill => {
      const matchesTags = tags.size === 0 || Array.from(tags).every(t => skill.tags.includes(t));
      const matchesQuery = !query || 
        skill.name.toLowerCase().includes(query) || 
        skill.description.toLowerCase().includes(query) || 
        skill.tags.some(t => t.toLowerCase().includes(query));
      
      return matchesTags && matchesQuery;
    });
  });

  ngOnInit(): void {
    this.registryService.getSkills().subscribe({
      next: (data) => this.skills.set(data),
      error: (err) => console.error('Error fetching registry:', err)
    });
  }

  protected onSearchInput(event: Event): void {
    this.searchQuery.set((event.target as HTMLInputElement).value);
  }

  protected toggleTag(tag: string): void {
    const nextTags = new Set(this.selectedTags());
    if (nextTags.has(tag)) {
      nextTags.delete(tag);
    } else {
      nextTags.add(tag);
    }
    this.selectedTags.set(nextTags);
  }

  protected clearFilters(): void {
    this.searchQuery.set('');
    this.selectedTags.set(new Set());
  }

  protected getGenericUsageSnippet(): string {
    return `<build>
    <plugins>
        <plugin>
            <groupId>io.github.bksantani</groupId>
            <artifactId>skills-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version> <!-- Static plugin binary version -->
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
                    <version>${this.latestVersion()}</version> <!-- Bushed when skills change -->
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
