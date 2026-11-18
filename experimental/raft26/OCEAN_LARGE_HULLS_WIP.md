# Ocean 2.6 — medium and large hull WIP

This branch is intentionally separate from the working `fix/2.6-ocean-crew-sails-20261108` patch. **These hulls are not playable or installable**: no entity type, shipyard upgrade button, or world mutation is enabled.

Implemented here: deterministic pure Java draft geometry for 13×21 and 21×33 hulls, full-block keel, two-block-high dry hold, real hatch and ladder positions, cabin shell, door/window placeholders, raised wheel deck and cabin ladder; plus proportional materials preview and an explicit `movementApproved=false` gate.

Block moves require capturing and restoring all source and target block entities, preserving container orientation and inventory NBT, migrating the current 7×11 cargo grid losslessly, checking the destination and source footprint for players and blocks, chunk load budgets and performance instrumentation before becoming functional. Naïvely moving thousands of blocks every 4–10 ticks is not acceptable.

Run only the pure-Java draft audit:

```powershell
New-Item -ItemType Directory -Force build\ocean-large-proof | Out-Null
javac -encoding UTF-8 -d build\ocean-large-proof src\main\java\org\slavicmyths\ships\experimental\OceanLargerHullBlueprint.java experimental\raft26\OceanLargerHullProof.java
java -ea -cp build\ocean-large-proof OceanLargerHullProof
```

No live server or multiplayer tests have been run for medium/large hulls. **Do not merge this branch as a finished feature.**
