# Моровой Огненный змей — ТЕСТЫ (строгий минимум)
1. Normal form remains original: 0 mor skills, no extra spawn egg; existing spawn unchanged; unlike corrupt, normal can keep own existing sounds/drops.
2. With flask: MOR_TRANSFORMING 50t, MOR_ACTIVE persists after saving/reloading, health multiplier once, target only active player.
3. Fire: 22t telegraph, shots at22/27/32, 3HP per original shot, collision hits no double-damage, one-shot damage for clones2HP, never ignites or destroys blocks.
4. Dive: 18t warning, max12 range, 6HP original /3HP clone, misses safely when obstacle, never goes into void/unloaded block.
5. Clones: transition trigger at exactly HP≤50%, occurs once; 2×30HP not more or less; 3 visually same textures/scale/animation/particle frequency/sound/name; no special color for original; bossbar HIDDEN until clones end; player hits original by mistake ⇒ HP falls but it doesn't vanish at30HP; hit clone enough ⇒ disappears, no loot/XP; parent continues battle; both dead ⇒ bossbar returns.
6. Reload while clones alive => same three entities, parent references valid, no clones re-summoned; reload after summon but before spawn time => flag true and bounded reconstruction without duplicate if needed; no infinite orphan after death/unload; 90s TTL no loot.
7. Multiplayer 2 players: one set of clones, same original UUID for both, no separate per-player illusions, no duplicated damage or effects, sound consistent.
8. Reference physical silhouette long segmented wingless serpent, black/crimson fissures, claws only if base has, horns/fiery fins/face; no giant 3-headed Gorynych.
9. Build/resource/JAR static checks and server test. Client screenshots and manual flight behavior/user acceptance — **not run by Codex**.
