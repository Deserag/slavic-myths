# 1.7.3 — фактическая проверка

Клиент не запускался. Ни один пункт, требующий наблюдения в игре, не объявлен пройденным по компиляции или offline render. Dedicated GameTests используют искусственных ServerPlayer с локальным соединением; это не реальный multiplayer.

| Проверка | Результат | Доказательство |
|---|---|---|
| Новые механики, реальные сущности/предметы/команды/урон/арена | PASS: 44 обязательных GameTests | runtime-final-02.log |
| Илья/Идолище, прежние проверки | PASS: 48 | ilya-regression.log |
| Добрыня/Маринка, прежние проверки | PASS: 36 | marinka-regression-02.log |
| Diamond damage Easy/Normal/Hard | PASS: 1 последовательный тест | difficulty-01.log |
| Сохранение и чтение между двумя настоящими серверными процессами | PASS: write/read | restart-write-01.log, restart-read-01.log |
| Native ModelPart, UV, конечность поз, 26 профилей | PASS: 183715 assertions; не игровой render | offline-final.log |
| Clean production build | PASS, Java 21 / NeoForge 21.1.255 | clean-build-final.log |
| Ресурсы, аудио, старый JAR, воспроизводимость генератора | См. resource-gates.json | resource-gates.log |

Всего 15 реальных запусков GameTest server, включая неуспешные итерации. Клиентских запусков 0. Первые ошибки исправлены: fixture размещения цели в claw combo; новый лишний READY-запрет Маринки удалён, прежнее поведение восстановлено. Итоговые повторные проверки успешны.

## Соответствие пользовательской матрице из архива

Исходные 54 кейса находятся в ../../specs/1.7.3/09_Testing/01_ACCEPTANCE_CASES.md. Следующие статусы относятся именно к полной игровой приёмке, а не к частичному автоматическому доказательству.

| ID | Статус полной приёмки | Что уже проверено |
|---|---|---|
| MODEL-01…05 | NOT_RUN | Native offline geometry/poses, собственные атласы, высоты; изображения не скриншоты игры |
| MODEL-06 | PASS | Entity atlas 256×256, inventory 64×64, PNG decode/UV bounds |
| HERO-01…03 | PARTIAL | Server use frames, cooldown, расход, recall и max2, опасные позиции; клиентское использование не запускалось |
| HERO-04 | PARTIAL | Два серверных процесса, UUID/HP/cooldown; реальный клиентский relog/dimension travel NOT_RUN |
| HERO-05…08 | PARTIAL | Scoped taunt/dodge/aggro/safe movement, конечные автоматы; длительное наблюдение NOT_RUN |
| HERO-09 | PARTIAL | Dedicated прямой dodge против AOE, без общей неуязвимости |
| HERO-10 | NOT_RUN | 30-секундное измерение DPS не выполнялось |
| HERO-11…12 | PARTIAL | Last Stand, однократный lethal clamp, recovery/dormant slot; полноценный бой визуально NOT_RUN |
| HERO-13…15 | PARTIAL | Server story locks, matching hero, сохранение item count; UI в игре NOT_RUN |
| BOSS-01…03 | PARTIAL | Регистрация/HP750, /summon и реальные /give; useOn яйца Маринки; render/bossbar NOT_RUN |
| BOSS-04…05 | PARTIAL | Marker/start validation, повторный horn не создаёт второго босса; клиентский use NOT_RUN |
| BOSS-06…07 | PARTIAL | Реальный серверный урон diamond/Resistance и три сложности; игровые 3 последовательных прыжка и Protection NOT_RUN |
| BOSS-08 | PARTIAL | Три независимых настоящих hit windows; визуальный warning NOT_RUN |
| BOSS-09…12 | PARTIAL | Aim lock, зоны/LOS, ground roar при низком потолке, фактический урон; звук/телеграфы в игре NOT_RUN |
| BOSS-13…15 | PARTIAL | Rescue threshold и timeout/invalid victim release; реальные disconnect/camera/сетевой jitter NOT_RUN |
| BOSS-16…17 | PARTIAL | Fatigue cycle, post-armor vulnerability, single rage; длительное наблюдение NOT_RUN |
| BOSS-18 | PARTIAL | Production не ломает произвольные блоки, сохранность pending изменённых ячеек проверена; тест с домом NOT_RUN |
| BOSS-19 | PASS (server) | Debug/creative/admin kill без story progression |
| ARENA-01 | PARTIAL | Реальный server builder, 65×65+логово, сундуки/ворота; прогулка и осмотр NOT_RUN |
| ARENA-02 | NOT_RUN | Длительная игровая навигация с двумя сущностями не проверена |
| ARENA-03…05 | PARTIAL | Safe reset и disk restart, один boss/loot, ворота продолжают открытие; клиентский уход/возврат NOT_RUN |
| PROGRESS-01…02 | PASS (server) | Фактическая победа/участие, disk persistence, нет уникальных наград 1.7.6 |
| UI-01…02 | PARTIAL | Все keys/model JSON/distinct PNG, RU/EN; GUI и first-person NOT_RUN |
| AUDIO-01…02 | PARTIAL | 48 OGG: SHA источника, Vorbis48k mono, полный ffmpeg decode, events/subtitles; прослушивание/сеть NOT_RUN |
| MULTI-01 | PARTIAL | Последовательные запросы двух synthetic players в одном tick; настоящие два клиента NOT_RUN |
| MULTI-02 | PASS | Реальные dedicated server старты, common без client linkage |
| BUILD-01 | PASS | gradlew.bat clean build, production JAR |

Краткая ручная проверка: отдельный survival world; разместить долину на свободной площадке; призвать Алёшу, использовать рог у маркера; проверить все телеграфы/позы/звук, два клиента, захват и отключение, навигацию 5 минут, реальный relog и MSPT 10 минут. Не использовать пользовательские миры для автоматических тестов.
