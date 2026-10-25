# #295 + часть #275 — IN_PROGRESS / OWNER_PENDING

База hell 22f06d6bff0b3561b0a097b8509ff1e73e53137c; локальная копия чиста при входе, origin совпадал. Main не изменяется. Разрешены только отдельные временные native миры генерации; пользовательский PolyMC не запускается и не изменяется.

| REQUIREMENT | REAL_SOURCE | PLAYER_PATH | TEST | EXPECTED |
|---|---|---|---|---|
| Поиск не строит | ChudSearchCommands, ExistingSiteSearch, LargeSiteJobs | Исследование существующих мест; OP locate отдельно | Команды до/после, block snapshot, tickets | Только actual loaded starts, честное отсутствие |
| Явное создание | ChudGenerateCommands, ManualStructureCommands | Прежнее поручение Яги/Баюна → ChudResearch → местность | Native несколько seed/Y | Физические блоки, entrance, start, записи |
| Ограниченный footprint | ChudCreationJobs | Ожидание явно запрошенного создания | Actual чанки/отказы/cancel | Только raster sections, одна площадка, cleanup |
| Городище superflat | LargeSiteJobs.plan | Естественное исследование; dev generate диагностически | Disposable flat server | Нет ложного порога minY+16 |
| Яга на склоне | YagaPlacement | Книга → создание → дом | Реальные flat/slope блоки | Крутой обрыв отвергается до записи |
| Близкие Баюны | BayunFeature, BayunHomes | Лес → NPC → книга | Source UUID/home аудит; runtime | Различать дома и маркеры; без самовольного нового spacing |
| Сохранение/повтор/отмена | SiteChunks, native saved starts | Возврат к месту после перезахода | Реальный stop/start | Нет повторной постройки; tickets освобождены |

## Причины

Locates вызывали create при отсутствии записи. Bounding rectangle Чуди загружался целиком, включая пустые растеризованные колонки; отказ повторялся до шести раз. Природные raw ore blocks отсутствовали в natural allowlist. Superflat имел поверхность ниже искусственного порога minY+16. On-demand Яга обходила slope limit и поднимала дом до наивысшей точки.

## Изменённые исходники

Точный список и SHA — [changed-sources.txt](changed-sources.txt), [receipt.json](receipt.json).

- `ExistingSiteSearch`: завершённые manual records с проверкой доступных native references/starts; иначе только фактически загруженные starts. Радиус стандартного адаптера 1024, `city locate radius` сохраняет свой параметр. Не создаёт задания, не запрашивает chunks, не запускает seed planning.
- `ChudSearchCommands`, `ChudCommands`, `AbandonedCommands`: search/locate не переходят в create; устаревшие страницы кандидатов и variant-search честно отклоняются, `abandoned locate <kind>` продолжает проверять загруженные варианты.
- `ChudCreationJobs`: один выбранный участок вместо шести; сначала owner/вход и pure raster, затем FULL chunks только для непустых секций. 289 был размером bounding rectangle, а не фактических заполненных колонок. Нет предварительного вывода этого прямоугольника как загружаемого footprint. Waterlogged-блоки больше не получают исключение только из-за непустого fluidState: допустим настоящий LiquidBlock, а не обводнённый забор. Все ManualStructureRecords защищены от пересечений, включая Ягу/Баюна; deny-chunk не обходится section fast path. Контейнеры, bedrock и порталы исключены явно. Raw copper/iron и big dripleaf/stem, cave vines/plant, spore blossom, hanging roots и природная водная растительность допустимы; fences/cobweb/mossy cobble остаются защищёнными.
- `LargeSiteJobs`: `locate` только читает; `create` отдельно. `ManualStructureCommands` добавляет `/slavicmyths dev create gorodishche|serpent_hill|sanctuary_unfading_fire|abode_pure_spring`. Старые явные генераторы сохранены; новые команды не конфликтуют с общей canonical migration.
- `LargeSiteJobs.plan`: снят искусственный порог minY+16; реальные bounds/template checks сохранены.
- `YagaPlacement`: on-demand больше не обходит прежние ограничения уклона/опор. Перепад >3 на survey или >4 под опорой отклоняется до записи; поиск должен выбрать террасу вместо длинных колонн над обрывом.
- `BayunHomes`, `BayunFeature`, `BayunCreation`: **явно согласованный владельцем в этом чате минимум 1024 по горизонтали** между новыми домами. Атомарная reservation используется до natural commit и перед on-demand spawn, rollback забывает только собственный UUID. Старые близкие homes и UUID сохраняются при reconcile/load. Ledger загружается на Level.Load до worldgen, кеш очищается на unload; операции map/save синхронизированы. Rarity 1/128 и частоты остальных структур не изменены.
- `tools/generation295-runtime.gradle`, `tools/generation295-runtime/Generation295Probe.java`: отдельный native dedicated server, собственные миры `work/qa295-*`, loopback/динамический port, ограниченная подготовка чанков, auto halt и прямое чтение native tickets. Эти classes отсутствуют в production JAR. Исторический NormalWorldProbe переведён на явный `LargeSiteJobs.create`.

Registry IDs, модели, UV, текстуры, ресурсы, loot, баланс, выдача прогресса #271–#273 и их production handlers не изменялись. Minecraft client/PolyMC не запускались.

## Реальные результаты Minecraft

Полный журнал **27 запусков dedicated Minecraft**, 0 client, 0 GameTest: [runtime-runs.json](runtime-runs.json). **15 успешных сценариев, 11 FAIL, 1 FAIL_STARTUP**. Не переименовывать эти числа в 27 PASS. Полные логи каждого запуска лежат в evidence/.

| Мир/seed | Физический результат | Доказательство |
|---|---|---|
| 29501, обычный | Город: вход **132,63,-210**, bounds Y **−48..74**, footprint **99**; итог за 19.5с тестового сценария | work-295-runtime-seed1-retry.log |
| 29502, обычный; явная новая позиция X=2048 | Средний заброшенный комплекс: вход **2156,63,-186**, bounds Y **−48..70**; 25.1с | work-295-runtime-seed2-x2048.log, seed2-generation295-created.json |
| 29503, обычный; высокая естественная местность X=34816 | Город: вход **34948,170,-210**, bounds Y **32..181**, footprint **98**; 39.3с | work-295-runtime-hills.log, seed3-generation295-created.json |
| 29504, FlatLevelSource | Городище **271,−45,359**; 476 non-air / 3620 air samples; 26с | work-295-runtime-flat.log, flat-generation295.json |
| 29505 и финальный повтор 29507 | Избушка: на плоскости **795**, на пологом склоне **909** фактических non-air блоков; крутой склон отклонён до commit | work-295-runtime-terrain.log, work-295-runtime-terrain-final.log |
| 29507 | Natural placement function: X5000 — дом и живой Баюн; X5512 — отказ; X6024 — дом и второй живой UUID | work-295-runtime-terrain-final.log |
| 29507, второй настоящий запуск | UUID **32fca6b4-cd70-4439-b1bb-43d275eac78d** и **70f711a5-ac2b-4959-aa02-38f9adb3860a** восстановлены как реальные BayunEntity; промежуточная reservation снова отклонена | work-295-runtime-bayun-reload.log, terrainfinal-generation295.json |
| 29501, отдельный protection world | Сундук в **8,−3,−248** сохранился; preflight отказал. Затем второй явный запрос с raw copper/iron физически заменил оба тестовых блока и создал город | work-295-runtime-protection.log, protection-generation295.json |
| 29501, отдельный waterlogged-final world | Обводнённый oak_fence отклонён и сохранён; после отдельного нового запроса natural kelp/raw ores разрешены, город физически создан, native tickets0 | work-295-runtime-waterlogged-final.log, waterlogged-final-generation295.json |
| 29508 | Отмена подготовки → новый запрос → отмена; native site tickets **0** | work-295-runtime-cancel-final.log |
| 29501, отдельный partial world | Отмена в phase3 после начала записи; construction reservation сохранена, готовый город не найден, native tickets **0** | work-295-runtime-cancel-partial.log |
| Тот же partial world после настоящего stop/start | Reservation сохранилась, locate не показывает частичную постройку | work-295-runtime-partial-reload.log, partial-generation295.json |
| Город 29501 после настоящего stop/start | **32/32 точных position/state witnesses совпали**; запись/старт сохранились | work-295-runtime-snapshot-final.log, work-295-runtime-reload-verified.log, seed1-generation295-created.json |

Высотный кандидат выбирался тестовым probe по 24 noise-column высотам, затем строился **один** выбранный участок в настоящем мире. Это не 24 строившихся города и не доказательство распределения natural spawn. Опубликованы все sampled heights; источник X34816 имел поверхность около Y166. Внутри самого production job автоматических смен площадки больше нет.

Проверка Баюна вызывает настоящую функцию natural feature с настоящими блоками/NPC, но **не доказывает частоту случайных срабатываний rarity_filter в полном выживании**. Две избушки проверялись на контролируемых блоковых площадках внутри native мира; это не клиентский рендер, не полное прохождение NPC и не доказательство natural landscape eligibility каждого seed.

Во всех сценариях phase0 выполнялись actual command dispatcher пути `/locate ...chud_city`, `city search`, `abandoned search`, `/locate ...gorodishche`, `/locate ...serpent_hill`; количество записей и holders до/после совпало. Успешные повторные locate не создавали новые records. Финальные creation/cancel дополнительно проверили внутреннюю **нативную** таблицу DistanceManager.tickets: `slavicmyths_site_build=0`, а не только собственный ACTIVE.

### Почему сохранены неудачные запуски

- Первый runtime не загрузил мир: конфликт canonical команды serpent_hill. Исправлено отдельной веткой `dev create`. Старый лог сохранён.
- Первый выбранный участок seed29502 пересекал `tuff_bricks`; seed29503 — `oak_fence`, затем `big_dripleaf`, затем `cobweb`, другой — `mossy_cobblestone`. Природный dripleaf — настоящий исправленный false-negative. После его разрешения та же попытка прошла его координату и отказала уже на cobweb. Остальные материалы нельзя объявить безопасной породой: охрана dungeon/построек намеренно сохранена. Сами сценарии создания на этих участках остаются FAIL, а не записаны как построенные. Отдельный waterlogged regression прошёл защиту забора, но выявил KELP_PLANT: прежде он проходил лишь через слишком широкое fluid-исключение. Добавлены явные природные kelp/sea grass/pickle/bubble/coral правила; обводнённые искусственные блоки исключения не получают.
- Неудачи reload/snapshot fixtures: предположение о двух кирпичах вместо фактической палитры, запуск reload без успешного created.json, недостаточное число свидетелей, преждевременное освобождение тестовых tickets перед чтением blocks. Они не доказывали исчезновение города и не скрыты. Исправленный retained snapshot и restart подтвердили 32 точных блока. При написании probe были также compiler failures (это 0 игровых запусков).
- Первый контролируемый terrain fixture синхронно подготавливал дальние test chunks и дал `Can't keep up` около 11с. Сам fixture переведён на SiteChunks и перепроверен; это не измерение ускорения пользовательского клиента.

## Воспроизведение отдельных прогонов

`tools/run_generation295.ps1` создаёт только новую собственную папку `work/qa295-<Name>` с ownership marker, loopback и динамическим port; существующий путь допускает только `-Resume` и только с marker. Он проверяет **native result=PASS**, не только exit code Gradle. Например:

```
./tools/run_generation295.ps1 -Scenario waterlogged -Seed 29501 -Name my-qa295
./tools/run_generation295.ps1 -Scenario flat -Seed 29504 -Name my-flat295
```

Для сохранённой тестовой Чуди `snapshot/reload` предназначены для описанного baseline seed29501/owner0,-16; это не общий игровой инструмент. Snapshot фиксирует before-save witnesses, reload сверяет их после настоящей остановки/загрузки. Исходники fixture не регистрируются в обычном production run.

## Технические gates

- Java21: `gradlew.bat -I tools/structures275-offline.gradle clean build verifyStructures275Offline` — **PASS, 2m40s**. Отдельно **99852 native offline checks**, они не засчитываются как игровые.
- `python tools/verify_resources.py` — **PASS**: 8001 ресурса, 4402 JSON, 1443 PNG, 1886 models, 1943 Java21 classes. Production JAR не содержит Generation295Probe/test/dependency classes.
- `git diff --check` — PASS.
- JAR **D:/slavic-myths/build/libs/slavicmyths-2.0.1-HELL.jar**.
- SHA256 **a2ea8322e79562b6273c033aaa288835b0fb63f2d278256ab529e96d905716fe**.
- `resources.json` содержит runtime=NOT_RUN, поскольку ресурсный скрипт сам не запускает Minecraft; он не отменяет отдельные 27 реальных запусков из runtime-runs.json.
- PolyMC **POLYMC_NOT_INSTALLED**. Прочитан только hash прежнего установленного JAR: **273767789fb6555f16c485bf64bc2cf37627787f1ca50255d820bbe36660182c**; до/после совпал. Точный прежний path — receipt.json. Установка/backup/настройки/сохранения владельца не изменялись; основной PolyMC не запускался. Игровых процессов после тестов нет. После копирования логов/metrics шесть собственных миров сохранены в локальном архиве `D:/slavic-myths/work/qa295-world-evidence.zip` (SHA в temporary-worlds.json), 11 собственных временных папок удалены с проверкой абсолютных путей внутри work; см. cleanup.json. Пользовательские файлы не затронуты.

## Итоговая матрица

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Поиск Чуди/городища/горы не строит и не берёт tickets | Dispatcher calls каждого сценария; ExistingSiteSearch; native table checks | PASS |
| Явное создание физически строит | Три seed, два города и medium, разные Y; actual block samples/records | PASS |
| Отказ не перезапускает шесть площадок | Все защитные отказы — один owner, один phase2 fail; source retry отсутствует | PASS |
| Footprint не равен пустому bounding rectangle | City99/98 вместо289; raster-derived columns | PASS |
| Природные raw ore / cave plants | Protection runtime физически заменил copper/iron; dripleaf preflight прошёл старую точку после source fix | PASS |
| Физическая замена всех cave plants | Scope runtime: raw ores заменены; dripleaf проверен preflight, остальные виды физически отдельно не заменялись | NOT_RUN |
| Контейнер / bedrock / portal / boundary / свои reservations | Сундук реальный; existing offline boundary/reservation checks, явные exclusions; partial stop/start | PASS |
| Любая старая постройка из природных блоков, любой внешний claim plugin | У природного камня/декора нет универсального provenance; внешние claim integrations не подтверждены | BLOCKED на полном обещании защиты |
| Городище superflat | Native FlatLevelSource + созданные блоки, повторный locate/tickets | PASS |
| Яга flat / moderate / steep | Native actual commit:795/909, steep отказ до записи | PASS |
| Новые дома Баюна ≥1024 | Actual feature X5000/5512/6024, реальные разные UUID, restart same UUID | PASS |
| Три маркера в исходном мире владельца = три NPC? | Пользовательский мир/карта не открывались по ограничению задачи | NOT_RUN; исходные UUID не установлены |
| Сохранение / повторный поиск | 32 exact block witnesses после полноценного restart; no duplicate records | PASS |
| Cancel / restart / release | Preparation cancel, replacement, partial-write cancel, restart reservation, native tickets0 | PASS |
| Автоматический rollback/resume частичной Чуди | Не реализован в прежней очереди и этим пакетом не добавляется; construction остаётся защитной reservation | BLOCKED для полного rollback/resume |
| Natural survival discovery, путь книги/поручений без OP, реальная случайная частота Баюна | Source path сохранён; dedicated placement не заменяет полный плейтест | NOT_RUN |
| Два настоящих клиента/MP/полный модпак/GPU/render screenshot | Не запускались | NOT_RUN |
| Java21/resources/production JAR | Логи/receipt/resources.json | PASS |
| Ручная приёмка / выпуск / установка | Владелец ещё не принимал, JAR не устанавливался | NOT_RUN |

## Границы и риски

1. Это **частичный технический пакет IN_PROGRESS**, не вся #275 и не owner acceptance #295. Открытые обязательные результаты выше запрещают «Тестирование/Готово» и финальный релиз.
2. Read-only поиск ограничен загруженными native starts и завершёнными manual records в указанном радиусе. Незагруженные native starts без manual record не индексируются с диска; будущие worldgen кандидаты не выдаются за построенное. Страницы seed-кандидатов/типизированный `search` не строят и дают явный отказ. Прежний `abandoned locate <kind>` проверяет actual loaded pieces.
3. Паутина, заборы и mossy cobblestone не разрешены к сносу. На занятом участке явное создание может законно отказать; новая позиция выбирается отдельным запросом, а не шестью скрытыми тяжёлыми попытками.
4. Внешние claims и старые пользовательские постройки из неотличимых природных блоков не имеют доказанной универсальной защиты. Не выполнять dev create в ценных старых мирах. Сам мод не может достоверно узнать происхождение любого старого BlockState.
5. Частичная Чудь после отмены не откатывается автоматически. Reservation остаётся и защищает место от следующего строительства/ложного locate; автоматическое удаление или достраивание без отдельного решения не выполняется.
6. Строгий выбор террасы Яги может чаще отклонять скалистую местность. Проверка на соседних естественных склонах и клиентская оценка естественности ещё у владельца.
7. Цифры 19–39с — длительность конкретного native сценария; это не гарантия latency, measured MSPT/FPS полного модпака и не исправление #299. Графических экспериментов/изменений драйвера не было.
8. Старые Баюны не удаляются. Минимум1024 относится к новым домам, не обещает очистку старой карты.
9. Соседние риски для регрессии: книга/исследование Чуди, очередь конкурирующих созданий, заметность входов, попадание в старые protected chunks, population/load order Баюна и подбор террасы Яги. Loot/частоты других мест не менялись.
10. Откат кода — обратный коммит в `hell`, без reset/force и без перезаписи миров. Миры с частичной постройкой нельзя «лечить» удалением ledger: это снимает защиту с незавершённой области. Перед ручным сравнением использовать копию мира.

## Как проверить владельцу в Minecraft

Только после **отдельной ручной установки владельцем** JAR с SHA выше в копию HELL 1.21.1/NeoForge21.1.255/Java21. Этот пакет сейчас **не установлен**. Backup старого JAR и копия мира обязательны для контролируемого сравнения; дублей slavicmyths JAR быть не должно. Исходный основной мир не использовать для dev create.

1. **Новое обычное выживание, без OP:** пройти существующие условия доступа к Яге/Баюну, открыть книгу, получить поручение Чуди и запросить место через предусмотренную сюжетную кнопку. Ожидание: статус явного создания → область/цель после успеха; дойти пешком до реального входа, проверить зал/проходы и продолжение поручения. Отказ должен назвать причину, не сообщать ложное «найдено». Если условия доступа сами не проходят — это не PASS генерации, зафиксировать шаг/seed.
2. **Повтор без OP:** повторить тот же запрос/поручение; ожидание — прежнее место, один NPC/запись, без второй постройки. Выйти с сохранением и зайти; проверить вход, блоки, NPC и книгу.
3. **Яга:** в новом/тестовом обычном мире запросить дом из книги у холмистой местности, дойти без телепорта. Ожидание — небольшая терраса/устойчивые ноги; не высокая земляная башня над пропастью. На крутом обрыве корректен отказ и выбор другого кандидата. Осмотреть дверь/лестницу, поговорить с NPC, повторить запрос после relog.
4. **Баюн:** исследовать новые лесные чанки. Записать координаты и UUID новых NPC (UUID — отдельно OP-диагностика), отличать живого кота от старого waypoint. Новые дома должны быть не ближе1024 по X/Z; старые близкие маркеры не должны сами исчезнуть. Повторный запрос книги должен использовать известное место. Проверить после relog.
5. **Старая копия мира, без OP:** прийти к ранее найденным городу/Баюну/Яге, сравнить блоки, NPC, книгу и доступ. Ничего автоматически перестраивать/удалять не должно. Мир с оригинальными тремя близкими котиками нужен именно для диагностики UUID/waypoint, которая здесь NOT_RUN.
6. **OP-диагностика отдельно от выживания:** на пустом месте `/locate structure slavicmyths:chud_city`, `...:chud_abandoned`, `...:gorodishche`, `...:serpent_hill`; `/slavicmyths dev city search`, `/slavicmyths dev abandoned search`. Ожидание — известная постройка либо явное отсутствие в ограниченном охвате, без сообщений «Создание», новых блоков и очереди. Повторить10раз, затем сохранить/перезайти.
7. **Явное OP-создание только в отдельном мире:** `/slavicmyths dev generate city`, `/slavicmyths dev generate abandoned medium`; для superflat `/slavicmyths dev create gorodishche`. Ожидание — физические стены/входы, конкретные координаты после завершения; повторный locate ничего не строит. Отдельный сундук/портал в пересечении должен вызвать безопасный отказ; не проводить опыт в ценной постройке.
8. **Отмена:** `/slavicmyths dev generation cancel` во время подготовки и отдельно после начала записи. В первом случае блоков нет; во втором возможна частичная защищённая construction, которая не выдаётся как готовая. После сохранения она остаётся защищённой. Не удалять её ledger вручную. Проверить, что сервер нормально закрывается и новое создание в другом свободном месте возможно.
9. **MP:** два настоящих игрока повторяют запросы одного места, один выходит во время подготовки. Ожидание — нет дублей, неверной выдачи задания/координат, утечки tickets; после restart оба видят тот же результат. Этот тест здесь NOT_RUN.

**НЕ ПРОВЕРЕНО:** полное выживание/наградная цепь без OP; разные клиентские масштабы/visual render; исходные три маркера владельца; full-modpack client и MP; произвольные claims/старые каменные постройки; rollback/resume; случайные natural частоты и дальний disk-only native поиск. Не считать их проверенными по clean build.

## Следующая задача

Подготовлена [передача #296 → #278 → #297 → #298 → #299](HANDOFF.md). Следующие реализации **не запускались**, их статусы не переводились в готовые. Первый пакет остаётся «В работе», Issues OPEN, OWNER_PENDING.

## Квитанция исходников

Source commit: `5afe3a3d1177bd225ec0aed53639ad5649458d05` (`hell`). Этот commit содержит точные Java исходники production JAR с SHA выше. Следующий commit добавляет только полные raw logs, .gitattributes для их byte preservation и эту квитанцию; Java не меняется. Установки в PolyMC и приёмки владельца не было.
