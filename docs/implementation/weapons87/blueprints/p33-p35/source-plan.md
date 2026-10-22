# #87 P33–35 — source-first visual / pose targets (22.10.2026)
Main starting point `a0d0f5e97ea4dbb0d30f5a5f41f78eb8d3e9e972`. All work isolated to ranged-independent client arm transform of item and one server impact VFX; no entity rig/mod world generation/network changes.

## P33 #162 ordinary maces
Source `ModItems`: `wooden_mace`, `stone_mace`, `mace`, `silver_mace`, `perunite_mace` all `org.slavicmyths.combat.MaceItem`. Client `IClientItemExtensions.applyForgeHandTransform` already proven by `BowDrawClient` and `FlailClient`. Extend only these five registered Items. Visible phases: ready hand at (±.49,-.40,-.91); windup duration normalized 0–.18 swings, weapon raised +.11 with -24° pitch; contact around .33–.38 rotation X +58°, weighted vertical -0.15; recovery by .84, rest by1. No server damage or action timing touched. Vanilla player third-person and boss rigs remain untouched; true full melee animation #162 not yet accepted.

## P34 #165 ordinary long spears
Source 7 real IDs `spear`, `silver_spear` + five ordinary material spears in `WeaponCatalog`: `gold_spear`, `iron_spear`, `diamond_spear`, `perunite_spear`, `netherite_spear`. First-person grip-only thrust with -0.38Z at contact and minor 11° setup pitch, -12° drive, no swing over head; rest/return within native swing. Does NOT touch special Morozko/Karachun staff rigs; long staff subclasses should get their own separate pose. Server SpearItem range+1.25, push, speed/damage unchanged. Third-person and walk/sprint owner retest blocked; full #165 not complete.

## P35 #161 impact VFX only
`StormStaffItem.use` currently finds closest line-of-sight enemy, applies magic damage and runes, sends 7×3 ENCHANT tracer and one TRIDENT_THUNDER sound. Missing distinct visual **impact** cue: add one compact `ELECTRIC_SPARK` burst **only when `e.hurt` returns true**. Keep original tracer 21 particles, sound, damage and rune call; do not add second damage/ray/world weather or additional particles on miss. PREP: old tracer, IMPACT: 8 sparks at hit living target and not environment, COOLDOWN: no new effect. 8 sparks per actual hit only. Multiplayer via server `sendParticles` to observing clients. Source after/actual client runtime NOT_RUN.

### Gates
Freeze before StormStaff Java in `blueprints/p33-p35/before-StormStaffItem.java.txt`; client class created separate; source stage table (ready/prep/hit/recovery) and Java21 clean build; model/source storyboard of actual registered IDs; CI regression assert item registration exact 5+7, correct hand mirroring/animation curves, no extra `hurt` or procedural world writes, VFX 8 budget only on accepted damage. Client/PolyMC game launches 0, all issues OPEN.
