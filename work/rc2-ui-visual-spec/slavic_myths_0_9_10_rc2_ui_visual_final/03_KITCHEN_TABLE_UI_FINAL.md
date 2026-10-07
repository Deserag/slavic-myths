# 03 — KITCHEN TABLE UI
## FINAL APPROVED COOKING SCREEN

Target:
`references/target/ref_target_03_kitchen_table_final.png`

Current:
`references/current_problems/ref_problem_04_kitchen_current.png`

---

# 1. SCREEN PURPOSE

The Kitchen Table must answer:

- What can I cook?
- What ingredients do I need?
- Which ingredients do I already have?
- Which tool is required?
- What is the output?
- How much food/saturation/effect does it provide?
- Why is Cook disabled?

The current interface technically shows some of this but has poor hierarchy and truncation.

---

# 2. THREE-COLUMN LAYOUT

Recommended proportions:

LEFT — recipes:
24–28%.

CENTER — preparation:
43–47%.

RIGHT — result:
25–30%.

Inventory:
bottom center across center/right region.

---

# 3. LEFT RECIPE LIST

Header:
`Рецепты`

Each row:
- small item icon;
- full recipe name where possible.

Examples:
- Блины
- Блины с мясом
- Блины с черникой
- Блины с маком
- Каравай
- Ягодный пирог
- Грибная похлёбка
- Говяжья похлёбка
- Свиная похлёбка
- Оленья похлёбка
- Медвежья похлёбка
- Уха

Do NOT rely on severe truncation like:
`Блины с ма...`

If text cannot fit:
increase row/list width first.

If still too long:
use concise localized name that remains semantically complete.

Vertical scrollbar required if list exceeds panel height.

---

# 4. SELECTED RECIPE

Selected:
warm gold/cream outline;
dark background;
clear item icon.

Unselected:
dark wood/charcoal row.

Hover:
lighter border.

Do not make every row large vanilla gray button.

---

# 5. CENTER — PREPARATION

Header:
`Подготовка`

Two subsections:

## Ingredients
Each ingredient card:
- icon;
- localized name;
- count owned / required.

Example:

`Мука`
`1 / 1`

Green when enough.
Red when missing.

Use actual ingredient identity, not only text if item icon exists.

## Tool
Header:
`Инструмент`

Show actual required tool:

rolling pin;
pot;
or other real implemented kitchen tool.

If either of multiple tools is accepted:
show exact logic:
`Скалка ИЛИ кастрюля`
only if recipe truly permits either.

Do not visually imply optionality if the recipe specifically requires one.

---

# 6. TOOL STATE

Available:
green:
`1 / 1`

Missing:
red:
`0 / 1`

Hover missing:
`Требуется кастрюля для этого рецепта.`

Tool is not consumed unless existing gameplay says it is.

---

# 7. RIGHT — RESULT

Header:
`Результат`

Large result icon.

Show stack count:
`x3`

Name:
`Блины`

Then:

Food:
`Еда: 5`

Saturation:
`Насыщение: 5.0`

Effects:
`Эффекты: нет`
or actual effects.

If effect:
show icon + readable description.

No huge unused blank area.

---

# 8. COOK BUTTON

Bottom right:
`Приготовить`

VALID:
active dark-red/wood/gold button.

INVALID:
disabled.

Below button:
status message.

Examples:

`Не хватает предметов для приготовления.`

`Требуется скалка.`

`Инвентарь переполнен.`

One concise blocker at a time, with tooltip if multiple.

---

# 9. PLAYER INVENTORY

Standard 3 rows + hotbar.

Should not visually merge into recipe list.

Shift-click:
ingredients may move into appropriate internal/preparation inventory only if current cooking mechanics use explicit slots.

If recipes automatically read player inventory:
do not create fake input slots that do nothing.

---

# 10. RECIPE UPDATE

When player selects another recipe:
center and right panels update immediately.

Do not require closing/reopening screen.

---

# 11. ART DIRECTION

Frame:
warm kitchen wood.

Parchment/linen panels:
light warm beige.

Accents:
red woven Slavic textile;
small herbs/pottery decoration;
candle only if it does not steal space.

Do not put decorative food graphics over slots/text.

---

# 12. SCALE

GUI 2/3/4.

Recipe scrollbar must remain clickable.

Tooltips must not cover critical permanent content where avoidable.

---

# 13. ACCEPTANCE

FAIL if:
- recipe names are aggressively truncated;
- player cannot understand missing ingredients/tool;
- result stats are hidden;
- Cook can execute invalid recipe;
- content overlaps;
- screen remains mostly generic gray buttons.
