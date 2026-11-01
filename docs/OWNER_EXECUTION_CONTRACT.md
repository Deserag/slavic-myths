# SLAVIC MYTHS — MANDATORY OWNER EXECUTION RULES

**Effective:** 2026-11-01. **Authority:** direct owner decision, [Issue #406](https://github.com/Deserag/slavic-myths/issues/406). **Applies to every ChatGPT, Codex, planning assistant, coding agent, code reviewer and subsequent chat.** MUST and MUST NOT are non-negotiable. The owner's explicit decision is the sole authority for scope, visual approval and final acceptance. This document creates no permission to start gameplay development.

**ZERO-TOLERANCE RULE:** No real GitHub Issue, no accessible owner-approved reference, incomplete per-component specification, wrong branch, unapproved scope expansion, missing real runtime evidence or missing owner acceptance = **NOT DONE**. An agent must not substitute its own judgment, a compiled JAR, an SVG sketch, a partial geometry fix, a long report or a speculative "equivalent" asset for a requested result.

## 1. Mandatory reading and binding repository requirements

At the beginning of every task, inspect **current files on the actual integration/working branch**, not only the repository default branch. Before sending an implementer prompt, read and obey:
- [AGENTS.md](../AGENTS.md), [this contract](OWNER_EXECUTION_CONTRACT.md), [current branch policy](BRANCH_POLICY.md), [project workflow](PROJECT_WORKFLOW.md), [Codex playbook](CODEX_PROJECT_PLAYBOOK.md).
- [Requirements index](requirements/README.md), [master contract](requirements/00-MASTER-CONTRACT.md), [save/MP/performance](requirements/12-SAVES-MP-PERFORMANCE.md), [test gates](requirements/13-TEST-GATES.md), [open regressions](requirements/14-OPEN-REGRESSIONS.md).
- **Every applicable category document:** [world generation](requirements/01-WORLDGEN.md), [structure catalog](requirements/02-STRUCTURE-CATALOG.md), [mobs/bosses](requirements/03-MOBS-BOSSES.md), [Nether biomes](requirements/04-NETHER-BIOMES.md), [weapons/combat](requirements/05-WEAPONS-COMBAT.md), [tools/equipment](requirements/06-TOOLS-EQUIPMENT.md), [loot/rewards](requirements/07-LOOT-REWARDS.md), [NPCs/quests](requirements/08-NPC-QUESTS.md), [blocks/crafting](requirements/09-BLOCKS-CRAFT.md), [art/audio/UI](requirements/10-ART-AUDIO-UI.md), [transport/farming](requirements/11-TRANSPORT-FARMING.md), [weapon-by-ID](requirements/15-WEAPON-BY-ID.md).
- The exact currently approved Issue, linked parent/child Issues, last owner decisions, direct reference files, actual relevant source/assets, PRs and their commits, real HEAD and worktree status.

**For every boss:** the minimum applicable set is README, 00, 03, 10, 12, 13, 14; add 01/02/04 for worldgen/arena, 05 for weapons, 07 only if loot is specifically approved and within scope, and other categories if changed. Each applicable requirement MUST be represented in an acceptance matrix. Unchecked requirements are NOT_RUN, never PASS. Historical notes and old snapshot branches are not executable instructions. The ONLY branch authority is the latest explicit owner decision and current [branch policy](BRANCH_POLICY.md).

## 2. HARD STOP: one independent implementation/fix requires one REAL GitHub Issue

**NO ISSUE = NO CODING / MODELING / ASSET PRODUCTION / FIX / MERGE.**

1. Every new mob, boss, structure, weapon, item, block, UI, sound set, model, animation, worldgen system, feature or independent bug fix MUST have a real GitHub Issue **before implementation**.
2. One independently implementable and testable result requires its own Issue. A large package must use a parent Issue with separately specified child Issues for independent entities/fixes unless the owner explicitly approves a genuinely inseparable unit. A branch, PR, chat, comment, Project draft or code file is NOT a substitute for the Issue.
3. Before declaring a task absent, the agent MUST search **Issues, PRs, feature/integration branches, repository files, relevant commits and source**. GitHub default-branch code search cannot prove a named feature branch is empty.
4. If no Issue exists: create the detailed Issue, link the reference evidence and ask for explicit owner approval before implementation. Issue creation alone does not authorize coding.
5. For an existing task/defect, UPDATE the canonical Issue and PR; do not create duplicates or rewrite a working feature without verifying its existing state.

### Every execution Issue MUST be self-contained, with these exact content groups

- **Identity:** version, precise owner-approved feature/fix, direct owner decision, Issue and linked children, status.
- **Baseline:** actual commit/branch, existing registry IDs and files, observed before-state or reproducible defect; do not invent source paths.
- **References:** embedded owner-approved originals, verified direct links, approval date, SHA-256/blob SHA, source priority, missing-file status.
- **Detailed blueprint:** every visible part; dimensions/Steve scale, geometry, material, palette, UV/texture, rigs, attachments, poses, animations, sounds, VFX, special states.
- **Mechanics:** each ability, attack, phase, trigger, hitbox/contact window, HP/damage in HP not hearts, cooldown, AI, spawning/access, server/client ownership, data persistence, limits.
- **Resources:** specific required editable source models (.bbmodel where applicable), textures PNG/UV/emissive, Java/JSON files, sounds/OGG and licenses, sprites, assets and RU/EN localization.
- **Survival path:** how a player finds/obtains/uses it without cheats; test-only spawn eggs and commands are clearly diagnostic, NOT substitutes for natural encounters.
- **Dependencies:** what existing systems must be reused; exact IN SCOPE and OUT OF SCOPE file groups/behaviors.
- **Branch contract:** repository, integration base, working branch, current verified SHA, PR target.
- **Per-detail acceptance:** requirement, original reference/coordinate, implementation path, reproducible test, actual evidence, PASS/FAIL/BLOCKED/NOT_RUN.
- **Regression/negative cases:** saved worlds, reload/restart, two players/dedicated server, permission/ownership and anti-duplication, collision, performance where relevant.
- **Blockers/unknowns:** concrete unapproved numbers, unavailable originals and explicit decisions outstanding. An unresolved critical detail blocks relevant execution.
- **Full definition of completion:** technical, functional, visual and owner-acceptance gates (see §7).

**Forbidden Issue:** "Make it look like the picture" with no linked image or part-by-part blueprint. A new agent must be able to open the Issue and access all approved materials without an old private chat.

## 3. HARD STOP: approved reference is REQUIRED AND DIRECTLY ACCESSIBLE FROM THE ISSUE

**NO APPROVED ACCESSIBLE VISUAL ORIGINAL = BLOCKED_REFERENCE_MISSING / BLOCKED_OWNER_DESIGN_APPROVAL.**

For every *new* visible asset — creatures, bosses, structures, items, tools, weapons, blocks, NPCs, UI, particles — the Issue MUST include:
1. The latest **actually owner-approved original** image/blueprint. Attach it directly to GitHub Issue or commit it into the repository; embed an image preview **in the Issue** AND include a working link to the original file. A private chat reference, just a filename, an unnamed archive/ZIP, a hash without bytes, or a claimed Library path is insufficient.
2. Every required view supported by the approved art: front, side, back, ¾, distinctive close-ups, size comparison to Steve, materials and animation/attack storyboard when appropriate. If an additional view lacks approval, mark it unapproved and do not claim it is canonical.
3. A manifest row per reference: `file name | approval source/date | GitHub direct link | SHA-256 or blob SHA | views | canonical priority | file-access test YES/NO`. The implementation agent MUST successfully read each source; text saying a file exists is not an access test.
4. Approved priority: newest specific owner-approved **original** first, then owner changes, then Issue's accepted dimensions/details, then technical SVG/Blockbench supporting diagrams. SVG line art or a generic model MUST NEVER replace an approved full-color PNG without explicit owner consent.
5. **Exact comparison:** complete recognizable shape, anatomy, scale, silhouette, placement, materials, palette, UV, opacity/emissive, details and animations must match as closely as the Minecraft medium allows. No generic humanoid skin, enlarged cubes, recoloring or arbitrary "inspired by" redesign.
6. For an **existing-asset repair**, include approved target images and actual source/before screenshots; define precisely each mismatched component and its required after-state. Fixing geometry alone while wrong textures/UV/colors/animation remain is **FAIL_ART_INCOMPLETE**, not a fixed boss.

When a real reference is missing, report **which file is missing, from which Issue/branch it was checked, and what must be supplied**. Never invent a replacement and call it approved. Work on unrelated details cannot be used to claim visual progress or readiness. Where a task is entirely nonvisual, require an approved behavioral/data specification and reproducible expected outcome instead of forcing an irrelevant picture.

## 4. HARD STOP: every component must be independently specified

The task author MUST break the reference into **checkable fine-grained parts**, not a vague paragraph. For each part write `part | approved image region/frame | precise design/dimensions | model/UV/rig/source | animation/state | acceptance evidence`.

**Mandatory categories when applicable:**
- Silhouette, total length/height/width in Minecraft blocks, hitbox vs rendered body, comparison with Steve, four angles.
- Count, proportions and positioning of heads, faces, horns, eyes, beaks, jaws, claws, limbs, joint pivots, wings, feathers/membranes, spines, tails, armor, clothing, held equipment, ornaments and all distinctive decorations.
- Pixel texture resolution, albedo/UV/emissive/material and exact palette areas; no missing textures, blank UV islands, flipped faces or placeholders.
- Rig bones, independent pivots, collision-safe motions, key poses, idle, locomotion, takeoff/flight/landing, sleep/wake, warnings, attack hit frames, recovery, phases, hit reactions and death.
- Audio: legal original OGG/vanilla sources, sounds.json/SoundEvent/callsites, distinct audible events and RU/EN subtitles. Synthesized noise or pitch-shifted generic filler is not approved vocal art.
- Combat: attack-by-attack behavior, ranges/HP damage, telegraph, server-authoritative collision/hit frame, timers, targets/allies, transitions, state save/reload and multiplayer effects.
- World rules: arena size/materials, natural placement, protected blocks, Nether roof/air clearance, chunk and existing-world safety, reachability without OP, no persistent forced chunk loads.
- Item or UI models where present: correct inventory/held/1P/3P visuals, grip, readable layouts, dimensions and player interactions.

Every feature must have **numbers if approved** and explicit owner-pending markers where not approved. Do not silently choose permanent game balance, loot, new abilities or appearance details. Being unable to infer one detail is not license to ignore it.

## 5. ABSOLUTE PROHIBITIONS: NO SCOPE CREEP

**Do not touch what the owner did not request.** The Issue and executor prompt MUST have explicit IN SCOPE and OUT OF SCOPE.

MUST NOT:
- Repair, refactor or modify unrelated mobs/bosses, existing accepted models, textures, rewards, economy, balance, recipes, quests, other structures, biomes, AI, UI, release metadata or build systems "while you're here."
- Create future features, placeholder classes, unapproved materials or drops, pets, new skill systems, extra visual variants or inventory items outside this Issue.
- Change mod ID, registry IDs, NBT, existing world/save state, old content, ownership records or protected player worlds without a separate approved migration requirement.
- Use another artist's game/mod audio unlawfully or pass synthetic tones/noise as real creature vocal recordings.
- Force-push, hard-reset, delete/rebase other contributors' commits or overwrite unrelated worktrees/branches. NEVER write to `main` without separate explicit owner authorization.
- Start another Issue automatically, self-approve, auto-close an Issue, mark owner-accepted or claim a test that was not performed.
- Run client GUI automation in the owner's active desktop environment, modify the main PolyMC instance or original worlds; if isolation is not demonstrably safe mark `BLOCKED_ISOLATION`.
- Change branch because a historical document says so; for a task use ONLY the newly verified target from §6.

If a dependency appears outside scope, record it as a **blocker or separate Issue**, not an excuse to alter that system. Scope approval is not inferred.

## 6. EXACT BRANCH INFORMATION IS MANDATORY BEFORE EVERY IMPLEMENTER PROMPT

**INVALID PROMPT if ISSUE, verified branch/commit, references or in/out scope is absent.** The very beginning of the prompt MUST be:

```text
REPOSITORY: Deserag/slavic-myths
ISSUE: #<ID> <direct GitHub URL>
BASE/INTEGRATION BRANCH: <owner-approved current exact branch>
BASE HEAD SHA: <verified commit>
WORKING FEATURE/FIX BRANCH: <exact current branch>
PR TARGET: <exact target branch>
REFERENCES: <approved original URLs embedded in Issue and verified>
MANDATORY DOCS: AGENTS.md; docs/OWNER_EXECUTION_CONTRACT.md; docs/BRANCH_POLICY.md; docs/requirements/
IN SCOPE: <enumerated paths and exact outcomes>
OUT OF SCOPE: <enumerated prohibited changes>
ACCEPTANCE GATES: <source+gameplay+art+runtime evidence>
```

Check [current branch policy](BRANCH_POLICY.md), actual remote refs, `git status`, HEAD, PR state and parallel writers. The **current policy for paired sisters** identifies integration `3.3` and the existing feature branch `feat/3.3-paired-sisters-bosses-20261101`; confirm whether its changes were already integrated before any further modifications. No old version branch is a general default, and `main` is protected.

A branch name found in an old report, prompt, archived spec, status log or filename MUST NOT be treated as an instruction. If branch selection is ambiguous, **BLOCKED_BRANCH** and report the discrepancy. Never invent a target.

## 7. EXECUTION GATES AND DEFINITION OF DONE

### Gate A — owner authorization and planning
Have a real approved Issue; read relevant requirements and source; verify exact branch and explicit authorization. Determine blocked reference/spec components BEFORE coding.

### Gate B — complete implementation, not a demo
Implement every in-scope component: actual runtime Java/NeoForge integration, registry/data JSON, real editable model, voxel anatomy, UV/albedo/emissive textures, animation clips, audio, localization, UI/equipment, natural acquisition/encounter and saved state as applicable. No concept art substituted for production assets.

### Gate C — build and static checks
Java 21 clean build, validated production JAR, asset/model/texture/registry/localization checks. Build PASS means **BUILD_PASS ONLY**, not runtime or visual success. Include source SHA and actual artifacts.

### Gate D — genuine gameplay and compatibility
Real authorized Minecraft/world/client/server execution: natural survival path, collision and attacks, encounters across multiple seeds if applicable, actual phases and death, player interaction, negative cases, saved reload, two-player/dedicated server and old-world regressions when relevant. Diagnostic `/summon`, `/give`, `/locate` and artificial unit tests DO NOT prove natural survival playability.

### Gate E — exact visual/audio acceptance
Use the **actual running Minecraft client** in an isolated environment; capture in-game front/side/back/¾ at matched scale and comparable lighting next to the original approved reference, plus closeups and actual animation states. Confirm geometry, **texture, UV, palette, emissive, rig, proportions, VFX, animation and distinct audio**. An editor preview, static SVG, modeled geometry without UV, or generic placeholder is NOT visual parity. If actual client access unavailable, mark visual runtime `NOT_RUN` and never say ART_PASS.

### Gate F — integration and optional deployment
PR only into the correct approved integration branch after source checks; preserve simultaneous work and reconcile conflicts. JAR built ≠ installed. Only if specifically authorized, verify PolyMC installation path, backup, SHA, absence of duplicate JAR, and correct profile. Otherwise state `NOT_INSTALLED`.

### Gate G — owner accepts
Only the owner can accept the in-game result, set ready/completed status or close the Issue. Otherwise `OWNER_PENDING`; task remains OPEN.

**Completion requires ALL:**
1. Actual fully approved Issue + accessible original sources.
2. All components, not just part of geometry or a selected mechanic.
3. Every applicable rule in `docs/requirements/` satisfied, with evidence per line.
4. Real build, runtime behavior and visual/audio tests in the required environment (any missing gate visibly marked).
5. Relevant survival, negative, reload, multiplayer, compatibility, performance checks.
6. Correct branch/PR/JAR deployment state, without unapproved extras.
7. Explicit owner acceptance. **Nothing else is DONE.**

**Every requirement is evaluated independently** in the matrix:

| Requirement / visible part | Approved original and SHA | Actual code/resource path and commit | Expected player-visible behavior | Real test + evidence (seed, world, screenshot/video/log) | PASS / FAIL / BLOCKED / NOT_RUN / OWNER_PENDING |
| --- | --- | --- | --- | --- | --- |

If ANY mandatory item is `FAIL`, `BLOCKED` or `NOT_RUN`, the deliverable stays **NOT DONE**. A partial success is reported precisely as partial, never promoted to a full completion claim. An assistant is forbidden to treat hundreds of assertions as a substitute for one unperformed user-required visual comparison.

## 8. EFFICIENCY RULES: comprehensive engineering, short user-facing responses

- The Issue must be extremely detailed; **user-facing chat must be concise and fast**. Typically give: `Issue/PR/branch | actual changes | verified tests | remaining blockers | next authorized action`. Do NOT send large reports unless the owner requests them.
- Prefer precise reads of known Issue, branch, diff and required assets. Do not repeat full repository analyses, create duplicate tasks or fill conversation with repeated status narratives.
- Preserve a short canonical GitHub checkpoint for new chat sessions. A long thread, volume of logs, number of tokens or unrelated fixes is **not** development progress.
- If blocked, identify the exact missing original, approval or gate and stop misrepresenting progress. Do not invent test results, dates, paths, links, commits or promises.
- Documentation of validation belongs in the Issue/repository so it is durable, but it must describe real evidence rather than word count.

## 9. AUTOMATIC NONCOMPLIANCE STATES

| Failure | Required state | Claim forbidden |
| --- | --- | --- |
| No/empty GitHub Issue | `BLOCKED_ISSUE` | started/implemented |
| No approved or accessible original artwork | `BLOCKED_REFERENCE_MISSING` / `BLOCKED_OWNER_DESIGN_APPROVAL` | matches approved reference |
| Missing part-by-part specification | `BLOCKED_SPEC_INCOMPLETE` | implementer may improvise |
| Incorrect/unverified working base/target | `BLOCKED_BRANCH` | work integrated correctly |
| Scope change not authorized | `FAIL_SCOPE` | a useful bonus fix |
| Geometry improved but colors/UV/texture/animation still wrong | `FAIL_ART_INCOMPLETE` | boss fully fixed |
| Build passed; real Minecraft/client not tested | `BUILD_PASS; RUNTIME_NOT_RUN` | gameplay/visual QA PASS |
| Source editor screenshot only | `FAIL_VISUAL_EVIDENCE` | visual parity accepted |
| JAR built, no verified installation | `NOT_INSTALLED` | installed / test profile updated |
| In-game result not accepted by owner | `OWNER_PENDING` | DONE / Issue closed |

**Enforcement:** if any required gate is missed, the agent MUST clearly state which exact requirement failed and MUST NOT continue to mark the feature completed. Escalate ambiguity instead of "being helpful" by weakening this contract.

**Policy change control:** edit this contract only on explicit owner direction, in a tracked GitHub Issue and a reviewed PR. Preserve links in `AGENTS.md`, `docs/requirements/README.md` and `docs/CODEX_PROJECT_PLAYBOOK.md`. This document is an operating constraint, not evidence that any current boss has been repaired.
