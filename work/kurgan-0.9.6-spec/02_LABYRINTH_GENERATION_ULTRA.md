# 02 — LABYRINTH GENERATION — ULTRA DETAIL

# 1. PROBLEM TO ELIMINATE

The current experience resembles:
entrance
→ staircase
→ floor
→ staircase
→ next floor
→ staircase
→ final room.

This is explicitly rejected.

The new dungeon must be a graph of routes.

---

# 2. ABSTRACT GRAPH

Build an abstract graph before placing room geometry.

Node types:
- ENTRANCE
- VESTIBULE
- JUNCTION
- ROOM
- REWARD_ROOM
- DANGER_ROOM
- SECRET_ROOM
- TRANSITION_UP
- TRANSITION_DOWN
- DEEP_OBJECTIVE
- DEAD_END

Edges:
- NORMAL_CORRIDOR
- RUINED_CORRIDOR
- DAMP_CORRIDOR
- CEREMONIAL_CORRIDOR
- SECRET_PASSAGE
- VERTICAL_TRANSITION

Each node has:
- depth band;
- orientation;
- footprint estimate;
- connector count;
- mandatory/optional flag;
- room archetype;
- seed.

---

# 3. GRAPH TARGET METRICS

## Small Kurgan
Nodes: 7–12.
Rooms: 2–4.
Optional nodes: at least 25%.
Loops: target 1, minimum 0 only for very small valid variant.
Dead ends: 1–3.
Secrets: 0–1.
Depth bands: 1–2.

## Warrior Kurgan
Nodes: 14–24.
Rooms: 5–8.
Optional nodes: 30–45%.
Loops: minimum 2.
Dead ends: 2–5.
Secrets: 1–2.
Depth bands: 2–3.

## Great Kurgan
Nodes: 28–45.
Rooms: 8–14.
Optional nodes: 35–55%.
Loops: minimum 4.
Dead ends: 4–9 but not all empty.
Secrets: 2–4.
Depth bands: 3–4.

---

# 4. PRIMARY ROUTE LIMIT

The shortest required route from entrance to deepest mandatory objective MUST NOT consume almost every node.

Target:
- Small: primary route <= 70% of nodes.
- Warrior: <= 60%.
- Great: <= 50%.

This guarantees side exploration exists.

---

# 5. LOOP REQUIREMENTS

A loop means the player can leave a node along one route and later reconnect to a previously reachable route without reversing the exact same corridor.

Valid loop examples:
- two branches reconnect;
- secret passage reconnects;
- lower-level connector returns to alternate upper branch;
- side corridor circles a room cluster.

Invalid:
- decorative alcove that returns to same corridor after 3 blocks;
- same staircase widening then narrowing.

---

# 6. VERTICAL TRANSITIONS

## Rule
Different level changes MUST be physically distributed.

### Warrior
Between adjacent depth bands:
at least 1 mandatory transition.
Prefer an optional second transition on at least one pair of bands.

### Great
Between adjacent depth bands:
target 2 transitions where footprint allows.
At least one pair of depth bands must have 2 physically separated transition paths.

Distance between alternate transition entrances:
prefer at least 20 horizontal blocks or separate branches.
Do not place both next to each other.

## Transition archetypes
A. Wide stair:
3 blocks wide, landing every 5–7 vertical steps.

B. Side stair:
turns 90° one or two times.

C. Broken descent:
partial stairs + short 2–3 block drop/ruin route, with safe traversal.

D. Narrow shaft:
compact vertical connector with current valid climbing mechanism.

E. Secret descent:
hidden alternative route, Great Kurgan only or rare Warrior.

No endless 1-wide ladder shafts as the main route.

---

# 7. CORRIDOR DIMENSIONS

Ordinary:
width 3–4 clear.
height 4–5 clear.

Ceremonial:
width 5–7.
height 5–7.

Narrow secret:
width 1–2.
height 2–3.
Never use for required main route.

Ruined:
nominal width 3–4, locally narrowed by rubble but maintain passability.

Damp:
3–5 wide, may include 1-block water channel.

---

# 8. CORRIDOR LENGTH

Preferred ordinary segment:
5–18 blocks between meaningful features.

Soft maximum:
24 blocks.

Hard maximum without feature:
28 blocks.

If longer:
insert at least one:
- 90° bend;
- support bay;
- wall niche;
- small junction;
- elevation step;
- collapsed section;
- room doorway.

---

# 9. CORRIDOR ARCHITECTURE

## Ordinary burial corridor
Floor:
60% custom Burial Bricks/Cold Stone,
40% vanilla stone/stone brick variants.

Walls:
lower band durable,
upper band mixed.

Every 4–6 blocks:
support pier, wall recess or arch rib.

## Ruined corridor
20–40% visible damage palette:
cracked custom brick;
vanilla cracked brick;
cobblestone;
gravel at collapse edge.

Do not reduce passable route below intended minimum.

## Damp corridor
Lower walls:
moss/damp custom blocks + vanilla mossy stone brick.
Floor:
occasional shallow water patch.
Ceiling:
darker intact block; no excessive vines if underground logic does not support them.

## Ceremonial corridor
Symmetry.
Carved block markers at arch spring points.
Cleaner floor.
Used before major rooms.

---

# 10. JUNCTION DESIGN

## T-junction
Minimum room/corridor width 5 at intersection.
One focal wall element opposite incoming branch.

## Four-way
Use 7×7 or 9×9 junction cell.
Central low marker or floor design.

## Offset fork
Two routes split with 3–5 block staggering so player cannot see both endings immediately.

## Asymmetric fork
One route broad/intact, one narrow/ruined.
Do not always make broad route correct.

---

# 11. DEAD ENDS

Dead ends are required but must not feel like generator failures.

Distribution:
- ~40% minor reward/niche;
- ~25% atmospheric remains;
- ~20% trap or suspicious false route;
- ~15% secret switch/reveal opportunity.

Do not make every dead end contain a chest.

---

# 12. SECRET PASSAGES

Secret entry tells:
- cracked 2×2 wall field;
- carved block slightly different from adjacent pattern;
- shallow niche with false back;
- broken masonry gap.

Do not use invisible impossible-to-guess one-block holes.

Secret passage length:
3–12 blocks.
May connect:
- treasure room;
- reliquary;
- alternate descent;
- loop shortcut.

---

# 13. ROOM PLACEMENT

Place high-identity rooms apart enough that they do not overlap and all connectors have clearance.

For each room:
- reserve footprint + 2 block shell + safety margin.
- check vertical volume.
- reject overlapping room or adjust graph embedding.

Avoid rooms sharing wall unless there is an intentional secret connection.

---

# 14. LEVEL BANDS

Do not treat each band as a perfectly flat floor.

Within a band:
allow Y variation ±2–4 through:
- short steps;
- half-level landings;
- slopes represented by stairs.

This makes the complex feel excavated/constructed rather than office floors.

---

# 15. ENTRANCE-TO-OBJECTIVE PATH

Minimum path richness:

Small:
at least 2 meaningful direction choices total.

Warrior:
at least 4 choices/junction decisions before deepest objective.

Great:
at least 7 decisions/junctions distributed across route.

Do not fake decisions where both branches immediately reconnect in 3 blocks every time.

---

# 16. GRAPH VALIDATION

Before block placement validate:

- mandatory nodes connected;
- entrance reaches objective;
- optional node percentage;
- loop count;
- dead-end count;
- secret count;
- transition separation;
- no single node degree >5 without deliberate hub;
- no 80% linear chain;
- no transition cluster;
- no invalid vertical crossing;
- no impossible room overlap.

If invalid:
regenerate graph or repair before placement.

---

# 17. LAYOUT DEBUG EXPORT

Create an optional textual/JSON report:
`build/reports/slavicmyths/kurgan/<seed>-<type>.json`

Include:
- node list;
- edges;
- depth bands;
- room types;
- transition nodes;
- loop count;
- optional percentage;
- container count.

Optional developer command may print summary.

---

# 18. DETERMINISM

Given:
world seed + structure position + Kurgan variant seed,
layout must be reproducible.

Do not use uncontrolled global random state.

---

# 19. PERFORMANCE

Do graph generation before expensive block placement.
Keep retries bounded.
Do not repeatedly generate full layouts every tick.
