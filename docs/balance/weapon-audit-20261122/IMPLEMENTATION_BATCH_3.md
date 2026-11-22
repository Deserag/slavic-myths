# Slavic Myths — weapon progression balancing, batch 3 (2026-11-22)

**Branch:** `feature/weapon-skill-input-standard-20261122`. No branch created; batches 1 and 2 preserved.

## Prerequisite CI and source state

Workflow run [45012793472](https://github.com/Deserag/slavic-myths/actions/runs/45012793472) completed **successfully** on 2026-11-22 at 10:33 UTC. The 102-item catalog/input check and the compile-and-test job succeeded. The previous `IMPLEMENTATION_BATCH_1.md` and `IMPLEMENTATION_BATCH_2.md` exist in this branch, as do the previously changed `WeaponCatalog.java` and `PoolSpear.java`. The inventory `CATALOG.md` and `catalog.json` remain historical baselines, not refreshed balance values.

## Targeted changes

All damage numbers are **source-derived intended values before armor/mitigation**, not in-game measured DPS.

| Weapon | Acquisition/progression | Base hit before | Base hit after | Other changes | Justification |
|---|---|---:|---:|---|---|
| `karachun_scythe` — Коса Карачуна | Late boss/trial reward | 12 | 16 | Native scythe attack attribute +4; existing special strikes use main-hand attack value, so scale accordingly | 12 damage at 1 attack/s was low for a late trial boss (Karachun configured with 1600 HP). The combo, lifesteal, undead bonus, soul effects and windups remain untouched |
| `morozko_staff` — Посох Морозко | Rare reward from Morozko trial (staff roll 15%, boss configured at 1400 HP) | 8 | 8 | Player bolt 8 → 11; falling icicles 5 → 8 each; player frost wave 4 → 7; cooldowns unchanged (16/400/320 ticks respectively) | Improve slow/conditional magical effects against high-health enemies without changing freeze, control or targeting |
| `morovoy_staff` — Посох Мора | Composite high-level staff crafted from boss-related components | 7 | 7 | Standard mor/fire bolts 8/11 → 11/14; flock cooldown 900 → 700 ticks (45 → 35 s); fire-mor area cooldown 800 → 600 ticks (40 → 30 s); tooltip numeric text synchronized | Existing AoE hit for 12 damage was limited by a 40-second cooldown, summon by 45 seconds. Increase practical uptime while preserving area target cap (12), two elemental modes and debuffs |
| `pharaohka_trident` — Трезубец Фараонки | Rare crafted marine/boss-related weapon | 9 | 9 | Tidal wave damage to secondary targets 4 → 8, 240-tick cooldown unchanged, 5-target cap unchanged | Secondary AoE was weak relative to cost and 12-second cooldown; ordinary trident attack and throw stay distinct |

These targeted changes intentionally leave unrelated items, armor, mob health, boss drops, recipes, game progression, enchantments, runes, animations and LMB/RMB/Shift weapon skill bindings unchanged.

## Other priority rewards reviewed and retained

| Weapon | Code-derived attack | Reason not to buff in batch 3 |
|---|---:|---|
| `nine_mor_spear` — Копьё Девяти Пастей | 11, 2 attacks/s | Four ghost projectiles (each initialized at 9), circular sweep and short cooldowns create strong hybrid combat |
| `nine_mor_axe` — Секира Погибели | 27, 0.5 attacks/s | Intentionally slow massive hit; abilities deal 16/20, execution deals 40–100 based on target HP |
| `mytar_shackled_sickle` — Оковный серп | 21, 1.3 attacks/s | Chain pull and throw (tooltip 24 damage), rupture (36 + wave 14); significant boss/area utility |
| `neveya_scythe` — Коса Невеи | 22, 1.2 attacks/s | Charged attacks 35/53, plus other effects and high-tier stats |
| `lomeya_hammer` — Молот Ломеи | 21, 1.4 attacks/s | Charge 24, dual charge 32/44 plus additional strike mechanics |
| `poloz_whip` — Плеть Полоза | 13, 1 attack/s | Sweeping hits and fissure/spike utility (spikes 10), plus optional rune interactions |
| `nightingale_dagger` — Кинжал Соловья | 10, 2 attacks/s | 20 nominal melee DPS; wind projectile for utility |
| `victor_spear` — Копьё победителя | 12, 1.2 attacks/s | Range plus conditional second hit window and cooldown, already compelling |
| `tugarin_sword` — Парные клинки Тугарина | 10, 1.8 attacks/s | Paired 1.4x fully charged multiplier and bleed effects |

This is an assessment from code and mechanics, **not proof of measured in-game effectiveness**. Additional named boss rewards outside this priority group may need a later review. In particular the `karachun_scythe` special damage includes configurable `NightConfig.baseDamage`, which may affect effective numbers on servers with non-default configs.

## Changes committed to GitHub

- [845c3bb](https://github.com/Deserag/slavic-myths/commit/845c3bbd80aa6ea975436d01b912cd0eb18b1e08): `karachun/NightScythe.java`
- [162eec6](https://github.com/Deserag/slavic-myths/commit/162eec6504437ab28da75074571df8516a1e9099): `morozko/FrostSkills.java`
- [7e3a129](https://github.com/Deserag/slavic-myths/commit/7e3a129cc7fe7b49cea2f0f0479e16f4badff6b0): `morozko/FrostHazard.java`
- [de6a1c6](https://github.com/Deserag/slavic-myths/commit/de6a1c68e9e4f0266297acba7b01c323586963ce): `morovoy/MorovoyStaff.java`
- [3c5a2a1](https://github.com/Deserag/slavic-myths/commit/3c5a2a13564d022ada52efe9a0eb74be0733a253): `marine/PharaohkaTrident.java`

## Verification and open risks

- Previous batch CI **PASS**, run 45012793472.
- Batch 3 GitHub Actions clean-build jobs were triggered automatically by code pushes. The latest code-commit workflow is [45013064968](https://github.com/Deserag/slavic-myths/actions/runs/45013064968). At initial report preparation it was in progress; verify its final conclusion before declaring batch 3 compilation green.
- Every replacement was exact-match checked against the current file version; affected source code was reread. These checks do not replace compilation or GameTests.
- No client-side Minecraft playtest or in-world GameTests performed in this session, including cooldown/target interactions or multiplayer.
- Candidate follow-up: evaluate `likho_staff`, `ataman_flail`, `trophy_kalin_sabre`, `finist_bow` and other rewards against their true acquisition costs and actual skill uptime, using the existing catalog rather than rebuilding it.
