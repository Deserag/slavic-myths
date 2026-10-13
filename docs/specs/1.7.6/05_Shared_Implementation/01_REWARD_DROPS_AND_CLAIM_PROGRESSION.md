# Source of truth for drops, historical completion and claim-once rewards

## 1. Authorized events
Sources in exact order: Ilya/Idolishe (1.7.1), Dobrynya/Marinka (1.7.2), Alyosha/Skim (1.7.3), Finist/Kartaus (1.7.4), Kalin (1.7.5). Use existing registry and boss victory ledger; NEVER identify by displayed name or translated text.

## 2. First-clear boss grant
For each eligible player on a real encounter victory: atomically persist `rewards176.<bossKey>.firstClaimed=true` only when grant succeeds; give associated rune x1 and fixed initial material amount (Idol 4, Marinka 4, Skim 3, Kartaus 4, Kalin 5). If inventory full, put rewards in player-bound pending claim inbox/explicit UI entry (prefer existing Book of Tales interaction) rather than dropping unbound stack and marking claimed. If no ready inbox exists, retain claim pending with safe retrieval via existing progression NPC/interface. Store exactly-once persistent claim IDs, not only an item `count`.

## 3. Retroactive
On player login or first relevant reward UI access, read already-persisted boss victory flags and quest completion flags. Show eligible unclaimed items in existing Book of Tales if it supports reward entry, otherwise in existing related NPC/quest interaction; do not force replay. If flags are absent, never infer victory from item possession or version number. For first-clear loot that was already granted after patch, claim status prevents duplication.

## 4. Hero gifts
Separate four one-time grants from boss-specific rewards. The respective hero must have its completed story state and boss victory as in previous release. Gift interaction: speak/interact with the corresponding hero; if the current hero UI exists, add one `Получить дар` line. Do NOT exchange, upgrade or consume the hero's non-consumable summoning item. Gift can be reclaimed only if a pending grant was never delivered. No repeat claim after losing/dropping a received item by default.

## 5. Repeat legitimate encounters
Repeat boss victory yields only respective material at random count: Idol 1–2, Marinka 1–2, Skim 1–2, Kartaus 1–2, Kalin 2–3. Additional rune chance 15% per properly cleared repeat, 0 on normal mobs. Use server-side loot table/state event with one roll per boss death/surrender. Do not distribute to participants at great distances or offline without authenticated contribution/encounter membership. If existing 1.7.1–1.7.5 system already handles participant credit, follow it.

## 6. Anti-exploit + multiplayer
Reward ID = serverWorld/saveUUID + playerUUID + bossRegistryKey + rewardVersion, plus firstClaimed and delivery state. Idempotent by design. Never trust inventory alone. For co-op, count players recorded in real encounter and eligible by participation rules; if existing victory ledger marks everyone, preserve its semantics rather than invent a brand-new participation service. Replay tests: reconnect mid-grant, two simultaneous claims, missing inventory capacity, unloaded chunk, boss instantly killed by command, clone kill.

## 7. Source-specific rooms
Marinka's ritual basement and Skim den can contain additional vanilla resources; adding boss-exclusive materials in chests is only allowed if clearly tied to the per-player first claim and prevents shared chest farming/duplication. Prefer grants through existing victory ledger to avoid modifying worldgen/structure files in 1.7.6.
