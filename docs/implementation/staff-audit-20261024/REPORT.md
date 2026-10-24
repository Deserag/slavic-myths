# Посохи, зачарования и регрессия оружия — 24.10.2026

Статус IN_PROGRESS / ручная приёмка владельца ожидается. Выполнены исправления и автоматические runtime-проверки по прямому разрешению запуска. PolyMC, пользовательские миры, registry IDs и версия мода не изменялись. Commit/push не выполнялись.

## Причины и баланс

У Грозового посоха и посоха Велеса не было melee attribute modifiers. Морозный также не имел их и дополнительно перехватывал удар по сущности, отменяя native melee и вызывая снаряд. Сам снаряд уже имел 7 HP; отсутствие мгновенного melee-урона нельзя было считать отсутствием всего урона посоха.

Грозовой направлял проверку конуса к ногам цели относительно глаз игрока, поэтому на близкой дистанции мог пропускать цель при наведении в корпус. Теперь учитываются пересечение луча с bounding box и направление к центру тела.

Посох | Обычный удар до → после | Скорость | Роль / неизменённые способности
--- | --- | --- | ---
Резной | 3 → 3 HP | 1,2/с | Раннее оружие; не усилен
Грозовой | 1 → 6 HP | 1,2/с | Средний боевой посох; импульс 6 HP, радиус до 7, откат 48 тиков сохранены
Велеса | 1 → 4 HP | 1,2/с | Вспомогательный артефакт призыва; питомец и cooldown не изменены; прочность 48 сохранена
Лиха | 10 → 10 HP | 1,2/с | Сильное оружие позднего босса; исцеление/морок не изменены
Морозко | native удар отменялся → 8 HP | 1,2/с | Поздняя награда: близкая цель получает melee, ЛКМ в воздух стреляет прежним снарядом 7 HP

Морозный поток: 1 → **3 HP за импульс** каждые 10 тиков, длительность 60 тиков и cooldown 200 тиков сохранены. За полную непрерывную струю возможно 6 импульсов, то есть теоретически до 18 HP до сопротивлений/прочих условий. Runtime проверяет один настоящий импульс, отпускание и cooldown; не заявляется измерение полного sustained DPS.

Управление Морозным посохом согласовано с клиентским input hook и серверным AttackEntityEvent: удар по близкой сущности больше не отменяется; воздушный выстрел сохранён. Подсказки RU/EN обновлены.

CombatStaffItem даёт единый melee/enchanting контракт без наследования sword sweep. У прочных посохов обычный удар изнашивает предмет; магические расходы сохранены. Морозный остаётся неразрушаемым, как раньше.

## Зачарования

Реальная загрузка NeoForge подтвердила следующие допуски:

Посох | Поддерживаемые vanilla enchantments
--- | ---
Грозовой | Острота, Небесная кара, Бич членистоногих, Прочность, Починка, Проклятие утраты
Велеса | То же
Лиха | То же
Морозко | Острота, Небесная кара, Бич членистоногих; Починка/Прочность не добавлялись, поскольку предмет не имеет износа

Три вида чар урона взаимоисключающие по штатным правилам vanilla. Обычный стол применяет свои стандартные treasure/random/level фильтры: Починка и проклятия не обещаются как предложение стола. Native tests проверяют supportsEnchantment и isPrimaryItemFor по реальным загруженным definitions/tags, а не конкретный случайный roll меню.

Для Грозового/Велеса/Морозного добавлены weapon/sharp_weapon tags. Для первых двух добавлен durability tag. У боевых посохов и посоха Лиха primary-item admission допускает поддерживаемые чары урона на столе, не добавляя sword-only tags. У резного посоха сохранены существующие sword tags/vanilla sword правила.

**«Сила» (эффект Strength)** усиливает native обычный удар. **«Сила» (чары Power для лука)** на посохах не поддерживается. Острота/Кара/Бич относятся к melee; они не подмешиваются тайно в урон магического импульса, потока или призванного животного. Это объяснено в подсказке.

Не добавлены чары лука/арбалета/трезубца и sword-only sweeping/looting/knockback к новым Item-посохам. Для остальных оружий правила не менялись: пользовательские типы и их overrides продолжают определять применимость. Регрессия метательных вариантов проверяет перенос уже имеющихся чар/рун, а не выдаёт произвольным оружиям новые категории зачарований.

## Проверки

REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN
--- | --- | ---
Атрибуты трёх посохов | StaffStats/CombatStaffItem; native JVM checks; server actual ServerPlayer.attack | PASS
Сила и Острота | runtime-3-pass.log: Грозовой 6/9/9, Велеса 4/7/7, Морозко 8/11/11 HP для base / Sharpness V / Strength I, без брони | PASS
Допуск зачарований | supportsEnchantment / isPrimaryItemFor, реальные loaded enchantment definitions/tags, 4 боевых посоха; Power отвергнут | PASS
Грозовой импульс | Настоящий Item.use по Slime: 6 HP, cooldown, расход прочности | PASS
Морозный поток | Настоящий use/onUseTick/releaseUsing: 3 HP, остановка и запрет обхода cooldown | PASS
Навыки/прочее оружие | Сабля: Slime, конус, стена и лимит 6; reach копья/плети; 10 primary weapons; 14 метательных вариантов | PASS — выбранная регрессия, не все навыки всей игры
Улучшения | 17 настоящих операций ArmorerMenu: сохранены имя, руна, чары | PASS
Ранее проблемные растения | Реальный BoneMealItem.growCrop, расход и сбор/повторное выращивание 5 ягод | PASS — без многодневного natural growth/relog
Внешний вид | Реальный клиент и integrated server; 36 кадров: 5 посохов и 4 результата upgrade recipes, правая/левая рука/третье лицо/creative GUI | PASS — скриптовый осмотр, не приёмка владельца
Обычный инвентарь | Дополнительный отдельный клиентский прогон, см. client-inventory/ | PASS — 9 кадров обычного survival inventory, иконки в ячейках
Clean build / production JAR | staff-build.log, resources.log, artifact.json | PASS — clean build 55s, 7688 source/JAR ресурсов совпадают, harness исключён
Полная кампания / все сочетания навыков / MP / FPS/MSPT | В scope автоматического прогона не входят | NOT_RUN
Ручная приёмка владельца | Не заменяется тестами | NOT_RUN
PolyMC install | Не поручался | NOT_INSTALLED

Первый GameTest процесс: 3 теста, 2 FAIL; выявлены причины прицеливания Грозового и перехвата melee Морозного. После исправлений второй процесс: 4/4 PASS. Третий, расширенный: **11/11 PASS**, включая всю указанную регрессию. Неудачный первый запуск сохранён, не выдан за PASS.

Клиент использовал только копии disposable VisualQA: work/staff-20261024/client и client-inventory. Скрипт сам завершает Minecraft после кадров; исходный QA-мир и PolyMC не трогались. Условие runtime=0 в старом standalone resource checker описывает только сам checker, а не эту работу.

## Файлы и воспроизведение

Production Java: combat/StaffStats, CombatStaffItem, LikhoStaffItem; item/StormStaffItem; artifact/VelesStaffItem; morozko/FrostStaff, FrostRules, FrostSkills, FrostEvents; client/FrostClient. Ресурсы: enchantable weapon/sharp_weapon/durability и ru_ru/en_us.

Harness только в tools: staff-runtime.gradle, staff-client.gradle, staff-client/StaffVisualProbe, weapon-tests/.../StaffAuditTests; расширен существующий offline harness. Scoped генератор: tools/staff_resources.py.

Команды с JAVA_HOME=C:/Program Files/Java/jdk-21.0.12 только для команды:

- gradlew.bat clean build
- gradlew.bat -I tools/feedback-offline.gradle -I tools/morozko-offline.gradle checkFeedbackOffline frostOfflineChecks
- gradlew.bat -PweaponRuntimeCheck -PstaffRun=3 -I tools/staff-runtime.gradle runWeaponChecks
- gradlew.bat -I tools/staff-client.gradle runClient
- gradlew.bat -PstaffGuiOnly -I tools/staff-client.gradle runClient
- python tools/verify_current_resources.py

Нет художественных правок существующих ModelPart/UV/PNG: сравнения новых моделей до/после не подменяются концептами. Клиентские кадры показывают фактические существующие ресурсы после изменения боевого поведения.

## Итоговые артефакты

Production JAR: D:\slavic-myths\build\libs\slavicmyths-1.9.7.61.jar

SHA256: 9b9cf3b1cd3e7176bb5694450c8683d347410c7a4a39d023b94b361a5cfaf2dc

Source commit: b85689ee11db5d0b32026b9caf9b54a46fbd9787 + сохранённые изменения workspace + текущий незакоммиченный патч. Это построенный, не установленный JAR.

Реальных процессов Minecraft: **7** — 3 GameTest-сервера и 4 клиента. Два клиентских прогона завершены с 45 кадрами; две попытки инвентарного профиля остановлены на AccessibilityOnboardingScreen до входа в мир и не объявляются PASS. Для disposable профиля экран обработан test-only probe, затем получены 9 survival inventory кадров. Integrated servers входили в два завершённых клиентских процесса, не считаются дополнительными процессами.

Native/offline: 7071 checks расширенного feedback harness и 7018 Frost checks PASS. Эти числа в основном отражают UV/geometry/policy проверки и не являются количеством игровых сценариев. Final server: 11/11 PASS; финальный клиент и ресурсы PASS. См. artifact.json и сохранённые логи.
