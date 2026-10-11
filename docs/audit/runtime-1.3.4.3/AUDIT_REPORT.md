# Slavic Myths 1.3.4.3 — Runtime Audit

Дата: 2026-10-08. Это аудит; gameplay, баланс, модели, registry IDs и production код не изменены. Commit/push не выполнялись.

## 1. Executive summary

20 записей: 17 исходных пунктов A01–A17 и 3 дополнительных A18–A20. P0/P1 не установлены. Есть подтверждённые дефекты выбора цели и подсчёта знаний; остальные записи включают UX/design requests и явно непроверенные симптомы. Полного ручного игрового прохода нет: клиент загрузился, но native capture после двух попыток и восстановления инструмента завершился timeout. Скриншотов игры нет; ASSET_REFERENCE_NOT_GAME.png — только статическая подборка исходных текстур.

## 2. Environment / build / launches

Minecraft1.21.1, NeoForge21.1.255, Java21.0.12, mod1.3.4.3. gradlew.bat clean build: SUCCESS26sec. Production JAR build/libs/slavicmyths-1.3.4.3.jar, SHA256 e0c0b0d195e3e7596e4ce5b244d1729146d6a89383267e5a8f2e0a7ae5e4a951. Проверены3018JSON,901PNG,659 зарегистрированных предметов; отсутствующих item models/ru/en переводов и сломанных локальных model/texture references не найдено. Тестовых классов в JAR нет. Эта проверка не доказывает правильность всех recipe semantics или каждой геометрии.

Реальных запусков: 7 GameTest серверов и 1 dev client. Первый сервер16tests:15pass/1устаревшее ожидание Heat. Второй19/19pass. Третий24tests:2падения из-за солнца/геометрии fixtures. Четвёртый24/24pass с изоляцией. Пятый36tests:4падения (3 подтвердили отсутствие auto-target босса;1 перекрёстное воздействие соседнего fixture). Шестой36/36pass, каждый floating case изолирован по высоте, проверки фиксируют наблюдаемое поведение, включая отсутствие урона Соловью. Седьмой37/37pass, включая Knowledge duplicate. Итог последнего прогона приведён в SERVER_RESULT.txt. Прохождение observation assertion не означает исправление зарегистрированного дефекта.

Тестовые ServerPlayer имеют stub network listener; для RuneVisual владелец зарегистрирован в PlayerList UUID map только в fixture. Это реальная серверная симуляция tick/damage/entity events, но не подключение настоящего игрока, не проверка клавиши R и не игровой перезаход. Старые проверки storage roundtrip — same-process, не restart. GameTest world отдельно; игровые миры PolyMC не использовались. Client .tools/runtime-audit/client; audit server .tools/runtime-audit/server; исторический первый run-rare-1322 тоже test-only.

Клиент загрузил мод/Curios/JEI и atlases; после невозможности UI capture тестовый процесс был явно остановлен, поэтому runAuditClient завершился FAILED (не ошибка clean build и не обнаруженный runtime crash). ошибок отсутствующих ресурсов мода в log не найдено. Есть vanilla goat sound warnings и sampler warning; звук на слух, экран и механики клиентом не подтверждены. MSPT, multiplayer и процессный restart не измерялись.

## 3. Подтверждённые пользовательские проблемы

A02: automatic targeting не видит Соловья без lastHurtMob; stationary zombie/skeleton/bandit на2/5/10блоках получают урон, на15 — нет. Босс на2/5/10/15 остаётся100HP. Зомби, перенесённый с5 на14блоков до удара, остаётся100HP; это выход за валидный диапазон12, а не доказательство поломки движения. Несколько целей одновременно/движение внутри range/препятствия требуют дальнейших тестов.

A05: exported Tugarin6damage ×1.8=10.8номинальный DPS против diamond7×1.6=11.2. Разница DPS около3.6%, одного удара14.3%; парный заряженный бонус даёт8.4damage/15.12DPS до bleed. Не все редкие предметы слабее: nightingale dagger7×2=14DPS. A06/A10: mixed creative category подтверждена export, не наблюдением экрана. A01: тихий отказ Creative подтверждён, первоначальный пользовательский случай не установлен.

## 4. Не воспроизведено / не проверено

Общий отказ Flight в Survival не воспроизведён. Общий отказ Floating на обычных stationary enemies не воспроизведён. Код flail использует SPEAR, не BOW; нож TRIDENT_THROW, не ARROW_SHOOT. Отдельных wind_boots нет. A04/A07/A15 визуально не проверены. Сеть и проценты Death не проверены игровым использованием; animal/boss staff restrictions подтверждены по predicate, а не новой полной runtime matrix. Нельзя превращать эти записи в список уже исправленных багов.

## 5. Новые findings

A18: duplicate depth_gift в Knowledge.ENTRIES; bitCount может учитывать одно открытие дважды и рано выдавать lore_keeper. A19: control predicate сети отличается от общего, защита NPC требует проверки. A20: устаревшие validators не соответствуют новым mechanics/version. Исторический docs/port/final-check.json был перезаписан старым verifier и восстановлен в исходное содержимое; его исторический результат не пересматривается.

## 6. Баланс

BALANCE_TABLE.csv содержит все зарегистрированные атакующие/utility weapon classes и редкие посохи/щит/сеть/сапоги; vanilla sword references отмечены отдельно. Damage/speed/reach/durability/socket values получены в сервере с mainhand equip и native attributes. DPS=damage×attack_speed, без armor, crit, damage windows, charging и projectile travel. Для utility/staff не подставлен фиктивный melee DPS. Source — прямые references recipe/loot; dynamic trade/quest sources без reference помечены для отдельного анализа, не объявлены недоступными.

Тугарин: native bleed0.5HP/stack, max3 сохранён; руна blood max2. Ataman9/.7=6.3 rawDPS, учитывать четвёртый удар/pressure. Likho4/1.2=4.8 и heal/control. Victor spear и burial weapons см. CSV; повреждённые находки намеренно слабее восстановленных. Perunite sword6.5/1.6=10.4, durability850 против diamond1561, но3rune slots против2. Имеет смысл согласовать роль perunite, не менять автоматически весь tier.

Short bow12ticks draw, velocity2.4, damage factor.8; heavy30ticks, velocity3.6, factor1.15. Их итоговый ranged DPS зависит от ammo/enchantments/flight/critical и требует отдельного измерения, не melee formula. Death:5HP порог,12HP цена, cooldown400ticks, immune bosses. Варианты10/12.5/15% — только дизайн: для100HP это10/12.5/15HP против нынешних5; для300HP30/37.5/45, поэтому отдельный boss cap/elite policy обязателен до реализации.

## 7. Creative audit

CREATIVE_CURRENT.json — полный текущий builder order, component variants сохранены текстом. CREATIVE_TAB_PROPOSAL.md — отдельное предложение.9 вкладок: slavicmyths/resources/farming/food/tools/armor/decor/magic/mobs. Категории явно исключают неиспользуемые bear-cub/doe eggs; это не потерянные предметы. Weapon/tool/supply separation сейчас не реализована. Новых вкладок этим аудитом не добавлено.

## 8. Visual audit

Проверены наличие/валидность моделей и PNG, parent/override references. ASSET_REFERENCE_NOT_GAME.png — исходные sprites, не gameplay evidence. Heavy bow имеет все3pull-stage models. SevenLeagueBoots —3D model и отдельная armor texture; наличие геометрии не объясняет мерцание без кадра. Будущий redesign: sword — длинный клинок/гарда; dagger — короткий широкий клинок; spear — длинное тонкое древко/наконечник; axe — боковое лезвие; mace — компактная тяжёлая головка; staff — тонкое древко/отчётливое навершие; shield — широкая защитная плоскость. Перед изменением определить размеры, материалы и признаки; разные классы не делать recolor одного шаблона.

## 9. Animation / sound

Flail: SPEAR use animation, charge18ticks, cooldown40; отдельный spinning-chain telegraph не обнаружен. Knife: TRIDENT_THROW pitch1.6 и TRIDENT_HIT; сравнение на слух не выполнено. Heavy bow ARROW_SHOOT pitch.8; short pitch1.3. Likho cast particles и control; отсутствие выразительного hit/fail feedback проверить в client. RuneVisual synced phase/item state есть, изображение полёта не проверено. Нельзя выводить отсутствие кастомных анимаций только из сообщения AnimationLoader Loaded0: часть анимаций реализована кодом renderer.

## 10. AI / faction

Bandit target goals: Player,Villager,IronGolem; hostile-marker и faction relation не эквивалентны. Предложение в FACTION_AI_PROPOSAL.md. Никакой новой faction AI не внедрено. Старый static audit не является MSPT measurement; не вводить global scanning для решения A02.

## 11. Knowledge book

Нынешний advancement-backed lore GUI не является требуемым data-driven bestiary. Подробнее KNOWLEDGE_BOOK_PROPOSAL.md. A18 следует отделить от масштабного GUI redesign; обязательна совместимость saved advancement IDs и network mask. Фольклорные источники не выдумывались.

## 12. Recommended roadmap / issue cards

Пять будущих групп: (1) target predicates + отказ/tooltip рунам и control; (2) Knowledge duplicate compatibility; (3) роли редкого/perunite и Death design после замеров; (4) visual/client review boots/bows/staves/flails/sounds; (5) creative/book/faction scoped iterations. Ни одна группа не реализована этим аудитом.

Top10: A02 targeting; A18 duplicate knowledge; A01 flight feedback; A11 staff predicate; A19 net NPC safety; A05 rare/perunite roles; A07 boots visual reproduction; A13 net immunity feedback; A10 creative order; A20 trustworthy validators.

### Карточки

#### A01 [P2] rune_flight / rune_wind

Категория: runes. Полёт работает в Survival; Creative/mayfly отклоняется без объяснения

Шаги: Установить flight в diamond boots; вызвать ability intent в Survival и Creative.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Полёт работает в Survival; Creative/mayfly отклоняется без объяснения.

В игре/серверном runtime: partial; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: RareRuneRuntime.intent/startFlight; Wind не является Flight. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): rpg/RareRuneRuntime.java;client/RareRuneClient.java;rpg/RareRunes.java.

Рекомендация: Показать R, совместимые ботинки и причину отказа; пользовательский случай требует точного предмета/режима. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: локальный контракт и тесты; полный redesign не обязателен.

#### A02 [P2] rune_floating_weapon / nightingale

Категория: runes. Нет автоматического выбора Соловья без предварительного удара

Шаги: Экипировать sword с floating_weapon, зарегистрировать owner; stationary enemy2/5/10/15; без предварительного удара по boss.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Нет автоматического выбора Соловья без предварительного удара.

В игре/серверном runtime: yes; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: Автопоиск ограничен instanceof Enemy; NightingaleEntity extends PathfinderMob. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): rpg/RuneVisual.java;bandit/NightingaleEntity.java.

Рекомендация: Явно определить hostile target predicate с защитой NPC/союзников; проверить lastHurtMob и движущиеся цели. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: локальный контракт и тесты; полный redesign не обязателен.

#### A03 [P3] all material flails

Категория: animation. Зарядка не имеет отдельной читаемой анимации; BOW в коде отсутствует

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Зарядка не имеет отдельной читаемой анимации; BOW в коде отсутствует.

В игре/серверном runtime: no; по коду: partial. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: UseAnim.SPEAR; release18ticks; cooldown40; нет отдельного зарядного звука. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): combat/FlailItem.java.

Рекомендация: Предложить цепь/вращение и фазовый звук; проверять каждый материал в клиенте. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: да, согласовать визуальный/UX/системный вариант.

#### A04 [P3] heavy_bow

Категория: visual. Тяжёлый лук наследует ванильный масштаб bow

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Тяжёлый лук наследует ванильный масштаб bow.

В игре/серверном runtime: unverified; по коду: partial. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: parent minecraft:item/bow; свои текстуры и 3 pull-модели. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): resources/assets/slavicmyths/models/item/heavy_bow.json;combat/FieldBowItem.java.

Рекомендация: Сначала согласовать крупные плечи, рукоять, толщину и 3 стадии натяжения. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: да, согласовать визуальный/UX/системный вариант.

#### A05 [P2] rare/quest weapons

Категория: balance. Редкость не гарантирует больший одиночный удар; одиночный Тугарин6 против diamond7

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Редкость не гарантирует больший одиночный удар; одиночный Тугарин6 против diamond7.

В игре/серверном runtime: partial; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: Роли и специальные механики; pair x1.4, AS1.8; perunite tier bonus2.5. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): combat/RareWeapons.java;combat/RareCombat.java;registry/ModGear.java.

Рекомендация: Балансировать роли по полной таблице, а не увеличивать весь редкий loot. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: да, согласовать визуальный/UX/системный вариант.

#### A06 [P3] runes

Категория: creative. Руны находятся в смешанной вкладке magic

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Руны находятся в смешанной вкладке magic.

В игре/серверном runtime: yes_export; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: ModItemGroup category7 + RuneFoundation variants. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): registry/ModItemGroup.java.

Рекомендация: Выделить вкладку Рун; сырьё и пустые основы перед обычными/редкими рунами. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: да, согласовать визуальный/UX/системный вариант.

#### A07 [P2] seven_league_boots

Категория: visual. Мерцание/смена цвета в руках не проверены

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Мерцание/смена цвета в руках не проверены.

В игре/серверном runtime: unverified; по коду: partial. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: 3D elements, общий atlas folk_materials; нет отдельного firstperson transform. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): item/SevenLeagueBoots.java;resources/assets/slavicmyths/models/item/seven_league_boots.json.

Рекомендация: Проверить UV, coplanar faces, culling, две руки и first/third-person; сохранять armor texture. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: да, согласовать визуальный/UX/системный вариант.

#### A08 [P3] wind boots

Категория: content. Отдельных усиленных wind_boots среди 659 registry items нет

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Отдельных усиленных wind_boots среди 659 registry items нет.

В игре/серверном runtime: no; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: Есть rune_wind, rune_flight, seven_league_boots и material boots. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): registry/ModItems.java;rpg/RareRunes.java;REGISTRY_EXPORT.json.

Рекомендация: Прояснить названия и ожидаемый предмет; не вводить новую броню без задания. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: локальный контракт и тесты; полный redesign не обязателен.

#### A09 [P3] wooden_barrel

Категория: storage. Деревянная бочка: специальное хранилище9 слотов, не vanilla barrel27

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Деревянная бочка: специальное хранилище9 слотов, не vanilla barrel27.

В игре/серверном runtime: unverified; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: Household.Kind.BARREL9; barrel_items; Fermentation interaction before menu. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): storage/Household.java;storage/StorageBlock.java;brewing/Fermentation.java.

Рекомендация: Уточнить tooltip, разрешённые предметы, брожение и роль относительно сундука. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: локальный контракт и тесты; полный redesign не обязателен.

#### A10 [P3] tools / herb_pouch

Категория: creative. Оружие, инструменты и мешочки смешаны; порядок в основном по registry ID

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Оружие, инструменты и мешочки смешаны; порядок в основном по registry ID.

В игре/серверном runtime: yes_export; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: TOOLS включает herb_pouch/meshochek_trofeev; category4. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): registry/ModItemGroup.java;CREATIVE_CURRENT.json.

Рекомендация: Разделить Оружие/Инструменты/Снаряжение; сортировать семейство затем материал. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: да, согласовать визуальный/UX/системный вариант.

#### A11 [P2] likho_staff

Категория: combat. Контроль работает на тестовом hostile; животные и Соловей отсекаются фильтром

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Контроль работает на тестовом hostile; животные и Соловей отсекаются фильтром.

В игре/серверном runtime: partial; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: Enemy or Player + narrow ray12; cooldown560; no failure feedback. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): combat/LikhoStaffItem.java.

Рекомендация: Явно документировать допустимые цели/иммунитет; пересмотреть predicate и обратную связь. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: локальный контракт и тесты; полный redesign не обязателен.

#### A12 [P3] rune_death

Категория: balance. Порог5HP и цена12HP; боссы из immune tag исключены

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Порог5HP и цена12HP; боссы из immune tag исключены.

В игре/серверном runtime: unverified; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: RareRuneRuntime execution + rune_execute_immune tag. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): rpg/RareRuneRuntime.java;resources/data/slavicmyths/tags/entity_type/rune_death_immune.json.

Рекомендация: Обсудить варианты10/12.5/15% и elite/boss policy; не применять проценты автоматически. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: да, согласовать визуальный/UX/системный вариант.

#### A13 [P2] vodyanoy_net

Категория: combat. RMB метает сеть; это замедление, не отдельная анимация связывания

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: RMB метает сеть; это замедление, не отдельная анимация связывания.

В игре/серверном runtime: unverified; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: Projectile; land60ticks amp3/water100ticks amp4; maxHP>=100 immunity. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): depth/VodyanoyNetItem.java;depth/ThrownNet.java.

Рекомендация: Показать траекторию/попадание/иммунитет и назначение в tooltip. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: локальный контракт и тесты; полный redesign не обязателен.

#### A14 [P3] lore_book

Категория: knowledge. Нынешняя книга основана на advancement mask, полноценного bestiary нет

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Нынешняя книга основана на advancement mask, полноценного bestiary нет.

В игре/серверном runtime: no; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: Knowledge.ENTRIES + LoreNetwork/LoreBookScreen. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): progression/Knowledge.java;item/LoreBookItem.java.

Рекомендация: Проектировать data-driven HIDDEN/ENCOUNTERED/DEFEATED; отдельные gameplay/lore. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: да, согласовать визуальный/UX/системный вариант.

#### A15 [P3] storm_staff

Категория: visual. Существующая 3D-модель посоха требует визуального review

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Существующая 3D-модель посоха требует визуального review.

В игре/серверном runtime: unverified; по коду: partial. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: Ресурсы wood/binding/crystal; вытянутые elements и transforms. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): resources/assets/slavicmyths/models/item/storm_staff.json.

Рекомендация: Утоньшить древко; читаемый навершник/кристалл и обмотка; согласовать пропорции. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: да, согласовать визуальный/UX/системный вариант.

#### A16 [P3] throwing knives

Категория: sound. Бросок использует TRIDENT_THROW, а не звук лука

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Бросок использует TRIDENT_THROW, а не звук лука.

В игре/серверном runtime: no; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: SoundEvents.TRIDENT_THROW, pitch1.6; попадание TRIDENT_HIT. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): combat/ThrowingWeaponItem.java;combat/WeaponProjectile.java.

Рекомендация: Оценить звук в клиенте; отдельный короткий whoosh при подтверждённой необходимости. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: локальный контракт и тесты; полный redesign не обязателен.

#### A17 [P3] throwing knives loyalty

Категория: enchantment. Loyalty не реализована; hit recovery может терять нож с шансом25%

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Loyalty не реализована; hit recovery может терять нож с шансом25%.

В игре/серверном runtime: no; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: AbstractArrow-based projectile; sword-compatible enchantments, not Loyalty. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): combat/ThrowingWeaponItem.java;combat/WeaponProjectile.java.

Рекомендация: Проектировать owner-return state; сохранение компонентов; unloaded/dead-owner fallback. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: да, согласовать визуальный/UX/системный вариант.

#### A18 [P2] depth_gift / lore_keeper

Категория: knowledge. Два уникальных открытия могут выполнить порог трёх открытий

Шаги: Award depth_gift и meet_domovoy; вызвать Knowledge.mask; проверить bitCount/lore_keeper.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Два уникальных открытия могут выполнить порог трёх открытий.

В игре/серверном runtime: yes; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: depth_gift дважды в ENTRIES; mask.bitCount считает оба бита. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): progression/Knowledge.java.

Рекомендация: Убрать дубликат через совместимую миграцию mask/protocol; считать уникальные advancement IDs. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: да, согласовать визуальный/UX/системный вариант.

#### A19 [P2] vodyanoy_net / NPC

Категория: targeting. Обычная сеть не использует общий canAffect predicate; риск воздействия на нейтральных NPC

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Обычная сеть не использует общий canAffect predicate; риск воздействия на нейтральных NPC.

В игре/серверном runtime: unverified; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: Только owner/allied/maxHP/dimension/music filters, отдельная ветка ClassNetDuration. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): depth/ThrownNet.java;rpg/Abilities.java.

Рекомендация: Согласовать защиту NPC и нейтралов для всех control abilities; проверить до изменения. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: локальный контракт и тесты; полный redesign не обязателен.

#### A20 [P3] verify_resources.py / rare tests

Категория: tooling. Исторические проверки дают ложные падения на нынешней версии

Шаги: Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.

Ожидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: Исторические проверки дают ложные падения на нынешней версии.

В игре/серверном runtime: yes; по коду: yes. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: Port validator pinned0.9.4; rare test expects guaranteed Heat ignition instead of15%. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): tools/verify_resources.py;tools/verify_port_1211.py;tools/rare-tests/org/slavicmyths/verify/RareWeaponGameTests.java.

Рекомендация: Версионировать validators и использовать детерминированные assertions для вероятностных рун. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: локальный контракт и тесты; полный redesign не обязателен.

