# Производительность, пределы серверных операций и защита миров

## Бюджет единственного мирового события
- One Poloz `great_poloz` root Overworld. Не сканировать весь Level каждый тик.
- Block break <=32 actual block updates / tick; conversion <=48 / tick, суммарно <=80 / tick (в обычных нагрузках ниже), очереди максимум 2048 позиций каждого типа, de-duplicate block positions до обработки.
- Sample/collision для 24 сегментов не чаще одного обновления каждого 4 тика, а прямой визуал интерполируется, чтобы змея не «ступенчато» телепортировало.
- Nearby attacker scans at most once per 20 ticks per actor and event area radius 48 blocks от тела, не десятки полных `getEntities` per tick по миру; event minions cap8.
- Server sync части тела пакетами не чаще 5 раз/сек (каждые 4 ticks) с interpolation, кроме критичных атак/начальной фазы. Не пересылать массивы из 3D-вершин.
- Boss pathfinding только локальный radius 24–32 blocks; никаких 200–300-блочных A* путей через все чанки.
- Dedicated server с игроками должен не вызывать постоянные full-GC spikes/очереди; при overload сокращать **частицы/частоту сканов**, но не тихо выключать заражение/разрушение/слабые точки.
- Временные chunk tickets около root и реальных сегментов при активном игроке, максимум **16 поддерживаемых event tickets одновременно**; запрашивать только границу движения и release позади хвоста; не выделять квадрат 7×7=49 постоянно. Если engine требует более широких ticket для 40-блочного босса, удерживать его движение до availability, а не повреждать unloaded chunks.
- Если игроков нет в Overworld 5 мин, пауза без дальнейшего разрушения. Возвращение продолжает, без дубликатов.

## Safety
- Respect `doMobLoot`, `doTileDrops`, `mobGriefing`, world border, protection/claim systems when installed. Не стирать inventory целиком без vanilla item drops. Запрет непредсказуемого разрушения порталов и служебных блоков.
- Пробовать на специально созданной копии мира, никогда на единственном рабочем save.
- По event death все chunk tickets, pending block queue, summoned mob pointers и bossbar subscriptions освобождены, иначе memory/chunk leak.
- If dedicated server bootstrap/headless тесты реально выполнены — назвать их как headless; **FPS/MSPT** можно указывать ТОЛЬКО если измерены инструментально.
- При невозможности выдержать бюджет честно сообщить неготовый gate; не сдавать «мировую катастрофу» как бесконечный tick loop.
