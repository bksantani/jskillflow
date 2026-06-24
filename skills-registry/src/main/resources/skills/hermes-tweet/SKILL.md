# Hermes Tweet X/Twitter Workflow

Use this skill when an AI agent needs a public X/Twitter research, monitoring,
or approved posting workflow and the project can use the Hermes Tweet Hermes
Agent plugin.

Hermes Tweet source: https://github.com/Xquik-dev/hermes-tweet

## When To Use

Use Hermes Tweet when the task needs one of these outcomes:

- collect public X/Twitter context for market, support, product, or launch work
- monitor public accounts, keywords, or posts before a report or decision
- draft posts from approved source material for human review
- route Hermes Agent social-media work through a maintained plugin instead of an
  ad hoc browser or cookie workflow

Do not use this skill for private account scraping, credential collection,
platform bypass, spam, harassment, or mass unsolicited posting.

## Prerequisites

- Hermes Agent is available in the target environment.
- Hermes Tweet is installed from its source repository.
- Required API credentials are configured in the user's own runtime environment.
- Posting or other mutating actions stay disabled unless the user explicitly
  enables them and approves the final action.

## Workflow

1. Define the goal in one sentence.
   - Examples: competitor launch scan, customer-support sweep, topic monitoring,
     post drafting, or campaign response review.
2. Decide whether the task is read-only or action-capable.
   - Prefer read-only research by default.
   - Use action tools only for explicit, approved posting workflows.
3. Build a narrow query plan.
   - Specify accounts, keywords, post URLs, date windows, and output format.
   - Avoid broad collection when a small evidence set answers the question.
4. Run Hermes Tweet read or exploration tools.
   - Capture relevant post URLs, timestamps, authors, and short summaries.
   - Keep raw credentials, cookies, and private identifiers out of notes.
5. Synthesize the result.
   - Separate direct evidence from interpretation.
   - Flag uncertain or low-confidence claims.
6. For posting workflows, draft first.
   - Present the exact post text to the user.
   - Require explicit approval before any action-capable tool call.
7. Record follow-up work.
   - Include missing sources, rate-limit constraints, and suggested next checks.

## Prompt Template

```text
Use Hermes Tweet to perform a read-only X/Twitter workflow.
Goal: <goal>
Inputs: <accounts, keywords, URLs, or time window>
Return: concise findings, cited post URLs, and recommended next actions.
Do not post, like, follow, reply, or change anything.
```

For an approved posting workflow, replace the last line with:

```text
Draft the post first and wait for explicit approval before using action tools.
```

## Safety Checklist

- Keep credentials, cookies, tokens, and screenshots out of the skill output.
- Use public evidence and cite source URLs where available.
- Do not automate engagement, scraping, or posting beyond the approved scope.
- Respect platform rules, user privacy, and project policies.
- Keep action-capable tools gated until the user confirms the exact final text or
  operation.
