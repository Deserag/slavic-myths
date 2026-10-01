# CHECKLIST 0.8.1

DONE означает: реализация есть, compile/build и проверки данных пройдены.
Minecraft запусков **0** по заданию пользователя; визуальный и игровой QA — TODO.
Это не подтверждение игрового поведения посадки, тревоги, worldgen или перезахода.

## STRUCTURE

- Large Camp worldgen — DONE
- Forest layout — DONE
- Plains layout — DONE
- Hill layout — DONE
- Palisade — DONE
- Gates — DONE
- Towers — DONE
- Central yard — DONE
- Nightingale Yard — DONE
- Nightingale tree/platform — DONE

## BUILDINGS

- Barracks — DONE
- Forge — DONE
- Warehouse — DONE
- Stable — DONE
- Prison — DONE
- Ataman house — DONE
- Secondary commander area — DONE
- Utility buildings — DONE

## FURNITURE

- Table — DONE
- Chair — DONE
- Stool — DONE
- Bench — DONE
- Shelf — DONE
- Wardrobe — DONE
- Bedside cabinet — DONE
- Weapon rack — DONE
- Bag — DONE
- Wooden crate — DONE
- Firewood bundle — DONE
- Training dummy — DONE

## BANDITS

- Ataman #1 — DONE
- Ataman #2 — DONE
- Guards — DONE
- Patrol — DONE
- Training behaviour — DONE
- Smith behaviour — DONE

## ALERT

- Calm — DONE
- Suspicious — DONE
- Alert — DONE
- Signal bell — DONE
- Zoned propagation — DONE

## LOOT

- Barracks loot — DONE
- Forge loot — DONE
- Warehouse loot — DONE
- Ataman loot — DONE
- Secret cache — DONE
- Cache loot table — DONE

## STATE

- Active — DONE
- Cleared — DONE
- Respawn prevention — DONE
- Persistence — DONE

## LORE

- Mysterious feather — DONE
- Book entry — DONE
- Nightingale environmental hints — DONE

## COMMANDS

- locate large — DONE
- locate large next — DONE

## DATA

- Recipes — DONE
- Loot tables — DONE
- Models — DONE
- Textures — DONE
- Localization — DONE
- Tags — DONE
- Advancements — DONE
- Sounds — DONE

## RELEASE

- Compile — DONE
- Resource verification — DONE
- Clean build — DONE
- Reobf JAR — DONE
- README — DONE
- CONTINUE file — DONE
- PolyMC — IN_PROGRESS

## Ручная проверка — TODO

В отдельном мире/копии с cheats, Overworld:

```mcfunction
/slavicmyths locate bandit_camp large
/slavicmyths dev tp_bandit_camp large
/slavicmyths locate bandit_camp large next
/give @p slavicmyths:pine_chair
/give @p slavicmyths:linden_stool
/give @p slavicmyths:pine_bench
/give @p slavicmyths:linden_wardrobe
/give @p slavicmyths:pine_weapon_rack
/give @p slavicmyths:mysterious_black_feather
```

1. Осмотреть три семейства планировки в разных биомах; крыши, интерьер, проходы,
   лестницы башен, входные ступени и основания. Проверить казарму, кузницу, кухню,
   склад, конюшню, тюрьму и задний двор с пустым помостом. Соловья нет.
2. Сесть/встать, сломать занятое сиденье; поставить/сломать обе половины шкафа.
   Поместить в стойку меч, лук, щит и посох; забрать, сломать стойку, перезайти.
   Предмет не должен теряться/дублироваться; проверить второй клиент при возможности.
3. В survival дать часовому заметить игрока: короткая настороженность, движение
   к сигналу, местная тревога, затем соседние зоны. Сломать сигнал заранее и
   сравнить распространение. Дальний NPC не получает координаты игрока через record.
4. Проверить патруль, посты, тренировку/кузнеца; Атаман ворот 68 HP,
   внутренний 64 HP, разная скорость/экипировка. Малый/средний лагеря сохраняют 0/1 Атамана.
5. Победить обоих Атаманов и суммарно ≥18 из 26 защитников; проверить достижение,
   затихание тревоги и отсутствие повторного появления после выгрузки/перезахода.
6. Проверить тематический лут, иногда скрытый погреб (шанс 35%, два места), перо и
   статью «Свист в чаще». Освобождение пленника не даёт отдельной награды.

## Проверки данных

- 23 NBT, 8 комбинаций вариантов зданий: всегда 26 основных защитников,
  ровно два Атамана, пять сигнальных точек; отдельные индексы лошадей/пленников.
- Маркеры имеют пол/место для головы, лестницы — опоры; ссылки на блоки/loot валидны.
- 21 мебельный блок, 22 предмета с пером; модели, facing, половины шкафа, recipes,
  loot, ru/en; декоративная мебель без TileEntity, только стойка хранит предмет.
- Тайник: отдельные вероятностные пулы, редкая добыча не гарантирована.
- Ресурсы воспроизводимы; production reobf Java 8 JAR соответствует ресурсам.
- Фактические интервалы между станами, проходимость terrain и MSPT не измерялись.
