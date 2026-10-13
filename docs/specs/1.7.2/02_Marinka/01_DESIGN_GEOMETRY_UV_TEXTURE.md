# MARINKA — appearance, 3D block model, textures, four views and form variants

## Visual authority
`05_Visual_References/Marinka/Marinka_character_and_abilities.png` is the full style sheet. This character is **a woman and a sorceress** of Slavic folkloric flavor, not a generic Minecraft Evoker, zombie/necromancer, European green-skinned witch, anime character or sleek high-poly game model. Concept panel depicts poses and effects; written geometry/colors are implementation constraints.

## Core shape
Visual height 2.10 blocks (collision H~1.96, W~0.68 in upright pose, physically appropriate). Tall narrow tapered robe, long dark hair in one heavy braid, pale angular face, crimson head ribbon with small gold leaf/clasp; no pointy wizard hat, no crown that adds .5b. Slender defined shoulders, pointed squared sleeves, layered robe down to just above feet; black, dark carmine and aged burgundy with golden embroidery and tiny bone/trinket chains. Skin sallow peach; eyes piercing darker red/pink iris but not full glowing anime eyes. Left palm holds floating red-black fire; her hands remain recognizable in close view. Robe has three separate strips (front central embroidered, left side and back torn outer hem) animated from hip/shoulders.

## Dimensional rig (local blocks)
| Bone/mesh | Center xyz | xyz size | Purpose |
|---|---|---|---|
| feet pair | +/-0.13,.10,0 | .20,.21,.30 | footwear visible below hem |
| legs pair | +/- .14,.44,0 | .19,.57,.22 | hidden under cloak, still animate |
| pelvis | 0,.93,0 | .45,.24,.35 | cloak anchors |
| upper torso | 0,1.41,0 | .55,.63,.36 | layered bodice and embroidered corset |
| shoulders | +/- .34,1.64,0 | .14,.22,.40 | capelet folds |
| forearms | +/- .37,1.16,-.08 | .16,.46,.18 | spell casting hands |
| head | 0,1.89,-.035 | .40,.40,.39 | visible cheek line, no silly huge head |
| braid | .14,1.45,+.28 | .14,.79,.14 | 4–6 articulated links |
| hair mass | 0,1.88,+.22 | .42,.43,.14 | back hair; ribbon |
| outer mantle | 0,1.15,+.23 | .69,1.12,.075 | not flat cuboid skin |
| belt charms | x -0.27..+0.29 at y1.05 | each .04–.16 | bones, coins, rings, talisman |
| robe skirt ring | 0,.39,0 | width .88 taper down | tessellated 8-fold hem, visible drape |

Textures: body+face 128x128 minimum, robe 128x128, jewelry 64x64 and braid 64x64, magical FX atlas 64x64 minimum. Never use 16x16 primary entity texture, blurred interpolation, pre-lit photographic gradients. Use pixel-cluster fabric folds, 1–3px gold stitches, irregular geometric embroidery, grime at hem.

## Exact intended color hierarchy
60% dark wine/black cloth; 15% faded claret lining; 10% gold-brown trinket; 10% skin/hair; 5% red runic accents. Anchor RGB HEX:
- Hair `#272124`, highlight `#4B3434`, ribbon `#812932`.
- Robe almost-black `#21191F`, shadow `#141117`, burnt claret `#52222A`, red dominant panels `#7C202C`, aged rose `#9D4C4B`.
- Gold `#C49B57`, tarnished brass `#7C5A33`; bones `#C5B7A0`, shadow `#7B7468`.
- Skin light `#E0B49E`, shade `#A97772`, eyes `#9B3245` (one or two pixels each).
- Offensive fire base `#B52A47` and inner core `#F85B59`, shadow particles `#2B0C1D`, barrier highlight `#CC5D73`.
- **Don't use purple for all spells**: fire crimson-red, mind curse amber-red/black, clone transitions smoky charcoal, shield a precise deep magenta-ruby ring, aurochs curse gold.

## Key identifiable elements
Left-hand fire hovering .15b above palm, right wrist gold-ring charm, single central bone pendant on belt, three bone amulets/pouches on hip, one old key-like object dangling, tattered triangular cloak edge. Hair braid back must be plainly seen from rear. Top view reveals ribbon placement, broad rear cape semicircle and left-hand glowing palm not detached.

## Two false doubles + real visual equality
Clone render MUST use same renderer model, outfit, textures, face, sound pitch, speed cap and standard attacks as original. No extra sparks, no aura, no static different animation, no nametag difference, no outline/highlight from glowing effect or entity tracking. If ordinary bosses show health bars, replace with ONE shared three-form bar without an individual real-ID marker while clone state is active. Clone attack payloads are gameplay-safe (see AI doc), but that must not expose them via distinct particle trails or hit sparks; damage feedback only after a clone's 24 HP break or target continues to persist. Attack-hit particles identical on all three until clone disappears.

## Magpie form (сорока)
True magpie = black-white angular bird, wing spread 0.90b, body 0.45b, long forked tail .55b; 64x64 textures with light white side panels, dark blue-black wings and head, tiny dark beak, 4 rigid tail cuboids and side feathers. Not raven/crow/owl/bat. Distinct bird shape should be visible at 8–16 block range. Legs tucked, flaps with 4–6 bone sections; transform hides witch hitboxes for scripted short flight only, not permanent immunity.

## Golden-horned aurochs (Dobrynya's curse)
Shorter than courtyard obstructions: shoulders 1.65–1.8b high, length ~2.65b, width ~1.15b, large gold horns spread ~1.45b and glow limited to rune seams. Dark umber body, beige patches, golden horns, clearly recognizable *bull/aurochs*, not generic cow or 8-block giant. Required temporary rig geometry and animation included in its own technical card. Entity faction stays allied to player. On reversion, restore white-haired/stubbled human hero exactly; no duplicate entity, equipment or loot.

## Motion and VFX compatibility
Use common vanilla lightmap, fog shading and particle culling; retain pixel art look. No billboard-only magic barrier pretending to be solid, no reflective modern shaders requirement, no invisible body when she is actually hittable. Test at low render distance and from F1 freecam.
