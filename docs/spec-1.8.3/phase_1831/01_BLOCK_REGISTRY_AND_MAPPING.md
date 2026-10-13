# 1.8.3.1 — регистр 12 заражённых вариантов базовых ванильных блоков

## Референс
`../refs/01_approved_corrupted_vanilla_blocks.png` (относительно корня архива: `refs/01_approved_corrupted_vanilla_blocks.png`). Воспроизводить Minecraft-пиксельный дизайн референса, не скриншоты для UI. Цвет: исходный материал всё ещё различим под моровыми чёрно-багровыми трещинами.

## Ровно 12 вариантов — без лишнего контента
| Ванильный исходник | Новый ID `slavicmyths:*` | Русское отображаемое имя |
|---|---|---|
| `minecraft:dirt` | `mor_dirt` | Поражённая земля |
| `minecraft:grass_block` | `mor_grass_block` | Поражённый дёрн |
| `minecraft:stone` | `mor_stone` | Поражённый камень |
| `minecraft:cobblestone` | `mor_cobblestone` | Поражённый булыжник |
| `minecraft:sand` | `mor_sand` | Поражённый песок |
| `minecraft:gravel` | `mor_gravel` | Поражённый гравий |
| `minecraft:oak_log` | `mor_oak_log` | Поражённое дубовое бревно |
| `minecraft:oak_leaves` | `mor_oak_leaves` | Поражённая дубовая листва |
| `minecraft:oak_planks` | `mor_oak_planks` | Поражённые дубовые доски |
| `minecraft:birch_log` | `mor_birch_log` | Поражённое берёзовое бревно |
| `minecraft:sandstone` | `mor_sandstone` | Поражённый песчаник |
| `minecraft:obsidian` | `mor_obsidian` | Поражённый обсидиан |

## Состояния / форма / физика
- `mor_oak_log`, `mor_birch_log`: ось `AXIS` переносить в изменённый блок. Текстура торца и бока разная.
- `mor_oak_leaves`: сохранить свойства распада листвы (`DISTANCE`, `PERSISTENT`, `WATERLOGGED`, если есть в текущем API) и прозрачность. При конверсии сохранять эти значения; spread/leaf update vanilla совместимы.
- `mor_grass_block`: поддержать топ/side/bottom, снежное состояние при необходимости; не превращать все травяные поля мира без события.
- `mor_gravel`, `mor_sand`: падающие блоки; механика гравитации должна совпадать с прототипом, и при падении нельзя терять «мор».
- `mor_obsidian`: сохранить extreme hardness / mining tier / explosion resistance / portal-material tag там, где он есть; не сделать дешёвый проход сквозь обсидиан.
- Все блоки: эквивалентные mining tool tags, hardness, resistance, occlusion, light level (по умолчанию 0; видимую красноту сделать emissive-mask/частицами, а не сделать мир бесконечно ярким), sound type, slipperiness, fire/lava/water behavior, flammability (древесина/листва), opacity, piston reaction. Исключения **только** локальные status effects и заражённая текстура.
- Blockstates, block models, item models, loot tables, tags и ru_ru/en_us обязательно; Creative Tab — существующая группа мода, не отдельная вкладка.

## Что НЕ заражать в 1.8.3.1
- Bedrock, barrier, portal/end-portal frames, command blocks, structure/jigsaw blocks, moving pistons, spawners, личные хранилища и BlockEntity с данными. Эти объекты при соприкосновении Полоз может **разрушать безопасным vanilla путём** только если это допускает отдельный список из 1.8.3.2; никогда не «переименовывать» их в заражённый блок и не терять содержимое.
- Ступени, плиты, двери, заборы, все породы дерева, вода, лава, руды и множество модовых блоков: НЕ создавать сотни новых вариантов автоматически; при проходе Полоза они могут быть разрушены по правилам разрушения (если разрешено), но не заменены несуществующими infected registry блоками.
- Не менять глобально ванильные resource paths `assets/minecraft/...`.

## Лут блоков
При ручной добыче использовать базовые свойства дропа соответствующего vanilla блока и его enchantment/loot conditions; заражённый variant item не должен самостоятельно размножаться вне Creative. При Silk Touch заражённого блока выдавать **соответствующий чистый базовый ванильный блок**, с учётом его vanilla добычи; для камня без Silk Touch — обычный булыжник, для листвы обычные vanilla saplings/sticks и т. п. При разрушении боссом — обычное vanilla block breaking с `doTileDrops` / фактическими gamerules, без удвоения содержимого. Нельзя обменивать 1 заражённый блок на более ценный vanilla-материал.

## Мир после катастрофы
Поражённые блоки устойчивы в сохранении, после смерти Полоза остаются, до последующей ручной очистки/замены игроком. Спонтанной самораспространяющейся системы порчи в отсутствие босса нет. Отдельное очищающее зелье/механика в этом патче НЕ добавляются.
