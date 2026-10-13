"""Report actual implementation/gates/installation without claiming gameplay validation."""
from pathlib import Path
import json,re,hashlib
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/serpent-hill-1.9.5'
g=json.loads((O/'resource-gates.json').read_text(encoding='utf-8'));i=json.loads((O/'polymc-installation.json').read_text(encoding='utf-8-sig'))
assert g['status']==i['status']=='PASS' and g['sha256']==i['sha256']
intro=f"""## Змеиная гора — 1.9.5, 2026-10-13

Реализована редкая единая структура Overworld 128×144×128: проклятый холм, крепость, два различных входа, связанная сеть тоннелей/пещер, внутренние лестницы, двор, залы, четыре башни с ярусами, галерея, оружейная, разрушенная комната и заблокированный путь к будущему логову. Одна детерминированная HillPlan используется native worldgen и операторской командой. 11 блоков с моделями/32px текстурами, 11 рецептов, пять собственных OGG, лут и сказание.

Команда /slavicmyths generate_serpent_hill [x y z] [rotation] размещает комплекс поверх обычных блоков в авторском объёме, сохраняет bedrock, проверяет высоту/границу мира. Работа распределена по 4096 проверкам клеток на серверный тик; очередь и cursor сохраняются. Глубинный Мор наносит исходные 3 HP каждые 30 тиков с обычной обработкой брони. Существующий mor_ward в любом слоте инвентаря или активный WARD_EFFECT полностью отключает эту ауру. Две детали 1.9.4 защитой не считаются; готовый оберег не добавлен.

Чистая сборка, восемь offline наборов и {g['checks']} production/resource gates — PASS. Статический граф подтверждает маршруты между входами и доступность 15 именованных помещений/башен; это не игровой pathfinding. PolyMC Testing обновлён на 1.9.5; {i['other_files_preserved']} других файлов сохранены побайтно. SHA256: {g['sha256']}. Клиент/сервер/PolyMC/GameTest не запускались: 0 реальных запусков. Команда в superflat, natural seed generation, MP, runtime light/visual/audio и MSPT/FPS — NOT_RUN / NOT_MEASURED. [Отчёт](verification/serpent-hill-1.9.5/REPORT.md).

"""
for name in ['PROJECT_STATUS.md','ROADMAP.md','ARCHITECTURE.md']:
 p=R/'docs'/name;old=p.read_text(encoding='utf-8')
 if not old.startswith('## Змеиная гора — 1.9.5'):p.write_text(intro+old,encoding='utf-8')
report=f"""# Змеиная гора — 1.9.5

Статус реализации: **Complete; игровая приёмка NOT_RUN**. Реализовано ТЗ из docs/specs/1.9.5. Шесть reference PNG прочитаны как художественные ориентиры, не как готовые текстуры или буквальные NBT. Target: Minecraft 1.21.1, NeoForge 21.1.255, Java 21, official mappings, modid slavicmyths.

## Структура

Единый SerpentHillStructure с одним сериализуемым SerpentHillPiece и общей HillPlan. Размер 128×144×128. Процедурная масса холма, каменистые ступенчатые склоны, изогнутые моровые жилы, мёртвые деревья, руины и укреплённые внешние лестницы составляют один объект с крепостью. Нативный postProcess пишет только clip текущего чанка и сохраняет bedrock. Неавторские углы bounding box не очищаются. Это полноценная программная генерация геометрии, а не пустой NBT/только surface feature.

Нижний вход — камерный проход в склоне около local (60,13,114); верхний — массивные восточные ворота local (97,81,82). Внешняя лестница и отдельный внутренний подъём связывают уровни. Внизу есть спуск, коридоры, ритуальная и заражённая пещеры; вверху — входной зал, главный коридор, внутренний зал, двор, ритуальная камера, оружейная/склад, разрушенная боковая комната, четыре лестничные башни, gallery level 92 и верхние площадки. Красные знамёна, трёхглавые барельефы, тёплые/моровые огни и отдельные синие факелы. Будущий путь перекрыт кладкой; за ним нет полноценной арены, босса или boss loot.

Natural placement: Overworld, RandomSpread spacing 80 / separation 32, подходящий biome tag, 25 ограниченных land/fluid probes, отсечение слишком большого перепада высот. Комплекс уступает принятым существующим vanilla/mod footprints; placement исключает strongholds на 12 чанков. В старом collision helper новая структура исключена из зависимостей старых planners, чтобы не возникала рекурсия и не менялась их прежняя генерация. Глобальных обходов мира и forced chunk tickets нет.

## Команда

Синтаксис: **/slavicmyths generate_serpent_hill** либо **/slavicmyths generate_serpent_hill x y z [rotation]**.

Повороты: none, clockwise_90, clockwise_180, counterclockwise_90. Координаты поддерживают стандартные относительные значения ~. Требуется permission level 2; только Overworld. X/Z задают минимальный угол bounding box, Y — уровень основания. Без аргументов используется позиция оператора с основанием на 12 блоков ниже, ограниченным высотой мира. Некорректная по высоте Y заменяется безопасным уровнем; слишком низкий world height и выход за world border отклоняются до записи.

Пример для отдельного superflat теста: сначала отойти от важных построек, затем выполнить **/slavicmyths generate_serpent_hill**. Для выбранного места: **/slavicmyths generate_serpent_hill ~100 ~-12 ~100 clockwise_90**.

Обычные блоки внутри авторского объёма заменяются, включая воздух тоннелей. Bedrock не заменяется. Постройка идёт с бюджетом 4096 проверок клеток на серверный тик, одна активная очередь на мир. Сохраняются cursor/seed/origin/rotation, native start и ссылки чанков; после перезапуска незавершённая очередь может продолжиться. Checkpoint записывается по завершении каждого 16×16 вертикального tile. Игровые длительность генерации и MSPT не измерялись; бюджет не является доказательством отсутствия лагов.

## Опасная зона и блоки

HillWorld хранит instance metadata и локальный chunk index. Native population передаёт регистрацию индекса на server thread; обнаружение дополнительно использует только загруженные native references. Индивидуальная проверка ауры выполняется каждые 30 тиков в глубоких областях и внутренней осквернённой камере. Исходный урон 3 HP проходит обычную обработку сопротивления/брони. Багровые частицы и предупреждение показывают опасность; глубокий ambient ограничен общей частотой на instance.

Защита: **существующий Quest185Content.WARD (mor_ward)** в любом слоте инвентаря, включая переименованный stack, или его действующий WARD_EFFECT. Это выбранная совместимая трактовка зависимости ТЗ на уже существующий оберег. Две части амулета 1.9.4, зимние реликвии и обычный оберег огня не заменяют эту защиту. Рецепт будущего составного амулета не добавлялся, старая механика mor_ward не менялась.

Все 11 блоков доступны в существующей Creative Tab: serpent_masonry, cracked_serpent_masonry, ruined_serpent_masonry, serpent_bas_relief, ancient_ritual_slab, defiled_ritual_slab, serpent_torch, ritual_brazier, defiled_ritual_brazier, ancient_hanging_lamp, mor_crystal. Полные voxel models/blockstates/item models/loot, 32px PNG, RU/EN. Факел имеет standing/wall states и проверку опоры, плиты — плоскую форму, жаровни/кристалл/подвесной светильник различимы по геометрии. Полная кладка сохраняет occlusion; декоративные формы используют свои collision/selection shapes.

Уровни света: факел 12; обычная жаровня 15; осквернённая 10; подвесной светильник 12; кристалл 7; осквернённая плита 4; барельеф 2. Это обычный Minecraft light; цвет читается через текстуры и частицы, без shader dependency. 11 рабочих shapeless recipes используют vanilla blackstone/iron/coal/redstone/amethyst и новую кладку. Пять оригинальных нормализованных OGG; placement sounds наследуют stone/metal/wood.

Шесть атмосферных сундуков имеют seeded vanilla loot: умеренные материалы/еда/блоки и существующие расходники. Нет новых boss rewards или переноса сюжетных половин в сундуки. Новое сказание описывает игровую крепость и опасность.

## Фактические проверки

gradlew.bat clean build — **PASS**. Восемь offline suites — **PASS**: Hill {g['offline_checks']}, Guardians 6429, Sanctuary 1392, Karachun 40, Morozko 6946, quest185 182, Poloz 4290, mor-blocks 120.

Hill: повороты и обратное преобразование координат, единые bounds, height fallback, spatial index/NBT и resume cursor, допустимая vanilla palette/facing. На seed 195 проверен статический граф клеток с двухблочной clearance и шагом до одного блока: {g['reachable_cells_static']} достижимых позиций, оба входа и все 15 именованных помещений/башен, недоступность клетки за закрытым будущим проходом. {g['solid_voxels_seed195']} non-air voxels. Это не реальный Minecraft pathfinding, не natural seed test и не реальный restart.

Production/resource gates **{g['checks']} PASS**: Java 21 class version, совпадение JAR с актуальными классами/ресурсами, no smoke/test code, воспроизводимость генератора, наличие моделей/PNG/OGG/recipes/lang, связность resource IDs, bytecode event subscribers, общая сторона без client imports. Проверено сохранение старых ресурсов/переводов относительно **фактически установленной свежей 1.9.4** SHA256 {g['baseline_sha256']}; новый JAR не откатывает её ресурсы.

Production JAR: build/libs/slavicmyths-1.9.5.jar. SHA256 **{g['sha256']}**. [Машинный отчёт](resource-gates.json), [реальные данные layout](layout.json), [offline preview](offline-preview.png). Preview — схематическая проекция surface/section и текстур, не игровой скриншот и не утверждение визуальной идентичности.

PolyMC Testing обновлён после gates: установлен один JAR 1.9.5, {i['other_files_preserved']} остальных файлов сохранены побайтно. Backup: {i['backup']}. Миры/конфигурации/чужие моды и старый профиль 1.16.5 не затронуты. [Квитанция](POLYMC_INSTALLATION.md).

Реальных запусков клиента/сервера/PolyMC/GameTest: **0**. Команда в реальном superflat, natural generation на seed, bedrock readback в игровом мире, реальный restart/unload, MP, aura health/effect/inventory, light/visual/audio — **NOT_RUN**. MSPT/FPS — **NOT_MEASURED**. Горыныч, финальный бой/награды, новые мини-боссы, измерения, rank/rune rebalance и готовый древний амулет не реализовывались.

## Короткая ручная проверка

1. На отдельном superflat мире вызвать команду без аргументов; дождаться завершения. Проверить четыре поворота, bedrock, максимальную/невалидную Y и world border. Команда заменяет обычные блоки: не использовать на пользовательских постройках.
2. Пройти оба входа отдельно, внутренний подъём, пещеры, двор, комнаты, башни/галерею; убедиться, что глубокий проход закрыт и полноценной арены нет.
3. В Survival проверить сильную ауру без защиты, с mor_ward в инвентаре (включая renamed/offhand) и после его активации. В MP защита каждого игрока независима.
4. Проверить крафты, wall/standing torch, свет, модели и звуки; исследовать natural generation в новых чанках и сделать реальный restart незавершённой command job на тестовой копии.
5. Измерить генерацию/нагрузку и проверить runtime logs. Не заменять пользовательские миры тестовыми.
"""
(O/'REPORT.md').write_text(report,encoding='utf-8');(O/'03_REPORT.md').write_text(report,encoding='utf-8')
(O/'CHECKLIST.md').write_text("# Приёмка 1.9.5\n\n- [x] Единый native complex / программная геометрия, два входа и интерьеры.\n- [x] Ограниченная операторская команда, height/border/bedrock guards, persisted cursor.\n- [x] 11 блоков/рецептов, световые свойства, реальные модели/PNG/OGG/lang/loot.\n- [x] Персональная aura check с существующим mor_ward, без будущего босса/амулета.\n- [x] Clean build, восемь offline suites, resource/JAR gates, установка PolyMC.\n- [ ] Реальные superflat command, natural seeds, MP/ward effects и restart.\n- [ ] Runtime visual/light/audio/physics и измеренные MSPT/FPS.\n\nРеальных запусков 0. Матрица: docs/specs/1.9.5/04_ACCEPTANCE.md. Подробнее REPORT.md.\n",encoding='utf-8')
(O/'CONTINUE.md').write_text("# Продолжение\n\nСначала ручная приёмка по REPORT.md и docs/specs/1.9.5/04_ACCEPTANCE.md на отдельном superflat/test world. Relevant code: serpenthill/, HillPlan shared by native and command, generator hill_assets.py, tests hill-tests/. Не повторять полный аудит. Не считать статический cell graph игровой проверкой. Горыныча/финальный амулет/бой добавлять только по следующему отдельному ТЗ.\n",encoding='utf-8')
p=R/'README.md';s=p.read_text(encoding='utf-8');s=re.sub(r'\*\*Версия \d+\.\d+\.\d+ ·','**Версия 1.9.5 ·',s,count=1);s+="\n\n1.9.5: единая Змеиная гора с крепостью, двумя входами, интерьерами, 11 блоками/рецептами и опасной аурой. Команда: /slavicmyths generate_serpent_hill [x y z] [rotation]. PolyMC Testing обновлён; игровых запусков 0. [Отчёт](docs/verification/serpent-hill-1.9.5/REPORT.md).\n";p.write_text(s,encoding='utf-8')
p=R/'CHANGELOG.md';p.write_text("# 1.9.5 — Змеиная гора\n\nЕдиный редкий Overworld complex, два входа и связанные интерьеры, операторская очередь генерации, 11 блоков/рецептов, свет/звук и индивидуальная опасная аура с существующим mor_ward. Build/offline/resource gates PASS; PolyMC Testing обновлён. Игровых запусков 0.\n\n"+p.read_text(encoding='utf-8'),encoding='utf-8')
spec=R/'docs/specs/1.9.5';write={p.relative_to(spec).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in spec.rglob('*') if p.is_file()};(O/'spec-sha256.json').write_text(json.dumps(write,indent=2)+'\n',encoding='utf-8')
print('HILL_DOCUMENTATION_PASS')
