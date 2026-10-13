# COMBAT BALANCE — player in real armor, spell classification, thresholds

## What HP numbers mean
Minecraft uses 2 HP per heart. Baseline test player **24 HP (12 hearts)**, in full **diamond armor** (20 armor points, 8 armor toughness vanilla where relevant) and WITHOUT Resistance, Protection enchantments, accessory shields, regen or potions. All listed final-damage values refer to **actual health deducted after armor calculations** on that baseline. Strong direct boss hits target >=6 HP (3 hearts) actual, so well-geared player notices impact. Minor hazards/curses are intentionally lighter, NOT 6HP every tick; test with buffs and debuffs for interaction, no universally armor-ignoring damage.

## Test method / formula
Define one shared balance helper that calculates effective final damage through **the current engine's actual entity damage pipeline**, honoring armor/toughness/enchantment/protection and configured mods. Calibrate raw damage using automated dev tests or in-game instrumentation by applying hit at baseline, reading health before/after. Do not substitute a hand-written approximate armor formula as final authority. If a built-in magic damage type bypasses armor, set direct `healthDamageGoal=6.5` responsibly and verify protection charms still work. Floor minimum applies only to unblocked **major direct hits** at baseline; if player wears weaker armor, damage should be higher. Never force a floor on actual ally shield/parry/cover dodge or invuln frames.

## Proposed measured values (must verify in test world)
| Source | Expected effective baseline health loss | Counter |
|---|---|---|
| Marinka Witchfire main centerline | 6.0–7.0 HP | telegraph + dodge, ally deflect if reflectable |
| Marinka Witchfire wing or near miss | ~3.0 HP | sideways roll, collision cover |
| Footprint curse detonation | ~3.5 HP, Weakness I | leave glowing tiles |
| Luring call | ~2.0 HP plus 0.8s pull | line-of-sight cover or deflect/resist |
| Standing burning ground | 1.0 HP per second, max 3s per patch | step out, anti stacking cooldown |
| Marinka phase 3 runic burst | 6.0–6.5 HP main center tile | >=1s visible tile warning |
| Summoned aurochs/tur minion | expected 2–3 HP through diamond | block/spear counter |
| Shadow clone direct main bolt | <=45% of equivalent real spell, grouped burst cap | kill illusions, no 3x lethal spam |
| Dobrynya hero long thrust (raw enemy damage) | 21 raw physical HP, subject to target armor | aim + recovery |
| Dobrynya combo | 9 +10 +15 raw, independent contact windows | geometry / timing |
| Dobrynya magic ward | mitigates/reflects selected missile once | directional only, cooldown |
| Marinka 30% barrier | multiplies incoming damage by 0.70 | spellbreaker exposes 8s |

## Player enhancements
If Protection enchantments, rings, rune armor, defensive blessings, absorption, Resistance or accessories are active, damage legitimately drops below nominal 6 HP. Conversely armor debuff or Weakness may change damage numbers as actual rules; test. Do not secretly cancel legitimate healing/regen, but avoid bosses hitting only 1 heart every 15s against baseline.

## Controlled vulnerability
Hero tanking must not create free infinite backstab: Marinka alternates movement and targeting, has positional attacks/teleports, can push ranged hazards toward player even while Dobrynya guarding. Ally magic reflects limited attacks and has cooldown. Boss has a real opportunity to attack when veiled/exhausted. Long repeated clone phases gated and cannot consume 80% of battle.

## Effects and warnings
Crimson telegraph = damaging fire. Black smoky vortex = illusion. Ruby circular runes = defense. Gold footprints/horns = transformation. Blue-white brief spark from spear = successful ally protection. Red tile runes must remain visible without particles (e.g. in minimal particle setting use block overlay). Play directional sounds. Strong direct damage warning at least 1.0 sec. Boss avoids unavoidable stacked 12HP burst inside 2 seconds.

## Expected rough rhythm
With player competent diamond weapon and Dobrynya support, a fight lasting ~3–6 minutes is a tuning target, not a strict timer; 600 HP shield only 30%, clone hard cap and downtime generate varied but manageable pacing. Do not allow endless stall, hide behind walls, repeatedly exit arena or reset boss HP while player still actively fighting.
