# #258 — test balance approved for executor selection, 24.10.2026

The owner explicitly authorized both non-Nether implementation tasks (#241/#258), the initial PolyMC update and selecting moderate test balance. Release/cleanup/Nether Issues remain outside this assignment. Latest main was inspected/fetched and its documentation/workflow changes merged; prior book/guide work is retained. These are test values, not owner acceptance of the final balance or artwork.

Four commissioned objects use six new registry IDs because the multitool has three real native forms. No crafting recipes, coin currency or netherite upgrade path is added.

Item | Source and cost | Mechanics / bounds
--- | --- | ---
chud_mining_lantern | miner:4 purified iron +8 amethyst, stock4 | client propagated block light14 in either hand, updates at4 ticks, max8 nearby already tracked players, no server/block state mutation
chud_master_pickaxe | miner:16 purified iron +8 diamonds, stock2 | durability2304, speed10, enchantability18, repair purified iron; air Shift-use toggles virtual FortuneII / SilkI through both native NeoForge hooks, stored FortuneIII retained
chud_multitool_axe / pickaxe / shovel | artisan:12 purified iron +6 diamonds, stock2; initial form axe | same tier; native stack transmute copies component patch, one durability/name/enchant/rune history; one existing protected ToolRuneRuntime queue, four breaks/tick; built-in axe detects one connected natural tree (max128 logs,32 high,8 horizontal,8192 reads), rejects player-placed/unknown/constructed surroundings; leaves use vanilla decay
chud_mining_lift | artisan:8 purified iron +16 emeralds, stock2 |64 uses,30 tick activation,1200 tick cooldown; loaded9x9 column heightmaps, true top surface only, open sky, body collision/fluids/hot floor/border/native permission and teleport-event cancellation; no charge on failure

All four can appear in existing loot: city storehouse2.5%, abandoned mine3.5%, abandoned treasury8% per chest. Conditional weights lantern2, each other object1, one item maximum. This is a per-chest chance, not a per-city guarantee. Existing old saved merchant offers receive missing new trades idempotently, preserving old rows and their uses. Existing already-opened chests are not refilled.

Pickaxe/shovel/axe are actual Java tool classes, not a catch-all mining API. Rune family activation follows the current item; stored other-form runes remain recoverable/inactive. Shift mining with an installed lumberjack rune uses its original bounded rune mode; ordinary axe mining invokes the built-in single-tree mode. There is no second processing queue. Tools have two nominal sockets; prior earned sockets remain grandfathered by Runes.max.

Artwork: original code-generated pixel sprites and two voxel item models. Lantern is a square metal cage with raised bronze handle; master pickaxe has a long asymmetric head; folding axe has a broad head, pick a short head and shovel a spade; lift is a wide spool/frame/crank. Dark metal, bronze and small teal accents. These are production assets, not the earlier concept poster. New IDs have no prior model to render as a true before image. Real client first/third-person/inventory captures and owner visual acceptance are recorded separately. No existing weapon/model/UV/audio was replaced.

The lantern uses the native client light engine and max emission, without an added lighting draw pass. Server engine is excluded by exact ClientLevel identity. Third-party light engines/shaders and eight real multiplayer clients require explicit compatibility tests; no universal compatibility claim. Lift is blocked in the native great-kurgan final room until boss defeat, outside Overworld and by the native teleport cancellation hook. Other custom arena/claims integrations require runtime verification.
