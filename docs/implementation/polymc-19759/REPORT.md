# PolyMC — тестовая версия1.9.7.59, 23.10.2026

Прямое поручение владельца: проверить статусы последних задач и обновить мод в PolyMC. #97/#100/#112/#113 фактически «Тестирование», снимок project-status.json; дополнительных изменений статусов не потребовалось. Это тестовый кандидат текущего dirty source, не итоговое завершение #87 или приёмка новых assets.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Статусы четырёх задач | project-status.json | PASS |
| Java21 clean build | build.log:45s | PASS |
| Source/production JAR, Java21/no QA | jar-gates.json:7631resources/1809classes | PASS |
| Сохранены30canonical материалов/трофеев | canonical-pixels.json | PASS |
| Установка/backup/SHA/no duplicate | installed.json; перечитан установленный JAR | PASS |
| Minecraft/PolyMC/GameTest/server runtime | запусков0; проверяет владелец | NOT_RUN / OWNER_ONLY |

Installed: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.59.jar`

Version: `1.9.7.59`

SHA256: `f211c5e5b00d626f1093b6e6ff1dcb7f87aa1051d520b155e939b71b08fd294b`

Backup: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-1.9.7.59-20261023-113053\slavicmyths-1.9.7.58.jar`

Backup SHA256: `5a4a58c61599978bf80ec517ee1642defb20dd60b6f1b1d31e01c7a5af40463e`

Source commit: `1192e229c4aa9166b53661f71f5ca15299f3537e` + незакоммиченные изменения. Точный snapshot: source-manifest.json (SHA256 `18e1150d5ad5792d31cc536cddce3d06bd22e14bf35fc0fb97c1587791ab1305`). Ни commit, ни push не выполнялись. Старый профиль1.16.5 не затрагивался. В mods ровно1JAR modId slavicmyths; остальные моды/миры/настройки сохранены.
