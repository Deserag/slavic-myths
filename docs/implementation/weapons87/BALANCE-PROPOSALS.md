# #158/#159 — согласования и предложения

Пакет A явно согласован владельцем 21.10.2026 («Согласовать пакет A»). Реализован отдельным `combat/WeaponDefaults.java` через штатное ModifyDefaultComponentsEvent; offline native-event проверка сравнивает все компоненты 68 предметов. Пакет B явно согласован22.10.2026 («Согласовать пакет B»), реализация/проверки отдельным пакетом12. C/D всё ещё предложения, не применять без ответа.

Измерения: `native-attributes.json` (реальные production-конструкторы Java21). Аналитический DPS без crit/enchant/рун/промахов; не игровой замер.

## A. Восстановить явно заданную в исходниках прочность

Вызывающий конструктор задаёт p.durability(...), но TieredItem/SwordItem затем заменяет её прочностью материала. Исправление ставит явно заданное значение после получения компонентов родителя, без изменения урона/скорости.

| ID | фактически сейчас | предлагается | основание |
|---|---:|---:|---|
| nightingale_dagger | 250 | 700 | NightingaleDagger.java задаёт 700; редкая награда Соловья |
| ancient_carolingian_sword | 131 | 90 | BurialWeapon.java задаёт 90; находка должна быть повреждённой |
| restored_carolingian_sword | 250 | 450 | BurialWeapon.java задаёт 450; результат реставрации |
| ancient_spear | 131 | 90 | BurialWeapon.java задаёт 90 |
| restored_spear | 250 | 380 | BurialWeapon.java задаёт 380 |
| ancient_chekan | 131 | 90 | ModItems.java задаёт 90 |
| chekan | 250 | 400 | ModItems.java задаёт 400 |
| pool_spear | 250 | 420 | PoolSpear.java задаёт 420 |

## B. Два слабых изделия ритуального этапа

| ID | сейчас | предложение | эффект и риск |
|---|---|---|---|
| perunite_mace | damage9.5, speed0.6, rawDPS5.7 | damage9.5, speed0.8, rawDPS7.6 | Железная mace уже rawDPS7.2, pressure.35; перунит pressure.4 помогает броне, но ритуал требует mace/perunite/thunder_stone/ancient_sign. Урон удара и armor pressure остаются прежними; ускорение на33% требует согласования. |
| storm_staff | magic3 / CD80t (руна72t), range7; raw0.75DPS | magic6 / CD60t (руна54t), range7; raw2DPS | Ритуал использует carved_staff/perunite/thunder_stone/ancient_sign. Одноцелевой луч без боеприпаса; предлагаемая полезность ниже full-draw heavy bow, но надёжнее прицеливание. Не распространять на Лихо/Морозко/боссов. |

## C. Дальность копий

| ID | сейчас | предложение | основание |
|---|---|---|---|
| ancient_spear, restored_spear, thunder_spear | native interaction range3 | 4.25 (как SpearItem) | Сейчас это SwordItem/HeavyWeaponItem, без spear_reach. Название не доказывает ошибку: это изменение механики, до согласования НЕ применять. Урон/скорость/спецэффект сохранить. |

## D. Посох Велеса

Подтверждённого survival output не найдено: ни рецепт/лут, ни явно адресованная выдача; найденные ссылки — регистрация, отображение, руны, advancement и потребление в рецепте чана. **Не добавлять источник получения автоматически.** Возможное решение для обсуждения: armorer shapeless `carved_staff +leshy_heart +fern_flower +silver_fitting`, без изменения 60-секундного призыва/CD120с. Это новый acquisition path и требует решения владельца; аудит не выдаёт его за существующий канон.

## KEEP чисел

`three_breath_crossbow`: уже ≥14 на стрелу/3 стрелы, burst≥42 до armor при попадании всех, плюс режимы; усиление не предлагается. `karachun_scythe`: сохраняются базовые12, AoE, undead+25%, life/soul и три навыка. `poloz_whip`, `tugarin_sword`, `likho_staff`, `victor_spear`, `finist_bow` имеют значимые условные преимущества, поэтому безусловный рост урона не предлагается. Материальный perunite tier bonus2.5 ниже diamond3 компенсируется системой рун/получения; весь tier не менять.
