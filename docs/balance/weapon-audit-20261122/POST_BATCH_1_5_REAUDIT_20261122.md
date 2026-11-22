# Slavic Myths — independent re-audit of weapon balance batches 1–5 (2026-11-22)

**Read-only code/balance audit:** no combat changes made. The task of a subsequent implementation session is to resolve confirmed issues and measure conditional risks, not to apply indiscriminate buffs. Audited working branch: `feature/weapon-skill-input-standard-20261122`; source snapshot at audit start `71c6d22c1d9a645060c605f7cc1e9e5f9cb44224` (later changes on branch must be rechecked).

## Scope and methodology

- Reviewed all five `IMPLEMENTATION_BATCH_*.md` files, `FINAL_BALANCE_SUMMARY.md`, and the original `catalog.json` (102 IDs), plus Java definitions / handler classes for affected weapon families and key boss rewards. The 102-ID catalog is a **historical pre-balance snapshot** and contains shields, ammunition, bows, a dedicated offhand hammer and magical tools; it is **not** 102 independently DPS-comparable melee weapons.
- Five batches targeted approximately **25 distinct weapon IDs**, not all 102 individually. This re-audit is source-level risk triage, **not** an exhaustive runtime equipment test. Source-level damage calculations are nominal before armor, invulnerability frames, charge limits, runes, etc.
- Priority is based on objectively verified inconsistencies, then gameplay/progression hypotheses. Do not confuse a hypothesis with a confirmed bug.

## Confirmed issues — resolve first

### P0 / B01 — Kalin Warlord Saber underpowered for its encounter and reward rank

- Current `rewards/RewardContent.java`: `trophy_kalin_sabre` damage 9, attack speed 1.3; one RMB special `RewardEvents.sabreWave`, 9 damage per affected target, max six, 5.5-block reach, 140-tick cooldown. The fourth batch increased only sweep 5→9.
- Current `combat/RareWeapons.java` + `combat/RareCombat.java`: `tugarin_sword` damage 10, speed 1.8, conditional 1.4× fully charged hit and bleeding; `bandit/NightingaleDagger.java`: 10 damage, 2.0 attacks/s and wind-wave utility.
- Nominal plain melee DPS: Kalin 11.7 vs Tugarin 18 and Nightingale 20 (respectively ~54% and ~71% higher than Kalin), without accounting for Tugarin bleed.
- Boss context: `kalin/KalinEntity.java` exposes a fixed max HP pool of 1800, multiple phases, mounted state and soldiers; `hunt/BossKind.java` Tugarin HP 400; `bandit/NightingaleEntity.java` Nightingale HP 350. This is **not** a complete difficulty ranking but strongly warrants a review of reward strength.
- `docs/specs/1.7.6/03_Boss_Trophies/14_trophy_kalin_sabre.md` defines a three-stack kill-based Momentum mechanic, unlike current RMB wave. `tools/verify_weapons87_kalin_wave.py` intentionally asserts Momentum is absent: it was an explicit earlier implementation divergence, not merely forgotten code. **User has explicitly required at least two distinct substantive abilities for the Warlord Saber.** Implement a second meaningful skill while retaining the existing wave unless incompatible; evaluate the prior Momentum concept against present code and choose a compatible design. Do not blindly restore deprecated logic that the existing test explicitly prohibits. Update that test to match the final intended two-skill specification and avoid asking the owner again for requirements already provided.
- Action: agree intended 2-skill design and progression target; review melee damage, attack rate, abilities and interactions with knockback/cooldown/runes/PvP; implement independent tests for both skills, no fictional stat claims.

### P0 / B02 — Batch 3 Morozko projectile 'damage buff' actually changed cold buildup, not damage

- `morozko/FrostShard.java`: `launch(LivingEntity,Vec3,float damage,int cold,UUID)`.
- `morozko/FrostSkills.java`: `FrostShard.launch(p, ..., 7, 11, null)`: **7 direct damage, 11 cold**, not 11 damage.
- The actual batch-3 commit [`162eec650`](https://github.com/Deserag/slavic-myths/commit/162eec6504437ab28da75074571df8516a1e9099) explicitly replaced old `...,7,8,null` with `...,7,11,null` and documented “player bolt 8→11 damage” in `IMPLEMENTATION_BATCH_3.md` and `FINAL_BALANCE_SUMMARY.md`. That documentation is factually incorrect. The change increases cold buildup 8→11 instead.
- `morozko/FrostCold.java`: cold buildup affects slow/weakness tiers and conditional aura damage; therefore it is **not** a harmless numerical typo.
- `morozko/FrostHazard.java`: separately, player wave damage currently 7 and falling icicles damage 8; these are correct distinct source fields and must **not** be mixed up with cold values.
- Action: check expected projectile damage against encounter and other skills; decide whether to preserve/revert raised cold, and whether actual direct damage 7 needs a change. Add a test that checks damage and cold parameters separately. Correct reports and any incorrect tooltips.

### P1 / B03 — Kalin stale descriptions conflict with implemented behavior

- `assets/slavicmyths/lang/en_us.json` includes `item.slavicmyths.trophy_kalin_sabre.desc` describing the old Momentum three-stack mechanic, while `tooltip.slavicmyths.kalin.sabre` correctly describes current RMB cleave.
- `compat/JeiRunes.java` registers `item.slavicmyths.<reward-id>.desc` as ingredient information; a player using JEI may read an obsolete skill description.
- Action: once intended kit B01 is agreed, reconcile RU/EN main tooltip, JEI text, specs, test assertions, and numeric labels **together**, rather than patching only one localization key.

## Material follow-up risks — validate before changing stats

### P1 / R04 — Charged flail incremental payoff is small and uses a separate damage path

- `combat/FlailItem.java`: charges require at least 18 ticks (0.9 s), 4-block ray, cooldown 40 ticks (2 s); special deals `chargedDamage()` through direct `target.hurt(playerAttack, amount)` rather than native `Player.attack` path. This can affect interactions with enchantments, attack-strength scaling and downstream proc timing; do not assume identical handling.
- After batches: diamond flail normal 10.35 vs charged 11.5; Netherite 13 vs charged 14.3; Zhara normal approximately 14.49 vs charged 15.5. The added damage is only ~1–1.3 over a normal hit, but 4-block range, defensive utility and shield interaction may compensate. **Not a proven weakness** without combat testing.
- `nethersmithing/NetherSmithing.java` applies a flail-specific 1.4 attribute multiplier via default components, while charged damage is separately from tier and constructor multiplier 1.55. Confirm actual damage and compatible runes/enchants on charged vs normal hit.
- Action: compare time-to-kill, effective reach and buffs/enchants; decide whether charge needs changed scaling or explanation; do not remove flail identity.

### P1 / R05 — Likho vs Tugarin boss-tier reward comparison unclosed

- `hunt/BossKind.java`: Likho 350 HP, Tugarin 400 HP, similar hunt boss tier. `combat/LikhoStaffItem.java`: base 10, speed 1.2, 12-block control with 440-tick (22 s) cooldown; control-resistant targets get weakness/slowness rather than stun. `tugarin_sword`: 10, 1.8 attacks/s plus charged damage/bleed.
- Their damage is not comparable by melee alone because Likho has ranged control and self-healing. However, bosses may resist that control; the next session should test **boss-effective** value before declaring Likho balanced.
- Action: document encounter difficulty, stun immunity and health recovery, compare representative targets, only buff if real deficit is demonstrated.

### P1 / R06 — High-health reward progression comparison was omitted as a system

- Prior batches primarily picked unusually low absolute numbers, not a full reward-tier comparison. Include `karachun_scythe`, `morozko_staff`, `morovoy_staff`, `pharaohka_trident`, `nine_mor_spear`, `nine_mor_axe`, `mytar_shackled_sickle`, `neveya_scythe`, `lomeya_hammer`, `poloz_whip`, `victor_spear`, `finist_bow`, `tugarin_sword`, `nightingale_dagger`, and `trophy_kalin_sabre`.
- Compare stage/cost, encounter HP/defenses, skill count and usefulness, nominal single-target DPS, crowd damage, cooldown and utility. Don't demand two skills for every item: the Kalin two-skill expectation is user-specific. Avoid forcing late-tier weapons to match without stage evidence.
- Action: a compact progression matrix (not a speculative mass buff), flag only reproducible deficits; report independent P1/P2 candidate fixes.

### P2 / R07 — Pharaohka trident has at least two wave-like paths with different damage

- `marine/PharaohkaTrident.java`: thrown-trident impact wave targets take 8 secondary damage (12 s cooldown).
- `marine/MarineWeaponAbilities.java`: Shift+RMB frontal ability deals 6 damage to targets in a distinct 8-block cone, also with 240-tick cooldown tracked under a **different key**. Not necessarily a bug, but if descriptions present one generic 'Tidal Wave' number, users may infer the wrong value and trigger distinct cooldowns.
- Action: check exact source/label for both modes, separate cooldowns and input interactions in game; avoid merging them without design review.

### P2 / R08 — Catalog/CI coverage overstated when used as balance evidence

- `tools/weapon_skill_input_check.py` checks 102 **IDs**, input matrices, and token presence in a limited set of input classes; it does **not** assert 102 per-item DPS comparisons, actual hit damage, boss tier, skill effectiveness or runtime visuals.
- The catalog includes `lomeya_hammer_offhand` as a separate ID, plus shields, ammo, bows, staves; e.g. `sisters/BossRewardWeapons.java` uses the second Lomeya hammer as an actual offhand prerequisite for dual abilities, not an independent normal attack DPS category.
- Batch reports were truthful about no manual tests but `FINAL_BALANCE_SUMMARY.md` still concluded the software phase complete; that conclusion should be narrowed to “CI green, play-balance unverified.”
- Action: define separate pass criteria for code compilation, static input routing, source parameter checks and in-game playtests. Introduce targeted unit/simulation checks for corrected damage/skills.

## Review of all five balance batches (do not undo by default)

| Batch | Weapon IDs changed | Re-audit disposition |
|---|---|---|
| 1 | `perunite_sword`, `perunite_axe`, `perunite_mace`, `perunite_spear`, `perunite_flail`, `netherite_spear`, `netherite_flail`, `navya_ostroga`, `shadow_throwing_knife` | No directly confirmed source-parameter mistake in this spot-check. Charged flail path R04 must be playtested |
| 2 | `abyss_staff`, `storm_staff`, `pool_spear` | Source currently shows 8 pulse for Abyss, 11 pulse for Storm, and 11 melee / 12 wet charge for Pool. No confirmed error; verify control/target caps in game |
| 3 | `karachun_scythe`, `morozko_staff`, `morovoy_staff`, `pharaohka_trident` | **Confirmed Morozko parameter error B02**; review relative endgame tiers and the separate Pharaohka ability paths R07 |
| 4 | `likho_staff`, `ataman_flail`, `trophy_kalin_sabre`, `thunder_axe` | **Confirmed unacceptable Kalin comparative gap B01 and stale localization B03**; Likho R05 remains balance risk |
| 5 | `diamond_flail`, `zhara_flail`, `berdysh`, `battle_axe`, `thunder_spear` | No confirmed incorrect source value found; charged flail scaling R04 merits validation |

**Important limitation:** A row “no confirmed bug” means none was established in this source spot-check; it is **not** a certification of every aspect of the listed weapon in Minecraft.

## Next session — required order

1. Pull current head of `feature/weapon-skill-input-standard-20261122`, compare with audit snapshot; inspect latest CI. Preserve recent tooltip/localization work.
2. **B01:** agree intended Kalin 2-skill kit, main damage/speed and boss-tier target against Tugarin/Nightingale; implement safe code + tests, adjust docs and localization.
3. **B02:** correct Morozko damage-vs-cold parameter mix-up, test the direct damage and cold independently, repair third-batch and final report claims.
4. **B03:** remove stale Kalin Momentum description only after gameplay kit decision; audit RU/EN/JEI tooltip references.
5. **R04–R07:** gather reproducible stage-based / in-game evidence and make only clearly justified changes.
6. Add targeted tests for key abilities and run `./gradlew clean build --no-daemon` / GitHub Actions, then perform Minecraft in-game tests if available; report unperformed test types explicitly.

This file records **audit findings only**. No gameplay/source changes in this audit. Success of GitHub Actions is not proof of balance.

## Extended recheck — concrete source references and audit caveats

### E01: Evidence on source snapshot, not just notes

- `kalin/KalinEntity.java` `getMaxHealth()` returns 1800; `hunt/BossKind.java` sets Tugarin HP to 400; `bandit/NightingaleEntity.java` sets Nightingale HP to 350. Their health totals differ substantially, but survivability and encounter complexity also depend on armor, mechanics, helpers and progression. Compare effective combat difficulty, not only HP.
- `combat/RareCombat.java` gives Tugarin +40% charged-attack damage and applies stacking bleeding after fully charged melee hits. `rewards/RewardContent.java` has no analogous event/charge logic for the Kalin saber, only its right-click sweep. The saber's current balance gap is source-verifiable.
- The former Momentum text still exists in English `item.slavicmyths.trophy_kalin_sabre.desc`, even though live `tooltip.slavicmyths.kalin.sabre` describes the new sweep. `compat/JeiRunes.java` directly exposes the generic `.desc` translation for reward items through JEI ingredient info.
- The updated official `.github/workflows/weapon-skill-input-standard.yml` runs catalog/input routing, tooltip palette/localization checks, and `./gradlew clean build --no-daemon`. Neither route is an automated comparative DPS / reward-tier test.
- `combat/FlailItem.java` uses a separate `LivingEntity.hurt` call for charged impact after resetting player attack strength. This is a specific test target for enchantment, cooldown and crit behavior. The charge numbers in R04 are not intended effective damage measurements.

### E02: What should *not* be reported as established failures

- `karachun/NightScythe.java` sets base nominal melee to 16; `karachun/NightCombat.java` derives special damage from attack attributes plus configurable `NightConfig.baseDamage`. Its final numerical output varies with configuration, enchantments and target type: lack of a static fixed skill damage number is not itself a bug.
- `morovoy/MorovoyStaff.java` currently defines 11/14 base bolt damage, 12 area damage and 700/600-tick summon/area cooldowns. No swapped method parameters were found in this focused review; no general buff is justified without tests.
- `marine/MarineWeapons.java` currently defines Abyss Staff direct pulse 8 and cooldown 140 ticks. `depth/PoolSpear.java` uses standard hit 11 and water-only charged 12. The reviewed phase-two values match their reports.
- `item/ThunderAxeItem.java` has +2 proc damage at a 60-tick cooldown and the stronger base axe attributes. `item/ThunderSpearCombat.java` has +2 discharge with 120-tick cooldown. These are conditional bonuses, not base melee damage; do not add them to every hit in a comparative table.

### E03: Concrete implementation handoff

- Unit-check expected `FrostShard.launch` positional signature: fourth argument `cold`; create a regression test which fails if cold values are labelled as damage in `FrostSkills` and batch reports.
- Introduce a source-grounded test for the finalized **two distinct** Kalin skills and for relative damage targets of the saber vs comparable high-tier rewards. Remove/replace the old `verify_weapons87_kalin_wave.py` assumption deliberately, not accidentally.
- After correcting B01/B02, search `ru_ru.json`, `en_us.json`, JEI ingredient descriptions, and generated tooltip strings for old numeric or removed-skill descriptions. A successfully parsed localization file can still misstate gameplay.
- Test combat in representative single-target, group, player-with-rune, boss-resistant and multiplayer contexts. Record reproducible observed damage/time-to-kill before proclaiming full balance readiness.
