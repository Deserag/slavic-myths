# Slavic Myths 0.9.5 — Minecraft 1.21.1 / NeoForge

Существующий контент перенесён на NeoForge **21.1.255**, Java **21**.
Полный clean build и loader/datapack/headless checks проходят. Клиент автоматически не запускался;
готовность к ручному тесту не означает, что игровые сценарии уже проверены.

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

Production JAR: `build/libs/slavicmyths-0.9.5.jar`.
SHA-256: `66579a054a2ed14dfa3fdff809e43656c9a2621fa18a743177b32ff6d2581b4c`.

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
