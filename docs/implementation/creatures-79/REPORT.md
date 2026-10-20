# #79 — существа, отношения и физиология · 1.9.7.32

**IN_PROGRESS / OWNER_PENDING. Issue остаётся OPEN.** Агент не запускал Minecraft, PolyMC, сервер или GameTest (0 запусков). Ни runtime registry, ни загрузка mixin, ни игровые контакты не подтверждены. Это результат реализации и offline gates, не итоговая приёмка владельцем.

База: main `7e6d64d41b330d675c65cb10ac738f7a1091a9b8`; нет автоматического commit/push/PR. Предыдущий незакоммиченный #78 сохранён: [архив и SHA](PREVIOUS_PACKAGE.json); файлы lifecycle, геройские модели и настоящий GreyWolf совпадают с baseline #78. Общий git diff содержит предыдущий пакет: **diff только #79** — [implementation.patch](implementation.patch), [49 production paths / before-after SHA](changed-files.json). Исходные байты до #79 — `before/`, manifest — [baseline](baseline-source-sha256.json).

## Реализация

CreatureProperties: восемь primary nature tags, traits и кеш с очисткой на tag reload. CombatTargeting: отдельные dynamic intent, directed relation и существующие owner/team/story источники; не назначает всем hostile общую войну. NATIVE_HUNT и NATIVE_DAMAGE явно сохраняют делегирование действующим encounter/hurt, включая охоту на жителей и допустимые удары по сюжетным NPC.

Физиология: кровь для HUMANOID/ANIMAL/MYTHICAL_BEAST без BLOODLESS/legacy immunity; SPIRIT/UNDEAD/ELEMENTAL/CONSTRUCT/PLANT без крови и POISON. Старый bleed_immune не даёт новой poison immunity. Приём и исторические тики CUT/RARE_BLEED/WOLF_CUT, повторные pulse blood_mark фильтруются без стирания owner NBT. POISON использует Applicable и точечный PoisonMobEffect mixin; остальные магические эффекты не блокируются.

Огонь: один positive accepted IS_FIRE hook, SPIRIT 1.25, PLANT 1.5, остальные 1; FIRE affinity заменяет на .5, ICE — 1.5. Native fireImmune и phase immunity сохраняются. В существующий is_fire добавлен только подтверждённый marinka_fire. Firebird retaliation, rune generic magic и скриптовый poisonTicks не переименованы в огонь/яд по названию. [Карта источников](fire-source-audit.json).

Чудь: LivingEntity semantic threat вместо Monster-only; локальные home/group nodes и адресная помощь при нападении; bounded chase/return, прежние trade/flee. Native TargetGoal restriction расширен только стражам, без увеличения attribute/damage/HP. Village guards/golems используют общий semantic допуск, сохранены профессия/работа/щит/LOS/creeper/Tugarin. Scoped projectile/area adapters сохраняют реального caster/owner и PvP/team, у отражённого болта корректная атрибуция.

## Обязательная матрица

PASS ниже означает указанный offline результат, а не игровой тест.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Сохранить #78, main/чужие изменения | PREVIOUS_PACKAGE.json, baseline, before, preservation-check.json | PASS source |
| Все 69 approved nature | approved-primary.json, inventory-check.json | PASS source |
| Текущие регистрации всех registrars | source-inventory.json: 114; три существующих Chud вне старого аудита | PASS source |
| Актуальный runtime registry | без запуска; compiled hierarchy не заменяет registry | NOT_RUN |
| Все 42 aux, living/proxy/owner/damage/targets/lifetime | AUXILIARY.csv, auxiliary-source-facts.json, compiled-inventory.json | PASS inventory; runtime NOT_RUN |
| 8 nature + независимые intent/traits/affiliation | CreatureProperties, CombatTargeting; CREATURE_CLASSIFICATION.md | PASS native/source |
| Unknown ID/conflicting nature/affinity audit failure, без game crash | inventory negative cases, native helpers | PASS offline |
| Tag reload invalidates only immutable cache | CreatureProperties.tags; actual world reload отсутствует | PASS source; runtime NOT_RUN |
| Vanilla anatomy, undead, player, neutral wolves/golem | tags + namespace fallback; native policy | PASS source; contacts NOT_RUN |
| CUT / RARE_BLEED / WOLF_CUT admission и historical tick | native bytecode callsite checks + effect-source-inventory.json | PASS offline; runtime NOT_RUN |
| blood_mark admission и повторные pulses | ClassUltimateRuntime two callsites, native checks | PASS offline; runtime NOT_RUN |
| POISON только физиологический, исторический эффект | Applicable + mixin descriptor installed dependency | PASS offline; mixin loading NOT_RUN |
| HARM/BAD_BLOOD/Mor/curses не приравнены к яду | named effect filtering, effect inventory, preservation | PASS source; runtime NOT_RUN |
| Один IS_FIRE coefficient, affinity precedence/native immunity | native-checks.json + fire-source-audit.json | PASS offline; hits NOT_RUN |
| Legacy tags / +1.5 SilverCombat / ClassControl | legacy-tags.json, preservation-check.json, baseline SHA | PASS source; regression runtime NOT_RUN |
| Чудь локальная защита, LOS, chase/return, адресная тревога | ChudPerson + ENTITY_RELATIONS.md | PASS source; world NOT_RUN |
| Гражданские торговля/flee, невиновные не цель | ChudPerson guarded role goals; central predicate | PASS source; world NOT_RUN |
| Village guards/golems, прежние приоритеты и Тугарин | GuardBehavior/BossGolemTargetMixin/SettlementTargetSafety | PASS source; world NOT_RUN |
| Owner/team/PvP/dimension/creative/spectator | CombatTargeting + scoped projectile adapters | PASS source/native policy; multiplayer NOT_RUN |
| Dynamic Mor/boss/cleansing/story gates | sceneProtected/bossMode/intent; own encounter delegates | PASS source; phases NOT_RUN |
| Real GreyWolf не получает Mor hunt; геройский #78 не переписан | preservation-check.json; unchanged native hero controllers | PASS source; narrative runtime NOT_RUN |
| Нет новых global scans/loader/AI manager, registry/attrs сохранены | isolated diff, bounded query paths | PASS source; MSPT NOT_RUN |
| Java21 clean build | BUILD.log: BUILD SUCCESSFUL in 1m 1s | PASS |
| Native rules + compiled hierarchy + pinned target descriptors | native-checks.json: 327 checks | PASS offline |
| Resource/JAR/meta/mixin config parity, production excludes fixtures | jar-check.json | PASS offline |
| Runtime animation/story/target/AI, saves/relog, PvP/team/MP, FPS/MSPT | ENTITY_RELATIONS.md, manual matrix ниже | NOT_RUN |
| Ручная приёмка владельца | явное подтверждение отсутствует | NOT_RUN / OWNER_PENDING |
| Final PolyMC installation | polymc-unchanged.json; остаётся 1.9.7.30 | NOT_RUN / POLYMC_NOT_INSTALLED для .32 |

## Сборка и содержимое

Команда: `JAVA_HOME=C:/Program Files/Java/jdk-21.0.12` только для процесса; `gradlew.bat clean build -I tools/creatures79-native.init.gradle creature79Checks`. Запусков игры: **0**. Native harness не создаёт мир/сервер и не Bootstrap-ит игру. Java target 65.

Production JAR: `D:\slavic-myths\build\libs\slavicmyths-1.9.7.32.jar`

SHA256: `4614b9ef67280fa7433c84b2ebedacb17341385939a67fd58bac7ce37ce98947`

Source fingerprint: `ce7342586caf3007537f120c0be665909a9054220e90f807e03f3264cc892921` (uncommitted working tree; [full manifest](build-source-sha256.json)).

Проверено byte-for-byte: 7431 ресурсов, 1337 PNG, 1732 classes; ZIP integrity, JSON, NeoForge metadata version32, оба mixin config и отсутствие test fixtures. Текстуры/OGG не менялись относительно31; все 1375 OGG упакованы, это не утверждение качества или приёмки аудио прежних задач.

PolyMC не изменён: [точный installed path/version/SHA/source commit/backup/one-JAR](polymc-unchanged.json). Это не доставка финальной цепочки из незавершённой #78.

## Матрица ручной приёмки из ТЗ — все NOT_RUN

| Сценарий | Ожидаемый результат  Статус |
|---|------|
| Чудь-страж + vanilla zombie/skeleton + бандит + курганная нежить | Атакует доступную угрозу в зоне поселения, возвращается на пост, не бросает весь город/защиту  NOT_RUN |
| Чудь-страж + незаражённый нейтральный дух / очищенный хранитель / сюжетный NPC | Не начинает бой, не ломает диалог и нейтралитет  NOT_RUN |
| Нападение на жителя Чуди со стороны монстра / игрока | Локальная защита адресного виновника, без бесконечной агрессии ко всем игрокам  NOT_RUN |
| Деревенский страж и голем + реально враждебные vs neutral | Приоритет угроз, нет friendly-fire, сохраняется защита и особые правила vanilla  NOT_RUN |
| Кровотечение на зомби/курганную нежить/духа/огненного змея | Не накладывается, старые stack/tick не вредят; прямой удар остаётся  NOT_RUN |
| Кровотечение на человека/зверя/мифического зверя без `BLOODLESS` | Имеющийся эффект наносит урон по прежним лимитам  NOT_RUN |
| Яд на духа, нежить, живого человека; проклятие/Мор на духа | Биологический яд блокирован по типу; сюжетные эффекты/Мор не блокируются автоматически  NOT_RUN |
| Огненный урон на нейтрального духа, Морозко, огненного змея, Жар-птицу | Множитель 1.25/1.50 или существующий абсолютный native immunity по таблице; строго один раз  NOT_RUN |
| Высшее Мор-Лихо, обычное Лихо, очищенные стражи | Правильные динамические отношения при том же EntityType; без спонтанного боя Серого Волка  NOT_RUN |
| Серый Волк в/после испытания, Жар-птица, Сивка-Бурка, Яга, Баюн | Нет регрессии истории, непрошенного targetSelector, порчи мирных состояний  NOT_RUN |
| Призванные вороны/волки/двойники/миньоны | Союзность, owner, lifetime, despawn, отсутствие своих союзных попаданий  NOT_RUN |
| Сохранение, повторный вход, multiplayer/лаг | Нет потери состояния/изменения NBT и дорогостоящих глобальных сканов  NOT_RUN |

[Направленные пары по семействам и короткий порядок проверки](../../ENTITY_RELATIONS.md). Ни одна строка выше не заменяется количеством assert-ов. До runtime и принятия владельцем Issue OPEN.
