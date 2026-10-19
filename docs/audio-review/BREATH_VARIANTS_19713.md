# #63–#66: дыхание, реакции и действующие варианты

IN_PROGRESS / AGENT_SELECTED / OWNER_PENDING. Hearing NOT_LISTENED; runtime NOT_RUN.

14 новых OGG для14 прежних events. Новые первоисточники — записанный человеческий
вдох otherthings, исполненный смех qubodup, сессии дыхания exe2be и dav0r,
женское дыхание OwlStorm и отдельные gasps slamaxu/kanyonwyvern и4 инструментальных источника.11 источников CC0.
У Freesound сохранён именно официальный HQ MP3 preview, не выданный за raw WAV.
Все файлы, авторы, URL, лицензии, точные SHA и crops находятся в sources/selection.

Соловей: вдох до выпуска свиста и смех при смене фазы/редком idle. Оба ранее
отключённых события подключены к реальным записям. Это технический выбор,
не утверждение, что владелец принял угрозу, тембр или громкость.

Ночница: редкое тихое дыхание и дыхание при преследовании — различные фразы
OwlStorm. Перед криком — резкий записанный вдох slamaxu; при прерывании —
другая записанная реакция kanyonwyvern. Вдох завершается до release: callback
WINDUP age10, SCREAM age30; cap0.6s. Прерывание cap0.38s укладывается в интервал
обычного hurt, не требует изменения боевого cooldown.

Илья: спокойное дыхание, тяжёлое при отступлении и восстановительное — разные
фразы сессии exe2be (записаны медленный и быстрый темпы). Старые near-duplicate
ambient/retreat не являются новым голосом. Добрыня получает отдельного
исполнителя dav0r для спокойного дыхания. Это не набор pitch-shift копий.

WitchActor теперь чередует записанные варианты шагов/копья Добрыни и крыльев
сороки Маринки; FalconActor — два взмаха меча Финиста. Только отдельные аудио
счётчики, прежнее количество random draws и существующие вызовы/кадры.
Счётчики не сериализуются; после загрузки порядок вариаций начинается заново.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Человеческие записи и права | art/audio/project-61/sources.json;7 новых used CC0 sources | PASS (source/license) |
| Отдельные события и RU/EN подписи | audio_groups_61_plan.py / selection.json / sounds.json | PASS (source) |
| OLD/NEW и подписанные последовательности | soundboard.html; auditions/index.html;6 OGG22–34s; review.zip includes auditions | PASS (playable files) |
| Реальные триггеры, только аудиоправки | hero_audio_routes_61.py + audio_vocal_priority_61.py;6 exact source contracts | PASS (offline) |
| Byte reproduction / decode / spatial mono / JAR | Scoped project gate; JAR SHA92d288d87450b4f1d357d859369612cf87e815ee6bdd8c64a93355cea690d648 | PASS (offline) |
| Java21 clean build | source8e5c3494; clean build58s;6 offline gates | PASS |
| Мод в PolyMC пока владелец проверяет1.9.7.11 | Установленные файлы не изменяются | PASS (preserved; new build not installed) |
| Полноценная приёмка на слух и в игре | Владелец отдельно | NOT_RUN |

Не принятые источники не попали в runtime: Dialog vocal samples MirceaKitsune
созданы Espeak; colorsCrimsonTears Whispers происходят из обработанного прибора,
а не человеческого шёпота. Quiet SofieHolmark session оставлена только кандидатом.
Маринкины whispers, Игoша и специфические hurt/death детёныша/оленей остаются
неоконченной работой. Русалка/Кузя и принятые голоса Тугарина/Скима не менялись.

Финист: появление — Wind Chime GnoteSoundz; метка — короткий удар bell
connersaw8; ободрение — harp glissando PhonosUPF; победа — акустический finger
cymbal MirkoMeter84. Разные материальные жесты вместо четырёх near-identical
legacy сигналов. Это художественные магические акценты, не речь.

Приоритеты: Соловей не запускает ambient при getTarget()!=null; короткий
записанный chuckle cap0.55s помещается в прежний recovery12ticks. Ночница
сохраняет прежний явный pitch random draw при каждом cue, но не запускает
secondary breath поверх недавнего важного вокального события; обычный hurt
ограничен18ticks только по звуку. HP, урон, clock, AI и NBT не изменены.
Остаточное наложение важных one-shot при получении урона требует проверки
в игре: полного клиентского перехвата каждого канала этот пакет не заявляет.
