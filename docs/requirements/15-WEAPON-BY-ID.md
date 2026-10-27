# 15. Поимённая матрица всех 68 ID оружия

**Источник:** реальный файл `docs/implementation/weapons87/audit-68.csv` из `hell`. Это **снимок прежнего source-аудита**, не новый native runtime и не обещание, что численные значения не изменились позже. Перед любой правкой заново измерять текущие `ModItems`/`ModGear` и боевые handlers. По каждому ID дополнительно обязательны [общие требования оружия](05-WEAPONS-COMBAT.md): добыча без OP, соответствие утверждённому силуэту, атрибуты/DPS против защищённых целей, точный эффект, хват/первое лицо, сохранение NBT и визуальная приёмка владельцем.

**Чтение колонок:** «снимок dmg/speed/dur» — зафиксированный технический аудит, **не фактический live damage**; «что проверить» — формулировка в исходном аудите, НЕ автоматически выполненный фикс. Если указано `KEEP`, это не игровое принятие.

| Registry ID | Русское имя | Семейство | Снимок dmg | Снимок speed | Снимок durability | Требование/открытый вопрос из аудита |
|---|---|---|---:|---:|---:|---|
| bone_knife | Костяной нож | knives | 3.0 | 2.5 | 75 | KEEP stats; inspect family geometry and ANIMATION |
| silver_dagger | Серебряный кинжал | knives | 4.0 | 2.5 | 220 | KEEP stats; inspect family geometry and ANIMATION |
| nightingale_dagger | Кинжал Соловья | knives | 10.0 | 2.0 | 250 | BALANCE/ART/ANIMATION/BUG: durability source700 actual250 |
| ancient_carolingian_sword | Древний каролингский меч | swords | 4.0 | 1.5999999046325684 | 131 | KEEP stats; inspect family geometry and ANIMATION; BUG requested durability overridden by tier constructor |
| restored_carolingian_sword | Восстановленный каролингский меч | swords | 6.0 | 1.5999999046325684 | 250 | KEEP stats; inspect family geometry and ANIMATION; BUG requested durability overridden by tier constructor |
| silver_sword | Серебряный меч | swords | 6.0 | 1.5999999046325684 | 220 | KEEP stats; inspect family geometry and ANIMATION |
| perunite_sword | Перунитовый меч | swords | 6.5 | 1.5999999046325684 | 850 | KEEP stats; inspect family geometry and ANIMATION |
| tugarin_sword | Парные клинки Тугарина | swords | 10.0 | 1.7999999523162842 | 1561 | ART/ANIMATION; balance evaluate conditional bleed |
| trophy_kalin_sabre | Сабля Воеводы | swords | 9.0 | 1.2999999523162842 | 1561 | ART/ANIMATION; KEEP numbers pending contextual comparison |
| club | Дубина | maces | 5.0 | 0.9000000953674316 | 59 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace |
| wooden_mace | Деревянная булава | maces | 5.0 | 1.0499999523162842 | 59 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace |
| stone_mace | Каменная булава | maces | 7.0 | 0.8499999046325684 | 131 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace |
| mace | Железная булава | maces | 8.0 | 0.9000000953674316 | 250 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace |
| silver_mace | Серебряная булава | maces | 7.0 | 1.0 | 220 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace |
| perunite_mace | Перунитовая булава | maces | 9.5 | 0.5999999046325684 | 850 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace |
| ancient_chekan | Древний чекан | maces | 5.0 | 1.2000000476837158 | 131 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace; BUG requested durability overridden by tier constructor |
| chekan | Чекан | maces | 6.0 | 1.2999999523162842 | 250 | KEEP/FIX geometry after source render; ANIMATION; BALANCE for perunite_mace; BUG requested durability overridden by tier constructor |
| battle_axe | Боевой топор | axes | 10.0 | 0.7000000476837158 | 250 | KEEP stats; inspect family geometry and ANIMATION |
| thunder_axe | Громовой топор | axes | 8.5 | 0.9000000953674316 | 850 | BUG candidate proc timing; ART/ANIMATION |
| stone_flail | Каменный кистень | flails | 7.0 | 0.7000000476837158 | 131 | ART/ANIMATION: flat material flails require assembled geometry |
| gold_flail | Золотой кистень | flails | 6.0 | 0.7000000476837158 | 32 | ART/ANIMATION: flat material flails require assembled geometry |
| flail | Кистень | flails | 8.0 | 0.7000000476837158 | 250 | ART/ANIMATION: flat material flails require assembled geometry |
| silver_flail | Серебряный кистень | flails | 8.0 | 0.7000000476837158 | 220 | ART/ANIMATION: flat material flails require assembled geometry |
| diamond_flail | Алмазный кистень | flails | 9.0 | 0.7000000476837158 | 1561 | ART/ANIMATION: flat material flails require assembled geometry |
| perunite_flail | Перунитовый кистень | flails | 8.5 | 0.7000000476837158 | 850 | ART/ANIMATION: flat material flails require assembled geometry |
| netherite_flail | Незеритовый кистень | flails | 10.0 | 0.7000000476837158 | 2031 | ART/ANIMATION: flat material flails require assembled geometry |
| ataman_flail | Кистень атамана | flails | 12.0 | 0.7000000476837158 | 1561 | ART/ANIMATION; KEEP gameplay architecture |
| ancient_spear | Древнее копьё | spears | 4.0 | 1.2000000476837158 | 131 | KEEP stats; ANIMATION/grip; BUG/BALANCE BurialWeapon reach discrepancy for owner review |
| restored_spear | Восстановленное копьё | spears | 6.0 | 1.2000000476837158 | 250 | KEEP stats; ANIMATION/grip; BUG/BALANCE BurialWeapon reach discrepancy for owner review |
| spear | Копьё | spears | 5.0 | 1.2000000476837158 | 131 | KEEP stats; ART/ANIMATION/grip review |
| gold_spear | Золотое копьё | spears | 4.0 | 1.2000000476837158 | 32 | KEEP stats; ART/ANIMATION/grip review |
| iron_spear | Железное копьё | spears | 6.0 | 1.2000000476837158 | 250 | KEEP stats; ART/ANIMATION/grip review |
| silver_spear | Серебряное копьё | spears | 6.0 | 1.2000000476837158 | 220 | KEEP stats; ART/ANIMATION/grip review |
| diamond_spear | Алмазное копьё | spears | 7.0 | 1.2000000476837158 | 1561 | KEEP stats; ART/ANIMATION/grip review |
| perunite_spear | Перунитовое копьё | spears | 6.5 | 1.2000000476837158 | 850 | KEEP stats; ART/ANIMATION/grip review |
| netherite_spear | Незеритовое копьё | spears | 8.0 | 1.2000000476837158 | 2031 | KEEP stats; ART/ANIMATION/grip review |
| pool_spear | Копьё омута | spears | 9.0 | 1.2000000476837158 | 250 | ART/ANIMATION/BUG: durability source420 actual250; targeting audit |
| thunder_spear | Громовое копьё | spears | 7.5 | 1.0999999046325684 | 850 | BALANCE reach comparison needs approval; ART/ANIMATION |
| victor_spear | Копьё победителя | spears | 12.0 | 1.2000000476837158 | 1561 | KEEP geometry candidate; ANIMATION/grip review |
| berdysh | Бердыш | polearms | 9.0 | 0.7000000476837158 | 250 | KEEP stats; inspect family geometry and ANIMATION |
| noon_sickle | Серп Полудницы | polearms | 6.0 | 1.5 | 220 | KEEP stats; inspect family geometry and ANIMATION |
| karachun_scythe | Коса Карачуна | polearms | 12.0 | 1.0 | 2400 | KEEP ornate geometry; ANIMATION/grip #117/#165 |
| poloz_whip | Плеть Полоза | polearms | 13.0 | 1.0 | 2031 | ART/ANIMATION #180/#190; KEEP numbers pending evaluation |
| stone_throwing_knife | Каменный метательный нож | throwing_knives | 3.0 | 2.3 | 131 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| gold_throwing_knife | Золотой метательный нож | throwing_knives | 2.0 | 2.3 | 32 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| iron_throwing_knife | Железный метательный нож | throwing_knives | 4.0 | 2.3 | 250 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| silver_throwing_knife | Серебряный метательный нож | throwing_knives | 4.0 | 2.3 | 220 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| diamond_throwing_knife | Алмазный метательный нож | throwing_knives | 5.0 | 2.3 | 1561 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| perunite_throwing_knife | Перунитовый метательный нож | throwing_knives | 4.5 | 2.3 | 850 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| netherite_throwing_knife | Незеритовый метательный нож | throwing_knives | 6.0 | 2.3 | 2031 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| stone_sulitsa | Каменная сулица | sulitsas | 4.0 | 1.5 | 131 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| gold_sulitsa | Золотая сулица | sulitsas | 3.0 | 1.5 | 32 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| iron_sulitsa | Железная сулица | sulitsas | 5.0 | 1.5 | 250 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| silver_sulitsa | Серебряная сулица | sulitsas | 5.0 | 1.5 | 220 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| diamond_sulitsa | Алмазная сулица | sulitsas | 6.0 | 1.5 | 1561 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| perunite_sulitsa | Перунитовая сулица | sulitsas | 5.5 | 1.5 | 850 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| netherite_sulitsa | Незеритовая сулица | sulitsas | 7.0 | 1.5 | 2031 | KEEP mechanics pending effects QA; ART/ANIMATION distinct knife vs dart |
| short_bow | Короткий лук | bows | 1.0 | 4.0 | 300 | ART/ANIMATION; compare vanilla bow draw20t |
| heavy_bow | Тяжёлый лук | bows | 1.0 | 4.0 | 500 | KEEP family difference; ANIMATION/pull QA |
| finist_bow | Лук Финиста | bows | 1.0 | 4.0 | 640 | ART/ANIMATION #163/#186; evaluate pair not naked bow |
| bone_arrow | Костяная стрела | bows | 1.0 | 4.0 | 0 | KEEP mechanics; ART arrow family review |
| serpent_crossbow | Змеиный арбалет | bows | 1.0 | 4.0 | 520 | KEEP accepted design candidate; ANIMATION charge/state QA |
| three_breath_crossbow | Арбалет трёх дыханий | bows | 1.0 | 4.0 | 680 | KEEP accepted geometry candidate; ANIMATION/VFX review; no blind buff |
| carved_staff | Резной посох | staves | 3.0 | 1.2000000476837158 | 59 | KEEP stats; inspect family geometry and ANIMATION |
| storm_staff | Грозовой посох | staves | 1.0 | 4.0 | 240 | BALANCE: expensive pulse weak relative same-stage ranged; ART/ANIMATION |
| veles_staff | Посох Велеса | staves | 1.0 | 4.0 | 48 | BUG: acquisition unproven; utility design review, no invented recipe |
| likho_staff | Посох Лихо | staves | 10.0 | 1.2000000476837158 | 1561 | ART/ANIMATION; KEEP control/healing numbers |
| morozko_staff | Посох Морозко | staves | 1.0 | 4.0 | 0 | KEEP geometry candidate; ANIMATION/VFX/ability review |

**Ни одному из 68 ID этот документ не выставляет PASS в реальной игре.** Дополнительно сверить #87/#157/#279 и `docs/implementation/weapons87/PROGRESS.md`; в оружейной задаче отмечать отдельно `SOURCE_PASS`, `GAMEPLAY_NOT_RUN`, `OWNER_PENDING`.
