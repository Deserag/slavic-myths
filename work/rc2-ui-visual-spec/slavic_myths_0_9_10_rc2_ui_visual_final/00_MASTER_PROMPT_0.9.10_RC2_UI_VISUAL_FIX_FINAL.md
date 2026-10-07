# SLAVIC MYTHS — 0.9.10 RC2
# FINAL UI / VISUAL / MODEL CORRECTION PACKAGE
## Minecraft 1.21.1 / NeoForge / Java 21
## Mod ID: `slavicmyths`

---

# 0. STATUS OF THIS PACKAGE

This package is a STRICT implementation specification for the next RC2 correction pass.

Codex is the IMPLEMENTER.
Codex is NOT allowed to redesign the approved concepts from scratch.

The package contains:

1. CURRENT PROBLEM screenshots — what is wrong right now.
2. TARGET references — what the approved result should look like.
3. Exact interaction/layout/visual rules.
4. Acceptance tests.

Where a target image contains decorative text/numbers that conflict with this specification:
**the written specification wins.**

Do NOT ask the user design questions already answered here.

---

# 1. CURRENT MANUAL QA FINDINGS

The user manually tested RC2 and found:

## 1.1 Path Stone
- functional menu exists;
- visual design is still weak/basic;
- large gray buttons dominate;
- hierarchy is unclear;
- class/path choice does not visually feel like an important progression system;
- skill description is mostly hidden in tooltip instead of being readable as part of the main screen.

## 1.2 Rune Anvil
- basic function exists;
- current screen is essentially beige/gray slots + generic buttons;
- it does not visually explain the resulting rune effect well;
- old version unnecessarily showed a large Catalyst input slot;
- approved redesign MUST NOT use a dedicated Catalyst slot for rune application;
- player should understand: item + rune -> result;
- installed runes and their effects must be readable.

## 1.3 Kitchen Table
- function exists;
- current screen is technically usable but visually unfinished;
- recipe list truncates names aggressively;
- ingredient/tool/result hierarchy is weak;
- result side has too much empty space;
- missing item/tool state is not visually strong enough.

## 1.4 Druzhinnik Shield
- front/back orientation is wrong in hand;
- front decorative side may face inward toward player;
- first-person render looks strange;
- shield itself is otherwise closer to acceptable than the old broken version;
- orientation/transforms/renderer must be corrected.

## 1.5 Club and Battle Axe
- held/item models are visually broken;
- current render may show corrupted/exploded geometry/texture;
- this is a release blocker.

## 1.6 New Wood Families
- current log/plank designs read mostly as recolors;
- silhouettes/material identity are too weak;
- they do not yet communicate willow / rowan / pine / darkened wood clearly enough.

---

# 2. REFERENCE POLICY

Read:
`REFERENCE_INDEX.md`

TARGET references are APPROVED.

Use them as:
- composition reference;
- color/material reference;
- information hierarchy reference;
- overall style reference.

Do NOT:
- trace every decorative pixel blindly;
- reproduce spelling mistakes from generated concept art;
- introduce decorative elements that reduce readability.

---

# 3. GLOBAL UI DESIGN SYSTEM

All three redesigned screens must belong to the same Slavic Myths visual language.

## Materials
Primary frame:
- dark warm wood;
- visible pixel-art grain;
- reinforced corners.

Secondary structure:
- dark iron/forged metal brackets;
- rivets only at meaningful connection points.

Content panels:
- warm parchment / pale cloth;
- high contrast dark-brown text.

Accent:
- dark Slavic red;
- muted gold only for important selection/result states;
- green = valid/available;
- red = missing/invalid.

## Forbidden
- giant flat gray Minecraft buttons filling most of screen;
- blur directly under text;
- transparent brown-on-brown panels with poor contrast;
- ornamental patterns crossing text;
- text over item icons;
- decorative elements that reduce slot readability.

---

# 4. PIXEL SCALE

UI texture assets should be designed as crisp pixel art.

Do not use blurry scaling.

Maintain readable borders at GUI scales:
- 2;
- 3;
- 4.

Do not shrink text below normal Minecraft readability just to preserve a fixed screenshot layout.

Use responsive spacing / scroll where required.

---

# 5. PRIORITY

P0:
1. broken Club model;
2. broken Battle Axe model;
3. Druzhinnik Shield orientation/render;
4. Rune Anvil final UI;
5. Path Stone final UI;
6. Kitchen Table final UI.

P1:
7. wood-family visual pass;
8. Book readability polish if still not completed.

Do not publish the next RC with P0 broken.

---

# 6. DO NOT REWRITE WORKING GAMEPLAY LOGIC

This pass is primarily:
- UI presentation;
- render/model correction;
- visual identity;
- interaction feedback.

If a functional bug is encountered while touching a system:
fix it minimally.

Do not redesign unrelated gameplay systems.

---

# 7. RUNE ANVIL IMPORTANT OVERRIDE

For rune installation the approved workflow is:

`ITEM + RUNE -> RESULT`

There is NO dedicated Catalyst slot in the main rune-application layout.

If some operation technically requires an extra material:
show it as a SMALL COST REQUIREMENT line in the information panel and consume it automatically from player inventory on confirmation.

Example:

`Серебряная нить: 1 / 1 ✓`

Do not force player to drag the material into a third big input slot.

Detailed rules:
`01_RUNE_ANVIL_UI_FINAL.md`

---

# 8. CREATIVE MODE RULE

For Path Stone / Rune Anvil systems:

Creative mode:
- XP cost = 0;
- no XP deduction;
- interface must clearly state that XP is not required.

Preferred text:
`Творческий режим — XP не требуется.`

Do not hide a fake `5 XP` cost and then silently not deduct it.

---

# 9. INVENTORY SAFETY

All container screens:
- must return player-owned items on safe close if they are not committed;
- must not delete contents on disconnect/reload;
- shift-click must obey slot validation;
- result preview must not duplicate items.

---

# 10. NO CLIENT AUTO-LAUNCH

Allowed:
- build;
- resource validation;
- screen/menu tests;
- model JSON validation.

Do not automatically launch Minecraft client.
User will test through PolyMC.

---

# 11. DEFINITION OF DONE

RC2 visual correction is complete only if:

- Rune Anvil matches approved information hierarchy and no Catalyst slot exists for rune installation;
- Path Stone matches approved progression-screen hierarchy;
- Kitchen Table is comfortable to use without truncated/overlapping content;
- Druzhinnik Shield front faces outward correctly in first/third person;
- Club and Battle Axe models render normally in inventory, first person and third person;
- wood sets have visibly distinct material identity;
- GUI scales 2/3/4 do not overlap;
- build succeeds.
