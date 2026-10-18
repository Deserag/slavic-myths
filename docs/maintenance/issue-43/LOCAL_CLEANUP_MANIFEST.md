> Исторический снимок до фактической очистки. Условия «не удалено», «ожидает согласования» и состояния PR ниже заменены новой инструкцией владельца. Актуальный результат: [REPORT.md](REPORT.md), последняя доставка: [PR #44](https://github.com/Deserag/slavic-myths/pull/44).

# Адресная очистка issue43

**Ничего не удалено и не перемещено.** Все размеры логические, GiB=2^30; значения наблюдались до cleanup. Parent KEEP rows в CSV включают отдельные SAFE descendants: их нельзя суммировать повторно.

|Категория|Результат|
|---|---|
|13 зарегистрированных рабочих копий, shared Git один раз|23.559 GiB (D:16.473; C:7.087)|
|Основная D:/slavic-myths, включая независимый вложенный clone|9.106 GiB|
|SAFE:55 generated/cache directories с проверенным backup|2.218 GiB (D:1.578; C:0.640)|
|ARCHIVE: целиком старый независимый publish clone с уникальными specs|1.441 GiB с D на C; это перенос, не уничтожение истории|
|Возможное освобождение D после SAFE+ARCHIVE|3.019 GiB|
|Реально освобождено сейчас|0 bytes|

## TOP20 непересекающихся кандидатов

|Точный путь|MiB|Решение|Backup|
|---|---:|---|---|
|`D:\slavic-myths\.tools\publish-1.9.4`|1475.68|ARCHIVE|historical-publish-1.9.4.zip|
|`D:\slavic-myths\.gradle`|141.04|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`C:\Users\pavel\.codex\worktrees\performance-2-release\slavic-myths\build`|123.62|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`C:\Users\pavel\.codex\worktrees\poloz-body-melee\slavic-myths\build`|123.59|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`C:\Users\pavel\.codex\worktrees\guardian-cube\slavic-myths\build`|123.28|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`D:\slavic-myths\build`|123.07|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`C:\Users\pavel\.codex\worktrees\poloz-effects\slavic-myths\build`|123.07|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`C:\Users\pavel\.codex\worktrees\advancements-2-implementation\slavic-myths\build`|123.02|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`D:\slavic-myths-performance\build`|122.89|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`D:\slavic-myths-advancements-v3\build`|122.85|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`D:\slavic-myths-integrated\build`|122.62|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`D:\slavic-myths-balance\build`|122.62|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`D:\slavic-myths\work\creative-build`|121.52|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`D:\slavic-myths\work\pesigolovtsy\snapshot-20261010-221422\build`|107.47|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`D:\slavic-myths-balance-baseline\build`|104.33|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`D:\slavic-myths\work\chud-152\production-build`|100.36|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`D:\slavic-myths\work\boss-rework-build`|100.04|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`D:\slavic-myths\.tools\boss-reimplementation\release-snapshot-20261008\build`|98.57|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`D:\slavic-myths\work\chud-151\build`|95.93|SAFE_TO_REMOVE|rebuildable-output-backup.zip|
|`D:\slavic-myths\work\chud-152\build`|90.38|SAFE_TO_REMOVE|rebuildable-output-backup.zip|

## Полный адресный manifest и команды

[CSV: все пути, bytes, tracked, unique, consumers, backup, command, actual/proposed](LOCAL_SIZES_BEFORE_AFTER.csv). SAFE only: `tools/recovery-43/cleanup.ps1` — без аргументов только список, без -Execute только проверка. Исполнение требует точных ApprovedPath владельца и отдельного -Execute/ShouldProcess. Скрипт заново проверяет resolved root, отсутствие tracked/unique/reparse данных, SHA256 архива и каждого текущего файла; при изменениях отказывается удалять. **Скрипт не выполнялся**, только разобран PowerShell parser без ошибок. Никакого массового work/ удаления.

Backup: `C:\Users\pavel\.codex\recovery\slavic-myths-20261018`. Рабочие изменения9 грязных worktrees сохранены с patch/index status и file SHA256. Generated/cache backup: каждый entry SHA256 проверен. Полный shared Git и старый clone: все CRC проверены, SHA256 архивов в [ARCHIVE_RECEIPTS.json](ARCHIVE_RECEIPTS.json). Общий объём созданных zip 4.177 GiB на C; это затраты на защиту, не уже освобождённое место.

Восстановление: не перезаписывать активную .git. Извлечь backup в отдельный каталог; working-changes-*.zip содержит прежние HEAD/status/staged+unstaged patches и files.json. Для generated outputs `_files.json` хранит исходный путь каждого файла, `NNN/` prefix указан в CSV. Полный clone восстанавливается как независимый репозиторий.

## Что сохраняется

- Все worktrees с изменениями/PR, локальная V3, ветки с unique commits и их ignored unique data. Сейчас **0 worktree признаны пригодными для удаления целиком**; поэтому автоматических git worktree remove команд нет.
- Весь work/kuzya/revision: исходное видео/аудио, расшифровки и обработка; не кэш. art, future, docs/specs, docs/verification, release и лицензии не чистятся по расширению/возрасту.
- run-* и world/saves/level.dat: точные тестовые состояния и restart evidence. Нет разрешения на их уничтожение; не включены в SAFE.
- .tools/jdk8, .tools/port-backups и другие старые runtime/artifacts: отдельное решение владельца; основной target Java21 не доказывает, что исторические инструменты больше не нужны.
- Shared .git/objects не трогать. Full fsck shared и независимого clone exit0; missing/broken/invalid не найдены.10 orphan idx и42 garbage (~51.51MiB), dangling objects — не доказательство потери reachable history. gc/prune не запускались.
- Пять незарегистрированных Codex task папок содержат только0-byte name marker; размер0, очищать ради места бессмысленно.

## Как не потерять main и рабочие копии

Merge integration PR обновляет GitHub main, но не checkout других13 копий. После принятия: fetch, сверить ожидаемый main HEAD; local main разрешено двигать только FF, если нигде не checked out. Основной dirty checkout не переключать/сбрасывать автоматически: его16 changed files уже сохранены и local-only reports включены в PR. Три пользовательских status docs не перезаписывались — новые canonical sections объединены вручную в интеграционном checkout.

Единственная новая ветка переиспользует ранее чистый `D:/slavic-myths-advancements`; дополнительных worktrees не создано. Прежняя codex/advancements-micro-patch branch сохранена локально и на origin. Оригинальный основной checkout D:/slavic-myths не изменялся.
