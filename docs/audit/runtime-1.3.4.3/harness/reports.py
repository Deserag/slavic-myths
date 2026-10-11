from pathlib import Path
import csv,json,shutil,subprocess
O=Path('docs/audit/runtime-1.3.4.3')
# Restore only the file overwritten by the obsolete audit command, preserving pre-existing work.
Path('docs/port/final-check.json').write_bytes(subprocess.check_output(['git','show','HEAD:docs/port/final-check.json']))
rows=[
('A01','P2','runes','rune_flight / rune_wind','partial','yes','Полёт работает в Survival; Creative/mayfly отклоняется без объяснения','RareRuneRuntime.intent/startFlight; Wind не является Flight','rpg/RareRuneRuntime.java;rpg/RareRuneClient.java;rpg/RareRunes.java','Показать R, совместимые ботинки и причину отказа; пользовательский случай требует точного предмета/режима'),
('A02','P2','runes','rune_floating_weapon / nightingale','yes','yes','Нет автоматического выбора Соловья без предварительного удара','Автопоиск ограничен instanceof Enemy; NightingaleEntity extends PathfinderMob','rpg/RuneVisual.java;bandit/NightingaleEntity.java','Явно определить hostile target predicate с защитой NPC/союзников; проверить lastHurtMob и движущиеся цели'),
('A03','P3','animation','all material flails','no','partial','Зарядка не имеет отдельной читаемой анимации; BOW в коде отсутствует','UseAnim.SPEAR; release18ticks; cooldown40; нет отдельного зарядного звука','combat/FlailItem.java','Предложить цепь/вращение и фазовый звук; проверять каждый материал в клиенте'),
('A04','P3','visual','heavy_bow','unverified','partial','Тяжёлый лук наследует ванильный масштаб bow','parent minecraft:item/bow; свои текстуры и 3 pull-модели','resources/assets/slavicmyths/models/item/heavy_bow.json;combat/FieldBowItem.java','Сначала согласовать крупные плечи, рукоять, толщину и 3 стадии натяжения'),
('A05','P2','balance','rare/quest weapons','partial','yes','Редкость не гарантирует больший одиночный удар; одиночный Тугарин6 против diamond7','Роли и специальные механики; pair x1.4, AS1.8; perunite tier bonus2.5','combat/RareWeapons.java;combat/RareCombat.java;registry/ModGear.java','Балансировать роли по полной таблице, а не увеличивать весь редкий loot'),
('A06','P3','creative','runes','yes_export','yes','Руны находятся в смешанной вкладке magic','ModItemGroup category7 + RuneFoundation variants','registry/ModItemGroup.java','Выделить вкладку Рун; сырьё и пустые основы перед обычными/редкими рунами'),
('A07','P2','visual','seven_league_boots','unverified','partial','Мерцание/смена цвета в руках не проверены','3D elements, общий atlas folk_materials; нет отдельного firstperson transform','item/SevenLeagueBoots.java;resources/assets/slavicmyths/models/item/seven_league_boots.json','Проверить UV, coplanar faces, culling, две руки и first/third-person; сохранять armor texture'),
('A08','P3','content','wind boots','no','yes','Отдельных усиленных wind_boots среди 659 registry items нет','Есть rune_wind, rune_flight, seven_league_boots и material boots','registry/ModItems.java;rpg/RareRunes.java;REGISTRY_EXPORT.json','Прояснить названия и ожидаемый предмет; не вводить новую броню без задания'),
('A09','P3','storage','wooden_barrel','unverified','yes','Деревянная бочка: специальное хранилище9 слотов, не vanilla barrel27','Household.Kind.BARREL9; barrel_items; Fermentation interaction before menu','storage/Household.java;storage/StorageBlock.java;brewing/Fermentation.java','Уточнить tooltip, разрешённые предметы, брожение и роль относительно сундука'),
('A10','P3','creative','tools / herb_pouch','yes_export','yes','Оружие, инструменты и мешочки смешаны; порядок в основном по registry ID','TOOLS включает herb_pouch/meshochek_trofeev; category4','registry/ModItemGroup.java;CREATIVE_CURRENT.json','Разделить Оружие/Инструменты/Снаряжение; сортировать семейство затем материал'),
('A11','P2','combat','likho_staff','partial','yes','Контроль работает на тестовом hostile; животные и Соловей отсекаются фильтром','Enemy or Player + narrow ray12; cooldown560; no failure feedback','combat/LikhoStaffItem.java','Явно документировать допустимые цели/иммунитет; пересмотреть predicate и обратную связь'),
('A12','P3','balance','rune_death','unverified','yes','Порог5HP и цена12HP; боссы из immune tag исключены','RareRuneRuntime execution + rune_execute_immune tag','rpg/RareRuneRuntime.java;resources/data/slavicmyths/tags/entity_type/rune_execute_immune.json','Обсудить варианты10/12.5/15% и elite/boss policy; не применять проценты автоматически'),
('A13','P2','combat','vodyanoy_net','unverified','yes','RMB метает сеть; это замедление, не отдельная анимация связывания','Projectile; land60ticks amp3/water100ticks amp4; maxHP>=100 immunity','depth/VodyanoyNetItem.java;depth/ThrownNet.java','Показать траекторию/попадание/иммунитет и назначение в tooltip'),
('A14','P3','knowledge','lore_book','no','yes','Нынешняя книга основана на advancement mask, полноценного bestiary нет','Knowledge.ENTRIES + LoreNetwork/LoreBookScreen','progression/Knowledge.java;item/LoreBookItem.java','Проектировать data-driven HIDDEN/ENCOUNTERED/DEFEATED; отдельные gameplay/lore'),
('A15','P3','visual','storm_staff','unverified','partial','Существующая 3D-модель посоха требует визуального review','Ресурсы wood/binding/crystal; вытянутые elements и transforms','resources/assets/slavicmyths/models/item/storm_staff.json','Утоньшить древко; читаемый навершник/кристалл и обмотка; согласовать пропорции'),
('A16','P3','sound','throwing knives','no','yes','Бросок использует TRIDENT_THROW, а не звук лука','SoundEvents.TRIDENT_THROW, pitch1.6; попадание TRIDENT_HIT','combat/ThrowingWeaponItem.java;combat/ThrownWeaponEntity.java','Оценить звук в клиенте; отдельный короткий whoosh при подтверждённой необходимости'),
('A17','P3','enchantment','throwing knives loyalty','no','yes','Loyalty не реализована; hit recovery может терять нож с шансом25%','AbstractArrow-based projectile; sword-compatible enchantments, not Loyalty','combat/ThrowingWeaponItem.java;combat/ThrownWeaponEntity.java','Проектировать owner-return state; сохранение компонентов; unloaded/dead-owner fallback'),
('A18','P2','knowledge','depth_gift / lore_keeper','yes','yes','Два уникальных открытия могут выполнить порог трёх открытий','depth_gift дважды в ENTRIES; mask.bitCount считает оба бита','progression/Knowledge.java','Убрать дубликат через совместимую миграцию mask/protocol; считать уникальные advancement IDs'),
('A19','P2','targeting','vodyanoy_net / NPC','unverified','yes','Обычная сеть не использует общий canAffect predicate; риск воздействия на нейтральных NPC','Только owner/allied/maxHP/dimension/music filters, отдельная ветка ClassNetDuration','depth/ThrownNet.java;rpg/Abilities.java','Согласовать защиту NPC и нейтралов для всех control abilities; проверить до изменения'),
('A20','P3','tooling','verify_resources.py / rare tests','yes','yes','Исторические проверки дают ложные падения на нынешней версии','Port validator pinned0.9.4; rare test expects guaranteed Heat ignition instead of15%','tools/verify_resources.py;tools/verify_port_1211.py;tools/rare-tests/org/slavicmyths/verify/RareWeaponGameTests.java','Версионировать validators и использовать детерминированные assertions для вероятностных рун')]
fields='id,severity,category,item_or_entity,reproduced,code_confirmed,summary,suspected_cause,files,recommended_action'.split(',')
with(O/'AUDIT_ISSUES.csv').open('w',encoding='utf-8-sig',newline='')as f:w=csv.writer(f);w.writerow(fields);w.writerows(rows)
(O/'CREATIVE_TAB_PROPOSAL.md').write_text('''# Предложение Creative Tab (без реализации)

Фактический полный порядок 9 вкладок и component-варианты рун сохранены в CREATIVE_CURRENT.json. Registry export содержит 659 предметов. Export получен вызовами тех же ModItemGroup.items и RuneFoundation.creative, которые использует builder; экран вкладок не проверен.

Предлагаемый порядок: Основное → Материалы → Земледелие → Еда/напитки → Оружие → Инструменты → Снаряжение → Броня → Руны → Магия → Декор/хранилища → Существа.

Оружие: мечи, кинжалы, копья, сулицы, топоры/булавы/цепы, луки/стрелы, щиты; внутри семейства stone → iron → silver → gold → diamond → perunite → netherite, затем редкие/квестовые рядом с соответствующим семейством. Материальную прогрессию perunite/netherite согласовать после баланса. Мешочки, охотничий рог и сеть — Снаряжение, не оружейный ряд. Посохи с основной магической способностью — Магия, с пояснением класса оружия.

Руны: пустые основы с вариантами материала → обычные I/II → редкие активные/пассивные → осколки. Совместимость и действие видны в tooltip; названия Ветер/Полёт различаются. У руны Flight указать R, 5сек,45секCD и boots slot. Сокеты не должны выглядеть отдельными полностью одинаковыми предметами без понятного материала.

Сохранить registry IDs, исключения неиспользуемых spawn eggs, search entries и компонентные варианты. Новые вкладки — отдельная будущая задача; сейчас порядок не менялся.
''',encoding='utf-8')
(O/'KNOWLEDGE_BOOK_PROPOSAL.md').write_text('''# Книга знаний: проект, без реализации

Существующая LoreBookItem открывает LoreNetwork с 32-bit advancement mask. Knowledge.ENTRIES содержит повтор depth_gift (A18), каталог ограничен 28 позициями и не описывает всех нынешних боссов/предметы. Нельзя просто расширять int без протокола и миграции.

Записи data/slavicmyths/knowledge/<id>.json: id, category, entity_ids, icon, ru/en translation keys, unlock criteria, gameplay sections, lore sections, source references. Состояния HIDDEN → ENCOUNTERED → DEFEATED. HIDDEN показывает только неизвестный силуэт/категорию; ENCOUNTERED — внешность, поведение, угрозы, биом; DEFEATED — подтверждённые трофеи/советы и механики. Для нейтральных духов финальное состояние можно получать мирным взаимодействием, не требовать убийства.

Сервер подтверждает encounter через ограниченное событие взаимодействия/бой/первое отслеживание с диапазоном, defeat через LivingDeathEvent с корректным kill credit. Никакого поиска всех сущностей каждый тик. Сохранение через namespace advancements или versioned per-player IDs; существующие advancements сохраняются. Синхронизация registry IDs/state deltas после login/изменения, никакого произвольного unlock от клиента.

Gameplay: здоровье, опасные фазы, допустимые эффекты, предметы/источники, иммунитет, реальные игровые условия. Lore: фольклорный источник, поздние реконструкции, игровая интерпретация отмечены раздельно; не придумывать библиографию. Поиск, категории, связанные предметы/руны, подсказки управления, ru/en. JEI — необязательная ссылка через изолированный adapter, не зависимость общего кода.

Первый шаг: исправить duplicate accounting совместимо, затем schema/network, 3 пилотные записи (Соловей, Лихо, Водяной), после проверки расширять каталог. Полный GUI вне этого аудита.
''',encoding='utf-8')
(O/'FACTION_AI_PROPOSAL.md').write_text('''# Фракции и AI: проект, без реализации

Сейчас BanditEntity целится в Player, Villager и IronGolem стандартными target goals; общего контракта humans vs unclean нет. Enemy-marker недостаточен: NightingaleEntity — PathfinderMob, поэтому автопоиск RuneVisual его пропускает.

Предлагаемые data-driven tags/relations: humans, bandits, unclean_hostile, neutral_spirits, protected_npcs, bosses. Человеческий разбойник остаётся угрозой игроку/поселению; отношение к нечисти отдельное, не должно превращать все соседние фракции в бесконечную общую драку. Нейтральные духи агрессируют только по подтверждённому событию; торговцев/сюжетных NPC защищает явный predicate. Общий canTarget проверяет ally, team, owner, NPC protection, boss encounter, PvP и LOS.

Приоритет: текущая активная схватка/агрессор → игрок encounter босса → явный опасный противник → idle. Не перенаправлять босса с участвующего игрока на случайного зомби. Retaliation контролировать по фракции/радиусу, setAlertOthers не считать готовой фракционной системой.

Hysteresis: минимальное удержание цели, ограниченная дальность выхода, потеря LOS с отсрочкой, новый target должен быть существенно приоритетнее. Для standard goal acquisition использовать ограниченные интервалы и локальный AABB, не world scans/forced chunks. Обновления отношений через события, без обхода игроков каждый тик.

Проверки будущего патча: игрок+разбойник+нечисть; нейтральный дух; защищённый NPC; два союзника; босс с участником и посторонним мобом; unload/reload; owner death; PvP off. Замер MSPT на заданной плотности до/после — отдельная runtime-проверка, нынешний статический аудит его не заменяет.
''',encoding='utf-8')
# Report skeleton plus complete issue cards.
text='''# Slavic Myths 1.3.4.3 — Runtime Audit

Дата: 2026-10-08. Это аудит; gameplay, баланс, модели, registry IDs и production код не изменены. Commit/push не выполнялись.

## 1. Executive summary

20 записей: 17 исходных пунктов A01–A17 и 3 дополнительных A18–A20. P0/P1 не установлены. Есть подтверждённые дефекты выбора цели и подсчёта знаний; остальные записи включают UX/design requests и явно непроверенные симптомы. Полного ручного игрового прохода нет: клиент загрузился, но native capture после двух попыток и восстановления инструмента завершился timeout. Скриншотов игры нет; ASSET_REFERENCE_NOT_GAME.png — только статическая подборка исходных текстур.

## 2. Environment / build / launches

Minecraft1.21.1, NeoForge21.1.255, Java21.0.12, mod1.3.4.3. gradlew.bat clean build: SUCCESS26sec. Production JAR build/libs/slavicmyths-1.3.4.3.jar, SHA256 e0c0b0d195e3e7596e4ce5b244d1729146d6a89383267e5a8f2e0a7ae5e4a951. Проверены3018JSON,901PNG,659 зарегистрированных предметов; отсутствующих item models/ru/en переводов и сломанных локальных model/texture references не найдено. Тестовых классов в JAR нет. Эта проверка не доказывает правильность всех recipe semantics или каждой геометрии.

Реальных запусков: 7 GameTest серверов и 1 dev client. Первый сервер16tests:15pass/1устаревшее ожидание Heat. Второй19/19pass. Третий24tests:2падения из-за солнца/геометрии fixtures. Четвёртый24/24pass с изоляцией. Пятый36tests:4падения (3 подтвердили отсутствие auto-target босса;1 перекрёстное воздействие соседнего fixture). Шестой36/36pass, каждый floating case изолирован по высоте, проверки фиксируют наблюдаемое поведение, включая отсутствие урона Соловью. Седьмой добавляет отдельную проверку Knowledge duplicate. Итог последнего прогона приведён в SERVER_RESULT.txt. Прохождение observation assertion не означает исправление зарегистрированного дефекта.

Тестовые ServerPlayer имеют stub network listener; для RuneVisual владелец зарегистрирован в PlayerList UUID map только в fixture. Это реальная серверная симуляция tick/damage/entity events, но не подключение настоящего игрока, не проверка клавиши R и не игровой перезаход. Старые проверки storage roundtrip — same-process, не restart. GameTest world отдельно; игровые миры PolyMC не использовались. Client .tools/runtime-audit/client; audit server .tools/runtime-audit/server; исторический первый run-rare-1322 тоже test-only.

Клиент загрузил мод/Curios/JEI и atlases; ошибок отсутствующих ресурсов мода в log не найдено. Есть vanilla goat sound warnings и sampler warning; звук на слух, экран и механики клиентом не подтверждены. MSPT, multiplayer и процессный restart не измерялись.

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

'''
for r in rows:
 id,sev,cat,item,repro,code,summary,cause,files,action=r
 text+=f'#### {id} [{sev}] {item}\n\nКатегория: {cat}. {summary}\n\nШаги: '+({'A01':'Установить flight в diamond boots; вызвать ability intent в Survival и Creative.','A02':'Экипировать sword с floating_weapon, зарегистрировать owner; stationary enemy2/5/10/15; без предварительного удара по boss.','A18':'Award depth_gift и meet_domovoy; вызвать Knowledge.mask; проверить bitCount/lore_keeper.'}.get(id,'Проверить указанный предмет/систему в условиях исходного обращения; сопоставить код и перечисленные ресурсы.'))+f'\n\nОжидается: понятное назначение, правильный выбор цели/состояния и честное сообщение об ограничениях. Фактически: {summary}.\n\nВ игре/серверном runtime: {repro}; по коду: {code}. partial/unverified не означают подтверждение исходного игрового симптома. Причина/гипотеза: {cause}. Файлы (от src/main/java/org/slavicmyths, кроме resources/tools/export): {files}.\n\nРекомендация: {action}. Риск исправления: изменение сохранённых компонентов/баланса/target policy; нужен отдельный патч и регрессия по затронутой системе. Предварительный redesign: '+('да, согласовать визуальный/UX/системный вариант'if cat in ['visual','creative','knowledge','balance','enchantment','animation']else'локальный контракт и тесты; полный redesign не обязателен')+'.\n\n'
(O/'AUDIT_REPORT.md').write_text(text,encoding='utf-8')
for n in ['build.log','client.log','server-tests.log','server-audit.log','server-matrix.log','server-matrix-isolated.log','server-final.log','server-matrix-final.log','server-evidence.log']:
 p=Path('.tools/runtime-audit')/n
 if p.exists():shutil.copy2(p,O/n)
print('Reports written:',len(rows),'records')
