# Slavic Myths 2.2 — 2.2.4 Дружинный двор + 2.2.5 Кузнечный двор

**Ветка:** `hell`; `main` не изменяется. Дата: 30.10.2026. Issues #350 / #351, parent #358.

## Фактическая реализация

- Дружинный двор: утверждённые 2440 блоков из `2.2_DRUZHINNY_DVOR_MODEL_R1.json`, точный слот `PosadGeometry.Slot.DRUZHINNY_DVOR` X12..39/Z75..99, 28×25×18, 8 комнат, плац и 5 сундуков. Runtime asset: `data/slavicmyths/posad/druzhinny_dvor_2_2_4.gz.base64`; опубликованный Minecraft 1.21.1 template: `data/slavicmyths/structure/posad/druzhinny_dvor_2_2_4.nbt`.
- Кузнечный двор: утверждённые 1901 блок из `2.2_KUZNECHNY_DVOR_MODEL_R1.json`, слот `PosadGeometry.Slot.KUZNECHNY_DVOR` X12..37/Z44..69, 26×26×18, 5 сундуков, исходные направленные блоки кузницы. Runtime asset: `data/slavicmyths/posad/kuznechny_dvor_2_2_5.gz.base64`; Minecraft template: `data/slavicmyths/structure/posad/kuznechny_dvor_2_2_5.nbt`.
- `PosadNextBuildings` декодирует исходную R1 JSON без упрощения 3D модели, строго разрешает vanilla blockstate properties, проверяет 2440+1901 позиций и 5+5 сундуков. `PosadPiece` рендерит обе постройки только в СУЩЕСТВУЮЩЕМ едином городе 113×113 и поворачивает вместе с городом. Строгая проверка chunk clip остаётся в `postProcess`.
- Новые `PosadPiece` получают **Schema 3**; сохранённые Schema 0/1/2 НЕ достраиваются, не переставляют сундуки и не меняют положение старых домов. Миграция старого города без server-owned журналов и доказанного anti-dupe запрещена (fail closed).
- Все девять JSON loot таблиц (четыре дружинных + пять кузнечных) перенесены **byte-for-byte** из одобренных `loot_drafts`; нет нового предмета или изменённых весов. При первичном размещении сундуки используют серверный lazy loot. Сотник гарантированно получает 1 элемент незеритовой брони, 20% — вторую часть из того же исходного JSON; 70% монеты и 25% дополнительное оружие.
- `tools/generate_posad_next_buildings.py` воспроизводимо создаёт бинарные NBT и сжатые игровые JSON из двух исходных моделей. Workflow `.github/workflows/posad-next-buildings-assets.yml` публикует только заданные ресурсы в `hell` с обычным fast-forward push; никакого `force`.
- `tools/posad-tests/verify_next_buildings.py`: блочное NBT-сопоставление со всеми R1, вся исходная палитра, 10 chest NBT, четыре вращения без выхода за границы города, сравнение loot JSON с drafts, 10 000 offline сидов для награды сотника. Это не серверное открытие лута.

## Новые административные команды (только права OP)

`/slavicmyths dev posad info` — покажет текущую схему, перечень зданий и режим fail-closed.
`/slavicmyths dev posad templates <anchorX> <deckY> <anchorZ>` — только в Незере выводит четыре готовые vanilla-команды `/place template` по точным утверждённым слотам общего anchor города для поворота 0°. Команда сама **ничего не ставит** и не загружает чанки.

Пример **только для disposable QA Nether world**, если anchor города (0,64,0), поворот 0:
```mcfunction
/place template slavicmyths:posad/veche_hall_2_2_2 42 64 12
/place template slavicmyths:posad/communal_house_2_2_3 13 64 15
/place template slavicmyths:posad/druzhinny_dvor_2_2_4 12 64 75
/place template slavicmyths:posad/kuznechny_dvor_2_2_5 12 64 44
```
**ВНИМАНИЕ:** vanilla `/place template` обходит preflight города, не подходит для достройки уже существующего города и может заменить блоки. Не выполнять на пользовательских мирах. Нет нового /generate, который обходит защиту.

## Матрица приёмки

| Гейт | Состояние |
| --- | --- |
| Одобренные точные модели и fixed slots | CHECKED by generated/offline assets |
| Dev commands + единственный city-schema 3 и совместный rotation | IMPLEMENTED in code; physical runtime verify tracked separately |
| Лут 4+5: исходные JSON/вероятности | Static/10k seeded Python verification; Minecraft loot-open test NOT_RUN |
| Java21 clean production build + NeoForge physical tests | See GitHub Actions; only label PASS after confirmed logs |
| 5 seed natural Nether, biome/terrain, структура/StructureSet, /locate | **NOT_RUN / BLOCKED** — global StructureSet не активирован |
| Автоматическая безопасная достройка старых городов | **NOT_IMPLEMENTED**, previous schemas untouched |
| Полное удаление/повторная генерация сундука, антидюп + save/reload/MP | **NOT_RUN**, rollout блокирован |
| Фактическая проходимость, collision/headroom, client screenshots, orig PNG | **NOT_RUN/BLOCKED art** |
| Измеренные TPS/MSPT/performance | **NOT_RUN** |
| PolyMC test profile | **NOT_INSTALLED** (нет доступа к локальному ПК) |

**Важно:** если когда-нибудь появится новый native `nether_posad` StructureStart, schema3 уже размещает все четыре внутренние постройки (#347/#348/#350/#351), но **естественная генерация города всё ещё заблокирована** до безопасности, реального performance / biome QA. Нельзя выдавать появление зданий в коде за достигнутый survival-spawn без OP. Issues остаются OPEN, owner acceptance не объявляется.

## Подтверждённые результаты CI после физического исправления

- Последний кодовый checkpoint: [`c8a94d6c`](https://github.com/Deserag/slavic-myths/commit/c8a94d6c041c3435819f31b62ff95de55d16c47a) — узкое исправление `PosadPiece` для трёх резных тыкв тренировочных чучел: локальный безопасно clipped `ChunkAccess.setBlockState` вместо вызова vanilla carved-pumpkin `onPlace`, который иногда пытался читать соседний чанк и получал `null`. Оригинальные блоки/facing и утверждённая R1-модель сохранены.
- [Physical Java21/NeoForge CI 41049719791](https://github.com/Deserag/slavic-myths/actions/runs/41049719791): **SUCCESS**, `BUILD SUCCESSFUL`, настоящий `6 GAME TESTS COMPLETE`, `All 6 required tests passed :)`. Все четыре поворота; точная физическая укладка 2440+1901 новых блоков, все 10 новых chest block entities и server-side lazy loot table key, а также существующий каркас и дома.
- [Headless regression CI 41049719832](https://github.com/Deserag/slavic-myths/actions/runs/41049719832): **SUCCESS**, настоящий `36 GAME TESTS COMPLETE`, `All 36 required tests passed :)`, Java21 production build PASS.
- [Offline geometry CI 41049719826](https://github.com/Deserag/slavic-myths/actions/runs/41049719826): **SUCCESS**. Exact NBT verification, 2440 blocks / 20 states / 5 loot chests и 1901 blocks / 46 states / 5 loot chests; 10k-seed captain distribution: 7991 single armor piece, 2009 double, 7001 coin bonuses, 2512 extra weapons.
- Первые физические прогоны [41047764178](https://github.com/Deserag/slavic-myths/actions/runs/41047764178), [41048531784](https://github.com/Deserag/slavic-myths/actions/runs/41048531784) были **FAIL**, не скрыты: динамические соединения `crimson_fence` и непостоянный выход `BlockInWorld.getState()=null` при vanilla-carved-pumpkin pattern. Исправлены строго соответственно логике dynamic state и ограниченному chunk-local placement. Отдельный [run 41049263550](https://github.com/Deserag/slavic-myths/actions/runs/41049263550) прошёл ещё до второго исправления, поэтому возможную нестабильность vanilla callback отслеживать на будущих прогонах; один PASS не доказывает полной надёжности.

**Критическое ограничение для нового мира:** native `PosadPiece` schema3 действительно содержит все четыре дома (#347/#348/#350/#351) на фиксированном плане. Но **натуральный глобальный StructureSet всё ещё отсутствует/отключён**: пока не пройдены ограничение тяжёлого полного noise planner, атомарная защита участка, no-retrofit и реальные seed/biome/MSPT. Клиент/survival/MP/save-reload и доказанная защита от вторичного наполнения сундука остаются **NOT_RUN**. Утверждения о естественном появлении посада без OP и окончательном релизе не допускаются.
