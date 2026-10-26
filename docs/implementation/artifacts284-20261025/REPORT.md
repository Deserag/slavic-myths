# #284 — артефакты и реликвии, IN_PROGRESS / OWNER_PENDING

Пакет начат25.10, отчёт завершён26.10.2026. База `hell` — `4fba8ac19b0d8946c7c7f2f33effef9c9eec1dc8`, чистое дерево после сохранённого #278. #296 сохранён ранее; его owner-приёмка не подменяется новой работой. Main `8789e08dbcb029ae7b252049dec62c47c7780cce` не меняется. Issues не закрывались, owner-accepted не устанавливался, поле Project Status не менялось.

## Исправлено

Три production Java-файла, две страницы книги RU/EN и пять доказанных функциональных дефектов. Уже существовали и прошли указанные серверные сценарии без изменения механики: гусли, скатерть, метла/ступа/пест, четыре hero calls, bottle collection/upgrade и воздействия Горыныча. Остальные строки каталога имеют source evidence и перечисленные ограничения, а не общий gameplay PASS. [Исходники до правок](before/) сохранены, точные разрешённые замены проверяет `tools/verify_artifacts284.py`:

1. `mor_flask`: если сервер отклонил `addFreshEntity`, возвращается FAIL; склянка и10-tick cooldown не расходуются. Успешный бросок и creative-правило сохранены.
2. `fading_flame_charm`: сначала принимается GUARD, затем расходуется1. При отказе эффекта FAIL, расхода нет.
3. `mor_ward`: сначала принимается WARD, затем обычная копия расходуется. Особая `GuardianWard196` по-прежнему многоразовая. При отказе эффекта расхода нет.
4. `tracker_sense`: `TrackerReady`/`TrackerUntil` записываются после успешного эффекта. Отклонённое чутьё больше не блокирует повтор на12000ticks.
5. `spring_tear`: предмет и cooldown расходуются после принятия TEAR. При отклонении эффекта стек и сохранённый cooldown остаются прежними.

Не менялись коэффициенты, длительности, цены, источники наград, recipes, loot, registry IDs, item components, полёт, модели/UV/текстуры, оружие/броня и сюжетные gates. Аудио и legacy audio generators не запускались. Scoped artifacts284_resources.py меняет только две существующие studied-страницы книги RU/EN: описание применения гуслей/скатерти и честное отсутствие Survival-пути вместо ложного совета искать рецепт в JEI. Overlay подключён последним в maintained generator; полный генератор не запускался, идемпотентность проверяется отдельно.

## Реальный реестр и путь игрока

Полные34 строки: [CATALOG.md](CATALOG.md), [catalog.json](catalog.json). Включены ровно20 `magic/artifacts`, четыре `magic/charms` и10 существующих функциональных/сюжетных предметов вне этой группы. Есть прямые ссылки на actual Java lines, рецепты, loot, custom recipes и динамические награды. Историческая открытость Issue не считается отсутствием реализации.

| REQUIREMENT | REAL_SOURCE | PLAYER_PATH | TEST | EXPECTED |
|---|---|---|---|---|
| Гусли | GusliItem, ArtifactEvents, RusalkaSong | Получение BLOCKED → удерживать ПКМ около eligible моба → calm → отпустить → истечение | gusliCalmRangeImmunityExpiryAndCancel | r12; exposure30; calm60; иммунные/дальние не затронуты; release cooldown; короткая отмена100 только source-аудит |
| Скатерть | SkatertItem/Block/Tile, ItemState | Получение BLOCKED → поставить на опору → порции → забрать → rest | skatertPortionsSingleReturnAndRest | шесть порций; еда6; CD24; один возврат; rest6000 |
| Метла/ступа/пест | YagaServices.EXCHANGES; FlightItem/FlyingVessel/CargoMenu/FlightNetwork | Яга stage2/3 → штатный обмен → поставить → сесть → управлять → посадка/упаковка | broomPlacementCollisionOwnerAndOneSeat; flightOwnerPestleCargoAndSinglePack | collision fail не расходует; владелец;1/2места; пест; cargo3diamonds сохраняется; повторного pack нет |
| Призывы богатырей | HeroIntroductions; HeroCallItem/FalconCallItem; StoryPolicy/HeroCalls/HeroLease | Поручение знакомства Яги/Баюна → помочь герою → токен → вызов/отзыв | storyCallsCannotBypassIntroductions; introducedHeroesSingleCanonicalEntityAndRecall | без Helped отказ; после контролируемого Helped один canonical UUID, отзыв и cooldown, нет дубля во время отзыва |
| Волк/чутьё/Сивка | Quest185Story/WolfSupport; SivkaBonds/Whistle | Сюжет185/личная связь → разрешённый предмет → применение | locked calls; tracker success/cancel/save | locked Wolf/unbound Sivka отказ; tracker active/CD верны; естественные цепочки ещё не пройдены |
| Око Лиха | HuntItems.Role.FANG; реальный boss drop; рецепты | Лихо → трофей → существующие рецепты/ключи | registry/recipe audit | ингредиент/ключ, не новый active detector |
| Три дыхания | BreathCloud.interact; GorynychUpgrade; SerpentCrossbow/GorynychEquipment/StormCrossbow | Собрать облака Горыныча бутылками → улучшить арбалет → mode | bottlesCollectedOnceAndNotDirectWeapons; breathBottlesActualUpgradeAndRemainders; threeBreathImpactEffectsAndStormRepeatGuard | plain ingredients; стекло3; collection budget3; разные mor/fire/storm воздействия; per-target storm guard |
| Magic-charms | Rituals, SanctuaryItem/Events, Quest185Item/Events | warding ingredient; volkhv collectible; два consumable use | registry/recipes + accepted/rejected effects | не переносить в Curios; новые баффы не добавляются |
| Ресурс/повтор/отмена | три изменённых item handlers | законное use → успех/отказ → повтор | пять rejection tests + positive use/repeat | при отказе нет расхода/CD; при успехе прежние значения |

`mor_flask` **заражает**, а не очищает: `MorController.apply(false)` начинает50-tick превращение обычного подходящего духа. Tooltip прямо говорит «Для испытаний». Survival источник отсутствует намеренно согласно этой диагностической маркировке; склянка не предлагается в обычный лут.

Бутылки Горыныча — реальные материалы улучшения, их tooltip прямо говорит «для стрельбы не требуется». Отсутствие отдельного `use` не является сломанным оружием. Громовой камень и warding_charm — ингредиенты. Камень очищения — материал рецепта fading_flame_charm. Амулет волхва добывается из существующих таблиц, но active/equipment роль не заявлена; новый бафф/Curios не придуман.

## Не сделано / решения владельца

**BLOCKED_NEEDS_ACQUISITION_APPROVAL:** `gusli`, `skatert`, `kartaus_black_seal`, `kalin_battle_seal`. В текущих production recipes/loot/trades/rewards не найден путь получения этих четырёх предметов. Прямой поиск по item ID/registry field и динамическим местам выдачи сохранён в catalog.json. Их механизм существует, но появление в Creative не является Survival-доступностью. Доступность самих боссов через другие сюжетные entrypoints этим аудитом не отрицается и не доказывается.

Новые источники/стоимость/стадию не устанавливал: поручение запрещает придумывать рецепты, менять прогрессию и раздавать уникальные ключи без согласования. Предложение для решения: гусли/скатерть — отдельные редкие сделки или награды Яги после её существующих этапов; печати — персональная выдача соответствующей сюжетной ветки либо явное сохранение как диагностических triggers. Перед реализацией нужны выбор пути и конкретные цена/условия. Варианты не внесены в код и не являются утверждённым балансом.

Остаётся owner-only runtime: реальное Survival-получение всех34, полный естественный бой/сюжет, клиентское управление и посадка, GUI/подсветка, настоящие relog/respawn/dimension и сетевой MP. Не объявлять всю #284 завершённой, не передавать цепочку на итоговую приёмку при четырёх BLOCKED.

## Фактические проверки

Разрешение на изолированный #284 GameTest и дальнейшие проверки на25.10 дано владельцем в чате. Все игровые прогоны завершены25.10;26.10 выполняются финальные offline/build/package gates. Основной PolyMC, пользовательские saves/config/options и установленный JAR не менялись.

Четыре реальных GameTest сервера в `D:/slavic-myths/run-artifacts284-20261025`:

- baseline:14 tests,7PASS/7FAIL — пять доказанных production-дефектов и две ошибки фикстуры;
- run2:14 tests,13PASS/1FAIL — оставался неверно подготовленный cloud tick;
- run3:15/15PASS;
- run4:19/19PASS — дополнительно bottle collection, broom collision/seat, положительный summon/recall всех4 и настоящий полёт projectile склянки в опору с заражением Лешего.

Сырые [baseline](gametest-baseline.log), [run2](gametest-run2.log), [run3](gametest-run3.log), [run4](gametest-run4.log). В фикстурах: crouching Pose устанавливается явно; владелец облака регистрируется в server level; `ServerLevel.tickNonPassenger` увеличивает tickCount, в отличие от прямого `Entity.tick`. Production flight/cloud ради подгонки тестов не менялись.

После #284 изменений ещё один реальный сервер аксессуаров: [7/7 PASS](accessories-regression.log). Вместе с #278 в этой работе8 GameTest запусков:4 для #278 и4 для #284; клиентских запусков0. Серверные игроки используют embedded connection без отправки пакетов: два таких игрока — не настоящий сетевой MP. Контролируемый Helped — не прохождение знакомства без читов. SaveWithoutId/load и Curios NBT — не настоящий перезаход. Полные записи coverage/status: [runtime.json](runtime.json).

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| 24 обязательных IDs +10 дополнительных; source/acquisition classification | catalog.json; all34RegistryAndAcquisitionRecipes | PASS registry/loaded recipes; natural obtaining NOT_RUN |
| Пять расходных дефектов reproduced→fixed | baseline5FAIL → run4 соответствующие5PASS | PASS server events |
| Гусли calm/range/immune/expiry/release | run4 gusliCalmRangeImmunityExpiryAndCancel | PASS server behavior; client sound/render/early100cancel NOT_RUN |
| Скатерть portions/return/rest/no duplicate | run4 skatertPortionsSingleReturnAndRest | PASS tested server lifecycle; relog/expiry2400 after unload NOT_RUN |
| Метла collision/owner/seat, ступа pestle/cargo/pack | run4 две flight tests | PASS tested server operations; client steering/landing/MP NOT_RUN |
| Hero gates, четыре canonical summons/recall/CD | run4 locked/introduced tests | PASS controlled server preconditions; full story/restart NOT_RUN |
| Breath bottles/material recipe/three impacts/repeat guard | run4 три breath tests | PASS server callbacks/recipe; whole crossbow flight/client/real boss battle NOT_RUN |
| Tracker/tear успех/повтор и сохранённый Ready | run4 trackerTearSuccessRepeatAndSavedCooldown | PASS use/native player codec; true rejoin NOT_RUN |
| Регрессия Curios | accessories-regression.log7/7 | PASS server fixtures; client MP NOT_RUN |
| Java21 clean build/offline/source/resources/production JAR | receipt.json; build.log; source-jar.json; current-resources.json | точный финальный результат в receipt |
| Получение gusli/skatert/двух seals | исчерпывающий source/resource index без найденного источника | BLOCKED_NEEDS_ACQUISITION_APPROVAL |
| Настоящее Survival/GUI/relog/MP/owner acceptance | не проводились | NOT_RUN |
| Установка PolyMC | не проводилась, прежний SHA сохранён | POLYMC_NOT_INSTALLED |

## Как проверить в Minecraft — владелец

Точная сборка `slavicmyths-2.0.1-HELL.jar`, путь/SHA/source commit — [receipt.json](receipt.json). Автоматической установки нет. Для ручной проверки — отдельный инстанс1.21.1/NeoForge21.1.255/Curios9.5.1, новый мир и отдельная копия старого. Сохранить backup прежнего mods/JAR, оставить один slavicmyths JAR. Не заменять основные миры.

1. **Получение без OP.** Пройти обычные пути из CATALOG: древнее капище/рецепт громового камня; святилища для fading charm/слезы/воды/камня; Лихо для Ока; сюжет185 для wolf_call/чутья; поручения знакомства Яги/Баюна для hero tokens; личная связь Сивки для её whistle. Гусли/скатерть/две печати сейчас записать BLOCKED, не заменять этот этап `/give` и не считать его PASS.
2. **Исправленные расходники.** Взять несколько обычных mor_ward/fading charms: активировать → появляется прежний эффект, расход1 → повторить при действующем эффекте → расход0. Для особой GuardianWard196 сделать существующий рецепт из фрагментов двух хранителей и Ока Мора: активация не должна уничтожить его. Для слезы — выпить при ране → лечение и cooldown, повтор до cooldown не расходует новую. Чутьё включается на180с, повтор при активном эффекте/до600с запрещён, после конца работает снова. Полный cooldown ждать в тестовом мире, не имитировать командой при приёмке.
3. **Негативное отклонение.** Нельзя воспроизвести запрет эффекта/спавна чистым Survival без подходящего внешнего правила. Этот случай воспроизведён именно GameTest listeners, указанными в5 rejection tests; для диагностики разрешённая команда: `gradlew.bat -Partifacts284Check runArtifacts284Checks` с Java21, отдельным test world. Не выдавать это за обычный игровой маршрут.
4. **Полёт.** Купить у Яги метлу наstage2, ступу и пест наstage3 по действующим ценам EXCHANGES. Поставить в свободном месте, сесть владельцем. Метла одноместная; ступа2места, управляет явный pilot с пестом. Без песта/с чужим владельцем управление не работает. Перевезти3 обычных предмета в багаже, приземлиться, слезть и упаковать присев ударом; ожидается ровно одна ступа с тем же грузом. При занятом месте/открытом багаже pack запрещён. Повторить полёт/посадку и проверить collision, высотные ограничения и посадку пассажира.
5. **Союзники.** После настоящей помощи каждому герою получить токен, использовать до завершения channel/после полного channel, повторить вызов/отзыв. Отмена до windup не создаёт героя. Во время отзыва и cooldown второго экземпляра нет. Чужой токен не заменяет личный Helped. Волк требует WolfUnlocked и не создаёт второго support; Сивка требует личный действующий bond. Переместить/перезайти, проверить канонический единственный UUID и отсутствие сброса cooldown.
6. **Горыныч.** В настоящей встрече собрать три разных вида облака в пустые бутылки: расход стекла1, получение соответствующего дыхания1, не более бюджета облака. Бутылки не бросаются по ПКМ. Улучшить прежний serpent_crossbow существующим custom recipe с scale/steel и всеми3дыханиями: получается three_breath_crossbow, возвращаются3стеклянные бутылки. На подходящих противниках поочерёдно проверить моровое облако/слабость, огонь и штормовой контроль; не цеплять союзника, не получать новые bottle из собственного арбалетного облака. Шторм не должен повторно обходить per-target guard одним залпом.
7. **Гусли/скатерть — отдельная диагностика, НЕ Survival PASS.** При наличии старого законно полученного экземпляра: гусли удерживать около обычного моба в12блоках, ждать минимум30ticks, отпустить → успокоение ещё60ticks, затем кончается; immune/boss и13блоков не затронуты. Короткое прерывание имеет100tick cooldown, полный play600. Скатерть поставить на опору:6порций, сытый не расходует; быстрое повторное нажатие не обходит24tick pause; снять/сломать/убрать опору → одна скатерть с5мин rest, повторная установка до rest невозможна. Проверить естественный возврат после120с, выгрузку чанка/перезаход с поставленной скатертью. Командная выдача нужна только при осознанной диагностике, не решает недоступность.
8. **Сохранение/негативные случаи/MP.** В отдельной копии старого мира сохранить положенный груз, cooldown чутья/героев, эффекты, personal whistles; полностью выйти и вернуться. Повторить после смерти и смены измерения; cooldown не сброшен, предметы/союзники не дублируются. Два настоящих клиента: разные личные токены/Сивки, один активирует/другой наблюдает, попробовать чужое управление/багаж/призыв. Все успешные результаты записать отдельно от GameTest.

НЕ ПРОВЕРЕНО: реальная Survival-доступность и естественные встречи, client GUI/render/audio/control/landing, полная кампания/боссы, настоящий relog/respawn/dimension/сетевой MP, длительная выгрузка скатерти и все комбинации других модов. Риски соседних функций: GUARD/TEAR/WARD/TRACKER зависят от event cancellation внешних модов; успешный addEffect теперь обязателен до расхода. Обычная вода/очищение хранителей/сюжетные rewards и Curios не переработаны, но ручная регрессия соседних эффектов полезна.

## Передача #276 и оценка #286/#287

| Место/сложность | Candidate ID | Обоснование | Предложенная редкость/ограничение |
|---|---|---|---|
| Ранние/средние руины | warding_charm | существующий craft/ritual ingredient, без новой силы | редкий материал; шанс только по утверждению #276 |
| Более опасные капища/магические места | thunder_stone | действующие ритуалы/крафт | редкий, сравнить с нынешним ancient_shrine; не гарантированный ранний camp |
| Средние/опасные очищенные места | fading_flame_charm, purified_water_bottle | проверенные use/effect либо существующий consumable | редкий расходник по stage, согласовать #276 |
| Любой случайный лагерь | hero tokens, flight vehicles, Sivka whistle, seals, breaths, wolf_call | сюжет/личная связь/unique progression | не раздавать без отдельного решения |
| Любая обычная структура | mor_flask | прямо диагностический предмет | не добавлять |

Ни один loot weight здесь не менялся. #241 уже содержит отдельные27таблиц/3boss pools; их не переисполняли и награды Тугарина сохранили.

Предварительный объём #286:127catalog resources +94food +19tools +48farming =288записей до фильтрации декоративных/производных; дополнительно часть153magic после исключения #284/рун/ритуалов и wearable. Это размер исходного каталога, не число доказанных багов. Делить на расходники/инструменты, материалы, затем монеты и реально существующие траты; цены/баланс отдельно утвердить.

#287:34catalog armor entries, включая clothing/пояс, которые ещё нужно сверить по реальному ArmorItem/equip slot. Проверять комплектами и уникальными вещами; новые числа/art KEEP до решения. Уже есть серверный harness экипировки #278 для переиспользования. Доказанного маленького независимого патча, который сейчас следует объединить с большим #286/#287, не установлено; предлагается общий regression harness и отдельно небольшие source-proven фиксы после их аудита. Экономику/большой ребаланс/loot с #284 не смешивать.

## Риски/откат / следующая задача

Полезный частичный пакет публикуется только в `hell` по разрешению из задания. Стабильные save IDs/components сохранены; откат в тестовом инстансе — прежний JAR из собственного backup, без перезаписи worlds. Codex ничего в PolyMC не устанавливал; backup установки не создавался, потому что установки нет.

Следующий шаг: решения по четырём Survival-путям и ручной owner checklist. #286/#287 оценены, **их реализация автоматически не начата**. При BLOCKED/NOT_RUN #284 остаётся OPEN/«В работе», не READY и не «отправлена на итоговую проверку».
