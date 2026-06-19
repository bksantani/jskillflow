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
import { GettingStarted } from './getting-started';
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

describe('GettingStarted', () => {
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
      imports: [GettingStarted],
      providers: [
        { provide: SkillRegistryService, useValue: mockSkillRegistryService }
      ]
    }).compileComponents();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('should create the component', () => {
    const fixture = TestBed.createComponent(GettingStarted);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });

  it('should generate usage snippet with correct version', () => {
    const fixture = TestBed.createComponent(GettingStarted);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    const snippet = (component as any).getGenericUsageSnippet();
    expect(snippet).toContain('<version>1.1.0</version>'); // plugin version
    expect(snippet).toContain('<version>1.2.3</version>'); // registry version
  });

  it('should copy text to clipboard and set feedback signal', async () => {
    const fixture = TestBed.createComponent(GettingStarted);
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
