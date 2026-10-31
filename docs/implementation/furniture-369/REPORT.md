# Furniture expansion #369 — implementation checkpoint

**Branch:** `feat/furniture-wood-variants-issue-369-20261031`  
**Base:** the active architecture branch `codex/hell-posad-districts-227-228-20261030` (not `main` or `hell`).

## Scope
- Added 56 variants (7 mod-wood families × 8 shapes), preserving 16 preexisting IDs and behavior.
- Nine complete wood families: pine, linden, apple, rowan, willow, darkened, mor_oak, navya, blackwood. **Excluded by owner decision 31.10.2026:** charred_blackwood — no new planks and none of the eight furniture variants. Vanilla families are outside this mod-wood patch and require separate approval.
- Uses existing unique mod wood textures and original pine furniture geometry via model parent inheritance, retaining particle texture/real log edges, with separate blockstates, item models, loot, shaped survival recipes.
- Registered all new blocks and items, included all tables in `table_display` block entity type and all weapon racks in `weapon_rack` type, retained the originals.
- Dedicated Furniture tab `slavicmyths:furniture` with 72 items, by eight shape families. Existing category indices untouched. Russian/English names.
- Does NOT alter the architecture branch, structure templates, NBT, worldgen, old saves or decorative containers.

## Checks and acceptance

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| All 56 ID-specific JSON sources, textures, recipes, loot and localization; charred exclusion | `tools/furniture-expansion/verify_furniture.py` | PASS on baseline `43085cbe` (56/56); exclusion guard added in follow-up, awaiting its CI |
| Reproducible new JSON (without modifying legacy items) | `tools/furniture-expansion/generate.py` + `git diff --exit-code` in furniture CI | New regression gate added; its CI result pending |
| Creative inventory ordering, stable category indices | `tools/creative-tests/CreativeCatalogChecks.java` | PASS on baseline `43085cbe` |
| Java 21 clean build and production artifact | [GitHub Actions run 41180819893](https://github.com/Deserag/slavic-myths/actions/runs/41180819893) | PASS for baseline `43085cbe` (Java 21, resource verification, artifact); follow-up CI pending |
| Runtime seat, multi-user, table display and weapon rack NBT | Actual isolated Minecraft test | NOT_RUN |
| Visual side-by-side comparison of seven new textures and silhouettes | Client screenshots, owner acceptance | NOT_RUN |
| PolyMC JAR installed and SHA checked | Owner machine | POLYMC_NOT_INSTALLED |
| User acceptance of exact 56 variants and charred exclusion | #369 | OWNER_PENDING |

**Owner manual tests:** survival recipe with each wood; place/rotate/sit/break; join two equal tables; food-on-table, rack item save/relog/break, wardrobe break upper/lower, 72 items in tab, no NBT/data loss in copy of old world.

The baseline CI artifact is `furniture-369-java21-jar`, version `2.1.6.2-TEST`, reported SHA256 `12f279cd3bf75ca78c02c7572b30c114aa8e98c3170400540e79266716342ae6`. This is **BUILT**, not installed. Regeneration drift and the explicit charred exclusion are now CI checks on the subsequent commit, not claimed as already passing.\n\nThe issue remains open, cannot be marked Done until owner acceptance.
