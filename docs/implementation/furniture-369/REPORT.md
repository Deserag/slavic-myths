# Furniture expansion #369 — implementation checkpoint

**Branch:** `feat/furniture-wood-variants-issue-369-20261031`  
**Base:** the active architecture branch `codex/hell-posad-districts-227-228-20261030` (not `main` or `hell`).

## Scope
- Added 56 variants (7 mod-wood families × 8 shapes), preserving 16 preexisting IDs and behavior.
- Nine complete wood families: pine, linden, apple, rowan, willow, darkened, mor_oak, navya, blackwood. **Deferred:** charred_blackwood has no planks; vanilla families require a separate approval.
- Uses existing unique mod wood textures and original pine furniture geometry via model parent inheritance, retaining particle texture/real log edges, with separate blockstates, item models, loot, shaped survival recipes.
- Registered all new blocks and items, included all tables in `table_display` block entity type and all weapon racks in `weapon_rack` type, retained the originals.
- Dedicated Furniture tab `slavicmyths:furniture` with 72 items, by eight shape families. Existing category indices untouched. Russian/English names.
- Does NOT alter the architecture branch, structure templates, NBT, worldgen, old saves or decorative containers.

## Checks and acceptance

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| All 56 ID-specific JSON sources, textures, recipes, loot and localization | `tools/furniture-expansion/verify_furniture.py` | NOT_RUN — CI pending |
| Reproducible new JSON (without modifying legacy items) | `tools/furniture-expansion/generate.py` | Source prepared; regeneration comparison NOT_RUN |
| Creative inventory ordering, stable category indices | `tools/creative-tests/CreativeCatalogChecks.java` | NOT_RUN — CI pending |
| Java 21 clean build and production artifact | `.github/workflows/furniture-369-java21.yml` | NOT_RUN — CI pending |
| Runtime seat, multi-user, table display and weapon rack NBT | Actual isolated Minecraft test | NOT_RUN |
| Visual side-by-side comparison of seven new textures and silhouettes | Client screenshots, owner acceptance | NOT_RUN |
| PolyMC JAR installed and SHA checked | Owner machine | POLYMC_NOT_INSTALLED |
| User acceptance of exact 56 variants and charred exclusion | #369 | OWNER_PENDING |

**Owner manual tests:** survival recipe with each wood; place/rotate/sit/break; join two equal tables; food-on-table, rack item save/relog/break, wardrobe break upper/lower, 72 items in tab, no NBT/data loss in copy of old world.

The issue remains open, cannot be marked Done until owner acceptance.
