# Материалы и ID 0.9.6

Исходные восемь материалов и их ID сохранены. Добавлены 26 блоков и 26 BlockItem.

- `slavicmyths:pale_blue_kurgan_stone_slab`
- `slavicmyths:pale_blue_kurgan_stone_stairs`
- `slavicmyths:pale_blue_kurgan_stone_wall`
- `slavicmyths:pale_blue_kurgan_cobblestone_slab`
- `slavicmyths:pale_blue_kurgan_cobblestone_stairs`
- `slavicmyths:pale_blue_kurgan_cobblestone_wall`
- `slavicmyths:cracked_pale_blue_kurgan_cobblestone_slab`
- `slavicmyths:cracked_pale_blue_kurgan_cobblestone_stairs`
- `slavicmyths:cracked_pale_blue_kurgan_cobblestone_wall`
- `slavicmyths:mossy_pale_blue_kurgan_cobblestone_slab`
- `slavicmyths:mossy_pale_blue_kurgan_cobblestone_stairs`
- `slavicmyths:mossy_pale_blue_kurgan_cobblestone_wall`
- `slavicmyths:bog_green_kurgan_stone_slab`
- `slavicmyths:bog_green_kurgan_stone_stairs`
- `slavicmyths:bog_green_kurgan_stone_wall`
- `slavicmyths:bog_green_kurgan_cobblestone_slab`
- `slavicmyths:bog_green_kurgan_cobblestone_stairs`
- `slavicmyths:bog_green_kurgan_cobblestone_wall`
- `slavicmyths:cracked_bog_green_kurgan_cobblestone_slab`
- `slavicmyths:cracked_bog_green_kurgan_cobblestone_stairs`
- `slavicmyths:cracked_bog_green_kurgan_cobblestone_wall`
- `slavicmyths:mossy_bog_green_kurgan_cobblestone_slab`
- `slavicmyths:mossy_bog_green_kurgan_cobblestone_stairs`
- `slavicmyths:mossy_bog_green_kurgan_cobblestone_wall`
- `slavicmyths:carved_burial_stone`
- `slavicmyths:carved_burial_stone_slab`

Рецепты: 3 блока → 6 плит; 6 → 4 ступени; 6 → 6 оград. Камнерез: 1 → 2 плиты / 1 ступень / 1 ограда. Резной камень: две исходные плиты → один блок, либо камнерез. Четыре камня → четыре кладочных блока; кладка → треснувшая в печи; кладка + мох → замшелая. PNG новых каменных материалов: 256×256 из новых исходников 1024×1024, без увеличения старых спрайтов. Общая палитра задаётся комнатой, пятна 4×3×4; ванильный камень и кирпичи входят в смесь.

Art review и источники: `art/kurgan-0.9.6/`. В игре не проверено.


## Проверка ресурсов и получение

11 новых авторских текстур блоков 256×256; стороны и верх камня/кладки разделены, у холодного базового камня есть визуальный вариант. Источники: `art/kurgan-0.9.6/`. Пятна кладки и веса ванильных/новых материалов назначаются по архетипу комнаты. 25 настоящих строительных форм используют SlabBlock/StairBlock/WallBlock; carved_burial_stone — полный блок. Все 40 ориентаций лестниц сверены с Minecraft 1.21.1, двойные плиты возвращают два предмета; block/item tags, кирка, creative и стандартные рецепты JEI присутствуют.

Всего 60 новых рецептов. Плиты: 3 блока→6; лестницы: 6→4; стены: 6→6. Stonecutting: 1→2 плиты или 1 лестница/стена. Исходный холодный камень получается из ванильного stone через stonecutter; болотный — из четырёх stone и moss_block. Кладка получается из четырёх соответствующих базовых камней, треснувшая кладка — обжигом, замшелая — с moss_block. Резной камень: две холодные базовые плиты→1 блок либо stonecutting холодного базового камня; резные плиты имеют craft и stonecutting.

### Точные файлы добавленных рецептов

- `bog_green_kurgan_cobblestone.json`
- `bog_green_kurgan_cobblestone_slab.json`
- `bog_green_kurgan_cobblestone_slab_stonecutting.json`
- `bog_green_kurgan_cobblestone_stairs.json`
- `bog_green_kurgan_cobblestone_stairs_stonecutting.json`
- `bog_green_kurgan_cobblestone_wall.json`
- `bog_green_kurgan_cobblestone_wall_stonecutting.json`
- `bog_green_kurgan_stone.json`
- `bog_green_kurgan_stone_slab.json`
- `bog_green_kurgan_stone_slab_stonecutting.json`
- `bog_green_kurgan_stone_stairs.json`
- `bog_green_kurgan_stone_stairs_stonecutting.json`
- `bog_green_kurgan_stone_wall.json`
- `bog_green_kurgan_stone_wall_stonecutting.json`
- `carved_burial_stone.json`
- `carved_burial_stone_slab.json`
- `carved_burial_stone_slab_stonecutting.json`
- `carved_burial_stone_stonecutting.json`
- `cracked_bog_green_kurgan_cobblestone.json`
- `cracked_bog_green_kurgan_cobblestone_slab.json`
- `cracked_bog_green_kurgan_cobblestone_slab_stonecutting.json`
- `cracked_bog_green_kurgan_cobblestone_stairs.json`
- `cracked_bog_green_kurgan_cobblestone_stairs_stonecutting.json`
- `cracked_bog_green_kurgan_cobblestone_wall.json`
- `cracked_bog_green_kurgan_cobblestone_wall_stonecutting.json`
- `cracked_pale_blue_kurgan_cobblestone.json`
- `cracked_pale_blue_kurgan_cobblestone_slab.json`
- `cracked_pale_blue_kurgan_cobblestone_slab_stonecutting.json`
- `cracked_pale_blue_kurgan_cobblestone_stairs.json`
- `cracked_pale_blue_kurgan_cobblestone_stairs_stonecutting.json`
- `cracked_pale_blue_kurgan_cobblestone_wall.json`
- `cracked_pale_blue_kurgan_cobblestone_wall_stonecutting.json`
- `mossy_bog_green_kurgan_cobblestone.json`
- `mossy_bog_green_kurgan_cobblestone_slab.json`
- `mossy_bog_green_kurgan_cobblestone_slab_stonecutting.json`
- `mossy_bog_green_kurgan_cobblestone_stairs.json`
- `mossy_bog_green_kurgan_cobblestone_stairs_stonecutting.json`
- `mossy_bog_green_kurgan_cobblestone_wall.json`
- `mossy_bog_green_kurgan_cobblestone_wall_stonecutting.json`
- `mossy_pale_blue_kurgan_cobblestone.json`
- `mossy_pale_blue_kurgan_cobblestone_slab.json`
- `mossy_pale_blue_kurgan_cobblestone_slab_stonecutting.json`
- `mossy_pale_blue_kurgan_cobblestone_stairs.json`
- `mossy_pale_blue_kurgan_cobblestone_stairs_stonecutting.json`
- `mossy_pale_blue_kurgan_cobblestone_wall.json`
- `mossy_pale_blue_kurgan_cobblestone_wall_stonecutting.json`
- `pale_blue_kurgan_cobblestone.json`
- `pale_blue_kurgan_cobblestone_slab.json`
- `pale_blue_kurgan_cobblestone_slab_stonecutting.json`
- `pale_blue_kurgan_cobblestone_stairs.json`
- `pale_blue_kurgan_cobblestone_stairs_stonecutting.json`
- `pale_blue_kurgan_cobblestone_wall.json`
- `pale_blue_kurgan_cobblestone_wall_stonecutting.json`
- `pale_blue_kurgan_stone_slab.json`
- `pale_blue_kurgan_stone_slab_stonecutting.json`
- `pale_blue_kurgan_stone_stairs.json`
- `pale_blue_kurgan_stone_stairs_stonecutting.json`
- `pale_blue_kurgan_stone_stonecutting.json`
- `pale_blue_kurgan_stone_wall.json`
- `pale_blue_kurgan_stone_wall_stonecutting.json`
