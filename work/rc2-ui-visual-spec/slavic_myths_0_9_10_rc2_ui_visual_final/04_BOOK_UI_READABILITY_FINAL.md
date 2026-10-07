# 04 — BOOK OF TALES UI
## READABILITY POLISH FOR RC

No new concept image is required for this pass.
Use the shared UI visual system from master.

---

# 1. PRIMARY BUG

Blur directly under book text reduces readability.

Remove blur from the actual reading surface.

Blur may exist:
behind the entire window as world-background separation.

Blur must NOT exist:
between parchment and glyphs.

---

# 2. PAGE PANEL

Use nearly opaque parchment.

Text:
dark brown/charcoal.

Margins:
minimum ~16–24 visual pixels equivalent around body content.

Line spacing:
comfortable and consistent.

Do not place ornaments in text flow.

---

# 3. NAVIGATION

Left navigation approximately 22–26% width.

Use actual existing categories.

Example categories only if already represented:
- Creatures;
- Places;
- Artifacts;
- Rituals;
- Stories.

Do not invent lore sections to fill screen.

---

# 4. ARTICLE

Right/main panel:
- title;
- separator;
- body text;
- optional image/icon;
- scrollbar.

Long text must scroll.

No text clipping.

---

# 5. GUI SCALE

Test 2/3/4.

Article width should adapt.

Do not shrink font excessively.

---

# 6. ACCEPTANCE

FAIL if:
- blur is still underneath body text;
- body overlaps images/buttons;
- long article cannot scroll;
- categories/list overlap at GUI scale 4.
