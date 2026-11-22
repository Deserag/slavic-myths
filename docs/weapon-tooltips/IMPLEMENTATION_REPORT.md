# Slavic Myths — weapon tooltip implementation report (2026-11-22)

**Branch:** `feature/weapon-skill-input-standard-20261122`; continued in place, without creating a new branch.

## Scope and implementation

- The catalog-based client handler `src/main/java/org/slavicmyths/client/WeaponTooltips.java` applies a consistent title palette to all **102** existing weapon-related IDs, including mundane weapons, rare/epic boss weapons, bows, shields and ammunition. It does not recolor foreign items or overwrite custom anvil names.
- Descriptive lines use distinct colors for controls, numeric values, ability descriptions, cooldowns and positive effects. The client-specific handler does not change damage, AI, item components, drops or combat input mappings.
- Main color palette: normal `#E4E4E7`, rare `#55FFFF`, epic `#AA55FF`; elder serpent `#FFAA00`; elder status `#D4AF37`; skill `#FFAA00`, damage `#FF5555`, positive `#55FF55`, controls `#FFFF55`, supporting text `#AAAAAA`.
- Selected named boss-weapon Java tooltip strings have been migrated to `Component.translatable` and matching `ru_ru.json`/`en_us.json` entries. Implemented in `ninemor/NineMorNamedItems.java`, `sisters/BossRewardWeapons.java`, `posad/boss/ShackledSickleItem.java`, `rewards/RewardContent.java`, `item/StormStaffItem.java` and the Morovoy staff item names. Existing locale content and unaltered weapon descriptions were preserved.
- Other weapon classes with correct descriptions were left unchanged; generic non-skill weapons do not receive artificial ability paragraphs. A historical 102-entry catalog provides scope, not the current numeric values after balance batches 1–5.

## Verified golden elder serpent weapons

| Boss | Golden weapon IDs | Evidence |
|---|---|---|
| Zmey Gorynych | `serpent_crossbow`, `three_breath_crossbow` | `gorynych/GorynychContent.java` and crossbow reward/material system |
| Greater Morovoy Fire Zmey | `morovoy_staff` | Morovoy staff crafting consumes the higher serpent's materials |
| Ocean Zmey | `storm_anchor`, `storm_harpoon` | Marine weapon recipes consume `ocean_zmey_storm_heart` |

All five display `#FFAA00` when not renamed by the user, plus translated `#D4AF37` status text. Materials, decorative trophies, armor and equipment from other bosses were not promoted to the golden-weapon class.

## Localization and verification

- Client presentation: `src/main/java/org/slavicmyths/client/WeaponTooltips.java`.
- Locales: `src/main/resources/assets/slavicmyths/lang/ru_ru.json` and `en_us.json`.
- New static source/translation check: `tools/weapon_tooltip_style_check.py` verifies all 102 IDs, 5 golden weapons, approved colors, custom-name exclusion, localized keys and duplicate-key policy.
- The GitHub Actions workflow `.github/workflows/weapon-skill-input-standard.yml` now runs the new check alongside prior input-routing checks, then Java 21 `./gradlew clean build --no-daemon`.
- During validation, earlier workflow attempts detected duplicate historical English translation keys. Those keys were consolidated in commit `9837be157179db5ab1473a172f5abf2d41a8de8c`; the latest GitHub CI result must be checked before confirming success.

## Code changes

Relevant new commits in the working branch:

- Tooltip coverage and color formatting: `src/main/java/org/slavicmyths/client/WeaponTooltips.java`
- Per-line token colors (controls, numeric damage): [3438fa46](https://github.com/Deserag/slavic-myths/commit/3438fa46827b963243fb3cb3250e8fc81f02da00)
- Style guard: `tools/weapon_tooltip_style_check.py`
- Bilingual tooltip strings: named weapons in `ninemor/`, `sisters/`, `posad/boss/`, `rewards/`, `item/`
- Both locale JSONs and stale duplicate translation keys
- Unified standard: `docs/weapon-tooltips/WEAPON_TOOLTIP_STANDARD.md`

Find the full commit history on the branch: https://github.com/Deserag/slavic-myths/commits/feature/weapon-skill-input-standard-20261122

## Tested vs. not tested

Static checks and CI builds establish source consistency and compilation only when completed successfully. **No in-world Minecraft client review has been performed** for long tooltip wrapping, actual rendered name colors, anvil names, RPG effects, resource packs, or multiplayer.

Remaining review before release: visually inspect a plain item, one rare and one epic, one weapon from each of the three elder-serpent groups, a renamed weapon, a charged ranged item, and both Russian and English client locales. Verify ability activation, damage values and cooldowns in game, especially dynamic rune bonuses. If discrepancies remain, make **text-only** fixes rather than changing balance.

This report covers weapons only; armor and accessories are deferred to a separate task.


## Additional completed localization and coverage

- `morovoy/MorovoyStaff.java`: both elemental modes' active skill titles, damage, cooldown and `Shift + ЛКМ` / `Shift + ПКМ` combinations moved to RU/EN keys; conductor and staff display names now use translatable components ([0d5ca6c](https://github.com/Deserag/slavic-myths/commit/0d5ca6c1422aa7c660d6c1a073bfe3a28cb77eef), [dbbd32f](https://github.com/Deserag/slavic-myths/commit/dbbd32fc89861f307fb60855335ede8bfff335fd)).
- `nethersmithing/InfernalBow.java`: both standard and enhanced bows use translated descriptions for draw duration, basic projectile damage, burning/slowness and charged skill rather than fixed Russian text ([50a03f2](https://github.com/Deserag/slavic-myths/commit/50a03f2ad7bb313a65e40b3df14a362b83acf1f1)).
- `sisters/BossRewardWeapons.java` and `posad/boss/ShackledSickleItem.java`: dynamic rune slot counts use `Component.translatable` instead of a Russian literal ([fb5189f](https://github.com/Deserag/slavic-myths/commit/fb5189fbcd89974e78d6ac329614bee0355bb734), [4b11339](https://github.com/Deserag/slavic-myths/commit/4b1133970ff0bb6c17d521705650f199b6bd4020)).
- Missing catalog item names were added to both locales and validated with the strict automated checker; full per-ID coverage is recorded in [`CATALOG_COVERAGE.md`](CATALOG_COVERAGE.md) ([1d7103c](https://github.com/Deserag/slavic-myths/commit/1d7103ce3f0d7a04824bfc9249f765f9c89e035d)).
- All five golden weapon IDs, all 102 standard item names in both languages and duplicate key detection are asserted in CI.

The formatting standard remains **presentation-only**. No manually measured Minecraft-client screenshots or multilingual in-game layouts were produced. Legacy weapon classes not explicitly listed above still use their existing ability text, which the client formatter presents with a common semantic palette; a full dynamic skill-text audit remains a release QA concern.

## Post-balance numeric tooltip reconciliation

Additional review of both language files against current Java mechanics found and corrected these stale displays:

- `combat.slavicmyths.likho_tooltip` in Russian: ability cooldown **28 → 22 s**, matching the earlier weapon-balance commit (English was already correct).
- `frost191.staff_stats` in Russian and English: Morozko staff projectile **7 → 11**, icicles clearly described as **8 damage each**, and wave **4 → 7**. Stream timing remains unchanged.
- `book.slavicmyths.pool_spear.text` in Russian and English: water-only charged strike **9 → 12** after balance batch 2. The guide now matches the source code, not the historical catalog.

Changes made only in localization, with no combat code or key mapping modifications. Relevant commits: [bc9c8ae](https://github.com/Deserag/slavic-myths/commit/bc9c8aee99475da155f308b48edb396591f8d180), [bf12bb3](https://github.com/Deserag/slavic-myths/commit/bf12bb3b573ed6a03d0ab85a54fd70c0b39b159d), [b847e9d](https://github.com/Deserag/slavic-myths/commit/b847e9de4fd369692f6c04def135e074ca329742), [67a4580](https://github.com/Deserag/slavic-myths/commit/67a45809df14374e541966c11ff283aa324f1c0a).
