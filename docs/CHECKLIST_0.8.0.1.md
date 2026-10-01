# CHECKLIST 0.8.0.1

DONE = реализация, compile/build и статические/геометрические проверки пройдены.
Minecraft запусков **0**; игровой QA — TODO. Нельзя считать эти проверки запуском мира.

| Система | Статус |
|---|---|
| LINDEN TREE | DONE |
| LINDEN WOOD SET | DONE |
| LINDEN LEAVES | DONE |
| ROWAN TREE | DONE |
| ROWAN WOOD SET | DONE |
| ROWAN LEAVES | DONE |
| ROWAN BERRIES | DONE |
| WILLOW TREE | DONE |
| WILLOW WOOD SET | DONE |
| WILLOW LEAVES | DONE |
| HANGING WILLOW LEAVES | DONE |
| PINE TREE | DONE |
| PINE WOOD SET | DONE |
| PINE NEEDLES | DONE |
| WORLDGEN | DONE |
| SAPLINGS | DONE |
| AXE STRIPPING | DONE |
| RECIPES | DONE |
| TAGS | DONE |
| ADVANCEMENTS | DONE |
| BOOK | DONE |
| DEV COMMAND | DONE |
| BUILD | DONE |
| POLYMC | DONE — один JAR 0.8.0.1, SHA256 совпадает с production |
| Ручной игровой тест | TODO — выполняет пользователь |

## Проверено

- Четыре семейства: 69 блоков, 66 предметов, 48 новых рецептов; сохранены прежние ID.
- Полные blockstate схемы осей/дверей/лестниц/кнопок; loot с Fortune/Shears/Silk Touch,
  double slabs и нижней половиной двери; обычный предмет листвы не хранит урожай.
- Собственные текстуры: четыре различные alpha-маски листвы, кора, срезы, двери/люки.
- 256 seed на породу: ветви связаны, листья имеют цепочку к древесине, подвесы имеют опору.
  Диапазоны высот в выборке: липа 8–13, рябина 6–8, ива 7–8, сосна 13–22 блока.
- Ресурсы воспроизводятся побайтово; все новые ресурсы совпадают с production JAR.
- clean build / reobf / Java 8 PASS. Тестовая геометрия не включена в JAR.

## Краткий ручной тест

Отдельный тестовый мир или копия, cheats включены. Найдите большую свободную площадку.

```mcfunction
/slavicmyths dev growtree linden random
/slavicmyths dev growtree rowan random
/slavicmyths dev growtree willow random
/slavicmyths dev growtree pine random
/give @p slavicmyths:linden_sapling 8
/give @p slavicmyths:rowan_sapling 8
/give @p slavicmyths:willow_sapling 8
/give @p slavicmyths:pine_sapling 8
/give @p minecraft:bone_meal 64
/give @p minecraft:iron_axe
/give @p minecraft:shears
/give @p slavicmyths:linden_sign 4
```

1. Сравнить несколько вариантов четырёх пород рядом с oak/spruce; рассмотреть
   кору/срез/листву, силуэт сосны и свисающие полосы ивы. Пройти сквозь подвесы.
2. Посадить и вырастить каждую породу костной мукой; в тесном месте дерево должно
   отказать в росте без потери саженца и повреждения постройки. Топором снять кору
   с вертикального/горизонтального log/wood; проверить поворот, recipes и топливо.
3. Собрать красные гроздья рябины ПКМ, повторить сразу (повторного урожая нет),
   дождаться восстановления при свете и древесине рядом. Сравнить ножницы/Silk Touch.
4. Срубить иву, дождаться decay кроны и падения подвесов; листья с ножниц должны
   сохраняться после установки как vanilla persistent leaves.
5. Проверить наборы в Creative по порядку Linden → Rowan → Willow → Pine:
   ступени/плиты/ограды/калитки, двери/люки, кнопку/плиту; поставить стоячую и
   настенную табличку каждой породы, написать текст, сохранить и перезайти.
6. Исследовать новые Forest/Dark Forest, Taiga, Swamp и берега River; проверить
   умеренную частоту и отсутствие плавающих стволов. Получить древесину всех пород,
   проверить два достижения и статью «Деревья родной земли».

Ограничения: реальная частота worldgen, баланс random ticks, сохранение табличек,
визуальное качество и игровой рост не проверялись запуском. MSPT не измерялся.
Саженец и dev-команда допускают иву на обычном грунте; близость воды обязательна
только для естественной генерации. Новых биомов и переделки структур нет.
