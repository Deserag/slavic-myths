# Slavic Myths — weapon balance implementation, batch 5 (final, 2026-11-22)

Branch: `feature/weapon-skill-input-standard-20261122`. Previous four batches are preserved.

## Prerequisite checks

GitHub Actions run [45013991063](https://github.com/Deserag/slavic-myths/actions/runs/45013991063) completed **successfully**. Its `weapon-input` job passed the 102-item catalog/input validation and the configured `Compile and test` step. This is CI verification, not an in-game Minecraft playtest.

## Changes

Source-derived nominal attack damage, before armor, resistances, target conditions, potion effects, enchants or hit timing.

| Weapon | Normal hit before | Normal hit after | Special change | Reason |
|---|---:|---:|---|---|
| `diamond_flail` | 9 | 10.35 | Charged: 10 → 11.5 | Costly diamond assembled weapon at only 0.7 attacks/s compared poorly with diamond alternatives |
| `zhara_flail` | 10.35 | ~14.49 | Charged: 11.5 → 15.5 | Nether-hardened flail previously weaker than an upgraded netherite flail (13 nominal hit); smithing default components were overriding constructor attributes |
| `berdysh` | 9 | 11 | Speed retained: 0.7 attacks/s | Heavy, slow iron poleaxe needed more impact than faster early alternatives |
| `battle_axe` | 10 | 11 | Speed retained: 0.7 attacks/s | Improve slow iron axe hit while keeping slow tempo and axe identity |
| `thunder_spear` | 7.5 | 9.5 | Existing +2 discharge (120-tick cooldown), +1.25 interaction reach and knockback retained | After the earlier perunite spear increase to 8.5, the rarer thunder spear needed better normal damage |

Zhara flail final damage is calculated as the *new* diamond-flail source attribute 10.35 × a flail-specific Nether smithing multiplier 1.40 = 14.49. The charge is independently controlled by `FlailItem.chargedDamage()`: diamond tier base 10 × 1.55 = 15.5. Non-flail smithing multiplier stays at 1.15; source attributes are transformed by `NetherSmithingRules` in `ModifyDefaultComponentsEvent`. The separate multiplier keeps `zhara_sword`, `zhara_axe`, `zhara_spear`, `zhara_sulitsa` and `zhara_throwing_knife` at their existing balance.

## Reviewed, unchanged

- `veles_staff`: Despite modest melee (4 at 1.2/s), summons either a wolf (80% selection) or a bear (20%) for 1200 ticks (60 s), with a 2400-tick (120 s) cooldown, one durability damage per successful summon, 48 maximum durability. The guardian follows the owner's attack/retaliation targets, seeks its target and strikes on a limited timer, and is dismissed after its lifespan. Combat value should be measured in-game before modifying guardian damage or cooldown. Its base melee DPS is not a meaningful summon balance metric.
- Existing earlier buffs to Netherite/Perunite flails and the boss rewards were retained; no changes to enemy health, recipes or weapon skill routing.

## Remaining risk and scope

A risk-based cross-check against the prior catalog and four batch reports does not justify indiscriminate buffs to all 102 ID entries. The catalogue includes shields, arrows, bows and staff items with default melee values that **do not measure their ability damage**. The pre-balance `CATALOG.md` and `catalog.json` were not regenerated; they remain comparison baselines. Late boss weapons with high base damage and strong attacks intentionally remain unchanged.

Potential issues requiring Minecraft gameplay measurements rather than another speculative numeric pass: actual damage versus armor/resistances, charged flail activation at range, large targets and boss resistance, Veles summoned guardian pathfinding and attack frequency, reach/knockback and player attack intervals, rune/skill interactions and performance against Nether groups.

## Code commits

- [e62ff514](https://github.com/Deserag/slavic-myths/commit/e62ff51461ebd023b0500a8a6199dee01ccfc5dc) — diamond flail: `combat/WeaponCatalog.java`
- [cdd5d280](https://github.com/Deserag/slavic-myths/commit/cdd5d280e565679e8b43d570be8aed341f855219) — smithing multiplier overload: `nethersmithing/NetherSmithingRules.java`
- [a0a1bec7](https://github.com/Deserag/slavic-myths/commit/a0a1bec7908d4ab76df92096dda54e3851536516) — zhara flail: `nethersmithing/NetherSmithing.java`
- [f9293964](https://github.com/Deserag/slavic-myths/commit/f92939642a4bf676d938e849e5b794232ec61679) — heavy weapons and thunder spear: `registry/ModItems.java`

## Validation

Batch 4 prerequisite CI succeeded. Batch 5 pushes have triggered GitHub Actions and need final-run confirmation after documentation commits. The CI workflow performs 102-item source checks and `./gradlew clean build --no-daemon` with Java 21. It does not prove the weapons work in game. No manual Minecraft test or verified in-world GameTests were run in this session.
