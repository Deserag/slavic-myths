# Классификация существ — #79, 1.9.7.32

Статус: IN_PROGRESS / OWNER_PENDING. Это инвентарь исходников и compiled hierarchy, **не runtime registry**. Реальных запусков: 0. Все контакты в мире UNVERIFIED.

Три независимые оси: природа даёт физиологию; intent вычисляется для текущего экземпляра; affiliation использует существующего владельца/команду/сцену. Hostile не означает войну со всеми hostile. Registry IDs, MobCategory и наследование не менялись.

Старый аудит: 69 основных + 42 вспомогательных. Текущий исходник: 114 типов (дополнительно три уже существовавшие регистрации Чуди, пропущенные старым индексом), 84 LivingEntity и 30 nonliving. Из 42 вспомогательных 12 living: 11 с природой и одна проекция; это не 42 самостоятельных моба.

## Физиология

| Nature | Кровотечение | POISON | IS_FIRE |
|---|---|---|---|
| HUMANOID / ANIMAL / MYTHICAL_BEAST | да | да | 1.0 |
| SPIRIT | нет | нет | 1.25 |
| UNDEAD / ELEMENTAL / CONSTRUCT | нет | нет | 1.0 |
| PLANT | нет | нет | 1.5 |

BLOODLESS запрещает кровь/POISON; старый bleed_immune запрещает только кровь. FIRE_AFFINITY заменяет множитель на 0.5, ICE_AFFINITY — на 1.5, без перемножения с природой. Конфликт affinity отклоняет аудит. Нативная fireImmune/фазовая неуязвимость сохраняется. PLANT предусмотрен контрактом, новые существа не добавлялись.

CUT, RARE_BLEED, WOLF_CUT и blood_mark проверяют допуск при назначении и тике. POISON проверяется при назначении и нативном applyEffectTick (исторический эффект прекращается); применение mixin в игре NOT_RUN. HARM, BAD_BLOOD, Мор и проклятия не считаются физиологическим ядом. Скриптовый poisonTicks Идолища оставлен его контроллеру.

Resolver использует восемь nature tags; кеширует только EntityType→nature и очищает кеш при TagsUpdatedEvent. Intent/owner/state не кешируются. Ванильные Animal/WaterAnimal имеют fallback только для namespace minecraft. Новый неизвестный mod ID проваливает inventory, игра получает безопасный UNCLASSIFIED, а не аварийное завершение.

## Динамическое поведение

Чудь: guard DEFENDER, гражданские PEACEFUL; жители-стражники и големы DEFENDER. Прокси NEUTRAL. NeutralMob — по состоянию гнева; нейтральный дух не становится общей угрозой деревне из-за чужого конфликта. Мор 1 — NEUTRAL, Мор 2 — HOSTILE; сюжетная защита и активная босс-фаза проверяются отдельно. Реальный Серый Волк остаётся сюжетным, WolfSupport — отдельный помощник. Владельцы богатырей и союзных воронов дают DEFENDER. Остальные legacy hostiles/Enemy — HOSTILE, прочие NEUTRAL. См. точный порядок CombatTargeting.intent/sceneProtected/bossMode.

Ниже **статические** nature/traits; динамические BOSS, SUMMONED, ложное тело Маринки и сюжетные состояния дополнительно вычисляются CreatureProperties. Ни один столбец не утверждает проверку спавна в игре.

| Entity ID | Living | Nature / роль | Static traits | Исходник регистрации |
|---|---|---|---|---|
| slavicmyths:alyosha_arrow | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/skim/SkimContent.java:16 |
| slavicmyths:alyosha_popovich | да | HUMANOID | — | src/main/java/org/slavicmyths/skim/SkimContent.java:14 |
| slavicmyths:ataman | да | HUMANOID | — | src/main/java/org/slavicmyths/registry/ModEntities.java:106 |
| slavicmyths:baba_yaga | да | HUMANOID | STORY_PROTECTED | src/main/java/org/slavicmyths/registry/ModEntities.java:126 |
| slavicmyths:bandit_archer | да | HUMANOID | — | src/main/java/org/slavicmyths/registry/ModEntities.java:103 |
| slavicmyths:bandit_fighter | да | HUMANOID | — | src/main/java/org/slavicmyths/registry/ModEntities.java:102 |
| slavicmyths:bandit_heavy | да | HUMANOID | — | src/main/java/org/slavicmyths/registry/ModEntities.java:104 |
| slavicmyths:bandit_senior | да | HUMANOID | — | src/main/java/org/slavicmyths/registry/ModEntities.java:105 |
| slavicmyths:bannik | да | SPIRIT | — | src/main/java/org/slavicmyths/registry/ModEntities.java:29 |
| slavicmyths:bear_cub | да | ANIMAL | — | src/main/java/org/slavicmyths/registry/ModEntities.java:34 |
| slavicmyths:boar | да | ANIMAL | — | src/main/java/org/slavicmyths/registry/ModEntities.java:36 |
| slavicmyths:bolotnik | да | SPIRIT | WATER_AFFINITY | src/main/java/org/slavicmyths/registry/ModEntities.java:45 |
| slavicmyths:brown_bear | да | ANIMAL | — | src/main/java/org/slavicmyths/registry/ModEntities.java:33 |
| slavicmyths:buried_volkhv | да | UNDEAD | — | src/main/java/org/slavicmyths/registry/ModEntities.java:111 |
| slavicmyths:carcass | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/textile/Textiles.java:18 |
| slavicmyths:carp | да | ANIMAL | — | src/main/java/org/slavicmyths/registry/ModEntities.java:96 |
| slavicmyths:chud_artisan | да | HUMANOID | — | src/main/java/org/slavicmyths/industry/ChudPeople.java:11 |
| slavicmyths:chud_guard | да | HUMANOID | — | src/main/java/org/slavicmyths/industry/ChudPeople.java:11 |
| slavicmyths:chud_resident | да | HUMANOID | — | src/main/java/org/slavicmyths/industry/ChudPeople.java:11 |
| slavicmyths:crayfish | да | ANIMAL | — | src/main/java/org/slavicmyths/registry/ModEntities.java:97 |
| slavicmyths:cursed_volkolak | да | MYTHICAL_BEAST | BOSS | src/main/java/org/slavicmyths/registry/ModEntities.java:118 |
| slavicmyths:defiled_spring_keeper | да | SPIRIT | BOSS, WATER_AFFINITY | src/main/java/org/slavicmyths/guardians/GuardianContent.java:14 |
| slavicmyths:dobrynya_nikitich | да | HUMANOID | — | src/main/java/org/slavicmyths/marinka/MarinkaContent.java:14 |
| slavicmyths:doe | да | ANIMAL | — | src/main/java/org/slavicmyths/registry/ModEntities.java:38 |
| slavicmyths:doghead_arrow | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/pesigolovtsy/DogheadContent.java:29 |
| slavicmyths:domovoy | да | SPIRIT | STORY_PROTECTED | src/main/java/org/slavicmyths/registry/ModEntities.java:19 |
| slavicmyths:earth_encounter_actor | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/earth/EarthContent.java:21 |
| slavicmyths:earth_servant | да | CONSTRUCT | EARTH_AFFINITY, SUMMONED | src/main/java/org/slavicmyths/earth/EarthContent.java:20 |
| slavicmyths:earth_spirit | да | SPIRIT | EARTH_AFFINITY | src/main/java/org/slavicmyths/earth/EarthContent.java:19 |
| slavicmyths:elder_vodyanoy | да | SPIRIT | WATER_AFFINITY | src/main/java/org/slavicmyths/registry/ModEntities.java:101 |
| slavicmyths:ember_clump | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/registry/ModEntities.java:119 |
| slavicmyths:extinguished_flame_guardian | да | ELEMENTAL | BOSS | src/main/java/org/slavicmyths/guardians/GuardianContent.java:13 |
| slavicmyths:finist | да | HUMANOID | — | src/main/java/org/slavicmyths/finist/FinistContent.java:14 |
| slavicmyths:finist_magic_arrow | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/registry/ModEntities.java:18 |
| slavicmyths:fire_serpent | да | ELEMENTAL | FIRE_AFFINITY | src/main/java/org/slavicmyths/registry/ModEntities.java:114 |
| slavicmyths:firebird | да | MYTHICAL_BEAST | FIRE_AFFINITY, STORY_PROTECTED | src/main/java/org/slavicmyths/firebird/FirebirdContent.java:17 |
| slavicmyths:flame_vial | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/sanctuary/SanctuaryContent.java:28 |
| slavicmyths:flying_broom | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/registry/ModEntities.java:124 |
| slavicmyths:flying_mortar | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/registry/ModEntities.java:125 |
| slavicmyths:forest_wolf | да | ANIMAL | — | src/main/java/org/slavicmyths/registry/ModEntities.java:35 |
| slavicmyths:furniture_seat | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/furniture/Furniture.java:18 |
| slavicmyths:gorynych_breath_cloud | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/gorynych/GorynychContent.java:12 |
| slavicmyths:gorynych_breath_shot | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/gorynych/GorynychContent.java:13 |
| slavicmyths:great_poloz | да | MYTHICAL_BEAST | BOSS | src/main/java/org/slavicmyths/poloz/PolozContent.java:11 |
| slavicmyths:grey_wolf | да | MYTHICAL_BEAST | STORY_PROTECTED | src/main/java/org/slavicmyths/registry/ModEntities.java:47 |
| slavicmyths:grey_wolf_support | да | MYTHICAL_BEAST | STORY_PROTECTED, SUMMONED | src/main/java/org/slavicmyths/quest185/Quest185Content.java:14 |
| slavicmyths:guardian_water_echo | да | ELEMENTAL | SUMMONED, WATER_AFFINITY | src/main/java/org/slavicmyths/guardians/GuardianContent.java:15 |
| slavicmyths:guardian_water_prison | да | ELEMENTAL | SUMMONED, WATER_AFFINITY | src/main/java/org/slavicmyths/guardians/GuardianContent.java:16 |
| slavicmyths:hot_stone | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/registry/ModEntities.java:32 |
| slavicmyths:idolishche_poganoe | да | MYTHICAL_BEAST | BOSS | src/main/java/org/slavicmyths/bogatyr/BogatyrContent.java:17 |
| slavicmyths:igosha | да | SPIRIT | — | src/main/java/org/slavicmyths/registry/ModEntities.java:30 |
| slavicmyths:ilya_muromets | да | HUMANOID | — | src/main/java/org/slavicmyths/bogatyr/BogatyrContent.java:16 |
| slavicmyths:kalin_common_soldier | да | HUMANOID | — | src/main/java/org/slavicmyths/kalin/KalinContent.java:17 |
| slavicmyths:kalin_elite_soldier | да | HUMANOID | — | src/main/java/org/slavicmyths/kalin/KalinContent.java:18 |
| slavicmyths:kalin_tsar | да | HUMANOID | BOSS | src/main/java/org/slavicmyths/kalin/KalinContent.java:15 |
| slavicmyths:kalin_war_horse | да | ANIMAL | — | src/main/java/org/slavicmyths/kalin/KalinContent.java:16 |
| slavicmyths:karachun | да | SPIRIT | BOSS, ICE_AFFINITY | src/main/java/org/slavicmyths/karachun/NightContent.java:11 |
| slavicmyths:karachun_blade | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/karachun/NightContent.java:13 |
| slavicmyths:karachun_raven | да | ANIMAL | SUMMONED | src/main/java/org/slavicmyths/karachun/NightContent.java:12 |
| slavicmyths:kartaus | да | HUMANOID | BOSS | src/main/java/org/slavicmyths/finist/FinistContent.java:15 |
| slavicmyths:kartaus_banner | да | CONSTRUCT | SUMMONED | src/main/java/org/slavicmyths/finist/FinistContent.java:16 |
| slavicmyths:kartaus_curse_anchor | да | CONSTRUCT | SUMMONED | src/main/java/org/slavicmyths/finist/FinistContent.java:17 |
| slavicmyths:kikimora | да | SPIRIT | — | src/main/java/org/slavicmyths/registry/ModEntities.java:26 |
| slavicmyths:kladovik | да | HUMANOID | — | src/main/java/org/slavicmyths/underground/UndergroundMobs.java:16 |
| slavicmyths:kladovik_spike | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/underground/UndergroundMobs.java:19 |
| slavicmyths:kladovik_stone | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/underground/UndergroundMobs.java:18 |
| slavicmyths:kot_bayun | да | MYTHICAL_BEAST | STORY_PROTECTED | src/main/java/org/slavicmyths/bayun/BayunContent.java:17 |
| slavicmyths:kurgan_bolt | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/registry/ModEntities.java:113 |
| slavicmyths:kurgan_druzhinnik | да | UNDEAD | — | src/main/java/org/slavicmyths/registry/ModEntities.java:109 |
| slavicmyths:kurgan_voevoda | да | UNDEAD | — | src/main/java/org/slavicmyths/registry/ModEntities.java:110 |
| slavicmyths:kuzya | да | SPIRIT | STORY_PROTECTED | src/main/java/org/slavicmyths/kuzya/KuzyaContent.java:16 |
| slavicmyths:large_podzemnik | да | HUMANOID | — | src/main/java/org/slavicmyths/underground/UndergroundMobs.java:17 |
| slavicmyths:leshy | да | SPIRIT | SILVER_VULNERABLE | src/main/java/org/slavicmyths/registry/ModEntities.java:22 |
| slavicmyths:likho_one_eyed | да | SPIRIT | — | src/main/java/org/slavicmyths/registry/ModEntities.java:120 |
| slavicmyths:marinka | да | HUMANOID | BOSS | src/main/java/org/slavicmyths/marinka/MarinkaContent.java:15 |
| slavicmyths:marinka_bolt | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/marinka/MarinkaContent.java:18 |
| slavicmyths:marinka_hex_focus | да | CONSTRUCT | SUMMONED | src/main/java/org/slavicmyths/marinka/MarinkaContent.java:16 |
| slavicmyths:marinka_rune | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/marinka/MarinkaContent.java:19 |
| slavicmyths:marinka_tur_minion | да | MYTHICAL_BEAST | SUMMONED | src/main/java/org/slavicmyths/marinka/MarinkaContent.java:17 |
| slavicmyths:mor_bolt | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/mor/MorContent.java:12 |
| slavicmyths:mor_flask_projectile | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/mor/MorContent.java:11 |
| slavicmyths:mor_zone | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/mor/MorContent.java:13 |
| slavicmyths:morozko | да | SPIRIT | BOSS, ICE_AFFINITY | src/main/java/org/slavicmyths/morozko/FrostContent.java:12 |
| slavicmyths:morozko_ice_bolt | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/morozko/FrostContent.java:15 |
| slavicmyths:morozko_ice_double | да | ELEMENTAL | ICE_AFFINITY, SUMMONED | src/main/java/org/slavicmyths/morozko/FrostContent.java:13 |
| slavicmyths:morozko_icicle | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/morozko/FrostContent.java:16 |
| slavicmyths:nav | да | UNDEAD | — | src/main/java/org/slavicmyths/registry/ModEntities.java:108 |
| slavicmyths:nightingale | да | HUMANOID | — | src/main/java/org/slavicmyths/registry/ModEntities.java:43 |
| slavicmyths:nochnitsa | да | SPIRIT | — | src/main/java/org/slavicmyths/nochnitsa/NochnitsaContent.java:23 |
| slavicmyths:ovinnik | да | SPIRIT | FIRE_AFFINITY | src/main/java/org/slavicmyths/registry/ModEntities.java:31 |
| slavicmyths:pesigolovets | да | HUMANOID | — | src/main/java/org/slavicmyths/pesigolovtsy/DogheadContent.java:28 |
| slavicmyths:pike | да | ANIMAL | — | src/main/java/org/slavicmyths/registry/ModEntities.java:95 |
| slavicmyths:player_ice_double | да | ELEMENTAL | ICE_AFFINITY, SUMMONED | src/main/java/org/slavicmyths/morozko/FrostContent.java:14 |
| slavicmyths:podvey | да | SPIRIT | — | src/main/java/org/slavicmyths/registry/ModEntities.java:115 |
| slavicmyths:podzemnik | да | HUMANOID | — | src/main/java/org/slavicmyths/underground/UndergroundMobs.java:15 |
| slavicmyths:polevik | да | SPIRIT | — | src/main/java/org/slavicmyths/registry/ModEntities.java:28 |
| slavicmyths:poloz_glob | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/poloz/PolozContent.java:12 |
| slavicmyths:poloz_stone_spike | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/poloz/PolozContent.java:13 |
| slavicmyths:poludnitsa | да | SPIRIT | — | src/main/java/org/slavicmyths/registry/ModEntities.java:27 |
| slavicmyths:rune_visual | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/rpg/RuneVisual.java:20 |
| slavicmyths:rusalka | да | SPIRIT | WATER_AFFINITY | src/main/java/org/slavicmyths/registry/ModEntities.java:99 |
| slavicmyths:serpent_projection | да | PROJECTION | BLOODLESS, PROJECTION | src/main/java/org/slavicmyths/registry/ModEntities.java:116 |
| slavicmyths:sivka_burka | да | ANIMAL | STORY_PROTECTED | src/main/java/org/slavicmyths/sivka/SivkaContent.java:18 |
| slavicmyths:skim_beast | да | MYTHICAL_BEAST | BOSS | src/main/java/org/slavicmyths/skim/SkimContent.java:15 |
| slavicmyths:stag | да | ANIMAL | — | src/main/java/org/slavicmyths/registry/ModEntities.java:37 |
| slavicmyths:thrown_net | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/registry/ModEntities.java:100 |
| slavicmyths:tugarin_stone | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/registry/ModEntities.java:122 |
| slavicmyths:tugarin_zmey | да | MYTHICAL_BEAST | BOSS | src/main/java/org/slavicmyths/registry/ModEntities.java:121 |
| slavicmyths:unresting_prince | да | UNDEAD | — | src/main/java/org/slavicmyths/registry/ModEntities.java:112 |
| slavicmyths:upyr | да | UNDEAD | — | src/main/java/org/slavicmyths/registry/ModEntities.java:107 |
| slavicmyths:vodyanoy | да | SPIRIT | WATER_AFFINITY | src/main/java/org/slavicmyths/registry/ModEntities.java:98 |
| slavicmyths:volkolak | да | MYTHICAL_BEAST | — | src/main/java/org/slavicmyths/registry/ModEntities.java:117 |
| slavicmyths:weapon_projectile | нет | NO_PHYSIOLOGY | — | src/main/java/org/slavicmyths/registry/ModEntities.java:127 |
| slavicmyths:zmey_gorynych | да | MYTHICAL_BEAST | BOSS | src/main/java/org/slavicmyths/gorynych/GorynychContent.java:11 |

## Вспомогательные типы и прежние механики

Все 42 строки: [AUXILIARY.csv](implementation/creatures-79/AUXILIARY.csv). Полные source facts: [auxiliary-source-facts.json](implementation/creatures-79/auxiliary-source-facts.json). Для каждого указаны owner/proxy, damageable, allowed targets, lifetime/cleanup и ссылка на реализацию; inherited/native означает делегирование указанному superclass, а не измеренный результат. Runtime всех строк UNVERIFIED.

[Legacy inventory](implementation/creatures-79/legacy-tags.json) сохраняет девять старых tags, их SHA, resolved members и source callsites, плюс minecraft:undead. class_mythical не равен MYTHICAL_BEAST. SilverCombat сохраняет прежний additive +1.5 и старый whitelist; ClassControl, BAD_BLOOD и Mor-контроллеры не переписывались.

Проверки и ограничения: [матрица #79](implementation/creatures-79/REPORT.md).
