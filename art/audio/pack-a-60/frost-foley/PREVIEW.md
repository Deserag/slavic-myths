# #60 / #64 — коса, посох и ледяные объекты

Записанный предметный foley, не голоса. Стеклянные props изображают фантастический лёд. Выбрано по делегированию; прослушивание и игровая приёмка NOT_RUN. Геометрия #58 сохранена.

## Коса: первый взмах

Event `karachun.scythe_windup` → `foley/karachun/swosh-04.ogg` → `karachun/Karachun.java action(1/2/3), hit10/10/14`. 0.255s.

A — прежний файл (только сравнение): ![Прежний Коса: первый взмах](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/karachun/scythe_windup_0.ogg)

B — новая запись: ![Коса: первый взмах](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/foley/karachun/swosh-04.ogg)

## Коса: второй взмах

Event `karachun.scythe_windup` → `foley/karachun/swosh-05.ogg` → `karachun/Karachun.java action(1/2/3), hit10/10/14`. 0.411s.

A — прежний файл (только сравнение): ![Прежний Коса: второй взмах](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/karachun/scythe_windup_0.ogg)

B — новая запись: ![Коса: второй взмах](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/foley/karachun/swosh-05.ogg)

## Коса: металлический удар 1

Event `karachun.scythe_hit` → `foley/karachun/metal_hit_01.ogg` → `karachun/Karachun.java castTick age10/10/14`. 0.256s.

A — прежний файл (только сравнение): ![Прежний Коса: металлический удар 1](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/karachun/scythe_hit_0.ogg)

B — новая запись: ![Коса: металлический удар 1](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/foley/karachun/metal_hit_01.ogg)

## Коса: металлический удар 2

Event `karachun.scythe_hit` → `foley/karachun/metal_hit_03.ogg` → `karachun/Karachun.java castTick age10/10/14`. 0.375s.

A — прежний файл (только сравнение): ![Прежний Коса: металлический удар 2](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/karachun/scythe_hit_0.ogg)

B — новая запись: ![Коса: металлический удар 2](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/foley/karachun/metal_hit_03.ogg)

## Посох: деревянный удар

Event `morozko.staff_hit` → `foley/morozko/wood_hit_03.ogg` → `morozko/Morozko.java castTick a1 age10`. 0.380s.

A — прежний файл (только сравнение): ![Прежний Посох: деревянный удар](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/morozko/staff_hit.ogg)

B — новая запись: ![Посох: деревянный удар](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/foley/morozko/wood_hit_03.ogg)

## Ледяной снаряд босса: звон при вылете

Event `morozko.ice_bolt` → `foley/morozko/bfh1_glass_hit_01.ogg` → `morozko/Morozko.java castTick a2 age13`. 0.299s.

A — прежний файл (только сравнение): ![Прежний Ледяной снаряд босса: звон при вылете](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/morozko/ice_bolt.ogg)

B — новая запись: ![Ледяной снаряд босса: звон при вылете](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/foley/morozko/bfh1_glass_hit_01.ogg)

## Посох игрока: вылет осколка

Event `morozko.staff_shoot` → `foley/morozko/bfh1_glass_hit_02.ogg` → `morozko/FrostSkills.java shoot`. 0.333s.

A — прежний файл (только сравнение): ![Прежний Посох игрока: вылет осколка](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/morozko/staff_shoot.ogg)

B — новая запись: ![Посох игрока: вылет осколка](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/foley/morozko/bfh1_glass_hit_02.ogg)

## Лёд: треск при ударе снаряда или появлении шипа

Event `morozko.spike_break` → `foley/morozko/bfh1_glass_breaking_01.ogg` → `morozko/FrostShard.java onHit; FrostHazard.java kind0 a0`. 0.650s.

A — прежний файл (только сравнение): ![Прежний Лёд: треск при ударе снаряда или появлении шипа](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/morozko/spike_break.ogg)

B — новая запись: ![Лёд: треск при ударе снаряда или появлении шипа](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/foley/morozko/bfh1_glass_breaking_01.ogg)

## Ледяной двойник: разрушение

Event `morozko.clone_break` → `foley/morozko/bfh1_glass_breaking_02.ogg` → `morozko/IceDouble.java dissolve; Morozko.java die`. 0.388s.

A — прежний файл (только сравнение): ![Прежний Ледяной двойник: разрушение](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/morozko/clone_break.ogg)

B — новая запись: ![Ледяной двойник: разрушение](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/foley/morozko/bfh1_glass_breaking_02.ogg)

## Зеркальная копия игрока: разрушение

Event `morozko.mirror_break` → `foley/morozko/bfh1_glass_breaking_03.ogg` → `morozko/PlayerDouble.java shatter`. 0.700s.

A — прежний файл (только сравнение): ![Прежний Зеркальная копия игрока: разрушение](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/morozko/mirror_break.ogg)

B — новая запись: ![Зеркальная копия игрока: разрушение](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/foley/morozko/bfh1_glass_breaking_03.ogg)

## Сосулька: короткий звон при появлении

Event `morozko.staff_icicles` → `foley/morozko/bfh1_glass_falling_01.ogg` → `morozko/FrostHazard.java kind1 a0`. 0.417s.

A — прежний файл (только сравнение): ![Прежний Сосулька: короткий звон при появлении](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/morozko/staff_icicles.ogg)

B — новая запись: ![Сосулька: короткий звон при появлении](D:/slavic-myths/src/main/resources/assets/slavicmyths/sounds/foley/morozko/bfh1_glass_falling_01.ogg)
