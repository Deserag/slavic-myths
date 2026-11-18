# 2.0.4.6 — ОБЯЗАТЕЛЬНЫЙ ФИНАЛЬНЫЙ ИНТЕГРАЦИОННЫЙ КОНТРАКТ ЧЕРТОГОВ ЗАБЛУДШИХ

**ПОРУЧЕНИЕ ВЛАДЕЛЬЦА 27.10.2026:** последний микропатч третьего биома должен **объединить абсолютно все готовые части**, продемонстрировать реальное естественное появление, завершённый бой Постеня, трофеи, руну, три головы, навигацию и регрессии двух предыдущих биомов. Базовая [#294](https://github.com/Deserag/slavic-myths/issues/294), строгий контракт [STRICT_ACCEPTANCE_2.0.4.md](STRICT_ACCEPTANCE_2.0.4.md), прежний worldgen [WORLDGEN_GATES_2.0.4.6.md](WORLDGEN_GATES_2.0.4.6.md). Новая матрица **приоритетнее** прежних исторических слов «босс не выбран».

## 0. ВХОДНЫЕ ВОРОТА: сначала inventory, затем worldgen

| Gate | Исполнительская задача | Проверяемый бинарный результат |
|---|---|---|
| I01 | [#289](https://github.com/Deserag/slavic-myths/issues/289), подготовка | original blocks/scenes/mobs PNG SHA + проверенные названия/рисунки и физические ID |
| I02 | [#290](https://github.com/Deserag/slavic-myths/issues/290), блоки | 4 семейства (Камень №3, треснувший, голубой светожильный, тонкий налёт), все textures/models/mining/loot/creative, строительные формы |
| I03 | [#291](https://github.com/Deserag/slavic-myths/issues/291), геометрия | настоящие модули коридор, мост, огромный зал, грибной зал, светящийся грот, развилка (S1–S6); физическая связность/границы чанков |
| I04 | [#292](https://github.com/Deserag/slavic-myths/issues/292), пять мобов | **5 EntityTypes**, Шептун 4 окраски 60 HP, Оскольник 90, Гулокрыл 30 tame, Эховик 60 neutral, Грибошмыг 60 peaceful; 3D models+AI+animations+sounds, HP/NBT |
| I05 | [#301](https://github.com/Deserag/slavic-myths/issues/301), loot и навигация | Нить шёпота, светожильный осколок, склянка эха, споровый мешочек, питомец-Гулокрыл, Путевая метка, Камертон; мирные способы добычи и рецепты |
| I06 | [#293](https://github.com/Deserag/slavic-myths/issues/293), Постень | **650 HP, 4 маски, wall travel, split 4×65 при 260 HP, один bossbar, один финальный death/loot, anti-dupe** |
| I07 | [#303](https://github.com/Deserag/slavic-myths/issues/303), руна | 80 ticks invis/ручной выход, **3 copies на 120 ticks**, 1-hit dispel, реальные copy attacks/aggro, cooldown |
| I08 | [#302](https://github.com/Deserag/slavic-myths/issues/302), heads 3 новых биомных боссов | Сторож/Жареница/Постень **по 1 голове за победу**, 3 уникальные 3D головы, анимации, голова Постеня вращает четыре маски при player near6/stop8 |
| I09 | текущее Java/NeoForge/ветка/старый мод | Один согласованный writer, Java 21, нет несохранённых конфликтующих изменений и перезаписи чужого active work |
| I10 | оригиналы моделей/наград/Постеня/трёх heads | Действительные файловые SHA, доступные Codex, source/render сравнены. Без исходника только `BLOCKED_REFERENCE`, ART не DONE |

**Если I01–I10 не прошли — не заявлять «биом сделан».** #294 может выявлять проблемы зависимостей и создавать минимальные integration patches только после проверки ownership/согласования; **не переписывать вместо предыдущих микрозадач существ без референсов и не заменять босс-заглушкой**. #302 затрагивает два предыдущих биома, поэтому для окончательного эпизода 2.0.4 нужно проверить актуальные головы в общем JAR, даже если #302 выполнена раньше.

## 1. КОНКРЕТНЫЙ НАТУРАЛЬНЫЙ WORLDGEN

- Только в `minecraft:the_nether` с сохранением генерации **всех пяти ванильных Nether биомов**, vanilla fortress/bastion/ruined portal, portals, lava sea level, прежних Навьей рощи и Чёрной гари. Старые чанки/старые ID не переписывать/не переименовывать.
- **Большие природные многоярусные зоны Чертогов, а НЕ шахматные микрозоны**: открытые скальные колонны/мосты/ущелья + внутренние большие каменные/грибные/светящиеся залы + развилки/многоярусные переходы; S1–S6 должны встречаться в реальном существующем worldgen, не в одиночном /place debug.
- **Связность пути**: игрок обычной survival высоты/прыжка и без OP должен пройти к залам/арене и вернуться; недоступность из-за несуществующего моста или единственного лавового тупика = FAIL. Маршрутные данные минимальны и доступны инструменту Камертон без дальних chunk loads.
- **Обязательная арена Постеня**: заранее спроектировать зональную deterministic seed-based структуру так, чтобы **каждая полная естественная связная область** Чертогов получала собственный **доступный** boss site. Убрать независимый `chance(no boss)`. Формальный `fullRegion` критерий зафиксировать до QA на реальных нескольких seed, не менять после провала. **Предлагаемая проверяемая модель**: полная область — как минимум 64 смежных chunk-column биома, содержащих достаточно свободного пространства для арены с физически проверенным footprint ≥32×32 blocks, минимум 2 доступных маршрута к ней; **точные границы предварительные инженерные**, не прикрывать ими потенциально полноразмерные пропущенные регионы. При невозможности обеспечить гарантию таким простым критерием изменить worldgen region planning детерминированно, а не скрыть absence через порог.
- **Арена** в крупном зале: естественные стены/колонны с не менее двух проверенных anchor-позиций входа/выхода Постеня, безопасные дорожки, обходы от каждого telegraph-attack, нет лавы на обязательной траектории, резерв физического места для **4×65 HP копий одновременно**. 650 HP phase I и phase II должны оставаться playable. Прозрачный visibility bossbar и читаемые маски в shader-free Minecraft.
- **Chunk consistency**: footprint/биомный ID/арена/пути по seed воспроизводятся независимо от порядка генерации соседних чанков (склейка по 2/4-чанковым границам), без unloaded heightmap => minHeight ошибочной подстановки, без чтения сотен удалённых FULL-чанков в worldgen/spawn/tick, no tickets leaks, no synchronous locate freeze (#274/#275). Состояние босса `defeated` persisted, при relog не возрождается.
- **Native locate**: `/locate biome slavicmyths:<REAL_ID>` и `/execute in minecraft:the_nether run locate biome slavicmyths:<REAL_ID>`, arena через `/locate structure` только если фактически зарегистрирован structure ID; иначе bounded подсказка/метка для обычного survival-поиска. Никаких выдуманных координат/сразу телепорта в лаву.
- **Natural spawns** по подтипам: Шептун S1/S6, Оскольник S5/S3, дикий Гулокрыл S2/S3, Эховик S3, Грибошмыг S4, vanilla искажённые/багровые грибы только разумными островками в S4. Проверить biome/spawn tags/substrate/light/caps/height и отсутствие в соседних старых биомах. **Пять видов**, 4 цвета Шептуна считаются одним mob, но все 4 варианта реально естественно встречаются.
- **Камертон** должен распознавать реальные nearby room nodes/выход, **Путевая метка** ставится на стене/полу и светится, без блокирования прохода. Не считать навигацию реализованной, если только слышится звук и нет реального поиска.

## 2. МИНИМАЛЬНЫЕ АВТОМАТИЧЕСКИЕ ИГРОВЫЕ ТЕСТЫ

**Сид-сетка:** пять воспроизводимых long seed `42041,42042,42043,42044,42045` (либо такая же задокументированная серия) × **не менее четырёх** пространственно разнесённых диагностических окон **на каждый seed** = **20+** независимых окон. Размер окон и координаты фиксируются перед запуском, не уменьшать после неудачного теста. Плюс **1 копия старого мира** (только копия) и отдельный 2-player test при наличии среды. Ни одна сделанная вручную структура или /summon не подменяет natural spawn. Кодекс сам тестирует там, где разрешено AGENTS.md.

| QA | Проверяемое поведение | PASS evidence / FAIL |
|---|---|---|
| Q01 | все 5 vanilla + Навья роща + Чёрная гарь + Чертоги реально существуют | biome registry, фактическая карта 20 окон, coverage %, целостность регионов |
| Q02 | 4 семейства блоков, светожилы, налёт, building forms, vanilla flora | реальные world coords, mining/drop/model/UV/light, не screenshot artwork |
| Q03 | 6 пространств S1–S6 и connectivity | граф узлов, координаты, реальные screenshots передвижения туда/назад без OP |
| Q04 | 5 natural mob types и 4 color sheptun | spawn counts/biome/непоявление в неверных залах, idle/aggro/natural tests; spawn egg недостаточно |
| Q05 | Гулокрыл tame/Heal/ownerPersist, Эховик neutral, Грибошмыг peaceful | interaction matrices, 2 players, server restart/relog |
| Q06 | L01–L07 наград/утилит: нить, осколок, склянка, мешок, питомец, waymark, tuning fork | loot/interaction/recipe/anti-dupe/full inventory, genuine mapping room nodes |
| Q07 | Босс Постень: max 650, 4 маски, phase2 при 260 HP, 4×65, 4 разных атаки, wall travel | реальные боевые логи/видео, `sharedHP=sum(cloneHP)`, отсутствие дюпа |
| Q08 | Руна: 4 сек invis, ручной выход, 3×6 сек combat decoys, 1 hit vanish, player/clone targeting | 12 R gates из rune contract с log/server proof |
| Q09 | Новые головы Сторожа/Жареницы/Постеня ×1, Posten head плавно вращается при подходе | head item/world model/ракурсы/эффекты/дроп, 6→8 block test, регрессия #140 |
| Q10 | гарантированный Posten site в **каждой полноценной** области, spawn/defeat state один, arena safe | region IDs, site UUID, X/Y/Z, survival route, before-after relog |
| Q11 | старые сохранения/ванильные структуры, old registry ID, порталы, строения | snapshot/MD5 chunks before-after, dedicated access и где портал приводит |
| Q12 | перфоманс: chunk-gen, locate, world create/save/unload, p95/p99 MSPT и память | фактическая baseline-серия, raw профили и отсутствие зависаний по #274/#275 |
| Q13 | артефакты/качество моделей: approved poster vs actual client no shader | SOURCE→model/texture/animations/audio/video SHA, VIEW full 360°, 4 маски boss |
| Q14 | multiplayer 2 clients и dedicated server-safe/owner state/anti-dupe | реальная матрица авторизации boss/decoy/loot/head placement/reload |
| Q15 | Java 21, NeoForge 1.21.1 build, resources, localization, JAR and creative | real `clean build`, tests, jar content, missing texture/sound=0, signed HEAD |
| Q16 | one writer safe branch, no PolyMC/user saves touched, releases other biomes safe | git status before/after, source diff, no forced updates |

**ГЕЙТ ПРИЁМКИ:** Q01–Q16 не могут получить PASS без соответствующей сырой информации. Отдельная таблица `I_ID/Q_ID | SOURCE_SHA | SEED/WINDOW_XZ | ACTUAL_REGISTRY/PATH | TEST/VIDEO/LOG | PASS/FAIL/BLOCKED/NOT_RUN`, версия Java/NeoForge и commit/JAR_SHA. Положить отчёт `docs/implementation/lost-halls-2.0.4.6/REPORT.md` и каталог `evidence/` с реальными Minecraft screenshots. **Нет тестовой среды/запрет в AGENTS.md => `NOT_RUN_ENV_BLOCK`, а не тихий PASS**. Никто кроме владельца не ставит «Готово» и не закрывает финальную Issue.

## 3. ПОРЯДОК ДЕЙСТВИЙ ИСПОЛНИТЕЛЯ

1. Git status/branch/HEAD, сверить реальные файлы/референсы, законченные зависимости #289–#293, #301–#303 и отчёты. **Не обещать DONE, если хотя бы один реальный блокер.**
2. Исправить только missing integration worldgen/spawn/arena/find/navigation в пределах этой Issue, не заменить чужую художественную работу упрощенными placeholders.
3. Собрать Java21 clean build/test/JAR, проверить ресурсы и dedicated server classloading.
4. Прогнать минимум 5 seeds × 4 windows, сравнить coverage, проверить end-to-end пути и boss fight, loot, rune и головы там, где runtime разрешён.
5. Приложить полные evidence + QA matrix, указать все BLOCKED/NOT_RUN со ссылками, оставить OPEN до принятия владельцем.

**Отдельная оговорка:** Гарантия boss site не означает respawn убитого босса при каждом заходе и не означает «один босс на каждый 1 biome block». Она означает реальную арену/встречу в каждом осмысленном полном участке Чертогов при первичном исследовании.
