# 06 — KURGAN ITEMS / ARTIFACTS / PROPS — ULTRA ART REWORK

The user specifically rejected weak Kurgan item art that looked more like assets from a generic browser/mining game than Minecraft.

This file defines the audit and redraw requirements.

---

# 1. INVENTORY BEFORE DRAWING

Search:
- item registries;
- Kurgan loot tables;
- Kurgan recipes;
- artifact restoration recipes;
- archaeology docs;
- localization.

Build exact table:
`ID | RU name | EN name | current PNG | dimensions | purpose | redraw?`

Do not guess registry IDs.

---

# 2. GENERAL ITEM ICON RULES

Every redrawn item:
- source 256×256 minimum;
- clear silhouette;
- 1 main object;
- controlled highlights;
- pixel-art cluster logic;
- transparent background;
- no soft outer glow unless magical and intentionally restrained;
- no smooth airbrushed shading;
- no fake UI frame around item;
- no tiny photo-like details.

At inventory scale, player must identify item shape immediately.

---

# 3. ANCIENT METAL PALETTE

For iron/steel burial objects:
shadow #20272B
dark metal #343C40
mid #505A5E
light edge #737D80
rust #704536 / #8A5841

For bronze/copper:
shadow #3A2A22
dark bronze #664634
mid bronze #8B6244
highlight #B0835C
patina #496A61 used sparingly.

For gold:
dark #7D5A24
mid #B58932
light #D9B85A
highlight #F0D680
Never neon yellow.

---

# 4. EXISTING WEAPON-TYPE KURGAN ITEMS

For every current Kurgan sword/axe/spear/dagger or burial weapon item found:

## Sword
Silhouette:
long straight blade;
blade occupies ~65–72% icon length;
guard clearly distinct;
grip 15–20%;
pommel visible.

Ancient version:
subtle edge chips;
dark iron;
small bronze/gold inlay at guard/pommel;
do not make blade a generic gray rectangle.

## Axe
Head:
broad wedge, asymmetrical old forged form.
Wood grip:
dark aged brown.
Metal:
dark iron with edge highlight.
Optional tiny patina/rust at socket.

## Spear
Must look long.
Do not repeat previous short-spear problem.
Shaft dominates icon.
Head clear and narrow.
Binding/socket visible.

## Dagger
Clearly shorter and broader relative to sword.
Different blade silhouette.
Do not simply scale sword to 80%.

Only redesign item types that actually exist.

---

# 5. JEWELRY

For each current ring/amulet/brooch/belt fitting:

## Ring
Large readable ring silhouette.
Metal band thick enough to survive inventory scale.
One motif:
twisted ends;
small stone;
engraved plate.

## Pendant / amulet
Chain is not rendered as 40 tiny links.
Use short visible loop + dominant pendant.
Pendant silhouette should be distinctive.

## Brooch / fibula
Use crescent/arc body with pin suggestion.
Bronze/patina palette.

## Belt fitting
Rectangular or oval bronze plate.
Geometric stamped motif.
Dark recesses.

No generic RPG jewel sparkle everywhere.

---

# 6. BURIAL / ARCHAEOLOGICAL ITEMS

For existing:
- pottery;
- cups;
- combs;
- coins;
- fragments;
- burial goods.

## Coin
Round/oval old coin.
Dark edge.
One readable stamped symbol.
Irregular chipped perimeter.
No perfect modern coin shine.

## Ceramic vessel/fragment
Earth palette:
#4A352B
#68483A
#865C46
#A17559
Simple incised bands.
No glazed bright orange unless current lore requires.

## Comb
Bone/wood tone.
Strong teeth silhouette.
One decorated spine.

## Fragment
Must read as broken piece of a known object, not random rock.
Show one finished edge + one fracture edge.

---

# 7. RELIC / ARTIFACT ITEMS

Existing major artifacts should look special through:
- stronger silhouette;
- rare material accent;
- carved motif;
- restrained magical highlight if they are magical.

Do NOT use:
rainbow glow;
huge bloom;
generic legendary gold frame.

Artifact quality should come from object design, not UI effects.

---

# 8. URN / BURIAL VESSEL BLOCK OR ITEM

If current Kurgan has urns:
shape:
broad lower body;
narrow neck;
small rim;
optional handles only if readable.

Stone urn:
Cold Burial Stone palette.
Ceramic urn:
earth/charcoal palette.

Carved band:
one horizontal geometric band around upper third.

If container block:
model should have actual volume, not cube with urn texture painted on front.

---

# 9. KURGAN CHEST / CACHE

Only if a custom Kurgan chest already exists or is deliberately added.

Look:
dark aged wood;
iron/bronze straps;
stone dust/moss variant optional;
hardware chunky and Minecraft-readable.

Do not make it look like a modern treasure chest with bright gold everywhere.

---

# 10. SARCOPHAGUS

If current implementation uses a stone sarcophagus:
use block model/structure, not an inventory icon-only solution.

Design:
rectangular stone box;
heavy lid;
2–3 block long depending model;
carved border;
central simple motif;
dark seam between lid/body.

Palette:
Cold Burial Stone + Carved accent.

---

# 11. MANUAL ART QA

For each item:
- open inventory at normal UI scale;
- check against vanilla items;
- confirm silhouette still readable;
- confirm no icon looks like web/mobile game art;
- check transparent edges;
- check no accidental blurry interpolation.

Document before/after filenames.
