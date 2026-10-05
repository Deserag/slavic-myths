# 0.9.4 РІР‚вЂќ NeoForge 1.21.1 port (BLOCKED)

Target: Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21 / ModDevGradle 2.0.148.
Dependencies: Curios NeoForge 9.5.1+1.21.1; optional JEI NeoForge 19.51.0.418.
Gradle wrapper 9.2.1, obtained from the official 1.21.1 MDK.

The source baseline includes the working 0.9.3 Yaga placement/egg hotfix.
Do not reintroduce the old Y>4 bug or false planned Y=0 location from package history.
No content redesign, intentional ID changes, client launches, pushes, tags or commits.

## Gates

| Gate | Status | Evidence / remaining work |
|---|---|---|
| 1 РІР‚вЂќ inventory/build | DONE (build foundation only) | Java 21, Gradle help, dependency resolution, target Minecraft artifact generation and processResources succeed. compileJava reaches real legacy-source errors. This is NOT a successful mod build. |
| 2 РІР‚вЂќ registries/resources | BLOCKED | 385 items, 153 blocks, 41 entities mapped; 715 data paths migrated, 660 JSONs structurally compared, 547 binary assets unchanged. ModSounds foundation updated. Other registration/source APIs and target decoder loading pending. |
| 3 РІР‚вЂќ item state/accessories/data | BLOCKED | RpgNbt migrated/tested. Recipe result/icon/predicate/loot formats converted, including equivalent Looting bonus math. ItemStack components, Curios and custom serializer codecs pending. |
| 4 РІР‚вЂќ entities/AI/render/effects | NOT STARTED | Preserve all detailed geometry/animations; port against actual target sources. |
| 5 РІР‚вЂќ payloads/UI/commands | NOT STARTED | Replace SimpleChannel; menus, GuiGraphics and command signatures. |
| 6 РІР‚вЂќ SavedData/worldgen | BLOCKED | BurialRecords, HuntRecords, PoolIndex use target SavedData.Factory and pass JVM NBT checks. Kurgan layout/instance/encounter NBT and Yaga blueprint ported. Remaining SavedData, all runtime callers, generation/structure APIs and negative world-height handling pending. |
| 7 РІР‚вЂќ Curios/JEI | NOT STARTED | Correct target artifacts configured, gameplay integration unported. |
| 8 РІР‚вЂќ validation | BLOCKED (full mod; subset passes) | verifyKurgan/verifyHunt/verifyPortData pass on actual target API classes. Full clean build fails; no production JAR, server smoke or target gameplay QA. |

## Recovery and evidence

- Full pre-port source checkpoint (includes uncommitted user work):
  `D:/slavic-myths/.tools/port-backups/slavicmyths-0.9.3-pre-port.zip`.
- Baseline content IDs and SHA256 for source/resources: `baseline-0.9.3.json`.
- Human inventory: `REGISTRY_INVENTORY_1.16.5.md`.
- Target statuses and five technical external namespace updates: `registry-parity-0.9.4.json`.
- Forge API usage by file: `forge-api-usage.json`.
- Package unpacked under `work/port-0.9.4-spec/`.
- Existing PolyMC 1.16.5 instance and working 0.9.3 JAR unchanged during port.

## Commands

PowerShell, at `D:/slavic-myths`:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.12'
.\gradlew.bat help
.\gradlew.bat compileJava --console=plain
.\gradlew.bat verifyPortCore --console=plain
python tools/verify_port_1211.py --resources-only
```

## Results and next work

- `clean build`: FAIL at legacy Java APIs, 100 displayed errors (javac default
  cap; not a count of total migration work). Log: `full-build.log`.
- `help`, `processResources`, target compile/runtime dependency resolution: PASS.
- `verifyPortCore`: PASS РІР‚вЂќ full existing Kurgan/Hunt non-gameplay checks, exact old
  Yaga blueprint and candidates, RpgNbt isolation, PoolIndex roundtrip.
- `--resources-only`: PASS РІР‚вЂќ schema-specific baseline comparison and binary hashes;
  this is not decoder/registry loading and must never substitute for final validation.
- Final `verify_port_1211.py` is expected to FAIL while old APIs/JAR/gates remain.
- Core checks compile selected real production modules separately for intermediate
  API verification; the main source set still includes EVERY original system.
- Old resource/art generators and scoped old source-audit tools must be adapted
  before reuse. Do not run old generators and accidentally recreate plural folders.

Next concrete block is listed at the end of this document; the old phase-1 next gate has advanced into the stage-2 checkpoint.
Do not mass-rewrite unreviewed event/network/worldgen imports or replace gameplay
with stubs to obtain a green build. The exact target API is cached in
`build/moddev/artifacts/neoforge-21.1.255-sources.jar`.

Sources consulted: [official MDK](https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle),
[1.17 migration primer](https://docs.neoforged.net/primer/docs/1.17/),
[1.21 migration primer](https://docs.neoforged.net/primer/docs/1.21/), and actual target
Minecraft/NeoForge source codecs. Runtime dependencies are recorded in
`gate1-dependencies.log`; no Forge 36.2.42 or ForgeGradle remains in the build graph.

Never run `runClient`. A bounded headless dedicated-server smoke is permitted by
the package after a successful target build, using a separate new test world.
Do not claim registry mappings prove target runtime parity or old headless passes
prove 1.21.1 compatibility.

Working legacy recovery JAR: `.tools/port-backups/slavicmyths-0.9.3-working.jar`;
installed PolyMC SHA256 remains `4E6CCD1F3294FB146AA3376840ABA0CB3A60790BAB302323ED20652320849E71`.
Current machine-readable progress (NOT final acceptance): `progress.json`.

## Stage 2 — current factual checkpoint

| System | Status | Evidence / limits |
|---|---|---|
| Resource layout + baseline preservation | DONE (static migration scope) | All original data paths retained in singular target layout; 660 baseline data JSONs compared with explicit schema normalization; 547 original PNG/OGG/NBT hashes unchanged. NBT template decoding remains runtime work. |
| Recipes, loot, advancements, tags | BLOCKED (target loading); static checks pass | All 213 recipes, 227 loot tables, 154 advancements, 35 item tags, 28 block tags and 4 entity tags audited; 1831 references checked against target vanilla resources/sources and preserved mod inventory, zero errors. This is NOT actual target codec loading. |
| Armorer recipe type + both serializers | COMPILES | Actual production modules compile on target: MapCodec, RegistryFriendlyByteBuf StreamCodec, complete 4x4 input, mirror/offset and shapeless matching retained. ArmorerInput deliberately preserves empty borders; vanilla CraftingInput trims them and would regress padded patterns/remainder slots. Recipe IDs belong to RecipeHolder; menu/JEI callers updated. |
| Armorer codec/matching runtime test | BLOCKED | verifyPortRecipes compiles but plain-JVM vanilla bootstrap hits NeoForge FeatureFlagLoader with no initialized LoadingModList. No fake loader/registries introduced. Test source retained for a real loader harness after full mod compilation. |
| Sounds + effect registrar modules | COMPILES | All 147 sound definitions and both MobEffect definitions compile on actual target API. Curse uses NeoForge custom amplifier curve: -10% at level 0, -15% at higher levels, not vanilla -20%. Persistence/login/respawn/secondly restore callbacks compile. Full registry freeze/effect consumers still unverified. |
| Items / Blocks / creative tab / materials | BLOCKED | Core + wood/darkened/kurgan/furniture registrar holders modernized; item/tool/armor properties and creative tab ported. New technical armor-material/component/tab registries replace old inline material/tab/NBT concepts; existing public item/block IDs unchanged. Custom constructors, block callbacks and gameplay dependencies still contain legacy APIs. |
| BlockEntityTypes + persistence | BLOCKED | All seven BE registrar definitions retained. Target positional constructor/provider-aware save/load implemented for altar, cloth, fishing net, pool stone, rack and coffin; wood signs use SignBlockEntity. Corresponding factory signatures adapted. Concrete blocks still need complete EntityBlock/ticker integration; coffin render bounds and gameplay consumers remain old API. Do not count these systems as ready. |
| MenuTypes | BLOCKED | All seven menu declarations use MenuType/DeferredHolder/IMenuTypeExtension; target container/slot APIs and RecipeHolder adapted. Screens, networking, NPC/entity dependencies and remaining kitchen item-damage API still block full menu systems. |
| ItemStack components | COMPILES (boundary); gameplay BLOCKED | Persistent/synchronized components for runes, horn target, nine-slot trophy/cargo inventories and cloth cooldown. Runtime callers wired. Legacy custom_data readers preserve foreign keys; nested old stacks use actual Minecraft item-stack DFU. No getOrCreateTag/getTag/hasTag/removeTagKey remains on ItemStacks (one getTag is a block-entity packet). Entity/world/player CompoundNBT usages still need their own migration. |
| Component value checks | DONE (value/codec scope only) | Actual RuneState/HuntTarget JSON codecs, rune packet roundtrip, immutable values, invalid socket/duplicate/unknown-target rejection pass. Pouch/cargo ItemStack serialization + stack copies + legacy DFU in initialized mod registries remain BLOCKED, not DONE. |
| Curios 9.5.1 | BLOCKED | Original head/necklace/ring/belt/charm definitions retained (1/1/2/1/1); data-driven slots/player assignment and existing item tags, SlotContext callbacks, handler lookup ported. Existing five accessory geometries preserved; registration moved to a client-only ICurioRenderer adapter. Effect consumers/render implementation classes still use legacy APIs; equip/unequip runtime unverified. |
| JEI 19 | BLOCKED | All four existing categories ported: RecipeType, layout builder, ingredient roles, Component titles, GuiGraphics and modern dimensions. Armorer uses RecipeHolder and updated transfer registration. No common code references plugin classes; compileOnly + optional runtime dependency unchanged. Real JEI/core boot (with/without JEI) awaits main build. |
| Entity/feature registration boundary | BLOCKED | Modern DeferredHolder/registrar keys introduced, but concrete entity/feature classes still use old APIs. No entity/AI/worldgen behavior changed. |
| Structure registration | BLOCKED | Existing structures retained; full worldgen API migration remains. |
| Tailwind enchantment | COMPILES (lookup boundary); target loading BLOCKED | Same slavicmyths:tailwind ID, three levels, treasure/trade/random-loot tags, original costs/weight; flight queries the dynamic enchantment registry. |

### Explicit resource compatibility corrections

- Nested loot-table entries use `value`, as required by the target NestedLootTable codec, instead of obsolete `name`. No pool weights/counts/chances changed.
- `whistle_fragile` replaces vanished vanilla `minecraft:grass` with `minecraft:short_grass`.
- Licho loot referenced non-existent `slavicmyths:pustoy_sosud`; corrected the reference to existing `slavicmyths:pustoy_ritualny_sosud`. This does not rename any registered ID or add an item. Generator typo corrected; older generators still must not be run before full target-layout adaptation.
- Explicit old harvestTool/harvestLevel rules are represented by vanilla mining/tier tags.
- Migration tool rerun is idempotent (0 path/schema changes after migration).

### Current stage verification

- `clean build`: FAIL on actual remaining legacy APIs (100 displayed javac errors; not the total). No target production JAR produced. Log: `stage2-clean-build.log`.
- `verifyPortCore verifyPortComponents processResources -PwithoutJei`: PASS after clean; actual production modules, no stubs or main-source exclusions. Log: `stage2-final-headless.log`. This is not a full mod boot without JEI.
- `python tools/verify_static_data_1211.py`: PASS all current data categories and 1831 references. Report: `stage2-data-audit.json`.
- `python tools/verify_port_1211.py --resources-only`: PASS baseline/schema/binary checks. `git diff --check`: PASS.
- `verifyPortRecipes`: BLOCKED at loader bootstrap, preserved test source; never described as passed.
- Remaining API source audit: `stage2-remaining-api.json`; final port verifier still expected to fail while source/JAR/acceptance gates remain.
- Minecraft client/server launches: 0. Old PolyMC/working 0.9.3 JAR unchanged. No commits/pushes/tags.

### Next concrete work

Complete the custom Item/Block implementation APIs that still block these registrars: utility/ritual/artifact items and tool subclasses, then EntityBlock factories/tickers for altar/cloth/net/pool/rack/coffin and remaining container/transaction signatures. Keep all current behavior. After these classes compile, initialize real mod registries and run every data codec + inventory component/Curios/JEI checks. Only then mark full systems DONE and move to the major Entities/AI/Client/Networking/Worldgen API stage. This checkpoint does not claim that the entire requested Items/Blocks/Data stage is finished.


## Finalize package — current work (2026-10-05)

Current contract: work/port-0.9.4-finalize-spec/*.md. Full port followed by an independent
Slavic-Myths-1.21.1-Testing PolyMC instance only after successful real gates.
Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21 / mod 0.9.4; JEI 19.51.0.418.
The Stage 2 section above records the earlier checkpoint; this section supersedes its next steps.

| Block | Status | Evidence / remaining work |
|---|---|---|
| Source type migration | BLOCKED | Verified type/import/FQN changes; no gameplay system declared complete from renames. Main sources include all existing content. |
| Payload boundaries | COMPILES | Real CustomPacketPayload + StreamCodec for lore, RPG sync/activation and flight; roundtrip, defensive copies, malformed/NaN/Inf checks pass. End-to-end handling BLOCKED by gameplay/client classes. |
| Generic HP overlay removal | DONE (authorized source scope) | Overlay/config/CombatTarget removed; NPC mace push kept in MobMaceCombat. Boss bars and unique UI retained; QOL_DEDUP_0.9.4.md. |
| Basic weapons/tooltips/boots | COMPILES (selected classes) | Actual production classes verified in portCore; full item-system runtime remains BLOCKED. |
| Block implementations | BLOCKED | useItemOn/useWithoutItem, EntityBlock factories, proper Bush/Furniture/Coffin codecs and coffin FOOT ticker restored. Menu/gameplay/render dependencies still block full system. ReedBlock/YagaLegBlock compile in portCore. |
| SavedData | BLOCKED (full system) | All seven use provider-aware save + modern Factory; pure Burial/Hunt/Pool boundaries tested. Yaga/Camp/Stronghold/Smoke depend on unported gameplay/worldgen/events. |
| Authored geometry | BLOCKED (full renderer system) | 17 custom model geometries ported, fractional boxes/UV/pivots/opacity verified with actual ModelPart CPU vertices. Bandit humanoid mesh and coffin geometry adapted. Visual QA remains MANUAL. |
| Entities/events | BLOCKED | SynchedEntityData.Builder, inherited target vanilla spawning packets, passenger attachment APIs and split tick/incoming-damage events adapted. Remaining AI/damage/attributes/client/worldgen callbacks must compile and run. |
| Tailwind | COMPILES (lookup boundary); loading BLOCKED | Dynamic registry enchantment with existing ID/levels/costs and treasure tags. No new enchantment/content. |
| Companion acquisition | DONE (download/hash/metadata scope) | All six exact pins downloaded to .tools/test-pack-0.9.4, hashes recorded in packaging/test-pack-lock.json. NOT INSTALLED. |
| PolyMC deployment | BLOCKED_BUILD | Genuine PolyMC 7.0 data/schema/NeoForge support inspected. Legacy instance unchanged; no new profile/JAR installation before successful gates. |

Verification: verifyPortCore verifyPortComponents verifyPortPayloads verifyPortGeometry
processResources -PwithoutJei PASS (finalize-headless.log). This is NOT full mod boot.
Resource audit PASS: 660 baseline data files, 547 unchanged binary assets; 1833 references,
zero static errors (36 item tags after Tailwind). Actual initialized target codec loading BLOCKED.
Full compile still fails; latest log finalize-compile.log. No production JAR/server launch/client launch.

Next: finish concrete entity/effect/render/event API errors, then structure/worldgen conversion,
real mod/codec bootstrap, clean production build and only then PolyMC deployment.

### Finalize checkpoint: production sources compile, runtime gates pending

- Main `compileJava` and production `build` passed in the clean invocation (`finalize-build.log`), but that combined invocation later failed on legacy Yaga test imports. This is not yet a completely successful clean build + verification suite.
- All production registrars/items/blocks/menu/client/entity/payload/worldgen sources compile against the real target API. No main-source exclusions or fake Minecraft classes.
- 13 structure/type IDs and five piece IDs use target codecs/deferred registration; 13 structure + structure_set definitions preserve spacing/separation/salts. Candidate exclusion is bounded noise-only registry work. Authored templates/geometry remain unchanged. Structure loading/generation runtime still BLOCKED.
- Core/tree configured/placed features and biome modifiers are data-driven. Tree regression: 1024 legacy/current snapshots, geometry and post-generation RNG state identical (`finalize-tree-regression.json`).
- Eight screens, Curios render adapters, player equipment/flight/music renderers and keybindings compile. Client visual/input/JEI runtime remains MANUAL/BLOCKED; no client launched.
- Removed undead classification and underwater-breathing overrides are represented by actual target entity tags. Existing named fire/projectile damage uses target damage registries/tags, same messages/exhaustion/scaling.
- Yaga placement now uses min-build-height and non-overlapping failure sentinels, preserving the old rollback/preflight while allowing the target's negative-height superflat. Original placement tests are being ported without deleting assertions; they will run under the actual NeoForge loader.
- Static resource audit: zero errors, 660 baseline JSON and 547 binary assets preserved. Actual datapack codec/registry loading still pending.
- An opt-in headless GameTest server verifies every recipe/loot/advancement/worldgen definition plus existing Yaga/recipe checks and inventory migration. Test classes/resources remain outside production JAR. First preparation attempts reached test API/configuration errors; no successful server boot yet.
- PolyMC deployment remains BLOCKED_RUNTIME_GATES. Six companion pins downloaded; no profile installed. Legacy instance unchanged.

Current statuses: production API layer COMPILES; complete gameplay systems BLOCKED until loader/resource/gameplay tests; companion acquisition DONE; deployment NOT STARTED.
Next: finish loader-backed verification, repair genuine registration/datapack failures, pass clean build/checks, then install the independent PolyMC profile.
