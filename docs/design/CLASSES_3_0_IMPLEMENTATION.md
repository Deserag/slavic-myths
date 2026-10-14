# Classes 3.0 — реализация, миграция, источники XP и приёмка

Статус: `SPEC_ONLY`. Согласованная концепция: [CLASSES_3_0_DECISIONS.md](CLASSES_3_0_DECISIONS.md); способности/испытания: [CLASSES_3_0_BALANCE_AND_TRIALS.md](CLASSES_3_0_BALANCE_AND_TRIALS.md). База 1.9.7.3 / MC1.21.1 / NeoForge21.1.255 / Java21. Работа против **актуального кода рабочей ветки**, а не исторических документов.

## До правок: обязательная диагностика

- Прочитать `src/main/java/org/slavicmyths/rpg/classes/{ClassDefinitions,ClassState,ClassLearningRules,ClassBalance,ClassRuntime,ClassEvents,ClassControl,ClassNetwork}.java`, `client/{ClassClient,ClassScreen,ClassTreeLayout}.java`, `rpg/{RpgNetwork,PathData,Runes}.java`.
- Сохранение сейчас: `SlavicPaths.Class2` в persisted player NBT; `Base`, `First`, `Second`, `Ranks`, `Active0..2`, `Passive`, `Cooldowns`, `Level`, `Xp`, `Points`; миграция `LEGACY` и чистка временных эффектов на login/respawn.
- По исходникам `ClassState.awardXp` используется через операторскую команду `/smclass ... xp`; production-источников survival Class XP пока не найдено. Проверить поиском по **всему новому HEAD**, не ограничиваться одним файлом.
- `ClassDefinitions`: I=1, II=10, III=25, max50, +1 point/level, стоимость `xpForLevel(level)=100+50*(level-1)`. Для IV при 40-м уровне сумма до него ≈40 950 Class XP. Баланс начисления соотнести с этой кривой на 15–30 часах реального Survival как **целевой критерий для последующего измерения**, а не гарантированный факт.
- У Дружинника 2 пассивных, Ведуна 3, Разбойника 3; `ClassState.passive(p,id)` учитывает только `Passive`. Новый второй слот должен проходить через ВСЕ серверные проверки, бонусы, UI и обновления атрибутов.
- Базовые клавиши Classes G/H/J, открытие K. `FrostClient.ICICLES` уже использует G; новый ultimate key регистрировать с проверкой всех существующих назначений, делать переназначаемым и предупреждать конфликт. Не ломать предыдущие M/G из других модов.
- Старые документы `docs/CLASS_SYSTEM.md` и `docs/CLASSES_1.3.1.md` исторические, не заменяют текущую кодовую базу.

## Этап A — полноценный Class XP в Survival

1. Отдельный server-side `ClassXpEvents` / `ClassXpService` с небольшим набором **реально подключенных** событий; вызов `ClassState.awardXp` только после server validation. Не менять vanilla xp/levels и существующие XP от рун/зачарований.
2. Общие основные источники XP для всех трёх классов (чтобы маг/поддержка не был вынужден убивать тысячи мобов):
   - убийство **настоящего** враждебного моба игроком: ориентир 8–16 Class XP; элитных 30–60; boss/значимое испытание 350–800 (с реальной проверкой участия);
   - первое прохождение уже зарегистрированной истории/Knowledge/advancement/святыни: 150–600 Class XP **один раз** за уникальное событие;
   - реальные открытия структур, ранее не посещённых игроком: 120–250, не каждое перемещение по чанкам, без каждый-тик сканирования.
3. Ролевые вторичные источники: Дружинник — подтверждённый блок/опасный бой; Ведун — реальные успешные обереги/очищения/ритуалы/лечение; Разбойник — меткие выстрелы с дистанции/успешное уклонение/скрытная атака. Ориентир 2–8 XP за действие, ограничить частоту, повтор одной цели/рецепта/комбинации. Не награждать за само нажатие клавиши, не начислять за бесконечно создаваемые дешёвые объекты.
4. Обязательна борьба с обходами: friendly/PvP/самоурон/манекены/Creative/Spectator/командная имитация/повтор события, farm spawners/тот же target. Ограниченный rate cap на ролевые действия и источник, не на нормальные сюжетные награды. Одиночные бой и PvE остаются жизнеспособны.
5. Добавить `AwardSource`/dedupe key, ограниченную ledger-метку одноразовых наград, persistent player state и диагностическую команду для QA. Синхронизировать UI сразу после реального начисления, простое всплытие +XP с редкой группировкой уведомлений.
6. Два тестовых профиля для каждого базового класса (боевой и мирный/исследовательский). Рассчитать фактические часы достижения 10/25/40, откалибровать цифры и справедливость без превращения XP в AFK farm.

## Этап B — второй пассивный слот + 6 пассивок

1. Новое persisted поле `Passive2` в `Class2` **в дополнение** к неизменённому `Passive`. Идентификатор первого слота сохраняется для миграции и сторонних ссылок, не перенумеровывать массово NBT.
2. `ClassState.passive(p,id)` должен возвращать ранг только если id находится в одном из активных разблокированных пассивных слотов (без дубля). До выбранного `Second` слот2 недоступен; после удаления/изменения ветви `Passive2` безопасно очищается/не применяется без потери выученных рангов.
3. `equip`, `ClassNetwork.Intent`, `ClassScreen`, `ClassClient`, `ClassState.restore`, render HUD, tooltip, классовые проверки/модификаторы и controls адаптировать к двум слотам. `Passive` не должен исчезать при релоге или переключении.
4. 6 новых пассивок в `ClassDefinitions` и **реальные event hooks** `ClassEvents` / `ClassBalance`: `battle_discipline`, `iron_will`, `spiritual_fortitude`, `sign_mastery`, `ambush_master`, `survival_instinct`. Учитывать ранги 1–3, не применять их без экипировки.
5. `iron_will`: использовать уже подтверждённый `LivingShieldBlockEvent` / native damage container, а не сырую animation; `spiritual_fortitude` применять на реально добавленном эффекте без изменения protected/story; `ambush_master` использовать уникальную возможность первого hit и one-target safeguard; `survival_instinct` только после входящего реального damage.
6. Миграция из v2 в v3 идемпотентна. Запретить запись пассивок чужого класса; конфликт дублей; сервер синхронизирует изменения, клиент никогда не является источником чисел.

## Этап C — 12 высших классов, 12 уникальных IV умений

1. Добавить `THIRD_LEVEL=40`, `Third` в persisted классовые данные и 12 цепочек `III→IV`. Существующие `First`/`Second` и их 18 ID сохраняются; IV выбран **единственно** по `Second`, а не произвольно. Для старых сохранений поле `Third` пустое, ничего автоматически не выдавать.
2. Новые 12 IV registry IDs и 12 skill IDs с Kind `ULTIMATE` (либо отдельный зарегистрированный immutable UltimateDef); ранг один, отдельный cooldown. **Не** добавлять их в три обычных `Active0..2` и не подменять старые EVOLUTION.
3. Реальные серверные обработчики эффектов — индивидуальная механика каждого, см. таблицу. Для защитных зон ограничение количества игроков/сущностей, bounded area; для автономных областей обязательная серверная очистка; для ударов владельцем damage source и lossless team filter; никаких `setHealth` как обхода событий без веской причины.
4. Высший слот `Ultimate`/автоматическое назначение по выбранному IV, отдельная регистрируемая и **переназначаемая** клавиша; показывать cooldown, failure reason, target restrictions. Требуются RU/EN имена, lore, 12 различимых пиксельных эмблем, HUD и доступность на разных GUI scales.
5. `ClassLearningRules` и `ClassState.advance` расширить явным enum/проверкой ступени, а не конструкцией `b.level()==FIRST_LEVEL?"First":"Second"`, которая для level40 запишет в Second по ошибке.
6. Сервер/клиент имеют обратную совместимость по network payload schema; при необходимости bump payload version, и защищённая ошибка несовместимого клиента, не silently wrong state.

## Этап D — испытания и UI

1. Реальный серверный `ClassTrialService`: хранит `TrialProgress.<Second>`, `TrialCompleted.<Second>`, timestamps/dedupe-данные в player persisted store. **Не** логировать каждое движение/тик; слушать существующие ограниченные боевые/помогающие события, использовать фактические damage, heal, shield, success.
2. Все 12 испытаний реализовать по [матрице](CLASSES_3_0_BALANCE_AND_TRIALS.md). Прогресс до 40-го не даётся, даже если игрок выполнял аналогичные действия ранее; после 40-го выполняется на выбранной III ветви. Нет условия «иметь открытую IV способность», которое создаст цикл. Не требовать мультиплеер.
3. ClassScreen K показывает текущее дерево, карточку IV с испытанием, прогресс и кнопку завершения; известные старые карточки II/III не переписывать. Дополнить Книгу сказаний/имеющийся журнал только если уже есть подходящий API; не изобретать новую систему квестов.
4. Сообщения: «Нужен уровень 40», «Сначала выбери III», «Испытание: X/Y», «Умение недоступно: cooldown/цель/щит/здоровье» и т. д. Локализация RU/EN, не строковые ключи в игре.

## Этап E — тестирование и независимая приёмка

- Native automated tests: все 3 класса, 18 существующих II/III ветвей, 12 IV маппингов, 12 trial completions, 6 passives×3 ранга×0/1/2 equip slot, XP sources/duplication, no-gameplay-without-class.
- Проверка старых сохранений: `Class2` v2→v3, старый `Passive`, Lv1/25/50, migration из legacy, logout/relogin/restart, смерти/clone, смена измерения, перезарядки offline и повторный вход; no data loss.
- Боевое тестирование: одиночная/групповая цель, броня, иммунитеты, boss PvE, PvP ally/hostile, тест пересечения с 54 рунами, оружием/морозным посохом, friendly NPC, 2 клиента; targeted effects не бьют союзников.
- UI: K, G/H/J, новый ultimate key, конфликт с Frost G, HUD F1/scale1..4/узкое окно, tooltips, звуки/частицы и отсутствие утечек эффектов/сущностей.
- Perf: измеренный MSPT/FPS с 2 игроками и большим боем; bounded tick/entity operations.
- `gradlew.bat clean build` с Java21, все доступные class GameTest/offline/resource/JAR gates, JSON/CSV доказательства, таблица before/after 12 способностей и reachability Class XP.
- Не считать clean build доказательством визуальной приёмки; клиент и выделенный сервер запускать только в отдельных QA мирах и по актуальным разрешениям пользователя. PolyMC Testing/миры **не заменять** без отдельного разрешения.

## План разделённых PR для Codex

A. XP progression and anti-abuse (без нового контента).
B. Two-passive-slot/migration and 6 passives (без IV).
C. IV model/abilities, effects and tests.
D. 12 trials, GUI, icon pack, RU/EN, documentation.
E. Integration + final real gameplay acceptance/packaging.

После каждого PR: честные доказательства, коммиты и ссылки, затем тестирование и только после приёмки merge. Никаких массовых перезаписей соседних веток, изменений версии `gradle.properties` или установки PolyMC без явной задачи.

## Проверяемые исходники

- `src/main/java/org/slavicmyths/rpg/classes/*.java`
- `src/main/java/org/slavicmyths/client/ClassClient.java`
- `src/main/java/org/slavicmyths/client/ClassScreen.java`
- `src/main/java/org/slavicmyths/rpg/RpgNetwork.java`
- `src/main/java/org/slavicmyths/rpg/PathData.java`
- `src/main/java/org/slavicmyths/rpg/Runes.java`
- `tools/class-tests/org/slavicmyths/verify/ClassGameTests.java`
- `docs/MANUAL_QA_CLASSES_1.3.1.md` (исторический чек-лист, адаптировать к актуальному состоянию)

Дизайн не изменяет production-код. Если выбранные имена навыков и численные параметры конфликтуют с текущим runtime, исполнитель **перед изменением** документирует root cause, сравнение и минимальную альтернативу.
