Обновление 19.10: владелец разрешил сохранить этот проверенный пакет в main;
предыдущие NO_COMMIT ниже описывают историческую локальную итерацию.

# #60 / #64 — коса, посох и ледяные объекты

Статус: **IN_PROGRESS**. #58 закрыта владельцем только по рукам/хвату;
это не приёмка звука. Автономный выбор аудио разрешён владельцем.
Новых голосов/песен в этом пакете нет. Источники CC0: qubodup — bamboo
swishes; rubberduck — metal/wood hits, glass breaking/falling.
Стекло — честно обозначенный предметный prop для фантастического льда.
Клипы не прослушаны владельцем, качественная/игровая оценка NOT_RUN.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Сохранить принятые руки/хват, модели, UV, текстуры и игровой код | `frost-foley/gates.json`: byte comparison (text EOL normalized) с 037f80ac, полный frost/night Java и визуальные ресурсы | PASS (offline) |
| Записанный источник и законная лицензия | `frost-foley/sources.json`: автор, primary URL, archive/member SHA; 11 отдельных takes; лицензия в JAR | PASS (offline) |
| Разные звуки для косы, посоха и ледяных объектов | 11 файлов / 9 прежних events; подписи, callsites, SHA, duration/RMS/peak в `selection.json`; [клипы](../../../art/audio/pack-a-60/frost-foley/PREVIEW.md) | PASS (offline) |
| Event → actual OGG → реальный callsite; RU/EN | `selection.json`, existing subtitles, source contract в `verify_frost_foley_60.py` | PASS (offline) |
| Замах до hit10/10/14; сохранность таймингов | Клип .42s maximum / minimum pitch .94 < .5s; исходный Java сохранён | PASS (offline) |
| Несколько сосулек без лишнего усиления | `morozko.staff_icicles` entry volume .55, file RMS −24dBFS, unchanged .6 source volume | PASS (offline); игровой микс NOT_RUN |
| Генераторы не возвращают старые выбранные события | Идемпотентный `frost_foley_60.restore_selected`, hook общего restore и пропуск выбранных событий в обоих legacy audio generators | PASS (offline) |
| Карантин не включён; одобренные 4 голоса сохранены | Общие audio/voice gates, exact SHA | PASS (offline) |
| Java21 clean build и production JAR | `.tools/frost-foley-build.log`; полный CRC/ресурсы/OGG в gates | PASS (offline) |
| Игровое прослушивание/итоговая ручная приёмка | 0 запусков, владелец ещё не слушал пакет | NOT_RUN |
| Остальные голосовые группы и песня русалки | Отдельная незавершённая работа; этот foley не является голосом | BLOCKED_NEEDS_AUDIO_SOURCE / IN_PROGRESS |
| PolyMC для этой итерации | Не обновлялся; установленный ранее SHA4fa880a3… проверяется отдельно | NOT_RUN — не устанавливалось |

Никаких коммитов, закрытия Issue, повышения версии или запусков.
Новый локальный JAR 1.9.7.9 отличается от ранее установленного JAR той же
тестовой версии. Акт прошлой установки не переписывается под новую сборку.

## Реальные ограничения

`spike_break` вызывается исторически и при попадании FrostShard, и при начале
шипа. Этот общий ID сохранён; это не утверждение о разделении двух событий.
Двойник Морозко при некоторых путях смерти имеет два прежних callsite
`dissolve`/`die`; семантику сущностей не меняли. Проверка баланса одновременных
источников возможна только в игре владельцем.

После этого пакета остаются ветер/метель/поток, отдельные special cues других
существ и полноценные голоса. Синтетические старые файлы не удалялись, чтобы
сохранить оригинальные ресурсы; девять выбранных events теперь на них не ссылаются.
Полные legacy visual generators не запускались, принятые source/UV проверяются
побайтно. Генераторы новых записей не используют карантинные синтезаторы.

## Фактический итог проверок

Java21 clean build: PASS, 1m14s. Frost gate285, voice7031, old audio7736, creature248: PASS.
Production JAR SHA256 `63fdef98d36a976253cd78ebf7570303e022259841bd470f9325c5c95f25c80a`.
Отдельный disposable restoration test начал со старых maps и восстановил точные11 новых OGG.
Геймплейные классы/JSON сравниваются с Git после нормализации только CRLF/LF; PNG побайтно.
Длинный сравнительный demo20–45s для полной #64 ещё NOT_RUN; есть подписанные A/B для каждой записи.
Поэтому пакет является частичным вкладом в #60, вся #64 не объявлена готовой.
