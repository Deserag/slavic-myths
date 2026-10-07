# Slavic Myths 1.1.0 — Земледелие

## Implemented

Сохранено текущее рабочее состояние RC2. 7 культур (`rye`, `barley`, `oat`, `turnip`, `cabbage`, `pea`, `flax` с суффиксом `_crop`), 7 seeds, 7 harvest items, общий age 0..6 и стандартный random tick рост. Bone meal +1/2; коэффициенты 1.00, flax .90, cabbage .80. Отдельные текстуры каждой стадии, 49 crop sprites и 18 item sprites, все 32x32.

Loot age 0/1: seed 50%; age 2..5: seed 1; mature: produce/seeds по таблицам ZIP. Fortune +0..min(level,3) только produce. Sickle выдаёт mature loot одного mod/vanilla crop и сохраняет растение на age 1. Field hoe: поперечная полоса 3, sneak 1; расход durability на блок. Лейка: 8 зарядов, source refill без удаления воды, moisture=7 у vanilla farmland 3x3; возраст не меняет. Organic fertilizer: +1 age у immature supported crops 3x3, consume 1 только при изменении. Все изменения server-authoritative с проверкой доступа.

Seeds из grass: 12% short, 18% tall; additive GLM, ровно один uniformly chosen seed; intact-pair guard для tall grass. 4 recipes, 4 item tags + block harvest tag, enchantability tags, advancement с vanilla husbandry parent/no XP, ru/en и упорядоченные 18 предметов в существующей Creative Tab.

## Build

- Java 21, MC 1.21.1, NeoForge 21.1.255.
- `gradlew.bat clean build` — PASS, 43 s, 10 существующих deprecated warnings; `test NO-SOURCE` (unit tests не заявляются).
- После static проверки исправлены Fortune tag и tall grass JSON; `gradlew.bat processResources jar` — PASS, 19 s; compileJava UP-TO-DATE, повторной компиляции не было.
- `python tools/verify_farming.py --jar build/libs/slavicmyths-1.1.0.jar` — PASS: 4740 static checks + 166 production checks; 2075 JSON, 4225 model/texture links; Java class major 65, mod version 1.1.0, test code отсутствует в JAR.
- SHA256: `17ffe1b89b7d5a3899167b9d192b55ab61d8c613aa6c0a149b4068cfa1ab2956`.
- Minecraft запуски: **0**. Компиляция и static checks не заменяют gameplay/visual acceptance.

## PolyMC

`C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods/slavicmyths-1.1.0.jar` установлен и hash checked. Профиль: Slavic Myths 1.1.0 - NeoForge 1.21.1 TEST. Предыдущий RC2 и instance.cfg сохранены в `C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.slavicmyths-backups/pre-farming-1.1.0-17ffe1b89b7d`. Companion JARs не изменились; configs/saves/старый профиль 1.16.5 не затронуты.

## Git

`main`, исходный HEAD `741acac`; commit/push не выполнен автоматически в соответствии с прямым пользовательским AGENTS.md («Не делайте commit автоматически»). Пункт Git finish вложенного ZIP не имеет приоритета над этой инструкцией. Локальные изменения сохранены; release branches/tags/GitHub releases не менялись.

## Manual test checklist

1. В отдельном тестовом мире: 18 новых предметов в существующей вкладке, RU/EN названия и tooltip воды.
2. Посадка семи семян на farmland; запрет на dirt/grass и неверной почве.
3. Рост всех семи стадий: силуэты rye/barley/oat/turnip/cabbage/pea/flax, прозрачность; bone meal даёт 1/2 стадии, не выше 6.
4. Незрелый/mature дроп по ТЗ; Fortune I–III/V не увеличивает seeds, produce бонус максимум +3.
5. Серп: mature new/vanilla wheat/carrot/potato/beet loot, age 1, durability −1; immature no-op; нет AoE.
6. Полевая мотыга: полоса поперёк взгляда, sneak один блок, неподходящие/занятые позиции не меняются; износ по обработанным блокам.
7. Лейка: crafting 0/8, sneak refill source 8/8, source остаётся; 3x3 moisture 7, water −1, crop age не меняется; 0/8 no-op.
8. Сохранение воды в лейке после выбрасывания/подбора и реального перезахода.
9. Удобрение: +1 stage каждому immature crop в 3x3, зрелые не меняются; no-op без расхода; неподдерживаемые plants не растут.
10. Grass seed drops с обеих половин tall grass без двойного дропа; advancement по любому новому seed.
11. Репа/капуста/pea pod: hunger 2, saturation 1.6/1.2/1.2, обычное время еды/no effects; seeds/grains/flax не съедобны. Unbreaking/Mending на инструментах.

## Art reference / implementation note

ZIP прочитан целиком: README, farming specification и overview image. Текст имеет приоритет над концептуальными добавками картинки. По письменной стоимости лейки рецепт содержит 5 iron nuggets и bucket (исходная нарисованная строка давала 6). Offline contact sheet: `farming-assets.png`; это не скриншот Minecraft.
