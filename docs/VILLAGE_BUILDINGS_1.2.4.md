# Village buildings 1.2.4 — implementation and verification

Deployment update 2026-10-07: the user subsequently authorized installation. The verified JAR was copied to PolyMC `Slavic-Myths-1.21.1-Testing`, previous 1.2.3 backed up outside mods, SHA-256 verified, one active Slavic Myths JAR and all eight companion/configuration files unchanged. [Installation receipt](verification/polymc-1.2.4-installation.json). No new client/server launch or rebuild was performed; the deferral references below describe the earlier implementation stage.

Implemented from the supplied `Slavic_Myths_1.2.4_Codex_Package.zip`, with the user's permission to choose compatible materials where unspecified. Minecraft 1.21.1, NeoForge 21.1.255, Java 21, official mappings, mod ID `slavicmyths`. PolyMC installation and client launch were explicitly deferred. Existing uncommitted work was retained; no commit or push was made.

The catalog contains 34 structures in each of three climates (102 showcase templates): three Seni, three Zemlyanka, three Izba, twelve vanilla profession houses, five separate custom profession houses, Hlev with the herder workplace, Ambar, Klet, stable, two fields, barracks and village center. The cold variant changes roof slope, enclosure and snow detailing; the warm variant changes porch/open space and light wood/sandstone choices. Log framing, infill, raised floors, shutters, fireplaces/chimneys, pitched roofs and profession annexes use existing vanilla/mod blocks. The architecture is a game interpretation of the supplied visual references, not a claim of uniform historical authenticity.

Five vanilla village families, including zombie pools, now use authored residential, profession, utility and center templates. All 334 original house/center selections were mapped while preserving original weights and empty entries. Native street geometry/graph is retained, but house connectors are moved outward to road bounding-box edges with short paved approaches: the original inward sockets prevented the larger templates from fitting. Additional custom buildings have low weights. Every start center includes a generic protected carved-post shrine; this adds no deity or religion system. Existing `slavicmyths:military/barracks` remains valid; both legacy and new barracks IDs are limited to one per native assembly.

Furniture, actual beds and existing native POIs are physically placed. Hunter drying racks and cook/guard tables contain complete multiblock parts. Ordinary decorative mod barrels do not introduce unintended fisherman workplaces. Ambar's 64-slot settlement chest starts empty. Ordinary chests use lazy, one-time themed loot tables with small material/food stacks and scarce tools/coins; no periodic refill or generated trade inventory was added. Livestock is authored in stable/Hlev templates. Footings add at most eight local supporting blocks during placement, respect loaded chunks and preserve bedrock; no tick handler or production world scan was added.

Operator commands:

```text
/sm test village_showcase
/sm test village_showcase temperate
/sm test village_showcase cold
/sm test village_showcase warm
/sm place village slavicmyths:village/temperate/residential/seni_01
```

The default palette follows the source biome. Showcase placement loads and validates all 34 templates and their actual bounds before edits, uses a six-by-six grid with the center in a central plot, connects entrances with roads, and reports palette/origin/region/counts. It places 23 professional villagers and four livestock per palette. Individual placement accepts complete resource IDs. Height checks accept negative Y; surface height is read after loading the anchor chunk. Clearing preserves the ground layer outside the authored excavation. Explicit QA chunk tickets are temporary and removed in `finally`; normal village generation does not force chunks. These commands intentionally edit the selected area: use a separate empty test world.

Two existing issues exposed by placement were corrected: `wooden_barrel` now uses the already-defined Barrel class and its required BREWING property; drying-rack parts retain their collision shapes but use `forceSolidOff()` so native navigation can use the space beneath the beam. The `/sm` alias now redirects to the registered command tree. No saved registry ID, profession ID or production recipe was changed.

The reproducible generator is `tools/village_buildings.py`, called after earlier village/military resource generators to retain the latest building overlay. The shipped village directory contains 389 NBT templates including biome-specific centers, zombie variants and streets, in addition to the 102-entry catalog. [Replacement mapping](verification/village-1.2.4-mapping.json) and [resource/JAR checks](verification/village-1.2.4-checks.json) provide machine-readable evidence.

Final verification used separate headless NeoForge GameTest worlds, never a user save. There were **20 actual server launches** during this iteration, including failed diagnostic runs; an additional compile-only failed attempt was not a launch. There were **zero client launches** and no PolyMC update.

| Gate | Final result and evidence |
| --- | --- |
| New building runtime tests | 4/4 PASS, `work/server-1.2.4-buildings-final.log` |
| Native village planning | 40 valid assemblies across five families; houses present, no vanilla houses selected, at most one barracks; four barracks across sampled assemblies |
| Minecraft resource parsing | 8,562 raw palette states checked against actual block properties; real loot tables resolved |
| Showcase placement and native paths | 102/102 catalog entries placed on genuinely generated layered superflat surface Y=-57, bedrock Y=-64 retained; native paths to beds and workplaces; full-ID individual command also executed |
| Native workplace ownership and arrival | 21/21 professions claimed compatible POIs and actually approached them after starting outside; no JOB_SITE memory shortcut |
| Abort before mutation | Out-of-height placement rejected without modifying witness blocks |
| Existing regression suite | Final 53/53 PASS, `work/server-1.2.4-regression-diagnostic.log` |
| Clean production build | `gradlew.bat clean build` PASS, `work/build-1.2.4-final.log` |
| Production packaging | `tools/verify_buildings_124.py --jar build/libs/slavicmyths-1.2.4.jar` and `tools/verify_village_121.py --version 1.2.4 --jar build/libs/slavicmyths-1.2.4.jar`; all resource bytes match, no test classes/resources in JAR |

The production artifact is `build/libs/slavicmyths-1.2.4.jar` (8,312,789 bytes), SHA-256 `4c2b48ada706353c25f72d7f01cebb54c182c31c1a17166f2ac7a18af6220975`. The building validator completed 317,916 static/package assertions; the broader asset gate checked 2,803 JSON files and 5,923 model/texture links, with 10,010 pure-Java outfit assertions. These are resource/unit checks, not additional game launches. Final runtime logs retain the known development warnings about Curios refmaps, vanilla teleport ambiguity and unavailable Windows performance counters; no mod runtime error appears in the final building run.

Intermediate regressions exposed intermittent old shepherd/linen physical-chain tests. Their scheduling-sensitive long scenarios were separated into daytime-only test batches; the final full suite passed, but earlier failures remain recorded in the logs. Test diagnostics do not affect production behavior. Existing restart tests with the default `phase=none` skip their restart scenario: this iteration does **not** claim a new real reconnect/restart test, measured MSPT or gameplay acceptance from those passes. Native assembly tests validate the real jigsaw planner, not a player exploration of naturally generated terrain. The schematic previews under `work/` depict NBT geometry, not Minecraft screenshots.

Remaining manual acceptance is deliberately short: inspect all three showcases in a separate client test world; verify roof/material/detail appearance and animal gates; inspect new natural villages on varied terrain; interact with furniture/chests and observe residents sleeping/working. Client appearance, varied-terrain exploration and manual mechanics have not been accepted yet. Update PolyMC only when the user explicitly asks.
