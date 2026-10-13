# Kartaus Ryzhiy Us — design, model geometry, texture and UV

**User-approved:** `06_References/Approved/APPROVED_Kartaus_Character_Animations.png`. Render is deliberately Minecraft blocky; full front, profile, back and top, head, crown, chest, eye-staff and battle action are all specified in PNG. DO NOT substitute a generic wizard, witch, red-eyed illager with robe texture or copy Marinka clothing. Red moustache is the first instantly recognizable facial characteristic. This is a commander and occult sovereign, not a clone illusion caster.

## World dimension
Total 2.70 blocks = 43.2 model pixels, ground origin (0,0,0), capped at crown tip. Nominal standing width 1.05 blocks (including armored pauldrons); depth ~0.52 block; hitbox 0.92 x 2.65 blocks, eyes ~2.33 blocks. Staff length about 2.45 blocks: from ground near right hand to just below crown height, square dark carved shaft, iron cage containing a glowing red-black ocular crystal. Staff MUST animate independently; don't render as a small item hovering at wrist. Moustache protrudes on LEFT and RIGHT from face, thick burnt red strips visible from profile.

## Model bone hierarchy and dimensions (Minecraft units = 1/16 world blocks)
| Bone | Pivot (x,y,z) at idle | Shapes in model px | Key characteristics |
| --- | --- | --- | --- |
| core | (0,21,0) | robe upper 13x18x9, waist 11x6x8 | coal black, metal chest ridges, hanging front blood-red sash. |
| skull | (0,35,0) | head 10x10x10; angular face | dark steel-grey hair, human pale weathered face, deep brows, RED-ORANGE MOUSTACHE extends side 2–3px. |
| crown | (0,41,0) | dark irregular 11x5x11 with 3 raised antler/crossed metal teeth <= y43.2 | red inscription cutouts; NO smooth round horns. |
| pauldrons | (+/-9,29,0) | 7x6x9 each + layered plates | asymmetrical angular dull-steel shoulders, red seam; slightly larger right to carry staff. |
| arms | (+/-8,26,0) | forearm 5x10x5, hand 4x3x4 | worn steel cuff, dark long sleeves. |
| lower robe front/back | (0,11,0) | three independent skirt panels left/right/center, 4x16x2 each | animate split in stride, deep maroon underside; not a static full cylinder. |
| legs | (+/-3,9,0) | 5x12x6 and 6x5x7 boots | mostly under robe, essential motion for walks/staggers. |
| cape back | (0,30,+5) | 13x25x1, 3 segments | back red single emblem (eye/cross-like heraldic rune), aged hem; cloth sways during cast. |
| moustache L/R | (+/-5,33,-5.4) | each 4–7x2x2 voxel strips | single continuous *recognizable* rust/orange crescent on both sides, not a long beard. |
| staff & floating gem | right hand, full shaft | dark carved length 38–41px; metal cruciform cage 7x8x7; inset red eye 3x3 | ocular lens can pulse via texture & a single small shaderless emissive cuboid; avoid bright permanent light source. |

## Atlases
Main **256x256 RGBA** recommended due cape and intricate crown; minimum acceptable 128x128 with hand-authored motifs. UV partitions on 256 atlas: head/face 0:52 x 0:52, crown 56:116 x 0:64, torso front/back 0:72 x 60:130, arms 76:142 x 68:142, robe 0:100 x 144:238, cape back 144:216 x 72:180, sash/buckles 148:248 x 188:238. Maintain 2px gutters. Separate **64x64 staff** RGBA texture + optionally eye glow 32x32 atlas. Paint front and back independently, don't mirror lettering or hero motifs. All pixel faces use correct UV inside atlas. No 16x16 base sprites.

## sRGB palette
obsidian `#18181D`, charcoal `#2B252A`, black-purple edge `#3A303A`, tarnished iron `#646269`, metal highlight `#929095`, deep crimson `#771F28`, fresh banner crimson `#AB2B30`, orange moustache base `#B94C24`, red moustache high `#DA6735`, pallid skin `#B8A28C`, grey leather `#5A4B47`, witch violet magic `#6C2B9A`, eye/core `#D62D3B`, bone ornaments `#B8A68C`, brass worn `#A17A46`.

## FX model visibility
Idle cape sign readable from rear at >=12 blocks. Red moustache unmistakable front at 8–15 blocks. Staff head must point toward curse target, not spin independent of hand. In rage, transient emissive eye and glowing cape rune + red particles, but keep base textures intact. No floating skulls or extra summoning props by default. Commander authority communicated by posture, banner staff and heavy armor/crown, not high-speed blinking around arena.
