# Weapon progression balance — implementation batch 1 (2026-11-22)

Branch: `feature/weapon-skill-input-standard-20261122`.

Baseline: `docs/balance/weapon-audit-20261122/catalog.json` and `CATALOG.md` (historical pre-balance values, deliberately preserved for before/after comparison). Weapon inputs: `docs/requirements/WEAPON_INPUT_102_COVERAGE_20261122.md`.

This is **a partial implementation**, not a completed balance pass of 102 catalog entries. The values below are code-derived attack damage, not experimentally measured in-game damage or DPS.

| Item | Progression | Before | After | Reason |
|---|---|---:|---:|---|
| perunite_sword | Rare Overworld material | 6.5 | 8.5 | Improve rare sword over common equivalents |
| perunite_axe | Rare Overworld material | 8.5 | 10.5 | Late material's slow attack lacked impact |
| perunite_mace | Rare Overworld material | 9.5 | 11.5 | Reward heavy mace speed with stronger hits |
| perunite_spear | Rare Overworld material | 6.5 | 8.5 | Was below diamond spear despite rare material |
| perunite_flail | Rare Overworld material | 8.5 | 11.475 | Slow 0.7 attack/s; improve both melee and charged attack |
| netherite_spear | Netherite | 8 | 10 | Better match late-material cost |
| netherite_flail | Netherite | 10 | 13 | 0.7 attack/s caused poor nominal melee DPS |
| navya_ostroga | Nether smithing | 10 | 13 | Improve late-game viability |
| shadow_throwing_knife | Nether smithing | 8 projectile (10 skill) | 11 projectile (14 skill) | Preserve useful damage vs Nether enemies |

Perunite flail charged: 9.5 → 12.825; netherite flail charged: 11 → 14.3. Attack speeds, enchantment logic, weapon inputs, cooldowns, enemies and recipes were not intentionally changed.

**Not changed in this batch:** remaining catalog entries. Many remain to be evaluated against real progression and ability effectiveness; items with strong abilities may not need raw damage increases. No categorical declaration that every unchanged item is balanced.

Touched: `combat/WeaponCatalog.java`, `registry/ModItems.java`, `nethersmithing/NetherWeaponRules.java`, `nethersmithing/NetherWeapons.java`.

Commits:
- 2ca1dd070546e019acbad79c8572559823eaebc7
- ac3c79e0decee97c85c9e5e4658ad14eff52af89
- c58d3f618ea8bc478228a149cfb3ea1bd7fabf66
- 84901547c5e37e592391234f392b014ad420d85d

Validation: Source changes were checked for exact unique replacement matches before committing. The branch CI workflow (`.github/workflows/weapon-skill-input-standard.yml`) is configured to run on push and executes `tools/weapon_skill_input_check.py` and `./gradlew clean build --no-daemon`. A completed CI result was **not available** when writing this report. Local Gradle build, GameTests, and Minecraft gameplay testing were **not performed** in this environment. All of those remain open before claiming release readiness.
