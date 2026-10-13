# Book discoveries, precise state machine and honest approximate location system

## Server-owned event mapping
Known event hooks (match actual current implementation; do not invent API): `entity_seen`, `entity_interacted`, `entity_defeated`, `boss_defeated`, `location_discovered`, `item_obtained`, `food_cooked`, `ritual_completed`, `recipe_used`, `quest_started`, `quest_completed`, `advancement_awarded`. One authoritative dispatcher normalizes `{eventType, sourceId, subjectId, playerUUID, dimension, gameTick, extra}`; deduplicate via event-specific idempotency. Never mark boss `DEFEATED` on seeing loot, when another player kills it, or on death by unrelated means unless existing boss credit handles group participation.

## Data ownership & volume
- Player persistent fields: schemaVersion integer; `bookGranted` boolean; `discoveredEntries` sparse map `{entryId:stage,int flags,timestamps}`; `heardRumors` map `{hintId,anchorId,centerOffset,radius,validUntil,status}`; `readFlags` set; optional menu last-view state CLIENT ONLY; no world scan per open.
- Registration data: datapack-side book entry JSON and translations, generated coverage manifest.
- Events must preserve existing identifiers, save on mutating operation, never regress on relog/clone/end portal/death; handle removed entries gracefully (still store their IDs for migration), ignore broken JSON with actionable errors not crash entire mod.
- Content size budget: 1–4 KB changed per update, full snapshot on initial book open; cap packet size and per-player stored hint count (max 64 active), avoid sending an entire thousands-entry registry in a single client packet.

## Location hint semantics — firm promise to player
1. Source must have **real resolved entity HOME anchor** or **real world structure anchor** at time of rumor. Book cannot generate fake markers merely because dialogue says “probably somewhere”. Use existing known NPC/structure lookup or reference captured on encounter/generation.
2. Choose deterministic world/target/rumor seeded offsets bounded by 25–80 blocks from actual anchor, search radius default 250 blocks (config 200–350); ensure target anchor lies in circle. For moving NPC anchor, region centers on saved home or credible last seen position; after moving outside region by >radius, mark stale.
3. Direction text from **player position at time clue received**, 8-point compass (N, NE, E...) and coarse distance band: «поблизости» <500, «неподалёку» 500–1500, «вдалеке» >1500. Do NOT invent landmarks not found in world data. Prefer recognized real biome + structure if available; else «в хвойных лесах к северо-востоку».
4. Region is per player, per dimension; do not display Overworld circle as if it were in Nether/End. `/tp`, target removal, server migration invalidates stale hints sensibly.
5. No click-to-teleport, no coordinates copy button, no exact HUD marker at this stage. Area drawn on book map; hints should be useful even WITHOUT Xaero. Zoom 1.0/1.5/2.0 and north compass; map terrain only if underlying mod supports known map data, otherwise clearly schematic.
6. In this final 1.6 patch demonstrate on **Kot Bayun home anchor** after being told rumor by an EXISTING known source with implementable way to obtain, or on another actually present existing place. Do NOT spawn future bogatyr or create claim its location exists.

## No magical retroactive knowledge
- `discovered` cannot arrive through mere registry load.
- Rumor of hostile boss may be initially visible in legendary part; verified attack pattern only when actual event earned.
- Peaceful unique beings (Bayun, Baba Yaga, Firebird) use `befriended/trial_complete/interacted` for verified; never require kill.
- Source data for loot links is existing loot table registry, not speculative rewards.

## Multiplayer / performance
- Verify encounter credit for players individually, not all players in radius indiscriminately.
- Entity proximity discovery scan at most once every 10–20 ticks for active player within a targeted entity tracking area, event-first; no world-wide entity searches each tick.
- If location hint cache isn't available when player first reads, show clear Russian «Пока нет достоверных сведений о местонахождении» rather than a fake map.
