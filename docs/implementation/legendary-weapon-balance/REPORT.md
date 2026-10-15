# Issue #26 — наградное оружие I–V, версия1.9.7.6

База: `main@fb2994e958911169317523e1066b8a1ca7a155af`, подтверждена повторным ls-remote перед публикацией. Отдельная ветка `codex/legendary-weapon-balance`, worktree `D:/slavic-myths-balance`. Пользователь явно разрешил обновить версию и PolyMC. PR достижений #25 оставлен отдельным; пользовательские отчёты Classes3 в исходной папке не менялись. Refs #26 #7 #9 #10; #3–#6 изучены, уже включённые исправления не откатывались.

## Источники и числа

Все значения raw измерены настоящим ServerPlayer и потерей HP настоящей незащищённой цели Spider1000HP. Для обычного melee используется Player.attack; для посоха — production shoot + native collision handler, для косы — native custom damage/scheduler, для арбалета — native Arrow.onHitEntity. Это не таблица одних лишь конструкторов. Отдельно проверены реальные физические залпы.

| ID / этап | Проверенный путь получения | База до→после | Native HP до→после | speed / CD | Native DPS до→после за200т |
|---|---|---|---|---|---|
| ataman_flail /I | Атаман100: гарантированный ataman_sign — ингредиент; armorer recipe: diamond_flail + sign + chain + wrap | 12→12 | 12.00→12.00 | 0.7/с | 8.82→8.82 |
| nightingale_dagger /II | Соловей350: сам кинжал гарантирован entity loot table (при действующих loot rules) | 10→12 | 10.00→12.00 | 2.0/с | 20.00→24.00 |
| likho_staff /II | Лихо350: oko_likha — трофей-ингредиент; armorer: carved_staff + eye + shaft + 2 нитей доли | 10→13 | 10.00→13.00 | 1.2/с | 12.00→15.60 |
| tugarin_sword /II | Тугарин400: меч12% либо armorer:2 diamond_sword +3 tugarinova_kozha + wrap | 10→14 | 14.00→19.60 | 1.8/с | 23.80→33.32 |
| victor_spear /III | Курганы: печать князя (unresting_prince), фрагмент клинка (druzhinnik/сундуки) — ингредиенты; diamond_spear + seal + fragment + silver_fitting + wrap | 12→15 | 12.00→15.00 | 1.2/с | 14.40→18.00 |
| morozko_staff /IV | Морозко1400:15% при успешном испытании, FrostLedger.prepare/roll и персональная очередь; не обычный death loot | 7→16 | 7.00→16.00 | bolt16т; stream10т,60т/CD200 | 9.10→20.80 |
| karachun_scythe /IV | Карачун1600:15% default scytheChance при испытании, NightLedger.prepare/очередь; не обычный death loot | 12→20 | 12.00→20.00 | 1.0/с | 12.00→20.00 |
| trophy_kalin_sabre /IV | Калин1800: победа даёт mat_royal_iron через RewardLedger; shaped recipe:5 royal iron + diamond_sword + gold | 9→18 | 9.00→18.00 | 1.3/с | 11.70→23.40 |
| three_breath_crossbow /V | Горыныч2800:4 scales и seal по очищению/claim; serpent_crossbow и ancient_serpent_steel — сундук fortress; upgrade:base + scale + steel +3 разных дыхания | 14→16 | 14.00→16.00 | заряд35т, очередь0/4/8 | 24→27.43 fan;19.53→22.33 burst (модель, без эффектов) |
| poloz_whip /V | Полоз3000: сам предмет гарантирован GreatPoloz.die при doMobLoot=true; core/trophy/scales отдельно | 13→24 | 13.00→24.00 | 1.0/с | 13.00→24.00 |

TugarinPair всегда включён для этого предмета: таблица пользователя10→14 — база, а правомерный встроенный бонус×1.4 даёт фактические14→19.6 HP. Он сохранён. Native DPS — неподвижная контролируемая цель, полная зарядка с интервалом ceil(20/speed),200 настоящих серверных тиков, без классов/рун/чар. Начальный заряженный удар включён; это конечное10-секундное окно, а не бесконечный предел. Frost bolt в damage clock вызывает настоящий collision handler на достижимой цели, без времени полёта. DPS арбалета обозначен **аналитической оценкой**, не измерением полного цикла пользовательского ввода:42→48 прямого burst, charge35т по действующему use/left+10, для очереди ещё8т блокировки старта.

## Защита и TTK

Armor20/toughness8 и ResistanceII проверены отдельным native hit для всех10. Enchantment damage pipeline проверен на9 предметах (Frost bolt не использует оружейный enchant damage — сохранено прежнее поведение). Для арбалета Sharpness в fixture принудительный тест стороннего projectile modifier; это не утверждение, что Sharpness доступен арбалету в Survival. Настоящие tipped effects, explicit crit16–25 и vanilla crossbow8 в тех же условиях также проверены.

| ID | Raw HP после | HP через armor20/tough8 | Weak20HP TTK до→после,с | Armor100HP TTK после,с | HP-only3000 TTK до→после,с |
|---|---|---|---|---|---|
| ataman_flail | 12.00 | 5.071 | 1.45→1.45 | 27.55 | 361.05→361.05 |
| nightingale_dagger | 12.00 | 3.840 | 0.50→0.50 | 13.00 | 149.50→124.50 |
| likho_staff | 13.00 | 4.290 | 0.85→0.85 | 19.55 | 254.15→195.50 |
| tugarin_sword | 19.60 | 7.762 | 0.60→0.60 | 7.20 | 128.40→91.80 |
| victor_spear | 15.00 | 5.250 | 0.85→0.85 | 16.15 | 211.65→169.15 |
| morozko_staff | 16.00 | 5.760 | 1.60→0.80 | 13.60 | 342.40→149.60 |
| karachun_scythe | 20.00 | 8.000 | 1.00→0.00 | 12.00 | 249.00→149.00 |
| trophy_kalin_sabre | 18.00 | 6.840 | 1.60→0.80 | 11.20 | 266.40→132.80 |
| three_breath_crossbow | 16.00 | 5.760 | 0.00→0.00 | 8.75 | 124.25→108.50 |
| poloz_whip | 24.00 | 10.560 | 1.00→0.00 | 9.00 | 230.00→124.00 |

TTK здесь **расчёт по измеренному одному удару**, первая атака при t=0. Это ориентир HP/скорости без heal/AI/фаз, дополнительных эффектов, дистанции и не модель реального убийства босса. Для арбалета в TTK взят полный залп (48), без8т задержки и дополнительных эффектов. Не выдаётся за игровую Survival-приёмку. Отдельный настоящий multipart тест Полоза: во всех фазах1/2/3 удар плетью снимает24 HP; повтор того же игрока в тот же tick не снимает ещё24. Boss hit policy/armor/phase code не менялся.

## Умения, сохранённые ограничения

- Морозко: bolt7→16/CD16т; stream1→4 за прежний pulse раз в10т. Полный цикл60т даёт6×4=24; heartbeat grace20, CD200 и admission/release/menu/deadline прежние. G/V проверены и до, и после: волна4/CD320т, пять сосулек по5/CD400т, фактически25 на неподвижной широкой цели. Эти два AoE не усилены. Контроль/стужа и серверные damage types сохранены.
- Коса: база20; все3 комбо по20, один delayed hit на цель; G28, V22 с общим cast ledger — три клинка не дают66 одной цели. Wither/лечение/undead×1.25/тайминги/геометрия/двуручность не менялись. Старый NightConfig.baseDamage=12 сохранён как прежняя настройка ±от baseline12: default/старые12 теперь дают20, пользовательские отклонения сохраняются. Визуально перепроверка в клиенте не проводилась.
- Арбалет: скорость нормализуется **до** vanilla enchantments/crit/target defenses. Обычная специальная стрела2×1.18 даёт16, а не минимум с неизвестным потолком. Автоматический vanilla random crit при создании специального штатного залпа отключён ради заданных16; явно добавленный позднее crit проходит vanilla16–25, custom ammo base/enchant modifiers и tipped effects сохраняются. Обычный serpent_crossbow не менялся.
- Native12 сочетаний mode×fan/burst×main/offhand:3 настоящие стрелы, каждый прямой hit16, один ammo/3durability. Fixture только сводит веер к одной неподвижной hitbox, остальная коллизия настоящая. Мор/Пламя дали48 direct; Буря48 direct+3 separate, target cooldown/LOS/radius unchanged. Fire5/splash/burn40/VFX40 сохранены; fire entity impact перенесён после arrow hit, иначе первый прямой16 превращался в11 из-за preceding5 и native iframe subtraction. Нового bypass для огня нет; по уже раненой основной цели fire5 подчиняется прежним native i-frames, по другим целям splash остаётся5.
- Дубликат projectile, другой owner/volley и vanilla cooldown не переписаны. Нет нового true damage/глобального reset i-frames, setHealth-урона или tick polling. Рецепты/источники/шансы/registry IDs/редкости/модели/анимации/звуки/сетевые payloads/руны/классы/HP боссов сохранены.
- Плеть: основной24, прежний sweep13 по≤3 вторичным целям/CD20 и spikes10 с unique target/CD360 сохранены побайтно; массовая реальная AoE-сцена плети отдельно NOT_RUN. Кистень12 и разгон×1.35 сохранены; парный×1.4 Тугарина, второй удар копья, heal/морок Лиха и momentum сабли не переписаны.
- Общая политика RewardWeaponBalance питает атрибуты/два frost-числа/arrow base и числовые RU/EN tooltips. Генераторы и конечный текстовый overlay синхронизированы; overlay idempotence проверяется gate.
- ClassAttackBonus и поддерживаемая Strength проходят native event/attribute pipeline всех восьми melee. Сабля Калина уже раньше отсутствовала в rune_melee_weapons: тест фиксирует её прежнее отсутствие bonus, теги совместимости не расширены. Classes3 I–IV дополнительно прошли существующий37-case набор. Это не полная матрица каждой комбинации10оружий×всех классов/рун.
- PvP disabled проверен всеми10; creative/spectator/pet exclusion — отдельный native safety test. Тесты используют заглушку transport connection, а не подключённые клиенты.

## Проверки и воспроизведение

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
./gradlew.bat clean build
./gradlew.bat runBalanceChecks -PbalanceCheck -PwithoutJei
./gradlew.bat runClasses3Checks verifyRuneRework verifyRareRunes -I tools/classes3.init.gradle -PwithoutJei -PruneReworkOfflineCheck -PrareRuneOfflineCheck
python tools/verify_weapon_balance.py
./tools/install_weapon_balance.ps1
```

Для source CI: `python tools/verify_weapon_balance.py --source-only`; полный JAR gate требует Pillow и сохранённые логи. GitHub Actions запускает source contract. Для baseline выделен отдельный detached worktree исходного main; production src/main там не изменён, скопированы только build.gradle с opt-in run и fixtures. `-PbalancePhase=before` записывает BEFORE/DPS_CLOCK_BEFORE. Исходная папка пользователя не используется для тестовых запусков.

Финал: FINAL_BUILD.log clean build PASS; ACCEPTANCE_RUNTIME.log20/20 PASS; BASELINE_EXTENDED_RETRY.log18/18 (большинство after-only cases явно пропущены; реально измерены10 raw,9 DPS, волна/сосульки и существующие lifecycle/VFX/Storm проверки); REGRESSIONS.log37/37 Classes3,398 RuneRework и20491 RareRune offline PASS. Все production resources/compiled classes совпадают с JAR; тестовых классов в production нет.

Production: `build/libs/slavicmyths-1.9.7.6.jar`, **8491 gates**, 222 subscribers; SHA256 `7e0f9f634bbdbe733642fd0cd7c9ba2d468e0917e8211f817e1d9113c47cc210`.

Всего **16 реальных GameTest server boots**, **0 клиентов**, **0 integrated clients**, **0 запусков PolyMC**. Список по каждому логу хранится в RUNS.json. Было несколько ранних FAIL: baseline fixture использовал tick вместо doTick; обнаружены и исправлены округление17 и preceding flame5; затем исправлены fixtures (слишком низкий6-блочный потолок для сосулек, natural despawn при удалении>128 от единственного fake player, poison immunity Spider, RuneRuntime.refresh и старый unsupported sabre tag). Все ранние логи сохранены, финальные отказы отсутствуют.

## Ограничения и приёмка

Client first/third person, две руки в реальном GUI, визуальная длительность/звук, dedicated с2 подключёнными игроками, реальная задержка heartbeat, полный Survival reward route, PvP enabled/co-op бой, реальный player relog, полный AoE босса и MSPT/FPS **NOT_RUN**. Работающий headless NeoForge server не называется integrated/connected gameplay. Источники наград прослежены в runtime/loot/recipes, но не пройдены все сюжетные бои в Survival. Материалы не превращены в новые оружейные награды. Старые миры не открывались и не конвертировались. Сохранение item ID/обычных компонентов проверено native stack roundtrip, не названо реальным перезаходом.

Краткий оставшийся игровой checklist:4 действия Морозко в Survival; коса combo/G/V first/third-person/idle; оба арбалета со стеной/особыми стрелами и2 игроками; плеть/AoE по боссу и союзникам; получение наград и relog на копии мира. Issue26 оставить открытым до игровой приёмки владельца; PR не merge-ить автоматически.

Установка PolyMC документируется отдельно в POLYMC_INSTALLATION.json / INSTALLATION.log: backup предыдущего JAR, SHA256, сохранение остальных файлов и версия профиля. Старый1.16.5 и игровые сохранения не изменяются.

Установка **PASS**:1.9.7.6 в PolyMC1.21.1 Testing; SHA256 совпал с production gates,609 остальных файлов сохранены побайтно; backup прежнего JAR и instance.cfg проверен. Изменены только JAR и версия/суффикс имени профиля. Установленный клиент не запускался.
