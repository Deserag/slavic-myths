# 07 — COMMON INTERACTION / CREATIVE / TOOLTIP RULES

---

# 1. CREATIVE XP

Creative:
XP requirement = 0 for:
- Path Stone;
- Rune Anvil operations.

Display explicitly:
`Творческий режим — XP не требуется.`

Do not deduct levels/points.

---

# 2. SURVIVAL XP

Survival:
use the actual existing designed XP cost.

Before commit:
server validates XP again.

Do not trust client-only disabled button.

---

# 3. TOOLTIP RULE

Tooltips complement the UI.

They must NOT be the only place that explains a core effect.

Core effect should be visible in persistent panel.

Tooltip may show:
- longer detail;
- exact prerequisite;
- reason disabled.

---

# 4. COLOR LANGUAGE

Green:
available / enough / compatible.

Red:
missing / incompatible / destructive warning.

Gold/cream:
selected / result.

Gray:
disabled.

Do not use color as sole indicator:
include text/icon.

---

# 5. CLOSE SAFETY

If player closes:
- uncommitted item/rune/tool content returns safely;
- no deletion;
- no duplicate.

Disconnect/reload:
same server-safe logic.

---

# 6. SHIFT-CLICK

Rune Anvil:
only valid items enter relevant inputs.

Kitchen:
follow actual cooking inventory architecture.

Path Stone:
no inventory transfer system unless current mechanic requires it.

---

# 7. BUTTON LOCK

Disabled action has tooltip/reason.

Do not allow click then fail silently.

---

# 8. LOCALIZATION

RU is primary manual-test language.

All new UI strings also need EN entries.

Do not hardcode Russian text inside screen renderer.

Use translatable components.

---

# 9. ACCESSIBILITY / READABILITY

Do not use:
- low-contrast brown text on dark brown;
- blur behind text;
- tiny gray explanatory text.

Important numerical values must be readable.

---

# 10. NO OVERLAP

At GUI scales 2/3/4:
- panels;
- tooltips;
- button labels;
- result icons;
- lists

must not overlap.

Use scroll containers rather than compressing everything.
