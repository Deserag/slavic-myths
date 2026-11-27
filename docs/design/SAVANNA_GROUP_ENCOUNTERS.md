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
- Reward policy is encounter-specific. For the jackal pack the owner explicitly supersedes completion-only reward delivery; see the dated specification below. Other encounters are not changed.
- Define disengage/reset policy and spawn caps before implementation; avoid endless pursuit or duplication.
- Separate common group logic from species-specific attacks and animations.

## Acceptance targets
- Natural group spawn and coherent movement in savanna variants.
- Correct shared targeting, member separation and behavior changes on losses.
- Collective boss bar accurately reflects individual health and group deaths.
- No duplicate reward or completion from unload/reload; multiplayer and repeated death callbacks tested.
- No forced chunk loading, bounded AI workload, and no interference with desert content.
- Build, GameTests, in-game visual and behavioral tests required before acceptance.

## Approved ordinary wildlife roster

The following nine species are approved for the savanna update (user decision, 2026-11-26):

| Species | Russian name | Broad ecological role |
| --- | --- | --- |
| Aurochs (Bos primigenius) | Тур | Large herd herbivore |
| Saiga (Saiga tatarica) | Сайгак | Herd herbivore |
| Tarpan | Тарпан | Wild horse, herd herbivore |
| Golden jackal (Canis aureus) | Шакал | Small predator |
| Striped hyena (Hyaena hyaena) | Полосатая гиена | Predator/scavenger |
| Persian leopard (Panthera pardus tulliana) | Переднеазиатский леопард | Solitary predator |
| Common pheasant (Phasianus colchicus) | Фазан | Ground bird |
| Steppe eagle (Aquila nipalensis) | Степной орёл | Flying raptor |
| Goitered gazelle (Gazella subgutturosa) | Джейран | Herd herbivore |

Kulan, great bustard and steppe marmot are explicitly deferred; no additional species are approved at this stage. Existing creatures are not to be duplicated; review AurochsMesh and existing wildlife AI before implementation. Ecological roles are design suggestions, not finalized behaviors or balance. Ordinary wildlife is distinct from boss encounters; subsequent dated boss decisions below supersede the original pending roster. Ordinary wildlife and encounter balance remain subject to testing.


## Cross-biome wildlife distribution

Agreed design principle: all nine approved species must be able to spawn naturally in savanna (including plateau and windswept variants, subject to valid terrain). Wildlife is not exclusive to the savanna; individual species may also spawn in ecologically suitable existing Overworld biomes. Do not extend boss encounters automatically with ordinary wildlife.

User-approved examples: pheasant, aurochs, saiga and tarpan can also occur in ordinary and dark forests; golden jackal can also occur in dark forests and badlands (clay/terracotta deserts). For open-country herd animals in forests, prefer clearings and woodland edges, not dense canopy. The following is a preliminary expansion matrix, with additional biomes and relative frequencies subject to tuning and approval:

| Species | Primary | Other candidate biomes |
| --- | --- | --- |
| Aurochs | Savanna | Plains, forest, dark forest |
| Saiga | Savanna | Plains; forest and dark forest clearings, rare |
| Tarpan | Savanna | Plains; forest and dark forest clearings |
| Golden jackal | Savanna | Dark forest, badlands, wooded badlands, plains |
| Striped hyena | Savanna | Badlands, desert margins, rare |
| Persian leopard | Savanna | Forest, dark forest, wooded badlands, foothills |
| Common pheasant | Savanna | Forest, dark forest, plains |
| Steppe eagle | Savanna | Plains, badlands, windswept hills |
| Goitered gazelle | Savanna | Plains, badlands, desert margins |

Implementation proposal: use species-specific biome tags and NeoForge spawn modifiers with separate primary and secondary weights; validate local terrain, clearance and group size at spawning, including suitable perches/flight space for eagles. Secondary habitats must not overwhelm their existing fauna. Large herds favor open ground; secondary forest populations are smaller/rarer where appropriate. No universal spawn tag; no spawning in oceans, Nether, End or unsuitable cold biomes. Preserve existing species and their balance. Exact weights, group sizes, biome IDs and terrain constraints remain pending. Test each approved habitat and negative controls for unsuitable biomes. This section documents design only, not implemented spawn changes.

## Boss 1 — Steppe jackal pack (owner update 2026-11-27)

- Nine equal boss jackals; ordinary jackals are unchanged. Reuse existing model/texture and animation, scale all dimensions by 1.5. Natural roaming group in the three savanna variants, no arena. Preliminary balance: 100 HP each (900 total), level 6 candidate; 8 attack damage, 25-tick bite recovery. No final balance approval implied.
- When at least half the original nine are dead (five deaths, four survivors), the surviving animals remain enraged: attack recovery 10 ticks and faster chase. Chunk unload is not death. Shared bossbar always uses the original 900 HP denominator and includes last known health of temporarily unloaded members.
- Hunt living targets with strictly lower max HP, excluding allied entities, same-species jackals, fellow pack members, creative/spectator players. Stronger attackers can be retaliated against. HP is the initial explicit strength proxy; actual equipment/armor and balance can be revisited. Recent attackers take priority, and members distribute/reassess targets independently in multiplayer. Bites and short lunges reuse the existing animated strike; approach from offset positions and regroup when idle.
- Reward rule (owner decision): future unique rewards must be randomly distributed among individual member deaths, NOT awarded only when the last dies. A reward may drop from the first, another from the third, etc. Exact items, counts, chance and guarantee not approved. Future design: persist randomized per-encounter reward assignment and per-member claims; death triggers only its assigned unclaimed reward, with MP/environmental death policy agreed before enabling; no reward on unload or repeated death callback. Do not add any boss reward, soul, or last-member bonus now. The current boss loot table is intentionally empty, XP zero. Completion and reward delivery are independent.
- Implementation initial pass; compilation, GameTests, natural spawn density, visual and multiplayer QA still required. Group spawning uses a rare single seed, expands to nine only if all eight placements and entity additions succeed, no forced chunk load. Local separation cap 128 blocks (loaded encounters). Numbers and cooldowns not final.

### Pack spawn egg
- `slavicmyths:steppe_jackal_pack_spawn_egg`, separate from the ordinary jackal egg. Listed in the existing mobs creative tab via SpawnEggItem classification. Right-click solid ground in the Overworld with open sky and sufficient loaded space for all nine; failed placement preserves the held egg and rolls back spawned members. Creative does not consume it; survival consumes one on successful placement. Explicit player placement bypasses the natural 128-block encounter separation for testing; natural spawn rules are unchanged. Standard dispenser seed behavior is not covered by the transactional hand-use path. Build and in-game verification pending.

## Boss 2 — Black hyena and her retinue (owner-approved concept, 2026-11-27)

- Combine roaming herd and leader-with-entourage archetypes. Eleven members, no required arena: one black striped-hyena leader (2.5x normal dimensions, 400 HP, attack damage 18, armor 8); two elite bodyguards (2x normal dimensions, 150 HP each, damage 12, armor 4); eight ordinary striped hyenas (1x, 29 HP each, damage 5, armor 0). Total initial HP 932; candidate difficulty level 6, subject to gameplay balancing. Keep standalone ordinary hyenas unchanged. Reuse existing model and animations; black coat with subdued dark-gray stripes for leader, darker coat for bodyguards. Scale physical dimensions and visual model consistently.
- Roam and hunt weaker living entities (initial max-HP strength proxy, exclude own group and allies; no creative/spectator targeting); retaliation against stronger attackers allowed. Shared aggro, target distribution and reassessment in multiplayer, spatial separation and coordinated attacks. Save exact roster and casualties; unloaded entities are not dead, no respawning defeated members. Reuse common group logic without changing jackal encounter rules.
- All members bite, lunge and flank; bodyguards and leader have heavy knockback strikes; bodyguards prioritize defending leader and redirect toward its attacker. Leader has a command roar temporarily increasing retinue pursuit/attack frequency and a strong charge. First bodyguard death increases roar frequency; death of both triggers leader rage; below 25% leader HP increases charge/heavy bite frequency. Leader death removes coordination and buffs, but survivors remain dangerous. Exact cooldowns, duration and magnitudes remain to be balanced.
- Boss bar displays leader HP only (400 initial), and leader death is encounter victory independently of retinue kills. Retinue can be fought before or after leader; their reward eligibility must survive leader death and reload. Natural spawn in savanna variants, all-or-nothing group placement and a separate whole-pack spawn egg recommended for testing.
- **Rewards — explicit owner rule:** distribute rewards by role, between leader and retinue, not a single final-kill payout. Future loot specification MUST explicitly list each leader-only reward, each bodyguard reward (including whether one/both can drop it, allocation and limits), and any ordinary retinue reward (separate from ordinary wildlife loot; no automatic unique boss drops). Leader-exclusive rewards must never drop from bodyguards; bodyguard-specific rewards must not be deferred to leader death. Future reward tables should specify item/registry ID, role, count, chance/guarantee, allocation, MP eligibility, persistence and anti-duplication, and be submitted for owner approval before implementation. Each eligible reward drops on the relevant member's death, not when the last animal is killed. Do not lose or duplicate claims on leader death, unload/reload or repeated callbacks. No specific rewards, rates, souls or new item assets approved yet; no unique loot implementation now.
- Status: owner-approved design, not implemented in Java/assets. Build/GameTests and gameplay/MP checks pending implementation.
