"""Documentation-only finalization, preserving the prior report as history."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/audit/creatures-1.9.8.1'
report=OUT/'REPORT.md';history=OUT/'REPORT_BEFORE_FINALIZATION.md'
if 'финальный аналитический аудит' in report.read_text(encoding='utf-8'):
    raise SystemExit('Documentation already finalized; edit reviewed sections explicitly.')
if not history.exists():history.write_text(report.read_text(encoding='utf-8'),encoding='utf-8')
report.write_text('''# Slavic Myths 1.9.8.1 — финальный аналитический аудит

Статус **COMPLETE_WITH_LIMITATIONS**, 2026-10-13. Классифицированы все **69/69**
самостоятельных существ исходного инвентаря. Сохранены 111 EntityType-строк:
69 существ + 42 auxiliary. Обработаны все **262 известные строки боевых действий**;
добавлены 3 явные строки исключённых недостижимых веток, итого **265**.
Это завершение анализа существующего инвентаря, а не доказательство исчерпывающего
runtime покрытия всех событий. Наличие обработчика не доказательство игрового вызова.
Перечень остаточных неопределённостей открыт и не препятствует завершению анализа.

## Готовые материалы

- [CREATURES.csv](CREATURES.csv): уровень, несколько семантических типов, текущие
  HP/база или явное N/A с причиной, целевые HP и предполагаемая база для всех 69.
  danger_status, target_hp_status, base_damage_status отделяют VERIFIED от PROPOSED.
  Семантические типы предложены аналитически, не являются существующими runtime tags.
- [ATTACKS.csv](ATTACKS.csv): A1–A7/коэффициент/числовой целевой бюджет для
  повреждающих действий; отдельно DAMAGE, PERIODIC_DAMAGE, HEALING, SUMMON,
  CONTROL, NO_DIRECT_DAMAGE, UNREACHABLE. Эффекты выделены вторичной категорией.
  Ноль direct damage у лечения/контроля не означает нулевой эффективности.
  Лечение хранительницы остаётся текущими 5 HP/20 ticks в source description;
  предложение — сохранить лечение, призыв и контроль до отдельного задания.
- [FORMS.md](FORMS.md): обычные состояния, Мор, роли/NBT, фазы и позы одного ID.
- [TECHNICAL_RISKS.md](TECHNICAL_RISKS.md): точные файлы/места будущих изменений.
- [UNVERIFIED.csv](UNVERIFIED.csv): ограничения и **4 группы блокирующих решений**.
- [POST_IMPLEMENTATION_REQUIREMENTS.md](POST_IMPLEMENTATION_REQUIREMENTS.md):
  требования к приёмке, Git и PolyMC после реализации, с подписью «патч оружия».

## Утверждённые цели HP — изменения кода НЕ внесены

| EntityType / состояние | Сейчас | Цель | Статус |
|---|---:|---:|---|
|zmey_gorynych logical|3600, transform1800, floor180|2800, transform1500, floor75|VERIFIED|
|likho_one_eyed normal|320|350|VERIFIED|
|likho_one_eyed mor|640|700|VERIFIED; усиленное состояние того же ID|
|firebird|3000|3000 + обязательная сюжетная неубиваемость|VERIFIED; отсутствие полной защиты — DEFECT|
|cursed_volkolak|400|700|PROPOSED; требуется подтверждение пользователя|
|ataman|150|100|VERIFIED; сохранена прежняя цель|
|tugarin_zmey|350|400|VERIFIED; сохранена прежняя цель|
|nightingale|250|350|VERIFIED; сохранена прежняя цель|
|extinguished_flame_guardian|500|900|VERIFIED; сохранена прежняя цель|
|defiled_spring_keeper|450|800|VERIFIED; сохранена прежняя цель|

Остальные ранее заполненные цели сохраняются. Где согласованной цели не было,
предложено сохранить актуальные HP и все вариантные максимумы; это PROPOSED,
а не задним числом утверждённый баланс. 1024 служебных Горыныча/Жар-птицы/Калина
не заменяют логические/override HP. Пороги Горыныча1500/75 абсолютные:
они не равны 50%/5% нового максимума2800.

## Предложения опасности и урона

IV..VIII имеют прежние базы14/18/20/24/26 и согласованные коэффициенты
A1=.65, A2=.80, A3=.70, A4=1, A5=1.15, A6=1.35, A7=1.50.
Для ранее не назначенных уровней предложены конкретные значения в CSV.
Нижние базы 0=0, I=4, II=6, III=10 — **PROPOSED**, не часть ранее согласованной
шкалы. Небоевые существа уровня0 не получают новых атак. Уровень VIII Баюн/
Жар-птица описывает опасность сюжетного ответа, не обычный hostile AI.
Личные базы Серого Волка13, Оборотня15, ответ Жар-птицы20 предложено сохранить.
База auxiliary6 — только явно предложенная аналитическая калибровка, не
утверждённый атрибут каждой копии/снаряда. Копии игрока сохраняют snapshot формулу.

Цель = база × коэффициент. Для комбо, облака, DoT и сложной атаки число —
**общий бюджет действия/окна на одну жертву**, не новая величина каждого импульса.
Components shot/cloud/landing/aftermath должны делить родительский бюджет:
числа нескольких строк не складываются как независимые casts. Для вторичного
effect damage требуется отдельная проверка полного бюджета при реализации.
У raw(net)/diamondRaw/rawForDiamond/rawForIron числовая цель — предложение
калибровки: не подставлять net в hurt как raw. Native стрелы зависят от скорости;
Mob.doHurtTarget — от экипировки/атрибутов/эффектов. Эти зависимости сохранены.
Баюн max(1000,maxHP+absorption+100) — сюжетное исключение: nominal A7=39
не является разрешением заменить летальную формулу обычным ударом.

reachability отдельно от target_status: VERIFIED_STATIC_SELECTABLE означает
найденный активный выбор (Тугарин), STATIC_CANDIDATE означает обработчик/контекст
без полной сертификации producer chain, UNVERIFIED_PRODUCER — отсутствующее
подтверждение создания. Такие строки не названы реально наблюдавшимися атаками.
Все они обработаны как известные действия, остаток явно перенесён в UNVERIFIED.
Старый scheduler Тугарина, его новый STONE и поздняя think-ветка Игоши исключены
как VERIFIED_STATIC_UNREACHABLE. HotStone не приписан Баннику как доступная атака.

## Блокирующие решения перед реализацией: 4

1. **B1:** подтвердить 700 HP именно для cursed_volkolak. До ответа не внедрять.
2. **B2:** утвердить предложенные уровни/HP/личные базы/A-категории и нижние
   базы; отдельно сохранить или переопределить сюжетный летальный ответ Баюна.
3. **B3:** утвердить правила старых сохранений. Предложена миграция доли HP с
   версией/идемпотентностью, сохранением завершённых фаз и приоритетом ledger.
4. **B4:** утвердить состав silver_vulnerable и единое правило начисления;
   предложение — один максимальный бонус за hit, прежние +1.5 сохранять до решения.

Это четыре группы решений, а не число непроверенных ячеек. Неподтверждённые
частоты спавна, cooldown/radius, producer chains, NBT/event order, фактические
попадания/i-frames/экипировка, lethal/void Жар-птицы проверяются при реализации
и тестировании; они не требуют запускать Minecraft в аналитическом патче.

## Проверки и границы результата

Табличный gate — PASS:111 уникальных ID,69 классификаций,265 строк,
арифметика коэффициентов, наличие файлов/строк, отсутствие повторов.
[table-checks.json](table-checks.json) и [completion-summary.json](completion-summary.json).
Gate не доказывает полноту игрового поведения или правильность предложенного баланса.
Build/resource gates **NOT_REPEATED**; Minecraft/client/server/GameTest **0 запусков**;
MSPT/FPS NOT_MEASURED. Gameplay, art, атрибуты, registry ID и production-версия
этим аудитом не менялись. Переход к1.9.8.2, установка в PolyMC, commit/push не выполнялись.

Исторический SHA2561.9.7: 0f1e76dd6bef952350a67d83da5357cc26bcc8dcb290b7eb281c8e46d2290e0e.
Текущий наблюдаемый SHA256 приведён в table-checks.json и отличается от исторического:
4f2b9ae1542e7aadd63592d7c987f84a44c164654071815fdcaab156ebb7eaae.
Во время задачи появились параллельные изменения production-файлов/ресурсов.
Они сохранены; этот аудит JAR не создавал и не заменял. Побайтная неизменность
production относительно исторического отчёта не заявляется, чужая сборка не принята.
Исторические gates (включая FAIL старого resource validator) сохранены в CHECKS.md;
[предыдущий отчёт](REPORT_BEFORE_FINALIZATION.md) — исторический, с заменёнными
этим заданием целями2200 и неутверждённой моровой формой, не актуальное ТЗ.
''',encoding='utf-8')
forms=OUT/'FORMS.md';s=forms.read_text(encoding='utf-8')
s=s.replace('целевые HP морового/«проклятого» — UNVERIFIED.','целевые HP морового Лиха700 — VERIFIED по финальному заданию; тот же EntityType. Не создавать третью «проклятую» форму.')
s=s.replace('Утвердить, что цель700 относится именно к этому ID.','Цель700 для этого ID — PROPOSED по финальному заданию, не внедрять до подтверждения пользователя.')
s+='''
## Финальные цели состояний (2026-10-13)

|Состояние одного ID|Текущее HP|Целевое HP|Статус|
|---|---:|---:|---|
|likho_one_eyed normal|320|350|VERIFIED|
|likho_one_eyed mor|640|700|VERIFIED; base350 × существующий mor_health2|
|zmey_gorynych full logical|3600|2800|VERIFIED|
|zmey_gorynych TRANSFORM threshold|1800|1500|VERIFIED; абсолютный порог|
|zmey_gorynych EXHAUSTED floor|180|75|VERIFIED; абсолютный порог|
|firebird narrative actor|3000|3000 + неубиваемость|VERIFIED; обычный путь смерти — DEFECT|
|cursed_volkolak full|400|700|PROPOSED; отдельный ID от volkolak|

Все другие формы сохраняют текущие множители как PROPOSED, если отдельно не
согласованы: медведица120/оленёнок12; SWAMP Русалка36/Водяной72;
шахтёр40; песиголовец28/40/50. Не переносить normal HP на все роли.
Моровые цели при предложенном сохранении normal: Леший250, Полудница120,
Кикимора72, Огненный змей387.5, Овинник350; Лихо700 утверждено отдельно.
Предложенные базы моровых атак сохраняют normal nominal base, без нового
множителя каждой спецатаки; текущие атрибутные множители остаются отдельной
формулой. Уровень морового Лиха предлагается VI как normal, опасность усиливают
HP700 и отдельный набор HigherMor. Поза/phase/summon не добавляют самостоятельных
существ к69. Живой auxiliary и связанный WarHorse оставлены в исходной группе,
не меняя соглашение подсчёта.
'''
forms.write_text(s,encoding='utf-8')
risk=OUT/'TECHNICAL_RISKS.md';s=risk.read_text(encoding='utf-8')
s=s.replace('Горыныч 3600 → 2200','Горыныч 3600 → 2800; пороги1500/75')
s=s.replace('HP3600→2200; при сохранении долей TRANSFORM_HP1800→1100, FLOOR180→110. Решение о сохранении долей зафиксировать в ТЗ.','HP3600→2800; TRANSFORM_HP1800→1500, FLOOR180→75. Все три значения VERIFIED; доли50%/5% не сохранять.')
s=s.replace('×2200/3600','×2800/3600').replace('относительно1100/110','относительно1500/75')
s+='''
## Лихо — normal350 и mor700, один EntityType

- src/main/java/org/slavicmyths/hunt/BossKind.java:3 — LIKHO base320→350.
- src/main/java/org/slavicmyths/hunt/WorldBoss.java:58 — attributes(k.hp);
  src/main/java/org/slavicmyths/hunt/WorldBoss.java:124 — NBT повторно задаёт базу.
- src/main/java/org/slavicmyths/mor/MorController.java:49 — существующий
  ADD_MULTIPLIED_TOTAL+1 даст700; не добавить второй множитель/отдельный тип.
  src/main/java/org/slavicmyths/mor/MorController.java:48 — hasModifier по ID,
  src/main/java/org/slavicmyths/mor/MorController.java:113 — load: проверить порядок
  permanent modifier и повторного base reset, сохранение доли без autoheal.
- src/main/java/org/slavicmyths/mor/HigherMor.java:25 — активный mor scheduler;
  src/main/java/org/slavicmyths/mor/HigherMor.java:37 — sweep14/12,
  src/main/java/org/slavicmyths/mor/HigherMor.java:52 — beam12.
  Literal спецатак не равен атрибуту11×1.05; ordinary Fight заменяется MorGoal.

## Проклятый Оборотень — условная цель700, не внедрять до B1

- src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java:33 — HP400/base15.
- src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java:102 — phase250/100:
  PROPOSED сохранить доли62.5%/25%, получить437.5/175, утвердить с B2/B3.
- src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java:103 — bar denominator400.
- src/main/java/org/slavicmyths/volkolak/CursedVolkolak.java:60 — strike attribute
  delta и phase3+2; GRAB shield16 не обязательно меняется с базой.
- VolkolakEvents.lethal (точный source в FORMS.md) и escape: сохранить сюжетное
  несмертельное завершение отдельно от независимой встречи. Проверить Health/phase
  NBT и сохранённые Attributes, не путать с hunt/HuntMob ordinary volkolak165.

## Хранители —900/800, состояние очищения того же типа

- src/main/java/org/slavicmyths/guardians/GuardianRules.java:6 — FIRE_HP/WATER_HP;
  src/main/java/org/slavicmyths/guardians/GuardianRules.java:10 — threshold5%:
  будущие45/40 следуют существующему правилу, PROPOSED сохранить5%.
- src/main/java/org/slavicmyths/guardians/GuardianEntity.java:25 — attributes;
  :38 configure ledger Health; :68 beginPurification; :72 setHealth interception.
  Проверить entity NBT/ledger, самостоятельную и структурную встречи отдельно.
- src/main/java/org/slavicmyths/guardians/GuardianLedger.java:10 — ensure;
  :14 stage; :29 load — ACTIVE→READY/сброс вентилей, не сбросить CLEANSED/claims.
- FireGuardian/SpringKeeper producers и WaterEcho/WaterPrison источники в ATTACKS:
  новая HP не меняет literal melee/slam/pulse/whip/whirl, лечение5 и echo25.
  Не считать очищение смертью, не выдавать standalone структурные фрагменты.

## Разделение рисков

Блокирующие решения **B1–B4** описаны в REPORT.md и UNVERIFIED.csv. Они блокируют
зависимые изменения, но не завершение аналитического инвентаря.
В рамках реализации проверить доступность STATIC_CANDIDATE/UNVERIFIED_PRODUCER,
компоненты damage budget, native immunity, сохранения, фактические lethal пути
Жар-птицы (обязательная защита уже согласована), no duplicate silver и subscriber
order. Эти проверки не являются дополнительными обязательными игровыми gates
аналитического1.9.8.1. Полная runtime достижимость здесь не заявляется.
'''
risk.write_text(s,encoding='utf-8')
for name in ['PROJECT_STATUS.md','ROADMAP.md','ARCHITECTURE.md']:
    p=ROOT/'docs'/name;s=p.read_text(encoding='utf-8')
    p.write_text('''## Финальный аналитический аудит1.9.8.1 —2026-10-13

COMPLETE_WITH_LIMITATIONS:69/69 существ классифицированы,262 известные строки
действий обработаны +3 исключённые недостижимые ветки (265). VERIFIED/PROPOSED
разделены;4 группы решений перед реализацией, source/runtime ограничения явные.
Горыныч target2800/1500/75; Лихо350/mor700 тот же ID; Жар-птица3000 и обязательная
неубиваемость (текущий путь смерти — дефект); cursed_volkolak700 PROPOSED.
Архитектура runtime и production-версия аудитом не менялись,1.9.8.2 не начата.
Build/resources NOT_REPEATED, игровые запуски0. Параллельные production правки
сохранены, текущий JAR hash отличается от исторического; чужие gates не приняты.
Результаты: docs/audit/creatures-1.9.8.1/REPORT.md. Git/PolyMC и маркировка
«патч оружия» после реализации: POST_IMPLEMENTATION_REQUIREMENTS.md там же.
Нижеследующие IN_PROGRESS записи — история предыдущих итераций.

'''+s,encoding='utf-8')
