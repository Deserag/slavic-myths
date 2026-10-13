# Проверки первого этапа 1.9.8.1

- `gradlew.bat clean build` с JAVA_HOME только в процессе команды: **PASS**, BUILD SUCCESSFUL in 1m 46s. Журнал `.tools/audit-1981-build.log`; compileJava FROM-CACHE, test NO-SOURCE. Это не тест механик.
- Production: `build/libs/slavicmyths-1.9.7.jar`, SHA256 `0f1e76dd6bef952350a67d83da5357cc26bcc8dcb290b7eb281c8e46d2290e0e`. Совпадает с ранее документированным артефактом 1.9.7; runtime не изменился.
- `python tools/verify_resources.py`: **FAIL**, сообщение `FAIL: 224 checks. Port incomplete.` Журнал `.tools/audit-1981-resources.log`. Делегирует историческому verify_port_1211.py: сообщает Mod version и Data semantics changed для advancements относительно прежнего checkpoint. В рамках аналитического задания baseline и ресурсы не перезаписывались.
- Актуальная ограниченная проверка `python tools/check_creature_audit_artifact_1981.py`: **PASS**, 6714 resource files: ZIP CRC, metadata, отсутствие tools/smoke/Checks, наличие ресурсов, побайтное совпадение assets/data с исходниками, JSON syntax. [Квитанция](artifact-checks.json). Это не замена проверки runtime codecs или всей семантики ресурсов.
- verify_winters.py просмотрен, но не запускался: привязан к прежнему offline журналу/установленному 1.9.6, вызывает генератор ресурсов и перезаписывает историческую квитанцию. Ранее пройденные десять offline suites в этой работе повторно не запускались и не заявляются как новые результаты.
- Клиент/сервер/GameTest: **0** запусков. Установка в PolyMC и commit не выполнялись.

Остаётся завершить ручной аудит существ; полный патч 1.9.8.1 пока IN_PROGRESS. Исправление исторического общего validator — отдельная задача, не исправление игрового баланса.
