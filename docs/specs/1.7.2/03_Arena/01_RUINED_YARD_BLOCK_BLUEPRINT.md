# RUINED WITCH YARD — authored 60x60 construction blueprint and materials

## Reference and scale
Approved thematic reference: `05_Visual_References/Arena/Ruined_Witch_Yard_architecture_ritual_basement.png`. Explicit map: `05_Visual_References/Technical/Yard_top_down_60x60.png`. Both must be included in Codex design reference read. Required footprint target **60x60 blocks**: local X in [-30,29], local Z [-30,29] centered at `(0,0)`, surface level local Y=0. Rotatable structures translate/local-transform coordinates, never assume world (0,0). Natural outer terrain fade may spread ~4 extra blocks for blending, but not spawn invasive structures across protected world regions.

## Arena usable space (non-negotiable)
- Primary fight circle center ~X=0,Z=0, radius 20 to 22.5b (effective combat area 40–45b across), 80% free horizontal clearance, at least 5b vertical clearance above at least 85% of center; cover and glyphs along border. Not an empty featureless disc, but NO 2-block-high maze dividing the center.
- Distinct outer ruined yard enclosing the battlefield with irregular fence/wall fragments 1–3b high. At least two broad passages around obstacles. Approach from S (+Z) with 5b-wide entrance and line of sight toward witches' house NW.
- Ground patchwork: packed coarse dirt ~42%, rooted dirt/grass ~24%, gravel/cobble ~20%, charred cracked stone ~10%, ritual red inscribed blocks <=4%. Reuse existing game materials where possible; new blocks only if a visible rune texture is essential, avoid adding dozens of pointless blocks.
- Geometry must avoid levitating fence pillars, dangling roof chunks or arbitrary holes that trap player/hero; ruins are partially collapsed, braced and physically plausible.

## Feature position & detail table (local coordinates center and xz footprint)
| Element | Local region X,Z | Main dimensions / Y | Block detail and internal contents | Gameplay |
|---|---|---|---|---|
| House/Terem | x=-29..-12, z=-27..-9 | 18x19, intact sections max 13b high | 2-storey timber witch house with steep damaged gable, crossbeams, 2 chimneys 1 broken, birch-shingle dark roof, bent porch, 4 dark windows, 2 partly boarded; blackened planks and copper/brass bells, carved raven/folk lintel; some playable interior with table, broken stair, herb drying | line of sight partial, not hard combat prison |
| Dry well | x=20..26, z=-10..-4 | 7x7, rim y1, 5b visible drop with sealed bottom | mossy stone, bent roof supports, crank+bucket, wood scraps, no accidental fall-death | cover; decor, not secret entrance by default |
| Broken barn | x=12..27, z=-29..-14 | 16x16, roof <=7b | hay, witch root bundles, dirt floor, charred double door, haycart, lattice storage loft, fragmented roof | peripheral movement, no block on central dash |
| Ritual hub | center x=0,z=0 | outer runic radius 6, 1b circle markings | red engraved irregular ring segmented 16/24 segments, central low stone altar 1.5b tall, charred wax, 4 candles, 2 suspended bone mobiles | boss intro, floor magic, defeat opens basement hatch |
| Minor sigils | x=-10,z=10; x=11,z=9; x=-5,z=-12 | each 3–5b | red runes with 1b low stone marker, shattered ritual jars, ember ash | boss blink landmarks and phase FX |
| Cursed trees | x=-24,z=14; x=22,z=17; x=-21,z=-2 | heights 7–10b | twisted 3–5-branch dead wood, 1–2 lanterns, rope+bone and tiny red tatters | environmental identity, avoid canopy occluding entire center |
| Wagon | x=-19,z=21 | 5x3x3 | 2 axles, 4 square wheels, scattered sacks/roots/herb crates | human-scale cover |
| Lean-to | x=19,z=22 | 8x5x5 | tilted awning, empty barrels, shelves, pots/earthenware | corner dressing |
| Main entrance | x=-3..3,z=+29 | width 7, posts y0–5 | broken gateway post pair, low rune totem, stone causeway leading into court | encounter ownership/lobby boundary |
| Outer fence | near 60x60 perimeter | fragmented 1–3 high; gaps 1–6 wide | half stone, half dark oak poles, woven fence, charred hanging cloth; no square modern wall | visual boundary and fight radius |

## Thematic assets (counts/density, not random scatter)
- Charred lanterns 9–14 (max 1 per 4b near center); candles 22–32 including indoor/basement, 2–4 clumps; bones/ornaments 14–20 items; herb bundles 12–18 groups; old jars 12–16; wooden stools 3, ritual tables 2, hanging charms 6, shabby fences 18–26 segments; cart crates 5–8. Never over-fill central center: decorative collision footprint of any prop max 1x1 unless marked cover.
- Distinct material palette: aged wood `#4C352D`, blackened oak `#2C2928`, gray cracked stone `#666763`, moss `#53604D`, ash earth `#6C5B52`, ritual crimson `#7E2A35`, candle warm `#D3A76A`, old bone `#B9B29F`.
- Avoid modern machinery, neon beams, glass skyscraper silhouettes, nonsense physics or decorative mobs permanently preventing the ritual. No natural vanilla village houses.

## Robust world placement & boundaries
- Follow existing world structure generator/story map integration. If the current system has no agreed trigger, implement a safe explicit admin/test command and an existing quest marker registration hook; do NOT generate naturally everywhere without design approval. Respect block protection, dimension suitability, ground solidity, sea/water/lava avoidance, existing building overlap, chunk validation.
- For uneven ground, sample median surface within 60x60, terraform only minimal local area, distribute support piers beneath house/well/ritual circle, blend outer 4b; do not hollow the entire 60x60 into sheer platforms. No natural village or ruin destruction without worldgen policy.
- Permanent encounter controller placed at ritual core with UUID, owner/progress save data; refuse duplicate generation in same reserved footprint; can regenerate missing props only with explicit admin/debug operation, not automatically on load.
- Participant bounding box a roughly 29b half-extent, fight leash 25b from center; Marinka blink and flight endpoints strictly within inner 19–21b radius unless intended cover. Prevent teleport into house closed room or into 2b space.
