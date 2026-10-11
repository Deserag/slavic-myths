# Ручная проверка мобов 1.5.5

Самостоятельно перезапустите тестовый MC 1.21.1-Testing. Агент не запускал Minecraft. `/sm` равно `/slavicmyths`; новые команды требуют OP2. Для боевых проверок нужен survival/adventure и сложность не peaceful. Используйте отдельный тестовый мир.

## Отдельные противники

```mcfunction
/sm underground spawn podzemnik
/sm underground spawn miner
/sm underground spawn kladovik
/sm underground spawn large_podzemnik
/sm underground status
/give @s slavicmyths:podzemnik_spawn_egg
/give @s slavicmyths:kladovik_spawn_egg
/give @s slavicmyths:large_podzemnik_spawn_egg
/difficulty normal
/gamemode survival
```

Команда spawn создаёт моба рядом с текущей точкой только при свободной коллизии. Для крупного нужен просторный пол и потолок минимум 4 блока. Общее яйцо Подземника выбирает обычного/шахтёра 70/30; `spawn miner` принудительно выбирает шахтёра. Кладовик охраняет домашнюю площадку: сначала предупреждает при входе в радиус 7 либо отвечает на прямую атаку. Случайные сундуки не служат триггером.

Проверить 28/40/200/300 HP; обычные удары и тяжёлый замах киркой; у Кладовика нырок/след/предупреждение/выход, камень и столкновение со стеной, три шипа, волну и ярость на 100 HP. У крупного — дрожь, уклонение прыжком, короткое падение и возвращение управления, тяжёлый удар с уклонением, прямой рывок и оглушение при ударе в стену, ярость на 90 HP. Снаряды/шипы не должны менять блоки или оставаться после смерти.

## Привязка к новым и старым Б

```mcfunction
/sm city generate attached
/sm abandoned generate medium
/sm abandoned generate great
/sm abandoned info
/sm abandoned tp gallery
/sm abandoned tp quarter
/sm underground status
/sm underground populate
```

Варианты генерируйте отдельно, в разных чанках. Новый Б автоматически ставит известные места появления в сохранённую очередь. Сначала должны загрузиться все чанки соответствующей комнаты. При небезопасной позиции очередь постепенно пробует альтернативы, не запрашивая чанки. Посетите галерею/жилые/складские комнаты и дайте очереди обработаться; `status` показывает pending/consumed/defeated.

`populate` — явное одноразовое заселение уже известных Б в загруженных чанках радиуса 16 чанков вокруг позиции. Для старого Б это способ проверки без перестройки; для уже обработанного комплекса повторный вызов не создаёт новые слоты. Команда не заселяет старые города дополнительной мирной Чудью. Маленькая Б: до четырёх обычных врагов, не более двух шахтёров, без крупного босса. Кладовик появляется только в подходящем существующем складе/сокровищнице самостоятельной Б; крупный — только в просторной галерее. Если подходящей комнаты/пола нет, босс пропускается, в журнале записывается причина.

Убить противников, сохранить/перезайти, снова вызвать populate: погибшие не появляются повторно. Отдельно сохранить во время нырка/рывка/дрожи: после загрузки допустимо безопасное восстановление с отменой незавершённого удара, но без невидимого/неуязвимого моба и повторного лута. После смерти/выгрузки BossBar исчезает.

## Чудь и торговля

```mcfunction
/sm city generate
/sm city tp forge
/sm city tp square
/sm city tp residential
/summon slavicmyths:chud_resident ~ ~ ~ {RareStock:1b}
/summon slavicmyths:chud_artisan ~ ~ ~ {ChudRole:1}
/summon slavicmyths:chud_artisan ~ ~ ~ {ChudRole:2}
/summon slavicmyths:chud_guard ~ ~ ~ {ChudRole:3}
/summon minecraft:zombie ~3 ~ ~
/summon minecraft:skeleton ~3 ~ ~
/summon minecraft:spider ~3 ~ ~
```

В городе проверить расписание, работу у домашнего рабочего места, реальные инструменты в руках, безопасную ходьбу/бегство, реакцию стражи. Торговля и её цены/лимиты должны остаться прежними. Игрок, отбивающийся от врага, не становится агрессором; удар по Чуди вызывает локальную реакцию на конкретного виновника, без вечной репутации. После 60 секунд без новых нападений и выхода из ближнего боя агрессия должна затухнуть. Профиль шахтёра продолжает продавать добычные руны. Перезаход не сбрасывает offers/uses. Проверяйте двух игроков отдельно.

## Руна, броня и голод

```mcfunction
/give @s slavicmyths:rune_ominous_aura 2
/give @s slavicmyths:runic_anvil
/give @s minecraft:iron_ingot 64
/give @s slavicmyths:silver_ingot 16
/give @s slavicmyths:ancient_sign 4
/give @s minecraft:diamond_helmet
/give @s minecraft:diamond_chestplate
/give @s minecraft:diamond_leggings
/give @s minecraft:diamond_boots
/give @s minecraft:iron_helmet
/give @s minecraft:iron_chestplate
/give @s minecraft:iron_leggings
/give @s minecraft:iron_boots
/give @s minecraft:shield
/experience add @s 100 levels
/data get entity @s foodExhaustionLevel
/data get entity @s foodSaturationLevel
/data get entity @s foodLevel
```

Установить через прежнюю наковальню по одной руне на два отдельных разрешённых предмета и надеть/держать их. Одна копия не защищает; две защищают от непровоцированных обычных hostile с **max** HP ≤50. Копии в рюкзаке не считаются, гнёзда не увеличиваются, duplicate rune в одном предмете запрещён прежними правилами. Проверить надевание второй руны уже во время преследования, снятие, ответ конкретного ударенного моба и независимость второго игрока. Руны не дают иммунитет к уже летящему снаряду/взрыву.

Кладовик/крупный, Warden/Wither/Dragon, max HP >50 и сущности из `slavicmyths:ominous_aura_immune` не подчиняются. Wizard/Ender Skeleton отсутствуют в установленном наборе модов: конкретные сторонние ID не выдуманы; при добавлении соответствующего мода его реальные ID нужно внести в этот расширяемый тег.

Без рун сравнить одинаковый бег/прыжки/атаки внутри 12 блоков от крупного во время боя и вне зоны: реальные затраты exhaustion умножаются ×3, ожидание не создаёт искусственного голода, два босса не дают ×9. В creative/spectator множителя нет. Тяжёлый удар проверить на полном обычном алмазе без чар/классовых бафов — расчётный результат 16.64 HP; камень на полном железе — 2.805 HP. Protection, toughness и щит остаются рабочими; это ещё требует проверки в игре.

## Лут и регрессия

```mcfunction
/loot give @s loot slavicmyths:entities/podzemnik
/loot give @s loot slavicmyths:entities/podzemnik_miner
/loot give @s loot slavicmyths:entities/kladovik
/loot give @s loot slavicmyths:entities/large_podzemnik
/sm depths generate
/sm depths status
/sm depths locate
```

Руна Зловещей ауры имеет независимый шанс 30% у крупного; единичное убийство не подтверждает вероятность. Проверить реальные дропы, одноразовую смерть/перезаход и отсутствие повторной награды. Проверки металлургии/торговли/аксессуаров — [прежний список 1.5.4](../industry-1.5.4/COMMANDS.md); геометрия/портал/карта В — [1.5.3.1](../ancient-depths-1.5.3.1/COMMANDS.md). Подземелья не перестраиваются ради мобов, энд-портал и рамки не затрагиваются.
