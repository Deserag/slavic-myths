# 08 — TESTING / ACCEPTANCE — ULTRA

No Minecraft client auto-launch.

---

# 1. GENERATOR UNIT TESTS

Generate at least:
50 Small,
50 Warrior,
50 Great
logical layouts headlessly if feasible.

Collect:
node count;
rooms;
loops;
secrets;
dead ends;
transitions;
primary path percentage.

Fail if:
Great loop count <4;
Warrior loop count <2;
Great optional node percentage <30%;
layout disconnected;
all transitions clustered;
primary route dominates beyond allowed threshold.

---

# 2. STRUCTURE COLLISION TESTS

Ensure:
- no room overlap;
- no corridor cutting through unrelated room;
- no impossible doorway into wall;
- no stair ending in ceiling;
- no container blocked;
- no secret room orphaned.

---

# 3. ROOM PRESENCE

Across sample set confirm every room archetype appears at intended tier frequency.

Do not require every room in every Kurgan.

---

# 4. LOOT DISTRIBUTION

For generated sample:
Small reward points 2–5 target.
Warrior 5–9.
Great 10–18.

Flag outliers.

---

# 5. BLOCK FAMILY

Verify:
- registry;
- blockstates;
- models;
- item models;
- recipes;
- stonecutting;
- loot/drop;
- tags;
- creative tab;
- JEI visibility.

---

# 6. ART FILE VALIDATION

For every redrawn Kurgan texture:
- dimensions >=256×256;
- PNG valid;
- alpha expected;
- no missing model references.

Generate a report listing texture dimensions.

---

# 7. MANUAL QA DOCUMENT

Create manual checklist including:

A. Find Small Kurgan.
B. Verify entrance.
C. Confirm first section not direct endless stair.
D. Find branch/dead end.
E. Verify room identity.
F. Verify loot.
G. Repeat for Warrior.
H. Repeat for Great.
I. Confirm multiple descents are physically separated.
J. Confirm loop allows alternate return.
K. Find secret.
L. Check flooded room.
M. Check trap fairness.
N. Compare Cold Burial Stone to vanilla stone.
O. Build 5×5 wall of each new block.
P. Test slabs/stairs/walls.
Q. Test stonecutter.
R. Test survival recipes.
S. Inspect all Kurgan item icons.
T. Save/reload.
U. Check dedicated server later.

---

# 8. PERFORMANCE

Great Kurgan generation must not freeze the server due unbounded retry.

Use bounded graph/layout attempts.
Log regeneration reason in dev mode.

---

# 9. FINAL CODEX RESPONSE

Return factual:
- layout algorithm;
- exact room types implemented;
- exact block IDs added/reworked;
- recipe list;
- texture file dimensions;
- item art audit result;
- loot tables;
- automated test statistics;
- build result;
- manual QA remaining.

Do not say "looks good in game" unless user manually tested it.
