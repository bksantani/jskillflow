<!-- Copyright 2026 Bharat Santani -->
<!-- SPDX-License-Identifier: Apache-2.0 -->

# Contributing to JSkillflow

Thanks for your interest in contributing to JSkillflow.

This repository is a multi-module monorepo with:

- `skills-registry` – versioned skill content packaged as resources
- `skills-maven-plugin` – Maven plugin for listing and fetching skills
- `skills-web` – Spring Boot backend and API
- `skills-ui` – Angular frontend bundled into `skills-web`

## Before you start

Please:

- read the root `README.md`
- use `SUPPORT.md` for help and issue routing expectations
- search existing issues / pull requests before starting work
- keep changes focused and scoped to a single improvement whenever possible

## Development setup

### Prerequisites

From the current repository docs, the main toolchain is:

- Java 25
- Maven 3.9+
- Node.js 24 / npm for `skills-ui`

## Repository workflow

### 1. Fork and branch

Create a topic branch from the default branch:

```bash
git checkout -b feat/short-description
```

Examples:

- `feat/pr-prompt-filtering`
- `fix/plugin-scan-warning`
- `docs/readme-clarifications`

### 2. Make focused changes

Please avoid mixing unrelated changes in the same pull request.

Good pull requests usually contain one of:

- one feature
- one bug fix
- one refactor
- one documentation improvement

### 3. Run tests locally

Run the full repository test suite before opening a pull request:

```bash
mvn test
```

If you are working only on the UI, you can also run:

```bash
cd skills-ui
npm test
```

If you are working on the Spring Boot app locally:

```bash
cd skills-web
mvn spring-boot:run
```

## Working on each module

### `skills-registry`

Use this module for:

- adding new skills
- updating skill metadata
- maintaining `skills/**` resource content

When adding a skill, keep the expected structure described in `README.md`.

### `skills-maven-plugin`

Use this module for:

- plugin goals and lifecycle behavior
- scanning / extraction logic
- plugin-side validation and fetch behavior

Please add or update unit tests with any plugin behavior change.

### `skills-web`

Use this module for:

- backend APIs
- provider integrations
- pull-request-to-skill generation
- prompt assembly and diff processing

If you change request handling, response payloads, or filtering behavior, please update tests and relevant docs.

### `skills-ui`

Use this module for:

- skill catalog UX
- getting-started flows
- prompt generation UI
- frontend display of warnings, versions, and metadata

Remember that this app is bundled into `skills-web` during the Maven build.

## Coding expectations

### Java

- prefer small, focused classes and methods
- keep public APIs and response contracts explicit
- add or update unit/integration tests for behavior changes
- preserve existing code style unless there is a strong reason to change it

### Angular / TypeScript

- keep components focused and readable
- avoid unnecessary framework churn
- update specs when UI behavior changes
- keep API contract assumptions in sync with `skills-web`

### Documentation

Documentation changes are welcome and valuable.

Please update docs when you change:

- public APIs
- module responsibilities
- local setup steps
- prompt-generation behavior or limits
- skill structure requirements

## Pull request guidelines

Please include:

- a clear summary of the change
- the motivation / problem being solved
- any screenshots for UI changes
- notes about testing performed
- any follow-up work that remains

A good PR description often includes:

```text
## Summary
- ...

## Why
- ...

## Testing
- mvn test
- any targeted tests

## Notes
- anything reviewers should pay special attention to
```

## Commit guidance

Readable commit history helps a lot.

Examples:

- `Add PR prompt filtering for binary assets`
- `Document 50-file limit in README`
- `Fix warning handling in Azure change processor`

## Security-sensitive changes

If your change touches:

- credentials
- authentication headers
- Azure DevOps integration
- prompt generation from private repositories
- file filtering / content fetching

please read `SECURITY.md` before opening the PR.

Do not include secrets, PATs, or private repository data in commits, tests, screenshots, or issue descriptions.

## Reporting bugs

When reporting a bug, please include:

- module affected
- expected behavior
- actual behavior
- minimal reproduction steps
- environment details (Java, Maven, Node versions)
- logs or screenshots when helpful

## Requesting features

Feature requests are welcome. The most helpful requests explain:

- the use case
- why the current behavior is insufficient
- what outcome you want
- any constraints around enterprise / Maven / PR workflows

## Review philosophy

The goal is to keep JSkillflow:

- easy to understand
- easy to adopt in JVM environments
- safe when working with pull-request content and credentials
- practical for real developer workflows

Thanks again for contributing.

