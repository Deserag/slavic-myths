"""Record factual build/deployment state without changing prior feature reports."""
from pathlib import Path
import json,shutil
root=Path('.');out=root/'docs/verification/farming-1.1.0'
static=json.loads((out/'static-checks.json').read_text());install=json.loads((out/'polymc-installation.json').read_text(encoding='utf-8-sig'))
sections={
'PROJECT_STATUS.md':'''## 1.1.0 Земледелие — реализовано локально, установлено в PolyMC (2026-10-07)

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

''',
'ROADMAP.md':'''## 1.1.0 Земледелие — локальная реализация завершена (2026-10-07)

- [x] Семь farmland crops, семь стадий, разные силуэты; семена/продукты/4 хозяйственных инструмента.
- [x] Возрастной loot, capped Fortune, grass seed injection, data component лейки, ru/en/recipes/tags/advancement/Creative Tab.
- [x] Один clean compile/build, финальная упаковка JSON, статическая проверка и production JAR; установка в тестовый PolyMC.
- [ ] Ручная игровая/визуальная приёмка пользователем. Minecraft не запускался.
- [ ] Commit/push `main` — не выполнен автоматически в соответствии с AGENTS.md.
- Мельница, мука, переработка льна, дополнительные GUI/культуры/механики в этой итерации не добавлены. Существующий RC2 сохранён.

''',
'ARCHITECTURE.md':'''## 1.1.0 farming integration (2026-10-07)

`org.slavicmyths.farming` содержит общий `FarmingCrop` (собственный AGE 0..6, lazy seed supplier, коэффициент growth gate, vanilla CropBlock randomTick/NeoForge hooks). Посадка проверяет vanilla farmland; no collision/no block entity/no global polling. Vanilla runtime/data registries дополнены через существующие ModBlocks/ModItems/ModLoot.

Четыре item-use механики выполняют изменения только на сервере, проверяют mayInteract/mayUseItemAt и не загружают чужие chunks. SickleItem получает стандартный Block.getDrops с реальным инструментом, меняет age на 1, затем выдаёт loot и изнашивает инструмент. FieldHoeItem использует стандартную HOE_TILL ability/NeoForge modification hook и отдельные проверки каждой позиции в полосе; coarse/rooted dirt → farmland при свободном верхнем блоке. Это Item с точной durability 350: TieredItem иначе перезаписал бы её iron-tier значением 250.

`ItemState.WATER_CHARGES` — Codec.intRange(0,8), persistent + network synchronized. WateringCanItem содержит default component 0, fills from source fluid without removal, modifies only FarmBlock.MOISTURE. FertilizerItem использует `sickle_harvestable` tag (7 mod/4 vanilla crops), increments each immature crop exactly once, does not call vanilla bonemeal and does not consume on no-op.

`GrassSeedsModifier` добавляет один seed в existing loot; вероятность/table identity/валидная пара tall grass задаются JSON. `CappedFortune` — зарегистрированная loot function в Registries.LOOT_FUNCTION_TYPE, применяется один раз к mature produce, +0..min(Fortune,3); seeds unaffected. Дроп остальных возрастов полностью описан loot tables. Blockstates/model textures содержат ровно 7 стадий. ClientSetup помечает новые блоки cutout только в client-side классе.

`tools/farming_resources.py` воспроизводит актуальные 1.1.0 ресурсы без изменения RC2 UI/wood/weapon assets; `tools/verify_farming.py` проверяет спецификацию данных, прозрачность/размеры/различимость, JSON/ссылки и production JAR без Minecraft. Исходная схема рецепта лейки содержит 6 I, но письменная стоимость 5 nuggets; разрешённый альтернативный нижний ряд ` I ` сохраняет стоимость 5 nuggets + bucket.

'''}
for name,section in sections.items():
    p=root/'docs'/name;s=p.read_text(encoding='utf-8');heading=s.find('\n')+1
    if section.split('\n')[0] not in s:s=s[:heading]+'\n'+section+s[heading:]
    p.write_text(s,encoding='utf-8')
lockpath=root/'packaging/test-pack-lock.json';lock=json.loads(lockpath.read_text(encoding='utf-8'))
lock['slavicMyths'].update(version='1.1.0',filename='slavicmyths-1.1.0.jar',sha256=static['sha256'],installedPath=install['jar'],status='INSTALLED_NOT_LAUNCHED',localCorrection='1.1.0 Farming over current RC2 UI Visual FINAL')
lock['deploymentStatus']='INSTALLED_NOT_LAUNCHED';lockpath.write_text(json.dumps(lock,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
report=f'''# Slavic Myths 1.1.0 — Земледелие

## Implemented

Сохранено текущее рабочее состояние RC2. 7 культур (`rye`, `barley`, `oat`, `turnip`, `cabbage`, `pea`, `flax` с суффиксом `_crop`), 7 seeds, 7 harvest items, общий age 0..6 и стандартный random tick рост. Bone meal +1/2; коэффициенты 1.00, flax .90, cabbage .80. Отдельные текстуры каждой стадии, 49 crop sprites и 18 item sprites, все 32x32.

Loot age 0/1: seed 50%; age 2..5: seed 1; mature: produce/seeds по таблицам ZIP. Fortune +0..min(level,3) только produce. Sickle выдаёт mature loot одного mod/vanilla crop и сохраняет растение на age 1. Field hoe: поперечная полоса 3, sneak 1; расход durability на блок. Лейка: 8 зарядов, source refill без удаления воды, moisture=7 у vanilla farmland 3x3; возраст не меняет. Organic fertilizer: +1 age у immature supported crops 3x3, consume 1 только при изменении. Все изменения server-authoritative с проверкой доступа.

Seeds из grass: 12% short, 18% tall; additive GLM, ровно один uniformly chosen seed; intact-pair guard для tall grass. 4 recipes, 4 item tags + block harvest tag, enchantability tags, advancement с vanilla husbandry parent/no XP, ru/en и упорядоченные 18 предметов в существующей Creative Tab.

## Build

- Java 21, MC 1.21.1, NeoForge 21.1.255.
- `gradlew.bat clean build` — PASS, 43 s, 10 существующих deprecated warnings; `test NO-SOURCE` (unit tests не заявляются).
- После static проверки исправлены Fortune tag и tall grass JSON; `gradlew.bat processResources jar` — PASS, 19 s; compileJava UP-TO-DATE, повторной компиляции не было.
- `python tools/verify_farming.py --jar build/libs/slavicmyths-1.1.0.jar` — PASS: {static['checks']} static checks + {static['productionChecks']} production checks; {static['jsonFiles']} JSON, {static['references']} model/texture links; Java class major 65, mod version 1.1.0, test code отсутствует в JAR.
- SHA256: `{static['sha256']}`.
- Minecraft запуски: **0**. Компиляция и static checks не заменяют gameplay/visual acceptance.

## PolyMC

`{install['jar']}` установлен и hash checked. Профиль: Slavic Myths 1.1.0 - NeoForge 1.21.1 TEST. Предыдущий RC2 и instance.cfg сохранены в `{install['backup']}`. Companion JARs не изменились; configs/saves/старый профиль 1.16.5 не затронуты.

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
'''
(out/'REPORT.md').write_text(report,encoding='utf-8')
for source,target in [('work/farming-1.1.0-clean-build.log','clean-build.log'),('work/farming-1.1.0-resource-packaging.log','resource-packaging.log')]:shutil.copy2(source,out/target)
files=[]
files.extend(root.glob('src/main/java/org/slavicmyths/farming/*.java'))
for p in ['gradle.properties','src/main/java/org/slavicmyths/SlavicMyths.java','src/main/java/org/slavicmyths/client/ClientSetup.java','src/main/java/org/slavicmyths/item/ItemState.java','src/main/java/org/slavicmyths/registry/ModItems.java','src/main/java/org/slavicmyths/registry/ModBlocks.java','src/main/java/org/slavicmyths/registry/ModLoot.java','src/main/java/org/slavicmyths/registry/ModItemGroup.java','packaging/test-pack-lock.json']+[f'docs/{n}' for n in sections]+['tools/farming_integration.py','tools/farming_resources.py','tools/verify_farming.py','tools/farming_finish_docs.py']:files.append(root/p)
res=root/'src/main/resources'
crops=['rye','barley','oat','turnip','cabbage','pea','flax'];ids=[c+'_seeds' for c in crops]+['rye_grain','barley_grain','oat_grain','turnip','cabbage','pea_pod','flax_stalk','sickle','field_hoe','watering_can','organic_fertilizer']
for c in crops:
    files.extend(res.glob(f'assets/slavicmyths/models/block/{c}_crop_stage*.json'));files.extend(res.glob(f'assets/slavicmyths/textures/block/{c}_crop_stage*.png'))
    files.append(res/f'assets/slavicmyths/blockstates/{c}_crop.json');files.append(res/f'data/slavicmyths/loot_table/blocks/{c}_crop.json')
for id in ids:
    files.extend([res/f'assets/slavicmyths/models/item/{id}.json',res/f'assets/slavicmyths/textures/item/{id}.png'])
for name in ['sickle','field_hoe','watering_can','organic_fertilizer']:files.append(res/f'data/slavicmyths/recipe/{name}.json')
for name in ['agricultural_seeds','cereal_grains','raw_vegetables','farm_products']:files.append(res/f'data/slavicmyths/tags/item/{name}.json')
for name in ['durability','mining','mining_loot']:files.append(res/f'data/minecraft/tags/item/enchantable/{name}.json')
for name in ['short_grass','tall_grass']:files.append(res/f'data/slavicmyths/loot_modifiers/{name}_farming_seeds.json')
for path in ['assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','data/neoforge/loot_modifiers/global_loot_modifiers.json','data/slavicmyths/tags/block/sickle_harvestable.json','data/slavicmyths/advancement/new_seeds.json']:files.append(res/path)
files.extend(out.glob('*'))
names=sorted(set(p.as_posix() for p in files));(out/'changed-files.txt').write_text('\n'.join(names)+'\n',encoding='utf-8')
print(f'Documents and deployment lock updated; {len(names)} scoped files recorded.')
