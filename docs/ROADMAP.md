## 0.9.10 RC1 — public playtest stabilization (2026-10-06)

Ветка `release/0.9.10-playtest`, версия `0.9.10-rc1`. Исправлены UV/round model/blocking щита, зарегистрирован экран существующего RPG-меню Камня пути/рунной наковальни, добавлен shift-click наковальни; книга получила непрозрачную страницу и scroll, кухня — выбор рецепта/условия/результат. Курганный hotfix включён: сохранённые encounter slots, фиксированные квоты/боссы, наружные patches и единый 100-tick Порез. Геометрия кургана и существующие ID сохранены. Разбойники/Соловей не перерабатывались; полный Equipment & Art Overhaul и новая система рун не заявлены реализованными.

`clean build` и шесть CPU/data/codec проверок PASS; production JAR/resources PASS, 2685 статических ссылок, 0 новых ошибок. 16 прежних door-model parent issues отмечены отдельно. До последнего запроса hotfix: 5/5 GameTests; общая регрессия 26/27 обнаружила расчистку кладки, исправленную затем без повторного запуска игры. Клиент 0; после последнего запроса Minecraft не запускался. Игровая/визуальная приёмка — MANUAL PENDING. Отчёт: `docs/verification/playtest-0.9.10-rc1/acceptance.json`; playtest docs/JAR/checksum: `release/0.9.10-rc1/`. Изменения прежнего рабочего дерева включены, runtime worlds/logs/configs не включаются в release commit.

## 0.9.10 — анализ нового пакета (2026-10-06)

Прочитан Equipment & Art Overhaul FINAL; начато сопоставление с реальными ID, способностями и boss loot. Production-изменений 0.9.10 пока нет: требуется согласовать отсутствующие точные параметры и рецепты согласно просьбе пользователя не додумывать логику. Разбор: `docs/equipment/SPEC_REVIEW_0.9.10.md`. Существующие изменения 0.9.9 сохранены. Build/resource gates этого этапа не запускались; Minecraft-клиент — 0 запусков.
Acceptance update: thin/high superflat each passed 10 actual placements; six sequential commands from one point passed; water/native-terrain gates and all 27 companion tests passed without ERROR logs. Clean build and production JAR audit passed. Current override JAR is installed in Slavic-Myths-1.21.1-Testing (SHA256 22d6d025e106d912ed3c867704078f560ce5ff3bcd67bbc3b31340b3cd7f61cb); previous dev JAR backed up outside mods. Other mods/worlds unchanged; client not launched. Evidence: docs/verification/mob-worldgen-0.9.9/structure-priority-override.json. Full ordinary-world natural coverage remains NOT ACCEPTED.

## Текущий override: структура имеет приоритет над сушей

Реализована отдельная пошаговая ручная генерация трёх курганов и трёх лагерей: FULL-чанки до измерения высоты, проверка конечного объёма, подъём на тонком superflat, заполнение грунтом, выравнивание суши, локальный перенос от воды/защищённых построек в пределах 256 блоков. Поиск и генерация имеют status/cancel и не выполняют прежние синхронные обходы сотен удалённых starts. Проверка команд выполняется на headless сервере, Minecraft-клиент не запускается.

Подробности, ограничения отмены и результаты проверок: `docs/worldgen/STRUCTURE_PRIORITY_OVERRIDE_0.9.9.md`. Полная 0.9.9 остаётся в разработке: естественное покрытие обычного мира и компенсация не приняты. Записи ниже — история предыдущих этапов; их заявления о неизменённом production-коде относятся к моменту диагностики.

Research update 2026-10-06: real kurgan generate command reproduces the reported false 237-block terrain rejection caused by an unloaded heightmap; original-position bedrock preflight and synchronous search risks identified. User save structure settings inspected read-only. This is a diagnosis, not a fix; production code and installed JAR unchanged. Details: `docs/worldgen/COMMAND_GENERATION_DIAGNOSIS_0.9.9.md`.

Deployment update: by explicit user request, `0.9.9-dev` is installed in PolyMC `Slavic-Myths-1.21.1-Testing` for manual generation testing. Previous 0.9.7 JAR is backed up outside mods; other mods and worlds are unchanged. This is not release acceptance. Receipt: `docs/verification/mob-worldgen-0.9.9/polymc-dev-installation.json`. Minecraft was not launched.

# Slavic Myths 0.9.9 — implementation in progress

Continuation 2026-10-06: fixed raised-kurgan authored entrance stones using the persisted graded surface; soil excavation preserves underground construction. All three tiers and all three approach lanes pass; all 27 required companion GameTests pass. Clean production build `-Pmod_version=0.9.9-dev` passes; JAR resource validation passes. The latest natural-coverage measurement code includes relocated eligible cells and fails unmeasured/empty applicable networks; full natural gate still pending. Evidence: `docs/verification/mob-worldgen-0.9.9/continuation-2026-10-06.json` and `docs/worldgen/STRUCTURE_COVERAGE_AUDIT_0.9.9.md`. Stable PolyMC artifact is unchanged.

COMPILES: six spirit combat timelines, wildlife families/stat changes, existing bandit HP alignment, shared placement profiles, retained template foundation bounds, accepted-neighbor collision planning, and bounded persisted kurgan earthworks for thin superflat (explicitly authorized by the user). Build and verification details: docs/worldgen/STRUCTURE_COVERAGE_AUDIT_0.9.9.md and work/mob-worldgen-099-*.log. Static data audit: 2676 references, zero errors. Actual accepted-placement coverage/compensation, complete mob polish/audio audit, final resource/release gates and companion runtime checks remain unfinished. This is not 0.9.9 release acceptance. Installed stable PolyMC artifact remains 0.9.7; Minecraft client has not been launched.

# Slavic Myths 0.9.7 — болото и runtime hotfix

Реализованы: семь семейств болотных мест / 21 modern template; четыре дерева и природные детали только vanilla swamp; болотник 48 HP / редкий страж 112 HP; новые swamp русалки 36 HP и водяные 72 HP; пять таблиц структурной добычи + drop болотника. Старые ID/templates и остальной контент сохранены, новой biome/ore/block семьи нет.

Xaero logout/publish null-safe с очисткой внутренних данных перед optional callbacks, пользовательские waypoint сохраняются; JEI catalyst связан с зарегистрированным ritual RecipeType; linden_door использует native 1.21.1 states/models. Другие 16 старых door model parent issues остаются backlog.

DONE по build/headless: production 0.9.7, clean build; финальные core без JEI и шесть companions — по 14 GameTests без ERROR/FATAL. 42 сухих/мокрых размещения, 96 контейнеров, 720 опор, 400 форм деревьев, семь реальных accepted starts; пять features и болотник подтверждены в modified biome settings. Проверены 10 lifecycle cases production Xaero adapter с doubles и compiled JEI recipe-type contract. Проверки/gates и hash: docs/verification/swamp-0.9.7/acceptance.json; установка: installation-receipt.json в той же папке. JAR 0.9.7 установлен в Slavic-Myths-1.21.1-Testing, семь хешей проверены, прежний 0.9.6 сохранён; старые профили/миры не изменены. Headless размещение/данные/генерация/геометрия проверяются отдельно от клиента. Manual visual/gameplay QA: NOT STARTED; следующий конкретный блок — docs/MANUAL_QA_0.9.7_SWAMP.md. Клиент не запускался.

Подробнее: docs/swamp/SWAMP_0.9.7_*.md. Ниже сохранены исторические состояния прежних версий.

# Slavic Myths 0.9.6 — курганы

DONE по build/headless: графовый план v2 и совместимость v1, 14 архетипов, альтернативные лестницы, новая архитектура/палитры, 26 блоков + 26 BlockItem, 60 рецептов, семь новых таблиц наград, 35 авторских текстур 256×256 (11 блоков и 24 иконки). Существующие ID, характеристики, старый loot и глобальная частота генерации сохранены.

PASS: clean build; 150 новых планов; 1200 legacy планов и 900 составов; по 10 GameTests без JEI и с companion-модами, включая реальное размещение/контейнеры/ловушку. Data totals: 411 items, 179 blocks, 273 recipes, 260 loot tables, 154 advancements. Production: `build/libs/slavicmyths-0.9.6.jar`; evidence: `docs/verification/kurgan-0.9.6/acceptance.json`.

Client: COMPILES. Ручная визуальная/игровая QA: NOT STARTED, клиент не запускался. Следующий конкретный блок — `docs/MANUAL_QA_0.9.6_KURGAN.md`. Известный post-port backlog: 20 старых missing door model parents; подробности в resource-check.json. Проверенный JAR 0.9.6 установлен в известный тестовый PolyMC, семь JAR сверены по hash; прежний 0.9.5 сохранён. Receipt: `docs/verification/kurgan-0.9.6/installation-receipt.json`. Остальные профили/миры не изменены.

Документация алгоритма, комнат, ID/рецептов, loot и аудита иконок: `docs/kurgan/*0.9.6.md`. Далее сохранено историческое состояние 0.9.5.

# Slavic Myths 0.9.5 — навигация и зоны поиска

Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Слой навигации реализован по последнему
пакету V2. Предыдущий перенос 0.9.4 завершён; его отчёты сохранены как историческая база.

Core/server: DONE по headless проверкам. Client screen/HUD/Xaero: COMPILES, ручная QA не выполнена.
Build и финальные loader/data checks PASS. Minecraft-клиент не запускался.
Production: `build/libs/slavicmyths-0.9.5.jar`. Точный hash: `packaging/test-pack-lock.json`.

[Навигация, алгоритм и фактические ограничения](NAVIGATION_0.9.5.md),
[ручные проверки](MANUAL_QA_0.9.5_NAVIGATION.md), [evidence](verification/navigation-0.9.5/acceptance.json).

Следующий блок: ручной запуск тестового профиля и 28 navigation QA сценариев,
затем только воспроизведённые ошибки. Новые боссы/loot/worldgen не входят в 0.9.5.
Известный fallback: Xaero approximate center+radius, без polygon overlay. Контракт без
известного реального encounter ждёт появления цели; новых encounters навигация не генерирует.

## Исторические записи до 0.9.5

# Slavic Myths 0.9.4 — текущий перенос

Launcher fix: исправлены несовпадающие official PolyMC/Maven checksums NeoForge installer/universal локальным patch нового профиля. Реальный ForgeWrapper detector PASS; ручной повторный запуск ещё не проверен.

Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Все production sources компилируются; clean build, headless logic/resource/loader gates A–F PASS. Отдельный PolyMC Slavic-Myths-1.21.1-Testing установлен с семью проверенными JAR. Старый 1.16.5 профиль/миры сохранены. Клиент не запускался; игровая QA ещё не выполнена.

Фактическое состояние и журналы: [PORT_STATUS_0.9.4.md](port/PORT_STATUS_0.9.4.md). Исторические записи ниже описывают предыдущие этапы и версии, а не подтверждение работоспособности текущего порта.

Next concrete block: manual launch of the new profile and fresh-world QA (client/Curios/JEI/AI/structures/persistence), then fix reproduced port regressions. No 0.9.5 or gameplay redesign scope.

# Актуальная ветка 0.9.x

- 0.9.0: Овинник/Волколак, первые трофеи и hunting система реализованы; headless QA, игровой QA TODO.
- 0.9.1: Огненный змей/Подвей, восемь наград, расширение существующей охоты реализованы; headless QA PASS, игровой QA TODO.
- 0.9.2: Лихо Одноглазое / Тугарин Змей — код и headless проверки реализованы; игровой QA/визуальная сверка в Minecraft TODO.
- 0.9.3: исправлен superflat/placement, добавлено яйцо Яги и production placement regression; Яга, избушка, прогресс, GUI, предметы/обмен/котёл реализованы; headless проверки PASS, игровой QA TODO (MANUAL_QA_0.9.3.md).
- 0.9.4: перенос существующего контента на NeoForge 1.21.1 / Java 21 — в работе; см. port/PORT_STATUS_0.9.4.md. Дизайн/баланс не входят в scope.
- 0.9.5+: content freeze, аудит/баги/визуал/баланс/performance/polish.
- 1.0.0: стабильный релиз.

Живая/мёртвая вода перенесены на поздний этап и в 0.9.0 не реализованы.
Следующее действие: завершить gates порта 0.9.4; дальнейшие переработки выполняются только на 1.21.1.
Исторические планы ниже сохранены для контекста.

---

# Актуальная курганная арка

- 0.8.4: предметы, колода, дымовая обработка и внешние прототипы реализованы;
  сборка, ресурсы и установка PASS, ручной QA TODO.
- 0.8.5: лабиринты, кладка, декор, нарушение покоя и проклятие реализованы; headless QA PASS, игровой QA TODO.
- 0.8.6: шесть противников, два мини-босса и главный босс реализованы; headless QA PASS, игровой QA TODO.
- 0.8.7: прежний план главного босса выполнен в 0.8.6 по новому ТЗ; дополнительных работ без нового задания нет.

# Актуальный этап

- 0.8.2: реализация encounter, ресурсов, наград и интеграции завершена.
  Финальная сборка/проверки/установка PASS; ручной игровой QA — TODO.
- Следующее действие — ручной QA 0.8.2; новые арки и глобальный polish не начинались.

Ниже сохранена история планов.

# План итераций

- **0.8.1 — Большой разбойничий стан:** реализация завершена, build/resources PASS;
  следующий шаг — пользовательский QA по CHECKLIST_0.8.1.md. Игровых запусков 0.
- **0.8.2 — Соловей-разбойник:** не начата; требуется отдельное задание.
  В 0.8.1 есть только архитектурно подготовленный двор и lore-перо.

Ниже сохранены предыдущие планы как история.

- **0.8.0.1 — Woodlands & Timber:** реализация завершена; clean build/resources PASS.
  Четыре породы, древесные наборы, ягоды, свисающая листва, biome integration.
  Ручной игровой QA — следующий шаг; Minecraft запусков 0.
- **0.8.1 — Большой разбойничий стан:** только после отдельного задания.
  Новая древесная палитра готова для использования, структуры пока не перерабатывались.

Далее — история ранее выполненных итераций.

- **0.8.0:** реализация Combat & Bandits завершена, build/resources PASS.
  Следующий шаг — ручной игровой QA по CHECKLIST_0.8.0.md; запусков 0.
- **0.8.1:** не начата; требуется отдельное задание.

Ниже — исторические планы, а не дополнительные обязательства текущей итерации.

- **0.7.3 — Топи и затопленные руины:** реализация завершена: шесть основных
  категорий и малые POI, 23 NBT, команды swamp/structure/next, terrain checks,
  loot, книга и exploration-достижения. Clean build и resource/NBT checks PASS.
  Игровых запусков 0; ручной тест пользователя — следующий шаг.
- **0.7.4:** только отдельное задание после ручного QA 0.7.3; баланс/доводка/Creative.
  Контент отсутствующей в этой копии 0.7.1 не создаётся в рамках 0.7.3.

Ниже сохранена история планов прошлых итераций; актуальный срез — CONTINUE_0.7.3.md.

- **0.6.5 — wildlife/cooking:** реализован завершённый первый срез животных,
  их лута и деревенской еды. Ягодные кусты, cooking workstations и мифическое
  оборудование остаются следующими самостоятельными этапами без заглушек.

- **0.6.0:** реализован RPG-фундамент: 4 пути / 24 навыка, 8 рун, перековка,
  серверные меню, сохранение NBT. Ожидается ручная проверка в игре.

## 0.6.5 — Mythic Equipment (только план)

- Дополнительные слоты: 1 амулет, 2 кольца, 1 пояс, 1 оберег.
- Создаваемые аксессуары и совместимые с рунами украшения.
- Уникальные несоздаваемые реликвии, добыча существ/боссов, Сапоги-скороходы.

- **0.5.1 — entity hotfix:** модели, текстуры, анимации и звуки четырёх духов
  обновлены; build PASS. Остаётся ручная проверка в игре, запуск пользователем.

- **0.4.6 — art polish:** улучшены ключевые предметы, блоки и броня; игровой
  контент и будущие персонажи не расширялись. Ожидается ручная визуальная проверка.

- **0.4.5 — визуальная переработка и предметы:** реализованы оригинальные спрайты,
  янтарь, посохи, оружие, щиты, обереги, три обряда, книга и достижения. Ручная
  проверка внешнего вида/баланса в PolyMC ожидается. Новых персонажей нет.
- **Следующий этап персонажей:** сначала отдельный дизайн каждого существа
  (источник, образ, модель, звук, поведение, взаимодействие и роль в прогрессии).
  Прежние идеи 0.5.0 остаются предварительными, не назначены к реализации.

- **0.4.1 — расширение материального контента:** первый проход реализован: перунитовое
  снаряжение, серебро, травы и ремесленные ингредиенты, еда и декор. В работе остаются
  ступка и некоторые напитки ещё оставались планами этого прохода; позже серебряный
  бонус, дополнительные обереги, книга и достижения вошли в 0.4.5.

- **0.1.0 — фундамент:** предметы, ресурсы, документация.
- **0.2.0 — природные ресурсы:** растения, береста, перунит, рецепты и достижения.
- **0.3.0 — первые духи:** Домовой и Леший, модели, звуки, spawn и loot; optional JEI.
- **0.4.0 — духи, капища и первые ритуалы:** отношения и дом Домового, способности Лешего,
  капище, идол, жертвенник, первый ритуал и знак, находки, Книга сказаний и знания.
  Реализовано; границы проверки — PROJECT_STATUS и VERIFICATION_0.4.0.

## Перед расширением

Пройти MANUAL_TEST_0.4.0: естественное капище и баланс, старый survival-мир,
два игрока/dedicated server, боевые способности, GUI/JEI и сохранение.
Измерить нагрузку в длительной игре, прежде чем усложнять AI.

## Предварительные будущие этапы — НЕ реализованы

- **0.5.0 — Воды и болота:** Водяной, Русалка, водные/болотные существа,
  растения, ресурсы, водные места, ритуалы, книга и достижения.
- **0.6.0 — Ведьмина тропа:** Баба-яга, избушка, диалоги, задания, обмен и загадки.
- **0.7.0 — Путь героя:** классы Дружинник/Волхв/Ведун, способности, развитие и бой.
  Это игровые роли, а не исторические «RPG-классы».
- **Позже:** курганы, Змей, крупные структуры, Навь, Перун, Велес, другие боги,
  боссы, измерения и end-game progression.

Не реализовывать весь roadmap одним проходом.

0.4.7: weapon proportions, shields, thunder spear, mace variants and berdysh implemented; build/install verified. Manual in-game weapon/shield verification pending; optional silver berdysh and sharpening stone deferred.

0.5.0 content implemented and packaged: Kikimora, Poludnitsa, Polevik, Bannik, Igosha, Ovinnik, bathhouse and barn. Manual gameplay/balance validation pending; advancements unspecified because the supplied task ends at section 121.
# Итог финализации 0.6.5

0.7.2 «Хозяева глубин»: основной encounter и награды реализованы, техническая сборка
проверена; отсутствует интеграция с 0.7.1, поскольку её контента нет в этой копии проекта.
Следующие отдельные этапы: 0.7.3 — крупные постройки/исследование; 0.7.4 — исправления,
категории Creative и общая доводка арки после ручных тестов. Оба этапа сейчас не начаты.

Следующий реализованный этап: **0.7.0 «Тихие воды»** — рыбалка, сети, речные обитатели,
водная кухня, растения, малые POI, Водяной, Русалка и постепенная песня с counterplay.
Техническая сборка проверена; далее ручной тест пользователя и точечные исправления.
0.7.1 и крупные водно-болотные структуры не реализуются в текущем проходе.

Artifact content drop: пять основных артефактов реализованы, ожидают ручной проверки.
Дорогой рецепт Оберега сохранён; survival-интеграция четырёх новых артефактов отложена.
Необязательные резной сундук, столб и утварь не добавлены.

Реализованы ягодные кусты, кухня, ограничение шапки в существующей системе аксессуаров,
Метла, Ступа, Пест, полёт, груз и «Попутный ветер». Получение транспорта через сказочный
survival-контент отложено согласно заданию; сейчас Creative/команды.
Следующий шаг — ручная проверка пользователем и точечные исправления по её результатам.
0.7.0 «Воды и болота» в этом проходе не начинается.

## 0.9.4 current next block

Finish custom item/block APIs and concrete EntityBlock/ticker/menu transaction integration, then initialize real registries for codecs/inventory/Curios/JEI verification. Major entity/client/network/worldgen work follows. No 0.9.5 and no gameplay/worldgen/visual backlog fixes in this stage.
