# Достижения v3 — реализация для офлайн-приёмки

Статус: IMPLEMENTED_FOR_GAMEPLAY_ACCEPTANCE; Minecraft/gameplay/GUI NOT_RUN.
Ни клиент, ни dedicated/integrated server, ни GameTest, ни тестовые миры не запускались: **0 запусков**.

Основа — PR #40, `82a0cccb569307b2992e4c82bf30667a7862fade`; новая ветка `codex/advancements-v3`, отдельный checkout `D:/slavic-myths-advancements-v3`.
Проверенный main `fb2994e958911169317523e1066b8a1ca7a155af` — предок #40. При подготовке PR #25/#31/#40 открыты, не merged.
Версия остаётся 1.9.7.5; Minecraft 1.21.1, NeoForge 21.1.255, Java 21.0.12, official mappings.
Пользовательский checkout `D:/slavic-myths`, его незакоммиченные изменения, main, PolyMC и миры сохранены.

## Результат и отличие от #40

75 видимых карточек: корень + 7 мирных + 24 исследовательских + 25 боевых + 18 сюжетных.
Вместо шести пустых заголовков и 55 карточек #40 — четыре реальных стартовых события и связанные цепочки.
Максимальная глубина 6; 74 реальных parent-child связи. Карточки task/goal/challenge, промежуточные поручения без toast/chat.
[Дерево и таблица всех условий](TREE.md), [PNG](TREE.png), [SVG](TREE.svg), [TREE.json](TREE.json).
Машинная ведомость всех 75: [CSV](ACHIEVEMENTS.csv), [JSON](ACHIEVEMENTS.json).
Схема сгенерирована из фактического дерева, визуально просмотрена; это не скриншот Minecraft.

## Сохранено из #40 и #25

Все 217 definitions #40 (206 исторических + 11 добавленных), 77 recipe unlocks, 234 записи Книги.
Criteria, requirements и rewards существующих holders неизменны; служебные рецепты сохранены в Git-канонических байтах (Windows CRLF не считается изменением).
Сохранены native accepted-victory callbacks и contributor/participant policies, включая отмену смерти,
creative/admin/debug exclusions, очищение Горыныча, сдачу Калина и победы событий.
Ни AI боссов, ни урон/баланс/лут, ни награды Яги/Волка не менялись.
Четыре корректировки #25 уже включены в #40 commit `04af0674b0b2e711ceca2332c5f94f7ae3b54ea8`;
повторный cherry-pick не выполнялся. Исходные SOURCE/save-contract/DESIGN_CONTRACT/resource-contract не переписаны.
HistoricalMilestonePolicy остаётся неактивным: нет массового импорта, автоматической выдачи родителей или reward replay.
[Полная матрица совместимости](COMPATIBILITY.csv) содержит все прежние ID, условия, требования, награды, родителей и политику successor.

Новая семантика требует отдельных ID:

- `first_brew` остаётся legacy: он выдавался за готовый напиток из чужой бочки/предмета. `self_brewed_beer` — только настоящий starter завершённого брожения пива.
- `linen_craft` остаётся legacy inventory fact. `linen_woven` — реальный ItemCraftedEvent полотна.
- `follow_the_thread` остаётся legacy: он также выдавался за использование клубка. `yaga_thread_earned` — только успешное второе основное поручение.
- `garden_harvest` остаётся legacy inventory fact. `own_harvest` — принятая посадка модовой культуры и фактическое удаление её зрелой версии этим игроком.
- `bad_people` остаётся legacy discovery лагеря; `meet_bandits` — живой разбойник в LOS в прежнем bounded 12-block sampler.
- `meet_bayun` остаётся legacy proximity; `talk_to_bayun` — принятый разговор.

Новые события не выводятся из приблизительно похожего старого прогресса. Уже завершённые до обновления события с новой семантикой не импортируются автоматически.

## Источники событий

- Новая профессия: RETURN существующего `Villager.mobInteract`, подтверждённое открытие торговли и точный trading player. Нет перехвата результата.
- Посадка: существующий redirect ItemStack.useOn после native placement commit/rollback; сбор — успешный removeBlock, без выдачи за отменённый break. Сохраняются до128 персональных посадок.
- Пиво: сохранённые UUID starter/award flag конкретной партии `Fermentation`; callback только при смене native quality на ready в действующем tick бочки.
  Чан готовит сусло; настоящее брожение происходит в StorageTile-бочке. Переливание и получение чужой кружки новый ID не выдают.
  Офлайн starter получает сохранённый точный pending fact в TaleData, выдаваемый при следующем входе; это не historical import.
- Чудь: `chud_city`/`chud_abandoned` зарегистрированные структуры; залы и лабиринт — сохранённые ChudPlan/AbandonedPlan реального загруженного StructureStart.
  Обратное преобразование всех4 ориентаций; major halls учитываются отдельно для одного city/seed/dimension, не смешиваются между городами.
  Лабиринт проверяет реальные сегменты mazeEdges/mazeNodes с шириной прохода и Y, а не bbox всего комплекса/похожие блоки.
  Используется прежний moved-position sampler раз/сек, максимум32 загруженных starts; нет locate, новых tick subscribers или chunk requests.
  Tag `advancement_discoveries` включает зарегистрированные структуры и расширяется будущим datapack без перестройки корня.
- Яга: существующие `completeMain` 0/1/2 → stage1/2/3 и native cauldron_service. Условия Лихо/Тугарина сохранены.
- Баюн: canTalk/open, реально новая legend stage0→1, существующий Trust≥85.
  «Песня» выдаётся при серверном завершении CHARMED/POUNCING с живым survival player в том же мире; смерть, logout, смена мира, простая близость и warning не засчитываются.
  Переходы, сроки и поведение NPC не изменены; только callback в прежнем abort.
- Мор: реальные Invitation и успешный claim шестой основной главы (index5 / FinalChapter); не9 отдельных карточек.
- Волк: native WolfProgress.complete; committed claim трёх первых IDS из существующих четырёх определений.
  Финальный `curse_resolved` сохранён из #40: authentic owner/encounter/nonce skull hand-over → RESOLVED/ready финального поручения.
  Это завершение предметной истории, а не смерть существа; выдача последующих сюжетных наград остаётся GreyWolfStory.

## Независимое получение и Книга

Knowledge.award не проверяет parent. Vanilla source review: завершённый child получает SHOW;
видимость его предков не означает выдачу их criteria. Книга передаёт Done независимо от parentDone,
всегда включает все75 non-hidden holders и рисует Done до Available; поиск сохраняет контекст предков.
В навигацию добавлены четыре имени ветвей вместо поиска только group_*.
[Установленные API и hashes](INSTALLED_API.json); фактический GUI/дельты/звуки/MP — NOT_RUN.

## Офлайн-проверки и артефакт

- Gradle clean build task graph заранее проверен: нет runClient/runServer/runGameTest или loader-dependent verify tasks.
- Java21 clean builds: [первая](BUILD.log), [промежуточная](BUILD_FINAL.log), [итоговая](BUILD_RELEASE.log). Предупреждения deprecated API исходной базы сохранены.
- 40326 pure JVM checks геометрии/раскладки/границ брожения и314 regression checks inactive historical policy: [лог](PURE_JAVA.log).
- Старый auditor адаптирован под75 карточек с сохранением прежних save/helper/recipe/tale contracts; mutation tests проверяют отклонение повреждённых входов.
- [Новые проверки и JAR](CHECKS.json), [лог](CONTRACTS_JAR.log), [прежние контракты](LEGACY_CONTRACTS.log), [mutation tests](MUTATIONS.log).
- Replay v3 и обоих исторических advancement entrypoints должен давать побайтно те же ресурсы. Полные resource generators не запускались, чтобы не регенерировать несвязанные art/механики.
- Тестовые исходники остались в tools; production JAR сверяется с main classes/resources и не содержит GameTest/verify/smoke.

Итог: clean build PASS;19/19 mutation tests PASS;40326+314 pure JVM checks PASS;3954 JSON/1325 PNG и точная сверка main classes/resources с JAR PASS.

Production JAR: `build/libs/slavicmyths-1.9.7.5.jar`, 19730967 bytes. SHA256 `8808ed388fc9f524ff43a6555720afaa5aa8b57c452762bffd7e75a5100e2e10`. Точная квитанция: CHECKS.json.
Это офлайн-подготовка к игровой приёмке, не релизная приёмка.

## Риски и будущие игровые проверки

1. Runtime применение нового villager mixin и существующих placement/removal hooks, Survival craft/отмена действий/shift-click: NOT_RUN.
2. Child completed / parent incomplete в vanilla GUI и Книге (initial/full/delta, scroll/search), RU/EN и toast/chat: NOT_RUN.
3. MP/offline starter, настоящая перезагрузка сохранений, история Волка со связанным черепом, три основных поручения Яги, освобождение от чар Баюна и финал Мора: NOT_RUN.
4. Залы/лабиринт в4 ориентациях и независимых комплексах, старые chunks с незагруженным start, природная генерация: NOT_RUN. Start должен быть уже загружен; принудительная загрузка исключена.
5. Посадки ограничены128 записями, незавершённые города —8 записями; переполнение вытесняет одну старую запись, не блокирует дальнейшее получение.
   Посадки — персональное наблюдение посадки/сбора, не новая система защиты владения полем: замены чужими игроками/механизмами и claims требуют отдельной MP проверки.
6. FPS/MSPT, звук, визуальная и длительная игровая приёмка не измерялись.

## Публикация

По явному выбору владельца «Оставить изменения локально» изменения оставлены в отдельной ветке без commit/push/PR. Публикация не выполняется. Готовое описание для возможной будущей публикации: [PR_BODY.md](PR_BODY.md).
Он должен заменять отклонённый layout #40 и явно указывать исходный HEAD; #40 не закрывать/merge/rewrite.
