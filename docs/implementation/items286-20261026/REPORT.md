# #286 — аудит и точечные исправления, 26.10.2026

Ветка `hell`, исходный HEAD `79ff212c447080a6c333cb85d5f0e24956d89101`. Исполнение прямо разрешено владельцем в чате. #286 и #276 OPEN / IN_PROGRESS, OWNER_PENDING. Изменений `main`, новых рецептов/товаров/цен/статов и loot tables нет. Установка тестового кандидата в HELL PolyMC отдельно разрешена последним сообщением владельца; старое ограничение Issue на установку им переопределено. Запуск игры этим не разрешён.

## Что проверено

[Автоматическая классификация всего каталога](catalog-triage.tsv), [поимённый машинный индекс игровых предметов и функциональных блоков](gameplay-matrix.tsv), [полные ссылки на registration/handler/producer/consumer с номерами строк](source-index.json), [счётчики](summary.json). Это source-аудит, не live registry dump и не сотни заявлений gameplay PASS. Все 923 строки классифицированы, включая исходные 441 записи пяти групп. Развёрнутое описание ниже дано семействам механик; материалам достаточно графа применения. Декор, яйца/hidden и делегированные #278/#284/#285/#287 отделены. Обычный Item может быть нужным ингредиентом; отсутствие ПКМ не является дефектом.

| Семейство | REAL_SOURCE / игровой путь без OP | Что установлено / этапы полезности |
|---|---|---|
| 7 культур, семена, урожай | Farming/FarmingCrop, crop loot, field plants; найти растение → семена → пашня → зрелый урожай | Ранние пища/волокно; далее кухня/брожение/текстиль. Sickle сбрасывает возраст к1 и использует штатные drops; Fortune ограничен III и применяется к produce, не семенам. Fertilizer растит одну стадию в3×3; верх хмеля/куста приводится к низу. Числа KEEP |
| Ягоды/саженцы/травы | Gardens/PerennialBush, Woodlands, worldgen/loot/tags → посадка/сбор → рецепты | Сырая еда/кухня/настои во все этапы. Малина/черника сейчас plain edible Item, посадка отдельными saplings. FolkBerryItem не привязан к registry: найденный в нём дефект НЕ изменён и НЕ приписан действующей игре |
| Еда/сырьё/кухня | ModItems, KitchenII/KitchenRecipe/KitchenTile, Household, Husbandry → сырьё → стол/печь/сушилка → еда | Категория food включает несъедобное сырьё. Nutrition/saturation из registrations сохранены; при наличии helper-вызова численные поля вынесены в индекс. Кухня различает скалку/котёл, порции/миски, занятые инструменты и выход/возвраты. Ранние cooked meat/хлеб уже конкурентны; поздние блюда дают удобство, не обязательную лестницу силы |
| Напитки | Brewing/BeverageItem/BrewTile/Fermentation/BrewRules,15vat recipes → пресс/чан/бочонок → разлив → питьё | 4яблока/ягоды +3действия пресса дают сок/mash+pomace; это динамический источник. Ready2400, aged8400, spoiled12000ticks; young×.5, aged×1.2 duration. Пиво: speed/resistance/haste/absorption/nightvision/luck, mead regen, cider jump; квас/морс/сок питание2. Нагрузка1/2, первые debuffs при4, спад1/1200ticks. Не равнозначно четырём безопасным сильным buff-напиткам. Всё KEEP |
| Средства Яги | YagaServices BREWS/EXCHANGES, stage2/3 → собрать травы/трофеи/тару → услуга → употребить | Очищение снимает7перечисленных debuffs, не все эффекты/проклятия; зоркость60s, стойкость30s, бодрость60s, восстановление10s. Мазь FlightRecovery:6000ticks, repack recovery60→30ticks. Исправлена клиентская выдача/расход; серверные эффекты, длительности и цены сохранены |
| Лейка/серп/мотыга/удобрение | 4реальных handlers farming → штатные рецепты → применение | Лейка8зарядов,3×3,moisture7; теперь без расхода на уже мокром участке/отказе записи. Серп250durability, мотыга350, зачаровываемость14, износ1за изменённый блок. Это бытовое удобство, не поздний боевой power |
| Перунитовые инструменты | ModGear/ModItems, existing recipes/repair tags | Tier850uses,speed7,attack bonus2.5,enchant13,incorrect-for-iron; ремонт perunite. Между обычными iron/diamond по удобству, а не гарантированно лучше всех vanilla. Не менять статы без решения; источник редкости рассмотрен в #276 |
| Чудь | ChudTools/ChudToolTrades/ChudPerson/IndustryRules → найти действующее поселение → купить → использовать | Master pick2304uses/speed10:16purified_iron+8diamonds,FortuneII или Silk; Multitool12purified_iron+6diamonds,режимы transmuteCopy сохраняют компоненты. Pick/shovel mode не creative-only предметы с пропавшим рецептом. Lift8purified_iron+16emerald,64uses,30tick channel,CD1200,loaded radius4/arena restrictions. Lantern — inventory light path; соседние режимы/руны сохранены |
| Прочие tools | KitchenTile, BannikEntity, FishingNetBlock/Tile, HuntItems/NightingaleWhistle, RuneFoundation/RuneCraftRecipe | Скaлка/котёл — станочные инструменты. bath_broom — подношение Баннику; bath_stone — ингредиент/объект его механики. herb_pouch — ингредиент forest_charm, НЕ инвентарная сумка. Net4slots/32wear,scheduled6000–8399ticks,≥20water,шанс1/(2+4near); hunting pouch/horns имеют фактические bounded handlers, не общий template Item.use |
| Металлы/текстиль/компоненты | Recipe/tag graph, Industry MachineTile, TextileStation, WeaponCatalog | Flax_stalk НЕ бесполезен:4→4fiber после3действий мялки, далее2fiber→thread,4thread→cloth. Компоненты семи материалов идут в оружие #285; consumer существует, оружейный баланс здесь не меняется. Переплавка/очистка/ремонт/квестовые материалы проверены по действующим consumers |
| Руны/магические ингредиенты | RuneFoundation/RuneCraftRecipe/RuneRuntime/RareRuneRuntime/AdvancedRunes/ToolRuneRuntime, existing recipes/loot/reward | Blank rune — substrate без пассива; dusty/soul материалы — реальные входы; руна работает после установки/совместимости/стадии. Не выдавать каждый rune ID за самостоятельный активный предмет. Старые и сюжетные руны/ключи не ускорять сундуком; #278/#284/#287 и weapon effects сохранены |
| Функциональные блоки | См. отдельные handlers в индексе: кухня/станки/хранилища/кормушки/очаг/баня/ритуалы | Рецепт/добыча → установка → действие/хранение/производство; не смешаны с геометрическим декором. Посещение святилища/пьедестала не требует выдуманного portable recipe |

## Подтверждённые ошибки и правки

| ID | Baseline → фактическая правка | EVIDENCE / TEST / EXPECTED |
|---|---|---|
| watering_can | Считал любую пашню успешной до записи; тратил заряд на moisture7 и при setBlock=false → пропускает moisture7, считает только принятую запись | Тело production useOn компилируется против явных doubles: moisture0/6/7 × permissions × write true/false. Заряд/частицы/звук/результат лишь при изменении |
| otvar_ochishcheniya, otvar_lesnoy_zorkosti, yagin_nastoy_stoykosti, otvar_bodrosti, lesnoy_nastoy_vosstanovleniya, letuchaya_maz | Effects были server-only, но shrink и bottle return/inventory.add исполнялись также клиентом → весь finish transaction server-only; THREAD/HERBS не съедаются случайным finish callback | 6kind × stack1/3 × survival/creative × client/server × inventory full/free. Server consume1, bottle1 кроме мази; client0mutation; существующий effect остаётся |

Итог теста актуальных обработчиков: **204 checks; baseline24FAIL → after0FAIL**. [До](tests/baseline.txt), [после](tests/after.txt). Это ветви/транзакции на doubles, не NeoForge/MP integration. Два production Java-файла, 7 ID, без смены registration/components/чисел. Архивный FolkBerry не меняется.

## Экономика и предложения — НЕ реализованы

Монета `ancient_coin` имеет два подтверждённых sink: `ChudPerson.updateTrades` role0 условно добавляет **1coin→2karavai, stock8**; `traveler_charm.json` расходует1coin,1fern_flower,2linen_thread,1birch_bark. Рецепт/торговля — настоящие потребители, не название tooltip. У Чуди сохраняются offers; restock при нахождении у home и вне торговли, окна с2000/14000ticks, два сброса uses за день. В деревенских JSON trades и YagaServices coin не участвует. ChudToolTrades использует очищенное железо и diamond/emerald/amethyst, не монету. Обратной покупки coin за результат этих двух sink не найдено; денежный цикл из них не образуется. Это source вывод, не измеренная живая экономика.

`bandit_token` — расходуемое доказательство военных поручений, не вторая монета: MilitaryQuests проверяет Amount и изымает токены перед saved Rewards. `grivna/lunula` — аксессуары #278, не валюты; trophy seals/signs/ancient_sign — компоненты/ключи, не автоматически платёжное средство. Раздачу наград/старые сохранённые offers не менял.

| Вариант для approval | Сейчас → предложение | Зачем / ограничения |
|---|---|---|
| E1, предпочтительный минимальный | Монетный offer есть примерно у половины role0 merchants → сделать существующий1→2karavai доступным у всех role0; цена/stock/restock прежние | Повысить обнаружимость настоящего sink, не добавляя валютной системы/силы. Это изменение доступности, поэтому **только после согласования**. Помощь/tooltip может назвать торговца и рецепт |
| E2, только если нужен материальный sink | Новый optional role1 offer **12coin→2purification_flux, stock4** рядом с существующими, без замены8iron→2flux | Средний лагерь≈45coin даёт3покупки/6flux; ограничение stock не разрешает бесконечную закупку за один заход. Не повышать одновременно количество монет в loot. Требует утверждения цены/стадии; перед внедрением дополнительно проверить все альтернативные пути монет и flux |

## Предметы для обсуждения, а не самовольного усиления

Единственный оставшийся source-кандидат без найденного штатного producer — `ovinnik_claw`: есть рецепт `claw_knife` и старый `Runes.extra`, а дропа/рецепта когтя не найдено. **BLOCKED_NEEDS_ACQUISITION_APPROVAL**: не добавлял дроп, рецепт, замену на `ovinnik_claw→ovinnaya_zola` или цену. Рекомендуемый выбор — сначала решить, считать ли это legacy-компонентом с сохранением ID либо вернуть источник у Овинника; вероятность/замена должны быть отдельно согласованы.

19 записей с непонятной практической ценностью перечислены в индексе как PROPOSE_PURPOSE_REVIEW:

- `upyr_fang`, `grave_cloth_scrap`, `torn_burial_ribbon`, `shield_boss_fragment`, `pottery_fragment`, `old_arrowhead`, `old_buckle`, `ancient_fibula`, `ancient_comb`, `ancient_beads`, `nightingale_mark`, `voevoda_insignia`, `naboyka_tugarina`: преимущественно находки/трофеи, без найденного функционального consumer, кроме tooltip. Честный статус коллекционной находки; не включать их в оценку сильной функциональной награды. Не удалять и не менять существующий дроп Тугарина. Малое предложение — явно обозначить коллекционность текстом; новый обмен/ремесло пока не проектировать автоматически.
- `navya_ash`, `living_root`, `bone_thorn`, `grove_core`, `guardian_branch`, `soul_blight`: зарегистрированные ресурсы Навьей рощи с дропом, без найденного producer→functional-consumer продолжения. Не создавать будущие системы ради оправдания лута; отдельно выбрать коллекционный статус или конкретное уже существующее применение.

Гусли/скатерть/печати Картауса и Калина остаются блокированными решениями #284. Средства вызова, Морозко/Карачун, fragments guardians и personal reward materials имеют динамические источники; отсутствие JSON recipe само по себе не означает недоступность. #278/#284/#287/#277 не переисполнялись.

## Автоматические gates и границы

- Java21 `clean build --no-build-cache`; production2.0.2-HELL; ресурсы/JAR и source preservation — точные результаты в [source-jar.json](source-jar.json), [current-resources.json](current-resources.json), [build receipt](build-receipt.txt). `gradle test NO-SOURCE` не выдаётся за unit suite.
- 204 transaction checks; ещё70 actual BrewRules/IndustryRules checks ([rules](tests/rules.txt)). Ни один Java process этих suites не загружает Minecraft.
- Отдельно #276:631offline gates,120000complete-container samples и60000actual pure KurganPlan/Layout plans; см. [отчёт](../loot276-20261026/REPORT.md). RNG не Minecraft, Luck0, время боя/естественный поиск/MP не измерены.
- Исторический verify_brewing.py был остановлен после обнаружения запуска генератора; его resource reproducibility gate **не PASS** и не используется. Все его изменения/новые файлы восстановлены по исходному HEAD и archived baseline. Финальное byte-сравнение JAR подтверждает: все ресурсы, включая карантинное аудио, совпадают с baseline; ни один изменённый генератором asset не поставляется.
- Новых запусков Minecraft/GameTest/runClient/runServer/PolyMC: **0**. Установка JAR — файловая операция без запуска; точные source commit/installed SHA/backup в [delivery.json](delivery.json). Ручная приёмка отсутствует.

## Матрица сдачи

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| Классификация/функция/получение/consumer/stage | catalog-triage.tsv,gameplay-matrix.tsv,source-index.json; references source-only | PASS source audit |
| Исправление двух актуальных транзакций | production2files,204offline branches,baseline24FAIL→0 | PASS offline / NOT_RUN gameplay |
| Древние монеты/аналоги/малые предложения | ChudPerson,VillageTrades,ChudToolTrades,YagaServices,MilitaryQuests,recipe graph,E1/E2 выше | PASS audit; approval pending |
| Никакого нового дизайна/баланса/loot | exact source delta gate; registry/resources/classes preservation | PASS при успешном source-jar receipt |
| Доступность ovinnik_claw | source-wide producer search; recipe consumer найден | BLOCKED_NEEDS_ACQUISITION_APPROVAL |
| #276 только подготовительный аудит | отдельный REPORT,81tables,6×20k,loot hashes | PASS audit-only; distribution implementation NOT_RUN |
| Реальная работа survival/MP/save | пошаговый checklist ниже | NOT_RUN / OWNER_PENDING |

## Как проверить в Minecraft владельцу

Точная установленная сборка2.0.2-HELL и SHA/source — delivery.json; сначала проверить, что в выбранном **1.21.1 HELL** профиле один slavicmyths JAR. Сделать отдельную копию старого мира; дополнительно новый survival-мир. Старый1.16.5не использовать. Ниже обычный маршрут без OP; никаких результатов этих шагов Codex не приписывает себе.

1. Через книгу/известные структуры найти семена, вырастить хотя бы рожь и хмель/ягодный куст; скрафтить лейку по существующему рецепту. Shift+ПКМ по источнику воды →8зарядов. ПКМ по сухой пашне3×3 → влажность и минус1. Сразу повторить на полностью мокрой пашне → заряд прежний; по камню/пустой лейкой → без расхода/эффекта. На участке со смешанной влажностью расход один за успешный проход. Серп по незрелому → без урожая; по зрелому → один сбор, возраст1, износ1. Удобрение на зрелом кусте → без расхода.
2. Получить средства Яги её действующими услугами stage2/3 (травы по BREWS в YagaServices/JEI, без /give). Для зоркости:2fireweed+2juniper_berries+1brown_mushroom+1glass_bottle. Выпить одну/из стека → night vision60s, минус1настой, ровно1бутылка. Прервать питьё до конца → без расхода/тары. Повторить с полным инвентарём → одна бутылка падает/возвращается сервером, без мерцания/второго предмета. Другие отвары сверить по таблице длительностей выше; мазь не должна возвращать стеклянную бутылку.
3. Загрузить копию старого мира с зарядами лейки/средствами Яги/старыми offers. Применить по разу, сохранить, выйти и зайти → остатки/заряды без восстановления/дублирования. Проверить creative отдельно: сервер не расходует настой/не добавляет тару.
4. Найти торговца Чуди role0; монетное предложение условное — его отсутствие у одного NPC ожидаемо. Найдя1coin→2karavai, выполнить одну покупку → coin−1/karavai+2; исчерпать8uses, отойти/дать отработать расписание у дома → restock по текущим окнам. Независимо скрафтить traveler_charm из fern_flower+2linen_thread+coin+birch_bark → coin расходуется один раз. Ничего из E1/E2 пока в игре не ждать.
5. Регрессия повседневного пути: урожай→кухонный стол (скалка/котёл, миска) → еда/возврат миски;4яблока→пресс→сок→бутылка;3×работа мялки с4flax_stalk→4fiber. У инструмента Чуди после Shift-переключения сохранить damage/name/enchants, затем перезайти; обе стороны при Silk/Fortune не применяются вместе.
6. На MP повторить лейку/настой/торговлю двумя игроками: видимые эффекты/остатки согласованы, нет второй бутылки от клиентской выдачи; два одновременных покупателя не получают бесплатный результат. Ранее принятые Curios/броня/принц и #284 — короткий smoke штатным путём, их runtime в этом пакете НЕ перепроверен.

OP-диагностика при необходимости: `/data get entity @s Inventory` только для сверки components/count; команды не заменяют получения в survival. Отказ внешнего claim/mod в setBlock офлайн смоделирован, но совместимость конкретного сервера требует отдельного теста администратора.

## Риски / откат / следующая задача

В игре не проверены client prediction последней бутылки, сторонние claim-моды, latency/MP, старые save/relog, фактическое нахождение торговцев и full progression. Runtime старых #277/#278/#284/#287 не повторялся. Архивные предметы/декоративные трофеи не стали полезнее от классификации. Числа вероятностей — offline model, не измеренный Minecraft seed/spawn/clear time.

Откат: закрыть игру, вернуть сохранённый2.0.1-HELL JAR из backup delivery.json, удалить только новый2.0.2-HELL JAR; миры не переписывать. Компоненты/ID/схема saves неизменны. Source откат — отдельный revert commit по согласованию, без reset/force.

Следующая задача: владелец выбирает E1/E2, судьбу ovinnik_claw/коллекционных материалов и вариант наград #276; затем отдельно разрешает реализацию/игровую проверку. Автоматического продолжения и закрытия Issues нет.
