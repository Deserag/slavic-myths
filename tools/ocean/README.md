# Slavic Myths — Ocean updates / Tsar Pike (approved V1)

This folder keeps **offline Python prototypes** for the Tsar Pike boss.
It does not register a Minecraft mob, implement combat AI, or add Java code.

Approved by owner:
- Visual model: **V1** (no geometry changes requested).
- Danger level: **V**, base A4 damage **18 HP**.
- Escaping a jaw grab: **deal actual damage to the pike**, not a button-mashing QTE.
- Other numbers (HP, armor, escape threshold, cooldowns) are proposals, not approved.

## Run

Requirements: Python 3, numpy, matplotlib, Pillow.

From the repository root:

```bash
python tools/ocean/tsar_pike_v1/tsar_pike_v1.py --only static
python tools/ocean/tsar_pike_v1/tsar_pike_v1.py --only gif
python tools/ocean/tsar_pike_v1_1/tsar_pike_v1_1_escape.py
```

Generated PNG/GIF files are written next to the respective scripts; do not
commit generated images unless explicitly approved. See
`docs/ocean/tsar-pike/DECISIONS_AND_BALANCE_RU.md` for design rules.

The current marine integration branch is `integration/ocean-updates`,
forked from `integration/release-ships-pharaohka-buyan-20261118`.
