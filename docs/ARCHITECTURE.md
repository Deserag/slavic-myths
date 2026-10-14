## Коса по образцу Карачуна — 2026-10-14

Адаптированы силуэт/палитры босса, закрытая OBJ, native first-person хват и двуручная активная поза без замены PlayerRenderer. clean build/40 offline/37 server assertions PASS;5 реальных клиентов, финальные15 игровых кадров. [Актуальная ревизия](weapon-stage-a/boss-adaptation.md). PR№2 только оружие; сводная сборка отдельной веткой, main не слит. Независимая и авторская приёмка ожидаются.

## Этап A оружейного патча — коса Карачуна (2026-10-14)

В отдельной ветке переработаны только коса, её материалы/иконка/held transforms и непрерывная временная поза; баланс12HP и сохранённые способности проверены реальным GameTest-сервером. Финальные37 сценарных проверок/clean build PASS; всего8 серверов (3 PASS,5 ошибки стенда),0 клиентов. Software previews не являются игровыми кадрами; авторская и независимая приёмка PENDING. PolyMC не обновлялся, версия1.9.7 сохранена. Этап B не начат. [Материалы и границы](weapon-stage-a/README.md).

## Патч оружия — визуальная ревизия брони (2026-10-13)

Runtime changes только в4 существующих Dist.CLIENT armor model implementations: выделенные UV islands, солнечная застёжка, передние драконьи вставки, приталенный robe/split pointed tails и широкий воинский стяг. Generated item models используются для3 прежних cuboid items. armor_revision_assets — детерминированный финальный overlay, legacy hooks сохраняют weapon overlay. Никаких common/server зависимостей от клиента или новых gameplay систем. Native bake/UV fixture и software projection — tools-only, исключены из production. [Аудит/проверки](armor-revision/REPORT.md).

## Патч оружия — поверх 1.9.7, 2026-10-13

Исправлены фильтр целей/учёт боевых атрибутов косы Карачуна и сброс её позы; компактная объёмная коса/16px иконка, разные модели копья победителя, кистеня атамана, посоха Лихо и Сабли Воеводы. Арбалеты: безопасный pull predicate и компактные first-person transforms. Registry ID и номер версии сохранены. Clean build + Night offline PASS; 15764 resource gates, 921 item models, 208 subscribers PASS. Игровых запусков 0; визуальная/боевая приёмка OPEN. PolyMC Testing обновлён с backup, имя дополнено «Патч оружия». Параллельные правки брони/аудита сохранены отдельно. [Отчёт](verification/weapon-patch/REPORT.md).

Серверный delayed swing использует CombatTargeting.canAttack с NPC/PvP/ally guards и реальные ATTACK_DAMAGE атрибуты для удерживаемой косы; projectile lanes сохраняют собственную базу. Existing Pose duration=0 отменяет клиентское Swing состояние без нового протокола; vanilla first-person renderer, временная third-person pose. Quest185 context renderer выбирает held geometry для ground/fixed косы. Resource overlay tools/weapon_patch_assets.py воспроизводит изменения после owning generators; opt-in native Night checks не входят в JAR. Нет нового глобального tick polling или registry entries.

## 1.9.8.1 — начат аналитический аудит существ (2026-10-13)

Статус IN_PROGRESS: исходный индекс 111 деклараций EntityType, выдержки кода и проверенные расхождения HP; полная ручная таблица атак/форм/типов/спавна ещё не завершена. Production остаётся 1.9.7; баланс, механики, ресурсы и ID не менялись. Следующие патчи не начаты. Генератор tools/creature_audit_1981.py работает только с документацией; новой runtime архитектуры нет. Отчёт: docs/audit/creatures-1.9.8.1/REPORT.md; результаты gates: CHECKS.md в том же каталоге. Клиент/сервер/GameTest: 0 запусков.

## 1.9.7 — Оберег двух зим (2026-10-13)

Один артефакт из существующих наград Морозко/Карачуна; точные рецепты оригинала и копии с серверной квитанцией и возвратом шаблона. Два серверных режима, пассивы во всех измерениях, активный щит/волна только в Нижнем мире, общий persisted кулдаун, персональная поэтапная аура и собственный эффект без удаления чужой Слабости. Старые боссы и награды не изменены.

Чистая production сборка + десять offline наборов **PASS**; новый набор 4853, ресурсные gates 10991. PolyMC Testing обновлён до **1.9.7**, SHA256 0f1e76dd6bef952350a67d83da5357cc26bcc8dcb290b7eb281c8e46d2290e0e; предыдущий JAR в backup, остальные файлы/миры/конфигурации сохранены. Клиент/сервер/GameTest: **0** запусков; игровые проверки не заявляются.

Архитектура: существующий PathData/RPG sync; WintersEvents — события боя и локальный cast, inventory/aura раз в 20 тиков плюс лёгкий Inventory.setChanged; WintersRecipe — exact 3×3 CustomRecipe/native remaining-items; WintersTemplates — SavedData выданных оригиналов. CLIENT property/tooltip; stable KB modifier, свой щит и эффект aura. Автоматический Crafter отключён для этих двух рецептов. Будущие milestone не реализованы.

Подробнее: docs/TWO_WINTERS_1.9.7.md, docs/verification/winters-1.9.7/REPORT.md.

## Змей Горыныч — 1.9.6, 2026-10-13

Добавлен сюжетный Горыныч Змеиной горы: один привязанный к instance босс, три отдельные multipart головы, серверные логические 3600 HP, наземная и воздушная фазы, появление крыльев при 50%, несмертельное поражение при 5%, очищение порошком/пеплом/водой в любом порядке, живая мирная форма. Яйцо `zmey_gorynych_spawn_egg` находится в существующей вкладке «Мобы» и создаёт самостоятельный бой без сюжетных требований. Самостоятельные экземпляры не выдают сюжетные награды.

Worldgen, завершение команды создания горы и загрузка её чанков подключены к локальному населению; проверяются UUID, последнее место и готовность entity chunks. Повторное посещение существующей горы также подключено. Девять размеченных точек, переходы между крупными верхними помещениями, отдельные нижние компоненты; перед второй фазой выполняется явная сцена выхода в открытый двор. Под потолками/в интерьерах шеи, хвост и крылья складываются. При движении и поиске пути не используются принудительные загрузки чанков.

Реализованы независимые атаки голов с бюджетом двух крупных действий, захват с сопротивлением/помощью и пределом три секунды, каменные копья с геометрическим эффектом, хвост, дождь Мора, огненная бомбардировка, ветер, пикирование и десятисекундные наземные окна. Три временных собираемых дыхания расходуют общие три порции области. Снаряды/области не портят блоки; их время жизни и количество ограничены.

После полного очищения участникам выдаются 4 чешуи, печать и одна равновероятная руна; полному инвентарю оставляется ожидающая персональная награда. Receipt игрока сохраняется вместе с инвентарём до world claim. Чешуя после спасения — одна на игрока раз в 72000 тиков. Три руны интегрированы в существующую установку/слоты и реальные hit events. Змеиный арбалет/сталь добавлены в лут крепости; арбалет трёх дыханий хранит раздельные заряды и использует vanilla arrows. Улучшения оружия/четырёх частей перунитовой брони сохраняют допустимые компоненты и долю износа. Все четыре части имеют отдельную worn geometry и эффекты.

Для связки оберега сохранён ID `mor_ward`: обе детали хранителей + существующее Око Мора создают собранный многоразовый экземпляр с `GuardianWard196`. Ранние экземпляры остаются расходуемыми. Защита горы требует победы над заражённым Лихом, обоих персональных получений фрагментов и наличия/действия оберега; активные атаки Горыныча этим оберегом не блокируются. Ownerless crafter сохраняет запрет персонального рецепта. Новый портал, измерение и объединённая руна не добавлялись.

Native mesh: 40 bones, 292 cuboids, 7008 проверенных UV vertices; 9 согласованных appearance atlases, реальные 3D состояния арбалетов, 4 worn armor models, три разные рунические иконки, 31 собственный синтезированный OGG. Генератор воспроизводит актуальные ресурсы и Java mesh; существующие генераторы горы/финальной 1.8.5 сохраняют новую интеграцию.

BUILD SUCCESSFUL in 1m 32s. Девять offline наборов — PASS; 17065 production/resource gates — PASS. PolyMC Testing установлен на 1.9.6, SHA256 `e197311398f3634c3e2303dce37535d6dd457f1cf88e52e503452d32125a37aa`; 311 остальных файлов профиля сохранены побайтно. Реальных запусков клиента/сервера/PolyMC/GameTest: **0**. Gameplay/MP/перезаход, seed QA, runtime visuals/audio, MSPT/FPS: **NOT_RUN / NOT_MEASURED**. NBT roundtrip и offline projection не являются игровым тестом. [Отчёт](verification/gorynych-1.9.6/REPORT.md).

## Яйца стражей и население святилищ — 1.9.5.1, 2026-10-13

По запросу игрока добавлены яйца Стража угасшего пламени и Хранителя осквернённого родника в существующую вкладку «Мобы». Vanilla-модель яйца с индивидуальными цветами, ru_ru/en_us; генератор guardian_assets включает воспроизводимое создание новых ресурсов. Остальные существующие яйца, рог Полоза и тотемы Морозко/Карачуна сохранены. Горыныч отсутствует в текущем реестре: ТЗ 1.9.5 предусматривает гору и заблокированный путь к будущему логову, полноценного босса откладывает на 1.9.6.

Два стража святилищ создаются вместе с загруженным местом, до входа игрока в арену. Worldgen передаёт метаданные на серверный поток; существующие загруженные сердца повторяют локальную проверку не чаще раза в 20 тиков на instance. Проверяется готовность чанков всей комнаты, UUID/последняя позиция и период восстановления; дубли удаляются. Чанки принудительно не загружаются. SEALED-страж присутствует, но не начинает бой до прежнего открытия святилища. CLEARED восстанавливает очищенную форму. Для уже существующих святилищ тот же механизм действует при загрузке их сердца.

Яйца и /summon создают самостоятельную арену 21×9×21 у места появления: моб не исчезает из-за отсутствия привязки к структуре. Запечатывание святилища не применяется; очищение самостоятельного стража завершается ПКМ по нему после победы. Эта форма не устанавливает блоки/пьедестал, не выдаёт фрагменты святилища; родниковое лечение от отсутствующего структурного источника отключено. Структурная версия сохраняет вентили, источник и награды. Этот патч меняет население двух святилищ, а не условия ритуальных охот и курганных встреч прежних версий.

Чистая сборка и восемь offline наборов — PASS; 16407 production/resource gates — PASS. Проверены сохранение самостоятельной привязки, границы арены и сброс срока проверки населения при загрузке SavedData. Это offline NBT-проверка, не игровой перезаход. PolyMC Testing обновлён на 1.9.5.1; 454 остальных файла профиля сохранены побайтно. Клиент/сервер/PolyMC/GameTest не запускались: 0 реальных запусков; появление мобов, бой, игровой перезаход и runtime визуализация — NOT_RUN. [Отчёт](verification/guardian-spawn-1.9.5.1/REPORT.md).

## Змеиная гора — 1.9.5, 2026-10-13

Реализована редкая единая структура Overworld 128×144×128: проклятый холм, крепость, два различных входа, связанная сеть тоннелей/пещер, внутренние лестницы, двор, залы, четыре башни с ярусами, галерея, оружейная, разрушенная комната и заблокированный путь к будущему логову. Одна детерминированная HillPlan используется native worldgen и операторской командой. 11 блоков с моделями/32px текстурами, 11 рецептов, пять собственных OGG, лут и сказание.

Команда /slavicmyths generate_serpent_hill [x y z] [rotation] размещает комплекс поверх обычных блоков в авторском объёме, сохраняет bedrock, проверяет высоту/границу мира. Работа распределена по 4096 проверкам клеток на серверный тик; очередь и cursor сохраняются. Глубинный Мор наносит исходные 3 HP каждые 30 тиков с обычной обработкой брони. Существующий mor_ward в любом слоте инвентаря или активный WARD_EFFECT полностью отключает эту ауру. Две детали 1.9.4 защитой не считаются; готовый оберег не добавлен.

Чистая сборка, восемь offline наборов и 23227 production/resource gates — PASS. Статический граф подтверждает маршруты между входами и доступность 15 именованных помещений/башен; это не игровой pathfinding. PolyMC Testing обновлён на 1.9.5; 374 других файлов сохранены побайтно. SHA256: dbdb101677397ac28ebe4b937d6f2352956fb4e40f36d6c3c55aebd885047b22. Клиент/сервер/PolyMC/GameTest не запускались: 0 реальных запусков. Команда в superflat, natural seed generation, MP, runtime light/visual/audio и MSPT/FPS — NOT_RUN / NOT_MEASURED. [Отчёт](verification/serpent-hill-1.9.5/REPORT.md).

## Команды создания святилищ — 2026-10-13

Добавлены operator-level-2 команды `/slavicmyths generate_fire_sanctuary [x y z]` и `/slavicmyths generate_pure_spring [x y z]`. Полное принудительное размещение через обычные SanctuaryPiece, StructureStart, маркеры, лут и ссылки чанков; собственный UUID и SanctuaryRecords. Только Overworld, проверка высоты/границы/загруженных чанков/существующих начал/всех шаблонов до изменения блоков; новые чанки не подгружаются. Естественная генерация, registry IDs и баланс не изменены. RU/EN для ошибок и результата. [Использование](SANCTUARY_COMMANDS_1.9.4.md).

Отдельная 1.9.4: clean build + семь offline suites — PASS; 23306 production/resource gates — PASS. SHA256 `0142494a6445e325c48194949f7d1a957110942357b232e20155346f8334b1b8`. Установлена в PolyMC Testing с backup, 360 прочих файлов сохранены побайтно. Начатая 1.9.5 сохранена локально; её clean build и те же семь offline suites также PASS, но это не игровая приёмка/готовность 1.9.5. Реальных запусков client/server/GameTest агентом: 0; команды в игре NOT_RUN. По запросу пользователя версия 1.9.4 с командами опубликована в GitHub вместе с проверенным JAR; версия 1.9.5 не включена. Квитанции: `docs/verification/sanctuary-commands-1.9.4/`.

## Исправление загрузки 1.9.4 — 2026-10-13

Устранён пользовательский startup crash `axis` при регистрации SanctuaryDecor: постоянный MapColor вместо функции OAK_LOG, требующей AXIS. Аналогичный путь SanctuaryBlock защищён. Registry IDs, facing/lit, gameplay и ресурсы сохранены. В production JAR изменились ровно два класса; ресурсы побайтно идентичны исходной 1.9.4. Начатая 1.9.5 сохранена отдельно от опубликованной версии.

Clean build, семь offline suites (63 новые проверки создания украшений) и 23304 production/resource gates — PASS. PolyMC Testing обновлён с backup; 305 прочих файлов сохранены. SHA256 `410407f918508c0b04fef14aca0c7cad7aa39b7d77c4279f2e1088bd870837c3`. Реальных запусков игры/client/server/GameTest агентом: 0; игровой запуск исправления NOT_RUN, MSPT/FPS NOT_MEASURED. [Отчёт](verification/startup-fix-1.9.4/REPORT.md).

## Осквернённые хранители — 1.9.4, 2026-10-13

Добавлены два уникальных хранителя в существующие залы 1.9.3: деревянный Страж погасшего пламени (500 HP, очищение на 25 HP) и блочная Хранительница источника (450 HP, очищение на 22.5 HP). Серверные фазы/телеграфия, сменяющиеся безопасные полосы, два затвора лечения, водоворот, плети, разрушаемая темница, ограниченный разлив и два временных водных воплощения. Очищенные сущности остаются нейтральными. Старые ритуальные флаги открывают залы без регенерации структуры.

Два персональных фрагмента выдаются постаментами после очищения, один раз на UUID/тип независимо от числа святилищ. Поздние участники могут получить свою копию; полный инвентарь не расходует право. Самостоятельные модели (14/34 bones, 92/119 cuboids), совместимые очищенные UV/текстуры, скелетные анимации, две сопряжённые иконки, 28 собственных OGG, три записи сказаний. Готового оберега, его рецепта, новых святилищ/измерений и контента 1.9.5–1.9.6 нет; лут/рецепты/NBT 1.9.3 сохранены.

Clean build, семь offline наборов и 23304 production/resource проверок — PASS. PolyMC Testing обновлён до 1.9.4; 302 остальных файлов сохранены побайтно. SHA256: 04a995f83146dd05a4c3c0c22950ec53fbe0da660026a890b916df8d2235f0f7. Реальных запусков клиента/сервера/PolyMC/GameTest: 0. Бой, визуал/звук, MP, полный инвентарь в игре, перезаход и реальный restart/unload — NOT_RUN; MSPT/FPS — NOT_MEASURED. [Отчёт](verification/guardians-1.9.4/REPORT.md).

## Две древние святыни — 1.9.3, 2026-10-13

Реализованы две естественные структуры Overworld: Святилище неугасимого огня (80×76) и Обитель чистого ключа (96×86), 61 нативный NBT-шаблон. Восстановление огня, очищение двух бассейнов, однократные награды и тайники, девять предметов, семь рецептов, тематический лут, достижения и предания. Закрытые помещения содержат пустые постаменты и арены; будущие боссы и сюжетные обломки не добавлены.

Чистая сборка, шесть offline наборов и 22684 production/resource проверок — PASS. PolyMC Testing обновлён на 1.9.3; 301 остальных файлов сохранены побайтно, предыдущий JAR в резервной копии. SHA256: ab2397c7ae3aada79526305a620715515e986a7974cc31a54bd056cd4088f4e9. Клиент/сервер/PolyMC/GameTest не запускались: 0 запусков. Реальная генерация, игровой визуал/звук, MP и перезаход — NOT_RUN; MSPT/FPS — NOT_MEASURED. Структуры генерируются в новых чанках. [Отчёт](verification/sanctuaries-1.9.3/REPORT.md).

## Испытание Карачуна — 1.9.2, 2026-10-13

Реализован отдельный server trial: 1200 HP по умолчанию с собственным health ceiling (по существующему паттерну Полоза), остановка на 5%, 4 физических ворона, 3 фазы, голод/мороз/смерть/погребальный взмах/локальная ночь/страх/фиксированный Последний час. Независимые шкалы угасания/страха; логическая блокировка offhand сохраняет предмет и ванильную проверку тотема. Источники эффектов учитываются отдельно. Фантомы — только клиентские модели, без server entity IDs/AI/damage.

Самостоятельные boss/raven rigs (26/7 bones, 293/55 boxes), общая объёмная коса boss/item, отдельные пиксельные иконки, 24 собственных sound events. Коса: три серверных удара по дуге, две активные способности, общий hit ledger трёх лезвий, вампиризм по фактически снятому HP и независимые passive cooldown/target chains. Персональные награды: первое Чёрное крыло, каждый раз одна руна и осколок, независимый шанс косы. Отдельные NPC ветки и SavedData без сброса 1.9.1. Готовый тотем не проверяет репутацию; повторное испытание разрешено.

Финальная чистая сборка и пять offline наборов — PASS; production/resource gates 21918 — PASS. SHA256 `75f1cec8b4af7fe2f566765328ec2a0108617a85921a169e6b84f709a511ea70`. PolyMC Testing обновлён после gates; 297 остальных файлов сохранены побайтно. Предыдущий JAR сохранён в `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-karachun-1.9.2-20261013-113133`. Агент не запускал клиент/сервер/PolyMC/GameTest: 0 запусков. Игровые бой, MP, relog, визуал/звук/анимации — NOT_RUN; MSPT/FPS — NOT_MEASURED. Не реализованы 1.9.3, новые структуры/измерения/порталы и будущий рецепт оберега. [Отчёт](verification/karachun-1.9.2/REPORT.md).

## Исправление краша Полоза — 2026-10-13

Пользовательский crash-report crash-2026-10-13_10.21.57-client.txt фиксирует IllegalStateException: Not building! в PolozRenderer.render, строка 34, при отрисовке тела. Буфер тела получался до переключений на текстуры головы и глаз; MultiBufferSource.BufferSource в установленном Minecraft 1.21.1 завершает общий буфер при смене RenderType. Теперь буфер тела получается после головы/глаз и используется для непрерывного прохода тела и шрамов. Геометрия, текстуры, свечение, gameplay и registry IDs сохранены. Генераторы tools не содержат шаблона этого renderer.

Проверки завершены: gradlew.bat clean build — PASS; offline Морозко 6946, quest185 182, Полоз 4290, Mor blocks 120 — PASS; production/resource gates 19725 — PASS. Исправленный JAR установлен в PolyMC Testing после gates: SHA256 94e471d9c3c96a8d63168a499f6256015e870de8ecd227570d6d9def3ba951f1; предыдущий JAR сохранён, 456 остальных файлов профиля побайтно сохранены. Реальных запусков игры агентом: 0; исправление ещё требует ручного входа в мир с Полозом. Миры и NBT не редактируются.

## Морозко и доступ к призыву — 1.9.1, 2026-10-13

`morozko/FrostContent` использует существующие registries и Creative Tabs. `Morozko` — ограниченный локальный server controller/FloatGoal, state/action data, delayed hitframes и HP floor в damage-event + setHealth; `IceDouble` реиспользует rig/basic combat. `FrostCold` обновляет только помеченные сущности5Hz; HUD/cooldowns4Hz через `FrostNetwork`. Новых глобальных world/player scan циклов нет. `FrostShard/FrostHazard` — временные кристаллы/пики/фиксированные падения/волны без setBlock и без сохранения. `PlayerDouble` показывает ownerUUID skin/slim model и read-only equipment snapshots, зеркалирует targetUUID/bbox/LOS/weapon tempo; noSave/no pickup/no loot, Looting исключён с сохранением glint.

Overworld `FrostLedger` хранит sessions/participant contributions/per-player progression/cooldowns/pending delivery UUIDs. Complete готовит все rolls и сохраняет journal до выдачи; `FrostPlayerSaveInvoker` сохраняет inventory+receipt перед acknowledgement. Reload отменяет незавершённое испытание, не возвращает осиротевшие copies. `FrostSkills` проверяет input/предмет/Curios slot/цель/потолок/таймеры; stream heartbeat≤7ticks. Руны в старой `Runes`/`RuneDefinition`/наковальне; отдельный itemId mapping для новых icons. JEI только в прежнем optional plugin; common не импортирует client renderer/screens.

По прямому override: use готового тотема не проверяет unlock, creative пропускает CD и принимается как trial owner; NPC приобретение по прежним реальным trust markers. `MorHorn` обычным ПКМ начинает60tick use, без altar/Shift/confirmation/dust/cooldown gates. `PolozEvent.canStart` допускает IDLE/DEFEATED/FAILED; CAS и EntityJoin guard исключают второго Полоза, сохранённые старые IDs/receipt поля не удаляются. Existing world-impact/balance/art сохранены. Призыв не включает mobGriefing принудительно.

Client-only native30-bone rig/198 boxes/UV, pose controller, translucent/glow layers, actual owner skins/armor/items, custom held/GUI staff+heart, bound G/V/M, HUD и tickable stream sound. `morozko_assets.py` воспроизводит authored PNG/models/21 OGG/translations; `summon_access_policy.py` применяется также legacy Poloz generators и сохраняет пользовательский override. Offline fixtures не входят в production. [Статус и ограничения](verification/morozko-1.9.1/REPORT.md).

## Очищение — финальная редакция 1.8.5

`Quest185State` добавляет независимые early/earth one-time receipts; `Quest185Story` сохраняет предъявленное Око, переводит legacy horn/eye/прежние знания, кладёт4/8 порошков в отдельные pending-поля при полном инвентаре. Q6 теперь только финальные800 XP/глава. Нативные mixins сохраняют personal gates лишь для ward/tracker; порошок свободен в ручном и автоматическом крафте. Его JSON shaped-рецепт проверяется реальным ShapedRecipePattern codec/ingredient matching установленного Minecraft.

`CleansingTasks.connected` выполняет ограниченный BFS сферы radius4 по6 соседям, сортирует distance/y/x/z и выбирает9 разрешённых клеток. Сервер резервирует/списывает1 порошок, сохраняет график с последним шагом start+60. Новых активных задач не больше8/измерение при бюджете8/тик; старые32 записи при чтении не отбрасываются. Незагруженные клетки ожидают без tickets. Перед восстановлением проверяются spawn/world border/build/interact/BreakEvent, отсутствие BE и актуальный ожидаемый state; offline-владелец представлен своим FakePlayer UUID.

`CleansedLand` — chunk attachment с fingerprint/Generation/Until=gameTime+1800, лимит16384/чанк. Регистрация выполняется до attachment bus. Централизованный MorBlocks.convert и HEAD-инъекция LevelChunk.setBlockState блокируют заражение защищённых позиций; RETURN-инъекция/placement/drop события очищают stale origins, immunity и tutorial ownership. Hook не запрещает обычное разрушение. Натуральный mor-grass→dirt переход обёрнут адресным ThreadLocal-контекстом, чтобы vanilla промежуточный dirt не терял исходный state и владельца; прямое естественное распространение сохраняет origin и тоже проверяет иммунитет.

`CleansingLessons` — один Overworld SavedData: максимум20 ключей восстановлений/игрока, UUID урока/owner,20 реальных исходных/ожидаемых samples или paid virtual receipt60 тиков. Индекс dimension+pos даёт адресное ownership invalidation без обхода всех игроков на block change. Реальный урок создаётся единожды по явной кнопке на проверенном натуральном turf patch; при отсутствии места/утрате образцов используется виртуальный fallback. Конкурирующее физическое очищение чужого tutorial sample не даёт чужой/двойной прогресс. Rollback разрешён лишь для живой ownership-записи и ожидаемого блока. Work-set содержит только активные paid/cleanup операции, с bounded round-robin, без общего player/world polling.

Журнал расширен одной параллельной строкой, рецепт открывается непосредственно в существующей TaleScreen. Новые белые/зелёные particles и canvas voxel pouch воспроизводятся финальным overlay генератора. Иконки/модели NPC, Полоза/рога и остальных утверждённых наград сохранены. Runtime мира не запускался: reflection/NBT/offline/resource checks не подтверждают действительную работу mixins, protection-модов, UI или save/restart; это отдельная ручная приёмка в отчёте FINAL.

## Сюжетная ветка прежней редакции 1.8.5

`quest185` использует существующий player PathData CompoundTag: этапы, доказательства, приглашение, порядковые запросы, cooldown и pending rewards. `Quest185State` вычисляет готовность; `Quest185Story` принимает реальные события, мигрирует подтверждённый рог/достижение и атомарно выдаёт XP/предметы. `Quest185Sessions` проверяет nonce, порядок, срок, расстояние, дверь и безопасность прибытия; клиент подтверждает загрузку места. Сцена — только локальные проекции старых моделей, не мировые NPC. Common-код не загружает клиентские классы; сеть доставляет владельцу снимок через клиентский обработчик.

`WolfSupport` наследует модель/анимации прежнего GreyWolf, отключая его сюжетное поведение только для помощника. Владение, время ухода, cooldown, снифф/прыжок/вой ограничены локальными задачами. Полоз признаёт помощника участником владельца и кратковременной целью угрозы. Исходный GreyWolf сохраняет прежнюю сюжетную ветку. Owner-only tracker использует ограниченный локальный запрос загруженных сущностей раз в 10 тиков; глобальный glowing не устанавливается.

`Quest185Origins` — сериализованный chunk attachment с исходным и заражённым состоянием, максимум 16384 записи/чанк. `CleansingTasks` — SavedData по измерению, максимум 32 очереди из девяти клеток, бюджет 8 шагов/тик; ожидает выгруженные клетки без force-load. Проверяет build/interact/BreakEvent с UUID владельца, в том числе offline FakePlayer. Нативные прототипы служат fallback только при отсутствии исходной записи. Оберег меняет собственный mor-block damage/duration; очистка снимает лишь отслеженные эффекты контакта блока.

`Quest185CraftingMixin` закрывает три личных рецепта через общий native crafting-grid callback (верстак/2×2). `Quest185CrafterMixin` запрещает только эти рецепты ownerless crafter. `Quest185ItemRenderer` выбирает standalone baked icon/held модели через BEWLR; отдельные клиентские particles, эффекты и глава прежней TaleScreen. `tools/quest185_assets.py` воспроизводит новые ресурсы. Production JAR проверяется отдельно от offline source sets. Игровой runtime этой итерации не запускался; реальные AI/UI/защита территорий остаются в ручной приёмке.

## Награды и визуальная адаптация Полоза — Full TZ 1.8.3.2

`PolozRewards` регистрирует четыре ArmorItem на ArmorMaterials.NETHERITE, отдельную плеть и руну; ItemStack.transmuteCopy сохраняет DataComponentPatch. `PolozRewardEvents` выполняет серверную операцию улучшения, equipment-driven transient modifiers, LivingDamageEvent mitigation и bounded lash/fissure. Четвёртое попадание учитывается один раз за тик; secondary damage исключён из счётчиков через серверный reentry guard; cooldown опирается на время Overworld. Руна установлена через существующие Runes/socket/anvil APIs и не совместима с бронёй. Multipart-попадание плетью использует фактический ENTITY_INTERACTION_RANGE; дополнительный разлом на Полозе сохраняет единственного владельца HP.

`PolozTrophy` занимает 36 cell/facing состояний одного блока, один BlockEntity и одну loot-bearing anchor. Удаление любой части убирает трофей; loot условен по cell=0; перемещение поршнем запрещено. `PolozStoneSpike` — техническая Entity с жизнью 24t; три проекции/одна серверная активация, без изменений блоков. `MorBlocks` расширен до 14 прототипов с сохранением первых 12 IDs; отдельный `MorGrowth` наносит ограниченный контактный урон и учитывает защиту комплекта. Призыв: 4/6 существ, группа откладывается при нехватке места под общий лимит 8.

Клиентские `PolozArmor`, `PolozTrophyRenderer`, `PolozSpikeRenderer` зарегистрированы только Dist.CLIENT. Пять поз плети выбираются ItemProperties; основной взмах отображается и во время cooldown особого удара. Рендерер Полоза использует прежний серверный track и расширенную геометрию челюстей/клыков/рогов/пластин/спинных шипов, реакции попаданий, затухание смерти и светящиеся глаза. Голос — авторская нерегулярная glottal/airflow/formant модель, mono OGG, без сторонних записей. Генераторы `mor_blocks_assets.py`, `poloz_assets.py` (в конце вызывает `poloz_full_assets.py`) воспроизводят финальные ресурсы. Тестовые source sets не входят в production.

Полный архив Полоза 1.8.3.2 реализован поверх предыдущей итерации. Добавлены трофейная голова 3×3×4, гарантированная плеть, сердце с сохранением ID `poloz_core`, 8–12 чешуй и редкая руна IV (5%). Незеритовая броня укрепляется за две чешуи на часть: Shift + ПКМ по столу кузнеца с бронёй в руке; компоненты сохраняются. Бонусы частей суммируются: 55% сопротивления отбрасыванию и 22% снижения урона; комплект даёт ровно +2 HP и защиту от заражённой земли. Плеть: 13 урона, 6 блоков, широкая атака, три временных каменных шипа, 18 с перезарядки. Руна: четвёртое успешное попадание, разлом 6 урона/до четырёх целей/2 блока, 20 с перезарядки.

Clean build **PASS**; 4290 native geometry/policy checks (8 моделей), 120 native block checks, 31 финальный headless GameTest, 8208 resource/production checks **PASS**. Эта итерация: 5 headless JVM/5 GameTestServer миров, 4 успешных прогона; один прогон обнаружил оставленную тестом наград сохранённую перезарядку — изоляция фикстуры исправлена. Первая чистая сборка обнаружила UV-выход нового рога; исправление подтверждено. Клиентских запусков **0**; реальный multiplayer, перезаход и игровая оценка визуала/звука **NOT_RUN**; MSPT/FPS **NOT_MEASURED**. PolyMC Testing обновлён без запуска; 382 прочих файлов сохранены побайтно. [Отчёт](verification/poloz-full-1.8.3.2/REPORT.md).

## Постоянное поражение и мировой Полоз — 1.8.3.2

`MorBlocks` — точное отображение 12 прототипов/state properties, отдельные регистрации/ванильный lootFrom; `MorBlockExposure` — player-local server sampling раз в 20t. `PolozEvent` — одно Overworld SavedData с подготовкой 180t, UUID/commit receipt/refund map, участниками, cooldown и максимум 16 temporary region tickets. `GreatPoloz` — один health owner (getMaxHealth=3000 без изменения глобального атрибута), NeoForge PartEntity[27], arc-length history, synced compound track каждые 4t, independent attacker cooldown, serial attack scheduler. `PolozWorldImpact` — deduplicated очереди 2048/2048, бюджеты 32/48, максимум один контейнер за тик; обычные destroyBlock/setBlock, без rollback terrain. Прежние временные зоны 1.8.2 сохраняют свой lifecycle.

`PolozAim` направляет прежние боевые действия героев в реальную зону, damage проверяет первый пересечённый AABB. `PolozEvents` даёт временные target/melee/flee goals; обе точки ограничения (`HeroCalls` и `marinka/StoryPolicy`) согласованы на три core-героя только во время события. Common не загружает клиентские рендереры/камеру; новые client handlers только Dist.CLIENT. Тестовые source sets из tools не входят в production. Генераторы mor_blocks_assets/poloz_assets воспроизводят текущие ресурсы.

Clean build PASS; 834 native geometry/policy assertions, 112 block state/physics assertions, 23 финальных headless GameTests и 8079 resource/production checks PASS. Всего 12 headless JVM запусков (10 завершённых успешно, 1 bootstrap failure, 1 исправленный runtime crash); загруженных GameTestServer миров 11, клиентских запусков 0. Реальный multiplayer/перезаход/визуал/впечатление от звука — NOT_RUN; MSPT/FPS — NOT_MEASURED. PolyMC Testing обновлён без запуска, предыдущая 1.8.3 сохранена вне mods. [Отчёт](verification/poloz-1.8.3.2/REPORT.md).

## Lifecycle bossbar, угрозы и хват — 1.8.3

KartausEntity/KalinEntity/HuntMob немедленно скрывают bar/removeAllPlayers и возвращаются при !isAlive перед любой веткой добавления зрителей. HuntMob также очищает bar в die. Removal callback очищает bar у HuntMob, Idolishche, Marinka, SkimBeast, Kartaus, Kalin; WorldBoss уже имел callback. Долгие анимации смерти продолжают работать, урон/лут/квестовые записи не заменялись.

HeroThreats.blighted проверяет ACTIVE состояние LandSpiritEntity, включая все шесть форм и двойников; общий owned-companion target filter использует этот признак наряду с Enemy/debugEnemy. Старые owner/range/LOS/story/allied проверки сохраняются. HigherMor и прежний MorController допускают защиту от напавшего доступного призванного героя, LandSpiritEntity.setTarget согласован с этим правилом. Богатыри без owner по прежнему не управляются контроллером компаньона; это отдельная существующая политика, а не владение, назначаемое этой правкой.

hero_grip_style.apply изменяет реальные авторские rigs до упаковки UV: продлевает руки, сохраняет дочернюю иерархию, ставит pivot оружия в ладонь, строит отдельные связные палицу/саблю/меч, обращает копьё наконечником вперёд. hero_visual_style.prepare вызывает его один раз; face не изменяет геометрию после упаковки. Генераторы и native Mesh классы синхронны. Runtime pose controllers сгибают правое предплечье в neutral forward хват; атаки используют тот же joint. THRUST двигает корпус, а не отрывает копьё/кисть от родительского плеча. Проверяется попадание pivot рукояти внутрь реальной ладони во всех видимых weapon позах, связность кубов оружия и UV на 1004 native кадрах. Офлайн-виды только из этих ModelPart, не отдельная mock-up геометрия.

## Высшие формы и временная арена — 1.8.2

Существующий `MorController` распознаёт дополнительные kind=5 (реальный LIKHO_ONE_EYED) и 6 (OvinnikEntity), использует прежние MOR_STATE/REMAINING/SKILL/CLOCK и передачу управления `HigherMor`; оригинальные fight goals выключаются при morState!=0. Нормальные и прежние четыре моровые формы сохраняют прежние ветви. Новых видов Лиха/Овинника и яиц нет. Спавн естественных высших форм не добавлен; склянка расширена, защитные фильтры имени/владельца сохранены.

`HigherSnapshot` хранит фазу, время незавершённого действия, кулдауны и фиксированное направление в `slavicmyths:higher_*`, ограничивает диапазоны и отбрасывает NaN. Старый MorSnapshot остаётся совместимым. Постоянные атрибутные модификаторы сохраняют ID и долю HP; здоровье высших x2 (Лихо 640, Овинник 350). Размеры вычисляются на обоих логических сторонах, клиент обновляет dimensions при смене MOR_STATE. Renderers выбирают отдельные native-модели; обычные геометрии проверены против установленного JAR 1.8.1.

Технический `slavicmyths:mor_zone` MISC хранит владельца, возраст и стиль земли/облака, ограничен 12 зонами в локальном радиусе 32. Ни одного block write/explosion/chunk ticket. Земля живёт 180 загруженных тиков, облако 120; отсутствие владельца/уход >32 даёт удаление через 40, смерть удаляет локальные зоны сразу. Локальная выборка игроков каждые 10 тиков, непрерывная экспозиция земли 60, урон раз в 20 тиков, Slowness I; зольное облако дополнительно Blindness. Проверка загруженных чанков/видимости защищает от прохождения эффекта сквозь стены. Клиент рисует временные плитки на верхах collision shapes и прозрачный миазм, не меняя terrain.

`MorRejection` устанавливает standard NearestAttackableTargetGoal с chance=20 для Enemy и AvoidEntityGoal для животных/жителей/NeutralMob и нейтральных духов; WeakHashMap не допускает дублирования на одной сущности и не сохраняется в NBT. Приручённые существа исключены. Активные моровые не получают эти цели. Выборки локальны, нет глобальных тиковых обходов. Высшие отвечают на нападение Enemy, ближайший S/A игрок приоритетен.

`HigherRules` используется runtime и офлайн-тестами, native-геометрия экспортируется из ModelPart; это не runtime/MSPT измерение. Инструменты: higher_mor_assets.py, higher-offline.gradle, higher-tests/HigherChecks.java, verify_higher_mor.py, higher_preview.py, install_higher_mor.ps1. Генераторы 1.8.1/1.8.2 сохраняют актуальную расширенную подсказку склянки. Production не содержит тестов.

## Мор 1.8.1 — entity-local controller и отдельный client visual path

`LandSpiritEntity` владеет optional synced mor state/remaining/skill-clock/clone/concealment и одним `MorController`; NORMAL пропускает mor schedule. `MorGoal` блокирует прежние MOVE/LOOK/JUMP только в моровых состояниях. Четыре `MorLeshy/Poludnitsa/Kikimora/Serpent` разделяют реальные move sets; общие focus/cooldown/save/navigation guards не копируются. `MorRules` — production policy/timings; `MorSnapshot` — namespaced NBT schema без повторной сериализации vanilla identity/equipment. MAX_HEALTH/damage/speed permanent modifiers имеют stable ResourceLocation IDs; movement freeze transient. Копии используют старый fire_serpent type, persistent parent/created count/age и отдельный death path без kill credit; bossbar concealment не меняет здоровье.

`MorEvents` делает 4% natural рулетку после day8/>160 и один loaded cap-query; реальные NeoForge FinalizeSpawnEvent/pre-init и LandSpirit.finalizeSpawn/post-super разделены pending флагом. Combat выбор5t, book40t, parent20t, loaded collision guards; нет global tick registry loop/chunk tickets/world block changes. Flask и bolt/root — технические projectile shells; один new visible Item/no recipes. TaleService/TaleData обеспечивают существующую once-only discovery.

Client-only `MorVisual`/отдельные model instances переключают ordinary/forming/final stages и вызывают native pose methods. `MorBoltRenderer` рисует настоящие cuboid roots; MorParticle имеет finite12t fade. MorAudio использует реальные vanilla SoundEvents. Optional `MorJadeCompat` связывает установленный Jade15 callback reflection только на клиенте, удаляет health-tag для concealed serpents, не вводит dependency/не пишет configs. Common не загружает renderer/UI/Jade.

`tools/mor_assets.py` воспроизводит актуальные отдельные UV atlases/sprites/RU+EN/tale без отката ordinary resources. `mor-offline.gradle`/`mor-tests`/work meshes не входят в production. Gates и runtime limits: [REPORT](verification/mor-1.8.1/REPORT.md); игровые visual/audio/MP/rejoin/MSPT NOT_RUN.

## Установка текущей сборки 1.8.1 — 2026-10-12

По подтверждению пользователя текущая рабочая сборка 1.8.1 установлена в PolyMC Slavic-Myths-1.21.1-Testing. Minecraft1.21.1/NeoForge21.1.255 сохранены. Clean build PASS;71 UI helper checks и223098 Mor offline checks/69 meshes PASS;22245 production integrity checks и174 event subscribers PASS. Клиентов/серверов запущено0; gameplay/визуальная игровая приёмка NOT_RUN. Backup1.7.9 вне mods,274 других файлов профиля сохранены побайтно; миры/старый1.16.5 не затронуты.

Согласованные макеты книги/графического дерева, коррекция рук/хвата и лимит двух восстановлений за бой пока НЕ внедрены: в текущей сборке они не изменились. Установка текущей версии не является завершением этих задач. Проверка verify_static_data_1211.py остановилась на устаревшем предположении о biome modifiers; её результат не считается PASS. JSON/source/JAR integrity проверены отдельным текущим verifier. [Отчёт](verification/update-1.8.1/REPORT.md).

## Аудит пропорций рук и лука — 2026-10-12

В исходной опущенной геометрии Алёша заканчивает кисть на4.3px выше нижнего края таза; подготовлена коррекция+5px суммарно. Для Добрыни предложено+2.5px, Финиста+1px; длина Ильи сохранена. Сравнение всех четырёх героев/двух рук и20 offline geometry checks PASS. Найдены зазоры крайних сегментов лука, перемещение тетивы целиком и проблема системы координат BOW; подготовлена геометрическая схема закреплённых концов/V-натяжения. Это только макеты/статический аудит: production integration NOT_IMPLEMENTED,1.7.9/PolyMC не менялись,build/client/server не запускались. [Измерения и макеты](design-review/1.8.0-proposal/revision-3/README.md).

## Второй макет поз и ограничение восстановления — 2026-10-12

Offline предложены forward позы четырёх богатырей со сгибом предплечья/нейтральным хватом, увеличенная головка палицы6.8px; учтены противоположное направление копья Добрыни и постоянный58° наклон меча Финиста. Код-аудит подтверждает отсутствие recovery counter у Финиста/Алёши вне их персональных encounter и отдельную автоматическую snapshot ветку HeroCalls. Пользователь согласовал reset лимита после завершения боя. Executable модель правила2 восстановления/3-е истощение=уход:376 checks PASS; production integration NOT_IMPLEMENTED. Production остаётся1.7.9; build/клиент/сервер не запускались,PolyMC не менялся. [Проверка и макеты](design-review/1.8.0-proposal/revision-2/README.md).

## Изолированные макеты UI/оружия после1.7.9

tools/design_review_180.py читает текущие ресурсы и сохранённые native mesh captures, рисует отдельные макеты в docs/design-review. Не меняет registry/gameplay/resources/версию/установку. Предлагаемая следующая геометрия задаётся отдельным weapon-proposals.json; возвращение prerequisite graph и компактная навигация книги пока являются проектом интерфейса. [Макеты и границы проверки](design-review/1.8.0-proposal/README.md).

## Пути развития и ресурсная политика1.7.9

ClassScreen больше не использует ClassTreeLayout для входа/навигации: список навыков и направления доступны через «Пути развития», learn/equip/advance используют прежние ClassNetwork/ClassState. Назначения имеют явные buttons, подписи занятых навыков и clear; auto skills объясняют отсутствие клавиши. ClassTreeLayout и persisted IDs сохранены. Screen.render в установленном Minecraft повторно вызывает renderBackground: ClassScreen/TaleScreen/ClassSettingsScreen выводят background первым, затем content и widgets отдельно. Это устраняет повторное затемнение без blur.

DeferredSpawnEggItem стандартно регистрирует palette handler NeoForge; прежние overrides -1 удалены. feedback_resource_policy.py восстанавливает10 template_spawn_egg моделей и99 semantic vanilla sound aliases с сохранением event/subtitle IDs; vanilla_item_style.py рисует63 GUI sprites16×16, не трогая held rigs/UV. Профильные генераторы применяют завершающую policy; class UI generator содержит новые строки. verify_feedback_179.py проверяет production/art воспроизводимость и unchanged server/data/held/entity entries. install_feedback_179.ps1 дополнительно настраивает только Jade overlay/numericHP/toggle NumPad6, сохраняет3 configs и предыдущий JAR, проверяет другие файлы. Jade необязателен для самого мода и не включён в его зависимости. [Отчёт](verification/feedback-1.7.9/REPORT.md).

## Клиентские UI и startup guards1.7.8

ClassScreen использует прежний ClassTreeLayout/prerequisites с отдельными display coordinates: icons32/24, horizontal spacing занимает canvas, shared render/hit testing; side panel удалён, details в bounded hover tooltip, progression остаётся ClassNetwork/server-authoritative. ClassClient.conflicts объясняет vanilla hotbar/class/other-category collisions и не меняет чужие mappings.

UiText задаёт реальные Font wrap/ellipsis/tooltip bounds для ClassTheme/FolkUi. UiSearch — literal tokens/Locale.ROOT/е-ё; TaleScreen flat navigation/global search, clearWidgets на init, no blurred background. TaleTreeLayout — finite tidy forest по существующим parents с caching/ancestor context. Бывшие внешние HUD wrappers больше не зарегистрированы на GAME bus, внутренние MOD renderer registrations сохранены. Bytecode gate check_event_subscribers.py обнаруживает пустые subscribers до установки. tools/ui-client и ui-tests изолированы от production; клиентские испытания идут только в copied dev world. install_ui_revision.ps1 использует passed hash gates, backup/staging и проверку прочих файлов выбранного профиля.

## Локальные цели / общий Hero HUD / native материалы 1.7.7

`tools/install_feedback.ps1` устанавливает только прошедший gates production JAR в ранее выбранный PolyMC Slavic-Myths-1.21.1-Testing. Проверяет отсутствие запущенного Minecraft, версии платформы и hashes, создаёт backup вне mods, заменяет один JAR со staging/rollback и проверяет сохранность других файлов. Установка 1.7.7 выполнена по отдельному запросу; квитанция `verification/feedback-1.7.7/polymc-installation.json`. Клиент/миры не запускаются, старый профиль1.16.5 не затрагивается.

`bogatyr/HeroThreats` — common/server helper для прежних entity-local контроллеров. `nearest` использует видимого ближайшего Enemy и явные standalone/debug виды боссов; NeutralMob требует агрессии. Радиусы/cadence прежние, владелец должен быть живым в том же уровне, leash32; нет глобального polling/chunk loading. `availableHero`/`debugOpponent` позволяют тестовым боссам выбирать четырёх героев. Реальные encounter IDs/состав/прогресс не меняются. `activeStory` отделяет standby персонального Идолища от debug-боя, а прежний BogatyrLedger.active по-прежнему блокирует призыв/отзыв в ходе боя.

`client/HeroHud` заменяет четыре независимых Post handlers одним client-only списком максимум двух рядов с HP/состоянием. ClassClient масштабирует существующие иконки/labels в65% и размещает снизу справа; на узком GUI оставляет место хотбару. `tools/hero_visual_style.py` задаёт пиксельные лица и низкоконтрастные face-aligned материалы; bone names/pivots/оружие/hit frames сохраняются. `feedback_assets.py` воспроизводит только выбранные изменённые meshes/textures в отдельных Python processes; legacy материалы нетронутых птиц/props/форм сохраняются. `tools/feedback-runtime*`/offline/export вне production JAR.

## Серверные награды / equipment1.7.6

`rewards/RewardContent` добавляет ровно19 items и22 original sound events в прежние registries. `RewardRunes` ограничивает категории пяти новых рун; `Runes` и `RuneDefinition` сохраняют прежний component/socket/XP pipeline. Новые cloaks имеют один socket без изменения вместимости прежней leather armor. Curios slot IDs остаются ring/belt/necklace; новый amulet slot не создаётся. Три chest items занимают vanilla slot и используют отдельные клиентские модели `RewardArmor`; silhouettes: bone ribs/shoulders, split witch tails, square military mantle/collar.

`RewardLedger` читает только реальную историю BogatyrLedger/WitchYard/SkimValley, существующие Kartaus player flags и KalinEncounter.progress. Каждая прежняя completion routine вызывает маленький adapter после своего authenticated credit. Login/book не roll repeat rewards. Inbox/first/gift/Seen/claimIDs authoritative в `slavicmyths.reward_claims176` persistent data игрока и сохраняются одним playerdata record вместе с inventory. Overworld SavedData `slavicmyths_rewards_176` хранит saveUUID и аудит. Admission проверяет весь комплект на36 копиях inventory slots; при отказе нет частичной выдачи или world drops. Server-thread delivery и once flags выполняются последовательно; Clone копирует durable claims и cooldowns.

`RewardEvents` использует обычные damage/landing/death/equipment hooks. Для новых Curios предметов проверяет текущий конкретный slot без tick-cache. Одна accepted-block точка в прежнем `WardBlockMixin` подключает отражение; отдельный `reward_reflection` bypasses armor/effects/enchantments/shield и не считается melee. Potential calculation не вызывает mutable armor-wear/damage-container методы. `reward_sweep` сохраняет credit без recursion или Momentum farming. Per-target suppression записана на entity; cast cancel ограничен реальным Kartaus EYE wind-up и whitelist tag. Пояс реагирует только на поддерживаемые native direct wind-ups, не на глобальные фазы. Все durable cooldowns используют Overworld gameTime.

Проверки рядом с игроком только при нужном equipped accessory: threats≤18 каждые80ticks, поддерживаемые telegraphs каждые5ticks; списки для packet ограничены30. Основные radius queries происходят на proc. Нет глобального обхода мира, chunk force-load или новых entity clones. MSPT не измерен. Клиентские классы не загружаются common/server; JEI info добавлены только в существующий isolated plugin. `tools/rewards-runtime*` и offline checks находятся вне production source set/JAR. `tools/rewards_assets.py` воспроизводит approved sprites/audio и вызывает model generator; утверждённые19 PNG сохранены byte-identical.

## Калин / военная ставка1.7.5

`kalin/KalinContent` регистрирует boss/horse/2 soldier types,печать/2 eggs/23 собственных event IDs. Common не ссылается на client. `KalinEntity` переиспользует `FalconActor` action/start/length/cooldowns; собственный фиксированный `getMaxHealth=1800` обходится без изменения vanilla ranged attributes (их предел1024). Обычная synced health остаётся настоящим1800HP, armor16/toughness4 и normal damage pipeline сохраняются.

Один `WarHorse` несёт настоящего passenger Калина: riding base1.4 + boss2.8 = total4.2, конь3.2. Удар по owned коню перенаправляется к Калину с одним обычным armor pass, HP-пул общий для фазы. Mounted charge/burst, круговой удар, топот/приказ; после65% scripted45t dismount и45t выход коня. Straight charge ограничен28 от центра, проверяет loaded safe floor/collision каждый шаг, один hit на цель. Препятствие даёт45t stagger. Heavy sabre12t hit, whirlwind15/26/37t, parry2–8t/one cancellation + counter14t, command22t; cast/recovery mutually exclusive. Below40% KB resistance1, ниже20% смешанная волна.14 known soldier UUIDs максимум, common6–10/elite2–3;6 edge anchors, не ближе5 от участника. Soldiers target only owner/selected heroes, без loot/XP, orphan checks10t.

`KalinEncounter` Overworld SavedData `slavicmyths_kalin_175`: BUILDING/ACTIVE/RESTORING jobs с owner/boss/2 hero UUIDs, выбранными registry IDs, center и сохранёнными before/placed cells. Компактный `KalinCamp`1925 sparse placements,65×65;4 towers/4 small tents/royal tent,4 wide gates, braziers/props/banner posts. Только открытый ровный loaded участок, без flattening/BE overwrite/chunk tickets. Preflight65×65 floor/4.4-high open columns + все1925 placements; pending cell recheck перед записью. По256 writes/tick. Rollback сравнивает установленный блок, сохраняет изменения игрока; у fences/bars игнорируются только vanilla-generated direction connection properties. Без loaded cleanup cells повторная проверка через20t. После load jobs толькоRESTORING; orphan NPCs удаляются, победа не повторяется.

`KalinParty.prepare/target` — адресный адаптер четырёх native hero AI. Canonical HeroCalls UUID maps и существующие registry IDs сохранены. Два выбранных героя действуют, невыбранные стоят; support changes ограничены Kalin encounter. Ilya использует native shield/taunt/shockwave, Dobrynya сохраняет spear/parry/combos, Alyosha taunt/dodge/bow, Finist marks/watch/inspiration/falcon intercept/morph. Герой при15% HP уходит к краю, сохраняет занятый слот; оставшийся продолжает. Стандартные восстановления возобновляются после окончания, stale retreat flag очищается. Native attacks/cooldowns вне нового боя сохраняются.

`KalinEvents` ограничивает friendly fire, вызывает surrendered state при lethal postarmor damage: живой1HP, kneel100t, right-click surrender line, army/horse cleanup. Story credit только owned non-creative encounter с реальным вкладом живого owner; debug egg победы не даёт. SavedData Progress(owner) и Player persistent compound `slavicmyths.kalin175`: kalin_battle_started/won, selected_hero_a/b, times_kalin_defeated,last_battle_timestamp,pending_reward_1_7_6. Clone переносит compound, наград/достижений не выдаётся. StoryPolicy/HeroCalls блокируют start/recall/switch в BUILDING/ACTIVE; прямоугольная cleanup reservation предотвращает также диагональное перекрытие двух65×65 лагерей и препятствует новому лагерю, но после сдачи позволяет отзыв героев.

`client/KalinClient/KalinModel` и4 авторских meshes из `tools/kalin_assets.py`: native cuboid poses, собственные256px atlases, двигающиеся части/оружие/плащ/legs. Saber силуэт:27px curved3.5px-wide blade,8px guard,leather grip/steel/gold; не recolor старого straight sword/staff. Client registration isolated by Dist.CLIENT. Test/offline sources only under tools, never production.

1.7.5 установлена в PolyMC Slavic-Myths-1.21.1-Testing с backup1.7.4. Clean build,1 focused GameTest/101 assertions, 149000 native checks/48 profiles и50122 production/resource gates PASS. Реальные16 GameTest server starts (включая исправленные итерации),0 клиентов.7426 prior JAR entries byte-identical;2 synthetic switch classes имеют прежние opcodes, изменились debug line tables. История победы дополнительно прочитана из настоящего SavedData .dat на диске. Полный игровой client/real network MP/relog/world restart/MSPT NOT_RUN.

## Финист и Картаус1.7.4

`finist/FalconActor` синхронизирует конечный action/start/length; `FinistEntity` сохраняет owner/HP/cooldown/last-stand budget, управляет одним visual falcon controller и временной формой того же UUID. `HeroCalls` расширен четвёртым canonical hero map, recall/dormant snapshots и max2 через все существующие входы. `StoryPolicy` добавляет matched Kartaus/Finist, owner/nearby dimension-aware locks и standby; old hero combat classes сохранены.

`KartausEncounter` — Overworld SavedData с адресными STARTING/ACTIVE jobs, owner/hero/boss IDs, center/status/start;17-point safe site sampling, no blocks/worldgen/chunk tickets. Активные jobs после load сбрасываются; tagged поздно загружаемые helpers проверяют живого boss/event. `KartausEntity` имеет один cast clock, known UUID sets с caps2 banners/3 vanilla guards/1 anchor, scoped mitigations/DOT/curse/cleanup; данные личной победы — в Player persistent data, только owner credit. Завершённые jobs удаляются из активного map.

`client/BrightFalconClient` изолирует native models/HUD/independent low-particles quads, синхронизированные guard spawn spots и locked eye. Companion визуальный, не отдельный tameable creature; morph не создаёт новый герой. Прежний `client/FinistClient` обслуживает старый лук и не заменён. `tools/finist_assets.py` воспроизводит authored rigs/UV и supplied sprites/audio; native/runtime fixtures отсутствуют в production JAR. [Результаты и пределы проверки](IMPLEMENTATION_REPORT_1.7.4.md).

## Модуль Алёши/Скима1.7.3

`skim/SkimActor` задаёт синхронизированный action/start/length и серверные crossed-frame callbacks. AL/Skim используют entity-local AI/cooldown/threat/stamina; настоящий `AlyoshaArrow` имеет owner filtering/range/TTL. `HeroCalls` supplements прежние UUID ledgers, хранит recall/dormant snapshots и retired tombstones, безопасные позиции и max2; нет принудительной загрузки чанков. `StoryPolicy` связывает matched hero и standby с прежними story systems. Common не загружает client.

`SkimValley` Overworld SavedData и `ValleyBlueprint` дают проверенное явное размещение65×65+den, bounded256-cell jobs, expected pending block states, once-only chests/gate/history. Активные jobs проверяют известные UUID/позиции с ограниченным интервалом; полных обходов мира/игроков каждый tick нет. Startup ACTIVE→READY, VICTORY продолжает gate. Игровой герой иличный progression отделены от physical arena.

`client/SkimClient/SkimModel` изолируют native meshes/26 action profiles/HUD/tells и arrow renderer. Два atlas256,7 icons64,48 additive sound events. Generator reproduces outputs0 изменений; offline/runtime tools не входят в production JAR. Удлинённый oriented AABB не меняется при visual rear; routing и network/camera эффекты требуют ручной приёмки. [Результаты и ограничения](IMPLEMENTATION_REPORT_1.7.3.md).

## Модуль Добрыни и Маринки 1.7.2

`marinka/WitchActor` синхронизирует action/start/length/sequence и две силы hurt; серверные event frames/cooldown отделены от client pose. `DobrynyaEntity` сохраняет тот же UUID/HP при смене формы, ограничивает парирование/отражение, sweep idempotence, retreat/drink/recovery и owner identity. `MarinkaEntity` использует один EntityType/renderer для оригинала и двух false bodies; parent/falseHP и групповой heavy budget серверные. `WitchBolt`, `WitchRune`, `HexAnchor`, `TurMinion` имеют локальные проверки и конечные TTL/cleanup. Common не загружает client.

`WitchYard` SavedData хранит авторскую размещённую арену, cursor/ожидаемые состояния, phase/hatch/loot и участников. UUID героя и личная история находятся в overworld data; физический двор разрешён только в overworld. Адресные jobs пишут максимум256 blocks/tick, не force-load chunks и не перезаписывают изменённую игроком pending cell. COMBAT после рестарта откатывается к READY, WON продолжает opening; OPEN/entrance и vanilla chest contents независимы от личной victory eligibility. `StoryPolicy` связывает уже существующий Ilya summon/standby с личными участниками испытания, не ограничивая посторонних игроков; сохранение новых записей не требует переписывать старые registry IDs.

`client/MarinkaClient` изолирует renderer/HUD/ground tell; пять собственных native meshes используют атласы256, distinct spear/shield silhouettes и form-specific poses. `tools/marinka_assets.py` воспроизводит только новые rigs/atlases/39 OGG и additive locale/sounds; повтор0 изменений/6411 files. Opt-in runtime/offline fixtures не входят в production JAR. [Отчёт и фактические ограничения](IMPLEMENTATION_REPORT_1.7.2.md), [тесты](verification/marinka-1.7.2/TEST_MATRIX.md).

## Модуль богатырей 1.7.1 — 2026-10-11

`bogatyr/` содержит серверные entity-local автоматы Ильи/Идолища, конечные budgets/cooldown, Overworld SavedData по UUID, marked vanilla minion goals, события mitigation/перехвата и валидируемые пакеты призыва/освобождения. Common не ссылается на client. `BogatyrMotion` задаёт shared pivots multipart рук и native model. `client/BogatyrClient` изолирует renderer/36 clips/menu/HUD/доступные наземные предупреждения. `BogatyrArena` записывает <=256 проверенных air cells/tick только пока есть явная очередь, не очищает terrain и не грузит чанки. `tools/bogatyr_assets.py` воспроизводит rigs/UV/33 исходных OGG и additive lang; opt-in runtime/offline fixtures отсутствуют в JAR. IDs прежних сущностей/мира сохранены. [Подробности](bogatyr/IMPLEMENTATION.md), [проверки и ограничения](verification/bogatyr-1.7.1/REPORT.md).

## Модуль Ночницы 1.6.4 — 2026-10-10

`nochnitsa/NochnitsaContent` регистрирует сущность, яйцо, атрибуты/spawn placement, 12 SoundEvents и MapCodec biome modifier. Общая конфигурация `slavicmyths-nochnitsa.toml` — вес и debug. `NochnitsaRules.Clock` — реальный серверный автомат; `NochnitsaEntity` валидирует цель/LOS/ауру на старте, во время windup и на выпуске, синхронизирует state/start, сохраняет cooldown, рассеивается днём и не возобновляет незавершённый крик после загрузки.

`NochnitsaHorde` выполняет одну ограниченную локальную выборку загруженных Monster на крик, фильтрует сферу32/вид/союз/месть/руны, сортирует ближайших и обрабатывает максимум48. WeakMap хранит UUID/TTL240/write gap40; target goal устанавливается через EntityJoinLevelEvent, EntityLeave удаляет память. Нет глобального tick polling или принудительной загрузки чанков. Existing OminousAura и её mixins не изменены; её же API решает экипировку и провокацию, защищённый игрок не допускается в качестве цели коллективного крика.

Клиент изолирован в `client/NochnitsaClient` и сгенерированном `NochnitsaMesh`. Собственная иерархия120 костей, distance-based gait, артикуляция волос/лоскутов/когтей; FULL_BRIGHT только два eye cubes, без повторного прохода тела. Три кратковременных кольца — локальная render geometry с camera cull48. Ресурсный генератор и rig JSON воспроизводимы; оригинальный audio DSP с manifest и decode QA. Тесты в `tools/nochnitsa-tests` не входят в production JAR.

EarthConfig исправлен отдельно: default shot8, допустим melee30, balanceRevision1 мигрирует только известные legacy defaults и сохраняет остальные значения. Загрузка и однократность проверены offline на установленной конфигурационной библиотеке, не игровым запуском. Build/resource/audio/offline PASS; 1.6.4 установлена с backup, **Minecraft запусков0, dedicated/MP/игровая приёмка NOT TESTED**. [Подробности](nochnitsa/IMPLEMENTATION.md), [отчёт](verification/nochnitsa-1.6.4/REPORT.md).

## Исправления боя и визуала 1.6.3 — 2026-10-10

По новому игровому отзыву: в последнем сохранении подтверждены EASY + Resistance I, которые снижали старый raw удар дополнительно. Для физических атак Духа введены scaling=never damage types без обхода брони/защиты: кулак 8 HP после обычного алмаза, с Resistance I 6.4 HP. Добавлена диагностика фактического Post damage в `/sm earth_spirit status`. Ускорены кулак/залп/снаряды и cooldown первой фазы, походка следует реальному перемещению. Весь корпус получил vanilla stone/cobble/moss/andesite/deepslate atlas; noOcclusion печати исправляет скрытую траву; хвост Сивки больше не использует сетевой modulo gait. Яйцо Жар-птицы первое в «Мобы»; её принятый дизайн сохранён.

1.6.3 установлена в PolyMC 1.21.1-Testing с резервной копией 1.6.2; 464 остальных файла сохранены. Полный Earth resource regeneration: 0 изменений.

Clean build PASS (31s), Earth6186/Sivka7107/Firebird200020/Visual15 и resource/production gates PASS. **Запусков Minecraft: 0, новый игровой бой и визуал NOT TESTED**. [Отчёт и ручные проверки](verification/visual-1.6.3/REPORT.md). Мировые данные read-only, registry IDs и чужие изменения сохранены; commit не создавался.

## Исправления Сивки, Духа земли и Жар-птицы 1.6.2 — 2026-10-10

По пользовательской игровой проверке: разнесены coplanar facets Сивки/Духа, устранён double draw emissive у Духа, добавлены каменная фактура/vanilla block clusters и поэтапный косметический распад на 58 блоков разных материалов. Жар-птица переработана (182 bones/369 cuboids), лапы с пальцами/когтями анимированы; **яйцо добавлено по новому прямому указанию**. Дух реагирует на нападающего и приоритетно отвечает melee. Пользователь отменил старое arena-only ограничение: **руна и обычные награды за победу вне арены тоже**. Кулак теперь 8 HP после diamond без enchantments; тяжёлые атаки на той же основе, слуги неизменны.

Clean build PASS (49s); Earth6186/Sivka7107/Firebird200020/Visual8 offline PASS, 18206 production/resource checks. Actual static geometry: 0 coplanar face pairs; glow vertices submitted once. **Запусков Minecraft агентом:0; исправленная runtime-приёмка NOT TESTED.** [Отчёт и ручная проверка](verification/visual-1.6.2/REPORT.md). Сохранены пользовательские изменения/registry IDs; commit не создавался.

## Жар-птица 1.6.1 — 2026-10-10

Тестовый кандидат: новая `slavicmyths:firebird` без яйца по уточнению пользователя; Overworld nighttime SavedData event/+20…30 дней, единый бюджет 2/3 drops и 10/30/60, три ресурса без рецептов, 3000 HP/20 HP defensive armor bypass, native rig138 bones/267 cuboids/fullbright и 28 оригинальных OGG. Существующий `ognennoe_pero` и прежние механики сохранены. [Архитектура и ограничения](firebird/IMPLEMENTATION.md).

Clean build PASS (1m 2s), 200020 offline assertions, 23286 resource/production checks; **запусков Minecraft: 0**. Fidelity/звук/бой/полёт/MP/relog/MSPT и dedicated server startup NOT TESTED; приёмка OPEN. [Отчёт](verification/firebird-1.6.1/REPORT.md), [ручная проверка](verification/firebird-1.6.1/COMMANDS.md). Runtime не выдан за компиляцию.

## Сивка-Бурка 1.6.0 — 2026-10-10

По новому пакету реализован тестовый кандидат: отдельный белый native-конь152кубоида/51узел, артикулированные наземный/воздушный галоп,7этапов кормления, canonical SavedData/temporary72000online ticks/permanent bond, свисток, безопасный призыв, saddleless riding/разбег/полёт/уход,80HP/retreat, редкий native spawn,31новый OGG и RU/EN. По уточнению пользователя снижение на **X**, как у метлы/ступы; Space вверх, Shift спешивание. Кот Баюн/квесты/новые milestone не добавлены.

Clean build PASS (1m 35s), 7107offline /12878resources-production PASS; сохранены 1055классов/5290ресурсов1.5.6. Кандидат установлен в существующий PolyMC1.21.1 с backup; 357остальных файлов неизменны. **Запусков Minecraft:0**; gameplay/visual/audio/MP/relog/MSPT приёмка OPEN, все runtime-строки NOT TESTED. Статическое превью/codec не заменяют игру. [Отчёт](verification/sivka-1.6.0/REPORT.md), [краткая ручная проверка](verification/sivka-1.6.0/COMMANDS.md).

`sivka`: `SivkaContent` расширяет прежние registries/spawn placement; `SivkaBonds` один overworld SavedData owner→bond, online-play timer, token/generation, адресный UUID lookup в загруженном измерении. `SivkaWhistle` bounded safe search/reuse/generation, custom data UI, серверная валидация; `SivkaEntity` локальные стандартные goals и server physics/native move, last-input timeout/collision/landing/retreat. `SivkaNetwork` intent-only finite/rate/owner/passenger/entity/sequence validation. Null controlling passenger не отдаёт movement authority клиентскому vanilla vehicle packet. Client-only `SivkaClient/SivkaMesh`, native eased `SivkaPoses`, weak per-entity visual cache. Движение не teleport/noPhysics; forcedchunks/global AI/world scans нет. Единственный player tick адресно обновляет обязательный active timer существующей связи; inventory reconciliation1Hz. `tools/sivka_resources.py` воспроизводит geometry/poses/textures/lang/sounds; editable sources/docs и opt-in offline fixtures исключены из JAR.

## Дух земли 1.5.6 — 2026-10-10

`earth` расширяет существующие deferred registries без изменения прежних ID. `EarthArena` — SavedData с адресными encounter ID/state, boss UUID, consumed pillars, gate state, reward flag и retired servants. Единственный локальный hook в завершении нового `DepthsService` job размещает элементы в проверенных пустых клетках прежнего cave layout, без реконструкции старых миров или изменения А/Б/поиска. `EarthSeal.attack` и BreakEvent активируют зарегистрированную арену однократно; поставленный вручную блок создаёт тестового босса без arena ID/награды. Никакой арены вокруг ручного блока не генерируется.

`EarthSpirit` ведёт один серверный cast, cooldown/recovery, локальный target раз в 10 тиков, pressure только действительных melee-участников через прямой UUID lookup, фазы и замороженные таймеры сна. `EarthActor` обслуживает ограниченные swept projectiles, волну со ступнями/LOS, линию шипов и временные FX; не сохраняется, не загружает чанки. `EarthServant` сохраняет ownership, не форсирует загрузку владельца; смерть/реальное удаление снимают UUID, разгрузка сохраняет. Spawn wave атомарна. Награда только настоящей победе; `DEFEATED` записывается до дропа и очистки.

`EarthRules` централизует defaults/raw calibration и wrist release transform; `EarthConfig` COMMON применяет HP/скорость новым сущностям и restart-настройки фаз/таймеров/руны/cooldown. `EarthRune` обновляет transient атрибуты событийно и учитывает только четыре надетых слота. Существующие ограничения наковальни сохранены. `EarthClient`/`EarthCamera` изолированы Dist.CLIENT: native 95-cuboid rig, суставные keyframes, точная привязка props к кисти, synced clocks, отдельные атласы/свечение, контролируемая смерть без vanilla flip, отключаемая локальная камера. `earth_imbalance` — короткий 13-tick эффект, только после успешного tremor damage. Отдельного глобального server tick dispatcher для этой системы нет.

`tools/earth_resources.py` воспроизводит собственные pixel assets и merge-добавления, сохраняя старые ключи/ресурсы; звук копируется побайтно из архива. Offline Gradle task находится вне production source sets. Финальные build/offline/resources PASS; **0 запусков Minecraft**, codec/mesh preview не являются игровой проверкой или измерением MSPT. [Карта реализации](verification/earth-1.5.6/IMPLEMENTATION.md).

## Мобы подземелий 1.5.5 — 2026-10-10

`underground` регистрирует три типа мобов, вариант Подземника, два временных визуальных типа, яйца, руну и краткий эффект падения. `EncounterMob` хранит синхронизированную фазу/начало, серверные cooldown и домашнюю точку; `RoomBoss` обслуживает BossBar. Урон и расход exhaustion стандартные. Снаряды/шипы временные и не сохраняются; незавершённые атаки после загрузки безопасно отменяются. Клиентские native ModelPart, отдельные 128px атласы и анимации находятся в `UndergroundClient`; common не загружает renderer.

`EncounterPopulation` — SavedData с планами, курсорами и результатами SPAWNED/SKIPPED/DEFEATED. Комнаты выбираются из прежнего Б, арены не перестраиваются. ChunkEvent.Load ставит только новые разрешённые планы; очередь каждые 40 тиков проверяет максимум четыре слота по четыре позиции без запроса чанков. Legacy NBT без флага не заселяется автоматически. Команды spawn/status/populate позволяют ручную проверку.

`OminousAura` проверяет установленные руны и адресную провокацию событийно; common mixin TargetingConditions/ LivingEntity.canAttack покрывают выбор и продолжение цели. Player.causeFoodExhaustion меняет реальные затраты один раз, с локальным запросом боевого крупного моба. Эффект падения ограничен сроком и владельцем; вынужденная pose восстанавливается при завершении/смерти владельца/смене измерения. Глобального обхода мобов нет. `ChudPerson` сохраняет registry IDs, native MerchantOffer и approved mesh; добавлены стандартные/ограниченные goals, инструменты и временный UUID-список агрессоров.

Финальный clean build PASS; 18 проверок правил, 2387 native mesh-проверок, SavedData roundtrip, 13 104 ресурсных/production проверки PASS. 1.5.5 установлена после gates; запусков Minecraft 0, runtime и MSPT не измерены. [Доказательства и ограничения](verification/mobs-1.5.5/REPORT.md).

## Промышленность и торговля Чуди 1.5.4 — 2026-10-09

`industry/Industry` подключает deferred registries к существующим блокам, предметам, block entities и сущностям. `MachineRecipe` — два RecipeType/Serializer с MapCodec/StreamCodec; `MachineTile` — серверный BE ticker, 3/4 слота, реальные топливо/флюс, проверка выхода до расхода, NBT прогресса/опыта, sided item capabilities; `MachineMenu` — native menu/ContainerData. Экраны, оригинальные модели NPC, рендер очков и tooltip серебра изолированы в client; JEI — в optional compat.

`BogIronFeature` размещает небольшие пласты под поверхностью болот только Overworld. `IndustryAccessories` использует существующие слоты Curios и событийные проверки скорости/финального FALL-урона; очки сохраняют и восстанавливают внешний Night Vision, пояс использует постоянные ID transient modifiers. `MiningRunes` расширяет существующие категории/слоты рун кирок; узкий common mixin `ApplyBonusCount` меняет эффективный Fortune только для подходящей руды/кирки, учитывает Silk Touch и потолок IV, не изменяет зачарования ItemStack. Серебряный combat handler сохранён.

`ChudPerson` использует native MerchantMenu/MerchantOffer и стандартные AI goals; ограниченные локальные проверки раз в 40 тиков обслуживают торговца, а пополнение сохраняет два дневных окна. Защита стражей реагирует на атаку, Zombie/Skeleton получают цель при EntityJoinLevel. Глобального обхода мира/игроков не добавлено. `ChudContents` после прежнего writer-а добавляет только в свободные поддержанные клетки новые native chest loot tables, машины и NPC. `ChudPiece`/`AbandonedPiece` сохраняют разрешение нового содержимого и завершённые чанки; прежний NBT без флага не переоборудуется. Планы, размеры, проходы, сетка/правила размещения и все 12 классов Древних глубин побайтно сохранены.

`tools/industry_resources.py` воспроизводит оригинальные ≥32px ресурсы и добавочные JSON; opt-in `tools/industry-offline.gradle` компилирует чистые правила отдельно от production. Финальный clean build PASS; 994 production-класса, 5443 проверки правил и 79 781 ресурсная/production проверка PASS. Minecraft не запускался: runtime/AI/GUI/сохранение/MSPT не измерены. [Доказательства и ограничения](verification/industry-1.5.4/REPORT.md).

## Древние глубины 1.5.3.1 — исправление плоского мира, 2026-10-09

Исправлен отказ на четырёхслойном flat: при FlatLevelSource общий путь команды и карты поднимает origin на 32 блока над максимальной отметкой входа. Нижняя граница bbox оказывается на один блок выше поверхности. Все помещения, спуски, арена и содержимое перемещаются вместе; отдельный blueprint и формат сохранения не меняются. Подземная проверка покрытия и выбор природного пролома в этом режиме не применяются: используется CLOSED. В обычном мире размещение остаётся подземным. End portal/frame, бедрок, чужие контейнеры и фактическое пересечение с другими сооружениями защищены. Ссылки на соседние native сооружения не запрещают свободный объём над flat, но пересечение их bbox продолжает запрещаться.

Отказы теперь показывают число кандидатов и причины (вода, склон, высота, граница, занятый объём, защищённые блоки). Истечение общего времени поиска сообщает текущий кандидат/этап и не выдаётся за отсутствие сухой земли. Сообщение о flat содержит реальные координаты поднятого входа.

Финальный clean build — PASS, 46 секунд. 2318 геометрических проверок (включая все блоки поднятого blueprint при четырёх высотах поверхности и четырёх ориентациях), 144 поля NBT, 12 049 ресурсных/production и 1063 статических проверки — PASS. В production JAR 952 класса; по сравнению с 1.5.3 изменились только DepthsPlan/DepthsService/DepthsService$Job. Все прежние переводы сохранены. 1.5.3.1 установлена в PolyMC 1.21.1-Testing с резервной копией; 296 остальных файлов экземпляра неизменны. Запусков Minecraft: 0; игровая приёмка открыта. [Отчёт](verification/ancient-depths-1.5.3.1/REPORT.md), [команды](verification/ancient-depths-1.5.3.1/COMMANDS.md).

## Древние глубины 1.5.3 — 2026-10-09

Реализовано отдельное подземелье В по приложенному пакету 1.5.3. Появляется только по команде или активации карты: зоны E0–E5 и переход K, вход на расстоянии 1500–2000 блоков, закрытый свод либо безопасный пролом к небу. Внутри два сундука, десять обычных мобов и два ванильных стража; финальная арена 29×29 оставлена без босса. Общая серверная операция ограничивает загрузку чанков и число записей за тик; SavedData сохраняет план и прогресс. Завершение выдаёт настоящую карту с RED_X. Естественная генерация В отсутствует; А/Б не изменены.

Финальный `clean build depthsOfflineChecks` — PASS (29 секунд). Геометрия: 2269 проверок / 8 вариантов; NBT: 144 проверки / 12 roundtrip. Ресурсы и production JAR: 12 049 проверок; отдельный статический аудит: 1063 проверки. Все 952 скомпилированных класса совпадают с JAR; 50 классов Чуди и 6 её ресурсов побайтно сохранены. Версия 1.5.3 установлена в PolyMC 1.21.1-Testing с резервной копией; 260 остальных файлов экземпляра неизменны. Клиентских и серверных запусков: 0. Свет, AI, карта в игре, реальный перезаход и MSPT требуют ручной проверки. [Отчёт](verification/ancient-depths-1.5.3/REPORT.md), [команды](verification/ancient-depths-1.5.3/COMMANDS.md).

## Свет/кладка Чуди1.5.2.2 — 2026-10-09

Clean build PASS;29 614 plain JVM assertions (11 221 новых decor policy) и12 020 resources/production checks PASS;940 compiled classes совпадают с JAR.14 классов сохранённых планов и6 классов генерации/portal protection побайтно сохранены. Установлена1.5.2.2 в PolyMC1.21.1-Testing с backup;259 остальных файлов/миров/настроек неизменны. Клиенты0/серверы0; visual/light-engine/relog NOT_RUN. SHA256 925b9eb3914a39bd1b33a39de59f3acb6b36e3724db899e104fe25f08d40e398.

По ручной обратной связи пользователя flat: тёплые вставки8×8 в открытый каменный пол и чаще corridor lanterns. Шесть палитр по назначению зон; tuft/cobble/cracked/mossy/deepslate, patch3×3×4; ruined имеет больше износа. Отделка final chunk writer заменяет только полные кубы; saved plans, проходы, размещение, IDs и portal protection сохранены. Старые chunks не перестраиваются. Клиенты0/серверы0; игровая визуальная приёмка открыта. [Отчёт/gates](verification/chud-1.5.2/lighting-masonry/REPORT.md), [команды](verification/chud-1.5.2/COMMANDS.md).

## Генерация Чуди1.5.2.1 — 2026-10-09

Clean build +18 393 plain JVM assertions PASS;12 014 resource/production checks PASS;937 compiled classes совпадают с JAR;18 saved plan/geometry classes побайтно сохранены. Установлена1.5.2.1 в PolyMC1.21.1-Testing, backup прежней версии;2428 остальных файлов/миров/настроек побайтно неизменны. SHA256 5e692a7e90ebbb4dc29c648f2b54fd6e383489df87595134eaff996e5c085022. Клиенты0/серверы0; runtime NOT_RUN, ручная приёмка открыта.

Убраны rock/fluid/route-cover и конфликты обычных построек. Общий конечный blueprint/roof sampling16:20 блоков под грунтом,5 под дном воды; actual bounds выше бедрока. Существующая сетка independent gap2298≥2000 сохранена, часть клеток выделена B. Новые operator команды generate сохраняют native starts/references; flat размещает над поверхностью. Seed stronghold exclusion, командный preflight PortalRoom/frames и защита portal blocks в rasterizer. Saved geometry/IDs сохранены, старые chunks не перестраиваются. Никаких игровых запусков; runtime/relog/MSPT приёмка открыта. [Отчёт и gates](verification/chud-1.5.2/simple-generation/REPORT.md), [команды](verification/chud-1.5.2/COMMANDS.md).

## Region placement Чуди1.5.2 — 2026-10-09

Новый placement проверяет прежнюю точку и до9 запасных в области, выбирает один А/Б root. Горы — предпочтение, biome tags разрешают Overworld;12% фильтр лесов удалён. Исправлена нижняя граница medium/great по фактической конструкции, сохранённые bbox/NBT/геометрия не менялись. Минимальный gap независимых комплексов2298≥2000, native owner совпадает с выбранной точкой. Курганы/поселения не правились.

Clean build PASS;18387 offline assertions, включая192 immutable plans на искусственном сухом грунте Y64;12010 resource/production checks;8 resource files воспроизводятся побайтно.935 compiled classes совпали с JAR;20 saved geometry classes совпадают с CA2. Это не проверка реального worldgen:0 клиентов/0 серверов, runtime/native density/chunk-order/MSPT NOT_RUN по запрету пользователя.

Development candidate SHA256 `8181fe0481c542314b88d64f68de2681a240541fbe7cceb093266a7846988336` установлен в PolyMC1.21.1-Testing;backup предыдущего6ae сохранён,2366 остальных файлов/миров/настроек неизменны. Полная приёмка1.5.2 открыта. [Отчёт размещения](verification/chud-1.5.2/placement-fix/REPORT.md).

## Ограниченный поиск Чуди1.5.2 — 2026-10-09

`ChudSearchCommands` добавляет точные Brigadier literals для двух ID под vanilla `locate structure`; остальные аргументы сохраняют vanilla обработчик. `/sm … locate` сначала использует немедленный `getChunkNow`, затем при отсутствии starts запускает тот же seed-only поиск. Один daemon worker с низким приоритетом, одна активная задача без очереди; страницы96 кандидатов, кооперативный deadline60 секунд, уведомления каждые2 секунды только при активном запросе. Нет глобального tick polling.

В серверном потоке фиксируются registry, генератор/biome source, random state, template manager, seed, границы мира и отдельный immutable `LevelHeightAccessor`; worker не использует `ServerLevel`, чтение блоков или загрузку чанков. `ChudSearchBudget` — ThreadLocal только ручного поиска: проверки между noise-колонками, baseHeight и соседними native-планами; обычная worldgen не ограничивается этим deadline. Ответы возвращаются через server executor. Продолжение хранит центр/измерение/seed/тип/страницу/незавершённый индекс; остановка сервера отменяет задачу и очищает продолжение.

Результат — native план для новых чанков, без изменения мира, без гарантии ближайшего существующего start. Сохранённые layouts/NBT/registry IDs неизменны:21 соответствующий класс побайтно совпал с прежним JAR. Исправление проверено clean build,11 plain JVM checks и11992 resource/production checks;926 compiled classes совпадают с JAR. В текущей итерации Minecraft не запускался:runtime `NOT_RUN`, исторические native gates относятся к baseline CA2. [Отчёт](verification/chud-1.5.2/search-fix/REPORT.md).

## Исправления боя после обратной связи — 2026-10-09

ТугаринII получил видимый эффект защиты50%; Соловей отгоняет игрока после5 секунд близости и воII фазе летает/пикирует; AI Проклятого Оборотня переработан, устранены простои и зона недоступной атаки. Clean build и21813 resource/production checks PASS; финальные41/41 native GameTests PASS (всего4 headless запуска, первый с исправленной ошибкой). В этой итерации были2 dev client запуска до возражения пользователя; автоматический запуск признан ошибкой. После возражения0 клиентов/0 серверов; дальнейшие игровые запуски только по отдельному явному разрешению. Проверенный JAR установлен в тестовый PolyMC без запуска, backup/179 других файлов сохранены. Ручная survival/multiplayer/relog/MSPT приёмка открыта. [Отчёт](verification/combat-feedback-2026-10-09/REPORT.md).

Серверный serpent_guard — только видимый маркер; единственный множитель0.5 остаётся в WorldBoss.hurt. Flight/pressure/dive встроены в ограниченный entity controller Соловья, используют native physics и локальную проверку потолка. Оборотень использует стандартное получение цели, native navigation/MoveControl и cooldown атак; глобальный поиск игроков не добавлен. Test fixtures и клиентские сцены изолированы от production JAR.

## Переработка трёх боссов — 2026-10-09

Серверные TugarinCombat/CursedVolkolak сохраняют локальные state machines и ограниченные cooldown/LOS, без новых глобальных tick scans. WorldBoss применяет фазовую защиту в одной hurt-точке; native armor/iframes/щит остаются ванильными. Геометрия клиентских моделей и атласы воспроизводятся из Python-генераторов/JSON manifest; common не загружает рендереры. Клиентские/серверные QA sourceSets включаются только отдельными Gradle properties, production JAR проверяется на их отсутствие. Размеры задаются в EntityType и согласованных стойках; loaded-only BossAnchors проверяет место для увеличенного корпуса.

По отдельному ТЗ переработаны пропорции обеих форм Соловья, гигантский Тугарин/змеиная формаII с50% защитой и тройное вращение; Проклятый Оборотень получил Variant B геометрию/две стойки, повышенные скорость/урон/комбо и корректное видимое имя. Stable registry IDs/старый Волколак/квест сохранены. Clean build PASS;34/34 native GameTests,12396 model checks,21807 resource/production checks PASS.8 реальных запусков клиента (5 игровых сцен,2 меню,1 сбой общего build directory) /3 headless сервера; финальный клиент без ERROR/FATAL. Ручной осмотр игровых кадров выполнен; ручной survival/multiplayer/relog/MSPT NOT TESTED, Computer Use не получил кадр окна. Полная игровая приёмка остаётся открытой. JAR `build/libs/slavicmyths-1.5.1-boss-rework.jar`; параллельные изменения общей задачи сохранены, Коммит не выполнялся. По последующему запросу пользователя JAR установлен в тестовый PolyMC1.21.1: backup, один активный mod JAR, SHA-256 совпадает, миры/настройки сохранены; запуск после установки0. Квитанция: `verification/boss-rework-2026-10-09/polymc-installation.json`. [Изменения, доказательства и короткая проверка](verification/boss-rework-2026-10-09/REPORT.md).

## Generation-v2 1.5.2 — установленный тестовый кандидат, 2026-10-09

AbandonedPlan/Geometry/Structure/Piece строят конечные generation-v2 комплексы. ChudPlan v1 сохраняется; optional AbandonedPlan находится только в новых A+B. Старый NBT без поля не открывает ворота. Shared random-spread grid оставляет принятый A приоритетным, B использует отклонённую клетку; старые planners не зависят от A/B. Attached проходит точные v1 mineGate координаты и опускает пол до -54 относительно якоря; XZ union остаётся внутри исходной оболочки, Y piece расширяется только у A+B. ShaftProfile0/1/2 сохраняет высоту76/60/44 и четыре/четыре/три кольца; каждый профиль имеет две независимые лестницы. Все ориентации более высокого great проверяются до меньшего great, затем medium. Меньшие профили сохраняют квартал/галерею/42 узла лабиринта; спуск вынесен за галерею и имеет отдельный подход к узлам. Оболочки коридоров предшествуют расчистке путей; rails восстанавливаются на поддерживаемом полу, вода находится в замкнутых открытых чашах. Final attempt37:14/14 PASS, production/runtime921 classes identical; естественная квота ОТКРЫТА. Тестовый кандидат установлен; generation-v2 восстановление существующих starts впредь сохранять. Проверки имеют отдельные buildDirectory/project-cache-dir/game fixtures, production не содержит QA sourceSets. Vanilla /place пишет блоки без native StructureStart; поиск/inspect таких showcases не гарантируются. [Детали](verification/chud-1.5.2/REPORT.md).

## Подземный град Чуди — текущая архитектура 1.5.1, 2026-10-09

`org.slavicmyths.chud`: native codec/structure set/сериализуемый piece; immutable seed plan с 3/4/5 крупными залами и шестью уникальными ролями. Учётная область 279×118×279, native piece 255×118×255; восемь чанков native reference reach соблюдены. Spacing256/separation160 гарантируют минимум2298 по внешним XZ границам. Кэш noise columns ограничен8192 на invocation; общая конечная проверка чужих accepted footprints, без загрузки чанков. `chud_city` имеет низший приоритет в shared planner graph: старые структуры от него не зависят, новый город уступает их принятым геометрическим областям.

Процедурный finite blueprint сводов/домов/мостов сворачивается в конечный raster только заданного чанка перед записью. Bedrock и чужие block entities не заменяются. Администраторские `/sm city` читают загруженные native starts, ограничивают BFS100000 и отказывают небезопасному TP. Нет tick polling, retro-generation или inventory loot. Точный mineGateId/floor/facing сохранён в generation-v1 NBT; ворота запечатаны. Технические gates1.5.1 PASS; установлен тестовый1.5.2 без ретрогенерации, его worldgen-квота/ручная приёмка открыты. [Проверки и ограничения](verification/chud-1.5.1/REPORT.md).

## 1.4.3 — Проклятый Волколак, 2026-10-09

Продолжение по запросу пользователя завершено: прежний `volkolak` сохранён, отдельный `cursed_volkolak`/яйцо, Variant B/две позы, 400HP/атаки/хватка/Дурная кровь, четвёртое поручение/могила/личный череп/атомарная награда Финиста. 28 OGG/23 события с лицензиями. Clean build (isolated output), 156 финальных native GameTests, 1660 model checks и 15945 production/resource checks PASS. 16 headless loader запусков /0 клиентов, промежуточные ошибки исправлены. Установлена проверенная 1.4.3 в тестовый PolyMC, один активный JAR, backup; миры/настройки/старый 1.16.5 сохранены. Клиентская/survival/multiplayer/relog/restart/Xaero приёмка и измеренный MSPT открыты. [Отчёт](verification/volkolak_1.4.3/REPORT.md), [реализация](IMPLEMENTATION_MAP_1_4_3.md).

Собственный Monster/state machine + стандартные goals, synchronized move/start/phase/stance/held. OW SavedData с encounter по seed+WolfHome и персональными nonce; PlayerTick только для active quest каждые40 ticks, bounded loaded-only placement, без force chunks. Native passenger ≤20 ticks и cleanup; Scoped instant-heal mixin/food gain redirect сохраняют ванильные расчёты. Тайник добавляется только в воздух после раскопок и не заменяет постройки. Client renderer/UI/tooltips изолированы; packet/NPC/LOS/nonce validation и policy-filtered snapshots прежнего движка сохранены.

## Дополнение1.4.2 — 2026-10-08

GreyWolfStory использует Military.QUESTS attachment/copyOnDeath и четыре JSON definitions; действующая линейная серверная стадия + disclosure + pending inventory retry. WolfStoryNetwork проверяет NPC/LOS/range/trial/nonce и передаёт policy-filtered snapshots; common не ссылается на client. BurialRecords сохраняет main-boss clearII/III и реальные contributorUUID. HuntRecords/BossAnchors обеспечивают существующего Лихо, bounded index4096/noise128, loaded-only terrain, грубую персональную подсказку.

GreyPackRing сохраняет cooldown у игрока, использует forest_wolf и его native AI с локальным owner/TTL контролем. FinistQuiver хранит typed persistent/network component; Curios active slots читаются непосредственно без кратковременного кэша поиска. FinistArrow расширяет AbstractArrow: native ballistics/collision/enchants, ограниченное наведение, owner mark и один damage multiplier. Клиент: native408x218 Screen, старый живой renderer Волка, отдельный Curios body quiver mesh. Производственные output исключают opt-in fixtures. [Проверки и ограничения](verification/grey_wolf_1.4.2/REPORT.md).

## Исправления AI Волка и управления полётом — 2026-10-08

В общей версии 1.4.2.1 исправлены прыжки под кроной/в ствол, остаточное управление движением и самоурон Волка; снижение транспорта по умолчанию X, подсказка показывает сохранённые клавиши, Shift — высадка. Прежние registry IDs, art, урон атак и дальности сохранены. Clean build PASS; финальные 20/20 native GameTests и 10967 production/resource checks PASS; 6 headless запусков / 0 клиентов. Проверенная сборка установлена в тестовый PolyMC с backup и SHA-256, миры/настройки сохранены. Параллельные изменения версии сохранены; roadmap этой задачей не расширен. Клиентская приёмка конкретного дерева и клавиш открыта. [Отчёт и краткая ручная проверка](verification/wolf-flight-hotfix/REPORT.md), [установка](verification/wolf-flight-hotfix/polymc-installation.json).

## Проверка актуальности PolyMC — 2026-10-08

По запросу пользователя подтверждена установленная 1.4.1 в `Slavic-Myths-1.21.1-Testing` (Minecraft 1.21.1 / NeoForge 21.1.255). Повторный `gradlew.bat clean build` PASS, 22672 resource/production checks PASS; SHA-256 единственного активного JAR совпадает с новой сборкой. Замена не требуется, файлы экземпляра и миры не изменены; старый 1.16.5 сохранён. Код, версия и архитектура не менялись; roadmap не расширен. Реальных запусков в этой проверке: 0 клиентов / 0 серверов; игровая приёмка остаётся открытой. [Квитанция](verification/polymc-1.4.1-recheck.json).

## Серый Волк — 1.4.1 (2026-10-08)

Реализовано ТЗ пользовательского архива и яйцо `grey_wolf_spawn_egg`: самостоятельная модель30 частей/87 кубов,512px atlas, личное согласие и испытание500→50HP, пять атак, синхронный реальный вой/ограниченная стая, общий bleeding cap, постоянный NPC и персональная запись existing Journal/Xaero. Прежние Сапоги ветра сохранены. Будущие задания/награды1.4.2 не реализованы.

Серверный state machine + стандартные goals/entity data; safe loaded-only encounter, bounded candidate checks, без force loading. Seed probability.10 и квота≤40% подтверждённых natural Tugarin anchors (до3 таких якорей wolf отсутствует). Native pounce5.386/dash3.243blocks; wolf/bear визуальные размеры≈×1.99. Глобального обхода игроков/мира каждый тик нет. Истечение WOLF_CUT отложенно возвращает оставшийся слабый bleed через server TickTask; milk не восстанавливает удалённый эффект.

Итог: clean build и production/resource gates PASS;15 wolf +63 combat +23 unique native GameTests PASS,2116 wolf model +20491 rare +355 rune +147 armor +9176 existing models offline checks PASS.13 headless запусков /0 клиентов в этой итерации; промежуточные ошибки исправлены. Клиентская визуальная/звуковая приёмка, реальный relog/Xaero, natural частота и MSPT **не проверены**. Production установлен в тестовый PolyMC после gates с резервной копией прежнего JAR; игровые миры/1.16.5 сохранены. Commit не выполнялся.

[Реализация и ограничения](verification/grey_wolf_1.4.1/IMPLEMENTATION.md), [ручная приёмка](verification/grey_wolf_1.4.1/MANUAL_QA.md), [resources/JAR](verification/grey_wolf_1.4.1/resource_gates.json), [установка](verification/grey_wolf_1.4.1/polymc-installation.json), [звуки](verification/grey_wolf_1.4.1/SOUND_SOURCES.md). Ниже сохранена история.

## Разгон Сапог ветра — 1.3.4.5.5 (2026-10-08)

По запросу пользователя добавлен временный серверный заряд: 8 с спринта с прыжками повышают высоту от **4.154 до 12.461 блока**. Штатная физика и syncable JUMP_STRENGTH; дальность до ровного пола растёт от **7.133 до 11.492 блока**, максимальная дуга 36 тиков. Импульсы рассчитаны по высоте ×3; горизонтальная скорость и бонус отрыва не увеличены. Спад после остановки 1.6 с, стена сбрасывает заряд, lifecycle/equipment/flight/fluid/ladder/vehicle/teleport очищают состояние. Добавленная высота конкретного прыжка вычитается из fall distance однократно с ограничением по достигнутому пику; базовое снижение урона 40% сохранено. Registry IDs/рецепты/ресурсы внешнего вида сохранены; RU/EN и генератор актуальны.

`WindBootsMomentum`: O(1) PlayerTick.Post только владельца, без world/player scans, custom movement packets или сохранённого заряда. Атрибут меняется только по ступеням; микропроба 1000 синтетических образцов/handler 4.0043 мс, не MSPT. **1.3.4.5.4 → 1.3.4.5.5**; clean build PASS, 23/23 итоговых native GameTests, 19336 resource/JAR checks. **3 headless запуска / 0 клиентов**; один промежуточный сбой fixture исправлен. Production установлен в PolyMC `Slavic-Myths-1.21.1-Testing`, прежний JAR сохранён вне mods, SHA-256 проверен, 291 прочий файл экземпляра сохранён побайтно. Клиентская приёмка открыта; commit не выполнялся.

[Физика, поведение и проверки](WIND_BOOTS_MOMENTUM_1.3.4.5.5.md), [gates](verification/wind-charge-1.3.4.5.5.json), [установка](verification/polymc-1.3.4.5.5-wind-charge-installation.json). Ниже сохранена история.

## Сапоги ветра: дуга выше четырёх блоков — 1.3.4.5.4 (2026-10-08)

По запросу пользователя вертикальный импульс Сапог ветра изменён на 0.82 блока/тик (`JUMP_STRENGTH +95.238095%`). Штатная физика Minecraft даёт пик **4.154 блока**, дальность **7.133 блока с разбега и удержанием W** до ровного пола и 21 тик полёта. Native ServerPlayer.travel подтвердил расчёт и посадку на уступ высотой 4 блока. Скорость +40%, momentum +10%, снижение fall damage 40%, руны и registry IDs сохранены; RU/EN и генератор ресурсов обновлены.

Версия **1.3.4.5.3 → 1.3.4.5.4**. `gradlew.bat clean build` PASS; 19/19 native GameTests PASS; 19336 resource/production JAR checks PASS. **2 headless запуска сервера / 0 клиентов** для этой правки. Параллельные изменения боссов сохранены и отдельно записаны в audit. Production JAR установлен в PolyMC `Slavic-Myths-1.21.1-Testing`; предыдущая 1.3.4.5.3 сохранена вне mods, SHA-256 проверен, 273 прочих файла экземпляра сохранены побайтно. Клиент не запускался, ручная игровая приёмка открыта; commit не выполнялся.

[Расчёт и ограничения](WIND_BOOTS_JUMP_1.3.4.5.4.md), [gates](verification/wind-boots-1.3.4.5.4.json), [установка](verification/polymc-1.3.4.5.4-wind-boots-installation.json). Ниже сохранена история предыдущих итераций.

## Установка дополнения уникального снаряжения в PolyMC — 2026-10-08

По прямому запросу пользователя проверенный production `slavicmyths-1.3.4.5.3.jar` установлен в существующий `Slavic-Myths-1.21.1-Testing` (Minecraft 1.21.1 / NeoForge 21.1.255). Предыдущая 1.3.4.5.2 сохранена вне mods; активен один JAR, SHA-256 совпал с unique-equipment gates. 246 прочих файлов экземпляра, включая миры и настройки, сохранены побайтно. Версия исходников не менялась. Клиент/сервер при установке не запускались; старый экземпляр 1.16.5 не затронут. [Квитанция](verification/polymc-1.3.4.5.3-unique-equipment-installation.json). Ручная клиентская приёмка открыта; ниже предыдущие записи.

## Уникальное снаряжение — дополнение 1.3.4.5.3 без изменения версии (2026-10-08)

Реализованы Сапоги ветра, Нагрудник богатыря, Шлем ясного взора, Поножи неукротимости, Щит-оберег и Закалённое железо. Пять upgrades существующего верстака сохраняют компоненты и долю износа; ёмкость рун 3. База сапог — Скороходы; чёрное перо из стана одобрено пользователем. RU/EN, нативные sprites/worn models, Creative/Search и optional JEI integration готовы. Версия **1.3.4.5.3 → 1.3.4.5.3**.

Clean build PASS; 17/17 итоговых native GameTests, 147 armor +355 rune offline, 19336 resource/JAR checks. 8 headless servers/0 clients; новый процесс проверил 5 stacks/2 КД в player .dat. Это не клиентский relog/визуальная приёмка/MSPT. Production `build/libs/slavicmyths-1.3.4.5.3.jar`; PolyMC/клиент не запускались, установка/commit/push не выполнялись. Внешние параллельные изменения боссов сохранены и отделены в machine audit. **REQUIRES_MANUAL_CLIENT_CHECK**: [отчёт](UNIQUE_EQUIPMENT_COMPLETION.md), [ручная приёмка](MANUAL_VISUAL_CHECK.md), [gates](verification/unique-equipment.json). Ниже предыдущий статус/история.

`UniqueEquipment` регистрирует отдельные armor materials/5 equipment/1 material; `UniqueEquipmentRules` централизует числа и чистые решения. `UniqueEquipmentEvents` использует damage/equip/health/control/knockback events; единственная новая player tick проверка — pending локальный 5-секундный таймер (constant work, без world/entity scans). Health mixin даёт немедленный threshold refresh с constructor guard. Block mixin читает accepted absorption из DamageContainer после всех subscribers, запрещает reflect recursion. ClearSight C2S принимает только intent; сервер проверяет шлем/КД и один loaded radius query при активации. S2C UUID/ID/dimension targets передаются только активировавшему; клиентский Minecraft outline mixin, без server Glowing. КД — существующий PathData persisted NBT/overworld clock; lifecycle снимает transient visuals/buff, сохраняя ready timestamps. Upgrade дополняет существующий ArmorerRecipe только wear-ratio mapping. Existing rune component/GUI, optional JEI isolation, registry IDs/Classes сохранены. Resource overlay воспроизводим; opt-in test source set не входит в production JAR.

## Boss reimplementation 1.3.4.5.3

`BossReworkGeometry` генерируется из четырёх независимых manifest. WorldBossModel
сохраняет отдельную ветку Лихо; renderer Тугарина выбирает два новых экземпляра.
NightingaleModel имеет два разных rig; четыре собственных atlas и native ModelPart
проверяются offline без Minecraft client. Geometry preview — авторская проекция,
не игровой render и не визуальная приёмка.

Соловей хранит tracked Phase/ACTION/START и persisted Phase/Cooldowns; переход
40 ticks / DR 75%. Тугарин использует прежний отдельный finite scheduler,
теперь raw melee 12, combo 5+5+8 и усиленные specials. Local threat ≤32,
reselection 10 ticks / lock 30 / expiration 200; никаких глобальных tick scans.
В VillagerHostilesSensor расширяется настоящий `isMatchingEntity`; отдельный
golem vanilla target goal ограничен только Тугарином. Соловей реализует Enemy.

WindImpulse использует native knockback и ограниченный минимальный наружный
velocity после resistance. `wind_immune` — mod tag; большой MAX_HEALTH не
означает иммунитет. Кинжал: fixed-width 8-block wave, 7 damage через native
MOB_PROJECTILE, NONE animation. Draw — LivingEquipmentChangeEvent.

Итоговые изолированные gates 1.3.4.5.3 пройдены: 40 boss + 63 combat tests,
9176 native model checks, 20491 rune checks. В установленной 1.3.4.5.5
37 файлов boss code/assets и 39 sound events совпадают с проверенным JAR.
Снаряжение параллельной задачи не редактировалось. Клиентская приёмка ручная.

## Общий Boss Rework — 1.3.4.5.2, этап1 (2026-10-08)

Первый атомарный этап нового архива Тугарин+Соловей: кинжал wind range6.5/base impulse4/visible arcs; прежний melee10/SPEAR/CD7с сохранён.39stable boss SoundEvent keys/aliases; оригинальные OGG неизменны. Полный rework **не завершён**: две фазы/новые модели/способностиСоловья и новыеdamage/combo/boss-heldbladesТугарина остаются в [STATUS_HANDOFF](../STATUS_HANDOFF.md). Механики1.3.4.5.1 сохранены.

Clean build PASS,63/63GameTests,20491pure rune,13503resource gates;1server/0clients.1.3.4.5.2 установлена в существующийtestPolyMCбеззапуска;765прочихфайловсохранены. REQUIRES_MANUAL_CLIENT_CHECK. [Sound notes](SOUND_PRODUCTION_NOTES.md), [manual](MANUAL_CHECK_BOSSES.md), [gates](verification/boss-stage1-1.3.4.5.2.json). Commit/push не выполнены. Ниже история.

## Тугарин Змей — 1.3.4.5.1 (2026-10-08)

Две формы и единичная трансформация при50% HP: отдельный серверный scheduler, finite DR85% на3сек, устойчивая aggro, четыре phase-two abilities, отдельная геометрия Змея с крыльями и разные звуковые профили.350HP без heal, raw melee6/6.6, phase-two speed+23%; hitbox1.3×2.35. Прежние трофеи сохранены, pair drop12%, natural region probability25% (один multiplier, применён и к прежним anchors). Дополнены alternating pair slash/sheath sound; offhand escrow/native bleed сохранены.

Clean build PASS; итог21/21Tugarin +61/61combat regression,20491pure rune,9772native model bake,11357resource gates. Реальных запусков5servers/0clients; первый11/14, последующие PASS. **REQUIRES_MANUAL_CLIENT_CHECK**: модели/анимации/звук/боевые окна не проверены клиентом.1.3.4.5.1 установлена в существующий тестовый PolyMC1.21.1 без запуска;765 остальных файлов сохранены. Commit/push не сделаны; пользовательские и прежние незакоммиченные изменения сохранены. [Отчёт](TUGARIN_REWORK_1.3.4.5.1.md), [ручная приёмка](MANUAL_CHECK_TUGARIN.md), [gates](verification/tugarin-1.3.4.5.1.json), [установка](verification/polymc-1.3.4.5.1-installation.json). Старые разделы ниже — история.

TugarinCombat работает на том же WorldBoss UUID; stage/current/previous Move синхронизированы vanilla EntityData. Threat bounded32/expiry200/reeval10/lock30; поиск игроков local AABB32 только без цели. Лихо использует прежний Fight. Клиент выбирает earth/serpent ModelPart по stage+animationTime; обе геометрии используют прежний atlas. Production не сканирует мир, не force-load chunks и не включает полёт/noGravity. Opt-in GameTests/model bake не входят в JAR.

## Актуальная версия 1.3.4.4 — 2026-10-08

Реализованы CombatTargeting, исправления Flight/Floating/Likho/Death/Net, knife Loyalty и звук, утверждённый trophy damage10/12/12/10/10 (Pool Spear9), один stack парных клинков Тугарина с безопасным offhand escrow и render-only вторым клинком. Vanilla/material weapons и registry IDs сохранены. Clean build PASS,61/61GameTests,20491pure assertions,6591resource checks. Реальных запусков4servers/0clients; ручной renderer/keybind/sound check не выполнен.1.3.4.4 установлена в 1.21.1 test PolyMC,765прочих файлов сохранены. Commit/push не выполнены. [Полный отчёт](COMBAT_RUNE_FIXES_1.3.4.4.md); [установка](verification/polymc-1.3.4.4-installation.json). Более ранние разделы ниже — история.

CombatTargeting переиспользует village_hostiles и explicit protected/control tags. Pair ownership — one ItemStack + persisted offhand escrow; второй клинок существует только в client layers/context models. Projectile Returning state сохраняет carried ItemStack, Loyalty-specific admission не включает прочие trident enchants. Production не грузит client classes и не force-load chunks;3×3chunk loading только server fixture без connected player.

## Редкие руны — 1.3.4.3 (2026-10-08)

Реализованы шесть rare runes, четыре именных fragment, четыре восстановления через существующую Rune Anvil и два stage-3 обмена Яги. Источники: Соловей (3 гарантированных flight fragments, одноразовый бой на стан), Тугарин/Лихо/курганный воевода (1 гарантированный fragment); 25% extra, 4% full rune. Старая rune_protection сохранена, новая — rune_rare_protection; прежние IDs/loot pools/runes I–II/Classes Screen и HUD layout сохранены.

RareRuneRules/Runtime/Network: server authority, persisted player cooldown по overworld clock, одна общая R-кнопка (Shift — выбор), временный полёт и safe cleanup, execute immunity/tag и exact health-cost marker, Berserker/Sacrifice buffs. RuneVisual — одна noSave/non-AI entity с tracked item/count/phase; own client renderer, shields/crack/fade и лёгкий configurable edge overlay. Архитектура/параметры/файлы: [отчёт](RARE_RUNES_1.3.4.3.md).

Clean production build PASS. Offline rare20491, ordinary355, foundation75064, armor147; production resource/preservation audit PASS, сохранены 4931 resources. Итоговый JAR build/libs/slavicmyths-1.3.4.3.jar; [машинная проверка](verification/rare-runes-1.3.4.3.json). Реальных клиентов/серверов 0/0 по запрету пользователя: игровой lifecycle, flight/render/PvP/JEI/dedicated smoke остаются непроверенными. MSPT не измерен. PolyMC остаётся 1.3.3; полная переделка Classes GUI по-прежнему ждёт утверждения. Future frost/clone/III upgrades/new bosses не добавлены; commit не создан. Записи ниже исторические.

## Rune System 2.0 — 1.3.4.2

Восемь рун I–II реализованы: existing IDs heat/wind переиспользованы, шесть новых; RuneBalance централизует diminishing returns/caps, RuneRuntime применяет серверные эффекты и transient modifiers по событиям. Старые особенности heat/wind сохраняет persistent/network legacy_rune_specials; existing RUNES и registry IDs сохранены. RuneCraftRecipe проверяет base component, GUI/JEI используют counted inputs. Кровотечение: 0,5 HP, rune max2/native max3, совместный max3 по ответу пользователя. Силуэты физических камней обновлены.

Clean build PASS; offline 355 + armor147 + foundation75064; resource gates22838, preserved4898. Production build/libs/slavicmyths-1.3.4.2.jar. Реальных запусков клиент/сервер 0/0: игровая приёмка открыта, запрет пользователя сохраняется. Classes GUI ждёт утверждения, PolyMC остаётся 1.3.3. Rune III и следующие milestones не реализованы. [Полный отчёт](RUNES_REWORK_1.3.4.2.md).

## Rune Foundation 1.3.4.1

`RuneFoundation` регистрирует 14 предметов в ModItems и variants в существующей magic tab. `RuneBase` — immutable tier/material с валидированным Codec; `ItemState.RUNE_BASE` persistent/network component, независимый от неизменённого installed `RUNES`. NeoForge GameData registration order (установленные исходники) ставит DATA_COMPONENT_TYPE перед ITEM, поэтому defaults пустых рун получают зарегистрированный component type.

`RuneCraftRecipe` (`slavicmyths:rune_crafting`) — ordered counted Ingredient slots, JSON/registry packet serializer; все рецепты в ресурсах. Existing `RpgMenu` ID сохранён: старые 2 inputs, 4 creation inputs, output и inventory; режим/выбор DataSlot. Выдача output через authoritative Slot.remove сначала оплачивает целую партию; paid remainder возвращается при закрытии, unpaid preview нет. Full-inventory Shift-click preflight, запрет output SWAP/CLONE/PICKUP_ALL, отсутствие новых C2S payload. `RuneAnvilScreen` только client; старый RpgScreen для path stone остаётся. `JeiRunes` client-only optional compat, subtype по RuneBase.

Смерть исходного UPYR обрабатывается LivingDropsEvent без scans; persistent RuneSummoned исключает осколки с summons. KurganInstance хранит RunePopulation: новые +1 UPYR через прежний bounded roster/admissibility, old saves false сохраняют состав. Soul boss drops — добавочные loot pools. Worldgen и эффекты рун не менялись. [Отчёт](RUNE_FOUNDATION_1.3.4.1.md): build/offline PASS, runtime не запускался; GUI-иллюстрация не является игровым screenshot.

## Classes 2.0 — исправление, этап макетов на утверждение (2026-10-08)

По новому пользовательскому ТЗ выполнена диагностика затронутого UI и подготовлены пять PNG-макетов, дополнительный вид направлений без модалки и 40 resolution/state snapshots. 1344 проверки предлагаемой геометрии/bitmap glyphs PASS; это не проверки рабочей реализации. Проверены реальные prerequisites всех 34 навыков; KeyMapping snapshot — ЛКМ/H/J из options.txt, файл не изменён. Макеты и причины дефектов: [проект на утверждение](CLASSES_UI_REDESIGN_APPROVAL.md).

Ожидается утверждение согласно §18.2: «До моего утверждения не приступать к полной переделке GUI». Production GUI, ресурсы/механики, JAR и PolyMC на этом этапе не менялись; установленная версия остаётся 1.3.3. Клиент/сервер не запускались, сборка исправления ещё не выполнялась. Предлагаемый HUD18/right8/bottom8 требует утверждения: найденный в backup1.3.1 старый HUD24/Y=H−74 расходится с новыми координатами ТЗ. История Git для untracked ClassClient отсутствует. Будущая реализация, тесты её ввода/камеры и установка ещё не выполнены.

## Установка 1.3.3 в PolyMC — 2026-10-08

По прямому запросу пользователя production 1.3.3 установлен в существующий `Slavic-Myths-1.21.1-Testing` (Minecraft 1.21.1 / NeoForge 21.1.255). Включены парные мечи Тугарина и остальное редкое оружие 1.3.2.2, Classes UI и рунные слоты брони. Предыдущая 1.3.2.3 сохранена вне mods, активен один JAR, SHA-256 установленной копии совпал; 542 остальных файла экземпляра (включая миры/настройки) побайтно сохранены. Клиент и сервер не запускались. [Квитанция](verification/polymc-1.3.3-installation.json). Записи ниже об отложенной установке исторические.

## 1.3.3 — Рунные слоты брони (2026-10-08)

Существующая Runes.capacity расширена на player ArmorItem: leather/семимильные сапоги 0; chainmail/gold/iron/turtle 1; diamond/netherite 2; perunite 3; gambeson/plate 1. Одежда без ArmorItem, аксессуары и animal BODY исключены. Armor max строго ограничен материалом; grandfathering прежних оружейных ячеек сохранён. Наковальня использует прежние открытие ячеек/установку/удаление, общий лимит и проверку дубликатов. Tooltip/GUI показывают руны, открытые ячейки и отсутствие эффектов на броне. Боевые характеристики и эффекты не добавлены.

`gradlew.bat clean build` PASS; 147 офлайн-проверок capacity/admission/RuneState JSON/NBT/packet, 10 542 проверки ресурсов/JAR PASS. По сравнению с production 1.3.2.3 изменены только пять классов Runes/RuneDefinition/RpgEvents/RpgScreen/RpgClient; 4 862 прежних ресурса побайтно сохранены, RU/EN добавлены только две строки. Новых registry ID нет. ItemState/RuneRepair, материалы/предметы/рецепты/текстуры/классы/HUD не менялись.

По прямому запрету пользователя в этой итерации 0 клиентов и 0 серверов. Полный native ItemStack harness остановился на offline bootstrap: FeatureFlagLoader требует LoadingModList; обходов в production нет. JSON/NBT roundtrip компонента не считается игровым relog. Смерть/respawn/relog/restart/dimension/drop-pickup, реальная починка/отделка и GUI не проверены в игре. Копирование компонентов vanilla-процессами сверено по установленным зависимостям, без заявления runtime PASS.

Production `build/libs/slavicmyths-1.3.3.jar`; в PolyMC в рамках этого задания не устанавливался, там остаётся 1.3.2.3. Пользовательские изменения/миры сохранены; commit не выполнялся. [Таблица, файлы и приёмка](ARMOR_RUNE_SLOTS_1.3.3.md), [gates](verification/armor-runes-1.3.3.json). Предыдущие записи ниже исторические.

`Runes.capacity(ItemStack)` сначала проверяет ArmorItem и material registry key; `armorCapacity` содержит единую таблицу. `max/slots/list` переиспользуют результат; `installationReason` — общая чистая admission rule, `installationBlocker` адаптирует ItemStack и Component для server/preview. `category` остаётся только оружейной; guard life-on-kill исключает броню в руке. Все восемь существующих рун могут храниться на броне, но RuneEffects/attributes не расширены. ItemState.runes сохраняет прежний persistent/network-synchronized компонент slavicmyths:runes; operation меняет только его на исходном stack, preview использует copy. Weapon max grandfathering сохранён; armor capacity не повышается старым/внешним значением slots. Offline fixture source set подключается только через -ParmorOfflineCheck, не входит в production и не создаёт NeoForge runs.

## 1.3.2.3 — Classes 2.0: интерфейс (2026-10-08)

Переработаны существующие классы: дерево по фактическим prerequisites, крупный корень, состояния узлов и ранги, правая карточка с текущими/следующими параметрами и точными требованиями, компактные 3 active + 1 passive. Выбор класса: временный 3D-preview собственного игрока, три карточки с ролью, подробности и подтверждение. Нативные 32px иконки, 64px гербы, деревянные GUI sprites; blur отключён. Управление использует прежние Minecraft KeyMapping и сохраняет options.txt. HUD справа снизу; единственный действующий переключатель — Mods → Slavic Myths → настройки, клиентский config по умолчанию включён. Creative вручную изучает навыки своего класса без требований и расхода очков; пределы рангов сохранены, Survival проверяет обычные требования.

`gradlew.bat clean build` PASS (14 предупреждений deprecated/unchecked), 3 710 офлайн-проверок правил/геометрии и 530 проверок ресурсов/JAR PASS. Проверены границы для 1920×1080/2560×1440, GUI scale 2/3/4; это не проверка игровой отрисовки. В этой итерации 0 запусков клиента и 0 сервера, реальные relog/смерть/механики и FPS/MSPT не проверялись. Существующие сохранение Class2, lifecycle events, баланс, названия, ветви и registry IDs сохранены.

Production `build/libs/slavicmyths-1.3.2.3.jar` установлен в существующий PolyMC `Slavic-Myths-1.21.1-Testing`. Предыдущая 1.3.2.1 сохранена вне mods, установлен один JAR, SHA-256 совпал; 539 остальных файлов экземпляра, включая миры и настройки, побайтно сохранены. Старый экземпляр 1.16.5 не затронут. Commit/push не выполнялись. [Детали и ручная приёмка](CLASSES_UI_1.3.2.3.md), [gates](verification/classes-ui-1.3.2.3.json), [установка](verification/polymc-1.3.2.3-installation.json). Более старые записи об установленной версии ниже исторические.

`ClassTreeLayout` строит дерево из существующего ClassDefinitions; `ClassTheme` масштабирует полные UV и рисует nine-slice кнопки. `ClassLearningRules` общий для UI и серверного ClassState; сервер повторно проверяет текущий gamemode при каждом изучении. `ClassHudConfig` — один client ModConfigSpec без загрузки клиентских классов в common. `ClassSettingsScreen` подключён через IConfigScreenFactory. Preview меняет только предметы в руках на время рендера с восстановлением в finally. Протокол, NBT и события сохранения не менялись; legacy NBT Hud больше не управляет клиентской видимостью. Нового server tick polling нет. Генераторы старых class/integration ресурсов вызывают актуальный UI overlay.

## 1.3.2.2 — Редкое оружие (2026-10-08)

Реализованы парные мечи Тугарина, посох Лихо, кистень атамана и копьё победителя: разные силуэты, серверные механики, максимальная ёмкость 3 руны, 4 рецепта оружейного верстака, RU/EN. Существующие дропы Тугарина/Лихо и курганные трофеи переиспользованы; добавлен только гарантированный знак атамана дополнительным pool. Два меча крафтятся одновременно и сохраняют данные каждого донора отдельно; второй остаётся в ячейке верстака. Броня/новые руны/боссы не добавлены.

`gradlew.bat clean build` PASS; 15,935 ресурсных/JAR-проверок PASS, 4,618 прежних ресурсов побайтно сохранены, остальные изменения старых ресурсов только добавочные. Финальные headless suites: новые 16/16 + прежнее оружие 16/16. 11 реальных серверных запусков, ноль клиентов; промежуточные failures сохранены в отчёте. Проверены нативные крафт/ремонт/подбор, смерть, два dimension transitions, реальные boss drops и player .dat новым серверным процессом (4 оружия + 2 КД). Это не реальный клиентский relog; внешняя ручная приёмка остаётся открытой, FPS/MSPT не измерялись.

[Параметры, рецепты, файлы и краткая ручная приёмка](RARE_WEAPONS_1.3.2.2.md), [acceptance](verification/rare-weapons-1.3.2.2-acceptance.json), [ресурсы/JAR](verification/rare-weapons-1.3.2.2-resources.json). Production: `build/libs/slavicmyths-1.3.2.2.jar`. PolyMC остаётся на установленной 1.3.2.1; клиент/установка не запускались. Пользовательские изменения и игровые миры сохранены, commit/push не выполнялись.

Архитектура патча: `RareWeapons` регистрирует 4 оружия/1 материал; `RareCombat` использует AttackEntityEvent и damage/target/player events. `RareBleedEffect` имеет отдельную нативную cadence, `DisorientationEffect` — только effect-local bounded target search и стандартный блокирующий AI goal. Нет нового глобального world/player tick polling. Второй выпад расходует UUID/dimension-bound окно до штатного Player.attack; только его attackStrengthTicker открыт access transformer. Долгие КД сохраняются в существующем PathData по overworld clock, transient окна очищаются при login/respawn/dimension. ArmorerRecipe возвращает второй нестакающийся клинок в input remainder со своими donor components; RuneRepair и существующий main-hand RuneEffects переиспользованы. `-PrareRuntimeCheck` подключает изолированные fixtures, не входящие в production JAR. Генераторы сохраняют текущий rare overlay.

## Установка 1.3.2.1 в PolyMC — 2026-10-08

По запросу пользователя проверенный JAR установлен в существующий `Slavic-Myths-1.21.1-Testing` (Minecraft 1.21.1 / NeoForge 21.1.255). Предыдущая 1.3.1 сохранена вне mods; SHA-256 установленной копии совпал. Активен один JAR мода, 439 сопутствующих файлов (включая миры и настройки) сохранены побайтно. Клиент не запускался. [Квитанция установки](verification/polymc-1.3.2.1-installation.json). Записи об отложенной установке ниже исторические.

## 1.3.2.1 — Оружие 2.0 (2026-10-08)

Реализованы 30 форм оружия: копья, сулицы, кистени и метательные ножи семи материалов, короткий и тяжёлый луки. 53 новых предмета включают детали; три прежних оружейных ID сохранены. 36 рецептов оружейного верстака и 23 рецепта деталей; просмотр рецептов работает без JEI. Разные силуэты, ru_ru/en_us, серверные броски/подбор, сохранение данных и совместимые ремонты. Централизованы ёмкость рун и категории существующего оружия; ранее открытые слоты сохранены.

Финальный `gradlew.bat clean build` PASS; 15 067 проверок ресурсов/JAR PASS, 2 566 прежних ресурсов сохранены побайтно. Итоговые headless suites: оружие 16/16, классы 15/15. 11 реальных серверных запусков, ноль клиентов. 30 оружейных стеков проверены после загрузки player .dat новым процессом; проверены смерть и два нативных перехода между измерениями. 512 столкновений ножа: 117 потерь при заданной вероятности 25%.

[Реализация, характеристики и рецепты](WEAPONS_1.3.2.1.md), [отчёт и ограничения](verification/weapons-1.3.2.1-acceptance.json), [ручная приёмка](MANUAL_QA_WEAPONS_1.3.2.1.md). Клиентский вид, GUI, бой нескольких игроков и реальный выход/перезаход остаются ручными; FPS/MSPT не измерялись. PolyMC остаётся на проверенной 1.3.1; установка новой версии, commit и push не выполнялись.

`WeaponCatalog` централизует материалы/регистрацию; `ThrowingWeaponItem` и `WeaponProjectile` используют нативный серверный перенос ItemStack, сохранение и однократный подбор. `FieldBowItem` сохраняет стандартные ammo/enchantment hooks. `Runes` определяет категории/ёмкость; `RuneRepair` защищает данные при ремонте. Существующий `ArmorerRecipe` поддерживает перенос компонентов при улучшении. Рендерер, bow properties и браузер рецептов изолированы на client side. Нет глобального tick polling или chunk loading ради оружия. Opt-in `weaponCheck` и его ресурсы не входят в production JAR. Версия class network 1.3.2.1 требует одинаковой версии клиента/сервера, включая новый формат рецепта.

## 1.3.1 — Классы 2.0 (2026-10-08)

Production JAR установлен по ранее зафиксированному пути тестового PolyMC 1.21.1 после gates, с резервной копией 1.2.5 и проверкой SHA-256; зависимости, конфигурация и миры сохранены. [Квитанция](verification/polymc-1.3.1-installation.json).

Реализованы дружинник, ведун и разбойник, 18 ветвей, 28 основных навыков и шесть эволюций. Отдельные class XP/очки, серверные требования, три активных слота G/H/J и один пассивный, триггеры, экран K/камня пути, HUD и 34 разные pixel icons с ru_ru/en_us. Миграция прежних путей сохраняет данные/КД; нативные смерть, /kill и загрузка player .dat новым процессом проверены. API/debug `/smclass`; массовые источники class XP не добавлены.

Clean build PASS; ресурсы/воспроизводимость/JAR PASS. Итоговые серверные suites 15 + 53 + 8 + 6 = 82/82; 25 реальных headless запусков этой итерации, ноль клиентов. Промежуточные ошибки сохранены в [машинном отчёте](verification/classes-1.3.1-acceptance.json). Пользовательские изменения городских построек сохранены. Commit/push не выполнялись; игровые миры не перезаписывались.

[Реализация и ограничения](CLASSES_1.3.1.md). Ручная приёмка GUI scales/F1/keybindings, двух клиентов, боя и реального выхода в меню/перезахода остаётся открытой; FPS/MSPT не измерялись. [Краткий список](MANUAL_QA_CLASSES_1.3.1.md).

Class2 хранится внутри прежнего SlavicPaths; LegacyPaths сохраняет исходный compound. ClassDefinitions/ClassBalance централизуют требования и численные параметры; ClassState выполняет серверные переходы/идемпотентные атрибуты. ClassNetwork принимает только намерения; ClassRuntime/ClassEvents используют нативный бой/столкновения и ограниченные таймеры. ClassClient/ClassScreen изолированы на client side. Нет глобального поиска по миру/игрокам или chunk loading ради способностей; test source set включается только opt-in и не входит в production JAR.

## 1.3.1 — Воспроизводимые интерьеры и сохранённые уличные фонари (2026-10-08)

`city_furnishing.py` формирует реальные Items NBT для table_display / household_storage, обычное оружие в weapon_rack и десять тематических lazy loot tables. Геометрия башни использует полный 9×9 footprint / цельные стойки; окна однородны, узкие проёмы учитывают дверь; перед лестницей два ряда пола. `CityRoadPiece` хранит StreetLights, индексирует отдельные компоненты по чанкам и выполняет только bounded placement. Старые saved pieces без StreetLights совместимы. `CityPlacement` включает столбы в protected-block-entity preflight. Нет новых tick scans / AI chunk loading; расставленные полки / столы / неактивные хранилища не тикают.

[Изменения и доказательства](CITY_POLISH_1.3.1.md), [SHA / runtime receipt](verification/city-polish-1.3.1.json). Clean build / три production resource gate PASS; 8 + 6 + 53 = 67/67 финальных серверных проверок. Одиннадцать реальных headless запусков / ноль клиентов; ранние failures документированы. Установленный PolyMC JAR не заменялся автоматически, commit не создавался.

## Установка 1.2.5 в PolyMC — 2026-10-08

По прямому запросу пользователя проверенный production JAR 1.2.5 установлен в `Slavic-Myths-1.21.1-Testing` (Minecraft 1.21.1 / NeoForge 21.1.255). Предыдущая 1.2.4 сохранена вне mods; SHA-256 копии совпал, активен один JAR мода, восемь сопутствующих файлов / настроек не изменились. Клиент не запускался. [Квитанция](verification/polymc-1.2.5-installation.json). Прежние записи об отложенной установке ниже исторические.

## 1.2.5 — Maintenance boundaries (2026-10-08)

`VillageWork.validJob` short-circuits unloaded chunk before `PoiManager.getType`; loaded behavior/cooldowns/IDs unchanged. No new cache, tick polling, worldgen rarity or art changes. [Code audit](maintenance/PERFORMANCE_AUDIT_1.2.5.md).

Xaero compileOnly uses immutable `maven.modrinth:xaeros-minimap:Q1tuMQBB` (NeoForge1.21.1/26.5.0) through scoped Maven repository, replacing ignored local .tools JAR without bundling. Independent empty-cache source build produces identical production JAR. Test-only fishing uses daylight batch and keeps strict physical assertions; no tests in production.

Old run-* QA data is preserved outside checkout; broad anchored ignore prevents new runtime staging. Git index cleanup affects runtime/generated files only; gc preserves history/reflogs and does not prune. README media is tracked separately and explicitly schematic. Current verifiers/resource IDs remain canonical; duplicates were inventoried, not deleted without full orphan proof.

Final headless suites65/65 and actual restart assertions PASS;8 new headless launches/0 clients. Git publication and remote release clone wait for direct authorization; source-export check is distinct. [Evidence](maintenance/RELEASE_PREPARATION_1.2.5.md).

## 1.2.5 — Native city pieces и пятислотовый мешок

`gorodishche/CityCatalog` читает immutable snapshot ресурсов при reload; `CityPlan` строит дороги перед участками, фиксирует фасад / красную линию / размеры / взвешенные варианты. `GorodishcheStructure` использует настоящую native StructureType с ограниченным отбором рельефа и общим ациклическим контролем пересечений. `CityBuildingPiece` / `CityRoadPiece` сохраняют нативные pieces и размещают только часть своего чанка; фундаменты — общий `LandFoundationPiece`. Маркеры NPC имеют детерминированные UUID / сохранённые обработанные биты, JOB_SITE получает ванильный Brain. `CitySites` ищет город только при старте существующего нападения, используя загруженные references / `ManualStructureRecords`; AI не загружает чанки.

`CityCommands` предварительно проверяет assets / высоту / границы / block entities, готовит временные tickets, размещает всю геометрию, затем NPC и освобождает tickets в finally. Showcase и native generation используют один каталог / план / pieces. `VillageFoundation` выбирает нижний несущий блок столбца и ограниченно продолжает его материал в пустоте, не заменяя авторские сплошные блоки. `SackMenu` содержит пять produce-only слотов / обычный player inventory; `SackScreen` регистрируется только на клиенте. Содержимое сохраняется стандартными Items, старый излишек хранится в SackOverflow; fill = min(5, total/64). Нет новых глобальных tick scans.

Генераторы выполняют городской слой последним, сохраняют исправления sack / beer и воспроизводят ресурсы побайтно. GameTests / QA namespace opt-in и исключены из production. Clean build / resource / JAR gates и 65 серверных проверок PASS; 19 реальных серверов / 0 клиентов. [Подробности и границы доказательств](GORODISHCHE_1.2.5.md), [runtime](verification/gorodishche-1.2.5-runtime.json).

## 1.2.4 — Authored village templates and explicit QA placement

Deployment update 2026-10-07: installed the existing verified production artifact in PolyMC `Slavic-Myths-1.21.1-Testing`, retaining a backup of 1.2.3 and validating all eight companion/configuration files. [Receipt](verification/polymc-1.2.4-installation.json). Architecture/source unchanged, zero new game launches; the earlier installation deferral below is historical.

`tools/village_buildings.py` emits catalog-driven NBT, native pool overlays, biome-specific centers/streets/zombie variants, themed chest loot and a mapping audit. Prior village/military generators invoke it last. Original pool weights and native street graphs remain; outward house sockets permit larger geometry. Existing barracks ID remains, and its native per-assembly limit recognizes both old/new IDs. `VillageFoundation` is a registered stateless placement processor: bounded eight-block local footings, bedrock preservation, no polling or chunk forcing.

`VillageShowcase` registers operator commands on the shared tree and reads the resource catalog. It resolves actual templates, prevalidates bounds, supports negative Y, loads surface chunks before height reads, preserves exterior ground and releases explicit temporary QA tickets in `finally`. Template entities are suppressed only for QA placement; authored livestock is restored and professional villagers use real native POIs. No JOB_SITE assignment shortcut is used. Server/common code has no client dependency. Test-only `slavicmyths_buildings` resources and harness are opt-in and excluded from production. [Verification and limits](VILLAGE_BUILDINGS_1.2.4.md): final 4/4 building and 53/53 regression checks PASS; zero client launches, PolyMC unchanged.

## 1.2.3 — Bandit aggression / golem defense hotfix

See [fix and verification](MILITARY_1.2.3_BANDIT_HOTFIX.md). Ordinary bandits target residents/golems; melee routing no longer stops outside attack reach. Stronghold player alarms remain intact. Golem bandit detection uses 32 blocks without changing other native targets. Clean build, production/resources and final 53/53 server checks PASS; corrected JAR installed in PolyMC with backup. Zero client launches, no commit/push.

## 1.2.3 — Native guard / persistent military architecture

See [implementation and verification](MILITARY_1.2.3.md). Native Villager guard profession, persistent military board/quests, bounded camp raids and modular rare barracks are implemented atop 1.2.1/1.2.2. Clean build, production/resource validation, geometry and final 49/49 isolated server tests PASS; real save/load assertions PASS. Installed 1.2.3 in the PolyMC testing instance with backup and hash validation; zero client launches. Manual visual acceptance is pending. Calm neutral spirits are excluded until aggression, by explicit user decision. No automatic commit/push.

GuardBehavior extends native CORE packages; ResetProfession is guarded only for the 1200-tick reassignment interval. The ordinary worker pickup uses native GoToWantedItem within four blocks. MilitaryBoards and BanditRaids use dimension SavedData; player CompoundTag quest attachment and saved/synced archetype persist separately. Network actions validate a live near-table menu on the server. Client GUI/equipment stay in Dist.CLIENT classes. Active raid maintenance is bounded to active records every 200 ticks; active quest visits every 20 player ticks. Camp discovery runs only on loaded existing leaders; no production chunk forcing. Five copied vanilla village pools preserve baseline elements and append one rare template; jigsaw placement filters a second barracks per assembly.

## 1.2.2 — Native WORK and physical storage architecture

See [implementation and verification](WORK_AI_1.2.2.md). Native WORK behavior, a 64-slot settlement chest and existing production engines implement this iteration. Weaver remains flax-only by explicit user decision. No future settlement systems were scaffolded. Clean build, resource/production gates and final 30/30 isolated server tests PASS; separate real save/load phases PASS. Installed 1.2.2 in the PolyMC testing instance; zero client launches. No commit/push.

## 1.2.1 Village extension architecture

Common `village/VillageRoles` registers six VillagerProfession and PoiType entries with DeferredRegister. NeoForge registry callbacks install state→POI mapping automatically; vanilla acquirable_job_site/village tags are appended. Only drying-rack master states count, so a multiblock has one owner. No custom villager entity or workstation scan is introduced. `MillstoneBlock` is the approved compact workstation-only fallback; existing flour recipes remain intact.

`VillageTrades` handles NeoForge VillagerTradesEvent for these six IDs only. Bundled `data/slavicmyths/village_trades/*.json` supplies four listings for each of five levels. These are bundled configurable source data, not a new datapack reload framework. Vanilla Villager.updateTrades selects two; native MerchantOffers serialization/restock/XP/demand/discounts own the result. Vanilla and wandering-trader pools are untouched.

`OutfitRules` is pure UUID selection and villager-type→climate mapping. `VillageEvents` initializes one saved, automatically synchronized integer attachment and copies it on vanilla LivingConversionEvent.Post. The base bundle is independent of profession and does not reroll at render time; no gender or age data exists.

Client-only `VillageVisuals` extends standard VillagerRenderer/ZombieVillagerRenderer just to switch the existing robe between short and long bundles. The zombie's same native robe geometry/UVs is split into a child part. Adult profession layers replace the vanilla profession layer; babies use the original layer and native proportions. Face/nose, animation, equipment layers and shadows remain native. A real head item suppresses costume head geometry. Climate/profession/level textures and reusable accessory meshes are cached at renderer-layer registration. `VillageOutfitGeometry` is a separate pure mesh factory, validated without graphics/registry bootstrap. Zombie clothing reuses normal profession assets plus dark tint/damaged mask. Merchant uses the native WanderingTrader renderer with a coat/hood/satchel/pack/rolled-rug layer; its AI, llamas, invisibility and trades are untouched.

Operator tools extend `/slavicmyths debug/test`; `/sm` redirects to that existing root. `debug villager [entity]` inspects data/offers, `test villager workstation <role>` places a complete real workstation next to an eligible test villager without assigning a profession or teleporting, `test villager level <1..5>` changes the displayed test level. The latter is a visual helper; normal trading progression is verified separately.

`tools/village-tests` and `tools/village-test-resources` load only with `-PvillageRuntimeCheck`; no test code enters the production JAR. The server run uses isolated `run-village-checks`. `verifyVillageGeometry` bakes actual production accessory meshes without opening a window. `tools/verify_village_121.py` checks resources, bundled bytes, source-side isolation, generation reproducibility and the actual pure Java function. Legacy resource generators invoke the latest village overlay through `integration_polish_resources.py`.

See [implementation/acceptance](VILLAGERS_1.2.1.md) and [measured checks](verification/village-1.2.1-checks.json). Four real headless server launches, zero client/PolyMC launches; no actual relog or measured MSPT is claimed.

## 1.1.8 Integration Polish — build / production / PolyMC gates PASS

Implemented grouped Kitchen II screen and continuous supported shelf, single headscarf render path, rebuilt carved/storm staff and open-crescent sickle, native 16px berries/bushes, raised Perunite equipment meshes, common numeric rune definitions/tooltips and incompatibility, crate nine-slot GUI, C descent and smooth -0.32 descent for both vessels, rare individual animal ambient calls. Ambient source events: goose/duck use minecraft:entity.chicken.ambient at existing 0.75/1.10 sound pitch; domestic goat uses minecraft:entity.sheep.ambient at 0.9. Defensive hiss uses minecraft:entity.cat.hiss only from GooseDefence.

Apple uses one taller fixed crown with the old Y=4 saved-world anchor; pine is 9–12 blocks with separated tiers; four pine saplings select a validated 2x2 giant of 16–20 blocks, never natural giant spawning. Sapling preflight now respects world height and loaded chunks. Existing berries/hops bonemeal increments remain intact.

Five definitive recipes repaired. Shared kurgan candidates use 41:4:1 weights, approximating old relative density (1/20² : 1/64² : 1/128²); legacy warrior/great sets are preserved with frequency 0. Common placement 20/15 → 50/30; retained legacy placements 64/55 → 160/110 and 128/119 → 320/238. Large bandit camp 128/119 → 320/238, salt retained. Apple rarity 18 → 6. These are configured values, not measured world frequencies.

165 acquisition IDs reviewed, missing routes 0; RU/EN missing keys 0 and remaining mojibake 0. See LOCALIZATION_AUDIT_1.1.8, OBTAINABILITY_AUDIT_1.1.8 and FUNCTIONAL_AUDIT_1.1.8. `gradlew.bat clean build`: first attempt failed on Component.withStyle; permitted retry PASS in 33s, 12 deprecation warnings; Gradle test task NO-SOURCE. Actual pure Java assertions: 300 TreeShape + 49 BrewRules. Production/resource gates PASS (2692 JSON, 5894 asset links); duplicate Vat screen bytecode regression PASS; tests excluded from JAR. Source rune numerical equivalence PASS. Versioned JAR: build/libs/slavicmyths-1.1.8.jar, 7192357 bytes, SHA256 b108d74baf242c71136a63670bb9da90db40d7c029b025475e4e9091d107c4fe. Installed only this mod into Slavic-Myths-1.21.1-Testing; backed up/replaced 1.1.7; six companion files and two instance configurations have identical hashes. Receipt: docs/verification/polymc-1.1.8-installation.json. Branch main, no release branch/tag. Automated Minecraft/PolyMC/server launches: 0; visual, gameplay, rejoin and natural frequency acceptance remain manual.

## 1.1.7 — Screen registration invariant (2026-10-07)

ClientSetup.screens регистрирует VatMenu/VatScreen ровно один раз. tools/verify_brewing.py проверяет уникальность menu TYPE registrations в source и единственную ссылку VatMenu.TYPE в production ClientSetup bytecode через javap. Это проверка устранения конкретного startup duplicate, не runtime/game acceptance. Версия/registry IDs сохраняются; common/server code не изменён.

## 1.1.7 — Brewing (2026-10-07)

`brewing.Brewing` дополняет существующие ModItems/ModBlocks/ModTiles/recipe/menu registers; собственный server-only persistent attachment drink_overload без copyOnDeath/sync/tick polling. `BeverageItem` содержит exact drink mapping/load/effects, native CustomData quality/beverage/container/servings и CustomModelData alternate bottle/pitcher models; drinking32ticks, same-amplifier vanilla effects, lazy elapsed/1200 decay; milk не изменяет attachment. `BrewRules` — pure arithmetic quality/duration/decay с49 actual source-Java assertions. `BrewEvents` получает pickup event для first_brew; server pour/overload тоже award appropriate advancement, vanilla namespace untouched.

`VatRecipe` — native Recipe<KitchenInput>/MapCodec/RegistryFriendlyByteBuf stream, four counted inputs, optional catalyst/retain flag, process time/mode/result/explicit returns. Count allocation переиспользует выделенный KitchenRecipe helper, не меняет кухонные рецепты. Thunder fifth consumed reagent в catalyst slot; Veles retained staff; Witch consumed forest rune. `BrewTile`9slots, snapshot pending/consumed inputs, dirty-triggered RecipeManager lookup, active tick only, output/remainder capacity checked before transaction; manual press no ticker, keg no ticker. `VatMenu/VatScreen` native screen/slot guards/input locks/data slots; client display/renderers изолированы.

`StorageTile` того же household_storage TYPE сохраняет старые ContainerHelper Items, только BARREL читает/пишет дополнительный Fermentation compound beverage/age/servings. `StorageBlock.Barrel` brewing=false default (old blocks), derived flag enables/disables BE ticker on state change; storage mode has no fermentation ticker. `Fermentation` ages loaded ticks only,8servings, quality derived, mode resets empty; active batch lost on break, storage Items unaffected. Keg same beverage/quality invariant,1serving insert,1mug/bottle or4pitcher extract, filled portions drop on break. Pitcher-to-held/offhand empty container transfer uses server inventory give/drop safe path; no real fluid API.

`HopsCrop` AGE0..5/HALF, authoritative lower, growTo keeps upper synchronized, stages3+ tall; fertilizer/sickle reuse existing actions; upper manual break drops lower once. Existing grass seed modifier independently adds rare cutting without altering old seed distribution. `tools/brewing_resources.py` deterministic native16px assets/recipes/loot/tags/lang; `verify_brewing.py` JSON/model/texture links/recipes/native pixel checks/reproducibility/pure Java/production byte checks. No Minecraft harness enters JAR, no global scans/player tick/network spam/system-time fermentation.

## 1.1.6 — Household storage (2026-10-07)

`storage.Household` дополняет существующие deferred block/item/tile и recipe registers, сохраняет прежние IDs. Ограниченный Kind описывает десять storage kinds/capacity/tag; StorageBlock concrete state subclasses: Sack/FILL, Chest/OPEN, Vertical/standard LOWER-UPPER, Multi/drying four parts, Hay/LEVEL. SheafBlock отдельный без EntityBlock/BE. Multiblock placement проверяет все позиции до установки, partners устанавливаются flags2 до neighbor notifications; delegates вычисляют master по orientation/offset. OnRemove очищает реальные items только master, один multiblock item drop; hay только exact saved count. Orphans self-remove без повторных drops.

`StorageTile` Container/MenuProvider, native NonNullList/ContainerHelper inventory и DataVersion1; barrel1.1.6 не содержит fermentation state. Vanilla ChestMenu27 и restricted subclass GENERIC_9x1 для barrel; Slot.mayPlace проверяет tag/один item type, Container.canPlaceItem даёт ту же проверку hoppers. Chest viewer count задаёт простую OPEN модель без tick polling. Direct storages используют одинаковую серверную insert/extract логику; shelves адресуются hit position, max16. Client update packets только при изменении; sack FILL вычисляется из counts/capacity. HayCount отдельный1..4, синхронизирует LEVEL обеих частей.

`DryingRecipe` — native custom Recipe<SingleRecipeInput>/serializer, JSON ingredient/result/time. Drying в StorageTile включено только для DRY kind: четырём slots соответствуют progress/time/recipe ID/result snapshot. Recipe lookup только insert, ticker только master DRY, progress сохраняется каждый tick, network только stage transition/completion. Offline simulation не добавлена.

Client-only StorageDisplay читает реальные synced stacks, все3/6 shelf slots и representative4/6 baskets/crate; reuse KitchenDisplay для cooked 3D food/metal pot, остальные item/block models FIXED/category transforms. Нет ItemEntity, bob/spin/global manager. `tools/storage_resources.py` воспроизводит native16px meshes/data, `tools/verify_household.py` проверяет data/links/cuboids/UV/recipes/повторяемость/production bytes, не заявляет игровые тесты. Бочка остаётся storage-only до отдельной следующей1.1.7 итерации.

## 1.1.5 — Kitchen II (2026-10-07)

`kitchen.KitchenII` использует существующие ModItems/ModTiles deferred registers; kitchen RecipeType/Serializer дополняют существующие ArmorerRecipe registers. `KitchenRecipe` хранит counted Ingredient (tags/alternatives), mode, tool, time, result, servings, explicit remainders; matching распределяет counts по четырём slots и исключает unrelated stacks. Recipe manager вызывается по dirty inventory/finish/load, не каждый tick. Старый giant-switch KitchenRecipes удалён; IDs прежних кухонных items/menu/block сохранены.

`KitchenTableBlock` horizontal PART LEFT/RIGHT, master LEFT. Сначала проверяется replaceable partner position; только master создаёт KitchenTile и ticker. OnRemove master очищает inventory и выдаёт один block item; партнёр удаляется без собственной loot table. `KitchenTile` SimpleContainer9: tool0/1, input2..5, output6, returns7/8. Start атомарно проверяет tool/output/returns, расходует inputs, вставляет explicit containers, сохраняет recipe ID/result snapshot/time/mode/servings. Prepared pot state независим от parallel rolling, требует bowl, блокирует снятие pot; разрушение не материализует servings/consumed recipe. Menu Slot rules исключают ручную вставку output/returns и изъятие активных tools, DataSlots дают progress/servings, сервер проверяет serving button.

`furniture.TableBlock` расширяет прежний FurnitureBlock только для pine/linden table IDs; connection enum NONE/NORTH/SOUTH/EAST/WEST, reciprocal flags2 updates без цепочек, двусторонняя однозначность pair. `TableTile` non-ticking, четыре отдельных food stacks, vanilla finishUsingItem/canEat и возвращение container, no global scans. Inventory snapshots синхронизируются block update packet при изменении и initial chunk data. Старые furniture SINGLE models/textures не меняются; paired models удаляют только внутренние ноги.

Client-only `KitchenDisplay` регистрирует standalone baked low-poly meshes, рендерит фактический ItemStack без bob/spin; ordinary fallback FIXED, master kitchen renderer виден на обеих половинах, tools/dough/formed dough/pot и редкий локальный steam. `KitchenScreen` — один точный layout без выбора recipe, одна кнопка порции. JEI plugin изолирован от common code и использует актуальный recipe manager/category. `tools/kitchen_resources.py` canonical final overlay для затронутых legacy generators; `tools/verify_kitchen.py` проверяет точные food data/recipes/native dimensions/links/geometry/старый SINGLE/production bytes, не заявляет runtime проверку. BeltData RightClickBlock пропускает оба типа столов, чтобы Shift+ПКМ eating/tool removal имели приоритет над снятием пояса. Hopper automation отсутствует. Minecraft launches0; PolyMC untouched.

## 1.1.4 — Textile processing (2026-10-07)

`textile.Textiles` дополняет существующие deferred registers. `TextileEvents` заменяет только стандартные animal drops после успешного создания одной `Carcass`; XP не затрагивается. Entity variant синхронизируется, размеры масштабируются по виду; local life/fire сохраняются в NBT, processed guard исключает повторную выдачу. Нет AI, глобальных обходов или force loading.

`StationBlock` имеет три конкретных state definitions; общий `TextileStation` хранит ограниченные counts/progress, материал фиксирован типом станка. Мялка не тикает; прялка/ткацкий стол работают локальным серверным ticker, меняют visual state только при изменении значения. Ввод/выдача/разрушение выполняются на сервере. `LinenBed` наследует vanilla BedBlock gameplay, использует authored MODEL без BedBlockEntity; completed-deep-sleep PlayerWakeUpEvent flags дают ровно два атрибута well_rested, без player polling.

`ClothingItem` реализует Equipable без ArmorItem/ArmorMaterial/durability. Client-only `TextileClothingLayer` строит отдельные HumanoidModel meshes, копирует позу игрока, поддерживает standard/slim; invisible/spectator скрыты. `BeltData` — NeoForge persistent ItemStack attachment с native stream synchronization/copyOnDeath; перед death drop attachment очищается, keepInventory сохраняет. Empty-air снятие использует небольшой client-to-server payload, сервер повторно проверяет состояние игрока/руки. GUI/keybind/обязательная Curios зависимость отсутствуют. `CarcassRenderer` имеет одну low bundle geometry/семь atlas variants; `SpinningWheelRenderer` рисует вращающийся rotor только у прялки.

`tools/textile_resources.py` воспроизводит native pixels/models/data, старые crafting генераторы больше не восстанавливают bypass. `tools/verify_textile.py` проверяет JSON/links/геометрию/UV dimensions/recipes/tags/translations и byte-identical production resources. Это статические проверки, не gameplay/save-load/runtime тесты. Реальных Minecraft запусков 0.

## 1.1.3 — Animal husbandry (2026-10-07)

`husbandry/Husbandry` использует существующие deferred registers. Только три EntityType<YardAnimal>; kind определяется фабрикой, baby age сохраняет vanilla, canMate и YardParentGoal фильтруют именно EntityType. Entity-local egg timer сохраняется в NBT; feeder cooldown — absolute game time в стандартных NeoForge persistent entity data, без world manager. GooseDefence — временный local goal и synced visual state, 20-tick warning/80-tick chase, 20-tick bite cooldown; после load пассивен. DuckWaterGoal ограничен radius 8 и cadence 200..399 ticks, использует стандартный navigation/FloatGoal и land roam.

`YardFeeding` EntityInteract выполняет love/baby growth/heal/consume только на сервере; EntityJoinLevel добавляет FeederGoal и дополнительный TemptGoal один раз по наличию goal, без лишнего persisted marker. Старые vanilla isFood/tempt/drops/models не заменены. FeederGoal ищет только для baby/injured, radius 10, cadence >=40 ticks, timeout 200, consume one/heal2/grow400/cooldown600, никогда setInLove. NestGoal ищет при истечении egg timer, radius 12, path timeout200/fallback drop. YardSearch проверяет только загруженные блоки в вертикальной полосе +/-3 и сферическом радиусе, максимум 4 path attempts к ближайшим candidates; duck search +/-2. Нет global polling/chunk force loading.

`YardStorageBlock.Feeder/Nest` имеют только свои count properties и horizontal facing; общий YardStorage — non-ticking BlockEntity, inventory source of truth, save/load sanitation и onLoad visual synchronisation, block removal drops contents. Capacity16 single-type / capacity3 mixed eggs. Козье молоко наследует vanilla MilkBucketItem, без hunger/buff. Biome modifiers/loot/recipes/feed tags/advancement data-driven. Client-only HusbandryRenderer выбирает один из шести meshes и texture atlases; head accessories иерархически прикреплены к head, baby не uniform adult scale. `tools/husbandry_resources.py` воспроизводит authored cuboids/UV/native pixel assets/data, `tools/verify_husbandry.py` делает static links/UV/JAR checks без Minecraft. Vanilla audio references документированы как временные.

## 1.1.2 gardens (2026-10-07)

`garden.PerennialBush` — reusable BushBlock/BonemealableBlock, PHASE 0..4 сериализуется как прежний `age` ради сохранения старых berry states; raspberry upper/lower синхронизированы, lower authoritative, переходы randomTick 1/18. FolkBerryBush сохраняет прежний класс/ID и делегирует общему base. Loot data-driven: shears sapling, early/adult probabilities, ripe extra berry; upper loot требует surviving adult lower, поэтому уничтожение lower не дублирует drop. Fertilizer обрабатывает только lower berry state в 3x3; watering/sickle остаются farmland/crop-only.

`AppleTreeShape` хранит immutable 9 log offsets, 74 leaf offsets и 26 deterministic fruitable offsets. AppleTreeFeature используется configured apple_tree/apple_sapling; стандартный WoodlandGrower reuse и предварительная проверка пространства без уничтожения соседних блоков. AppleLog CROWN_ANCHOR=false по умолчанию; generated top log true; chance 1/8 и максимум один missing air leaf с adjacent apple timber/leaves в mask. Player placement не создаёт anchor. AppleLeaves наследует vanilla distance/persistent/decay, добавляет fruitable/fruit_stage; chance 1/24, независимые leaves, ripe harvest 1 apple +25% второй, stage reset 0. Break loot целиком исключает apple и сохраняет vanilla-like sapling/stick/Silk/shears.

`Gardens` использует существующие ModBlocks/ModItems/ModFeatures/ModTiles, отдельные типы sign/hanging sign с client-only renderer registration/Sheets wood material. Для точных apple IDs и custom log/leaves требуется отдельная регистрация семейства; старые Woodlands семьи не рефакторятся. Worldgen placement/biomes остаются data-driven, никаких global tick scans или BlockEntities для растений/anchor. `ModItemGroup` содержит 9 стандартных табов с ordering edges и детерминированным category priority; старый tab ID сохранён как lore-only. Farm preferred ordering: 1.1.0 crops/fertilizer, berries, bush saplings, apple sapling; tools отдельно; apple family в decor.

`tools/gardens_resources.py` генерирует отдельные native pixels и использует установленные vanilla 1.21.1 geometry/data schemas; старые генераторы 1.1.0/RC2 сохранены. `tools/verify_gardens.py` проверяет resources/JAR без Minecraft; статическая маска/loot проверка не считается игровым regrowth/harvest тестом.

## RC2 UI / Visual correction — 2026-10-06

## 1.1.0 farming integration (2026-10-07)

`org.slavicmyths.farming` содержит общий `FarmingCrop` (собственный AGE 0..6, lazy seed supplier, коэффициент growth gate, vanilla CropBlock randomTick/NeoForge hooks). Посадка проверяет vanilla farmland; no collision/no block entity/no global polling. Vanilla runtime/data registries дополнены через существующие ModBlocks/ModItems/ModLoot.

Четыре item-use механики выполняют изменения только на сервере, проверяют mayInteract/mayUseItemAt и не загружают чужие chunks. SickleItem получает стандартный Block.getDrops с реальным инструментом, меняет age на 1, затем выдаёт loot и изнашивает инструмент. FieldHoeItem использует стандартную HOE_TILL ability/NeoForge modification hook и отдельные проверки каждой позиции в полосе; coarse/rooted dirt → farmland при свободном верхнем блоке. Это Item с точной durability 350: TieredItem иначе перезаписал бы её iron-tier значением 250.

`ItemState.WATER_CHARGES` — Codec.intRange(0,8), persistent + network synchronized. WateringCanItem содержит default component 0, fills from source fluid without removal, modifies only FarmBlock.MOISTURE. FertilizerItem использует `sickle_harvestable` tag (7 mod/4 vanilla crops), increments each immature crop exactly once, does not call vanilla bonemeal and does not consume on no-op.

`GrassSeedsModifier` добавляет один seed в existing loot; вероятность/table identity/валидная пара tall grass задаются JSON. `CappedFortune` — зарегистрированная loot function в Registries.LOOT_FUNCTION_TYPE, применяется один раз к mature produce, +0..min(Fortune,3); seeds unaffected. Дроп остальных возрастов полностью описан loot tables. Blockstates/model textures содержат ровно 7 стадий. ClientSetup помечает новые блоки cutout только в client-side классе.

`tools/farming_resources.py` воспроизводит актуальные 1.1.0 ресурсы без изменения RC2 UI/wood/weapon assets; `tools/verify_farming.py` проверяет спецификацию данных, прозрачность/размеры/различимость, JSON/ссылки и production JAR без Minecraft. Исходная схема рецепта лейки содержит 6 I, но письменная стоимость 5 nuggets; разрешённый альтернативный нижний ряд ` I ` сохраняет стоимость 5 nuggets + bucket.


Внесён пакет UI/Visual поверх текущего main/RC2: два входа наковальни и copy-only preview, три режима/автоматический расход доп. материалов, Creative XP=0, новый Path Stone/Kitchen UI, исправлен повторный blur книги, реальные оси щита, пересобраны Club/Battle Axe UV0..16, четыре разных древесных материала и двери/люки/signs. Registry ID, рецепты, баланс survival, курганный hotfix сохранены. Сброс пути и новые пары конфликтов рун не выдуманы. Разбойники/Соловей отложены.

PASS: clean build; 10/10 loader GameTests с companions; шесть CPU/data/codec/geometry gates; production resources 0 missing refs. Реальных headless запусков в этом этапе: 2 (первый 8/10 с ограничением synthetic connection/DataSlot, финальный 10/10). Клиент: 0. GUI scales 2/3/4, игровые held poses и реальный reconnect — MANUAL PENDING. Результат не объявлен визуально принятым. Версия остаётся 0.9.10-rc2; опубликованный tag/prerelease не перезаписывается. Исправленный JAR установлен в существующий PolyMC 1.21.1 RC2 после gates; прежний сохранён вне mods, остальные моды/config/saves без изменений. SHA256 `ca32e6ac1b8a67a24cd5efadb051fbc0079e525bdb5b9642b1b0552f228a571e`. Commit/push не выполнялись по текущему AGENTS.md. Отчёт и список файлов: `docs/verification/rc2-ui-visual/REPORT.md` и `changed-files.txt`.

## 0.9.10 RC2 — server acceptance и установка (2026-10-06)

Завершены оставшиеся серверные проверки: 9/9 workstation+hotfix GameTests и 27/27 регрессий, включая подход всех трёх курганов без повреждения кладки. Щит реально блокирует спереди, изнашивается, получает axe cooldown; repair/Unbreaking/Mending/NBT проходят. Камень пути: RMB/menu, offering/XP, навыки, navigation SavedData, range guard, NBT. Наковальня: forge/install/remove, расходы, validation, shift-click, close-return и components. Кухня: 12/12 рецептов, guards, consumption, tool wear, remainder, output shift-click и NBT.

Исправлены все 16 старых door-model parents у darkened/pine/rowan/willow; native blockstates включают все facing/half/hinge/open варианты, старые имена моделей сохранены как aliases. В кухне отображаются food effects или их отсутствие, подробности в tooltip; gameplay блюд не изменён. Воспроизведение door resources: `python tools/migrate_remaining_doors_1211.py` после исторических resource generators.

`clean build` + шесть CPU/data/codec checks PASS. Production resources: 4042 references, 0 missing asset issues; static data 2685/0 errors. JAR SHA256: `faa54bd5decf79dc029b1d722f1995b195aa313dd5cea900c16bd7a70ea5a499`. Установлен в существующий PolyMC Slavic-Myths-1.21.1-Testing; прежний dev JAR сохранён вне mods, 6 прочих модов и configs/saves не изменены. Client 0 по выбору пользователя; эта итерация имела 3 реальных headless server запуска (первый исправлял test fixture, затем 9/9 и 27/27). Реальный multiplayer/перезаход, визуал и GUI scales — MANUAL PENDING. Отчёт/receipt: `docs/verification/playtest-0.9.10-rc2/`. Сборка/docs/checksum: `release/0.9.10-rc2/`.

Исходники опубликованы в `main`, она установлена основной веткой GitHub; история существующей `master` сохранена. Player branch `release/0.9.10-distribution` обновлена до RC2. Prerelease: https://github.com/Deserag/slavic-myths/releases/tag/v0.9.10-rc2 — JAR/ZIP/MRPACK скачаны обратно и проверены по SHA256. RC1 не перезаписывается, новый tag `v0.9.10-rc2`. Нового overhaul разбойников/Соловья, Equipment & Art или rune system 0.9.11 нет. Полная ordinary-world natural coverage и measured MSPT не заявлены принятыми.

## RC1 — отдельная сборка для игроков и уточнение приёмки (2026-10-06)

Опубликована отдельная distribution branch `release/0.9.10-distribution` с 9 файлами: playable JAR в mods, installer закреплённых зависимостей, lock/checksum и player docs. Исходников/Gradle/миров/configs нет. Source branch `release/0.9.10-playtest` и tag `v0.9.10-rc1` сохранены. Загрузчик реально скачал 6/6 внешних модов с проверкой размеров/SHA256; набор содержит 7 JAR. XaeroLib вложен в обе карты и отдельно не требуется. ZIP и .mrpack подготовлены для prerelease; Java/NeoForge/Minecraft и профиль пользователя не устанавливались/не запускались.

Список невыполненного: `docs/release/RC1_REMAINING_WORK.md`. P0 исправлены в коде, но реальная игровая/визуальная приёмка не пройдена. GUI scales не проверены в клиенте; дополнительные meal effects в kitchen preview не показаны; регрессия расчистки кладки не перезапускалась после исправления; 16 старых door-model parents остаются. Не обозначать RC как полностью принятую игровую сборку. Этот этап меняет упаковку/документацию; production JAR/код/контрольная сумма RC1 не менялись.

## 0.9.10 RC1 — public playtest stabilization (2026-10-06)

Ветка `release/0.9.10-playtest`, версия `0.9.10-rc1`. Исправлены UV/round model/blocking щита, зарегистрирован экран существующего RPG-меню Камня пути/рунной наковальни, добавлен shift-click наковальни; книга получила непрозрачную страницу и scroll, кухня — выбор рецепта/условия/результат. Курганный hotfix включён: сохранённые encounter slots, фиксированные квоты/боссы, наружные patches и единый 100-tick Порез. Геометрия кургана и существующие ID сохранены. Разбойники/Соловей не перерабатывались; полный Equipment & Art Overhaul и новая система рун не заявлены реализованными.

`clean build` и шесть CPU/data/codec проверок PASS; production JAR/resources PASS, 2685 статических ссылок, 0 новых ошибок. 16 прежних door-model parent issues отмечены отдельно. До последнего запроса hotfix: 5/5 GameTests; общая регрессия 26/27 обнаружила расчистку кладки, исправленную затем без повторного запуска игры. Клиент 0; после последнего запроса Minecraft не запускался. Игровая/визуальная приёмка — MANUAL PENDING. Отчёт: `docs/verification/playtest-0.9.10-rc1/acceptance.json`; playtest docs/JAR/checksum: `release/0.9.10-rc1/`. Изменения прежнего рабочего дерева включены, runtime worlds/logs/configs не включаются в release commit.

## 0.9.10 — анализ нового пакета (2026-10-06)

Прочитан Equipment & Art Overhaul FINAL; начато сопоставление с реальными ID, способностями и boss loot. Production-изменений 0.9.10 пока нет: требуется согласовать отсутствующие точные параметры и рецепты согласно просьбе пользователя не додумывать логику. Разбор: `docs/equipment/SPEC_REVIEW_0.9.10.md`. Существующие изменения 0.9.9 сохранены. Build/resource gates этого этапа не запускались; Minecraft-клиент — 0 запусков.
Acceptance update: thin/high superflat each passed 10 actual placements; six sequential commands from one point passed; water/native-terrain gates and all 27 companion tests passed without ERROR logs. Clean build and production JAR audit passed. Current override JAR is installed in Slavic-Myths-1.21.1-Testing (SHA256 22d6d025e106d912ed3c867704078f560ce5ff3bcd67bbc3b31340b3cd7f61cb); previous dev JAR backed up outside mods. Other mods/worlds unchanged; client not launched. Evidence: docs/verification/mob-worldgen-0.9.9/structure-priority-override.json. Full ordinary-world natural coverage remains NOT ACCEPTED.

## Текущий override: структура имеет приоритет над сушей

Реализована отдельная пошаговая ручная генерация трёх курганов и трёх лагерей: FULL-чанки до измерения высоты, проверка конечного объёма, подъём на тонком superflat, заполнение грунтом, выравнивание суши, локальный перенос от воды/защищённых построек в пределах 256 блоков. Поиск и генерация имеют status/cancel и не выполняют прежние синхронные обходы сотен удалённых starts. Проверка команд выполняется на headless сервере, Minecraft-клиент не запускается.

Подробности, ограничения отмены и результаты проверок: `docs/worldgen/STRUCTURE_PRIORITY_OVERRIDE_0.9.9.md`. Полная 0.9.9 остаётся в разработке: естественное покрытие обычного мира и компенсация не приняты. Записи ниже — история предыдущих этапов; их заявления о неизменённом production-коде относятся к моменту диагностики.

Research update 2026-10-06: real kurgan generate command reproduces the reported false 237-block terrain rejection caused by an unloaded heightmap; original-position bedrock preflight and synchronous search risks identified. User save structure settings inspected read-only. This is a diagnosis, not a fix; production code and installed JAR unchanged. Details: `docs/worldgen/COMMAND_GENERATION_DIAGNOSIS_0.9.9.md`.

Deployment update: by explicit user request, `0.9.9-dev` is installed in PolyMC `Slavic-Myths-1.21.1-Testing` for manual generation testing. Previous 0.9.7 JAR is backed up outside mods; other mods and worlds are unchanged. This is not release acceptance. Receipt: `docs/verification/mob-worldgen-0.9.9/polymc-dev-installation.json`. Minecraft was not launched.

# Slavic Myths 0.9.9 — implementation in progress

Continuation 2026-10-06: fixed raised-kurgan authored entrance stones using the persisted graded surface; soil excavation preserves underground construction. All three tiers and all three approach lanes pass; all 27 required companion GameTests pass. Clean production build `-Pmod_version=0.9.9-dev` passes; JAR resource validation passes. The latest natural-coverage measurement code includes relocated eligible cells and fails unmeasured/empty applicable networks; full natural gate still pending. Evidence: `docs/verification/mob-worldgen-0.9.9/continuation-2026-10-06.json` and `docs/worldgen/STRUCTURE_COVERAGE_AUDIT_0.9.9.md`. Stable PolyMC artifact is unchanged.

COMPILES: six spirit combat timelines, wildlife families/stat changes, existing bandit HP alignment, shared placement profiles, retained template foundation bounds, accepted-neighbor collision planning, and bounded persisted kurgan earthworks for thin superflat (explicitly authorized by the user). Build and verification details: docs/worldgen/STRUCTURE_COVERAGE_AUDIT_0.9.9.md and work/mob-worldgen-099-*.log. Static data audit: 2676 references, zero errors. Actual accepted-placement coverage/compensation, complete mob polish/audio audit, final resource/release gates and companion runtime checks remain unfinished. This is not 0.9.9 release acceptance. Installed stable PolyMC artifact remains 0.9.7; Minecraft client has not been launched.

# Slavic Myths 0.9.7 — болото и runtime hotfix

Реализованы: семь семейств болотных мест / 21 modern template; четыре дерева и природные детали только vanilla swamp; болотник 48 HP / редкий страж 112 HP; новые swamp русалки 36 HP и водяные 72 HP; пять таблиц структурной добычи + drop болотника. Старые ID/templates и остальной контент сохранены, новой biome/ore/block семьи нет.

Xaero logout/publish null-safe с очисткой внутренних данных перед optional callbacks, пользовательские waypoint сохраняются; JEI catalyst связан с зарегистрированным ritual RecipeType; linden_door использует native 1.21.1 states/models. Другие 16 старых door model parent issues остаются backlog.

DONE по build/headless: production 0.9.7, clean build; финальные core без JEI и шесть companions — по 14 GameTests без ERROR/FATAL. 42 сухих/мокрых размещения, 96 контейнеров, 720 опор, 400 форм деревьев, семь реальных accepted starts; пять features и болотник подтверждены в modified biome settings. Проверены 10 lifecycle cases production Xaero adapter с doubles и compiled JEI recipe-type contract. Проверки/gates и hash: docs/verification/swamp-0.9.7/acceptance.json; установка: installation-receipt.json в той же папке. JAR 0.9.7 установлен в Slavic-Myths-1.21.1-Testing, семь хешей проверены, прежний 0.9.6 сохранён; старые профили/миры не изменены. Headless размещение/данные/генерация/геометрия проверяются отдельно от клиента. Manual visual/gameplay QA: NOT STARTED; следующий конкретный блок — docs/MANUAL_QA_0.9.7_SWAMP.md. Клиент не запускался.

Подробнее: docs/swamp/SWAMP_0.9.7_*.md. Ниже сохранены исторические состояния прежних версий.

# Slavic Myths 0.9.6 — курганы

DONE по build/headless: графовый план v2 и совместимость v1, 14 архетипов, альтернативные лестницы, новая архитектура/палитры, 26 блоков + 26 BlockItem, 60 рецептов, семь новых таблиц наград, 35 авторских текстур 256×256 (11 блоков и 24 иконки). Существующие ID, характеристики, старый loot и глобальная частота генерации сохранены.

PASS: clean build; 150 новых планов; 1200 legacy планов и 900 составов; по 10 GameTests без JEI и с companion-модами, включая реальное размещение/контейнеры/ловушку. Data totals: 411 items, 179 blocks, 273 recipes, 260 loot tables, 154 advancements. Production: `build/libs/slavicmyths-0.9.6.jar`; evidence: `docs/verification/kurgan-0.9.6/acceptance.json`.

Client: COMPILES. Ручная визуальная/игровая QA: NOT STARTED, клиент не запускался. Следующий конкретный блок — `docs/MANUAL_QA_0.9.6_KURGAN.md`. Известный post-port backlog: 20 старых missing door model parents; подробности в resource-check.json. Проверенный JAR 0.9.6 установлен в известный тестовый PolyMC, семь JAR сверены по hash; прежний 0.9.5 сохранён. Receipt: `docs/verification/kurgan-0.9.6/installation-receipt.json`. Остальные профили/миры не изменены.

Документация алгоритма, комнат, ID/рецептов, loot и аудита иконок: `docs/kurgan/*0.9.6.md`. Далее сохранено историческое состояние 0.9.5.

# Slavic Myths 0.9.5 — навигация и зоны поиска

Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Слой навигации реализован по последнему
пакету V2. Предыдущий перенос 0.9.4 завершён; его отчёты сохранены как историческая база.

Core/server: DONE по headless проверкам. Client screen/HUD/Xaero: COMPILES, ручная QA не выполнена.
Build и финальные loader/data checks PASS. Minecraft-клиент не запускался.
Production: `build/libs/slavicmyths-0.9.5.jar`. Точный hash: `packaging/test-pack-lock.json`.

[Навигация, алгоритм и фактические ограничения](NAVIGATION_0.9.5.md),
[ручные проверки](MANUAL_QA_0.9.5_NAVIGATION.md), [evidence](verification/navigation-0.9.5/acceptance.json).

Core: navigation/SlavicMarker, NavigationState, SearchArea, NavigationRecords.
Серверный SavedData хранит exact assignments отдельно от public player snapshot.
Typed navigation_request проверяет ownership и права; navigation_snapshot содержит только
одного игрока. Client UI и compat/xaero изолированы; Xaero compileOnly и optional.
Реальный immutable home определяет стабильную revision зоны; discovery разрешает temporary
exact point, lifecycle очищает его. Проверки polling индексируются собственными marker IDs,
не обходят чужие quests; bounded loaded-chunk discovery не создаёт chunks.
Путевой камень по-прежнему открывает RPG menu; новые travel-механики не добавлены.

## Исторические записи до 0.9.5

# Slavic Myths 0.9.4 — текущий перенос

Launcher fix: исправлены несовпадающие official PolyMC/Maven checksums NeoForge installer/universal локальным patch нового профиля. Реальный ForgeWrapper detector PASS; ручной повторный запуск ещё не проверен.

Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Все production sources компилируются; clean build, headless logic/resource/loader gates A–F PASS. Отдельный PolyMC Slavic-Myths-1.21.1-Testing установлен с семью проверенными JAR. Старый 1.16.5 профиль/миры сохранены. Клиент не запускался; игровая QA ещё не выполнена.

Фактическое состояние и журналы: [PORT_STATUS_0.9.4.md](port/PORT_STATUS_0.9.4.md). Исторические записи ниже описывают предыдущие этапы и версии, а не подтверждение работоспособности текущего порта.

Current port: provider-aware SavedData (all seven); persistent ItemStack data components with legacy custom_data/DFU migration; four typed payloads with server validation and client-only handlers; target event registration and GuiGraphics; data-driven features/spawns/structure sets with codecs and bounded candidate exclusions. Test sources/resources attach only to the opt-in GameTest run and never enter production JAR. Generic HP overlay removed under the package instruction; unique boss/UI mechanics retained.

# 0.9.4 — текущая архитектура переноса

Единственный активный toolchain — NeoForge 1.21.1 / Java 21, без dual-loader.
Entrypoint принимает IEventBus и ModContainer; конфигурация ModConfigSpec.
ModSounds использует DeferredHolder/DeferredRegister и variable-range SoundEvent.
BurialRecords, HuntRecords, PoolIndex используют SavedData.Factory, CompoundTag и
сохранённые старые имена файлов/ключей; сериализация не меняет игровую логику.
Некоторые runtime callers пока используют 1.16 API: полный port НЕ завершён.

portCore — тестовая компиляция выбранных реальных независимых production-файлов
на target dependencies; из main ничего не исключается, fake/stub API отсутствуют.
verifyKurgan/verifyHunt используют эти же production-классы; полный mod boot
и stateful ItemStack components этим не проверяются. Детали — port/PORT_STATUS_0.9.4.md.

port_resources_1211.py выполняет явные schema conversions, переносит singular data
folders; текстуры/звуки/structure NBT байт-в-байт сохраняются. Resource verifier
сравнивает нормализованные baseline data и бинарные SHA256; decoder/runtime QA pending.
Исторические генераторы assets ещё требуют адаптации: не запускать их как новый datagen.

---

# Исправление placement 0.9.3

YagaPlacement содержит production preflight/commit на IWorld, а не копию алгоритма
для тестов. Prepare проверяет loaded bounds до первого чтения, terrain Y>=0,
контейнеры/защиту/жидкости, полный desired/old snapshot и опоры почвы.
Commit использует flags=2|16; лишь затем вычисляет connected states всех блоков,
отклоняет неподдерживаемые/потерянные блоки, применяет формы и вызывает spawn.
Только успешный spawn позволяет YagaHut записать placed/anchor/canonical UUID.
Отказ блока, опоры, спавна или исключение откатывают snapshot с теми же flags18.

YagaPlacementHeadless использует vanilla bootstrap и локальные vanilla block tags,
изолированный IWorld fixture и этот же prepare/commit. Никакого server/client main.
NBT NPC теперь содержит YagaEggSummoned; SpawnReason.SPAWN_EGG включает сервисный
доступ standalone NPC, но не меняет world home/UUID. YagaMenu допускает egg NPC
без house intro; BabaYaga не выдаёт им hut advancement и не меняет house intro.
Все прежние серверные проверки и общая личная прогрессия остаются.

---

# 0.9.3 — избушка и сервисы Яги

YagaData — WorldSavedData `slavicmyths_yaga` в Overworld: anchor/placed/NPC UUID,
исключённые кандидаты, игроки по UUID (intro/stage/active/contract/cooldown/warnings).
Активный UUID резервируется и при выгрузке чанка. Три стадии завершаются только
по совпадению принятого поручения; повторная сдача не повышает стадию.

YagaHutPlan — чистый детерминированный blueprint и максимум 64 лесных кандидата.
YagaHut использует noise biome source без чтения незагруженных чанков; затем только
локальное PlayerTickEvent раз в 80 тиков до размещения. `hasChunksAt` предшествует
рельефу/блокам; height scan ≤48. Проверка полного plan до записи, snapshot+rollback,
после размещения восстанавливаются neighbor shapes (дверь, ограда, стекло, лестницы),
дневные грибы получают podzol. Нет force-load/глобального world-tick обхода. YagaLegBlock даёт 7 форм конечностей;
геометрия/чешуйки native, BlockItem не регистрируется.

BabaYaga — CreatureEntity, не Witch/boss; простые home/look goals, 40 HP, отказ вместо
смерти, жесты synchronized data. Canonical UUID проверяется на сервере.
YagaMenu зарегистрирован до регистрации RpgMenu.MENUS; NetworkHooks открывает меню,
все действия идут стандартным vanilla clickMenuButton, без новой сети.
Server checks: world, NPC, alive, distance≤8, intro/refusal, stage/active, полные inputs,
capacity/тип output. Персональная Inventory(5), возврат всего на removed.

YagaServices — единый конечный каталог MAIN/CONTRACTS/EXCHANGES/BREWS,
используемый сервером, экраном и изолированным необязательным JeiYaga.
YagaScreen/BabaYagaModel/Renderer загружаются только ClientSetup (Dist.CLIENT).
Статус лишь на Talk; 2 независимых modal popups, без постоянных справок;
награда Stranger скрывается; dynamic controls обновляются по смене menu state.

YagaUtilityItem: whitelist ordinary effect removal, никакого blanket clearEffects;
FlightRecovery — 60-tick repack delay у существующих flight items, с мазью 30,
6000-tick buff, без изменения cargo/motion/registry IDs. До этой итерации такого
таймера в FlyingVessel не было; README фиксирует фактическое расширение.
10 новых ModSounds завершаются vanilla event fallbacks с собственными субтитрами.

Scoped assets: tools/yaga_093.py. tools/yaga_review_093.py читает production Java
geometry и headless экспорт настоящего hut plan. Общий генератор подключает scoped
слой в конце; прежняя пользовательская weapon art в этой итерации не перезаписана.
Headless tests и ресурсные проверки не подтверждают игровой runtime.


---

# 0.9.2 — сохранённые региональные встречи

HuntRecords остаётся прежним WorldSavedData slavicmyths_hunts и сохраняет Hunts;
BossAnchors/BossRegions добавлены отдельными полями. BossKind не расширяет HuntTarget.
BossRules: seed, кандидаты, фазы и cooldown; BossAnchors: ChunkEvent.Load и локальная
40-tick активация, без force-load. ACTIVE UUID сохраняется независимо от наличия сущности
в текущей памяти; finishBoss принимает только matching UUID и реальную смерть.
WorldBoss использует базовый HuntMob для spirit/save инфраструктуры, но заменяет goals,
атрибуты, собственный owner/encounter, bar и scheduler. Его owner не попадает в old horn lifecycle.
BossRitualItem использует vanilla 60-tick held use, повторную проверку и расход после addFreshEntity.
BossEffects: общая регистрация POTIONS, PotionApplicable с ограниченным recursion guard,
Curios-only Napор и transient UUID-модификатор. BossStone — физический ограниченный projectile.
DashFacing используется старыми и новыми наземными/воздушными атаками.
WorldBossGeometry генерируется из того же manifest, что и offline геометрические ракурсы;
WorldBossModel/Renderer/BossAccessoryRenderer загружаются только клиентской регистрацией.
Обычный крафт обеспечивает JEI без отдельной категории или ложных рецептов добычи.

---

# 0.9.1 — расширение существующей охоты

HuntTarget задаёт стабильные строковые IDs четырёх целей; HuntRecords по-прежнему
хранит по одной встрече owner→UUID/anchor/status/ready. Новое Kind дополняет
старое Ovinnik; чтение boolean 0.9.0 мигрирует, ACTIVE при выгрузке сохраняется.
HuntMob остаётся владельцем lifecycle/баров/Home/guardian/NBT, старые два AI
не заменяются. ElementHuntMob добавляет два собственных move sets на тех же
lifecycle hooks, synced MOVE/SINCE и абсолютные сохранённые cooldown.

Змей: проверка loaded collision перед физическим move, bounded orbit/высота,
телеграфы/восстановления, локальные временные traces. Сервер не ставит огонь и
не разрушает блоки. SerpentProjection — технические 1-HP визуальные копии,
TTL70/absolute expiry, parent UUID, ноль damage/XP/loot/encounter, не hunt target.
Максимум два при cast, повтор только после expiry. EmberClump переиспользован
для редкого fallback; параметр урона определяется владельцем.

Подвей использует существующую наземную навигацию. VORTEX_ACTIVE выполняет
локальный pull с тремя силовыми ступенями, velocity cap .45 и center-hit CD30;
снаряды проверяются раз в 2 тика только в этом состоянии. Набор UUID исключает
повторное отклонение одной стрелы; только обычные arrows/snowballs/eggs,
жемчуг и специальные типы не затрагиваются, LOS обязателен. После60 active
и на выходе guard отсутствует. Во всех игроковых атаках учитываются LOS и
creative/spectator. Рывок физический и ограничен7, без телепорта.

HuntSpawns расширяет существующие placements/BiomeLoading, не добавляет
player/world tick spawning. Horn ищет≤40 loaded candidate positions, безопасный
объём, border, exclusion кургана; один owner/6000tick gate общий для всех целей.
HuntTags используется детектором и утилитой; PouchMenu меняется только данными
hunt_trophies и сохраняет9слотов/slot-swap ограничения.

StarCharmEffects — серверное PlayerTickEND, equipped Curios necklace lookup,
fallDistance/downward/flight guards, Slow Falling100, fallDistance=0; player
absolute Overworld clock ready1200 в persistent data, Clone сохраняет ready.
WindKnotItem — только ПКМ, MobEntity local radius4/LOS/hostility, CD500;
без урона/PvP/снарядов, maxhealth/target helper снижает elite/boss толчок.
BottledElements использует native inventory_changed AND двух предметов.

Client-only ElementModel/ElementRenderer/HuntAccessoryRenderer: свои512atlases,
блоковая геометрия, synced state animations, emissive только eyes/mouth/seams.
Новые зависимости отсутствуют. tools/hunt_091.py — scoped native art/data;
Hunt I tag writer additive, полный pipeline запускает II после I.
verifyHunt проверяет pure timing/math/NBT migration/cycle; verify_hunt_091.py —
exact recipes/loot/tags/resources/protected art/reproducibility. Эти проверки
не проверяют реальный бой/GUI/перезаход/сеть/MSPT: Minecraft запусков0.

---

# 0.8.5 — лабиринты и состояние экземпляра

KurganPlan — чистый Java-план с ограничением 12 попыток. Три диапазона этажей и
помещений, сетка с разными расстояниями между осями, случайное связное дерево,
ветви/тупики/петли, отдельная нижняя гробница. Корневой южный порт зарезервирован
под вход/лестницу: обычный коридор не может затереть ступени. План проверяет пары
room/room, room/connector, connector/connector, высоты и связность. Модульные роли
включают длины, повороты, развилки, тупики, лестницы и shortcuts. Основной путь
маркируется; у Великого заканчивается у намеренно запечатанного входа.

KurganPlanNbt сохраняет фактический план Version=1, комнаты/соединения/палитры,
флаги critical, позиции, границы печати и ниш. При загрузке нет повторного RNG.
KurganDungeonPiece — новый тип kurgan_dungeon; старый kurgan_shell не изменён.
Placement ограничен текущим чанком; оболочка, комнаты, проходы, затем декор.
Две клетки новой колоды лежат в одном чанке при любом debug-anchor.
Новая оболочка использует только vanilla; естественные Structure ID kurgan_small,
kurgan_warrior, kurgan_great и их сетка 64/24, salt остаются прежними.

BurialRecords расширен, прежний Opened сохранён. Instances хранят UUID, origin,
полный план, disturbance, fired sources, thresholds, sealOpened; dimension задаёт
WorldSavedData-хранилище. Индекс chunk -> instance обновляется при register/load.
Worker worldgen только ставит запись экземпляра в server executor; SavedData
меняется на серверном потоке. Обработчик placement синхронизирован на piece.

Источники `loot:roomId` и `zone:roomId` учитываются по одному разу. Домашние колоды
не имеют natural UUID. Никакого HUD/сканирования мира для disturbance нет.
BurialRecords в Overworld отдельно хранит player UUID -> source Great UUID set.
KurganCurse: один Effect, amplifier 0 = -10%, 1 = -15%, MULTIPLY_TOTAL к movement
и attack. Временная длительность 36000 ticks. Persistent state восстанавливается
на login/respawn и раз в 20 тиков игрока (один lookup, не обход сущностей/чанков).
clear удаляет данные и эффект; bossDefeated(player, sourceUUID) очищает конкретный
источник, сохраняя другие. KurganDungeonPiece.openSeal(world,id) открывает известный
проход только при загруженных чанках и записывает sealOpened; сейчас не вызывается
survival-механиками. Пустых классов будущего босса нет.

Generate — OP2, четыре ограниченные кандидатуры в 192 блоках, предварительная
проверка всего footprint, worldborder, terrain, tile entities и искусственных блоков.
Только затем размещение. Это dev-инструмент тестового мира, без автотелепорта.
Custom locate включает сохранённые debug-экземпляры; vanilla — естественные starts.

Ресурсы: tools/kurgan_085.py, 64px собственная кладка/декор/иконка, прежние loot
находки 0.8.4. verifyKurgan — отдельный JVM sourceSet tools/kurgan-tests, в JAR
не входит и не загружает Minecraft-клиент/сервер. Headless checks не подтверждают
игровую производительность, визуал, сетевую синхронизацию или фактический respawn.

---

# 0.8.4 — материальная основа курганов

kurgan/BurialCoffinBlock: две клетки, FACING/PART, tile только FOOT; HEAD направлен
по FACING. Placement проверяет вторую клетку; предмет не переносит Natural/NBT.
Пять слотов LockableLootTileEntity, стандартный vanilla loot filling, отдельный Container.
Любая половина удаляет пару; loot блока только FOOT, contents выгружаются из FOOT.
Creative HEAD убирает FOOT без block loot. Поршни блокированы, свойства facing
сверяются при удалении пары. Player-placed tile очищает принадлежность кургану.
BurialCoffinTile: inventory/Natural UUID/remains/opened в NBT; packet передаёт только
remains/opening. Viewers — UUID-set; menu close и проверка текущего меню раз в секунду
закрывают крышку. Client-only renderer, плавная крышка, четыре статических вида останков.

BurialRecords: только Set<barrow UUID> opened, запись по событию первого открытия
естественной колоды. Никаких ticks, проклятий, mobs, глобального поиска или истории
домашних колод. UUID один на structure piece. Полное состояние dungeon отложено.

BurialWeapon использует SwordItem/существующую логику толчка копья. Найденное оружие
слабее восстановленного. Чекан использует MaceItem и существующий CombatEquipment
armor pressure (.22/.30), без bypassArmor. Restoration — armorer_shapeless с древним
оружием, железом, кожей, палкой; отдельного стола нет. Аксессуары — FolkAccessoryItem,
Curios necklace/charm. Лунница: -5% creature damage ночью; гривна -4% armor-sensitive;
grave_ward: -9% только от LivingEntity с CreatureAttribute.UNDEAD (включая его стрелы).
Существующий warding_charm — ритуальный материал, не anti-undead accessory.

DarkenedWood: девять блоков в существующих registries/tab, не новая порода дерева.
Axe strip сохраняет AXIS; vanilla state/model schemas, wood tags/recipes, flammability.
SmokeAging: event-discovered jobs в WorldSavedData; до 512 на dimension. Placement,
interaction и loaded-chunk tile list обнаруживают только вертикальную колонку костра.
Chunk-load позиции обрабатываются на следующем серверном периоде, не вызывая рекурсивной
загрузки чанка из callback. Раз в 20 тиков обходится только ограниченная очередь jobs;
до пяти клеток на колонку, hasChunkAt, без глобального scanner. 1200–4800 тиков
фактической обработки; пауза при погасшем/перекрытом дыме и при chunk unload.
Break target/fire удаляет job; target identity проверяется. AXIS сохраняется.
Лимит 512 — защитное ограничение: последующие новые задания не принимаются до
освобождения места. /setblock не является основным способом регистрации процесса;
для таких случаев взаимодействовать с костром или перезагрузить его чанк.

KurganStructures: три стандартных Structure/StructurePiece в существующем
SwampStructures DeferredRegister. Общая сетка 64/24 чанка, salt 841973; один
псевдослучайный выбор 60/30/10 на candidate. Дополнительное исключение соседних
Great candidate regions делает Great реже номинальных 10%. Биомы vanilla plains и
birch_forest. Наличие biome и terrain не гарантирует попадание каждого grid candidate.
KurganStructure проверяет сухие ground heights с шагом 2, перепад ≤4/6, height 63–140,
близкие кандидаты крупных структур; без загрузки чанков при preflight.
KurganShape: радиусы 14/24/40, высоты 10/18/30, плавный профиль (1-r²)²; основание
сопрягается с локальным terrain, не выравнивает прямоугольник. Piece пишет только свой
chunk clip; обе клетки колоды геометрически помещаются в один чанк. Есть короткая
входная ниша, а не dungeon. Деревья используют TreeShape pine/linden с фиксированным
seed, чтобы порядок чанков не менял геометрию. BurialPlaced сериализуется.

KurganCommands используют существующий namespace/стандартные structure starts;
permission 2, bounded ring search (до 10 regions), biome/type prefilter. Next исключает
радиус 192 от игрока и последний возвращённый экземпляр. Teleport ищет безопасную
поверхность около входа, исключает листья/брёвна/жидкости/опасный грунт.

Ресурсы: tools/burial_084.py, последний слой create_resources.py. Своё pixel-grain
состаривание, native cuboid silhouettes, две собственные mono OGG. Vanilla скопированы
только JSON-схемы древесины, не перекрашенные изображения. verify_burial_084.py
проверяет воспроизводимость, таблицы, recipes, модели/локализацию. Manifest добавляет
динамические darkened registrations в общий verifier. Его запрет smoke test classes
уточнён до реального package org/slavicmyths/smoke; production SmokeAging разрешён.
Java8 KurganShapeTest проверяет форму, границы и 100000 выборов кандидата.
Это не проверка живого worldgen, GUI/сохранений, звука или MSPT.

# 0.8.2 — Соловей-разбойник

NightingaleEntity — отдельный CreatureEntity, 250 HP, bbox 1.1 × 2.35.
Серверная state machine: NOTICE → melee/retreat → inhale/release/recovery;
короткий, тяжёлый удар, толчок, обычный/разрушительный свист и anti-contact radial.
Синхронизируются action, время начала, drawn и сила hurt; модель интерполирует позы.
HP-пороги 150/75 меняют паузы и частоту комбинаций. На свисте направление фиксировано.
С помоста босс физически идёт к открытому южному краю, спускается под гравитацией;
нет телепортации. При потере цели возвращается к home, здоровье не восстанавливает.

WindAttack: только на выпуске, до 64 living targets. WindGeometry — чистая Java
геометрия конуса и точный voxel traversal трёх лучей к корпусу (до 128 шагов каждый).
Полные collision blocks защищают; листва/забор/неполная мебель не считаются стеной.
Только загруженные чанки; незагруженный блок считается укрытием. Эффект ослабевает
с расстоянием и прикрытием. Terrain — 9 дорожек по максимум 20 клеток, максимум
24 разрушения из whistle_fragile, исключение tile entities и Forge mobGriefing.
Частицы — 9 cloud, до 6 dust и 2 leaf за один шаг, 12 шагов обычной волны.
Это статические ограничения работы, не измеренный MSPT.

StrongholdRecords получил два независимых persisted флага на UUID лагеря:
NightingaleSpawned/NightingaleDead. CLEARED обычных защитников не блокирует босса.
LargeCampPiece обрабатывает ровно один marker nightingale и deterministic entity UUID;
ставит spawned только после успешного addFreshEntity. Старая структура обновляется
только при генерации ещё не созданного чанка: нет ретрогенерации загруженных дворов.
Существующий шаблон двора дополнен мебелью, сундуком и маркером; остальные части
лагеря и штатный roster сохранены. tp_nightingale использует существующий поиск
structure starts, выбирает arrival из конкретного LargeCampPiece, затем safe-ground.

После успешного super.die (включая Forge cancellation) фиксируется победа и удаляется
boss bar. Обычный death loot отложен до 55-го тика падения; затем вызывается vanilla
super.dropAllDeathLoot с Forge loot hooks/XP и сущность удаляется. CollapseTicks и
lootReleased сохраняются в NBT. Camp dead записывается сразу при смертельном ударе.
Участники — живые survival игроки в радиусе боя и атакующие; при смерти присутствующим
в том же мире не дальше 64 блоков выдаётся победа. Книга использует bit 27 существующей
маски (28 entries из 32). Свист атакует область, полосы/лут управляются сервером.

NightingaleWhistle: hold 24 тика, 9 блоков, cooldown 600 тиков, малый damage,
отталкивание, тушение огня; terrain destruction выключен. Кинжал: 7 damage,
2 атаки/сек, 700 durability; hold ≥18, release, 3.5 блока, cooldown 140, износ 2.
Player artifacts пропускают боссов (max health ≥100 либо !canChangeDimensions).
Bandit horn использует ту же простую use-механику только для звука, без урона.

NightingaleModel/Renderer — самостоятельная геометрия и UV 256²; BreathingChest
масштабируется на вдохе, руки сегментированы, волосы/борода объёмные. Ножны остаются
на поясе, рукоять скрывается после извлечения. Death renderer отключает vanilla flip.
Ресурсы: nightingale_082.py — модели/UV/локализация/loot/recipe/13 audio events,
15 mono OGG (hurt ×3). Звуки собственного синтеза; пространственные, не UI.
Генератор нормализует Ogg serial/CRC для байтовой воспроизводимости. В общем pipeline
он выполняется после stronghold_081.py. Последний владеет изменённым шаблоном двора.

Тесты: tools/tests/WindGeometryTest.java (JDK8, без Minecraft),
verify_nightingale_082.py (воспроизводимость/двор/лут/tag/models/audio wiring),
verify_stronghold_081.py (roster/шаблоны/мебель), verify_resources.py (production JAR).
Minecraft/manual QA не выполнялся.

# Архитектура

## 0.8.1 — III уровень и мебель

`furniture/Furniture` добавляет 21 блок/22 предмета в существующие registries/tab.
FurnitureBlock — facing и составные collision shapes; обычная мебель без TileEntity.
WardrobeBlock — lower/upper, два блока и drop только lower. Shelf требует опору.
SeatEntity — transient MISC, noSave/noSummon, без визуального renderer; удаляется
после высадки или потери блока. Используется для игрока, не сохраняет сидящих NPC.
RackTile — один ItemStack, серверный обмен, NBT/update packet и сброс предмета при
разрушении; FurnitureClient отображает оружие стандартным ItemRenderer без tick.
Шкаф/тумба/ящик декоративные, собственного inventory не имеют.

`LargeCampStructure`/`LargeCampPiece` — отдельные стандартные StructureStart и
TemplateStructurePiece, регистрация через CampStructures. Сетка 192/64 чанка,
случайное смещение регионов, salt 813797 (3072 блока размер региона, не гарантия
реальных интервалов). Vanilla plains/forest/taiga; hill выбирается в приподнятом биоме.
Проверяются сухой грунт, высота 61–135, локальные перепады ≤3 у зданий/дворов,
≤8/12 у sparse perimeter; общий перепад ≤9/14. Отказ до размещения при нарушении.
Проверка соседних vanilla крупных структур/болотных кандидатов без загрузки чанков.
Малый/средний лагеря уступают large; их roster/Атаман не меняются.

23 NBT в structures/stronghold; forest/plains/hill меняют ломаный периметр и смещения
построек, варианты казармы/кузницы/склада выбираются отдельно. Sparse perimeter
следует высоте каждой колонки; здания имеют локальную основу и входные ступени.
Дворы допускают только малый перепад и локальные грунтовые опоры; общего квадрата
платформы 80×80 нет. Тайник — 35%, под домом или складом, привязан к высоте родителя.
Шаблоны и loot воспроизводятся tools/stronghold_081.py; мебель — furniture_081.py.

StrongholdRecords — отдельный WorldSavedData без глобального tick handler:
UUID→spawned/dead long masks, CLEARED, пять bell positions/state/deadlines.
26 защитников, slots 0/1 — Атаманы, вспомогательные horse/prisoner slots ≥40.
Повторные спавны блокируются маской и deterministic entity UUID; успех addFreshEntity
проверяется перед фиксацией. Зачистка: два Атамана и ≥18 погибших защитников;
достижение получает игрок последнего нужного убийства. CLEARED сохраняется,
тревога и поздние marker spawns отключены; ownership/boss-state сейчас не добавлялись.

StrongholdGoal работает только для campTotal=26. Проверка раз в 20 тиков на загруженном
NPC, без поиска всей структуры: короткое наблюдение (50 тиков), путь к известному
колоколу, локальный ALERT. Проверенные неразрушенные соседние сигналы передают
тревогу по одному ребру за 100 тиков. Незагруженные чанки не подгружаются; нет передачи
player UUID/координат дальним NPC, они ищут цель обычным локальным goal.
После 1200 тиков без нового звонка тревога затихает. При уничтожении сигнала передача
через него прекращается; уже поднятая локальная тревога не отменяется мгновенно.
Patrol/guard/training/smith используют home/duty; кузнец имеет видимый фартук.
NPC открывают деревянные двери стандартным OpenDoorGoal; атаки остаются 0.8.0.

Книга: bit 26 для black_feather. Только lore, без entity/boss bar/свиста Соловья.
Проверки 0.8.1: data/NBT/roster/headroom/ladder support, без игрового запуска.

## 0.8.0.1 — древесина и леса

`wood/Woodlands` регистрирует четыре упорядоченных семейства через существующие
ModBlocks/ModItems, в существующей Creative Tab. Каждый набор: log/wood/stripped
log/stripped wood, planks/leaves/sapling, stairs/slab/fence/gate/door/trapdoor,
pressure plate/button, standing/wall sign. 69 блоков и 66 предметов всего.
`TimberLog.getToolModifiedState` использует Forge 36.2.42 API и сохраняет AXIS.
Горение задаётся Forge block overrides; `WoodlandFuel` — карта Item→burnTime,
читаемая только событием печи, включая gates/signs. Vanilla tags дополнены без replace.

`TreeShape` — чистая геометрия без доступа к миру: отдельные linden/rowan/willow/pine
алгоритмы. Ветви face-connected, нерегулярные листовые массы; bounded BFS вычисляет
vanilla distance≤6 и исключает неподдержанные листья. `WoodlandFeature.grow` сначала
проверяет ВСЕ клетки и грунт, затем размещает: воздух, собственный саженец и обычная
мягкая растительность разрешены; чужие деревья, постройки, жидкости и грунт не заменяются.
`WoodlandGrower` переопределяет Tree.growTree: общий путь с worldgen, без vanilla oak/spruce
генератора. Natural feature использует ground heightmap; у ивы максимум 9×9×3 проверок
воды. Варианты выбираются Random, история каждого дерева не сохраняется.

Worldgen — standard configured features + BiomeLoadingEvent, только vanilla Overworld.
Шанс попытки на чанк: forest/dark forest липа 1/4, рябина 1/8, сосна 1/24,
ива 1/12; taiga сосна 1/2 и рябина 1/12; plains липа 1/24 и рябина 1/12;
swamp ива 1/2 и рябина 1/12; river ива 1/4 и рябина 1/24.
Отбраковка занятых крон и неподходящего грунта снижает реальную частоту; она не измерена.
Vanilla features не удаляются, forest manager/chunk polling отсутствуют.

`RowanLeaves` сохраняет berries boolean вместе с vanilla leaf state: серверный ПКМ
сначала снимает berries, затем выдаёт 1–2 ягоды. Random tick восстанавливает урожай
с шансом 1/12 при distance<7 и свете≥9. Leaf loot не сохраняет ягодное состояние:
обычная добыча даёт саженцы/палки, ножницы/Silk Touch — обычные листья, без двойной награды.
`HangingLeaves`: cross model, без collision/TileEntity/анимации; цепочки 1–3 блока,
поддержка верхним блоком ивы/подвесом, scheduled tick на потерю опоры и random tick страховка.

Таблички: отдельный TileEntityType с восемью допустимыми блоками, SignTileEntity
переопределяет getType для сохранения собственного ID; стандартный клиентский renderer,
четыре WoodType/atlas textures 64×32. Client-only setup изолирован в WoodlandClient.
Книга: новый knowledge bit 25, открываемый woodlands advancement.

`tools/woodlands_0801.py` — последний слой create_resources.py; собственные рисунки,
vanilla 1.16.5 blockstate/model/loot/recipe schemas из установленного client-extra.jar
(не импортирует vanilla текстуры). `verify_woodlands_0801.py` проверяет ресурсы и запускает
только pure Java TreeShapeCheck (1024 формы), вне production JAR. Игровых запусков 0.

## 0.8.0 — бой, оружейник, лагеря

`armorer/`: custom IRecipe serializers shaped/shapeless, сетка 4×4, серверный
результат и расход/остатки ингредиентов; закрытие возвращает входные предметы.
Shift-click результата требует места под полный выход. JEI изолирован в compat.
`combat/`: MaceItem имеет собственные атрибуты без sword sweep; бронебойность
обрабатывается LivingHurt. Кистень: основная рука, заряд 18 тиков, дальность 4 блока,
перезарядка 40 тиков, серверная проверка луча. Пластинчатый нагрудник: скорость −5%.
`bandit/BanditEntity`: стандартные goals и ограниченные cooldown, melee windup/
recovery, лук с дистанцией 8–14 блоков, щит, тяжёлая атака и команда Атамана.
Команда ищет союзников локально только по событию, не глобально каждый тик.
Steve-like BipedModel, HeldItemLayer, 20 собственных текстур; семь original OGG.

CampStructures: стандартные StructureStart/TemplateStructurePiece, vanilla
plains/forest/taiga, авторские 10 NBT. Малый лагерь 25×25, средний 43×38.
Сетки spacing/separation: 48/16 и 64/24 чанка (768/1024 блока между регионами).
Это параметры кандидатов, не измеренные расстояния между фактическими лагерями:
биом, рельеф и резерв соседних структур могут отбраковать кандидата.
Высоты отдельных частей проверяются до размещения, глобального выравнивания нет.
CampRecords WorldSavedData хранит UUID и битовые маски состава/смертей; маркеры
задают ровно 5/9 жителей, у среднего ровно один Атаман. Нет периодического respawn.
Поиск команд ограничен семью кольцами, подтверждает STRUCTURE_STARTS; способен
генерировать эту стадию чанка как vanilla locate, не запускается фоновым polling.
HUD читает crosshair либо ID из S2C принятого урона, timeout 100 тиков; world scan нет.
Клиентские экран/рендерер/HUD отделены от common/server.

Ресурсы последовательно генерируются tools/create_resources.py; финальный слой
0.8.0 — tools/bandits_080.py. Собственные OGG имеют стабильные serial/CRC.
Существующие registry ID, включая mace/perunite_mace, сохранены.
Проверки структуры данных не подтверждают игровую синхронизацию/сохранение/баланс.

## 0.7.3 — структуры топей

`swamp/SwampStructures` регистрирует семь Structure<NoFeatureConfig> через Forge
DeferredRegister, configured structures и один тип TemplateStructurePiece. Явная
стадия SURFACE_STRUCTURES; только vanilla swamp, underwater_ruins также river.
При загрузке Overworld копируется structureConfig с сохранением чужих параметров;
добавляются только отсутствующие настройки мода. Единственный mapped reflection
доступ к `DimensionStructuresSettings.field_236193_d_` нужен из-за immutable codec maps.
API проверен по локальному Forge 36.2.42 mapped JAR; client/common разделение сохранено.

Spacing/separation в чанках: изба 24/12, поселение 52/26, гать 18/9,
святилище 48/24, стан 30/15, руины 22/11, малые POI 10/5. Это параметры кандидатов,
а не измеренные частоты: biome/terrain/collision rejection дополнительно сокращают их.
Перед стартом bounded noise-only проверка соседних кандидатов крупных vanilla и
приоритетных custom structures. Порядок приоритетов исключает взаимное наложение
наших разных типов независимо от очередности генерации. Это консервативный резерв,
а не глобальный анализ фактических построек; совместимость с произвольными чужими
структурами/изменёнными datapack сетками не гарантируется.

`SwampStructure.Start` формирует конечный список частей, загружает авторские NBT и
проверяет footprint через base-height queries без загрузки чанков. Все части валидируются
до принятия старта. Нет выравнивания всего поселения; отдельные основания и пути имеют
свои высоты. Подводные руины ограничены глубиной, стан требует сушу у навеса и воду у
конца причала. `SwampPiece` сохраняет имя/позицию/seed/EncounterProcessed, размещает
template с vanilla chunk bounding box, добавляет максимум восемь блоков опоры вниз.
Пути — sparse-шаблон, локальная высота каждой колонки, деревянный настил вместо грунта
над водой. Отсутствующие в шаблоне блоки не заменяются воздухом.

Loot markers выставляют vanilla barrel с lazy loot table и стабильным seed.
Encounter marker имеет одну сохранённую попытку на часть: Кикимора 10%, водные духи
12,5%, peaceful и коллизии исключены. Нет спавнеров, повторных попыток или тик-менеджера.
Старые DeepPoolFeature/малые постройки WaterFeature пропускают чанки, уже содержащие
references новых структур, без загрузки соседних чанков. Растительность сохраняется.

`SwampCommands` — permission 2, сервер, только явный вызов. Swamp ищет noise biomes
в радиусе до 8192, пропускает 1280 вокруг игрока уже в болоте и проверяет промежуточный
неболотный участок. Для безопасной посадки загружается только ближайшая область цели.
Structure search ограничен 12 кольцами сетки, проверяет настоящие STRUCTURE_STARTS;
возвращает координаты без размещения шаблона командой. Next пропускает ближайший старт.

Discovery использует vanilla location trigger/feature predicate; новый PlayerTickEvent
не добавлен. Новые knowledge bits 14–19 продолжают прежний int S2C без смены старых битов.
Источник ресурсов — последний слой `tools/swamp_073.py` в `create_resources.py`.
`verify_swamp_073.py` отдельно декодирует NBT, проверяет границы, маркеры, loot references,
варианты, повторяемость и упакованные ресурсы. Это не визуальный или игровой тест.

## 0.6.5 — wildlife

Шесть registry ID используют один серверный `WildlifeEntity` с фиксированным видом,
синхронизированным locomotion state и разными goal-наборами/attributes. Клиентские
`WildlifeModel` и renderer строят отдельные пропорции медведя, медвежонка, волка,
кабана, оленя и оленихи; gait, run, swim и attack pose зависят от состояния.
`WildlifeSpawns` использует стандартные placement/biome spawn lists без поиска мира
в тике. `generate_065.py` — последний ресурсный слой: текстуры, loot, food, recipes,
advancements, звуковые event-ссылки и локализация.

## 0.6.0 — RPG

`PathData` использует `PlayerPersisted/SlavicPaths`; Main/Secondary по умолчанию
пусты, навыки — ranks. `RpgNbt` меняет только собственные namespaces в NBT.
`RpgMenu` валидирует блок, расстояние, XP, материалы, путь и prerequisites на сервере.
Операции над предметом выполняются в первом слоте; при закрытии все слоты возвращаются.
`Runes` определяет категории/лимиты через tags и vanilla типы, не через один класс.
Proc cooldowns принадлежат игроку, что исключает сброс сменой оружия. Лук сохраняет
снимок использованного предмета в projectile NBT. Forge 36 не имеет ShieldBlockEvent:
щит подтверждается приростом vanilla blocked-damage stat после завершения hurt().
Обработчики событий проверяют используемый предмет; tick-проверка только завершает
помеченный блок/разряд, без обходов инвентаря или всех навыков. Клиентский ввод G
не передаёт стоимость/силу; сервер проверяет изученную выбранную способность и откат.
`generate_060.py` — последний слой генератора: native JSON voxel-модели с vanilla
материалами, рецепты, tags, достижения и RU/EN. Новых raster masters не требуется.

## 0.5.1 — представление духов

Последний этап `create_resources.py` — `generate_051.py`: родительские ModelRenderer,
пиксельные атласы и оригинальные mono OGG только для Полудницы, Полевика,
Банника и Кикиморы. Звуки привязаны к существующим действиям на сервере;
редкие ambient/notice имеют интервалы. Геометрия Полудницы следует прежним
20 тикам формы; высота hitbox согласована с моделью. AI и игровые параметры
сохранены. Старые генераторы выполняются до 0.5.1, чтобы не откатывать ресурсы.

## 0.4.6 — ресурсы

`tools/generate_046.py` вызывается последним из `create_resources.py` и заменяет
только выбранные пиксельные textures и JSON models. Физические блоки используют
несколько cuboid elements и отдельные текстуры граней; Java renderer не добавлен.
Registry ID и механики 0.4.5 сохранены.


## Добавление 0.4.5

`create_resources.py` последовательно вызывает исторические генераторы 0.2/0.4/0.4.1
и последним `generate_045.py`, который задаёт актуальные уникальные текстуры,
модели, локализации и data-ресурсы. `visual_audit.py` проверяет модели и PNG и
строит контактный лист. Новый контент остаётся в прежних DeferredRegister и tab.
Amber встречается в loot капища, без новой руды и ретрогенерации.

Ритуалы имеют шесть фиксированных определений в `Rituals`; altar хранит номер и шаг
в NBT, старые сохранения без номера открываются как первый обряд. JEI изолирован.
Серебряная уязвимость и лесные духи задаются entity type tags. Урон и защита
обрабатываются событиями; импульс посоха — только по right-click, с ограниченной
областью и cooldown. Щиты используют vanilla ShieldItem и плоские item models.


## Добавление 0.4.1

Новые предметы и блоки зарегистрированы в прежних DeferredRegister без изменения ID.
Серебряная руда и четыре BushBlock-травы используют ConfiguredFeature в
BiomeLoadingEvent; нет поиска чанков в игровом тике. Снаряжение использует vanilla
SwordItem/ToolItem/ArmorItem и собственные ограниченные tier/material. Данные,
текстуры и рецепты 0.4.1 воспроизводит `tools/generate_041.py`, вызываемый перед 0.4.5
из `create_resources.py`. AltarTileEntity хранит вид обряда и шаг в NBT;
старое сохранение без RitualKind читается как первый обряд. Три определения в Rituals
показаны необязательным JEI. Громовой топор обрабатывает удар на сервере с cooldown;
защита оберега и полного комплекта применяется по событию урона без tick polling.

## Актуальное расширение 0.4.0

Разделы ниже описывают историю 0.1/0.2, а не полный текущий состав.

- Существующие реестры и ID сохранены; добавлены один TileEntityType и один Feature.
- DomovoyEntity: Map UUID → reputation/attacks/nextGift только для взаимодействовавших
  игроков. Home, owner, dimension и nextBlessing в Entity NBT. Deadline использует gameTime.
  Сервер расходует подарки и возвращает тару. AvoidEntityGoal/MoveTowardsRestrictionGoal
  отвечают за избегание/возвращение. Очаг ищет духа только по клику в AABB радиусом 8.
  Разрушенный очаг не снимает persistence; помощь требует существующий очаг.
- LeshyEntity: Slowness по успешному удару; escape по урону при низком HP,
  максимум шесть позиций, loaded-chunk check, vanilla randomTeleport проверяет безопасность.
  Deadline сохраняется. Зов использует vanilla ambient cadence; поиск игрока только при звуке.
- ShrineFeature: стадия SURFACE_STRUCTURES, vanilla forest/plains/taiga, проверка Overworld,
  1/96 попытка до проверки местности. Вся площадка 11×11 внутри одного чанка.
  До записи проверяются почва и четыре слоя препятствий/жидкостей (до 605 проверяемых позиций).
  Нет тикового поиска/ретрогенерации. Это Feature, не StructureStart: `/locate` и карт нет.
- FirstRitual определяет порядок/результат; AltarTileEntity исполняет и хранит int step;
  AltarBlock передаёт interaction. TileEntity не тикает. Обряд общий, результат последнему
  участнику. Полный инвентарь выдаёт предмет рядом с игроком; creative не расходует дар.
  Разрушение сбрасывает шаг без возврата принятых компонентов.
- Knowledge: source of truth — vanilla advancements. Они уже сохраняются по игроку,
  переживают смерть/вход и синхронизируются Minecraft. Дублирующая capability не нужна.
  Будущая система классов потребует отдельных данных и остаётся планом.
- LoreBookItem открывает книгу на сервере; LoreNetwork отправляет один S2C varint по
  открытию. Направление фиксировано; C2S выдачи знаний нет. LoreScreen — в client,
  вызов через DistExecutor. Нет периодических пакетов/глобальных Map игроков.
- compat.JeiRituals загружается только JEI @JeiPlugin. Common setup на него не ссылается.
  CompileOnly API + optional runtimeOnly; сторонних JAR в моде нет.
- generate_milestone.py выполняется последним в create_resources.py; это источник данных 0.4.
- tools/smoke включается только с -PsmokeTest, не входит в production. Тестовый стенд
  может загружать чанки/выдавать предметы только в отдельной копии мира.

Performance: нет новых глобальных tick-handler, тикающего жертвенника, поиска мира,
частой синхронизации или принудительной загрузки чанков в AI. Замеры и ограничения —
VERIFICATION_0.4.0. Исторические утверждения «нет сети/духов» ниже относятся к 0.1/0.2.

## Исходное состояние

Перед изменениями выполнен поиск Forge-проектов и AGENTS.md в рабочей папке
Documents. Проект, реестры, ресурсы и существующие системы не обнаружены.
Documents не является Git-репозиторием. Создан отдельный каталог slavic-myths;
чужие файлы не изменялись. Git-репозиторий и commit не создавались.

## Основа 0.1.0 (сохраняется в 0.2.0)

- `org.slavicmyths.SlavicMyths`: единственная @Mod-точка входа, MOD_ID и подключение реестра к mod event bus.
- `registry.ModItems`: единственный DeferredRegister<Item>, ленивые RegistryObject.
- `assets/slavicmyths`: модели item/generated, PNG 16×16 RGBA и ru_ru/en_us.
- `data/slavicmyths`: стандартные рецепты, advancement inventory_changed и открытия книги рецептов.
- `META-INF/mods.toml`: точные ограничения Minecraft и Forge; версия из Gradle.

Java 8, Forge 36.2.42; official mappings 1.16.5. В этих mappings используются
Item.Properties.tab, ItemGroup.TAB_MISC, Rarity.RARE. Forge RegistryObject находится
в net.minecraftforge.fml. Не переносить имена классов из современных версий.
Использованы wrapper из официального MDK и ForgeGradle 5.1.77 / Gradle 7.3.3
для работы сборочной JVM на Java 8. Более новый MDK использует другой стек сборки.

Регистрация общая для клиента и сервера. Клиентский рендер 0.2.0 изолирован в client.ClientSetup (Dist.CLIENT).
Ни одного tick-handler, сетевого пакета, глобального поиска, состояния игрока,
собственной системы достижений или фонового потока не создано.

## Расширение по необходимости

Предмет добавляется в существующий ModItems, затем получает модель, текстуру,
локализацию и нужные data-ресурсы. Блоки и BlockItem — отдельный реестр блоков
и тот же реестр предметов. Руды и растения опираются на блоки и будущий worldgen.
Мобы/NPC/духи/монстры получают реестр EntityType только при первой реализации;
клиентские рендеры должны быть изолированы от общих классов.
Структуры и измерения проектируются отдельно под worldgen API 1.16.5.
Рецепты, лут и advancements остаются data-driven.
Боги пока описываются в документации, не являются обязательными NPC.

Ритуалы, знания, классы и способности в будущем — отдельные пакеты систем.
Не размещать прогрессию в основном классе, ModItems или глобальном Map игроков.
Когда появятся данные игрока, добавить один провайдер Forge Capability с
версионированным NBT и небольшими логическими разделами. Это план, не код.
Подробности и нерешённые правила: CLASS_SYSTEM.md.
Не создавать интерфейс универсального модуля, собственную шину событий или DI
ради трёх предметов. Стабильные ID служат точками связи контента и будущих систем.

## Производительность будущих систем

Сервер — источник истины для боя, энергии, знаний и ритуалов. Проверять действие
по событию/запросу; ограничения дальности и частоты, права и стоимость — на сервере.
Синхронизировать изменённые поля адресно при входе и изменении, не каждый тик.
Временные эффекты: ограниченные очереди и бюджет работы, очистка при завершении.
AI: ограниченная частота поиска и область; worldgen — на генерации чанка,
без сканирования загруженного мира в игровом тике. Частицы — клиентские с лимитом.
Измерять MSPT, количество пакетов и расход памяти на реальном сценарии до расширения.

## Источники API и сборки

- https://docs.minecraftforge.net/en/1.16.x/concepts/registries/
- https://docs.minecraftforge.net/en/1.16.x/gettingstarted/
- https://files.minecraftforge.net/net/minecraftforge/forge/index_1.16.5.html

Источником истины совместимости служит сборка против закреплённого Forge и игровой тест.

## Реализация 0.2.0

- ModItems остаётся единственным реестром предметов (10 ID), ModBlocks — блоков
  (flax, wormwood, perunite_ore). Три прежних registry ID не изменены.
- ModItemGroup: одна Creative Tab с ленивым созданием иконки.
- Растения используют BushBlock: без randomTick, стадий роста и BlockEntity;
  стандартная проверка почвы/соседей, cross-модель и cutout на клиенте.
- ModWorldGen регистрирует 3 ConfiguredFeature в common setup enqueueWork.
  BiomeLoadingEvent с HIGH дополняет списки генерации, не заменяет чужие features.
  Руда: NATURAL_STONE, size=3, range(15) означает исходную Y=0..14,
  шанс одной попытки 1/2 на чанк. Для сравнения vanilla diamond — size=8,
  одна попытка на чанк. Фактическая жила может содержать меньше блоков.
- Лён: до 24 попыток RANDOM_PATCH с шансом 1/3 на чанк; полынь: 16 с шансом 1/4.
  Размещение HEIGHTMAP_SQUARE; пригодность позиции проверяет vanilla generator.
- Ограничение: только vanilla-биомы, исключены NETHER/THEEND/NONE. Это привязка к
  биомам; стороннее измерение, переиспользующее Overworld-биом, может унаследовать
  его features. Гарантия отдельной dimension whitelist сейчас не заявляется.
- Никакой ретрогенерации и runtime-обходов чанков. Все три features работают только
  в штатном pipeline генерации новых чанков. Сохранённый тестовый мир содержит ресурсы.
- Руда — Block с harvestTool(PICKAXE), harvestLevel(2), requiresCorrectToolForDrops.
  Loot table выдаёт ресурс с Fortune либо блок с Silk Touch; XP не добавлен.
- FernFlowerModifier — один Forge Global Loot Modifier с сериализатором из ModLoot.
  JSON сначала ограничивает loot table до minecraft:blocks/fern, затем исключает
  ножницы/Silk Touch и применяет шанс 0,002. Исходные drops сохраняются.
  Это короткая проверка при генерации loot, а не обработчик каждого тика.
- Береста получается рецептом с расходованием бревна; никаких событий для деревьев.
- tools/create_resources.py — единая точка генерации, generate_data.py и
  generate_textures.py содержат новые данные/пиксели. Старый генератор обновлён,
  повторный запуск не возвращает рецепты 0.1.0.

Аудит производительности: PASS по статическому анализу. Нет polling игроков,
мира, блоков, тяжёлых тиковых обработчиков, сетевых пакетов и сохранённых данных игрока.
Число попыток генерации ограничено. MSPT и масштабная статистика не измерялись.

0.4.7: MythShieldItem overrides Forge isShield; blocking property/models are client-only. Weapon JSON and pixel material sources: tools/generate_047.py. ThunderSpearCombat augments accepted server melee hits with a 120-tick item cooldown; no terrain effects.

0.5.0: six separate entity/model classes; LandSpiritEntity shares bounded encounter/persistence helpers. Models are generated from explicit native cuboid masters in generate_050_models.py. Synced visual states, persistent cooldowns/offering UUIDs; homesteads occupy one chunk, guards store home positions. Hot stones use Forge spawn packets. Sound sources are original synthetic PCM encoded as Vorbis.
# Финализация 0.6.5

0.7.2: depth/ElderVodyanoy использует общую водную навигацию, но отдельные модель,
анимации и собственное расписание атак без vanilla instant melee. Стандартный ServerBossInfo.
PoolStoneTile хранит state/intro/UUID, вызывает spawn один раз; PoolIndex (WorldSavedData)
хранит известные/побеждённые омуты. Знак обращается к индексу только при использовании,
не ищет/генерирует чанки. DeepPoolFeature ограничен одним чанком. При фазе III сохраняется
слой до 121 блока, источники воды добавляются только над низкими выступами; при победе
вода в сохранённом слое восстанавливается. Амулет использует прежний Curios necklace.
Новые ресурсы — depth_072.py, последний слой общего генератора.

0.7.0: пакет water добавляет RiverFish поверх AbstractFishEntity, WaterSpirit поверх
существующего LandSpiritEntity, обработчик ItemFishedEvent без замены vanilla loot tables.
FishingNetTile сохраняет четыре улова, износ и владельца; только scheduled ticks раз в
6000–8400 тиков, ограниченная проверка соседства 7×3×7 без загрузки чанков.
VodyanoyEntity хранит до 64 локальных отношений; уведомления о вылове приходят по событиям.
RusalkaEntity проверяет слушателей раз в 20 тиков. RusalkaSong считает очарование на сервере,
корректирует скорость существующим velocity sync и не принимает Charm state от клиента.
WaterFeature размещает растения/малые POI в пределах чанка. Ресурсы — water_070.py;
JeiKitchen изолирован как optional JEI plugin. Книга расширяет существующую битовую маску знаний.

Дополнение артефактов: ArtifactEvents добавляет при появлении моба лёгкие Goal для музыки
и временного защитника. Поиск музыкой — раз в 10 тиков только во время использования,
радиус 12 и до 64 обрабатываемых существ. WildlifeEntity точечно пропускает собственный
encounter-контроллер для этих состояний. Владелец/срок призыва хранятся в Forge persistent NBT,
готовность Посоха — в PlayerPersisted. SkatertTile не тикает: используются scheduled block ticks,
порции синхронизируются blockstate, возврат предмета централизован в onRemove без обычного loot.
GusliClient запускает заранее записанный OGG и прекращает его при отпускании/удалении игрока.
Ресурсы дополнения генерирует tools/artifact_065.py последним слоем общего генератора.

FolkBerryBush использует random ticks, AGE и HALF; BerryPatchFeature создаёт небольшие
группы только при генерации чанка. FolkBerryItem сохраняет прежние ID ягод.
KitchenMenu выполняет рецепты KitchenRecipes на сервере; стол не имеет ticking block entity,
при закрытии меню ингредиенты возвращаются игроку.
FlyingVessel считает физику и сохраняет владельца, транспорт и 9 грузовых слотов в NBT.
FlightNetwork передаёт только ограниченные управляющие значения, без клиентской позиции;
ванильный controlling passenger отключён, чтобы CMoveVehiclePacket не управлял движением.
CargoMenu использует обычную серверную синхронизацию слотов, блокирует вложение транспорта
и подбор при открытом грузе. FlightClient/FlightRenderer отвечают за интерполяцию и позы.
Tailwind ограничен FlightItem. Новые ресурсы воспроизводит tools/finalize_065.py,
вызываемый последним из общего генератора. Curios остаётся единственной системой аксессуаров.


## 0.8.6: finite kurgan encounters

KurganFighter хранит фиксированные профили/тайминги; KurganRoster — чистую
детерминированную политику населения. KurganCreature использует единственный
боевой Goal: серверный release, sync action/start/phase/guard/decoy позиции,
ограниченные cooldown, память 120 тиков, navigation cadence 8 тиков. KurganBolt
— физический ProjectileItemEntity, нормальный урон 6, срок жизни 50 тиков.
KurganCreatureModel/Renderer полностью клиентские; геометрия и pose ветвятся
по фиксированному виду, два ложных силуэта не создают дополнительные entities.

KurganEncounters подписан на PlayerTick раз в секунду, использует существующий
chunk index BurialRecords. До первого спавна проверяет позиции всей группы;
не загружает чанки. KurganEncounterState (triggered/cleared/live UUID) вложен
в NBT экземпляра; отсутствие entity среди загруженных никогда не считается
смертью. Повторный спавн запрещён флагом triggered. Ограниченный summon и
боевые фазы сохраняются в entity NBT. BossDefeated предотвращает повторную
проклятую причину. KurganCurse.sourceDefeated удаляет один UUID кургана из
Overworld player/source map; отсутствующий source очищает устаревший сильный
эффект при следующем login/tick, включая офлайн победу. Старый NBT без
Encounters/BossDefeated загружается с пустым состоянием.

tools/kurgan_086.py воспроизводит только ресурсы 0.8.6, не затрагивая старое
оружие. Manifest содержит все fallback sources. verifyKurgan — чистая JVM
геометрия/NBT/roster проверка, не игровой AI тест и не измерение MSPT.


## 0.9.0 Hunt I

HuntMob наследует LandSpiritEntity для существующего Home/guardian/save API,
но отключает старое расписание usesSpiritSchedule и использует один Fight Goal.
OvinnikEntity сохраняет registry/class ID; VolkolakEntity — второй фиксированный
профиль. Synced action/start/rage управляют native HuntModel. Овинник применяет
physical EmberClump и ограниченный 40-tick trail без блоков/terrain grief.
Combo follow-ups учитывают vanilla hurt window только для собственного удара.
Старые saved attributes Овинника переводятся на 175/10/10/2/.55/32 без autoheal.

HuntRecords — минимальный WorldSavedData с последней охотой каждого owner:
target UUID, anchor, kind, status, HornReady. Отсутствие loaded entity не
завершает запись. Смерть = CLEARED, admin/Peaceful = FAILED. Summoned owner
сохраняется в entity NBT, смена измерения блокируется. После admin clear
выгруженный участник удаляется при следующей загрузке без награды.

HuntItems/HuntSpawns используют только use/placement scans с ограниченными
радиусами и количеством кандидатов. Проверяется только loaded пространство;
нет forced chunks и player tick scans на 128 блоков. Cooldown рога в records,
ванильный item cooldown не блокирует смену цели/локализованную причину отказа.

PouchMenu использует существующий RpgMenu.MENUS, 9-slot Inventory на точном
held ItemStack identity. Запись NBT немедленная; pouch slot/swap защищены,
tag применяется к ручной вставке/shift move. Vanilla ItemStack copy выполняет
глубокую копию nested NBT. PouchScreen переиспользует стиль CargoScreen.
HuntEffects проверяет Curios; stable transient UUID пояса и огненный множитель
без inventory-passive эффектов/дубликатов. SilverCombat использует один item
tag/helper, Volkolak получает ×1.25 в LivingDamageEvent после брони.
HuntAccessoryRenderer использует существующий Curios body API; все вызовы
client методов в OnlyIn/client коде. Custom category JEI не нужна для JSON
crafting. tools/hunt_090.py — воспроизводимая отдельная прослойка native art.

verifyHunt не запускает Minecraft: owner/UUID/state/cooldown NBT, модификаторы
по времени, сроки способностей и глубокая копия nested pouch NBT. Это не
runtime proof отсутствия duplication/AI bugs; ручной QA выделен отдельно.

## 0.9.4 stage-2 data boundaries

ItemState owns persistent/synchronized immutable components; player/entity/world NBT stays separate. ArmorerInput keeps the full grid instead of vanilla trimming; RecipeHolder owns recipe identity. Curios uses data-driven slots and client-only renderer registration. Optional JEI has no common entrypoint reference. All old content retained; source migration is not runtime acceptance.


## Runtime audit 1.3.4.3 — 2026-10-08

Аудит без изменения production кода/баланса/ресурсов. Clean build успешен; проверены 659 предметов,3018JSON,901PNG и исключение тестов из JAR. Реальных запусков:7 GameTest servers и1 dev client; итоговый37/37 проверяет наблюдаемое поведение, включая дефекты A02 (Enemy predicate пропускает Соловья без lastHurtMob) и A18 (duplicate depth_gift рано открывает lore_keeper). Native capture клиента недоступен; визуальные симптомы, ручной игровой проход, multiplayer/restart и MSPT не подтверждены. Отчёт и предложения: [docs/audit/runtime-1.3.4.3/AUDIT_REPORT.md](audit/runtime-1.3.4.3/AUDIT_REPORT.md). Ничего не исправлено и не отправлено в Git.

Fixtures вне main/source JAR, opt-in init script; ServerPlayer stub listener и owner UUID registration только для серверных тестов. Общий target contract должен учитывать hostile PathfinderMob, allied/NPC/PvP; расширение knowledge требует versioned protocol вместо бесконтрольного расширения int mask. Текущая архитектура этим проходом не менялась.


## Модуль песиголовцев 1.6.5 — 2026-10-10

`pesigolovtsy/DogheadContent` регистрирует единый EntityType/три role-яйца, custom arrow, звуки и biome codec. `DogheadEntity` — серверный автомат marker/cooldown/LOS/персональной ауры; `DogheadPacks` — UUID/WeakReference индекс только загруженных сущностей, поддерживаемый Join/Leave, без глобального поиска/загрузки чанков. Сигнал не создаёт NPC и не переносится на чужие стаи. NBT сохраняет роль/HP/pack/cooldowns и сбрасывает незаконченные атаки. `DogheadPose/Keyframes` содержат общую математику и CSV; `client/DogheadClient/Mesh` — только клиентские native rigs/IK/UV. Состояние погони и наклон укола передаются сервером; оружие и обе руки наклоняются вместе по низкой цели. Групповые запросы bounded; formation25 ticks, редкая добыча200 ticks; нет сканирования всех игроков/мира каждый тик.

Runtime-fixtures в opt-in `tools/pesigolovtsy-runtime` вне production JAR. Три dev-клиента и7 GameTest-серверов действительно запускались; финальные7 сценариев PASS, кадры и настоящий QA-relog сохранены. Это не проверка two-client MP/MSPT/длительного естественного спавна/прослушивания. [Границы и evidence](verification/pesigolovtsy-1.6.5/REPORT.md), [реализация](pesigolovtsy/IMPLEMENTATION.md). Production построен в изолированном snapshot1.6.5, без изменения параллельных файлов Кузи.


## Кузя и интерьер Яги 1.6.6 — 2026-10-10

Добавлен канонический домашний NPC `slavicmyths:kuzya`: нативная модель/33 клипа, 48 OGG, физический маршрут на печь, бытовые сцены, гостевые таймеры и явный repair. Новые дома получают интерьер/блюда/умеренный loot; старые автоматически не перестраиваются. Песиголовцы 1.6.5 сохранены. [Реализация](kuzya/IMPLEMENTATION.md).

Clean build PASS; 471427 offline checks Кузи, 73926 regression checks песиголовцев, 31058 resource/JAR checks PASS. Реальных запусков этой задачи: 10 GameTest-серверов, 0 клиентов; финальный интеграционный сценарий PASS. Кандидат установлен после gates в существующий PolyMC1.21.1-Testing, backup1.6.5 вне mods, 474 прочих файла сохранены. Commit не создавался.

Открыты клиентская визуальная/звуковая приёмка, полный длительный бытовой прогон, two-client MP, настоящий перезаход и MSPT. Четыре ориентации не реализованы: сохранена северная генерация. Эти пункты не считать выполненными. [Отчёт](verification/kuzya-1.6.6/REPORT.md).


## Доработка Кузи 1.6.7 — 2026-10-11

По пользовательским скриншотам убран контрастный пиксельный шум модели, исправлен UTF-8 импорт русских реплик. Вместо TTS подключены пять выбранных оригинальных мультфрагментов; 20 старых sound registry ID сохранены как aliases, синтетические файлы исключены из production. Таймер домашнего занятия независим от клипа/гостевых реакций; первое занятие8s, паузы15–45s. Интерьер оставлен принятым пользователем.

Clean build PASS; 471397 offline checks Кузи, 73926 regression checks песиголовцев, 31106 resource/JAR checks PASS; 3 новых GameTest-сервера (финальный autonomous + ПКМ + физическая печь PASS), 0 клиентов. Кандидат установлен после gates в PolyMC1.21.1-Testing, backup1.6.6 вне mods, 484 других файла сохранены. Миры и остальные системы сохранены. Commit не создавался.

Человеческое прослушивание/игровой микс, client visual, длительный бытовой прогон, MP/relog/MSPT остаются открыты. Локальный ASR не приравнивается к прослушиванию. [Отчёт и оригинальные аудиофайлы](verification/kuzya-1.6.7/REPORT.md).


## Kuzya loaded household scenes1.6.8 (2026-10-11)

KuzyaEntity maintains a per-resident scene scheduler: initial delay20–60 ticks, rest60–120 ticks, due check1Hz. Chores select among four fixed anchors, avoid the previous spot when alternatives are safe, and use ordinary local navigation. Routes clear stale scene queues. Native path rebuilding is capped at once/40 route ticks; no global scan or forced chunk loading.

Schema2 persists clip/age, queue≤16, route≤8, waypoint, movement deadline remainder, eating/rest state and timers. Restore validates finite home-bounded coordinates and known clips, preserves actual entity position, resets native navigation for local regeneration and skips historical sound markers. Elapsed world time (bounded24000 ticks) ages sleep/idle/speech cooldowns, not physical motion; offline server time does not advance gameTime. Schema1 activates immediately and recognizes atop-stove rest. NBT fixture is not a real relog. UTF-8 resource generation is mandatory; packaged exact entity-name and all owned-key mojibake checks prevent the observed CP1251 regression. [Verification](verification/kuzya-1.6.8/REPORT.md).

## Баюн и sparse книга 1.6.9 (2026-10-11)

`bayun/BayunContent` использует существующие DeferredRegisters сущностей/предметов/features/sounds; `BayunHomes` — per-dimension home→canonical UUID SavedData. `BayunEntity` работает только в загруженном мире: local notice12b раз10ticks, ≤32 персональных sessions, UUID player lookup, один физический victim. Grace160/warning70/song40/charm200, collision17–21/leap30/recovery120; LOS/retreat/world/alive/creative/spectator/body obstruction cancel. Gifts consume1 server-side, milk bucket возврат, first+15/milk10/fish8/day cap; pet4/2days, friend85. Нет chunk forcing/world scan. Ambient выбор30–90s в home radius4, night sleep weight; sync clip/start ticks. Native renderer143bones254cubes/17CSV clips, 2-joint legs/grounding32corners, eyes-only fullbright, curl pose inheritance. Client song один позиционный loop perloadedNPC с fade/cancel; контроллер имеет TTL/logout cleanup.

`tales/TaleCatalog` reloadable217 JSON entries и subject/adv indices. `TaleData` overworld SavedData UUID→sparse Entries(0..4 monotonic/unread/seen/verified/отдельныйDefeated), Bonds по NPC UUID, bounded64Hints, durable delivery; future schema сохраняется opaque. Legacy book/earned lore_book/playtime помечает delivered; новый full inventory pending без drop/dup, lost book no reissue. `TaleService` читает actual PlayerAdvancements без legacy mask award side effects; Yaga adapter только players.get, без создания/accept/complete/rewards.

Snapshot содержит real registered metadata, 487 Recipe.CODEC JSON, actual graph/player flags, privacy-filtered hints. RecipeManager weak cache/reload invalidate; open cooldown10, read4, nonce sequence/TTL/range/choice10; compact delta на discovery/award, full graph reset при revoke. Wire229697 bytes native codec PASS. Discovery: interactions/craft/pickup/smelt/containerclose/death LOWEST; local LivingEntity12b sample1Hz≤64; moved-position actual structure references≤32 через getChunkNow, без locate/loading. Exact home/private target/Anchor не отправляются: смещение25–80/radius250, stale/dimension/source/direction/band.

Dist.CLIENT: TaleScreen native font/перенос/scissor, 8icons32px, трехуровневое оглавление/search/back-forward/history/scroll, cached real entity portrait, actual recipe viewer, readonly Yaga journal, schematic map, real pan/zoom advancement graph. Все старые registry/162 criteria/rewards/vanilla IDs сохранены, тестовые harness namespaces не входят в production. Только test restart fixture имеет forced chunk для native entity activation; он изолирован и исключён из JAR. Clean build/15native server cases/actual disk restart PASS; 19servers/0clients; реальные GUI/audio/MP/MSPT/FPS не приняты. [Evidence](verification/bayun-book-final/REPORT.md).
