# 1.3.4.1 — основа рунного дела

Реализовано по `Slavic_Myths_1.3.4.1_Rune_Foundation_Codex.zip`. Minecraft 1.21.1, NeoForge 21.1.255, Java 21. Существующая наковальня `slavicmyths:runic_anvil` и menu ID `slavicmyths:rpg` сохранены. Второй станок/система эффектов не создавались. Classes 2.0 остаётся на этапе утверждения макетов, его production-код не менялся.

## Предметы и данные

Все ID ниже в namespace `slavicmyths`:

| ID | Назначение |
|---|---|
| `rune_chisel` | Зубило, 192 применения; каждое измельчение расходует ровно 1 прочность, последняя операция ломает инструмент |
| `iron_dust`, `gold_dust`, `coal_dust`, `lapis_dust`, `diamond_dust`, `perunite_dust` | Шесть отдельных предметов пыли; обычные stackable ингредиенты |
| `soul_fragment`, `soul`, `empowered_soul`, `bound_soul` | Осколок, душа, усиленная и заключённая души |
| `blank_rune_1`, `blank_rune_2`, `blank_rune_3` | Пустые основы I–III, без эффектов и без допуска к установке как готовой руны |

14 новых item IDs; тип/serializer рецептов `slavicmyths:rune_crafting`; новый компонент `slavicmyths:rune_base`. RuneBase хранит tier 1–3 и material; допустимые пары строго ограничены: I iron/gold, II diamond/perunite, III perunite. Persistent Codec + network codec; item defaults, JSON-выходы и Creative stacks содержат компонент. RU/EN tooltip показывает уровень, основу и отсутствие эффекта. Структура допускает дальнейшее расширение кодека без сохранения параметров в имени/lore; пустые поля будущих механик не добавлялись.

Существующий компонент `slavicmyths:runes`, ID восьми готовых рун, их эффекты/рецепты, ёмкость брони и оружия сохранены. Раздельная пыль выбрана для штатных Ingredient/model/JEI без дополнительных subtype-компонентов пыли. Для пустых рун JEI различает tier/material через subtype interpreter.

Предметы стоят в существующей вкладке «Магия»: зубило → шесть пылей → осколок → душа → усиленная → заключённая → пустые I–III. I и II представлены обеими допустимыми основами. Новых вкладок нет.

## Фактические рецепты

Обычный 3×3: `rune_chisel.json`, pattern `" II" / " RI" / "S  "`, I = iron_ingot, R = существующий iron_rings, S = stick. Всего 3 железных слитка, 1 железные кольца, 1 палка. Перунит для зубила не нужен.

Остальные 15 JSON — `data/slavicmyths/recipe/rune_foundation_*.json`, только существующая рунная наковальня:

| Рецепт | Вход → выход |
|---|---|
| `dust_iron`, `dust_gold`, `dust_coal`, `dust_lapis`, `dust_diamond`, `dust_perunite` | 1 соответствующий слиток/уголь/лазурит/алмаз/перунит + зубило → 4 соответствующих пыли; зубило −1 прочность |
| `soul_coal`, `soul_lapis` | 4 осколка + 1 угольная либо лазуритовая пыль → 1 душа |
| `empowered_soul` | 1 душа + 4 железных слитка + 2 перунита → 1 усиленная душа |
| `bound_soul` | 1 усиленная душа + 4 алмазной пыли + 4 перунитовой пыли → 1 заключённая душа |
| `blank_1_iron`, `blank_1_gold` | 1 душа + 4 железной либо золотой пыли + 4 угольной пыли → пустая руна I выбранной основы |
| `blank_2_diamond`, `blank_2_perunite` | 1 усиленная душа + 4 алмазной либо перунитовой пыли + 4 лазуритовой пыли → пустая руна II выбранной основы |
| `blank_3` | 1 заключённая душа + 4 перунитовой пыли + 4 алмазной пыли → пустая руна III, основа perunite |

Перунит: фактическая existing worldgen-жила size 3, rarity_filter chance 2, высота 0–14; ore drop поддерживает Fortune. Это статическая оценка редкости, не измеренная добыча в мире. Поэтому усиленная душа требует 2 перунита вместо предложенных стартовых 4. Worldgen не менялся.

## Курганы и дропы

- Осколок: 35%, ровно 1, только смерть исходного UPYR, связанного с курганом. LivingDropsEvent добавляет ItemEntity в штатный набор дропа, прежняя loot table упыря не меняется.
- Призванные упыри помечаются persistent `RuneSummoned`; осколки с них исключены. Глобальный spawn, disturbance и правила конечных encounter waves сохранены; новых scans/tick handlers нет.
- В новых экземплярах курганов конечная квота UPYR +1: малый 3–5, средний 4–6, большой 6–9. Существующие room admissibility/capacity и boss quotas сохранены. Старые сохранённые экземпляры без `RunePopulation` сохраняют прежний roster; флаг новых экземпляров сохраняется в BurialRecords.
- `kurgan_voevoda` и `buried_volkhv`: дополнительная душа с шансом 50%; `unresting_prince`: 1 душа гарантированно. Три добавочных JSON loot pools; все старые pools сохранены.

## Меню, GUI и серверные расчёты

Компактный container 202×214 GUI-пикселей. Вкладки «Создание / Усиление / Установка»; снятие доступно из установки. Старые открытие ячеек/установка/снятие используют прежние Runes.blocker/preview/operation и прежние расходы XP/ингредиентов.

Создание: три counted input slots, отдельное зубило, один реальный output slot. Выбор рецепта из компактной сетки 4×4 либо колесом над кнопкой; варианты только с допустимыми основами. Числа have/required возле слотов, подробные имена/количества в tooltip. Все предметы остаются в штатных слотах Minecraft; скрытые при смене вкладки входы не уничтожаются.

Сервер выбирает рецепт из RecipeManager, повторно проверяет входы/количества/зубило/дистанцию и блок. Перед выдачей первого предмета партии списывает ингредиенты и прочность; частично полученная партия уже оплачена, остаток можно забрать без повторного списания. До получения остатка переключение режима/рецепта запрещено. При закрытии возвращаются все входы и только оплаченный остаток; виртуальный preview не выдаётся.

Shift-click выхода сначала проверяет место для всей партии, затем выполняет тот же transactional Slot.remove. Быстрая обработка нескольких партий использует штатный vanilla loop, каждая партия отдельно оплачивается. SWAP/CLONE/PICKUP_ALL выхода заблокированы; проверено по фактическим исходникам AbstractContainerMenu/Slot установленной 1.21.1, это аудит кода, не игровой тест. Повтор запроса без подходящих ингредиентов не создаёт выход; новые C2S payload не добавлялись.

JEI: новой категории для наковальни раньше не было; добавлена `JeiRunes` с 15 рецептами, counted stacks, выходами с компонентами, catalyst наковальни и зубила, расходом прочности. Client-only JEI-классы изолированы в compat. Automatic recipe transfer не добавлен; раскладка выполняется игроком.

Ванильный звук наковальни для измельчения; amethyst chime для душ/основ; 4 небольшие ENCHANT-частицы. Новых sound assets нет.

## Проверки и ограничения

`gradlew.bat clean build -PruneFoundationOfflineCheck -ParmorOfflineCheck verifyRuneFoundation verifyArmorRunes` — PASS, 14 deprecated/unchecked warnings. Production JAR исключает opt-in fixtures.

- 75 064 JVM-проверки: допустимые/недопустимые tier/material, JSON/NBT/packet roundtrip, deterministic bounded roster 3000 курганов трёх размеров, ровно +1 UPYR и сохранение boss count. Это codec/planning checks, а не relog/restart или измерение TPS.
- 147 прежних JVM-проверок ёмкости/допуска/компонентов рунной брони — PASS.
- 13 862 resource/JAR checks — PASS: JSON, 15 рецептов, 14 моделей/32×32 RGBA sprites, RU/EN, reproducibility генератора, изоляция common от client/JEI, отсутствие test classes, границы 202×214 для восьми resolution/scale pairs. 4859 старых неизменяемых ресурсов побайтно сохранены; исключения только добавочные локали и 3 boss loot tables.
- Все прежние боевые эффекты/баланс/классы/HUD/оружейные модели подтверждены сравнением с production 1.3.3. Установленные runes component/codecs не заменялись.

По запрету пользователя: **0 клиентов, 0 серверов**. Реальные крафт/поломка инструмента/Shift-click/network replay/дроп/JEI/отображение GUI, смерть/respawn/relog/restart/dimension/drop-pickup/chest storage не выполнялись в игре. Dedicated-server safety проверена компиляцией и границей client references, не запуском. MSPT/TPS не измерялись. Визуальные иллюстрации [предметов](media/rune-foundation-1341/items.png) и [раскладки](media/rune-foundation-1341/container-layout.png) офлайн, не игровые скриншоты.

Краткая ручная приёмка при последующем разрешённом запуске: все 15 крафтов и последний durability зубила; частичный/Shift-click выход при полном инвентаре и закрытии; старые 3 операции рун/броня; JEI tier/base; конечный roster/drops; сохранение предметов через смерть/контейнер/relog/restart.

Production: `D:/slavic-myths/build/libs/slavicmyths-1.3.4.1.jar`, 9 659 806 bytes; SHA-256 `37d0f0fd2e77e99dfa0542f4fe24b42f0b79c85c1c4da8a1a62de645009c590b`. [Квитанция gates](verification/rune-foundation-1.3.4.1.json). PolyMC не обновлялся в этой итерации, установленная копия остаётся 1.3.3. Миры/настройки не менялись; commit не выполнялся.

## Изменённые файлы этой итерации

- `SlavicMyths.java`, `item/ItemState.java`, `registry/ModItemGroup.java`, `client/ClientSetup.java`, `rpg/RpgMenu.java`.
- Новые `rpg/RuneBase.java`, `RuneFoundation.java`, `RuneCraftRecipe.java`, `RuneFoundationLoot.java`, `client/RuneAnvilScreen.java`, `compat/JeiRunes.java`.
- `kurgan/KurganRoster.java`, `KurganInstance.java`, `KurganEncounters.java`.
- `gradle.properties`, opt-in offline source set в `build.gradle`; новый `tools/rune-foundation-tests/.../RuneFoundationOffline.java`.
- 14 item models + 14 textures, 16 recipe JSON, 2 локали, 3 boss loot JSON.
- Reproducible generator, targeted verifier и offline preview в `tools/*rune_foundation*1341.py`; отчёт/gates/media и PROJECT_STATUS/ROADMAP/ARCHITECTURE.
