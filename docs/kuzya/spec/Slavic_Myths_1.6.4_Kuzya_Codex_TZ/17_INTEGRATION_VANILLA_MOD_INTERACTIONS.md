# Integration with existing Slavic Myths and Minecraft

- Reuse existing Baba Yaga NPC registry, model assets/AI, structure, loot table mechanisms and events. Never replace her with generic villager. If old hut already contains brewing or altar blocks, honor their original behavior while placing new decor around them.
- Preserve existing Domovoy personal relations, hearth and blessing behavior; Kuzya does not share resident key nor accept gifts through Domovoy logic. Reusing abstract base `HouseSpirit` only if semantics separate and no auto blessing.
- Existing kitchen/cooking foods may be used as cosmetic prop references; do not create broken item recipes or introduce unfinished food items. Entity's chest inventory not exposed except designated vanilla barrel.
- Existing class/accessories systems, Nocnitsa hostility, Wolf, etc must not classify Kuzya as targetable. Non-combat neutral category, not hostile. If faction selectors call targeting helpers, explicitly omit `KuzyaEntity`.
- In hut, if player manipulates table/barrel, Kuzya must not block interaction or consume real barrel items; line-of-sight to barrel remains valid when Kuzya nearby.
- No external dependencies on image/audio generative services at runtime. Resource files from ZIP included in built mod JAR. Sound registry and assets compile on Windows and Linux (UTF-8 Russian strings supported).
