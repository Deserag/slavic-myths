# Kuzya model: exact implementation geometry and rig (all values in Minecraft model pixels: 16 px = 1 world block)

## Coordinate model
Set model origin at center of shoes at ground Y=0; positive Y is up (convert to your renderer's pivot coordinates explicitly). Target bounding box **0.45–0.52 blocks wide, 0.38–0.46 deep, 0.62–0.70 tall**, canonical height **10.24 model pixels = 0.64 block**. Animated hair may extend to 11.0 px; gameplay hitbox width 0.52 b, height 0.70 b; do not cause head clipping on stove. No vanilla scaling of child model.

## Body volume (recommended initial cubes; all must be subdivided where silhouette demands)
| Named bone | Center x,y,z (px) | Approx extents xyz (px) | Notes |
|---|---|---|---|
| root_pelvis | 0,3.30,0 | 3.4,1.5,2.3 | neutral pivots anchored to center; do not animate hitbox for breathing |
| torso | 0,5.10,0 | 4.3,3.1,2.8 | warm red-brown patched tunic, wide sleeves |
| neck | 0,6.95,0 | 1.9,.8,1.8 | mostly hidden by collar/hair |
| face | 0,8.05,-.35 | 5.0,3.7,3.65 | flattened puppet face; two eyes actual textured geometry |
| hair_cap | 0,9.05,0 | 7.4,3.2,6.3 | volumetric asymmetric dark fibers; not sphere/no flat plane |
| arm_R / arm_L | ±2.80,5.10,0 | 1.50,3.0,1.8 | short sleeve plus dark hand, extra elbow bend |
| leg_R / leg_L | ±1.08,1.55,0 | 1.55,2.6,1.9 | chunky short legs |
| shoe_R / shoe_L | ±1.08,.55,-.45 | 1.90,1.0,2.65 | non-floating shoes, earthy charcoal |
| nose | 0,8.06,-2.28 | 0.80,.45,.55 | tiny, deep brown/black nose, NOT rosy clown nose |
| pupil_L / pupil_R | ±1.15,8.55,-2.15 | 0.55,.64,.04 | slight separate mesh for look animation |
| eyelid_L / eyelid_R | ±1.1,9.0,-2.15 | 1.05,.18,.08 | blink scale or bone translation |
| mouth | 0,7.35,-2.15 | 1.05,.22,.10 | tiny oral gap/open-close speech shapes |

Box sizes are **model construction starting dimensions**, not an excuse to collapse character into nine rectangular cubes. Split face into cheek, eye socket, brows; sleeve fabric into at least 2 slopes; hair into many short irregular strands. Ensure head+hair is about 56–61% of visible character height, limb/torso silhouette stocky. Feet rest on ground without half-pixel float.

## Hair geometry (critical)
At least **28 individually discernible cubic tuft clusters**, not just texture noise; distribute front 7–9, left 5–6, right 5–6, rear 7–9 and top 6–8, allowing shared clusters where joined. Tufts should have 2–4 stepped segments of different depth and length; silhouette should read as irregular 'thatch' with off-center silhouette. Front fringe partially covers upper eyebrows, NEVER both eyes. Back hair extends toward top of tunic. Colors `#261B19` shadow, `#38261F` brown main, `#69452F` warmed tips; never yellow/blond.

## Detailed face
Broad rounded/boxy brow; dark face `#211717`, `#382621`, lifted cheeks `#654439`; big pale whites `#D5CEC0`, irises very dark `#433D37` with 1 pixel glints. Eyes naturally look upward/sideways, not anime sparkle. Lips are tiny warm dark-red line; nose dark rectangular bump. Brows can angle left/right independently ±12°. Mouth opens 0.5–1.2 model px during voiced frames; do NOT rotate whole head with speech.

## Clothing and props
Deep weathered russet tunic `#763128` / `#9F4D38`, worn slightly oversized, crude stitching `#C59F71`, subtle sleeves bands, patched seams, belt segment, dark patched trousers, charcoal-gray slippers or small bast footwear. Hand shape 3 stub fingers with optional thumb; visible while eating/gesturing. Build detachable *food prop bone*, empty by default, attaches to right hand only during eating.

## Required bones & transforms
`root`, `pelvis`, `torso`, `neck`, `head`, `jaw`, `hair_root`, `fringe_01..n`, `back_hair_01..n`, `eye_L`, `eye_R`, `eyelid_L`, `eyelid_R`, `brow_L`, `brow_R`, `shoulder_L/R`, `elbow_L/R`, `hand_L/R`, `hip_L/R`, `knee_L/R`, `foot_L/R`, `food_prop`, optional `scarf/hem`. Each limb has real proximal/distal movement; hair sway ≤8° and <0.12 b offset; avoid physics explosion on crouch.

## Compare with reference
- `references/crops/MODEL_TURNAROUND_APPROVED.png`, `FACE_APPROVED.png`, `HAIR_HEAD_APPROVED.png`, `BODY_CLOTHES_APPROVED.png`; `diagrams/KUZYA_PALETTE.png`.
- Steve standing next to model: Kuzya's highest tufts ~0.64–0.70 block, approximately mid-shin to knee depending Steve pose, not waist-high.
- No transparent billboard hair, no infant villager face, no light beige model, no blonde straw mop, no excessive red scarlet armor.
