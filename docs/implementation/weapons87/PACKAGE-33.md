# #87 — P33 / #161 targeted Storm Staff VFX

**Baseline main:** a0d0f5e97ea4dbb0d30f5a5f41f78eb8d3e9e972. **Before:** StormStaffItem already emitted 21 ENCHANT particles along 7 block ray, played thunder sound and applied pulse to the closest qualifying target; did not show an impact at the actual target. **After:** only if `e.hurt()` returns true, spawn 12 ELECTRIC_SPARK near target torso (one server→client particle packet), then invoke the existing rune staff effect. Ray trace, cooldown54/60, base damage6, enchant feedback and audio remain unchanged. Failed damage has no hit sparks/rune post-hit effect.

| ID | Trigger | Source cue | Expected | Budget/MP | Validation |
|---|---|---|---|---|---|
| storm_staff | Use, 7-block ray | 7×3 ENCHANT, SFX | charge/ray still visible | 21 particles | source PASS; owner runtime NOT_RUN |
| storm_staff | accepted magic impact | **12 ELECTRIC_SPARK** at target | flash on successful hit only | max12 per 54–60 ticks, ServerLevel.sendParticles | Java21 CI pending, owner runtime NOT_RUN |
| thunder_axe | proc 20% on successful player melee | visual-only LightningBolt | visible bolt, no block fires | existing one bolt per proc | KEEP P29; owner retest NOT_RUN |
| trophy_kalin_sabre | third accepted hit | 15×2 gold arc + actionbar | visible 95° frontal wave | 30 particles per three hits | KEEP P30; owner retest NOT_RUN |

Full #161 remaining: other unique weapons, multiplayer saturation, owner 1P/3P evidence. Do not mark whole issue complete. No new gameplay feature/registry/recipe/loot/world file changes. Source-only check `tools/verify_weapons87_p33.py` plus Java21 CI/JAR.

## #162 melee animation audit (no production change yet)

Existing `WeaponGrip58.karachun` and `WeaponGrip58.morozko` drive **entity/boss models** (not vanilla player's item hand), so they must NOT be reused as the player's 68 melee animations. `WeaponHands` owns special client first-person crossbow/scythe only; default sword/axe/mace item states still use vanilla hit animation. No evidence from client screenshot of Alex/Steve multi-frame melee animation after P28. Required next implementation: exact selected melee registry classes, four-phase idle→windup→impact→recovery storyboard at grip pivot, 1P+3P on Steve/Alex; do not invent 68 server hit events or alter combat timing. **Audit only**, #162 OPEN.

## #165 polearm animation audit (no production change yet)

Source `WeaponHands.scytheWrist` already handles Karachun held 1P position; `WeaponGrip58.morozko` is a boss arm, not the player's own staff. Existing JSON display transforms define polearm 3P idle but not coordinated animation. Need separate player-class archetypes (staff, scythe, thrust spear, short sickle) with per-ID item mapping; real 3P player pose must be staged against actual grip dimensions and coexist with #115. No actor rig or boss arm modifications made in this P33. **Audit only**, #165 OPEN.

**Owner validation:** use Storm Staff against hit target then blocked/no valid target and compare rays + yellow/electric impact; check no repeat spam or FPS issue in mob crowd/MP. Minecraft/PolyMC NOT_RUN.
