# #300 — ручное восстановление естественной генерации Чуди, 27.10.2026

**Ветка `hell`. Состояние: SOURCE_PATCHED, BUILD_NOT_RUN, GAME_NOT_RUN, OWNER_NOT_ACCEPTED.** Критерии PASS пока не выполнены. Предыдущий отчёт о том, что операторский locate строит Чудь, **утратил силу** после прямого исправления требований владельцем.

## Фактический дефект, воспроизведённый владельцем

В тестовой сборке в обычном равнинном мире и на побережье:
- `/locate structure ...` либо поисковые dev-команды не находили действительных естественных native starts, вместо независимого read-only поиска запускали manual building;
- 8 последовательных попыток manual создания city/abandoned отклонялись с `Безопасный подземный объём/наружный выход не найдены`;
- часть попыток доходила до загрузки 100+ чанков и отклонялась с `Недостаточно фактического грунта над кровлей после генерации`;
- ни одного фактического завершения по этим скриншотам не показано.

## Проверка исходников и технические причины

1. `ChudSimplePlacement.cityPlan / abandonedPlan` **отбрасывали весь native root**, если `ChudSurfaceAccess.city/abandoned` не смог выстроить длинную лестницу к идеально безопасному устью. Значит, даже физически подходящий подземный объём не становился native `StructureStart`.
2. `ChudSimplePlacement.height/exactHeight` устанавливают Y по максимальной твёрдой крыше и min footprint; прежняя дополнительная 20-блочная засыпка верха конкурировала с глубиной самых нижних этажей и bedrock. Нынешнее ограничение нового покрытия `ChudRoofCover.DEPTH=8`, AIR fills не учитываются при поиске крыши; `ChudTerrainPolicy.roofAnchor` legacy-контракт сохранён.
3. `ChudCreationJobs` после реальной загрузки terrain повторно требовал заполненность *всех* колонок над кровлей и отклонял root, хотя карверы/расщелины могут менять фактический грунт по сравнению с noise-сэмплом. Вместо отказа `ChudRoofCover.repair` достраивает только доступные воздух/жидкость на толщину кровли при размещении; чужие blocks/portals/bedrock по-прежнему нельзя вырезать.
4. `ChudKnownSites.loaded` исключал честные native underground starts без Access из списка городов для Яги/Баюна.
5. Собственная правка lookup ошибочно сделала `/locate` строящей командой. Отменена; native read-only forecast `ChudFutureSearch` восстановлен.

## Текущие адресные исходные изменения в `hell`

| Файл | Что изменено |
|---|---|
| `chud/ChudSimplePlacement.java` | City и independent abandoned не требуют `ChudSurfaceAccess` для natural start. Новые city пытают vestibule floor 40/24/12, не ломая утверждённые комнаты, соединения и legacy NBT. Уточнён расчёт высоты по солидным fill и крыше. |
| `chud/ChudRoofCover.java` | Глубина нового компенсационного покрытия8; AIR не признаётся крышей. |
| `chud/ChudTerrainPolicy.java` | Старое правило `roofAnchor(...,water)` оставлено для legacy/offline tests; новые верхние проверки вынесены в placement. |
| `chud/ChudCreationJobs.java` | Ручная и сюжетная генерация не зависят от Access; сохраняют native root/start без access piece, возвращают X/Z ориентир на поверхности, а saved arrival — реально находящийся в bbox подземный вход. Отбрасывание после actual-cover снято; `roofCover.repair` выполняется при завершающей посекционной обработке. Перебор до8 заранее назначенных explicit кандидатов пока оставлен как **вторичная fallback**; он не относится к native worldgen и не гарантия удачи. |
| `chud/ChudSearchCommands.java`, `ChudCommands.java` | Read-only `locate/search`, при ненайденном уже загруженном start запускается только seed-only forecast, а не `ChudCreationJobs.create`. `/slavicmyths dev generate city/abandoned` по-прежнему единственный явный OP путь строительства. |
| `chud/ChudFutureSearch.java` | В отсутствие наземного access показываются приблизительные X/Z подземного объекта, без обещания физического входа на поверхности. |
| `chud/ChudKnownSites.java` | Настоящие подземные native starts регистрируются для поручений Яги/Баюна без требования наземной лестницы. |
| `worldgen/ExistingSiteSearch.java` | Вспомогательный `reportMissing` позволяет тихо проверять loaded starts перед seed-поиском, не строя объект. |
| `chud/ChudSurfaceAccess.java` | Предшествующая попытка улучшения лестницы сохранена для совместимости старых записей; **создание новых структур от неё более не зависит**. |

Динамическая плотность/редкость worldgen не менялась: `spacing=256` чанков (`4096` блоков), исходная региональная сетка и seed salt остаются прежними. Поэтому **не ожидается city буквально в первых видимых чанках спауна каждого нового мира**; в природной генерации структуры возникают при генерации подходящих новых чанков, без dev-команд.

## Проверка после получения нового JAR

1. Сначала `./gradlew --no-daemon clean build` с Java21, проверить `tools/verify_current_resources.py`; **NOT_RUN**.
2. Создать новый disposable world, **не выполнять** `/slavicmyths dev generate`, `city/abandoned search` для создания; новые чанки должны автоматически порождать нативные `chud_city/chud_abandoned`. Для навигации теперь безопасно `/locate structure slavicmyths:chud_city`; результат обязан явно различать физически загруженный start и будущий seed-план. Дойти до X/Z, при необходимости докопаться, увидеть полноценную структуру и NPC; **NOT_RUN**.
3. После посещения city: `/slavicmyths dev city info` и `/slavicmyths dev city validate` — должны найти реальный start в загруженных чанках; **NOT_RUN**.
4. Отдельный мир для explicit `/slavicmyths dev generate city`, `/slavicmyths dev generate city attached`, `/slavicmyths dev generate abandoned medium`, `/slavicmyths dev generate abandoned great`. Эти результаты **никогда не считать natural спавном**; **NOT_RUN**.
5. Квест Яги/Баюна: приблизительные X/Z для городов без наземной лестницы, посещение всех требуемых комнат, award/reload; **NOT_RUN**.
6. Пять seed, 3 варианта abandoned, незащищённые природные блоки vs сохранённые игроком объекты, old save, server load/restart, равнина, возвышенность, склон, море; **NOT_RUN**.

**Важное ограничение:** успешная компиляция ещё не докажет, что естественный worldgen действительно появился: нужна игровая приёмка с физическими блоками, а не только forecast, dev-generate и offline fixtures.

## Обновление Windows-копии

```powershell
git status --short
git fetch origin hell
git switch hell
git pull --ff-only origin hell
.\gradlew.bat --no-daemon clean build
```

Старые или незакоммиченные изменения не сбрасывать `reset --hard`. Выбирать единственный модовый JAR из `build/libs`, не оставлять старую версию мода рядом. `main` не трогать.

