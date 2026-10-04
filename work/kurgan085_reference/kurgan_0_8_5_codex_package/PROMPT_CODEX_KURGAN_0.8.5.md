# SLAVIC MYTHS — CODEX IMPLEMENTATION SPECIFICATION
## Version target: 0.8.5 — Kurgans / procedural burial labyrinths / Kurgan Curse
## Minecraft 1.16.5 / Forge 36.2.42 / Java 8 / mod id `slavicmyths`

---

# 0. THE MOST IMPORTANT RULE: THIS IS AN IMPLEMENTATION TASK, NOT A DESIGN TASK

You are Codex working inside the existing `slavic-myths` repository.

The game design, visual design and content scope are already fixed in this document and in the files from `references/`.

DO NOT redesign the feature.
DO NOT propose alternatives.
DO NOT invent additional systems.
DO NOT replace specified visual elements with "something similar".
DO NOT expand the scope because you think another mechanic would be interesting.
DO NOT create placeholder art and call the task complete.
DO NOT spend tokens describing possible designs when the design is already specified here.

Your responsibility is:
1. inspect only the parts of the repository necessary for this task;
2. identify the project's existing patterns;
3. implement exactly the specification below;
4. update README;
5. build the mod without launching Minecraft;
6. copy the resulting JAR into the PolyMC instance `mods` directory used by this project, if that path is already known from the repository/local setup;
7. return a short technical summary.

If there is a direct conflict between this specification and an existing project API/architecture, preserve the gameplay/design requirements and adapt only the technical implementation.

Only ask a question if implementation is genuinely impossible without a missing technical fact.
Do not ask design questions whose answer is already present in this document or in `references/`.

---

# 1. TOKEN / CONTEXT BUDGET RULES

The purpose of this specification is to prevent unnecessary exploration and token consumption.

## 1.1 Do not perform a broad repository audit
Inspect only files directly related to:
- Forge registration;
- blocks/items;
- block models and textures;
- structures / world generation;
- saved world data or the existing project equivalent;
- commands;
- effects/potions/status effects;
- loot tables;
- localization;
- advancements only if existing kurgan content already uses them;
- README;
- build output / PolyMC deployment.

Do not read unrelated mobs, food, classes, fishing, bandits or other systems unless a referenced class is required by the current kurgan implementation.

## 1.2 Reuse existing patterns
Before adding a new manager/base class/registry:
- search for an existing equivalent;
- extend the existing implementation where possible.

Do not create a second parallel registration or world-data framework.

## 1.3 Do not narrate internal reasoning
Work directly on files.
The final response should be concise and factual.

## 1.4 Do not run the Minecraft client
Forbidden:
- `runClient`;
- anything that opens a Minecraft window;
- GUI/manual automated testing.

Allowed:
- headless compilation;
- headless Gradle build;
- resource validation that does not launch the game.

Do not run expensive unrelated tests.

---

# 2. FIXED SCOPE OF VERSION 0.8.5

0.8.5 must finish the architectural and technical foundation of the kurgan dungeon system.

This version includes:
- three kurgan tiers;
- vanilla-block exterior mounds;
- procedural modular underground labyrinths;
- 1 / 2–3 / 4–5 floors depending on tier;
- two custom interior stone palettes;
- room/corridor modules;
- burial chambers;
- sarcophagus architecture;
- decorative kurgan blocks;
- a protected Great Kurgan final tomb;
- three ritual niches in the final tomb;
- per-instance persistent kurgan data;
- hidden Disturbance / `Нарушение покоя`;
- custom status effect `Kurgan Curse / Проклятие кургана`;
- locate/debug commands;
- loot tables;
- README documentation;
- manual-test deployment JAR.

This version DOES NOT include:
- the 0.8.6 kurgan enemies;
- kurgan mini-bosses;
- the 0.8.7 Great Kurgan boss;
- combat encounters designed around those enemies;
- traps;
- new player classes;
- unrelated weapons or armor;
- unrelated archaeology content already covered by previous patches;
- a new quest system;
- new rituals not specified here.

The architecture must be ready for 0.8.6 enemies and the 0.8.7 boss, but those entities are not implemented now.

---

# 3. REQUIRED VISUAL REFERENCES

The `references/` directory is part of this specification.

Do not treat the images as loose inspiration. They are design constraints.

## 3.1 Conflict priority
If implementation details appear ambiguous:
1. explicit numeric/text requirement in this specification;
2. the corresponding isolated reference image;
3. `REF_00_FULL_KURGAN_VISUAL_BOARD.png`;
4. existing project style.

Do not invent a fifth option.

## 3.2 Reference list

### Core stone blocks
- `REF_01_PALE_BLUE_KURGAN_STONE.png`
- `REF_02_PALE_BLUE_KURGAN_COBBLESTONE.png`
- `REF_03_CRACKED_PALE_BLUE_KURGAN_COBBLESTONE.png`
- `REF_04_MOSSY_PALE_BLUE_KURGAN_COBBLESTONE.png`
- `REF_05_BOG_GREEN_KURGAN_STONE.png`
- `REF_06_BOG_GREEN_KURGAN_COBBLESTONE.png`
- `REF_07_CRACKED_BOG_GREEN_KURGAN_COBBLESTONE.png`
- `REF_08_MOSSY_BOG_GREEN_KURGAN_COBBLESTONE.png`

### In-world palette usage
- `REF_09_PALE_BLUE_WALL_PALETTE.png`
- `REF_10_BOG_GREEN_WALL_PALETTE.png`
- `REF_33_MIXED_PALE_BLUE_WALL.png`
- `REF_34_MIXED_BOG_GREEN_WALL.png`

### Corridors
- `REF_11_STANDARD_CORRIDOR.png`
- `REF_12_WIDE_CORRIDOR.png`
- `REF_13_GRAND_CORRIDOR.png`
- `REF_14_CORRIDOR_SUPPORT_DETAIL.png`
- `REF_15_CORRIDOR_JUNCTION.png`
- `REF_16_FLOOR_STAIRS.png`

### Rooms
- `REF_17_SMALL_BURIAL_CHAMBER.png`
- `REF_18_LARGE_BURIAL_CHAMBER.png`

### Great Kurgan final room
- `REF_19_GREAT_CENTRAL_TOMB_HALL.png`
- `REF_20_GREAT_TOMB_TOP_VIEW.png`
- `REF_21_GREAT_TOMB_THREE_NICHES.png`

### Burials and decorations
- `REF_22_SIMPLE_SARCOPHAGUS.png`
- `REF_23_REINFORCED_SARCOPHAGUS.png`
- `REF_24_MONUMENTAL_SARCOPHAGUS.png`
- `REF_25_STONE_SLAB_DECOR.png`
- `REF_26_COLUMN_FRAGMENT.png`
- `REF_27_RITUAL_BOWL.png`
- `REF_28_KURGAN_POTTERY.png`
- `REF_29_STONE_ALTAR.png`
- `REF_30_WALL_TORCH_HOLDER.png`
- `REF_31_HANGING_BRAZIER.png`
- `REF_32_KURGAN_RUBBLE.png`

### Curse icon source motif
- `REF_35_KURGAN_CURSE_SEAL_MOTIF_SOURCE.png`

---

# 4. HISTORICAL / ART DIRECTION

The visual period is Early Rus / Ancient Rus, with an upper stylistic boundary around the 11th century.

This is a stylized Minecraft fantasy interpretation, not a museum reconstruction, but the following visual language is mandatory:

USE:
- earth burial mounds;
- rough early masonry;
- heavy stone;
- simple geometric carvings;
- timber supports where appropriate;
- burial niches;
- low, heavy architecture;
- simple ritual stone elements;
- pottery;
- age, moisture, moss and subsidence;
- restrained ornament.

DO NOT USE:
- Gothic arches;
- late medieval castle interiors;
- Renaissance details;
- Roman temple columns as the main language;
- Egyptian tomb motifs;
- polished fantasy palace architecture;
- bright magic crystals;
- neon runes;
- clean modern geometry;
- ornate baroque sarcophagi.

A kurgan must read as an ancient burial complex, not a Stronghold, Mineshaft, castle or generic RPG dungeon.

---

# 5. EXTERIOR KURGAN — VANILLA BLOCKS ONLY

The visible exterior mound uses vanilla Minecraft blocks. Do not introduce custom exterior terrain blocks.

## 5.1 Shape
The mound is an irregular earth hill with a buried/partly exposed entrance.

It must NOT be:
- a pyramid;
- a perfect dome;
- a symmetrical temple;
- a large stone building.

The silhouette should look terrain-like.

## 5.2 Material palette
Use a controlled mixture of:
- `grass_block`
- `dirt`
- `coarse_dirt`
- `stone`
- `cobblestone`
- `mossy_cobblestone`
- `stone_bricks` sparingly
- `cracked_stone_bricks` sparingly
- `gravel` in very small quantities
- `oak_log` / `stripped_oak_log` in rare support remains
- `spruce_log` / `stripped_spruce_log` in rare support remains

Do not create a colorful exterior palette.

## 5.3 Distribution guidance
Approximate visual proportions, not literal block-count constraints:
- 65–75% earth/grass shell;
- 8–15% coarse earth patches;
- 5–12% visible stone/cobblestone;
- 2–6% mossy stone;
- 1–4% gravel;
- timber remnants should be rare accents, generally below 3%.

## 5.4 Tier silhouette
### Small Kurgan
- approximate diameter: 13–19 blocks;
- approximate visible mound height: 4–7 blocks;
- modest entrance;
- little exposed masonry.

### Warrior/Clan Kurgan
- approximate diameter: 19–27 blocks;
- approximate visible mound height: 6–9 blocks;
- more visible stones around entrance;
- entrance can have 2–4 rough stone side supports.

### Great Kurgan
- approximate diameter: 27–39 blocks;
- approximate visible mound height: 8–12 blocks;
- more substantial entrance composition;
- still reads as a mound, not a building.

Variation is allowed inside these ranges.
Do not force exact circles.

---

# 6. INTERIOR CORE PALETTE — NEW BLOCKS

Both custom palettes are used throughout the labyrinth system.
Do NOT reserve one palette for one kurgan tier.
Do NOT randomly checkerboard them.

Use them as controlled architectural/material zones:
- pale-blue = colder, more sacred, better preserved, deeper/important masonry;
- bog-green = damp, old, ordinary, weathered/subsided masonry.

A room can contain both, but one must be visually dominant.

All final textures:
- minimum 32×32;
- preferred 64×64;
- no 16×16 final texture;
- no photo texture;
- Minecraft-readable pixel art;
- no placeholder texture.

---

# 7. BLOCK-BY-BLOCK VISUAL SPECIFICATION

The following registry IDs are fixed unless they already exist under semantically equivalent IDs. If an equivalent already exists, reuse it rather than duplicate it.

## 7.1 `pale_blue_kurgan_stone`
Reference: `REF_01_PALE_BLUE_KURGAN_STONE.png`

Purpose:
- relatively intact cold ancient stone;
- wall fields;
- floor bands;
- important rooms;
- lower/deeper areas.

Visual:
- large subdued stone faces;
- not brick-shaped;
- dusty cold blue-gray;
- slightly irregular value changes;
- surface must look worn, not polished;
- no glowing pixels.

Suggested palette target:
- base: approximately `#788997`
- mid-dark: `#667783`
- dark: `#4D5B66`
- highlight: `#9AAAB2`

The exact pixels may vary, but keep saturation low.

Do not recolor vanilla Stone with a flat hue overlay.
The texture needs its own stone-face breakup.

## 7.2 `pale_blue_kurgan_cobblestone`
Reference: `REF_02_PALE_BLUE_KURGAN_COBBLESTONE.png`

Purpose:
- rough wall construction;
- supports;
- less preserved sections.

Visual:
- visibly separate stones;
- irregular cobble silhouettes;
- clear mortar/deep gaps;
- cold blue-gray palette;
- rougher than `pale_blue_kurgan_stone`.

It must be recognizable as a different material at normal Minecraft play distance.

## 7.3 `cracked_pale_blue_kurgan_cobblestone`
Reference: `REF_03_CRACKED_PALE_BLUE_KURGAN_COBBLESTONE.png`

Purpose:
- damaged structural sections;
- visual indication of age/subsidence;
- room/corridor accent.

Visual:
- same family as pale-blue cobble;
- several clearly readable cracks crossing individual stones and joints;
- darker internal crack pixels;
- no exaggerated black cartoon lines;
- visibly more damaged than the normal version.

Do not make it only 2–3 changed pixels.

## 7.4 `mossy_pale_blue_kurgan_cobblestone`
Reference: `REF_04_MOSSY_PALE_BLUE_KURGAN_COBBLESTONE.png`

Purpose:
- damp walls;
- lower floors;
- moisture pockets;
- old corners.

Visual:
- pale-blue cobble foundation;
- muted gray-green/olive moss;
- moss appears in joints and lower faces;
- moss coverage must be visible but not turn the whole block bright green.

Moss tones should stay desaturated.

## 7.5 `bog_green_kurgan_stone`
Reference: `REF_05_BOG_GREEN_KURGAN_STONE.png`

Purpose:
- common damp ancient masonry;
- ordinary corridors;
- outer underground ring;
- transition rooms.

Visual:
- gray/olive/green stone;
- muddy, desaturated;
- not emerald;
- not slime;
- not grass-colored.

Suggested palette:
- base: approximately `#707866`
- mid-dark: `#5F6758`
- dark: `#454B3F`
- highlight: `#8A907B`

## 7.6 `bog_green_kurgan_cobblestone`
Reference: `REF_06_BOG_GREEN_KURGAN_COBBLESTONE.png`

Purpose:
- rough common wall;
- supports;
- old patched masonry.

Visual:
- clear individual cobbles;
- gray-green stones with darker joints;
- somewhat dirtier/heavier than the pale-blue set.

## 7.7 `cracked_bog_green_kurgan_cobblestone`
Reference: `REF_07_CRACKED_BOG_GREEN_KURGAN_COBBLESTONE.png`

Purpose:
- collapsed/subsided visual zones;
- old branch corridors.

Visual:
- same material family;
- visible cracks;
- dark brown-gray/olive crack interiors;
- not just vanilla cracked texture recolored 1:1.

## 7.8 `mossy_bog_green_kurgan_cobblestone`
Reference: `REF_08_MOSSY_BOG_GREEN_KURGAN_COBBLESTONE.png`

Purpose:
- strongest damp/old wall material;
- lower corners;
- near rubble and old chambers.

Visual:
- cobble remains readable;
- moss is darker olive;
- use clustered moss, not uniform green noise.

## 7.9 Mixture rules
References:
- `REF_09_PALE_BLUE_WALL_PALETTE.png`
- `REF_10_BOG_GREEN_WALL_PALETTE.png`
- `REF_33_MIXED_PALE_BLUE_WALL.png`
- `REF_34_MIXED_BOG_GREEN_WALL.png`

Do not construct walls from one repeated block only.

Typical wall field:
- dominant base family: 55–75%;
- secondary same-family variant: 15–30%;
- cracked/mossy accents: 5–20%;
- opposite color family accents: normally 0–15%, higher only in transition/sacred rooms.

Do not use randomized noise per every block.
Use clusters/patches so masonry looks intentionally built and then aged.

---

# 8. PROTECTED GREAT-TOMB BLOCK

Add a themed protected block:

`sealed_kurgan_masonry`

This block is required even though it is not a separate cube in the original board.

Visual source:
- use the masonry language from `REF_19_GREAT_CENTRAL_TOMB_HALL.png`,
- `REF_20_GREAT_TOMB_TOP_VIEW.png`,
- and the pale-blue/bog wall references.

## 8.1 Visual
- cold blue-gray base;
- darker stone joints;
- subtle desaturated olive age marks;
- heavier and more regular than ordinary cobble;
- occasional simple recessed geometric burial-seal shapes;
- no glowing rune;
- no bright magic color.

Suggested tones:
- base `#56646C`
- dark `#3E494F`
- highlight `#7B877D`
- seal recess `#45515D`

## 8.2 Gameplay
- survival players cannot break it with normal tools;
- explosion resistant;
- cannot be bypassed by mining an adjacent ordinary wall;
- Creative can remove it for development/testing;
- do not use vanilla Bedrock as the solution.

The entire protected final-tomb shell must be closed:
- walls;
- floor;
- ceiling;
- corners;
- entrance sealing section.

No one-block bypass gaps.

---

# 9. DECORATIVE KURGAN CONTENT

The following objects are part of the 0.8.5 visual foundation.
Keep their function simple.
Do not add unrelated gameplay systems.

## 9.1 Burial stone slab
Reference: `REF_25_STONE_SLAB_DECOR.png`

Registry target:
`burial_stone_slab`

This is a decorative low stone plate, NOT a normal building slab replacement.

Shape:
- low rectangular block;
- rough broken edges;
- 3–6 pixels/units visually lower than a full block in the model;
- simple weathered top.

Use:
- under sarcophagi;
- ritual floor accents;
- sealed niches.

No inventory animation.

## 9.2 Column fragment
Reference: `REF_26_COLUMN_FRAGMENT.png`

Registry target:
`kurgan_column_fragment`

Visual:
- squat broken stone support;
- square/rough circular impression;
- damaged top;
- early simple geometry, not Greco-Roman fluted marble.

Use:
- collapsed supports;
- room corners;
- large burial chamber accents.

## 9.3 Ritual bowl
Reference: `REF_27_RITUAL_BOWL.png`

Registry target:
`kurgan_ritual_bowl`

Visual:
- shallow stone bowl on a short base;
- thick heavy rim;
- gray-blue or gray-olive stone;
- no gold trim.

Function in 0.8.5:
- decorative;
- no invented ritual UI.

## 9.4 Pottery
Reference: `REF_28_KURGAN_POTTERY.png`

Registry target:
`kurgan_pottery`

Visual:
- early simple ceramic vessels;
- muted terracotta/brown/ochre;
- no glossy glaze;
- 2–3 shape/texture variants may be implemented using the simplest project-consistent method.

Function:
- decoration and optional loot-room prop;
- do not create a pottery minigame.

## 9.5 Stone altar
Reference: `REF_29_STONE_ALTAR.png`

Registry target:
`kurgan_stone_altar`

Visual:
- short heavy pedestal;
- simple blocky cap;
- no church-like late Christian decoration;
- no elaborate fantasy runes.

Function:
- architectural prop only in 0.8.5.

## 9.6 Wall torch holder
Reference: `REF_30_WALL_TORCH_HOLDER.png`

Registry target:
`kurgan_wall_torch_holder`

Visual:
- dark iron/blackened metal bracket;
- small flame;
- simple early-metalwork shape.

Function:
- provides light;
- static;
- do not create fuel mechanics.

## 9.7 Hanging brazier
Reference: `REF_31_HANGING_BRAZIER.png`

Registry target:
`kurgan_hanging_brazier`

Visual:
- dark metal bowl;
- 3 support chains;
- small contained flame;
- visibly larger than wall torch.

Function:
- static light/decor;
- no fuel inventory;
- no cooking.

Use only in large/significant rooms.
Do not fill ordinary corridors with them.

## 9.8 Rubble
Reference: `REF_32_KURGAN_RUBBLE.png`

Registry target:
`kurgan_rubble`

Visual:
- pile of broken stone pieces;
- low collision where practical;
- palette follows current room stone family.

Function:
- decorative age/collapse element;
- must not randomly block the guaranteed critical path.

---

# 10. SARCOPHAGUS / BURIAL ARCHITECTURE

The references depict stylized stone burial containers. Keep them as part of the mod's stylized Early-Rus burial language.

DO NOT create Egyptian/Roman carved coffins.

## 10.1 Simple sarcophagus
Reference: `REF_22_SIMPLE_SARCOPHAGUS.png`

Used in:
- Small Kurgan;
- minor chambers in Tier II.

Target visual footprint:
- approximately 1×2 blocks;
- around 1 block tall;
- rectangular heavy stone burial chest;
- thick lid;
- one small recessed geometric symbol at most.

Use the simplest technical implementation compatible with the project:
- a dedicated sarcophagus block/multi-part block if the project already has such patterns;
- otherwise construct it as a deterministic small room feature from kurgan stone + `burial_stone_slab`/lid-like geometry.

Do NOT create six separate complex registries merely to imitate a screenshot.

The visible result matters more than making the sarcophagus a player-placeable furniture system.

## 10.2 Reinforced sarcophagus
Reference: `REF_23_REINFORCED_SARCOPHAGUS.png`

Used in:
- Warrior/Clan Kurgan;
- high-value side chamber.

Target:
- around 1×2 or 1×3 footprint;
- thicker stone frame;
- more pronounced lid edge;
- a small restrained geometric carving;
- better preserved than the simple version.

## 10.3 Monumental sarcophagus
Reference: `REF_24_MONUMENTAL_SARCOPHAGUS.png`

Used only in:
- Great Kurgan final tomb / most important burial architecture.

Target:
- visibly much larger than ordinary sarcophagi;
- approximately 3×5 architectural footprint including its raised base;
- heavy stepped base;
- massive central stone chest;
- restrained circular/geometric burial seals;
- no ornate humanoid statue on top.

It should read as a room centerpiece, not as an item pickup.

---

# 11. CORRIDOR DIMENSIONS — FIXED

General playable corridor clear height:
- 4–5 blocks.

Do not reduce the main corridors to 2 blocks high.

This is required so future 0.8.6 enemies and mini-boss-sized entities can navigate the dungeon.

## 11.1 Standard corridor
Reference: `REF_11_STANDARD_CORRIDOR.png`

Clear width:
- 3 blocks.

Clear height:
- 4 blocks normally;
- occasional 5-block support bay.

Visual:
- heavy walls;
- floor slightly varied;
- support rhythm every several blocks;
- no perfectly flat endless tube.

## 11.2 Wide corridor
Reference: `REF_12_WIDE_CORRIDOR.png`

Clear width:
- 4 blocks.

Clear height:
- 4–5 blocks.

Use:
- approach to important rooms;
- loop connector;
- main route on Tier II/III.

## 11.3 Grand corridor
Reference: `REF_13_GRAND_CORRIDOR.png`

Clear width:
- 5 blocks.

Clear height:
- 5 blocks.

Use sparingly:
- Great Kurgan;
- approach to the protected lower tomb;
- large-room transition.

Do not use this width for every corridor.

## 11.4 Support detail
Reference: `REF_14_CORRIDOR_SUPPORT_DETAIL.png`

Support bays:
- shallow stone protrusions/pillars;
- they must not reduce the guaranteed clear route below 3 blocks where future large enemy navigation is expected.

Use broken supports only as side decoration.

---

# 12. CORRIDOR MODULE TYPES

The generator must have semantic module categories, even if the implementation uses templates/pieces.

Required:
1. short straight;
2. normal straight;
3. long straight;
4. 90-degree corner;
5. T-junction;
6. 4-way junction;
7. dead end;
8. room connector;
9. stair connector;
10. loop/shortcut connector.

Reference for junction:
`REF_15_CORRIDOR_JUNCTION.png`

Do not create one visual template copied for all ten categories.

Each category should have at least enough controlled variation that two labyrinths do not look identical:
- palette dominant family;
- support positions;
- cracked/mossy patches;
- small rubble accents;
- minor length differences where valid.

Never allow cosmetic variation to invalidate connectors.

---

# 13. STAIRS BETWEEN FLOORS

Reference:
`REF_16_FLOOR_STAIRS.png`

Required:
- minimum clear width: 3 blocks;
- floor-to-floor transition must account for 4–5 block room height plus floor/ceiling thickness;
- stairs must physically align between generated floors;
- no stair ending inside solid wall;
- no one-block head collision on the guaranteed route.

Visual:
- broad stone stepped passage;
- support side walls;
- darker/lower light than ordinary room;
- can mix blue/green stone families.

Do not implement ladders as the primary floor transition.

---

# 14. ROOM ARCHITECTURE

Rooms must not all be identical square boxes.

Every room piece has:
- bounding box;
- connector positions;
- floor index;
- semantic type;
- dominant palette;
- optional secondary palette;
- disturbance contribution metadata;
- loot profile;
- `isCriticalPath` or equivalent;
- optional special role.

Required room categories:
1. small burial chamber;
2. large burial chamber;
3. offering chamber;
4. empty/atmospheric chamber;
5. ruined chamber;
6. transition chamber;
7. optional blocked side chamber;
8. final chamber where applicable.

Secret rooms are NOT mandatory for 0.8.5.
Do not spend a large amount of implementation time on them.

---

# 15. SMALL BURIAL CHAMBER

Reference:
`REF_17_SMALL_BURIAL_CHAMBER.png`

Target approximate interior:
- 5×5 to 7×7 playable floor;
- 4–5 blocks high.

Contains:
- one simple sarcophagus/burial structure;
- 0–2 pottery props;
- 0–1 ritual bowl;
- restrained light;
- wall age variation.

Do not guarantee a loot chest in every room.

This room should feel compact and burial-focused.

---

# 16. LARGE BURIAL CHAMBER

Reference:
`REF_18_LARGE_BURIAL_CHAMBER.png`

Target approximate interior:
- 8×8 to 12×12 playable floor;
- 5 blocks high.

Contains some combination of:
- reinforced sarcophagus;
- stone supports;
- burial slabs;
- pottery;
- ritual bowl;
- rubble;
- 1–2 wall lights;
- rare hanging brazier.

This room must clearly feel more important than the small chamber.

Do not turn it into a boss arena.

---

# 17. OFFERING CHAMBER

Approximate interior:
- 5×6 to 8×8;
- 4–5 blocks high.

Visual focus:
- stone altar;
- pottery;
- ritual bowl;
- burial slabs.

Loot:
- generated through loot table;
- not necessarily a normal chest if the existing project has a better compatible container;
- no hardcoded item list in Java.

No new ritual mechanics in 0.8.5.

---

# 18. EMPTY / ATMOSPHERIC CHAMBER

Purpose:
- pacing;
- ambiguity;
- architectural realism.

Must still contain visual storytelling:
- cracked wall patch;
- support;
- rubble;
- a slab;
- pottery fragment/prop.

Do not leave a literal empty stone cube.

These rooms must not dominate the dungeon count.

Recommended maximum:
- around 15–20% of content rooms.

---

# 19. RUINED CHAMBER

Contains:
- local wall/floor damage;
- rubble;
- cracked/mossy block clustering;
- partial support damage.

Critical rule:
the guaranteed route through a ruined chamber remains traversable.

Do not use falling blocks/traps.
Do not make random obstruction that can seal the main path.

---

# 20. GREAT KURGAN CENTRAL TOMB — FIXED DESIGN

References:
- `REF_19_GREAT_CENTRAL_TOMB_HALL.png`
- `REF_20_GREAT_TOMB_TOP_VIEW.png`
- `REF_21_GREAT_TOMB_THREE_NICHES.png`

This is the most important room in 0.8.5.

It is NOT a normal room selected randomly.

It is a dedicated final piece.

## 20.1 Placement
- always on the deepest generated floor of a Great Kurgan;
- floor 4 if the kurgan generated 4 floors;
- floor 5 if the kurgan generated 5 floors;
- never close to the entrance;
- cannot become a branch-side optional room.

## 20.2 Shape
Preferred shape:
- circular / near-circular / octagonal.

Do not use a plain rectangular box.

Target usable inner diameter:
- approximately 21–29 blocks.

Target height:
- approximately 10–14 blocks clear at the high central area.

This is intentionally much larger than normal rooms.

## 20.3 Layout
The room contains:
- a central raised burial platform;
- monumental sarcophagus composition;
- circular or near-circular walking area;
- perimeter supports;
- three distinct ritual/burial niches;
- controlled larger lighting elements;
- protected outer shell.

## 20.4 Three niches
Three niches are mandatory.

Their role in 0.8.5:
- architectural preparation for later content only.

Each niche:
- visibly recessed;
- large enough to be distinct from decorative alcoves;
- can be sealed/blocked by heavy stone;
- must be stored/identifiable in the kurgan instance data if practical.

Do NOT invent:
- three keys;
- three bosses;
- three crystals;
- three rituals;
- activation mechanics.

Those are future design decisions.

## 20.5 Final room access
The entrance to the protected central room is architecturally present but sealed in 0.8.5.

The sealed entrance must be represented by a known bounding box/anchor in the per-kurgan data so a later patch can open it.

Do not create the final opening puzzle now.

---

# 21. PROCEDURAL GENERATION MODEL

A kurgan is not one giant fixed NBT layout.

The dungeon is assembled from modules/pieces under constraints.

Two kurgans of the same tier must be able to generate different maps.

Randomness must never override required connectivity.

## 21.1 High-level generation order
Use this logical order:

1. choose tier;
2. choose floor count inside tier constraints;
3. derive deterministic per-kurgan random seed from world seed + placement position/structure seed;
4. create an abstract dungeon graph/layout in memory;
5. create guaranteed critical path from entrance to deepest/final area;
6. reserve floor-transition nodes;
7. reserve mandatory special rooms;
8. add side branches;
9. add dead ends;
10. add optional loops/shortcuts;
11. assign room/corridor piece variants;
12. assign palette zones;
13. validate bounding boxes/connectors;
14. validate graph connectivity;
15. validate final room constraints;
16. if invalid, retry layout up to a fixed maximum attempt count;
17. only after a valid plan exists, place blocks/pieces into the world.

Do not place half a dungeon and then discover the layout failed.

## 21.2 Retry limit
Use a bounded retry count.

Recommended:
- 8–16 whole-layout attempts depending on implementation.

After failure:
- fail cleanly / skip structure placement;
- do not generate a broken partial dungeon.

No infinite random placement loops.

---

# 22. TIER I — SMALL KURGAN

Fixed requirements:
- 1 underground floor;
- 3–6 content rooms;
- short total route;
- several corridor modules;
- 0–2 dead-end branches;
- one primary burial room;
- no mandatory loop;
- no Great-tomb protected room;
- no persistent curse.

The layout must still be procedural.

Typical graph:
Entrance -> transition/corridor -> 1–3 rooms -> primary burial chamber,
with up to two side branches.

Do not create a 20-room small kurgan.

---

# 23. TIER II — WARRIOR / CLAN KURGAN

Use one consistent English ID/name in code. Preferred registry naming:
`warrior_kurgan`

Russian display/documentation:
`Родовой / Воинский курган`

Fixed requirements:
- 2–3 floors;
- 10–18 content rooms total;
- multiple branches;
- dead ends;
- additional burial rooms;
- one dedicated final burial chamber on the lowest floor;
- at least one optional loop/shortcut where generation permits;
- temporary Kurgan Curse only;
- no boss.

Recommended distribution:
- floor 1: 30–40% rooms;
- middle floor if present: 25–35%;
- final floor: remaining rooms including final chamber.

Do not put the final chamber on floor 1.

---

# 24. TIER III — GREAT KURGAN

Registry/display target:
`great_kurgan` / `Великий курган`

Fixed requirements:
- 4–5 floors;
- 25–40 content rooms total;
- multiple branches;
- dead ends;
- several burial zones;
- several loops/shortcuts where valid;
- grand corridors only in selected areas;
- final protected central tomb on deepest floor;
- persistent Great-Kurgan curse state;
- no boss in 0.8.5.

Recommended room distribution:
- upper floors: more bog-green/common masonry;
- deeper floors: increased pale-blue/sacred masonry;
- final floor: strongest pale-blue + sealed masonry identity.

This is a visual tendency, not a separate palette lock.

The Great Kurgan should be large enough that a player may explore it over several trips.

Do not inflate the count using dozens of meaningless empty nodes.

The 25–40 range refers to meaningful room/section content, not individual 3-block corridor segments.

---

# 25. LOOPS / SHORTCUTS

Tier II and Tier III should not be pure tree graphs.

After the primary graph is valid, attempt to add connectors between non-adjacent compatible nodes.

Example topology:
A -> B -> C -> D -> E
and optional connector:
E -> B

Rules:
- loops are supplementary;
- they must not bypass sealed Great-Tomb protection;
- they must not connect directly from an upper floor to the boss chamber;
- they must respect bounding boxes;
- avoid excessive spaghetti connections.

Tier II:
- target approximately 1–2 loop opportunities.

Tier III:
- target approximately 2–5 loop opportunities depending on floor count/layout.

Failure to create every target loop is acceptable if the geometry prevents it.
Do not invalidate the dungeon to force a loop.

---

# 26. MODULE COLLISION / BOUNDS

Every planned piece must have a known world-space bounding box before placement.

Reject:
- room overlap;
- corridor cutting through unrelated room;
- stairs intersecting solid unrelated modules;
- protected tomb shell intersection;
- surface breach from deep room unless explicitly intended.

Maintain reasonable separation between different floors.

Do not rely only on block-level "place if air" logic.

---

# 27. CRITICAL PATH VALIDATION

Before placement validate:

- entrance exists;
- entrance node belongs to graph;
- deepest floor exists;
- all required floor connectors exist;
- at least one traversable graph path exists from entrance to tier final room;
- final room is on deepest floor;
- Tier II/III stair chain is complete;
- no mandatory connector points into a wall;
- no mandatory route depends on breaking protected blocks;
- Great-Tomb shell is closed;
- Great-Tomb entrance is sealed by intended protected material, not by accidental terrain.

If validation fails, discard layout and retry.

---

# 28. PALETTE ZONING

Do not choose each wall block independently with raw randomness.

Assign a dominant palette to chunks of architecture:
- room;
- corridor section;
- support bay;
- floor zone.

Then introduce controlled secondary patches.

Suggested tendencies:
- upper ordinary corridor: bog green dominant;
- burial room: either palette;
- important preserved chamber: pale blue dominant;
- water/damp-looking corner: mossy bog;
- lower Great Kurgan: increased pale blue;
- final central tomb: pale blue + sealed masonry with limited bog aging.

Do not create a perfect gradient.
It should feel naturally varied.

---

# 29. LIGHTING RULES

Do not over-light the dungeon.

Allowed light sources:
- wall torch holder;
- hanging brazier in significant spaces;
- existing appropriate project light if already used and visually compatible.

Standard corridors:
- sparse wall lights;
- darker gaps allowed, while still playable.

Small chamber:
- 0–2 visible lights.

Large chamber:
- 1–3 visible lights.

Great final hall:
- several deliberate lights / hanging braziers, positioned symmetrically or semi-symmetrically enough to frame the room.

No modern lantern spam.
No bright magical light blocks.

---

# 30. LOOT

Use loot tables.
Do not hardcode room loot arrays in generation Java.

Loot profile depends on:
- kurgan tier;
- room semantic type;
- burial importance.

Not every room has loot.

Do not make every burial chamber contain a vanilla chest.

If the project has a prior burial-container/archaeology mechanism, reuse it.
If not, use the simplest compatible container strategy and keep it visually integrated.

Do not add unrelated new artifact items in this task unless they already belong to 0.8.4 and need to appear in kurgan loot.

---

# 31. KURGAN INSTANCE IDENTITY / SAVED DATA

Every generated kurgan must be addressable as an individual instance.

Store enough data to associate:
- unique kurgan ID;
- tier;
- world/dimension identifier as appropriate;
- origin/anchor position;
- overall bounds;
- floor count;
- generated layout seed;
- final-room bounds;
- sealed entrance bounds/anchor;
- three Great-Tomb niche positions/bounds when applicable;
- Disturbance value/state;
- curse progression state;
- whether special disturbance thresholds have already fired.

Do not use one global Disturbance value for all kurgans.

Use the existing project saved-data pattern if one exists.

Data must survive:
- world save/reload;
- server restart.

---

# 32. DISTURBANCE / `НАРУШЕНИЕ ПОКОЯ`

Disturbance is a hidden per-kurgan state.

Player must NOT see a HUD meter such as:
`Нарушение покоя: 72/100`.

Internally use a clear bounded scale such as 0–100.

## 32.1 What increases Disturbance
Keep triggers deterministic and easy to maintain.

Recommended event values:
- first looting/opening of a minor burial loot source: +8;
- first looting/opening of an important burial source: +15;
- first disturbance of a reinforced/important sarcophagus zone: +15;
- disturbance of a Great-Kurgan major burial zone: +20;
- protected/final-zone violation event in future versions can add a major amount, but do not invent an opening mechanic in 0.8.5.

Important:
- count a given source/event once, not every time the player reopens it;
- store fired source/event IDs or equivalent;
- do not allow repeatedly opening one container to farm Disturbance.

## 32.2 Environmental feedback
No numeric UI.

At threshold bands, provide restrained feedback using existing low-cost mechanisms.

Suggested bands:
- 0–24: normal;
- 25–49: occasional ambient cue / subtle sound;
- 50–74: stronger burial-area feedback;
- 75–100: curse threshold territory.

Do not add expensive particle storms.
Do not create a new large ambient system.

Use only subtle effects consistent with the project:
- one low sound cue;
- brief particles if appropriate;
- room light does NOT need to dynamically change unless an existing system makes this trivial.

---

# 33. CUSTOM STATUS EFFECT — `KURGAN_CURSE`

Registry target:
`kurgan_curse`

Russian name:
`Проклятие кургана`

English:
`Kurgan Curse`

This is a negative effect.

It has two gameplay modes based on the source kurgan tier:
- temporary Tier-II curse;
- persistent Great-Kurgan curse.

Do not implement two unrelated status-effect registries unless the existing effect architecture requires it.
Prefer one custom effect plus source/persistence metadata.

---

# 34. CURSE ICON / VISUAL MODEL

Reference motif:
`REF_35_KURGAN_CURSE_SEAL_MOTIF_SOURCE.png`

This crop is a source motif from the monumental burial decoration.
Do NOT literally paste this crop into the game icon.

Create a dedicated status-effect icon consistent with Minecraft UI.

Source artwork:
- minimum working source 32×32;
- preferred 64×64, downsample/pixel-adjust as needed for the actual version's effect icon system.

## 34.1 Icon composition
Mandatory shape:
- dark circular/oval burial seal;
- center contains a simplified cracked stone-ring motif;
- 3 short outward notches/marks arranged approximately triangularly;
- one visible diagonal crack through the central seal;
- outer edge slightly incomplete/worn.

Do not use:
- skull;
- pentagram;
- Christian cross;
- bright rune;
- neon aura;
- demon face.

## 34.2 Icon palette
Use:
- dark slate: approximately `#3E474B`
- cold blue-gray: `#657780`
- muted bog/olive: `#5D674F`
- pale highlight: `#8B9997`
- crack shadow: `#22292C`

The icon must remain readable at small UI size.
Avoid tiny micro-detail that disappears.

## 34.3 Effect presentation
The status icon should be the primary visual indicator in the HUD/inventory effect list.

Do not create a 3D player model attachment for the curse.
No floating skull.
No large screen overlay.

---

# 35. TIER-II TEMPORARY CURSE

Applies only to Warrior/Clan Kurgan disturbance.

Trigger:
- when that kurgan reaches the high disturbance threshold, preferred threshold `>= 75`.

Duration:
- fixed target: 30 minutes real gameplay time (20 minutes = 24000 ticks; therefore 30 minutes = 36000 ticks).
Use the project/version-correct tick duration.

If the effect is refreshed by a later qualifying event in the same kurgan:
- refresh to the full 30-minute duration;
- do not stack unlimited amplifiers.

Mechanical penalties:
- movement speed approximately -10%;
- attack damage approximately -10%.

Keep the effect meaningful but not crippling.

Do not add:
- blindness;
- nausea;
- constant damage;
- random teleport;
- hunger drain;
unless already required elsewhere by the existing project design. They are NOT required here.

Milk/normal effect clearing:
- Tier-II temporary curse MAY be removable by normal expiration;
- if standard milk removes the temporary variant, that is acceptable unless implementing source metadata makes preventing/removing it simpler.
The strict "cannot be removed by milk/death" rule is for the Great Kurgan variant.

On death:
- temporary Tier-II curse does not need special persistence.

---

# 36. GREAT-KURGAN PERSISTENT CURSE

This is the serious version discussed for the Great Kurgan.

Trigger:
- Great Kurgan Disturbance reaches `>= 75`.

Do NOT apply it merely for walking into the mound.
The player has to significantly disturb/loot the burial complex.

Persistent behavior:
- survives player death;
- survives logout/login;
- survives world save/reload;
- cannot be removed by milk;
- cannot be removed by ordinary generic effect-clearing gameplay;
- tied to the player and source Great-Kurgan identity in saved data where practical.

Mechanical penalties:
- movement speed approximately -15%;
- attack damage approximately -15%.

Do not make it deal unavoidable periodic damage.

## 36.1 Removal
Final intended removal:
- defeat of the Great Kurgan boss in version 0.8.7.

The boss does NOT exist in 0.8.5.

Therefore:
- implement a clean future hook/API/state method that can clear the persistent curse for a player when the 0.8.7 boss is defeated;
- do not invent a normal survival cure in 0.8.5.

Development/debug removal is mandatory:
`/slavicmyths dev clear_kurgan_curse [player]`

This command is for testing/admin only and must require appropriate permission.

README must warn that the Great-Kurgan curse has no normal survival cure until the later boss content is implemented.

---

# 37. CURSE PERSISTENCE IMPLEMENTATION REQUIREMENT

A normal vanilla potion instance alone is not sufficient if milk/death can trivially erase the persistent Great-Kurgan curse.

Implement persistence using the project's simplest reliable server-side mechanism.

Required behavior:
- if a player is marked persistently cursed, the effect is restored after death/login if it disappears;
- milk/generic removal cannot permanently clear the state;
- debug clear removes both the visible effect and persistent curse marker;
- future boss-clear hook removes both.

Avoid per-tick heavy scans of all world entities.

Prefer event-driven reapplication:
- player login;
- clone/respawn;
- effect removed / periodic low-frequency safety check only if needed.

---

# 38. COMMANDS — REQUIRED

All commands must:
- be server-safe;
- have permission checks where destructive/debug;
- give concise feedback;
- be documented in README.

Use existing command root if the project already has one.
Preferred root:
`/slavicmyths`

## 38.1 Locate commands
Provide a reliable way to find each tier.

Preferred custom commands:
- `/slavicmyths kurgan locate small`
- `/slavicmyths kurgan locate warrior`
- `/slavicmyths kurgan locate great`

Behavior:
- searches nearest generated/locatable matching kurgan using the implementation's supported structure lookup;
- returns coordinates and distance;
- does not teleport automatically.

If the structures are registered in a way compatible with vanilla `/locate`, README must ALSO list the exact working commands, e.g.:
- `/locate slavicmyths:small_kurgan`
- `/locate slavicmyths:warrior_kurgan`
- `/locate slavicmyths:great_kurgan`

Do not document commands that do not actually work in Minecraft 1.16.5.

## 38.2 Generate/debug commands
Required for manual testing:
- `/slavicmyths kurgan generate small`
- `/slavicmyths kurgan generate warrior`
- `/slavicmyths kurgan generate great`

The structure should generate at a safe nearby test anchor according to existing project debug conventions.

Optional deterministic variant:
- `/slavicmyths kurgan generate <tier> <seed>`

If technically straightforward, implement it because reproducible layouts are useful.

## 38.3 Info command
Required:
- `/slavicmyths kurgan info`

When executed while inside/near a known kurgan, print:
- instance ID;
- tier;
- origin;
- floor count;
- layout seed;
- Disturbance value ONLY because this is a debug/admin info command.

Normal players must not get a Disturbance HUD.

## 38.4 Curse debug clear
Required:
- `/slavicmyths dev clear_kurgan_curse [player]`

## 38.5 Optional curse debug apply
If easy and consistent:
- `/slavicmyths dev apply_kurgan_curse <temporary|persistent> [player]`

Do not spend significant time on optional commands if core work remains.

---

# 39. VANILLA `/LOCATE` SUPPORT

Where Forge 1.16.5 structure registration permits it without architectural hacks, each kurgan tier should be individually locatable.

Preferred registered structure IDs:
- `slavicmyths:small_kurgan`
- `slavicmyths:warrior_kurgan`
- `slavicmyths:great_kurgan`

If the existing project uses one parent structure with variants and vanilla locate cannot distinguish them cleanly:
- keep existing architecture;
- ensure the custom `/slavicmyths kurgan locate <tier>` commands work;
- document the actual situation in README.

Do not restructure the whole mod only to satisfy vanilla `/locate` if a safe custom command solves it.

---

# 40. WORLDGEN FREQUENCY

Do not make kurgans extremely common.

If existing 0.8.x values exist, preserve them unless broken.

If no values exist, use restrained relative rarity:
- Small Kurgan = most common;
- Warrior/Clan = clearly rarer;
- Great Kurgan = rare.

Do not invent biome restrictions unrelated to project lore unless existing roadmap/code already has them.

Generation must avoid obvious invalid placement:
- deep ocean;
- floating in air;
- severe terrain breakage.

Prefer terrestrial overworld placement consistent with existing project worldgen.

---

# 41. FUTURE 0.8.6 ENEMY NAVIGATION PREPARATION

Do NOT add enemies now.

But architecture must not block them.

Therefore:
- main corridors are 3–5 blocks wide;
- main clear height is 4–5 blocks;
- critical stairs width 3;
- decorative rubble cannot permanently choke all paths;
- door/connectors on critical route must not be 1-block-wide squeezes;
- final Great-Tomb protected room is a special excluded zone for future mini-boss path rules.

Store enough room/floor information that future code can determine:
- kurgan bounds;
- floor/room zones;
- final protected room.

Do not implement mob AI now.

---

# 42. FUTURE 0.8.7 BOSS PREPARATION

Do NOT add the boss.

Required future extension points:
- identify final central tomb bounds;
- identify sealed entrance;
- identify the 3 niches;
- provide method/state capable of opening the seal later;
- provide method capable of clearing Great-Kurgan persistent curse after boss defeat.

Do not write placeholder boss entity classes.

---

# 43. TEXTURE / MODEL QUALITY RULES

Every new visible final asset:
- >= 32×32 source;
- preferably 64×64;
- unique readable silhouette/material;
- no 16×16 placeholder;
- no flat recolor presented as a finished unique asset when structure/detail should differ.

Core color-variant stone families can share structural principles, but:
- pale-blue and bog-green need deliberate palette work;
- cracked variants need real crack design;
- mossy variants need real moss clusters.

JSON/block models:
- validate paths;
- validate texture references;
- no missing-texture purple/black outcome.

---

# 44. RESOURCE / LOCALIZATION

Add Russian and English localization for:
- all new blocks;
- `Kurgan Curse`;
- relevant command feedback if project localizes commands/messages;
- structure display/debug labels where applicable.

Russian names should be natural:
- `Бледно-синий камень кургана`
- `Бледно-синий булыжник кургана`
- `Потрескавшийся бледно-синий булыжник кургана`
- `Замшелый бледно-синий булыжник кургана`
- `Болотный камень кургана`
- `Болотный булыжник кургана`
- `Потрескавшийся болотный булыжник кургана`
- `Замшелый болотный булыжник кургана`
- `Запечатанная кладка кургана`
- `Погребальная каменная плита`
- `Обломок колонны кургана`
- `Ритуальная каменная чаша`
- `Погребальная керамика`
- `Каменный алтарь кургана`
- `Настенный держатель факела`
- `Подвесная курильница`
- `Обломки кладки кургана`
- `Проклятие кургана`

Do not introduce awkward machine-translated Russian if the existing localization style uses another grammatical convention; keep the meaning.

---

# 45. README UPDATE — MANDATORY

After implementation update the repository README.

Add a dedicated 0.8.5 Kurgan section containing:

## 45.1 Version summary
Explain:
- three kurgan tiers;
- procedural labyrinths;
- new stone palettes;
- Disturbance;
- Kurgan Curse;
- final Great-Tomb groundwork.

## 45.2 Tier table
Document:
- Small: 1 floor, 3–6 rooms;
- Warrior/Clan: 2–3 floors, 10–18 rooms;
- Great: 4–5 floors, 25–40 rooms.

## 45.3 Commands
List EVERY implemented kurgan/debug command with:
- exact syntax;
- permission expectation;
- short example.

Especially include:
- all locate commands;
- vanilla `/locate` forms if actually supported;
- generate commands;
- info command;
- curse clear command.

## 45.4 Manual test instructions
Write concise steps:
1. create/load a test world;
2. use locate or generate command;
3. inspect each tier;
4. verify floor count;
5. verify Great-Tomb seal;
6. raise Disturbance through burial interaction;
7. verify temporary/persistent curse behavior;
8. use debug clear after Great-Kurgan curse testing.

## 45.5 Known 0.8.5 limitation
Explicitly state:
- Great-Kurgan boss is planned for 0.8.7;
- persistent Great-Kurgan curse currently has no normal survival removal because its intended removal is boss defeat;
- admin/debug command exists for testing.

Do not leave README commands outdated.

---

# 46. BUILD / POLYMC DELIVERY

After code/resources are complete:

1. do not launch Minecraft;
2. run only a headless build/compile necessary to produce the mod JAR;
3. fix compilation/resource errors introduced by this task;
4. produce the current JAR;
5. copy/update the JAR in the PolyMC instance `mods` folder used for manual project testing, if the local path is known;
6. avoid leaving multiple conflicting old/new Slavic Myths JARs enabled in that same test `mods` folder.

If the PolyMC path is not discoverable from the existing project/local environment:
- do not guess an arbitrary user path;
- state the built JAR path in the final summary.

---

# 47. IMPLEMENTATION ORDER — FOLLOW THIS TO REDUCE TOKENS

Use this order unless existing code requires a minor adjustment:

1. inspect current kurgan/worldgen registration;
2. inspect existing block/effect/command registry patterns;
3. define/reuse kurgan instance data model;
4. register the 8 core stone blocks;
5. add `sealed_kurgan_masonry`;
6. add required decorative blocks with minimal behavior;
7. add textures/models/localization;
8. implement abstract dungeon graph/layout model;
9. implement module bounding/connectors;
10. implement Tier I plan;
11. implement Tier II floors/loops/final chamber;
12. implement Tier III floors/loops/Great final tomb;
13. implement layout validation;
14. implement placement;
15. implement kurgan saved-instance data;
16. implement Disturbance hooks;
17. implement Kurgan Curse temporary/persistent modes;
18. implement commands;
19. implement loot-table integration;
20. update README;
21. headless build;
22. copy JAR to PolyMC test mods if path is known.

Do not spend the first half of the task writing a theoretical architecture document.
The architecture is already specified.

---

# 48. STRICT NON-GOALS / THINGS CODEX MUST NOT "HELPFULLY" ADD

Do NOT add:
- boss;
- mini-boss;
- undead mobs;
- traps;
- puzzles;
- secret-key system;
- magical crystals;
- procedural questline;
- minimap;
- Disturbance HUD bar;
- special kurgan compass;
- teleport command for normal players;
- lore books unless already required by existing content;
- new weapon set;
- late-medieval décor;
- random curse symptoms beyond the specified penalties;
- screen shaders;
- constant particle aura;
- 16×16 final art.

Do not rework unrelated mod content.

---

# 49. ACCEPTANCE / DEFINITION OF DONE

The task is complete only when ALL applicable items below are true.

## World generation
- [ ] three kurgan tiers exist;
- [ ] exterior mound uses vanilla palette;
- [ ] Small = 1 floor;
- [ ] Small = 3–6 content rooms;
- [ ] Warrior/Clan = 2–3 floors;
- [ ] Warrior/Clan = 10–18 content rooms;
- [ ] Great = 4–5 floors;
- [ ] Great = 25–40 content rooms;
- [ ] same-tier structures can generate different layouts;
- [ ] guaranteed entrance-to-final route exists;
- [ ] floors connect physically;
- [ ] piece overlaps are rejected;
- [ ] Tier II/III can contain loops/shortcuts;
- [ ] no broken partial labyrinth is placed after failed validation.

## Visual blocks
- [ ] 8 core palette blocks implemented;
- [ ] both color palettes used;
- [ ] cracked variants visibly cracked;
- [ ] mossy variants visibly mossy;
- [ ] `sealed_kurgan_masonry` implemented;
- [ ] final visible textures are >=32×32;
- [ ] no missing-texture models.

## Architecture
- [ ] corridor clear height 4–5;
- [ ] standard corridor width 3;
- [ ] wider/grand variants present;
- [ ] floor stairs clear width 3;
- [ ] small burial room follows reference;
- [ ] large burial room follows reference;
- [ ] Great final tomb is round/near-round/octagonal;
- [ ] Great final tomb is much larger than normal rooms;
- [ ] Great final tomb exists only on deepest floor;
- [ ] three final-tomb niches exist;
- [ ] protected shell cannot be survival-mined/exploded through;
- [ ] no adjacent bypass gap into sealed room.

## Disturbance and curse
- [ ] Disturbance is per-kurgan;
- [ ] Disturbance is saved;
- [ ] normal HUD does not expose numeric Disturbance;
- [ ] repeated opening of same source cannot infinitely increase it;
- [ ] temporary Tier-II curse implemented;
- [ ] Great persistent curse implemented;
- [ ] Great curse persists through death/relogin;
- [ ] milk/generic clear cannot permanently cure Great curse;
- [ ] debug clear removes Great curse state;
- [ ] future boss-clear hook exists;
- [ ] custom curse icon implemented from specified motif/design.

## Commands/docs
- [ ] locate each tier;
- [ ] generate each tier;
- [ ] info/debug inspection;
- [ ] clear curse debug command;
- [ ] README updated;
- [ ] README lists exact working commands;
- [ ] README has manual test procedure;
- [ ] README documents persistent-curse 0.8.7 limitation.

## Delivery
- [ ] Minecraft client was not auto-launched;
- [ ] headless build completed if environment supports it;
- [ ] JAR produced;
- [ ] PolyMC test mods updated if the path is available.

---

# 50. FINAL RESPONSE FORMAT — KEEP IT SHORT

Do not return a long essay.

Return only:

## Implemented
Short bullet list.

## Main files changed
Paths.

## Commands
Exact working commands.

## Build
Build result and JAR path.

## PolyMC
Where the JAR was copied, or state that no known path was available.

## Intentionally deferred
- 0.8.6 enemies / mini-bosses
- 0.8.7 Great Kurgan boss / survival curse removal

Do not claim a feature is complete unless the corresponding files were actually changed.
