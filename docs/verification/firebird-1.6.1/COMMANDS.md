# Краткая ручная проверка 1.6.1

Запускать вручную в отдельном тестовом мире Minecraft 1.21.1/NeoForge 21.1.255. Запусков при реализации: 0.

1. `/summon slavicmyths:firebird ~ ~ ~` — проверить фронт/бок/спину, сложение крыльев, взлёт, ночь без shaders/torch в 16/32/64 blocks. У командной птицы награды отсутствуют. Яйца нет.
2. `/data get entity @e[type=slavicmyths:firebird,limit=1,sort=nearest] Health` — начальные 3000; ударить обычным оружием/стрелой. После предупреждения проверить ответный 20 HP с железной бронёй, затем absorption/Resistance/Protection; исключить контактный спам. Блоки/деревья не горят.
3. `/sm firebird status` — узнать eligible day. В Survival, открытом загруженном поле установить время на этот день и 13500 ticks (`/time set <day*24000+13500>`). Нового eligible не должно быть после повторного time night или rollback.
4. Дождаться настоящей встречи, следовать за птицей: отдельно проверить ровно 2/3 единичных ресурсов за полную ночь, безопасное падение, pickup и сохранение предметов после её ухода. RU/EN иконки различны; рецептов нет.
5. Реальный save/relog после второго слота, unloaded chunks, sleep/dawn, doMobSpawning=false, Nether/End; два игрока наблюдают одну птицу и один drop. Смерть не дополняет квоту.
6. Послушать все 12 cue families, снять 30fps takeoff/drop/landing/retaliation/depart, сравнить с approved reference; проверить runtime logs и dedicated-server startup. 20+ минут performance проверки — отдельное измерение, не static audit.
