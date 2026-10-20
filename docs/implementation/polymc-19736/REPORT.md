# PolyMC 1.9.7.36 — коррекция ванильной формы металлов

Installed JAR `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.36.jar`

Version **1.9.7.36**, SHA256 `de33a23866ab1bb5c7616d488f3eb009550e042f4a856ae484ae23968c096dd7`.

Source commit [`fad9d44d82fb8ec75571e76e08da11c16a37e571`](https://github.com/Deserag/slavic-myths/commit/fad9d44d82fb8ec75571e76e08da11c16a37e571), опубликован в main.

Source fingerprint `a09a2482cb461e2938ac5148bcc682ba8fc72e35dc0b3183a9fa09ce2be544c7`; full manifest ../metals-vanilla-36/build-source-sha256.json.

Backup35 `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\codex-backups\before-1.9.7.36-20261020-164130\slavicmyths-1.9.7.35.jar`, SHA256 `6a55ae13ee18bb7b55f70575e1922e591dd8b81b5d27fb916248aeacdf6dd672`.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Java21 clean build/resources/JAR | ../metals-vanilla-36/BUILD.log и jar-check.json | PASS offline |
| Реальная установка, installed SHA=build SHA | installed.json, путь выше | PASS |
| Backup35 проверен до удаления active old JAR | точный путь/SHA выше | PASS |
| Нет дублей,6других модов сохранены |1active slavic JAR +6unchanged SHA | PASS |
| GitHub main исходники обновлены | source commit выше | PASS |
| Minecraft/owner runtime и визуальная приёмка | агент0запусков | NOT_RUN / OWNER_PENDING |

[Реальное A/B и scope](../metals-vanilla-36/REPORT.md). Новый вид заменяет отвергнутые формы серебра/перунита M1. Все5слитков vanilla iron_ingot layout;2руды vanilla iron_ore, материал только палитра/небольшие жилки. Свойства/рецепты/ID неизменны. Binary GitHub Release не создавался, опубликованы source/evidence/метаданные. Issue/owner-accepted не менялись.
