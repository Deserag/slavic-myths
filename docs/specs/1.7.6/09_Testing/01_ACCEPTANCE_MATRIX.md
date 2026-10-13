# Functional acceptance matrix — 1.7.6

| ID | Test | Pass condition |
|---|---|---|
| REG-01 | Register 19 items | 5 rune + 4 gifts + 5 trophy + 5 mats, valid creative tab |
| ART-02 | Preview all icons | 64x64 transparent icons readable, 3 approved sheets bundled, no broken models |
| MAT-03 | Defeat bosses | Each grants exactly own rune+material on first eligible clear |
| MAT-04 | Repeat boss | Material 1–3 and independently 15% rune roll; no first grant again |
| RETRO-05 | Load prior save | Earlier legitimate boss victories are eligible for one claim without repeat |
| DUPE-06 | Claim at once / inventory full | No duplication or lost item, pending state correct |
| GIFT-07 | Finish Ilya, Dobrynya, Alyosha, Finist stories | Correct one-time gift from each; summoning whistle remains |
| RUNE-08 | Install each in runic anvil | Correct allowed slot, effect and cooldown visible |
| RUNE-09 | Same rune multiple slots | No stacking/exploit |
| SHIELD-10 | Block 6 HP potential hit | Enemy takes 9 HP and impulse, once per 35s |
| SHIELD-11 | Damage while shield down or environmental | No reflection |
| SHIELD-12 | Reflection recursion attempt | No infinite loop even against shield user |
| RING-13 | Block then hit within 4s | Next direct melee hit ×1.75 only; cooldown 12s |
| RING-14 | Expire / unequip / shoot arrow | No bonus applied |
| BELT-15 | Heavy telegraph with safe side cell | Dodges ~2.5 blocks and 40s cooldown |
| BELT-16 | Unsafe side / boss arena-wide hit | No invalid teleport |
| FINIST-17 | Nearby hostile mobs | Detection once/4s only eligible range, alert 20-tick telegraph |
| CHEST-18 | Idol chest heavy hit | 10% damage reduction and 45% KB reduction, -5% speed |
| CLOAK-19 | Damage accepted, safe blink | 2–3 block blink and smoke on 45s cooldown |
| NECK-20 | Hit target below 30% HP | +10% direct player damage, no reflected buff |
| MANTLE-21 | Kill boss-owned hostile summon | Resistance I 5s / CD20s, not from player pets |
| SABRE-22 | 3 unique hostile kills in 30s | Next charged sweep affects <=3 extra foes, then stacks reset |
| MP-23 | Two players in same battle | Per-player claim and no double drops, no desync |
| SAVE-24 | Relog/die/switch dimension | Claim state and relevant cooldown persist |
| PERF-25 | 30 nearby mobs + rune procs | No per-tick unbounded memory allocations or particle spam |
| BUILD-26 | Build | Actual project build succeeds; changed-file list and test evidence supplied |

Also test existing rune effects, shield raising animation, accessories, and all four bogatyr summon items for regressions.
