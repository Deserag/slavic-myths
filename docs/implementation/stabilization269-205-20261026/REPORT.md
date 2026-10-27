# #269 / #276 — 2.0.5-HELL, продолжение IN_PROGRESS

> Позднейшее ограничение: один игровой процесс за раз. После исправления подписи кухни установлена2.0.6-HELL; [текущая установка и остаток](../stabilization269-kitchen-20261026/REPORT.md). Ниже история предыдущей серии.

26.10.2026. По прямому поручению владельца работа продолжается самостоятельно по оставшимся пунктам, без нового списка заданий. Только `hell`. Issues/Project не закрывались и не переводились в «Готово», `main` не менялся, commit/push не выполнялись автоматически. Это тестовая сборка незавершённой стабилизации, не принятый релиз.

## Установка PolyMC — выполнена

Версия: **2.0.5-HELL**. Installed JAR:

`C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-2.0.0-HELL/.minecraft/mods/slavicmyths-2.0.5-HELL.jar`

SHA256: **1746acf792f1599f8d36dbc855c88823e7ecd71bf072053cefd01f823dbd55df**.

Backup прежней установленной2.0.4:

`D:/slavic-myths-backups/stabilization269-205-20261026T180601Z/slavicmyths-2.0.4-HELL.jar`

Backup SHA256: **1c22bf186ab6886fee8a464fb44b123620354ef93ee0f990d1208660d3743010**.

Source commit: `61cb344eeaf3a31db098052800a5e8ab710568de` **+ незакоммиченный production diff**, сохранённый в [source-production.patch](source-production.patch), SHA256 **4e9681b1fe0105cd276d1d80b483b089fe05237445e5d2b7c52b008b89e6d12c**. HEAD сам по себе не содержит всех установленных исправлений. Полная квитанция — [installation-receipt.json](installation-receipt.json).

Установлен один Slavic Myths JAR, pending-файлов/дублей нет. Launcher и личный игровой профиль установкой не запускались; миры/конфигурация не изменялись. Тестовые игровые процессы используются отдельно в `work/stabilization269-*`. Для будущих видимых клиентских тестов сохраняется volume0.3.

От предыдущего **протестированного developer snapshot2.0.4** с SHA636ab1c… (не от прежнего установленного1c22bf…) новый JAR отличается только `META-INF/MANIFEST.MF` и `META-INF/neoforge.mods.toml`: [packaging-diff.json](packaging-diff.json). Все1950 классов, игровые ресурсы, loot и audio побайтно сохранены. Установленная2.0.5 включает ранее проверенные локальные исправления Сивки, дома Яги, locate, ресурсов и collision frequency; новых механик/баланса в этом этапе нет.

Серия завершена:9 реальных dedicated-server процессов, включая один неуспешный QA version gate; клиентов и GameTest0. Итоговые467 проверок отдельных операций PASS, в том числе32 native ritual/restart. Это не полная приёмка мода. Все игровые процессы серии завершены. [Машинная сводка](summary.json), [SHA и копии логов](log-evidence-index.json).

## Матрица

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Java21 clean build | `clean-build.log.txt`:55s, Java21, NeoForge21.1.255 | PASS |
| Ресурсы/production JAR | `resources.log.txt`:8031resources,4432JSON,1443PNG,1886models,1950Java21classes | PASS integrity, не gameplay |
| Настоящий новый production JAR | `jar-regression/restart-results.json`:mod_version2.0.5-HELL, actual code origin из JAR,415 проверок с `result=PASS`; class SHA совпадает с JAR | PASS save/anti-refill/сохранённые ritual и Depths receipts; не полный Survival |
| Backup/установка/нет дублей | `installation-receipt.json`, SHA до/после и backup, единственный mod JAR | PASS |
| КурганIII естественно доступен | `natural-seed12345-results.json`:native FULL-чанки, seed12345, старт−843/807,99 footprint-чанков, main route1, настоящий locate без job/record | PASS один реальный старт; встречаемость не измерена |
| Святилище огня естественно доступно | `natural-shrines-seed-minus269-results.json`:seed−269, старт98/−230,25 footprint-чанков,main1, настоящий locate без стройки;5 PASS | PASS один native старт; полный ritual Survival отдельно NOT_RUN |
| Обитель чистого ключа естественно доступна | `natural-spring-seed8675309-results.json`:seed8675309, старт121/−602,42 footprint-чанка,main1, настоящий locate без стройки;5 PASS | PASS один native старт; не оценка частоты встреч |
| Ритуалы в двух настоящих native-стартах | `native-ritual-minus269-results.json`11 PASS, `native-ritual-8675309-results.json`13 PASS: закрытый тайник, неверный предмет, расход уголька/двух порошков, открытие/реальный loot/невозможность reroll вторым ServerPlayer | PASS native server methods; supplied reagents/dummy connections, не прохождение без OP и не network MP |
| Новый процесс после каждого природного ритуала | `native-ritual-restart-minus269-results.json`4 + `native-ritual-restart-8675309-results.json`4: unlock сохраняется, пустой тайник остаётся пустым и не reroll | PASS два отдельных настоящих restart; не NBT-only roundtrip |
| Приёмка всего мода/Survival/всех AI/MP | Остаток [общего отчёта](../stabilization269-20261026/REPORT.md) | NOT_RUN в полном объёме |

Все игровые прогоны здесь — серверы Minecraft/NeoForge, не GameTest. Native-проверки не вызывают dev create/feature.place и не меняют spawn/loot/terrain settings. Выборка намеренно смещена к редким вариантам: native placement predicate, первый weighted выбор, биом, затем для seed−269 грубая сетка3×3 высот. Грубый terrain forecast не ставит блоков и не доказывает принятие; PASS выставляется только после настоящих FULL-чанков, valid start и всех footprint-чанков. Это не оценка вероятности встречи. Первый great-first кандидат seed12345 дал допустимый ванильный fallback к кургануI; засчитан I, а не III.

В первой попытке JAR-стенда NeoForge отказал из-за старого QA-only диапазона `[2.0.4-HELL]`. Исправлены только QA metadata и проверка имени versioned JAR. Лог неуспеха сохранён как `jar-regression-before-qa-version-fix.log.txt`; повтор415 PASS. Установщик сначала остановился до файловых изменений из-за неправильного имени поля результата (`status` вместо `result`); это исправлено, установка выполнена лишь после проверки всех415 реальных результатов.

## Как проверить в Minecraft

1. PolyMC → профиль `Slavic-Myths-2.0.0-HELL`: убедиться, что версия мода2.0.5-HELL и один JAR. Новый тестовый мир или копия; исходное личное сохранение не перезаписывать. Громкость30%.
2. Без OP: пройти кормление Сивки яблоко → пшеница → морковь → черника → черничные блины → бутылочка мёда → золотое яблоко, минимум24 активных тика между кормлениями. Без седла Space должен прыгать через блок, W+Sprint+Space — быстро взлетать; A/D смещают вбок, камера задаёт направление, отпускание W тормозит, X снижает и сажает. Негативные случаи: потолок, узкий проход, чужой игрок. Оценить положение ног в повороте/полёте.
3. Выйти верхом в полёте → войти → полностью перезапустить игру. Ожидаются прежний конь/владелец/управление без двойника. В MP другой игрок не должен управлять чужим конём; проверить одновременное открытие награды.
4. Обычным исследованием найти курган/лагерь/святилище. Проверить тайник до ритуала → выполнить настоящий ритуал → забрать лут → выйти и перезапустить. Награда не должна повторяться, замок до ритуала не должен обходиться. Этот полный путь пока не заменён автоматическим save-тестом.
5. OP-диагностика отдельно: `/locate structure slavicmyths:kurgan_great`, `/locate structure slavicmyths:sanctuary_unfading_fire`, `/locate structure slavicmyths:abode_pure_spring`. Поиск существующих загруженных стартов не должен создавать постройку. Святилище огня: QA-мир `work/stabilization269-resume-natural2/stabilization269-test`, seed−269, X1568…1647/Z−3680…−3605. Обитель: `work/stabilization269-resume-natural3/stabilization269-test`, seed8675309, X1947…2042/Z−9643…−9558, Y48…74. КурганIII: **только** QA-мир `work/stabilization269-resume-natural1/stabilization269-test`, seed12345, bboxX−13578…−13446/Z12856…13016, Y2…78. В двух указанных QA-мирах ритуалы уже выполнены и тайники намеренно опустошены этой интеграционной проверкой. Для сценария «до ритуала» использовать новый тестовый мир/отдельную исходную копию, не удалять чанки. Центр bbox не является безопасной точкой teleport. Не удалять личные чанки ради воспроизведения.

## Не сделано, риски/откат и следующая работа

Полный Survival, все AI/редкие встречи/бои/экономика и исходный GPU hang #299 не проверены полностью. В предыдущем MP-стенде был Connection reset при принудительном завершении хоста; это не исправленная production-сеть. Архивные отчёты2.0.4 описывают состояние установки на своё время; актуальная установка указана здесь.

Сохранённые IDs/NBT/loot не меняются. Исправления planner влияют на новые чанки; существующие структуры не перестраиваются. Откат: закрыть игру, убрать новый JAR и вернуть проверенный backup2.0.4 в тот же mods, оставив ровно один Slavic Myths JAR. Сохранять миры: откат JAR не отменяет уже созданные чанки. Автоматическое удаление/миграция миров не выполнялись.

Естественные старты всех трёх ранее ненайденных типов теперь подтверждены по одному случаю; встречаемость по этому смещённому отбору не рассчитывается. Следующая работа остаётся в текущем scope: непройденные integration/Survival/AI и сетевые ритуалы. Новых поручений для продолжения этого списка не требуется. Issues OPEN; решение о готовности остаётся за владельцем.
