# #129 — актуальная интеграционная проверка брони

Задача исполнена после подтверждённых статусов «Готово» #122–#128. Художественные пакеты не переделывались: фактические исходники всех 29 ID совпадают с ранее подготовленными A/B. Новый verifier заменяет применение исторических проверок, привязанных к версиям 46/47, к сегодняшнему дереву.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Все семь обязательных предшественников приняты | verification.json → accepted_prerequisites; Project snapshot work/project-after-acceptance.json | PASS |
| 29 ID, слоты, пути, old/new SHA и source A/B | inventory-current.json; ../armor88-full/AUDIT.md и 29 изображений | PASS |
| Свежая нативная геометрия совпадает с фактическими A/B | native-current.json: 232 состояния, Steve/Alex × idle/walk/raised/sneak; полное равенство историческому after-native | PASS |
| UV/атласы и source-проверка копланарных пересечений | verification.json; ../armor88-full/coplanar-intersections.json | PASS |
| 13 аксессуаров и весь production source сохранены этой задачей | source-before.json; verification.json → accessories | PASS |
| Clean build, production JAR, ресурсы и отсутствие QA-классов | clean-build.log (44s); jar-resources.json; verification.json | PASS |
| Игровые ракурсы, смешанные комплекты, стойка, trim/runes/enchants, relog/MP | ../armor88-full/RUNTIME-MATRIX.md; owner-only final runtime | NOT_RUN — только ручная приёмка владельца |

Старая предпосылка «42 строки вкладки armor» больше неверна: текущая вкладка содержит 34 строки; восемь из исключённых аксессуаров находятся в других группах. Все 29 целевых и все 13 исключённых ID присутствуют. Каталог не менялся. Более поздние принятые исправления щита также сохранены: отличие от baseline43 записано, не объявлено ошибкой этой задачи.

Первый вызов Gradle содержал неверно переданный PowerShell аргумент с точкой; сохранён неудачный лог. После исправления quoting clean build и native export прошли. Первый checker остановился на устаревшей предпосылке каталога, исправлена сама проверка по реальному scope; production не изменялся.

Source HEAD 1192e229c4aa9166b53661f71f5ca15299f3537e + DIRTY. Minecraft/PolyMC/сервер/GameTest не запускались: 0. JAR 1.9.7.60 имеет тот же SHA256, что установленный владельцу кандидат; новой установки не было. #129 — «Тестирование», Issue OPEN; #88 не объявляется принятой до финального решения владельца.
