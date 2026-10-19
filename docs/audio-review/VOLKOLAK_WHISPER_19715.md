# Обычный Волколак и шёпот Маринки — 1.9.7.15

IN_PROGRESS / AGENT_SELECTED / OWNER_PENDING. Hearing NOT_LISTENED, runtime NOT_RUN.

Обычный `slavicmyths:volkolak` — VolkolakEntity, наследник HuntMob, а не
CursedVolkolak. Прежде ярость на50%HP и начало HOWL проигрывали один RAVAGER_ROAR
с pitch0.65. Теперь его override выбирает разные events при pitch1:

| Реальный trigger | Event | Actual source |
|---|---|---|
| Переход в rage на50%HP, прежний одноразовый guard | volkolak_hunt_rage,2 варианта | Собака Harvey Bird, запись Frederick Bird/coldvet404920, CC0; разные фразы12–13.2s и33–34.2s |
| begin(HOWL), прежние20ticks windup/8recovery/200cooldown | volkolak_hunt_howl | Настоящий вой волков U.S. Fish and Wildlife Service, PD-USGov-FWS,18–19.25s |

Три разные PCM-записи; ни pitch-clone, ни старый harmonic mix Проклятого Волколака.
Оригинальный FWS OGG взят из уже существующего raw-cache work/grey-wolf/audio;
права и URL перепроверены на Commons. Повторное HTTP-скачивание403, поэтому
новое remote-byte совпадение не заявлено. SHA сохранён в sources.json.
Основной бой, HP, Move/timers, target selection, RNG/NBT и остальные sounds
обычного Волколака не менялись. Звуки CursedVolkolak не менялись вообще.
Остальные наследники HuntMob используют прежний default hook. Наличие
RAVAGER_ROAR в его default методе не означает вызов его ordinary override.

Маринка: два пробела заменены разными фразами записанного женского шёпота
BlueSiren377743, CC0. Автор сам записал и наслоил свой голос: бессвязные/тёмные
фразы. Это не синтез и не заявленная русская реплика; точная транскрипция
не заявляется. `_01` тихий редкий idle, `_02` короткое заклятие LURE frame22,
раньше второй event был зарегистрирован, но не вызывался. Изменена только
строка cue, прежние попадание/контроль/RNG/кадры сохранены.

Подписанные OLD/NEW: общий soundboard.html, review.zip; новые последовательности
auditions/ordinary_volkolak и marinka_whispers. OLD для Волколака — доступный
локальный vanilla Ravager, явно отмечено, что playback без старого pitch/gain.
Это офлайн-последовательности файлов, не захват из игры.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Обычный Волколак,2 новых registry events/3 OGG | volkolak_hunt_audio_61.py; sources/selection | PASS_SOURCE |
| Разный шёпот Маринки,2 OGG и два реальных callbacks | MarinkaEntity; audio_vocal_priority_61.py | PASS_SOURCE |
| Модели, другой Hunt dispatch и CursedVolkolak сохранены | Exact full-source contracts + unchanged Cursed bank | PASS_OFFLINE |
| Реальный compiled JAR virtual dispatch | VOLKOLAK_HUNT_GATES.json; javap production JAR | PASS_OFFLINE |
| Java21 clean build +6 общих gates +scoped gate | 66a19070; clean build69s;6 общих+1 scoped gates | PASS_OFFLINE |
| Установка тестового PolyMC,backup/SHA/one active JAR | После всех gates | NOT_RUN |
| Приёмка угрозы, тембра и громкости в игре | Владелец | NOT_RUN |

## Остальные10 source gaps

| Events | Причина, безопасное продолжение |
|---|---|
| igosha_ambient/hurt/death (3) | Реальный младенческий плач уже candidate-only; не является актёрской болью/смертью. Чувствительная интерпретация ещё не принята; взрослый grunt не подставлен. |
| cub_hurt/death (2) | Adult vanilla bear fallback сохраняется; отдельного качественного cub-specific источника нет. Pitch-копией взрослого не объявлен новый детёныш. |
| stag_hurt/death, doe_hurt/death (4) | Имеющиеся species calls не подтверждают подходящую реакцию hurt/death. Не отключаем метрику, переименовав alarm в death. Нужны подходящие записи/исполнение и контекстная оценка. |
| karachun.voice_whisper (1) | Настоящий шёпот unfa уже candidate-only, но это хор голосов с неоценённой речью; не выдаём его за скупой низкий голос Карачуна. Карантин остаётся. |

Полный прежний аудит не повторялся: обновлены только относящиеся к Маринке
устаревшие route rows. Новые ресурсы и совместимость проверяются gates.
