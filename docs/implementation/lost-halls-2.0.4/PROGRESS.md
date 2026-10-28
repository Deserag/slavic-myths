# ПЕРЕДАЧА КОНТЕКСТА — SLAVIC MYTHS 2.0.4


## Референсы в GitHub Issues: сверено 28.10.2026

- **Десять задач (#288–#294, #301–#303) обновлены** в самих GitHub Issues: в начале каждой указаны применимые оригиналы, точное имя архивов из ChatGPT Library, утверждённые размеры/SHA-256, и ссылка на общую карту.
- [Единый список утверждённых ZIP, пяти оригинальных SHA и ownership по Issues](../../concepts/lost-halls/APPROVED_REFERENCE_ASSET_INDEX_2.0.4.md); путь на рабочей ветке `hell`: `docs/concepts/lost-halls/APPROVED_REFERENCE_ASSET_INDEX_2.0.4.md`. Фактическое обнаружение файлов отражено и в [references-current.json](references-current.json).
- **Где находятся архивы:** пользовательская библиотека ChatGPT `/славянская мифология/`: основной `Чертоги_заблудших_2.0.4_жёсткое_ТЗ_референсы.zip` (8837561 байт), награды `Чертоги_2.0.4_награды_утверждённые_референсы.zip` (3241149), Постень `Постень_2.0.4.5_утверждённый_арт_и_чертежи_Codex.zip` (5106217), мобы `Чертоги_заблудших_2.0.4.4_референсы_для_Codex.zip` (4629422). **Файлы найдены; не требовать повторного художественного утверждения**.
- **Граница доступа:** исходные ZIP/исходные PNG не появились в GitHub, побайтовый доступ через Library `files.materialize` отказал; утверждённые пять SHA до сих пор **НЕ перепроверены фактическими байтами**. Статус SOURCE для художественной работы `BLOCKED_REFERENCE`, не `PASS`. Прямые ссылки на GitHub-контракты = **ссылки на ТЗ**, не изображение.
- **Техническая работа по правилам пользователя продолжается без подмены исходного арта**, при этом настоящие игровые тесты и приемка будут отдельно; старые локальные отрицательные результаты не опровергают обнаружение архивов в Library.


## Контрольная точка: топология S1–S6, 28.10.2026

- Ветка `hell`; при продолжении зафиксированы параллельные независимые документы 2.0.5 (`20c76ba2`, `0a6b243a`) и сохранены без изменений.
- Коммит `f71b82c7704d124d79da1e6bbad8a5997be7cbed`: создан `LostHallsRegionTopology` — воспроизводимый по seed и региону связный граф из шести пространств S1–S6 и уникального логического узла арены Постеня. Три смещённых планировочных уровня, поворот/отражение, 8 двусторонних связей включая обход; UUID привязаны к seed+region. Никакой записи блоков, загрузки чанков или регистрации новых ID.
- Коммит `e3eed4a9255f13e6fb66ffe2f883ae33f7badcb5`: добавлены offline проверки 5 seed × 3 условных региона, всех шести типов, связности, числа выходов, UUID и диапазонов.
- **Важно:** это только математический план. Не реализованы действительные 3D-пещеры, chunk clipping, серверный вызов `LostHallsRoomPieces`, природный биом и гарантированная **физическая** арена. Указанные выше коммиты не означают выполнение #291/#294.
- Архивы четырёх утверждённых наборов обнаружены по названиям в ChatGPT Library, но `files.materialize` для всех вернул `no authorized raw-byte materialization path`; исходные SHA не прочитаны/не подтверждены. Художественные работы остаются `BLOCKED_REFERENCE` без заменителей.
- Java 21/Gradle clean build, новые offline tests — **NOT_RUN** (нет рабочей копии и артефактов сборки; GitHub Git недоступен по DNS); Minecraft/PolyMC/dedicated server/GameTest — **NOT_RUN_ENV_BLOCK** согласно запрету запуска. Никакого PASS по компиляции, worldgen, визуалу или релогу.
- Следующий шаг: получить доступные байты утверждённых ZIP, проверить SHA; затем настоящие текстуры/BlockStates/ModelParts и физическая генерация S1–S6; серверный completion, 5 мобов, Постень/руна/награды/головы; последним #294. Не закрывать Issues без владельца.


## Posten server reconciliation checkpoint — 28.10.2026

- HEAD before stage `7c770fa31e35db2a4f4500c43b0a6c912f640c68`; source commit [`bd8db5c7d57d`](https://github.com/Deserag/slavic-myths/commit/bd8db5c7d57d6b99f9273ccb592fb2e32a495d7d), branch `hell`.
- `PostenReconciliation.inspect` computes a **read-only, deterministic** recovery plan from journal snapshots and observed owned UUIDs when *all arena chunks and entities are loaded*: restore BODY at 650→260, restore ONLY living copies (each at own NBT HP), never restore dead copies/defeated boss, mark obsolete body/clones for removal. Duplicate UUID observations and invalid snapshots fail closed. Missing chunks => **defer**, no chunk tickets or speculative spawns.
- Tests authored for body, split, one dead clone, defeated after NBT reload, missing/unloaded area, duplicate observed UUID, malformed masks; these new tests and production code are **NOT_RUN/NOT_COMPILED**. Important: **this is the reconciliation decision model, NOT physical spawning/recovery**; no Posten EntityType/arena controller exists and no actual server hook invokes it.
- New `LostHallsRoomPieces` completion journal + its tests remain unexecuted too. No Minecraft/GameTest/server/PolyMC runs; owner runtime QA intentionally deferred. Art SOURCE_GATE `BLOCKED_REFERENCE`, scene generator and boss EntityType `BLOCKED_DEPENDENCY`.
- Next concrete integration: obtain approved PNGs/SHA, implement genuine model/EntityType/arena anchors, then on bounded arena load inspect the journal and reconcile actual entities with health, masks, spawn checks and bossbar; implement **crash-consistent** physical reward delivery before claiming victory.


## Checkpoint — продолжение третьего биома (28.10.2026)

- Starting HEAD `ab421d9186ee11a84fef46d6b7d8f43bdf4e6560`, production commit: [`a0f3608955b1`](https://github.com/Deserag/slavic-myths/commit/a0f3608955b178c99f0192d1310d3cfa70c54cf4), branch **hell**.
- New `LostHallsRoomPieces` server SavedData: per-UUID S1–S6 room descriptor and immutable expected chunk footprint, persistent per-chunk completion receipts, replay-safe acknowledgements, fail-closed corrupt/unknown NBT. Only when *all* pieces have acknowledged can producer invoke `publish`, which checks final loaded blocks and walkable exits using existing `LostHallsRoomPublication` and stores the navigable node in `LostHallsRooms`. No force-load, global tick scan or new registry IDs. Pending receipts persist on incomplete/unloaded routes.
- Offline fixtures added for 2-chunk out-of-order completion, duplicate/unknown/wrong chunks, wrong UUID/scene, NBT reload and fail-closed corruption. These fixtures were **AUTHORED but NOT_RUN**, including no Java/Gradle compilation; remote GitHub commit is not verification of their execution.
- Genuine worldgen producer/callback for S1–S6 **does not exist yet**, so no actual room can currently acknowledge a piece or publish itself in gameplay; Posten entity/arena/spawns/items also missing. **BLOCKED_DEPENDENCY**.
- Approved PNGs not present in available local/repository files: **BLOCKED_REFERENCE**. NeoForge client/dedicated server/Minecraft/GameTest/PolyMC **NOT_RUN_ENV_BLOCK**; no owner acceptance claimed. The second biome also remains pending joint runtime QA.
- Next: obtain exact original approved art/SHA for #289/#290; implement real chunks/clipping and server-thread completion callbacks, call `LostHallsRoomPieces.begin/acknowledge/publish` only after physical chunk pieces complete. Separately implement Posten entities + recovery when source models are available; review its crash-consistent loot delivery before claiming rewards.


## Browser/GitHub checkpoint — 28.10.2026, route publication (#291)

- Baseline `hell` HEAD: `c7bbe137d8cd842be8d66636a409252aa9150f13`; production gate commit: [`cf289f11bd68`](https://github.com/Deserag/slavic-myths/commit/cf289f11bd689d057c5c435f40b6ff10f05181a5).
- `LostHallsRoomPublication.publish` validates ONLY already loaded Nether blocks and final declared exits through a 4096-candidate bounded route before saving a node in existing `LostHallsRooms`; no old registry ID or NBT schema changed.
- Offline fixture coverage added to `tools/lost-halls-tests/.../LostHallsOffline.java`: crossing a chunk border; missing, duplicate and unreachable exits; unsafe bridge support; out-of-bounds candidate; existing metadata NBT roundtrip. Fixtures and new Java code are **NOT_RUN** (GitHub connector is not a Gradle/JDK execution environment).
- The S1–S6 generator does not exist and therefore cannot call this production gate yet. Never publish unbuilt rooms; future producer MUST call `publish` after final multi-chunk placement.
- Posten entity/arena reconciliation and physical phase restoration remain `BLOCKED_DEPENDENCY` (no real Posten EntityType). Existing `PostenEncounters` NBT phase records are present, but do not spawn or restore physical copies.
- Minecraft / dedicated server / GameTest / PolyMC: **NOT_RUN_ENV_BLOCK**; original PNG art: **BLOCKED_REFERENCE**; no Issue accepted or closed.


> **Актуализация через GitHub, 28.10.2026.** Текущая реализация трёх backend-классов отправлена в `hell` коммитом [`6f79d3f9`](https://github.com/Deserag/slavic-myths/commit/6f79d3f98783e9cff1defa835394c6b13e16e47a). Ниже сохранён **исторический checkpoint Codex до коммита**, включая старый `HEAD=877b7d69` и уже неактуальную пометку «незакоммичено» — он не описывает нынешнее состояние удалённой ветки. Для продолжения разработки, порядка этапов, актуальных блокеров и code-review см. [NEXT_IMPLEMENTATION_REQUIREMENTS.md](NEXT_IMPLEMENTATION_REQUIREMENTS.md). Полный статус независимой серверной инфраструктуры — [REPORT.md](REPORT.md). SOURCE ART остаётся `BLOCKED_REFERENCE`, gameplay `NOT_RUN`.


Обновлено 28.10.2026. **Задача не завершена. BLOCKED_REFERENCE на #289.**
Причина checkpoint — сохранение точки продолжения по поручению владельца;
это не сдача биома и не утверждение об исчерпании контекста.

## 1. Git и рабочая папка

- **Работать здесь:** `C:/Users/user/.codex/worktrees/lost-halls/slavic-myths`.
- Ветка `hell`, HEAD `877b7d69af0e809850827806045d82fde901a1a3`.
- `origin/main`: `e7fcd0e2f761ab81dd2b5694d3ee78f04f733c2c`.
  Повторный fetch обеих веток не выявил новых коммитов. История shallow.
- Исходная папка `C:/Users/user/Documents/slavic-myths` остаётся на старой
  `master` с пользовательскими незакоммиченными текстурами/документами.
  **Не переносить их поверх новой hell и не очищать эту папку.**
- Незакоммичены: Maven filters в `build.gradle`; дополнения трёх status docs;
  перенесённые из main 14 contracts/blueprints; tools и evidence первого
  source gate; текущий каталог `lost-halls-2.0.4`.
- `git diff HEAD -- src/main` пуст. Commit/push/merge/PR не выполнялись.

## 2. Что реально выполнено

Получены исходники двух предыдущих биомов, адресно скопированы только ТЗ третьего
из main. Изучены #288/#289 и снимки #290–#294/#301–#303, актуальные AGENTS и
requirements. Повторно прочитаны live #288/#289 и их пустые комментарии:
новых решений, отменяющих source gate, нет.

Сделаны static source/resource inventory, SHA256 manifest 9457 production-файлов,
проверка предложенных ID, SHA256/PNG/ZIP source validator и восемь его тестов.
Исправлена маршрутизация запросов JEI/Curios через Maven group filters.

**Production-механики третьего биома НЕ реализованы:** нет его блоков, шести
модулей, пяти EntityTypes, Постеня, наград, руны, worldgen или новых голов.
Не считать tooling реализацией #289 целиком или исполнением игровых задач.

## 3. Файлы

- `docs/concepts/lost-halls/` — 12 документов/чертежей, strict acceptance и все locks.
- `docs/concepts/NETHER_THREE_BOSS_HEADS_LOCK_2.0.X.md` и соседний SVG.
- `docs/implementation/lost-halls-source-gate/REPORT.md`, `baseline.md`, `id-plan.tsv`.
- `docs/implementation/lost-halls-source-gate/issues/` — 10 снимков Issues.
- `docs/implementation/lost-halls-source-gate/evidence/` — inventories, SHA manifest,
  build/resource logs, receipts и сохранность исходной рабочей папки.
- `tools/check_lost_halls_references.py`, `tools/test_lost_halls_references.py`,
  `tools/audit_lost_halls_sources.py`.
- `docs/implementation/lost-halls-2.0.4/references-current.json` — текущий поиск.

## 4. Проверки

Прошлый проход: обычный Java21 clean build FAIL (timeout Curios Maven).
С тем же Curios 9.5.1+1.21.1 из Modrinth (immutable version `yohfFbgD`, SHA512
сверен) и локальным init-script — **clean build PASS, 28s**. Resource/JAR verifier
PASS: 8254 resources, 2004 Java21 classes. Gradle test = NO-SOURCE.
Восемь тестов source validator PASS; это не тесты игровой логики.

JAR `build/libs/slavicmyths-2.0.8-HELL.jar`, SHA256:
`872c3f468b5c626faafc4aeb32b500da2b7214f685905178ac93d9c52778be18`.
В текущем проходе SHA повторно подтверждён; повторная сборка прежних исходников
не запускалась. В нём существуют два предыдущих биома, Чертогов ещё нет.
Версия контента 2.0.4 не означает понижение mod_version с 2.0.8-HELL.

**Minecraft/client/server/GameTest/PolyMC = 0 запусков в этой работе.**
Runtime/worldgen/сохранения/MP/FPS/MSPT — NOT_RUN_ENV_BLOCK.
JAR не устанавливался: POLYMC_NOT_INSTALLED.

Для следующей сборки (PowerShell, JAVA_HOME только в дочерней команде):

```powershell
$env:JAVA_HOME='C:/Users/user/Documents/slavic-myths/.tools/jdk21/jdk-21.0.12.1+1'
.\gradlew.bat clean build -I C:/Users/user/Documents/slavic-myths/.tools/curios-9.5.1/local-curios.init.gradle
```

Для проверки resources/reference tools задать дочерний
`PYTHONPATH=C:/Users/user/Documents/slavic-myths/.tools/python-libs` (Pillow).
Локальный init-script и его объяснение также сохранены в evidence.

## 5. Блокеры

Проверены 474 PNG в attachments, Downloads, Desktop, Pictures и docs/concepts.
Пять обязательных SHA не найдены, ошибок чтения нет. Требуются оригиналы:

1. `approved_originals/blocks.png` (1448×1086), камень именно вариант №3.
2. `approved_originals/scenes.png` (1448×1086).
3. `approved_originals/mobs.png` (1672×941).
4. Постер наград (1536×1024), SHA начинается `0eb92eea714205ca`.
5. `approved_originals/posten_original_1536x1024.png`, SHA начинается `34e30e8cde56dffa`.

Все полные SHA закреплены в `tools/check_lost_halls_references.py` и контрактах.
Архив исходной тройки называется
`Чертоги_заблудших_2.0.4_жёсткое_ТЗ_референсы.zip`; награды/Постень передаются
отдельными архивами. Для #302 нужны и утверждённые оригиналы двух прежних боссов.
Владельцу задан вопрос о локальном пути/ссылке; ответа на момент checkpoint нет.

Основание запрета начинать production без этого: #289, раздел «Результаты ДО
любого production-кода» и «Не начинать блоки до принятого baseline/texture sheet»;
STRICT_ACCEPTANCE §1: «Если не получил — остановить художественную часть».
Не подменять отсутствующие PNG словами, SVG, генерацией или похожими ассетами.

## 6. Конкретная точка продолжения

Получить путь/архив, затем:

```powershell
python tools/check_lost_halls_references.py 'ПУТЬ_К_АРХИВУ_ИЛИ_ПАПКЕ'
```

После SOURCE PASS закончить #289: approved-reference manifest, texture sheet
главного варианта №3, floorplan и три разреза с путями возврата; исходники
`BLOCK_PIXEL_CONTRACT_2.0.4.md` / `SCENE_AND_NAVIGATION_CONTRACT_2.0.4.3.md`.
Далее #290 → #291 → #292 → #301 → #293 → #303/#302 → #294, без запроса нового
поручения после каждого этапа. Вся цепочка уже прямо поручена владельцем.

## 7. Что сохранять и учитывать

- Сохранить все текущие незакоммиченные изменения, evidence не перезаписывать
  новыми «PASS». Main не менять/не сливать; не закрывать Issues, не commit/push.
- Уже есть `slavicmyths:zharenitsa_head` в `blackcinder/CinderBlocks.java`:
  сохранять ID, развить wall mount/animation по #302, не создавать дубликат.
- Nether mixin `navya/mixin/NetherParametersMixin.java` различает сейчас только
  cinder/non-cinder. При #294 нужна явная поддержка третьего biome holder:
  иначе у него окажется climate Навьей рощи. Пока mixin не изменялся.
- Registry inventory статический и содержит RESOURCE_CANDIDATE, не является
  полным runtime registry dump. Совпадение каталога не доказывает natural spawn.
- Runtime запрещён актуальным AGENTS; ни перезаход, ни 5×4 seed, ни MP не PASS.
  Сохранения/установленный PolyMC JAR не трогать. Технический JAR ≠ готовый биом.
