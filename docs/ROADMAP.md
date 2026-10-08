## Редкие руны — 1.3.4.3 (2026-10-08)

Реализованы шесть rare runes, четыре именных fragment, четыре восстановления через существующую Rune Anvil и два stage-3 обмена Яги. Источники: Соловей (3 гарантированных flight fragments, одноразовый бой на стан), Тугарин/Лихо/курганный воевода (1 гарантированный fragment); 25% extra, 4% full rune. Старая rune_protection сохранена, новая — rune_rare_protection; прежние IDs/loot pools/runes I–II/Classes Screen и HUD layout сохранены.

RareRuneRules/Runtime/Network: server authority, persisted player cooldown по overworld clock, одна общая R-кнопка (Shift — выбор), временный полёт и safe cleanup, execute immunity/tag и exact health-cost marker, Berserker/Sacrifice buffs. RuneVisual — одна noSave/non-AI entity с tracked item/count/phase; own client renderer, shields/crack/fade и лёгкий configurable edge overlay. Архитектура/параметры/файлы: [отчёт](RARE_RUNES_1.3.4.3.md).

Clean production build PASS. Offline rare20491, ordinary355, foundation75064, armor147; production resource/preservation audit PASS, сохранены 4931 resources. Итоговый JAR build/libs/slavicmyths-1.3.4.3.jar; [машинная проверка](verification/rare-runes-1.3.4.3.json). Реальных клиентов/серверов 0/0 по запрету пользователя: игровой lifecycle, flight/render/PvP/JEI/dedicated smoke остаются непроверенными. MSPT не измерен. PolyMC остаётся 1.3.3; полная переделка Classes GUI по-прежнему ждёт утверждения. Future frost/clone/III upgrades/new bosses не добавлены; commit не создан. Записи ниже исторические.

## Руны I–II — 1.3.4.2

Восемь рун I–II реализованы: existing IDs heat/wind переиспользованы, шесть новых; RuneBalance централизует diminishing returns/caps, RuneRuntime применяет серверные эффекты и transient modifiers по событиям. Старые особенности heat/wind сохраняет persistent/network legacy_rune_specials; existing RUNES и registry IDs сохранены. RuneCraftRecipe проверяет base component, GUI/JEI используют counted inputs. Кровотечение: 0,5 HP, rune max2/native max3, совместный max3 по ответу пользователя. Силуэты физических камней обновлены.

Clean build PASS; offline 355 + armor147 + foundation75064; resource gates22838, preserved4898. Production build/libs/slavicmyths-1.3.4.2.jar. Реальных запусков клиент/сервер 0/0: игровая приёмка открыта, запрет пользователя сохраняется. Classes GUI ждёт утверждения, PolyMC остаётся 1.3.3. Rune III и следующие milestones не реализованы. [Полный отчёт](RUNES_REWORK_1.3.4.2.md).

## 1.3.4.1 — текущая итерация Rune Foundation

Готовы создание пыли/душ/пустых рун I–III, сохранение tier/base, bounded кurgan integration, compact Rune Anvil и JEI. Production build и офлайн gates PASS; подробности [в отчёте](RUNE_FOUNDATION_1.3.4.1.md). Runtime-приёмка крафта/GUI/JEI/дропа/смерти/перезахода остаётся открытой: запуск клиента/сервера запрещён пользователем. Это не реализация новых эффектов Rune System 2.0: rune effects, double-edged/boss-runes/class synergies и прочие будущие milestones не добавлены. Classes GUI отдельно ожидает утверждения макетов. PolyMC не обновлялся.

## Classes 2.0 — исправление, этап макетов на утверждение (2026-10-08)

По новому пользовательскому ТЗ выполнена диагностика затронутого UI и подготовлены пять PNG-макетов, дополнительный вид направлений без модалки и 40 resolution/state snapshots. 1344 проверки предлагаемой геометрии/bitmap glyphs PASS; это не проверки рабочей реализации. Проверены реальные prerequisites всех 34 навыков; KeyMapping snapshot — ЛКМ/H/J из options.txt, файл не изменён. Макеты и причины дефектов: [проект на утверждение](CLASSES_UI_REDESIGN_APPROVAL.md).

Ожидается утверждение согласно §18.2: «До моего утверждения не приступать к полной переделке GUI». Production GUI, ресурсы/механики, JAR и PolyMC на этом этапе не менялись; установленная версия остаётся 1.3.3. Клиент/сервер не запускались, сборка исправления ещё не выполнялась. Предлагаемый HUD18/right8/bottom8 требует утверждения: найденный в backup1.3.1 старый HUD24/Y=H−74 расходится с новыми координатами ТЗ. История Git для untracked ClassClient отсутствует. Будущая реализация, тесты её ввода/камеры и установка ещё не выполнены.

## Установка 1.3.3 в PolyMC — 2026-10-08

По прямому запросу пользователя production 1.3.3 установлен в существующий `Slavic-Myths-1.21.1-Testing` (Minecraft 1.21.1 / NeoForge 21.1.255). Включены парные мечи Тугарина и остальное редкое оружие 1.3.2.2, Classes UI и рунные слоты брони. Предыдущая 1.3.2.3 сохранена вне mods, активен один JAR, SHA-256 установленной копии совпал; 542 остальных файла экземпляра (включая миры/настройки) побайтно сохранены. Клиент и сервер не запускались. [Квитанция](verification/polymc-1.3.3-installation.json). Записи ниже об отложенной установке исторические.

## 1.3.3 — Рунные слоты брони (2026-10-08)

Существующая Runes.capacity расширена на player ArmorItem: leather/семимильные сапоги 0; chainmail/gold/iron/turtle 1; diamond/netherite 2; perunite 3; gambeson/plate 1. Одежда без ArmorItem, аксессуары и animal BODY исключены. Armor max строго ограничен материалом; grandfathering прежних оружейных ячеек сохранён. Наковальня использует прежние открытие ячеек/установку/удаление, общий лимит и проверку дубликатов. Tooltip/GUI показывают руны, открытые ячейки и отсутствие эффектов на броне. Боевые характеристики и эффекты не добавлены.

`gradlew.bat clean build` PASS; 147 офлайн-проверок capacity/admission/RuneState JSON/NBT/packet, 10 542 проверки ресурсов/JAR PASS. По сравнению с production 1.3.2.3 изменены только пять классов Runes/RuneDefinition/RpgEvents/RpgScreen/RpgClient; 4 862 прежних ресурса побайтно сохранены, RU/EN добавлены только две строки. Новых registry ID нет. ItemState/RuneRepair, материалы/предметы/рецепты/текстуры/классы/HUD не менялись.

По прямому запрету пользователя в этой итерации 0 клиентов и 0 серверов. Полный native ItemStack harness остановился на offline bootstrap: FeatureFlagLoader требует LoadingModList; обходов в production нет. JSON/NBT roundtrip компонента не считается игровым relog. Смерть/respawn/relog/restart/dimension/drop-pickup, реальная починка/отделка и GUI не проверены в игре. Копирование компонентов vanilla-процессами сверено по установленным зависимостям, без заявления runtime PASS.

Production `build/libs/slavicmyths-1.3.3.jar`; в PolyMC в рамках этого задания не устанавливался, там остаётся 1.3.2.3. Пользовательские изменения/миры сохранены; commit не выполнялся. [Таблица, файлы и приёмка](ARMOR_RUNE_SLOTS_1.3.3.md), [gates](verification/armor-runes-1.3.3.json). Предыдущие записи ниже исторические.

Руны 2.0 и новые эффекты брони не реализуются в этом патче. Следующий шаг по 1.3.3 — разрешённая пользователем ручная игровая приёмка.

## 1.3.2.3 — Classes 2.0: интерфейс (2026-10-08)

Переработаны существующие классы: дерево по фактическим prerequisites, крупный корень, состояния узлов и ранги, правая карточка с текущими/следующими параметрами и точными требованиями, компактные 3 active + 1 passive. Выбор класса: временный 3D-preview собственного игрока, три карточки с ролью, подробности и подтверждение. Нативные 32px иконки, 64px гербы, деревянные GUI sprites; blur отключён. Управление использует прежние Minecraft KeyMapping и сохраняет options.txt. HUD справа снизу; единственный действующий переключатель — Mods → Slavic Myths → настройки, клиентский config по умолчанию включён. Creative вручную изучает навыки своего класса без требований и расхода очков; пределы рангов сохранены, Survival проверяет обычные требования.

`gradlew.bat clean build` PASS (14 предупреждений deprecated/unchecked), 3 710 офлайн-проверок правил/геометрии и 530 проверок ресурсов/JAR PASS. Проверены границы для 1920×1080/2560×1440, GUI scale 2/3/4; это не проверка игровой отрисовки. В этой итерации 0 запусков клиента и 0 сервера, реальные relog/смерть/механики и FPS/MSPT не проверялись. Существующие сохранение Class2, lifecycle events, баланс, названия, ветви и registry IDs сохранены.

Production `build/libs/slavicmyths-1.3.2.3.jar` установлен в существующий PolyMC `Slavic-Myths-1.21.1-Testing`. Предыдущая 1.3.2.1 сохранена вне mods, установлен один JAR, SHA-256 совпал; 539 остальных файлов экземпляра, включая миры и настройки, побайтно сохранены. Старый экземпляр 1.16.5 не затронут. Commit/push не выполнялись. [Детали и ручная приёмка](CLASSES_UI_1.3.2.3.md), [gates](verification/classes-ui-1.3.2.3.json), [установка](verification/polymc-1.3.2.3-installation.json). Более старые записи об установленной версии ниже исторические.

Следующий шаг по этой системе — ручная игровая приёмка UI и сохранения; будущие milestone не реализуются.

## 1.3.2.2 — Редкое оружие (2026-10-08)

Реализованы парные мечи Тугарина, посох Лихо, кистень атамана и копьё победителя: разные силуэты, серверные механики, максимальная ёмкость 3 руны, 4 рецепта оружейного верстака, RU/EN. Существующие дропы Тугарина/Лихо и курганные трофеи переиспользованы; добавлен только гарантированный знак атамана дополнительным pool. Два меча крафтятся одновременно и сохраняют данные каждого донора отдельно; второй остаётся в ячейке верстака. Броня/новые руны/боссы не добавлены.

`gradlew.bat clean build` PASS; 15,935 ресурсных/JAR-проверок PASS, 4,618 прежних ресурсов побайтно сохранены, остальные изменения старых ресурсов только добавочные. Финальные headless suites: новые 16/16 + прежнее оружие 16/16. 11 реальных серверных запусков, ноль клиентов; промежуточные failures сохранены в отчёте. Проверены нативные крафт/ремонт/подбор, смерть, два dimension transitions, реальные boss drops и player .dat новым серверным процессом (4 оружия + 2 КД). Это не реальный клиентский relog; внешняя ручная приёмка остаётся открытой, FPS/MSPT не измерялись.

[Параметры, рецепты, файлы и краткая ручная приёмка](RARE_WEAPONS_1.3.2.2.md), [acceptance](verification/rare-weapons-1.3.2.2-acceptance.json), [ресурсы/JAR](verification/rare-weapons-1.3.2.2-resources.json). Production: `build/libs/slavicmyths-1.3.2.2.jar`. PolyMC остаётся на установленной 1.3.2.1; клиент/установка не запускались. Пользовательские изменения и игровые миры сохранены, commit/push не выполнялись.

Следующий шаг — ручная приёмка текущего оружия; будущие milestone автоматически не реализуются.

## Установка 1.3.2.1 в PolyMC — 2026-10-08

По запросу пользователя проверенный JAR установлен в существующий `Slavic-Myths-1.21.1-Testing` (Minecraft 1.21.1 / NeoForge 21.1.255). Предыдущая 1.3.1 сохранена вне mods; SHA-256 установленной копии совпал. Активен один JAR мода, 439 сопутствующих файлов (включая миры и настройки) сохранены побайтно. Клиент не запускался. [Квитанция установки](verification/polymc-1.3.2.1-installation.json). Записи об отложенной установке ниже исторические.

## 1.3.2.1 — Оружие 2.0 (2026-10-08)

Реализованы 30 форм оружия: копья, сулицы, кистени и метательные ножи семи материалов, короткий и тяжёлый луки. 53 новых предмета включают детали; три прежних оружейных ID сохранены. 36 рецептов оружейного верстака и 23 рецепта деталей; просмотр рецептов работает без JEI. Разные силуэты, ru_ru/en_us, серверные броски/подбор, сохранение данных и совместимые ремонты. Централизованы ёмкость рун и категории существующего оружия; ранее открытые слоты сохранены.

Финальный `gradlew.bat clean build` PASS; 15 067 проверок ресурсов/JAR PASS, 2 566 прежних ресурсов сохранены побайтно. Итоговые headless suites: оружие 16/16, классы 15/15. 11 реальных серверных запусков, ноль клиентов. 30 оружейных стеков проверены после загрузки player .dat новым процессом; проверены смерть и два нативных перехода между измерениями. 512 столкновений ножа: 117 потерь при заданной вероятности 25%.

[Реализация, характеристики и рецепты](WEAPONS_1.3.2.1.md), [отчёт и ограничения](verification/weapons-1.3.2.1-acceptance.json), [ручная приёмка](MANUAL_QA_WEAPONS_1.3.2.1.md). Клиентский вид, GUI, бой нескольких игроков и реальный выход/перезаход остаются ручными; FPS/MSPT не измерялись. PolyMC остаётся на проверенной 1.3.1; установка новой версии, commit и push не выполнялись.

Объём текущего архива завершён; броня, руны 2.0, мана, боги и измерения не добавлялись. Следующий шаг — ручная приёмка текущей версии. Прежние записи ниже относятся к своим версиям.

## 1.3.1 — Классы 2.0 (2026-10-08)

Проверенный JAR установлен в существующий тестовый PolyMC-профиль 1.21.1, предыдущий сохранён. [Квитанция](verification/polymc-1.3.1-installation.json). Следующая ручная приёмка выполняется в отдельном тестовом мире; клиент автоматически не запускался.

Реализованы дружинник, ведун и разбойник, 18 ветвей, 28 основных навыков и шесть эволюций. Отдельные class XP/очки, серверные требования, три активных слота G/H/J и один пассивный, триггеры, экран K/камня пути, HUD и 34 разные pixel icons с ru_ru/en_us. Миграция прежних путей сохраняет данные/КД; нативные смерть, /kill и загрузка player .dat новым процессом проверены. API/debug `/smclass`; массовые источники class XP не добавлены.

Clean build PASS; ресурсы/воспроизводимость/JAR PASS. Итоговые серверные suites 15 + 53 + 8 + 6 = 82/82; 25 реальных headless запусков этой итерации, ноль клиентов. Промежуточные ошибки сохранены в [машинном отчёте](verification/classes-1.3.1-acceptance.json). Пользовательские изменения городских построек сохранены. Commit/push не выполнялись; игровые миры не перезаписывались.

[Реализация и ограничения](CLASSES_1.3.1.md). Ручная приёмка GUI scales/F1/keybindings, двух клиентов, боя и реального выхода в меню/перезахода остаётся открытой; FPS/MSPT не измерялись. [Краткий список](MANUAL_QA_CLASSES_1.3.1.md).

Объём Class 2.0 завершён в рамках задания. Оружие/броня 1.3.2, руны 2.0, мана, боги и новые измерения не реализованы. Следующий шаг — ручная приёмка текущей версии, а не автоматическое выполнение будущих milestone.

## 1.3.1 — Архитектурная корректировка по игровой приёмке (2026-10-08)

Шесть конкретных замечаний по скриншотам реализованы и прошли gates: башни / стены, содержательные интерьеры / конечные припасы, комнатное / уличное освещение, удобные нижние площадки лестниц и чистые стеклянные либо деревянные окна. Будущие gameplay milestones не добавлены. Осталась ручная клиентская оценка новых построек, вида вещей / ночного света и ходьбы игрока.

[Изменения и доказательства](CITY_POLISH_1.3.1.md), [SHA / runtime receipt](verification/city-polish-1.3.1.json). Clean build / три production resource gate PASS; 8 + 6 + 53 = 67/67 финальных серверных проверок. Одиннадцать реальных headless запусков / ноль клиентов; ранние failures документированы. Установленный PolyMC JAR не заменялся автоматически, commit не создавался.

## Установка 1.2.5 в PolyMC — 2026-10-08

По прямому запросу пользователя проверенный production JAR 1.2.5 установлен в `Slavic-Myths-1.21.1-Testing` (Minecraft 1.21.1 / NeoForge 21.1.255). Предыдущая 1.2.4 сохранена вне mods; SHA-256 копии совпал, активен один JAR мода, восемь сопутствующих файлов / настроек не изменились. Клиент не запускался. [Квитанция](verification/polymc-1.2.5-installation.json). Прежние записи об отложенной установке ниже исторические.

## 1.2.5 — Подготовка публикации (2026-10-08)

Локальные maintenance gates завершены: audits, резервное перемещение QA worlds, Git ignore/index/gc, pinned Xaero dependency, loaded-before-POI check, README/media/CHANGELOG/release notes. Build, independent fresh-cache source build, финальные 65/65 headless checks и реальные restart assertions PASS; промежуточные failures сохранены. [Отчёт](maintenance/RELEASE_PREPARATION_1.2.5.md).

Осталось прямое разрешение на commit/push main и release/1.2.5-distribution, GitHub default при доступных правах, remote release clean-clone build. Manual pending: client models/GUI, natural city целиком на обычном рельефе, bosses/player relog, MSPT/FPS. Новые gameplay milestones не реализовывались. 8 новых headless запусков / 0 клиентов.

## 1.2.5 — Реализованный объём и оставшаяся ручная приёмка

Пакет городища и прямые исправления пользователя реализованы: два master layout с улицами / красной линией, три климата / 240 NBT, городской жилой / профессиональный каталог, терем / торговый дом / площадь, каменная оборона, переиспользованные хозяйственные здания / казарма, native worldgen и надёжная полная showcase-команда. Мешок 5×64, формы заполнения, объёмные напитки, опоры домов и крыша исправлены. Будущие княжеский AI, экономика и религиозные системы не добавлены. [Отчёт 1.2.5](GORODISHCHE_1.2.5.md).

Финальные gates PASS: clean build, ресурсы / production JAR, 65/65 серверных проверок; 19 серверных запусков / 0 клиентов. Осталась отдельная ручная клиентская проверка моделей / GUI, полный natural city на обычном рельефе и реальный перезаход. Установка PolyMC в этой итерации не выполнялась; 1.2.4 остаётся установленной. Нет автоматического commit.

## 1.2.4 — Village building iteration

Update 2026-10-07: PolyMC installation is now complete after explicit user authorization; [receipt](verification/polymc-1.2.4-installation.json). Client/manual acceptance remains pending; no additional build or game run was required for copying the unchanged verified artifact.

Implemented the supplied building package: residential/profession/utility/field/center/barracks templates, three climate designs, five native village families plus zombie pools, physical POIs/beds, themed lazy loot and deterministic full showcase/individual placement. Final headless gates 4/4 new and 53/53 regression PASS, clean build and production resources checked. See [scope and evidence](VILLAGE_BUILDINGS_1.2.4.md). Remaining: user-authorized client visual/interaction and varied-terrain acceptance. PolyMC installation is deferred until explicitly requested. No future religion, settlement expansion or new materials/items were scaffolded.

## 1.2.3 — Bandit aggression / golem defense hotfix

See [fix and verification](MILITARY_1.2.3_BANDIT_HOTFIX.md). Ordinary bandits target residents/golems; melee routing no longer stops outside attack reach. Stronghold player alarms remain intact. Golem bandit detection uses 32 blocks without changing other native targets. Clean build, production/resources and final 53/53 server checks PASS; corrected JAR installed in PolyMC with backup. Zero client launches, no commit/push.

## 1.2.3 — Military and settlement defense

See [implementation and verification](MILITARY_1.2.3.md). Native Villager guard profession, persistent military board/quests, bounded camp raids and modular rare barracks are implemented atop 1.2.1/1.2.2. Clean build, production/resource validation, geometry and final 49/49 isolated server tests PASS; real save/load assertions PASS. Installed 1.2.3 in the PolyMC testing instance with backup and hash validation; zero client launches. Manual visual acceptance is pending. Calm neutral spirits are excluded until aggression, by explicit user decision. No automatic commit/push.

## 1.2.2 — Current settlement production iteration

See [implementation and verification](WORK_AI_1.2.2.md). Native WORK behavior, a 64-slot settlement chest and existing production engines implement this iteration. Weaver remains flax-only by explicit user decision. No future settlement systems were scaffolded. Clean build, resource/production gates and final 30/30 isolated server tests PASS; separate real save/load phases PASS. Installed 1.2.2 in the PolyMC testing instance; zero client launches. No commit/push.

## 1.2.x People and settlements — 1.2.1 implemented; manual playtest pending

1.2.1 now provides vanilla villager visual bundles, climate outfits, 13 preserved vanilla professions, six additional native POI professions and limited random trades with vanilla XP/restock/persistence. Idler, Merchant, zombie profession outfits and vanilla children are covered. Builds, resource/production gates, seven isolated server tests and pure/headless geometry checks passed. PolyMC testing instance has 1.2.1; client was not launched.

Acceptance still needs the short [manual matrix](VILLAGERS_1.2.1.md#manual-acceptance). Future settlements, villages, elders, guards, workday production AI, families, relationships and quests are not part of this iteration and were not scaffolded. No gender/age mechanics were added. All earlier 1.1.x changes remain.

## 1.1.8 Integration Polish — build / production / PolyMC gates PASS

Implemented grouped Kitchen II screen and continuous supported shelf, single headscarf render path, rebuilt carved/storm staff and open-crescent sickle, native 16px berries/bushes, raised Perunite equipment meshes, common numeric rune definitions/tooltips and incompatibility, crate nine-slot GUI, C descent and smooth -0.32 descent for both vessels, rare individual animal ambient calls. Ambient source events: goose/duck use minecraft:entity.chicken.ambient at existing 0.75/1.10 sound pitch; domestic goat uses minecraft:entity.sheep.ambient at 0.9. Defensive hiss uses minecraft:entity.cat.hiss only from GooseDefence.

Apple uses one taller fixed crown with the old Y=4 saved-world anchor; pine is 9–12 blocks with separated tiers; four pine saplings select a validated 2x2 giant of 16–20 blocks, never natural giant spawning. Sapling preflight now respects world height and loaded chunks. Existing berries/hops bonemeal increments remain intact.

Five definitive recipes repaired. Shared kurgan candidates use 41:4:1 weights, approximating old relative density (1/20² : 1/64² : 1/128²); legacy warrior/great sets are preserved with frequency 0. Common placement 20/15 → 50/30; retained legacy placements 64/55 → 160/110 and 128/119 → 320/238. Large bandit camp 128/119 → 320/238, salt retained. Apple rarity 18 → 6. These are configured values, not measured world frequencies.

165 acquisition IDs reviewed, missing routes 0; RU/EN missing keys 0 and remaining mojibake 0. See LOCALIZATION_AUDIT_1.1.8, OBTAINABILITY_AUDIT_1.1.8 and FUNCTIONAL_AUDIT_1.1.8. `gradlew.bat clean build`: first attempt failed on Component.withStyle; permitted retry PASS in 33s, 12 deprecation warnings; Gradle test task NO-SOURCE. Actual pure Java assertions: 300 TreeShape + 49 BrewRules. Production/resource gates PASS (2692 JSON, 5894 asset links); duplicate Vat screen bytecode regression PASS; tests excluded from JAR. Source rune numerical equivalence PASS. Versioned JAR: build/libs/slavicmyths-1.1.8.jar, 7192357 bytes, SHA256 b108d74baf242c71136a63670bb9da90db40d7c029b025475e4e9091d107c4fe. Installed only this mod into Slavic-Myths-1.21.1-Testing; backed up/replaced 1.1.7; six companion files and two instance configurations have identical hashes. Receipt: docs/verification/polymc-1.1.8-installation.json. Branch main, no release branch/tag. Automated Minecraft/PolyMC/server launches: 0; visual, gameplay, rejoin and natural frequency acceptance remain manual.

## 1.1.7 — Startup screen hotfix (2026-10-07)

- [x] Устранена двойная регистрация slavicmyths:vat из crash log пользователя.
- [x] Regression source/production bytecode gate; clean build и9701 resource/production checks +49 Java assertions PASS.
- [x] Исправленный JAR установлен в PolyMC, SHA совпадает, остальные моды/настройки сохранены.
- [ ] Повторный запуск исправленного клиента и открытие чана пользователем; автоматически Minecraft не запускался.

## 1.1.7 — Пивоварение и брожение (2026-10-07)

- [x] После завершения1.1.6 реализованы hops/tall stages/sickle/fertilizer/grass cuttings.
- [x] Malt/infusions/press3 actions/ingredients/containers/native assets.
- [x] Data-driven vat15recipes,9slots/labels/progress/mode, existing magical integrations.
- [x] Existing barrel compatible storage +8servings loaded-tick fermentation/quality/spoilage, active-only ticker.
- [x] Eight beers +kvass/mead/cider/mors/juice, container/pitcher/keg transfers, effects and persistent lazy overload/death reset.
- [x] Tags/ru/en/tabs/two advancements; static resource gates and49 actual pure-Java arithmetic assertions PASS.
- [x] Final clean build PASS29s и production9698checks/49Java assertions; дополнительные пересборки после отдельного разрешения пользователя.
- [x] После commit/push установлен1.1.7 в существующий PolyMC profile; SHA/6 companion mods/config hashes проверены; предыдущий JAR сохранён вне mods.
- [ ] Ручная игровая/визуальная/save-rejoin приёмка пользователя в обновлённом PolyMC; автоматических игровых запусков0.
- JEI additional categories skipped; existing optional integration retained. Не начинать будущие напитки/дистилляцию/milestones.

## 1.1.6 — Хранение и хозяйственный быт (2026-10-07)

- [x] Перед началом1.1.6 установлен проверенный1.1.5 в существующий PolyMC по команде пользователя, без запуска.
- [x] Sack/baskets/crate, chest27, storage-only barrel9 и реальные ограничения/GUI/hoppers.
- [x] Wall shelf3/standing shelf6, реальные ItemStacks видны, hit selection/max16/save/client sync.
- [x] Drying2x2/4 independent slots/2400ticks/products, distinct sheaves/reversecraft, haystack1..4 exact hay return.
- [x] Native assets/data/tags/ru/en/Creative/winter_stores, static/reproducibility validation PASS7575.
- [x] Финальный clean build PASS29s после одного исправления собственной generic-map ошибки; production validation10881checks.
- [ ] Ручная gameplay/visual/save-load приёмка; Minecraft launches0.1.1.6 в PolyMC не переносится; далее задача1.1.7.

## 1.1.5 — Кухня II (2026-10-07)

- [x] Старые обычные столы без редизайна: PAIR максимум2, 4 food slots/блок, сохранение/синхронизация/подача/еда/3D.
- [x] Existing kitchen_table LEFT/RIGHT с одним master, точный menu, non-consumed rolling pin/pot, безопасные containers/servings/breaking.
- [x] Flour/groats fallback (mill отсутствует), dough, 22 data-driven kitchen recipes, точные food values без effects.
- [x] 16px native assets, dedicated table food models, ru/en/tags/Creative/generous_table; optional JEI category обновлена.
- [x] Static resource/geometry checks PASS, 7205 checks.
- [x] Финальный clean build PASS, production validation 10406 checks; всего 3 build команды (одна сверх лимита вследствие собственной UTF-8 ошибки служебного скрипта).
- [ ] Ручная gameplay/visual/save-load приёмка: запусков Minecraft 0; PolyMC не изменён.

## 1.1.4 — Текстиль и переработка (2026-10-07)

- [x] Нож и семь вариантов carcass, player kill filtering, точные enhanced/fallback drops, сохранение срока/огня, fat/hide/candle.
- [x] Три станка без GUI и полная льняная цепочка; старые IDs сохранены, crafting bypass удалён.
- [x] Семь отдельных силуэтов одежды, внутренний BELT attachment и синхронизация, льняная кровать/точный well_rested.
- [x] Native assets, data/ru/en/Creative и linen_craft; статический валидатор ресурсов.
- [x] Финальный clean build PASS (один повтор после собственной renderer type ошибки), 8665 static/resource/production checks.
- [ ] Ручная visual/gameplay/save-load приёмка; Minecraft launches 0, PolyMC не изменяется.

## 1.1.3 — Животноводство (2026-10-07)

- [x] Три отдельных вида и baby appearance, только три новых spawn eggs, breeding/плавание/ограниченная защита гуся.
- [x] Корма новых и vanilla животных, feeder без auto breeding, mixed-egg nest и таймер яйцекладки/fallback.
- [x] Козье молоко, мясо/loot/cooking, biome spawns, отдельные models/textures, Creative tabs/ru/en/advancement.
- [x] Разрешённые vanilla audio fallback для 10 новых событий; custom OGG пока отсутствуют.
- [x] Финальный clean build / production JAR validation (один разрешённый повтор после собственной compile ошибки; 8063 static checks).
- [ ] Ручная visual/gameplay/save-load приёмка пользователем; Minecraft launches 0. PolyMC до отдельной команды не изменяется.

## 1.1.2 — локальная реализация завершена (2026-10-07)

- [x] Пять ягод, отдельные saplings, reusable perennial phases, two-block raspberry, fertilizer integration, scoped natural patches.
- [x] Fixed apple tree + fruit cycle + bounded anchor regrowth; wood family, обычные/подвесные таблички, ресурсы/теги/рецепты.
- [x] Девять стандартных Creative tabs, распределение старых предметов и установленный порядок нового контента.
- [x] Build/production resource validation, version 1.1.2. Git main commit/push разрешены пользователем; нет release/tag.
- [ ] Ручная visual/game acceptance пользователем. Minecraft не запускался. PolyMC остаётся 1.1.0 до отдельной будущей команды.

## RC2 UI / Visual correction — 2026-10-06

## 1.1.0 Земледелие — локальная реализация завершена (2026-10-07)

- [x] Семь farmland crops, семь стадий, разные силуэты; семена/продукты/4 хозяйственных инструмента.
- [x] Возрастной loot, capped Fortune, grass seed injection, data component лейки, ru/en/recipes/tags/advancement/Creative Tab.
- [x] Один clean compile/build, финальная упаковка JSON, статическая проверка и production JAR; установка в тестовый PolyMC.
- [ ] Ручная игровая/визуальная приёмка пользователем. Minecraft не запускался.
- [ ] Commit/push `main` — не выполнен автоматически в соответствии с AGENTS.md.
- Мельница, мука, переработка льна, дополнительные GUI/культуры/механики в этой итерации не добавлены. Существующий RC2 сохранён.


Внесён пакет UI/Visual поверх текущего main/RC2: два входа наковальни и copy-only preview, три режима/автоматический расход доп. материалов, Creative XP=0, новый Path Stone/Kitchen UI, исправлен повторный blur книги, реальные оси щита, пересобраны Club/Battle Axe UV0..16, четыре разных древесных материала и двери/люки/signs. Registry ID, рецепты, баланс survival, курганный hotfix сохранены. Сброс пути и новые пары конфликтов рун не выдуманы. Разбойники/Соловей отложены.

PASS: clean build; 10/10 loader GameTests с companions; шесть CPU/data/codec/geometry gates; production resources 0 missing refs. Реальных headless запусков в этом этапе: 2 (первый 8/10 с ограничением synthetic connection/DataSlot, финальный 10/10). Клиент: 0. GUI scales 2/3/4, игровые held poses и реальный reconnect — MANUAL PENDING. Результат не объявлен визуально принятым. Версия остаётся 0.9.10-rc2; опубликованный tag/prerelease не перезаписывается. Исправленный JAR установлен в существующий PolyMC 1.21.1 RC2 после gates; прежний сохранён вне mods, остальные моды/config/saves без изменений. SHA256 `ca32e6ac1b8a67a24cd5efadb051fbc0079e525bdb5b9642b1b0552f228a571e`. Commit/push не выполнялись по текущему AGENTS.md. Отчёт и список файлов: `docs/verification/rc2-ui-visual/REPORT.md` и `changed-files.txt`.

## 0.9.10 RC2 — server acceptance и установка (2026-10-06)

Завершены оставшиеся серверные проверки: 9/9 workstation+hotfix GameTests и 27/27 регрессий, включая подход всех трёх курганов без повреждения кладки. Щит реально блокирует спереди, изнашивается, получает axe cooldown; repair/Unbreaking/Mending/NBT проходят. Камень пути: RMB/menu, offering/XP, навыки, navigation SavedData, range guard, NBT. Наковальня: forge/install/remove, расходы, validation, shift-click, close-return и components. Кухня: 12/12 рецептов, guards, consumption, tool wear, remainder, output shift-click и NBT.

Исправлены все 16 старых door-model parents у darkened/pine/rowan/willow; native blockstates включают все facing/half/hinge/open варианты, старые имена моделей сохранены как aliases. В кухне отображаются food effects или их отсутствие, подробности в tooltip; gameplay блюд не изменён. Воспроизведение door resources: `python tools/migrate_remaining_doors_1211.py` после исторических resource generators.

`clean build` + шесть CPU/data/codec checks PASS. Production resources: 4042 references, 0 missing asset issues; static data 2685/0 errors. JAR SHA256: `faa54bd5decf79dc029b1d722f1995b195aa313dd5cea900c16bd7a70ea5a499`. Установлен в существующий PolyMC Slavic-Myths-1.21.1-Testing; прежний dev JAR сохранён вне mods, 6 прочих модов и configs/saves не изменены. Client 0 по выбору пользователя; эта итерация имела 3 реальных headless server запуска (первый исправлял test fixture, затем 9/9 и 27/27). Реальный multiplayer/перезаход, визуал и GUI scales — MANUAL PENDING. Отчёт/receipt: `docs/verification/playtest-0.9.10-rc2/`. Сборка/docs/checksum: `release/0.9.10-rc2/`.

Исходники опубликованы в `main`, она установлена основной веткой GitHub; история существующей `master` сохранена. Player branch `release/0.9.10-distribution` обновлена до RC2. Prerelease: https://github.com/Deserag/slavic-myths/releases/tag/v0.9.10-rc2 — JAR/ZIP/MRPACK скачаны обратно и проверены по SHA256. RC1 не перезаписывается, новый tag `v0.9.10-rc2`. Нового overhaul разбойников/Соловья, Equipment & Art или rune system 0.9.11 нет. Полная ordinary-world natural coverage и measured MSPT не заявлены принятыми.

## RC1 — отдельная сборка для игроков и уточнение приёмки (2026-10-06)

Опубликована отдельная distribution branch `release/0.9.10-distribution` с 9 файлами: playable JAR в mods, installer закреплённых зависимостей, lock/checksum и player docs. Исходников/Gradle/миров/configs нет. Source branch `release/0.9.10-playtest` и tag `v0.9.10-rc1` сохранены. Загрузчик реально скачал 6/6 внешних модов с проверкой размеров/SHA256; набор содержит 7 JAR. XaeroLib вложен в обе карты и отдельно не требуется. ZIP и .mrpack подготовлены для prerelease; Java/NeoForge/Minecraft и профиль пользователя не устанавливались/не запускались.

Список невыполненного: `docs/release/RC1_REMAINING_WORK.md`. P0 исправлены в коде, но реальная игровая/визуальная приёмка не пройдена. GUI scales не проверены в клиенте; дополнительные meal effects в kitchen preview не показаны; регрессия расчистки кладки не перезапускалась после исправления; 16 старых door-model parents остаются. Не обозначать RC как полностью принятую игровую сборку. Этот этап меняет упаковку/документацию; production JAR/код/контрольная сумма RC1 не менялись.

## 0.9.10 RC1 — public playtest stabilization (2026-10-06)

Ветка `release/0.9.10-playtest`, версия `0.9.10-rc1`. Исправлены UV/round model/blocking щита, зарегистрирован экран существующего RPG-меню Камня пути/рунной наковальни, добавлен shift-click наковальни; книга получила непрозрачную страницу и scroll, кухня — выбор рецепта/условия/результат. Курганный hotfix включён: сохранённые encounter slots, фиксированные квоты/боссы, наружные patches и единый 100-tick Порез. Геометрия кургана и существующие ID сохранены. Разбойники/Соловей не перерабатывались; полный Equipment & Art Overhaul и новая система рун не заявлены реализованными.

`clean build` и шесть CPU/data/codec проверок PASS; production JAR/resources PASS, 2685 статических ссылок, 0 новых ошибок. 16 прежних door-model parent issues отмечены отдельно. До последнего запроса hotfix: 5/5 GameTests; общая регрессия 26/27 обнаружила расчистку кладки, исправленную затем без повторного запуска игры. Клиент 0; после последнего запроса Minecraft не запускался. Игровая/визуальная приёмка — MANUAL PENDING. Отчёт: `docs/verification/playtest-0.9.10-rc1/acceptance.json`; playtest docs/JAR/checksum: `release/0.9.10-rc1/`. Изменения прежнего рабочего дерева включены, runtime worlds/logs/configs не включаются в release commit.

## 0.9.10 — анализ нового пакета (2026-10-06)

Прочитан Equipment & Art Overhaul FINAL; начато сопоставление с реальными ID, способностями и boss loot. Production-изменений 0.9.10 пока нет: требуется согласовать отсутствующие точные параметры и рецепты согласно просьбе пользователя не додумывать логику. Разбор: `docs/equipment/SPEC_REVIEW_0.9.10.md`. Существующие изменения 0.9.9 сохранены. Build/resource gates этого этапа не запускались; Minecraft-клиент — 0 запусков.
Acceptance update: thin/high superflat each passed 10 actual placements; six sequential commands from one point passed; water/native-terrain gates and all 27 companion tests passed without ERROR logs. Clean build and production JAR audit passed. Current override JAR is installed in Slavic-Myths-1.21.1-Testing (SHA256 22d6d025e106d912ed3c867704078f560ce5ff3bcd67bbc3b31340b3cd7f61cb); previous dev JAR backed up outside mods. Other mods/worlds unchanged; client not launched. Evidence: docs/verification/mob-worldgen-0.9.9/structure-priority-override.json. Full ordinary-world natural coverage remains NOT ACCEPTED.

## Текущий override: структура имеет приоритет над сушей

Реализована отдельная пошаговая ручная генерация трёх курганов и трёх лагерей: FULL-чанки до измерения высоты, проверка конечного объёма, подъём на тонком superflat, заполнение грунтом, выравнивание суши, локальный перенос от воды/защищённых построек в пределах 256 блоков. Поиск и генерация имеют status/cancel и не выполняют прежние синхронные обходы сотен удалённых starts. Проверка команд выполняется на headless сервере, Minecraft-клиент не запускается.

Подробности, ограничения отмены и результаты проверок: `docs/worldgen/STRUCTURE_PRIORITY_OVERRIDE_0.9.9.md`. Полная 0.9.9 остаётся в разработке: естественное покрытие обычного мира и компенсация не приняты. Записи ниже — история предыдущих этапов; их заявления о неизменённом production-коде относятся к моменту диагностики.

Research update 2026-10-06: real kurgan generate command reproduces the reported false 237-block terrain rejection caused by an unloaded heightmap; original-position bedrock preflight and synchronous search risks identified. User save structure settings inspected read-only. This is a diagnosis, not a fix; production code and installed JAR unchanged. Details: `docs/worldgen/COMMAND_GENERATION_DIAGNOSIS_0.9.9.md`.

Deployment update: by explicit user request, `0.9.9-dev` is installed in PolyMC `Slavic-Myths-1.21.1-Testing` for manual generation testing. Previous 0.9.7 JAR is backed up outside mods; other mods and worlds are unchanged. This is not release acceptance. Receipt: `docs/verification/mob-worldgen-0.9.9/polymc-dev-installation.json`. Minecraft was not launched.

# Slavic Myths 0.9.9 — implementation in progress

Continuation 2026-10-06: fixed raised-kurgan authored entrance stones using the persisted graded surface; soil excavation preserves underground construction. All three tiers and all three approach lanes pass; all 27 required companion GameTests pass. Clean production build `-Pmod_version=0.9.9-dev` passes; JAR resource validation passes. The latest natural-coverage measurement code includes relocated eligible cells and fails unmeasured/empty applicable networks; full natural gate still pending. Evidence: `docs/verification/mob-worldgen-0.9.9/continuation-2026-10-06.json` and `docs/worldgen/STRUCTURE_COVERAGE_AUDIT_0.9.9.md`. Stable PolyMC artifact is unchanged.

COMPILES: six spirit combat timelines, wildlife families/stat changes, existing bandit HP alignment, shared placement profiles, retained template foundation bounds, accepted-neighbor collision planning, and bounded persisted kurgan earthworks for thin superflat (explicitly authorized by the user). Build and verification details: docs/worldgen/STRUCTURE_COVERAGE_AUDIT_0.9.9.md and work/mob-worldgen-099-*.log. Static data audit: 2676 references, zero errors. Actual accepted-placement coverage/compensation, complete mob polish/audio audit, final resource/release gates and companion runtime checks remain unfinished. This is not 0.9.9 release acceptance. Installed stable PolyMC artifact remains 0.9.7; Minecraft client has not been launched.

# Slavic Myths 0.9.7 — болото и runtime hotfix

Реализованы: семь семейств болотных мест / 21 modern template; четыре дерева и природные детали только vanilla swamp; болотник 48 HP / редкий страж 112 HP; новые swamp русалки 36 HP и водяные 72 HP; пять таблиц структурной добычи + drop болотника. Старые ID/templates и остальной контент сохранены, новой biome/ore/block семьи нет.

Xaero logout/publish null-safe с очисткой внутренних данных перед optional callbacks, пользовательские waypoint сохраняются; JEI catalyst связан с зарегистрированным ritual RecipeType; linden_door использует native 1.21.1 states/models. Другие 16 старых door model parent issues остаются backlog.

DONE по build/headless: production 0.9.7, clean build; финальные core без JEI и шесть companions — по 14 GameTests без ERROR/FATAL. 42 сухих/мокрых размещения, 96 контейнеров, 720 опор, 400 форм деревьев, семь реальных accepted starts; пять features и болотник подтверждены в modified biome settings. Проверены 10 lifecycle cases production Xaero adapter с doubles и compiled JEI recipe-type contract. Проверки/gates и hash: docs/verification/swamp-0.9.7/acceptance.json; установка: installation-receipt.json в той же папке. JAR 0.9.7 установлен в Slavic-Myths-1.21.1-Testing, семь хешей проверены, прежний 0.9.6 сохранён; старые профили/миры не изменены. Headless размещение/данные/генерация/геометрия проверяются отдельно от клиента. Manual visual/gameplay QA: NOT STARTED; следующий конкретный блок — docs/MANUAL_QA_0.9.7_SWAMP.md. Клиент не запускался.

Подробнее: docs/swamp/SWAMP_0.9.7_*.md. Ниже сохранены исторические состояния прежних версий.

# Slavic Myths 0.9.6 — курганы

DONE по build/headless: графовый план v2 и совместимость v1, 14 архетипов, альтернативные лестницы, новая архитектура/палитры, 26 блоков + 26 BlockItem, 60 рецептов, семь новых таблиц наград, 35 авторских текстур 256×256 (11 блоков и 24 иконки). Существующие ID, характеристики, старый loot и глобальная частота генерации сохранены.

PASS: clean build; 150 новых планов; 1200 legacy планов и 900 составов; по 10 GameTests без JEI и с companion-модами, включая реальное размещение/контейнеры/ловушку. Data totals: 411 items, 179 blocks, 273 recipes, 260 loot tables, 154 advancements. Production: `build/libs/slavicmyths-0.9.6.jar`; evidence: `docs/verification/kurgan-0.9.6/acceptance.json`.

Client: COMPILES. Ручная визуальная/игровая QA: NOT STARTED, клиент не запускался. Следующий конкретный блок — `docs/MANUAL_QA_0.9.6_KURGAN.md`. Известный post-port backlog: 20 старых missing door model parents; подробности в resource-check.json. Проверенный JAR 0.9.6 установлен в известный тестовый PolyMC, семь JAR сверены по hash; прежний 0.9.5 сохранён. Receipt: `docs/verification/kurgan-0.9.6/installation-receipt.json`. Остальные профили/миры не изменены.

Документация алгоритма, комнат, ID/рецептов, loot и аудита иконок: `docs/kurgan/*0.9.6.md`. Далее сохранено историческое состояние 0.9.5.

# Slavic Myths 0.9.5 — навигация и зоны поиска

Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Слой навигации реализован по последнему
пакету V2. Предыдущий перенос 0.9.4 завершён; его отчёты сохранены как историческая база.

Core/server: DONE по headless проверкам. Client screen/HUD/Xaero: COMPILES, ручная QA не выполнена.
Build и финальные loader/data checks PASS. Minecraft-клиент не запускался.
Production: `build/libs/slavicmyths-0.9.5.jar`. Точный hash: `packaging/test-pack-lock.json`.

[Навигация, алгоритм и фактические ограничения](NAVIGATION_0.9.5.md),
[ручные проверки](MANUAL_QA_0.9.5_NAVIGATION.md), [evidence](verification/navigation-0.9.5/acceptance.json).

Следующий блок: ручной запуск тестового профиля и 28 navigation QA сценариев,
затем только воспроизведённые ошибки. Новые боссы/loot/worldgen не входят в 0.9.5.
Известный fallback: Xaero approximate center+radius, без polygon overlay. Контракт без
известного реального encounter ждёт появления цели; новых encounters навигация не генерирует.

## Исторические записи до 0.9.5

# Slavic Myths 0.9.4 — текущий перенос

Launcher fix: исправлены несовпадающие official PolyMC/Maven checksums NeoForge installer/universal локальным patch нового профиля. Реальный ForgeWrapper detector PASS; ручной повторный запуск ещё не проверен.

Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Все production sources компилируются; clean build, headless logic/resource/loader gates A–F PASS. Отдельный PolyMC Slavic-Myths-1.21.1-Testing установлен с семью проверенными JAR. Старый 1.16.5 профиль/миры сохранены. Клиент не запускался; игровая QA ещё не выполнена.

Фактическое состояние и журналы: [PORT_STATUS_0.9.4.md](port/PORT_STATUS_0.9.4.md). Исторические записи ниже описывают предыдущие этапы и версии, а не подтверждение работоспособности текущего порта.

Next concrete block: manual launch of the new profile and fresh-world QA (client/Curios/JEI/AI/structures/persistence), then fix reproduced port regressions. No 0.9.5 or gameplay redesign scope.

# Актуальная ветка 0.9.x

- 0.9.0: Овинник/Волколак, первые трофеи и hunting система реализованы; headless QA, игровой QA TODO.
- 0.9.1: Огненный змей/Подвей, восемь наград, расширение существующей охоты реализованы; headless QA PASS, игровой QA TODO.
- 0.9.2: Лихо Одноглазое / Тугарин Змей — код и headless проверки реализованы; игровой QA/визуальная сверка в Minecraft TODO.
- 0.9.3: исправлен superflat/placement, добавлено яйцо Яги и production placement regression; Яга, избушка, прогресс, GUI, предметы/обмен/котёл реализованы; headless проверки PASS, игровой QA TODO (MANUAL_QA_0.9.3.md).
- 0.9.4: перенос существующего контента на NeoForge 1.21.1 / Java 21 — в работе; см. port/PORT_STATUS_0.9.4.md. Дизайн/баланс не входят в scope.
- 0.9.5+: content freeze, аудит/баги/визуал/баланс/performance/polish.
- 1.0.0: стабильный релиз.

Живая/мёртвая вода перенесены на поздний этап и в 0.9.0 не реализованы.
Следующее действие: завершить gates порта 0.9.4; дальнейшие переработки выполняются только на 1.21.1.
Исторические планы ниже сохранены для контекста.

---

# Актуальная курганная арка

- 0.8.4: предметы, колода, дымовая обработка и внешние прототипы реализованы;
  сборка, ресурсы и установка PASS, ручной QA TODO.
- 0.8.5: лабиринты, кладка, декор, нарушение покоя и проклятие реализованы; headless QA PASS, игровой QA TODO.
- 0.8.6: шесть противников, два мини-босса и главный босс реализованы; headless QA PASS, игровой QA TODO.
- 0.8.7: прежний план главного босса выполнен в 0.8.6 по новому ТЗ; дополнительных работ без нового задания нет.

# Актуальный этап

- 0.8.2: реализация encounter, ресурсов, наград и интеграции завершена.
  Финальная сборка/проверки/установка PASS; ручной игровой QA — TODO.
- Следующее действие — ручной QA 0.8.2; новые арки и глобальный polish не начинались.

Ниже сохранена история планов.

# План итераций

- **0.8.1 — Большой разбойничий стан:** реализация завершена, build/resources PASS;
  следующий шаг — пользовательский QA по CHECKLIST_0.8.1.md. Игровых запусков 0.
- **0.8.2 — Соловей-разбойник:** не начата; требуется отдельное задание.
  В 0.8.1 есть только архитектурно подготовленный двор и lore-перо.

Ниже сохранены предыдущие планы как история.

- **0.8.0.1 — Woodlands & Timber:** реализация завершена; clean build/resources PASS.
  Четыре породы, древесные наборы, ягоды, свисающая листва, biome integration.
  Ручной игровой QA — следующий шаг; Minecraft запусков 0.
- **0.8.1 — Большой разбойничий стан:** только после отдельного задания.
  Новая древесная палитра готова для использования, структуры пока не перерабатывались.

Далее — история ранее выполненных итераций.

- **0.8.0:** реализация Combat & Bandits завершена, build/resources PASS.
  Следующий шаг — ручной игровой QA по CHECKLIST_0.8.0.md; запусков 0.
- **0.8.1:** не начата; требуется отдельное задание.

Ниже — исторические планы, а не дополнительные обязательства текущей итерации.

- **0.7.3 — Топи и затопленные руины:** реализация завершена: шесть основных
  категорий и малые POI, 23 NBT, команды swamp/structure/next, terrain checks,
  loot, книга и exploration-достижения. Clean build и resource/NBT checks PASS.
  Игровых запусков 0; ручной тест пользователя — следующий шаг.
- **0.7.4:** только отдельное задание после ручного QA 0.7.3; баланс/доводка/Creative.
  Контент отсутствующей в этой копии 0.7.1 не создаётся в рамках 0.7.3.

Ниже сохранена история планов прошлых итераций; актуальный срез — CONTINUE_0.7.3.md.

- **0.6.5 — wildlife/cooking:** реализован завершённый первый срез животных,
  их лута и деревенской еды. Ягодные кусты, cooking workstations и мифическое
  оборудование остаются следующими самостоятельными этапами без заглушек.

- **0.6.0:** реализован RPG-фундамент: 4 пути / 24 навыка, 8 рун, перековка,
  серверные меню, сохранение NBT. Ожидается ручная проверка в игре.

## 0.6.5 — Mythic Equipment (только план)

- Дополнительные слоты: 1 амулет, 2 кольца, 1 пояс, 1 оберег.
- Создаваемые аксессуары и совместимые с рунами украшения.
- Уникальные несоздаваемые реликвии, добыча существ/боссов, Сапоги-скороходы.

- **0.5.1 — entity hotfix:** модели, текстуры, анимации и звуки четырёх духов
  обновлены; build PASS. Остаётся ручная проверка в игре, запуск пользователем.

- **0.4.6 — art polish:** улучшены ключевые предметы, блоки и броня; игровой
  контент и будущие персонажи не расширялись. Ожидается ручная визуальная проверка.

- **0.4.5 — визуальная переработка и предметы:** реализованы оригинальные спрайты,
  янтарь, посохи, оружие, щиты, обереги, три обряда, книга и достижения. Ручная
  проверка внешнего вида/баланса в PolyMC ожидается. Новых персонажей нет.
- **Следующий этап персонажей:** сначала отдельный дизайн каждого существа
  (источник, образ, модель, звук, поведение, взаимодействие и роль в прогрессии).
  Прежние идеи 0.5.0 остаются предварительными, не назначены к реализации.

- **0.4.1 — расширение материального контента:** первый проход реализован: перунитовое
  снаряжение, серебро, травы и ремесленные ингредиенты, еда и декор. В работе остаются
  ступка и некоторые напитки ещё оставались планами этого прохода; позже серебряный
  бонус, дополнительные обереги, книга и достижения вошли в 0.4.5.

- **0.1.0 — фундамент:** предметы, ресурсы, документация.
- **0.2.0 — природные ресурсы:** растения, береста, перунит, рецепты и достижения.
- **0.3.0 — первые духи:** Домовой и Леший, модели, звуки, spawn и loot; optional JEI.
- **0.4.0 — духи, капища и первые ритуалы:** отношения и дом Домового, способности Лешего,
  капище, идол, жертвенник, первый ритуал и знак, находки, Книга сказаний и знания.
  Реализовано; границы проверки — PROJECT_STATUS и VERIFICATION_0.4.0.

## Перед расширением

Пройти MANUAL_TEST_0.4.0: естественное капище и баланс, старый survival-мир,
два игрока/dedicated server, боевые способности, GUI/JEI и сохранение.
Измерить нагрузку в длительной игре, прежде чем усложнять AI.

## Предварительные будущие этапы — НЕ реализованы

- **0.5.0 — Воды и болота:** Водяной, Русалка, водные/болотные существа,
  растения, ресурсы, водные места, ритуалы, книга и достижения.
- **0.6.0 — Ведьмина тропа:** Баба-яга, избушка, диалоги, задания, обмен и загадки.
- **0.7.0 — Путь героя:** классы Дружинник/Волхв/Ведун, способности, развитие и бой.
  Это игровые роли, а не исторические «RPG-классы».
- **Позже:** курганы, Змей, крупные структуры, Навь, Перун, Велес, другие боги,
  боссы, измерения и end-game progression.

Не реализовывать весь roadmap одним проходом.

0.4.7: weapon proportions, shields, thunder spear, mace variants and berdysh implemented; build/install verified. Manual in-game weapon/shield verification pending; optional silver berdysh and sharpening stone deferred.

0.5.0 content implemented and packaged: Kikimora, Poludnitsa, Polevik, Bannik, Igosha, Ovinnik, bathhouse and barn. Manual gameplay/balance validation pending; advancements unspecified because the supplied task ends at section 121.
# Итог финализации 0.6.5

0.7.2 «Хозяева глубин»: основной encounter и награды реализованы, техническая сборка
проверена; отсутствует интеграция с 0.7.1, поскольку её контента нет в этой копии проекта.
Следующие отдельные этапы: 0.7.3 — крупные постройки/исследование; 0.7.4 — исправления,
категории Creative и общая доводка арки после ручных тестов. Оба этапа сейчас не начаты.

Следующий реализованный этап: **0.7.0 «Тихие воды»** — рыбалка, сети, речные обитатели,
водная кухня, растения, малые POI, Водяной, Русалка и постепенная песня с counterplay.
Техническая сборка проверена; далее ручной тест пользователя и точечные исправления.
0.7.1 и крупные водно-болотные структуры не реализуются в текущем проходе.

Artifact content drop: пять основных артефактов реализованы, ожидают ручной проверки.
Дорогой рецепт Оберега сохранён; survival-интеграция четырёх новых артефактов отложена.
Необязательные резной сундук, столб и утварь не добавлены.

Реализованы ягодные кусты, кухня, ограничение шапки в существующей системе аксессуаров,
Метла, Ступа, Пест, полёт, груз и «Попутный ветер». Получение транспорта через сказочный
survival-контент отложено согласно заданию; сейчас Creative/команды.
Следующий шаг — ручная проверка пользователем и точечные исправления по её результатам.
0.7.0 «Воды и болота» в этом проходе не начинается.

## 0.9.4 current next block

Finish custom item/block APIs and concrete EntityBlock/ticker/menu transaction integration, then initialize real registries for codecs/inventory/Curios/JEI verification. Major entity/client/network/worldgen work follows. No 0.9.5 and no gameplay/worldgen/visual backlog fixes in this stage.
