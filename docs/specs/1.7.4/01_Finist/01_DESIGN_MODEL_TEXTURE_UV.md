# Finist — approved model, appearance, geometry, texturing and UV

**Approved picture:** `06_References/Approved/APPROVED_Finist_Character_Animations.png`; its front / 3-quarter / back / top, bird views and action panels are binding creative references. Model must look at home among high quality blocky Minecraft characters (cube/low-poly, hard edges, textured details), not smooth humanoid mesh. This hero differs clearly from Ilya's grey massive armor and Dobrynya's white hair/heavy spear; he differs from Alyosha's red/light equipment and sabre/bow.

## Scale & anchors
World model from sole to crown **2.25 blocks / 36 model pixels** (model 'pixel' = 1/16 block). Default actual width excluding animated cape ~0.75 block; depth ~0.40 block. Hitbox approx 0.76 wide x 2.20 tall, stepheight following existing heroes. Attach origin at floor center (0,0,0), humanoid forward = local -Z. Dominant right hand holds sword; falcon perch on left forearm in peace. Eyes at 2.00 blocks. Keep geometry visible in 3rd and first-person inspect, avoid clipping at idle.

## Modular cube geometry (in model pixels, symmetric unless noted)
| Bone | Pivot (x,y,z), Y above ground | Visible cuboid(s) dimensions / location | Notes |
| --- | --- | --- | --- |
| Root/pelvis | (0,15,0) | belt torso overlap 10x4x7 | rotation only root turns. |
| Torso | (0,22,0) | chest 11x13x7, waist 9x5x6 | grey segmented chainmail front, ivory tunic visible below. |
| Neck | (0,31,0) | 4x2x4 | collar red embroidery line. |
| Head | (0,33,0) | 9x9x9, top <= 36 | pale skin, square blue eyes, restrained brow, NO beard, finely shaded nose. |
| Hair | (0,36,0) | layered 10x3x10 + rear locks | sandy light-brown/blond hair, cut just below ears with separate 2-3 cube strands. |
| Upper arms L/R | (+/-7,27,0) | arm plates 4x10x5 | minimal shoulder guard: left decorative golden feather, right muted steel; cloth white sleeves. |
| Forearms/hands | upper elbow | 4x8x4 and 3x3x4 hands | brown leather bracers; left perch / casting pose. |
| Legs L/R | (+/-3,14,0) | thighs 5x10x5 | white tunic slit opens over dark brown pants. |
| Shins/boots L/R | (+/-3,6,0) | 5x8x6 | warm brown block boots, blue/bronze cuff. |
| Cape spine | (0,30,+4) | hanging upper 12x14x1 / lower 14x13x1 | deep blue, gold falcon emblem on BACK; two cloth panels articulated with 3 chain segments for running/dive; no rigid floating rectangle. |
| Sword hilt bone | right hand | separate sword blade ~23px x 2px x 2px; guard 6x2x3 | steel-blue straight blade, gold-blue crossguard, unique hilt, held diagonal in idle. |
| Belt pendant | waist | 3x5x1 | brass sun/feather medallion; simple square details not round mesh. |

UV/texture MUST be pixel-authored rather than painting over generic Steve. Use one main **128x128 atlas** (256x256 permitted). Coordinates in texture pixels, `x0,y0,w,h`, reserving 2px gutters: head skin (0,0,36,36), hair (40,0,40,32), tunic/chainmail (0,40,48,42), cape (52,40,48,56), arms (0,84,48,32), legs (52,100,36,24), ornaments (92,0,34,32), sword separate 64x64 alpha atlas. Face uses distinct 2x2px each eye with 2-column separation in the *face painting*; this is texture-facing guidance, not a demand for giant glowing pupils. For each cube face compute proper box UV extents; avoid overlapping distinct colored faces. If rendering API rearranges UV atlas, keep regions equivalent rather than forcing invalid mapping.

### Exact indicative palette (sRGB HEX)
skin light `#E6BC96`, skin shadow `#B68464`, hair highlight `#D1AF73`, hair shadow `#8A6745`, iris blue `#357FC0`, white shirt `#E5E0D1`, embroidered burgundy `#8F3432`, chainmail light `#B6B8B4`, iron dark `#606772`, royal blue `#243F6A`, cape shadow `#182A49`, gold crest `#D0A64E`, dark leather `#65472F`, boots `#473727`, sword steel `#C4CED3`, sword dark `#5C768A`, magical gold FX `#EACB67`.

### Texture rules
Individual chainmail plates min 2x2 texels, offset 1px seams; sun/feather embroidery not jagged random paint. Cape back must read as simple gold heraldic falcon (one body/head, two outspread wings) at 8+px diagonal width. Arm armor asymmetric light, NEVER bulky football pads. Leather belts layered 1–2 model px physically. Lower tunic has vertical crimson geometric hem motif. No exposed modern buckles/zipper. Keep color palette within 12–16 dominant colors plus controlled shading. Test motion for cape/sword/left falcon arm clipping, idle rear cape readable, and leg penetration into floor.

### LOD
<20 blocks full subdivided cuboids, >20–40 lower feather strips at reduced animation complexity without changing silhouette, >40 vanilla cull if existing engine does so. No expensive mesh per frame. Mandatory separate small falcon model described in `03_Falcon/01_MODEL_AND_ANIMATION.md`.
