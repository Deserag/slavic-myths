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
