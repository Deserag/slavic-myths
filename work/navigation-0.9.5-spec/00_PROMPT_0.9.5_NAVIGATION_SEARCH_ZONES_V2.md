# SLAVIC MYTHS — CODEX IMPLEMENTATION SPECIFICATION
## Version: 0.9.5 — Navigation, Discovery & Search Zones V2
## Target: Minecraft 1.21.1 / NeoForge / Java 21
## Mod ID: `slavicmyths`
## Repository: https://github.com/Deserag/slavic-myths
## Prerequisite: 0.9.4 port to 1.21.1 NeoForge MUST be complete
## Status: HARD REQUIREMENTS — DO NOT REDESIGN FROM SCRATCH

---

# 0. PURPOSE

This specification defines the navigation/discovery layer for Slavic Myths 0.9.5.

The goal is NOT to reveal the whole world to the player.

The goal is to:
- remember important locations the player has legitimately discovered;
- integrate those discoveries with the installed map/minimap;
- allow the player to hide/show whole marker categories;
- allow exactly one active tracked target;
- display direction and distance to the tracked target;
- allow quest mechanics to reveal only an approximate SEARCH AREA instead of exact boss/miniboss coordinates;
- avoid turning exploration into `/locate` with a fancy icon;
- avoid filling the world map with hundreds of permanent useless markers.

This task is part of 0.9.5 Worldgen, Navigation & QoL Rework.

Do NOT use this task to:
- redesign kurgans;
- add boss fights;
- rebalance mobs;
- add loot;
- add new weapons;
- add runes/enchantments;
- build a new custom minimap/world map.

---

# 1. CORE DESIGN RULE

Slavic Myths owns:
- discovery state;
- marker categories;
- quest search areas;
- tracked-target state;
- visibility/filter preferences;
- permanent/temporary marker lifetime;
- unlock logic;
- server authority over quest targets.

Xaero's Minimap / World Map is only an OPTIONAL DISPLAY TARGET.

The core game logic MUST still work when Xaero is absent.

No quest, Yaga mechanic, Putevodny Klubok, Waystone/Path Stone, boss encounter or structure discovery may require Xaero to exist.

---

# 2. IMPORTANT XAERO INTEGRATION CONSTRAINT

Do NOT assume Xaero has a stable public API.

Before implementing direct integration:
- inspect the exact installed Xaero 1.21.1 NeoForge classes;
- inspect known compatibility projects such as Xaero's Minimap & World Map — Waystones Compatibility;
- determine the least fragile supported integration method for the pinned Xaero version.

Do NOT:
- hard-depend the Slavic core mod on Xaero;
- crash when Xaero is absent;
- use random reflection hacks across many internal private fields;
- copy third-party source code without respecting its license;
- bundle Xaero classes into Slavic Myths.

Preferred architecture:
- core navigation API inside Slavic Myths;
- optional client-side adapter behind safe mod-presence checks and client-only loading;
- if direct Xaero integration is too fragile, isolate it into one compatibility package/module so a future Xaero update does not affect core navigation.

If an area overlay cannot be implemented safely through Xaero:
- DO NOT write a full replacement map;
- use the fallback visualization specified below.

---

# 3. CORE DATA MODEL

Create a player-specific navigation/discovery system.

Suggested logical types:

`SlavicMarker`
`MarkerCategory`
`MarkerKind`
`MarkerLifetime`
`MarkerVisibilityState`
`TrackedTarget`
`SearchArea`
`SearchAreaShape`
`DiscoveredLocation`
`NavigationManager`
`MapIntegrationBridge`

Use modern player attachments / persistent player data where appropriate.
Names may follow project conventions, but responsibilities must remain.

Every marker must have a stable internal UUID or stable source key.

---

# 4. MARKER CATEGORIES

Implement at least these categories:

## `YAGA`
Examples:
- Baba Yaga Hut.

Default: VISIBLE.

## `WAYSTONE`
Examples:
- discovered Path/Way Stones.

Default: VISIBLE.

## `KURGAN`
Examples:
- Small Kurgan;
- Warrior Kurgan;
- Great Kurgan.

Default: VISIBLE.

## `BANDIT`
Examples:
- small camp;
- medium base;
- large settlement.

Default: VISIBLE.

## `BOSS`
Examples:
- Likho;
- Tugarin;
- future world bosses.

Default: VISIBLE for active/current markers.
Completed/dead one-time boss markers should normally auto-hide/remove unless the underlying location remains important.

## `QUEST`
Examples:
- Yaga contract target;
- Hunt contract;
- approximate search region.

Default: VISIBLE.

## `SPECIAL_LOCATION`
Examples:
- bathhouse;
- shrine;
- rare unique POI.

Default: VISIBLE.

## `EVENT`
Examples:
- active Hunt;
- temporary ritual target;
- temporary encounter.

Default: VISIBLE while active.

Do not create a separate category for every individual structure.

---

# 5. MARKER KINDS

A category says WHAT family the marker belongs to.
A kind says HOW it behaves.

At least:

`PERMANENT_DISCOVERY`
A location the player has legitimately discovered and wants remembered.

`TEMPORARY_QUEST`
Removed/hidden when quest completes/fails/expires.

`TEMPORARY_EVENT`
Exists only while event is active.

`SEARCH_AREA`
Approximate region, NOT exact target coordinates.

`TRAVEL_POINT`
Activated Path/Way Stone.

`TRACKED_ONLY`
May be hidden from normal category display but visible because the player explicitly tracks it.

---

# 6. MARKER LIFETIME

At least:

`PERMANENT`
Persists until player explicitly forgets/deletes it or world/player data is reset.

`UNTIL_QUEST_END`
Automatically removed/archived when quest completes/fails/expires.

`UNTIL_EVENT_END`
Automatically removed at event end.

`UNTIL_DISCOVERED`
Approximate marker/search zone becomes a permanent discovery marker or is replaced after actual discovery.

Avoid `SESSION_ONLY` except debugging.

---

# 7. DO NOT AUTO-MARK EVERYTHING

Automatic permanent markers should be limited.

Auto-save by default:
- Baba Yaga Hut after legitimate reveal/discovery;
- activated Path/Way Stones;
- Great Kurgan;
- major bandit settlement;
- unique structures;
- important quest locations after actual discovery;
- other major/special locations.

Do NOT automatically create permanent markers for every:
- tiny camp;
- mushroom patch;
- small swamp pile;
- ordinary small POI.

For minor POIs:
- either no automatic marker;
- or offer `Сохранить место` in navigation UI.

This is mandatory to avoid long-term map clutter.

---

# 8. CATEGORY VISIBILITY FILTERS

Player can hide/show categories independently.

Required toggles:
- Baba Yaga;
- Path/Way Stones;
- Kurgans;
- Bandits;
- Bosses;
- Quests;
- Special Locations;
- Events.

Behavior:
- hiding a category does NOT delete discovered markers;
- hidden markers remain stored;
- turning category on restores them;
- tracked marker remains optionally visible even when its category is hidden.

Add:
`Always show currently tracked marker = true` by default.

Therefore player can hide ALL Kurgans but explicitly track one Great Kurgan and still see that one.

---

# 9. NAVIGATION SETTINGS

Create player/client preferences for:

- showYagaMarkers
- showWaystoneMarkers
- showKurganMarkers
- showBanditMarkers
- showBossMarkers
- showQuestMarkers
- showSpecialLocationMarkers
- showEventMarkers
- alwaysShowTrackedMarker
- autoTrackNewQuestTarget
- autoTrackPutevodnyKlubokTarget
- showTrackedTargetHudFallback
- xaeroIntegrationEnabled
- hideCompletedEventMarkers
- autoCreatePermanentDiscoveryMarkers

Defaults should favor usability without creating map spam.

These preferences must not make the dedicated server load client-only Xaero classes.

---

# 10. TRACKING SYSTEM — EXACTLY ONE PRIMARY TARGET

The player may have many discovered markers but exactly ONE primary tracked target.

Store stable `trackedMarkerId` or equivalent.

When tracking a new marker:
- previous target stops being tracked;
- previous marker remains saved;
- new marker becomes primary target.

UI action:
`Отслеживать`

When already tracked:
`Прекратить отслеживание`

If tracked marker is removed because quest/event ends:
- clear tracked state;
- notify player briefly;
- do not silently pick another random marker.

---

# 11. TRACKED TARGET DISPLAY

Preferred with Xaero available:
- highlight/use the tracked waypoint if safe;
- use Xaero's off-screen waypoint/minimap behavior if stable;
- keep distance visible when outside minimap zoom if Xaero supports it.

Fallback when Xaero cannot provide reliable tracking:
Slavic Myths may render a SMALL navigation HUD, NOT a map.

Example:

`Цель: Великий курган`
`↗ 1264 м`

Requirements:
- compact;
- configurable OFF;
- no giant overlay;
- no duplicate HUD if Xaero already provides equivalent guidance.

DO NOT recreate a minimap.

---

# 12. NAVIGATION SCREEN

Create a small Slavic navigation manager screen, NOT a map.

Suggested access:
- keybind using a free key or unbound by default;
- and `/slavicmyths navigation`.

Tabs/sections:
1. `Метки`
2. `Фильтры`

Marker row:
- icon/category;
- localized name;
- distance;
- dimension;
- state;
- `Отслеживать`;
- `Скрыть/Показать` if applicable;
- `Забыть` only for player-owned permanent discoveries where allowed.

For server-owned quest/event markers, `Скрыть` is display-only.
Deleting display marker must NOT delete quest state.

---

# 13. PUTEVODNY KLUBOK — FINAL BEHAVIOR

When player uses Putevodny Klubok:

## Hut not actually placed
Do NOT create fake map marker.
Do NOT reveal unresolved candidate.
Message:
`Клубок кружится, но путь пока не открылся.`

## Hut placed
Create/update:
- category `YAGA`;
- kind `PERMANENT_DISCOVERY`;
- name `Избушка Бабы-яги`;
- actual placed hut coordinates;
- correct dimension.

Marker becomes permanent immediately.
The magical item legitimately revealed the location; do not make the player forget it later.

If `autoTrackPutevodnyKlubokTarget=true`:
automatically track the hut.

Without Xaero:
core marker and fallback navigation still work.

Repeated use must UPDATE existing marker, not duplicate it.

---

# 14. PATH/WAY STONE MARKERS

On legitimate activation:
- save location as `WAYSTONE`;
- lifetime permanent;
- use actual stone name/ID;
- link marker to stable travel-point ID.

If stone is removed/deactivated:
- remove/disable only the Slavic-owned generated marker;
- never delete an unrelated manually-created user waypoint.

Activation is idempotent.
No duplicate marker after repeated interaction/login.

---

# 15. DISCOVERING STRUCTURES

A structure becomes discovered only through legitimate gameplay, such as:
- player enters discovery radius;
- advancement/trigger fires;
- player interacts with key block;
- quest explicitly reveals it;
- Putevodny Klubok reveals Yaga;
- current authoritative gameplay mechanic says it is discovered.

Do not reveal all SavedData structures just because they exist.

Suggested discovery distances:
- Great Kurgan / major structure: ~80–96 blocks;
- medium structure: ~64;
- small structure: ~40–48.

Use structure bounding box when available instead of only center distance.

---

# 16. HARD REQUIREMENT: BOSS/MINI-BOSS QUESTS MUST NOT REVEAL EXACT COORDINATES

When a quest tells player to find a boss/miniboss:
- SERVER knows actual target/encounter position;
- CLIENT does NOT receive exact X/Z before legitimate discovery;
- client receives only a `SearchArea`.

Do NOT send exact coordinates and merely draw them fuzzy.
Do NOT put exact coordinates into client NBT/config/marker metadata.
Do NOT log them to normal client log.

Exact position remains server-authoritative until actual discovery.

---

# 17. SEARCH AREA DESIGN

A SearchArea is NOT:
`circle centered exactly on boss`.

That would teach players to walk to center.

Instead:
- target MUST be inside displayed area;
- target should commonly be away from visual center;
- target may be near an edge;
- displayed center is deliberately offset from the target.

The search area must be deterministic per quest instance/target so relogging does not move it.

---

# 18. SEARCH AREA GENERATION — REQUIRED ALGORITHM

Given actual target position `T`.

Inputs:
- target type;
- quest instance UUID;
- world seed / stable quest salt;
- target encounter/home anchor;
- biome/terrain constraints.

Generate `SearchArea A`.

## Step 1 — choose area size

Recommended defaults:

Ordinary mini-boss hunt:
- effective radius 180–300 blocks.

Strong mini-boss / rare hunt:
- 240–380 blocks.

World boss / major quest target:
- 320–550 blocks.

Very rare unique encounter:
- 450–700 blocks only if actual travel scale warrants it.

Do not make every zone enormous.

## Step 2 — choose displayed center OFFSET

Choose deterministic random angle `theta`.

Choose normalized target distance fraction `f`.

Preferred distribution:
- minimum commonly around 0.45R;
- majority around 0.60–0.85R;
- occasional 0.30–0.45R;
- meaningful near-edge sample 0.85–0.95R.

Avoid `f < 0.20` for ordinary boss searches.

For conceptual circular area:

`displayCenter = target - direction(theta) * (f * radius)`

Therefore the TARGET lies at distance `f * radius` from visible center.

The visible center is a search-region reference, NOT target coordinate.

## Step 3 — validate zone

Ensure:
- target is inside zone;
- majority of zone is still plausible for the target's encounter/biome;
- do not center a Dark Forest search mostly in ocean/desert unless encounter logic truly allows it;
- for structure-bound boss, actual lair remains inside zone;
- visible center itself is not an obviously impossible location like deep lava ocean unless thematically valid.

Retry bounded deterministic candidates if invalid.

## Step 4 — optional irregular shape

Preferred when renderer allows safe area overlay:
- irregular polygon/ellipse rather than perfect circle;
- 8–12 radial control points;
- each radius = baseRadius * deterministic factor ~0.78–1.12;
- smooth if renderer supports it;
- actual target must remain inside.

Do not use extremely narrow shapes that effectively triangulate target.

---

# 19. TARGET SHOULD SOMETIMES BE NEAR EDGE

This is mandatory.

The player should NOT learn:
`go to search center = find boss`.

Create automated statistical test over at least 1000 generated zones.

Acceptance target:
- outside area: 0%;
- inside inner 25% radius: <10%;
- roughly 50–85% radius: majority, target ~55–75%;
- roughly 85–95% radius: meaningful ~10–25%;
- 100% inside valid displayed boundary.

Exact percentages may be tuned slightly, but center bias is forbidden.

---

# 20. SEARCH AREA SHAPE DISPLAY — PRIORITY + FALLBACK

## Option A — true area overlay
If a safe optional Xaero integration can draw a client-only translucent polygon/ellipse/area:
- draw search region;
- use category color;
- no exact target point;
- no exact target metadata on client.

## Option B — grouped boundary approximation
If Xaero supports generated waypoints but no safe polygon API:
- create temporary boundary markers ONLY if they can be grouped/hidden cleanly;
- do not flood normal user's waypoint list with 8 ugly permanent entries;
- all boundary markers are Slavic-owned and auto-cleaned.

If grouping is not practical, do not force this option.

## Option C — safe fallback: approximate anchor + radius
Create one waypoint:
`Район поиска: Тугарин`

Its coordinate is the deliberately OFFSET display center.

UI shows:
`Радиус поиска: ~420 м`

Tracking outside zone:
`До области поиска: 680 м`

Inside:
`Вы в области поиска (~420 м)`

This fallback is VALID because target is deliberately not centered on the marker.

Never replace SearchArea with exact boss waypoint merely because polygon rendering is unavailable.

---

# 21. SEARCH AREA STATES

At least:

`ASSIGNED`
Quest exists, target authoritative.

`APPROACHING`
Area tracked and player outside.

`INSIDE_SEARCH_AREA`
Player crossed area boundary.

`TARGET_DISCOVERED`
Actual target legitimately found.

`COMPLETED`
Objective complete.

`EXPIRED`
Quest failed/expired.

---

# 22. ENTERING SEARCH AREA

When crossing into area:
show one short message:
`Вы вошли в область поиска.`

Tracked indicator changes from:
`До области поиска: 420 м`

to:
`Вы в области поиска: ищите следы цели.`

Do NOT reveal exact boss coordinate after entering.

No repeated spam every tick when player walks near boundary.
Use enter/exit hysteresis or cooldown.

---

# 23. OPTIONAL LOCAL SEARCH CLUES

0.9.5 may provide lightweight qualitative clues inside area.

Examples by distance bands:

Far:
`Следы едва заметны.`

Medium:
`След становится свежее.`

Near:
`Цель где-то совсем рядом.`

Requirements:
- no exact number;
- no exact bearing;
- cooldown between messages;
- player setting to disable;
- not a new complicated minigame;
- no new art asset required.

---

# 24. TARGET DISCOVERY

Boss/miniboss becomes discovered when legitimate condition occurs.

Recommended condition union:
- target loaded and player within ~48–64 blocks;
- OR target acquires player;
- OR player damages target;
- OR encounter-specific trigger fires.

Do not use client map distance alone as authoritative discovery.

After discovery:

For moving boss:
- may create temporary exact tracked entity marker ONLY after discovery, if safe/useful;
- or rely on entity/minimap/Jade visibility while loaded.

For structure-bound boss:
- convert SearchArea into permanent discovered LAIR/STRUCTURE marker if appropriate.

Do not create permanent boss-residence marker after a one-time boss dies unless the location remains gameplay-relevant.

---

# 25. QUEST COMPLETION CLEANUP

When quest completes:
- remove SearchArea;
- remove temporary exact/event marker;
- clear tracking if tracking this quest;
- archive quest state;
- keep only a legitimate permanent structure/location marker if appropriate.

No orphan boundary points.
No hundreds of completed boss markers after long play.

---

# 26. QUEST FAILURE / TARGET REPLACEMENT

If target dies for unrelated reason, despawns permanently or is replaced:
server determines authoritative replacement policy.

If new target location differs materially:
- invalidate old SearchArea;
- increment search revision;
- generate new deterministic SearchArea around new authoritative target;
- sync only approximate area;
- remove old area markers.

Do NOT move visible SearchArea every few seconds with a roaming boss.

For roaming target, SearchArea represents encounter/home/spawn REGION, not tick position.

---

# 27. WORLD BOSS RULE

For controlled world-boss anchors:
- derive SearchArea from authoritative encounter/lair region;
- displayed area center still offset;
- do not expose exact anchor if it gives away boss;
- boss can be in outer part of area.

If encounter has a permanent lair/structure:
after actual discovery, replace temporary boss SearchArea with permanent location marker.

---

# 28. MINI-BOSS / YAGA CONTRACT RULE

For repeatable Yaga/Hunt mini-boss contracts:

On assignment:
1. choose/create authoritative encounter;
2. generate SearchArea;
3. sync only SearchArea;
4. optionally auto-track if player setting enabled.

Search area usually smaller than world boss area.

On completion:
remove it entirely unless a permanent structure discovery was made.

---

# 29. TRACKING A SEARCH AREA

Tracking a SearchArea is NOT tracking boss.

Outside area:
- arrow may lead toward display center OR nearest boundary if implementation supports it;
- UI MUST say `область поиска`, not boss exact location.

Preferred distance:
`До области поиска: 842 м`

If nearest-boundary calculation is easy, use it.
If not, display-center distance is acceptable as long as UI says approximate region.

Inside area:
- NO arrow to exact boss;
- show `Вы в области поиска`.

---

# 30. REACHING DISPLAY CENTER DOES NOT SOLVE QUEST

Because center is deliberately offset:
reaching center must NOT trigger exact reveal.

At center:
- keep zone active;
- player still searches terrain;
- no `boss not found` error;
- no hidden snap-to-target.

The center is only a useful travel reference into the region.

---

# 31. MAP CLUTTER CONTROL

Category filters are not enough; lifecycle must clean temporary data.

Automatically remove/hide:
- completed QUEST markers;
- ended EVENT markers;
- dead one-time BOSS markers;
- expired SearchAreas.

Minor discoveries should not auto-save by default.

Do NOT auto-delete major permanent discoveries.

---

# 32. MARKER OWNERSHIP

Every generated marker needs stable metadata:
- owner = `slavicmyths`;
- marker UUID;
- category;
- kind;
- source ID;
- dimension;
- createdAt;
- lifetime;
- revision.

Only modify/remove markers owned by Slavic Myths.

Never delete manually-created user waypoint merely because name/coordinate matches.

---

# 33. DUPLICATE PREVENTION

Create/update by stable source key.

Examples:
- `yaga_hut:world`
- `waystone:<uuid>`
- `kurgan:<structureStartId>`
- `quest:<questInstanceUuid>:search`
- `boss_event:<encounterUuid>`

Repeated login, dimension change, item use or map refresh must not duplicate markers.

---

# 34. MULTIPLAYER

Discovery is player-specific unless current design explicitly says shared.

Quest SearchArea:
- only relevant player/party receives it;
- unrelated players do not get approximate or exact location.

Waystone discovery:
player-specific unless actual existing travel design says otherwise.

Dedicated server must never load Xaero client classes.

---

# 35. NETWORKING SECURITY

For SearchArea sync send ONLY:
- area ID;
- quest/source ID;
- category;
- display center OR safe polygon geometry;
- radius/shape;
- dimension;
- state;
- display name;
- lifetime;
- revision.

DO NOT send:
- exact boss X/Z;
- hidden exact encounter anchor if equivalent to boss location;
- deterministic private server seed/salt that trivially reconstructs target.

Exact target remains server-side until discovery event.

---

# 36. PERSISTENCE

Persist:
- permanent discoveries;
- marker source identity;
- active SearchAreas or enough authoritative data to reconstruct identical display area;
- waystone links;
- tracking state;
- marker revisions.

Client/player preferences persist separately.

Temporary active quest marker must restore after relog if quest still active.
Completed temporary marker must NOT resurrect.

---

# 37. XAERO WAYPOINT GROUPING

If pinned Xaero supports waypoint sets/groups safely:
create/use dedicated group/set:
`Slavic Myths`

Prefer category prefixes/groups if practical.

Do not pollute arbitrary current custom set if integration can avoid it.

If Xaero internals force current set:
document limitation clearly.

---

# 38. COLORS / ICONS

Suggested visual language:

YAGA:
- dark purple;
- hut/cauldron symbol.

WAYSTONE:
- aqua/light blue;
- stone/rune.

KURGAN:
- gray-gold;
- mound/tomb.

BANDIT:
- dark red;
- flag.

BOSS:
- red;
- skull.

QUEST:
- gold;
- `!`.

SPECIAL_LOCATION:
- green/amber;
- rune.

EVENT:
- orange;
- claw/flame/general event icon.

SearchArea uses category color but softer/translucent if actual area overlay exists.

Do not rely only on color; names/icons must differentiate.

---

# 39. FILTER UI

Example:

`Метки Slavic Myths`

- [✓] Баба-яга
- [✓] Путевые камни
- [✓] Курганы
- [✓] Разбойники
- [✓] Боссы
- [✓] Задания
- [✓] Особые места
- [✓] События

Additional:
- [✓] Всегда показывать отслеживаемую метку
- [✓] Автоматически отслеживать новую цель задания
- [✓] Автоматически отслеживать цель клубка
- [✓] Скрывать завершённые события

Use standard Minecraft UI, not huge web-style settings panel.

---

# 40. NAVIGATION SCREEN EXAMPLES

Permanent marker:

`[icon] Великий курган`
`1284 м | Обычный мир`
`[Отслеживать] [Скрыть]`

SearchArea:

`[!] Район поиска: Тугарин`
`Область ~420 м`
`[Отслеживать]`

DO NOT show exact hidden boss coordinates for SearchArea.

---

# 41. DEV COMMANDS

Follow actual command style, but provide equivalent functionality:

`/slavicmyths dev navigation list`
Lists Slavic markers visible to chosen/test player.

`/slavicmyths dev navigation marker <id>`
Shows metadata.

`/slavicmyths dev navigation searcharea create-test <targetType>`
Creates deterministic developer SearchArea around explicit dev target.

`/slavicmyths dev navigation searcharea stats`
Runs statistical distribution test.

`/slavicmyths dev navigation clear-owned`
Removes only Slavic-owned markers for test player.

Dev exact coordinate inspection requires permission and explicit debug action.

---

# 42. SEARCH AREA BIAS AUTOMATED TEST

Generate at least 1000 deterministic SearchAreas.

Report:
- min/mean/max radius;
- normalized target distance from display center;
- % in 0–25%;
- % in 25–50%;
- % in 50–85%;
- % in 85–95%;
- outside count.

Acceptance:
- outside = 0;
- inner 25% uncommon (<10% target);
- majority in 50–85%;
- meaningful near-edge population 85–95%.

This test exists specifically to prevent accidental center bias.

---

# 43. EXAMPLE — MINI-BOSS

Actual target (SERVER ONLY):
X=1000, Z=2000.

Quest chooses radius:
R=320.

Deterministic fraction:
f=0.82.

Displayed SearchArea center may become approximately:
X=760, Z=1860.

Client sees:
`Район поиска: Овинник`
`Радиус: ~320 м`

Player walking to X=760/Z=1860 is only entering useful search region.
The boss may be hundreds of blocks away toward the outer zone.

Client does NOT receive 1000/2000 until legitimate discovery.

---

# 44. EXAMPLE — WORLD BOSS

Quest/ritual points player toward Tugarin region.

Actual encounter anchor/server target is hidden.

SearchArea:
- radius around 450 m;
- display center intentionally offset;
- target e.g. at 78% radius from visible center;
- if true polygon supported, zone is irregular;
- otherwise one approximate region marker + radius.

Player reaches center:
NOTHING special is revealed.
They must search the region.

When boss is legitimately found:
SearchArea state -> TARGET_DISCOVERED.

After boss dies:
temporary boss marker removed.
If there is a meaningful permanent lair/structure, only that location may remain.

---

# 45. FALLBACK WITHOUT XAERO

All mechanics MUST still work.

Without Xaero:
- Navigation screen lists discoveries;
- tracked permanent target uses compact direction/distance HUD;
- SearchArea tracking leads only to approximate region;
- inside-area indicator works;
- Putevodny Klubok works;
- Waystone system works.

No crash.
No missing class.
No required Xaero dependency in `neoforge.mods.toml`.

---

# 46. OPTIONAL XAERO BRIDGE PACKAGING

If direct integration requires Xaero internals and would make core fragile, choose one:

A. isolated optional compat package/source set loaded only if Xaero exists;

or

B. small separate addon artifact:
`slavicmyths-xaero-compat-0.9.5.jar`

Only choose separate artifact if substantially safer.
Core navigation must remain independent either way.

Document architecture chosen and reason.

---

# 47. EXISTING WAYSTONES COMPATIBILITY MOD IS A REFERENCE, NOT OUR DEPENDENCY

The existing Xaero Waystones compatibility project proves automatic waypoint creation is possible, but it targets the external Waystones mod.

Slavic Myths uses its own Path/Way Stone mechanics.

Do NOT install Waystones solely to make our markers work.
Do NOT depend on that compatibility mod.
Use it only as an implementation reference where technically and legally appropriate.

---

# 48. INTERACTION WITH USER XAERO WAYPOINTS

Slavic sync must not:
- rename user waypoints;
- delete user waypoints;
- claim user waypoints;
- merge by coordinate/name alone.

If Xaero cannot store our metadata:
maintain Slavic-side mapping from our marker ID to generated Xaero waypoint identity.

---

# 49. TRACKING PRIORITY

New quest:
if `autoTrackNewQuestTarget=true`, track its SearchArea once.

If player manually tracks another marker later:
manual choice wins.

Do NOT force quest tracking back every tick/login.

Putevodny Klubok auto-track also occurs only on reveal/item-use event.

---

# 50. DIMENSIONS

Markers are dimension-aware.

If tracked target is in another dimension:
do NOT show misleading normal distance arrow.

Display:
`Цель в другом измерении: <dimension>`

Do not invent cross-dimensional routing in 0.9.5 unless current travel system already supports it.

---

# 51. NO LEAKS THROUGH LOGS / TOOLTIPS

Normal client must not see:
- exact hidden boss X/Z;
- internal target seed;
- authoritative hidden anchor.

Dev exact coordinates only with permission and explicit debug command.

Do not log exact quest target coordinates at client INFO level.

---

# 52. SEARCH AREA BIOME QUALITY

Search region must remain plausible.

Example:
if boss is in Dark Forest, offset center should not result in 80% of visible search zone being ocean/desert unless encounter design permits it.

Score zone candidates by:
- same/compatible biome coverage;
- traversable terrain;
- dimension validity;
- target-inside guarantee.

Do not force target to biome geometric center.

---

# 53. PERFORMANCE

No per-tick regeneration.

SearchArea generated once per quest revision.

Marker sync is event-driven:
- login;
- quest state change;
- discovery;
- settings/filter change;
- waystone activation/removal;
- explicit sync recovery.

Do not send every marker every tick.

Area rendering must be lightweight.

---

# 54. DATA VERSIONING

Navigation saved data must have schema version, e.g.:
`navigation_data_version = 1`

Parsing should be defensive.
Do not silently crash old player data after future updates.

---

# 55. MANUAL QA DOCUMENT

Create:
`docs/MANUAL_QA_0.9.5_NAVIGATION.md`

Must include:

1. Xaero absent — core navigation works.
2. Xaero present — Slavic markers appear.
3. Category hide/show.
4. Track one marker.
5. Track second marker — first untracked.
6. Hidden category + tracked marker still visible if configured.
7. Putevodny Klubok before hut placement.
8. Putevodny Klubok after hut placement.
9. Repeated Klubok use no duplicates.
10. Waystone activation no duplicates.
11. Waystone removal affects only Slavic-owned marker.
12. Great Kurgan discovery.
13. Minor POI does not spam map.
14. Mini-boss SearchArea.
15. Boss not near center case.
16. Near-edge boss case.
17. Client does not receive exact boss coordinate before discovery.
18. Enter area message.
19. Target discovery transition.
20. Quest completion cleanup.
21. Quest failure cleanup.
22. Relog while quest active.
23. Dedicated server no Xaero client-class crash.
24. Two multiplayer players with different quests receive correct private markers.
25. User-created Xaero waypoint untouched.
26. Category filters persist.
27. Tracked target persists when source remains valid.
28. No orphan area markers after reload.

---

# 56. AUTOMATED TESTS

Do NOT run Minecraft client.

Add tests where practical for:
- marker serialization;
- category filters;
- duplicate prevention;
- marker ownership;
- tracking switch;
- SearchArea deterministic generation;
- target-inside invariant;
- center-bias distribution;
- quest revision;
- cleanup;
- multiplayer/player isolation;
- network payload does not contain exact hidden target fields;
- fallback direction math;
- dimension behavior.

Headless build/verify only.

---

# 57. DEFINITION OF DONE

Navigation task is DONE only if:

- [ ] core marker system works without Xaero;
- [ ] marker categories implemented;
- [ ] category visibility filters implemented;
- [ ] permanent/temporary lifecycle implemented;
- [ ] exactly one tracked target;
- [ ] tracked permanent target has direction/distance;
- [ ] compact no-Xaero fallback exists;
- [ ] Putevodny Klubok adds real permanent Yaga marker only after actual hut placement;
- [ ] Way/Path Stone markers persist after legitimate activation;
- [ ] major structure discovery markers supported;
- [ ] minor POIs do not spam map by default;
- [ ] boss/miniboss exact coordinates remain server-side before discovery;
- [ ] SearchArea center deliberately offset from target;
- [ ] boss commonly appears in outer half / sometimes near edge;
- [ ] target always inside SearchArea;
- [ ] SearchArea cleanup works;
- [ ] completed temporary markers auto-remove/hide;
- [ ] optional Xaero integration does not hard-depend core;
- [ ] user Xaero waypoints remain untouched;
- [ ] multiplayer privacy respected;
- [ ] no client launch;
- [ ] manual QA doc created;
- [ ] build passes.

---

# 58. FINAL CODEX RESPONSE FORMAT

Return factual sections:

## Навигационное ядро
Actual classes/files and state model.

## Категории и фильтры
What exists and defaults.

## Отслеживание
How one primary target works.

## SearchArea
Exact algorithm, radius ranges, offset distribution and shape fallback.

## Боссы / мини-боссы
How exact coordinates remain server-side and how discovery transition works.

## Баба-яга
Putevodny Klubok behavior.

## Путевые камни
Marker ownership/persistence.

## Xaero
Actual integration method. If true area overlay is unavailable, say which safe fallback was implemented.

## Tests
Automated/headless results, including the 1000-area distribution statistics.

## Manual QA
What must still be tested by manually launching game.

## Не выполнено
Only genuine remaining items.

Never claim in-game verification if Minecraft was not launched.
