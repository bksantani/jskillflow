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

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Skill {
  id: string;
  name: string;
  description: string;
  tags: string[];
  version: string;
}

export interface Versions {
  registryVersion: string;
  pluginVersion: string;
}

export interface PullRequestMetadata {
  filesAnalyzed: number;
  filesSkipped: number;
  commentsIncluded: number;
}

export interface CreateSkillResponse {
  content: string;
  metadata: PullRequestMetadata;
  warnings?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class SkillRegistryService {
  constructor(private readonly http: HttpClient) {}

  getSkills(): Observable<Skill[]> {
    return this.http.get<Skill[]>('registry.json');
  }

  getVersions(): Observable<Versions> {
    return this.http.get<Versions>('versions.json');
  }

  generateSkillFromPR(prUrl: string, pat: string): Observable<CreateSkillResponse> {
    return this.http.post<CreateSkillResponse>(
      '/api/skills/pull-requests',
      { prUrl },
      {
        headers: {
          'X-Provider-Name': 'AZURE_CLOUD',
          'X-Azure-DevOps-PAT': pat
        }
      }
    );
  }
}
