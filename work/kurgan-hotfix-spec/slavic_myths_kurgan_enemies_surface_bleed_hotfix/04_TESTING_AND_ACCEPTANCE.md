# 04 — TESTING / ACCEPTANCE

Do not auto-launch Minecraft.

---

# A. KURGAN ENCOUNTERS

Generate through manual/dev command:

1 Small
1 Warrior
1 Great

Verify the command-generated structures receive encounter metadata.

## Small
PASS only if:
- at least 2 Kurgan-specific hostiles appear;
- total matches allowed tier ranges;
- no miniboss/boss.

## Warrior
PASS only if:
- Upyr exists;
- Nav exists;
- Druzhinnik exists;
- exactly one Voevoda dedicated encounter exists;
- no Prince.

## Great
PASS only if:
- Upyr/Nav/Druzhinnik population exists;
- exactly one Voevoda;
- exactly one Buried Volkhv;
- exactly one Unresting Prince final encounter.

---

# B. REVISIT

Clear a normal room.
Leave Kurgan.
Reload chunks.
Return.

PASS:
cleared room does not farm-spawn the same encounter again.

---

# C. ACTIVE UNLOAD

Trigger room but do not kill all enemies.
Unload/reload.

PASS:
encounter does not duplicate.
If assigned mobs were lost, encounter safely restores only missing required state.

---

# D. SURFACE

Generate:
- Small;
- Warrior;
- Great;
- raised Superflat Great.

Capture exterior screenshots.

PASS:
- mound remains clearly earthen;
- visible surface contains coherent grass/coarse/root/gravel/stone variation;
- no dirt blob;
- no block checkerboard;
- entrance transition visible;
- raised superflat support is blended.

---

# E. CUT

Test:
- first hit starts 5 sec;
- second hit adds stack and refreshes 5 sec;
- fast hits reach max 5;
- hit at max refreshes but never exceeds 5;
- 5 sec without hit clears all stacks;
- immune entity receives nothing;
- two players share same cap 5;
- PvP rules respected.

---

# F. BUILD

Run build/resource/data tests only.
Do not launch Minecraft client automatically.
