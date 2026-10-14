# 1.9.8.2 — баланс существ: итог для независимой проверки ChatGPT

Дата документальной сверки: **2026-10-14**, Europe/Moscow. Путь:
`D:/slavic-myths/docs/implementation/creatures-1.9.8.2/REPORT.md`.
Копия для ветки: `D:/slavic-myths-audit/docs/implementation/creatures-1.9.8.2/REPORT.md`.

Статус: **первый этап реализован и прошёл указанные offline gates; игровая
приёмка NOT_RUN**. Этот отчёт описывает код и пределы доказательств, не объявляет
механики проверенными в игре. Вторая часть1.9.8.2 не начата. При подготовке
отчёта production Java, тестовые исходники и ресурсы не менялись; сборка и тесты
повторно не запускались. Выполнены чтение файлов, Git-сверка и SHA256 JAR.

## 1. Проверяемая поставка и полный список файлов

Ветка **audit**, отдельный worktree `D:/slavic-myths-audit`; HEAD/base
`b76160800223267abc04e7c6d46812dfba8976dc`. В ней **44 staged файла**, без commit/push. Это накопленный комплект:
реализация первого этапа + принадлежащие этой работе материалы предыдущего
аудита1.9.8.1. Число44 не означает44 production-файла, изменённых только на этапе1.

Категории: **19 production Java +6 тестов/инструментов +0 ресурсов +19 документов**.
Из6 tools текущему этапу относятся `creature-health-offline.gradle`,
`CreatureHealthChecks.java`, `verify_creature_health_1982.py`; остальные3 относятся
к предшествующему аудиту. Runtime-пакеты тестами не дополнены.
**23 файла tracked и21 untracked в основном дереве** из этого списка. В audit
они представлены индексом, поэтому `git diff --cached` включает и их содержимое.
Обычный `git diff` этих21 новых файлов не показывает. `FILES.json` содержит
43 checksum-записи и отдельно описывает сам manifest: его SHA256 не включён
в собственное содержимое во избежание циклической зависимости.

### Production Java —19

| № | Файл | В audit index | В основном дереве |
|---:|---|---|---|
|1|[src/main/java/org/slavicmyths/bandit/BanditEntity.java](D:/slavic-myths/src/main/java/org/slavicmyths/bandit/BanditEntity.java)|M|tracked M|
|2|[src/main/java/org/slavicmyths/bandit/LargeCampPiece.java](D:/slavic-myths/src/main/java/org/slavicmyths/bandit/LargeCampPiece.java)|M|tracked M|
|3|[src/main/java/org/slavicmyths/bandit/NightingaleEntity.java](D:/slavic-myths/src/main/java/org/slavicmyths/bandit/NightingaleEntity.java)|M|tracked M|
|4|[src/main/java/org/slavicmyths/combat/CreatureHealthBalance.java](D:/slavic-myths/src/main/java/org/slavicmyths/combat/CreatureHealthBalance.java)|A|untracked ??|
|5|[src/main/java/org/slavicmyths/firebird/FirebirdEntity.java](D:/slavic-myths/src/main/java/org/slavicmyths/firebird/FirebirdEntity.java)|M|tracked M|
|6|[src/main/java/org/slavicmyths/gorynych/Gorynych.java](D:/slavic-myths/src/main/java/org/slavicmyths/gorynych/Gorynych.java)|M|tracked M|
|7|[src/main/java/org/slavicmyths/gorynych/GorynychLedger.java](D:/slavic-myths/src/main/java/org/slavicmyths/gorynych/GorynychLedger.java)|M|tracked M|
|8|[src/main/java/org/slavicmyths/gorynych/GorynychRules.java](D:/slavic-myths/src/main/java/org/slavicmyths/gorynych/GorynychRules.java)|M|tracked M|
|9|[src/main/java/org/slavicmyths/guardians/GuardianEntity.java](D:/slavic-myths/src/main/java/org/slavicmyths/guardians/GuardianEntity.java)|M|tracked M|
|10|[src/main/java/org/slavicmyths/guardians/GuardianLedger.java](D:/slavic-myths/src/main/java/org/slavicmyths/guardians/GuardianLedger.java)|M|tracked M|
|11|[src/main/java/org/slavicmyths/guardians/GuardianRules.java](D:/slavic-myths/src/main/java/org/slavicmyths/guardians/GuardianRules.java)|M|tracked M|
|12|[src/main/java/org/slavicmyths/hunt/BossKind.java](D:/slavic-myths/src/main/java/org/slavicmyths/hunt/BossKind.java)|M|tracked M|
|13|[src/main/java/org/slavicmyths/hunt/WorldBoss.java](D:/slavic-myths/src/main/java/org/slavicmyths/hunt/WorldBoss.java)|M|tracked M|
|14|[src/main/java/org/slavicmyths/karachun/Karachun.java](D:/slavic-myths/src/main/java/org/slavicmyths/karachun/Karachun.java)|M|tracked M|
|15|[src/main/java/org/slavicmyths/karachun/NightConfig.java](D:/slavic-myths/src/main/java/org/slavicmyths/karachun/NightConfig.java)|M|tracked M|
|16|[src/main/java/org/slavicmyths/morozko/FrostConfig.java](D:/slavic-myths/src/main/java/org/slavicmyths/morozko/FrostConfig.java)|M|tracked M|
|17|[src/main/java/org/slavicmyths/morozko/FrostRules.java](D:/slavic-myths/src/main/java/org/slavicmyths/morozko/FrostRules.java)|M|tracked M|
|18|[src/main/java/org/slavicmyths/morozko/Morozko.java](D:/slavic-myths/src/main/java/org/slavicmyths/morozko/Morozko.java)|M|tracked M|
|19|[src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java](D:/slavic-myths/src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java)|M|tracked M|

### Тесты и инструменты —6

| № | Файл | В audit index | В основном дереве |
|---:|---|---|---|
|20|[tools/check_creature_audit_tables_1981.py](D:/slavic-myths/tools/check_creature_audit_tables_1981.py)|A|untracked ??|
|21|[tools/creature-health-offline.gradle](D:/slavic-myths/tools/creature-health-offline.gradle)|A|untracked ??|
|22|[tools/creature-health-tests/CreatureHealthChecks.java](D:/slavic-myths/tools/creature-health-tests/CreatureHealthChecks.java)|A|untracked ??|
|23|[tools/finalize_creature_audit_1981.py](D:/slavic-myths/tools/finalize_creature_audit_1981.py)|A|untracked ??|
|24|[tools/finalize_creature_audit_docs_1981.py](D:/slavic-myths/tools/finalize_creature_audit_docs_1981.py)|A|untracked ??|
|25|[tools/verify_creature_health_1982.py](D:/slavic-myths/tools/verify_creature_health_1982.py)|A|untracked ??|

### Документация —19

| № | Файл | В audit index | В основном дереве |
|---:|---|---|---|
|26|[docs/ARCHITECTURE.md](D:/slavic-myths/docs/ARCHITECTURE.md)|M|tracked M|
|27|[docs/PROJECT_STATUS.md](D:/slavic-myths/docs/PROJECT_STATUS.md)|M|tracked M|
|28|[docs/ROADMAP.md](D:/slavic-myths/docs/ROADMAP.md)|M|tracked M|
|29|[docs/audit/creatures-1.9.8.1/ATTACKS.csv](D:/slavic-myths/docs/audit/creatures-1.9.8.1/ATTACKS.csv)|A|untracked ??|
|30|[docs/audit/creatures-1.9.8.1/CHECKS.md](D:/slavic-myths/docs/audit/creatures-1.9.8.1/CHECKS.md)|M|tracked M|
|31|[docs/audit/creatures-1.9.8.1/CREATURES.csv](D:/slavic-myths/docs/audit/creatures-1.9.8.1/CREATURES.csv)|A|untracked ??|
|32|[docs/audit/creatures-1.9.8.1/FORMS.md](D:/slavic-myths/docs/audit/creatures-1.9.8.1/FORMS.md)|A|untracked ??|
|33|[docs/audit/creatures-1.9.8.1/POST_IMPLEMENTATION_REQUIREMENTS.md](D:/slavic-myths/docs/audit/creatures-1.9.8.1/POST_IMPLEMENTATION_REQUIREMENTS.md)|A|untracked ??|
|34|[docs/audit/creatures-1.9.8.1/REPORT.md](D:/slavic-myths/docs/audit/creatures-1.9.8.1/REPORT.md)|M|tracked M|
|35|[docs/audit/creatures-1.9.8.1/REPORT_BEFORE_FINALIZATION.md](D:/slavic-myths/docs/audit/creatures-1.9.8.1/REPORT_BEFORE_FINALIZATION.md)|A|untracked ??|
|36|[docs/audit/creatures-1.9.8.1/TECHNICAL_RISKS.md](D:/slavic-myths/docs/audit/creatures-1.9.8.1/TECHNICAL_RISKS.md)|A|untracked ??|
|37|[docs/audit/creatures-1.9.8.1/UNVERIFIED.csv](D:/slavic-myths/docs/audit/creatures-1.9.8.1/UNVERIFIED.csv)|A|untracked ??|
|38|[docs/audit/creatures-1.9.8.1/completion-summary.json](D:/slavic-myths/docs/audit/creatures-1.9.8.1/completion-summary.json)|A|untracked ??|
|39|[docs/audit/creatures-1.9.8.1/table-checks.json](D:/slavic-myths/docs/audit/creatures-1.9.8.1/table-checks.json)|A|untracked ??|
|40|[docs/implementation/creatures-1.9.8.2/BUILD.txt](D:/slavic-myths/docs/implementation/creatures-1.9.8.2/BUILD.txt)|A|untracked ??|
|41|[docs/implementation/creatures-1.9.8.2/CHECKS.json](D:/slavic-myths/docs/implementation/creatures-1.9.8.2/CHECKS.json)|A|untracked ??|
|42|[docs/implementation/creatures-1.9.8.2/FILES.json](D:/slavic-myths/docs/implementation/creatures-1.9.8.2/FILES.json)|A|untracked ??|
|43|[docs/implementation/creatures-1.9.8.2/REPORT.md](D:/slavic-myths/docs/implementation/creatures-1.9.8.2/REPORT.md)|A|untracked ??|
|44|[docs/implementation/creatures-1.9.8.2/SPEC.md](D:/slavic-myths/docs/implementation/creatures-1.9.8.2/SPEC.md)|A|untracked ??|


Ресурсы: **0**. Текстуры, модели, blockstates, loot и локализации не менялись
этой поставкой. Папки `.tools`/build/work — локальные вспомогательные материалы,
не входят в44. Отдельно существующий untracked `tools/complete_creature_audit_1981.py`
в комплект44 **не включён**; его не следует молча добавлять через `git add .`.
При документальной сверке новых файлов сверх44 в audit не добавлено.

## 2. Реализованные HP, пороги и фазы

Все числа ниже — HP, не сердца. «Реализовано» означает значения и ветви
текущего исходника, **не измерение живой сущности в игровом мире**. Процентные
границы приведены номинально; точные операторы сравнения указаны, где существенны.

|Существо/состояние|Старый максимум → новый|Порог/фазы после изменения|Основание|
|---|---|---|---|
|Атаман|150→100|HP-фаз нет; обе точки фабрика/большой лагерь задают100|BanditEntity.attributes, LargeCampPiece|
|Тугарин Змей|350→400|PHASE_ONE→TRANSFORMING при≤200; переход60 ticks→PHASE_TWO; incoming×.15/×.5 сохранены|WorldBoss, TugarinCombat без изменения атак|
|Соловей-разбойник|250→350|трансформация при≤175,40 ticks, PHASE_TWO; saved TRANSFORMING восстанавливается как PHASE_TWO по прежнему правилу|NightingaleEntity|
|Обычное Лихо|320→350|II при≤227.5 (65%); III при≤105 (30%); saved higher phase не понижается обычным phase scheduler|BossKind, WorldBoss, BossRules|
|Моровое Лихо|640→700|тот же EntityType; mor_health ADD_MULTIPLIED_TOTAL+1; HigherMor II при≤350 (50%)|MorController/HigherMor/HigherRules без правок|
|Проклятый Оборотень cursed_volkolak|400→700|II при≤437.5 (62.5%); III при≤175 (25%); bar делит на actual max; сюжетный escape/independent различаются как прежде|CursedVolkolak; VolkolakEvents не менялся|
|Страж погасшего пламени|500→900|LOCKDOWN доступен≤675; LastEmbers≤225; несмертельное очищение≤45; PURIFY→CLEANSED, тот же ID|GuardianRules/Entity; FireGuardian не менялся|
|Осквернённая хранительница|450→800|desperation≤240; очищение≤40; PURIFY→CLEANSED, тот же ID; лечение5/echo25 не менялись|GuardianRules/Entity; SpringKeeper не менялась|
|Морозко|1000 default→1400 default|II при HP<840; III при HP<420; уступка при≤70 (в tick есть допуск .001); trial/награды прежние|FrostRules/Config/Morozko; существующий config может задать иной max|
|Карачун|1200 default→1600 default|II при≤1120 (70%); III при≤400 (25%); уступка≤80 с tick-допуском .001; custom config сохраняется|NightConfig/Karachun; NightRules без правок|
|Горыныч|3600 logical→2800 logical|PATROL0/GROUND1→TRANSFORM2 при≤1500→AIR3; EXHAUSTED4 при≤75→CLEANSING5→PURIFIED6; это absolute1500/75, не50%/5%; service MAX_HEALTH1024 сохранён|GorynychRules/Gorynych/Ledger|
|Жар-птица|3000→3000|новая защита hurt/setHealth/die/kill; Health ограничен1..3000; warning5→retaliation6 и departure7 сохранены|FirebirdEntity|

Контроль неизменённых: ordinary `volkolak`165; Калин1800; Полоз3000;
IceDouble50; PlayerDouble15; водное воплощение25. Они не включены в число
изменённых существ и не стали отдельными формами Лиха/Горыныча.
Production-версия остаётся **1.9.7**; Minecraft1.21.1, NeoForge21.1.255, Java21.
Согласованные в следующем задании уровни и коэффициенты не означают внедрения
общей шкалы урона: атаки, A1–A7, DoT и серебряные бонусы этим этапом не менялись.

## 3. Миграция сохранений: реализованный алгоритм и границы

### Обычные Attribute/Health сущности

`CreatureHealthBalance.prepare` вызывается **локально до super.readAdditionalSaveData**
в Атамане (только role ATAMAN), Соловье, WorldBoss, cursed_volkolak, хранителях и
основном Морозко. Это не глобальный Entity event и не обход мира.

1. Если `CreatureHealthBalance1982>=1`, входной NBT возвращается без пересчёта.
2. Для legacy создаётся копия; находится `attributes`/`minecraft:generic.max_health`.
   При отсутствии записи используется известная legacy база и добавляется запись.
3. Сохраняются permanent modifiers, их ID и native порядок: ADD_VALUE,
   ADD_MULTIPLIED_BASE от суммы с добавками, затем ADD_MULTIPLIED_TOTAL.
   Старый effective max ограничивается vanilla1024; новый — тем же пределом,
   кроме локального uncapped Морозко. Глобальный vanilla Attribute не изменён.
4. `newHealth=clamp(oldHealth/oldEffectiveMax×newEffectiveMax,0,newEffectiveMax)`.
   Health≤0/NaN→0 в helper. Если поля Health нет, оно не добавляется принудительно.
5. В копии меняется max-health base и пишется `CreatureHealthBalance1982=1`;
   копия передаётся vanilla до возможного clamp старого Health.
6. В `addAdditionalSaveData` целевой сущности метка сохраняется вместе с
   атрибутами/Health. При save→load пересчёт не повторяется. Повторный вызов
   на одной и той же немаркированной старой копии вычислит прежний результат
   заново из старого Health, а не умножит уже мигрированную сущность.

Номинальные примеры без modifiers: Атаман75/150→50/100; Тугарин175/350→200/400;
Соловей125/250→175/350; Лихо160/320→175/350; Оборотень200/400→350/700;
огненный250/500→450/900; водная225/450→400/800; Морозко500/1000→700/1400.
Пример с cap: legacy Морозко base1000+100 имел effective max1024; при
Health1024 новый base1400+100 даёт full1500, а не ошибочную долю от1100.

Лихо: saved base320 и **тот же единственный** `slavicmyths:mor_health`+100%
дают old640, после base350 —700. Пример wounded256/640→280/700.
MorController.hasModifier и load остались прежними; helper не создаёт второй
modifier. WorldBoss после super по-прежнему повторно назначает уже новую k.hp.
Тесты подтверждают сохранение одного modifier и его native value700, но **не
полный Entity load→MorGoal→повторный игровой relog**.

Гарантия «не воскрешать» здесь подтверждена для helper, а не для любого override
setHealth во всём моде. Горыныч/очищенные хранители/Жар-птица имеют нижеописанные
осознанные несмертельные исключения. Полные readAdditionalSaveData обычных боссов
в1545 assertions не исполнялись на сущности в мире.

### Горыныч: Entity NBT + SavedData

Общая метка **`GorynychBalance1982=1`** применяется независимо к двум источникам.
Entity использует `GorynychHP/GorynychState`, encounter ledger — `HP/State`.
`GorynychRules.migrate` масштабирует3600→2800 и ставит метку. Пример1800→1400.
Для EXHAUSTED/CLEANSING канонический HP75 независимо от прошлой доли;
для PURIFIED —2800; иные значения ограничиваются75..2800.

Мигратор не меняет State/clean mask/прочие payload поля. Новые ledger encounters
сразу получают2800 и метку. Ledger.load мигрирует legacy строки и помечает
SavedData dirty; configure также защищён тем же мигратором. Entity load готовит
копию NBT, восстанавливает entity значения, затем, если существует ledger Origin,
**configure переопределяет их ledger**. Метка уже мигрированного ledger исключает
повторное×2800/3600. Persist и entity save пишут версию. Служебное Health
проецируется как `1024×logicalHP/2800`; UI получает Rules.HP вместо literal3600.

Wings нормализуются по saved state≥AIR; stopAttacks сбрасывает головы/тело,
target/захват, active hazards; FLY/noGravity сбрасываются, цикл полёта восстанавливается
обычным контроллером. State TRANSFORM и завершённое очищение сохраняют relevant age.
Сами реальные захват, полёт, очищение/выдача наград тестами не проигрывались.

**Вопросы независимой проверки, не исправлявшиеся в отчётном проходе:** ledger
приоритет остаётся без сравнения «свежести» с Entity NBT; roundtrip не доказывает
согласованность двух файлов при crash/несовпадении checkpoints. Мигратор сохраняет
старую фазу буквально, не вычисляет её заново по новому absolute1500. Например,
GROUND с1900/3600 станет≈1477.78/2800 и останется GROUND; проверка перехода
TRANSFORM находится в hurt, поэтому немедленный переход до следующего входящего
удара не обеспечен этим мигратором. Это видимое по коду пограничное поведение,
которое нужно оценить относительно требований; игровой тест не выполнялся.

### Хранители: Entity NBT + GuardianLedger

Entity Health адаптируется helper500→900 или450→800 до super; instance перед
load сбрасывается, чтобы setHealth не начал новую purification сцену во время
восстановления. GuardianState≥PURIFY сохраняется, остальные возвращаются в IDLE
по прежнему правилу. В entity save пишется общая метка версии.

В ledger действует `GuardianRules.migrateRecord`: Health масштабируется по
legacy500/450; PURIFICATION получает45/40; CLEARED получает900/800. Иные поля
записи сохраняются. Миграция вызвана в GuardianLedger.load и configure; новые
ensure-записи и текущие Health checkpoints маркируются версией. Configure
ограничивает восстановленный Health несмертельным threshold. Старое правило
ledger load ACTIVE→READY, сброс вентилей/локальных deadlines сохранено; это не
результат нового HP-механизма. Очищение не объявлено смертью, standalone не стало
сюжетной встречей. Но настоящие claims/фрагменты/вентили/двойной checkpoint не
проверены: тесты вызывают migrateRecord на synthetic record, не GuardianLedger.load.

### Морозко и Карачун: конфигурации и собственные пределы

Морозко имеет только species-local uncapped getMaxHealth/getAttributeValue:
используются base и modifiers **существующего** MAX_HEALTH с прежним sync;
нового сетевого пакета/accessor нет. IceDouble исключён через instanceof.
Default config1400 (разрешён100..10000), old saved base1000 мигрирует к1400;
иные saved базы сохраняются. Старые TOML не переписывались. Следовательно
существующий config maxHealth=1000 по-прежнему может дать новому totem spawn1000,
в то время как немаркированный старый saved base1000 мигрирует к1400. Это
неоднозначность политики старых config, **не подтверждение1400 для любой установки**.

Карачун сохраняет `NightMax` и общую метку. Legacy NightMax1200→текущее
`max(100,NightConfig.health)`; явный иной saved max сохраняется. Health масштабируется
до vanilla load; session временно null, чтобы старый setHealth trial-floor не
подменил Health во время загрузки. Новый default1600, но старый TOML health1200
не переписывается, поэтому может остаться1200. Нельзя определить, был ли1200
старым default или намеренной настройкой. Повторный marked load сохраняет NightMax.

И Морозко, и Карачун **по прежней логике отменяют незавершённое испытание при load**;
этот этап не ввёл возобновление боя после перезахода. Completed records/reward logic
не менялись. Исполнение server-config веток Карачуна и полных load сущностей
не покрыто1545 assertions; у Карачуна проверяется лишь config default.

## 4. Неубиваемость Жар-птицы и ответная реакция

- Максимум3000 остался в override. `hurt` отклоняет client/неfinite/amount≤0;
  на сервере для положительного finite входа **вообще не вызывает super.hurt**
  и всегда возвращаетfalse: предотвращён весь обычный HP loss, не только lethal.
- Независимо от результата урона, hurt cue имеет прежний cooldown12. Живой
  LivingEntity aggressor, кроме creative/spectator player, при cooldown0 и
  state≠5/6/7 записывается в aggressor; запускаются state5 и warning cue.
- Прежний tick state5 age≥11→state6; в state6 age8, если aggressor жив,
  в≤3 блоках и LOS, наносится прежний retaliation20. На age≥17 возврат state2
  и cooldown60. Эти tick ветви не перерабатывались.
- `setHealth` ограничивает конечный hp диапазоном1..3000; для NaN/Infinity
  используется3000. Положительный внешний setter может оставить1/1500, поэтому
  «неубиваемость» не означает «Health всегда ровно3000». Health0 не становится смертью.
- `die(DamageSource)` и `kill()` пусты; путь обычной смерти/loot/close удалён.
  Штатный state7 depart→encounter.close→discard сохранён. Прямой административный
  Entity.remove/discard не блокируется: сценарное удаление отличается от смерти.
- Save/load сохраняет Visit/Anchor/state/age; штатный load нормализует state
  к departure7/perch1/flight2 как прежде, а Health проходит через защищённый setter.
  Это не восстановление незавершённой retaliation сцены.

Статически обычные игроки/мобы, fire/DoT/explosion/fall/void, идущие через hurt,
не смогут отнять HP. **Реальные DamageTypes/NeoForge events/эффекты в игровом мире
не прогонялись**. Fixture не исполняет реакцию на живого атакующего, sound,
tick state5/6, encounter.close, loot manager или настоящий NBT load. Возвратfalse
может влиять на сторонние hit/proc subscribers — это отдельный runtime остаток,
а не доказанная неизменность поведения всех оружейных/рунических комбинаций.

## 5. Что именно означают1545 PASS

Источник: `tools/creature-health-tests/CreatureHealthChecks.java`,87 строк.
Это **1545 вызовов assertion**, не1545 отдельных сценариев и не1545 игровых тестов.
Сумма ниже восстановлена по количеству итераций/вызовов check/near в сохранённом
исходнике; тесты в этом документальном проходе повторно не запускались.

|Строки harness|Проверяемое|Assertions|Характер и предел|
|---|---|---:|---|
|50–56|8 пар HP×8 долей×5 проверок: prepare/new base/input immutable/payload/idempotence|320|выполняется helper+native NBT, не Entity load|
|57–62|4 доли морового Лиха×4 проверки, один modifier/native value700|16|реальные AttributeInstance/NBT, не MorController.load и не MorGoal|
|63–65|vanilla cap1024, uncapped1400, legacy cap с+100, native ordering1980|4|поведение атрибутов/helper без мира|
|66|фабрики Ataman/Tugarin/Likho/Nightingale/Cursed дают требуемый base|5|фиксированные factory values, не spawn/camp|
|67|700×.625/.25 и Frost HP/Double HP|4|статические числа; две «phase» проверки — арифметика, не вызов phase-кода Оборотня|
|68|guardian factories900/800, thresholds45/40, HEAL5/ECHO25|6|фиксированные значения/простой threshold, не сцена очищения|
|69–72|2 хранителя×4 Stage×4 assertions migrateRecord/payload/idempotence|32|pure record migration; ClaimReceipt — synthetic строка, не реальная выдача фрагмента|
|73|Gorynych HP2800/1500/75/service1024|4|фиксированные правила/фабрика|
|74–78|7 states×8 clean masks×4 HP×5 assertions|1120|224 synthetic комбинации, Entity/ledger key policy, state/payload/idempotence; не224 боя/полёта|
|79|новый ledger2800 и начальная версия|2|реальный объект SavedData без world storage|
|80|GorynychLedger.load/save/load: HP1400, State/Clean, идемпотентность|3|native SavedData roundtrip, не файловый restart/relog|
|81|4 HP×4 damage: GorynychRules.remaining не ниже75|16|pure overkill policy, не Gorynych.hurt/armor/event pipeline|
|82|Health0 и NaN→0 в proportional|2|helper, не все override setHealth/load мода|
|25–38, вызов83|7 setter cases +environmental hurt +combined die/kill|9|настоящие методы Firebird на Unsafe-created объекте и неполном Level/SynchedData stub; нет server thread/world ticks|
|38|Firebird getMaxHealth3000|1|фиксированное значение|
|84|NightConfig health.getDefault1600|1|config default; не TOML/config reload/custom NightMax migration|
|**Итого**||**1545**|Assertions, не отдельные игровые сценарии|

Разделение: **1515 assertions поведения helpers/NBT/атрибутов +9 ограниченного
поведения объекта Жар-птицы =1524**; **21 assertion фиксированных значений/арифметики**.
Игровых/AI/полноценных интеграционных сценариев среди них **0**.
Synthetic `RewardReceipt` и `ClaimReceipt` доказывают только сохранение неизвестных
payload полей мигратором; они не доказывают сохранность реальных персональных наград.
1120 assertions большого matrix-теста доминируют в счёте и не должны создавать
впечатление широкого runtime покрытия.

Отдельно `verify_creature_health_1982.py`: исторические **13459 PASS**,6718 resources —
source string contracts/неизменность некоторых файлов, ZIP CRC/metadata/test exclusion,
наличие ресурсов и byte parity assets/data, JSON syntax. Они **не входят в1545**,
не проверяют actual entity spawn, runtime codecs, attack reachability или принятие
клиентом MAX_HEALTH>1024. Сохранность warning/retaliation там проверяется наличием
соответствующих ветвей текста, а не исполнением атакующего/таймера.

## 6. Параллельные оружие, броня и руны

База ветки —`b76160800223267abc04e7c6d46812dfba8976dc` (`b7616080`, опубликованная ревизия брони поверх прежнего
патча оружия). В audit staged diff нет `client/`, `item/`, `rpg/`, `industry/`,
`rewards/`, `src/main/resources/`, NightCombat/NightEvents или gradle.properties.
По этим защищённым путям в audit нет и untracked дополнений.19 production файлов
сверены по SHA256 с manifest предыдущей поставки и основной копией: совпадают.
Поэтому **собственный source/art diff не затрагивает параллельные оружие/броню/руны**.
Это подтверждение границ файлов, не runtime совместимости их эффектов.

Основное дерево — другая поверхность: на14 октября в нём есть новые параллельные
изменения вне собственных19 production файлов. Они не откатывались, не stage-ились
в audit и не были приписаны этой сборке. Снимок дополнительных production путей:

```text
src/main/java/org/slavicmyths/client/YagaScreen.java
src/main/java/org/slavicmyths/equipment/mixin/WardBlockMixin.java
src/main/java/org/slavicmyths/gorynych/GorynychRewards.java
src/main/java/org/slavicmyths/industry/IndustryAccessories.java
src/main/java/org/slavicmyths/item/ItemState.java
src/main/java/org/slavicmyths/morozko/FrostCold.java
src/main/java/org/slavicmyths/morozko/FrostEvents.java
src/main/java/org/slavicmyths/poloz/PolozRewardEvents.java
src/main/java/org/slavicmyths/rewards/RewardEvents.java
src/main/java/org/slavicmyths/rpg/RareRuneRuntime.java
src/main/java/org/slavicmyths/rpg/RpgEvents.java
src/main/java/org/slavicmyths/rpg/RuneDefinition.java
src/main/java/org/slavicmyths/rpg/RuneEffects.java
src/main/java/org/slavicmyths/rpg/RuneRework.java
src/main/java/org/slavicmyths/rpg/Runes.java
src/main/java/org/slavicmyths/yaga/YagaMenu.java
src/main/java/org/slavicmyths/yaga/YagaServices.java
src/main/resources/assets/slavicmyths/lang/en_us.json
src/main/resources/assets/slavicmyths/lang/ru_ru.json
src/main/resources/data/slavicmyths/tags/entity_type/rune_death_immune.json
```

Проверенный JAR относится к прошлой сборке этапа, а не автоматически к последним
параллельным исходникам в основном дереве. Нельзя заявлять их принятие на основании
старых PASS. Единственный root-untracked production файл своего комплекта —
CreatureHealthBalance.java; его содержимое включено в audit staged index.

## 7. Git, сборка, проверки и SHA256

Во время этой сверки build/GameTest/Minecraft не запускались. Историческая команда:

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
.\gradlew.bat clean build -I tools/creature-health-offline.gradle creatureHealthOfflineChecks
```

Последний сохранённый результат: **BUILD SUCCESSFUL in2m58s**,7 tasks executed;
`CREATURE_HEALTH_OFFLINE_PASS checks=1545`. `BUILD.txt` — сохранённая выдержка
логов; полный прежний журнал в `.tools/creature-health1982-final-build.log`.
`test NO-SOURCE` стандартного Gradle test не заменяет отдельно запущенный native suite.
В ранних попытках suite падал из-за offline NeoForge attributes/bootstrap и
constructor-defined SynchedData; стенд исправлен, затем PASS. После отдельного
исправления legacy cap Морозко выполнен финальный указанный clean build.
Исторический `verify_resources.py` имел известный FAIL и здесь не запускался,
baseline не переписывался; актуальный limited validator был PASS.

Production JAR: `D:/slavic-myths/build/libs/slavicmyths-1.9.7.jar`.
SHA256, записанный при gates:

```text
8a66c7b51dbc377c7ee331d9dc8598e7dca42f0adb66727c59621a8fc73bd2c5
```

SHA256 файла, заново прочитанный при подготовке отчёта:

```text
8a66c7b51dbc377c7ee331d9dc8598e7dca42f0adb66727c59621a8fc73bd2c5
```

Совпадение с проверенным артефактом: **ДА**.
Сам JAR этим документальным проходом не изменялся/не устанавливался.

Для полного44-file diff используется:

```powershell
git -C D:/slavic-myths-audit diff --cached --stat
git -C D:/slavic-myths-audit diff --cached --check
git -C D:/slavic-myths-audit diff --cached --name-status
```

`git diff --cached --check`: exit0, PASS, stdout пуст.
`git diff --check` основного дерева: exit0, ошибок whitespace нет.
В root встречаются предупреждения Git о будущей LF→CRLF нормализации; это не
результат компиляции и не самостоятельный whitespace FAIL. `git diff` audit
без --cached пуст: staged index содержит новые файлы, рабочая копия с ним совпадает.

Полный staged diff --stat после документального уточнения (самосогласованный снимок):

```text
 docs/ARCHITECTURE.md                               |   45 +
 docs/PROJECT_STATUS.md                             |   45 +
 docs/ROADMAP.md                                    |   45 +
 docs/audit/creatures-1.9.8.1/ATTACKS.csv           |  266 +++++
 docs/audit/creatures-1.9.8.1/CHECKS.md             |   22 +
 docs/audit/creatures-1.9.8.1/CREATURES.csv         |  112 ++
 docs/audit/creatures-1.9.8.1/FORMS.md              |  155 +++
 .../POST_IMPLEMENTATION_REQUIREMENTS.md            |   56 +
 docs/audit/creatures-1.9.8.1/REPORT.md             |  172 ++-
 .../REPORT_BEFORE_FINALIZATION.md                  |  183 ++++
 docs/audit/creatures-1.9.8.1/TECHNICAL_RISKS.md    |  213 ++++
 docs/audit/creatures-1.9.8.1/UNVERIFIED.csv        | 1099 ++++++++++++++++++++
 .../creatures-1.9.8.1/completion-summary.json      |   11 +
 docs/audit/creatures-1.9.8.1/table-checks.json     |   19 +
 docs/implementation/creatures-1.9.8.2/BUILD.txt    |   27 +
 docs/implementation/creatures-1.9.8.2/CHECKS.json  |   18 +
 docs/implementation/creatures-1.9.8.2/FILES.json   |  271 +++++
 docs/implementation/creatures-1.9.8.2/REPORT.md    |  547 ++++++++++
 docs/implementation/creatures-1.9.8.2/SPEC.md      |  280 +++++
 .../java/org/slavicmyths/bandit/BanditEntity.java  |    6 +-
 .../org/slavicmyths/bandit/LargeCampPiece.java     |    2 +-
 .../org/slavicmyths/bandit/NightingaleEntity.java  |    6 +-
 .../slavicmyths/combat/CreatureHealthBalance.java  |   67 ++
 .../org/slavicmyths/firebird/FirebirdEntity.java   |   12 +-
 .../java/org/slavicmyths/gorynych/Gorynych.java    |   10 +-
 .../org/slavicmyths/gorynych/GorynychLedger.java   |    4 +-
 .../org/slavicmyths/gorynych/GorynychRules.java    |   12 +-
 .../org/slavicmyths/guardians/GuardianEntity.java  |    8 +-
 .../org/slavicmyths/guardians/GuardianLedger.java  |    4 +-
 .../org/slavicmyths/guardians/GuardianRules.java   |   10 +-
 src/main/java/org/slavicmyths/hunt/BossKind.java   |    2 +-
 src/main/java/org/slavicmyths/hunt/WorldBoss.java  |    4 +-
 .../java/org/slavicmyths/karachun/Karachun.java    |   16 +-
 .../java/org/slavicmyths/karachun/NightConfig.java |    2 +-
 .../java/org/slavicmyths/morozko/FrostConfig.java  |    2 +-
 .../java/org/slavicmyths/morozko/FrostRules.java   |    2 +-
 src/main/java/org/slavicmyths/morozko/Morozko.java |    7 +-
 .../org/slavicmyths/volkolak/CursedVolkolak.java   |   10 +-
 tools/check_creature_audit_tables_1981.py          |   57 +
 tools/creature-health-offline.gradle               |    5 +
 .../CreatureHealthChecks.java                      |   87 ++
 tools/finalize_creature_audit_1981.py              |  122 +++
 tools/finalize_creature_audit_docs_1981.py         |  226 ++++
 tools/verify_creature_health_1982.py               |   45 +
 44 files changed, 4216 insertions(+), 98 deletions(-)
```

Для сравнения: обычный `git diff --stat` основного дерева **до** этого уточнения
REPORT.md ниже. Это другой, неполный для untracked и смешанный с параллельными
изменениями снимок; его нельзя называть собственными44 файлами:

```text
 docs/ARCHITECTURE.md                               |  45 ++++++
 docs/PROJECT_STATUS.md                             |  45 ++++++
 docs/ROADMAP.md                                    |  45 ++++++
 docs/audit/creatures-1.9.8.1/CHECKS.md             |  22 +++
 docs/audit/creatures-1.9.8.1/REPORT.md             | 172 ++++++++++++++-------
 .../java/org/slavicmyths/bandit/BanditEntity.java  |   6 +-
 .../org/slavicmyths/bandit/LargeCampPiece.java     |   2 +-
 .../org/slavicmyths/bandit/NightingaleEntity.java  |   6 +-
 .../java/org/slavicmyths/client/YagaScreen.java    |   6 +-
 .../equipment/mixin/WardBlockMixin.java            |   2 +-
 .../org/slavicmyths/firebird/FirebirdEntity.java   |  12 +-
 .../java/org/slavicmyths/gorynych/Gorynych.java    |  10 +-
 .../org/slavicmyths/gorynych/GorynychLedger.java   |   4 +-
 .../org/slavicmyths/gorynych/GorynychRewards.java  |   2 +-
 .../org/slavicmyths/gorynych/GorynychRules.java    |  12 +-
 .../org/slavicmyths/guardians/GuardianEntity.java  |   8 +-
 .../org/slavicmyths/guardians/GuardianLedger.java  |   4 +-
 .../org/slavicmyths/guardians/GuardianRules.java   |  10 +-
 src/main/java/org/slavicmyths/hunt/BossKind.java   |   2 +-
 src/main/java/org/slavicmyths/hunt/WorldBoss.java  |   4 +-
 .../slavicmyths/industry/IndustryAccessories.java  |   2 +-
 src/main/java/org/slavicmyths/item/ItemState.java  |   4 +-
 .../java/org/slavicmyths/karachun/Karachun.java    |  16 +-
 .../java/org/slavicmyths/karachun/NightConfig.java |   2 +-
 .../java/org/slavicmyths/morozko/FrostCold.java    |   2 +-
 .../java/org/slavicmyths/morozko/FrostConfig.java  |   2 +-
 .../java/org/slavicmyths/morozko/FrostEvents.java  |   4 +-
 .../java/org/slavicmyths/morozko/FrostRules.java   |   2 +-
 src/main/java/org/slavicmyths/morozko/Morozko.java |   7 +-
 .../org/slavicmyths/poloz/PolozRewardEvents.java   |   3 +-
 .../java/org/slavicmyths/rewards/RewardEvents.java |   4 +-
 .../java/org/slavicmyths/rpg/RareRuneRuntime.java  |   2 +-
 src/main/java/org/slavicmyths/rpg/RpgEvents.java   |   4 +-
 .../java/org/slavicmyths/rpg/RuneDefinition.java   |   9 +-
 src/main/java/org/slavicmyths/rpg/RuneEffects.java |   4 +-
 src/main/java/org/slavicmyths/rpg/RuneRework.java  |   4 +-
 src/main/java/org/slavicmyths/rpg/Runes.java       |  14 +-
 .../org/slavicmyths/volkolak/CursedVolkolak.java   |  10 +-
 src/main/java/org/slavicmyths/yaga/YagaMenu.java   |   7 +-
 .../java/org/slavicmyths/yaga/YagaServices.java    |   3 +-
 .../resources/assets/slavicmyths/lang/en_us.json   |  19 ++-
 .../resources/assets/slavicmyths/lang/ru_ru.json   |  19 ++-
 .../tags/entity_type/rune_death_immune.json        |  20 ++-
 43 files changed, 447 insertions(+), 135 deletions(-)
```

## 8. Остаточные ограничения и вопросы проверки

1. **Реальных запусков client/server/GameTest/Minecraft —0** в этом этапе и при
   подготовке отчёта. Нет настоящего old/new world restart/relog, two-client MP,
   seed/spawn QA, длительного боя, audio/visual/UI приёмки, MSPT/FPS измерений.
2. Не вызваны полные entity load/spawn/генерация лагеря на мире: Атаман camp100,
   Тугарин400 после load, Лихо350/700 после реального MorController.load,
   фазы/escape Оборотня и реальная bossbar проверены лишь исходниками и helpers.
3. Карачун custom config/NightMax миграция не исполнялась в suite. Старые config
   могут сохранить1200/1000; у Морозко старый config/new spawn и legacy entity
   migration могут дать разные максимумы. Политику этого случая нужно проверить.
4. Передача uncapped1400 Морозко клиенту через attribute sync, внешние modifiers
   и сторонние максимумы/эффекты не проверены в мире. IceDouble exclusion виден
   в коде; static HP50 не является интеграционным тестом клона.
5. Горыныч: приоритет ledger без версии свежести, порядок сохранения/чтения entity
   и SavedData, crash recovery, Standalone/story различия, головы, actual logical
   projection, загрузка AIR/EXHAUSTED/CLEANSING/PURIFIED, сброс захвата и очистка
   hazards требуют integration test. Непосредственное продвижение GROUND ниже1500
   после миграции до следующего hurt не реализовано мигратором — см. раздел3.
6. Хранители: actual GuardianLedger.load/configure и два checkpoint, ACTIVE→READY,
   scene replay,5% setHealth interception, вентили, no-death/no-double-reward,
   restored CLEANSED и персональные receipts не воспроизводились в игре.
7. Жар-птица: DamageTypes void/fire/explosion/DoT, настоящие entity die/kill/load,
   предупреждение, retaliation20 с LOS/range/creative guards, sounds/cooldown,
   отсутствие loot и штатный depart/close не прогонялись в игровом мире.
   Stub проверяет малый набор методов с отключённым воспроизведением звука.
8. Отдельно оценить влияние returnfalse из Firebird.hurt на rune/weapon proc
   subscribers. Файлы этих систем не менялись, но это не тест их взаимодействия
   с новым иммунитетом. Прямой admin discard сознательно остаётся возможным.
9. Общий helper не предназначен для произвольных datapack схем/испорченных либо
   будущих NBT. Сторонние permanent/effect modifiers, missing fields, dead entities
   и повторный load на **том же уже активном объекте** с собственным setHealth
   floor не покрыты полноценной интеграцией. Helper Health0→0 — более узкая гарантия.
10. Прежние auditor таблицы1.9.8.1 — исторический анализ с PROPOSED/UNVERIFIED,
    не обновлённый runtime dump после изменений HP. Исторические test suites с
    жёсткими old HP и полные regression suites в этом этапе не заявлены PASS;
    проверенный список ограничен сохранёнными CHECKS/BUILD.

На уровне сохранённых compile/offline/resource gates FAIL в итоговом прогоне нет.
Это **не** отсутствие возможных runtime дефектов и не разрешение начать следующий
этап. Перечисленные пограничные условия передаются независимому проверяющему,
код в этом отчётном проходе не исправлялся.

## 9. Передача

Для анализа предоставлены REPORT.md, FILES.json, CHECKS.json, BUILD.txt, SPEC.md
и полные44 файла в staged index audit (включая root-untracked). Manifest обновлён
только для изменившейся документации; source/tools checksums сохранены.
Commit/push **не выполнялись**, PolyMC **не обновлялся**, Minecraft **не запускался**.
Вторая часть1.9.8.2 **не начата**. Результат передаётся на независимую проверку.
