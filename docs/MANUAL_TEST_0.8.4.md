# Ручной QA 0.8.4 — TODO

Minecraft запусков агентом: 0. Использовать новый тестовый мир или новые чанки.

## Быстрый доступ

```text
/slavicmyths dev tp_kurgan small
/slavicmyths dev tp_kurgan warrior
/slavicmyths dev tp_kurgan great
/slavicmyths locate kurgan small next
/slavicmyths locate kurgan warrior next
/slavicmyths locate kurgan great next
/give @s slavicmyths:burial_log_coffin
/give @s slavicmyths:ancient_carolingian_sword
/give @s slavicmyths:restored_carolingian_sword
/give @s slavicmyths:ancient_chekan
/give @s slavicmyths:chekan
/give @s slavicmyths:ancient_spear
/give @s slavicmyths:restored_spear
/give @s slavicmyths:lunula
/give @s slavicmyths:grivna
/give @s slavicmyths:grave_ward
/give @s slavicmyths:ancient_fibula
/give @s slavicmyths:ancient_comb
/give @s slavicmyths:ancient_beads
/give @s slavicmyths:old_buckle
/give @s slavicmyths:pottery_fragment
/give @s slavicmyths:old_arrowhead
/give @s slavicmyths:darkened_log
```

1. Все три кургана: округлая земляная форма, высоты примерно 10/18/30, старые
   прерывистые дорожки, входы, опоры, деревья, метки, отсутствие летающих частей.
   Проверить границы чанков и следующий экземпляр через next; teleport безопасен.
2. Колода: placement во всех направлениях, занятая соседняя клетка, крышка/звуки,
   ровно 5 слотов, shift-click, два игрока одновременно, закрытие при выходе одного.
   Сломать FOOT и HEAD в survival/creative — нет дубликатов предмета и содержимого.
   Перезаход с вещами и при открытом GUI, выгрузка чанка, ломание при открытом меню.
3. Четыре вида содержимого. Для быстрого просмотра на FOOT домашней колоды:
   `/data merge block X Y Z {Remains:1}`; затем 0/2/3. Перезагрузить чанк для обновления
   изображения после прямой NBT-команды. Это визуальный тест, не natural ownership.
   У естественной колоды проверить Barrow/Open data до/после открытия; дома Barrow нет.
4. Крафт колоды; древние находки в нишах могут отсутствовать, оружие/украшения редкие,
   в одной колоде максимум две находки. Останки не гарантируют loot.
5. Над горящим обычным костром поставить log/planks с 1–5 клетками вертикального
   смещения. Подождать 1–4 минуты. Погасить/снова зажечь, перекрыть дым, перезайти.
   Блок рядом не изменяется. AXIS бревна сохраняется. Сломать/поставить заново:
   обработка начинается заново. Проверить рецепты всех девяти darkened вариантов.
6. Оружие: разные силуэты/размеры в руках и GUI, копьё длинное, чекан с узким клювом;
   древние варианты слабее восстановленных. Три восстановления на Столе оружейника,
   обычный рецепт чекана. Чекан сильнее против брони без полного её игнорирования.
7. Curios: лунница/гривна в necklace, оберег в charm; эффект только надетого предмета.
   Ночью/днём сравнить лунницу, для оберега — зомби/скелета против животного/разбойника.
   Нет Night Vision. Проверить RU/EN имена, tooltips и optional JEI recipes.
8. Отдельный сервер/два игрока: GUI, lid sync, единственный loot, сохранение,
   отсутствие новых mobs/проклятий при открытии. Частоту worldgen оценить вручную.
