# D2 — temporary terrain and cross-player reward cuts

## Implementation

TemporaryTerrain attaches Version1 Ground+Leases directly to its generating chunk. New islands mark only their actually written natural floor, max256 ground cells/chunk. LevelChunk.setBlockState invalidates marks and leases on ANY external write, including player replacement by the same-looking stone. Old chunks without this provenance are refused. RuneII turns one proven blackstone/basalt/obsidian under its player into magma for120t (engineering baseline), then restores exact BlockState. Response damage is preserved even if safe placement refuses.

Each lease contains original/placed states,owner UUID,expiry in overworld game clock. Lease is installed BEFORE setBlock and protection callback so even a callback forcing a chunk save sees a recoverable pair. Restore writes original BEFORE removing the lease. Both block and record serialize in one chunk image: cuts yield original/no lease, original/pending lease (mismatch->no replacement), magma/lease (restore), original/lease (remove), original/no lease. There is no assumption that an independent SavedData flush precedes a chunk save. Unknown versions are left unmodified.

Only server thread, loaded chunks and worldborder. Checks mayInteract/mayUseItemAt and cancellable NeoForge EntityPlaceEvent; cancellation restores only our placed state.32 leases/chunk,512 tracked resident chunks max; no global tick loop. Block scheduled ticks expire live leases, and Load/Unload/logout/death/dimension/clean ServerStopping restore known leases. Other replacement is not overwritten. Break/piston/explosion guards prevent harvesting/moving temporary magma. Player/tame/protected NPC contact is guarded. Actual claims plugin and event ordering runtime NOT_RUN.

The same helper provides bounded real minecraft:fire for later weapon hits:4 cardinal positions,radius1,TTL60,proven natural floor only, no BEs/unknown/flammable nearby, respect doFireTick and mobGriefing. Only leased fire scheduled ticks are intercepted, preventing vanilla spread; BaseFireBlock contact skips players/pets/allies, avoiding lingering friendly ignition. No safe proof ->0 placements, never permanent fallback. Fire numbers are BALANCE_PROPOSED, not owner-approved. Weapon calls come in G2.

## Evidence and limits

TerrainLeaseAudit:24 actual native binary NBT/state/expiry/replacement checks including basalt axis; this does NOT instantiate a chunk or simulate actual game relog. Source serialization coupling is architectural, real chunk saving/protection/piston/explosion/restart remains NOT_RUN.

Existing BossRewardAuthority/Inbox was already implemented; no second reward engine. Added4 deliberately halted CHILD JVM cuts after pin,world save,player coupled inventory+receipt save,and world rollback. New authority instances cannot reassign A to B or reroll4 to6; A without saved player receipt can recover once, saved receipt remains one. This is actual filesystem/process-halt testing of authority and simplified coupled player-file model, NOT an actual Minecraft server process crash or PlayerDataStorage test. Full inventory and source native inventory admission gates retained. Power loss/filesystem durability or inconsistent backup of authority itself is not proven; use consistent world+players+authority backup.
