# Morovoy Fire Zmey (#396) — implementation and verification ledger

**Scope:** Minecraft 1.21.1, NeoForge, Java 21. **This is an unaccepted draft, not an in-game signoff.**

## Implemented in this branch (source available, not necessarily verified)
- A separate registered monster with 2200 effective HP (physical health scaled to the vanilla 1024 attribute cap), sleeping/waking/phase I/phase II/dying states, persistent NBT, boss bar.
- Server-authoritative attack scheduler with two projectile types, directional breath with line-of-sight checks, two separate tail windows, bites with short pull, claw, aerial dive, alternating elemental streaks. Gameplay balancing/telegraphs require live QA.
- Temporary no-block-write elemental fields, reusable purple-fire particles (dark-violet/violet/white) and projectile visuals, without changing vanilla fire.
- An original two-headed, two-wing, two-tail voxel rig, original albedo/emissive PNGs and 2 material icon textures. This is **a first art pass only**, not side-by-side accepted against the owner PNG.
- Reuses the open #393 crater terrain and generation-piece classes; separate Zmey structure ID shares the same weighted structure set as the Raven, so they cannot occupy one chosen start.
- Two registered resource materials; **no drop loot table, staff, spawn egg, pet, ride, quest or reward economy**.

## Actual evidence
| Requirement | Status | Evidence |
| --- | --- | --- |
| Source pushed on isolated version feature branch | TO_VERIFY | Commit SHA is recorded by GitHub |
| Java 21 pure rules smoke | NOT_RUN | `tools/qa/MorovoyZmeyRulesSmoke.java` |
| Clean Gradle Java 21 build | NOT_RUN | GitHub Actions workflow added; results must be inspected |
| GameTest and dedicated server | NOT_RUN | No runtime attached to this work session |
| Two clients, PvP/targets, shields, relog | NOT_RUN | Must be tested in an actual server |
| Five Nether seeds, natural access / loaded area | NOT_RUN | Draft arena #393 has only pure geometry QA |
| Approved side-by-side PNG | ART_SOURCE_BLOCKED | `MOROVOY_FIRE_ZMEY_APPROVED_REFERENCE.png` not found in current branch |
| In-game screenshots/videos / FPS/MSPT | NOT_RUN | No Minecraft client run or capture produced |
| Precise head/tail colliders and grab/release | IN_PROGRESS | Current logic is a first-pass combat implementation |
| Individual animation clips and complete editable art rig | IN_PROGRESS | Native ModelPart rig exists; timeline clips and approved Blockbench comparison remain |

## Outstanding critical work
1. Correct Java compiler/NeoForge API errors from actual CI, if any.
2. Supply and hash-check the approved reference pack; finish a source-editable Blockbench model and proper 1:1 visual iteration.
3. Real 3D flight corridor and anti-wall clip tests with opened wings; verify collision windows for both moving tails, both bites and airflow.
4. Multiplayer/dedicated-server test, shield/faction tests, two heads and all hit timings, expiration after unload/restart, safe arena traversal.
5. Capture unedited **Minecraft** front/side/back/¾, sleeping/waking and attack screenshots beside the approved reference.
6. Explicit owner acceptance before moving from draft to ready or merging.

Neither #391 nor #393 is accepted/merged as of the source inspection. This branch is based on the source of draft PR #393, not on main or hell.
