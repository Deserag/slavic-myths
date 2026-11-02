# Slavic Myths — authoritative branch selection policy

**Owner decision, 2026-11-01. This is the ONLY active branch-selection rule for new work.** Earlier instructions embedded in archived reports, old prompts, screenshots, previous chats, project status history or an Issue's original version label MUST NOT choose a development branch.

## Non-negotiable branch gates

1. **One integration branch per release.** The exact branch is selected by the latest explicit owner decision for that release and task, then verified against GitHub current branch HEAD. No branch name is a permanent default for every future task.
2. **For the current paired-sisters implementation: integration/PR target = `3.3`.** Existing implementation feature branch = `feat/3.3-paired-sisters-bosses-20261101`, and its source history must be checked against actual `3.3` HEAD and existing PRs before further changes. Both pairs — Gneteya/Tryaseya and Ogneya/Ledeya — belong to this release plan. Earlier concept-number labels do not override the current integration branch.
3. Existing unfinished work of other releases stays governed by its **own newest owner-approved Issue and target**. An earlier cycle may have used `2.2` or `2.3`; neither is a default for a new task. Never move active or accepted fixes across release branches without approval, compare/diff review, tests and conflict reconciliation.
4. `main` is protected: MUST NOT modify it without explicit separate owner permission. Never overwrite parallel work or change integration history via forced push, reset or unapproved rebase. Preserve all commits and external contributors' changes.
5. **No automatic branch selection by AI.** Before writing a prompt or code, verify: `REPOSITORY | ISSUE | BASE/INTEGRATION BRANCH | BASE SHA | WORKING BRANCH | PR TARGET | IN/OUT SCOPE | REFERENCES`. Read actual remote HEAD, git status, related branches/PRs and open conflicts. If any branch dimension is unknown or contradictory: `BLOCKED_BRANCH`, no coding.
6. Historic branch names, release candidates, old build folder names and deprecated instructions embedded in large status logs are **historical evidence only**. They are NOT instructions to check out, push to, merge from or select any branch.
7. A PR must target the exact authorized integration branch and be reviewed before merge. Successful build or a code-only merge does not mean the owner accepted an asset or feature.

## Paired-sisters scope reminder (not implementation authorization)

- **Gneteya and Tryaseya:** the latest owner-approved detailed Minecraft visual reference originals are mandatory. Gneteya is a large heavy bone-armored tank (~5–6 blocks) with real bone structures and weapon; Tryaseya is a floating upper humanoid (~3–4 blocks), smoky lower half, ritual clothing, and an iron lantern. Only approved individual mechanics apply.
- Natural encounters spawn the approved sisters as a **pair**; each has a separate **Creative diagnostic spawn egg**. When one dies, the other enters an enrage state; do not fabricate independent additional phases. Combat/stat numbers identified as provisional are NOT approved final balance.
- **Ogneya and Ledeya:** fire/ice paired concept exists, but their exact approved appearance and individual skills still require reference confirmation before implementation.
- Rewards, crafting, quest extras and unrelated systems are OUT OF SCOPE unless explicitly approved in a separate Issue. Minecraft visual acceptance requires real in-game captures beside approved reference originals, authorized isolated runtime tests, and owner review.

## Mandatory linked authorities

- [Owner execution contract](OWNER_EXECUTION_CONTRACT.md)
- [AGENTS.md](../AGENTS.md)
- [Project workflow](PROJECT_WORKFLOW.md)
- [Codex playbook](CODEX_PROJECT_PLAYBOOK.md)
- [All relevant requirement documents](requirements/README.md)

**Fail-safe:** if instructions disagree, prioritize the newest task-specific explicit owner decision and request resolution rather than infer from old documents. Under no circumstances treat archival prose as an active work order.
