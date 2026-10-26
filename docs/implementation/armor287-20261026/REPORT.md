# #287 — броня: проверка существующего поведения, IN_PROGRESS / OWNER_PENDING

## Актуализация после исправления #277:28/28PASS

Первый собственный коммит аудита #287 — `1ef5dace`. Исходные14tests/четыре запуска и receipt.json ниже остаются историческим снимком. Затем проведены ещё два изолированных server запуска: [extended20.log](extended20.log)20/20PASS и [extended28.log](extended28.log)28/28PASS на неизменённом armor production и исправленном AI #277 `08076afc3d60b1b63d8323d0b9ec300b1dd1ffa1`. **Итого #287:6реальных server запусков,0клиентов.** Итоговый production JAR/source/SHA зафиксирован отдельно в [extended-receipt.json](extended-receipt.json); первоначальный JAR receipt не переписан.

Дополнительные PASS: SevenLeague +30% после60условных server movement samples, сброс на повороте/снятии; Wind160charge/decay/collision/teleport/flight/death/equipment cleanup и native jump attribute; native travel обычной дуги apex4.153745/range7.132678/21steps, ledge4blocks; charged native travel apex12.461236/range11.492487/36steps. Заряд подаётся контролируемыми movement samples, landing LivingFallEvent подаётся вручную как обычно сетевым движением; это не клиентский input/relog и не измерение MSPT.

Также PASS: родной lightning damageSource перунитового set и снятие; idol heavy-hit*.9 относительно такой же diamond chest armor/KB*.55 и снятие; Marinka safeSide blink2.5blocks/900tick CD/снятие; Kartaus mantle после actual owned-guard kill,400tick CD и снятие Resistance. Ward shield100native block attempts с настоящим projectile owner/отсутствием owner/cancelled block и environmental exclusion PASS. Prepared Kartaus encounter/owned guard создаются fixture, естественная босс-цепочка не объявлена проверенной.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Все ранее14armor cases после изменения AI | extended28.log | PASS |
| SevenLeague ramp и Wind charge/native travel/fall-credit | extended28.log, именованные extendedWind tests | PASS controlled server API/native travel |
| Perunite lightning/set removal | extended28 peruniteActualLightningSetAndRemoval | PASS native damage |
| Три trophy chest procs/CD/remove | extended28 idolHeavy/Marinka/Kartaus | PASS prepared native cases |
| Ward shield positive/negative block cases | extended28 extendedShield…/Environmental… | PASS перечисленных случаев |
| Полный natural Survival всех34/GUI/client input/real relog/real respawn/network MP/прочие shield procs | owner checklist, не запускались | NOT_RUN |

Доказанных armor defects по-прежнему нет; численные характеристики/арт/registry/рецепты не менялись, предложений ребаланса нет. #287 остаётся OPEN/IN_PROGRESS, поскольку natural acquisition и ручная приёмка не выполнены. Следующая задача — owner smoke, не автоматический следующий roadmap.


База `hell` edc915302243e481389a696727c2936d6ce030b3, дерево было чистым; origin совпал. Main8789e08d не изменяется. Предыдущие #278/#284 сохранены. Это собственный проверочный коммит #287, без AI #277, экономики, loot, новых рецептов и изменения production-кода/ресурсов. Прямое разрешение на отдельный изолированный GameTest для этого пакета получено в чате26.10.2026. PolyMC/пользовательские миры не изменялись.

## Исправлено / неизменённое

Доказанного функционального дефекта production-брони в выполненных сценариях не обнаружено; принудительного исправления ради коммита нет. **KEEP** все существующие defense/toughness/knockback/durability/enchantability/repair/stats, registry IDs, rune capacities, модели/UV/текстуры, цены, награды и эффекты. Не предлагается численный ребаланс без доказательства проблемы.

Добавлены воспроизводимый поимённый аудит и native server/offline/package проверки. В34строках каталога фактически: **22 ArmorItem,7 ClothingItem,4 ShieldItem,1 BeltItem**. Одежда намеренно не имеет armor/durability, пояс использует BeltData attachment; это не «сломанные34боевых части». Четыре shield включены в классификацию, расширенная shield balance/Curios не перерабатывались.

Именованные IDs, slots, защита/прочность/ремонт/зачарование/руны и источники: [CATALOG.md](CATALOG.md), [catalog.json](catalog.json). Все native before/equipped/removed и фактические классы/материалы/ремонтные ингредиенты: [native.json](native.json). Этот файл содержит34проверенные строки, а не34обещанных новых эффекта.

## Acceptance contract до реализации

| REQUIREMENT | REAL_SOURCE | PLAYER_PATH | TEST | EXPECTED |
|---|---|---|---|---|
| Registry классификация34 | ModItems, Textiles, ModGear, UniqueEquipment, PolozRewards, GorynychContent, RewardContent | обычная добыча/рецепт → настоящий equip slot | all34RegistryAttributesSlotsRepeatAndCodec |22Armor/7clothes/4shield/1attachment |
| Атрибуты/снятие/re-equip | actual ArmorItem modifiers, ArmorSetHealth, PolozRewardEvents | надеть → снять → повторить | native.json,8повторов каждого equipable | исходные значения возвращаются, нет накопления |
| Ремонт/enchants/runes | ArmorMaterial.repairIngredient, AnvilMenu, RuneRepair/ItemState | родной материал в наковальне → ремонт | all22NativeAnvilRepairAndEnchantmentEligibility | принимается22из22, dirt отвергается, имя/Unbreaking/старые rune components сохраняются |
| Базовая защита относительно vanilla | зарегистрированные материалы/vanilla CombatRules | физический удар в одинаковых условиях | sixFullSetsDamageHealthAndNoFreeHeal; offline curves | native damage совпадает с actual armor/toughness и существующими бонусами |
| Уникальные4 | UniqueEquipmentEvents/Rules, ClearSightNetwork, ArmorerRecipe | получить прежним4×4upgrade → надеть → активировать/получить удар → снять | выбранные реальные UniqueEquipmentGameTests через wrappers | wind base/fall, stance, control/CD, sight/CD,2×upgrade не дублирует |
| Перунит/дракон/Полоз set | ArmorSetHealth, GorynychEquipment, PolozRewardEvents | крафт/улучшение → полный комплект → разобрать | sets, dragonPiecesEffectRemovalAndSetProc, loadedSetAttributesNoDuplicateAndUnequip |25/40/34maxHP без бесплатного лечения, убратькомплект→20, KB снимается |
| Сохранение/смерть/измерение | native player save/load, existing Clone handlers | выйти/перезайти/умереть/сменитьизмерение | actual dimension + death/manual Clone; codec | native cases PASS; настоящий клиентский relog/MP отдельно NOT_RUN |

## Проверки и измерения

Четыре реальных изолированных GameTest запуска в `D:/slavic-myths/run-armor287-20261026`, клиентских запусков0. Игроки ServerPlayer с embedded listener: настоящий server engine, но без сетевого клиента. Старые публичные native UniqueEquipment tests используются выборочно через отдельные14 wrappers/scenarios; прежний namespace не включался целиком и весь исторический пакет не объявляется перепроверенным.

- [baseline](baseline.log):12tests,10PASS/2FAIL — ошибки фикстуры: vanilla generic обходит броню; пустое anvil itemName снимает custom name.
- [run2](run2.log):14tests,13PASS/1FAIL — сравнение случайно включало уже сработавший dragon low-health Resistance: надевание полного комплекта при10HP активировало существующую способность. Калибровка исправлена, production не менялся.
- [run3](run3.log):14tests,13PASS/1FAIL — ошибочная арифметика ожидаемого Poloz reduction .26; реальные исходные4+8+6+4=22%. Фикстура исправлена на .22, production не менялся.
- [run4](run4.log):**14/14PASS**. Сырые неудачные прогоны сохранены; они не представлены как доказанные баги брони.

Проверены34native registry rows, все equipable8раз на снятие/надевание;22материальных ремонта через настоящий AnvilMenu.createResult, enchant eligibility Unbreaking, сохранение имени/enchants/legacy rune component, dirt negative. Результат наковальни — серверное вычисление результата с сохранённым полем имени, не настоящий GUI-click игрока и не прохождение ремонта в Survival. Rune component был подготовлен фикстурой; это не утверждение, что любой новый rune разрешён всем материалам. Реальные nominal capacities записаны поимённо.

Сравнение одного физического удара10HP, без enchantment/активных временных proc. [comparison.json](comparison.json):

| Комплект | Armor | Toughness | KB | MaxHP | Получено урона10 |
|---|---|---|---|---|---|
| iron |15|0|0|20|6.0|
| diamond |20|8|0|20|3.0|
| netherite |20|12|.40|20|2.8|
| perunite |15|2|0|25|5.6|
| dragon |19|8|.40|40|3.4|
| poloz |20|12|.95|34|2.184|

Poloz .95 — сумма vanilla netherite-material .40 и существующих дополнительных .55; это не предложенный новый стат. Эти шесть измерений не являются универсальным рейтингом брони против всех источников. В отдельном offline-процессе проверяется offline-формула armor, сверенная с перечисленными native-измерениями на тех же registry-измерениях, проверяются raw1/4/10/20/40 и старые UniqueEquipmentRules. [offline.json](offline.json) не объявляется запуском игры.

Отдельно PASS: dragon helmet100→75ticks Blindness/снятие→100, chest onFire10→8.5/снятие→10, boots fall10→6.5/снятие→10, leggings knockback с material+proc .315/снятие .5; full-set low-health proc и отсутствие повторной активации до cooldown. Существующий кратковременный Resistance, выданный до снятия, не переписан — timed proc не назван постоянным attribute leak. Перунитовый lightning set modifier прочитан в StormProtection, полный живой lightning-сценарий здесь отдельно NOT_RUN.

Unique PASS: wind boots base jump/speed/fall/noFlight; богатыpский порог HP и idempotence/уменьшение физического damage; леггинсы control duration и5сек expiry/15сек CD; sight activation/equipment/cooldown/exclusions; существующие5upgrade recipes/component preservation/wrongbase rejection и native ArmorerMenu расход/повтор. Реальная смена измерения и смерть выполнены выбранным old test, Clone событие после смерти отправляется вручную. **Native save/load и ручной Clone не равны клиентскому relog/настоящему respawn.**

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
|34 классифицированных IDs/slots/attrs/remove/re-equip | native.json; run4 all34… | PASS server API |
|22 repair/enchant eligibility/component preservation | run4 all22… | PASS native calculation; GUI consumption NOT_RUN |
|6комплектов physical/set-health/no stack | comparison.json; run4 sets/loadedSet | PASS controlled server cases |
|Unique4 перечисленные бонусы/upgrade | run4 wrappers | PASS указанных случаев |
|Dragon individual effects и full-set CD | run4 dragonPieces… | PASS server |
|Native player data load, actual dimension/death+manual Clone | run4 loadedSet…/uniqueDimensionDeathClone | PASS указанных операций |
|Natural Survival acquisition всех34/каждый trophy proc/SevenLeague run charge/полный Wind momentum/perunite lightning | source index и owner checklist | NOT_RUN gameplay |
|Настоящие GUI/relog/respawn/сетевой MP/client render | клиент не запускался | NOT_RUN owner |
|Java21 clean build/offline/resources/JAR/no production delta | receipt.json,build.log,offline.json,source-jar.json | точный финальный receipt |
|PolyMC install/owner acceptance | не выполнялись | POLYMC_NOT_INSTALLED / OWNER_PENDING |

## Как проверить в Minecraft

Точный JAR/version/SHA/source commit — receipt.json. Только отдельный тестовый инстанс Minecraft1.21.1/NeoForge21.1.255/Curios9.5.1; новый мир и копия старого, backup прежнего mods/JAR, ровно один slavicmyths JAR. Автоматической установки нет.

1. Без OP изготовить одежду на её рабочих местах и gambeson/plate/perunite по существующим рецептам. Для уникальных частей использовать прежний Armorer4×4 рецепт; для dragon — существующий upgrade из перунитового donor/scale/perunite/diamond; Poloz — netherite donor в руке,2scales, Shift+ПКМ на smithing table. Source/reward index всех34 в CATALOG. Ожидаются правильные слоты; одежда не обещает armor; woven_belt носится своим attachment действием.
2. По одному надеть/снять каждую часть. Сравнить displayed stats, HP и knockback; повторить8раз и смешать с vanilla. Полный перунит25HP/дракон40/Полоз34; надевание не лечит, снятие убирает бонус. Повреждённую вещь отремонтировать родным материалом — durability уменьшается, имя/enchants/имеющиеся runes не исчезают; dirt не работает.
3. Уникальные: wind boots проверить прыжок/спринт/падение и накопление разгона; богатыpский нагрудник на HP≤35% и после лечения/снятия; leggings получить сильный knockback, ждать5сек, повтор до15секCD не срабатывает; clear_sight активировать соответствующим управлением, снять/надеть — CD не сбросился.
4. Dragon: сравнить fire/explosion, fall, blindness/darkness/nausea, knockback с отдельной частью и без неё. Полный комплект при HP<30% даёт краткий Resistance с прежним CD; повторное переодевание не должно сбрасывать cooldown. Проверить perunite lightning и Poloz снижение на настоящих источниках.
5. Отдельно проверить источник-специфичные trophy rewards: idol heavy-hit guard, Marinka dodge, Kartaus mantle при штатном условии. Эти proc не выдаются за проверенные всем набором только из-за успешного equip.
6. Полностью выйти/перезайти с зачарованной повреждённой бронёй/рунами, сменить измерение, умереть при текущем keepInventory и восстановиться. Предметы/компоненты сохраняются по правилам мира, нет дубликатов/лишнего HP/оставшихся после утраты modifiers. Два настоящих клиента проверяют независимость атрибутов и видимость эффектов.

НЕ ПРОВЕРЕНО: естественное получение/целиком все рецепты с настоящим игроком, все trophy/SevenLeague/Wind charged сценарии, client GUI/render, настоящий relog/respawn/MP, защита по всем видам урона и внешним модам. Риски production не добавлены, поскольку его delta0; остаются указанные пробелы приёмки. Никакие balance numbers на утверждение не выносятся как доказанный дисбаланс — достаточного основания не найдено.

## Риски/откат / следующая задача

Независимый проверочный коммит не меняет поведение мода; его откат убирает только отчёт/фикстуры/opt-in task. Main и пользовательский PolyMC untouched, установленный JAR не заменён, backup установки не создавался. #287 OPEN/«В работе»/OWNER_PENDING, Project Status не менялся. Следующая явно разрешённая часть этого пакета — #277 отдельным коммитом. #285/#286/#276/#298 и четыре решения #284 не исполняются.

Offline harness attempts: две попытки использовать NeoForge CombatRules без loader завершились ошибками bootstrap/FeatureFlags; никакая из них не объявляется PASS или игровым запуском. Финальный offline harness использует явно обозначенную формулу и реальные прежние UniqueEquipmentRules; native damage остаётся доказан отдельным GameTest.
