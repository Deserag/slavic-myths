## Установка 1.3.3 в PolyMC — 2026-10-08

По прямому запросу пользователя production 1.3.3 установлен в существующий `Slavic-Myths-1.21.1-Testing` (Minecraft 1.21.1 / NeoForge 21.1.255). Включены парные мечи Тугарина и остальное редкое оружие 1.3.2.2, Classes UI и рунные слоты брони. Предыдущая 1.3.2.3 сохранена вне mods, активен один JAR, SHA-256 установленной копии совпал; 542 остальных файла экземпляра (включая миры/настройки) побайтно сохранены. Клиент и сервер не запускались. [Квитанция](verification/polymc-1.3.3-installation.json). Записи ниже об отложенной установке исторические.

# Slavic Myths 1.3.3 — рунные слоты брони

## Вместимость

| Материал / предмет | Максимум | Причина |
|---|---:|---|
| Vanilla leather, seven_league_boots | 0 | Кожаный материал; намеренно без рун, редкость сапог не меняет материал |
| Vanilla chainmail, gold, iron | 1 | Базовое правило патча, все четыре player armor pieces |
| Vanilla diamond, netherite | 2 | Базовое правило патча, все четыре player armor pieces |
| Vanilla turtle helmet | 1 | Базовое правило патча |
| perunite_helmet/chestplate/leggings/boots | 3 | Явное требование для особого материала; характеристики не повышаются |
| gambeson | 1 | Стёганка: защита 4 на груди, toughness 0, durability 192; доступный wool/leather mid-game рецепт |
| plate_cuirass | 1 | Специализированная железная кираса: защита 7, toughness 1, durability 352, штраф скорости 5%; iron/rings + gambeson, ниже diamond chestplate по защите/прочности |
| ClothingItem, аксессуары, декоративные wearables | 0 | Нулевые armor stats / отдельная wearable-система |
| Animal BODY и неизвестные armor materials | 0 | Не входят в найденные player комплекты; нет автоматических трёх слотов по mod namespace |

Это максимумы, а не автоматически открытые ячейки. Сохранён прежний порядок наковальни: открыть ячейку за прежние материалы/XP, затем установить руну. Все восемь существующих рун допускаются для хранения на подходящей броне, дубликаты запрещены. Новых armor effects/passives/combat bonuses нет; надетая броня и броня в руке не активируют оружейные эффекты. Возможность автоматического открытия слотов не добавлена.

## Изменённые файлы

- `src/main/java/org/slavicmyths/rpg/Runes.java`: общая material capacity, строгий armor max, общая installation rule для server/preview, поддержка armor без добавления оружейной категории.
- `rpg/RpgEvents.java`: life-on-kill исключает броню в руке; остальные события сохранены.
- `rpg/RuneDefinition.java`: пояснение, что броня только хранит руны; числовые параметры/оружейные categories не менялись.
- `client/RpgClient.java`, `client/RpgScreen.java`: прежние tooltip/ячейки/preview/details для armor, предупреждение об отсутствии эффектов; описание эффектов оружия сохранено.
- `gradle.properties` → 1.3.3; `build.gradle` — только opt-in offline JavaExec/source set, без Minecraft runs.
- `tools/armor_rune_resources_133.py`, `tools/integration_polish_resources.py`, две локализации; `tools/armor-offline-tests/.../ArmorRunesOffline.java`, `tools/verify_armor_runes_133.py`; текущие docs и README.

Затронуты уже существующие registry ID брони из таблицы, все vanilla player armor pieces и существующие rune_* предметы. **Новых или переименованных registry ID нет.** Используется прежний компонент `slavicmyths:runes` / legacy SlavicRunes, без новых data formats. Оружейные capacity rules и ранее заработанные ячейки сохранены.

## Сохранение данных

Server operation записывает только rune component на существующем ItemStack; preview копирует stack и не попадает в инвентарь. Значит durability, custom name, enchantments, trims и foreign custom data не заменяются operation. ItemState и RuneRepair побайтно совпадают с предыдущим production JAR.

Установленные Minecraft 1.21.1/NeoForge 21.1.255 sources проверены: SmithingTransformRecipe.assemble использует base.transmuteCopy, SmithingTrimRecipe — base.copyWithCount(1); custom component переносится. RuneRepair для двух предметов сохраняет source stack и придерживается прежних vanilla disenchantment/curse правил. В ремонте двумя предметами уже существующие ограничения не позволяют поглотить несовместимые rune-bearing данные второго предмета. Это статическая проверка, а не игровой тест Mending/Unbreaking/Protection/repair/trim/upgrade.

## Проверки и ограничения

- `gradlew.bat clean build` PASS, Java 21, production NeoForge JAR; `.tools/armor-133-build.log`.
- `gradlew.bat -ParmorOfflineCheck verifyArmorRunes` PASS: 147 проверок material capacity, последовательного заполнения до предела, переполнения, дубликатов, unopened sockets, unknown rune, compatibility и count. RuneState immutable, JSON/NBT/packet roundtrip, отказ malformed component. Fixture находится только в tools и не входит в JAR.
- 10 542 ресурсных/JAR gates PASS: 4 862 прежних assets/data сохранены побайтно; в RU/EN только два новых ключа; изменены ровно пять production classes, нет новых классов/registry resources. ItemState/RuneRepair/ModGear/ModItems/RuneEffects/classes/HUD и старые модели/рецепты совпадают с production 1.3.2.3. Overlay воспроизводим.
- Один offline native-registry probe не смог инициализироваться без loader: FeatureFlagLoader → LoadingModList null. Диагностика `.tools/armor-133-native-probe.log` сохранена, probe удалён из текущего harness, production обходов нет. Поэтому item operations, serializing полного ItemStack, GUI и dedicated-server safety не выдаются за runtime-tested.
- По прямому ответу пользователя запускаются только сборка и офлайн-проверки. **0 клиентов, 0 серверов**. Смерть/respawn/реальный relog/restart/dimension/drop-pickup и игровые repair/rename/trim/upgrade не запускались; FPS/MSPT не измерялись.

## Краткая ручная приёмка (не выполнена)

1. В отдельном тестовом мире проверить отказ leather и пределы 1/2/3 для всех четырёх armor slots, turtle, perunite, gambeson/plate; дубликат и слот без ковки.
2. Установить/снять существующие руны; проверить tooltip/GUI и отсутствие эффектов на броне, в том числе life при убийстве с бронёй в руке.
3. Проверить повреждённый enchanted/named/trimmed stack: equip/drop/pickup, смерть/respawn, dimension, реальный relog и новый серверный процесс; затем repair/rename/Mending/trim/netherite upgrade. Сохранять отдельный тестовый мир.
4. На dedicated server проверить ту же наковальню и отсутствие client-class loading; проверить прежнее оружие/его руны. Запуск требует отдельного разрешения пользователя.

## Артефакт

`D:\slavic-myths\build\libs\slavicmyths-1.3.3.jar` — 9601539 bytes. SHA-256 `0a0003b2daced05fa86e458eaa9efee562179567ee06c8dd1afa4535559295b7`.

[Gates](verification/armor-runes-1.3.3.json). PolyMC не обновлялся этим заданием; установленная 1.3.2.3 сохранена. Commit/push не выполнялись.
