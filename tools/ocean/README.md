# Slavic Myths — Ocean updates / Tsar Pike (approved V1)

This folder keeps **offline Python prototypes** for the Tsar Pike boss.
The Java boss, natural river/ocean spawns, spawn egg, animated 1x2 trophy head and GameTests
are implemented separately in `src/main` and `tools/pharaohka-tests`.
The Python files remain independent technical references for V1 geometry and attack poses.

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

## Java integration

- `src/main/java/org/slavicmyths/marine/TsarPikeContent.java` registers the boss, egg, trophy and aquatic spawning.
- `src/main/java/org/slavicmyths/marine/TsarPikeEntity.java` controls prey selection, bites, grabs, tail sweeps and water leaps.
- `src/main/java/org/slavicmyths/marine/TsarPikeHeadBlock.java` provides the two horizontal placement cells; near-living-entity reactions are cosmetic, while the aura is activated by right-click.
- `src/main/java/org/slavicmyths/client/TsarPike*.java` contains model and renderers.
- `tools/pharaohka-tests/org/slavicmyths/marine/TsarPikeGameTests.java` contains isolated server-side checks.
- `.github/workflows/ocean-tsar-pike.yml` runs Java 21 clean build and GameTests on relevant commits.

**Do not mistake a passing headless GameTest for approval of appearance in Minecraft**: examine V1 mesh, trophy orientations, leap trajectory, hit zones and actual spawn frequency in game.
