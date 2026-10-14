# Файлы собственного патча и конфликты — 2026-10-14

Граница собственных изменений определена снимком рабочего дерева, а не diff от HEAD (HEAD не содержит параллельные незакоммиченные патчи). Полный список и hashes: [FILE_MANIFEST.json](FILE_MANIFEST.json). Source/resource/JAR preservation gates подтверждают неизменность всех предшествовавших production files вне явно разрешённых ниже общих файлов; старые RU/EN строки реконструируются побайтно. Три status docs содержат новый префикс и весь прежний byte suffix.

## Java: общие и новые файлы

| Файл | Точка (строка) | Собственное назначение |
|---|---:|---|
| [rpg/AdvancedRuneRules.java](../../../src/main/java/org/slavicmyths/rpg/AdvancedRuneRules.java) | 7 | Добавлены четыре craft IV в прежний список регистрации/definition. |
| [rpg/AdvancedRunes.java](../../../src/main/java/org/slavicmyths/rpg/AdvancedRunes.java) | 35 | Матрица/описания, защита IV перед III, единый damage cap, mining cap, lifecycle hooks; прежний final block hook сохранён. |
| [rpg/RuneBase.java](../../../src/main/java/org/slavicmyths/rpg/RuneBase.java) | 16 | Только допустимый IV/perunite, прежний формат codec. |
| [rpg/RuneFoundation.java](../../../src/main/java/org/slavicmyths/rpg/RuneFoundation.java) | 16 | Прежний register/creative/tooltip расширен до IV. |
| [rpg/Runes.java](../../../src/main/java/org/slavicmyths/rpg/Runes.java) | 12 | Четыре новых ID, старые ID/совместимость/вместимость; порядок reset/refresh на наковальне. |
| [rpg/RpgEvents.java](../../../src/main/java/org/slavicmyths/rpg/RpgEvents.java) | 38 | Вызов из существующего локального timer; AttackEntity intent. |
| [rpg/RuneRuntime.java](../../../src/main/java/org/slavicmyths/rpg/RuneRuntime.java) | 33 | Только transient speed Ярости через старый modifier. |
| [industry/IndustryAccessories.java](../../../src/main/java/org/slavicmyths/industry/IndustryAccessories.java) | 36 | Дополнение существующего ore-Fortune hook; без второго loot. |
| [compat/JeiRunes.java](../../../src/main/java/org/slavicmyths/compat/JeiRunes.java) | 26 | IV subtype; прежние категории/recipe discovery. |
| [rpg/CraftRuneRules.java](../../../src/main/java/org/slavicmyths/rpg/CraftRuneRules.java) | 6 | Новый pure helper с реальными формулами/state. |
| [rpg/CraftRunes.java](../../../src/main/java/org/slavicmyths/rpg/CraftRunes.java) | 14 | Новая реализация hooks без второго subscriber; event-local server authority. |

Потенциальные конфликты с предыдущими патчами: **Runes/AdvancedRunes/AdvancedRuneRules** (добавления предыдущего rune patch и оружейные категории), **RpgEvents/RuneRuntime** (серверные combat hooks/атрибуты), **RuneFoundation/RuneBase** (основы), **IndustryAccessories** (mining/loot), **JeiRunes** (optional client adapter). Правки ограничены расширением рун; реестры ModItems и weapon tags/models не менялись. Пользовательский `equipment/mixin/WardBlockMixin.java` сохранён побайтно — он уже содержал вызов AdvancedRunes.blocked. Он не собственный изменённый файл.

## Ресурсы: concept → item → texture/model

Item namespace всегда `slavicmyths`. Texture и model root: `src/main/resources/assets/slavicmyths/textures/item/` и `models/item/`.

| Concept | Реальный item path | Texture | Model | Статус |
|---|---|---|---|---|
| `counterstrike_iii` | `rune_counterstrike_iii` | `rune_counterstrike_iii.png` | `rune_counterstrike_iii.json` | Существующий, bytes сохранены |
| `hunter_iii` | `rune_hunter_iii` | `rune_hunter_iii.png` | `rune_hunter_iii.json` | Существующий, bytes сохранены |
| `last_stand_iii` | `rune_last_stand_iii` | `rune_last_stand_iii.png` | `rune_last_stand_iii.json` | Существующий, bytes сохранены |
| `depths_iii` | `rune_depths_iii` | `rune_depths_iii.png` | `rune_depths_iii.json` | Существующий, bytes сохранены |
| `unbreakable_iv` | `rune_unbreakable_iv` | `rune_unbreakable_iv.png` | `rune_unbreakable_iv.json` | Новый |
| `perun_rage_iv` | `rune_perun_rage_iv` | `rune_perun_rage_iv.png` | `rune_perun_rage_iv.json` | Новый |
| `forest_spirit_iv` | `rune_forest_spirit_iv` | `rune_forest_spirit_iv.png` | `rune_forest_spirit_iv.json` | Новый |
| `mastery_iv` | `rune_mastery_iv` | `rune_mastery_iv.png` | `rune_mastery_iv.json` | Новый |
| `blank_rune_4` | `blank_rune_4` | `blank_rune_4.png` | `blank_rune_4.json` | Новый |

Точная карта и hashes/art adaptation: [ASSET_MAPPING.json](ASSET_MAPPING.json). Референсы сохранены в `spec/02_blueprints`; исходная карта — [ASSET_MAP.csv](spec/03_mappings/ASSET_MAP.csv). Копия чертежей не входит в production resources. Велесова клятва и её resources не менялись.

## Рецепты и общие resource files

- [rune_foundation_blank_4.json](../../../src/main/resources/data/slavicmyths/recipe/rune_foundation_blank_4.json) — новый `rune_crafting`, exact base/counts, три входа.
- [rune_rework_unbreakable_iv.json](../../../src/main/resources/data/slavicmyths/recipe/rune_rework_unbreakable_iv.json) — новый `rune_crafting`, exact base/counts, три входа.
- [rune_rework_perun_rage_iv.json](../../../src/main/resources/data/slavicmyths/recipe/rune_rework_perun_rage_iv.json) — новый `rune_crafting`, exact base/counts, три входа.
- [rune_rework_forest_spirit_iv.json](../../../src/main/resources/data/slavicmyths/recipe/rune_rework_forest_spirit_iv.json) — новый `rune_crafting`, exact base/counts, три входа.
- [rune_rework_mastery_iv.json](../../../src/main/resources/data/slavicmyths/recipe/rune_rework_mastery_iv.json) — новый `rune_crafting`, exact base/counts, три входа.
- [ru_ru.json](../../../src/main/resources/assets/slavicmyths/lang/ru_ru.json) — 13 новых RU keys + 2 уточнённые прежние подсказки; исходный текст восстановим побайтно.
- [en_us.json](../../../src/main/resources/assets/slavicmyths/lang/en_us.json) — 13 новых EN keys + 2 уточнённые прежние подсказки; исходный текст восстановим побайтно.
- [creative_catalog.tsv](../../../src/main/resources/assets/slavicmyths/creative_catalog.tsv) — 5 явных строк в прежнюю magic/runes_IV группу.

Именно lang и creative catalog имеют возможные пересечения с параллельной локализацией/инвентарём. Генератор добавляет/обновляет только собственные ключи/строки, не заменяет JSON целиком новым словарём и не откатывает прежние resources.

## Инструменты, документы и доказательства

- [runes_craft_resources.py](../../../tools/runes_craft_resources.py).
- [verify_runes_craft.py](../../../tools/verify_runes_craft.py).
- [runes-craft-offline.gradle](../../../tools/runes-craft-offline.gradle).
- [RunesCraftOffline.java](../../../tools/runes-craft-tests/RunesCraftOffline.java).
- `tools/runes-craft-art/`: пять curated 64px sprite sources, production воспроизводится из них без нового AI запроса.
- Общие `docs/PROJECT_STATUS.md`, `docs/ROADMAP.md`, `docs/ARCHITECTURE.md`: только новая запись перед сохранённым прежним текстом; потенциальное пересечение с любой другой задачей, обновляющей статус.
- Этот каталог: `REPORT.md`, `FILES.md`, `BUILD_CHECKS.md`, `MANUAL_CHECKS.md`, `BASELINE_HASHES.json`, `START_STATUS.txt`, `WORKTREES.txt`, `RUNS_BRANCH.txt`, `STAGED_FILES.txt`, `FILE_MANIFEST.json`, `RESOURCE_JAR_CHECKS.json`, `ASSET_MAPPING.json`, `ICONS.png`, `OWNED_HASHES.json`; intake `spec/01_docs`, `spec/02_blueprints`, `spec/03_mappings`; все compile/build/offline/resource logs и `DIFF_CHECK.log`. Все конкретные файлы перечислены в manifest.
- Git staged additions прежней rune задачи не менялись; `git add`, commit/push/branch switch не выполнялись.

Полный игровой список открыт: [MANUAL_CHECKS.md](MANUAL_CHECKS.md). Вне этого списка новая реализация не оставлена как будущий каркас.

## Последующая установка по запросу пользователя

Добавлен `tools/install_runes_craft.ps1`, `POLYMC_INSTALLATION.md/json`; обновлены этот отчёт, REPORT и три status docs. Production source/resources не менялись.
