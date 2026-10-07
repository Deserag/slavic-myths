# 05 — DRUZHINNIK SHIELD / CLUB / BATTLE AXE
## RELEASE-BLOCKING MODEL CORRECTIONS

Target visual reference:
`references/target/ref_target_04_wood_shield_club_battleaxe.png`

Current screenshots:
- `ref_problem_05_druzhinnik_shield_third_person.png`
- `ref_problem_06_druzhinnik_shield_first_person.png`
- `ref_problem_07_broken_club_battleaxe.png`

---

# 1. DRUZHINNIK SHIELD — ORIENTATION BUG

Observed:
decorative front face is oriented toward player's body/inside in some held view.

Correct rule:

## Front / enemy-facing side
Must show:
- painted red face;
- Slavic white motif;
- central metal boss;
- outer metal rim/rivets.

## Back / player-facing side
Must show:
- wooden planks;
- grip/strap structure;
- metal reinforcement.

When held:
enemy sees decorated front.
player sees mostly inner wooden/back side from natural first-person angle.

Do NOT simply rotate texture 180° if model axes are wrong.
Correct actual transforms/model orientation.

---

# 2. SHIELD FORM

Round shield.

Readable thickness.

Layers:
1. wood body;
2. metal rim;
3. center boss;
4. small rivets.

No paper-thin flat disc.

---

# 3. SHIELD FIRST PERSON

Off-hand:
- believable vanilla-like defensive placement;
- not upside-down;
- not front-facing inward;
- center boss not clipping through forearm.

Blocking:
raise/rotate into defensive pose.

Main-hand:
same physical orientation logic.

---

# 4. SHIELD THIRD PERSON

Check:
- idle;
- walking;
- blocking;
- main hand;
- off hand.

Decorated front faces outward.

---

# 5. SHIELD ICON

Inventory icon:
front-facing decorated side.

Must clearly read as round shield at small size.

---

# 6. CLUB / ДУБИНА

Current model is broken/corrupted in hand.

Do not merely suppress rendering error.

Rebuild model.

## Visual
- massive wooden head/body;
- thick cylindrical/rough octagonal form;
- darker reinforced lower grip;
- 4–6 visible metal studs/bands;
- weight concentrated toward striking end.

Do not make:
- thin stick;
- mace with tiny head;
- random extruded texture fragments.

## Proportions
Held length:
roughly sword-to-heavy-club length.

Head width:
clearly larger than handle.

## Inventory icon
diagonal;
silhouette readable.

---

# 7. BATTLE AXE / БОЕВОЙ ТОПОР

Current model also broken.

Rebuild.

## Visual
- long wooden haft;
- leather/red wrap;
- broad asymmetrical or double-sided Slavic-style blade according to existing item identity;
- steel blade;
- restrained red rune/paint accent;
- strong socket geometry.

Blade must be the visual focus.

Do not create:
- tiny vanilla axe recolor;
- oversized fantasy polearm occupying half screen.

---

# 8. MODEL PIPELINE AUDIT

For shield/club/axe inspect:

- model JSON;
- parent;
- custom loader if any;
- texture mapping;
- UV;
- display transforms;
- item renderer;
- left/right hand transform;
- first-person left/right;
- third-person left/right;
- blocking predicates/components;
- legacy 1.16.5 assumptions.

The broken render may be port-related.
Fix root cause.

---

# 9. TEST MATRIX

Shield:
inventory;
FP main;
FP off;
TP main;
TP off;
block main;
block off.

Club:
inventory;
FP left/right;
TP;
dropped item.

Battle axe:
same.

No missing-texture purple/black.
No exploding geometry.
No inverted normals/UV.
