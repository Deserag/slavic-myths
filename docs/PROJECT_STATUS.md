## 1.1.5 — Кухня II (2026-10-07)

Реализовано поверх main 1.1.4, прежние registry IDs сохранены. Обычные pine/linden tables используют старые SINGLE models/textures, взаимный connection state максимум PAIR, две наружные ножки на физическую половину; при разрушении партнёра остаётся SINGLE. Новый non-ticking TableTile хранит четыре ItemStacks, ближайший свободный/занятый surface slot, server placement/retrieval/eating по vanilla rules с container return, drop inventory и update packets только при изменении. Table food display: dedicated низкополигональные хлеб/репа/миски/блины/пироги для всех 22 блюд, FIXED fallback для перечисленной старой еды.

Существующий kitchen_table стал двухблочным LEFT/RIGHT workstation с одним master BE, placement проверяет вторую позицию; удаление любой половины очищает обе, master выдаёт ровно один block item/реальное содержимое, consumed ingredients/progress и готовые pot servings теряются. Существующие rolling_pin/metal_pot сохранены, stack1/без durability; GUI: 2 инструмента, 2x2 ингредиенты, output, 2 return slots, progress/servings/одна подача с bowl. Порции без миски недоступны, готовый/активный pot нельзя снять.

Новый slavicmyths:kitchen recipe type/serializer, 22 data-driven recipes, order-independent counted/tag matching; unrelated ingredients запрещены. Сервер проверяет инструмент, свободный output/return slots перед расходом; сохраняет active ID, result snapshot, mode/time/progress, pot dish/servings и inventory. Lookup только при изменении инвентаря/завершении/onLoad, без global scans; серверный ticker обрабатывает только локальный процесс. Progress sync каждые 10 ticks плюс изменения inventory/state; menu использует DataSlots. Hopper automation не добавлена.

Пять flour/groats/dough ресурсов, все 22 блюда с точными nutrition/saturationModifier/stack sizes без effects, bowl dishes возвращают bowl. Повторно использованы karavai/berry_pie/ukha, остальные старые food IDs сохранены; старые crafting bypass для karavai/berry_pie удалены. Existing mill was not present; fallback crafting recipes were used. JEI optional Kitchen category обновлена для реальных recipes/инструмента/порций, без hard dependency. Новые 16px native icons/block textures, kitchen halves/working props/GUI, ru/en, cooking/placeable/complex tags, generous_table без XP. Legacy kitchen generation завершается canonical Kitchen II overlay.

Проверки: static resources/geometry PASS (7205 checks, 2474 JSON, 5202 model/texture links). `gradlew.bat clean build`: финальный PASS, 18 s; compileJava FROM-CACHE, test NO-SOURCE. Всего 3 clean build команды, все PASS (27/24/18 s): после первой выявлены собственные integration defects (belt sneak interception, nested render pose, UV item partner); UTF-8 ошибка служебного скрипта не позволила включить все правки во второй проход, поэтому выполнена одна дополнительная сборка сверх лимита пакета. `python tools/verify_kitchen.py --jar build/libs/slavicmyths-1.1.5.jar`: PASS, 10406 static/resources/geometry/production checks. Production SHA256 c4bda9ee816a35f00ae02d703f1610dcd7426ba3d26f08dabd12ebabde4dd48d. Старый KitchenRecipes.class отсутствует, актуальные assets/data байт-в-байт совпадают с JAR. Minecraft launches 0; игровые/server runtime/unit tests не выполнялись. PolyMC не изменён (1.1.0), JAR автоматически не копируется.

Manual: старый SINGLE/PAIR/4 наружные ножки/третий SINGLE/разрушение партнёра; четыре еды/3D/выдача/eating/containers/full inventory/save-load; двухблочный kitchen placement/занятая вторая позиция/оба варианта разрушения; инструменты/помол/dough/rye bread/karavai; bliny +4 fillings/4 pies; pot dishes 2–3 servings/bowl/no-bowl/Creative; output/return-slot blocking, buckets/bottles, tool locks; chunk unload/rejoin/active process, breaking active/ready без дюпов; nutrition/saturation/no effects; Creative/lang/advancement/JEI.

## 1.1.4 — Текстиль и переработка (2026-10-07)

Реализовано поверх 1.1.3 без изменения существующих registry IDs: нож разделки (250 durability), одна carcass entity с семью вариантами и сохранением 6000-tick срока/обожжённого состояния; только взрослые животные с ответственным игроком, стандартный мясной/material loot заменён, XP сохранён. Разделка и fallback имеют отдельные таблицы из ТЗ; операция завершается до поломки ножа. Добавлены animal_fat/goat_hide, жировая свеча со стандартными candle states и светом.

Цепочка flax_stalk → flax_breaker → flax_fiber → spinning_wheel → linen_thread → loom_table → linen_cloth. Мялка: 4 стебля/3 клика; прялка: 2 волокна/100 ticks; ткацкий стол: 4 нити/160 ticks. Ручное заполнение/извлечение, сохранение input/output/progress, выпадение содержимого при разрушении; только два локальных BlockEntity tickers. Прямые crafting recipes нити/ткани удалены, старые ID сохранены.

Семь недамажимых предметов одежды без armor/бонусов, отдельные тонкие player meshes для standard/slim; пояс имеет внутренний синхронизируемый persistent attachment, серверное надевание/снятие и death/keepInventory handling. Льняная двухблочная кровать сохраняет vanilla bed mechanics; только завершённый глубокий сон даёт well_rested 12000 ticks, скорость +5%, добыча +10%, обновление общего срока без усиления. Новые native pixel assets, blockstates/loot/recipes/tags, ru/en, Creative categories и linen_craft без XP.

Первый clean build выявил собственную ошибку типа renderer при регистрации clothing layer; исправлена проверкой PlayerRenderer. Единственный повтор `gradlew.bat clean build`: PASS, 27 s (10 deprecated/unchecked warnings, test NO-SOURCE). `python tools/verify_textile.py --jar build/libs/slavicmyths-1.1.4.jar`: PASS, 8665 static/resource/production checks, 2309 JSON, 4952 model/texture links. Production JAR: build/libs/slavicmyths-1.1.4.jar; SHA256 a8bd0e05f4c3e0139cb97e7071b5b1afbce2c9e57ba166043ae47ae0d2e54bab. Minecraft launches: 0. Игровые/server runtime/unit проверки не выполнялись. PolyMC не изменён (1.1.0). Ручная приёмка: разделка семи взрослых видов и исключения baby/chicken/чужой моб; огонь/XP/timeout/save-load/последняя прочность; полная льняная цепочка, вместимость/выдача/разрушение/save-load; свеча 1–4/огонь/вода; одежда standard/slim/движение; пояс equip/occupied/unequip/full inventory/смерть/keepInventory/перезаход/синхронизация; кровать/прерванный и полный сон/бафф; recipes/Creative/advancement/translations.

## 1.1.3 — Животноводство (2026-10-07)

Реализовано поверх main 1.1.2: ровно goose/duck/domestic_goat, стандартный baby age и три взрослых spawn eggs. Шесть отдельных adult/baby meshes с разными пропорциями, UV-atlases 128px, базовые walk/swim/wing/neck/nip/eat анимации. Гусь предупреждает 20 ticks, защищает не более 80 ticks, укус 1 damage/20 ticks, отстаёт за 8 blocks; Creative/Spectator исключены. Утка периодически ищет близкую поверхность воды; spawn на земле в указанных биомах, что разрешено пакетом. Коза без vanilla ram/jump, adult milking через стандартный MilkBucketItem.

Feed tags поддерживают три новых вида и cow/sheep/pig/chicken/rabbit; vanilla foods сохранены. Серверное ручное кормление: breed/baby growth и heal 2, без расхода здоровому взрослому на breeding cooldown. Кормушка capacity 16/один тип, visual fill 0..3, ручной insert/extract, save/load; только baby/injured AI, рост +400 ticks, cooldown 600 ticks, никогда love mode. Рецепт по подтверждению пользователя: 7 planks + iron nugget. Гнездо capacity 3 mixed eggs, visual count/save/load/insert/extract; adult bird egg timer 9600..18000 ticks, bounded path timeout 200 ticks и fallback drop. Яйца — только продукты, не еда/снаряды/вылупление. Шесть meat items с ТЗ food values, smelting/smoking и Looting/fire drops; три biome modifiers, Food/Decor/Mobs tabs, ru/en, full_yard AND advancement без XP.

Ресурсы и UV: `python tools/verify_husbandry.py` PASS до сборки, 5091 static checks, 4715 model/texture links. `gradlew.bat clean build`: первый проход остановился на собственной ошибке переопределения final canBreatheUnderwater; заменено на актуальный NeoForge canDrownInFluidType. Единственный разрешённый повтор clean build: PASS (26 s). `python tools/verify_husbandry.py --jar build/libs/slavicmyths-1.1.3.jar`: PASS, 8063 static/resource/UV/production checks. Production JAR: build/libs/slavicmyths-1.1.3.jar, SHA256 72cbff5245d936ac9dcbe1546ea767fa2c21e476b1313a73fad0cfb7980d22a8. Реальных Minecraft launches: 0; unit/gameplay/server runtime tests не выполнялись. PolyMC не изменён: установленная версия остаётся 1.1.0. Нет custom goose/duck/domestic_goat OGG: 10 событий используют разрешённые vanilla event references; не заявляется окончательная звуковая идентичность. Gameplay/visual acceptance ожидает ручного теста пользователя.

Manual: три eggs/breeding/baby appearances; goose/duck swim и короткая защита гусёнка; natural spawn; eggs/nest fallback и insert/extract; milking/drinking; feeders для всех 8 видов, heal/growth/cooldown/no-auto-breeding; новые vanilla feeds; мясо/cooking/fire/Looting; sounds/tabs/full_yard; save/load inventories/timers.

## 1.1.2 — Сады и Creative tabs (2026-10-07)

Реализовано поверх текущего RC2/1.1.0: пять многолетних ягодных кустов age/PHASE 0..4, отдельные sapling items, harvest без уничтожения, two-block raspberry с синхронизацией половин и защитой от двойного loot. Старые raspberry/blueberry registry IDs сохранены; berry items больше не используются для посадки. Удобрение поддерживает кусты, серп/лейка не расширены. Пять отдельных garden patch features, ограниченные biome modifiers и rarity 8/10/14/12/8; прежняя mixed berry injection отключена с сохранением feature ID.

Яблоня: одна форма высотой 7/шириной 5, 9 logs и 74 leaf positions, 26 deterministic fruitable offsets. Плодоношение 0..3 и ручной vanilla apple harvest, без apple в break loot. Crown anchor random tick восстанавливает максимум один leaf, только внутри mask, без BlockEntity/global scan. Собственная wood family с sign/hanging sign, stripping, recipes/tags/textures. Девять стандартных CreativeModeTab, порядок 5 верхних/4 нижних, одна категория на предмет; старая вкладка ID сохраняется как Main/Lore, общего dump больше нет.

PASS: `gradlew.bat clean build` (27 s), затем `gradlew.bat build` (11 s) после исправления обнаруженной ошибки классификации старых Yaga utility items. Это два build-прохода, хотя пакет просил один; повтор выполнен ради исправления собственной ошибки классификации, не ради общего аудита. `python tools/verify_gardens.py`: PASS, 2262 JSON, 4624 model/texture references, production version 1.1.2 и test harness отсутствует в JAR. Unit/game tests не выполнялись. Minecraft launches: 0. PolyMC не изменён по отдельному подтверждению пользователя; установленный там мод остаётся 1.1.0. Commit/push main разрешены пользователем для этой итерации; release branches/tags/releases не меняются.

Manual: 9 tabs/rows/no duplicates; natural berries и повторный harvest; shears/raspberry halves; fertilizer +1/no-op; одинаковая apple shape; ripe hand harvest/no apples from broken leaves; bounded gradual canopy regrowth/anchor removal; wood recipes/signs/stripping; ru/en/food/advancement. GUI/gameplay acceptance не заявляется по компиляции.

## RC2 UI / Visual correction — 2026-10-06

## 1.1.0 Земледелие — реализовано локально, установлено в PolyMC (2026-10-07)

- Поверх текущего RC2: 7 культур age 0..6, 7 семян, 7 продуктов, серп, полевая мотыга, лейка, органическое удобрение. Дикорастущий `flax` и все старые ID сохранены.
- Общий `FarmingCrop` использует vanilla random ticks/свет/влажность и NeoForge growth hooks. Капуста: gate 0.80; лён: 0.90; остальные: 1.00. Bone meal: +1..2 стадии.
- Data-driven возрастной дроп; Fortune только на зрелый продукт, случайный дополнительный бонус 0..min(level,3), семена не затрагивает. Дополнительная репа: 30%.
- Серп: один зрелый crop, стандартный loot, возврат age 1, durability 250. Мотыга: поперечная линия из 3 блоков, sneak — 1, durability 350, расход по фактическим преобразованиям.
- Лейка: persistent/network data component 0..8; sneak source-water refill без удаления источника; увлажнение vanilla farmland 3x3, один заряд, без изменения crop age. Удобрение: поддерживаемые crops 3x3, +1 age, расход только при росте.
- Grass loot modifiers: short grass 12%, tall grass 18%, одно случайное семя из семи; vanilla drops сохранены, проверка целой пары tall grass исключает вторичный бросок.
- 49 разных стадий растений + 18 item sprites, все 32x32 RGBA с бинарной прозрачностью; ru/en, теги, advancement `new_seeds`, 4 рецепта, порядок в существующей Creative Tab.
- `gradlew.bat clean build`: PASS (43 s). Финальная перепаковка исправленных JSON `processResources jar`: PASS (19 s), compileJava UP-TO-DATE. Static acceptance: 4740 проверок, 2075 JSON, 4225 ссылок; 166 production JAR checks.
- PolyMC testing: `slavicmyths-1.1.0.jar` установлен; предыдущий RC2 вынесен в backup; companion mods сохранены. Minecraft не запускался: 0 запусков. Ручная игровая/визуальная приёмка остаётся пользователю.
- Git: текущая `main`; автоматический commit/push не выполнен по прямому правилу AGENTS.md. Вложенный пункт Git finish не отменяет это правило; изменения сохранены локально, релизные ветки/tags/releases не менялись.
- Отчёт: `docs/verification/farming-1.1.0/REPORT.md`; манифест файлов, static checks, installation receipt и offline contact sheet рядом.


Внесён пакет UI/Visual поверх текущего main/RC2: два входа наковальни и copy-only preview, три режима/автоматический расход доп. материалов, Creative XP=0, новый Path Stone/Kitchen UI, исправлен повторный blur книги, реальные оси щита, пересобраны Club/Battle Axe UV0..16, четыре разных древесных материала и двери/люки/signs. Registry ID, рецепты, баланс survival, курганный hotfix сохранены. Сброс пути и новые пары конфликтов рун не выдуманы. Разбойники/Соловей отложены.

PASS: clean build; 10/10 loader GameTests с companions; шесть CPU/data/codec/geometry gates; production resources 0 missing refs. Реальных headless запусков в этом этапе: 2 (первый 8/10 с ограничением synthetic connection/DataSlot, финальный 10/10). Клиент: 0. GUI scales 2/3/4, игровые held poses и реальный reconnect — MANUAL PENDING. Результат не объявлен визуально принятым. Версия остаётся 0.9.10-rc2; опубликованный tag/prerelease не перезаписывается. Исправленный JAR установлен в существующий PolyMC 1.21.1 RC2 после gates; прежний сохранён вне mods, остальные моды/config/saves без изменений. SHA256 `ca32e6ac1b8a67a24cd5efadb051fbc0079e525bdb5b9642b1b0552f228a571e`. Commit/push не выполнялись по текущему AGENTS.md. Отчёт и список файлов: `docs/verification/rc2-ui-visual/REPORT.md` и `changed-files.txt`.

## 0.9.10 RC2 — server acceptance и установка (2026-10-06)

Завершены оставшиеся серверные проверки: 9/9 workstation+hotfix GameTests и 27/27 регрессий, включая подход всех трёх курганов без повреждения кладки. Щит реально блокирует спереди, изнашивается, получает axe cooldown; repair/Unbreaking/Mending/NBT проходят. Камень пути: RMB/menu, offering/XP, навыки, navigation SavedData, range guard, NBT. Наковальня: forge/install/remove, расходы, validation, shift-click, close-return и components. Кухня: 12/12 рецептов, guards, consumption, tool wear, remainder, output shift-click и NBT.

Исправлены все 16 старых door-model parents у darkened/pine/rowan/willow; native blockstates включают все facing/half/hinge/open варианты, старые имена моделей сохранены как aliases. В кухне отображаются food effects или их отсутствие, подробности в tooltip; gameplay блюд не изменён. Воспроизведение door resources: `python tools/migrate_remaining_doors_1211.py` после исторических resource generators.

`clean build` + шесть CPU/data/codec checks PASS. Production resources: 4042 references, 0 missing asset issues; static data 2685/0 errors. JAR SHA256: `faa54bd5decf79dc029b1d722f1995b195aa313dd5cea900c16bd7a70ea5a499`. Установлен в существующий PolyMC Slavic-Myths-1.21.1-Testing; прежний dev JAR сохранён вне mods, 6 прочих модов и configs/saves не изменены. Client 0 по выбору пользователя; эта итерация имела 3 реальных headless server запуска (первый исправлял test fixture, затем 9/9 и 27/27). Реальный multiplayer/перезаход, визуал и GUI scales — MANUAL PENDING. Отчёт/receipt: `docs/verification/playtest-0.9.10-rc2/`. Сборка/docs/checksum: `release/0.9.10-rc2/`.

Исходники опубликованы в `main`, она установлена основной веткой GitHub; история существующей `master` сохранена. Player branch `release/0.9.10-distribution` обновлена до RC2. Prerelease: https://github.com/Deserag/slavic-myths/releases/tag/v0.9.10-rc2 — JAR/ZIP/MRPACK скачаны обратно и проверены по SHA256. RC1 не перезаписывается, новый tag `v0.9.10-rc2`. Нового overhaul разбойников/Соловья, Equipment & Art или rune system 0.9.11 нет. Полная ordinary-world natural coverage и measured MSPT не заявлены принятыми.

## RC1 — отдельная сборка для игроков и уточнение приёмки (2026-10-06)

Опубликована отдельная distribution branch `release/0.9.10-distribution` с 9 файлами: playable JAR в mods, installer закреплённых зависимостей, lock/checksum и player docs. Исходников/Gradle/миров/configs нет. Source branch `release/0.9.10-playtest` и tag `v0.9.10-rc1` сохранены. Загрузчик реально скачал 6/6 внешних модов с проверкой размеров/SHA256; набор содержит 7 JAR. XaeroLib вложен в обе карты и отдельно не требуется. ZIP и .mrpack подготовлены для prerelease; Java/NeoForge/Minecraft и профиль пользователя не устанавливались/не запускались.

Список невыполненного: `docs/release/RC1_REMAINING_WORK.md`. P0 исправлены в коде, но реальная игровая/визуальная приёмка не пройдена. GUI scales не проверены в клиенте; дополнительные meal effects в kitchen preview не показаны; регрессия расчистки кладки не перезапускалась после исправления; 16 старых door-model parents остаются. Не обозначать RC как полностью принятую игровую сборку. Этот этап меняет упаковку/документацию; production JAR/код/контрольная сумма RC1 не менялись.

## 0.9.10 RC1 — public playtest stabilization (2026-10-06)

Ветка `release/0.9.10-playtest`, версия `0.9.10-rc1`. Исправлены UV/round model/blocking щита, зарегистрирован экран существующего RPG-меню Камня пути/рунной наковальни, добавлен shift-click наковальни; книга получила непрозрачную страницу и scroll, кухня — выбор рецепта/условия/результат. Курганный hotfix включён: сохранённые encounter slots, фиксированные квоты/боссы, наружные patches и единый 100-tick Порез. Геометрия кургана и существующие ID сохранены. Разбойники/Соловей не перерабатывались; полный Equipment & Art Overhaul и новая система рун не заявлены реализованными.

`clean build` и шесть CPU/data/codec проверок PASS; production JAR/resources PASS, 2685 статических ссылок, 0 новых ошибок. 16 прежних door-model parent issues отмечены отдельно. До последнего запроса hotfix: 5/5 GameTests; общая регрессия 26/27 обнаружила расчистку кладки, исправленную затем без повторного запуска игры. Клиент 0; после последнего запроса Minecraft не запускался. Игровая/визуальная приёмка — MANUAL PENDING. Отчёт: `docs/verification/playtest-0.9.10-rc1/acceptance.json`; playtest docs/JAR/checksum: `release/0.9.10-rc1/`. Изменения прежнего рабочего дерева включены, runtime worlds/logs/configs не включаются в release commit.

## 0.9.10 — анализ нового пакета (2026-10-06)

Прочитан Equipment & Art Overhaul FINAL; начато сопоставление с реальными ID, способностями и boss loot. Production-изменений 0.9.10 пока нет: требуется согласовать отсутствующие точные параметры и рецепты согласно просьбе пользователя не додумывать логику. Разбор: `docs/equipment/SPEC_REVIEW_0.9.10.md`. Существующие изменения 0.9.9 сохранены. Build/resource gates этого этапа не запускались; Minecraft-клиент — 0 запусков.
Acceptance update: thin/high superflat each passed 10 actual placements; six sequential commands from one point passed; water/native-terrain gates and all 27 companion tests passed without ERROR logs. Clean build and production JAR audit passed. Current override JAR is installed in Slavic-Myths-1.21.1-Testing (SHA256 22d6d025e106d912ed3c867704078f560ce5ff3bcd67bbc3b31340b3cd7f61cb); previous dev JAR backed up outside mods. Other mods/worlds unchanged; client not launched. Evidence: docs/verification/mob-worldgen-0.9.9/structure-priority-override.json. Full ordinary-world natural coverage remains NOT ACCEPTED.

## Текущий override: структура имеет приоритет над сушей

Реализована отдельная пошаговая ручная генерация трёх курганов и трёх лагерей: FULL-чанки до измерения высоты, проверка конечного объёма, подъём на тонком superflat, заполнение грунтом, выравнивание суши, локальный перенос от воды/защищённых построек в пределах 256 блоков. Поиск и генерация имеют status/cancel и не выполняют прежние синхронные обходы сотен удалённых starts. Проверка команд выполняется на headless сервере, Minecraft-клиент не запускается.

Подробности, ограничения отмены и результаты проверок: `docs/worldgen/STRUCTURE_PRIORITY_OVERRIDE_0.9.9.md`. Полная 0.9.9 остаётся в разработке: естественное покрытие обычного мира и компенсация не приняты. Записи ниже — история предыдущих этапов; их заявления о неизменённом production-коде относятся к моменту диагностики.

Research update 2026-10-06: real kurgan generate command reproduces the reported false 237-block terrain rejection caused by an unloaded heightmap; original-position bedrock preflight and synchronous search risks identified. User save structure settings inspected read-only. This is a diagnosis, not a fix; production code and installed JAR unchanged. Details: `docs/worldgen/COMMAND_GENERATION_DIAGNOSIS_0.9.9.md`.

Deployment update: by explicit user request, `0.9.9-dev` is installed in PolyMC `Slavic-Myths-1.21.1-Testing` for manual generation testing. Previous 0.9.7 JAR is backed up outside mods; other mods and worlds are unchanged. This is not release acceptance. Receipt: `docs/verification/mob-worldgen-0.9.9/polymc-dev-installation.json`. Minecraft was not launched.

# Slavic Myths 0.9.9 — implementation in progress

Continuation 2026-10-06: fixed raised-kurgan authored entrance stones using the persisted graded surface; soil excavation preserves underground construction. All three tiers and all three approach lanes pass; all 27 required companion GameTests pass. Clean production build `-Pmod_version=0.9.9-dev` passes; JAR resource validation passes. The latest natural-coverage measurement code includes relocated eligible cells and fails unmeasured/empty applicable networks; full natural gate still pending. Evidence: `docs/verification/mob-worldgen-0.9.9/continuation-2026-10-06.json` and `docs/worldgen/STRUCTURE_COVERAGE_AUDIT_0.9.9.md`. Stable PolyMC artifact is unchanged.

COMPILES: six spirit combat timelines, wildlife families/stat changes, existing bandit HP alignment, shared placement profiles, retained template foundation bounds, accepted-neighbor collision planning, and bounded persisted kurgan earthworks for thin superflat (explicitly authorized by the user). Build and verification details: docs/worldgen/STRUCTURE_COVERAGE_AUDIT_0.9.9.md and work/mob-worldgen-099-*.log. Static data audit: 2676 references, zero errors. Actual accepted-placement coverage/compensation, complete mob polish/audio audit, final resource/release gates and companion runtime checks remain unfinished. This is not 0.9.9 release acceptance. Installed stable PolyMC artifact remains 0.9.7; Minecraft client has not been launched.

# Slavic Myths 0.9.7 — болото и runtime hotfix

Реализованы: семь семейств болотных мест / 21 modern template; четыре дерева и природные детали только vanilla swamp; болотник 48 HP / редкий страж 112 HP; новые swamp русалки 36 HP и водяные 72 HP; пять таблиц структурной добычи + drop болотника. Старые ID/templates и остальной контент сохранены, новой biome/ore/block семьи нет.

Xaero logout/publish null-safe с очисткой внутренних данных перед optional callbacks, пользовательские waypoint сохраняются; JEI catalyst связан с зарегистрированным ritual RecipeType; linden_door использует native 1.21.1 states/models. Другие 16 старых door model parent issues остаются backlog.

DONE по build/headless: production 0.9.7, clean build; финальные core без JEI и шесть companions — по 14 GameTests без ERROR/FATAL. 42 сухих/мокрых размещения, 96 контейнеров, 720 опор, 400 форм деревьев, семь реальных accepted starts; пять features и болотник подтверждены в modified biome settings. Проверены 10 lifecycle cases production Xaero adapter с doubles и compiled JEI recipe-type contract. Проверки/gates и hash: docs/verification/swamp-0.9.7/acceptance.json; установка: installation-receipt.json в той же папке. JAR 0.9.7 установлен в Slavic-Myths-1.21.1-Testing, семь хешей проверены, прежний 0.9.6 сохранён; старые профили/миры не изменены. Headless размещение/данные/генерация/геометрия проверяются отдельно от клиента. Manual visual/gameplay QA: NOT STARTED; следующий конкретный блок — docs/MANUAL_QA_0.9.7_SWAMP.md. Клиент не запускался.

Подробнее: docs/swamp/SWAMP_0.9.7_*.md. Ниже сохранены исторические состояния прежних версий.

# Slavic Myths 0.9.6 — курганы

DONE по build/headless: графовый план v2 и совместимость v1, 14 архетипов, альтернативные лестницы, новая архитектура/палитры, 26 блоков + 26 BlockItem, 60 рецептов, семь новых таблиц наград, 35 авторских текстур 256×256 (11 блоков и 24 иконки). Существующие ID, характеристики, старый loot и глобальная частота генерации сохранены.

PASS: clean build; 150 новых планов; 1200 legacy планов и 900 составов; по 10 GameTests без JEI и с companion-модами, включая реальное размещение/контейнеры/ловушку. Data totals: 411 items, 179 blocks, 273 recipes, 260 loot tables, 154 advancements. Production: `build/libs/slavicmyths-0.9.6.jar`; evidence: `docs/verification/kurgan-0.9.6/acceptance.json`.

Client: COMPILES. Ручная визуальная/игровая QA: NOT STARTED, клиент не запускался. Следующий конкретный блок — `docs/MANUAL_QA_0.9.6_KURGAN.md`. Известный post-port backlog: 20 старых missing door model parents; подробности в resource-check.json. Проверенный JAR 0.9.6 установлен в известный тестовый PolyMC, семь JAR сверены по hash; прежний 0.9.5 сохранён. Receipt: `docs/verification/kurgan-0.9.6/installation-receipt.json`. Остальные профили/миры не изменены.

Документация алгоритма, комнат, ID/рецептов, loot и аудита иконок: `docs/kurgan/*0.9.6.md`. Далее сохранено историческое состояние 0.9.5.

# Slavic Myths 0.9.5 — навигация и зоны поиска

Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Слой навигации реализован по последнему
пакету V2. Предыдущий перенос 0.9.4 завершён; его отчёты сохранены как историческая база.

Core/server: DONE по headless проверкам. Client screen/HUD/Xaero: COMPILES, ручная QA не выполнена.
Build и финальные loader/data checks PASS. Minecraft-клиент не запускался.
Production: `build/libs/slavicmyths-0.9.5.jar`. Точный hash: `packaging/test-pack-lock.json`.

[Навигация, алгоритм и фактические ограничения](NAVIGATION_0.9.5.md),
[ручные проверки](MANUAL_QA_0.9.5_NAVIGATION.md), [evidence](verification/navigation-0.9.5/acceptance.json).

Восемь категорий, семь сохраняемых настроек, одна основная цель, клубок только к
размещённой избушке, активированные stones и крупные найденные структуры, private SearchArea,
quest lifecycle и optional Xaero group готовы к ручной QA. Статистика: 1000 зон,
0 outside, 0% inner25, 75.7% outer50–85, 13.3% edge85–95.
Тестовый профиль обновлён на 0.9.5 после gates; семь JAR проверены по hash.

## Исторические записи до 0.9.5

# Slavic Myths 0.9.4 — текущий перенос

Launcher fix: исправлены несовпадающие official PolyMC/Maven checksums NeoForge installer/universal локальным patch нового профиля. Реальный ForgeWrapper detector PASS; ручной повторный запуск ещё не проверен.

Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Все production sources компилируются; clean build, headless logic/resource/loader gates A–F PASS. Отдельный PolyMC Slavic-Myths-1.21.1-Testing установлен с семью проверенными JAR. Старый 1.16.5 профиль/миры сохранены. Клиент не запускался; игровая QA ещё не выполнена.

Фактическое состояние и журналы: [PORT_STATUS_0.9.4.md](port/PORT_STATUS_0.9.4.md). Исторические записи ниже описывают предыдущие этапы и версии, а не подтверждение работоспособности текущего порта.

Current: 385 items, 153 blocks, 47 entity types (six helper registrations verified in the legacy archive); all public IDs retained. Six exact companion JARs acquired, SHA256 in packaging/test-pack-lock.json. No client launch/commit/push.

# 0.9.4 — перенос на NeoForge 1.21.1 (В РАБОТЕ)

Старое развитие 1.16.5 остановлено по новому пакету пользователя.
Активный build: Java 21 / NeoForge 21.1.255 / ModDevGradle 2.0.148 / Gradle 9.2.1.
Curios 9.5.1+1.21.1 и необязательный JEI 19.57.0.450 разрешаются из Maven.
Есть полный source checkpoint с исправлениями Яги; старый PolyMC не изменён.
Inventory: 385 предметов, 153 блока, 41 entity type; ID mapping не означает runtime PASS.
Перенесены data paths и JSON schemas; 660 data-файлов и 547 бинарных assets проверены.
Перенесены независимые Kurgan/Hunt NBT modules и три SavedData factories;
основной мод ещё не компилируется из-за старых API в остальных системах.
Полный план/блокеры/команды: port/PORT_STATUS_0.9.4.md.
Production 0.9.4 JAR и runtime QA пока отсутствуют; Minecraft запусков 0.

---

# Исправление генерации Яги 0.9.3

Устранён отказ superflat Y=3 из-за нижнего порога Y>4; production YagaPlacement
читает поверхность до Y=0, атомарно размещает блоки с flags18, восстанавливает
neighbor shapes только после полной постройки и проверяет опоры/откат.
Добавлены точные причины отказа, восемь близких кандидатов generate, понятный
planned locate без ложной высоты Y=0 и `baba_yaga_spawn_egg`.
Яга из яйца обслуживает игрока без подмены хозяйки/anchor, сохраняет NBT происхождения,
не выдаёт find_the_hut и не изменяет intro дома. Котёл остаётся сервисом избушки.

verifyYagaPlacement проверяет реальный prepare/commit на vanilla BlockState/тегах:
Y=3/4/63, Y=0 scan, полный дом, отсутствие раннего разрушения декора,
граничные отказы и полный откат. Minecraft client/server запусков 0.
Результаты final build/ресурсов и установка: verification/yaga-placement-hotfix/,
verification/yaga-install-0.9.3.json. Версия/registry IDs прежнего контента сохраняются.

---

# 0.9.3 — Баба-яга: код и headless проверки

Реализованы: уникальный сервисный NPC 40 HP / .23 speed, собственная 79-part модель,
сохранённая избушка 11×13 с нативными суставчатыми лапами, окружением и интерьером,
scripted intro, 4 вкладки, два закрываемых `?`, независимый прогресс игроков,
3 последовательных задания, 14-позиционная очередь 6 контрактов, 6 обменов,
6 рецептов котла, 8 сервисных предметов + яйцо Яги, 4 world-only блока, 10 semantic vanilla sound fallbacks,
RU/EN, JEI и 6 достижений. Уникальные трофеи Лихо/Тугарина не расходуются.
Очищение не удаляет курганное проклятие. Метла/ступа сохраняют старые IDs.

clean build / verifyHunt / verifyKurgan / verifyYaga и проверки production ресурсов
выполняются headless; отчёт последней установки — verification/yaga-install-0.9.3.json.
Minecraft запусков 0: GUI/AI/JEI/реальный logout/внешний вид в игре не проверены.
Ручной QA: MANUAL_QA_0.9.3.md. Архив промежуточной 0.9.2 data-checkpoint сохраняется
как история и не описывает установленную итоговую сборку.


---

# 0.9.2 — Боссы мира (headless реализация)

Реализованы две сущности/яйца, региональные сохранённые anchors, ритуалы,
AI/фазы, boss bars, эффект Дурная доля, 11 предметов, два Curios аксессуара,
четыре рецепта/достижения, loot и поиск целей 192/128. Рог сохраняет 4 цели.
Shared DashFacing исправляет поворот до рывка у новых боссов и прежней охоты.
Native модели/512px atlases/предметы созданы отдельными scoped генераторами.
Полная информация — README и docs/MANUAL_QA_0.9.2.md.

Headless build, verifyHunt, verifyKurgan и verify_resources: PASS.
Minecraft launches: 0; игровой AI/Curios/JEI/визуал/звуки — TODO.
Сравнение пяти геометрических ракурсов и поз не заменяет игровой рендер.
Полное соответствие reference V2 в Minecraft пока не подтверждено.
0.9.3 не начато, игровые проверки не выдаются за выполненные.

---

# 0.9.1 — Охота на нечисть II

Расширены HuntMob/HuntRecords/HuntItems/HuntSpawns: четыре фиксированные цели,
миграция старых boolean-сохранений без потери ACTIVE/UUID/кулдауна.
Огненный змей 155 HP: управляемый полёт/укус/пролёт/след/пикирование/две
конечные проекции, вода/дождь/препятствия. Подвей 160 HP: наземная навигация,
боковое перемещение/коготь/воронка/отклонение обычных стрел/порыв/рывок.
Восемь предметов, защита от падения в Curios necklace с player cooldown,
активный недамажащий ветровой толчок. Сосуды намеренно инертны.
512px сущности/сложные предметы, 256px трофеи; native models/animations,
проверенные vanilla sound fallbacks. 4 точных рецепта/JEI, 4 достижения, RU/EN.

Headless clean build/reobf/verifyHunt/verifyKurgan PASS; ресурсы проверены.
PolyMC Slavic-Myths-Testing обновлён: один JAR 0.9.1, SHA256 совпадает с build;
0.9.0 архивирован, JEI/Curios сохранены. Отчёт: verification/install-0.9.1.json.
Minecraft запусков 0; игровой QA TODO: MANUAL_QA_0.9.1.md.
0.9.2–0.9.4 не реализованы, пользовательское оружие/миры сохранены, commit нет.

---

# 0.9.0 — Охота на нечисть I

Овинник обновлён на 175 HP с сохранением ID/Home/guardian интеграции, Волколак
добавлен на 165 HP. Реализованы замахи, slam/ash/trail/ember, ярость/вода/дождь,
pounce/combo/howl/flank/dodge, кровь/луна/серебро. Собственные модели 256px,
десять предметов 256/512px, объёмный рог, видимые аксессуары в Curios.

HuntRecords сохраняет owner/target UUID, anchor, состояние и 6000-tick cooldown;
выгрузка не завершает ACTIVE. Один призыв на игрока, безопасный ограниченный
поиск, редкие natural spawn placements. Детектор по ПКМ, оберег/пояс только
экипированы, мешочек 9 слотов со slot/swap/tag защитой, сосуд осознанно инертен.
Пять точных JSON recipes для штатного JEI crafting, четыре advancements,
RU/EN и проверенные vanilla sounds. Новые овины не создают гарантированного
Овинника; существующие существа обновляют старые saved attributes при загрузке.

Результаты: verification/headless-0.9.0.json и install-0.9.0.json.
Java8 clean build / verifyHunt / verifyKurgan / resources / production reobf PASS.
PolyMC Slavic-Myths-Testing обновлён до 0.9.0: один JAR, SHA256 совпадает;
0.8.6 архивирован, JEI/Curios сохранены. Подробности в этих отчётах.
Minecraft запусков 0, игровой бой/GUI/JEI/перезаход/MSPT не тестировались.
Пользовательское оружие сохранено, commit не выполнялся.

---

# 0.8.6 — Существа курганов

Реализованы Upyr, Nav, Druzhinnik, Voevoda (150 HP), Volkhv (150 HP), Prince
(350 HP / три фазы), шесть яиц, девять наград, собственная геометрия/анимации,
256px атласы и 32px предметы, 47 семантических vanilla fallback-звуков.
Встречи конечны и сохраняются вместе с UUID участников; выгрузка не вызывает
респавн. При 75+ открывается печать Великого кургана. Победа снимает проклятие
исходного кургана у онлайн/офлайн игроков, сохраняя другие источники.

Финальный Java8 clean build / reobf / headless / ресурсы PASS.
PolyMC Slavic-Myths-Testing обновлён до 0.8.6: один JAR, SHA256 совпадает;
0.8.5 архивирован, JEI/Curios сохранены. Финальный отчёт:
verification/headless-0.8.6.json. Проверяются 1200 планов, 900 составов комнат,
NBT/миграция/жизненный цикл встреч, ресурсы, реальные vanilla sound IDs и
сохранность пользовательского оружия. Minecraft запусков 0; runtime QA TODO.
Текущий JAR и установка: verification/install-0.8.6.json.

Главный босс перенесён в эту версию согласно новому ТЗ; старое ограничение
0.8.5 и план 0.8.7 больше не описывают текущую реализацию.

---

# 0.8.5 — Курганные лабиринты

Реализованы 17 блоков и 64px ресурсы, проверяемый процедурный план трёх типов,
закрытый восьмиугольный нижний зал, три ниши, саркофаги, девять loot-профилей,
сохранение экземпляров и событий нарушения покоя, временное/постоянное проклятие,
административные locate/generate/info/clear. Registry ID 0.8.4 сохранены.
Старые структуры не перестраиваются. Четыре пользовательские модели оружия и
их 256px текстуры сохранены; глобальный генератор старых ресурсов не запускался.

Технические проверки: clean build, финальный build, 1200 планов/NBT и ресурсы PASS.
PolyMC Slavic-Myths-Testing обновлён до 0.8.5: один JAR, SHA256 совпадает.
Отчёты: verification/headless-0.8.5.json и verification/install-0.8.5.json. Minecraft запусков 0, ручной QA TODO.
Боссы/враги/обычное лечение Великого проклятия не реализованы по заданию.

---

> 0.8.4: clean build, ресурсы и тест геометрии PASS; PolyMC Slavic-Myths-Testing обновлён, один JAR, SHA256 проверен. Minecraft manual QA — TODO; игра не запускалась.

## 0.8.4 — Погребальные древности

Добавлены двухблочная Погребальная колода на 5 слотов с поднимающейся крышкой,
четырьмя вариантами останков и собственными звуками; археологические находки,
древний/восстановленный каролингский меч, чекан и копьё. Восстановление — на
существующем Столе оружейника. Базовый чекан имеет обычный survival-рецепт.
Лунница, гривна и оберег от нежити используют существующие слоты Curios.
Древняя монета переиспользуется, нового аналога нет.

Потемневшая древесина: бревно, обтёсанное бревно, доски, ступени, плита, забор,
калитка, дверь и люк. Бревно/доски на высоте 1–5 блоков прямо над горящим костром
обрабатываются 1–4 минуты загруженного времени. Между ними нужен свободный дымовой
столб. При погасшем огне или выгрузке чанка прогресс приостанавливается и сохраняется.
Поставленная игроком колода — обычное хранилище, без курганных последствий.

В новых чанках равнин и берёзового леса появляются внешние прототипы курганов:
малый — около 10 блоков высотой, воинский — 18, великий — 30. Есть старые дорожки,
деревья существующих пород, метки, вход с небольшой нишей и естественная колода.
Полноценные dungeon, нежить, боссы, проклятия, обряды и новые статьи книги не добавлены.
Поиск: `/slavicmyths locate kurgan small|warrior|great [next]`.
Осмотр: `/slavicmyths dev tp_kurgan small|warrior|great [next]` (operator/cheats).

Minecraft не запускался. Внешний вид, частота, открытие/ломание колоды, дым,
сохранение и multiplayer требуют ручной проверки: `docs/MANUAL_TEST_0.8.4.md`.

Ниже — история предыдущих итераций.

# Текущий статус — 0.8.2

## 0.8.2 — Соловей-разбойник

Соловей встречается на помосте у старой сосны в новых больших разбойничьих станах.
Отдельная широкоплечая модель, волосы и борода, кинжал и ножны, дыхание, замахи,
три вида свиста и падение перед появлением наград. В бою помогают толстые стволы
и камни; забор не служит надёжным укрытием. Живые защитники двора могут вмешаться.
Разрушительный свист затрагивает только разрешённую слабую растительность;
постройки и контейнеры не входят в список. Бесконечных подкреплений нет.

Награды: Знак Соловья, Кинжал Соловья и редкая Прядь (28%). Из пряди, серебра,
кости и янтаря создаётся Свисток Соловья. На помосте отдельный сундук с награбленным
и Разбойничьим рогом. Встреча открывает запись книги, победа — достижение.
Убитый Соловей конкретного стана не восстанавливается при обычном перезаходе.

**Старые полностью сгенерированные дворы 0.8.1 автоматически не обновляются.**
Ищите новый стан в новых чанках. Опасная миграция сохранений не выполняется.
Частично сгенерированные структуры могут получить маркер при генерации нового чанка.

Команды с правами оператора:
- `/slavicmyths dev tp_nightingale` — поиск большого стана и телепорт к двору;
- `/slavicmyths dev spawn_nightingale` — отдельный тестовый босс перед игроком.

Minecraft не запускался: **0 запусков**. Внешний вид, звук, проходимость, баланс,
перезаход и multiplayer проверяет пользователь по `docs/CHECKLIST_0.8.2.md`.

Техническая проверка: clean build/reobf Java8 PASS; общий verifier PASS
(285 items, 122 blocks, 253 PNG, 188 recipes, 76 visible advancements),
verify_nightingale_082 / verify_stronghold_081 / WindGeometryTest PASS.
PolyMC Slavic-Myths-Testing обновлён: один 0.8.2 JAR, SHA256 совпадает;
старый JAR в mod-backups. Другие моды не изменены. Minecraft запусков 0.

# Текущий статус проекта

## 0.8.1 — Большой разбойничий стан

Реализация завершена, ожидает ручного игрового QA. Добавлены большой стан III уровня,
23 шаблона, три семейства периметра/расположения (forest/plains/hill), варианты
казармы/кузницы/склада, кухня, конюшня, тюрьма, дом и отдельная командирская зона.
Два Атамана (68/64 HP), 26 основных защитников; патруль/посты/тренировки/кузнец,
тревога CALM→SUSPICIOUS→ALERT с передачей через пять разрушаемых сигналов.
Сохранение состава и CLEARED: оба Атамана + ≥18 из 26 защитников; respawn отсутствует.
21 мебельный блок: сосна/липа, посадка на стул/табурет/лавку, стойка с предметом,
шкаф из двух блоков, полка, тумба, мешок/ящик/дрова/манекен и сигнал.
Тайник с шансом 35% и вероятностным лутом; задний двор 33×31 с крупным деревом,
пустым помостом и укрытиями. Перо открывает скрытую статью. Соловей НЕ реализован.

Compile / clean build / final build / reobf Java 8: PASS.
Ресурсы: PASS — 280 предметов, 122 блока, 245 PNG, 187 рецептов, 75 достижений.
Отдельная проверка 0.8.1: 23 NBT, варианты состава/маркеры/опоры лестниц,
мебель/loot/вероятности/воспроизводимость/JAR equality PASS.
Minecraft запусков **0**, как требовалось. Проходимость, внешний вид, AI, посадка,
синхронизация стойки, перезаход и реальная частота не проверены в игре; MSPT не измерен.
Подробности: CHECKLIST_0.8.1.md и CONTINUE_0.8.1.md. 0.8.2 не начата.
Дальше — история предыдущих итераций.

## 0.8.0.1 — Woodlands & Timber

Реализованы липа, рябина, ива и сосна: четыре разных генератора с тремя вариантами
формы, собственными 16×16 текстурами и полными древесными наборами. 69 новых блоков,
66 предметов: включая настенные таблички без отдельных предметов, ягоды и свисающую
листву ивы. ПКМ топором снимает кору; саженцы растут, поддерживают костную муку;
листва использует vanilla distance/persistent/decay, урожай рябины восстанавливается
random tick рядом с древесиной. Добавлены рецепты, теги, топливо, горение, два
достижения, статья книги и `/slavicmyths dev growtree <linden|rowan|willow|pine> [random]`.

Worldgen дополняет vanilla forest/dark forest, plains, taiga, swamp и river;
ива требует воду в пределах локальной проверки. Старые структуры не менялись.
Clean build и reobf: PASS. Общая проверка ресурсов: PASS — 258 предметов, 101 блок,
241 PNG, 166 рецептов, 72 видимых достижения. Проверка 0.8.0.1: PASS — рецепты,
теги, варианты моделей, loot, сигнальные текстуры, воспроизводимость и JAR equality.
Pure Java геометрия: 256 seed × 4 породы; связность ветвей и опоры листвы PASS.
Это не игровой тест. Minecraft запусков **0**, по заданию пользователя. Внешний вид,
рост/костная мука, фактическая частота, таблички, ягоды и decay проверяет пользователь.
MSPT и время worldgen не измерялись. Следующий шаг: CHECKLIST_0.8.0.1.md.
PolyMC Slavic-Myths-0.2.0 обновлён: один JAR 0.8.0.1, SHA256 совпадает с production.
Предыдущий 0.8.0 сохранён вне mods; миры и другие моды не изменены.
Отчёт установки: verification/install-0.8.0.1.txt.
0.8.1 не начата. Ниже сохранена история предыдущих версий.

## 0.8.0 — Combat & Bandits

Реализация завершена; игровой QA ожидает пользователя. Оружейный стол с серверным
меню 4×4 и 12 JSON-рецептами; четыре булавы, кистень, железные кольца для vanilla
кольчуги, стёганка и пластинчатый нагрудник. Пять человеческих ролей разбойников,
собственные модели/текстуры/звуки, малый и средний лагеря, Атаман, сохранение состава,
HUD, лут, достижения, пять статей книги, optional JEI и команды поиска/телепортации.

`gradlew.bat --offline clean build`: PASS, production/reobf Java 8 JAR.
Общая проверка: PASS — 192 предмета, 32 блока, 186 PNG, 118 рецептов, 70 достижений.
Проверка 0.8.0: PASS — 12 рецептов, 10 NBT, составы 5/9, один Атаман,
проходимость маркеров, loot references, различные геометрии оружия, побайтовая
воспроизводимость ресурсов (включая OGG), равенство ресурсов в production JAR.
Логи: docs/verification/build-0.8.0.log и resources-0.8.0.log.
Реальных запусков Minecraft: **0**, по заданию. Бой, внешний вид, shift-click,
перезаход, JEI и частота worldgen в игре не проверены; MSPT не измерялся.
Чеклист и команды: CHECKLIST_0.8.0.md. Рецепты: ARMORER_RECIPES_0.8.0.md.
PolyMC Slavic-Myths-0.2.0 обновлён: один JAR 0.8.0, SHA256 совпадает с build/libs.
Предыдущий JAR сохранён вне mods, Curios сохранён, миры не изменены.
Отчёт: verification/install-0.8.0.txt.
0.8.1 не начата. Следующие разделы — история предыдущих версий.

## 0.7.3 — Топи и затопленные руины

Реализация завершена, ожидает ручного теста пользователя. Добавлены стандартные
StructureStart с 23 собственными шаблонами: три избы, два состава поселения, гать,
два святилища, два стана, три подводных прототипа и четыре малых POI. Рельеф проверяется
при генерации; дома имеют отдельные фундаменты, пути следуют местной высоте. Нет
нового тикового поиска или ретрогенерации. Команды `/slavicmyths dev swamp` и
`/slavicmyths dev structure <id> [next]`, семь loot tables, шесть статей книги,
восемь exploration-достижений. Болотник/материалы отсутствующей 0.7.1 не добавлялись.

`--offline clean build`: PASS. Общая проверка ресурсов: PASS (179 предметов, 31 блок,
164 PNG, 106 рецептов, 65 достижений). Проверка NBT/повторяемости/loot: PASS.
Production/reobf JAR: `build/libs/slavicmyths-0.7.3.jar`, Java 8; тестового кода нет.
Игровых запусков: **0** по заданию. Внешний вид, частота, поиск, terrain integration,
loot и встречи не проверены в игре; MSPT не измерялся.
PolyMC: обновлён фактически существующий `Slavic-Myths-0.2.0`, один JAR мода,
добавлен отсутствовавший обязательный Curios из локальной зависимости. Старый JAR
сохранён вне mods, миры не изменены. Полные пути и ограничения: CONTINUE_0.7.3.md.

Краткая ручная проверка: новое/другое болото; шесть ID и next; варианты и крыши;
затопленные входы/тайники; книга/достижения; сохранение после перезахода.
0.7.4 не начата. Нижние разделы — история предыдущих проходов.

## 0.7.2 — Хозяева глубин

Основной контент реализован; общий STATUS: PARTIAL из-за отсутствующего в этой копии
проекта контента 0.7.1. Болотник/трясины/материалы 0.7.1 не регистрируются заново.

- [x] Progression clues — отношения с Водяным, редкая речная/болотная рыбалка, направление знакомого миру Омута
- [x] Elder Vodyanoy model
- [x] Elder Vodyanoy textures
- [x] Elder Vodyanoy animations
- [x] Elder Vodyanoy sounds
- [x] Encounter arena
- [x] Encounter activation
- [x] Boss bar
- [x] Phase I
- [x] Phase II
- [x] Phase III
- [x] Arena water mechanic
- [x] Defeated-state persistence
- [x] Pearl of the Pool
- [x] Pool Spear
- [x] Water thrust
- [x] Depth Amulet
- [x] Vodyanoy Net
- [x] Loot
- [x] Book of Tales
- [x] Advancements
- [x] Recipes/JEI
- [x] Resource verification
- [x] Production JAR
- [x] PolyMC
- [ ] Интеграция с отсутствующим контентом 0.7.1

Технически проверены clean build, JSON, PNG/OGG, Java 8, production/reobf JAR и модели
новых предметов. Игровых запусков: 0. Persistence/бой/волны/вода/навигация требуют ручного
теста в Minecraft, в том числе multiplayer и перезаход во время боя/после победы.
Подробности следующего шага: CONTINUE_0.7.2.md. 0.7.3 не начата.
Slavic-Myths-Testing обновлён до 0.7.2: один JAR, SHA-256 совпадает с production,
Curios/JEI не изменены; предыдущая сборка сохранена вне mods.

## 0.7.0 — Тихие воды

- [x] Fishing overhaul
- [x] Fishing nets
- [x] Pike
- [x] Carp
- [x] Crayfish
- [x] Water food
- [x] Aquatic plants
- [x] Water resources
- [x] Vodyanoy
- [x] Rusalka
- [x] Rusalka song
- [x] Water POI
- [x] Sounds
- [x] Book of Tales
- [x] Advancements
- [x] Recipes/JEI
- [x] Production build
- [x] PolyMC

Реализация 0.7.0 завершена; `--offline clean build` и resource verifier — PASS:
172 предмета, 30 блоков, 163 PNG, 103 рецепта, 52 достижения; JSON/OGG, Java 8 и reobf JAR.
Игровых запусков: 0. Ручная проверка: рыбные силуэты/движения, сети и сохранение улова,
Водяной/подношения/вылов, песня и сопротивление, кухня/JEI, растения и новые POI.
Вокал Русалки — собственная бессловесная формантная запись; звучание оценивает пользователь.
Новая крупная болотная арка и 0.7.1 не начаты.
PolyMC Slavic-Myths-Testing: установлен единственный slavicmyths-0.7.0.jar,
SHA-256 совпадает с production; Curios/JEI не изменены, предыдущая сборка в mod-backups.


## Artifact content drop — 0.6.5

- [x] Гусли-самогуды: удержание, музыка, постепенное успокоение, grace/cooldown, модель/поза.
- [x] Скатерть-самобранка: шесть общих порций, визуальное убывание еды, складывание, NBT cooldown.
- [x] Бадняк: трёхсекундное сжигание, расход, временное благословение, дым/угольки.
- [x] Посох Велеса: один временный зверь, владелец/союзники, срок, cooldown и прочность.
- [x] Оберег смерти: прежний ID retribution_charm, одноразовое спасение и ответный урон источнику.
- [ ] Дополнительные резной сундук, столб и утварь (необязательные).

`--offline clean build` и resource verifier — PASS: 153 предмета, 26 блоков, 93 рецепта,
46 достижений; production/reobf JAR, Java 8 и декодирование OGG проверены.
PolyMC `Slavic-Myths-Testing` обновлён: единственный JAR Slavic Myths совпадает с production
по SHA-256; Curios и JEI не изменены, прежняя сборка сохранена в mod-backups.
Игровых запусков: 0. Дополнительный декор пропущен; новые четыре артефакта пока Creative/commands.
Ручная проверка: удержание/остановка музыки, позы, поведение успокоенных мобов, расход порций
в multiplayer, таймер и складывание Скатерти, призыв/исчезновение зверя, спасение Оберегом.


## 0.6.5 — Finalization Patch (текущий проход)

Закрыты кусты малины/черники (рост, сбор, повторное созревание, лесные группы), кухня
с 11 рецептами и инструментами, Метла/Ступа/Пест, серверное движение, груз Ступы на 9 слотов
и «Попутный ветер» I–III. Существующие Curios-аксессуары сохранены без дублей;
у шапки ограничены длительность невидимости и частота активации.
Сбор из листвы заменён кустами. Новые модели — собственная JSON-геометрия с vanilla-материалами.
Minecraft и PolyMC: 0 запусков; PolyMC не изменён по актуальному указанию пользователя.
Ручная проверка: рост/сбор ягод, кухня, аксессуары, посадка и полёт, Пест, груз после
перезахода и переноса в предмет, first/third person, столкновения, звук и частицы.
Исторические записи ниже описывают предыдущие проходы. Версия 0.7.0 не начата.

Итог: `--offline clean build` и `tools/verify_resources.py` — PASS (149 предметов,
25 блоков, 93 рецепта, 42 достижения; JSON, PNG, OGG, Java 8 и production/reobf JAR).
JAR: `D:\slavic-myths\build\libs\slavicmyths-0.6.5.jar`. Игровые сценарии не проверялись.

## 0.6.5 — Wildlife & Cooking

Добавлены шесть wildlife entity type: бурый медведь, медвежонок, лесной волк,
кабан, олень и олениха. У видов отдельные размеры, модели, текстуры, параметры,
походки, плавание, боевые позы, AI, лесной/равнинный спавн и loot. Добавлены три
вида сырого/приготовленного мяса, крупная кость, костяная стрела, мука, две ягоды,
четыре вида блинов, каравай, пирог, печёное яблоко и пять похлёбок с рецептами.
До полноценной реализации кустов ягоды редко выпадают из листвы при обычной рубке;
ножницы и Шёлковое касание исключены, обработчик срабатывает только при ломании блока.
Вторичные showcase-предметы, ягодные кусты и кухонные станции перенесены без
placeholder-реализаций. Игровых запусков Codex: 0; ручная проверка обязательна.
`gradlew.bat --offline clean build` — PASS; `verify_resources.py` — PASS:
136 регистраций предметов, 22 блока, 148 PNG, 83 рецепта, 34 достижения,
production/reobf JAR и Java 8 проверены.
JAR установлен в PolyMC instance `Slavic-Myths-0.2.0`; оставлена ровно одна версия
Slavic Myths, SHA-256 установленной копии совпадает с `build/libs`.

## 0.6.0 — Пути, руны и перековка

Четыре пути, 24 навыка, основной/побочный выбор за vanilla levels и предметы.
Старшие навыки требуют открытий; побочный ограничен первыми тремя навыками.
Прогресс — отдельный compound в Forge PlayerPersisted, с копированием при Clone.
Камень Пути и Рунная наковальня имеют серверные меню; активная способность — G.
Восемь рун получаются существующими обрядами (JEI и Книга сказаний обновлены).
Перековка 0–3 гнёзд, установка/удаление без замены ItemStack и потери зачарований;
ванильные мечи, топоры, луки, щиты поддержаны. Малое святилище — редкий feature
5×5 в новых равнинных/лесных чанках; доступны также рецепты обоих блоков.
13 NBT contract checks — PASS: roundtrip, независимость копии, сохранность чужих
данных и зачарований, атомарный отказ некорректных гнёзд. Новые JSON, texture refs,
локализация, рецепты и tags проверены. Minecraft: 0 запусков по требованию.
`gradlew.bat --offline clean build` — PASS; production Java 8/reobf JAR проверен.
PolyMC instance `Slavic-Myths-0.2.0` обновлён до 0.6.0: ровно один JAR мода, SHA-256 совпал
с build/libs и выдаваемой копией; остальные моды сверены по хешам и сохранены.
Игровой persistence, меню, PvP/несколько игроков и баланс ещё не проверены в игре.
Ручная проверка: выбор/XP → покупка → смерть/перезаход → зачарованный железный меч
в наковальне → гнездо/руна/удаление → лук/щит/G → второй игрок с другим путём.

## 0.5.1 — Entity Visual & Audio Hotfix

Переработаны иерархические модели Полудницы и Полевика; доработаны поза,
кисти, борода Банника и птичьи конечности/одежда Кикиморы. Обновлены 8 текстур,
анимации четырёх существ и 25 собственных синтезированных OGG (11 новых событий).
`gradlew.bat --offline clean build` — PASS, включая reobfJar. Проверены изменённые
PNG/OGG, пути звуков/субтитры, Java 8 и соответствие ресурсов итоговому JAR.
Сравнение с 0.5.0 подтвердило неизменность Игоши, Овинника, предметов, блоков,
рецептов, loot и spawn-классов. Minecraft не запускался (0 запусков).
Статическая проверка силуэтов выполнена; игровой вид, звук и переход формы
требуют ручной проверки. PolyMC: существующий Slavic-Myths-Testing, версия 0.5.1.

## 0.5.0 — «Хозяева земли»

Добавлены Кикимора (4 одежды/2 убора), Полудница с превращением, Полевик
с персональными дарами, Банник с паром/камнем, Игоша с мирным исходом,
Овинник с предупреждениями, boss bar, тяжёлой лапой, рывком и второй фазой.
Собственные cuboid-модели, 10 новых entity textures, 30 оригинальных синтезированных
звуков; 9 предметов, 6 яиц, каменка и кадка. Редкие баня/овин — chunk features
в новых чанках, не locate-структуры. Новые ингредиенты имеют применение; ритуал
Угольного сердца включён в JEI. Кикимора — игровая интерпретация, не исторический канон.
`gradlew.bat --offline clean build` — PASS (обычный запуск остановился на проверке
сертификата Maven Forge; использован готовый локальный кэш без отключения TLS).
`verify_050.py` — PASS: JSON/PNG/OGG, регистрации, актуальность JAR, Java 8/reobf.
PolyMC Slavic-Myths-Testing обновлён, SHA-256 совпадает, остальные моды сохранены.
Minecraft не запускался. Ручная проверка: яйца/новые чанки, полуденный спавн,
переход Полудницы, подношения, защита сундуков, бой с Овинником и перезаход.
Баланс, естественная частота встреч и поведение в игре пока не подтверждены.
Входное задание оборвано в разделе 121; новые достижения не добавлялись.

## 0.4.7 — Weapon Overhaul

12 прежних оружий и 2 щита переведены на отдельные объёмные JSON-модели;
меч/кинжал по длине 1,83, копья и посохи длиннее меча. Forge isShield исправлен,
добавлены blocking predicates/models, ванильные use/block/durability сохранены.
Новые: Громовое копьё (+2 урона раз в 6 с), Булава, Перунитовая булава, Бердыш;
обмотка и серебряная оковка используются в рецептах. Два новых ритуала и JEI.
`clean build`, `reobfJar`, `verify_weapons.py` — PASS; Java 8, production JAR.
PolyMC Slavic-Myths-Testing обновлён, SHA-256 совпадает, прочие моды сохранены.
Minecraft не запускался. Положение в руках и блокирование требуют ручной проверки.
Опциональные серебряный бердыш и точильный камень отложены.

## 0.4.6 — visual polish

Финальный art pass: 27 предметов, 12 блоков (19 текстур, 4 JSON-модели),
2 слоя брони. Новые силуэты оружия, посохов, щитов и подвесок; резьба,
каменные поверхности, ниша очага, идол и свеча. Генератор: generate_046.py
с финальными пиксельными исходниками polish_046.py. У очага и идола отключено
отсечение соседних граней для неполных моделей; игровые характеристики сохранены.
`gradlew.bat clean build` — PASS, `reobfJar` — PASS. Проверены JSON-ссылки,
90 PNG, ресурсы внутри JAR и Java 8 bytecode. Звуковой этап общего проверяющего
скрипта не выполнен: отсутствует Python soundfile; звуки не менялись.
Production JAR: `D:/slavic-myths/build/libs/slavicmyths-0.4.6.jar`.
Установлен в PolyMC `Slavic-Myths-Testing`, имя «Slavic Myths - Test (0.4.6)».
SHA-256 копии совпадает; одна версия мода, JEI сохранён. Восстановлен JDK 8
по настроенному пути instance. Minecraft не запускался, игровых запусков: 0.

## 0.4.5 — Visual & Content Overhaul

Исправление после ручного запуска: Forge завершал `load_registries` с ошибкой
`Unable to have damage AND stack` при создании `retainer_shield`, потому что
`stacksTo(1)` вызывался после `durability(280)`. У обоих щитов удалён лишний
вызов `stacksTo(1)`; прочность уже делает предмет нестакуемым. Это исправление
пока проверено сборкой и статическим аудитом, повторного игрового запуска Codex нет.

74 предметные регистрации, 18 блоков; новых относительно 0.4.1: 20 предметов,
3 блока, 3 обряда, 17 обычных рецептов и 4 видимых достижения. Добавлены янтарь
из сундука капища, пять видов оружия/посохов, два функциональных щита, три новых
оберега, свеча, обрядовый уголь, еда и настенная резьба. Серебряное оружие получает
+1,5 урона по существам тега `silver_vulnerable` (сейчас Леший). Грозовой посох:
импульс 3 урона на расстояние до 7 блоков, cooldown 80 тиков, 240 применений.
Обработчики срабатывают по использованию/урону, не каждый тик. Новых мобов нет.

Перерисованы 37 существовавших предметных текстур, 15 блоковых и 2 слоя брони;
дополнительно растения получили отдельные item sprites. Старые registry ID сохранены.
`tools/generate_045.py` воспроизводит обновлённые assets после прежних генераторов.
`tools/visual_audit.py` проверяет ссылки моделей, размеры, непрозрачность, совпадения
пикселей и силуэтов; контактный лист просмотрен. Статический визуальный аудит — PASS:
80 различных item/block/armor текстур, одинаковых пиксельных файлов 0; одинаковые внешние
контуры у семейства подвесок допустимы, центральные знаки различаются.

Ограничения: щиты используют плоскую item-модель для надёжного vanilla блокирования,
не отдельный 3D renderer; вид на игроке и геймплей пользователь ещё не проверил.
Ступка, кувшины, вышитое полотенце и ритуальный коврик не добавлены, так как в
0.4.1 отсутствовали или требовали отдельной механики. Игровых запусков Codex: 0.
После финальных изменений `gradlew.bat clean build` — PASS, проверка ресурсов и
production/reobf Java 8 JAR — PASS. `visual_audit.py` — PASS; генератор ресурсов
повторён без изменения байтов. `slavicmyths-0.4.5.jar` установлен в подтверждённый
PolyMC instance `Slavic-Myths-0.2.0` (Minecraft 1.16.5, Forge 36.2.42), одна версия
мода в `mods`. Название instance историческое; игру пользователь запустит вручную.


## 0.4.1 — первый проход расширения контента

Добавлены 34 предметные регистрации и девять блоков: комплект инструментов и брони
из перунита, серебряная руда/слиток/самородок и оружие, костяной и обрядовый нож,
четыре дикорастущие травы, переработанные ингредиенты, ткань, мешочек, две еды,
ритуальная чаша и деревянный/соломенный декор. Всего 54 предмета и 15 блоков.
Серебро и травы появляются только в новых чанках; существующий мир не меняется.
Добавлены 28 рецептов и добыча новых ингредиентов в капище.

Добавлены два обряда: топор из перунитового топора, громового камня, Древнего знака
и чаши; оберег Перуна из ткани, громового камня и Древнего знака. Они показаны в JEI.
Громовой топор имеет ограниченный электрический удар без призыва молнии; полный
перунитовый комплект и оберег в левой руке уменьшают урон от молнии.

Границы этого прохода: новые травы пока прежде всего ремесленные ингредиенты;
бонус серебра против духов, напитки, прочие обереги и ступка ещё не реализованы.
Готового полного milestone 0.4.1 этот проход не означает. Модели брони
используют авторскую простую пиксельную раскраску. После последних изменений
`gradlew.bat clean build` — PASS, `tools/verify_resources.py` — PASS (54 предмета,
15 блоков, 60 PNG, 35 рецептов, reobf Java 8 JAR). JAR установлен в подтверждённый
PolyMC instance `Slavic-Myths-0.2.0` с Minecraft 1.16.5 и Forge 36.2.42;
название instance историческое. Minecraft не запускался по просьбе пользователя,
реальных запусков: 0.

## 0.4.0 — Духи, капища и первые ритуалы

Minecraft 1.16.5, Forge 36.2.42, Java 8; mod ID `slavicmyths`.
В начале прохода код уже содержал 0.3.0, хотя статус описывал 0.2.0.
Прежний документ сохранён в verification/status-before-0.4.txt; старые отчёты — история.

### Реализовано

- Сохранены 13 прежних предметных ID, три блока, оба духа, модели и семь оригинальных звуков.
- Домовой: отношения −100…100 по UUID взаимодействовавших игроков, четыре вида даров,
  cooldown 60 секунд, возврат тары, creative, возрастающая обида за удары, отказ и избегание.
  Четыре дара дают дружбу, восемь — преданность.
- Домовой очаг: рецепт, привязка друга в радиусе 8 блоков, хозяин, координаты/измерение,
  persistent и стандартное возвращение домой. NBT сохраняет отношения, дом и cooldown.
  Благословение пустой рукой у действующего очага: Regeneration I, 5 секунд, раз в 10 минут.
- Леший: лесной зов, 25% шанс Slowness I на 3 секунды при ударе; попытка безопасного
  исчезновения при HP ≤12, cooldown 30 секунд, максимум 6 локальных кандидатов.
- Древнее капище 11×11: каменный круг, дерево, костёр, растения, идол, жертвенник и сундук.
  Новые чанки vanilla forest/plains/taiga только в Overworld; попытка 1/96 до проверки площадки.
- Древний идол, жертвенник, древняя монета, осколок идола, Древний знак и Книга сказаний.
- Ритуал: полынь → берестяной свиток → оберег → Древний знак. Состояние в TileEntity NBT;
  подсказки, звук и частицы. Определение ритуала отделено от исполнения.
- Книга с GUI, страницами, пятью открываемыми записями, введением и намёком на будущие пути.
  Знания хранятся стандартными advancements; при открытии один серверный пакет.
- 20 предметов, 6 блоков, 7 обычных рецептов, 24 видимых достижения, единая Creative Tab.
  RU/EN, новые PNG 16×16, модели, blockstates, loot. Vanilla advancements сохраняются.
- Необязательная категория JEI «Ритуалы». JEI не вложен в JAR и не обязателен для запуска.

### Проверки и границы

Чистая сборка, ресурсы, Java 8, reobfuscation и содержимое production JAR проверены.
Dev client реально загружал меню и копию мира 0.3.0, в том числе без JEI.
Игровые assertions проверили подношения, тару, cooldown, сохранение NBT и перезаход,
благословение, полный ритуал, результат, loot, placement капища и открытие GUI сервером.
Снимки GUI, предметных текстур и тестовой сцены просмотрены.
Точные результаты запусков и ограничения — VERIFICATION_0.4.0.md.

Капище — worldgen Feature: `/locate` не поддерживается. Автотест размещает его на
подготовленной площадке; естественную частоту и survival-баланс проверяют вручную.
Dedicated server и два настоящих игрока не проверены. Статический performance-аудит
и единичный замер placement не заменяют массовый MSPT-тест.
Оставшиеся игровые проверки — MANUAL_TEST_0.4.0.md.

### Не реализовано намеренно

Классы, мана, выносливость, заклинания, боги, боссы, измерения, Водяной, Русалка и Баба-яга.
Знак — progression item; монеты и осколки пока не имеют торгового применения.
Нет автоматического распознавания домов, ретрогенерации или встроенного TreeCapitator.

Git-репозитория нет, commit не создавался. Пользовательские миры не перезаписывались.

## Следующий этап

После ручной проверки и баланса 0.4.0 — предварительный этап 0.5.0 «Воды и болота».
