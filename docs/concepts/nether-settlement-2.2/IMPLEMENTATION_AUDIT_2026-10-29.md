# Slavic Myths 2.2 — фактический аудит WorldGen и инженерный план
Дата: 2026-10-29. Ветка: hell. Исходный SHA дерева: c38c593b25a4d64d07b39db62fecf2256fd38494.
Ссылки: эпик #343, инженерная задача #358, стройкомплекты #346 #347 #348 #350 #351 #352 #355 #356.

## Подтверждённые результаты
1. До этого этапа в java отсутствовал модуль posad, native structure, chunk pieces, structure set, а в Nether worldgen отсутствовали городские candidates. Существующие Chud city и Nether watchposts — возможные образцы, не реализация города.
2. Игровые ресурсы: один NBT (data/slavicmyths/structure/posad/granary_2_2_8.nbt), размер 25x11x28, 1349 блоков, 5 chest block entities; пять server-side таблиц посевов/зерна и инструментов.
3. Дальнейшие модели зданий пока только docs JSON/Python/SVG и черновые loot tables. Без точной конвертации и валидации не ставить их в мир.
4. Реестр уже имеет ancient_coin, rye/barley/oat seeds и зерно, flax_stalk и sickle — не дублировать registry.
5. Оригинальные большие художественные PNG нескольких ранних построек отсутствуют; зафиксировать визуальный gate, не выдавать SVG за арт.

## Исполнительский порядок
A. Geometry contract: фиксированные участки 113x113, общий поворот на четверть оборота, правильная разбивка чанков; исключение overlap. Java offline проверки подтверждают только математику.
B. Native WorldGen: registrar StructureType/PieceType, Nether biome tag с vanilla/modded биомами, structure_set, /locate, seed-deterministic выбор, bounded noise preflight, защиты от соседних структур и синхронной chunk loading.
C. Укладка shell по R1: платформа, стены толщиной 5, башни, патруль, открытый семиблочный проезд, дороги/площадь. Земля/лава: несущие подпорки, поиск достижимого берега и мост. Не удалось построить безопасно — отвергать кандидат.
D. Здания: точный NBT из согласованных моделей, all-blockstate rotation, фиксированная координата каждого здания; серверный лут строго по утверждённым таблицам. Начать с готового NBT житницы.
E. Retrofit: сохранённый server-side immutable anchor, schema and slot stages, полный preflight всех нужных чанков и чужих блоков; durable APPLYING до изменений; при неполном состоянии не повторять действия и награды. UNKNOWN/legacy анкоры не трогать.
F. Реальные испытания: Gradle build, NeoForge GameTest, dedicated server, разные seed и 4 направления, 5 vanilla и подходящие модовые Nether биомы, суша и лавовые океаны, границы чанков, multiplayer, save/reload, TPS/performance, скриншоты и журналы.
G. После ядра — 2.2.9 растения/еда, 2.2.10 зелейный стол без выдуманной системы зелий, 2.2.11 жители, 2.2.12 торговля/поручения/награды в Вечевой палате, 2.2.13 босс и городской цикл, после сверки существующих boss issues.

## Фактический статус
Этот коммит вводит только A и запретительный вычислительный precheck E. Нет действующего worldgen, размещения NBT, сохранённого журнала миграции или серверного QA. Offline javac/Actions не равны сборке всего мода и Minecraft QA.

## Дельта после исходного SHA 29.10.2026
- Введён серверно-нейтральный геометрический контракт и fail-closed policy в a8101a7. [Java21 оффлайн CI](https://github.com/Deserag/slavic-myths/actions/runs/40909295495) — 154107 проверок успешно.
- В ecf1db3 в runtime добавлен второй NBT: src/main/resources/data/slavicmyths/structure/posad/zeleynaya_2_2_7.nbt и 5 таблиц posad_zeleynaya_*.json. Теперь NBT **два**: 2.2.7 и 2.2.8. Это подготовленные ресурсы, они пока НЕ ставятся worldgen.
- В 718102a включён независимый Python NBT verifier. [Оффлайн CI](https://github.com/Deserag/slavic-myths/actions/runs/40910358802) подтверждает точную идентичность всех 1599 блоков модели 2.2.7, 33 blockstates, 5 LootTable markers и регистрацию modded ID. Это не GameTest.
- Отдельное утверждённое ТЗ жителей уже существует: #357, включая 12 профессий, 50/70 HP и 15-минутный персональный cooldown агрессии. Не создавать дубликат Issue и не менять параметры.
- После этих коммитов **нет** действующего posad native Structure/StructureSet, интеграции NBT в структуру и проверенного механизма retrofit. Все ограничения и QA gate сохраняются.

## 30.10.2026 — baseline утверждён владельцем
Точный structure_set сохранён в APPROVED_STRUCTURE_SET_2026-10-30.json: slavicmyths:nether_posad, weight=1, random_spread, spacing=80, separation=32, salt=22022026, linear, frequency=0.75. Это НОВЫЙ утверждённый baseline. Конфликтов salt в src/main/resources на момент проверки нет. ID совпадает с PosadWorldgen. Не добавлять произвольную exclusion_zone. Включение production structure_set разрешено только после обязательных защит и успешных проверок. Отсутствие частотных параметров больше НЕ блокер; блокеры реализации/QA сохраняются.
