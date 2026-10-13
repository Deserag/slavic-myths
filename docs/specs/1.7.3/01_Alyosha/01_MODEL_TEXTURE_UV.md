# ALYOSHA — geometry, silhouette, materials, UV/texture (REQUIRED)

Approved visual reference: `06_References/Approved/APPROVED_Alyosha_BLOND_BLUE_EYES.png`, not the previous dark-haired sheet. Four views + actions in approved board. The approved design must take precedence over any generic model in mod.

## Identity and scale
- Young man, clever lively expression, **messy wheat-blond gold hair**, **saturated blue eyes**, pale skin, **NO beard**. Tall approximately **2.15 blocks / 34.4 model pixels at default 16 units per block**. Do not make him as wide/tall as 2.5-block Ilya, and do not dress him in Dobrynya's gray plate armor.
- Hero slim agile silhouette: maximal neutral arm-to-arm torso width ~0.82 blocks; actual collision AABB 0.72 W × 2.15 H unless existing navigation requires adjustment; eye height 1.90; step height 0.60. Proportions: head ~0.53 W × 0.55 D × 0.49 H; torso height 0.73; legs from y=0–0.86; pelvis + belt around y=0.86–1.02. Feet black-brown leather shoes. Human proportions remain voxel Minecraft-readable.
- Clothing: cream undershirt, red neck scarf, diagonal brown leather strap, layered brown leather jerkin with small dull iron fasteners, asymmetrical beige/red tunic, slim wrist wraps, dark leggings, brown boots, small light crimson travel cloak or long scarf panels. NO broad steel cuirass and NO giant pauldrons. He has practical travel bags and quiver.
- Default right hand curved short saber, left or back equipped short composite wooden bow; swap to bow with 6-tick arm transition rather than holding saber and bow clumsily at same time. Arrow quiver across back with clearly modeled 3–5 shafts/fletchings.

## Local model geometry — relative boxes in units of Minecraft block
Set model root at feet, +Y upward, +X entity right, +Z forward. Implementation can use pixels (×16) or Blockbench but MUST match shapes and pivots.

| Part | center X,Y,Z | width×height×depth blocks | Pivot | Geometry notes |
|---|---|---|---|---|
| Foot/boot L | -0.19,0.17,0.09 | .25×.34×.37 | (-.19,.38,0) | layered heel and shoe toe |
| Foot/boot R | +0.19,0.17,0.09 | .25×.34×.37 | (+.19,.38,0) | offset belt knee patches |
| Lower legs | ±.19,.54,0 | .22×.51×.25 | (±.19,.88,0) | leather knee strips, not steel boots |
| Hip+tunic | 0,.94,0 | .63×.20×.37 | (0,1.02,0) | two fluttering split skirt flaps |
| Torso | 0,1.38,0 | .61×.76×.38 | (0,1.12,0) | cream sleeves under jerkin, belt diagonal |
| Upper arm L/R | ±.405,1.53,0 | .22×.63×.26 | (±.39,1.78,0) | left arm optimized for bow elbow |
| Forearm/palm | ±.41,1.20,0 | .19×.36×.19 | (±.41,1.43,0) | separate grip geometry |
| Neck | 0,1.80,0 | .25×.14×.24 | (0,1.80,0) | scarf scarf-ends, cloth overlays |
| Head | 0,1.97,0 | .53×.48×.50 | (0,1.82,0) | cheeks/eyebrows; eye pixels blue |
| Hair layered cap | 0,2.225,0 | .56×.16×.54 | (0,1.97,0) | independent chunky blond locks, varied lengths, do not form helmet |
| Back cloak | 0,1.30,-.27 | .57×.91×.06 | (0,1.73,-.23) | 3 articulated cloth pieces with red-gold border |
| Quiver | +.23,1.42,-.34 | .18×.49×.17 | (+.23,1.57,-.25) | textured arrow nocks; no massive backpack |

Scene-specific collision remains centered on feet; avoid model clipping ground when crouching and prevent overly tall collision if cloak bobs.

## Weapon sizes / attachment
- Saber overall **0.98 block**, metal blade 0.70 long, 0.10 wide, gently curved forward; bronze guard, dark wooden grip 0.18, gold pommel 0.08. Pivot at right hand; distinct from Ilya mace and Dobrynya 1.5×height spear. Swing must visibly trace weapon tip.
- Bow unstrung height ~0.93 block and depth ~0.08; bent ends visibly recurved, brown/gold wrapped. Draw state stretches bowstring and visible arrow on handed model; arrow flight on server projectile path. Quiver arrows are prop, not unlimited visual magical conjuration.
- Scarf tails ~0.35 block and cloak strips ~0.65 flexible length; animatable as two-to-three rigid lobes. Avoid unsafe simulated cloth physics; keyframe oscillation sufficient.

## Color palette (exact defaults, RGB hex)
| Zone | primary | shade | edge/highlight |
|---|---|---|---|
| Blond hair | #D7AE54 | #A77836 | #F5D981 |
| Blue eyes | #2C83C9 | #174F84 | #79CBF5 |
| Skin | #E8B38C | #BD7F5C | #FFD7B1 |
| Cream shirt | #E9D6BC | #BFA78C | #FFF0D8 |
| Deep red scarf | #982D2B | #5C1F22 | #C85A46 |
| Tan jerkin | #8A5D38 | #563822 | #BC8652 |
| Dark brown boots | #453026 | #251D1A | #74513A |
| Iron small fittings | #8C9297 | #565B63 | #BEC4C8 |
| Gold embroidery | #CBA044 | #906626 | #F0CC70 |
| Steel saber | #C8D0D6 | #727E8E | #EDF5FC |

## Texture and UV rules
- Body/gear base atlas **128×128 PNG minimum**, nearest-neighbor, alpha preserved. Distinct reusable face area 32×32+ (portrait can be additional). Saber **64×64** (item inventory 32×32 minimum), bow **64×64**, call item **64×64** (inventory 32×32+). Use 3–6-tone materials with deliberately clustered pixels, not noise/random tiny dots.
- Allocate UV islands with padding >=2 atlas pixels: hair cap+strands x0–31,y0–31; front/back face x32–63,y0–31; torso upper x0–63,y32–63; arms x64–95,y32–79; legs x0–63,y64–111; quiver and cloak in x64–127,y80–127. These are **recommended logical atlas zones**, not a forced limitation on legacy texture layout; no UV overlap for distinct colored faces.
- Eyes TWO readable blue 3×3 or 4×3 pixel groups separated by skin bridge; no brown dots. Hair has broken front bangs with diagonal asymmetry; hair brighter than leather. Neck scarf must read from front/back even from 12–20 block distance.
- Flat color fallback: light tunic, red scarf, golden head, brown belts; maintain visual distinction from steel-gray/white-haired Dobrynya and large gray-red Ilya.
- Resource names suggested `textures/entity/alyosha_popovich.png`, `models/entity/alyosha_popovich`, `textures/item/alyosha_whistle.png`; adapt existing directory conventions.
- Orthographic previews: generate 4 in-game screenshots FRONT/SIDE/BACK/TOP for QA at daytime and nighttime, side-by-side with reference; front weapon visible and quiver not intersecting cloak.
