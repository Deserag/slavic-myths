# Редкие руны — 1.3.4.3

Реализовано по Slavic_Myths_1.3.4.3_Rare_Runes_Codex.zip на существующем рабочем дереве, с сохранением пользовательских изменений. Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21 / official mappings. Commit не создан. Клиент и сервер **не запускались: 0/0**, по сохраняющемуся прямому ограничению пользователя. Игровая приёмка ниже остаётся открытой. PolyMC остаётся на 1.3.3.

## Предметы, источники и миграция

| Руна | Полный registry ID | Источник |
|---|---|---|
| Парящего оружия | slavicmyths:rune_floating_weapon | Обмен Яги, stage 3 |
| Полёта | slavicmyths:rune_flight | Существующий nightingale |
| Защиты | slavicmyths:rune_rare_protection | Существующий kurgan_voevoda |
| Смерти | slavicmyths:rune_death | Существующий likho_one_eyed |
| Берсерка | slavicmyths:rune_berserker | Существующий tugarin_zmey |
| Жертвы | slavicmyths:rune_sacrifice | Обмен Яги, stage 3 |

Воевода выбран по фактическому KurganFighter: тяжёлый воинский мини-босс, 150 HP и armor 12. Новый босс не создан. Старая `rune_protection` со своим прежним эффектом сохранена; новая редкая защита получила отдельный ID, поскольку старая обычная руна не является трёхщитовой редкой механикой. Остальные существующие rune IDs/effects, RuneState schema, legacy heat/wind происхождение и материальные ёмкости сохранены. Шесть новых редких рун — отдельные предметы без tier-прокачки; все в существующей magic Creative Tab.

Четыре именных fragments: `slavicmyths:rune_flight_fragment`, `slavicmyths:rune_rare_protection_fragment`, `slavicmyths:rune_death_fragment`, `slavicmyths:rune_berserker_fragment`. Универсального fragment и fragments для exchange runes нет.

Тугарин, Лихо и воевода: один fragment гарантированно, независимый шанс 25% второго, независимый шанс 4% полной руны дополнительно. Лихо/Тугарин доступны через существующие повторные BossRitualItem encounters; spawn/ритуалы не изменены. В StrongholdRecords Соловей одноразовый для конкретного стана, а его loot откладывается до конца collapse: сохранён существующий путь выдачи, но гарантированы **три** flight fragments за победу, плюс 25% ещё одного и 4% целой руны. Это не требует повторно оживлять того же Соловья и не ломает его progression flags. Все прежние loot pools четырёх источников сохранены; новые добавлены в конец.

## Обмены и восстановление

Яга использует существующие YagaServices/YagaMenu, проверку NPC/range/canonical/progression и vanilla menu-button packets. Новые exchanges доступны после stage 3:

- Жертва: `soul ×2`, `soul_fragment ×8`, `perunite_dust ×4`, `kost_likha ×1` → `rune_sacrifice ×1`.
- Парящее оружие: `empowered_soul ×1`, `diamond_dust ×4`, `tugarinova_kozha ×1`, `ancient_sign ×2` → `rune_floating_weapon ×1`.

Использованы реальные трофеи Лиха и Тугарина, не новые фиктивные ресурсы. До consume проверяются суммарные потребности; списание/выдача последовательны в server menu handler. Rate limit четыре ticks отклоняет повторный двойной клик; повторная оплаченная покупка позже разрешена. При полном inventory остаётся прежняя выдача через drop возле игрока. Экран расширен до трёх страниц, показывает все четыре inputs и отключает кнопку при недостатке ресурсов; сервер всё равно перепроверяет.

Recipe IDs: `slavicmyths:rare_rune_restore_flight`, `_rare_protection`, `_death`, `_berserker`. Каждый: **три одинаковых именных fragment → одна соответствующая руна**, только существующий `slavicmyths:rune_crafting` в Rune Anvil. Никаких blank/dust/soul/catalyst/chisel costs. Нет ordinary crafting/upgrade recipes для шести rare runes. Mixed fragments не удовлетворяют recipe. Использован прежний authoritative output transaction с оплатой партии, защитой output/shift-click; режим восстановления представлен отдельными рецептами существующего каталога.

Для восстановления виден один fragment input, counter 3/3, ghost preview результата и output. Неиспользуемые пустые inputs скрыты; оставшиеся предметы из предыдущего рецепта видны и возвращаются при закрытии. Popup расширен до пяти колонок: все 27 recipes помещаются в прежний 202×214 экран. JEI автоматически показывает четыре восстановления через существующий optional JeiRunes; exchanges не добавлены как crafting recipes. Source/exchange и параметры указаны в RU/EN tooltips.

## Механики

Одна копия rare ID на предмет; RuneState/admission запрещают дубликаты. Эффект использует только один совместимый источник: main hand для melee, FEET для Flight, CHEST для Protection. Offhand копия не удваивает эффект. При нескольких экипированных копиях tooltip сообщает конфликт; предметы/руны не удаляются.

| Механика | Фактические параметры |
|---|---|
| Floating Weapon | До 6 с; максимум 3 attack attempts; интервал 28 ticks (1,4 с); урон 70% snapshot physical attack; CD 35 с |
| Flight | 5 с, flying speed 0,0675 (=0,05×1,35); полный vanilla flight input; fall grace 3 с; CD 45 с |
| Protection | 3 shields, каждый поглощает один direct hit; первый triggering hit расходует один; максимум 20 с; CD 60 с после depletion/expiry/abort |
| Death | После успешного direct melee: живая цель ≤5 HP, владелец >12 HP; цена 12 HP; CD 20 с после успешного execute |
| Berserker >50% HP | Нет bonus/penalty |
| Berserker ≤50% HP | Damage +10%, attack speed +5%; effective defense −5% |
| Berserker ≤30% HP | Damage +20%, attack speed +10%; effective defense −12% |
| Berserker ≤15% HP | Damage +30%, attack speed +15%; effective defense −20% |
| Sacrifice | Цена 6 HP при HP >6; 8 с damage +25%, attack speed +15%; CD 40 с от активации |

Берсерк выбирает только один текущий threshold. Defense реализована controlled final damage-taken multiplier `1/(1-penalty)`: ×1,052632 / ×1,136364 / ×1,25. Armor attribute не меняется. Damage Berserker/Sacrifice применяется только к успешному incoming direct player melee, не к стрелам. Attack speed — transient `slavicmyths:rare_speed`, удаление перед повторным добавлением; при unequip/expiry/cleanup снимается. Разные Berserker/Sacrifice bonuses складываются: до +55% melee и +30% attack speed; ordinary rune modifiers и native weapon state machines не заменены.

Self-cost — отдельный контролируемый exact health debit с marker `RuneSelfCost`, через setHealth после строгой проверки >cost. Hurt events не вызываются: armor/absorption/shield/Protection/reflect/on-hit реакции не могут поглотить оплату или запустить её каскадом. Жертва естественно обновляет Berserker threshold после потери HP. Для этой цены не симулируется hostile damage source.

Death использует registry-backed `slavicmyths:rune_execute`: bypass armor/shield/hurt cooldown, без bypass invulnerability. Урон отправляется по обычному hurt path, сохраняет player kill credit; оплата и CD только когда цель действительно погибла. Periodic-style directEntity=null не запускает melee/on-hit цепочки. Explicit `slavicmyths:rune_death_immune` содержит фактических phased bosses nightingale/likho_one_eyed/tugarin_zmey/kurgan_voevoda/unresting_prince: это консервативное исключение обязательных фаз. Обычные mobs остаются исполнимыми; technical visual entity не LivingEntity и не execute target.

## Сущности, рендер, полёт и завершение

`slavicmyths:rune_visual` — одна маленькая non-AI/noPhysics/noSave entity на instance Floating/Protection. Никаких armour stands, arrows под видом меча или AI сущностей на каждый щит. SynchedEntityData передаёт реальный ItemStack, owner entity ID, mode/count/phase. Сервер хранит owner UUID, snapshot damage, срок по overworld gameTime; локальный lifetime тоже ограничен. Истечение не продлевается выгрузкой chunk, принудительного chunk loading нет.

Floating использует настоящий held item model/texture; spawn scale, hover, aim, short dash, return и shrink/dissolve particles. Цель — lastHurtMob либо ближайший валидный Enemy в локальном радиусе 10 (дальность hit до 12), с line of sight/Abilities.canHit/PvP rules. Local target query максимум один на цикл, всего до трёх атак; мировой поиск отсутствует. Snapshot: 70% ATTACK_DAMAGE attribute (включая ordinary static rune/item modifiers) × текущий безопасный Berserker/Sacrifice multiplier. Enchantment conditional damage, class armed attacks и native special weapon state machines не повторяются. Source `slavicmyths:rune_echo`, directEntity — visual entity, cause — owner: все существующие player melee/arrow proc predicates исключают его. Другие игроки получают обычную entity tracking/render animation. Сам source не является fire/blood/melee proc.

Protection renderer рисует три небольшие вращающиеся двусторонние translucent плоскости вокруг owner, quick orient при incoming attack, отдельную crack texture и fade. Server считает remaining shields. Валидны direct melee и projectile impacts с attacker/source, включая direct magical/fire projectile; DOT без direct entity, environmental fire/lava, fall, drowning, void, thorns и unavoidable damage не расходуют щит. Self-cost/execute исключены. При depletion/expiry CD сохраняется в player state; выдача shield lifetime заранее сохраняет conservative cooldown deadline на случай restart.

Flight выдаёт временную server ability lease: mayfly/flying и snapshot прежней flying speed. Уже имеющий flight или creative player не получает новую lease; spectator не активирует. По завершении/unequip/death/logout/dimension/login stale lease отключается в survival, прежняя speed восстанавливается, current creative/spectator права не удаляются. Fall grace ровно 60 ticks, затем защита прекращается. Lease marker persistent, активный timer временный; login восстанавливает права после аварийно прерванной сессии. Полёт не постоянен.

## Input, overlay, сеть и сохранение

Class skill slots принимают только ClassDefinitions и не имеют rune selection. Поэтому добавлена одна общая remappable key `key.slavicmyths.rare_rune` (R): use, Shift+R cycle по реально экипированным Floating/Flight/Sacrifice. Нет отдельной кнопки на каждую руну. Существующие ClassClient/G-H-J skills и Classes Screen/HUD layout не переделаны. Новый compact rune HUD справа выше прежних slots/vanilla bars, с selected icon/name/key/cooldown.

RareRuneNetwork C2S intent содержит только boolean cycle; server определяет equipped abilities, проверяет alive/spectator, cooldown, цену и rate limit 4 ticks. Client не передаёт damage, timer, reward или ItemStack. State владельца синхронизируется через существующий RpgSync; visuals — через обычную entity tracking. Все gameplay decisions серверные; common rare classes не загружают client renderer/JEI.

Cooldowns `CD_<ability>` и Selected хранятся в `PathData/PlayerPersisted/SlavicPaths/RareRunes`, по существующему ClassState.now (overworld gameTime). Existing Clone копирует persisted compound; unequip/drop/chest/relog/dimension не обнуляют deadline. Logout/death/dimension завершают активные effects, а не их cooldown. GameTime не является wall clock: время закрытого сервера не вычитает cooldown.

Berserker vignette — лёгкие узкие red edge bands; интенсивность растёт с threshold, медленная мягкая пульсация на tiers 2/3, краткая реакция tier 3 на direct hit. Центр/crosshair и нижние vanilla bars остаются чистыми; при открытом Screen overlay/HUD не рисуются. Sacrifice даёт short red-purple edge pulse и пять witch particles. Client TOML options: `berserker_screen_effect=true`, `berserker_screen_intensity=0.65`, range 0..1; 0 отключает интенсивность. Опции находятся в existing CLIENT ClassHudConfig SPEC, не требуют отдельного общего/server config.

PlayerTick выполняет только постоянное число metadata/equipment checks для timers и сравнение HP/maxHP при held Berserker; атрибуты/packets не обновляются каждый tick без изменения. Это нужно для expiry и HP/maxHP изменений, включая heal/regeneration/внешние attribute effects. Нет обходов chunks/entities/всех игроков; actual MSPT не измерен.

## Art, проверки и ограничения

Шесть dark-stone 32×32 RGBA items с разными абстрактными glyphs и локальным цветом резьбы; никаких буквальных sword/wing/shield/skull/heart/drop пиктограмм, II/III variants или upgrades. Четыре fragments вырезаны по разным неправильным контурам из соответствующего stone/glyph, визуально отличаются от Soul Fragment. [Контактный лист](media/rare-runes-1343/items.png). Existing art/rune/weapon textures сохранены. `tools/rare_rune_resources_1343.py` воспроизводит актуальные assets/recipes/locales и добавочные source loot, не дублирует pools при повторе.

Clean build PASS. Offline shared-rule/JSON/NBT/wire checks **20491**: exact cooldown/duration, boundary HP thresholds, costs/execute gates, singleton admission и packet component preservation, three-shield arithmetic, first echo hit at tick 24 и интервалы 28 ticks, bounded selection/intent codec. Прежние regression checks: Rune Rework **355**, Foundation **75064** (3000 bounded plans), armor **147**. Ресурсные production/preservation gates, SHA и точный размер: [машинный отчёт](verification/rare-runes-1.3.4.3.json). Сохранены 4931 прежний resource; старые loot pools/locale entries проверены отдельно. Test/verify/smoke classes не входят в production; все JSON читаются, generator reproduction PASS.

Production: `build/libs/slavicmyths-1.3.4.3.jar`. Последняя clean production сборка и rare checks — `.tools/rune-1343/final-build.log`; общий предыдущий regression run — `.tools/rune-1343/build.log`. Компиляция сверяет установленный API, но не подтверждает игровой lifecycle/network/render. Unit NBT copy не называется реальным relog/death/restart. Client/server/JEI rendering, flight movement/lease restoration, direct protection hits, phase immunity, inventory transactions, PvP/два игрока и screen animation **не проверены игровым запуском**. Отдельные требования архива к runtime/dedicated smoke остаются открытыми из-за прямого запрета пользователя; MSPT не измерен.

Изменённые файлы итерации: `SlavicMyths.java`, `Runes.java`, `RuneDefinition.java`, `RuneRework.java`, `RpgMenu.java`, `MythDamageSources.java`, `ClassHudConfig.java`, `ClientSetup.java`, `RpgClient.java`, `RuneAnvilScreen.java`, `YagaMenu.java`, `YagaServices.java`, `YagaScreen.java`; новые `RareRuneRules.java`, `RareRunes.java`, `RareRuneRuntime.java`, `RareRuneNetwork.java`, `RuneVisual.java`, client `RareRuneClient.java`, `RuneVisualRenderer.java`; `gradle.properties`, `build.gradle`; десять item models/textures, два shield textures, четыре restoration recipes, четыре source loot overlays, два damage types, три damage tags, entity immunity tag, RU/EN additions; `tools/rare_rune_resources_1343.py`, `verify_rare_runes_1343.py`, `tools/rare-rune-tests/.../RareRuneOffline.java`; этот отчёт/media/verification и PROJECT_STATUS/ROADMAP/ARCHITECTURE. Future systems, new bosses, frost/clone/III upgrade/accessories не добавлены.

Ручная приёмка после разрешения запуска: источники/Яга/полный inventory/double click; 3 same и 2+1 fragments/shift-click/JEI; Floating spawn/aim/3 attacks/no proc chains/other player visibility; Flight five seconds/descending/expiry/creative switch/boots removal/fall grace; Protection direct/projectile versus DOT/last shield/lifetime; Death ordinary mob/tag/boss phases/cost; all Berserker thresholds/overlay/config; Sacrifice price/synergy; cooldown unequip/death/relog/restart/dimension; отдельный dedicated server/PvP. Только отдельный тестовый мир, без перезаписи игровых saves.
