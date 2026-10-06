# Slavic Myths 0.9.7 — Minecraft 1.21.1 / NeoForge

Существующий контент перенесён на NeoForge **21.1.255**, Java **21**.
Полный clean build и loader/datapack/headless checks проходят. Клиент автоматически не запускался;
готовность к ручному тесту не означает, что игровые сценарии уже проверены.

Текущий structure-priority JAR проверен и установлен в тестовый профиль PolyMC; предыдущий dev JAR сохранён вне mods. Подтверждены 10 сценариев на тонком и 10 на высоком superflat, шесть команд из одной точки и 27 общих headless-тестов. Полное естественное покрытие обычного мира пока не принято.

## Ручная генерация 0.9.9-dev

Используйте `/slavicmyths generate kurgan small|warrior|great` или `/slavicmyths generate bandit_camp small|fortified|large` (выберите одно значение после последнего пробела). Каждая команда создаёт задание; дождитесь сообщения с координатами входа перед следующей. `/slavicmyths generation status` показывает этап, `/slavicmyths generation cancel` останавливает работу. При отмене уже изменённые блоки остаются.

На тонком superflat курган поднимается вместе с подземными этажами и получает грунтовый массив с подходом. Суша выравнивается; вода и защищённые постройки вызывают локальный перенос до 256 блоков. Ручная генерация работает и при отключённых естественных структурах мира. Ограничения и проверки: [structure priority override](docs/worldgen/STRUCTURE_PRIORITY_OVERRIDE_0.9.9.md).

## Разработка 0.9.9

Текущий промежуточный JAR: `build/libs/slavicmyths-0.9.9-dev.jar`. Это незавершённая 0.9.9: проверка покрытия обычного мира, компенсация отклонённых мест и часть аудита мобов ещё не завершены. По прямому запросу пользователя в тестовый профиль PolyMC установлен `0.9.9-dev` для проверки генерации; прежний JAR 0.9.7 сохранён вне `mods`. Это промежуточная установка, не принятие релиза. Статус и ограничения проверок: [аудит генерации](docs/worldgen/STRUCTURE_COVERAGE_AUDIT_0.9.9.md). Minecraft автоматически не запускается.

## Болото и runtime hotfix 0.9.7

Семь семейств мест, 21 вариант: рыбацкие избушки, вышки, святилища, заброшенные дома, настилы, стоянки и редкий кластер. Только vanilla swamp, без нового биома. Новые четыре силуэта деревьев/детали берегов, болотник и более сильные новые болотные водные духи, распределённые полезные награды. Существующие ID/старые templates и остальной контент сохранены. Новых руд/строительных блоков не добавлено.

Исправлены Xaero cleanup после закрытия сессии, пустой JEI catalyst recipeTypes и linden_door models после порта. Пользовательские Xaero waypoint не удаляются. Клиент не запускался; реальный logout/JEI GUI требуют ручной проверки.

[Постройки и частота](docs/swamp/SWAMP_0.9.7_STRUCTURES.md), [мобы](docs/swamp/SWAMP_0.9.7_MOBS.md), [деревья](docs/swamp/SWAMP_0.9.7_TREES_AND_NATURE.md), [материалы](docs/swamp/SWAMP_0.9.7_BLOCKS_AND_ORES.md), [loot](docs/swamp/SWAMP_0.9.7_LOOT.md), [hotfix и границы проверок](docs/swamp/SWAMP_0.9.7_RUNTIME_FIXES.md), [ручная QA](docs/MANUAL_QA_0.9.7_SWAMP.md).

```text
/locate biome minecraft:swamp
/locate structure slavicmyths:fishing_camp
/locate structure slavicmyths:swamp_watchtower
/locate structure slavicmyths:flooded_shrine
/locate structure slavicmyths:abandoned_settlement
/give @s slavicmyths:bolotnik_spawn_egg
```

Полный список остальных structure IDs и locate/dev команд — в документации построек и QA. Проверки Java 21: `gradlew.bat clean build verifyKurganRework verifyPortCore verifyNavigation verifyPortComponents verifyPortPayloads verifyPortGeometry`; loader: `gradlew.bat -PportRuntimeCheck -PwithoutJei runRuntimeChecks` и `gradlew.bat -PportRuntimeCheck -PportRuntimeCompanions runRuntimeChecks`; ресурсы: `python tools/verify_static_data_1211.py --swamp-rework`, `python tools/verify_swamp_097.py`. Офлайн hotfix: `python tools/verify_xaero_lifecycle_097.py`, `python tools/verify_jei_ritual_contract_097.py`.

Остаются 16 старых missing parents моделей других дверей, список в [resource report](docs/verification/swamp-0.9.7/resource-check.json). Новые модели/ссылки валидны.

## Курганы 0.9.6

Новый graph-first план v2: 14 типов помещений, боковые ветви/циклы/секреты, разнесённые лестницы, две палитры кладки и собственная архитектура комнат. Старые конкретные планы v1 сохраняются. Добавлены 26 строительных блоков, 60 рецептов, семь таблиц распределённых наград и 35 авторских текстур 256×256; прежние предметы и их характеристики сохранены.

Команды OP 2 (пример для малого, доступны также `warrior` и `great`):

```text
/slavicmyths kurgan locate small
/slavicmyths kurgan generate small 0
/slavicmyths kurgan info
/slavicmyths dev clear_kurgan_curse
/locate structure slavicmyths:kurgan_small
/locate structure slavicmyths:kurgan_warrior
/locate structure slavicmyths:kurgan_great
```

Seed после generate необязателен; clear_kurgan_curse допускает аргумент player. Тонкий стандартный superflat не имеет нужной глубины: используйте обычный мир либо толстый каменный flat. Невалидная геометрия отвергается до размещения.

Текущий production: `build/libs/slavicmyths-0.9.7.jar`. Clean build, 150 новых планов и обе финальные серии по 14 NeoForge GameTests проходят. Регрессии старого планировщика/населения и навигации проходят. Клиент не запускался; визуальная QA остаётся ручной. Аудит 0.9.6 выявил 20 прежних missing door parents; четыре липовых исправлены в 0.9.7, остальные 16 записаны в backlog. Новые ресурсы курганов валидны.

[Алгоритм](docs/kurgan/KURGAN_LAYOUT_0.9.6.md), [комнаты](docs/kurgan/KURGAN_ROOMS_0.9.6.md), [все ID и рецепты](docs/kurgan/KURGAN_BLOCKS_0.9.6.md), [loot](docs/kurgan/KURGAN_LOOT_0.9.6.md), [аудит иконок](docs/kurgan/KURGAN_ITEM_ART_AUDIT_0.9.6.md), [ручная QA](docs/MANUAL_QA_0.9.6_KURGAN.md), [итоговые gates](docs/verification/kurgan-0.9.6/acceptance.json).

Headless повторная проверка (Java 21):

```text
gradlew.bat clean build verifyKurganRework verifyPortCore verifyNavigation verifyPortComponents verifyPortPayloads verifyPortGeometry
gradlew.bat -PportRuntimeCheck -PwithoutJei runRuntimeChecks
gradlew.bat -PportRuntimeCheck -PportRuntimeCompanions runRuntimeChecks
python tools/verify_static_data_1211.py --swamp-rework
python tools/verify_swamp_097.py
```

## Навигация 0.9.5

`/slavicmyths navigation` работает без OP. Назначить клавишу «Навигация» в настройках управления.
Восемь категорий, список/фильтры, одна отслеживаемая цель; скрытие метки сохраняет её данные.
Без Xaero работает компактный HUD. С Xaero выбрать группу **Slavic Myths** вручную.
Зона поиска отображается приблизительным центром и радиусом; круговой overlay отсутствует.
Точные координаты босса открываются только после discovery, в виде временной метки.

Клубок сохраняет метку Яги только после фактического размещения дома. Путевой камень
сохраняет личную метку при активации и сохраняет прежнее RPG menu; телепортация не добавлена.
Крупные структуры сохраняются при обнаружении; малые — кнопкой «Сохранить место рядом».
Контракт без известного реального encounter принимается, но зона ждёт появления подходящей цели.

Диагностика (OP 2):

```text
/slavicmyths dev navigation list
/slavicmyths dev navigation marker <UUID>
/slavicmyths dev navigation searcharea create-test mini
/slavicmyths dev navigation searcharea create-test rare
/slavicmyths dev navigation searcharea create-test world
/slavicmyths dev navigation searcharea create-test unique
/slavicmyths dev navigation searcharea stats
/slavicmyths dev navigation clear-owned
```

`create-test` не создаёт encounter; `clear-owned` очищает навигацию только данного игрока.
[Подробное устройство и ограничения](docs/NAVIGATION_0.9.5.md),
[28 ручных сценариев](docs/MANUAL_QA_0.9.5_NAVIGATION.md),
[сборка/loader evidence](docs/verification/navigation-0.9.5/acceptance.json).

## Установка и готовая тестовая сборка

Production JAR: `build/libs/slavicmyths-0.9.7.jar`.
SHA-256: `afd6995331674de8035338839813f66d8221f3982360b93b3ee3a678582a7b6c`.

Проверенный JAR 0.9.7 установлен, версия 0.9.6 сохранена в `.slavicmyths-backups/0.9.6/`; [receipt](docs/verification/swamp-0.9.7/installation-receipt.json).
В установленном PolyMC уже создан отдельный профиль **Slavic-Myths-1.21.1-Testing**:
`C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing`. Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21.0.12, память 512–4096 MiB.
Открыть PolyMC вручную, выбрать этот профиль и запускать на **новом тестовом мире**.
При первом запуске launcher загрузит недостающие libraries. Старый профиль и миры 1.16.5 сохранены отдельно.

Обязательная core dependency: **Curios 9.5.1+1.21.1**. JEI опционален для core.
Установленный test pack также содержит JEI **19.51.0.418**, Jade **15.10.6**,
Xaero Minimap **26.5.0**, Xaero World Map **1.45.0**, FallingTree **1.21.1.11**.
В папке mods ровно семь top-level JAR. Не добавлять самостоятельный XaeroLib поверх embedded зависимости.
[Lock с точными файлами, SHA-256 и путями](packaging/test-pack-lock.json).

## Команды 1.21.1

Для следующих dev/locate/generate команд требуются cheats/OP level 2; generate выполняется игроком в Overworld.
Регистрация команд проходит на headless server; фактические игровые результаты проверять вручную.

```text
/slavicmyths dev yaga locate
/slavicmyths dev yaga generate
/slavicmyths dev yaga status
/give @s slavicmyths:baba_yaga_spawn_egg
/slavicmyths kurgan locate small
/slavicmyths kurgan locate warrior
/slavicmyths kurgan locate great
/slavicmyths kurgan generate small
/slavicmyths kurgan generate warrior
/slavicmyths kurgan generate great
/slavicmyths kurgan info
/slavicmyths dev clear_kurgan_curse
/slavicmyths dev clear_kurgan_curse PlayerName
/locate structure slavicmyths:kurgan_small
/locate structure slavicmyths:kurgan_warrior
/locate structure slavicmyths:kurgan_great
```

`yaga locate` различает запланированный X/Z и реально размещённый дом, не обещает координату Y=0.
`yaga generate` пробует ограниченное число площадок вокруг игрока, не дублирует существующий дом,
откатывает неудачное размещение. Исходный 0.9.3 fix сохранён и дополнен отрицательными высотами мира 1.21.1.
Размещение/отказы на Y=-60/-1/0/3/4/63 проверены headless; natural generation остаётся ручной QA.

## Сборка и проверки без клиента

Из `D:/slavic-myths`, PowerShell:

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
python tools/fetch_xaero_api_095.py
.\gradlew.bat clean build verifyNavigation verifyPortCore verifyPortComponents verifyPortPayloads verifyPortGeometry --console=plain
.\gradlew.bat runRuntimeChecks -PportRuntimeCheck -PwithoutJei --console=plain
.\gradlew.bat runRuntimeChecks -PportRuntimeCheck -PportRuntimeCompanions --console=plain
python tools/verify_static_data_1211.py --navigation
python tools/verify_port_1211.py --navigation
python tools/verify_polymc_094.py --navigation
git diff --check
```

`runRuntimeChecks` запускает только отдельный headless GameTest server в `run-port-checks/`,
загружает реальный NeoForge/mod/datapack, выполняет семь tests (четыре port + три navigation) и завершает сервер.
Это не `runClient`, не GUI PolyMC, не запуск сохранённого пользовательского мира.
Gradle aliases `verifyYaga verifyYagaPlacement verifyPortRecipes verifyPortDamage`
автоматически используют этот же loader; несколько aliases в одном вызове дают один server run.
Тестовые классы/resources и companion classes не попадают в production JAR.
Старые source audit/resource generators для 1.16.5 нельзя использовать для перезаписи target resources.

## Сохранённый контент и ограничения

385 items, 153 blocks, 47 entities; все существующие Slavic registry IDs сохранены.
213 recipes, 227 loot tables, 154 advancements действительно загружаются.
Curios slots сохранены; JEI содержит четыре прежние категории.
Для ItemStack используются persistent Data Components с миграцией прежнего custom NBT.
Сохранены охота, боссы, курганы, бандиты, водные/болотные механики, еда,
метла/ступа, мебель, Яга/избушка, уникальные screens/HUD и RPG.
Удалён только общий target HP overlay/config/packet, заменяемый Jade; NPC mace push и boss bars остаются.
547 исходных бинарных art/sound/template assets побайтно не менялись.

[Исторический PORT_STATUS 0.9.4 и доказательства](docs/port/PORT_STATUS_0.9.4.md),
[parity всех исходных IDs](docs/port/REGISTRY_PARITY_0.9.4.md),
[QoL dedup](docs/port/QOL_DEDUP_0.9.4.md),
[payload boundaries](docs/port/NETWORK_PAYLOADS_0.9.4.md),
[ручной checklist](docs/MANUAL_QA_0.9.4_1.21.1.md).
AI/бой/анимации/JEI screens/полёт/реальный save-rejoin/генерация требуют ручного игрового теста.
Не заявляется прямая совместимость миров Forge 1.16.5; не открывать оригинальные старые saves новой версией.
Старый подробный контент и команды исходной платформы сохранены в
[архивном README 1.16.5](docs/README_LEGACY_1.16.5.md); это история, не команды текущего порта.

## Исправление первого запуска PolyMC

Для NeoForge 21.1.255 исправлены устаревшие launcher checksums installer/universal в локальном patch нового профиля; проверенные JAR уже установлены. При ошибке Unable to detect the forge installer полностью закрыть и открыть PolyMC для перечитывания patch. Доказательства и подробности: docs/port/launcher-repair-0.9.4.json. Minecraft автоматически не запускался.
