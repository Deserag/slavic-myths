# Текущая приёмка — 29.10.2026

Production current: `e760aabb10d26b0428cc5f6ff50acdf15926b714`, 2.1.6-HELL; ранние пятиseed прогоны на a5def9a9, gameplay classes равны.
JAR SHA256: `51ce73c42243bb48dfaf5aabaab8ba6003ff09d8be39f375b3bd171cffd9ca95`.
Новая тестовая реализация не является изменением игрового дизайна или приёмкой владельца.

| REQUIREMENT | REAL_SOURCE | PLAYER_PATH | TEST / EXPECTED | EVIDENCE | RESULT |
|---|---|---|---|---|---|
| Сохранить изменения/ветку hell | git/worktree/status | прежние миры и ID | fast-forward без merge main | REPORT.md | PASS |
| Актуальный production JAR | gradle.properties/build.gradle | запуск Testing | Java21 clean build; ресурсы/классы | 4m12s, verify_current_resources.py | PASS |
| #349 survival без стрел | combat/FieldBowItem.java | получить лук, убрать стрелы, натянуть | нет выстрела/расхода | bow349.log, creativeAndSurvivalAmmo | PASS |
| #349 creative без стрел | combat/FieldBowItem.java | отдельная диагностика creative | один выстрел, без износа, pickup CREATIVE_ONLY | bow349.log, три ID | PASS |
| #349 недотяг и расход | combat/FieldBowItem.java | натянуть короткий/тяжёлый/пекельный лук | недотяг ничего не расходует; полный один ammo/wear | bow349.log | PASS |
| #349 special/normal/Power | nethersmithing/InfernalBow.java, NetherWeaponEffects.java | получить лук; Shift полный натяг, повтор на cooldown | один projectile, урон/горение/износ, Power один раз | nativeBowSkillOrdinaryAndPower | PASS |
| #349 хват/GUI/натяг визуально | client/BowDrawClient.java, FieldBowClient.java | GUI и обе руки | реальные before/after кадры | изолированный GUI отсутствует | BLOCKED |
| #349 плащ/цвет/UV/glow/Steve/Alex | nethersmithing/client/NetherArmorClient.java, NetherArmorGlow.java | надеть царскую кирасу, движение/приседание | клиентский рендер без clipping/смены цвета | native 96030 проверяет только geometry/UV, не жалобу | BLOCKED |
| #298 точные preview и принятие | chud298-preview-20261027/APPROVED_REFERENCE.md | GUI/рука четырёх форм | 16×16 preview и одобрение до production PNG | утверждён подход; финальные PNG не приняты | OWNER_PENDING |
| #349 категории | CreativeCatalog/ModItemGroup/creative_catalog.tsv | вкладка heads, инструменты Чуди | source/native присутствуют, GUI нужен отдельно | productionJarRegistryRecipes; actual creative GUI NOT_RUN | NOT_RUN |
| #327/#333 manual Постень | losthalls/PostenMob.java | ручной spawn только диагностика | не исчезает в трёх измерениях | manualPostenDimensionsAndPeaceful | PASS |
| #327/#331 головы | trophy/TrophyHeads.java, TrophyHeadBlock.java | законный босс → предмет → размещение → разрушение | четыре BE/rotation/один drop | fourTrophyAnchorsAndRotation; legal boss campaign отдельно | PASS |
| #331 форма/эффекты голов | client/TrophyHeadsClient.java | пол/стена/GUI/idle/proximity | реальные клиентские кадры | source anchor test не доказывает visual | BLOCKED |
| #327 Peaceful | burningislands/IslandsMob.java | смена сложности, босс/нейтральные | тело босса исчезает, саламандра остаётся | manualPostenDimensionsAndPeaceful | PASS |
| #327 bossbar/helpers/reload | burningislands/IslandsMob.java | Peaceful/перезаход | нет orphan bar/helpers | первая проверка тела не покрывает все lifecycle | NOT_RUN |
| #334 суставы ползунов | mob332 + ordinary client models | наблюдать walk/stance/stop/attack | фактическая анимация восьми ног/угольного ползуна | source rigs не заменяют клипы | BLOCKED |
| #335/#336 19 вариантов | navya/blackcinder/losthalls/burningislands client | natural encounter | силуэты/UV/атаки в одном свете | требуется изолированный клиент и референсы | BLOCKED |
| Четыре биома, физические чанки | NetherParametersMixin/IslandsWorldgen | исследовать новые чанки | bounded native sampler и фактический floor | evidence/biomes-v2-*.log:20/20 на пяти seed | PASS |
| Полная survival встречаемость/плотность | biome spawn tables/worldgen | портал → пешее исследование | без OP, normal chunk loading | diagnostic forced chunks недостаточно | NOT_RUN |
| Руна магмы II TTL | burningislands/TemporaryTerrain.java | использовать руну на природном острове | 120t, точный original floor | realNetherBiomeAndOceanChunk | PASS |
| Руна II restart/unload/claims | TemporaryTerrain attachments | сохранить/перезайти; чужая территория | нет потери/перезаписи/дюпа | реальный process restart/claims не выполнен | NOT_RUN |
| Boss reward cross-player crash | losthalls/BossRewardAuthority.java, BossRewardInbox | законное убийство, полный инвентарь | exact once через реальный crash/player files | старые pure JVM cuts не являются Minecraft crash | NOT_RUN |
| 8 wearable/HP/speed/снятие | NetherArmor/NetherArmorEffects | рецепты → надеть/снять | HP35/full speed .12; после снятия20/.1 | armorAndSkillGuards | PASS |
| Сохранение компонентов smithing | NetherSmithingMixin/NetherSmithingRules | алмазная база с чарами/рунами | нет ремонта/потери данных | productionJarRegistryRecipes + native armor | PASS |
| Armor trim/смерть/dimension/relog/MP | NetherArmorEffects + client | сменить слот/измерение/войти снова | нет остающихся modifiers/сломанных trims | частичного equip теста недостаточно | NOT_RUN |
| 5 оружий/skills | NetherWeapons/NetherSkills/WeaponProjectile | catalysts → smithing → применить | guards/урон/износ/knife return | armorAndSkillGuards, projectile/status tests | PASS |
| Полные LOS/PvP/pets/MP оружия | NetherSkills.legal + CombatTargeting | атака союзника/чужого игрока | отказ/нет обхода cooldown | неполная матрица целей | NOT_RUN |
| Knife damage AND armor debuff | NetherSkills.knifeHit/NetherWeaponEffects | спецбросок по защищённой цели | Weakness и отдельный −2 armor без stack | flailAndKnifeStatusBudgets | PASS |
| Bow/flail настоящий безопасный огонь | TemporaryTerrain.fire | special hit на natural ground | реальные leased fire и отказ на чужом полу | fire-integration-v4.log: actual projectile hit и actual flail pulse создали4leased FIRE каждый; TTL/gamerule/неприродный floor отдельно | PASS |
| Печь fuel/menu/hopper/smelt/XP | infernalfurnace | изготовить/поставить/плавить | 5 чужих топлив отклонены;64 output; XP once | furnaceFuelSmeltNbt/furnace64AndXpOnce | PASS |
| Печь true restart/GUI/MP | InfernalFurnaceMenu/Tile | загрузить печь и перезайти | прогресс/инвентарь/XP сохранены | NBT roundtrip не равен restart | NOT_RUN |
| Руды/сера природная доступность | netherores source/features/loot | добыть в новых чанках | surface/heat/Fortune/Silk/vanilla regression | source и native formula не равны natural mining | NOT_RUN |
| Nature20 combat budgets | NETHER_CURRENT_ACTION_BUDGETS.csv | natural encounters | реальные damage/TTK каждого вида | 52 source action records, не замер боя | NOT_RUN |
| Aura ward active slot | WintersContent/WintersEvents/Curios | necklace → снять | inventory не защищает; aura очищается/возвращается | auraWardActiveSlotAndTwoWinters | PASS |
| 2.1.1–2.1.6 физические структуры/guards/loot | netherstructures | новые чанки → найти/сундук | 5 seed ×21 GameTest | seed-*.log:105/105 | PASS |
| Структуры survival/restart/MP/все пересечения | netherstructures | исследование/лут/перезаход вдвоём | нет дюпа, проходимость и соседние объекты | ограниченные fixtures не покрывают всё | NOT_RUN |
| Art originals/audio source | ASSET_PROVENANCE.tsv | увидеть/услышать контент | actual SHA-matched source, лицензированные OGG | локальный повторный поиск Downloads/attachments без совпадений | BLOCKED |
| 2.1.7/2.1.8/2.1.9/2.2/new bosses scope | owner instructions | не применимо | не начинать запрещённые задачи | production не менялся | PASS |
| PolyMC установлен, один JAR | Testing/.minecraft/mods | выбрать левый Testing профиль | build SHA==installed SHA, backup | REPORT.md; display name исправлен 2.1.6 | PASS |

PASS относится строго к названному тесту. Ни одна строка не означает принятие всей Issue. OWNER_PENDING не присваивает согласование. Все исходники указаны относительно `src/main/java/org/slavicmyths/`, если явно не указан документ.
