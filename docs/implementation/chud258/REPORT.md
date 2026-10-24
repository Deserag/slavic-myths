# #258 — IN_PROGRESS, 24.10.2026

Direct owner authorization covers implementation and selection of moderate test balance. Production commit **3b00acaf194aec4059b5b51d0b0b991614f1a740**. [Exact policy/prices/limits](POLICY.md). Four objects, six registry IDs, no new crafting recipes/currency/Nether upgrade. Prior book/guide/flight work retained; no repository cleanup or default-branch change.

REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN
--- | --- | ---
Client light14 in either hand, no light blocks/server emission | Actual two client processes: main-hand14, removed0, offhand14, removed0; server0 in every corresponding frame; [light readings](client/light.txt), [log](runtime-client-final.txt) | PASS vanilla native engine
Bounded nearby-player lighting, third-party engines/shaders | Four-tick updates, max8 sources, only tracked loaded client players; client identity excludes integrated server | PASS source; actual several clients/shaders/other dynamic-light mods NOT_RUN
Pickaxe two active modes; preserve FortuneIII | Both NeoForge enchant hooks; actual diamond ore produces ore in Silk mode despite fortune rune;30 native form changes and stored enchant retention | PASS native suite
Pickaxe extra rune blocks | Same native ToolRuneRuntime queue and loot stack | PASS source; Fortune/Silk × minerIII/IV/mastery full multi-block matrix NOT_RUN
Three actual tool families with shared stack state | AxeItem/PickaxeItem/ShovelItem, native transmuteCopy component patch, actual30 switches preserve name/damage/enchants/runes, registry-aware stack codec roundtrip | PASS native suite; physical relog/repair matrix NOT_RUN
One natural-tree queue, no duplicate durability | Actual native ray/LeftClickBlock/game-mode origin break + existing queue: six trunk logs and one branch, lumberjack rune installed, damage7; leaves left to decay | PASS native suite; broad species/old builds/third-party claims NOT_RUN
Bounded permissions and no second digging system | Existing natural-tree journal/rejection, guarded native destroyBlock for every extra, four breaks/tick; no new3×3 implementation | PASS source; real claims-mod matrix NOT_RUN
Lift true safe surface, finite loaded search | Actual native heightmap ascent; magma rejected; failed attempt no charge; dimensions/border/fluids/body/sky/native permissions checked | PASS native suite; broad biomes/dimensions/connection tests NOT_RUN
Closed arena and cancellation | Actual EarthArena gate record rejects; open gate permits; native teleport event canceled without movement/wear/cooldown; native great final room and active Bogatyr duel guards | PASS native suite for Earth/cancel; other guard paths source only
Lift timing/uses/cooldown/Unbreaking |30 use ticks,64 durability,1200 cooldown, enchantability10 + native durability tag; actual ascent consumes once | PASS native suite; manual Unbreaking proc distribution NOT_RUN
Exclusive trades/finds and existing merchants | Actual specialist exposes items; idempotent offer migration; native prices use purified iron, not ancient coins;3 existing loot tables receive one bounded rare pool | PASS native suite/source; natural city exploration NOT_RUN
Production models/textures, creative, RU/EN | Six item models,12 original texture files, creative catalog, RU/EN;22 real frames final +22 initial | PASS resources and [client frames](client/); owner visual acceptance NOT_RUN
Source-based before/after model adjustment | [prior voxel JSON](model-before/), [initial captures](client-initial/), [current JSON](../../../src/main/resources/assets/slavicmyths/models/item/chud_mining_lantern.json); matching scene01 shows corrected native block-style hand rotation | PASS actual renderer comparison; newly introduced objects had no pre-existing model
Final clean build and regression | Java21 clean build47s;50,289 book +11,731 tool-rune offline checks;8/8 native final server tests | PASS covered checks; complete two-socket MP NOT_RUN
Installed test candidate | [receipt](../polymc-19764/installed.json),1.9.7.64 source3b00acaf, SHA/backups/duplicate0 | PASS disk; actual PolyMC launch NOT_RUN
Owner acceptance | Issue stays OPEN, Project «В работе» | NOT_RUN

**This task's actual process count:9 isolated GameTest servers +2 dev clients =11 Minecraft processes**, including failed attempts. Both clients exit normally and save their disposable worlds. No PolyMC process launched. Resource checker `runtime_launches=0` describes that checker alone, not this campaign. Native fake connections/server players do not establish two-socket multiplayer. No measured FPS/MSPT improvement is claimed for new tools.

Final client captures show six native items in first person/third person/inventory (creative InventoryScreen redirects to native creative inventory; new items are visible in the hotbar). Four additional dark-room frames verify removal/offhand behavior. There are22 final frames and22 initial frames. Second run changes only the voxel held-item rotation; colors/geometry/source pose/GUI/resolution are preserved. Vanilla glowstone is used for visual item frames1–17 and removed before dark-room checks18–21. These are actual screenshots, not concepts/software mockups. Owner visual acceptance remains mandatory.

Core files: industry/ChudTools.java, ChudLiftItem.java, ChudToolTrades.java; ChudPerson offer migration; Industry registry initialization; Runes capacity; existing ToolRuneRuntime/ToolRuneTrees/ToolRuneRules; client/ChudLanternLight and client-only mixins;6 item models,12 textures, RU/EN/catalog, vanilla family/enchantment tags,3 Chud loot tables. No existing model or audio changed. Generator: tools/chud_tools_258_resources.py.

## Owner test

1. Miner Chud sells lantern/master pickaxe; artisan sells multitool/lift. For isolated checks use `/give @s slavicmyths:chud_mining_lantern`, `chud_master_pickaxe`, `chud_multitool_axe`, `chud_mining_lift` (repeat the full namespace after each give).
2. Hold lantern in either hand in a dark cave; remove it and move/teleport. Check your shader/dynamic-light setup and another player.
3. Shift+right-click **air** with master pickaxe; test diamond ore in both modes, FortuneIII and installed mining runes. Only one mode applies.
4. Shift+right-click air to cycle multitool axe→pickaxe→shovel. Name/enchant/socket/wear must persist, inactive other-form rune must not gain a free effect. Ordinary axe fells one natural tree; with Shift, installed lumberjack rune keeps its existing mode. Check player builds are protected.
5. Hold right-click with lift1.5s underground: reach safe open surface, one use,60s cooldown. Check lava/magma, buildings overhead, no safe exit, boss arena denial and rejoin. Failed use must cost nothing.

Inherited #235/#236 limitations (first snapshot/command placement spikes, full combat chain, two socket clients) remain open; this package does not claim to resolve them. All Issues remain OPEN and no stable2.0 release is published.
