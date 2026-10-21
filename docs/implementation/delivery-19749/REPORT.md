# 1.9.7.49 — установленная тестовая сборка

По прямому поручению владельца опубликован текущий рабочий snapshot, включая ранее существовавшие изменения брони/рун, трофеи #110/#111 и шесть семейств дерева #106/#107. Это IN_PROGRESS, не финальный релиз; #108/#109 и игровая приёмка не объявлены завершёнными. Issue не закрывались.

Source commit: `95f531046864987a070b96043de4f6934effa8b9`.

Installed JAR: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.49.jar`.

SHA256: `4c3d07526110edc8a7b52f020418b1336f1d7b4bc538b6c10d60173f8a6ef70a`.

Backup: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\codex-backups\before-1.9.7.49-20261021-101152\slavicmyths-1.9.7.48.jar`; SHA256 `0a51075d9b498632f46cad0d970e81cc67bad1425eee70494eb98e31ff8e5917`.

| REQUIREMENT | EVIDENCE | RESULT |
|---|---|---|
| Java21 clean build | build-delivery49.log, BUILD SUCCESSFUL 45s | PASS |
| Production PNG/JSON/JAR parity | 7444 ресурсов, installed.json | PASS |
| Art reproducibility | 68 wood PNG + #110/#111 canonical outputs | PASS |
| PolyMC MC1.21.1/NeoForge21.1.255 installation | installed.json, exact SHA256 | PASS |
| Backup / no duplicates / other mods unchanged | installed.json | PASS |
| Runtime / owner in-game acceptance | 0 запусков, игру не запускали | NOT_RUN |

Проверить в игре: новые трофеи в инвентаре/руке; все шесть пород рядом; брёвна по осям, обтёсанные брёвна, доски, лестницы/плиты/ограды; двери и люки в открытом/закрытом состоянии; надписи и свечение табличек, подвесная яблоневая табличка; мебель с материалами липы/сосны; сохранение и повторный вход. Независимые текстуры бытовых предметов и курган V2 этим пакетом не перерабатывались.
