# Existing Baba Yaga chicken-leg hut: overhaul, NOT replacement

The original structure is already in mod. Inspect its `structure` NBT / data templates, generation processor list, block palettes, bounding boxes, generation rotation, Baba Yaga spawn. **Do not overwrite exterior, chicken legs, entrance location, existing Yaga home AI or other custom blocks.** No second fake Yaga or second copy of house. Existing generated huts are not all to be destructively retrofitted on chunk load; new generation gets updated template, legacy only via safe opt-in upgrade/command if supported.

## Canonical composition (see `references/APPROVED_02_BABA_YAGA_HUT.png`)
1. **Real Russian white stove:** face warm ivory masonry with darker cracks; orange ember recess (avoid constant spread/fire ticking), proper chimney if existing; top **raised sleeping ledge physically supports Kuzya** with blanket in red/burgundy and white stitching. Width along ledge ≥1.8 world blocks if physically possible, safe climb path via low step or stairs, top head clearance ≥.90b for tiny NPC. No sleeping in fire collision. Rest anchor top offset +.04b, root Y on actual sleeping surface.
2. **Food table:** thick brown wood, table top, two stools/bench, clay bowls, bread, baked pastries, milk pitcher, patterned woven runner, crumbs. One chair/seat anchor with full body clearance and table reach 0.3–0.55b in front of K. Props should not block vanilla inventory UI or obstruct passage.
3. **Baba Yaga magic station:** existing magical cauldron is retained; make shelves herbs, red berries, hanging roots, mushroom drying strings, jars and tiny ingredient bags; accessible walkway around cauldron ≥1 world block for Baba Yaga NPC if she's ≥0.6b wide. Support her existing activities.
4. **Kuzya corner:** low crate/chest-like personal furniture, folded rag blanket, small jug, old rag doll / book/small broom, a few personal fabrics. Decorative only; do not give a second interactable inventory unless existing game systems already do.
5. **Storage and lighting:** **one vanilla barrel** (if free layout area), vines/herb bunches, 2–4 baskets, 4–8 shelf segments, 2–5 jars, 1–2 warm lanterns, rugs and woven red/brown textiles. Source game assets prefer existing mod item IDs; if missing use vanilla decorations without new item IDs. Lamp light level should make the place warm, but not all materials emissive.

## Placement / access constraints
- `diagrams/INTERIOR_ZONE_SCHEMATIC.png` is an adjacency/zone schematic, **NOT proof of existing template dimensions**. Use discovered template inner floor size, calculate positions by template-local rotation/offset; preserve doorway, stove-front, cauldron, nav points, windows.
- Minimum unobstructed primary door→center→Yaga path 1.0 b wide and 1.9 b high; Kuzya may use 0.65b passages but not route through solid wall or loot barrel; door must still be operable, exterior remains on chicken legs.
- Do not hide/replace interactive Yaga workbench/cauldron with deco models. Items on shelves can be decorative blockstates but do not make gratuitous 100-tile entities.
- Every anchor requires runtime coordinates and validity at all hut rotations (N/E/S/W). Test with structure placement facing 4 orientations.

## Legacy structure policy
If updating template with same structure ID cannot affect existing generated buildings without destructive modifications, document it clearly; apply new interior to future hut generation, plus optionally a non-destructive admin upgrade that only adds in verified-air positions. Do **not** silently overwrite player chests, floors, placed blocks or Yaga's changed workstation.

## Performance
Prefer static blockstates or baked block models for props, not 50 BlockEntities with tick updates. Mark visible decoration placement deterministic per structure; load-only Kuzya AI when chunk loaded. No per-tick scan of entire building; precompute/serialize zones.
