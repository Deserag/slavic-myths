# Bossbar UI — level colors, 05.11.2026

Source of creature ranks: [1.9.8.1 CREATURES.csv](../../audit/creatures-1.9.8.1/CREATURES.csv), field \`danger_level\`.
That audit predates the recent 2.0–2.5 bosses; additional rank assignments below are **provisional**, not retroactively described as user-approved.
The audit defines levels but **does not prescribe colors**. The following consistent vanilla bossbar palette is a UI convention implemented for this patch:

| Danger level | Bossbar color | Color enum |
| --- | --- | --- |
| IV | green | GREEN |
| V | yellow | YELLOW |
| VI | blue | BLUE |
| VII | red | RED |
| VIII | purple | PURPLE |

The original audit lists 28 bosses and minibosses at IV–VIII; their assigned ranks are copied unchanged into \`BossBarPalette\`. All existing bossbar constructors use the same color resolver, without changing their bossbar names, overlays, health calculations, visibility, effects, or combat.

## Newer entities not present in the old balance table

| Level | Entities |
| --- | --- |
| V | brown_bear (mother bear encounter bar) |
| VI | agneya, ledeya |
| VII | tryaseya, cursed_mytar, morova_deva, magma_lord, zharenitsa, grove_guardian |
| VIII | gneteya, bauk, trizna_prince, posten, lomeya, neveya, viy |

These UI classifications are based on relative encounter scale / existing boss stats, not a change to damage or health. Confirm them against the next approved balance table. For an unknown boss entity, retain its original bossbar color rather than invent a level.

## Nameplate fix

Keep bossbar names and entity custom names intact. Do not force \`setCustomNameVisible(true)\` for Lomeya, Neveya, or Viy; that caused redundant always-visible white labels above their models. They still show their Russian name in the bossbar. Nothing in this patch changes attacks, HP, rewards, spawn mechanics or entity registry names.

## QA

- Run \`python tools/verify_bossbar_tiers.py\` to guard constructor usage and absence of forced white nameplates for the three affected bosses.
- Java 21 \`clean build\`; confirm classes/resources in JAR.
- Runtime visual checks: all bossbar levels and Lomeya/Neveya/Viy head labels; verify visual accessibility and multiple nearby bosses.
- This patch does not claim an in-game combat test.
