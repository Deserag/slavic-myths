# Реализация через точечный осмотр

1. Определить реальный `build.gradle`, source sets, registries Forge, namespace, datapack locations, существующие в проекте layout helpers. Не делать полного чтения репозитория.
2. Найти **один-два** существующих аналогичных generated structures (капища, баня, овин, лагеря разбойников) и переиспользовать production pattern. Найти existing worldgen injection/TemplateManager/loot marker usage. Не копировать бездумно весь устаревший код.
3. Найти actual 1.8 моровые/заражённые ванильные блоки + предмет очищения. Интегрировать локально; **не менять** общие поведения/IDs существующих объектов.
4. Найти Book of Tales / advancement / recipe / rune crafting. Добавить новое минимально, не переписывая `RuneAnvil` или уровни рун.
5. Реализовать два structural definitions/templates: детерминированное placement, facing rotations, containers, safety bounds, future boss anchors.
6. Добавить blockstates/loot/собственные предметы, resource icons, короткие интеракции и их network sync, lang.
7. Добавить звуковые event mappings и реальные assets/корректное использование уже существующих звуков.
8. Проверить class loading dedicated server, только клиентский код для частиц/отрисовки, ограничение sent packets.

**Не объявлять, что нужный класс точно называется `SanctuaryStructure` или что project использует JEI, пока сам не проверил**. Названия в ТЗ логические, не ссылки на существующий код.
