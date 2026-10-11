# Robustness, multiplayer, server and resource constraints

## Performance
≤1 AI state decision/second per Kuzya, unless current animation completes; local anchor pathfinding at most once per 2 seconds; no per-tick full structure find. ≤2 noncombat audio events/15s on average except eating scene. Limit nearby client sound channels so 4 players do not multiply speech 4×. Passive stationary NPC CPU budget comparable to villager or less.

## Multiplayer
Per guest timer: player UUID; choose target for look/grumble as nearest eligible overstay player, pick one voice to all clients nearby; other visitors see accurate same face/body pose. Two right-clicks same tick -> one speech selected. Saving game while Kuzya climbs must safely resume at ground/stove anchor. Several huts in loaded chunks maintain independent household events and IDs.

## Failure modes
Missing template marker→log and fallback to nearest valid safe floor; missing voice entry→use another included original line in category, NEVER silent; missing food prop→hand remains empty but eating action skipped, not invisible solid attachment; broken path to bed→choose floor chair rest; missing loot-table item ID→replace with verified vanilla fallback, not crash; previous structure existing→no destructive auto-upgrade; `/reload` resources while game active→new `sounds.json` exists.

## Data upgrades
All entity persistent schema values must carry `data_version=1`; detect absent keys (legacy) then initialize safely. Validate config values against min/max (time, probabilities, distances) to prevent zero-delay voice spam and tick overflow. Don't introduce dimension-global client-only static classes.
