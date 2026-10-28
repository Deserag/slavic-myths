# Nether: ревизия первых трёх биомов — 28.10.2026

**Разрешено владельцем:** работать над уже обсуждёнными `2.0.2 Навья роща`, `2.0.3 Чёрная гарь`, `2.0.4 Чертоги заблудших`. **Строго исключено:** четвёртый биом / `2.0.5 Горючие острова` и его постановка. Рабочая ветка `hell`, `main` не меняется. Стартовый HEAD данной ревизии: `3ea423dda9c2efeebcdea2e3a331007883ae895b`. Исполнение не равно приёмке и не снимает owner-only проверки.

## Сводка конкретных production-поправок

| Биом | Дефект / риск | Изменение | Проверка | Статус |
|---|---|---|---|---|
| 2.0.2 Навья роща, `navya_heart` | `NavyaHeartPiece.postProcess` требовал загрузки 3×3 соседних чанков **при обработке каждого отдельного chunk clip**, из-за чего валидные части могли быть пропущены | `NavyaHeartStructure.findGenerationPoint` сначала вызывает `StructureCandidates.clearAccepted` для bounding box и vanilla accepted footprints; `NavyaHeartPiece` теперь на этапе фактического chunk postProcess проверяет residency / references **только текущего обрабатываемого чанка** | SOURCE REVIEW: две существующие структуры и действующий preflight helper осмотрены; реальный worldgen/проходимость в Survival **NOT_RUN** | IN_PROGRESS / NOT_RUN |
| 2.0.3 Чёрная гарь, Жареница | `CinderEncounters.claim` ранее создавал терминальную запись для **несуществующей** арены (при `old==null`), разрешая ложный claim | `claim` возвращает false для отсутствующего anchor, чужого UUID и уже побеждённого босса; `CinderMob.dropCustomDeathLoot` выдаёт уникальные трофеи только при непустом `home` и успешном journal claim | Написаны отрицательные assertions в `tools/cinder-tests/.../CinderOffline.java`; **NOT_RUN**, реальный drop/reload/MP **NOT_RUN** | IN_PROGRESS / NOT_RUN |
| 2.0.4 Чертоги заблудших, Постень | `PostenReconciliation.inspect` разрешал план восстановления по внешнему snapshot с чужими UUID масок или с уже погибшей копией ещё в фазе тела | Задаёт **детерминированные UUID каждой маски** на основе arena и имени маски, сверяет фазы и 4×65 до split | Два отрицательных offline fixture добавлены в `LostHallsOffline.java`; **NOT_RUN**, реальный EntityType/renderer/arena/spawn пока отсутствуют | IN_PROGRESS / BLOCKED_DEPENDENCY |

**Коррекция связей задач:** фиксы Жареницы относятся к **[#266](https://github.com/Deserag/slavic-myths/issues/266)** и финальному **[#267](https://github.com/Deserag/slavic-myths/issues/267)**, НЕ к #264 (чернодрево). В трёх ранних commit messages ошибочно упомянута `#264`; хеши не переписывались, ни один файл чернодрева не менялся. Навья `navya_heart` связана с #275/#257, Постень с #293.

## Коммиты этой ревизии

- `3b42324b0cdbb43843a24ec57a79591c8bfa6bee` — корректная проверка claim Жареницы.
- `cba01b69dfcbe2f39f62b9661d887d9c7b0d1230` — защита уникального лута Жареницы от босса без зарегистрированной арены.
- `d762c8825c92d3783225b4ece6eaf2f4e66d4a99` — негативные assertions на ложный claim.
- `ce64eb3fc6f3a0fcb30bccd6eff3c84f7ba12dc1` — preflight пересечений «Сердца Навьей рощи».
- `b88ab154e1a1dbec83f53a7d4664e5e27dcebe45` — chunk-local `postProcess` без требования соседних чанков.
- `8fe6e6a41f1a4d508493145eb52e20fc43016e33` — Posten: строгие UUID и phase contract.
- `862e0ac5ec716ba2ca5454d44f6f8edc1909f0b1` — негативные Posten fixtures.

## Недоделки и честная проверка

| Гейт | Реальная возможность / результат | Итог |
|---|---|---|
| Файловый scope | Сравнение `git compare` GitHub: только `src/main/java/org/slavicmyths/{navya,blackcinder,losthalls}` и названные два offline теста; **никаких файлов четвёртого биома** | PASS (source scope only) |
| Java21 clean build | В данном браузерном рабочем окружении отсутствует локально доступная копия репозитория и Gradle dependency cache; GitHub Actions в `hell` не предъявил runs/statuses | NOT_RUN |
| Offline assertions | Добавлены, но не запускались на JDK/Gradle | NOT_RUN |
| Navya #280/#281 | Фог/night-vision, реальные UV/силуэты мобов и FallingTree должны проверяться в установленной игре; не менялись на глаз без фактических кадров | NOT_RUN, owner QA |
| Black Cinder #261–#267 | Есть natural biome, entity types, бой/блоки в исходниках, но гарантия доступности арены, артовое сравнение и survival/MP остаются на проверки | NOT_RUN |
| Lost Halls #288–#294/#301–#303 | Backend присутствует, но нет полноценного registered Nether biome, настоящей chunk-геометрии S1–S6, пяти EntityTypes, Постеня, предметов, руны и физических голов | BLOCKED_DEPENDENCY |
| Исходные PNG | Реестр `docs/concepts/lost-halls/APPROVED_REFERENCE_ASSET_INDEX_2.0.4.md`: ZIP обнаружены в ChatGPT Library, но raw bytes не предоставлены окружению GitHub; пять SHA не верифицированы повторно | BLOCKED_REFERENCE (art) |
| Minecraft / dedicated server / GameTest / PolyMC | **0 запусков**, не устанавливался JAR | NOT_RUN_ENV_BLOCK |

**Проверка владельцем позднее:** новый мир + копия старого; 5 seed × 4 удалённые области; достижимые `navya_heart`, `zharenitsa_arena`, Чертоги S1–S6; повторная загрузка, победа и единичные награды, 2 клиента, реальная видимость night-vision, FPS/MSPT. Для трёх биомов не выдавать Source Review или офлайн logic за игровой PASS. До QA не закрывать Issues и не назначать owner-accepted.

**Риски:** Navya preflight может отклонять больше кандидатных площадок в насыщенных областях; только реальное исследование структуры и `structure_set` показывает итоговую частоту и гарантии. Cinder journal gating изменяет добычу от созданных без `home` боссов, но не от корректно размещённой через arena структуры Жареницы. Для Posten эти проверки — строгое решение модели восстановления, физическую entity интеграцию ещё нужно реализовать. Отмена — адресный revert указанных production-коммитов, без reset/force/изменения 2.0.5.

**Следующий этап:** закончить #289–#293 и #301–#303 при физической доступности оригинальных PNG, потом #294 worldgen/QA; отдельно дождаться игровой сверки #280/#281 и натурального босса Black Cinder. Четвёртый биом НЕ трогать.


## Дополнительный адресный source-fix / проверка scope — 28.10.2026

**Исходный HEAD этого прохода:** `bbd32f672c9d1551e2a914a7eb3d2f1d306c4384`. **HEAD после пяти production/test commits:** `5e12e127e5440a89194dedaa506137e9da34f34f`. Рабочая ветка `hell`, `main` не тронута. GitHub Compare подтвердил **ровно 5 изменённых файлов** в `navya/`, `blackcinder/`, `losthalls/` и соответствующих двух offline-test файлах. Файлы и задачи `2.0.5` (четвёртого биома) **не изменялись**.

| Биом / владелец задачи | Реальный дефект / изменение | Source commit | Проверка и результат |
|---|---|---|---|
| Навья роща, 2.0.2 — боевой safety gate | `NavyaMob.canAttack` дополнительно исключает `CombatTargeting.PROTECTED` и `CombatTargeting.sceneProtected`, в том числе для собственной area-атаки. Это **не** исправление тумана #280 и **не** приёмка моделей #281. | [`5e12e127`](https://github.com/Deserag/slavic-myths/commit/5e12e127e5440a89194dedaa506137e9da34f34f) | SOURCE REVIEW; безопасная/негативная проверка в игре **NOT_RUN** |
| Чёрная гарь, #266 и финальная регрессия #267 | `CinderEncounters.load` раньше молча игнорировал некорректные записи NBT, что могло терять defeated-state. Теперь версия журнала 1; старый журнал **без Version** читается; будущее/повреждённое/дублирующееся NBT сохраняется целиком в read-only и не позволяет register/claim. При корректном сохранении запись стабильна, дублирующий boss UUID на другом anchor и `null` отсекаются. | [`3b2c11be`](https://github.com/Deserag/slavic-myths/commit/3b2c11beee7f05e7b649487ca3a175901ae41c42) | [Негативные сценарии `b9b9c20f`](https://github.com/Deserag/slavic-myths/commit/b9b9c20f05317a590548f827ef89bbac0b6c1251): duplicate, future version, missing Defeated, old format, null UUID. **AUTHORED, NOT_RUN** |
| Чертоги заблудших, #291 | `LostHallsRoomPieces.begin` отклоняет disjoint / corner-only footprint вместо ложного принятия комнаты как одного многосоставного помещения; обход ограничен уже существующим лимитом 64 чанка, не обращается к миру. Это **не** генератор S1–S6. | [`78f8e38c`](https://github.com/Deserag/slavic-myths/commit/78f8e38cde5cf7d4ff3e2502f25e67038d5ab004) | [Негативные fixtures `2af755c7`](https://github.com/Deserag/slavic-myths/commit/2af755c7a359aca8dad75fa18bc6319c62649e03): отдельный остров, диагональный контакт, связные 3 чанка. **AUTHORED, NOT_RUN** |

**Границы доказательств:** GitHub compare — `PASS` исключительно для файлового scope; combined commit statuses на конечном HEAD пусты, **CI BUILD NOT_RUN**, исходники **НЕ КОМПИЛИРОВАНЫ** здесь. GitHub-connector сам не выполняет Gradle/Java тесты. В этой сессии **0 запусков** Minecraft, PolyMC, GameTest, dedicated server; это сохраняет запрет `AGENTS.md`. Ранее собранный JAR не отражает новые изменения и **не должен** использоваться как доказательство их работы. Состояние `ART_ORIGINAL_COMPARE=BLOCKED_REFERENCE` для Чертогов: исходные PNG и пять SHA не удалось побайтово получить из Library/GitHub.

**Что НЕ выполнено:** невозможно считать третий биом готовым: отсутствуют полноценные игровые blocks/models/5 EntityTypes/Постень/награды/руна/3 heads, настоящие шесть физических пространств, их природная регистрация в Nether, natural spawns и арена. Первые два биома требуют визуальной/игровой регрессии, 5 seeds × 4 области и перезахода. #280/#281 требуют самостоятельных проверок в установленной игре; описанные здесь поправки не закрывают эти Issues.

**Игровой smoke checklist для владельца после новой JDK21 clean build/JAR с SHA:**
1. Новый Survival-мир без OP, затем копия старого мира: естественно найти Навью рощу и Чёрную гарь; отдельно продиагностировать наличие Чертогов (**на данном HEAD они ещё не появляются как полноценный биом**).
2. В Навьей роще проверить: обычный враждебный моб получает урон; сюжетный/защищённый персонаж не оказывается жертвой атак NavyaMob; проверить негативный случай при двух существах рядом.
3. В Чёрной гари естественно найти арену, победить Жареницу, получить трофеи **один раз**, сделать unload/relog/restart, повторно прийти — новая выдача недопустима. Повреждённые save-файлы вручную в боевом мире **не редактировать**; для негативного NBT сценария использовать только копию.
4. После **будущей фактической** реализации Чертогов проверить физические S1–S6, двух-/четырёхчанковые границы, обратный пеший путь, спавны/Постеня/награды и persistence. Новые `LostHallsRoomPieces` fixtures не заменяют эту проверку.
5. На 5 разных seed × 4 удалённых областях проверить доступность арен и сохранение vanilla Nether, после перезапуска и с двумя игроками отдельно проверить награды/синхронизацию. Записать screenshot/log/FPS/MSPT, а не считать `source review` игровым PASS.

**Риски и откат:** Новый fail-closed journal при некорректном/неизвестном NBT блокирует регистрацию Жареницы вместо потери записей; для восстановления требуется отдельная диагностика по копии мира. Граница `sceneProtected` может исключить некоторые игровые цели Навьих мобов — проверить при QA. Room chunk connectivity проверяет только контур, а не настоящую ходимость. Откат — адресный revert пяти названных коммитов; без `reset`, `force`, слияния `main` или вмешательства в четвёртый биом.

**Итог:** `IN_PROGRESS`, Issues **OPEN**, owner QA **NOT_RUN**. Дальнейший основной scope по Чертогам — source art + реальные игровые реализации #289–#293/#301–#303, и только после них #294 worldgen.
