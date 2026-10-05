# 03 — KURGAN BLOCKS / MATERIALS / TEXTURES / CRAFTS — ULTRA DETAIL

Approved block reference:
`references/ref_01_blocks_and_decor.png`

The old recolor attempt is rejected.
These are not "change hue and save".
They need a newly authored Minecraft material identity.

---

# B01 — COLD BURIAL STONE / ХОЛОДНЫЙ ПОГРЕБАЛЬНЫЙ КАМЕНЬ

## Role
Core ancient stone material.

## Color target
Darker blue-gray than vanilla stone.

Suggested source palette:
- deepest joint/shadow: #202A31
- dark body: #2C3942
- middle body: #3B4A54
- light plane: #50616C
- restrained highlight: #687A84

Do not make it bright cyan.
Do not make it plain gray.

## Face design
Large irregular stone fields.
Aim for 5–8 readable masses across the face, not 100 tiny noise pixels.

Surface:
subtle layered mineral marks;
occasional short crack;
few lighter flecks.

Edges:
slightly darker perimeter clusters so adjacent blocks remain legible.

## Tiling
No single giant central stain repeated every block.
Create 2–4 random-compatible variants if current blockstate workflow permits.

## Minecraft feel
At a distance it should read as a dark stone block first.
Up close it shows cold mineral detail.

---

# B02 — BURIAL STONE BRICKS / ПОГРЕБАЛЬНЫЕ КАМЕННЫЕ КИРПИЧИ

## Role
Main built masonry.

## Palette
Derived from B01 but slightly more contrast:
joint #1E272D;
brick dark #303D46;
brick mid #42515B;
brick light #586975.

## Brick geometry
Use 2–3 horizontal courses.
Individual brick width varies.
Do not use perfectly uniform modern tiles.

Joint width at 256 source equivalent:
roughly 5–9 px, then preserve readable proportion.

Each brick:
top-left slight light;
bottom-right slight dark;
one or two surface chips.

## Variants
Full block.
Slab.
Stairs.
Wall.

Top/bottom textures:
compatible stone slab surface, not side brick face copied blindly.

---

# B03 — CRACKED BURIAL BRICKS / ПОТРЕСКАВШИЕСЯ ПОГРЕБАЛЬНЫЕ КИРПИЧИ

## Role
Age, traps, collapse hints.

## Base
B02.

## Cracks
1–3 main crack systems per face.
Cracks must:
- follow joints sometimes;
- cross one brick sometimes;
- have dark core and small chipped light edge.

Avoid spiderweb cracks over every pixel.

## Gameplay visual use
May hint:
- unstable area;
- secret wall;
- trap;
- collapsed section.

Do not make every cracked block secretly interactive.

---

# B04 — MOSSY / DAMP BURIAL BRICKS / ЗАМШЕЛЫЕ И СЫРЫЕ КИРПИЧИ

## Role
Flooded crypts, seepage, old lower levels.

## Base
B02/B03.

## Growth palette
Muted:
- deep moss #31432F
- moss mid #496044
- wet olive #5B6E4B

Coverage:
10–30% face.
Never 70% neon green.

Growth accumulates:
- joints;
- lower edge;
- crack pockets.

Optional wet darkening:
lower 20–35% slightly darker/bluer.

---

# B05 — CARVED BURIAL STONE / РЕЗНОЙ ПОГРЕБАЛЬНЫЙ КАМЕНЬ

## Role
Ceremonial accents, arches, tombs, ritual rooms.

## Base
Cleaner version of B01.

## Motif
Use one bold geometric/funerary motif per face:
- nested square spiral;
- interlocking angular knot;
- simple solar/meander-inspired geometry;
- border + central recessed square.

Do not use tiny illegible rune soup.
Do not copy a real-world religious symbol blindly.
Keep motif fictionalized/stylized within Slavic-inspired art direction.

## Relief
Recess:
dark line #202830.
Raised edge:
#596A74.
Main stone:
#3D4B55.

Create depth through 2–3 value bands, not gradient.

## Placement
Mostly important rooms/doorframes.
Do not wallpaper entire dungeon with carved block.

---

# B06 — BURIAL FLOOR TILE / ПОГРЕБАЛЬНАЯ ПЛИТКА

Only add/retain if useful and not redundant.

## Role
Floor borders, ritual geometry, burial dais.

## Design
Larger plate-like segments.
Subtle seam cross or rectangular slab division.
Less wall-like than B02.

Palette:
slightly smoother/lighter top surface.

Variants:
full block and slab recommended.
Stairs only if actually useful.

---

# SHAPE VARIANTS

For B01/B02/B03/B04 where appropriate:
- slab;
- stairs;
- wall.

Models must use correct textures per face.

Wall:
pillar should show coherent stone mass, not stretched nonsense.

Stairs:
top = top material;
riser = side/brick face;
underside = suitable bottom.

Slab:
top and side coherent.

---

# VANILLA MIX RATIOS

Typical corridor:
50–70% custom burial family,
30–50% vanilla stone families.

Ceremonial room:
65–85% custom,
15–35% vanilla.

Collapsed room:
35–60% custom,
40–65% vanilla rubble materials.

Flooded room:
40–65% custom,
35–60% vanilla stone/moss/water context.

These are artistic targets, not exact per-block quotas.

---

# RECIPES

## Slabs
Standard:
3 full blocks horizontal → 6 slabs.

## Stairs
Standard:
6 full blocks stair pattern → 4 stairs.
Also add stonecutter conversion.

## Walls
Standard:
6 full blocks in two horizontal rows → 6 walls.
Also stonecutter.

## Chiseled
Prefer:
2 slabs vertical → 1 carved/chiseled block
OR stonecutter from base burial stone.
Pick method matching current project recipe conventions.

## Cracked
If furnace aging is desired:
smelt Burial Bricks → Cracked Burial Bricks.
If this conflicts with logic, use stonecutter/crafting.
Document decision.

## Mossy
Do not make mossy decorative block absurdly expensive.
Possible recipe:
Burial Bricks + vine/moss block → Mossy Burial Bricks.
Use actual 1.21.1 materials available in project.

---

# MINING

Set tool requirements consistent with stone:
pickaxe.
Hardness/resistance near stone brick family unless current Kurgan balance needs slight increase.

Do not make decorative blocks obsidian-hard.

---

# CREATIVE TAB / JEI

All new construction variants:
- properly grouped;
- searchable;
- recipe visible in JEI.

No duplicate ghost variants.

---

# ASSET QA

For every block screenshot/manual review:
1. place 5×5 wall;
2. place 5×5 floor;
3. mix with vanilla stone brick;
4. inspect at 2, 8 and 20 blocks distance;
5. verify seams;
6. verify no ugly repeating central blob;
7. verify darker-blue stone is visibly different from vanilla stone.
