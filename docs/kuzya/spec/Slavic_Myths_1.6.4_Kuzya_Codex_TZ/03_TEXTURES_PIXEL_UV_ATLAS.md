# Pixel-level materials, UV map, and LOD rules

**Canonical texture atlas: 256×256 RGBA (or multiple 128×128 atlases if current renderer constraints require), NEVER 16×16.** Hard requirements: image sharp in NEAREST filtering, no baked photorealistic blur, no handwaving that pixel art can be generated 'later'. Work from the approved PNG; measure in-game screenshots against its key visible features.

## Deterministic UV allocation (atlas top-left origin, inclusive start / exclusive end)
| Pixel rectangle x0:y0–x1:y1 | Material / usage |
|---|---|
| 0,0–64,64 | FRONT / REAR head & cheek panels (can repack per face with clear mapper) |
| 64,0–128,64 | face sides/top, muzzle and jaw, brow, ears if any (tiny hidden, not canine ears) |
| 128,0–192,64 | hair cap crown and fringe, 12 unique tile variants |
| 192,0–256,64 | left/right/rear hair clusters, 12 variants |
| 0,64–96,160 | tunic front/back, 2 patched red-brown designs |
| 96,64–160,160 | sleeves, cuff seams, belt, hands |
| 160,64–224,160 | dark trousers, small legs, boots, foot soles |
| 224,64–256,160 | eyes/lids/mouth (additional overlay alternatives) |
| 0,160–96,224 | hair strand variants for far fringe/rear |
| 96,160–192,224 | food prop / bowl / loaf / crumb variants |
| 192,160–256,224 | small accessory and unique mark/patch decals |
| 0,224–256,256 | reserve, padding and seam duplication, NOT blank default texture |

Atlas mapping may be adapted to actual mesh UV, but no face should stretch a 4×4 patch across large complex hair surfaces. **Minimum 2 px padding** around all UV islands; keep anti-bleed duplicates for cloth seams. Save source `.png` plus metadata atlas map and image scaling policy.

## Exact face visual targets at 256-px texture scale
- Each eye's on-texture effective visible area target 9–12 px wide × 8–10 high depending UV density, with shadowed outline width 1–2 px; iris/pupil region 3–5 px across; 1 px light glint.
- Upper lids distinct from eyebrows; blink through lid bones, not white-eye disappearance.
- Nose 2–3 px wide by 2 px high in rendered face region; black-brown `#191818`; NOT bright pink button nose.
- Lips a 2–4 px long muted reddish line. Mouth-open version no wider than 5–6 px. Teeth optional and almost invisible.
- Face shadow around eye socket creates puppet-depth illusion, with shade steps not flat fill.

## Visible materials and weathering
- hair cluster base/dark/highlight in 3–5 stepped tones each (dark russet and brown ONLY);
- face base in 3–4 tones (dark chocolate warm, not black void); cheeks warm muted brown not blush pink;
- tunic base two values, faded embroidered cuffs, individual small coarse-stitch marks about 1–2 texels, 1 patched rough rectangle on torso and 1 on lower hem;
- legs, shoes muted brown/black; avoid polished shoes, glossy plastic, neon red.

## In-game presentation checks
At camera distances 2, 5, 10 and 16 b Kuzya should have legible dark head, white eyes, red tunic and foot silhouette. For far away use LOD simplification of strand count without repainting hair yellow. Screenshots taken day, warm indoor lamplight, night; verify proper gamma. Ensure texture path/model path are valid and `missing texture` never appears.
