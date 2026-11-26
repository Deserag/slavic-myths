# Savanna: roaming herds and group boss encounters

Status: design direction agreed; implementation and creature roster pending.
Scope: feature/savanna-biome-20261126; Minecraft 1.21.1 / NeoForge.

## Biome identity
- Open-world encounters rather than a chain of dedicated dungeons. Keep savanna open and recognizable.
- Wildlife and hostile groups roam in packs/herds; a group can be a boss in its own right.
- At most one or two bespoke structures are planned, not a mandatory arena per boss. Exact structure concepts are pending.
- Ordinary wildlife roster to be discussed separately, inspired by historical Kuban, North Caucasus and neighboring Pontic-Caspian steppe fauna and Slavic folklore. Do not equate historical fauna with mythical creatures.

## Encounter archetypes
1. Leader with an entourage: one large boss and several smaller followers; followers guard, flank and assist.
2. Collective boss: several large creatures (e.g. wolves) form one encounter, with individual health and a shared boss bar reflecting surviving members' current/maximum health. Complete only when the whole registered combat group is defeated.
3. Roaming herd: coherent moving group, potentially neutral until provoked, with species-specific defensive behavior.
4. Paired boss: two complementary large enemies; surviving member can change tactics on partner death.

Numbers (e.g. 3–6 followers, 5–7 pack members) are starting points, not final balance or roster commitments.

## Shared system design (proposal, not implemented)
- Server-authoritative persistent group identifier, member IDs, leader/roles, target, encounter state, bounded roaming destination.
- Natural spawn as a group, terrain validation and bounded retry; avoid villages, existing encounters, unloaded chunks, and forced chunk loading. Tune density by actual testing.
- Shared aggro with bounded range; maintain separation, regroup, flank, rotate attackers, protect leader where appropriate. No continuous global entity scans.
- Persistent state through save/reload and temporary chunk unload; unloading/despawning is not a kill. Never respawn defeated members of the same active encounter.
- One shared boss bar for collective encounters; distinguish leader-only completion rules where applicable. Ordinary wildlife must not get a boss bar.
- One completion reward per encounter, persisted before reward delivery; handle multiplayer contribution, duplicate callbacks, deaths and environmental damage. Exact loot and souls pending global boss reward audit.
- Define disengage/reset policy and spawn caps before implementation; avoid endless pursuit or duplication.
- Separate common group logic from species-specific attacks and animations.

## Acceptance targets
- Natural group spawn and coherent movement in savanna variants.
- Correct shared targeting, member separation and behavior changes on losses.
- Collective boss bar accurately reflects individual health and group deaths.
- No duplicate reward or completion from unload/reload; multiplayer and repeated death callbacks tested.
- No forced chunk loading, bounded AI workload, and no interference with desert content.
- Build, GameTests, in-game visual and behavioral tests required before acceptance.

No new bosses or wildlife are approved by this document; exact creatures, stats, loot, encounter count and structure placement remain open.
