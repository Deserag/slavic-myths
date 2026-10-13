# DOBRYNYA NIKITICH — definitive 3D appearance / geometry / pixel textures / UV

## Single authoritative reference
`05_Visual_References/Dobrynya/APPROVED_Dobrynya_white_hair_STUBBLE_spear_shield.png`. It is the LATEST user-approved look. Any earlier bearded/brown-haired Dobrynya render is OBSOLETE; do not use it. The image is an artistic concept, not a physically correct UV template: use the exact written proportions, especially the spear, white stubble, width and height. Cross-check the separate technical front/side/back/top diagram.

## Silhouette and visual differences from Ilya
- Total standing model height **2.40 blocks**, with 2.20-block tall server collision box and 0.80-block width (fine-tune hitbox to prevent passage clipping). Character head top at visual Y=2.4, boots bottom Y=0.00.
- Proud broad-shouldered fighter, more horizontal upper torso than Ilya: shoulder-to-shoulder geometry span 1.08 blocks, chest ~0.87 blocks, pelvis ~0.63; do NOT simply rescale Ilya geometry or texture.
- Motion signature: side-stepping, spear-point guard, sharp purposeful footwork; not Ilya's heavy two-handed mace/aggro-and-wall shield.
- **Hair** stark warm-white / silver, short frontal fringe and shoulder-length locks at back (pixel segments, without smooth strands), gathered in a partial back binding. Face: **short pale gray-white stubble** no more than 1–2 px in close-up, upper lip and jaw; facial outline stays clearly visible, **NO beard volume, no chin hanging below the block head**. Brows gray-white. Eyes icy blue. Adult confident, not elderly wizard.
- Steel/iron armor **gray is dominant (>= 60% of visible armored area)**: hammered riveted brigandine/chain shoulders and chest, layered gray plates, silver highlights, sparse warm-gold rivets and crimson fabric strips. Keep recognizable dark crimson cloak with pale Slavic motif, but do not fill torso with vivid red.
- Long spear **3.60 blocks from butt to tip (=1.5 × 2.4)**, not sword, bronze/ochre wood haft with four dark leather grips, small muted-red cloth tassel near long bright triangular steel spearhead. Shaft thickness ~0.09–0.11 block visual, iron head ~0.52 block long, tip sharp but voxel-safe. Spear may cross character vertically but must not penetrate terrain, vanish in first person or float in sheathed pose.
- Shield oval/rounded-heater about 0.52 block wide × 0.96 tall × 0.12 thick, red face with pale gold eight-point motif, gray iron boss/edging. Opposite arm from spear. Shield is an independent animated pivot, not a flat no-depth PNG.

## MODEL REFERENCE coordinate system
Entity model local +Y up; +Z forward, +X model's left (align actual renderer coordinate if conventions differ). Units blocks; origin feet at Y=0. Root translates + rotates for locomotion. Suggested piece envelopes (minimum actual voxel-detail count significantly higher):

| Part | Center xyz in blocks | Size xyz | Rig pivot | Note |
|---|---|---|---|---|
| boots L/R | +/-0.19,0.13,0.02 | .27,.26,.46 | hip | dark brown foot pad and steel toe |
| shins L/R | +/-0.19,.54,0.00 | .27,.52,.27 | knee | greaves have 3 raised plates |
| thighs L/R | +/-0.19,.97,0.0 | .30,.41,.33 | hip | asymmetric metal lames |
| waist/belt | 0,1.26,0 | .66,.22,.36 | pelvis | embossed bronze buckle and strap |
| torso/chest | 0,1.69,0 | .87,.64,.45 | spine | silver steel 3× rows of scale plates |
| pauldrons L/R | +/-0.48,1.93,0 | .22,.27,.53 | shoulder | total shoulder span ~1.08 |
| upper arms L/R | +/-0.53,1.64,0 | .23,.42,.30 | shoulder | three rivet columns |
| gauntlets L/R | +/-0.53,1.31,-.05 | .25,.28,.31 | elbow | working grip bones |
| neck | 0,2.045,0 | .25,.13,.25 | spine | crimson collar |
| head | 0,2.22,0 | .43,.42,.43 | neck | squared strong jaw, blue eyes |
| hair fringe | 0,2.405,-.10 | .46,.065,.32 | head | front 2–3 block clumps |
| back hair | 0,2.18,+.27 | .47,.42,.16 | head | white locks end at collar |
| short stubble | head surface lower face | <=.015 depth | head | 1–2-pixel painted texture ONLY |
| cloak | 0,1.34,+.285 | .69,1.12,.07 | upper spine + cloth bones | three strips + bottom hem motion |
| shield | hand-dependent | .52,.96,.12 | non-spear wrist | front red motif; three-pixel rim |
| spear | hand-dependent | length=3.60 | two-handed grip logic | point up in idle; fore/aft in attacks |

The gauntlet grip must adapt to spear pose: hold shaft with dominant hand, offhand shield remains independent; animate shield-side arm, DO NOT show 3 hands. Secondary spear stabilizing pose is allowed only when shield lowers and its wrist/pivot permit holding it without hand duplication.

## Texture maps and palette
Minimum **64×64 pixels per material atlas**; hero base **128×128 RGBA** strongly preferred, spear **64×128**, shield **64×64**, cloak **64×128**. Use nearest-neighbor filtering, pixel precise 1:1 source, no blurred texture, no 16×16 novelty textures. No high-res realistic rendering textures; maintain Minecraft readable groups of 2–5 pixels and strong outlines.

Palette (sRGB HEX, use as anchors, vary with shadow/value by <=~15%):
- Armor deepest shadow `#24262B`; iron `#565B60`; mid-steel `#818890`; edge glint `#B5B8B7`; bright rivets `#D2C9AC`.
- Hair `#E3E3DD`, lowlight `#AFB4BA`, bright `#F5F4E9`; STUBBLE `#B9BCBB` low-contrast; eyebrows `#CDD0CE`.
- Skin light `#DCA88B`, shadow `#AD7561`; blue eye `#44769E`.
- Crimson cloak `#7F262D`, deep fold `#4D1C25`, trim `#C49C66`; leather belt `#53372A`, leather shade `#33251F`, buckle brass `#B58B41`.
- Spear shaft `#68442A` with accents `#97724E`; spearhead `#C5C8C9` edges `#EEF0E9`, darkest crease `#57616A`; tassel `#A43D38`.
- Shield field `#962D32`, rim `#AF914B`, motif `#E4C67B`, central boss `#747F84`.
- Avoid making Marinka's crimson-black witch palette indistinguishable from this gray-white armored hero.

## UV instructions
Establish rectangles per cube and pixels-per-block by actual model project; map all faces, do not mirror front/back plates, shield emblem, face, belt buckle or cloak runes. UV seams on hidden armor edges; tile chain mail with deliberate shifts, NOT a uniform noisy gray fill. Unique visual motifs: 8-point chest strap rosette at x ~ +/- 0.23 of torso, cloak centered pale star embroidery, gold belt clasp, spear tassel. Clip no UV below seam. Spear shaft repeats 8x8 hand-painted woody weave, head separate non-mirrored faces with a 1px bright blade edge. Transparent pixels only for cloak/fringe edges, not armor fill. Render checks in day/night and with emissive spell effect; armor stays gray.

## Armor articulation / LOD
At 1–8 blocks show hair pixels and spearhead. At 24 blocks preserve wide silhouette, white hair, shield color; avoid overdraw and z fighting. No photoreal mesh and no purely flat cardboard shoulder shields. Cross-check: head not helmeted over hair, white hair remains visible; distinguish from Ilya in an intentionally blurred thumbnail.
