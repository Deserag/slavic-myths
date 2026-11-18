# Исполнительская интеграционная карта 2.2.13–2.2.14 — ID, сохраняемое состояние, API и проверяемый результат

Прочитать вместе с #365/#366/#367 и 00–03 в этой папке. Это **перечень целевых ID и сценариев**, а не утверждение, будто файлы уже зарегистрированы. Ветка ONLY hell, перед каждым новым RegistryObject проверить коллизии по actual HEAD. Если конфликт ID — НЕ переименовывать самовольно; документировать в Issue как технический BLOCKED и предоставить кандидат без записи в старые данные.

## 1. Итоговые ID (предпочтительная схема — не переименовывать после релиза)
| Тип | ResourceLocation | Назначение |
|---|---|---|
| EntityType | slavicmyths:cursed_mytar | Босс мытарь, НЕ другие мобы 2.1 |
| EntityType | slavicmyths:bauk | Босс зверь |
| Item | slavicmyths:mytar_seal | физический трофей для городского обмена |
| Item | slavicmyths:mytar_sickle | отдельный короткий серп Мытаря, 20% rare loot, материал |
| Item | slavicmyths:mytar_chain | отдельная цепь, гарантированный материал |
| Item | slavicmyths:okovny_sickle | итоговое комбинированное оружие серп на цепи |
| Item | slavicmyths:mytar_tax_purse | Curios belt аксессуар, 15% |
| Item | slavicmyths:bauk_fang | гарантированный городской трофей, НЕЛЬЗЯ расходовать на оберег |
| Item | slavicmyths:bauk_hide | гарантированный материал x1 |
| Item | slavicmyths:bauk_claw | гарантированный материал x2 |
| Item | slavicmyths:bauk_hide_charm | Curios charm, рецепт |
| Loot table | slavicmyths:entities/cursed_mytar | базовый boss loot (способ пути сверить с API/действующими ресурсами) |
| Loot table | slavicmyths:entities/bauk | базовый boss loot |
| Recipe | slavicmyths:okovny_sickle | 1 mytar_sickle + 1 mytar_chain + 1 minecraft:iron_ingot |
| Recipe | slavicmyths:bauk_hide_charm | 1 bauk_hide + 2 bauk_claw + 1 minecraft:leather |

Критические существующие ID: **slavicmyths:ancient_coin** (уже существует) и **slavicmyths:sickle** (уже существует); НЕ добавлять вторую монету и НЕ переписывать рабочий серп.
Два новых boss entity НЕ должны ссылаться на generic slavicmyths:brown_bear, slavicmyths:pesigolovets или cursed_volkolak как свой registry ID. Призрачные силуэты Мытаря — client VFX/model layers, без отдельного entity ID/loot.

## 2. Логическая декомпозиция (пакеты / Java классы — рекомендуемые имена, не прежние существующие)
- netherboss22/NetherBossSpawns: deterministic candidate anchor 40x40 chunks, spawn preflight, no ticket.
- netherboss22/NetherBossWorldData: SavedData region -> { dimension, gridX,gridZ, bossType, anchorPos, bossUUID, regionState, lastKnownPos, reservationVersion }; honor read/write concurrency and old worlds.
- netherboss22/NetherBossClaims: claimId UUID, bossType, payer/qualified participants, issueState, redeemedAt, usedNonce, recovery mailbox. Casefold UUID/idempotency. Snapshots/transaction semantics.
- netherboss22/MytarDebtLedger: encounterUUID + victimUUID -> stolenAmount, refundState, bonusState, server-committed inventory debits; crash-safe and no double refund. Separate from ordinary boss claim ID.
- netherboss22/CursedMytarEntity, netherboss22/BaukEntity: distinct boss AI/phase and attack states, save/reload. AI state stored server-side; client receives only rendering fields.
- client: named model + renderer + animation controller, original textures, sound events, item geometry; never load client-only classes on dedicated server.
- trading/quest integration: new entries **inside actual 2.2.12** city services, not new duplicate elder entity or second fake NPC. Keep existing quest/guide APIs compatible. Use source-based checked APIs, do not make up nonexisting classes.

## 3. Persistent state / transaction shape
regionState = UNRESERVED | RESERVED | ALIVE | DEFEATED | INVALID. BossUUID stable while ALIVE; when unload chunk, no new entity UUID; re-materialization only after verifying persisted state + absence of active entity on real chunk load, under single-threaded world authority.

Boss transient attack state stores actionId, actionStartTick/remainingTicks, targetUUID, cooldowns, phase, anchor, hook target/link. On load, incomplete attack **cancels safely**, with protective cooldown (no duplicate contact after reload). BOSS death event marks DEFEATED once before issuing rewards; never both entity loot + event for same non-stackable claim.

Mytar per-victim escrow sample: {encounterId: UUID, victimId: UUID, stolen: integer [0..24], refundIssued: boolean, bonusIssued: boolean}. Use server SavedData with transactional intent/pending changes rather than lose coins in crash between inventory mutation and SavedData write. On reload reconciliation: no path can both restore victim coins and preserve a second stolen copy. Boss healing only after successful debit + durable intent.

Boss claim sample: {claimId UUID, bossId, earnedByPlayerUUID, initialState ISSUED, redeemedState UNUSED|REDEEMED, issuedTime, redeemedTime, settlementId optional, deliveryState PENDING|DELIVERED}. Atomically validate item claim before deleting and paying rewards, no client-driven time/rand/quantity or ability to forge owner/nonce. Issued token can pass between players, but no duplicate redemption. Ownership of kill history remains personal. Unknown/legacy token missing claimId -> no silent payout; explain error and keep item, not delete it.

## 4. Spawn and quest deterministic acceptance examples
Given same world seed, completed chunks and boss region, anchors should be identical over reload. Confirm that "find boss" does not itself generate loaded chunks, does not claim an already-dead anchor and does not return a position inside a lava ocean/blocked wall. If no reachable alive target is recorded, return a truthful message and schedule a future-region candidate as allowed by shared contract; only after actual placement produce numeric X/Z and radius 48. **Never print fake coordinates for a reservation that has not spawned and been validated.** The shared 01 doc allows future reservation; this section clarifies that a future reservation produces *pending search*, not immediate coordinates.

On naturally encountering, server record registers that same UUID and anchor for later quest discovery, so spontaneous first-kill proof exists even before village. Nearby town guard cannot be dragged into the encounter by roaming NPC; hard no-spawn zone around town 192 blocks.

## 5. QA traceability / required executable evidence
| ID | Requirement | Real verification |
|---|---|---|
| S-01 | Natural spawn (both) | 5+ distinct seed: fresh survival Nether, real mob seen and attacked w/o /summon; biome/pos/time log |
| S-02 | Quest coordinates true | Compare numeric displayed X/Z/r48 vs persisted registered boss, no /locate worldgen; lost target reports pending |
| S-03 | Pre-city trophy | Legit kill -> inventory item -> find city -> exchange (no quest precondition), after logout |
| S-04 | Unique MP reward | 2-person damage >=5% each -> independent claim, two simultaneous clicks -> one redemption per token |
| S-05 | Offline/overflow | Legit participant logged off/full inventory -> protected delivery exactly once |
| M-01 | Mytar 1350 HP and threshold 607.5 | Server observed phase once, exact scaled and unscaled health; bossbar/client sync |
| M-02 | Hook projectile | 14-block cast, shield miss, wall, hit, pull, recovery, no collision into lava |
| M-03 | Theft 1–2 with cooldown | Success, zero coins, 24 cap, concurrent hit, actual inventory and heal measured |
| M-04 | Crash-safe refund | Logout, player death, boss death, boss reset, partial transaction replay: balance conserved |
| M-05 | Rare loot + weapons | 1000+ kill sims 20%, pity5, chain guarantee, recipe/actual held model and item CD |
| M-06 | Ghost traversal | Short valid wall only, bedrock/unloaded/claimed/crowded invalid; no free hit while intangible |
| B-01 | Bauk 1800/630 HP | Actual health, enraged animation and attack CD change exactly once |
| B-02 | Distinct bear model | source tree default bear unchanged; compare BAUK source model in 4 actual views against ordinary bear |
| B-03 | Bite/charge/slam | windups, valid collisions, path safety, stagger40s gating, shield/MP tests |
| B-04 | Loot and charm | Fang/hide/claws guaranteed, full recipe, charm per-player 60s CD persisted, remove/dispel |
| A-01 | Art/audio | source .bbmodel/UV + real textures, before/after source renders, in-game frames, OGG event→file→subtitle mapping and licenses |
| A-02 | Regression/ownership | No unrelated code/IDs or city grid changes, Java21 clean build, dedicated server+2 clients, old world backups and JAR hash |
| A-03 | Runtime authority | isolated display only under AGENTS.md, real Minecraft tests vs NOT_RUN, owner acceptance only |

Required report row: **REQUIREMENT | REAL_SOURCE | PLAYER_PATH | TEST | EXPECTED | EVIDENCE (seed, x/z, SHA, logs/video) | PASS/FAIL/BLOCKED/NOT_RUN**. One actual client screenshot DOES NOT prove rig/attack correctness; one offline simulation DOES NOT prove a natural spawn.

## 6. Unambiguous hand-off to Codex or another chat
Read #365 #366 #367 and all 00–04 docs plus three SVGs in hell, inspect actual HEAD and docs/requirements before any implementation. One writer per touched source file, don't overwrite parallel 2.1/2.2.9–12 changes. Separate checkpoints for shared foundation / each boss / QA. Keep main unchanged, no force push. After approved implementation, build/check and install ONLY in authorized test profile with proof of old JAR backup, new JAR SHA and no dual mod copies; GUI testing must not interfere with owner's active desktop. Never claim done on missing OGG or no actual gameplay.

For missing locally licensed vocal assets, the executor MAY use already verified legally licensed **existing mod/vanilla footsteps and metal impacts** if their licenses and target calls permit; must provide distinct real sound files and subtitles. If complete character-specific audio cannot be sourced lawfully, publish source map + BLOCKED_NEEDS_AUDIO_SOURCE and continue nonaudio scope, **do not ask owner to invent/download unlabeled files and do not substitute pure-tone filler**, do not mark task finished. This fallback is deliberately explicit to avoid unnecessary implementation questions.
