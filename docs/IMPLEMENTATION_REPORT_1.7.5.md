# Реализация Slavic Myths1.7.5

По пакету `docs/specs/1.7.5`. Полный анализ проекта заново не выполнялся: прочитаны3 status docs, требования пакета, фактические близкие системы/ресурсы и git status;382 исходных dirty entries сохранены, без commit. Пользовательские миры не использовались.

Калин1800HP/2.8 blocks, конь3.2/mounted4.2, native combat clock, sabre/whirlwind/charge/parry/counter/orders,65% dismount/40% Iron Will/20% mixed wave, cap14 реальных soldiers. Ending: живой1HP surrender100t, dialogue on interact, clean army/horse exit. Награды1.7.6 отсутствуют, только saved eligibility.

Ставка65×65 содержит1925 размещений: main tent/4 smaller tents/4 watch towers/palisade with4 gates/banners/braziers/supply props/reinforcement marks. Это явное staged размещение в air на ровном loaded безопасном участке, а не natural worldgen.256 block writes/tick. Original states/placed states сохраняются для cleanup; player edits сохраняются. Restart абортирует бой и восстанавливает camp; chunks не загружаются принудительно.

Пара выбирается прежними reusable summon tools: Ilya/Dobrynya/Alyosha/Finist. Нужно ровно2 живых готовых NPC рядом≤24; существующий unlock для Dobrynya соблюдён. Явный выбор: `/slavicmyths kalin select ilya_muromets finist`; старт: печать или `/slavicmyths kalin challenge`. Выбор не переключается в бою; раненый герой не заменяется. Все новые items существуют в Creative.

Доступ: `/give @s slavicmyths:kalin_battle_seal`, `kalin_tsar_spawn_egg`, `kalin_war_horse_spawn_egg`. Egg — debug бой без story credit. Предметы не расходуются при story start. Старые hero calls/boss eggs не удалены.

Изменены `gradle.properties`, main init, HeroCalls/StoryPolicy,4 native hero classes; добавлены9 `kalin/*` classes, KalinClient/KalinModel/4 meshes,4 damage types,3 item models/sprites,4 entity atlases,23 OGG events/RU+EN. Генератор `tools/kalin_assets.py` детерминирован, переиспользует только UV emitter, не старые character rigs. Полный diff сохранён в рабочем дереве; список production prior changes фиксируется resource gate. Pre-edit copies находятся в `verification/kalin-1.7.5/pre-edit`.

Звуки —23 byte-identical синтетических прототипа из архива; manifest не содержит человеческой озвучки. Направление голоса не выдаётся за записанные реплики. Offline native preview — не Minecraft screenshot. Все4 geometry heights, feet, UV и48 action profiles проверяются `kalinOfflineChecks`.

1.7.5 установлена в PolyMC Slavic-Myths-1.21.1-Testing с backup1.7.4. Clean build,1 focused GameTest/101 assertions, 149000 native checks/48 profiles и50122 production/resource gates PASS. Реальные16 GameTest server starts (включая исправленные итерации),0 клиентов.7426 prior JAR entries byte-identical;2 synthetic switch classes имеют прежние opcodes, изменились debug line tables. История победы дополнительно прочитана из настоящего SavedData .dat на диске. Полный игровой client/real network MP/relog/world restart/MSPT NOT_RUN. Focused GameTest использует synthetic ServerPlayers и отдельный generated тестовый мир: это настоящий dedicated server, но не real network multiplayer/client test/relog. Полная ручная приёмка NPC pathing/animation/audio/input и измеренный MSPT не выполнялись.

Кратко вручную после запуска пользователем: проверить Creative sprites и summon calls; выбрать2 героев на чистой площадке; посмотреть mounted render/charge/dismount; проверить troop lanes иcap14; проверить роли обеих сторон/retreat; довести до surrender/cleanup; проверить реальный save/rejoin отдельно в копии тестового мира.


Финальная поставка:

- JAR build/release/PolyMC: `03c52508b6ae5332f201259895df3e6f7fad085e35d41d8b8fc34704eac93cf4` (все3 копии совпадают).
- Установлен: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.7.5.jar`.
- Резервная копия1.7.4: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-kalin-20261011-220509\slavicmyths-1.7.4.jar`, прежний SHA `d5539688425f79b1a9d93e1cf11d21f14ebc3827d030999e52b046198958f4fc`.
- 262 других файлов экземпляра сохранены byte-identical; старая1.16.5 не затронута.
- Регистрация item models/textures,4 entity rigs,23 original OGG + vanilla horse breathing/armor/hurt; ffprobe/Vorbis/mono/full decode PASS. Генератор повторно не изменил ни один ресурс/клиентский source.
- Источник дисковой проверки: `work/kalin/runtime-17/world/data/slavicmyths_kalin_175.dat`; count1/won/started/selected pair/pending reward и пустой rollback list реально прочитаны из gzip NBT после серверного save. Это не запуск мира после restart и не перезаход игрока.
- Изменённые implementation/tool files: `verification/kalin-1.7.5/changed-files.txt`; specification/reference/evidence files отдельно в docs.
- Точный итог: `verification/kalin-1.7.5/final-verification.json`. Commit не выполнялся.
