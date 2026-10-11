# Slavic Myths 1.3.4.4 — Combat / Rune Fixes

Реализовано на main по архиву Combat_Rune_Fixes и двум уточнениям пользователя. Утверждённые10/12/12/10/10 заменяют старые8/9/11/9/6 из архива. Minecraft1.21.1 / NeoForge21.1.255 / Java21.0.12. Mod/registry IDs сохранены. Commit/push не выполнены.

## Реализованные механики

**Targeting.** CombatTargeting разделяет canAttack, canControl и isHostileCombatTarget. Исключает самого владельца, dead/removed/spectator/invulnerable, allied/team, собственных pets, защищённых NPC и armor stands. Использует существующий village_hostiles tag (включая Соловья/Тугарина) перед проверкой нейтральных LandSpiritEntity; control_immune — отдельный data-driven tag. combat_protected содержит Villager/WanderingTrader/Домового/Ягу. Нейтральный дух автоматически выбирается только при текущей агрессии к владельцу; friendly permits учитывается. Полной faction AI нет.

**Flight.** Активация R (или переназначенная клавиша) после установки в boots. Клиентский tooltip получает actual key display name, общий код не загружает KeyMapping. Survival/Adventure/Creative:100ticks flight и скорость0.0675, cooldown900ticks. Сохраняются mayfly/flying/speed/game mode; снятие/окончание/cleanup восстанавливает состояние. При смене game mode старые flags не переносятся обратно. Есть совместимость старого lease1.3.4.3. Начало/invalid state/нет совместимой активной руны/cooldown/невозможная цена имеют actionbar feedback по попытке, без сообщений каждый tick. Cooldown сохраняется в existing persisted RareRunes data при cleanup/death/logout. Сам реальный relog и процессный restart в этом проходе не проверялись.

**Floating.** Раньше Enemy-marker пропускал PathfinderMob Соловья; теперь общий helper учитывает custom hostile tag. LastHurtMob имеет приоритет, далее nearest hostile в12блоках с LOS. Aim/dash/hit/return,3попытки атаки, lifetime120ticks и dissolve8ticks. Пустой цикл не расходует атаки; умершая цель заменяется следующей. RuneEcho damage не является PLAYER_ATTACK и не запускает Death/Fire/Blood/Sacrifice chains. Нет world scans/forced chunks в production.

**Лихо.** Животные, обычные/custom hostile и допустимые PvP цели проходят ray12; NPC/allied/control-immune защищены. Обычная цель:25ticks stun и120ticks morok после него. Resistant/scripted bosses:6sec Weakness I + Slowness I без полного stun и без DISORIENTATION AI replacement. Heal1HP/180ticks и560ticks cooldown сохранены. Miss/immune/cooldown/success/soft resistance имеют feedback; звук успеха minecraft:entity.evoker.cast_spell.

**Death.** Точная формула `min(maxHealth * 0.15, 50 HP)`.20HP→3,100→15,300→45,500/1000→50. Цена12HP при health>12, cooldown400ticks. Цена прямым setHealth, без armor/shield/protection/reflect/hostile damage events; оплачивается только после успешного убийства. rune_death_immune сохранён, invulnerable/scripted targets не обходятся.

**Сеть.** Общий canControl; owner/allies/NPC/explicit immunity исключены. Обычная сеть сохраняет maxHealth≥100,dimension/music immunity; land60ticks slowIV иwater100ticks slowV. Existing ClassNetDuration остаётся отдельной class soft-control веткой после общего predicate, без автоматического переноса highHP immunity на class skill. Иммунитет виден actionbar+smoke, tooltip объясняет3/5сек и сильных существ.

**Knife Loyalty.** Только ножам разрешена Loyalty через фактические NeoForge Item enchant hooks; Riptide/Channeling не разрешены. Entity hit/block hit/flight timeout включают сохранённый Returning state. Level влияет на ускорение `.05*level` с damping. Возвращается тот же component-bearing stack (name/durability/enchant/rune data), не отдельный reward. При offline/dead/removed/spectator/different-level owner или600returnticks fallback — drop; inventory full — drop у owner. Pickup ограничен owner при возврате. Без Loyalty25% loss после entity hit сохранён; с Loyalty loss0%. Creative-only projectile не выдаёт transferable item. Возврат сохраняется в projectile NBT; реальный chunk-unload/restart отдельно не прогонялся.

**Звук ножа.** Все material knives используют `SoundEvents.SNOWBALL_THROW` (`minecraft:entity.snowball.throw`), volume0.3,pitch1.7. Sulitsa сохраняет TRIDENT_THROW, hit сохраняет metallic TRIDENT_HIT. Новых .ogg нет. На слух не проверено.

## Парные клинки Тугарина — уточнение пользователя

Один real tugarin_sword stack = одна пара. В inventory/ground две отдельные исходные blade silhouettes с прежним металлом/рукоятью/текстурой; в first/third-person по одному клинку в каждой руке. Baked model wrapper выбирает форму по ItemDisplayContext, не по key property, поэтому inventory icon не меняется при удержании. Третий/первый ракурс учитывают левую ведущую руку. Второй клинок только renderer, не реальный offhand ItemStack: извлечь/скопировать его невозможно.

При выборе пары в основной руке существующий offhand item сохраняется целиком в PlayerPersisted escrow, slot становится пустым. При переключении возврат в свободную offhand; при занятости — inventory/drop. Logout/death возвращают escrow, чтобы предмет попадал в обычные сохранение/drops. Не удаляется пользовательский щит/факел и не меняются его компоненты. Craft теперь потребляет оба donor swords и выдаёт один stack-пару, а не второй transferable pair в остатках. Как раньше, output наследует компоненты первого donor; второй donor потребляется рецептом. Существующие сохранённые tugarin_sword не удаляются/не объединяются: каждый такой предмет теперь является парой.

Парный charged bonus1.4 применяется один раз; native bleed0.5HP×max3 сохранён. Руны действуют только с основного real stack. Дополнительный износ второго удара теперь списывается с того же stack (вместе с обычным износом); второй предмет для износа не создаётся. Звук доставания пары — `minecraft:item.armor.equip_iron` volume0.45,pitch1.25.

## Урон и источник

| Предмет | Было → стало | Скорость | Nominal DPS |
|---|---:|---:|---:|
| Тугарин |6→10|1.8|18|
| Победитель |7→12|1.2|14.4|
| Атаман |9→12|0.7|8.4|
| Соловей |7→10|2|20|
| Лихо |4→10|1.2|12|
| Омутное копьё |7→9|1.2|10.8|

Омутное копьё — дополнительный подтверждённый trophy: recipe/pool_spear требует pool_pearl, loot/entities/elder_vodyanoy выдаёт его. Применено архивное+2. Остальные обычные materials/Perunite/vanilla/tools и utility artifacts не усилены. Vanilla netherite sword остаётся8damage. Nominal DPS=damage×attack_speed до armor/crit/specials, не измеренный sustained DPS. Tugarin charged14HP/25.2nominalDPS доbleed, Ataman fourth16.2HP; механики не ослаблены в компенсацию. Reach Victor4.25 сохранён.

## Проверки и границы

`gradlew.bat clean build -PrareRuneOfflineCheck verifyRareRunes`: SUCCESS35sec;20491pure-rule assertions.4реальных non-interactive GameTest server launches,0clients. Первый54tests:46pass/8fail (WorldBoss neutral-branch precedence и слишком ранний cleanup assertion). Второй54/54pass. Третий60tests:58pass/2fixture failures из-за entity movement в нетикаемый neighboring chunk без настоящего игрока. Последний61/61pass: только fixture локально force-load3×3chunks для матрицы, production chunk loading не добавлялся. Final assertions проверяют исправленное поведение, не старый дефект.

Матрица Floating: Zombie/Skeleton/Nightingale/Tugarin2/5/10/15; friendly NPC; реальный server damage, cleanup и retarget. Лихо animal/hostile/custom boss/NPC, cooldown/soft stun policy. Death formula/threshold/cost/insufficientHP/immune. Flight lease/Creative/game-mode/cooldown cleanup. Net land/water/highHP/owner/NPC. Loyalty admission+реальнаяAnvilMenu book application,entity/block/timeout returns,exact components,no duplicate,offline/dead/full-inventory fallback. Pair damage14 безdouble multiplier,escrow components/full inventory/death и one-output recipe; обычные редкие mechanics/loot/storage/repair regressions включены.

Test ServerPlayers имеют stub network listener; owner UUID registration только fixture. Это native server simulation, не настоящий login/input/multiplayer. NBT native storage roundtrip не выдаётся за процессный restart. Сетевое отображение, движение реального игрока, MSPT и hearing не проверены.

Resource gate6591checks,3021JSON,901PNG; JAR совпадает с исходными ресурсами, test classes отсутствуют. Обновлены RU/EN и additive generators; повторная генерация current overlay не откатывает модели/tooltip. Исторические validators не запускались и не переписывались. Knowledge duplicate, Creative, heavy bow/storm staff/boots redesign и faction AI остаются за рамками.

`REQUIRES_MANUAL_CLIENT_CHECK`: две руки first/third-person/default+slim skin+left main arm; inventory icon/ground; equip switching; custom/remapped rune key tooltip; draw/throw/spell/fail sounds. Клиент не запускался.

## Артефакт / установка

Production: `build/libs/slavicmyths-1.3.4.4.jar`; SHA256 `488fe8606a920486bb42956fef44ea401a9a45729a79b79c64513a5d9d762165`.

Установлен в Slavic-Myths-1.21.1-Testing (Minecraft1.21.1/NeoForge21.1.255). Предыдущая1.3.4.3 сохранена вне mods; активен один JAR. Остальные765files неизменны. Игровые миры и старый PolyMC1.16.5 не перезаписывались. [Квитанция](verification/polymc-1.3.4.4-installation.json). [Gate](verification/combat-fixes-1.3.4.4.json). [Логи](verification/combat-1.3.4.4/). [Balance CSV](verification/combat-1.3.4.4/TROPHY_BALANCE.csv).

## Production files

- `build.gradle`
- `gradle.properties`
- `src/main/java/org/slavicmyths/armorer/ArmorerRecipe.java`
- `src/main/java/org/slavicmyths/bandit/NightingaleDagger.java`
- `src/main/java/org/slavicmyths/client/ClientSetup.java`
- `src/main/java/org/slavicmyths/client/RpgClient.java`
- `src/main/java/org/slavicmyths/client/TugarinPairClient.java`
- `src/main/java/org/slavicmyths/combat/CombatTargeting.java`
- `src/main/java/org/slavicmyths/combat/DisorientationEffect.java`
- `src/main/java/org/slavicmyths/combat/LikhoStaffItem.java`
- `src/main/java/org/slavicmyths/combat/RareCombat.java`
- `src/main/java/org/slavicmyths/combat/RareWeapons.java`
- `src/main/java/org/slavicmyths/combat/ThrowingWeaponItem.java`
- `src/main/java/org/slavicmyths/combat/TugarinPair.java`
- `src/main/java/org/slavicmyths/combat/WeaponProjectile.java`
- `src/main/java/org/slavicmyths/depth/PoolSpear.java`
- `src/main/java/org/slavicmyths/depth/ThrownNet.java`
- `src/main/java/org/slavicmyths/depth/VodyanoyNetItem.java`
- `src/main/java/org/slavicmyths/item/SpearItem.java`
- `src/main/java/org/slavicmyths/rpg/RareRuneRules.java`
- `src/main/java/org/slavicmyths/rpg/RareRuneRuntime.java`
- `src/main/java/org/slavicmyths/rpg/RuneVisual.java`
- `src/main/resources/assets/slavicmyths/lang/en_us.json`
- `src/main/resources/assets/slavicmyths/lang/ru_ru.json`
- `src/main/resources/assets/slavicmyths/models/item/tugarin_sword.json`
- `src/main/resources/assets/slavicmyths/models/item/tugarin_sword_held.json`
- `src/main/resources/data/slavicmyths/tags/entity_type/combat_protected.json`
- `src/main/resources/data/slavicmyths/tags/entity_type/control_immune.json`

## Test files

- `tools/combat-fix-test-resources/data/slavicmyths_combat/structure/empty.nbt`
- `tools/combat-fix-tests/org/slavicmyths/verify/CombatFixGameTests.java`
- `tools/rare-rune-tests/org/slavicmyths/verify/RareRuneOffline.java`
- `tools/rare-tests/org/slavicmyths/verify/RareWeaponGameTests.java`

Команда повторного server gate: `gradlew.bat -PcombatRuntimeCheck runCombatFixes`; validator: `python tools/verify_combat_1344.py`. Старые16rare fixture cases синхронизированы с теми же16 уже проверенными текущими cases; test sourceSets opt-in, не main/JAR.
