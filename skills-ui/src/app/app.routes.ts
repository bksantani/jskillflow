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

import { Routes } from '@angular/router';
import { SkillsCatalog } from './components/skills-catalog/skills-catalog';
import { GettingStarted } from './components/getting-started/getting-started';
import { PrGenerator } from './components/pr-generator/pr-generator';

export const routes: Routes = [
  { path: 'catalog', component: SkillsCatalog },
  { path: 'getting-started', component: GettingStarted },
  { path: 'generate', component: PrGenerator },
  { path: '', redirectTo: 'catalog', pathMatch: 'full' }
];
