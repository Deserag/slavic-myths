# NeoForge 1.21.1 integration architecture — implementation checks, not copy/paste guesswork

Audit actual classes first. Do not create shadow duplicate systems. Use current equivalent of Entity attachment with persistent serializer + `copyOnDeath()` for player book and bond states, or extend existing SavedData/player data if equivalent and already used by project. A specific class or method name is not verified until matching installed Gradle dependencies. Official NeoForge concepts: data attachment persistence, SavedData for world state, registering custom network payload, AdvancementProvider JSON. Links in `10_SOURCES/RIGHTS_AND_TECH.md`.

### Persisted records
```
PlayerTaleProgress schemaVersion 1
  bookDeliveryState enum UNSEEN/PENDING/DELIVERED
  firstWorldJoin UUID
  catalogProgress: map ResourceLocation -> KnowledgeState(stage, discoveredAt, verifiedAt)
  rumorRegions: map id -> {dimension, worldAnchorUUID, centerX,centerZ,radius,expiresAt,sourceNPC}
  seenBookNotifications: set ResourceLocation
  bayunBond: map bayunEntityUUID -> {trust:int [0,100], firstGiftAccepted:bool, lastGiftDay:long, lastPetDay:long}

WorldBayunRegistry schemaVersion 1
  anchors: map anchorId -> {dimension, blockPos, entityUUID, placementVersion}

EntityBayunState
  anchorId, homePos, animState, stateEnterGameTick, targetPlayerUUID?, acceptedGiftSessionIds
```
Strict schema forward migration; if existing book state already uses data, migrate once; old worlds must retain their book and progress. Unknown fields tolerate future versions. `bookDeliveryState` must avoid duplicate on interrupted full inventory delivery. On player copy due to death, preserve state even if item drops and keepInventory false. On disconnect during `CHARMED`, clear temporary movement modifier/overlay while persistent trust unaffected. Entity book knowledge is always per PLAYER UUID, not globally per world.

### Network payload safety
Server sends compact `OpenBookSnapshotS2C` with sorted part/chapter/entry IDs+dynamic status and hints; localized static strings client resource files, not excessive networking. Update `BookDeltaS2C` only when server state changes. `ChooseBayunDialogueC2S` with entityId, optionId, monotonic interaction sequence only; server rechecks current dimension, range, LOS, trust, time and menu state. `GiveBayunGiftC2S` only if actual client interaction needs explicit packet; prefer standard vanilla entity use; server consumes held item and validates. `BayunAnimS2C` clipId, startTick, target(optional) for tracker clients; no forced global packets. Max packets per event bounded. Ensure no forged client success/cooldown bypass/double items. Do not send player coordinate secrets in net hint if not needed; client receives approximate region center for local map but GUI never prints exact coords.

### Thread and lifecycle
Server main thread owns world mutation; client only renders/clicks/subtitles. Register payload via correct installed NeoForge 1.21.1 APIs; common classes must never reference `Minecraft.getInstance()` or render classes. Data load for world and player login/clone/death/dimension switch; safe tick cleanup for stale charm states, no lingering attribute modifiers or flying/fall damage outside normal rules. On physical client GUI opened, read server-provided snapshot not local hardcoded demo data. Synchronization after advancements must wait for genuine server award.

### Asset paths
- Item model/texture under `assets/slavicmyths/models/item/...`, `textures/item/...`; verify actual 1.21.1 item model definitions in current repo (1.21.1 resource paths vs 1.21.4 item definitions).
- World entity texture & animation under `assets/slavicmyths/textures/entity/kot_bayun/...` and renderer/modeled rig according to actual library.
- Audio under `assets/slavicmyths/sounds/bayun/...` and matching `sounds.json` mappings.
- GUI sprites under `assets/slavicmyths/textures/gui/book/...`, registered client screen only.
- Advancement JSON under `data/slavicmyths/advancement/**.json` for 1.21.1 (singular). Localization `assets/slavicmyths/lang/ru_ru.json`,`en_us.json`.

### Build and distribution
Run `gradlew.bat clean build` using project's configured JDK21, not global incompatible Java. Inspect `build/libs` and exact jar size, unzip -l verify all actual new sprites, sound files and JSON. Never claim 'test run' if only compile. Follow AGENTS.md to deploy built JAR into correct PolyMC Minecraft 1.21.1 profile only after passes; avoid older 1.16.5 mods folders. Update real `docs/PROJECT_STATUS.md`, `docs/ROADMAP.md`, `docs/ARCHITECTURE.md`, do not modify git commit without request. If remote public repo lags local 1.6, active checkout wins.

### Performance budgets
Opening book from cached data <150ms median dev machine; no full chunk scanning; GUI sorting/catalog cache; Bayun AI worst-case 50 nearby entities queries/s/entity or fewer (prefer 2/s); heartbeat idle every 20 ticks, no repeated O(N-world players) checks. Worldgen spawn attempts only on existing supported biome feature callbacks. Visible entity model ≤16k triangles goal, 30+ fps at 10 entities in controlled dev scene, confirm measurement rather than assertion. Audio max 2 concurrent loops per Bayun. Unknown metrics explicitly report NOT_MEASURED.
