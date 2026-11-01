# Issue #391 — Morovoy Raven crater arena (implementation checkpoint)

Arena-only work. No Raven entity, AI, rewards, runes, quests or new gameplay items.

Native worldgen structure: 241x241 chunk-referenced bound, irregular ~236-block open crater. Central flat combat floor with basalt/blackstone terraced outer cliffs; validated exterior cave exits yield descending cuts. No columns or hanging decorations. Roof clearance carved through Y=186 without touching bedrock. Future flight metadata from structure-piece NBT: center, 120 radius, 55 combat radius, 70 flight radius, floor+30..169 flight Y, future anchor and access points.

Placement: allowed five vanilla Nether biomes; deterministic random-spread spacing 128 chunks, separation 80 chunks, salt 2301391; rejection on sampled base-noise block/biome hazards, missing cave access, and accepted vanilla/modded structure footprint overlaps. No fixed (0,0), loaded-chunk retrofits, global tick scans or chunk tickets. Every placement is clipped to its chunk. Samples cannot prove full terrain safety and runtime validation is required.

Offline geometry command:
javac -d build/morovoy-geometry src/main/java/org/slavicmyths/morovoy/MorovoyArenaGeometry.java tools/qa/MorovoyArenaGeometrySmoke.java
java -cp build/morovoy-geometry org.slavicmyths.morovoy.MorovoyArenaGeometrySmoke

## Acceptance matrix
| Requirement | Evidence | Status |
| --- | --- | --- |
| Pure geometry, five seeds | Java smoke harness | NOT_RUN |
| Java21 clean build and NeoForge | Gradle/CI logs | NOT_RUN |
| Five actual seed-generated Nether craters | Server logs, coords, chunks | NOT_RUN |
| Appearance and four Minecraft screenshots | In-game visuals | NOT_RUN |
| Flight clearance and chunk boundaries | Runtime survey | NOT_RUN |
| Accessible natural entrance in survival | Actual player test | NOT_RUN |
| Posad/vanilla structure collision, old saves | Server tests | NOT_RUN |
| Save/reload, /locate, duplicate avoidance | Dedicated server tests | NOT_RUN |
| Generation timing and no chunk force-load | Profiler / logs | NOT_RUN |

No gameplay test or owner acceptance is claimed. Remains IN_PROGRESS until runtime checks.
