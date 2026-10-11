# Ручная проверка 1.5.4

Самостоятельно перезапустите тестовый MC 1.21.1-Testing: установлен `slavicmyths-1.5.4.jar`. Агент игру не запускал. Используйте отдельный тестовый мир; команды генерации создают реальные сооружения. `/sm` — сокращение `/slavicmyths`; генерация/диагностика требуют OP2. Новое содержимое появляется только в новых А/Б, старые комплексы не переоборудуются.

## Новые комплексы, машины и NPC

```mcfunction
/sm city generate
/sm city generate attached
/sm city info
/sm city validate
/sm city tp forge
/sm city tp store
/sm city tp residential
/sm city tp square
/sm city tp council
/sm city tp gate
/sm city tp entrance
/sm abandoned generate medium
/sm abandoned generate great
/sm abandoned info
/sm abandoned validate
/sm abandoned tp entrance
/sm abandoned tp quarter
/sm abandoned tp gallery
/sm abandoned tp shaft
/sm abandoned tp labyrinth
/sm abandoned tp gate
```

Генерируйте варианты по отдельности в разных чанках. На flat постройки поднимаются над поверхностью; в обычном мире размещаются под землёй. `attached` проверяет связанный город и небольшую Б; medium/great — самостоятельные Б. Не у каждого типа Б есть все точки телепортации. `validate` проверяет доступные загруженные проходы, не заменяет осмотр всех комнат. Машины/сундуки могут пропускаться, если безопасной свободной клетки нет.

Для поиска естественных новых А/Б:

```mcfunction
/sm city locate
/sm abandoned locate attached
/sm abandoned locate medium
/sm abandoned locate great
/sm chud_search status
/sm chud_search next
/sm chud_search cancel
```

Осмотрите кузницу/склад/жилища: проходы свободны, сундуки доступны, жители и стражи присутствуют. Торговля должна списывать точную оплату, соблюдать stock/maxUses, прекращаться при смерти/уходе NPC; возле домашней зоны не более двух пополнений в день. Проверьте зомби/скелетов и реакцию стражей на нападение. Сохраните мир и перезайдите: NPC, offers/uses, машины и сундуки не должны дублироваться.

## Машины и материалы

```mcfunction
/give @s slavicmyths:chud_bloomery
/give @s slavicmyths:refining_hearth
/give @s slavicmyths:bog_iron_ore 16
/give @s slavicmyths:bog_iron_lump 16
/give @s slavicmyths:purification_flux 16
/give @s slavicmyths:purified_iron_ingot 16
/give @s slavicmyths:purified_copper_ingot 16
/give @s minecraft:iron_ingot 64
/give @s minecraft:copper_ingot 64
/give @s minecraft:coal 64
/give @s minecraft:hopper 8
/give @s minecraft:chest 8
/locate biome minecraft:swamp
/locate biome minecraft:mangrove_swamp
```

Домница: один комок → один железный слиток за 10 секунд при 20 TPS. Горн: один железный/медный слиток и один флюс → один очищенный слиток за 15 секунд. Проверьте отказ без топлива/флюса, полный или неподходящий выход, смену входа, Shift-click, JEI, поворот/свет/частицы. При работе сохранить/выйти/войти; затем сломать и поставить машину: содержимое должно выпасть один раз, прогресс поставленной машины — ноль. Хопперы: сверху металл, сбоку топливо/флюс, снизу результат и пустое ведро; вход не вытягивается снизу. Природную руду проверяйте в новых болотных чанках обычной киркой и Silk Touch.

## Украшения и руны

```mcfunction
/give @s slavicmyths:miners_goggles
/give @s slavicmyths:prospectors_ring
/give @s slavicmyths:stone_path_amulet
/give @s slavicmyths:underground_guard_belt
/give @s slavicmyths:mining_rune_1
/give @s slavicmyths:mining_rune_2
/give @s slavicmyths:fortune_rune_1
/give @s slavicmyths:runic_anvil
/give @s minecraft:iron_pickaxe
/give @s minecraft:diamond_pickaxe
/give @s minecraft:netherite_pickaxe
/give @s minecraft:diamond_ore 64
/give @s minecraft:diamond 64
/give @s minecraft:netherite_scrap 16
/give @s minecraft:netherite_ingot 2
/give @s minecraft:amethyst_shard 16
/give @s minecraft:gold_ingot 32
/effect give @s minecraft:night_vision 120 0 true
/enchant @s minecraft:fortune 3
```

Curios: head — очки, ring — кольцо, necklace — амулет, belt — пояс. Очки проверить в темноте; добавить внешний Night Vision, снять очки и убедиться, что внешний эффект сохранился с истекшим временем. Повторить с выпитым зельем. Пояс многократно надеть/снять и перезайти: бонус не накапливается. Амулет проверить на одинаковом падении в survival, отдельно на других источниках урона.

Используйте существующую руническую наковальню из creative tab. Руны только для кирок, с прежними ограничениями вместимости. Сравнить ту же руду: обычная кирка, кольцо (+12%), добыча I/II, кольцо+II (×1.344); камень/неподходящий инструмент не получают рудный бонус. `/enchant` применяется к предмету в руке. Удача I + Fortune III — эффективный IV; проверьте также Silk Touch на отдельной кирке, отсутствие двойного дропа и неизменность vanilla enchantment на предмете. Fortune работает для рудных loot tables с ApplyBonusCount; прежняя серебряная руда выдаёт свой блок и сохранена без нового бонуса к числу блоков.

## Изолированная проверка торговых профилей

```mcfunction
/summon slavicmyths:chud_resident ~ ~ ~ {RareStock:1b}
/summon slavicmyths:chud_artisan ~ ~ ~ {ChudRole:1}
/summon slavicmyths:chud_artisan ~ ~ ~ {ChudRole:2}
/summon slavicmyths:chud_guard ~ ~ ~ {ChudRole:3}
/give @s slavicmyths:chud_resident_spawn_egg
/give @s slavicmyths:chud_artisan_spawn_egg
/give @s slavicmyths:chud_guard_spawn_egg
```

Первая команда гарантирует редкие предложения жителя; ChudRole 2 — шахтёр с флюсом и рунами. Суммон без Home подходит для окна/цен, но пополнение и возвращение домой проверяйте на NPC нового города. При недостаточной оплате покупка запрещена. Страж не торгует.

## Шесть таблиц лута

```mcfunction
/loot give @s loot slavicmyths:chests/chud_residential
/loot give @s loot slavicmyths:chests/chud_workshop
/loot give @s loot slavicmyths:chests/chud_storehouse
/loot give @s loot slavicmyths:chests/abandoned_living
/loot give @s loot slavicmyths:chests/abandoned_mine
/loot give @s loot slavicmyths:chests/abandoned_treasury
```

Одна выдача не подтверждает вероятность редкого награждения. Отдельно осмотрите реальные новые сундуки А/Б и проверьте их содержимое после открытия, выгрузки чанка и перезахода; старые/поставленные игроком сундуки не переоборудуются.

## Регрессия Древних глубин

```mcfunction
/sm depths generate
/sm depths status
/sm depths locate
/sm depths map
/give @s slavicmyths:ancient_depths_map
/gamemode spectator
/tp @s X Y Z
/gamemode survival
```

Для `/tp` подставьте реальные координаты из locate/завершения. Код В в этой версии побайтно сохранён; подробная прежняя проверка — [команды 1.5.3.1](../ancient-depths-1.5.3.1/COMMANDS.md). Проверка энд-портала проводится на отдельном тестовом мире: рамки и портал не должны изменяться при генерации А/Б/В.
