# #149 — исходниковый аудит аксессуаров

38 предметов: объединение 30 записей magic/charms и 27 Curios ID. Проверены 537 рецептов, 450 loot tables (61 entity), 1069 Java-файлов. Это статический аудит; игровые запуски: 0. Этапы прохождения — оценка по зависимостям, не измерение времени получения.

Полные рецепты, вложенные условия/веса loot, ссылки на выдачу в коде, SHA256, модели и рендереры: inventory.json / INVENTORY.tsv / source-hashes.json. Отсутствие прямого entity loot не означает отсутствие награды через сюжетный код. Двойники hunter_charm/obereg_ohotnika используют один детектор; не объединены.

| ID | Слот/тип | Эффект до #151 | Решение |
|---|---|---|---|
| amber_charm | offhand | fire damage x.85; charm_power x.80 | KEEP |
| amulet_two_winters | inventory/use | inventory passive +.2 knockback resistance in defensive mode; use in Nether only:10HP shield160ticks or r5 wave6/3 boss damage, CD2400; shift-use switches mode | KEEP |
| black_wing_amulet | necklace | two allied ravens; default400ticks; successful activation CD3000ticks (configurable) | KEEP |
| depth_amulet | necklace | +1air every4ticks in water while air>0, capped at max; no swim speed | ART_REVIEW |
| fading_flame_charm | consumable/use | consumes one item when GUARD absent; GUARD duration WARD_SECONDS config x20 ticks | KEEP |
| finist_quiver | belt | 12 magic-arrow charges; +1charge/160ticks equipped, used by FinistBow; persists ItemState | KEEP |
| forest_charm | offhand | forest_spirits incoming damage x.85; charm_power x.80 | KEEP |
| frost_mirror | necklace | player ice double15HP, life600ticks, CD3600; safe-space check | KEEP |
| gift_alyosha_belt | belt | safe-side dodge from selected heavy windup within4blocks targeting player; CD800ticks | KEEP |
| gift_dobrynya_ring | ring | successful shield block opens80tick counter window; next hostile melee x1.75; CD240ticks | KEEP |
| gift_finist_amulet | necklace | every80ticks detects up to30 visible hostile entities within18; windup warning CD20ticks | KEEP |
| grave_ward | charm | incoming damage from undead LivingEntity x.91; bypass_invulnerability excluded | BALANCE_REVIEW |
| grivna | necklace | incoming damage not bypassing armor x.96; bypass_invulnerability excluded | BALANCE_REVIEW |
| hunter_belt | belt | projectile damage x1.10 only against WildlifeEntity | BALANCE_REVIEW |
| hunter_charm | use | right-click invokes HuntItems.detect; item cooldown400ticks | KEEP |
| invisibility_cap | head | 20tick fade; duration160ticks; broken by hurt/swing/sprint/use for100ticks; after expiry cooldown600ticks | ART_REVIEW |
| lunula | necklace | night-only LivingEntity source incoming damage x.95; bypass_invulnerability excluded | BALANCE_REVIEW |
| miners_goggles | head | night vision refreshed520ticks when remaining<500; external night vision preserved/restored | KEEP |
| mor_ward | quest/use | right-click consumes ordinary copy, applies24000tick Mor ward: mor_block damage x.5; special GuardianWard196 copy not consumed | KEEP |
| obereg_ohotnika | use | HuntItems.Role.DETECTOR; right-click bounded detection; no Curios passive | BALANCE_REVIEW |
| obereg_padayuschey_zvezdy | necklace | fallDistance>=5.5 and verticalSpeed<-.15 -> SlowFalling100ticks, clears fall distance; CD1200ticks; excludes water/flying/passenger | KEEP |
| odnoglazyy_obereg | necklace | 20% chance halve finite harmful duration1..72000; CD600ticks; excludes BAD_OMEN and protected burial curses; proc recursion guard | KEEP |
| perun_charm | offhand | lightning damage x.70; charm_power x.65; separate armor set multiplier retained | KEEP |
| perun_ring | ring | direct melee +2 raw damage CD60ticks; lightning incoming x.65 | ART_REVIEW |
| poyas_tugarina | belt | after40ticks sprinting +.2 knockback resistance, refreshed20ticks; melee knockback x1.15; disabled swimming/flying | KEEP |
| prospectors_ring | ring | mining speed x1.12 for correct pickaxe on c:ores, multiplied with existing mining runes | KEEP |
| resin_ring | ring | heal1HP/100ticks at food>=18 when hurt, exhaustion2; duplicate copies do not multiply callback | ART_REVIEW |
| retribution_charm | charm | consumed on otherwise lethal non-void death:1HP,40invuln ticks, ResistanceII60ticks; attributed50 damage to living non-allied killer | ART_REVIEW |
| ring_of_grey_pack | ring | two wolves20HP/6attack; arrival10ticks; expiry810ticks from activation; player-owned CD1800ticks; no unsafe spawn | KEEP |
| stone_path_amulet | necklace | final fall damage x.75 | KEEP |
| traveler_charm | offhand | fall damage x.85; charm_power x.80 | KEEP |
| trophy_skim_necklace | necklace | hostile melee/projectile damage x1.10 if target HP<30%; no cooldown | BALANCE_REVIEW |
| underground_guard_belt | belt | +1armor and +.10 knockback resistance while equipped; independent modifier IDs | KEEP |
| veles_amulet | necklace | Wildlife aggression threshold x.55; Rusalka charm buildup x.5 | ART_REVIEW |
| volchiy_poyas | belt | night movement +8%, full moon+11%, none in daytime | KEEP |
| volkhv_amulet | component | plain Item; no dedicated equipment/use hook found in all Java source; not a wearable passive | INVESTIGATE |
| warding_charm | component | plain Item; ritual/path offering ingredient and ritual result; no wearable passive | KEEP |
| zolny_obereg | necklace | fire damage x.8; newly observed burning duration ceil(x.6) | KEEP |

## Отображение на персонаже

| ID | Фактическая регистрация | Проверка в игре |
|---|---|---|
| black_wing_amulet | NONE: no dedicated registration/layer found | NOT_RUN / OWNER_PENDING |
| depth_amulet | NONE: no dedicated registration/layer found | NOT_RUN / OWNER_PENDING |
| finist_quiver | CuriosAccessories -> FinistQuiverRenderer | NOT_RUN / OWNER_PENDING |
| frost_mirror | NONE: no dedicated registration/layer found | NOT_RUN / OWNER_PENDING |
| gift_alyosha_belt | FolkEquipmentLayer body | NOT_RUN / OWNER_PENDING |
| gift_dobrynya_ring | NONE: no dedicated registration/layer found | NOT_RUN / OWNER_PENDING |
| gift_finist_amulet | FolkEquipmentLayer body | NOT_RUN / OWNER_PENDING |
| grave_ward | NONE: no dedicated registration/layer found | NOT_RUN / OWNER_PENDING |
| grivna | NONE: no dedicated registration/layer found | NOT_RUN / OWNER_PENDING |
| hunter_belt | FolkEquipmentLayer body | NOT_RUN / OWNER_PENDING |
| invisibility_cap | FolkEquipmentLayer head + invisibility override | NOT_RUN / OWNER_PENDING |
| lunula | NONE: no dedicated registration/layer found | NOT_RUN / OWNER_PENDING |
| miners_goggles | MiningGogglesRenderer | NOT_RUN / OWNER_PENDING |
| obereg_padayuschey_zvezdy | CuriosAccessories -> HuntAccessoryRenderer STAR | NOT_RUN / OWNER_PENDING |
| odnoglazyy_obereg | CuriosAccessories -> BossAccessoryRenderer EYE | NOT_RUN / OWNER_PENDING |
| perun_ring | NONE: no dedicated registration/layer found | NOT_RUN / OWNER_PENDING |
| poyas_tugarina | CuriosAccessories -> BossAccessoryRenderer BELT | NOT_RUN / OWNER_PENDING |
| prospectors_ring | NONE: no dedicated registration/layer found | NOT_RUN / OWNER_PENDING |
| resin_ring | NONE: no dedicated registration/layer found | NOT_RUN / OWNER_PENDING |
| retribution_charm | NONE: no dedicated registration/layer found | NOT_RUN / OWNER_PENDING |
| ring_of_grey_pack | NONE: no dedicated registration/layer found | NOT_RUN / OWNER_PENDING |
| stone_path_amulet | NONE: no dedicated registration/layer found | NOT_RUN / OWNER_PENDING |
| trophy_skim_necklace | FolkEquipmentLayer body | NOT_RUN / OWNER_PENDING |
| underground_guard_belt | NONE: no dedicated registration/layer found | NOT_RUN / OWNER_PENDING |
| veles_amulet | FolkEquipmentLayer body | NOT_RUN / OWNER_PENDING |
| volchiy_poyas | CuriosAccessories -> HuntAccessoryRenderer BELT | NOT_RUN / OWNER_PENDING |
| zolny_obereg | CuriosAccessories -> HuntAccessoryRenderer CHARM | NOT_RUN / OWNER_PENDING |

34 функциональных аксессуара предназначены для новой вкладки. Четыре исключения: mor_ward, fading_flame_charm — расходуемые сюжетные средства; warding_charm, volkhv_amulet — компоненты без найденной экипируемой пассивной механики.

Нет утверждённых художественных референсов для pool_ring, bath_ward, harvest_ring, podvey_belt, leshy_necklace, spark_ring. #152/#154/#155 не могут считаться реализованными. #153 требует визуальных проверок положения/брони; таблица выше подтверждает только исходниковые подключения.
