<!-- Copyright 2026 Bharat Santani -->
<!-- SPDX-License-Identifier: Apache-2.0 -->

# `skills-ui`

`skills-ui` is the Angular frontend for JSkillflow. It is the developer-facing portal that lets users:

- browse the published skill catalog
- read getting-started guidance for consuming skills
- generate a markdown skill prompt from an Azure DevOps pull request

The application is built as a standalone Angular app and is bundled into the `skills-web` Spring Boot module during the Maven build.

## What the UI does

The app currently exposes three main views:

- `/catalog` – browse skills from `public/registry.json`
- `/getting-started` – explain how to consume skills and use the Maven plugin
- `/generate` – submit an Azure DevOps pull request URL plus PAT and generate a prompt-ready markdown skill

During local development and production builds, `scripts/aggregate.js` regenerates:

- `public/registry.json` – aggregated skill metadata from `skills-registry`
- `public/versions.json` – registry and plugin versions shown in the header

## Pull request prompt generation

The Generate Skill screen calls the backend endpoint `POST /api/skills/pull-requests` and displays:

- generated markdown content
- analyzed/skipped file counts
- included review comment count
- warnings for skipped files or upstream fetch issues

Important backend guardrails that the UI should expect:

- only up to **50 changed files** are processed per request
- remaining files are skipped and reported as warnings
- binary or unsuitable assets such as `.png`, `.svg`, archives, fonts, and compiled artifacts are skipped before they are added to the prompt

## Local development

Install dependencies:

```bash
npm ci
```

Start the Angular dev server:

```bash
npm start
```

Then open `http://localhost:4200/`.

> `npm start` runs the aggregation script first so the catalog and version metadata stay in sync with the monorepo.

## Build

Create a production build:

```bash
npm run build
```

The build output is written to `dist/skills-ui/` and later copied into `skills-web` during the Maven build.

## Tests

Run the UI unit tests:

```bash
npm test
```

## Relationship to the Maven build

From the monorepo root, running Maven will also build and test this Angular app through the `skills-web` module:

```bash
mvn test
```

That flow installs npm dependencies, regenerates aggregated UI data, builds the frontend, and executes the Angular test suite.
