# SKIM DEN — small exploration reward space, not 1.7.6 unique loot

## One compact cave
Entrance centered (0,-29) local, extends **north** behind cliff ~15–20 blocks, width 10–13, ceiling height 5–8, with one chamber and short entrance tunnel. Preserve reference: dark broad cave with primitive trophy ledges, skeletal remains of prey, signs of territorial beast, a natural resting platform/hollow, **two normal chests**. Do NOT implement multi-floor procedural dungeon, locks, riddles, mobs galore, puzzles, traps, massive structure chain or optional miniboss.

Coordinate suggestions from entrance threshold:
- Tunnel entrance width8, length5 at (0,-29..-34), floor descending 1 block. Avoid narrow bottleneck with exterior boss geometry.
- Main chamber center at (0,-42), dimensions 12W×14L×7H; high central stalagmite offset (+3,-40), rest platform at (-1,-44), bones field along west wall.
- Trophy wall north (-1,-49): skull/bone clusters and red-brown torn banners (non-copyrighted). Normal supplies chest 1 in alcove east (+4,-43), chest 2 west (-4,-46), both with valid vanilla-ish loot table **no new boss-exclusive item**.
- Ambient torches 4–6 (low saturation), water drip ceiling 1 location, falling-dust particles sparse at cave entry.

## Access and victory behavior
Pre-victory cave can be visible but interior path optionally sealed by strong natural fallen boulder. After first registered Skim victory, boulder drops/retracts by a small animation 30 ticks, sound den_stone_open; if no gate is easier/consistent with existing structures, cave may remain physically explorable beforehand, but **do not award unique 1.7.6 loot**.
- Completion flag `skim_beast_defeated` saved on player/server dimension robustly. For existing world when later 1.7.6 introduces rewards, don't require boss refight; migration hooks listed in shared progress file.
- Chests can use existing vanilla loot tables; suggested moderate: bone, arrows, iron ingots, leather, bread, small emerald chance. No mythic amulet, spear, relic, craft recipes of later version. Avoid using chest as disguised unique reward distribution.
- Player can return after boss death; den should not despawn or regenerate each login. If boss respawn used by debug/creative, keep interior progress separate from arena temporary state.
