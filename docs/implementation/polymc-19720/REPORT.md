# PolyMC 1.9.7.20 — INSTALLED_TEST_PREVIEW

По явному запросу владельца установлен тестовый JAR для проверки дерева #75.
Это не финальная поставка и не закрытие Issue. Включены изменения #73/#74 и посоха.

Installed JAR: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.20.jar`

Version: 1.9.7.20. SHA256: `1ce3f5e4ad31f23b7fd15c456438c75140408227b4f6941940a27498a4678203`.

Source base commit: `8730595d09f84386d2556a714d7f8126649f3863` + незакоммиченные изменения #75 и версии.
Точные src/main и gradle.properties сохранены в `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-19720-20261019-212708\source-snapshot.zip`;
SHA256 snapshot: `0e8b5a98ec9b4dd490c7c5df2e6cdb8e9ebd5c772842408d99ef62f1afa77930`. Git diff — working-tree.patch рядом.

Backup: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-19720-20261019-212708`. Прежний 1.9.7.19 сохранён и проверен по SHA256.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Java21 clean build | .tools/polymc-19720-build.log, BUILD SUCCESSFUL 51s | PASS |
| Production resources/classes | 7398 ресурсов, 1706 Java21 классов: побайтовое совпадение с JAR | PASS |
| RU/EN text + packaged language | verify_user_text.py --jar | PASS |
| Установка точного JAR | INSTALLATION.json; source/installed SHA256 совпадают | PASS |
| Backup и один активный JAR | INSTALLATION.json; metadata scan всех mods/*.jar | PASS |
| Точный source | base commit + ZIP исходников и patch в backup | PASS |
| Игровая/визуальная приёмка #75 | Владелец проверит самостоятельно; запусков 0 | NOT_RUN |

Миры, остальные моды и профиль 1.16.5 не изменялись. Minecraft/PolyMC/GameTest/сервер не запускались.
Исходники не коммитились и не публиковались. #75 остаётся OPEN/IN_PROGRESS.
