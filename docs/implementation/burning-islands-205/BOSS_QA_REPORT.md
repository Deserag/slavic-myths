# Повелитель магмы — IN_PROGRESS / BLOCKED

Build2.0.8-HELL; sourcef9beb28d; SHA256 `102952bb4c573fcb370c13fca2e702d19fed189d6d965d89aa26cdde677cc9ab`; runtime launches0. Полная матрица — ACCEPTANCE_MATRIX.md. Натуральной арены/вулкана в этой сборке нет.

| REQUIREMENT | SOURCE | TEST | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|---|---|
| Зарегистрирован EntityType/850HP source/renderer/модель | IslandsContent/IslandsMob/IslandsClient/4rigs | compile+JAR | resources-final/static-jar-results | PASS |
| Реальные850HP/neutral/bossbar/425phase/save/reload | IslandsMob | actual entity combat/NBT | без registered NeoForge game runtime не проверено | NOT_RUN |
|6кастов raw14/18/0/16/18/20; release23/27/27/25/31/37 | MagmaCombat | pure policy/timeline | offline-final.log | PASS |
| Каждая техника реально читается/попадает/уклоняется на Normal/Hard | IslandsMob.perform/telegraph;IslandsModel.pose | actual armor/player/camera | нет игровых запусков | NOT_RUN |
| Phase70t/core60t+35%spatial; death80t/readyAt | IslandsMob + journal | native readyAt bound + model poses; actual timing | code/pure tests, не бой | NOT_RUN |
| Любой distant attacker/bridge/roof/melee/allies/LOS | target/loadedSight/castVictims | two real players + ranged/vertical positions | prototype HANDS128, pursue32,16participant cap; не измерено | NOT_RUN |
| Natural dry crater island/moat/bridges/guarantee per connected biome | absent terrain/arena |5real seeds/survival | BLOCKED_OCEAN_BIOME_ARCHITECTURE | BLOCKED |
| No duplicate UUID/unknown claim/future or malformed journal | MagmaEncounters VERSION2 | actual native binaryNBT + negative cases | offline-final.log | PASS |
| All rewards/full inventory/same-recipient reconciliation | MagmaRewards/RewardRules/receipts | native stacks/NBT/aggregate negatives | offline admission PASS; actual death/drop/network not run | NOT_RUN |
| Cross-player crash atomicity | separate world journal/player files | crash/cold restart2players | A saved/world rollback/B repeat cut not proven safe | BLOCKED |
| Real head/unique jaw/shards/GUI static | TrophyHeads+existing renderer, native rigs/sprite | native5jaw poses + source/JAR | jaw.png/icons.png/JSON/source | PASS |
| Real floor/wall/facings/one drop/10heads/6–8hysteresis/MP/relog/FPS | TrophyHeadBlock/Tile/renderer | owner scene | не запускалось | NOT_RUN |
| Original5PNG/ModelPart match | references | actual hash+ownerA/B | bytes absent, SVG is not original | BLOCKED |
| Genuine distinct OGG/license/subtitle/callsite | AUDIO_SOURCE_MATRIX | real source/listening/JAR | записей нет, empty/filler не добавлялись | BLOCKED |
| No new gear/fake heart recipe/duplicate soul family | source and recipe audit | scoped diff/recipe IDs | heart future tooltip, soul reused | PASS |

Все части чистой сборки и offline guards не означают, что boss battle пройден. Обязательны проверка Normal/Hard в железе/алмазе/незерите; два клиента; projectiles/крыша/мосты; interruption and relog atP1/P2/core/cast/death; lava/full inventory/logout/cross-player crash; natural entry. Владелец подтверждает только фактический результат, Issues не закрывались.
