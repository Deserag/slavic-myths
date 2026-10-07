# 01 — KURGAN ENCOUNTER SPAWN
## KURGAN-SPECIFIC ENEMIES MUST ACTUALLY APPEAR

The current outcome "beautiful dungeon but no Kurgan enemies" is FAILED implementation.

These enemies were created for this content and must be integrated directly into generated Kurgan instances.

---

# 1. REQUIRED KURGAN ROSTER

Use the actual existing registry IDs.

Expected existing IDs:

- `slavicmyths:upyr`
- `slavicmyths:nav`
- `slavicmyths:kurgan_druzhinnik`
- `slavicmyths:kurgan_voevoda`
- `slavicmyths:buried_volkhv`
- `slavicmyths:unresting_prince`

Do not create duplicate entity types if these already exist.

References are bundled in `/references`.

---

# 2. DO NOT USE BIOME NATURAL SPAWN AS THE KURGAN ENCOUNTER SYSTEM

Kurgan enemies inside the dungeon are structure encounters.

Do NOT depend on:
- random biome spawn;
- normal mob-cap luck;
- darkness alone;
- generic monster spawning.

A player entering a generated Kurgan must encounter the intended Kurgan roster even if natural spawning is disabled or mob-cap conditions are unfavorable.

Use structure/room encounter data.

---

# 3. ROOM ENCOUNTER STATE

Every combat-capable generated room needs a stable encounter identifier tied to its generated Kurgan instance.

Conceptual state:

`UNTRIGGERED`
`ACTIVE`
`CLEARED`

This may be implemented using the project's existing structure state system.

Rules:

UNTRIGGERED:
- no encounter has started.

ACTIVE:
- room was entered/triggered;
- intended enemies exist or are eligible to be restored after unload.

CLEARED:
- required encounter enemies were defeated;
- room never creates the same normal encounter again.

Do not create an infinite farming spawner.

---

# 4. TRIGGER DISTANCE

Normal room encounter:
trigger when a player enters the room or comes within approximately 6–10 blocks of its defined encounter center.

Do not spawn enemies:
- directly inside player;
- behind sealed geometry that cannot open;
- inside blocks;
- directly on trap trigger blocks;
- in lava;
- in invalid deep water unless the entity is intentionally compatible.

Minimum preferred spawn distance from triggering player:
3 blocks where room size allows.

---

# 5. UNLOAD / DEATH SAFETY

If an encounter was ACTIVE but all encounter entities disappeared because chunks unloaded or entities were cleaned up unexpectedly:

on the next room activation:
- detect whether assigned encounter mobs still exist;
- if none exist and room is not CLEARED, restore the required encounter ONCE.

Never duplicate already-living encounter entities.

Player death does not automatically mark room CLEARED.

---

# 6. SMALL KURGAN — LOCKED POPULATION

Small Kurgan is not empty.

Target ordinary population across the dungeon:

- Upyr: **2–4**
- Nav: **0–1**
- Kurgan Druzhinnik: **25% chance for exactly 1**

Hard minimum:
at least **2 Kurgan-specific hostile entities** in every valid Small Kurgan.

At least one encounter must be between entrance and main burial reward.

No miniboss.
No Unresting Prince.

Recommended room assignment:

Vestibule:
- normally empty OR 1 Upyr if dungeon has enough space.

Collapsed Chamber:
- 1 Upyr.

Ossuary / burial side-room:
- 1 Upyr;
- 30% chance replace/add Nav according to total cap.

Main Burial Chamber:
- 1 Upyr or Nav;
- 25% chance Druzhinnik, but only one Druzhinnik total.

Treasure/reward room:
- must not be completely unguarded if it contains the best Small-Kurgan loot;
- 1 surviving normal enemy encounter before or inside reward access.

---

# 7. WARRIOR / CLAN KURGAN — LOCKED POPULATION

Ordinary population:

- Upyr: **3–5**
- Nav: **2–3**
- Kurgan Druzhinnik: **2–4**

Hard minimum:
at least:
- 2 Upyr;
- 1 Nav;
- 2 Druzhinnik.

## VOEVODA

Warrior Kurgan generation must contain a dedicated Warrior Burial / miniboss chamber.

That chamber contains:

**exactly 1 `kurgan_voevoda`.**

Voevoda is NOT a random 10% ambient mob.

He is the Kurgan miniboss encounter.

HP remains:
**150**

After Voevoda is defeated, his dedicated encounter is CLEARED permanently for that Kurgan instance.

No Buried Volkhv required in Warrior tier unless the existing generation deliberately generates a ritual-miniboss branch; do not randomly place Volkhv in corridors.

No Unresting Prince.

---

# 8. GREAT KURGAN — LOCKED POPULATION

Great Kurgan must visibly use the complete Kurgan enemy ecosystem.

Ordinary population:

- Upyr: **5–8**
- Nav: **3–5**
- Kurgan Druzhinnik: **4–6**

Hard minimum:
- 4 Upyr;
- 3 Nav;
- 3 Druzhinnik.

Do not place all of them in one room.

Spread over the route.

---

# 9. GREAT KURGAN MINIBOSSES

Great Kurgan must contain both dedicated miniboss encounters.

## Kurgan Voevoda

Exactly:
**1**

Room:
Warrior Burial Hall / dedicated martial side chamber.

HP:
**150**

## Buried Volkhv

Exactly:
**1**

Room:
Ritual Hall / dedicated ritual side chamber.

HP:
**150**

Do NOT allow them to appear as corridor random spawns.

---

# 10. GREAT KURGAN BOSS

Final chamber:

**exactly 1 `unresting_prince` encounter.**

HP:
**350**

Only Great Kurgan.

Do not spawn him during normal corridor generation.

Do not spawn multiple copies.

The boss encounter must integrate with the existing seal/final-room/clear state.

If the boss has already been defeated in that generated Kurgan:
do not respawn him during ordinary revisit.

---

# 11. ROOM-SPECIFIC ENEMY TABLE

Use room identity, not arbitrary random coordinates.

## Entrance / Vestibule
Enemy density:
LOW.

Allowed:
- Upyr.

Avoid:
- Voevoda;
- Volkhv;
- Prince.

Purpose:
give player room to enter and understand environment.

---

## Crossroads / Navigation Hub
Enemy density:
LOW–MEDIUM.

Allowed:
- 1 Upyr;
- OR 1 Nav;
- Warrior/Great may use 1 Druzhinnik.

Do not block every branch with a crowd.

---

## Main Burial Chamber
Enemy density:
MEDIUM.

Small:
Upyr/Nav.

Warrior:
1 Druzhinnik + possible Upyr.

Great:
Druzhinnik + Upyr/Nav combination.

---

## Warrior Burial Hall
Enemy density:
HIGH.

Warrior tier:
dedicated Voevoda room.

Great tier:
dedicated Voevoda room OR strong Druzhinnik formation if Voevoda chamber exists elsewhere.

Never place two Voevodas in one Kurgan.

---

## Treasury
Enemy density:
MEDIUM.

Before high-value chest access:
- Small: 1 normal Kurgan enemy.
- Warrior: 1–2 normal enemies.
- Great: 2–3 normal enemies.

Do not put miniboss directly inside a tiny treasure closet.

---

## Ritual Hall
Enemy identity:
Nav / Buried Volkhv.

Warrior without Volkhv branch:
1–2 Nav.

Great:
dedicated Buried Volkhv encounter.
May include maximum 1 Nav initially.

Volkhv's own summon mechanics remain responsible for later summoned adds.

---

## Trap Gallery
Do not combine lethal trap activation with an unavoidable mob pile.

Default:
no initial mob OR one delayed Nav/Upyr after trap has telegraphed.

---

## Flooded Crypt
Allowed:
- Nav;
- Upyr only where floor/pathing is valid.

Avoid shield-heavy Druzhinnik if navigation is poor.

---

## Ossuary
Allowed:
- Upyr;
- Nav.

Good location for ambush.

---

## Offering Store
LOW density.

Allowed:
- 0–1 Upyr/Nav.

Do not guard every food/minor container with elite combat.

---

## Secret Reliquary
If high-value reward:
- 1–2 normal Kurgan enemies;
- Great may use one Druzhinnik.

No duplicate miniboss.

---

## Collapsed Chamber
Allowed:
- Upyr;
- Nav if enough room.

No Voevoda/Volkhv.

---

## Descent Hall
Use sparingly:
- 1 Upyr/Nav ambush;
- Warrior/Great can use one Druzhinnik.

Keep stairs navigable.

---

## Final Deep Chamber
Great:
Unresting Prince only as initial boss encounter.

Do not pre-fill final boss arena with 8 ordinary mobs.

Boss abilities may summon the limited adds already defined by boss design.

---

# 12. ENTITY MECHANICS ARE PRESERVED

Do not replace existing Kurgan entity combat mechanics merely to integrate spawning.

Expected identity:

Upyr:
40 HP, claws/dash/bite identity.

Nav:
32 HP, slow/reposition identity.

Kurgan Druzhinnik:
60 HP, sword/shield identity.

Kurgan Voevoda:
150 HP miniboss.

Buried Volkhv:
150 HP ritual/ranged miniboss.

Unresting Prince:
350 HP multi-phase boss.

If the current 0.9.9 Mob Overhaul contains a newer corrected implementation, preserve the newer mechanics while keeping these roles and encounter placement.

---

# 13. NO VANILLA SUBSTITUTE

Forbidden:

"Could not spawn Kurgan mob, so spawn Zombie/Skeleton instead."

If a required registry entity is unavailable:
FAIL the test and report the missing registration.

Do not hide the bug.

---

# 14. COMMAND-GENERATED KURGAN

A Kurgan generated through dev/manual command must receive the SAME encounter metadata as naturally generated Kurgan.

Command generation must not produce:
"geometry only, no enemies".

This is mandatory because manual testing uses command-created structures.

---

# 15. SPAWN TIMING

Prefer room-triggered spawning rather than spawning the entire dungeon population immediately when structure is placed.

Reason:
- lower server load;
- mobs do not wander away before player reaches room;
- easier persistent encounter state.

Do not use visible vanilla spawner blocks unless a specific room design explicitly calls for one.

---

# 16. FINAL ACCEPTANCE

A generated Kurgan is FAILED if:
- no Kurgan-specific mobs appear;
- miniboss chamber is empty;
- Great final room lacks Prince;
- enemies infinitely respawn;
- manual-command Kurgan lacks encounters;
- generic vanilla mobs replace missing custom enemies.
