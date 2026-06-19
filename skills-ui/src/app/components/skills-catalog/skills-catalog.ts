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
import { Skill, SkillRegistryService } from '../../services/skill-registry.service';

@Component({
  selector: 'app-skills-catalog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './skills-catalog.html'
})
export class SkillsCatalog implements OnInit {
  private readonly registryService = inject(SkillRegistryService);

  // State Signals
  protected readonly skills = signal<Skill[]>([]);
  protected readonly searchQuery = signal<string>('');
  protected readonly selectedTags = signal<Set<string>>(new Set<string>());
  
  // UI copy feedback signal
  protected readonly copiedSkillId = signal<string | null>(null);

  // Extract top 10 most frequent tags for filtering UI, sorted alphabetically
  protected readonly allTags = computed(() => {
    const tagCounts = new Map<string, number>();
    this.skills().forEach(s => {
      s.tags.forEach(t => {
        tagCounts.set(t, (tagCounts.get(t) || 0) + 1);
      });
    });

    // Sort by frequency descending, then alphabetically as tie-breaker
    const sortedByFreq = Array.from(tagCounts.entries())
      .sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
      .map(entry => entry[0]);

    // Slice to top 10 and sort alphabetically for a clean UI presentation
    return sortedByFreq.slice(0, 10).sort();
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

  protected copyText(text: string, feedbackId: string): void {
    navigator.clipboard.writeText(text).then(() => {
      this.copiedSkillId.set(feedbackId);
      setTimeout(() => this.copiedSkillId.set(null), 2000);
    });
  }
}
