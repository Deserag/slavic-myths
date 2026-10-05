# SLAVIC MYTHS — 0.9.6 KURGAN REWORK
## ULTRA-DETAILED CODEX IMPLEMENTATION SPECIFICATION
### Minecraft 1.21.1 / NeoForge / Java 21
### Mod ID: `slavicmyths`
### Repository: https://github.com/Deserag/slavic-myths

---

# 0. HOW TO TREAT THIS DOCUMENT

This is NOT a moodboard and NOT a list of loose ideas.

Treat every statement marked MUST / MUST NOT / REQUIRED as an implementation requirement.

Do not replace detailed room design with "generate a dungeon room".
Do not replace block art requirements with a recolor.
Do not replace labyrinth requirements with one main staircase plus side closets.
Do not simplify the package because implementation is more difficult.

The purpose of this package is specifically to remove ambiguity so Codex does not have to guess what the user meant.

Read every supplementary file before changing Kurgan generation.

Approved visual references are included inside `references/`.

---

# 1. VERSION GOAL

0.9.6 is the dedicated Kurgan rework.

Current problems to solve:

1. Kurgans are too empty and often have too little meaningful loot.
2. Their underground topology reads as a large staircase or a mostly linear descent with floors attached.
3. Different routes do not feel meaningfully different.
4. Level changes happen too predictably.
5. Room identities are weak or absent.
6. Existing Kurgan blocks are visually weak.
7. Existing recolored blocks do not feel like deliberate Minecraft materials.
8. Current custom textures are too weak/low-detail for the project’s current visual standard.
9. The current building family lacks enough stairs/slabs/walls/decorative variants.
10. Good-looking Kurgan blocks should be obtainable and usable by players in survival.
11. The entrance and the immediate mound approach need stronger visual identity.
12. Existing Kurgan-specific items/artifacts added in earlier versions must be audited and redrawn if they still use weak/off-style art.

The desired end result:

> A Kurgan must feel like an ancient, dangerous, multi-route underground burial complex with recognizable architecture, distinct chambers, meaningful rewards, optional secrets and a coherent construction palette that players also want to use in their own builds.

---

# 2. HARD SCOPE

0.9.6 MUST include:

- full Kurgan generation redesign;
- real graph-based multi-route labyrinth;
- multiple corridor archetypes;
- several distinct room archetypes;
- level transitions placed in different physical locations;
- loops, branches, dead ends, side rooms and secrets;
- meaningful containers and reward placement;
- full visual rework of current Kurgan building blocks;
- new slab/stair/wall variants for applicable families;
- recipes and stonecutting;
- improved entrance and surface threshold;
- audit/redraw of Kurgan-specific item icons that are visually weak;
- tests and developer diagnostics for generated layouts.

0.9.6 MUST NOT become:
- the global mob overhaul;
- the swamp overhaul;
- the bandit settlement overhaul;
- the final progression pass;
- the final whole-mod bug audit.

---

# 3. VISUAL STANDARD

The user rejected the previous weak block approach.

## 3.1 Asset resolution

For every newly drawn or substantially redrawn Kurgan block/item:
- author/source PNG target: minimum 256×256;
- direct in-mod texture may also remain 256×256 if consistent with current project pipeline;
- never silently fall back to old 16/32px-style low-detail art;
- do not create a 256×256 file by simply nearest-neighbor scaling an old 16×16 asset.

The detail must be newly authored.

## 3.2 Minecraft readability

The texture must still behave like a Minecraft texture:
- strong large shapes first;
- readable block seams;
- controlled pixel clusters;
- no photorealism;
- no smooth Photoshop-like gradients;
- no tiny unstructured noise;
- no browser-game icon aesthetic;
- no fake metallic gloss everywhere.

## 3.3 Approved aesthetic

The approved references define:
- cold ancient stone;
- dark bluish-gray burial material;
- warm torchlight;
- cracked and mossy age;
- ceremonial carved pieces;
- selective vanilla stone integration;
- architectural arches, thresholds, niches and platforms;
- rooms with strong purpose.

See:
`references/ref_01_blocks_and_decor.png`
`references/ref_02_dungeon_cutaway.png`
`references/ref_03_rooms_and_halls.png`
`references/ref_04_corridors_and_labyrinth.png`

---

# 4. REQUIRED MATERIAL FAMILIES

Implement/rework the exact families described in:
`03_BLOCKS_MATERIALS_TEXTURES_AND_CRAFTS_ULTRA.md`

At minimum:
- Cold Burial Stone;
- Burial Stone Bricks;
- Cracked Burial Stone Bricks;
- Mossy/Damp Burial Stone Bricks;
- Carved Burial Stone;
- optional Burial Floor Tile if current implementation benefits from a dedicated floor block.

Applicable families need:
- full block;
- slab;
- stairs;
- wall.

No "same texture copied to every face" where the design requires orientation or face variation.

---

# 5. LABYRINTH ARCHITECTURE

The dungeon MUST be graph-first.

Before actual block placement, create/derive a logical graph containing:
- entrance node;
- room nodes;
- corridor nodes/edges;
- junctions;
- vertical transition nodes;
- secret edges;
- reward nodes;
- danger nodes;
- final/deep objective zone where applicable.

Then translate that graph into modules/placed geometry.

The system MUST validate topology before committing generation.

See:
`02_LABYRINTH_GENERATION_ULTRA.md`

---

# 6. THREE KURGAN TIERS

Preserve the existing conceptual Kurgan tiers and actual registry IDs found in repository.

Do not invent replacement IDs.

## Small Kurgan
Target experience:
compact tomb exploration, still branched.

Approximate:
- 1–2 underground depth bands;
- 7–12 meaningful nodes;
- 2–4 room nodes;
- at least 1 side branch;
- at least 1 dead end OR secret;
- preferably 1 small loop;
- one principal burial/reward focus.

## Warrior Kurgan
Target experience:
defended military burial complex.

Approximate:
- 2–3 depth bands;
- 14–24 meaningful nodes;
- 5–8 room nodes;
- at least 2 meaningful branches;
- at least 2 loops/reconnections;
- guard/warrior room identity;
- multiple rewards and hazards;
- more than one possible physical descent location across the whole structure.

## Great Kurgan
Target experience:
full dungeon expedition.

Approximate:
- 3–4 depth bands;
- 28–45 meaningful nodes;
- 8–14 room nodes;
- multiple branches per depth band;
- at least 4 loops/reconnections;
- at least 2–4 secrets;
- multiple descent/return connectors;
- several high-identity chambers;
- one or more major burial/ritual/reward areas;
- no visually obvious "central staircase solves everything".

These are target ranges, not an excuse to generate nonsensical size. Validate world bounds and module fit.

---

# 7. ROOM SYSTEM

Implement the detailed room catalogue from:
`01_ROOMS_AND_HALLS_ULTRA.md`

Every room has:
- purpose;
- allowed dimensions;
- shape;
- floor;
- wall palette;
- ceiling palette;
- columns/supports;
- props;
- lighting;
- container positions;
- danger;
- connector rules;
- variations.

Do not generate "generic room #1" and call it several names.

---

# 8. CORRIDORS AND CONNECTORS

Implement corridor catalogue from:
`02_LABYRINTH_GENERATION_ULTRA.md`

Corridors must include:
- ordinary burial corridor;
- ruined corridor;
- damp corridor;
- ceremonial arched connector;
- narrow service/secret connector;
- transition stair/shaft types.

No uninterrupted corridor longer than the configured threshold without:
- corner;
- niche;
- support bay;
- junction;
- elevation change;
- room entrance;
or deliberate special reason.

---

# 9. VANILLA BLOCK MIXING

Custom Kurgan blocks MUST be combined with suitable vanilla blocks.

This is explicitly desired.

Common support palette may include:
- stone;
- cobblestone;
- stone bricks;
- cracked stone bricks;
- mossy stone bricks;
- stone slabs/stairs/walls where useful;
- gravel in collapse/debris;
- dirt/rooted dirt only where geology intrudes;
- water in damp/flooded rooms.

Custom stone should carry identity.
Vanilla stone should provide age, variation and Minecraft familiarity.

Do NOT make every room 100% custom material.

---

# 10. REWARDS

Implement room-aware reward placement.

Use:
- current existing Kurgan artifacts/items;
- current currencies/materials where appropriate;
- archaeological/burial objects already present;
- new container blocks only when directly justified.

Do not make a giant dungeon where the only meaningful reward is at the final room.

Side exploration must produce:
- loot;
- shortcuts;
- lore/atmosphere;
- secrets;
- useful containers;
or danger/reward choices.

See:
`05_LOOT_CONTAINERS_AND_REWARD_PLACEMENT_ULTRA.md`

---

# 11. EXISTING KURGAN ITEMS

Audit every Kurgan-specific item already present in registry/loot/recipes.

Produce an inventory before editing:
`docs/kurgan/KURGAN_ITEM_ART_AUDIT_0.9.6.md`

For each:
- ID;
- current PNG dimensions;
- current purpose;
- visual problems;
- whether redesign required;
- target visual description.

Then redraw every weak/off-style item using:
`06_KURGAN_ITEMS_AND_PROPS_ART_REWORK_ULTRA.md`

Do NOT silently invent dozens of new items just to fill the audit.

---

# 12. ENTRANCE

The entrance must become a proper sequence:

surface mound
→ exterior threshold
→ framed doorway / partially ruined portal
→ small vestibule / antechamber
→ first branching underground section.

The entrance must not immediately dump the player into the same long staircase pattern.

See:
`04_ENTRANCE_SURFACE_AND_ATMOSPHERE_ULTRA.md`

---

# 13. GENERATION VALIDATION

Every generated Kurgan must be validated logically.

Reject/regenerate layout if:
- graph is basically a line;
- all descents are clustered in the same location;
- no optional rooms;
- no side reward path;
- room overlap corrupts geometry;
- disconnected mandatory area occurs;
- secret rooms have no valid secret access;
- entrance has no valid route to required deep objective;
- Great Kurgan has too few loops;
- too many dead ends create tedious maze with no payoff;
- generation exceeds safe bounds.

---

# 14. DEV DIAGNOSTICS

Add/extend Kurgan dev diagnostics.

Recommended output:
- seed;
- Kurgan type;
- bounding box;
- depth bands;
- node count;
- room count by type;
- corridor count/type;
- loop count;
- dead-end count;
- secret count;
- transition count per level pair;
- shortest entrance→deep objective path;
- optional-node percentage;
- container count;
- validation result.

A generated Great Kurgan should be inspectable without manually guessing whether topology is good.

---

# 15. NO AUTOMATIC CLIENT LAUNCH

Do not run Minecraft client.

Allowed:
- build;
- data/resource verification;
- headless generator tests;
- deterministic graph tests;
- layout reports.

Create manual QA checklist for user.

---

# 16. REQUIRED DOCUMENTS

Create/update:
- `docs/kurgan/KURGAN_LAYOUT_0.9.6.md`
- `docs/kurgan/KURGAN_ROOMS_0.9.6.md`
- `docs/kurgan/KURGAN_BLOCKS_0.9.6.md`
- `docs/kurgan/KURGAN_ITEM_ART_AUDIT_0.9.6.md`
- `docs/kurgan/KURGAN_LOOT_0.9.6.md`
- `docs/MANUAL_QA_0.9.6_KURGAN.md`

---

# 17. DEFINITION OF DONE

0.9.6 is complete only when:
- Kurgans no longer generate as a simple giant staircase;
- transitions occur in different locations;
- branches and loops exist;
- several room types are visibly distinct;
- rooms use declared block palettes;
- loot/containers are distributed;
- custom block family has been visually reworked;
- first/cold stone is clearly darker blue-gray than vanilla stone;
- slabs/stairs/walls exist;
- recipes/stonecutting work;
- existing weak Kurgan items have been audited/redrawn;
- entrance is stronger;
- reference images are actually included in project/spec review;
- build passes;
- no client auto-launch occurred.
