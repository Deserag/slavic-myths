# 06 — WOOD FAMILY VISUAL REWORK
## CURRENT SETS LOOK TOO MUCH LIKE RECOLORS

Target:
`references/target/ref_target_04_wood_shield_club_battleaxe.png`

Current:
`references/current_problems/ref_problem_02_wood_blocks_current.png`

---

# 1. GOAL

The player should identify the wood family from:
- bark;
- stripped log;
- planks;

without reading tooltip.

Do not solve identity only by hue.

Use:
- grain direction;
- knot pattern;
- bark plate structure;
- plank cut/joint pattern;
- contrast.

---

# 2. WILLOW / ИВА

Identity:
light, moist, fibrous, greenish.

## Bark/log
- pale gray-green base;
- narrow vertical fibrous grooves;
- occasional darker mossy line;
- not smooth beige.

## End grain
- pale yellow-green core;
- thin subdued growth rings.

## Stripped
- cream/light greenish wood;
- long vertical fibers;
- subtle uneven streaks.

## Planks
- pale olive-beige;
- fine, slightly wavy grain;
- low contrast.

## Fence/gate/trapdoor
should preserve thin fibrous/woven feeling.

---

# 3. ROWAN / РЯБИНА

Identity:
warm reddish wood with characteristic dark marks.

## Bark/log
- warm gray-brown to reddish-brown;
- small horizontal/irregular darker marks;
- not simply orange oak.

## End grain
- warm salmon/light reddish center;
- clearer ring definition.

## Stripped
- peach/copper wood;
- soft reddish vertical grain.

## Planks
- warm terracotta/orange-red brown;
- visible flowing grain/knot;
- not brick-like rectangles.

IMPORTANT:
Current screenshot has a red plank pattern reading almost like brick.
Fix this.
It must remain WOOD.

---

# 4. PINE / СОСНА

Identity:
resinous cold pine.

## Bark/log
- dark brown-gray scaly bark;
- larger vertical plates than willow;
- patches of warmer inner brown.

## End grain
- yellow-ochre center;
- stronger ring pattern.

## Stripped
- honey/yellow wood;
- visible resin streaks/knots.

## Planks
- medium honey-brown;
- pronounced knots;
- long directional grain.

Should not look like spruce recolor.

---

# 5. DARKENED / CHARRED WOOD
## ПОТЕМНЕВШЕЕ / ОБГОРЕЛОЕ ДЕРЕВО

Identity:
old fire-darkened wood, not black concrete.

## Bark/log
- charcoal dark-brown;
- cracked surface;
- slightly lighter deep-brown fissures.

## End grain
- dark rings;
- center still visibly wood.

## Stripped
- dark brown-gray;
- scorched irregular streaks.

## Planks
- near-black brown;
- visible grain;
- subtle burnt edge accents.

No pure black flat fill.

---

# 6. PLANK CONSTRUCTION

Every wood plank texture must read as boards.

Use:
- 2–4 board divisions;
- continuous grain;
- occasional knots;
- no masonry/brick illusion.

---

# 7. FAMILY BLOCK SET CONSISTENCY

For each wood family verify:
- log;
- stripped log;
- wood;
- stripped wood;
- planks;
- slab;
- stairs;
- fence;
- fence gate;
- door;
- trapdoor;
- pressure plate/button if present.

Do not redesign only the log and leave all derived blocks as old flat recolors.

---

# 8. DOORS

RC checklist already includes:
- pine;
- rowan;
- willow;
- darkened.

Ensure:
- both halves;
- open/closed;
- hinge left/right;
- item icon;
- inventory model.

Door identity should match its wood family.

---

# 9. TREE ENTITY/WORLD SHAPE

This specific pass primarily fixes materials.

If custom tree world shapes already exist:
preserve.

Do not redesign worldgen trees unless visual tree shape is clearly still placeholder.

However if a tree is literally vanilla tree geometry with only recolored logs:
document it for the later nature/worldgen pass.

Do not hide this in final report.

---

# 10. ACCEPTANCE

From a lineup without tooltips:
tester should be able to distinguish all four families.

FAIL if:
- differences are mostly color swaps;
- rowan planks look like brick;
- pine looks like vanilla oak/spruce recolor;
- darkened wood reads like black concrete;
- stripped logs lack family identity.
