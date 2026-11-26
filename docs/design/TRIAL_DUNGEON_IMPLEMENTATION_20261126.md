# Trial dungeons — first implementation stage (2026-11-26)

Working branch: feature/desert-biome-20261125. Owner approved starting implementation; original concept #542.

This commit is a **geometry / command prototype**, not a completed survival dungeon.
Admin command: `/slavicmyths dev dungeon generate desert [floors] [seed]` (3–5, default 4).
Uses the command position as horizontal origin. Fits below surface when possible, otherwise raises the entire plan above surface. Placement is queued on the server thread. The command is destructive within the generated footprint; use a disposable world. Survival block breaking is cancelled in registered footprints. Do not use a live save yet.

Included: seeded independently varied floor graphs, loops, different room types, one trial and boss hall per floor, larger 65x59 / 91x77 arenas, physical ladder shafts, fixed biome palette entry point, sparse light, persistent site reservations, boss roster with separate / combined final encounter.

Not yet implemented: natural standalone well replacement and entry construction, precise geometry/route checks (including corridor crossings), gate locks, actual trial blocks / loot / boss spawning and +50% HP +25% all attack damage, protected secret access, explosion/piston/placement safeguards, recovery of interrupted generation, data-driven biome palettes, multiplayer / runtime / Gradle verification. Current command produces geometry only; not a gameplay-complete structure. Existing kurgans are unchanged.

Standalone smoke test: compile DungeonPlan.java and DungeonPlanSmoke.java with javac -d out; java -cp out org.slavicmyths.dungeon.DungeonPlanSmoke.
