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
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SkillRegistryService, Skill } from './skill-registry.service';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';

describe('SkillRegistryService', () => {
  let service: SkillRegistryService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        SkillRegistryService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(SkillRegistryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch skills via GET request to registry.json', () => {
    const dummySkills: Skill[] = [
      {
        id: 'git-commit',
        name: 'Git Commit',
        description: 'Generates a commit message.',
        tags: ['git', 'vcs'],
        version: '1.2.3'
      }
    ];

    service.getSkills().subscribe(skills => {
      expect(skills.length).toBe(1);
      expect(skills).toEqual(dummySkills);
    });

    const req = httpMock.expectOne('registry.json');
    expect(req.request.method).toBe('GET');
    req.flush(dummySkills);
  });
});
