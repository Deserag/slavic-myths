# Пекельный посад — checkpoint 2.2.2 + 2.2.3, 30.10.2026

Scope: #347 Вечевая палата + #348 Общинный жилой корпус; parent #358. Branch hell only.

## Source of truth
- Hall: exact 3598 non-air positions from 2.2_VECHE_HALL_VOXELS_R1.py, packed SHA256 8813d5c44e144ba5e6f087584f7acbf8dfa4cf25c0e31f6f9193f0b933035cda.
- House: exact 1992 positions from 2.2_COMMUNAL_HOUSE_MODEL_R1.json, 4 approved chest markers and their original loot drafts.
- Slot anchors: hall city origin 43/13 (models include -1 overhang); communal city origin 14/16 (models include -1 overhang).
- Only new native PosadPiece schema 2 invokes models; previously persisted schema 0/1 never silently adds these buildings. Full server-owned retrofit is not implemented.
- No independent structures or spawns. The structure_set for nether_posad has NOT been activated here.

## Проверенный CI / GameTest — 30.10.2026

- Exact R1 geometry, collision/slot bounds, four chest JSONs: [offline geometry CI PASS](https://github.com/Deserag/slavic-myths/actions/runs/41040903032).
- Java 21 clean production JAR build: **PASS** in [dedicated physical CI](https://github.com/Deserag/slavic-myths/actions/runs/41042304195).
- NeoForge 21.1.255 real GameTestServer: **6/6 required PASS** on [physical city QA](https://github.com/Deserag/slavic-myths/actions/runs/41042304195), explicit output `All 6 required tests passed :)`. Tests include exact 3598 + 1992 physical block checks, four 90° rotations, all four chest block entities and nonnull lazy loot tables, gate/platform/towers and terrain survey.
- Overall hell headless QA: **36/36 PASS on rerun** in [run 41040903005](https://github.com/Deserag/slavic-myths/actions/runs/41040903005). Initial failure in boss natural-spawn test `realnetherbiomeandoceanchunk` was not reproduced; flaky risk remains for separate investigation.
- Native Nether noise survey: 5 accepted / 40 candidates across 5 seeds; this is **not** physical natural placement or frequency acceptance. Performance gate pending.
- One early dedicated CI attempt appeared green while NeoForge crashed due to missing `META-INF/neoforge.mods.toml` on test fixture. Fixed in [a5191ee](https://github.com/Deserag/slavic-myths/commit/a5191eeae3ffe3d448bc2b9b281354c0afe6f8ce). CI now fails if the GameTest completion record is absent or failure appears.

## Requirement | Evidence | Status
| Requirement | Evidence | Status |
| --- | --- | --- |
| Exact hall/house voxels | Source data checksum+counts, physical assertions for all four turns | PASS (offline + GameTest) |
| Four fixed chest loot table bindings | Exact approved JSON drafts; 4 placed chest entities with nonnull lazy loot table | PASS (binding; actual loot opening/anti-dupe NOT_RUN) |
| Four city rotations, geometry nonoverlap | Physics and offline city rotation tests | PASS for the verified templates |
| Save/reload and no destructive retrofit | Persisted schema gate prevents schema0/1 automatic build; no real retrofit | PARTIAL; reload/MP NOT_RUN |
| Passage / headroom / stairs / bed safety | Template and shell gates tested; navigation as player not tested | PARTIAL; client pathfinding NOT_RUN |
| Five seeds, biome/terrain, chunk borders, MP, client screenshots, performance | Five-seed noise planner survey only | PARTIAL; real natural placement, performance, visuals, MP NOT_RUN |
| Java21 compile + NeoForge dedicated server GameTest | Native Java21 build + 6/6 GameTest PASS | PASS (bounded test scope) |
| Original owner-approved PNG visual reference | Not currently in GitHub | BLOCKED ART |

No Issue is closed or owner-accepted. StructureSet remains inactive. Do not confuse successful physical isolated GameTests with finished worldgen, staged migration or complete client/survival QA. `hell` only; `main` unchanged.

## Продолжение 30.10.2026 — проверенные бинарные NBT и 10 000 loot seeds

Из ветки hell (не main) воспроизводимо запущен существующий `tools/generate_posad_buildings.py`; обе структуры записаны **реальными gzip NBT** в коммите [9d0d553](https://github.com/Deserag/slavic-myths/commit/9d0d55302c384624dc33f789bbe5d81d41ba868f):

- `src/main/resources/data/slavicmyths/structure/posad/veche_hall_2_2_2.nbt` — 10 362 байта, ровно 3598 non-air блоков, 15 палитровых состояний, SHA256 `f0abcac0718159fddb53099bd2519801be102ff1c37b88fe438c54b8cb7118fc`.
- `src/main/resources/data/slavicmyths/structure/posad/communal_house_2_2_3.nbt` — 6071 байт, ровно 1992 блока, 17 палитровых состояний, четыре chest NBT LootTable, SHA256 `d9bc3ed45785d235d33ef367f7bcd21b4d261f40af81ac9c5b9bfe4285a57da7`.

Новые проверки: `tools/posad-tests/verify_generated_building_nbt.py` сверяет КАЖДУЮ позицию и каждый blockstate с одобренными R1-исходниками, NBT-метаданные и строго четыре loot-маркера; `tools/posad-tests/verify_communal_loot_distribution.py` моделирует 10 000 различных seed по точным JSON-таблицам. **PASS:** [offline CI 41044271772](https://github.com/Deserag/slavic-myths/actions/runs/41044271772) — 154 107 Java-geometry assertions, оба NBT, четыре исходных loot JSON, 10 000 loot simulations (20.30% шанс домов с монетой, среднее 0.3048 монеты на дом). Это аналитическая проверка конфигурации, НЕ реальное серверное открытие сундуков.

Добавлен `.github/workflows/posad-nbt-assets.yml`: Python-only воспроизводимое создание, валидация до публикации, запись только двух NBT, обычный push без force; при изменении одобренных исходников во время задания workflow отказывает в публикации устаревших NBT. Исходники архитектуры и лут не менялись. Уже существующая `PosadBuildings` продолжает детерминированно читать R1-ресурсы и строить город через `PosadPiece`; опубликованные NBT пока являются дополнительными проверенными ресурсами и НЕ отдельными спавнящимися structures.

**Нерешённые ограничения:** natural StructureSet отключён; новые натуральные старты 5 seed / multiplayer / save-reload / игроковая навигация / визуальная сверка PNG не пройдены. Реальное раскрытие loot и защита при удалении сундука перед повторным `postProcess` не подтверждены: по анализу текущего кода есть риск повторного создания сундука после его удаления, поэтому anti-dupe = **NOT_PASS** до защищённого runtime-сценария и исправления. Старые schema 0/1 не достраиваются. PolyMC не изменялся. Issues #347/#348/#358 остаются OPEN, без owner acceptance.

### Завершившийся физический CI на опубликованных NBT

[Run 41044342991](https://github.com/Deserag/slavic-myths/actions/runs/41044342991), исходный commit `0dfd750f1538fb8370188d41b944593d0dab40c3`: **SUCCESS**. Настоящий Java 21 clean production build — **BUILD SUCCESSFUL**; перед сборкой независимый NBT-verifier подтвердил 3598+1992 и 4 chest LootTables; NeoForge 21.1.255 dedicated GameTestServer дал точный лог: `6 GAME TESTS COMPLETE`, `All 6 required tests passed :)`. Шесть тестов включают физическую укладку обеих R1-моделей, 4 поворота и четыре lazy-loot chest block entities. Это НЕ испытание удаления сундука и повторной генерации, не перезаход игрока в мир и не нагрузочный performance gate. Список ограничений в предыдущем разделе остаётся в силе.
