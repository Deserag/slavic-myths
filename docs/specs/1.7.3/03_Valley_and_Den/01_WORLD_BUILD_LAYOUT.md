# 65×65 SKIM VALLEY — bounded structure, no mega worldgen work

Approved architecture: `06_References/Approved/APPROVED_Skim_Valley_Den_Architecture.png`. Make an open elongated basin bordered with boulder cliffs and pine forest, central grass combat zone, ruined hunters' camp in southwest, natural cave entrance on north rim, bones/scraped trees. KEEP APPROVED LAYOUT; use below metric coordinates. Do not spend time building a full procedural biome or chain of structures.

## Local coordinate system and footprint
- Anchor local center (0,0,0) and horizontal local `x,z ∈ [-32,+32]` (65 blocks), surface elevation `y = 0` center meadow. North = local z -32. Rotate 0/90/180/270 according to safe terrain placement. Refer to `06_References/Technical/Valley_65x65_annotated_plan.png` for exact top-down.
- Central **combat meadow** ellipse centered (0,+2), semiaxes x=23,z=21; maximum clear radius >=17 around center for beast landing, player/summon paths, avoiding props >1 block tall in clear area. Grass, gently uneven terrain ±2 blocks, sparse short vegetation. Soft cliffs/rim outside ellipse, average ridge 7–15 blocks high, pathfinding boundaries and line of sight; provide 2 reasonable dodge side lanes at x ±13, z -8..+14.
- North cliff cave entrance at (0,-29), entry facade W=8,H=6 (through 3-deep rock overhang), 3.5-block clearance widened to 5.5 for Skim's 2.1 width; if den is post-battle only, boss must never need to path through narrow passage during fight.
- West forest rim x=-27..-32; east rim +27..+32; south entrance at (0,+31) W=7 with subtle fallen gate and 5-block wide approach. A shallow creek in SE outside clear ellipse e.g. from (22,+16) to (30,+28), depth <=1.0 with simple stones for crossing; water not a trap.

## Landmark exact approximate centers and dimensions
| Landmark | Center (x,z) | Footprint | Restrictions |
|---|---|---|---|
| Ruined hunters camp | (-24,+18) | 9×7 | collapsed tents, wooden fence, 1 extinguished campfire, remains; no active friendly NPC needed |
| Stone obstacle A | (+18,+5) | 5×4×height3.5 | natural boulder suitable for leap failure when boss collides, no fragile player block damage |
| Stone obstacle B | (-19,-10) | 4×4×height3.2 | line-of-sight cover vs whistle but leaves passable detour 4+ wide |
| Bone field | (+20,-17) | 7×5 | vanilla bones/skull props/bone blocks; no new dangerous items |
| Scarred pine | (-20,-20) | trunk dia1–2, height7–11 | slash marks, physically passable around trunk |
| Scarred pine | (+24,+22) | trunk dia1–2, height9 | no boss collision trapping |
| Den mouth | (0,-29) | 8×6 facade | visible before fight; interior loot access after first victory if gated |
| Summon marker | (0,+27) | 3×3 | visually worn stone with carved scratch; reusable beast horn activates ONLY near it |
| Ally staging | (-8,+22) | 4×4 safe ground | allows call from owner item before fight, outside leap target center |

## Generation
- First try existing structure/template/world generation system and surface validation at target position; if no matching system, implement minimal deterministic single-template placement command/encounter activation near player: choose grassy stone upland suitable for 65×65, avoid settlements/protected blocks; do not cross major liquid pockets/bedrock and do not overwrite player constructions.
- Reuse valley if already generated. Store location+rotation in level data, fixed deterministic seed. Do not regenerate every use; do not force-place 65×65 above every player. If placement not possible, show failure and leave item unchanged. Limit candidate validation to finite number e.g. 16 local positions, no endless retries.
- Preferred integration: same world encounter entry pattern established in 1.7.1/1.7.2, not a custom world dimension or thousands of new structures. Worldgen rarity is not part of this patch unless existing structure naturally supports it. A map/wayfinding enhancement can come later.
- For developer testing, add **one permission-checked command** to ensure/place valley near current position when safe, with command name matching established mod conventions (e.g. `/slavicmyths debug place_skim_valley`). Avoid registering command that destroys blocks in survival without confirmation.

## Decor palette and survival integrity
Palette: grass blocks 40%, path/podzol/coarse dirt 22%, exposed granite/andesite/stone 24%, moss/lichen 4%, wooded border 8%, bone/ruin props 2% approximate visible area only. No excessive red ritual magic; this is a **natural beast valley**, not Marinka's witch yard. Fallen pine, stones, claw scoring, hunters' broken camp, deer bones, sparse torches at cave. Night readable (some fireflies ambient, not excessive permanent lighting).
- Place cover such that player can counter whistle, but boss cannot be permanently trapped by 2-block-wide columns. Ensure at least two 5-block-wide transit lanes near central zone. Mob's long tail must not cause collision/head clipping into cliff.
