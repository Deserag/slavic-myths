from pathlib import Path
import json,shutil
R=Path(__file__).resolve().parents[1];out=R/'docs/verification/visual-1.6.2';gate=json.loads((out/'resource-gates.json').read_text())
shutil.copy2(R/'work/visual-162/build.log',out/'build.log')
for subsystem in ['earth','sivka','firebird']:shutil.copy2(R/f'work/{subsystem}/native-preview.png',out/f'{subsystem}-native-preview.png')
report=f'''# Исправления 1.6.2 — 2026-10-10

По пользовательским скриншотам и сообщениям подтверждена необходимость исправления визуальных дефектов 1.6.1. Пользовательская игровая проверка выявила проблемы; она не является проверкой новой 1.6.2.

## Что исправлено

- Сивка: соседние перекрывающиеся cuboids имели совпадающие поверхности. Генератор теперь подбирает минимальное разнесение конфликтующих граней; скелет, кормление, полёт, X и registry IDs сохранены. 152 cuboids/51 bone, высота 2.975.
- Дух земли: убраны совпадающие грани и двойная отрисовка светящихся деталей. Локальная каменная текстура теперь содержит различимые камни/швы, а отдельные группы поверхности дополнительно используют настоящие vanilla cobblestone/mossy cobblestone/andesite block models. Они следуют суставам и стадиям появления/смерти, ничего не размещая в мире. Исправлены уже существовавшие испорченные русские субтитры (UTF-8/CP1251 mojibake).
- Жар-птица: длинная ступенчатая шея с отдельными перьями, более узкая голова/крючковатый клюв, семь направленных назад перьев хохолка, изменённый контур/оперение крыльев, расширенные хвостовые ленты. 182 bones/369 cuboids; standing source geometry 2.905 blocks, extended span 5.446. Ступня имеет три передних пальца с сочленёнными когтями и задний палец; наклон, складывание и движение лап в полёте реально обновляются по кадрам. Добавлено `firebird_spawn_egg`, existing creative category 8, RU/EN и template model. Egg-spawn по-прежнему не даёт природные ночные ресурсы.
- Смерть Духа: вместо сгибания тела поэтапно разрушаются корона, руки, грудь/торс и ноги. За пять стадий появляются 58 временных косметических блоков из 9 vanilla материалов: cobblestone, mossy cobblestone, stone, andesite, diorite, granite, cobbled deepslate, dirt, coarse dirt. Они движутся, вращаются в воздухе, оседают и исчезают; это не ItemEntity, не loot и не world placement. TTL ограничен 64…80 ticks; production smoke classes отсутствуют.
- Реакция Духа: полученный зарегистрированный удар назначает нападающего целью; вблизи ответный melee имеет приоритет, прерывается лишний подход к колонне, направление тела/взгляда обновляется также во время боевых фаз. Направление telegraph/hit фиксируется по aim, позволяя уклониться.
- Новое прямое решение пользователя отменяет старое arena-only правило редкой награды: алмазы 2…4, древние монеты 1…3 и руна III выдаются за победу и при призыве вне арены. Loot table сохраняет прежние обычные награды, снят `legitimate` запрет их выпадения; вне арены руна создаётся через custom death loot, в арене — через существующий one-time ledger. Обычный doMobLoot по-прежнему действует для native death loot.
- Подтверждён новый ориентир кулака: 7…9 HP после полной алмазной брони без зачарований (центр 8 HP, raw 20). Для сохранения тяжести остальных атак прежние цели крупного босса перенесены на ту же diamond-калибровку: колонна 13, splash 8, камень 5, волна 10.5, шипы 9. Слуги сохранены на 1.5 HP после железной брони. Никакого armor bypass Духу не добавлено; Protection/Resistance/absorption и руны продолжают снижать урон.

## Результаты

`gradlew.bat clean build` с четырьмя offline init scripts: PASS, 49s, 46 warnings. Earth 6186 checks, Sivka 7107 checks, Firebird 200020 assertions и Visual 8 regressions PASS. Последние проверяют actual native polygons (0 coplanar overlapping pairs у Сивки и Духа), actual vertex submission (светящиеся части Духа рисуются ровно один раз), настоящие bones/изменение позы лап Жар-птицы.

Resource/production gates: {gate['checks']} PASS; {gate['baseline_entries_preserved']} entries 1.6.1 сохранены. Полный список разрешённых изменений в resource-gates.json. Отдельные regression helpers не входят в JAR.

JAR: `build/libs/slavicmyths-1.6.2.jar`, SHA256 `{gate['sha256']}`.

Запусков Minecraft агентом: **0**; dedicated server запусков: **0**. Исправленная игра, реальные звуки/бой/смерть, визуальное совпадение с approved reference, FPS/MSPT/MP/relog: **NOT TESTED**. Source previews являются статическими проекциями; Earth preview не включает vanilla StonySkin render layer. Ни эти изображения, ни математический damage test не выдаются за игровые скриншоты или игровой бой.

## Краткая ручная проверка

1. Перезапустить тестовую 1.21.1; проверить версии и runtime logs. Старый мир не перезаписывать.
2. Сивка: осмотреть бок/гриву/хвост с разных расстояний на земле и в полёте; проверить отсутствие мерцания.
3. Дух из переносного камня: в Survival ударить, проверить разворот/ответный melee; diamond без enchantments должен получить 8 HP до иных эффектов. Отдельно проверить Protection, щит и тяжёлые атаки.
4. Убить такого босса, подобрать diamonds/coins/rune III; отдельно победить в реальной арене без двойной руны. Проверить распад на разные неподымаемые блоки и их cleanup.
5. Найти яйцо Жар-птицы в группе существ или `/give @s slavicmyths:firebird_spawn_egg`. Сверить тело/крылья/хвост с approved concept, folded pose, toe/claw movement в takeoff/flight/landing/retaliation.
6. Проверить ночь без shaders, натуральную награду, MP и повторный заход. Естественные расписание/бюджеты Жар-птицы и кормление Сивки не менялись.
'''
(out/'REPORT.md').write_text(report,encoding='utf8')
entry=f'''## Исправления Сивки, Духа земли и Жар-птицы 1.6.2 — 2026-10-10

По пользовательской игровой проверке: разнесены coplanar facets Сивки/Духа, устранён double draw emissive у Духа, добавлены каменная фактура/vanilla block clusters и поэтапный косметический распад на 58 блоков разных материалов. Жар-птица переработана (182 bones/369 cuboids), лапы с пальцами/когтями анимированы; **яйцо добавлено по новому прямому указанию**. Дух реагирует на нападающего и приоритетно отвечает melee. Пользователь отменил старое arena-only ограничение: **руна и обычные награды за победу вне арены тоже**. Кулак теперь 8 HP после diamond без enchantments; тяжёлые атаки на той же основе, слуги неизменны.

Clean build PASS (49s); Earth6186/Sivka7107/Firebird200020/Visual8 offline PASS, {gate['checks']} production/resource checks. Actual static geometry: 0 coplanar face pairs; glow vertices submitted once. **Запусков Minecraft агентом:0; исправленная runtime-приёмка NOT TESTED.** [Отчёт и ручная проверка](verification/visual-1.6.2/REPORT.md). Сохранены пользовательские изменения/registry IDs; commit не создавался.

'''
for name in ['PROJECT_STATUS.md','ROADMAP.md','ARCHITECTURE.md']:
 p=R/'docs'/name;p.write_text(entry+p.read_text(encoding='utf8'),encoding='utf8')
p=R/'docs/firebird/IMPLEMENTATION.md';p.write_text('> Обновление 1.6.2: по новому прямому указанию пользователя добавлено яйцо призыва; модель/лапы переработаны. Прежнее исключение яйца ниже описывает только 1.6.1. См. ../verification/visual-1.6.2/REPORT.md.\n\n'+p.read_text(encoding='utf8'),encoding='utf8')
print('1.6.2 documentation updated; runtime acceptance NOT TESTED.')
