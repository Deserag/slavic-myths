# #61/#64/#65 — шёпот Карачуна, реакции медвежонка

IN_PROGRESS / AGENT_SELECTED / OWNER_PENDING. Hearing NOT_LISTENED; runtime NOT_RUN.

Карачун: вместо карантинной синтетики — настоящий одиночный мужской шёпот
Joseph SARDIN & Axeline T., BigSoundBank3256, CC0. Сохранён исходный studio mono
WAV48kHz24bit, source SHA в sources.json. Две разные фразы4–5.1s и14–15.1s,
cap1.0s, без pitch/time stretching, без формант и подложенного тона/хора.
Низкая сдержанная подача — художественный выбор, тембр на слух ещё не принят.
Нет заявления о русской речи/содержании слов. Cue action8 начинается на age0;
даже при pitch0.94 заканчивается до прежнего fear release age24.

Медвежонок: собственная реакция вместо adult polar-bear hurt/death fallback.
YleArkisto/Yleisradio249442, CC-BY4.0: настоящий испуганный детёныш, Skansen,
Швеция,02.03.1975, Nagra/tape. Исходный official HQ MP3 сохранён неизменным.
Hurt — разные крики110s/161s, cap0.35s; terminal cue — другой крик242s,
cap0.85s. Это игровая интерпретация последней реакции, НЕ запись смерти животного.
На полевой записи присутствуют мать и ручей; полная изоляция голоса не заявлена.
Короткие участки отобраны в foreground vocal band; численный отбор не заменяет
слуховую оценку. Автор/URL/лицензия и изменения указаны в упакованных credits.
Высота взрослого медведя не изменялась для создания детского голоса.

Java и игровые механики не менялись. Использованы прежние events/registry IDs:
WildlifeEntity.sound(kind+suffix), getHurtSound/getDeathSound; Karachun.action(8).
Старые оригинальные файлы сохранены. Весь прежний аудит не повторялся.
Подписанные OLD/NEW доступны в soundboard/review.zip; две новые25-секундные
офлайн-последовательности karachun_whisper/cub_reactions не являются записью игры.

Игоша:3 пункта DEFERRED / LOW_PRIORITY / KEEP_CURRENT по решению владельца
19.10.2026. Event definitions и все её OGG остаются byte-identical к d7327451.
deferred.json отделяет их от активного backlog; это НЕ устранённые source gaps.
Канонический restorer пропускает их принудительное отключение.

Остаётся4 активных source gaps: stag_hurt/death и doe_hurt/death. Их не меняли
в ограниченном запуске; отдельные suitable species reactions — следующий приоритет.
IENBA850703 найден, но описан как breath/exhale/sigh, не выбран как низкий шёпот.
Хор unfa не использован. Модели/Русалка/Кузя/принятые голоса сохранены.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
|3 events/5 recorded variants; sources/rights | selection.json / sources.json / packaged project-61.txt | PASS_SOURCE |
| Игоша неизменна,3 deferred,4 active gaps | deferred.json; scoped byte checks | NOT_RUN |
| Java/gameplay unchanged; duration budgets | verify_whisper_cub_19716.py | NOT_RUN |
| Java21 clean build +6 общих+2 scoped gates | После source commit | NOT_RUN |
| PolyMC backup/installed metadata/SHA/one active JAR | После всех gates | NOT_RUN |
| Слуховая и игровая приёмка | Только владелец | NOT_RUN |
