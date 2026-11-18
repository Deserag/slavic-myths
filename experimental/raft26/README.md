# Slavic Myths 2.6 «Океан» — isolated raft technology spike (#433)

**State:** IN_PROGRESS / offline proof only. **Do not merge** as a gameplay feature or ship a JAR from this prototype.

## Owner-approved requirements (08.11.2026)

- Starter raft 3×3, four players maximum, one rower controls a pair of oars; remaining players should later be able to walk freely on a moving deck.
- Water-only navigation. The raft hull cannot be destroyed by ordinary damage; only a special controlled packing tool may dismantle it.
- Equipment may be placed in any **free allowed deck cell**, not in preset slots. Whitelist at launch: chest, barrel, crafting table; furnace only after a separate working-smelting proof. Ordinary construction blocks, portals, redstone machinery and Create factories are not allowed.
- A server-authoritative identity and locally addressed equipment. Inventory content must not be dropped and recreated every movement tick.
- Load-dependent heel, recoverable capsize; a single player can manually right the little raft more slowly than multiple players.
- Create is **not** a required dependency for Slavic Myths. Investigation of a standalone physics adapter (e.g. Sable) is allowed only after compatibility and licensing checks. Later upgrades lead from raft to sailboat, full ship, and a fairy-tale flying ship (not an airship).

## Contents

- `Raft26.java`: dependency-free Java 21 **logical state simulator**, intentionally outside `src/main`. Simulates bounded 3×3 deck coordinates, 4 occupant identities, one rower, water-only movement, equipment whitelist, separate local cargo slots, cargo weight, heel/capsize, single/multiple pusher recovery, bounded versioned snapshots and guarded packing.
- `Raft26Proof.java`: 24 deterministic logical assertions.

Run from repository root with Java 21:

```bash
mkdir -p /tmp/raft26-proof
javac -encoding UTF-8 -d /tmp/raft26-proof experimental/raft26/Raft26.java experimental/raft26/Raft26Proof.java
java -ea -cp /tmp/raft26-proof org.slavicmyths.ships.prototype.Raft26Proof
```

Expected: `RAFT26 OFFLINE PROOF PASS: 24 assertions. GAMEPLAY/NEOFORGE/MP NOT_RUN.`

## Evidence and limitations

| Requirement | Result |
|---|---|
| Java 21 compile and 24 logic assertions (local isolated JDK 21 check 08.11.2026) | PASS |
| Water-only movement model, bounded equipment/occupancy, save-load mock cargo and guarded packing | OFFLINE_PASS |
| Heel, capsize, solo/pair restoration simulation | OFFLINE_PASS; rates/thresholds deliberately provisional |
| Actual Minecraft moving 3×3 block deck, collision, player walking/jumping, networking | NOT_RUN |
| Actual vanilla ItemStack/DataComponents and chest/barrel menu; furnace ticking | NOT_IMPLEMENTED |
| Server SavedData/Entity persistence, game restart, crash-atomic packaging, chunk loading | NOT_IMPLEMENTED |
| NeoForge client/server build and GameTests on `release-2.5` baseline | NOT_RUN |
| 2–4 real players, user/client screenshot and owner acceptance | NOT_RUN |

**Do not confuse serialized strings in `Stack.components` with real vanilla ItemStack codecs.** This model only proves that our *own* locally referenced cargo state is independent of simulated ship world coordinates. It does not prove no dupe at the boundary between an actual ship entity and a dropped ship item; that operation will need a server-side one-time transfer with crash recovery.

## Next hard gate: moving-deck game prototype

Build the smallest isolated runtime adapter against **actual NeoForge 1.21.1**:
1. Register a disposable testing raft in a separate opt-in experimental source set, or an isolated test mod. Preserve the release registry and production resources untouched.
2. Compare (A) own moving entity with custom physical top collision and server-validated carrying of walkers; (B) true moving-block adapter through independently compatible physics library. Never require Create solely to get its factory machinery.
3. Demonstrate a player **standing, walking, jumping and voluntarily leaving the moving deck**, without being auto-mounted, in client and dedicated server. Check turning, lag, shore collisions, player disconnect and chunk boundaries.
4. Demonstrate exactly one shared **real** 27-slot chest using Minecraft ItemStack codecs/DataComponents. Move, save/reload, unload, restart, pack/unpack; verify no item loss or dupe.
5. Only after the walking and chest gates pass, register the shipbuilding table, oar input, prototype art and crafting recipes, followed by rescue mechanics.

Do **not** mark the Issue DONE while any hard gate remains NOT_RUN, and do not merge to `release-2.5` until explicit owner approval. Related concept: #385.