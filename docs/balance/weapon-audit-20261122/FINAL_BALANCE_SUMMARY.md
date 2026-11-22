# Slavic Myths — final source-level weapon balancing summary (2026-11-22)

**Branch:** `feature/weapon-skill-input-standard-20261122`
**Game:** Minecraft 1.21.1 / NeoForge
**Catalog baseline:** `CATALOG.md` and `catalog.json` containing 102 IDs (historic snapshot before balance commits).
**Implementation reports:** `IMPLEMENTATION_BATCH_1.md` through `IMPLEMENTATION_BATCH_5.md`.

## What changed over five batches

| Batch | Focus | Representative changes |
|---|---|---|
| 1 | Rare Perunite and Nether-related equipment | Perunite sword 6.5→8.5; axe 8.5→10.5; mace 9.5→11.5; spear 6.5→8.5; flail 8.5→11.475; netherite spear 8→10 and flail 10→13; Navya ostroga 10→13; shadow throwing knife skill damage 10→14 |
| 2 | Low-performing specialty weapons | Abyss staff AoE 4→8 and cooldown 10→7 s; storm staff pulse 8→11; pool spear normal 9→11 and water-only charge 9→12 |
| 3 | Major boss and late-game rewards | Karachun scythe melee 12→16; Morozko staff bolt 8→11/icicles 5→8/wave 4→7; Morovoy staff 8/11→11/14 bolts and 45→35 s / 40→30 s cooldowns; Pharaohka trident secondary tidal wave 4→8 |
| 4 | Other unique rewards and low-value skills | Likho staff control ability cooldown 28→22 s; Ataman flail hit 12→14; Kalin sabre sweep 5→9; Thunder axe hit 8.5→11.5 and periodic bonus 1→2 |
| 5 | Final expensive/slow equipment gaps | Diamond flail hit 9→10.35, charge 10→11.5; Zhara flail hit 10.35→14.49, charge 11.5→15.5; Berdysh hit 9→11; battle axe 10→11; thunder spear hit 7.5→9.5 |

All damage values are **source-derived nominal figures** before armor and conditions. They must not be interpreted as measured effective DPS or actual in-game damage. Some skill numbers are pre-mitigation and particular attacks hit multiple targets. Earlier report numbers are historical before/after values, not necessarily final values if a subsequent batch modified that family.

## High-power rewards intentionally unchanged

The Nine Mor spear and axe, Neveya scythe, Lomeya paired hammer, Mytar shackled sickle, Tugarin paired swords, Nightingale dagger, Victor spear, and other high-damage/high-utility boss rewards were retained after code-level reviews. Their charged/area/conditional effects make uniform buffs undesirable. Finist bow's magic arrow, quiver and guidance mechanics make its default melee attribute irrelevant to bow balance. Veles staff's summoned guardian mechanics similarly cannot be assessed from melee DPS.

## Scope coverage and remaining risks

The five batches targeted observed progression gaps rather than changing every one of the 102 catalog entries. The historic inventory includes ammunition, bows, shields and spellcasting weapons, for which nominal melee attributes must not be treated as their true damage. No broad code-level evidence from these targeted reviews justifies indiscriminately strengthening all of them.

**Software-level conclusion:** The planned five source-level tuning batches can be considered complete **conditional on final batch-5 CI success**. This does not establish that every one of 102 items has been tested, nor that all combat edge cases or balance risks are absent.

**Gameplay tests still needed:** A controlled Minecraft 1.21.1 NeoForge playtest with representative armor/resistance targets and Nether enemies, including hit timing, charged flail damage/range, Veles guardian AI and target retention, thunder spear reach/bonus damage, AoE target caps, ability cooldown UX, multiplayer behavior, runes and enchantments. Record observed DPS and time-to-kill against early/mid/late representatives before any further numeric tuning.

## Build and verification

Batch 4 [GitHub Actions #45013991063](https://github.com/Deserag/slavic-myths/actions/runs/45013991063) — **success**, including the configured 102-item catalog/input validation and Java clean build/test job. Batch-5 workflow checks run on each commit; verify the workflow attached to this final document commit before declaring the final build green. No game-client playtest or independently verified in-world GameTests occurred during this task.

## Next phase

Freeze the broad source-level numeric changes until the gameplay checks identify actionable, reproducible gaps. Then proceed to the distinct planned project: consistent structured equipment tooltips (weapons and armor), keeping thematic colors but standardizing headings, icons, keybind symbols, numeric descriptions and ability/cooldown formatting without fictional lore.
