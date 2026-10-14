# 03 — Файлы, архитектура и интеграция

## Исходные точки (проверены по предыдущему состоянию main, сверить с рабочей веткой)
- `src/main/java/org/slavicmyths/rpg/Runes.java`: `IDS`, `rune/itemId`, `compatible`, установка и проверка сокетов. В прежнем коде 37 базовых rune IDs; работа другого Codex могла уже добавить 5.
- `src/main/java/org/slavicmyths/rpg/RuneBase.java`: codec/validator для tier 1–3. Расширить до 4 **без слома прежней сериализации**.
- `src/main/java/org/slavicmyths/rpg/RuneFoundation.java`: регистрация основ 1–3 и креативного списка; добавить `blank_rune_4`.
- `src/main/java/org/slavicmyths/rpg/RuneCraftRecipe.java`: рецепты `slavicmyths:rune_crafting`, максимум три **входа**, проверка data component `RuneBase`.
- `src/main/java/org/slavicmyths/rpg/RuneRework.java`, `RuneBalance.java`, `RuneRuntime.java`, `RuneDefinition.java`, `RuneEffects.java`, `RpgEvents.java`: совместимость, meta tooltip, установка, эффекты — **только точечные изменения, без дублирующих слушателей**.
- `src/main/java/org/slavicmyths/industry/MiningRunes.java`, `IndustryRules.java`: корректное сложение шахтёрских бонусов и Fortune; реальные исходники уточнить.
- `src/main/java/org/slavicmyths/compat/JeiRunes.java`: рецепты и subtype interpreter заготовки IV. Не менять чужие категории JEI.
- `src/main/resources/assets/slavicmyths/lang/ru_ru.json` и `en_us.json`: новинки, UI, tooltips. Эти файлы часто затрагиваются другими патчами: редактировать минимально с сохранением изменений других задач.
- `src/main/resources/data/slavicmyths/recipe/`: шаблоны `rune_foundation_blank_3.json`, `rune_rework_*.json`. Пример output для blank IV содержит компонент `slavicmyths:rune_base`.
- `src/main/resources/assets/slavicmyths/models/item/` и `textures/item/`: файлы пяти новых предметов.

## Обязательный минимум новых путей (по именам в новом ТЗ)

```text
src/main/resources/data/slavicmyths/recipe/rune_foundation_blank_4.json
src/main/resources/data/slavicmyths/recipe/rune_rework_unbreakable_iv.json
src/main/resources/data/slavicmyths/recipe/rune_rework_perun_rage_iv.json
src/main/resources/data/slavicmyths/recipe/rune_rework_forest_spirit_iv.json
src/main/resources/data/slavicmyths/recipe/rune_rework_mastery_iv.json
src/main/resources/assets/slavicmyths/models/item/blank_rune_4.json
src/main/resources/assets/slavicmyths/models/item/rune_unbreakable_iv.json
src/main/resources/assets/slavicmyths/models/item/rune_perun_rage_iv.json
src/main/resources/assets/slavicmyths/models/item/rune_forest_spirit_iv.json
src/main/resources/assets/slavicmyths/models/item/rune_mastery_iv.json
src/main/resources/assets/slavicmyths/textures/item/blank_rune_4.png
src/main/resources/assets/slavicmyths/textures/item/rune_unbreakable_iv.png
src/main/resources/assets/slavicmyths/textures/item/rune_perun_rage_iv.png
src/main/resources/assets/slavicmyths/textures/item/rune_forest_spirit_iv.png
src/main/resources/assets/slavicmyths/textures/item/rune_mastery_iv.png
```

Новые Java классы создавать лишь если сокращается сложность и устраняется смешение legacy/modern путей. Избегать нового параллельного item registry или второго component storage.

## Рецепты
- У всех пяти JSON `type=slavicmyths:rune_crafting` и не более **трёх** `inputs`.
- Основа IV имеет **RUNE_BASE = {"tier":4, "material":"perunite"}**. При выполнении рецепта IV на основе проверить и входной компонент базы, и выходные item ID.
- Сверить реальные ID ингредиентов; проект существующих JSON подтверждает `slavicmyths:empowered_soul`, `slavicmyths:bound_soul`, `slavicmyths:perunite_dust`, `slavicmyths:diamond_dust`.
- Рецепты III из предыдущего патча **не менять без установленной проблемы**; если отсутствуют в локальном дереве, восстановить четыре согласно прежней согласованной реализации, но не пытаться создать новый параллельный вариант.

## Совместимость и ветки
- До изменений вывести `git status -sb`, HEAD SHA, список модифицированных файлов. Провести поверх чистой/контролируемой ветки без удаления пользовательских файлов.
- Из-за параллельных оружейных и броневых работ высок риск в `Runes.java`, tag файлах оружия, `ru_ru/en_us`, `ModItems`, loot/registry. Ограничить diff соответствующими строками. Не использовать автозамену целиком lang или реестра.
- Если требуется изменить общий файл, включить его в `FILES.md` и записать точные строки назначения; в случае реально несовместимого одновременно редактируемого кода не перезатирать и назвать блокер.
- Работа должна быть завершена в рабочей ветке/рабочем дереве. Не делать `git add -A` или commit/push без команды.

## Проверки корректности атрибутов
- В `RuneRuntime` раньше модификаторы обновлялись через `LivingEquipmentChangeEvent`, login/respawn/dimension. Для IV обязательно обновление характеристик после **установки/извлечения на наковальне без снятия экипировки**; отсутствие мгновенного пересчёта — баг.
- Реальный сервер авторитетен при проверке кулдауна, rune slots, разрешённой цели, прочих состояний. Client tooltip не может считаться валидатором.
- `RuneBalance.modern()` раньше разрешал дубликаты modern I/II. Новые IV не должны автоматически унаследовать дублирование. Отдельная ясная проверка установки IV.
- Не проводить глобальную миграцию data component `ItemState` ради добавления новых рун.

## Документы после выполнения
Сгенерировать `docs/implementation/runes-craft-iii-iv/REPORT.md`, `FILES.md` (включить Java, JSON, lang, texture/model, тесты), `BUILD_CHECKS.md`; проверить наличие файлов и новый JAR. Не присваивать релизу новую строковую версию самостоятельно, если проект использует отдельный процесс релиза.
