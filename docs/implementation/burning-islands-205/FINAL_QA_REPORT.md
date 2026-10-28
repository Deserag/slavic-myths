# Горючие острова 2.0.5 — результат исполнения, IN_PROGRESS / BLOCKED

**Биом НЕ полностью реализован и НЕ передан на окончательную приёмку.** Весь production-код и ресурсы ниже написаны в этой сессии; естественная география/доступность, обязательное аудио/совпадение с original PNG и игровые проверки отсутствуют. Владелец принимает все биомы лично. Issues остаются OPEN, Project №9 не менялся.

## Исправлено / действительно реализовано

### #313, природа — только data registry, этап BLOCKED

Есть `data/slavicmyths/worldgen/biome/burning_islands.json`, ResourceKey и таблицы трёх жителей. Это регистрируемые данные, а НЕ действующий природный биом. В действующий climate selector он не добавлен. **Островов, центрального вулкана, кратера, рва, мостов и природной арены в этой сборке НЕТ.** Вместо ложного lava-check проведён native аудит:5seed ×3окна, Y32/48/80; у гипотетического climate append534/1053выбранных проб не lava на уровне моря. Доказательство/пути установленных Minecraft sources/ограничения: [OCEAN_ARCHITECTURE_BLOCKER.md](OCEAN_ARCHITECTURE_BLOCKER.md). Это офлайн base columns, не5сгенерированных миров.

### #316, существа/добыча/руны — независимый production-код

- EntityTypes `heat_salamander`, `slag_crab`, `vent_golem`: 55/55/150HP, утверждённые damage/speed, нейтральные цели, вспомогательная тревога только уже загруженных same-type у первых двух;16блоков,200t cooldown,600t память, cap32получателя, без summon/retрансляции. Реальные NPC/creative/projectile/MP проверки не запускались.
- Отдельные модели salamander4лап/хвост, crabпанцирь/6опор/клешни, golemсегменты/руки/ноги; ModelPart/UV/pixel PNG/эмиссивные маски и idle/walk/warn/attack/hurt/death. Это самостоятельный технический арт, **совпадение с5original PNG НЕ подтверждено**.
- Реальные `magma_clot`, `magma_core`, `salamander_hide`, `crab_claw`, `magma_rune_fragment`; три entity loot table,3обычных shapeless recipe и recipe-book unlock advancements. Кожа и клешня расходуются на РунуI. Новые предметы имеют RU/EN, Creative metadata, отдельные16pxиконки. Стандартные рецепты должны отображаться JEI, но сам JEI в этой задаче не запускался.
- `rune_magma_1/2` подключены к существующим Runes/RuneDefinition/кузнице, только нагрудник, запрет второй руны семейства. Post-damage ответ2/4HP +2/4сек огня, общий40t cooldown, игнор природного урона/нулевого damage/recursive thorns, уважение allied/PVP/fire immunity/Fire Resistance. **Блоковая часть II НЕ реализована: безопасный отказ без setBlock**, явный tooltip. Natural-block provenance/restore не доказаны, `BLOCKED_TEMP_MAGMA_RESTORE`.

### #317–#323, босс — независимый production-код, НЕ полный этап

- `magma_lord`:850HP, P2на425, нейтральный до реального нападения, bossbar, home/spawn anchor и persist flag;6действий с raw14/18/0/16/18/20, release23/27/27/25/31/37;70t переход P2; spatial chest-core vulnerability60t/+35%;80t смерть. Каждая техника имеет модельный windup и ground particles; никакого terrain destruction. Fixed cast direction/геометрия, per-cast UUID set, loaded-only lookup до16участников; не удваивается vanilla difficulty scaling.
- Ответ дальнему инициатору — направленный телеграфируемый HANDS, технический максимум128блоков, без выхода за утверждённый32блоковый предел преследования. Реальные дальность/высота/уклонение/anti-cheese/Hard **не проверены**;128ипрочие инженерные числа не присвоены владельцу как согласованные.
- Новый journal `slavicmyths_magma_encounters` VERSION2: canonical boss/anchor/defeated/recipient/cores/readyAt. Неизвестный/повреждённый/старыйV1 сохраняется read-only, не обнуляется. Игровые205сохранения в этой сессии не создавались.
- Пакет голова1/сердце1/ядра4–6/существующая заражённая средняя душа1 поступает в inbox после80игровых тиков; полное admission на копиях inventory, отсутствие свободного места оставляет пакет на потом. Существующая soul family переиспользована. Player receipts и очередь находятся в том же player persistent tag, что сериализуется вместе с inventory; normal reload и частичная same-recipient reconciliation проверены NBT-тестами.
- **Cross-player crash exact-once НЕ доказан.** Возможный cut: файл playerA с наградой сохранён, журнал мира откатился, boss снова побеждён playerB. Персональный receiptA не является общей durable authority для B. Это отдельный `BLOCKED_REWARD_CRASH_ATOMICITY`, не заявление «дюп невозможен».
- `magma_lord_head` включён в прежнюю TrophyHeads/Tile/renderer; floor/side placement и facing наследуются от общего блока, jaw/shards/emissive pulse/6→8гистерезис client-only;1искра/200t на видимой голове, без world effects. Inventory использует отдельный статичный generated item icon. Челюсть исправлена после native bounds FAIL; текущие5поз внутри reserved cells. Реальное размещение/10голов/MP не запускались.

Все численные решения владельца и оставшиеся инженерные предложения — [BALANCE_DECISIONS.md](BALANCE_DECISIONS.md). Новых оружия, брони, аксессуаров, деревьев/жидкостей, рунIII и #315 нет. Сердце имеет честный tooltip о будущем gear patch; фиктивный рецепт не добавлен.

## Исходники/ресурсы

Production Java: `burningislands/IslandsContent`, `IslandsMob`, `MagmaCombat`, `MagmaRuneRules`, `MagmaRunes`, `MagmaRewardRules`, `MagmaRewards`, `MagmaEncounters`, `client/IslandsModel`, `client/IslandsClient`; интеграции в SlavicMyths, Runes, RuneDefinition, RuneRework, RpgScreen, JeiRituals, TrophyHeads, TrophyHeadBlock, TrophyHeadsClient. Новые руны исключены из старого алтарного JEI списка, чтобы не создавать фиктивные рецепты; tooltips нагрудника/кузницы показывают реальный ответный эффект и конфликт семейства, без общей ложной надписи «неактивна» и двойного описания. Старые руны не перерабатывались. Ни одного client reference в новых common class constant pools.

Assets:4`burning_islands_rigs/*.json`;8entityPNG(4base+4glow);9itemPNG;13itemmodels(9предметов+4eggs);1trophyblockstate/1blockmodel;RU/EN/catalog. Data:1biome,3recipes,3recipe-unlockadvancements,4entityloot(уbossempty:награда толькоinbox),1blockloot. Полный machine-readable production inventory/hashes: [source-manifest.json](source-manifest.json).

Дополнительный read-only поиск по профилю C:/Users/user включая hidden прочитал 36 001 PNG header, проверил SHA 34 кандидатов и просмотрел 126 ZIP до 30 MB, включая переименованные архивы. Совпадений с пятью approved SHA нет (reference-search.json); другие диски и архивы свыше 30 MB не исключены. Найдены только Markdown/SVG в репозитории. Самостоятельные ассеты не выданы за исходники из беседы. `BLOCKED_REFERENCE_BINARY`; ни import, ни SHA match originals не заявлены. Целевые аудиофайлы/лицензии отсутствуют, новых sound events/subtitle keys с фальшивыми путями не добавлено: [AUDIO_SOURCE_MATRIX.md](AUDIO_SOURCE_MATRIX.md), `BLOCKED_NEEDS_AUDIO_SOURCE`.

## Что фактически проверено

1. `JAVA_HOME=C:/Users/user/Documents/slavic-myths/.tools/jdk21/jdk-21.0.12.1+1`; стандартный `gradlew.bat --no-daemon clean build --console=plain`: **PASS21s**, без Curios init fallback и без stub зависимостей.
2. `gradlew.bat --no-daemon verifyBurningIslandsOffline verifyLegacyHeadsRegression --console=plain -I tools/burning-islands-offline.gradle`: **PASS11s**. Actual native ModelPart/UV/pose reset/trophy bounds412frames, pure timeline/geometry/cooldown/family conflict, aggregate4–6bounds, native ItemStack admission/components, binaryNBTjournal+receipt negatives.81916offline assertions — НЕ81916боёв. Отдельно прежние6голов ×600поз/nativeUV/bounds/drop matrix PASS. Исторические head140tools явно ограничены прежними6, новая голова проверяется отдельным205набором.
3. `verify_current_resources.py`: **PASS8302resources/2049classesJava21**,4655JSON/1491PNG/1992models; archive equals source, нет bundled dependencies/test classes. `verify_burning_islands.py`: scoped old-file hashes/no deletion, nonoverlap96UV islands, loot/recipe exact values, rune no-mutation, no alarm summons, common/client separation.
4. Generator `burning_islands_assets.py` повторно выполнился без единого изменения production bytes. `burning_islands_preview.py` прочитал только настоящие native baked polygons/UV; [4вида моделей](native-models-four-views.png), [jaw5поз](magma-lord-head-native-jaw.png), action GIF и [GUIиконки](inventory-icons.png). До этих новых EntityIDs моделей в055268cf не было: [before](before-models-absent.png), вымышленная старая модель не нарисована.
5. Пять native noise seed/base-column audits: контрпример climate-only selection, **не actual ocean/natural spawn test**.

Логи: [build](build-final.log), [offline](offline-final.log), [ocean](ocean-audit.log), [resource receipt](resources-final.json), [static/JAR receipt](static-jar-results.json). Промежуточные ошибки своего кода/теста исправлены до этих gates. Дополнительный native AttributeSupplier probe невозможен без registered NeoForge swim_speed: [сохранённый failure](offline-attribute-limit.txt); fake binding не добавлен, actual attributes/runtime codec/AI остаются NOT_RUN.

## JAR / Git / установка

- Source code commit **f9beb28d55cf76f76df0b5cc90a0e7b37fef102b**, поверх owner commit0fcbaf68. Начальный новый контракт055268cf. Commit0fcbaf68 сделал владелец во время работы; Codex не приписывает его себе. Commitf9beb28d — Codex, прямое разрешение в приложенном задании. Финальные отчёты фиксируются отдельным docs commit; production source послеf9beb28d не изменялся.
- Версия **2.0.8-HELL**, content stage2.0.5. Production JAR: `C:\Users\user\.codex\worktrees\lost-halls\slavic-myths\build\libs\slavicmyths-2.0.8-HELL.jar`; **24845769bytes**; SHA256 **102952bb4c573fcb370c13fca2e702d19fed189d6d965d89aa26cdde677cc9ab**.
- Новый кандидат **POLYMC_NOT_INSTALLED**, не копировался и не запускался. В тестовом экземпляре остался прежний установленный JAR SHA57445d037bd18238950cdf9cdc8b94714e2e2522cf0f942bbd95b199cb8ee3f1. Новый и прежний имеют одно имя/версию, поэтому проверять именно SHA.
- Minecraft/PolyMC/GameTest/server: **0запусков этой задачи**. Запуск предыдущей задачи сборки не является проверкой205.
- main/release/другие worktree/Issues/Project/owner-accepted не менялись; push/PR/merge не выполнялись.

## Не сделано / риски / откат / следующий шаг

**Не сделано:** ocean-only selection и вся природная геология/арена; доступность обычному игроку безOP; природные spawn; временная магмаII; исходный PNG match; genuine audio; cross-player crash guarantee; реальные игровые/визуальные/MP/performance/старые-сохранения gates. Поэтому все313/316/317–323 остаются IN_PROGRESS/BLOCKED/NOT_RUN, не «Готово» и не «Тестирование всего scope».

Риски: новые entities/attributes/recipe/biome codecs не грузились полноценным модом; баланс босса/высотный ranged-cheese/анимации не проверены; недостающие звук/исходники; указанный межфайловый MP crash cut; модифицирован общий trophy enum/renderer и native rune admission — прежние6голов прошли лишь offline regression. Не переносить новое содержимое в важный мир без копии.

Откат: текущий JAR в PolyMC вообще не заменён. Для будущего тестирования сохранить старый JAR внеmods, держать1modJAR, брать отдельный мир/копию. При появлении новых registry IDs в игровом save не удалять мод вслепую из этого save: использовать исходную нетронутую копию. Git reset/force/delete не выполнялись; source checkpoints сохранены.

Следующая необходимая работа в этой цепочке: доказанный terrain-aware ocean resolver/общая маска+естественная геология/арена; реальные originalZIP/OGG; provenance/restoreII и durable multiplayer reward authority; затем ownerQA всех биомов. Новые здания315 и gear не начинаются. [Точный ручной checklist](OWNER_SMOKE_CHECKLIST.md), [boss QA](BOSS_QA_REPORT.md), [полная матрица](ACCEPTANCE_MATRIX.md). Решение о готовности остаётся владельцу.

Кодовые коммиты Codex этой задачи: `4adb23c3` (boss/reward/native hardening), `f9beb28d` (точечная JEI/tooltip интеграция). Основной промежуточный `0fcbaf68` зафиксировал владелец.
