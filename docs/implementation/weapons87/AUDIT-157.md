# #157 — источник и аналитическая матрица 68 оружейных IDs

IN_PROGRESS. Это аудит исходников, не замеры боёв и не игровая приёмка. Ни один production-файл оружия не менялся.

Полные поля/точные рецепты/loot conditions/paths/SHA: `audit-68.json`, удобная таблица `audit-68.csv`. Атрибуты получены реальными конструкторами в изолированном offline Java fixture. Forge registry открыт только в отдельном тестовом процессе; миры/существа/client/server не создаются.

## Расчёт

База игрока: damage1, speed4, reach3; к ней добавлены реальные ItemAttributeModifiers. Для одинаковых ударов без критов верхняя частота min(speed,2) из-за hurt cooldown. Броня20/toughness0: damage × (1−clamp(armor−damage/(2+toughness/4),armor/5,20)/25); Mace pressure добавляется до брони. Это сравнимая аналитическая оценка, не измеренный DPS. У снарядов, контроля, призыва и косы дан отдельный бюджет/тайминг; fist attributes не выдаются за их урон.

## Матрица

| ID | RU | damage / speed / reach | durability | raw melee DPS | решение |
|---|---|---|---|---|---|
| bone_knife | Костяной нож | 3 / 2.50 / 3 | 75 | 6.0 | KEEP stats; inspect family geometry and ANIMATION |
| silver_dagger | Серебряный кинжал | 4 / 2.50 / 3 | 220 | 8.0 | KEEP stats; inspect family geometry and ANIMATION |
| nightingale_dagger | Кинжал Соловья | 10 / 2.00 / 3 | 250 | 20.0 | BALANCE/ART/ANIMATION/BUG: durability source700 actual250 |
| ancient_carolingian_sword | Древний каролингский меч | 4 / 1.60 / 3 | 131 | 6.4 | KEEP stats; inspect family geometry and ANIMATION; BUG requested durability overridden by tier constructor |
| restored_carolingian_sword | Восстановленный каролингский меч | 6 / 1.60 / 3 | 250 | 9.6 | KEEP stats; inspect family geometry and ANIMATION; BUG requested durability overridden by tier constructor |
| silver_sword | Серебряный меч | 6 / 1.60 / 3 | 220 | 9.6 | KEEP stats; inspect family geometry and ANIMATION |
| perunite_sword | Перунитовый меч | 6.5 / 1.60 / 3 | 850 | 10.4 | KEEP stats; inspect family geometry and ANIMATION |
| tugarin_sword | Парные клинки Тугарина | 10 / 1.80 / 3 | 1561 | 18.0 | ART/ANIMATION; balance evaluate conditional bleed |
| trophy_kalin_sabre | Сабля Воеводы | 9 / 1.30 / 3 | 1561 | 11.7 | ART/ANIMATION; KEEP numbers pending contextual comparison |
| club | Дубина | 5 / 0.90 / 3 | 59 | 4.5 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace |
| wooden_mace | Деревянная булава | 5 / 1.05 / 3 | 59 | 5.25 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace |
| stone_mace | Каменная булава | 7 / 0.85 / 3 | 131 | 5.95 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace |
| mace | Железная булава | 8 / 0.90 / 3 | 250 | 7.2 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace |
| silver_mace | Серебряная булава | 7 / 1.00 / 3 | 220 | 7.0 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace |
| perunite_mace | Перунитовая булава | 9.5 / 0.60 / 3 | 850 | 5.7 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace |
| ancient_chekan | Древний чекан | 5 / 1.20 / 3 | 131 | 6.0 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace; BUG requested durability overridden by tier constructor |
| chekan | Чекан | 6 / 1.30 / 3 | 250 | 7.8 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace; BUG requested durability overridden by tier constructor |
| battle_axe | Боевой топор | 10 / 0.70 / 3 | 250 | 7.0 | KEEP stats; inspect family geometry and ANIMATION |
| thunder_axe | Громовой топор | 8.5 / 0.90 / 3 | 850 | 7.65 | BUG candidate proc timing; ART/ANIMATION |
| stone_flail | Каменный кистень | 7 / 0.70 / 3 | 131 | 4.9 | ART/ANIMATION: flat material flails require assembled geometry |
| gold_flail | Золотой кистень | 6 / 0.70 / 3 | 32 | 4.2 | ART/ANIMATION: flat material flails require assembled geometry |
| flail | Кистень | 8 / 0.70 / 3 | 250 | 5.6 | ART/ANIMATION: flat material flails require assembled geometry |
| silver_flail | Серебряный кистень | 8 / 0.70 / 3 | 220 | 5.6 | ART/ANIMATION: flat material flails require assembled geometry |
| diamond_flail | Алмазный кистень | 9 / 0.70 / 3 | 1561 | 6.3 | ART/ANIMATION: flat material flails require assembled geometry |
| perunite_flail | Перунитовый кистень | 8.5 / 0.70 / 3 | 850 | 5.95 | ART/ANIMATION: flat material flails require assembled geometry |
| netherite_flail | Незеритовый кистень | 10 / 0.70 / 3 | 2031 | 7.0 | ART/ANIMATION: flat material flails require assembled geometry |
| ataman_flail | Кистень атамана | 12 / 0.70 / 3 | 1561 | 8.4 | ART/ANIMATION; KEEP gameplay architecture |
| ancient_spear | Древнее копьё | 4 / 1.20 / 3 | 131 | 4.8 | KEEP stats; ANIMATION/grip; BUG/BALANCE BurialWeapon reach discrepancy for owner review |
| restored_spear | Восстановленное копьё | 6 / 1.20 / 3 | 250 | 7.2 | KEEP stats; ANIMATION/grip; BUG/BALANCE BurialWeapon reach discrepancy for owner review |
| spear | Копьё | 5 / 1.20 / 4.25 | 131 | 6.0 | KEEP stats; ART/ANIMATION/grip review |
| gold_spear | Золотое копьё | 4 / 1.20 / 4.25 | 32 | 4.8 | KEEP stats; ART/ANIMATION/grip review |
| iron_spear | Железное копьё | 6 / 1.20 / 4.25 | 250 | 7.2 | KEEP stats; ART/ANIMATION/grip review |
| silver_spear | Серебряное копьё | 6 / 1.20 / 4.25 | 220 | 7.2 | KEEP stats; ART/ANIMATION/grip review |
| diamond_spear | Алмазное копьё | 7 / 1.20 / 4.25 | 1561 | 8.4 | KEEP stats; ART/ANIMATION/grip review |
| perunite_spear | Перунитовое копьё | 6.5 / 1.20 / 4.25 | 850 | 7.8 | KEEP stats; ART/ANIMATION/grip review |
| netherite_spear | Незеритовое копьё | 8 / 1.20 / 4.25 | 2031 | 9.6 | KEEP stats; ART/ANIMATION/grip review |
| pool_spear | Копьё омута | 9 / 1.20 / 3 | 250 | 10.8 | ART/ANIMATION/BUG: durability source420 actual250; targeting audit |
| thunder_spear | Громовое копьё | 7.5 / 1.10 / 3 | 850 | 8.25 | BALANCE reach comparison needs approval; ART/ANIMATION |
| victor_spear | Копьё победителя | 12 / 1.20 / 4.25 | 1561 | 14.4 | KEEP geometry candidate; ANIMATION/grip review |
| berdysh | Бердыш | 9 / 0.70 / 3 | 250 | 6.3 | KEEP stats; inspect family geometry and ANIMATION |
| noon_sickle | Серп Полудницы | 6 / 1.50 / 3 | 220 | 9.0 | KEEP stats; inspect family geometry and ANIMATION |
| karachun_scythe | Коса Карачуна | 12 / 1.00 / 3 | 2400 | ability/ammo: см. JSON | KEEP ornate geometry; ANIMATION/grip #117/#165 |
| poloz_whip | Плеть Полоза | 13 / 1.00 / 6 | 2031 | 13.0 | ART/ANIMATION #180/#190; KEEP numbers pending evaluation |
| stone_throwing_knife | Каменный метательный нож | 3 / 2.30 / 3 | 131 | 6.0 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| gold_throwing_knife | Золотой метательный нож | 2 / 2.30 / 3 | 32 | 4.0 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| iron_throwing_knife | Железный метательный нож | 4 / 2.30 / 3 | 250 | 8.0 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| silver_throwing_knife | Серебряный метательный нож | 4 / 2.30 / 3 | 220 | 8.0 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| diamond_throwing_knife | Алмазный метательный нож | 5 / 2.30 / 3 | 1561 | 10.0 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| perunite_throwing_knife | Перунитовый метательный нож | 4.5 / 2.30 / 3 | 850 | 9.0 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| netherite_throwing_knife | Незеритовый метательный нож | 6 / 2.30 / 3 | 2031 | 12.0 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| stone_sulitsa | Каменная сулица | 4 / 1.50 / 3.5 | 131 | 6.0 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| gold_sulitsa | Золотая сулица | 3 / 1.50 / 3.5 | 32 | 4.5 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| iron_sulitsa | Железная сулица | 5 / 1.50 / 3.5 | 250 | 7.5 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| silver_sulitsa | Серебряная сулица | 5 / 1.50 / 3.5 | 220 | 7.5 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| diamond_sulitsa | Алмазная сулица | 6 / 1.50 / 3.5 | 1561 | 9.0 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| perunite_sulitsa | Перунитовая сулица | 5.5 / 1.50 / 3.5 | 850 | 8.25 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| netherite_sulitsa | Незеритовая сулица | 7 / 1.50 / 3.5 | 2031 | 10.5 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| short_bow | Короткий лук | 1 / 4.00 / 3 | 300 | ability/ammo: см. JSON | ART/ANIMATION; compare vanilla bow draw20t |
| heavy_bow | Тяжёлый лук | 1 / 4.00 / 3 | 500 | ability/ammo: см. JSON | KEEP family difference; ANIMATION/pull QA |
| finist_bow | Лук Финиста | 1 / 4.00 / 3 | 640 | ability/ammo: см. JSON | ART/ANIMATION #163/#186; evaluate pair not naked bow |
| bone_arrow | Костяная стрела | 1 / 4.00 / 3 | 0 | ability/ammo: см. JSON | KEEP mechanics; ART arrow family review |
| serpent_crossbow | Змеиный арбалет | 1 / 4.00 / 3 | 520 | ability/ammo: см. JSON | KEEP accepted design candidate; ANIMATION charge/state QA |
| three_breath_crossbow | Арбалет трёх дыханий | 1 / 4.00 / 3 | 680 | ability/ammo: см. JSON | KEEP accepted geometry candidate; ANIMATION/VFX review; no blind buff |
| carved_staff | Резной посох | 3 / 1.20 / 3 | 59 | ability/ammo: см. JSON | KEEP stats; inspect family geometry and ANIMATION |
| storm_staff | Грозовой посох | 1 / 4.00 / 3 | 240 | ability/ammo: см. JSON | BALANCE: expensive pulse weak relative same-stage ranged; ART/ANIMATION |
| veles_staff | Посох Велеса | 1 / 4.00 / 3 | 48 | ability/ammo: см. JSON | BUG: acquisition unproven; utility design review, no invented recipe |
| likho_staff | Посох Лихо | 10 / 1.20 / 3 | 1561 | ability/ammo: см. JSON | ART/ANIMATION; KEEP control/healing numbers |
| morozko_staff | Посох Морозко | 1 / 4.00 / 3 | 0 | ability/ammo: см. JSON | KEEP geometry candidate; ANIMATION/VFX/ability review |

## Открытые доказательные вопросы

- Посох Велеса: источник выдачи в survival пока не подтверждён; не придумывать рецепт.
- Прочность ряда SwordItem/TieredItem не соответствует p.durability(...) в вызывающем конструкторе: native component показывает tier value. Исправление требует решения о фактических числах.
- ThunderAxe: вторичный magic1 вызывается после melee в hurtEnemy; проверить vanilla invulnerability/call order перед исправлением.
- Исходные GUI/held/player projections ещё готовятся; KEEP/FIX художественных частей не считать окончательным по одному наличию PNG.
- Runtime, PvP/MP, FPS, реальное время убийства и 1P/3P владелец: NOT_RUN / OWNER_PENDING.

| REQUIREMENT | EVIDENCE | RESULT |
|---|---|---|
| 68 IDs и параметры из настоящих конструкторов | native-attributes.json; generated factories + provenance | PASS |
| Источники получения/эффекты/сравнение каждого ID | audit-68.json/csv; неясности перечислены | IN_PROGRESS: Veles acquisition |
| Полная художественная оценка GUI/1P/3P | source renders ещё готовятся | NOT_RUN |
| Никаких production изменений во время #157 | baseline.json, отдельная preservation-проверка | pending verification |
| Java21 clean build/resource/JAR | audit-build.log; gate report готовится | IN_PROGRESS |
| Игровой бой/визуал/MP/FPS | 0 game launches | NOT_RUN / OWNER_PENDING |
