<!-- Copyright 2026 Bharat Santani -->
<!-- SPDX-License-Identifier: Apache-2.0 -->

# Security Policy

## Supported scope

JSkillflow includes:

- a Spring Boot backend (`skills-web`)
- an Angular frontend (`skills-ui`)
- a Maven plugin (`skills-maven-plugin`)
- packaged skill resources (`skills-registry`)

Security-sensitive areas include:

- provider authentication headers
- Azure DevOps Personal Access Tokens (PATs)
- pull-request content fetching
- prompt generation from repository content
- any handling of generated markdown that may include sensitive source snippets

## Reporting a vulnerability

Please do **not** report security vulnerabilities through public issues or public pull requests.

Instead, report vulnerabilities privately to the project maintainer through a private disclosure channel you control for this repository.

When reporting, please include:

- affected module(s)
- vulnerability type
- impact assessment
- reproduction steps or proof of concept
- any required configuration or headers
- suggested mitigation, if known

If you are unsure whether something is security-sensitive, treat it as sensitive and report it privately first.

## What to avoid sharing publicly

Never include any of the following in issues, pull requests, screenshots, logs, or tests:

- Azure DevOps PATs
- authorization headers
- private pull request URLs if they reveal confidential project information
- proprietary source code copied from private repositories
- internal hostnames, tokens, or credentials

## Current security expectations

### Credentials

JSkillflow can accept provider credentials such as Azure DevOps PATs for pull-request-based prompt generation.

Contributors should ensure that:

- credentials are passed only through intended request headers or secure runtime configuration
- secrets are never hardcoded
- secrets are never committed to source control
- logs do not expose token values

### Pull request content handling

The pull-request-to-skill flow may fetch repository file contents and convert them into prompt input.

Contributors should pay special attention to:

- filtering unsupported or binary content
- limiting oversized files
- limiting total processed file counts
- avoiding accidental leakage of private repository data in logs or fixtures

### Test fixtures

All committed test fixtures should be synthetic or safe-to-publish.

Do not commit:

- real enterprise pull request content
- copied proprietary code
- real secrets or real internal URLs

## Secure contribution guidelines

If your change affects any of the following, please treat it as security-relevant:

- request interceptors
- credential providers
- controller endpoints that accept credentials
- upstream HTTP clients
- prompt-generation pipelines
- file content fetching, filtering, or transformation

For such changes, please:

- add or update tests
- verify error handling
- verify secrets are not exposed in messages or logs
- document any new security assumptions

## Dependency and supply chain awareness

This repository uses Java/Maven and Node/npm dependencies.

Before releasing publicly, it is a good idea to:

- keep dependencies updated
- review transitive dependencies regularly
- enable dependency scanning in CI
- pin and verify toolchain versions where practical

## Hardening recommendations

As the project evolves, recommended follow-up practices include:

- CI-based dependency scanning
- secret scanning
- signed release artifacts
- clearer supported-version policy
- private vulnerability disclosure contact published in repository settings or project website

## Response goals

While no formal SLA is declared here yet, security reports should be triaged as quickly as possible and handled privately until a fix or mitigation is available.

## A note for open source users

If you deploy JSkillflow in production or against private source repositories, you are responsible for:

- securing your runtime environment
- protecting credentials and network access
- reviewing generated content before redistributing it
- validating compatibility with your internal compliance requirements

