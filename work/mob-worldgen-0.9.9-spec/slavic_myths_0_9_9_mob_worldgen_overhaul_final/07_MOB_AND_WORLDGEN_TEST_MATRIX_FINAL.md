# 07 — AUTOMATED TEST / MANUAL QA MATRIX

---

# A. WORLDGEN

Test multiple seeds.

Required:
- Kurgan family;
- Bandit family;
- Swamp family;
- Water structures;
- Bathhouse;
- Yaga;
- hunting/encounter structures;
- world boss locations;
- every other registered structure.

For normal tiered families:
Small <=1000 network gap.
Medium <=2000 to Small.
Large <=4000 to Small/Medium.

Superflat:
Small/Medium/Large representative structures accept valid clear land.

Snow:
snow layer is replaceable and not fake height.

---

# B. NATURAL SPAWNING

For every intended natural mob:
- eligible biome list non-empty;
- spawn placement registered;
- predicate can pass;
- weight/group valid.

Automated report.

---

# C. BEAR EGG

Run 1000 simulated egg resolutions.

Expected around:
~90% standard bear;
~10% mother family.

Tolerance:
do not fail from normal small statistical drift; use broad expected range such as 6–14% family.

Family:
exactly 1 mother;
1–2 cubs;
no orphan family spawn.

---

# D. DEER EGG

1000 resolution test.

Target roughly:
45% stag;
45% doe;
10% family.

Validate one user-facing egg.

---

# E. MOB COMBAT

For each FULL_REWORK mob:
- state transitions;
- cooldown;
- telegraph;
- damage once per intended active window;
- miss recovery;
- death cleanup;
- unload/reload cleanup.

---

# F. BOSS/MINIBOSS

Mother Bear:
120 HP;
protective aggro;
bar appears only engaged;
leash/reset;
reward once.

Solovey:
250 HP preserved.

Ataman:
150 HP preserved.

---

# G. SOUND

Validate all registered sound events resolve.

No missing-event spam for new mobs.

---

# H. MANUAL QA DOCUMENT

Create `docs/MANUAL_QA_0.9.9.md`.

Checklist must be actionable and grouped:
1. natural spawn;
2. each full rework mob;
3. mother bear;
4. deer family;
5. boss polish;
6. structure coverage;
7. locate;
8. save/reload;
9. dedicated server later.

Do not auto-launch client.
