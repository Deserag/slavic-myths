# 04 — WILDLIFE NATURAL SPAWN + FAMILY / SPAWN EGG LOGIC
## BLOCKER: NEW ANIMALS CURRENTLY DO NOT SPAWN NATURALLY

---

# 1. SPAWN AUDIT

For every entity intended to appear naturally:
- verify MobCategory;
- biome modifier;
- biome/tag match;
- weight;
- group min/max;
- spawn placement registration;
- heightmap;
- predicate;
- light rules;
- substrate rules;
- water rules;
- collision;
- finalizeSpawn;
- server config toggle if any.

Write actual results into:
`docs/mobs/WILDLIFE_SPAWN_AUDIT_0.9.9.md`

---

# 2. REQUIRED WILDLIFE TO VERIFY

At minimum, if present in current registry:
- Brown Bear;
- bear family/cub internal entity if present;
- Deer / stag;
- Doe;
- Fawn;
- Wild Boar;
- Forest Wolf;
- Pike;
- Carp;
- Crayfish;
- any other 0.6.x/0.7.x wildlife discovered in registry.

No entity intended as natural wildlife may finish with:
`natural spawn = NO`
unless the design explicitly says structure/quest-only.

---

# 3. BEAR — ONE SPAWN EGG

There must be exactly ONE user-facing bear spawn egg.

Suggested ID:
preserve existing `brown_bear_spawn_egg` or actual existing bear egg ID.

Remove/hide redundant separate female/cub egg from creative if one exists.
Do not break internal entity IDs unnecessarily.

---

# 4. BEAR EGG STANDARD RESULT

On use:
85–92%:
spawn one standard adult Brown Bear.

The exact chance inside this band may be selected once and documented.
Recommended default: 90%.

---

# 5. BEAR FAMILY MINIBOSS RESULT

Recommended: 10% from Bear spawn egg.

Spawn:
- 1 `Mother Bear` special state/variant;
- 1–2 cubs.

This is an ENCOUNTER, not three unrelated spawns.

## Mother Bear
HP: 120
Armor: 6
Damage:
normal swipe 8
charge 10
rage swipe 9

Speed:
normal moderate;
short defensive bursts.

Boss classification:
MINIBOSS.

Display:
use miniboss/boss bar only while engaged, titled localized:
`Медведица-мать`

## Trigger
Mother is neutral-warning until:
- player approaches cub within ~7 blocks;
- hits cub;
- hits mother.

Then aggressive.

## Warning
- stand/growl;
- clear sound;
- ~1 sec reaction window.

## Mechanics
Protect:
mother attempts to place herself between player and cubs.

Charge:
faces player;
strong short charge;
miss recovery.

Defensive rage:
if a cub is hurt OR mother below 50%;
attack cooldown reduced modestly;
no absurd doubling.

Leash:
mother should not chase forever across biome.

## Cubs
HP: 12
Damage: 0
Behavior:
- stay near mother;
- flee from active combat/player;
- no combat contribution.

Do NOT require player to kill cubs to finish encounter.

If mother dies, cubs flee/despawn naturally according to safe implementation.

## Reward
Mother Bear:
- guaranteed useful bear materials;
- increased hide/claw yield;
- rare `mother_bear_trophy` or `great_bear_claw` ONLY if no equivalent trophy exists.

Cubs:
no valuable drops.
Do not incentivize farming cubs.

---

# 6. NATURAL BEAR FAMILY SPAWN

Family encounters may also occur naturally at a LOWER rate than ordinary bear spawns.

Recommended:
5–8% of natural bear spawn events resolve to family composition, if local cap permits.

Normal:
single adult or rare pair.

Do not spawn mother miniboss families everywhere.

---

# 7. DEER — ONE SPAWN EGG

Exactly ONE deer-family egg should be user-facing.

On use, default recommended distribution:
45% adult stag;
45% adult doe;
10% family/small herd.

If actual internal types differ, adapt implementation while keeping one egg.

---

# 8. DEER FAMILY RESULT

Family result:
- 1 adult doe;
- 1 fawn;
- 35% chance second fawn;
- optional adult stag in only a subset of family results.

Do NOT make this a miniboss.

---

# 9. DEER STATS

Stag:
HP 26
speed high
attack only defensive: 3–4 antler shove if cornered.

Doe:
HP 22
speed high
primarily flee.

Fawn:
HP 12
speed moderate-high
always flee/follow mother.

---

# 10. DEER HERD AI

Natural deer should appear in small groups:
2–5.

Behavior:
- shared flee trigger;
- staggered direction but same general escape vector;
- avoid water/cliffs where possible;
- do not all occupy same block;
- after danger ends, gradually regroup.

Stag may remain between threat and herd for a very short warning window but should not become a tank.

---

# 11. NATURAL SPAWN TARGETS

These values are starting targets; final values must be documented.

Bear:
rare woodland/taiga-like wildlife.
Group 1, family special separately.

Deer:
uncommon/common forest/plains edges depending current design.
Group 2–5.

Boar:
uncommon forest/field.
Group 1–3.

Forest Wolf:
uncommon forest/taiga.
Group 2–4.

Fish:
correct water category and biome/depth behavior.

Crayfish:
water bottom/shallow-water appropriate predicate.

---

# 12. AUTOMATED SPAWN VALIDATION

For each intended natural animal:
run predicate/biome registration tests.

Also make a spawn table report:
entity
eligible biome count
weight
group
placement predicate
PASS/FAIL

FAIL if:
- eligible biomes = 0;
- weight = 0 unintentionally;
- no biome modifier;
- no spawn placement registration;
- impossible predicate combination.

---

# 13. MANUAL QA

User test checklist must include:
- create fresh normal world;
- explore eligible forest/plains/taiga;
- observe deer;
- observe bear;
- observe boar;
- observe wolves;
- test fish/crayfish water;
- use one Bear egg repeatedly until family case;
- verify mother miniboss;
- use one Deer egg repeatedly;
- verify stag/doe/family outputs;
- verify no redundant family eggs in creative.
