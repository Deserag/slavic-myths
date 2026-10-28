# ПЕРЕДАЧА КОНТЕКСТА — SLAVIC MYTHS 2.0.4

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
