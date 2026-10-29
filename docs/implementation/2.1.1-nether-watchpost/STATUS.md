# 2.1.1 — Дозорный пост (WIP, 29 October 2026)

Owner override: the owner explicitly instructed work on the 2.1 series **before** personal acceptance of the two versions, with **combined later owner Minecraft QA for 2.0 and 2.1**. This supersedes the old ordering prohibition in [the roadmap](../../concepts/nether-structures/ROADMAP_2.1_2026-10-29.md) and [#338](https://github.com/Deserag/slavic-myths/issues/338) for **starting 2.1**, but does NOT grant `OWNER_ACCEPTED`, claim version 2.0 is complete, or authorize closing any issue. Only `hell` is modified.

## Primary approved references

- [Blueprint: four elevations, interior/top/bottom and section](../../concepts/nether-structures/2.1.1_WATCHPOST_BLUEPRINT.svg) — available SVG schematic, **not** the missing original chat PNG, `ART_SOURCE_PENDING`.
- [Full technical specification](../../concepts/nether-structures/2.1.1_WATCHPOST_SPEC.md), [#338](https://github.com/Deserag/slavic-myths/issues/338).
- [Requirements 01/02/07/12/13](../../requirements/README.md) and [AGENTS](../../../AGENTS.md).

## Initial native implementation (do not confuse with acceptance)

- `src/main/java/org/slavicmyths/netherstructures/NetherWatchpostWorldgen.java` — native StructureType/PieceType registration using existing registries.
- `NetherWatchpostStructure.java` — strictly bounded noise-column preflight in Nether Y39–108, rejects lava, bedrock/unsupported ground, low ceilings and potential competing structure positions; one-chunk 11×11 footprint, no nonlocal chunk tickets, no retrogen.
- `NetherWatchpostPiece.java` — deterministic 4 provisional heights (upper-deck +5/+9/+13/+17), 3 damage levels, 4 rotations, centered supported ladder, one top OR bottom accessible chest, one vanilla campfire/lantern, basalt+blackstone palettes, native lazy chest loot seed. **These heights are unapproved engineering interpretations** of owner's 1–4 block wording; do not present as locked art or balance.
- `data/slavicmyths/worldgen/structure/nether_watchpost.json`, `structure_set/nether_watchpost.json` (first measured-frequency candidate spacing32/separation12/frequency0.8/salt21011021, **BALANCE_OWNER_PENDING**) and `tags/worldgen/biome/nether_structure_biomes.json` with 5 vanilla+4 current modded Nether biome IDs. Future Nether biomes must join tag.
- `loot_table/chests/nether_watchpost.json` uses only existing vanilla and confirmed `bone_arrow` IDs: always native arrow 4–12 + one food set 2–5; independent optional rolls; a shared 3% pool makes diamond sword/helmet mutually exclusive (roughly 1.5% each), no higher-tier equipment/new IDs.
- `tools/hell-qa-runtime/org/slavicmyths/netherstructures/WatchpostQaTests.java` — 96 source-geometry variants + 1000 **native** chest rolls once the test is actually executed; no user GUI control.

## Required evidence, NOT assumed PASS

| Requirement | Implementation / evidence | Status |
|---|---|---|
| Native registry + structure set / all known Nether biomes | Java and three worldgen JSON files | SOURCE_WIP until build + actual world |
| Four heights / 4 rotations / degradation / center ladder / one chest | `NetherWatchpostPiece`, GameTest fixture | NOT_RUN until CI; `OWNER_GEOMETRY_PENDING` |
| Guaranteed arrows+food, limited equipment | Native chest loot JSON; 1000-roll fixture | NOT_RUN until CI |
| Five natural worlds, accepted starts, 9 biomes, slopes/caves/shore, old vanilla structures | Must collect 5 seed, XYZ, variant, accepted and rejected stats | NOT_RUN |
| Normal survivor finds without OP and `/locate` read-only | Native structure only; no manual spawn/locate mutation | NOT_RUN |
| Client four-side model/real lantern/campfire, natural walkthrough and looting | Isolated GUI unavailable; later combined owner QA | GUI_BLOCKED_ISOLATION / OWNER_PENDING |
| 2-client, restart, old-world copy, MSPT | Independent runtime checks required | NOT_RUN |
| Exact chat PNG against real visual | SVG accessible; PNG absent in repo | ART_BLOCKED_REFERENCE |
| 2.1.2 / 2.1.3 / 2.1.4 and others | Their original Issues remain separate, no speculative item additions | NOT_STARTED |

The original 2.0 technical JAR and test logs remain untouched. This work does not install a new JAR in the owner's PolyMC or modify `main`. Latest clean Java21 build, actual run, source SHA, JAR checksum and regression results must be added after completed CI. No issue closed.

## First verified 2.1.1 candidate — 29.10.2026

- Candidate branch/source at test: `hell`, [738e8f78e57aafd66a54efa10ddcb2fd37b3f208](https://github.com/Deserag/slavic-myths/commit/738e8f78e57aafd66a54efa10ddcb2fd37b3f208).
- GitHub Actions [run 40836266223](https://github.com/Deserag/slavic-myths/actions/runs/40836266223), **success**: Java 21 clean build **PASS**, resources/JAR packaging **PASS**, headless actual packaged Minecraft GameTest **14/14 PASS**. No local desktop/GUI launch, main change or PolyMC replacement.
- The native watchpost generated in a **real new Nether chunk** (not only a theoretical planner candidate): seed `20261024`, owning chunk `[-54,7]`, biome `slavicmyths:lost_halls`, bounding box `(-861,50,115)..(-851,75,125)`. The bounded scan needed 9 potential placement candidates and found 1 accepted natural plan. The generated world had exactly 1 chest and 18 ladder blocks.
- 96 separate geometric variants (four section heights, four rotations, three ruin levels and two chest choices) passed native server checks. Native chest loot **1000/1000** had real guaranteed arrow and food; `bone_arrow` was present in optional outcomes; diamond-item mutual exclusion held; 26/1000 chests contained a rare diamond item in this seeded sample. These are **not** final economic/owner-ART approvals.
- Build artifact `slavicmyths-2.1.1-HELL.jar`, 25,378,009 bytes, SHA256 `172daa56badad584e08f5d0656dd7ee728556a6455723197c421c8cd028e1c46`. SHA was reverified against extracted CI output. Original technical build `2.0.10-HELL` remains separately available, no owner installation changed.
- Still **NOT_RUN**: minimum 5 independent natural seed worlds across all 9 Nether biome IDs; survival no-OP discovery and `/locate` read-only; old world/Nether fortress/bastion/ruined portal proximity; real map visuals, four facades and movement, FPS/MSPT, 2-client MP, restart and chests at chunk boundaries. The missing owner PNG remains `ART_BLOCKED_REFERENCE`; **OWNER_ACCEPTED remains false**. The next structure is separately Issue #339, not part of 2.1.1.

This candidate is a **technical checkpoint**, not an accepted 2.1 release or a claim that the 2.0 owner testing was completed.
