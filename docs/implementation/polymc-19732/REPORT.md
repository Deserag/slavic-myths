# PolyMC — установлен тестовый preview 1.9.7.32

Установлено по прямому запросу владельца. Это тестовая сборка #78 + #79, не финальная приёмка цепочки; обе задачи OWNER_PENDING. #80 не меняет production: IDENTIFICATION_BLOCKED.

Installed JAR: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.32.jar`

SHA256: `4614b9ef67280fa7433c84b2ebedacb17341385939a67fd58bac7ce37ce98947`

Backup: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\codex-backups\before-1.9.7.32-20261020-153628\slavicmyths-1.9.7.30.jar`

Backup SHA256: `c2dd2a3eb148ce88fa3bc7cd59964dd314f9dfb0642e2dc4d3ff5eca943e2375`

Source base: `7e6d64d41b330d675c65cb10ac738f7a1091a9b8`. Исходники #78/#79 НЕ закоммичены, отдельного source commit этой сборки нет. Source fingerprint: `ce7342586caf3007537f120c0be665909a9054220e90f807e03f3264cc892921`; полный manifest в `../creatures-79/build-source-sha256.json`.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Проверенные Java21 clean build/resources/production JAR | ../creatures-79/BUILD.log, jar-check.json | PASS offline |
| Source соответствует собранному manifest | SHA256 всех production inputs перед установкой | PASS |
| Backup предыдущей 1.9.7.30 | Путь/SHA выше, проверены до удаления старого active JAR | PASS |
| Реальная установка 1.9.7.32 | installed.json, installed bytes совпадают с build JAR | PASS |
| Нет двух Slavic JAR, прочие моды сохранены | один active JAR, SHA шести других модов не менялся | PASS |
| Runtime и приёмка #78/#79 | Агент не запускал Minecraft/PolyMC/сервер/GameTest, запусков 0 | NOT_RUN / OWNER_PENDING |

Изменения: #78 — конечное время помощи и восстановления богатырей, безопасный призыв/отзыв и HUD; #79 — природа/intent/принадлежность, физиология крови/POISON/огня, общая проверка целей и локальная защита Чуди/деревни. Полные условия и runtime ограничения — в матрицах соответствующих пакетов.
