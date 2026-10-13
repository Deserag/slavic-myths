# Арт и звук моровых форм 1.8.1

Прямые пользовательские эталоны сохранены в [пакете задания](../../tasks/1.8.1/references/README.md). SHA256 всех 39 файлов manifest проверены. Большие референсные изображения не входят в игровой JAR. «Мор» — авторская игровая интерпретация, не историческое название отдельного духа.

| Форма | Native геометрия и материал | Реальная текстура |
|---|---|---|
| Леший | Исходные joints/длинные руки, дополнительные ветви, кора плеч/груди, когти, корни ступней и свисающие полосы оливкового мха | `mor/leshy.png`, 64×64 |
| Полудница | Исходная высокая тонкая фигура, соломенные пряди с открытым лицом, колосья венка, рваные полотнища, почерневшие предплечья/пальцы; серп закреплён на кисти | `mor/poludnitsa.png`, 256×256 |
| Кикимора | Исходный небольшой сутулый rig, горб, пучки длинных тёмных волос, когти, рваные полосы одежды, красные нити крестом спереди/сзади | `mor/kikimora.png`, 256×256 |
| Огненный змей | Исходное число сегментов/длина и wingless rig; четыре угловатых рога, зубы на подвижной челюсти, красный гребень, светлое подбрюшье, три хвостовые ленты; оригинал/копии используют один renderer | `mor/fire_serpent.png`, 512×512 |

До изменения серпа его форма/размеры/материалы определены в ТЗ `mobs/02_poludnitsa/01_VISUAL.md`: короткое жатвенное орудие с загнутой кромкой, тёмной рукоятью, серебристым ребром и багровыми шипами. Используется прежняя геометрия короткого серпа, дополнительный конец рукояти/обратные шипы; это не длинная коса. Нового предмета оружия нет.

Все материалы нарисованы собственным детерминированным `tools/mor_assets.py` по UV реальных native cubes. Для каждого есть отдельная `_forming` стадия; 0–15ticks использует обычную геометрию/текстуру с transform pose, 16–34 — моровую геометрию с тёмными пятнами, 35–50 — финальную текстуру. Обычные atlases сохранены. Склянка — прозрачный 16×16 пиксельный splash sprite с тёмной жидкостью/пробкой/алой риской. Отдельные реальные sprites корня/снаряда и 4px квадратных частиц; частицы fade12ticks. Root — native transient cuboids, не поставленные блоки.

## Звуковые источники

Новых OGG/пустых событий нет. Использованы напрямую установленные vanilla SoundEvents Minecraft 1.21.1. Файлы других игр/модов не копировались; прежний `sounds.json` побайтно сохранён. Это ссылки на ресурсы Minecraft, не перераспространение аудиофайлов.

| Форма/приём | Actual vanilla event | Момент |
|---|---|---|
| Леший | `block.wood.break`, `block.rooted_dirt.break`, `block.wood.step`, `entity.ravager.roar` | ambient12s; transform0/50; roots0/24; wail12 |
| Полудница | `block.grass.step`, `block.grass.break`, `item.axe.scrape`, `block.beacon.power_select`, `entity.breeze.wind_burst`, `entity.player.attack.sweep`, phantom hurt/death | ambient12s; transform0/50; flash0/20; harvest warning0/dash14/swings19/25 |
| Кикимора | silverfish ambient/hurt/death, `item.crossbow.loading_middle`, `entity.witch.celebrate`, `block.wool.step`, `block.tripwire.attach` | ambient12s; transform0/50; dance0/10/16/22; threads0/16 |
| Змей/копии | blaze ambient/hurt/death/shoot, fire ambient/extinguish, phantom flap, `item.elytra.flying`, `entity.illusioner.mirror_move` | ambient12s; transform0/50; flame0/22/27/32; dive0/18; split0/30 |
| Склянка | `entity.splash_potion.throw`, `entity.splash_potion.break` | use/one impact |

В `MorAudio` volume≤0.95, pitch .75–.85; общий hit cue ограничен один раз за приём. Реальные namespaces подтверждены cached vanilla sound catalogue/установленным Java API. Верхнеуровневые оригинал и клоны используют одинаковые звуки/pitch, атласы, renderer и частоты частиц. Idle не проигрывается одновременно старым ambient timer и новым контроллером.

## Проверка

Реальные ModelPart cubes/UV и конечные vertex coordinates проверены в 69 offline profiles, включая ordinary geometry из production 1.7.9, transform, windup/release/recovery. [Леший](leshy-offline-views.png), [Полудница](poludnitsa-offline-views.png), [Кикимора](kikimora-offline-views.png), [змей](fire_serpent-offline-views.png) — собственный offline rasterizer реальных UV meshes, **не скриншоты Minecraft**. Внешний вид в игровом освещении, анимации и прослушивание: **REQUIRES_MANUAL_QA**.
