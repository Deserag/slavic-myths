# SLAVIC MYTHS — CODEX IMPLEMENTATION SPEC
## 0.9.4 FINALIZE PORT + POLYMC 1.21.1 TEST PACK
## Minecraft 1.21.1 / NeoForge / Java 21
## Repository: https://github.com/Deserag/slavic-myths
## Mod ID: `slavicmyths`

---

# 0. CURRENT STATE — DO NOT RESTART THE PORT

The first migration stage has already been completed.

According to the latest developer report:
- NeoForge 1.21.1 and Java 21 build foundation is configured;
- new dependencies are configured;
- data/resource directories and formats were partially migrated;
- textures were preserved;
- headless checks for kurgan/hunt/Yaga planning work on the new API;
- original source and the working 0.9.3 JAR were preserved;
- PolyMC was intentionally NOT modified yet;
- full build still fails because old Forge/Minecraft 1.16.5 APIs remain;
- there is no finished 0.9.4 JAR yet.

The exact current source of truth is:
`docs/port/PORT_STATUS_0.9.4.md`

FIRST action:
read that file and inspect current git diff/status.

DO NOT:
- regenerate the project from scratch;
- discard completed migration work;
- revert new NeoForge setup to old Forge;
- overwrite current port work with a clean MDK;
- begin 0.9.5 worldgen rework;
- launch Minecraft client.

This task finishes 0.9.4.

---

# 1. FINAL TARGET

At the end of THIS task:

1. Slavic Myths compiles for Minecraft 1.21.1.
2. Loader is NeoForge.
3. Java is 21.
4. Mod version is `0.9.4`.
5. A production Slavic Myths 0.9.4 JAR exists.
6. Existing 0.9.3 gameplay is ported with no silent content loss except explicitly approved QoL duplication removal described below.
7. A dedicated PolyMC testing instance uses:
   - Minecraft 1.21.1;
   - the EXACT same NeoForge version targeted by the Gradle project;
   - Slavic Myths 0.9.4;
   - Curios;
   - JEI;
   - Jade;
   - Xaero's Minimap;
   - Xaero's World Map;
   - FallingTree.
8. The old 1.16.5 working instance/build remains available as backup and is not silently destroyed.
9. No Minecraft client is auto-launched.
10. `PORT_STATUS_0.9.4.md` ends with an accurate final status.

---

# 2. IMPORTANT PROJECT DECISION: STOP REINVENTING GENERIC QoL MODS

During the original project some generic functions were implemented or planned manually even though mature mods already provide them.

From this point onward:

## KEEP inside Slavic Myths
These are domain-specific and remain our code:
- all Slavic mobs/bosses;
- kurgans;
- bandit content;
- swamp/water content;
- rituals;
- Baba Yaga;
- Yaga hut;
- Yaga quests/favor/exchange/cauldron;
- Putevodny Klubok;
- Hunt system;
- world-boss systems;
- custom progression;
- Slavic items/blocks/food/weapons/armor;
- Broom/Mortar;
- unique crafting/ritual UIs;
- Curios-backed Slavic accessories;
- JEI category integrations for Slavic recipes.

## DO NOT PORT / DO NOT DEVELOP GENERIC DUPLICATES WHEN A FIXED COMPANION MOD REPLACES THEM
After inspecting actual source:

### Enemy/block info / generic HP overlay
If Slavic Myths has its own generic entity HP/info HUD whose purpose is substantially duplicated by Jade:
- remove it from default runtime OR remove the old implementation if it is isolated and safe;
- do not spend time porting an obsolete 1.16.5 HUD just to delete it later;
- do NOT remove boss bars or Slavic-specific boss UI;
- do NOT remove unique mechanic indicators that Jade cannot replace.

Jade becomes the recommended/default information overlay in the PolyMC pack.

### Generic item/recipe browser
Do not build or port a separate generic item/recipe browser that duplicates JEI.
Keep Slavic JEI plugin/categories for unique recipes.

If no such duplicate custom browser exists, do nothing.

### Minimap/world map
Do not implement a Slavic custom minimap/world map.
Use Xaero's Minimap + Xaero's World Map in the testing/modpack environment.

### Tree capitator
Do not implement a custom Slavic treecapitator.
Use FallingTree.
Ensure Slavic logs/leaves have correct Minecraft/NeoForge tags.

IMPORTANT:
This de-duplication policy is NOT permission to delete unique Slavic systems merely because another mod has vaguely related functionality.

---

# 3. PORT COMPLETION STRATEGY

Continue from the current `PORT_STATUS_0.9.4.md`.

Complete the port by subsystem, not by random import replacement.

Recommended remaining order:

A. Registry/static data completion.
B. Stateful items and persistent player/world data.
C. Entities, attributes, AI, effects, damage.
D. Client renderers/models.
E. Networking.
F. Menus/screens/commands.
G. SavedData/attachments.
H. Worldgen/structures/spawns.
I. Curios/JEI integration.
J. Headless/dedicated-server validation.
K. QoL duplicate removal.
L. Production JAR.
M. PolyMC 1.21.1 test-pack deployment.

After each block:
- update `docs/port/PORT_STATUS_0.9.4.md`;
- distinguish `DONE`, `COMPILES`, `BLOCKED`, `NOT STARTED`;
- do not mark a system DONE solely because one class compiles.

---

# 4. STATIC CONTENT / REGISTRIES

Finish every real existing registry.

At minimum inspect and port:
- Items;
- Blocks;
- BlockEntityTypes;
- EntityTypes;
- MobEffects;
- SoundEvents;
- MenuTypes;
- ParticleTypes;
- custom recipe types/serializers;
- enchantments already implemented;
- any custom registries present in source.

Preserve existing `slavicmyths:*` IDs.

Do NOT beautify/rename IDs during port.

Validate against:
`docs/port/REGISTRY_INVENTORY_1.16.5.md`
if already created.

No silent disappearance.

---

# 5. DATA/RESOURCE COMPLETION

Complete Minecraft 1.21.1 resource/data migration.

Audit:
- `recipe`
- `advancement`
- `loot_table`
- `structure`
- `tags/item`
- `tags/block`
- `tags/entity_type`
- biome/worldgen data
- NeoForge biome modifiers
- models/blockstates
- sound definitions
- lang files

No obsolete duplicate folders should remain active merely "just in case".

Every referenced registry ID must resolve.

Textures must remain unchanged during this port unless a path/name fix is required.

No visual redesign in 0.9.4.

---

# 6. ITEMSTACK STATE / DATA COMPONENTS

Search remaining source for:
- old ItemStack NBT access;
- `getOrCreateTag`;
- `getTag`;
- old CompoundNBT/CompoundTag legacy patterns;
- custom inventory state;
- selected-mode state.

Port structured stack-owned state to valid 1.21.1 data components where appropriate.

Pay special attention to:
- Trophy Pouch;
- Hunting Horn;
- Broom;
- Mortar;
- ritual/utility items;
- any portable internal inventory.

Preserve:
- stack limits;
- internal contents;
- anti-nesting behavior;
- cooldown/selection semantics;
- serialization;
- drop/save/load behavior.

---

# 7. PLAYER DATA / WORLD DATA

Use correct NeoForge 1.21.1 mechanisms.

Player/entity-specific persistent state:
prefer Data Attachments where appropriate.

World-level state:
use modern SavedData.

Audit and port:
- Baba Yaga favor/progress;
- Yaga hut anchor/state;
- Hunt state;
- world-boss anchors/cooldowns;
- kurgan state;
- other current progression flags.

Every SavedData mutation must mark dirty.

Do not put everything in static maps.

---

# 8. ENTITIES / AI / ATTRIBUTES

Port EVERY currently implemented Slavic entity.

The port is not finished if the registry contains an entity but its AI/rendering/attributes were stubbed.

Inventory actual source list.

Must include all currently implemented families such as:
- early folklore mobs;
- water/swamp mobs;
- bandits;
- kurgan creatures;
- Ovinnik;
- Volkolak;
- Fire Serpent;
- Podvey;
- Likho One-Eyed;
- Tugarin Zmey;
- Baba Yaga;
- any other actual entity present.

Preserve gameplay values from current 0.9.3:
- health;
- damage;
- armor;
- speed;
- follow ranges;
- AI cooldowns;
- phases;
- attack timings;
- boss bars;
- drops.

Do NOT rebalance in 0.9.4.

---

# 9. MODELS / RENDERERS

Port current detailed geometry.

Do not simplify models because new rendering APIs are harder.

Port:
- model layers;
- renderers;
- emissive/translucent layers;
- held models;
- special boss geometry;
- Yaga model;
- all current entity model registrations.

Client code must be isolated from dedicated-server common code.

Compilation alone does NOT prove visual correctness.
Record runtime visual QA as manual.

---

# 10. EFFECTS / DAMAGE

Port:
- current custom mob effects;
- kurgan curse/effects;
- Durnaya Dolya / other actual effect IDs;
- current custom damage semantics.

Use correct 1.21.1 damage-type/source API.

Preserve armor/fire/magic/shield behavior as closely as possible.

No new balancing.

---

# 11. NETWORKING

Replace all old Forge SimpleChannel/network patterns with correct NeoForge 1.21.1 typed payload APIs.

Inventory old packets.

For each record:
`OLD PACKET | DIRECTION | NEW PAYLOAD | STATUS`

Important systems likely include:
- Baba Yaga UI actions;
- quest accept/submit;
- exchange;
- cauldron;
- Trophy Pouch UI;
- other custom menus;
- Broom/Mortar inputs if networked;
- Hunt state/control if networked.

Server remains authoritative.
Never trust arbitrary client progress or item counts.

---

# 12. MENUS / SCREENS

Port current unique screens, including actual source implementations such as:
- Baba Yaga;
- Yaga cauldron;
- Trophy Pouch;
- Book of Tales / Slavic lore UI if present;
- kitchen/ritual/custom crafting;
- any other unique Slavic screen.

Use modern 1.21.1 MenuType / screen registration / GuiGraphics APIs.

Do not redesign visuals.

Do not port a redundant generic item browser if it only duplicates JEI.

---

# 13. COMMANDS

Port existing command tree and aliases.

Commands must compile/register under 1.21.1 Brigadier/NeoForge.

Keep current names during 0.9.4.

Do NOT yet perform 0.9.5 command redesign or `/locate` cleanup.

Known command families include:
- Yaga dev;
- kurgan locate/generate/info;
- boss dev locate/summon/clear;
- other actual `/slavicmyths` commands.

Commands offered by tab completion must actually execute or return a useful localized error.

---

# 14. WORLDGEN / STRUCTURES

Port current behavior FIRST.

Port:
- kurgan generation;
- current bandit structures;
- current swamp/water structures;
- bathhouse;
- Yaga hut placement;
- world-boss anchor logic;
- plants/ores/features/spawns;
- all actual structure templates.

Use 1.21.1 data-driven biome modifiers where they are the proper replacement for old biome-loading injection.

Do NOT implement 0.9.5 terrain adaptation yet.

Preserve current behavior sufficiently that later 0.9.5 can intentionally redesign it.

---

# 15. BABA YAGA PARITY

0.9.4 is not done without full 0.9.3 Yaga parity.

Preserve:
- Baba Yaga entity;
- hut structure;
- current placement system;
- favor statuses;
- dialogue/interaction;
- quests;
- exchange;
- cauldron;
- Putevodny Klubok;
- punishment;
- repeatable contracts;
- current low-Y fix;
- current rollback fix;
- spawn egg.

Do NOT fix the planned-anchor/worldgen UX here unless required for basic port execution.
0.9.5 will redesign it.

---

# 16. HUNT / WORLD BOSSES / KURGANS PARITY

Hunt:
- Ovinnik;
- Volkolak;
- Fire Serpent;
- Podvey;
- Horn;
- Charm;
- Trophy Pouch;
- current encounter logic.

World bosses:
- Likho;
- Tugarin;
- boss bars;
- phases;
- summon systems;
- anchor state;
- current dash facing behavior.

Kurgans:
- current types;
- current labyrinth;
- current mobs/bosses;
- current curse;
- current blocks;
- current commands.

No redesign.

---

# 17. CURIOS

Curios remains required for Slavic accessory slots.

Target local/test version:
`curios-neoforge-9.5.1+1.21.1.jar`

CurseForge:
- Project ID: `309927`
- File ID: `6529130`

Use the normal runtime JAR, NOT api-only/sources JAR.

Port:
- ring slots;
- amulet slots;
- belt slots;
- all current accessory behavior/rendering.

---

# 18. JEI

JEI remains the generic item/recipe viewer.

Target local/test version:
`jei-1.21.1-neoforge-19.51.0.418.jar`

CurseForge:
- Project ID: `238222`
- File ID: `8792638`

JEI should remain optional for core Slavic Myths unless current architecture intentionally requires it.

Port Slavic-specific JEI categories:
- rituals;
- Yaga cauldron;
- cooking/custom stations;
- other actual categories.

Do NOT write/port another generic recipe browser.

---

# 19. JADE — REPLACES GENERIC CUSTOM HP/INFO HUD

Target local/test version:
`Jade-1.21.1-NeoForge-15.10.6.jar`

CurseForge:
- Project ID: `324717`
- File ID: `8591319`

Use Jade in the PolyMC test pack for generic entity/block information and HP.

Inspect source for Slavic generic HP/info HUD.

If it exists and substantially duplicates Jade:
- do not spend substantial effort porting it;
- remove/disable it from default core runtime cleanly;
- delete obsolete client event/render registration if safe;
- remove dead config keys/resources related only to that duplicate HUD;
- document the removal in `docs/port/QOL_DEDUP_0.9.4.md`.

Do NOT remove:
- Minecraft boss bars;
- boss phase indicators unique to Slavic Myths;
- Yaga favor UI;
- unique ritual status UI;
- mechanic-specific warnings.

No hard dependency on Jade in the Slavic JAR is required.

---

# 20. XAERO'S MINIMAP + WORLD MAP

Do NOT implement custom Slavic map code.

PolyMC test pack must include:

Xaero's Minimap:
`xaerominimap-neoforge-1.21.1-26.5.0.jar`
CurseForge:
- Project ID: `263420`
- File ID: `8849842`

Xaero's World Map:
`xaeroworldmap-neoforge-1.21.1-1.45.0.jar`
CurseForge:
- Project ID: `317780`
- File ID: `8698659`

No hard dependency from Slavic Myths.
No reflection hacks to create waypoints in 0.9.4.
No custom minimap.

---

# 21. FALLINGTREE — REPLACES CUSTOM TREECAPITATOR IDEA

Do NOT implement a custom Slavic treecapitator.

PolyMC test pack:
`FallingTree-1.21.1-1.21.1.11.jar`

CurseForge:
- Project ID: `349559`
- File ID: `6835168`

Ensure current and future Slavic tree blocks use correct standard log/leaves tags so FallingTree can identify them through normal ecosystem conventions.

Do not hard-depend Slavic Myths on FallingTree.

---

# 22. FIXED POLYMC TEST PACK CONTENTS

Final 1.21.1 testing instance MUST contain exactly one compatible active copy of:

1. Slavic Myths `0.9.4`
2. Curios `9.5.1+1.21.1`
3. JEI `19.51.0.418` NeoForge
4. Jade `15.10.6` NeoForge
5. Xaero's Minimap `26.5.0` NeoForge for MC 1.21.1
6. Xaero's World Map `1.45.0` NeoForge for MC 1.21.1
7. FallingTree `1.21.1.11`

Do NOT accidentally install:
- Forge variant;
- Fabric variant;
- 1.21.11;
- Minecraft 26.x;
- API-only Curios artifact;
- duplicate versions.

---

# 23. POLYMC MIGRATION STRATEGY

The old working 1.16.5 environment is a fallback.

DO NOT destroy it before the 1.21.1 project successfully builds.

Preferred safe final layout:

Legacy backup instance:
`Slavic-Myths-1.16.5-Legacy`
or preserve the existing old instance under its current name if renaming would be risky.

New active test instance:
`Slavic-Myths-1.21.1-Testing`

If the user already has an intended 1.21.1 Slavic testing instance, reuse it instead of creating duplicates.

The NEW instance must use:
- Minecraft `1.21.1`;
- EXACT NeoForge version used by the project's Gradle configuration;
- Java 21 runtime;
- an independent `.minecraft`/instance directory.

Do NOT create a fake instance by only changing a displayed version string.

Verify launcher component metadata actually points to Minecraft 1.21.1 and NeoForge.

---

# 24. CREATING/UPDATING POLYMC INSTANCE

First discover actual PolyMC installation and existing instance paths.

Do not assume a hard-coded user path if the current machine differs.

Preferred methods in order:
1. supported PolyMC import/instance mechanism already available on machine;
2. import a locally generated compatible CurseForge/Modrinth pack if this PolyMC build supports it;
3. clone a known-good 1.21.1 NeoForge instance and change only Slavic pack contents;
4. direct instance metadata editing ONLY after inspecting actual existing PolyMC format and validating components.

Do not invent unsupported `mmc-pack.json` schema from memory.

Record exact instance path.

---

# 25. JAVA 21 IN POLYMC

The instance must resolve Java 21.

If PolyMC already has a Java 21 runtime configured, use it.

If not:
- locate installed Java 21;
- set the instance/runtime appropriately if safely supported;
- do not download arbitrary unofficial Java binaries.

Do not change unrelated launcher Java settings globally if instance-specific configuration suffices.

---

# 26. DOWNLOAD POLICY FOR COMPANION MODS

If Codex environment has network access:
- download only from official CurseForge or official Modrinth project/file endpoints;
- exact MC version: 1.21.1;
- exact loader: NeoForge where loader-specific;
- prefer the pinned files above for reproducibility;
- calculate SHA-256 for every downloaded JAR;
- write `packaging/test-pack-lock.json`.

If official direct automated download is blocked:
- do NOT use third-party mirrors;
- do NOT scrape malware-prone mirrors;
- produce the exact file list/project IDs/file IDs;
- leave clear install instructions.

Never fake that a mod was installed if no file exists.

---

# 27. TEST PACK LOCK FILE

Create:
`packaging/test-pack-lock.json`

For each:
- name;
- version;
- fileName;
- loader;
- minecraftVersion;
- CurseForge projectId;
- CurseForge fileId;
- SHA-256;
- source URL/reference;
- installedPath;
- required/recommended.

Also include:
- exact NeoForge version;
- Slavic Myths JAR file name/hash;
- Java major version.

This makes the test environment reproducible.

---

# 28. THIRD-PARTY DISTRIBUTION

Local PolyMC testing may use official third-party JARs.

Do NOT:
- shade them into Slavic Myths;
- copy their source/classes into our project;
- commit third-party JARs to Git unless license/project workflow explicitly calls for it;
- publish a public pack yet.

Future public pack:
- CurseForge manifest/platform resolution;
- Modrinth `.mrpack`/platform resolution.

Prepare metadata if useful, but 0.9.4 does not publish.

---

# 29. REMOVE OLD INCOMPATIBLE MODS FROM NEW INSTANCE

The new 1.21.1 instance must not inherit random old 1.16.5 mods.

Before deployment:
- inventory old instance `/mods`;
- copy only intentionally selected compatible 1.21.1 files;
- do not copy old Forge 1.16.5 dependencies.

If old Slavic 0.9.3 JAR is present in the new instance:
remove/move it to backup.

Exactly one Slavic JAR must be active.

---

# 30. DO NOT CHANGE THE OLD WORLD AUTOMATICALLY

Do not automatically open or convert the user's existing 1.16.5 world.

For 1.21.1 manual testing:
prefer a fresh test world first.

If old saves are copied:
copy, never move;
mark them explicitly as migration-test copies.

Do not claim save compatibility without testing.

---

# 31. BUILD / VERIFY GATES

Before touching PolyMC new active pack:

GATE A:
project configuration resolves under Java 21.

GATE B:
main + client source compiles.

GATE C:
production `build` succeeds.

GATE D:
resource/data validation succeeds.

GATE E:
ported:
- Hunt checks;
- Kurgan checks;
- Yaga checks;
- any other existing headless tests.

GATE F:
dedicated-server smoke if available non-interactively:
- registries load;
- no client-only class crash;
- datapacks parse.

ONLY after A–E succeed (and F if feasible):
copy 0.9.4 JAR into PolyMC 1.21.1 test instance.

No half-compiling JAR should replace the active testing mod.

---

# 32. AUTOMATIC GAME LAUNCH IS FORBIDDEN

Do NOT:
- runClient;
- launch PolyMC;
- open Minecraft GUI;
- start a graphical game session.

The user will manually launch/test.

Codex must only prepare the instance and report it is ready for manual launch.

Do not claim actual in-game verification.

---

# 33. MANUAL QA DOCUMENT

Create:
`docs/MANUAL_QA_0.9.4_1.21.1.md`

Checklist:
- game reaches title screen;
- create fresh world;
- Slavic creative items visible;
- Curios slots work;
- JEI appears;
- Slavic JEI categories appear;
- Jade identifies entities/HP;
- no duplicate Slavic generic HP overlay;
- Xaero minimap appears;
- world map opens;
- FallingTree works on vanilla tree;
- test on Slavic custom tree if currently implemented;
- spawn several old mobs;
- boss bars;
- Hunt flow;
- Kurgan generation;
- Yaga generation/interactions;
- Broom/Mortar;
- custom screens;
- save/reload;
- dedicated server/multiplayer later.

Mark runtime items as `MANUAL`, not passed.

---

# 34. QOL DEDUP REPORT

Create:
`docs/port/QOL_DEDUP_0.9.4.md`

For each generic feature:
| Feature | Existing Slavic implementation | Companion mod | Decision | Files removed/disabled | Notes |

Must include:
- item/recipe viewer -> JEI;
- generic entity HP/info overlay -> Jade;
- minimap/world map -> Xaero;
- treecapitator -> FallingTree.

If Slavic did NOT actually implement one:
write `not present; no removal needed`.

Do not invent deleted systems.

---

# 35. PORT STATUS FINALIZATION

Update:
`docs/port/PORT_STATUS_0.9.4.md`

At end include:

## Platform
- Minecraft 1.21.1
- NeoForge exact version
- Java 21
- Slavic 0.9.4

## Systems
All migrated systems with actual status.

## Removed generic duplicate QoL
Exact items removed/disabled.

## Production artifact
Exact path/hash.

## PolyMC
Exact instance path and component versions.

## Companion mods
Exact JARs + hashes.

## Manual QA
What user still must test.

## Remaining port regressions
Anything not complete.

Do not call port COMPLETE if main build or critical content still fails.

---

# 36. DEFINITION OF DONE

This task is complete only if:

- [ ] current `PORT_STATUS` was used rather than restarting;
- [ ] all remaining old 1.16.5 APIs in production source are removed/ported;
- [ ] project targets Minecraft 1.21.1;
- [ ] NeoForge build succeeds;
- [ ] Java 21;
- [ ] Slavic version 0.9.4;
- [ ] production JAR exists;
- [ ] no silent core-content loss;
- [ ] Curios integration works at compile/data level;
- [ ] JEI integration compiles;
- [ ] generic duplicate HP/info HUD is removed/disabled if present;
- [ ] no custom minimap is developed;
- [ ] no custom treecapitator is developed;
- [ ] PolyMC has a real Minecraft 1.21.1 NeoForge testing instance;
- [ ] instance uses same NeoForge version as Gradle project;
- [ ] Java 21 configured;
- [ ] new Slavic 0.9.4 JAR installed;
- [ ] Curios 9.5.1 installed;
- [ ] JEI 19.51.0.418 NeoForge installed;
- [ ] Jade 15.10.6 NeoForge installed;
- [ ] Xaero Minimap 26.5.0 NeoForge installed;
- [ ] Xaero World Map 1.45.0 NeoForge installed;
- [ ] FallingTree 1.21.1.11 installed;
- [ ] no wrong-loader/wrong-Minecraft-version duplicate JARs;
- [ ] lock file with SHA-256 exists;
- [ ] old 1.16.5 working environment preserved;
- [ ] no Minecraft client launched;
- [ ] manual QA checklist created.

---

# 37. FINAL CODEX RESPONSE FORMAT

Return only factual sections:

## Порт 0.9.4
Exact completion status.

## Сборка
Build command/result and JAR path/hash.

## Что перенесено
Subsystems.

## Что заменено готовыми модами
Actual duplicate Slavic features removed/disabled.

## PolyMC
Instance name/path, Minecraft, NeoForge, Java.

## Установленные моды
Exact file names/versions/hashes.

## Проверки
Headless/build/server results.

## Ручная проверка
What user should test after manually launching Minecraft.

## Осталось
Only actual incomplete items/regressions.

Never say “игра проверена” if Minecraft was not launched.
