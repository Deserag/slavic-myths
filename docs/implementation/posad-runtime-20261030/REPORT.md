# Пекельный посад — runtime checkpoint 30.10.2026

Частичная реализация #358/#346. НЕ готовый WorldGen города. Исходный HEAD 5d6fc389, только hell, отдельный worktree; исходное незавершённое слияние master не тронуто.

## Код
- PosadWorldgen: StructureType/PieceType nether_posad в существующих deferred registries.
- PosadStructure: начальный noise-only land planner, проверка biome/candidate separation/reference bounds. Пока НЕ подключён data-driven Structure/StructureSet: sampled terrain precheck недостаточен для гарантии целого города, lava/bridge ещё отсутствуют. Нельзя включать этот planner в production placement до полного preflight.
- PosadPiece: сохранение anchor/rotation/schema, chunk-clipped физическая укладка. Стоимость обхода ограничена footprint текущего чанка. Нет loot и retrofit.
- PosadShell: 113x113 платформа, стены 5 блоков, 4 угловые/2 привратные башни, открытый проезд 7x6, дороги/площадь, лестницы и проходы к патрулю. Это начальная техническая реализация; точное визуальное совпадение R1 и непрерывная проходимость ещё не приняты.
- SlavicMyths: инициализация регистраций. build.gradle: опциональный изолированный GameTest fixture; тесты не попадают в production JAR.

## Матрица
| REQUIREMENT | EVIDENCE | RESULT |
|---|---|---|
| Java21 clean build | gates.txt; Temurin 21, NeoForge 21.1.255 | PASS |
| Ресурсы/production JAR | resources.txt: 8741 resources, 4984 JSON, 1599 PNG | PASS |
| Проезд/слоты/патруль — ограниченные assertions | 3/3 NeoForge GameTest | PASS |
| Физическая платформа и проезд через чанки, 4 поворота | physicalChunkPlacement: реальные блоки ServerLevel, отдельный тестовый Overworld | PASS |
| Естественная генерация Nether на 5 seed | не запускалась; отсутствует StructureSet | NOT_RUN |
| Мост, опоры, lava terrain, атомарность всего города | не реализованы | NOT_RUN |
| Ручные команды/locate/retrofit/здания/MP/reload | не реализованы/не проверены | NOT_RUN |
| GUI и R1 сравнение | изолированный GUI не создан, desktop владельца не использовался | BLOCKED |
| Утверждённая встречаемость | spacing/separation/salt/frequency не найдены в #346/#358 и концептных MD; запрос владельцу | BLOCKED |

Три реальных запуска GameTest server в disposable run-posad-isolated: 2/2, 3/3, 3/3. Финальный прогон 3.419 секунды для трёх тестов; это НЕ MSPT и НЕ скорость native worldgen. Проверка физического размещения принудительно готовит тестовые чанки; production piece не вызывает загрузку чанков. Результаты не доказывают безопасность натуральной/ручной генерации.

JAR SHA256: 0b631ddfd1f83814007069c6b67fb93b1ebe11217d54a89911597d21c8f34e2d. PolyMC не изменён.

## Как проверить / риски / следующий этап
На приёмку владельцу пока не передавать: survival-путь отсутствует. Воспроизводимая проверка исполнителя: JAVA_HOME=JDK21; gradlew.bat clean build -PposadCheck runPosadChecks; Python с Pillow tools/verify_resources.py. Только отдельный тестовый мир. Будущий smoke: новый Nether survival без OP -> город -> вход и патруль -> негативный biome/terrain -> save/reload -> копия старого мира -> два клиента; OP locate/generate отдельно. Все эти сценарии пока NOT_RUN.

Риск: начальный planner и piece пока не обеспечивают общегородскую атомарность и опоры; намеренно нет data placement activation. Откат checkpoint — git revert собственного коммита, registry сохранённых пользовательских миров не затрагивались этим тестом. Следующее: полноценный terrain/support/shore plan и защита всей территории, затем активация native data и штатные команды. Issues не закрывать.

## Продолжение 30.10.2026 — terrain/shore checkpoint

Частотные параметры получены и утверждены владельцем: spacing80/separation32/salt22022026/linear/frequency0.75/weight1. JSON baseline: docs/concepts/nether-settlement-2.2/APPROVED_STRUCTURE_SET_2026-10-30.json. ID совпадает с production registration nether_posad; конфликтов salt в runtime JSON нет. Прежний BLOCKED по отсутствию параметров снят. Production StructureSet ещё не активирован: нет полной защиты/проверки физических native стартов.

Изменено:
- PosadTerrain: плотный preflight всех 12769 колонн платформы, 225 несущих точек с шагом8, опоры до48 блоков, кратчайший проверенный подход к берегу до48 блоков, четыре ориентации, причины отказа HEADROOM/PROTECTED_PLATFORM/NO_FOOTING/NO_REACHABLE_SHORE. Это ограниченное количество проверок; НЕ утверждение о допустимом MSPT.
- PosadPiece: геометрия опор/моста/ограждений и расширенный bbox; supports и bridgeLength сохраняются в NBT. Нет засыпки всего океана: только slab и сетка опор.
- PosadStructure: полный terrain planner после coarse rejection, bbox-based проверка принятых соседних footprints вместо прежнего произвольного candidate radius12. Никакой exclusion_zone не добавлено.
- nether_posad_biomes: #minecraft:is_nether + существующий #slavicmyths:nether_structure_biomes. Раскрытие сторонних тегов/модовые runtime биомы пока не проверено.

Проверки:
- Java21 clean build + ресурсы: PASS. JAR SHA256 b8ec348b0c862d163764421ad18fd8e86e4a739d41235660deb71c6bdf860317.
- Синтетические terrain tests: PASS для суши/лавы/ближайшего берега/четырёх поворотов/защищённого блока между coarse samples/недостатка опор/низкого потолка. Это НЕ world seed QA.
- 5/5 NeoForge GameTest: PASS тестовых assertions. Новый тест сравнивает каждый voxel до/после NBT, поддержку фундамента, настил и свободный мост. Это NBT roundtrip, НЕ relog.
- Настоящий vanilla Nether noise, 5seed x8 candidate cells: seed11=1 принято,29=0,101=1,2026=2,22022026=1; всего5/40. Список содержит ВСЕ biome holders vanilla Nether generator. Это вызовы planner, НЕ физические StructureStart в сгенерированном Nether, НЕ измеренная естественная частота: frequency0.75 в этом terrain survey не применяется, пары расстояний/распределение городов пока не измерены.
- Затраты на восемь candidate cells: 8.96/5.49/9.47/8.51/12.67 секунд соответственно. Performance gate НЕ ПРОЙДЕН: нужен более дешёвый planner/защита server-thread поиска до включения. Тестовый survey синхронный и не предназначен для production. Итоговые 5 тестов заняли48.13сек.
- Полная атомарность через чанки, claims/player protection, сохранение физического города/reload, пять seed с настоящими успешными стартами, blacklist/модовые биомы, locate/manual commands/retrofit/MP/GUI: NOT_RUN/не завершены. Текущее разрешение владельца на активацию условное; обязательные guards пока не выполнены. Ошибку редкости частотой не маскировать.

Логи: terrain-gates.txt, terrain-resources.txt. Следующая задача: устранить стоимость плотного native noise scan и завершить целостную защиту/размещение города, затем production data и штатные команды. Отчёт является checkpoint, не сдачей scope. Smoke для владельца и ограничения выше сохраняются; production/profile не обновлены.
