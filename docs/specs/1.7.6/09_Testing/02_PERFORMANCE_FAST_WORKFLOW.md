# Fast execution strategy for Codex (NOT a replacement for implementation)

1. Determine code locations with targeted filename search by ITEM/RECIPE/RUNE/ACCESSORY/VICTORY tags; no whole-repo reading. Reuse existing utility classes instead of inventing a framework.
2. Register 19 items and copy the 19 image assets early. Build once to catch naming error.
3. Wire runic anvil and accessory hooks with smallest possible adapters; no general-purpose event bus rewrite.
4. Implement 4 hero gift and 5 trophy effects using event-driven hooks, not 20 independent global tick searches.
5. Wire boss victory -> rewards and retroactive claimed state with atomic grants. Use existing save records and UI.
6. Register recipes, JEI, RU/EN strings, sounds/VFX. Validate item models and wearables in actual game.
7. Run targeted compile/smoke tests and deliver changed-file list, screenshots if possible, limitations clearly stated. Put compiled JAR into player's designated PolyMC mods directory ONLY if path exists and workflow is available, never guess path or overwrite unrelated mods.

Time efficiency rule: do not spend project time on broad creative research, rewriting old documentation, renaming old code or re-inventing mechanics; implement the exact approved package and stop.
