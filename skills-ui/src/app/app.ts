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

import { Component, OnInit, signal, computed, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SkillRegistryService } from './services/skill-registry.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
  encapsulation: ViewEncapsulation.None
})
export class App implements OnInit {
  private readonly registryService = inject(SkillRegistryService);

  // Dynamic versions parsed from project POM files
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
}
