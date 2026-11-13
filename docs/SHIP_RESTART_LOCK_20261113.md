# Slavic Myths 2.6 — OWNER RESET / QUARANTINE — 2026-11-13

## Final owner decision
The owner explicitly REJECTS all ship changes after the last owner-accepted moving ship on branch `ship-release-20261111`, commit `d965d5c845c58f14c676931ef9621499573f926f`. The acceptance record is `docs/SHIP_MOTION_RELEASE_20261111.md` on that exact commit.

This is a hard, permanent prohibition unless the owner explicitly issues a NEW, narrow, written authorization for a wholly new implementation. Never assume that time passing, an old PR, a passing CI check, a green screenshot workflow, or a prior coding agent's instruction overrides the rejection.

## Forbidden, no exceptions by default
- DO NOT merge, rebase, cherry-pick, copy, resurrect, auto-migrate, or selectively import code, geometry, sails, rigs, camera patches, controls, shipyard UI, visual references, test scaffolding, renderer batches, caches, or optimizations from ANY rejected post-baseline ship branch/PR/commit.
- DO NOT restore rejected bow/stern appendages, rectangular box-like hulls, blocked views, missing glass, overly dense controls, camera regressions, or copied renderers merely because previous checks passed.
- Do not use the old work as an implementation template. Old PR descriptions, screenshots and tests are historical rejection evidence only.
- Do not modify the accepted baseline `ship-release-20261111`. Do not change `release-2.5`, `main`, or unrelated mod 2.5 workflows.
- Do not start any code change until the owner has inspected the restored baseline and explicitly chosen the first defect/feature to address.

## Git operations already done on 2026-11-13
The following ship PRs were closed without merge: #482, #483, #484, #485, #486, #487, #489, #490, #491, #492, #493, #495.
Their 12 head branches were force-reset to the accepted SHA with compare-and-swap SHA leases. The refs may still exist by their old names; old commits/closed PR records may remain reachable in GitHub history. This is not permanent deletion from the GitHub object database and does not authorize reuse.
The isolated new branch `restart/2.6-ships-approved-baseline-20261113` was created strictly from the approved commit. This file and a scope note in AGENTS.md are administrative safeguards, NOT ship feature changes.

## Execution process (speed + correctness)
1. Baseline: checkout accepted commit in a clean local branch, confirm SHA, run only minimal targeted smoke and the exact owner-reported ship behavior; do not repeat already known expensive tests. Capture owner observations.
2. Prioritization: owner decides ONE next issue after baseline inspection. No self-assigned backlog and no speculative improvements.
3. Design: owner-approved, specific multi-view references for each vessel. Reproduce proportions and structure precisely in actual Minecraft voxel/block models; no invented generic shapes. Explicitly state before/after expected behavior and what source files may change.
4. Implementation: one problem, smallest patch, ideally 1–3 production files. Avoid cross-cutting refactors, performance experiments, bulk documents, and unrelated fixes. Isolate in a single new topic branch from this restart branch.
5. Proof: targeted low-cost static/unit checks first; one necessary build; real client smoke when isolated safely or owner-tested in game; before/after views and a tiny regression checklist for controls/Shift/cargo only if affected. Headless geometry checks and CI do NOT prove visual acceptance.
6. Acceptance: report exactly changed files, verified facts, NOT_RUN/FAIL, screenshot evidence, and one rollback commit. If regression, revert that topic patch instead of adding layers of compensating fixes. No next task before owner acceptance.

## Communication
- Short and concrete messages with explicit evidence. No long task prompts spanning many unrelated files.
- Never claim done from a green CI, code compilation, or inferred behavior.
- Never delay the answer with broad GitHub history scans, off-scope re-reading, or speculative research.
