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

import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CreateSkillResponse, SkillRegistryService } from '../../services/skill-registry.service';

@Component({
  selector: 'app-pr-generator',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pr-generator.html'
})
export class PrGenerator {
  private readonly registryService = inject(SkillRegistryService);

  // Form input signals
  protected readonly prUrl = signal<string>('');
  protected readonly pat = signal<string>('');

  // UI state signals
  protected readonly isLoading = signal<boolean>(false);
  protected readonly error = signal<string | null>(null);
  protected readonly result = signal<CreateSkillResponse | null>(null);
  protected readonly copied = signal<boolean>(false);

  protected onPrUrlInput(event: Event): void {
    this.prUrl.set((event.target as HTMLInputElement).value);
  }

  protected onPatInput(event: Event): void {
    this.pat.set((event.target as HTMLInputElement).value);
  }

  protected generatePrompt(): void {
    const url = this.prUrl().trim();
    const token = this.pat().trim();
    if (!url || !token) {
      return;
    }

    this.isLoading.set(true);
    this.error.set(null);
    this.result.set(null);

    this.registryService.generateSkillFromPR(url, token).subscribe({
      next: (response) => {
        this.result.set(response);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        const errMsg = err.error?.message || err.message || 'An unexpected error occurred.';
        this.error.set(errMsg);
      }
    });
  }

  protected copyPrompt(): void {
    const content = this.result()?.content;
    if (!content) return;

    navigator.clipboard.writeText(content).then(() => {
      this.copied.set(true);
      setTimeout(() => this.copied.set(false), 2000);
    });
  }
}
