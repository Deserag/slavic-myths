# 08 — RC2 TESTING & ACCEPTANCE
## DO NOT AUTO-LAUNCH MINECRAFT

---

# A. RUNE ANVIL

Test:
- open;
- item input;
- rune input;
- no Catalyst primary slot;
- result preview;
- installed rune slots;
- effect info visible;
- compatibility visible;
- Survival XP;
- Creative XP=0;
- install;
- remove;
- reforge;
- shift-click;
- close returns uncommitted items;
- reconnect/reload.

GUI:
2/3/4.

---

# B. PATH STONE

Test:
- open;
- selected path visibility;
- skill list;
- exact effect detail;
- XP cost;
- Creative zero cost;
- choose primary;
- choose secondary if supported;
- reset;
- navigation marker;
- failure message.

GUI:
2/3/4.

---

# C. KITCHEN

Test:
each recipe family:
- ingredient counts;
- required tool;
- result count;
- food/saturation;
- effect;
- missing item;
- missing tool;
- inventory full;
- cook valid;
- recipe switch;
- scrolling.

GUI:
2/3/4.

---

# D. BOOK

Test:
- no blur under text;
- long article scroll;
- category switch;
- GUI 2/3/4.

---

# E. DRUZHINNIK SHIELD

Inventory icon:
front face.

First person:
main/offhand.

Third person:
main/offhand.

Block:
orientation.

Axe disable behavior.

Repair with existing intended material/wood if already defined.

Front decorative face must always face outward.

---

# F. CLUB / BATTLE AXE

For each:
- inventory;
- dropped;
- first person right;
- first person left;
- third person;
- attack animation.

No broken geometry.

---

# G. WOOD

Lineup:
Willow / Rowan / Pine / Darkened.

Check:
log;
stripped;
planks;
door;
trapdoor;
stairs/slabs;
fence/gate.

No tooltip during visual recognition test.

---

# H. RESOURCE VALIDATION

Check:
- texture paths;
- dimensions;
- model parents;
- atlas;
- translations;
- duplicate IDs.

---

# I. BUILD

Run:
clean build using project wrapper.

No Minecraft auto-run.

---

# J. CODEX FINAL REPORT

Keep final report factual.

List:

1. files changed;
2. Rune Anvil status;
3. Path Stone status;
4. Kitchen status;
5. Book status;
6. Shield orientation status;
7. Club status;
8. Battle Axe status;
9. wood families status;
10. build result;
11. manual QA remaining.

No generic:
`UI improved`.
