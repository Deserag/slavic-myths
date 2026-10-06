# Материалы болот 0.9.7

Новых строительных блоков и руд не добавлено: пакет допускает целевые изменения материалов, но не требует новых ore variants. Текстуры и баланс существующих руд сохранены.

Постройки используют dark oak/spruce planks, logs, stairs/slabs/fences/trapdoors, mossy/cracked/chiseled stone bricks, ограниченный cobweb как старые снасти, candles, lantern/soul lantern, flower pots, настоящий существующий slavicmyths:altar и vanilla barrel. Дно — естественная clay/mud/dirt, без массового изменения terrain. Деревья используют существующие willow/pine семьи и vanilla oak; корни — настоящие logs, а не отдельные пустые decor IDs.

Полузатопленные фрагменты используют водонаполняемые slabs/trapdoors. Стойки имеют полноценные опоры до дна. Материалы можно добывать и повторно использовать для строительства без новой бессмысленной цепочки рецептов. Существующие рецепты и loot блоков не менялись.

В дополнительном runtime hotfix липовая дверь переведена на все 32 native door blockstates Minecraft 1.21.1, восемь актуальных моделей left/right/open; четыре старых model IDs сохранены как корректные aliases. ID linden_door и её текстуры/recipes/drop сохранены. Проверено разрешение всех новых ссылок и точное соответствие vanilla oak door rotations.

Остаются 16 прежних missing vanilla model parents дверей darkened/pine/rowan/willow; пакет пользователя требует исправить linden, остальные отдельно задокументированы. Новых missing assets нет. Проверку в игровом освещении не выдаём за выполненную.
