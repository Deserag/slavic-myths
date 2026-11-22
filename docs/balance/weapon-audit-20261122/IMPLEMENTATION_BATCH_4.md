# Slavic Myths — weapon balance implementation, batch 4 (2026-11-22)

Working branch: `feature/weapon-skill-input-standard-20261122`. Continuation of batches 1–3; their source changes and historical audit are retained. No new branch created.

## Prerequisite

Batch 3 CI: [45013148878](https://github.com/Deserag/slavic-myths/actions/runs/45013148878) — **success**. Source files and batch reports were inspected on the existing branch. `catalog.json` and `CATALOG.md` are historical before-values, **not** an authoritative list of the current after-values.

## Implemented changes

Damage and DPS numbers below are nominal/source-derived, before mitigation and conditions, and are not measured in Minecraft.

| Weapon | Acquisition/progression | Before | After | Ability changes and reasoning |
|---|---|---|---|---|
| `likho_staff` (Посох Лихо) | Rare weapon assembled with Oko Likha and two threads of ill fate from late boss progression | Basic hit 10, 1.2 hits/s; morok cooldown 560 ticks / 28 s | Basic hit 10, 1.2 hits/s; morok cooldown 440 ticks / 22 s | The distinctive 12-block single-target disorientation/control remains, including 25-tick stun where permitted, 120-tick follow-up effect and weaker effects on resistant targets. Shorter cooldown improves usefulness without increasing control strength or changing targeting |
| `ataman_flail` (Кистень атамана) | Ataman sign + diamond flail + armorer components | Basic hit 12, 0.7 hits/s, nominal 8.4 DPS | Basic hit 14, 0.7 hits/s, nominal 9.8 DPS | Three-hit charged-combo bonus 1.35× and shield interruption kept; slow heavy weapon better rewards scarce boss-component investment |
| `trophy_kalin_sabre` (Сабля Воеводы) | Kalin-related rare trophy weapon | Basic hit 9 at 1.3 hits/s; special sweep 5 per target | Basic hit 9 at 1.3 hits/s; special sweep 9 per target | Sweep remains directional, max 6 targets, range 5.5 blocks, cooldown 140 ticks / 7 s. 5 was insufficient versus high-health opponents |
| `thunder_axe` (Громовой топор) | Rare thunder/perunite weapon | Basic hit 8.5 at 0.9 hits/s; proc +1 damage | Basic hit 11.5 at 0.9 hits/s; proc +2 damage | Perunite axe from batch 1 now deals 10.5; this rarer weapon should not be strictly worse outside a modest occasional +1 proc. Chance remains 20%, proc cooldown 60 ticks, visual-only lightning effect; no new bonus hits |

## Reviewed, left unchanged

- `finist_bow` (Лук Финиста): bow uses 30-tick charge curve, 1.4× regular arrow base damage, enchanted/magical pairing, optional quiver ammunition and guided arrows with target visibility; no evidence from source alone that it is underpowered compared to much earlier bows. The catalog's nominal 1/4 melee attributes do not represent bow damage.
- `storm_staff` (Грозовой посох): already strengthened in batch 2; pulse 11 damage, short 32-tick (or rune 28-tick) cooldown, 7-block range, path/rune multipliers. Avoid duplicate adjustment.
- `three_breath_crossbow` / `serpent_crossbow`: the special version fires a three-projectile volley with mode-dependent effects and burst controls. Raw melee attributes are irrelevant; further changes require projectile effectiveness checks.
- `tugarin_sword`, `victor_spear`, `nightingale_dagger`, `neveya_scythe`, `lomeya_hammer` and Nine Mor weapons: examined in batch 3, already show significant hit and/or special attack power; do not buff without gameplay evidence.
- `thunder_spear`: extended reach, knockback and its fast spear identity provide non-damage value; not enough evidence for another adjustment here.

## Code and commits

- [af4fe461](https://github.com/Deserag/slavic-myths/commit/af4fe4616043c8732f9b3422098eb2d8c9f8bdc6): `combat/LikhoStaffItem.java`
- [e4aa0d0](https://github.com/Deserag/slavic-myths/commit/e4aa0d0ca72bd814f5518369a7d53ea9ab51009d): `combat/RareWeapons.java`
- [07af041](https://github.com/Deserag/slavic-myths/commit/07af0414a45ffbe571eb2ba1ae0d03b78501be39): `rewards/RewardEvents.java`
- [5c53199](https://github.com/Deserag/slavic-myths/commit/5c5319986b5d4672adc3979897cb6d3ad2d6722f): `item/ThunderAxeItem.java`
- [7af7d7b](https://github.com/Deserag/slavic-myths/commit/7af7d7bc9495e075ba160351c7d98f6e838e4733): `assets/slavicmyths/lang/en_us.json` — update existing Likho tooltip numbers 28 → 22 s

All edits were performed as unique text replacements against current branch file blobs, without changes to creature health, armor, acquisition, recipes, input routing or item visual descriptions. Existing English tooltip texts were adjusted only to reflect the new numeric cooldown.

## Short residual review of the 102-item catalog — need batch 5?

**Yes: recommend a focused fifth batch, not another complete audit.** Reviewing historical catalogue figures against known buffs still leaves possible progression gaps. The highest-value outstanding candidates are:

1. `zhara_flail`: historical nominal 10.35 damage at 0.7 attack/s (7.245 DPS), unusually low for Nether-hardening smithing compared to the already enhanced netherite flail (13 damage, 9.1 DPS). Verify latest smithing attribute overrides, charged damage and how this recipe compares to netherite before modifying.
2. `diamond_flail`: historical 9 damage at 0.7 attacks/s; verify charged damage versus diamond spear/throwing weapon alternatives.
3. `berdysh`, `battle_axe`: historically low 0.7 attacks/s, could be deficient compared to comparably available weapons without skill compensation.
4. `veles_staff`: historically low 4 damage at 1.2 attacks/s, but summon mechanics may justify it; check damage, summon duration, targeting and restrictions before any adjustment.
5. `thunder_spear` versus current perunite/netherite equivalents: verify actual 1.25 reach bonus and control/proc advantages in game before changing numbers.

The historical catalog includes shields, ammo, bows and magic staves whose 'melee DPS' does not reflect their real gameplay damage. Thus this is a **prioritized remaining-risk list**, not a new full 102-item audit or an assertion that the listed items definitely require buffs.

## Validation

- Prior batch 3 CI: **success**, run 45013148878.
- New GitHub Actions workflow should run after each push to this branch; see the branch Actions run for the final documentation commit. Confirm final **success** before declaring batch 4 build green. Local `./gradlew clean build --no-daemon` was not run in this session.
- No Minecraft in-world manual testing or GameTests have been run as part of this task session. Static source review does not establish gameplay pass.
