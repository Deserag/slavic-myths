# #327 и 2.0.4 — матрица фактической реализации

Статус IN_PROGRESS, Issues остаются OPEN. PASS ниже ограничен указанным тестом; рядом отдельные строки runtime. Исходные clauses сохранены в requirement-clauses.json; они не становятся принятыми из-за количества assert-ов.

| REQUIREMENT | REAL_SOURCE / PLAYER_PATH | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|---|
| Сохранить предыдущие правки и hell | #327, AGENTS; существующие сохранения | resume-preserved.patch; Peaceful/UUID изменения сохранены | PASS source |
| 3 boss geometry/UV/poses | #256/#266/#317/#318; естественный бой | NavyaMesh, nether_boss327_geometry.py, CinderModel; native-before/after/models.json, три PNG и GIF | PASS native finite vertices/UV; не ART |
| Точное сравнение 14 оригиналов | reference handoff | REFERENCE_AUDIT.json | BLOCKED_REFERENCE |
| Четыре природных семейства + polished/forms | #289/#290; добыть/строить | HallsBlocks, halls_assets.py, textures/models/loot/recipes/lang/catalog, generator repeat | PASS packaged resources |
| Mining/recipe/support/orientation/negative | #290; добыть без OP | OWNER_SMOKE_CHECKLIST | NOT_RUN |
| S1–S6 и две дороги к арене | #291; пройти и вернуться | HallsGeometry + LostHallsRegionTopology, 20 heightfields, 32×32 cell tests in final-gates.log | PASS offline geometry |
| Реальные floor/headroom/сохранность vanilla | #291/#294; исследовать | HallsWorldgen Terrain, verifyRegion, chunk clipping and whitelist | NOT_RUN generated worlds; source guard present |
| 5 species /4 variants | #292; встретить/сражаться/кормить/приручить | HallsContent/HallsMob/HallsMesh/HallsClient, 5 entity IDs, sheptun_0..3, native exports | PASS compile/assets; AI runtime NOT_RUN |
| HP60/90/30/60/60, roles, no passive kill loot | #292/#301 | HallsMob, empty neutral entity loot, tame vanilla UUID data | NOT_RUN natural spawn/combat/relog/MP |
| Материалы, инструменты, spores/echo | #301; drops → craft → use | HallsUtility/HallsMob, recipe JSON, room metadata, no new ore | PASS source/resources; functional runtime NOT_RUN |
| Soul chances capped and existing IDs | #301 | native LevelBasedValue.CODEC in Nether327Models; Looting100 remains≤40%/5% | PASS codec/math, runtime loot NOT_RUN |
| Постень production entity/4 masks/casts | #293; естественная арена → бой | PostenMob/PostenRenderer/HallsMesh, entity registration, mask ring | PASS compile/native; combat NOT_RUN |
| 650→260→4×65, global UUID isolation, corrupt save | #293 | PostenEncounters and LostHallsOffline final-gates.log | PASS pure journal/NBT; real relog NOT_RUN |
| Body/controller clone restore, late viewers, wall transit | #293 | PostenMob actual loaded-safe bounded search | NOT_RUN real entity lifecycle/bossbar/MP |
| Durable rewards x1, no reassignment by journal rollback | #293/#302/#321 | BossRewardAuthority actual file pin/reload/conflict/corrupt tests, BossRewardInbox | PASS file authority unit tests; process crash/MP/full inventory in game NOT_RUN |
| Руна: chest, 80t invis,3×120t decoys,1500t cooldown | #303; босса победить→install→activate | ShadowFace/ShadowDecoy, Runes/RareRuneRuntime/RuneRework | PASS compile/source; behavior/persistence/MP NOT_RUN |
| Чужая invis/pet/PvP/no copy item | #303 | own lease, decoy owner targeting/no inventory | NOT_RUN concurrent effects/combat |
| 4 boss heads, existing zharenitsa ID | #302/#321; получить→floor/wall→сломать | TrophyHeads/TrophyHeadBlock/TrophyHeadsClient, source subtrees + loot routes | PASS IDs/assets/native bounds |
| New3 head rotated bounds and cosmetic poses | #302 | 2,396,736 native vertex checks in head-bounds-gate/final-gates | PASS native only |
| Old6 head regression | #302 | verifyLegacyHeadsRegression 6,840,695 checks | PASS native only |
| Floor/wall drop exactly1, support/explosion/piston/relog | #302/#321 | owner checklist | NOT_RUN actual world and migration |
| Magma Peaceful boss discard, neutrals remain | #327 | IslandsContent/IslandsMob server guards, old offline island suite | PASS source/offline; actual difficulty switch/bossbar NOT_RUN |
| Real biome registration and 8 classifications | #294 | lost_halls.json/NetherParametersMixin/HallsBiomeSourceMixin/HallsClimate; native-climate.json | PASS native sampler only |
| 5 seeds×4windows natural terrain/arena/spawn/performance | #294 | 53 climate candidate regions ≠ generated arenas | NOT_RUN; no guaranteed coverage claim |
| Minecraft1.21.1/NeoForge21.1.255/Java21 resource JAR | AGENTS/requirements13 | final-gates.log, resources.json, BUILD_DELIVERY.json | PASS if matching manifest; warnings retained |
| No client/server/GameTest launch or fabricated runtime | AGENTS | commands only Gradle build, offline JVM, image renderer and resource audit | PASS: zero game launches |
| Exact art/sound/owner acceptance | AGENTS/#327 | original references unavailable; quarantine untouched | BLOCKED_REFERENCE / NOT_RUN owner |
| Ocean-only islands worldgen | #313/#320/#323 out of current priority | existing islands integration not represented as complete | NOT_RUN / WORLDGEN_NOT_INTEGRATED |
| PolyMC installed path/SHA/backup/no double JAR | AGENTS | current candidate not installed, previous installation preserved | POLYMC_NOT_INSTALLED |

Ни один gameplay-test не PASS. Gate results не означают готовность релиза; FIELD Status GitHub Project не менялся.
