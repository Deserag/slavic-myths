# 02 — PATH STONE UI
## FINAL APPROVED PROGRESSION SCREEN

Target:
`references/target/ref_target_02_path_stone_final.png`

Current:
`references/current_problems/ref_problem_01_path_stone_current.png`

---

# 1. SCREEN PURPOSE

Path Stone is a progression/class-path interface, not a generic chest GUI.

The screen must make class choice feel important.

The player must immediately understand:

- current primary path;
- current secondary path;
- available XP;
- four paths;
- selected path;
- available skills;
- skill effect;
- skill cost;
- whether it is unlocked;
- what the Confirm/Reset action will do.

---

# 2. HEADER

Title:
`Камень Пути`

Top status line:

Left:
`Основной путь: <name/нет>`

Center:
`Побочный путь: <name/нет>`

Right:
`Доступно XP: <number>`

Use small icons:
primary tree/path;
secondary branch;
XP/orb.

Do not make this line enormous.

---

# 3. PATH TABS

Four equal-width tabs:

- Дружинник
- Ведун
- Разбойник
- Колдун

Each has a small class icon.

Selected:
deep red background;
gold/cream outline.

Unselected:
dark wood/brown.

Do not use vanilla flat gray buttons.

---

# 4. CLASS PREVIEW

IMPORTANT planned direction:
when class system receives its full 0.9.11 pass, left side should support character preview wearing class-themed equipment.

For current RC:
if preview is already technically feasible without scope explosion, reserve/implement a compact preview panel.

If not:
do NOT delay RC.
The layout must be structured so preview can be added later without complete screen rewrite.

Do not fake a static screenshot as player model.

---

# 5. SKILL LIST

Main center-left list.

Each skill row contains:

- icon;
- skill name;
- one-line short effect;
- XP price on right;
- lock/unlocked/available state.

Recommended row height:
enough for 2 text lines without clipping.

Example:

`Строевая выучка`
`Отбрасывание с оружием и щитом -15%`
`5 XP`

---

# 6. SKILL TREE VISUAL LANGUAGE

Use a vertical progression line to left of skills.

Node states:

LOCKED:
dark node.

AVAILABLE:
cream/gold border.

UNLOCKED:
green/gold confirmation.

SELECTED:
bright outline.

Do not imply prerequisites if current mechanics do not actually use them.

If skills are linear:
connect linearly.

If branching:
show real branches only.

---

# 7. DETAIL PANEL / TOOLTIP

Current design hides too much in tooltip.

Preferred behavior:
selected skill has a persistent detail panel if screen width permits.

At minimum tooltip must include:

- full name;
- full mechanical effect;
- exact values;
- cost;
- prerequisites;
- unlocked state.

Example:

`Строевая выучка`

`На 15% уменьшает получаемое отбрасывание, пока игрок держит оружие или щит.`

`Стоимость: 5 XP`

Do not use vague:
`Улучшает стойкость.`

---

# 8. MAIN ACTIONS

Bottom left/center:

`Выбрать путь`

Button active only when path selection is valid.

Secondary action:

`Сбросить`

Only active if reset is possible.

Tooltip must state reset consequences:
- XP returned or not;
- path reset only or skills too;
- cost if any.

Do not allow destructive reset without clear warning.

---

# 9. CREATIVE MODE

Creative:
- path/skill XP cost = 0;
- no XP consumed.

Display:
`Творческий режим — XP не требуется.`

Do not display false price as if player must pay.

Skill prerequisites may remain if needed to test progression structure, unless current debug design intentionally bypasses them.

---

# 10. NAVIGATION MARK

Existing Path Stone behavior that creates navigation marker:
preserve.

UI must give confirmation:

`Метка пути установлена.`

If marker creation fails:
specific error.

Do not silently consume XP/materials.

---

# 11. TEXT / WIDTH

No truncated:
`Блины с ма...`-style clipping equivalent in this screen.

Skill names should have sufficient horizontal width.

If localized text is longer:
wrap description;
do not scale font to tiny size.

---

# 12. GUI SCALE

Must be tested:
2/3/4.

No overlap between:
header;
tabs;
skills;
tooltip/details;
bottom buttons.

---

# 13. ACCEPTANCE

FAIL if:
- screen still looks like generic gray-button menu;
- selected path is visually unclear;
- exact skill effect is not available;
- XP cost is misleading in Creative;
- navigation marker fails silently;
- text overlaps/truncates badly.
