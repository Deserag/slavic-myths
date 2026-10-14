# Файлы инструментальных рун III–IV

Сравнение собственного патча — со снимком **рабочего дерева до задачи**, не с HEAD, который не содержит предыдущие незакоммиченные патчи. [BASELINE_HASHES.json](BASELINE_HASHES.json) + [RESOURCE_JAR_CHECKS.json](RESOURCE_JAR_CHECKS.json) доказывают сохранность предшествовавших production files вне явно перечисленных общих файлов. Новые строки lang/catalog и mixin config восстанавливаются к исходным bytes. Три shared status docs — новый prefix перед полным прежним byte suffix.

## Общие Java files и возможные конфликты

| Файл | Точка | Назначение |
|---|---:|---|
| [rpg/Runes.java](../../../src/main/java/org/slavicmyths/rpg/Runes.java) | 12 | 8 IDs, tool admission/matrix, Shovel/Hoe capacity; combat category/costs/max unchanged. |
| [rpg/RuneRework.java](../../../src/main/java/org/slavicmyths/rpg/RuneRework.java) | 20 | 8 factories in the prior ModItems DeferredRegister; equipped descriptions. |
| [rpg/RuneDefinition.java](../../../src/main/java/org/slavicmyths/rpg/RuneDefinition.java) | 34 | 8 real definitions and effect/tooltip/compat delegates. |
| [rpg/RpgEvents.java](../../../src/main/java/org/slavicmyths/rpg/RpgEvents.java) | 38 | Existing player tick hook calls only the active bounded worker. |
| [rpg/AdvancedRunes.java](../../../src/main/java/org/slavicmyths/rpg/AdvancedRunes.java) | 57 | Existing reset/anvil/lifecycle clears ephemeral work. |

Возможные пересечения: **Runes/RuneRework/RuneDefinition** с прошлым rune patch и weapon categories; **RpgEvents/AdvancedRunes** с боевыми и lifecycle hooks. Их пользовательский код сохранён, расширения адресные. Не менялись ItemState/RuneBase/RuneFoundation/RuneCraftRecipe/JeiRunes/RuneRuntime/MiningRunes, оружие/броня/боссы и resource generators прежних milestones. Rune socket capacity логически менялась только для ранее неподдержанных shovel/hoe; их combat category осталась прежней.

## Новая production реализация

| Файл | Назначение |
|---|---|
| [rpg/tools/ToolRuneRules.java](../../../src/main/java/org/slavicmyths/rpg/tools/ToolRuneRules.java) | Actual geometry/admission/family/radius/bounds. |
| [rpg/tools/ToolRuneContent.java](../../../src/main/java/org/slavicmyths/rpg/tools/ToolRuneContent.java) | Actual class compatibility and localized metadata. |
| [rpg/tools/ToolRuneTrees.java](../../../src/main/java/org/slavicmyths/rpg/tools/ToolRuneTrees.java) | Incrementally budgeted conservative detector used by runtime and tests. |
| [rpg/tools/ToolLogOrigins.java](../../../src/main/java/org/slavicmyths/rpg/tools/ToolLogOrigins.java) | Persistent placement/piston deny list, schema/cap/fail-closed handling. |
| [rpg/tools/ToolRuneRuntime.java](../../../src/main/java/org/slavicmyths/rpg/tools/ToolRuneRuntime.java) | Actual protected action queue, intents, crops, lifecycle/placement hooks. |
| [rpg/tools/mixin/ToolRuneBreakMixin.java](../../../src/main/java/org/slavicmyths/rpg/tools/mixin/ToolRuneBreakMixin.java) | Successful actual removal capture; native root/extras behavior. |
| [rpg/tools/mixin/ToolRuneUseMixin.java](../../../src/main/java/org/slavicmyths/rpg/tools/mixin/ToolRuneUseMixin.java) | Post-accepted item-use/placement transaction wrapper. |

## Ресурсы всех восьми items

Concept ID = logical Rune ID; item namespace `slavicmyths`. Каждый имеет свой прозрачный64px glyph по референсу, source64 в `tools/tool-runes-art`. Базовые определения и рецепты предыдущего архива не заменены.

| Concept / Rune ID | Реальный item ID | Texture | Model | Recipe |
|---|---|---|---|---|
| `miner_iii` | `slavicmyths:rune_miner_iii` | `assets/slavicmyths/textures/item/rune_miner_iii.png` | `assets/slavicmyths/models/item/rune_miner_iii.json` | `data/slavicmyths/recipe/tool_rune_miner_iii.json` |
| `excavator_iii` | `slavicmyths:rune_excavator_iii` | `assets/slavicmyths/textures/item/rune_excavator_iii.png` | `assets/slavicmyths/models/item/rune_excavator_iii.json` | `data/slavicmyths/recipe/tool_rune_excavator_iii.json` |
| `farmer_iii` | `slavicmyths:rune_farmer_iii` | `assets/slavicmyths/textures/item/rune_farmer_iii.png` | `assets/slavicmyths/models/item/rune_farmer_iii.json` | `data/slavicmyths/recipe/tool_rune_farmer_iii.json` |
| `lumberjack_iii` | `slavicmyths:rune_lumberjack_iii` | `assets/slavicmyths/textures/item/rune_lumberjack_iii.png` | `assets/slavicmyths/models/item/rune_lumberjack_iii.json` | `data/slavicmyths/recipe/tool_rune_lumberjack_iii.json` |
| `miner_iv` | `slavicmyths:rune_miner_iv` | `assets/slavicmyths/textures/item/rune_miner_iv.png` | `assets/slavicmyths/models/item/rune_miner_iv.json` | `data/slavicmyths/recipe/tool_rune_miner_iv.json` |
| `excavator_iv` | `slavicmyths:rune_excavator_iv` | `assets/slavicmyths/textures/item/rune_excavator_iv.png` | `assets/slavicmyths/models/item/rune_excavator_iv.json` | `data/slavicmyths/recipe/tool_rune_excavator_iv.json` |
| `farmer_iv` | `slavicmyths:rune_farmer_iv` | `assets/slavicmyths/textures/item/rune_farmer_iv.png` | `assets/slavicmyths/models/item/rune_farmer_iv.json` | `data/slavicmyths/recipe/tool_rune_farmer_iv.json` |
| `lumberjack_iv` | `slavicmyths:rune_lumberjack_iv` | `assets/slavicmyths/textures/item/rune_lumberjack_iv.png` | `assets/slavicmyths/models/item/rune_lumberjack_iv.json` | `data/slavicmyths/recipe/tool_rune_lumberjack_iv.json` |

Сопоставление с `spec/blueprints/III_TOOL_RUNES_BLUEPRINT.png`, `IV_TOOL_RUNES_BLUEPRINT.png` и8 previews: [ASSET_MAPPING.json](ASSET_MAPPING.json). Preview64/32: [ICONS.png](ICONS.png), не игровой screenshot. Numeric labels на чертежах не использованы вместо Markdown требований.

## Общие resource files

- [assets/slavicmyths/lang/ru_ru.json](../../../src/main/resources/assets/slavicmyths/lang/ru_ru.json) — 23 новых ключа; прежние values/bytes сохранены.
- [assets/slavicmyths/lang/en_us.json](../../../src/main/resources/assets/slavicmyths/lang/en_us.json) — 23 новых ключа; прежние values/bytes сохранены.
- [assets/slavicmyths/creative_catalog.tsv](../../../src/main/resources/assets/slavicmyths/creative_catalog.tsv) — 8 явных magic/runes_III–IV строк, старый порядок сохранён.
- [META-INF/neoforge.mods.toml](../../../src/main/resources/META-INF/neoforge.mods.toml) — Один новый [[mixins]] config; версия/зависимости не менялись.
- [slavicmyths.tool_runes.mixins.json](../../../src/main/resources/slavicmyths.tool_runes.mixins.json) — Новый config: ровно2 required common/server mixins, Java21.

**Потенциальные конфликты**: lang/catalog с переводами и творческим инвентарём, metadata с другой интеграцией mixins. Не перезаписывались целиком словари переводов или чужие config lists; соответствие старых bytes проверено. Стандартный JEI category/subtype/transfer path сохранён; новые recipes автоматически доступны generic discovery. Actual third-party redirect collisions/GUI/protection совместимость требуют ручной приёмки.

## Tools и evidence

- [tool_runes_resources.py](../../../tools/tool_runes_resources.py).
- [verify_tool_runes.py](../../../tools/verify_tool_runes.py).
- [tool-runes-offline.gradle](../../../tools/tool-runes-offline.gradle).
- [ToolRunesOffline.java](../../../tools/tool-rune-tests/ToolRunesOffline.java).
- `tools/tool-runes-art/`:8 curated64 PNG, воспроизводимые production sources.
- Shared `docs/PROJECT_STATUS.md`, `docs/ROADMAP.md`, `docs/ARCHITECTURE.md`: новая запись; полный прежний текст retained. Эти файлы могут пересекаться с любыми другими status задачами.
- Здесь `REPORT.md`, `FILES.md`, `BUILD_CHECKS.md`, `MANUAL_CHECKS.md`, `START_STATUS.txt`, `STAGED_START.txt`, `BASELINE_HASHES.json`, `OWNED_HASHES.json`, `FILE_MANIFEST.json`, `RESOURCE_JAR_CHECKS.json`, `ICONS.png`, `ASSET_MAPPING.json`, `api/API_SOURCE_HASHES.json`, все compile/offline/build/resource/diff logs и source `spec/` с пользовательскими docs/blueprints/BUNDLE_MANIFEST. Полный per-file inventory/hashes в [FILE_MANIFEST.json](FILE_MANIFEST.json).
- Готовый production JAR в build/libs под прежней1.9.7.3. Полный source/test/art inventory не означает игровую QA:0 запусков. Никакой installation script/изменения PolyMC в этой задаче не было.
- Ветка `codex/tool-runes-iii-iv`, прежний HEAD/staged files сохранены; commit/push/merge не выполнялись.

[Ручные сценарии](MANUAL_CHECKS.md) включают все runtime ограничения, в том числе main hand/adventure и неизвестное старое происхождение брёвен.
