# 25.10.2026 — объединённый тестовый кандидат HELL: IN_PROGRESS

Прямое поручение владельца: объединить фактические изменения Ада/Навьей рощи и поиска/генерации в основной папке, опубликовать только ветку hell, убрать лишние рабочие копии с сохранением данных и установить отдельную вторую сборку PolyMC. Это разрешает коммиты и push данного пакета и отдельную тестовую установку; не означает приёмку механик или разрешение запуска.

Каноническая папка: D:/slavic-myths, ветка hell. Сохранены HELL/Навья роща из 8e72a20a и генерация/встречи/Яга из 1ea15f8a. Merge ae9ebdf3. Два набора локализаций объединены по ключам с проверкой конфликтов; обе части документации сохранены. Исходники генерации Ада и Навьей рощи не переписаны при объединении. Registry IDs сохранены.

Старая копия advancements-v3: 101 из 113 отличавшихся source/tool-файлов уже совпадают с актуальными (с учётом LF/CRLF), оставшиеся 12 — последующие изменения основного проекта: интерфейс книги/поручения/NPC, новая ферментация, корректный значок first_flight, обновлённые проверки. Устаревший код не накладывается поверх новых реализаций. Полный исходный checkpoint 877bd57f сохранён локально, включая всё рабочее дерево в независимом архиве. Все файлы иных двух папок, включая ignored материалы/старые JAR/work, архивируются до удаления. [Пути/SHA архивов и коммиты](consolidation.json).

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Объединить оба актуальных пакета | Merge ae9ebdf3, обе ветки в истории hell | PASS |
| Сохранить локализации и актуальную логику | Merge по ключам, сборка и offline gates | PASS |
| Чистая сборка объединённого source | clean build 2м12с, Java21, production 2.0.0-HELL | PASS |
| Команды/постройка/признание победы/Жар-птица/Мор | checkOnDemandOffline, verifyCommandsOffline, firebirdOfflineChecks, higherPolicyChecks | PASS (JVM; не игра) |
| Ресурсы HELL/Навьи/общего JAR | 8001 ресурс,1925 Java21 classes; три независимые проверки | PASS |
| Модели в JAR не содержат tests; native export | exportNavyaRigs 9 rigs и resource checks | PASS (offline) |
| Удаление лишних рабочих копий | Полные архивы 1.88ГБ/1.01ГБ с SHA, покрытие всех файлов/хэши source проверены; git worktree remove без force | PASS |
| GitHub hell, main не изменён | hell опубликована; remote main остался 8789e08d | PASS |
| Отдельная сборка PolyMC | Отдельный Slavic-Myths-2.0.0-HELL, source commit/SHA/backup/duplicate0 в installed.json | PASS (установка; не runtime) |
| Внешний вид/генерация/порталы/старый мир/MP/MSPT | Minecraft/сервер/GameTest не запускались | NOT_RUN |

Общая clean build включает все перечисленные JVM gates. Тесты исторических моделей с отсутствующим release/1.7.9 baseline не объявлены пройденными. Проверка HELL поддерживает явный combined-scope флаг: разрешены только добавочные Navya tags из asset manifest; параметры рельефа/лавы/руд проверены независимо. Navya preservation сравнивает локализации с зафиксированным source поиска 1ea15f8a, поэтому не требует отката согласованных текстов Яги; профиль исходного HELL сохранён с учётом платформенных переводов строк.

Логи, включая неудачные проверки старого scope: D:/slavic-myths-maintenance/20261025/consolidation/. Игровые запуски 0, Issues не закрываются, owner-accepted/«Готово» не ставятся. Старые отчёты остаются историей; текущая установка описывается installed.json после её выполнения.

## Фактическая установка и очистка

- В PolyMC создан отдельный инстанс **Slavic Myths 2.0.0 - HELL - TEST**; Minecraft1.21.1, NeoForge21.1.255, Java21. Имя/pack/component versions записаны в instance.cfg/mmc-pack.json.
- Установленный JAR: `C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-2.0.0-HELL/.minecraft/mods/slavicmyths-2.0.0-HELL.jar`.
- SHA256 `b7373eeeecf6919fb585801ff5bd4b81fcd6476632f0198027278e9abfde3554`, версия `2.0.0-HELL`, source commit `db3ee6cbbc59f2c0585ed7b44551608acc280853` (исходники объединены в ae9ebdf3; следующие коммиты меняют только checks/отчёты). [Полная квитанция](installed.json).
- Исходный инстанс, JAR1.9.7.66/SHA1fa945a2…8704f28 и миры не изменены. Backup старого JAR, instance.cfg и mmc-pack.json: `D:/slavic-myths-maintenance/20261025/consolidation/polymc-backup`. Другие моды сверены SHA; в новом mods один slavicmyths JAR, дубликатов0. Миры не переносились и не перезаписывались.
- Ветку hell отправлено на GitHub обычным push, без force и PR/main merge. Remote main до/после одинаковый: `8789e08dbcb029ae7b252049dec62c47c7780cce`.
- Удалены лишние рабочие папки D:/slavic-myths-hell и D:/slavic-myths-advancements-v3. Перед удалением проверены полный состав архивов (30831/19679 файлов), размеры и SHA исходников/tools/локальных input. Локальные коммиты и refs сохранены. Каноническая папка только D:/slavic-myths, ветка hell.
- Проверки упаковки прошли на том же SHA, который установлен. [Лог общей clean build](clean-build.txt), [ресурсы](resources.txt), [HELL](hell-resources.txt), [Навья роща](navya-resources.txt).

Это исполнение объединения/публикации/установки тестового кандидата. Игровая готовность не заявлена: генерация в реальном мире, старые чанки/порталы, GUI, natural spawn, сохранение/перезаход, несколько игроков и MSPT остаются NOT_RUN. Minecraft/PolyMC/GameTest/сервер агентом не запускались. Issues остаются открытыми до приёмки владельца.
