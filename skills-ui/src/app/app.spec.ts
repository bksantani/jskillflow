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
import { of } from 'rxjs';
import { vi, describe, beforeEach, afterEach, it, expect } from 'vitest';

const mockSkills = [
  {
    id: 'git-commit',
    name: 'Git Commit',
    description: 'Generates a commit message.',
    tags: ['git', 'vcs'],
    version: '1.2.3'
  },
  {
    id: 'another-skill',
    name: 'Another Skill',
    description: 'Some other skill.',
    tags: ['other'],
    version: '1.2.3'
  }
];

const mockSkillRegistryService = {
  getSkills: () => of(mockSkills)
};

describe('App', () => {
  let mockClipboard: any;

  beforeEach(async () => {
    // Setup clipboard mock
    mockClipboard = {
      writeText: vi.fn().mockImplementation(() => Promise.resolve())
    };
    vi.stubGlobal('navigator', {
      clipboard: mockClipboard
    });

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        { provide: SkillRegistryService, useValue: mockSkillRegistryService }
      ]
    }).compileComponents();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
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

  it('should compute latest version correctly', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const app = fixture.componentInstance;
    expect((app as any).latestVersion()).toBe('1.2.3');
  });

  it('should compile sorted unique tags', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const app = fixture.componentInstance;
    expect((app as any).allTags()).toEqual(['git', 'other', 'vcs']);
  });

  it('should filter skills by search query', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const app = fixture.componentInstance;

    // Simulate search query update
    const event = { target: { value: 'Git' } } as unknown as Event;
    (app as any).onSearchInput(event);
    fixture.detectChanges();

    const filtered = (app as any).filteredSkills();
    expect(filtered.length).toBe(1);
    expect(filtered[0].id).toBe('git-commit');
  });

  it('should filter skills by selected tags', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const app = fixture.componentInstance;

    // Toggle tag 'git'
    (app as any).toggleTag('git');
    fixture.detectChanges();

    expect((app as any).selectedTags().has('git')).toBe(true);
    let filtered = (app as any).filteredSkills();
    expect(filtered.length).toBe(1);
    expect(filtered[0].id).toBe('git-commit');

    // Toggle tag 'git' again to deselect
    (app as any).toggleTag('git');
    fixture.detectChanges();
    expect((app as any).selectedTags().has('git')).toBe(false);
    filtered = (app as any).filteredSkills();
    expect(filtered.length).toBe(2);
  });

  it('should clear all filters', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const app = fixture.componentInstance;

    (app as any).onSearchInput({ target: { value: 'Git' } } as unknown as Event);
    (app as any).toggleTag('git');
    fixture.detectChanges();

    expect((app as any).searchQuery()).toBe('Git');
    expect((app as any).selectedTags().size).toBe(1);

    (app as any).clearFilters();
    fixture.detectChanges();

    expect((app as any).searchQuery()).toBe('');
    expect((app as any).selectedTags().size).toBe(0);
  });

  it('should generate usage snippet with correct version', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const app = fixture.componentInstance;
    const snippet = (app as any).getGenericUsageSnippet();
    expect(snippet).toContain('<version>1.2.3</version>');
  });

  it('should copy text to clipboard and set feedback signal', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const app = fixture.componentInstance;

    (app as any).copyText('test-text', 'test-id');

    // Wait for the Promise returned by writeText to resolve
    await Promise.resolve();
    await fixture.whenStable();

    expect(mockClipboard.writeText).toHaveBeenCalledWith('test-text');
    expect((app as any).copiedSkillId()).toBe('test-id');
  });
});
