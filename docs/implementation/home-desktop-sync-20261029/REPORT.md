# Домашняя синхронизация 29.10.2026 — IN_PROGRESS
Репозиторий D:/slavic-myths, hell: 537e3a7a → a5def9a94d57fabc34bc8673ef7194dc53998239, fast-forward. Исходное дерево чистое. main не менялся. Ветка будущих боссов только учтена, не слита. Полный аудит всех локальных копий и всех требований мастер-эпика НЕ завершён. Production source edits отсутствуют, commit/push не выполнялись.

| REQUIREMENT | EVIDENCE | RESULT |
|---|---|---|
| Java21 clean build | exec: BUILD SUCCESSFUL 4m12s | PASS |
| Production resources/JAR | verify_current_resources.py: 8729 resources, 4974 JSON, 1599 PNG, 2123 models, 2190 Java21 classes | PASS |
| Native runtime baseline | D:/slavic-myths-backups/home-sync-20261029/gametest.log: All 32 required tests passed; seed20261024, 1 headless launch | PASS |
| Полный пятиseed повтор | не запускался в этой сессии | NOT_RUN |
| GUI/плащ/цвета/хват луков/Чудь #349/#298 | нет изолированного GUI | BLOCKED_ISOLATION |
| Survival/MP/relog/старый мир | не запускалось | NOT_RUN |
| Приёмка владельца | не получена | OWNER_PENDING |

## Фактический scope
Руды, 8 wearable, 5 специальных оружий, печь и 6 структур уже присутствуют в текущем hell; это не доказательство полной игровой приёмки. #349: creative fallback FieldBowItem есть; InfernalBow сохраняет серверную special-shot логику; цвет/хват и approved Чудь остаются открытыми. #327/#331–336 требуют дальнейшей поэлементной матрицы и визуального аудита. 2.1.7 BLOCKED_TZ; 2.1.8/9 исключены; 2.2 и будущие боссы не реализовывались.

## Установка
Версия 2.1.6-HELL, source a5def9a94d57fabc34bc8673ef7194dc53998239.
Build JAR: D:/slavic-myths/build/libs/slavicmyths-2.1.6-HELL.jar
Installed: C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-2.1.6-HELL.jar
SHA256 обоих: 1cf4a2d8b16a301f475ed6fdf093d827d5adb7c1d2ae7367531a665da6c09032
Backup: C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/mod-backups/20261029-a5def9a9/
В mods один slavicmyths JAR. Основной HELL instance и миры не менялись. PolyMC/client 0 запусков.

## Как проверить в Minecraft
1. Выбрать Testing, убедиться в версии 2.1.6-HELL; новый survival мир и отдельная копия старого. Без OP исследовать новые чанки Незера, найти структуры, проверить проходы/опоры/сундуки. Мост без сундука.
2. Законно получить луки/экипировку: стрелять с боеприпасами, проверить расход, попадание, Shift special и cooldown. Негативно: без стрел в survival выстрела нет. Creative без стрел — отдельная диагностика.
3. Надеть броню короля, проверить ходьбу/прыжок/приседание, плащ и цвета, Steve/Alex; GUI/first/third person короткого и тяжёлого луков. В creative отдельно открыть вкладку голов и категорию Чуди.
4. Забрать лут, сохранить и полностью перезайти: повторной награды нет. Повторить вдвоём на копии мира, проверить снятие экипировки/эффектов.
5. OP только диагностика: /locate structure slavicmyths:trizna_pyre. Не считается доказательством естественной доступности.
НЕ ПРОВЕРЕНО: клиентские визуалы, survival получение, настоящий restart/relog, MP, старый мир, полная плотность worldgen, все биомы/seed, MSPT. Возможны регрессии соседних рендеров, оборудования и генерации.

## Риски/откат и следующий шаг
При закрытой игре убрать новый JAR и вернуть один старый из backup. Новые чанки этим не откатываются — нужна копия мира. Следующий шаг: завершить матрицу мастер-эпика и адресные проверки #349; без запуска новых 2.2/боссов. Полное задание пока IN_PROGRESS.


## Продолжение после замечания владельца — IN_PROGRESS

Прежняя запись об отсутствии пятиseed повтора устарела: он реально выполнен.
* Постройки: **105/105**, пять отдельных миров, seed20261024/20261025/20261026/3412026/9132026; `evidence/seed-*.log`.
* Все5 vanilla и4 mod biome: **20/20**, пять миров ×4 GameTest. Для каждого биома сверены sampler, записанный quart и реальный block biome; физический пол арены Горючих островов. Это diagnostic requests, не survival/normal loading. `evidence/biomes-v2-*.log`.
* Расширенный baseline: **35/35**, `evidence/full-queue-v3.log`. Реальные3лука ammo/creative/undercharge; четыре временных fire, отказ gamerules/неприродного пола,70t expiry/защита чужой замены.
* Native gear: armor96030, weapons1233, furnace6; не выдаются за gameplay.
* Creative Java parser2115checks/1045items; исходный extractor охватывает текущие семейства/IVtier; все19999 source/resource/JAR gates PASS на локальном кандидате после RU heads.
* Production изменение этой сессии: добавлена `itemGroup.slavicmyths.heads` в ru_ru. Ни одна игровая Java-механика не переписана ради количества изменений.
* Исправления QA: IVtier/families/historical log paths; biome quart-versus-Voronoi boundary; fire fixture включает и возвращает GameTest gamerules; boss fixture ждёт400t по production bound64/4×20=320t, убран direct spawn diagnostic. Неудачные прогоны сохранены; они не засчитаны PASS.
* Всего до remote client: **18 локальных Minecraft headless-процессов**, включая3 неуспешных (biome v1, fire fixture v1, queue timing v2). GUI локально0. Существующие fixture предупреждения block entity для air/barrier присутствуют — logs не называются чистыми.

### Синхронизация и публикация
Remote послеa5def9a9 продвинулся доc5120df6. Адресные независимые изменения2.2 другой сессии сохранены обычным merge существующейhell; main/experimental branch не сливались. В том числе remote добавил5lootJSON и1NBT2.2.8 — это чужие уже опубликованные ресурсы, не наша реализация2.2. Registry/worldgen2.2 эта сессия не добавляла.
Собственные commits204a0b0b иed6b888c опубликованы вhell, объединённый checkpoint e760aabb10d26b0428cc5f6ff50acdf15926b714. История не переписана, два отклонённых push не привели к force.

### Изолированный GUI — попытка снять блокировку
Подготовлен отдельный тестовый мод, исключённый изproduction, и workflow `hell-isolated-visual-qa.yml`. Запуск разрешён только с GITHUB_ACTIONS=true и RUNNER_ENVIRONMENT=github-hosted; Gradle task отказывает обычному локальному запуску. Runnerubuntu/Xvfb/Mesa не получает доступ к локальному desktop/PolyMC/мирам. Production загружается именно изJAR вmods; receipt сохраняет егоSHA.
Кадры:19обычныхвариантов (четыре Шептуна),4босса,4трофея с четырёх сторон; две брониfront/back/crouch/GUI;3лукаfirst/draw/third/GUI;4формыЧудиfirst/GUI. Это искусственные creative static fixtures, не survival/полнаяAI/MP/аудиоприёмка.
[Удалённый visual run](https://github.com/Deserag/slavic-myths/actions/runs/40904333746) наe760aabb — IN_PROGRESS. Пока не получены и не просмотрены кадры, visual PASS не ставится. Новый local clean build выполняется после remote resource additions.

### Реальные блокировки источников
Повторный SHA-поиск:8909PNG/ZIP entries,0совпадений. Боссы/биомы:14исходников MISSING, один UNVERIFIED_SOURCE; Downloads/attachments/worktrees проверены. `boss-reference-audit.json`, обновлённый reference-search.json. Это не разрешает перерисовать принятый облик. Genuine audio/source и owner approvals по-прежнему отсутствуют.
Полная строковая приёмка и непроверенные сценарии: [ACCEPTANCE.md](ACCEPTANCE.md). Общая задача не завершена.


### Текущий установленный checkpoint e760aabb
Java21 clean build1m35s, resources8735/classes2190 PASS. SHA51ce73c42243bb48dfaf5aabaab8ba6003ff09d8be39f375b3bd171cffd9ca95.
Точный installed/backup вinstallation-final.json. Одинslavicmyths modId проверен поmetadataВСЕХ JARпрофиля, не только именифайла. Testing не запущен нами локально; UI/основнойHELLмир не затронут.
CI40904333901SUCCESS наe760aabb,35/35. Дополнительный локальный v4 фактический bow hit/flail pulse разместили по4leasedFIRE вокруг цели; общий35/35. Теперь локальныхheadlessпроцессов19 (3 неуспешных сохранены). Клиентский удалённый прогон учитывается отдельно после окончания.
Статические Creative gates20005, Java parser2115. Оригинальные art/audio и ручная owner приёмка по-прежнему не получены.
