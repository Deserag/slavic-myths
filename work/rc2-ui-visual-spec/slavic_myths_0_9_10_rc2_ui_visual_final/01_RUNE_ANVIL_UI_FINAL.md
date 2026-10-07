# 01 — RUNE ANVIL UI
## FINAL APPROVED REDESIGN

Target:
`references/target/ref_target_01_rune_anvil_final.png`

Current problem:
`references/current_problems/ref_problem_03_rune_anvil_current.png`

---

# 1. SCREEN GOAL

The screen must immediately answer:

1. What item am I modifying?
2. What rune am I applying/removing?
3. What will the final item be?
4. Which rune slots are already occupied?
5. What EXACT effect does the selected rune provide?
6. Is the rune compatible?
7. What does the operation cost?
8. Why can/cannot I confirm?

The current generic 3-slot + buttons layout does not satisfy this.

---

# 2. MAIN FRAME

Recommended overall content ratio:

Top title/header:
~10% height.

Top operation strip:
~25%.

Mode tabs:
~10%.

Lower rune/info region:
~30%.

Player inventory:
~25%.

These are layout targets, not hard pixel coordinates.

---

# 3. HEADER

Title:
`Рунная наковальня`

Centered.

Frame:
dark wood with metal corner plates.

Small Slavic geometric accents allowed near title.

Do NOT put important gameplay controls in the header.

---

# 4. OPERATION STRIP — RUNE APPLICATION

Layout:

`[ Предмет ]  +  [ Руна ]  →  [ Результат ]`

Three large display areas:

## Item input
- accepts only valid equipment for selected operation;
- 36–44 px-equivalent visual area at normal GUI scale;
- label `Предмет`.

## Rune input
- accepts rune item only;
- label `Руна`.

## Result preview
- visually highlighted with muted gold border when valid;
- read-only;
- displays item + applied rune state preview;
- label `Результат`.

IMPORTANT:
There is no `Катализатор` slot in Rune Application mode.

---

# 5. OPTIONAL EXTRA MATERIALS

If current rune operation needs extra material:

DO NOT create a third primary slot.

Show under effect description:

`Доп. материал`

Example:

`Серебряная нить   1 / 1 ✓`

Missing:

`Серебряная нить   0 / 1 ✕`

Colors:
green available;
dark red missing.

On confirmation:
consume from inventory server-side.

---

# 6. MODE TABS

Exactly three conceptual modes:

1. `Перековка`
2. `Нанесение руны`
3. `Снятие руны`

Use full-width readable tabs.

Selected:
dark Slavic red + brighter border.

Unselected:
dark iron/wood neutral.

Each tab may have small simple icon:
hammer;
rune;
remove/undo.

Do not use huge decorative art inside tabs.

---

# 7. INSTALLED RUNE SLOTS

Panel title:
`Руны на предмете`

Show:
`I`, `II`, `III`

Three large rune socket cards.

## Occupied slot
- rune icon;
- visible frame;
- selected slot uses gold highlight;
- optional small level/rank label only if system actually has ranks.

## Empty unlocked slot
- visible `+`;
- gray dark slot;
- tooltip `Свободный слот`.

## Locked slot
- darker;
- lock icon;
- tooltip explaining WHY locked.

Do not show an empty slot as if an item is missing when the equipment simply does not support that many runes.

---

# 8. RUNE INFORMATION PANEL

This panel is mandatory and visible WITHOUT relying only on tooltip.

Show:

### Rune name
Example:
`Руна Силы`

### Primary effect
Example:
`Урон оружия +15%.`

### Compatibility
Green:
`Совместима: мечи, топоры, копья`

### Incompatibility
Red:
`Несовместима: Руна ярости`

### XP Cost
`Стоимость: 5 XP`

### Optional materials
if applicable.

### Short flavor text
1–2 lines maximum.
May be italic/darker.
Never replaces mechanical description.

---

# 9. CREATIVE MODE

In Creative:

Display:

`Творческий режим — XP не требуется.`

XP cost is effectively:
0.

Do not deduct XP.

Rune/material item requirements:
keep normal slot/item behavior for reliable UI testing unless the existing design intentionally gives infinite creative consumption.

Preferred:
Creative does not consume XP, but item movement still behaves predictably.

---

# 10. INSTALL CONFIRMATION

Do not apply just because item+rune are placed.

Provide explicit confirm button if current interaction architecture supports it.

Recommended:
`Нанести руну`

Button states:

VALID:
red/wood accented active button.

INVALID:
disabled gray.

Hover on disabled button:
show exact blocker:
- incompatible rune;
- no free rune slot;
- insufficient XP;
- missing material;
- invalid item.

---

# 11. REFORGE MODE

When `Перековка` is selected:

Top area may become:

`[ Предмет ] + [ Материал ] -> [ Результат ]`

This is the correct place for material input.

Right info panel shows:

### До
damage/durability/other changed property.

### После
changed values highlighted.

Do not show rune compatibility when in reforge mode.

---

# 12. REMOVE RUNE MODE

Layout:

`[ Предмет ] -> [ Результат ]`

Installed rune panel remains visible.

Player clicks rune slot to choose which rune to remove.

Right panel:

`Будет снята: Руна Силы`

`Стоимость: 2 XP`

If current mechanic destroys rune:
write prominently:

`Руна при снятии уничтожается.`

If it returns rune:
write exact return behavior.

Do not leave this ambiguous.

---

# 13. INVENTORY

Player inventory at bottom.

Standard:
3 rows + hotbar.

Slots:
consistent Minecraft size;
clear parchment/wood background;
no decorative overlay over slots.

Shift-click:
must move only into valid operation inputs.

---

# 14. TOOLTIP

Rune tooltip should duplicate important mechanics, but tooltip is SECONDARY.

Main screen must already show:
- rune effect;
- compatibility;
- cost.

---

# 15. ERROR STATES

Examples:

`Предмет не поддерживает руны.`

`Нет свободного слота.`

`Эта руна несовместима с Руной Ярости.`

`Недостаточно XP: требуется 5.`

`Не хватает: Серебряная нить x1.`

No generic:
`Невозможно выполнить операцию.`

---

# 16. ACCEPTANCE

FAIL if:
- Catalyst primary slot returns;
- effect is visible only by hover tooltip;
- Creative still displays misleading payable XP state;
- item can be lost on close;
- output duplicates;
- text overlaps at GUI scale 2/3/4;
- rune slot state is visually ambiguous.
