# Release criteria and hard numeric contract

| Component | PASS only when... |
|---|---|
| Alyosha visuals | Blond messy hair, bright blue eyes, youthful clean-shaven face, lighter cream/red fabric, leather gear; recognizable from 4 directions, not re-colored Ilya/Dobrynya |
| Alyosha role | He *annoys* enemies via taunts/rapid low-power hits, draws attention, **then dodges**, cannot face-tank or output Ilya-level burst |
| Alyosha base | max HP=175, height approx 2.15 blocks, width about 0.85 blocks including cloak/gear; last stand begins at <=15% (26.25 HP), separate retreat/heal state |
| Skim | HP=750, mostly 4 legs, ~3 blocks high quadruped, 4.5 blocks reared, length ~5 blocks tail excluded; mane/large tusks visible; 7 abilities and exhaustion/attack windows |
| Arena | Playable 65x65 valley main footprint, perimeter natural cliffs, unobstructed center, den entrance and interior; arena generated/located without full world scan or long-path worldgen project |
| Skim encounter | Story lock permits Alyosha, not Ilya/Dobrynya/Finist, includes boss healthbar, death persistence; disconnect/relog doesn't duplicate boss |
| Summons | Three NONCONSUMABLE hero items Alyosha/Ilya/Dobrynya, 3 boss spawn eggs Skim/Idolishe/Marinka in creative/testing; boss encounter item usable at designated valley |
| Combat baseline | Strong Skim attacks do >=6 health points post-diamond armor at 24 HP health without extra buffs; individual light claws can be lower; dodge has capped effective immunity |
| Audio | Registered sounds.json keys, event-appropriate OGG, range/gain, subtitles (EN/RU); variants where frequent |
| Progress | player/encounter victory flags saved; ZERO unique reward items or chests with exclusive 1.7.6 drops |
| Performance | no tick-by-tick broad scans, no repeated structure regens, minimal particles cap, <=one active skirmish encounter per arena |
| QA | execute `09_Testing/` acceptance scenarios, build no compile errors |

## Priorities
P0 = compilation, entity registration and rendering, AI with dodge+aggro, SFX, reuse summon tools and eggs, boss damage, encounter completion, safe world placement.
P1 = all named animations/ability telegraphs, natural valley with den, encounter multiplayer tests, 32+ textures, arena interactions.
P2 = optional ambience/extra cosmetics. Never sacrifice P0 to add decoration. This does NOT authorize omitting named mandatory assets: if time-constrained, compact their implementation, not the feature set.

## Grounded decisions vs implementation defaults
**Previously agreed:** blond + BLUE-eyed Alyosha, unique lighter costume, saber and bow; weak striking but strong enemy attention and avoidance; Skim appearance in approved board; valley retained; no 1.7.3 boss rewards; summon items for heroes nonconsumable and boss spawn eggs. **Implementation defaults authored for consistency:** damage numbers, exact cooldowns, geometry box coordinates, item names, 65x65 template details, 15% last-stand trigger matching past heroes. Use defaults unless repo-compatible constraints require adapting units without changing player-facing outcome.

## Build sanity
Minecraft player baseline: 20 HP vanilla vs mod-adjusted common 24 HP (12 hearts). Enemy base damage tests always label pre-armor vs post-armor. Tests should never claim e.g. 6 damage corresponds to six hearts.
