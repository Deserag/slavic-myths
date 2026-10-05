# PHASE 2 — REGISTRIES + ITEMS/BLOCKS + DATA
## Use together with MASTER prompt

Goal:
Port static content and data resources without redesign.

Priority:
1. Items/blocks/block entities.
2. Sounds/effects/menus/particles.
3. Resource paths.
4. Recipes.
5. Loot.
6. Advancements.
7. Tags.
8. ItemStack data components.
9. Curios item integration.

Important:
- preserve registry IDs;
- singularize 1.21 resource folders;
- migrate old stack NBT to custom data components where structured;
- no visual redesign;
- no new recipes;
- no balance changes.

Exit criteria:
- resource verifier passes;
- no unresolved resource registry IDs;
- current items/blocks can register under NeoForge.
