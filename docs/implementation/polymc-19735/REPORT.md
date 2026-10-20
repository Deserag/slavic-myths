# PolyMC / Git — установлен test preview 1.9.7.35

Выполнено по прямому запросу владельца после #83. Источники опубликованы в origin/main, source commit [`eb113d4c67b7ab046cacb413c0022fefb244f970`](https://github.com/Deserag/slavic-myths/commit/eb113d4c67b7ab046cacb413c0022fefb244f970). Это **тестовая сборка**, не owner-acceptance #81/#82/#83 и не финальная сдача всей цепочки.

Installed JAR: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.35.jar`

Version: **1.9.7.35**

SHA256: `6a55ae13ee18bb7b55f70575e1922e591dd8b81b5d27fb916248aeacdf6dd672`

Backup .32: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\codex-backups\before-1.9.7.35-20261020-162430\slavicmyths-1.9.7.32.jar`

Backup SHA256: `4614b9ef67280fa7433c84b2ebedacb17341385939a67fd58bac7ce37ce98947`

Source fingerprint: `31d3ba87a9234441e7e992146516998ed7c6b00f0f974ca2b5f9f2bba6011b5c`; полный byte manifest: `../meat-83/build-source-sha256.json`. Один active slavicmyths JAR; все6 соседних модов сохранены по SHA. Игровых запусков агентом0.

В .35 включены прежние #78 lifecycle богатырей, #79 physiology/relations/Chud, #81 первый пакет M1 (четыре металла), #82 пять модовых древесных саженцев/капуста и #83 пять raw meat PNG. #81 целиком не завершён, остальные #97–101 не исполнялись. #82/#83 требуют ручной визуальной приёмки. #80 отменена, closed/not_planned, физически не удалена (нет connector delete).

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Java21 clean build35 | ../meat-83/BUILD.log,52s | PASS |
| Production JAR/JSON/PNG/classes | ../meat-83/jar-check.json | PASS offline |
| Source байты соответствуют build manifest | проверены перед установкой; source commit выше | PASS |
| GitHub source main обновлён | успешный push source commit | PASS |
| Backup прежней .32 | path/SHA выше, проверен до удаления active .32 | PASS |
| JAR35 действительно установлен | installed.json; SHA active совпадает с build JAR | PASS |
| Дубликатов нет, другие моды не затронуты |1 slavic JAR,6 other SHA unchanged | PASS |
| Minecraft runtime и визуальная приёмка #83 | запусков0; владелец не принял новую версию | NOT_RUN / OWNER_PENDING |

GitHub содержит исходники, evidence и метаданные сборки. Бинарный JAR не добавлялся в Git и отдельный GitHub Release не создавался; существующая политика репозитория хранит release binaries вне source control.
