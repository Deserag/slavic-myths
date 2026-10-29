# QA: 2.2.5 Kuznechny dvor

Model generated from exact voxel coordinates. Placed blocks: **1901**; floor y0: **676**; physical chest count **5**. Source positions locally [0,25]^2, y=0..17. Traversable flood-fill at y=1..2 from (25,12), counting standard openable crimson doors as passable: **487 ground positions**, including forge and shed interiors. This flood-fill is a preliminary geometric check, **not a Minecraft runtime test**.

Loot table drafts: metals, fuel, parts, tools, supplies. Bonus pool 12% silver ingot (max 1) and bonus pool 10% silver fitting (max 1); no netherite/diamonds.

Warning: visual R1 art is an artistic approximation; full voxel JSON governs block placement. Must check door hinges, chest openability, roof, campfire, inventory and worldgen in real client before calling implemented.
