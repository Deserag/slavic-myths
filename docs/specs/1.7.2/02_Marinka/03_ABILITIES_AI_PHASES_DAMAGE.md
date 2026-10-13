# MARINKA — complete deterministic boss combat specification

## 600 HP, not a damage sponge
Health maximum = 600 (300 hearts engine scale). Boss relies on **counterplay**: light body mobility, finite 30% protective spell, scripted clones and recover windows. Not 800 HP. Tall about 2.1b. Base movement .29–.32b per tick on ground while avoiding obstacles; max sprint bursts .38b/tick for <=1.3s. No permanent flight. Start fight only inside area when player and matched Dobrynya are present/validated; recovery rules covered elsewhere.

## Server phase controller
- `PHASE_1 (HP >390, 100%-65%)`: caster footstep curse, witchfire, lure, short blinks and barrier, plus shadow clones on cooldown once unlocked ~first 14s.
- `PHASE_2 (390 >= HP >180, 65%-30%)`: one marquee **Golden-Horned Aurochs** story curse ritual on Dobrynya (at most once per attempt), anchor spawn and destroy interaction; shadow clones and barrier; avoid severe overlapping fire while ally transformed.
- `PHASE_3 (HP <=180, 30%-0%)`: trigger **Witch Rage** once; 15% faster casting, plus longer exhaustion windows after every 2 casts; telegraph extra floor runes. Never repeat the aurochs curse.
- Phase transitions cannot backtrack due to healing. She has no passive regeneration.

## Ability table
| Ability | Activation and timing | Telegraph, damage and counterplay |
|---|---|---|
| **Arcane Veil** | Starts after 4s, 22s cooldown once broken, max 9s uptime | 0.4s runic warning, blocks 30% of all damage (factor .70); a successful Dobrynya magic deflection exposes seal; his Spellbreaker breaks ward for 8s; no veil during shadow clones, magpie or scripted hex |
| **Shadow Dissolution** | first between 14–24s, repeat CD 35s from end; lasts max 18s | .9s smoke -> exactly 3 fully identical Marinkas: 1 real and 2 false. Decoys each have 24 HP; after enough cumulative hits, a false dissolves. Real remains and takes usual boss damage. No visible differences, including HUD, sound, movements, particles, hit spark; no 4th body |
| **Witch Blink** | 12s CD; maximum 3 in 35s | visible departing/reappearing rune; 8–12 block jump, landing on validated reachable ground, >=3 blocks away from player, 0.6s attackable landing recovery |
| **Magpie Shape** | CD 27s; not while triple-form active | .75s real crow-sized **magpie** transform; 1.3s visible arc 6–12b; .65s rematerialization; ranged hit can shorten flight; no indefinite invulnerability |
| **Footprint Hex** | CD 11s | mark last stepped tiles, .65s warning + 1.1s detonation wait; baseline 3.5 final HP and Weakness I 4s + Slowness I 2s; simply leave lit cells to avoid |
| **Witchfire Fan** | CD 7s | .6s palm buildup and visible trajectory; centered projectile at least 6 final HP through unbuffed full diamond (target 6.5), side waves 3HP; ember terrain damage 1HP/s, max 3s, anti-double-hit immunity between pulses |
| **Luring Call** | CD 21s | 1.05s chant ring, pulls max 2.5b over .8s unless cover/block; baseline 2HP, no combo with a heavy spell within 1.25s guaranteed response window |
| **Enchanted Herd** | phase 2+, CD 27s; maximum 2 at once | summons 2 enchanted tur minions, each 35 HP, 5 raw damage, 22s lifetime; all disappear after encounter, no independent persistent mob ecology |
| **Golden-Horned Aurochs Curse** | ONCE per attempt at 65% boundary if matched ally alive; 2.35s cast | visible footprint/anchor sigil; morphs Dobrynya to friendly golden-horned aurochs for up to 35s; disables spear/shield but lets him charge Marinka, player destroys anchor to break curse; can't become permanent |
| **Witch Rage** | ONCE below 180 HP | 2.1s clear telegraph; other offensive CDs ×.85; after any 2 offensive spells she spends 1.25s exhausted/vulnerable, additional ground runes 1s before eruption |

## Real clone fairness, server identity and group damage budget
Maintain encounter UUID and private server-only real actor pointer; every body renders exact same mesh, texture, equip, particle packet, footsteps and hit reaction. Even sound pitch jitter sampled from identical distribution; all spawn nodes shuffled 8–14 blocks from player with angular gap at least 55deg. No floating name, special outline, icon, health tag or post-cast head turn that reveals original. The single shared bossbar can respond to genuine damage but MUST NOT point to a particular body before the player tests it with attacks. False copies use the same attack VFX and anim but 45% effective damage internally; one group-wide burst budget means at most 1 high-damage bolt per 2s even if all 3 cast. Avoid overpowered 3x direct damage. All copies despawn on real defeat, death/area exit, or after 18s; they drop nothing and do not count as boss kills. Clone TTL expiration should visually resemble 24HP break but not auto-award kill.

## Golden aurochs curse rescue
Ritual focus appears at a prevalidated reachable arena position (preferred local x=+7,z=+2 on ground) with gold-red anchor, 80HP or 3 player strikes as closest existing mechanic. The player (not Marinka nor minions) breaks it. On anchor break Dobrynya morphs back using `D14`, Marinka gets 2s stumble and 8s broken veil; spell cannot repeat. Before cast, .8s obvious warning. Aurochs retains original entity UUID, all owner/alliance/progression and current health, does not wound player; charges Marinka every 8–13s with telegraphed line. When curse duration reaches 35s without rescue, force non-buggy reversion with weak exhausted state and no permanent prisoner. If Dobrynya is already retreating at 65%, delay max 10s and gracefully skip/fallback to a player footprint hex; no softlock.

## AI constraints, fair damage and counter windows
Stay in 7–13 block casting distance from player when possible. Cannot blink out of yard, into walls, onto unloaded blocks, behind closed doors or beyond 17-block radius from relevant center without a path. High-damage magic has clear >=1s warning overall and 1.2s minimum safe gap before another non-dodgeable direct heavy cast. Shield average uptime <30% of a normal fight, unless player's skill fails to punish it. Never overlap shield+shadow disappear+bird. Recovery after 2–3 casts makes 1.25s actual vulnerable window. Boss may attack Dobrynya, but only the correct personal bogatyr is in party; no forced lock on ally the whole encounter. No spell damage from client, duplicate server tick or multiparty hit; combat math normalized to 24 player HP and full diamond armor. DOT / weak graze does NOT each deal 6HP; 6HP minimum is reserved for heavy clean hits.
