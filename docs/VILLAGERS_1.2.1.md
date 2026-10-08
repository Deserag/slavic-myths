# Slavic Myths 1.2.1 — implementation and acceptance

Implemented on the existing main working tree, preserving 1.1.8 and all prior registry IDs. No automatic commit/push. Minecraft client and PolyMC were not launched.

## Professions and workstations

| Profession ID | RU / EN | Existing workstation |
| --- | --- | --- |
| slavicmyths:miller | Мельник / Miller | slavicmyths:millstone — the only new fallback |
| slavicmyths:brewer | Пивовар / Brewer | slavicmyths:fermentation_vat |
| slavicmyths:weaver | Ткач / Weaver | slavicmyths:loom_table |
| slavicmyths:herder | Скотовод / Herder | slavicmyths:feeder |
| slavicmyths:hunter | Охотник / Hunter | slavicmyths:drying_rack — only master part 0 |
| slavicmyths:cook | Повар / Cook | slavicmyths:kitchen_table |

The millstone is a compact wooden stand with a stepped stone wheel, trough and small flour-sack detail. Its approved recipe uses four planks, two cobblestone and one iron ingot. It is a workstation only; existing flour recipes/processes remain unchanged. Other five blocks retain their functional 1.1.x systems. Native POI tickets permit one owner per workstation. Non-master drying-rack pieces do not become extra workplaces.

All 13 vanilla professions keep their IDs, POIs and trades. Display overrides contain only 14 villager names and Wandering Trader. Librarian displays as Летописец / Chronicler, Cleric as Знахарь / Herbalist, Nitwit as Бездельник / Idler and Wandering Trader as Купец / Merchant.

## Visual system

Three climates use the stored vanilla villager type: snow/taiga → COLD; desert/savanna/jungle → WARM; plains/swamp/unknown → TEMPERATE. There is no biome scanning during rendering.

Two coherent bundles combine clothing, headwear, beard and accessories. An integer starts from the entity UUID, is serialized and synchronized through a NeoForge attachment, and is copied through vanilla infection/cure. Profession changes update the professional layer without rerolling that index. No gender, aging or demographic system exists.

The short bundle exposes trousers; the long bundle retains the native long garment. Cold outfits keep the longer coat with fur trim. Profession geometry includes seed/feed bags, net, wool roll, scroll/book, map tube, leather apron details, sheath, hammer, steel blank, small protective plates, herb bottle, grain scoop, measuring cup, spool/shuttle, bell and spoon. There are 58 new native 128×128 textures, generated reproducibly. Additional detail follows the existing trade level and retains the vanilla badge; no separate visual XP is stored.

The thin renderer subclasses preserve vanilla faces/noses, animation, scale/shadows and equipment layers. Costume head geometry is suppressed when a real head item is equipped. Babies use the native appearance/layer, without profession accessories, beard or level overlays. Zombie clothing reuses profession assets, dark tint and one damaged mask; native profession/XP/offers are untouched. Merchant uses a navy/burgundy/ochre travel coat, hood, satchel, pack and rolled cloth; vanilla trading, llamas, leash, invisibility, despawn and AI remain unchanged.

## Trading

Six bundled JSON pools contain 120 listings: four per level for five levels. Only these new professions receive new listings. Existing food, grain, textile, animal and brewing registry IDs are used; no items were added to fill trade pools. Recipes and balance of existing items are unchanged.

Vanilla selects two offers when each level unlocks. Restock resets uses without selecting a new catalogue. Native MerchantOffers save/load, demand, special price, reputation/Hero discounts and level thresholds remain responsible for transactions. Listings use maxUses 12 at levels 1–3 and 8 at levels 4–5, multiplier 0.05, XP 2/5/10/15/30. Raw goods are bought in batches; processed goods cost emeralds. There is no milk/empty-bucket buyback loop and no guaranteed catalogue of all rare Master drinks.

Pools are data-driven bundled source files under `data/slavicmyths/village_trades`, not a new external datapack framework.

## Operator tools

- `/sm debug villager [entity]`: nearest/selected villager or zombie UUID, type, climate, profession ID/name, level, outfit, baby/zombie state, claimed POI, lock state, offer count and item IDs. Zombie output includes saved offers and conversion state.
- `/sm test villager workstation <miller|brewer|weaver|herder|hunter|cook>`: place a real complete workstation near an adult unemployed test villager, only in free space. Native AI acquires it; no teleport or forced profession assignment.
- `/sm test villager level <1..5>`: set a visual test level. This is not a substitute for trading to unlock offers.

These extend the existing `/slavicmyths debug/test` tree; `/sm` redirects to it. Permission level 2 is required.

## Verification

- `gradlew.bat clean build`: PASS, final 34s; 12 existing deprecation warnings. Ordinary Gradle `test`: NO-SOURCE.
- `gradlew.bat -PvillageRuntimeCheck -PwithoutJei runVillageChecks verifyVillageGeometry`: final PASS.
- Seven GameTests: registry/state/tag mapping; offer selection/restock/NBT; 13 vanilla professions at 65 levels; six natural job acquisitions with distinct owners; six children and six Nitwits rejected; six new professions progress 1→5 using native notifyTrade/timers; real zombie infection followed by a shortened native cure timer.
- 360 trade samples produced 180 observed assortments, exactly two offers per sampled level. NBT roundtrip preserves offers/profession/type/level/outfit. It is not a real player relog.
- Four real GameTest-server launches. First two failed test assertions (object identity and unsuitable adjacent RNG seeds); final two passed. Latest run also verifies the multiblock master-only POI fix.
- Actual pure Java UUID/climate rules: 10,010 assertions over 10,000 UUIDs.
- Actual baked accessory geometry: 127 meshes, 1,537 cubes, non-degenerate geometry, UVs within atlas and no intersection with nose/eye volumes. No rendering window or GPU was initialized.
- `python tools/verify_village_121.py --jar build/libs/slavicmyths-1.2.1.jar`: PASS; 2,707 JSON and 5,900 model/texture links, translations, 58 image dimensions, generator reproducibility, common/client isolation and production bytes checked. All 642 production class files separately match compiled bytes. Test harness is absent from JAR.
- `git diff --check`: PASS.

Production: `build/libs/slavicmyths-1.2.1.jar`, 7,273,587 bytes, SHA-256 `283582b994e6a417f426ec7ce5d81af863e4b3af59e467ed6798c932aedd3471`.

Installed after gates into PolyMC `Slavic-Myths-1.21.1-Testing`. One active Slavic Myths JAR; previous 1.1.8 backed up outside mods. Six companion mods and both instance setting files unchanged by hash. No worlds/configs were overwritten. Old 1.16.5 instance is untouched.

Evidence: [checks](verification/village-1.2.1-checks.json), [installation receipt](verification/polymc-1.2.1-installation.json), [changed files](verification/village-1.2.1-files.json).

## Manual acceptance

1. In PolyMC, view all vanilla/new professions in COLD, TEMPERATE and WARM with several UUIDs: short/long clothing, readable tools, hats/scarves/beards, eye/nose clearance, walking/head rotation, level details and helmet compatibility. Check Idler, Merchant front/back/pack/llamas and professional zombies; babies should stay vanilla and acquire adult clothing when grown.
2. Place each real workstation beside unemployed adults; check competition for one POI, trading GUI, loss before first trade versus lock afterwards, daily restock and player reputation/Hero discounts. The test suite verifies transactions via server notifyTrade, not GUI payment by a player.
3. Perform a real relog and chunk unload/load: appearance and offers must remain stable. Check a traded villager through infection/cure in ordinary gameplay. No actual player relog, chunk reload or full server-restart playthrough is claimed.
4. Check Russian/English profession names, millstone recipe/drop and that all five reused functional workstations still process/store items normally.

Client visual acceptance and those player-driven checks remain pending. No measured MSPT or compatibility test with third-party villager render replacements is claimed. No future settlements, production-worker AI, families or quests were implemented.
