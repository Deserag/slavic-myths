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
