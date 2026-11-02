# Registration inventory — partial, verified against fix/event-bus-register-crash

This document is not yet a complete repository-wide inventory. The original entrypoint was inspected directly. Entries below indicate DeferredRegister bus attachment sites previously in SlavicMyths.java.

| Owner | Field(s) | Original call site |
|---|---|---|
| item.ItemState | COMPONENTS | SlavicMyths constructor |
| winters.WintersContent | EFFECTS | SlavicMyths constructor |
| winters.WintersRecipe | SERIALIZERS | WintersContent.init |
| registry.ModEffects | EFFECTS | SlavicMyths constructor |
| registry.ModBlocks | BLOCKS | SlavicMyths constructor |
| worldgen.ChudRegionPlacements | TYPES | SlavicMyths constructor |
| swamp.SwampStructures | STRUCTURES, PIECES | SlavicMyths constructor |
| item.ModGear | ARMOR_MATERIALS | SlavicMyths constructor |
| registry.ModItemGroup | TABS | SlavicMyths constructor |
| rpg.RuneCraftRecipe | TYPES, SERIALIZERS | SlavicMyths constructor |
| industry.MachineRecipe | TYPES, SERIALIZERS | Industry.register |
| underground.UndergroundMobs | EFFECTS | SlavicMyths constructor |
| registry.ModItems | ITEMS | SlavicMyths constructor |
| armorer.ArmorerRecipe | TYPES, SERIALIZERS | SlavicMyths constructor |
| farming.Farming | FUNCTIONS | SlavicMyths constructor |
| registry.ModLoot | SERIALIZERS | SlavicMyths constructor |
| registry.ModEntities | ENTITIES | SlavicMyths constructor |
| registry.ModSounds | SOUNDS | SlavicMyths constructor |
| registry.ModTiles | TILES | SlavicMyths constructor |
| registry.ModFeatures | FEATURES | SlavicMyths constructor |
| rpg.RpgMenu | MENUS | SlavicMyths constructor |

Registry types are suggested by field names only and require inspection of each declaration.
Repeated registration across other feature classes is not yet proven or disproven.
Remaining work: enumerate all Java sources, declarations and call sites; map static init dependencies; migrate every nested call to the bootstrap; run Gradle and runClient.
