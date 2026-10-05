from pathlib import Path
r=Path('.')
p=r/'README.md';s=p.read_text('utf-8');s='''# Исправление генерации 0.9.3 и яйцо Яги

Исправлена ошибка `y>4` в поиске поверхности: стандартный плоский мир с землёй
на Y=3 теперь проходит тот же production пайплайн, что и обычный мир.
Запись блоков использует flags18 (без преждевременных neighbor updates), затем
проверяются опоры и формы всей готовой постройки. Потерянные блоки не принимаются
как успешная генерация; при отказе записи/спавна выполняется проверяемый откат.

`/slavicmyths dev yaga generate` пробует до восьми мест вокруг игрока
(20 и 12 блоков в четырёх направлениях), без загрузки новых чанков. При отказе
показывает причину и координаты: чанки, вода, высота, склон, контейнер, защищённый
блок, препятствие, опора, запись или спавн. Уже сохранённый дом не дублируется.
`locate` теперь отличает **место будущего дома (только X/Z)** от размещённой избушки:
не нужно телепортироваться на прежний технический Y=0.
Автоматический выбор остаётся лесным, 800–2500 от spawn; в обычном superflat Plains
лесов нет, поэтому для теста используется команда генерации.

Добавлено `slavicmyths:baba_yaga_spawn_egg` в Creative Tab и RU/EN:

```mcfunction
/give @s slavicmyths:baba_yaga_spawn_egg 1
/slavicmyths dev yaga generate
/slavicmyths dev yaga locate
```

Яга из яйца в Overworld сразу открывает разговор/поручения/обмены по текущей
благосклонности игрока. Она хранит своё домашнее место и происхождение в NBT,
не заменяет canonical UUID хозяйки избушки и не создаёт дом.
Прогресс общий по UUID игрока: несколько яиц не дублируют награды.
Яйцо не выдаёт достижение «Найти избушку» и не пропускает знакомство с настоящим домом.
Котёл по-прежнему требует физический котёл внутри избушки.

Новая проверка: `gradlew.bat --offline verifyYagaPlacement`.
Она запускает production prepare/commit в изолированной JVM-среде с настоящими
vanilla BlockState/survival/shape правилами и тегами из локального client.jar 1.16.5.
Проверяет полную постройку на Y=3/4/63, поверхность Y=0, двери/фонарь/растения,
сохранность контейнеров и построек, отсутствие чтения незагруженных чанков,
откат при отказе блока/спавна/опоры, воспроизводит потерю блоков при старых flags2.
**Это не запуск игрового клиента/сервера.** Minecraft в этой итерации не запускался.
Логи и manifest исправления: `docs/verification/yaga-placement-hotfix/`.

---

'''+s
s=s.replace('`generate` размещает единственный дом в 24 блоках к югу от игрока в обычном мире;','`generate` размещает единственный дом в одном из восьми ближайших мест в обычном мире;')
s=s.replace('Y=0 у планируемого места означает, что высота ещё не проверена на загруженном рельефе.','У планируемого места показаны только X/Z: высота проверяется при размещении.')
s=s.replace('clean build verifyHunt verifyKurgan verifyYaga\n','clean build verifyHunt verifyKurgan verifyYaga verifyYagaPlacement\n')
p.write_text(s,'utf-8')
p=r/'docs/PROJECT_STATUS.md';s=p.read_text().replace('6 рецептов котла, 8 предметов,','6 рецептов котла, 8 сервисных предметов + яйцо Яги,');s='''# Исправление генерации Яги 0.9.3

Устранён отказ superflat Y=3 из-за нижнего порога Y>4; production YagaPlacement
читает поверхность до Y=0, атомарно размещает блоки с flags18, восстанавливает
neighbor shapes только после полной постройки и проверяет опоры/откат.
Добавлены точные причины отказа, восемь близких кандидатов generate, понятный
planned locate без ложной высоты Y=0 и `baba_yaga_spawn_egg`.
Яга из яйца обслуживает игрока без подмены хозяйки/anchor, сохраняет NBT происхождения,
не выдаёт find_the_hut и не изменяет intro дома. Котёл остаётся сервисом избушки.

verifyYagaPlacement проверяет реальный prepare/commit на vanilla BlockState/тегах:
Y=3/4/63, Y=0 scan, полный дом, отсутствие раннего разрушения декора,
граничные отказы и полный откат. Minecraft client/server запусков 0.
Результаты final build/ресурсов и установка: verification/yaga-placement-hotfix/,
verification/yaga-install-0.9.3.json. Версия/registry IDs прежнего контента сохраняются.

---

'''+s;p.write_text(s,'utf-8')
p=r/'docs/ARCHITECTURE.md';s=p.read_text();s='''# Исправление placement 0.9.3

YagaPlacement содержит production preflight/commit на IWorld, а не копию алгоритма
для тестов. Prepare проверяет loaded bounds до первого чтения, terrain Y>=0,
контейнеры/защиту/жидкости, полный desired/old snapshot и опоры почвы.
Commit использует flags=2|16; лишь затем вычисляет connected states всех блоков,
отклоняет неподдерживаемые/потерянные блоки, применяет формы и вызывает spawn.
Только успешный spawn позволяет YagaHut записать placed/anchor/canonical UUID.
Отказ блока, опоры, спавна или исключение откатывают snapshot с теми же flags18.

YagaPlacementHeadless использует vanilla bootstrap и локальные vanilla block tags,
изолированный IWorld fixture и этот же prepare/commit. Никакого server/client main.
NBT NPC теперь содержит YagaEggSummoned; SpawnReason.SPAWN_EGG включает сервисный
доступ standalone NPC, но не меняет world home/UUID. YagaMenu допускает egg NPC
без house intro; BabaYaga не выдаёт им hut advancement и не меняет house intro.
Все прежние серверные проверки и общая личная прогрессия остаются.

---

'''+s;p.write_text(s,'utf-8')
p=r/'docs/ROADMAP.md';s=p.read_text().replace('- 0.9.3: Яга,', '- 0.9.3: исправлен superflat/placement, добавлено яйцо Яги и production placement regression; Яга,');p.write_text(s,'utf-8')
p=r/'docs/MANUAL_QA_0.9.3.md';s=p.read_text();s+='''
32. [ ] На стандартном superflat с землёй Y=3 выполнить generate: целый дом, две половины двери, проходное крыльцо, фонарь/грибы/лианы и одна хозяйка; повторная команда сообщает existing anchor.
33. [ ] При низкой дальности прорисовки generate выбирает загруженное соседнее место или сообщает chunks, без принудительной загрузки. Planned locate показывает только X/Z.
34. [ ] Получить /give @s slavicmyths:baba_yaga_spawn_egg 1: имя RU/EN, Creative Tab, настоящий spawn NPC; сразу доступны разговор/поручения, без find_the_hut и house intro. После save/reload доступ сохраняется.
35. [ ] После яйца создать/найти дом: отдельная хозяйка, UUID/anchor не заменены яйцом; настоящее знакомство и find_the_hut работают. Общий прогресс/повторные сдачи не дублируются, котёл остаётся в доме.
36. [ ] Вода/контейнер/постройка/склон дают конкретную причину и координаты, не частичный дом; verifyYagaPlacement проходит в headless JVM.
''';p.write_text(s,'utf-8')
p=r/'src/main/java/org/slavicmyths/yaga/YagaHut.java';s=p.read_text().replace('import net.minecraft.entity.player.ServerPlayerEntity;\n','').replace('import net.minecraft.world.gen.Heightmap;\n','');p.write_text(s,'utf-8')
(r/'docs/verification/yaga-placement-hotfix').mkdir(exist_ok=True)
