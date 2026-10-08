# Classes UI 1.3.2.3

## Изменения

- `client/ClassScreen.java`: prerequisite tree с двумя исходными линиями каждого класса, прокруткой/перетаскиванием, 44px узлами и 52px корнем. Locked/available/learned/selected/max rank; tooltip и отдельная подробная карточка. Точные причины блокировки также доступны на кнопке изучения. 3 active + 1 passive, ПКМ снимает навык; trigger/evolution не назначаются в слот.
- Выбор класса: три герба/названия/роли, собственный игрок в 3D, прокручиваемые описание/сильные стороны/стартовый навык, видимые требования, компактная кнопка и модальное подтверждение. Предметы preview восстанавливаются в finally; скин и броня не меняются.
- Управление через алтарь меняет те же `ClassClient.ACTIVE` KeyMapping, которые зарегистрированы в Minecraft. Клавиатура/мышь, Esc отменяет захват, конфликты показывают названия действий; `options.save()` сохраняет options.txt. Пассивному навыку клавиша не назначается.
- HUD: три активных иконки и подписи реальных клавиш, маска/секунды cooldown, 8-tick flash готовности; пассивная иконка меньше и без клавиши. При узком GUI панель поднимается над hotbar/vanilla indicators. Единственный источник видимости — `ClassHudConfig.SHOW` в `slavicmyths-client.toml` (default true), доступен Mods → Slavic Myths → Config. Скрытие HUD не отключает клавиши/способности.
- `ClassState` и новый `ClassLearningRules`: серверный Creative bypass уровней/prerequisites/branch/очков при ручном изучении навыков своего класса, очки не списываются. Чужой класс и max rank запрещены. Branch choice bypass только уровня, родитель и одноразовость сохраняются. Возврат в Survival использует обычные проверки; уже изученное не удаляется. Автоматического изучения нет.
- `ClassTheme`, `ClassTreeLayout`, `ClassSettingsScreen`, `ClientSetup`, `SlavicMyths`, `gradle.properties`; 34 исходные 32×32 иконки, три 64×64 герба, девять GUI sprites и RU/EN. UI overlay подключён к `class_resources_131.py`/`integration_polish_resources.py`. [Лист иконок](media/class-icons-1323.png).

Баланс и ClassDefinitions, ClassRuntime, ClassEvents, ClassBalance, ClassNetwork не изменены этой итерацией. Существующие NBT/registry IDs и lifecycle сохранения не менялись. Редкое оружие предыдущей 1.3.2.2 включено в новый JAR.

## Проверки

Финальный `gradlew.bat clean build` PASS, Java 21 / NeoForge 21.1.255. 14 существующих deprecated/unchecked предупреждений; лог `.tools/classes-ui-build.log`. 3 710 офлайн-проверок (все 34 навыка: Creative, стоимость, identity/max cap и Survival gate order; три дерева: полнота, реальные prerequisite edges, глубина, отсутствие пересечений узлов/рангов; шесть resolution/scale pairs). 530 ресурсных/packaging gates: размеры/прозрачность/различимость изображений, обязательные GUI sprites, RU/EN ключи, source/JAR соответствие, воспроизводимость overlay, NeoForge metadata и отсутствие тестовых классов. `tools/class-ui-tests` никогда не входит в production.

Офлайн-геометрия подтверждает границы элементов, но не заменяет визуальную проверку Minecraft font/рендерера. Запуски текущей итерации: клиент 0, сервер 0. Реальные смерть/respawn/relog/server restart/cooldown persistence и игровая работа keybinds/config не проверялись. Не заявляется измеренный FPS/MSPT.

## Краткая ручная приёмка (не выполнена)

1. Выбор трёх классов и подтверждение, RU; дерево/карточка/loadout при 1920×1080/2560×1440 и GUI scale 2/3/4, без blur и наложения текста.
2. Изучить/улучшить/назначить/снять; проверить доступность, Survival gates, Creative без очков, возврат в Survival.
3. Назначить клавиатуру/мышь в алтаре, увидеть тот же mapping в обычных настройках, конфликт и сохранение после перезапуска; пассив без клавиши.
4. Cooldown mask/flash, HUD toggle, отсутствие vanilla overlaps; скрытый HUD не отключает способности.
5. В отдельном тестовом мире проверить смерть/respawn/реальный relog и перезапуск сервера: ранги/loadout/развитие/долгие cooldown сохранены.

## Артефакты

Production: `D:\slavic-myths\build\libs\slavicmyths-1.3.2.3.jar` (9600039 bytes). SHA-256 `6fd7546ff241e73a9c4f5d3480174915752fedf9a2686e6c1321a4425dbeabad`.

Установлено: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.3.2.3.jar`. Активен один JAR; 539 остальных файлов сохранены побайтно. Резервная копия: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-1.3.2.3-20261008-134808\slavicmyths-1.3.2.1.jar`. Minecraft не запускался.

[Gates](verification/classes-ui-1.3.2.3.json), [квитанция](verification/polymc-1.3.2.3-installation.json).
