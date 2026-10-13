# Server-authoritative programming contracts and edge cases

## Suggested entities and data objects (map to real repo APIs)
- `DobrynyaEntity`: regular ally AI controller + persistent `ownerUUID`, `summonSlot`, `heroState`, `phaseActionId`, health, cooldown sequence, transformed variant.
- `MarinkaBossEntity`: `encounterUUID`, `bossPhase`, `barrierState`, `shadowState`, `shadowGroupId`, `curseTriggered`, `activeAction`, `cooldowns`, current target, boundary origin.
- `MarinkaCloneEntity`: link `encounterUUID`, parent `realEntityUUID`, internal `remainingHP=24`, clone TTL and animation schedule. No loot/drops; no independent bossbar.
- `WitchYardEncounterController`: `arenaUUID`, world center (XYZ), structure orientation, start/progress/victory state, authorized hero ID, locked party, `basementUnlocked`, placement/generation status, active participants.
- `HexFocusEntity/BlockEntity`: anchor ID/HP, stable location, animation state, destruction flag; flags replay-safe.
- `AurochsForm`: model/renderer variant or entity that retains Dobrynya UUID/equipment/owner; do NOT lose persistent properties on shapeshift.
- `WorldStoryFlags`: player defeat timestamp, ally involvement, curse release, secret room discovery, potential 1.7.6 eligibility flag, unique award unclaimed/claimed placeholder only if current reward architecture supports it.

## State machine invariants
- `IDLE -> APPROACH -> PRE_FIGHT -> FIGHT_PHASE1 -> FIGHT_PHASE2 -> FIGHT_PHASE3 -> VICTORY -> EXPLORE`; failure can go from any active fight to RESET. Boss phases monotonic.
- Server assigns each action a monotonic `actionSequenceId`, startTick, valid target UUID and event payload. Only server deals damage/summons clones/sets locations/unlocks hatch; renderer reads replicated info. Invulnerable/false states have finite timeouts.
- Only one Marinka original exists per active arena; no more than two clones; no summoned herd beyond two; one hex anchor; one transformed Dobrynya. On combat cleanup remove all spell projectiles and mobile temporary objects owned by encounter.
- Summon replacement lock stays in force throughout `FIGHT*`, including hero transform/retreat. Prevent duplicate Dobrynya ownership on login, server restart and dimension changes.

## Serialization test matrix
1. Reload mid-ward (should keep/cancel with correct duration, cannot gain shield for free).
2. Reload when 3 forms alive (either restore clones with exact HP/timers OR clear clone phase and reset its cooldown, never 3 real bodies).
3. Reload mid-Dobrynya-aurochs (must recover original hero UUID and reconnect anchor; if anchor missing due migration, safely revert).
4. Reload at 15% retreat animation (resume or cleanly finish recovery without healing beyond cap).
5. Reload between boss death and stair opening (always unlock, no stone suffocation).
6. Two players interact with one ritual marker simultaneously (only one encounter instance).
7. Player disconnects in special combat: boss shouldn't fight empty world forever; safe rollback/cleanup policy.
8. Structure chunks unloaded during boss reposition: never teleport into an unready chunk; defer or cancel.

## Security and performance
- No client-supplied hit-damage IDs, clone claims, reward claims, or unrestricted spawn coordinates. Validate interaction distance<=5b and server entity context.
- Each actor pathfind update bounded <=5Hz where feasible; particle caps in view (<250 simultaneous in normal settings, <80 minimum); audio throttling; clone attacks per group cap; 60x60 structure generated without blocking normal tick loop for extended time.
- Avoid recursive phase start, clone-regenerating recursion, per-tick NBT writes, repeated chunk ticket churn or packet storms; save only significant state transitions.
- Debug traces behind existing `enable_debug`/dev configuration, no release tick logs. Diagnostic fields should be exact state/UUID/delta; prefer server counters to verbose every-frame console.
