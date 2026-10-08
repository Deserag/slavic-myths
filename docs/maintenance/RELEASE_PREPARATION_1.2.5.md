# Подготовка Slavic Myths 1.2.5 — 2026-10-08

Локальная подготовка завершена; Git-публикация и клиентская приёмка ещё не выполнены. Незакоммиченная работа пользователя 1.2.1–1.2.5 сохранена. Версия 1.2.5, Minecraft 1.21.1, NeoForge 21.1.255, Java 21, mod ID `slavicmyths`.

## Размер и сохранность

До: **5,559,312,107 bytes / 5.559 GB**. После: около **1.52 GB**, сокращение примерно **4.04 GB / 73%**, включая новые тестовые миры. Точные snapshots: [before](DISK_AUDIT_BEFORE_1.2.5.md), [after](DISK_AUDIT_AFTER_1.2.5.md).

- 25 старых QA runtime-папок (**3,823,721,458 bytes**) перенесены в `D:\slavic-myths-runtime-backup\maintenance-1.2.5-20261008`. Размер каждой проверен. Миры, configs, screenshots и логи сохранены. Перемещение уменьшает checkout, но не освобождает диск.
- `.git`: **837,227,971 → около 462,910,220 bytes**. Выполнен gc без prune, reflog expiry и history rewrite. История сохранена, fsck before/after PASS. Финальные index metadata дают небольшую вариацию числа.
- 4,798 runtime-файлов и 188 generated scratch artifacts исключены из индекса; локальные/резервные копии сохранены. Source, art, references и scripts не удалены. Legacy reference source в work сохранён.
- Source resources около **12.15 MB**. 127 byte-identical groups дают лишь **279,520** потенциальных raw bytes; IDs сохранены, speculative orphan deletion не выполнялся. Source-art/archive candidates в runtime resources отсутствуют.

[Cleanup](CLEANUP_1.2.5.md), [backup verification](backup-verification-1.2.5.json), [assets](ASSET_DUPLICATES_1.2.5.md), [JAR audit](JAR_CONTENT_AUDIT_1.2.5.md).

## Код и сборка

`VillageWork.validJob` проверяет loaded chunk **до** POI query. Для отсутствующего chunk запросов POI стало 0 вместо 1 за вызов, для loaded chunk результат прежний. Это вывод из кода, не timing measurement. Gameplay, cooldown, баланс, art, registry IDs и worldgen frequency сохранены. Новых global scans/caches нет. [Performance audit](PERFORMANCE_AUDIT_1.2.5.md).

Локальный Xaero compileOnly JAR заменён на **`maven.modrinth:xaeros-minimap:Q1tuMQBB`** (exact NeoForge 1.21.1 / 26.5.0). Published SHA-512 совпадает с старым локальным JAR. Адаптер по-прежнему необязателен и клиентский, сторонний JAR не bundling. Production build не требует `.tools`.

Test-only fishing scenario изолирован в daytime batch по примеру shepherd/linen и получил diagnostics. Assertions о реальной добыче, ровно одном catch, deposit и rod damage сохранены; test code не попадает в production.

## Фактические проверки

| Gate | Результат |
|---|---|
| `gradlew.bat clean build`, JDK 21 | PASS33s; 14 deprecation warnings; обычный `test` NO-SOURCE |
| City production resources | PASS469,691 checks / 240 templates / reproducible generator |
| Buildings production resources | PASS318,452 checks / 389 village NBT / 102 catalog entries / 334 replacements |
| Household production resources | PASS11,828 checks / 2,811 JSON / 6,014 model-texture links |
| Финальная general headless suite | PASS53/53 после fishing fixture isolation |
| City headless | PASS6/6: три климата, path/POI/trading/raid, native noise generation |
| Buildings headless | PASS6/6: showcase/entry/path, native jigsaw |
| Settlement SAVE + LOAD | PASS: actual chunk unload, region/entity/inventory/pending recipe чтение новым сервером |
| Military SAVE + LOAD | PASS: native playerdata load, quest progress, reward replay rejection, guard archetype |
| Git fsck before/after | PASS |
| Независимый source export + empty Gradle cache | PASS5m3s; без `.tools`; 5,358 JAR entries и SHA-256 совпали |
| Remote release clean clone | NOT RUN: release commit/branch ещё не опубликованы |

**8 реальных headless server запусков / 0 клиентов / 0 PolyMC запусков или установок** в этой работе. Loader/GameTest runs проверяют настоящий production code с внешним test sourceSet; отдельного запуска установленного JAR в обычном production-профиле не было. Restart проверяет реальное сохранение/новый server process, не пользовательский клиентский relog.

Финальный workload: 20/40 жителей, 1,000 ticks — 104/207 path requests и 40/80 searches; bounded gate PASS. Это actual counters, **не MSPT/FPS** и не полный gameplay acceptance большого поселения.

## Не скрытые неудачные попытки

- General первый **52/53**: `naturalworkstations` не получил одну профессию; новый мир **53/53**, финальный после fixture change **53/53**.
- Buildings первый **5/6**: guard POI/arrival в фиксированное время; новый мир **6/6**. Production AI/assumptions не ослаблялись.
- Restart SAVE **52/53** из-за рыбалки; оба SAVE assertions PASS. Restart LOAD **53/53**, оба LOAD assertions PASS. Fishing вынесен в daytime batch; итоговая general suite **53/53**.
- Первый city Gradle attempt не дошёл до сервера: resource read race при одновременном воспроизводящем генераторе. После завершения генератора run PASS. Это не дополнительный launch.

Все логи находятся в этой папке. Повторные passes не доказывают отсутствия scheduling variability прочих fixtures.

## Production JAR

`D:\slavic-myths\build\libs\slavicmyths-1.2.5.jar` — **9,276,270 bytes**.

SHA-256: `a515be4cca3c7424371ca3d0c4639ae80f19f6d04ccd005a2d100cfe8b87b9f6`.

Все 5,358 entries совпали с independently rebuilt JAR; manifest/metadata version корректны, smoke/verify/GameTests/worlds/source archives отсутствуют. [Evidence](clean-source-verification-1.2.5.json). Runtime resources не вырезались ради размера.

## README и ручная приёмка

README/CHANGELOG/RELEASE_NOTES обновлены; два заново построенных schematic NBT previews и лист 16 реальных item textures подписаны как схемы/ресурсы, не screenshots. [Capture guide](../media/readme/CAPTURE_GUIDE.md).

Краткая manual QA: client models/GUI мешка/напитков, natural city целиком на обычном рельефе, большие production/logistics/guards/raids, bosses, player relog. Клиент запрещён AGENTS.md. FPS/MSPT не измерялись. LICENSE отсутствует, metadata All Rights Reserved сохранена.

## Git-публикация ожидает прямого разрешения

Current main HEAD: **`8d3ea4885f96db823e36ce670dcaf03c9a57718e`** — прежний commit, не финальная 1.2.5. Working tree содержит сохранённую 1.2.x работу и эту подготовку; staged deletions относятся к index cleanup.

Remote: `https://github.com/Deserag/slavic-myths`; проверенный default HEAD **main**. `release/1.2.5-distribution` на remote отсутствует. Commit/push/default change не выполнялись: AGENTS.md требует **«Не делайте commit автоматически»**; архив этого не отменяет. GitHub CLI отсутствует, admin auth/permissions для смены default не проверялись.

После прямого разрешения: проверить final file list, commit main, ordinary push, создать/push `release/1.2.5-distribution` от final main, сменить default при доступных правах, выполнить **remote clean-clone build release** и оставить checkout на release. Source-export gate не подменяет clone.

Source export сохранён: `D:\slavic-myths-clean-source-125-20261008`. Private fresh Gradle cache `D:\slavic-myths-clean-gradle-125-20261008` занимает **706,463,638 bytes вне проекта**: автоматическая проверка отклонила recursive deletion (`blocked by policy`); cache оставлен, обход не предпринимался.
