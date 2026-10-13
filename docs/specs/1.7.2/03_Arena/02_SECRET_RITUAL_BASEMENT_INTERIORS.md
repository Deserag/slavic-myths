# SECRET BASEMENT — exact floor plan, ritual staging and repeat-visit behavior

## Narrative contract
After FIRST legitimate victory against Marinka in a particular constructed yard, witch power fades, central courtyard seal fractures, and a small hatch/stairwell reveals the underground secret ritual quarters. Do not require a timed puzzle or re-beat boss; no unique boss or bogatyr reward until v1.7.6. Ordinary vanilla loot may be placed in once-only chests, plus lore decor and books. This is an explorable physical space, not an unreachable cinematic background.

## Physical geometry
- Underground outer footprint ~**17 X × 23 Z**, central room layout ~13x15, height 5b clear (outer excavation up to 6-7b). Align entrance below ritual hub; floor top local Y=-10, ceiling local Y=-5, hatch local center x=0,z=0 at Y=0. Stair descends via 2b-wide turning flight or ladder with safe handrail; no open unsupported 10b shaft.
- Basement cutaway/blueprint in `05_Visual_References/Technical/Ritual_basement_floorplan.png`. Rooms are actual traversable, with visibly different functions; no one-room copy-paste gallery.

## Interior floor space (relative to basement entrance at x=0,z=0)
| Room | Approx size | Floor heights / access | Specific objects | Story and gameplay |
|---|---|---|---|---|
| Entry vestibule/stair | 3x6 | spiral/turning stair from y=0 to -10 | aging torches, cracked stone threshold, faded footprints, scratched warning sigil | one-player and ally-safe, no fall damage if correct |
| Ritual workshop (west) | 7x9 | level floor -10, door >=2 wide | heavy rectangular ingredient table 3x1, 1 black iron cauldron, mortar+herb press, 3 shelves, 4 hanging herb bundles, mushroom basket, 3 sealed jars, 2 readable documents, copper brazier; lit by 5 amber candle/torch clusters | hints on footprint curses and binding spells |
| Hall of transformations (central/east) | 7x9 | floor -10, oval ritual recess 5x5 | golden-red horn motif on walls, ritual dais 3x3, gold runic focus center, two chained stone columns with chains attached to walls (not floating), 3 scraps of bound leather, signs of failed experiments | explains Dobrynya's aurochs curse, vanishing power after fight |
| Hidden store (north) | 5x6 | 1 doorway or suspicious torn tapestry after victory; no extra compulsory puzzle | chest 1–2, books, old ledgers, potion rack, coin piles as static props, one seal motif | optional VANILLA consumables only in v1.7.2; story record for v1.7.6 |
| Narrow connective passage | 2b wide | -10 | stone brick arches, support beams, signposts as atmosphere | navigable collision and camera |

The desired three zones can overlap architecturally via arches; do not make 13x15 core impossible by allocating 7+7 widths plus walls inside 13. Use outer 17x23, 2b corridors and 1b wall, respecting height. The workshop and hall may share one common wall and door; northern store extends beyond 13x15 core.

## Hatch victory sequence with exact cues
1. Boss real entity HP hits zero on server; encounterController enters `WON` (once). Do not unlock on fake clone defeat.
2. At 0.0s disable harmful spells, dismiss remaining decoys/minions; audio witch_defeat.
3. 0.6s ritual ring fades from bright ruby to slate; 1.2s 4 stone cracks propagate inward; 1.8s central altar subsides by one block (without suffocating nearby player); 2.2s 2x3 hatch visible; 2.8s stairs and clear path open, unlock sound. No camera hijack that permanently traps player.
4. Mark block state `basementUnlocked=true` and entrance coordinate atomically in server save after safe generation; never spawn second basement on relog. This flag is independent of player's global historical boss-victory achievement.
5. If chunk unloads mid-sequence, on reload resolve safely to consistent **open** final state and valid stairs, not half-generated stone pit.

## Chests and later rewards
- v1.7.2: ordinary vanilla drops *only* (examples modest candles, bones, redstone dust, glowstone dust, 1–2 potions, an emerald; actual loot weighting configurable according to existing mod structure loot pattern). Avoid OP diamonds/netherite. Loot tables attached once and loot claimed state stored by normal Minecraft container rules; no respawn every revisit.
- v1.7.6: future unique items from a separate reward registry/progression claim flow. Store `marinka_defeated`, `marinka_participation`, `dobrynya_released`, `marinka_basement_discovered`, and potential unique reward `unclaimed` eligibility; do not spawn a fake placeholder chest and later fill it by resetting all chests. Already-defeated players must remain eligible without re-fighting.
- Documents/lore should describe witch footprints and aurochs curse in original short Russian-language text. No copyrighted dialogue excerpts.

## Structural sanity
Prevent submerged underground rooms, exposed voids into caves, fluid leaks and mob spawning on ritual dais. Prefab may include natural irregular cave boundary but never place rooms in unvalidated protected structures. Stair has at least two blocks headroom, doors remain open after unlocking and all 3 room types are clearly distinguishable under torchlight. Accessibility: low bloom, high-contrast golden-red cues vs gray rock, no reliance on sound alone.
