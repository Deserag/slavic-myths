# PolyMC1.9.7.25 — тестовая установка20.10.2026

INSTALLED_TEST_PREVIEW / OWNER_PENDING. По прямому запросу владельца.

Installed JAR: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.25.jar`

SHA256: `c210f054e5077d33aa85c54922c1a3c81fbbf6c16d8ea7566d16fb8443f5e397`

Source commit: `48ed68246c5073b581c6d9ef2fe6e0ed8e401254` + сохранённые локальные UI/command изменения.
Точный source snapshot: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-19725-20261020-090859\source-snapshot.zip`.
Snapshot SHA256: `d5460f5d7acd4f1164ee7e966310fb909af1c19c084f6684359d88a81036193a`.

Backup23 и63 файлов конфигурации: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-19725-20261020-090859`.
Шесть остальных файлов mods сохранены; один активный slavicmyths JAR25.
Переименован только display name профиля. Миры и старый1.16.5 не изменены.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Проверенные build/resources/classes/source/JAR | Матрица beer-84-85-v2 + повторная byte/hash сверка при установке | PASS |
| Правильный экземпляр1.21.1/NeoForge21.1.255 | mmc-pack, INSTALLATION.json | PASS |
| Backup прежнего JAR/профиля | Копии и SHA verified | PASS |
| Фактический installed25 и отсутствие дублей | Installed SHA и metadata всех активных JAR | PASS |
| Runtime/визуал и owner acceptance | Владелец проверит самостоятельно, запусков0 | NOT_RUN |

Minecraft/PolyMC/GameTest/server не запускались. Issues #84/#85 OPEN.
