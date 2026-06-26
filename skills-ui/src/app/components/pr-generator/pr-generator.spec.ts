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
import { PrGenerator } from './pr-generator';
import { SkillRegistryService } from '../../services/skill-registry.service';
import { of, throwError } from 'rxjs';
import { vi, describe, beforeEach, afterEach, it, expect } from 'vitest';

describe('PrGenerator', () => {
  let mockSkillRegistryService: any;
  let mockClipboard: any;

  beforeEach(async () => {
    mockSkillRegistryService = {
      generateSkillFromPR: vi.fn()
    };

    // Setup clipboard mock
    mockClipboard = {
      writeText: vi.fn().mockImplementation(() => Promise.resolve())
    };
    vi.stubGlobal('navigator', {
      clipboard: mockClipboard
    });

    await TestBed.configureTestingModule({
      imports: [PrGenerator],
      providers: [
        { provide: SkillRegistryService, useValue: mockSkillRegistryService }
      ]
    }).compileComponents();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('should create the component', () => {
    const fixture = TestBed.createComponent(PrGenerator);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });

  it('should initialize with default states', () => {
    const fixture = TestBed.createComponent(PrGenerator);
    const component = fixture.componentInstance;
    expect((component as any).prUrl()).toBe('');
    expect((component as any).pat()).toBe('');
    expect((component as any).isLoading()).toBe(false);
    expect((component as any).error()).toBeNull();
    expect((component as any).result()).toBeNull();
    expect((component as any).copied()).toBe(false);
  });

  it('should handle generate success', async () => {
    const fixture = TestBed.createComponent(PrGenerator);
    const component = fixture.componentInstance;
    const mockResponse = {
      content: 'Generated markdown prompt content',
      metadata: { filesAnalyzed: 3, filesSkipped: 1, commentsIncluded: 5 },
      warnings: ['A test warning']
    };
    mockSkillRegistryService.generateSkillFromPR.mockReturnValue(of(mockResponse));

    (component as any).prUrl.set('https://dev.azure.com/org/project/_git/repo/pullrequest/1');
    (component as any).pat.set('my-pat-token');

    (component as any).generatePrompt();

    expect((component as any).isLoading()).toBe(false);
    expect((component as any).error()).toBeNull();
    expect((component as any).result()).toEqual(mockResponse);
  });

  it('should handle generate failure with custom error payload', async () => {
    const fixture = TestBed.createComponent(PrGenerator);
    const component = fixture.componentInstance;
    const mockError = {
      error: { message: 'Azure DevOps request failed.' }
    };
    mockSkillRegistryService.generateSkillFromPR.mockReturnValue(throwError(() => mockError));

    (component as any).prUrl.set('https://dev.azure.com/org/project/_git/repo/pullrequest/1');
    (component as any).pat.set('my-pat-token');

    (component as any).generatePrompt();

    expect((component as any).isLoading()).toBe(false);
    expect((component as any).result()).toBeNull();
    expect((component as any).error()).toBe('Azure DevOps request failed.');
  });

  it('should copy text to clipboard and set copied signal', async () => {
    const fixture = TestBed.createComponent(PrGenerator);
    const component = fixture.componentInstance;
    const mockResponse = {
      content: 'Prompt content to copy',
      metadata: { filesAnalyzed: 1, filesSkipped: 0, commentsIncluded: 0 }
    };
    (component as any).result.set(mockResponse);

    (component as any).copyPrompt();

    // Wait for clipboard promise
    await Promise.resolve();
    await fixture.whenStable();

    expect(mockClipboard.writeText).toHaveBeenCalledWith('Prompt content to copy');
    expect((component as any).copied()).toBe(true);
  });
});
