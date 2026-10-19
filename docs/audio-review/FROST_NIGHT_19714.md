# #64–#66: мороз и ночь, версия 1.9.7.14

IN_PROGRESS / AGENT_SELECTED / OWNER_PENDING. Не итоговая приёмка.

35 прежних событий получили36 новых OGG:14 Морозко (два разных шага),
21 Карачуна. Сохранены9 ранее выбранных frost-foley events, все registry IDs,
геометрия, UV, оружие, AI, урон, интервалы и сохранения.
Исправлен двойной звук завершения Карачуна: action(10) уже запускает yield,
повторный вызов убран; его прежний pitch random draw сохранён отдельным
nextFloat(), поэтому последовательность боевого RNG не сдвигается.
Остальное содержимое Karachun.java сверяется с точным исходным контрактом.

Ворон — настоящая запись nielstii, Freesound346318, CC0. Последний час —
записанный часовой колокол Martineerok148284, CC0. Сохранены официальные
HQ MP3 preview, исходные SHA и лицензии; они не объявляются raw WAV.
Магический лёд художественно озвучен физическими стеклянными записями,
косы/снаряжение — фоли. Продолжительные метель и поток посоха используют
запись воздушного потока felix.blume234000 (CC0, дверной проём в Брюсселе),
разные отрезки22–25.8s и31–35.2s; морозный конус —40–42.8s. Ветер remaxim — библиотечный wind effect: не заявляем,
что он полевой, человеческий или голосовой. Все назначения подписаны RU/EN.

morozko.breath — атака морозным конусом, не дыхание человека.
karachun.last_breath/black_harvest — способности косы игрока, не голос босса.
Короткий last_hour_mark помещается в двухсекундный интервал; dark_frost —
в40ticks даже при минимальном pitch0.94. blizzard_loop короче80ticks;
staff_stream длиннее максимальных70ticks клиентского FrostStreamSound,
поэтому не требует повторения за штатное время канала. Бесшовность не заявлена.

Прежний karachun.voice_whisper синтетический. Он отключён, причина
BLOCKED_NEEDS_AUDIO_SOURCE. Подходящий записанный шёпот ещё нужен;
это не починенный голос. Другие независимые пробелы #61 остаются открыты.

Оба старых asset-генератора теперь имеют --audio-only: только canonical
frost-foley + project61, без синтеза. При полном историческом запуске canonical
audio восстанавливается после resources. Полные visual-генераторы не запускались.
Безопасный аудиорежим проверяется сравнением всех src/main байтов до/после.

Подписанные подборки: auditions/morozko_material, karachun_material,
frost_night_items. Это офлайн-последовательности файлов, не запись игры.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Реальные файлы, source SHA, лицензии, RU/EN | sources.json / selection.json / project-61.txt | PASS (source) |
| 35 событий /36 новых вариантов | selection.json; sounds.json; FROST_NIGHT_ROUTES.csv (36 routes including blocked whisper) | PASS (source) |
| Старый шёпот не возвращается | BLOCKED; canonical generators | BLOCKED_NEEDS_AUDIO_SOURCE |
| Аудиорежим не меняет модели и текстуры | FROST_RESTORER_GATES.json | PASS (all8415 src/main files unchanged) |
| Java21 clean build / production JAR | source69a77a70; Java21 clean build47s; production JAR SHA da114d33f6faced3f29d298644aeb81abe2336877ada017b0f2e8633b32e91d2 | PASS (offline) |
| Decode / JAR parity /6 offline gates | 6 offline gates,1359 OGG decode,0 JAR mismatches | PASS (offline) |
| PolyMC | TEST_PREVIEW_1.9.7.14.json / VERIFY.json; closed-process guard, verified backup, one active mod JAR | PASS (INSTALLED_TEST_PREVIEW) |
| Слуховая и игровая оценка | Только владелец | NOT_RUN |

Дополнительный read-only аудит #62 нашёл139 точных hash-связей с историческими
исходными пакетами/manifest. Из них111 процедурных и28 записанных/смешанных
по старым декларациям. Это уточнение происхождения, не приёмка и не полная
лицензионная очистка старых смесей. Старые генераторы для аудита не запускались.

Installed JAR: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.14.jar`.

Backup: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-audio-1.9.7.14-20261019-120950Z`.

Следующий P0: ordinary VolkolakEntity→HuntMob direct RAVAGER_ROAR
(rage и HOWL) ещё не заменён; его нельзя считать исправленным по
событиям отдельного CursedVolkolak. Русалка/Кузя защищены. Для шёпота
Карачуна и смеха Кикиморы добавлены только кандидаты (не в JAR).
