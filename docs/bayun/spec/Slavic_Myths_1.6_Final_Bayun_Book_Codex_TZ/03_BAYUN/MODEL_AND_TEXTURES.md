# Kot Bayun — actual production geometry, material, UV, texture and encounter site

## Approved art
`09_ASSETS/references/02_bayun_character.png` main full-body, six behavior scenes. `04_overview.png` secondary close-ups. Actual model must resemble these: old massive dark cat, not kitten, vanilla cat resize, giant wolf or human-feline warrior. No borrowed Disney/other-game assets.

## Coordinate convention
Dimensions in **Minecraft blocks** and **model pixels (16 px = 1 block)**. Entity rests on Y=0, facing local negative Z, X left/right, Y upward. Ground footprint approximate 1.40 wide × 2.00 long including hind quarters, tail reaches ~0.75 farther backwards; standing top of ear Y=1.52; standing shoulder Y=1.18; sitting top ear Y=1.67. Use view transforms consistent with hitbox width 1.25, height 1.65, step 0.6. Pivot geometry reflects realistic articulation; controller may scale to adjust actual bbox while matching relative proportions.

### Body units
- Torso: width 0.92 blocks, vertical span Y=0.55–1.28, length Z=-0.60..0.72; **elliptical multi-cube volume**, 3 stepped layers, not a single texture cube. Back peak Y≈1.28; underside at Y≈0.46.
- Neck/ruff: around Z=-0.51 to -0.80, Y=0.85–1.41, width 1.18; 24+ separately layered jagged clumps front/side; at least 3 depth planes to cast silhouette. Color dark back to warm ash chest.
- Skull: width 0.64, height 0.50, depth 0.56; center (0,1.30,-0.85), squarish cheek volume; nose tip local Z≈-1.22; snout 0.37×0.22; two clear cheek pillows not flat face.
- Left/right ears: triangular stepped 3–4 segments, height 0.25 above skull; pivots local (±0.28,1.50,-0.88), independently rotate yaw ±12°, roll ±22°. Tip tufts step 3–5 pixels. Inner ear muted #A78A82, not bright pink.
- Eyes: almond 0.17×0.095 blocks at ±0.17 from center, Y≈1.36; amber #EAA23F with golden highlight #FFD16A 1–2 pixels, narrow dark vertical pupils 2–3 pixels; eyelids are separately animated (blink 3–5 frames), eyelight subtle, NOT bloom spotlights.
- Whiskers: 3 per side using thin planes/quads anchored at muzzle sides, lengths 0.23–0.40 blocks, asymmetric; visible in silhouette but not thick swords.
- Front legs (2): pivot near X±0.33,Y=0.89,Z=-0.40, two segments upper 0.39 and lower 0.36; paws 0.25×0.13×0.29, toes indicated by texture and geometry. Hind legs have larger haunches Y=0.82,Z=0.46; knee joints articulate for sitting/coiling.
- Claws: per front paw at least 3 individually visible retractable curved silver tips, 0.09–0.14 block extension; default tucked away, emerge over 4 frames during attack and retract during recover. Never float on static idle.
- Tail: 5 segmented pieces with independent yaw/pitch/roll; base at (0,0.98,0.66), segments lengths 0.22,0.22,0.20,0.19,0.16 blocks with irregular width 0.25→0.34→0.20; fluffy brush tip. Curves C-shaped during sleep; erect S-shape during stretch; angry thrash has higher frequency.
- Fur ruffs: cheek tufts 3 layers, shoulder crest, flank lower row, tail fringe, optional brows; vertex/plane count under reasonable ~200-350 cubes / 8k-15k triangles budget, measure actual fps and LOD/culling. Wings/horns/human face prohibited.

## Pixel art UV requirements
Color palette: #202027 deep shadow, #302E36 graphite, #44424B outer mane, #5B585E midfur, #7D7473 cheek highlights, #AE9E8E chin/inner chest, #EAA23F iris, #F9D98C iris gleam, #BDBBC0 claws, #684A34 nose/lips; alpha only for whiskers and independent fur fringes. 256×256 primary skin; ≥64×64 detail face UV allocation, each eye at least 12×8 texture pixels, claw edge spec at least 6 pixels; 64×64 *minimum* for accessory texture, 32×32 *minimum* for small icon; NO 16×16 production textures. Nearest filtering, avoid blur and visibly tiled scribbles. UV islands must not mirror facial asymmetry when a non-symmetrical detail exists. Model native source (Blockbench `.bbmodel` or GeckoLib-compatible rig), exported game model/renderer, painted UV source and PNG committed.

### UV blueprint (normalized within 256 canvas; if atlas generator uses a different layout document it)
`face_front` x=0..95 y=0..79; `torso_sides` x=96..223 y=0..95; `legs_front` x=0..127 y=80..159; `rear_and_haunch` x=128..255 y=96..175; `tail_5_segments` x=0..159 y=160..223; `ruff_cheeks` x=160..255 y=176..239; `claws_ears_whiskers` x=0..255 y=240..255. Texture bleeding pad 2 pixels around UV islands; don't overlap islands absent intentional same material.

## Pose & effect quality
Standing with prominent wedge-shaped chest, heavy shoulders and low hips; no giant floating head. Sleeping must morph skeleton to a convincing closed C-shape: chest near ground, muzzle near tail, forepaws tucked, hind paws under hip; not simply rotate upright model 90°. Ears tail jaw eyes and paw joints must be separately animated. 60 FPS client interpolation between keyed server states; authoritative action start tick synchronized. Particle rings amber during warn → burgundy-violet at hypnosis, bounded ≤25 particles/tick within nearby active encounter, no full-screen strobe.

## Spawn / site: small, real usable world place
Rare natural **forested** encounter site: old root-and-stone nook with 1 large ancient stump/platform and a narrow rust-colored wrought-iron post (folkloric nod), no invented village, no mega-dungeon. Approx footprint 13×13 blocks, central 5×5 open platform, visible relief 1–3 high, 2+ exit corridors at least width 3 within 15-block danger radius; NO enclosing walls causing inescapable song. Minecraft-native stone/wood from existing blocks favored; add unique bespoke detail only when it improves silhouette. Generation must validate stable support on sampled terrain; no floating platform, cutting village house or bedrock; no dangerous extreme terrain placement. Rare enough for discovery, but don't prescribe untested seed probability. Worldgen anchor and Bayun persistent UUID must survive chunk unloaded/reloaded; server must not force-load.

## Production QA visual shots
Screenshots full body front/side/back at 2, 6, 12 blocks; sun/noon and dusk; pose sleeping/stretching/milk/song/leap; inventory icon at GUI scale 2 and 4; verify geometry no texture missing/flickering, no feet clipping, whiskers and eyes visible. Texture atlas and native model must be included.
