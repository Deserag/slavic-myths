# 07 — TESTING / ACCEPTANCE — ULTRA DETAIL

Do not auto-launch Minecraft client.

## Required checks

### Structures
- each swamp structure type can generate;
- structures fit swamp water/ground cleanly;
- no floating docks/posts;
- structure purpose and loot are present.

### Mobs
- swamp mobs spawn in intended contexts;
- HP/AI changes apply;
- stronger encounters feel distinct;
- no obviously broken targeting/pathing.

### Nature
- each new tree archetype appears;
- tree silhouettes vary;
- repetition reduced;
- water-edge integration looks intentional.

### Loot
- no empty “important” structures;
- loot roughly matches structure purpose;
- rare sites give meaningfully better rewards.

### Materials/Ores
- touched blocks look Minecraft-like;
- ores read correctly;
- no broken textures or missing models.

## Manual QA document
Create `docs/MANUAL_QA_0.9.7_SWAMP.md` including:
- find several huts;
- find watchtower;
- find shrine;
- verify rewards;
- verify stronger mob encounter;
- compare trees;
- inspect ore/block visuals;
- inspect water placement.

## Final response format
Return:
1. structures implemented/reworked;
2. mob changes;
3. tree/nature changes;
4. block/ore changes;
5. loot/reward changes;
6. worldgen integration;
7. tests run;
8. remaining limitations.
