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
import { SkillsCatalog } from './skills-catalog';
import { SkillRegistryService } from '../../services/skill-registry.service';
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

describe('SkillsCatalog', () => {
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
      imports: [SkillsCatalog],
      providers: [
        { provide: SkillRegistryService, useValue: mockSkillRegistryService }
      ]
    }).compileComponents();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('should create the component', () => {
    const fixture = TestBed.createComponent(SkillsCatalog);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });

  it('should compile sorted unique tags', () => {
    const fixture = TestBed.createComponent(SkillsCatalog);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    expect((component as any).allTags()).toEqual(['git', 'other', 'vcs']);
  });

  it('should compile only top 10 most frequent tags, sorted alphabetically', () => {
    const fixture = TestBed.createComponent(SkillsCatalog);
    fixture.detectChanges(); // First let ngOnInit run and complete
    const component = fixture.componentInstance;

    // 11 distinct tags. t1 and t2 have freq 2, others freq 1.
    const mockDetailedSkills = [
      { id: '1', name: 'S1', description: 'D1', tags: ['t1', 't2', 't3', 't4', 't5', 't6', 't7', 't8', 't9', 't10', 't11'], version: '1' },
      { id: '2', name: 'S2', description: 'D2', tags: ['t1', 't2'], version: '1' }
    ];
    (component as any).skills.set(mockDetailedSkills);
    fixture.detectChanges();

    const tags = (component as any).allTags();
    expect(tags.length).toBe(10);
    // Frequency sorting:
    // Freq 2: t1, t2
    // Freq 1: t10, t11, t3, t4, t5, t6, t7, t8 (t9 has freq 1, but sorted last alphabetically of the tie-breakers, so sliced off)
    // Sliced top 10 sorted alphabetically: t1, t10, t11, t2, t3, t4, t5, t6, t7, t8
    expect(tags).toEqual(['t1', 't10', 't11', 't2', 't3', 't4', 't5', 't6', 't7', 't8']);
  });

  it('should filter skills by search query', () => {
    const fixture = TestBed.createComponent(SkillsCatalog);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    // Simulate search query update
    const event = { target: { value: 'Git' } } as unknown as Event;
    (component as any).onSearchInput(event);
    fixture.detectChanges();

    const filtered = (component as any).filteredSkills();
    expect(filtered.length).toBe(1);
    expect(filtered[0].id).toBe('git-commit');
  });

  it('should filter skills by selected tags', () => {
    const fixture = TestBed.createComponent(SkillsCatalog);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    // Toggle tag 'git'
    (component as any).toggleTag('git');
    fixture.detectChanges();

    expect((component as any).selectedTags().has('git')).toBe(true);
    let filtered = (component as any).filteredSkills();
    expect(filtered.length).toBe(1);
    expect(filtered[0].id).toBe('git-commit');

    // Toggle tag 'git' again to deselect
    (component as any).toggleTag('git');
    fixture.detectChanges();
    expect((component as any).selectedTags().has('git')).toBe(false);
    filtered = (component as any).filteredSkills();
    expect(filtered.length).toBe(2);
  });

  it('should clear all filters', () => {
    const fixture = TestBed.createComponent(SkillsCatalog);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    (component as any).onSearchInput({ target: { value: 'Git' } } as unknown as Event);
    (component as any).toggleTag('git');
    fixture.detectChanges();

    expect((component as any).searchQuery()).toBe('Git');
    expect((component as any).selectedTags().size).toBe(1);

    (component as any).clearFilters();
    fixture.detectChanges();

    expect((component as any).searchQuery()).toBe('');
    expect((component as any).selectedTags().size).toBe(0);
  });

  it('should copy text to clipboard and set feedback signal', async () => {
    const fixture = TestBed.createComponent(SkillsCatalog);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    (component as any).copyText('test-text', 'test-id');

    // Wait for the Promise returned by writeText to resolve
    await Promise.resolve();
    await fixture.whenStable();

    expect(mockClipboard.writeText).toHaveBeenCalledWith('test-text');
    expect((component as any).copiedSkillId()).toBe('test-id');
  });
});
