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

import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { SkillRegistryService } from './services/skill-registry.service';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { describe, beforeEach, it, expect } from 'vitest';

const mockSkills = [
  {
    id: 'git-commit',
    name: 'Git Commit',
    description: 'Generates a commit message.',
    tags: ['git', 'vcs'],
    version: '1.2.3'
  }
];

const mockVersions = {
  registryVersion: '1.2.3',
  pluginVersion: '1.1.0'
};

const mockSkillRegistryService = {
  getSkills: () => of(mockSkills),
  getVersions: () => of(mockVersions)
};

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        { provide: SkillRegistryService, useValue: mockSkillRegistryService }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render title', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.brand-title')?.textContent).toContain('jskillflow');
  });

  it('should compute registry and plugin versions correctly', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const app = fixture.componentInstance;
    expect((app as any).registryVersion()).toBe('1.2.3');
    expect((app as any).pluginVersion()).toBe('1.1.0');
  });
});
