# Slavic Myths — 2.0.5: «Повелитель магмы» — окончательная исполнительская постановка

**Дата:** 28.10.2026. **Ветка:** только `hell` (main не затрагивать и не сливать до прямого решения владельца). **Трекер:** [#317 — эпик](https://github.com/Deserag/slavic-myths/issues/317), [#318 — референсы+3D](https://github.com/Deserag/slavic-myths/issues/318), [#319 — AI/фазы/урон](https://github.com/Deserag/slavic-myths/issues/319), [#320 — арена/worldgen](https://github.com/Deserag/slavic-myths/issues/320), [#321 — предметные награды и голова](https://github.com/Deserag/slavic-myths/issues/321), [#322 — анимации+audio](https://github.com/Deserag/slavic-myths/issues/322), [#323 — QA](https://github.com/Deserag/slavic-myths/issues/323). **Концепт:** [#310](https://github.com/Deserag/slavic-myths/issues/310); океанский биом: [#304](https://github.com/Deserag/slavic-myths/issues/304), [#313](https://github.com/Deserag/slavic-myths/issues/313). Другая работа — обычные мобы и Руна магмы I–II [#316](https://github.com/Deserag/slavic-myths/issues/316). Мелкие постройки [#315](https://github.com/Deserag/slavic-myths/issues/315) — другой будущий патч.

**Этап** одобрен владельцем для составления реальных implementation tasks. **Этот файл — ТЗ, а не уже написанный Java/worldgen, не доказанный JAR или игровая приёмка.** Точный тайминг приёмки может корректироваться после реальных измерений и решения владельца; не выдавать предложенное инженером за пользовательское утверждение.

## 0. СНАЧАЛА ИСХОДНИКИ, ИЗОБРАЖЕНИЯ И ПРАВИЛА

### Реальные оригиналы из ChatGPT (имеются; в GitHub ещё не загружены)
Оригинальные PNG действительно сохранились, повторно проверены SHA256 и упакованы в **`BURNING_ISLANDS_205_COMPLETE_REFERENCES_FOR_GITHUB.zip`**, SHA256 **`22256bf4df4f8c8abf6c5aec2ec6a9d30c3be02b0a3d7b59c5751908bc6edada`**, 12,002,229 байт. **ZIP передан в беседе ChatGPT, но НЕ лежит в GitHub**. Содержимое с уже готовыми репозиторными путями:

| При извлечении в корень git | SHA256 оригинала | Назначение |
| --- | --- | --- |
| `docs/concepts/burning-islands/references/BURNING_ISLANDS_205_BIOME_CONCEPT.png` | `a770acc9cb8401baa8199994f73cf28ee1fb3d43eff25fe0fd5bdbff92fbf605` | общий ландшафт острова над лавовым океаном |
| `docs/concepts/burning-islands/references/BURNING_ISLANDS_205_CREATURES_CONCEPT.png` | `a06f8d7730e43f93003a0a9c465c806e6e28a41260e7a61640d1f336924651d1` | саламандра, краб, голем, первоначальный образ босса |
| `docs/concepts/burning-islands/references/BURNING_ISLANDS_205_RUNE_MATERIALS_CONCEPT.png` | `47a9f11cd0e3c98e48983cf28c8b5ed7c6687689d9abb9cc5ee28edd7a47fdab` | два уровня рун, фрагмент, ядро, ресурсы |
| `docs/concepts/burning-islands/references/BURNING_ISLANDS_205_BOSS_CONCEPT.png` | `354c2c318c01ee67b21b60adc653a0a4747acc81786a433cb28e1c3fc26881a6` | одобренное направление полного босса |
| `docs/concepts/burning-islands/references/BURNING_ISLANDS_205_MAGMA_LORD_HEAD_PLACED_INVENTORY.png` | `287ce3f15e28b1a646cf31de1e51fab85519bb03dc935d83bb7aea920395bc93` | **владельцем утверждены обе формы головы — мир и GUI** |
| `docs/concepts/burning-islands/references/SOURCE_MANIFEST.txt` | included in zip | происхождение, размеры и имена |

**Пока GitHub PNG отсутствуют, обязательный статус `BLOCKED_REFERENCE_BINARY` для финальной артовой части.** Внимание: автоматизированный исполнитель на отдельном локальном компьютере не имеет доступа к контейнерным файлам этой беседы; извлечение ZIP должно действительно состояться в `hell` до запуска визуальной приёмки.

### Уже РЕАЛЬНО в GitHub, открываются как SVG
- [MAGMA_LORD_ORTHOGRAPHIC_DESIGN_GUIDE.svg — спереди/сбоку/сзади](references/MAGMA_LORD_ORTHOGRAPHIC_DESIGN_GUIDE.svg) — самостоятельный технический чертёж без включённых original PNG.
- [MAGMA_LORD_TROPHY_HEAD_MODEL_GUI_GUIDE.svg — поставленная голова и иконка](references/MAGMA_LORD_TROPHY_HEAD_MODEL_GUI_GUIDE.svg) — реальный воспроизводимый чертёж, но НЕ замена оригиналу.
- [references/README.md](references/README.md) — manifest/импорт оригинальных картинок.
- [MAGMA_LORD_REWARDS_HEAD_CONCEPT_2.0.5.md](MAGMA_LORD_REWARDS_HEAD_CONCEPT_2.0.5.md) и [MOBS_AND_REFERENCE_LOCK_2.0.5.md](MOBS_AND_REFERENCE_LOCK_2.0.5.md) — принятие художественного направления.

**Приоритет:** реальный последний одобренный PNG головы/босса выше упрощённых схем SVG; если геометрия чертежа противоречит оригиналу, корректировать модель по PNG, не объявлять лишнее количество рук/рогов автоматически утверждённым.

**До code changes читать:** `AGENTS.md`, `docs/PROJECT_WORKFLOW.md`, `docs/requirements/README.md`, `docs/requirements/00-MASTER-CONTRACT.md`, `01-WORLDGEN.md`, `03-MOBS-BOSSES.md`, `04-NETHER-BIOMES.md`, `07-LOOT-REWARDS.md`, `10-ART-AUDIO-UI.md`, `12-SAVES-MP-PERFORMANCE.md`, `13-TEST-GATES.md`, `14-OPEN-REGRESSIONS.md`. Сверить actual java/resource registries/loot/head support и чужой dirty worktree. **Один writer**, no force/reset, без автоматического изменения main/старых world IDs. Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. **Не запускать Minecraft/GameTest/PolyMC/сервер**, если запрет в AGENTS.md не отменён отдельным решением. Непроверенное = NOT_RUN.

## 1. АРЕНА / ПРИРОДНАЯ ГЕНЕРАЦИЯ #320
- Биом 2.0.5 **только в настоящем большом лавовом океане**. Крупный сравнительно ровный остров → широкий пологий вулкан → кратер с обычной лавой → сухая **центральная каменная площадка с боссом**, кольцевой лавовый ров → минимум один удобный устойчивый переход, концептуально два. Обязательные survival routes без прыжков через лаву, никаких обязательных перлов/полёта.
- Ориентиры ширины площадки **45–60 блоков**, лавового рва **4–7 блоков**, двух переходов **4–5 блоков шириной** не являются железобетонными числами — подстроить по натуральному worldgen, player path and actual boss AABB, сверить с владельцем.
- **Гарантировать босса/арену для каждой полноценной крупной связной области**, а не chance-per-chunk. Корректно детерминированно создавать при новогенерации, предотвращать коллизии чанков, повторный вызов и дюп.
- Беречь lava sea Y, старые миры, структуру vanilla Fortress/Bastion/порталы; после победы сохранять статус и контролируемое дальнейшее появление по общему policy. Исключить постоянные изменения мостов/камня от атак.

## 2. БОСС: СКЕЛЕТ И АНИМАЦИИ #318/#319
- Высокий тёмно-магмовый, каменные отдельно парящие сегменты/конечности, огненный сердечник на груди; 3D настоящие `ModelPart`, UV на реальных pixel texture с отдельным правильным emissive layer. Отличается от Жерловика-голема; никакой Dota/Clash Royale скопированной model/voice/sound.
- Модельный начальный масштаб ~**5–6 блоков**, обязательно полное сравнение с игроком и габаритами кратерной площадки; реальные joints `head`, `jaw`, `core`, `core_shells`, `arms`, `back_limbs`, `legs`, `floating_shards`, pivots/hitbox.
- **Нейтрален до нападения**. 850 maxHP, 2 фазы: phase I 850–426; phase II 425–0. Изменение на уровне половины HP, с настоящей анимацией расходящихся каменных пластин, открытым ядром; без многократной трансформации после reload.
- Обязательные animations: `idle_float` (дыхание), `look`, `neutral_notice`, `aggro_wake`, `chase`, `claws_windup`/`claws_hit`, `fracture_charge`/`fracture_release`, `pull_windup`/`pull_release`, `hands_windup`/`hands_strike`, `shadow_eruption_telegraph`/`shadow_eruption_release`, `eruption_ring_charge`/`eruption_ring_release`, `core_expose`/`core_close`, `phase2_transition`, `hurt`, `death_disintegrate`.
- Для каждой анимации — таймлайн frames/ticks с idle/windup/hit/recovery на конкретных ModelParts. Реальные движения не заменяются только particles/screen flash. Использовать эффекты как дополнение; читаемый silhouette и в фазе без свечения.

## 3. БОЕВОЙ НАБОР: УРОН ДО ARMOR #319

| Атака | P1/P2 | Baseline raw HP | Телеграф/механика |
| --- | --- | --- | --- |
| **Каменные когти** | P1 и P2 | **14** | широкий удар, 1–1.2с заметного замаха, уход вбок |
| **Магмовый разлом** | P1 и P2 | **18** | три направленные раскалённые дорожки, предупреждение до выхода, безопасные полосы |
| **Притяжение теней** | P1 и P2 | **0 напрямую** | управляющее притяжение, не гарантированный бросок в лаву, возможность сопротивляться/отойти |
| **Руки преисподней** | преимущественно P2 | **16** | телеграфируемый дальний удар дополнительной конечности |
| **Теневое извержение** | P2 | **18** | три последовательные отмеченные области, безопасная область всегда существует |
| **Кольцо извержения** | P2 или усиление P1 по QA | **20** | сильная расширяющаяся ударная волна от ядра, не автоматическое попадание |
| **Уязвимость грудного ядра** | P2 | примерно **+35% входящего урона** | ~3 сек после тяжёлого каста, реально точная зона попадания / counterplay |

Баланс рассчитан по vanilla armor/toughness, обычный damageSource `mobAttack`, без armor bypass, без рунических/зачаровательных бонусов, без зачарований, щита, Resistance и DoT:
| Raw | Iron Normal | Diamond Normal | Netherite Normal | Iron Hard | Diamond Hard | Netherite Hard |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| 14 | 9.52 | 4.76 | 4.37 | 17.22 | 8.61 | 7.73 |
| 16 | 11.52 | 5.76 | 5.25 | 21.12 | 10.56 | 9.41 |
| 18 | 13.68 | 6.84 | 6.19 | 23.76 | 12.69 | 11.23 |
| 20 | 16.0 | 8.0 | 7.2 | 26.4 | 15.0 | 13.2 |
Все числа в HP получаемого единичного damage event. На HARD часть атак **one-shot** незачарованную iron-броню, поэтому фреймы телеграфов, escape windows, длительность восстановления и запрет неизбежных последовательных ударов — **жёсткие acceptance критерии**. Не выводить из raw значения автоматическую победу по DPS; измерять actual attack cadence. Сверить Тугарин `TugarinCombat.java` (одиночные 12/14/16) и Моровое Лихо `HigherMor.java` (12/14 + магическая аура); источник проведённого анализа [#310](https://github.com/Deserag/slavic-myths/issues/310).
- Босс не застревает при стрелах/ограничениях, нет AFK-exploit у краёв или лагов в AI, серверное решение урона соответствует реальному synced frame. Нет ground-edit ради разлома, допустимы bounded particles/area overlay, не разрушать аренную платформу.

## 4. ЗВУКИ И ВИЗУАЛЬНАЯ ДРАМАТУРГИЯ #322

| Уникальное событие | Trigger | Требуется настоящий звук + кадры |
| --- | --- | --- |
| `magma_lord_idle_rumble` | idle 1–2 | низкое глухое многослойное вулканическое дыхание, не непрерывный spam |
| `magma_lord_aggro_roar` | после первого реального нападения | отдельный рёв, щелчок размыкания каменных пластин |
| `magma_lord_claws_prepare / impact` | claw windup / release | скрежет камня + удар |
| `magma_lord_fracture_charge / release` | предсказуемые 3 линии / damage | нарастающий каменный разлом |
| `magma_lord_pull_start / release` | управляемое притяжение | полый низкий свист и отпускание |
| `magma_lord_hands_prepare / strike` | подготовка дополнительной руки | трение каменных сегментов и резкий удар |
| `magma_lord_shadow_eruption_charge / burst` | 3 предупреждения / последовательные всплески | различимые предупредительные импульсы и отдельные releases |
| `magma_lord_ring_charge / impact` | вспышка раскрытого ядра / расширяющееся кольцо | сильный «пульс горна» с паузой до удара |
| `magma_lord_phase_break / phase_roar` | переход на 50% | самостоятельная трансформация, треск каменной оболочки и рёв |
| `magma_lord_core_exposed / close` | временная уязвимость P2 | слышимый signal vulnerability, не маскирует другие cues |
| `magma_lord_hurt / death` | реальный урон / смерть | hit reactions / финальное затухание и осыпание |
| `magma_lord_head_jaw_crack / wake_exhale` | подход к установленной трофейной голове | единичный каменный треск и низкий выдох |
| `magma_lord_head_place / break` | установка/разрушение Trophy | два различимых коротких взаимодействия |

Имена — ориентиры registry/event namespace (не безусловные definitive IDs). Нужны **реальные легально полученные OGG**, отдельные `sounds.json`, subtitle keys в RU/EN, callsite, loudness, real JAR asset audit; не заменять их автосгенерированным синусоидальным гудением/фальшивым треком. **НЕТ реальных OGG → BLOCKED_NEEDS_AUDIO_SOURCE**, не PASS и не «звук готов». Законный vanilla layering — только если звуковой стандарт это действительно позволяет и результат владелец примет. Громкость head с proximity и cooldown, 10 голов рядом не должны создавать лавину аудио.

## 5. НАГРАДЫ И ТРОФЕЙ #321
**Только предметы-материалы и декоративные трофеи в 2.0.5; никакой новой экипировки.**

| Предмет | Количество | Правило |
| --- | ---: | --- |
| **Голова Повелителя магмы** | **1** | 100%, настоящая размещаемая голова/Item/Block/3D+GUI |
| **Сердце Повелителя магмы** | **1** | 100%, уникальный предмет-материал для будущего gear patch (не готовая экипировка и не fake recipe) |
| **Магмовые ядра** | **4–6** | диапазон гарантированно, подключить к уже утверждённым магмовым ресурсам #316 |
| **Заражённая душа** | **1** | 100%, использовать реальный существующий вариант, проверить актуальный registry/tier |

При убийстве выдаётся весь целевой unique set, без reroll и дублирования из двух hook points/двух reward queues. При смерти в лаве/у края игрок не должен терять уникальное сердце/голову. В Multiplayer реализовать политику одной победы и распределения награды согласно действующим боссам/общим правилам, не создавать дубли.

**Голова по конкретному утверждённому image:**
- 3D обсидианоподобный зубчатый череп/венец, высокие шипы, ярко-оранжевые магмовые cracks и eye cavities, настоящая подвижная каменная lower jaw, 2–3 небольших плавающих каменных сегмента.
- **В мире**: геометрическая голова с анимациями, floor/wall, correct facing, компактный stable collision, собственный Block/Item, извлечение и установка без дюпа; не копировать редкую голову Постеня с циклом масок.
- **В инвентаре**: отдельная **статичная** читаемая иконка GUI (проверка на 16/32px), не gif/jittering/in-world pedestal baked into Item.
- `idle` — медленное «дыхание магмы» по локальным трещинам за ~4–5с, floating shards; `wake` — игрок входит в пределах 6 блоков, glow глаз + челюсть открывается на 12–20° за ~0.6–1с, ОДИН специфический каменный crack и выдох; `sleep` — когда за 8 блоков, return 1–2с; редкие 1–2 particles за 8–12 сек per visible head; GUI без движения.
- **Никаких gameplay effects или fire/lava/magma placement при установке/приближении**, никаких server scans every tick или мигающей физической blocklight; client-driven visuals bounded.
- Система `src/main/java/org/slavicmyths/trophy/TrophyHeads.java`, `trophy/TrophyHeadBlock.java`, `client/TrophyHeadsClient.java`, [boss head standard](../NETHER_THREE_BOSS_HEADS_LOCK_2.0.X.md). Голова реально соответствует источнику PNG и native ModelPart/UV; технический SVG лишь вспомогательный.

## 6. ЧТО НЕ РЕАЛИЗОВЫВАТЬ
- Оружие, броню, скипетры, амулеты, Obereg или любые новые готовые аксессуары: **будущий отдельный патч**.
- Новые типы деревьев/жидкостей/блоков вне утверждённой геологии.
- Малые пристани, плавильни, декоративные постройки [#315](https://github.com/Deserag/slavic-myths/issues/315).
- Мобов #316 или Руну магмы III в этом эпике. Нельзя ошибочно считать опубликованные ТЗ stage1/stage2 признаком их production готовности.
- Никакого общего переписывания всех старых TrophyHeads/куба-эффектов, чужих IP assets, фиктивных OGG/пустых модельных каркасов.

## 7. QA ПЕРЕД OWNER ACCEPTANCE #323
- 5+ seed, реальные координаты и отличающиеся окна, ocean-only biome; полный маршрут natural island→gentle volcano→crater→bridges→arena, без тупиков/сшивочных дыр, сохранность vanilla/старых миров.
- Boss: neutral→attacker, collision/hitbox, 850 HP, P1→P2 и после relog, отличимые 6 skills, normal/hard/iron/diamond/netherite numerical damage, melee/ranged, огнестойкость, игроки+союзные NPC, оригинальный клип анимаций/sounds.
- All loot 100%, one trophy head, material-only, no duplicate on reload, не тонет в лаве/инвентаре; real survival path, new soul uses actual ID.
- Head 3D vs static GUI, wall/floor/4 facings, distance 0/5.9/6.1/7.9/8.1 блока, 10 голов, 2 player / relog / unload, no gameplay effect, no audio spam, particle budget.
- Сравнить все original PNG SHA и source Native ModelPart render. Audio event→OGG→subtitles RU/EN→actual JAR; уникальные звуки не считать готовыми без actual OGG/owner accept.
- Честная матрица `REQUIREMENT | CODE/RESOURCE | TEST STEPS | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN` в `docs/implementation/burning-islands-205/BOSS_QA_REPORT.md`, реальный Gradle build/JAR source SHA, при запрете runtime Minecraft/GameTest/PolyMC/server = **NOT_RUN**. Boss, worldgen, loot, visual и audio не могут получить owner-accepted только по файлам/коммитам. Только владелец закрывает Issues.

**Важная сноска:** модель исполнителя, исходные PNG и аудио — три разных типа ассетов; присутствие картинок в ZIP не доказывает реализованные ModelParts или OGG. Предъявлять отдельную трассировку всех трёх.
