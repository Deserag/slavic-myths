# Достижения v3 перенесены в актуальный main

Реализовано по поручению владельца: вместо автономных микровех — 75 видимых карточек и четыре связанные ветви: мирная жизнь7, исследование24, сражения25, истории18, плюс общий корень. Родитель объясняет путь, но не запрещает независимое получение дочерней вехи. Книга показывает уже выполненное достижение независимо от незавершённого родителя. Условия и шаги изложены на русском и английском. Боссы и принятые победы получают отдельные вехи через серверные callbacks, а не простое наличие трофея.

Перенесена существовавшая разработка из свободной рабочей копии advancements-v3, а не повторный редизайн. Трёхстороннее объединение от fb2994e9 сохранило текущий код main b85689ee; отдельно разрешены два конфликта. У WorldBoss сохранены текущие trace/target/hurt/боевые изменения и добавлено подтверждение победы. У Fermentation сохранены современный BlockEntity/allowStart API, проверка тары BrewRules.pourCount и тестируемая загрузка напитков; добавлены только provenance/одноразовое достижение варившему игроку. Принятое оружие/модели/рунический эффект не переделывались.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| 75 связанных видимых узлов, четыре ветви без циклов/пропавших родителей | CHECKS.json; ../advancements-v3/TREE.md, TREE.png, TREE.json | PASS |
| Сохранены IDs, criteria/requirements/rewards всех217 прежних definitions,77 recipe unlocks | CHECKS.json; ../advancements-v3/COMPATIBILITY.csv; в текущем main240definitions | PASS |
| Локализация и icons75, source callbacks всех impossible-вех | read-only verify_advancements3_port.py; CHECKS.json | PASS |
| Книга, независимое завершение, отсутствие пересечений layout, зоны/брожение | 40326 pure JVM JourneyChecks +314 HistoricalPolicyChecks | PASS |
| Генератор не откатывает ресурсы | generator.json: semantic drift0, idempotence PASS, actual source не менялся; отличия форматирования записаны | PASS |
| Java21 clean build и production JAR | advancements-port-build.log:1m45s; advancements-port-resources.log | PASS |
| Настоящий сервер, native inventory/кухня/награды хранителей/загрузка240 | advancements-port-runtime-write.log:8/8,37inventory cases | PASS |
| Прогресс между отдельными процессами, нет ложного earth_heart | advancements-port-runtime-read.log:8/8, NEW_PROCESS_LOAD_PASS | PASS |
| Визуальная игровая приёмка вкладки/Книги и полное прохождение каждого босса/истории, MP | Ручная приёмка владельца; layoutPNG является схемой, не игровым скриншотом | NOT_RUN / OWNER_ONLY |

Три реальных процесса GameTest-сервера: первый выявил отсутствующие настройки тестового harness (root/phase) и старый hardcoded217; исправлен только fixture. Два следующих процесса write/read прошли8/8 каждый. Клиент не запускался. Старые runtime0 в скопированных отчётах относятся к прежней изолированной разработке, не к этому переносу.

JAR build/libs/slavicmyths-1.9.7.61.jar SHA256704ca1decb3caf5130df2e2ac5ca99350cc48a3cfd6f23fbde809be2842d51f7,7668resources,1824Java21classes, тестов в production нет. Source b85689ee +DIRTY; source-manifest.json. В PolyMC остаётся установленная1.9.7.60: новую сборку не устанавливали. Commit/push/закрытия/owner-accepted не выполнялись. Это сдача на тестирование, не ручная приёмка или завершение всей кампании.
