# SLAVIC MYTHS — 0.9.9 MOB OVERHAUL + GLOBAL WORLDGEN COVERAGE FIX
## FINAL / HARD REQUIREMENTS / NO-CODEX-FANTASY SPECIFICATION
### Minecraft 1.21.1 / NeoForge / Java 21
### Mod ID: `slavicmyths`

---

# 0. STATUS OF THIS DOCUMENT

This is the final implementation contract for 0.9.9.

Codex is NOT the designer in this task.
Codex is the implementer.

It MUST:
- inventory the actual repository;
- preserve real registry IDs;
- implement the mechanics written here;
- report deviations instead of inventing replacements.

It MUST NOT:
- simplify systems because they are difficult;
- silently change HP values or distance limits;
- invent random new structures, mobs, phases or rewards;
- call an entity "fixed" because it compiles;
- call worldgen "fixed" because `/locate` searches farther;
- call natural spawning "fixed" because `/summon` works.

If current source code conflicts with this specification, preserve IDs/data compatibility where practical but change the implementation.

---

# 1. VERSION HAS TWO EQUALLY MANDATORY BLOCKERS

0.9.9 consists of TWO major projects.

## PROJECT A — GLOBAL WORLDGEN COVERAGE FIX

The 0.9.5 worldgen rework did NOT solve structure accessibility.

Manual testing proved:
- Small Kurgans could be thousands of blocks away.
- Medium/Warrior Kurgans could not be found even after searching regions at 1000/2000/3000/5000 blocks.
- Bandit camps show the same structural problem.
- A flat/snow-flat test area could still be rejected as "no safe place".

Therefore this is now treated as a SHARED WORLDGEN ARCHITECTURE BUG.

All Slavic Myths structure families must be audited and migrated to one coverage-aware placement contract.

Detailed rules:
`01_GLOBAL_WORLDGEN_COVERAGE_CONTRACT_FINAL.md`

## PROJECT B — MOB OVERHAUL

Every registered mob/animal must be audited.

Each entity goes into one category:
- FULL_REWORK;
- MAJOR_REWORK;
- POLISH_ONLY;
- NON_COMBAT_NPC;
- AMBIENT/WILDLIFE.

Detailed mechanics:
`02_FULL_REWORK_MOBS_FINAL.md`
`03_EXISTING_MOBS_POLISH_MATRIX_FINAL.md`
`04_WILDLIFE_NATURAL_SPAWN_AND_FAMILY_LOGIC_FINAL.md`

---

# 2. DO NOT ASSUME THE REGISTRY FROM MEMORY

Before code changes:
1. enumerate all current `EntityType` registrations;
2. enumerate spawn eggs;
3. enumerate attribute registrations;
4. enumerate natural spawn registrations / biome modifiers;
5. enumerate loot tables;
6. enumerate entity sounds;
7. enumerate renderers/models/textures;
8. enumerate structure-linked spawn logic.

Create:
`docs/mobs/MOB_REGISTRY_AUDIT_0.9.9.md`

Columns:
- registry ID;
- entity class;
- current HP;
- attack damage;
- armor;
- movement speed;
- follow range;
- spawn egg;
- natural spawn yes/no;
- biome(s);
- spawn weight;
- min/max group;
- loot table;
- sounds;
- renderer/model;
- assigned overhaul category;
- found problems.

The actual repository wins for IDs.
This prompt wins for desired behavior.

---

# 3. FIX NATURAL SPAWNING FIRST

The user reports that newly added animals do not naturally spawn.

This is a BLOCKER.

Before redesigning combat:
- verify NeoForge 1.21.1 spawn registration;
- verify biome modifiers;
- verify `SpawnPlacements`;
- verify heightmap type;
- verify predicate;
- verify creature category;
- verify spawn weight/group ranges;
- verify tags/biome selection;
- verify final spawn initialization.

A wildlife mob that can only be spawned through a command or egg is BROKEN.

Detailed rules:
`04_WILDLIFE_NATURAL_SPAWN_AND_FAMILY_LOGIC_FINAL.md`

---

# 4. FIX SPAWN EGGS

## Bears
There must be ONE bear spawn egg.

Do NOT keep separate normal-bear and cub/female eggs.

One bear egg normally spawns a standard adult bear.

With a small probability it creates the special family encounter:
- protective mother bear;
- 1–2 cubs;
- mother acts as a miniboss.

Exact behavior in file 04.

## Deer
There must be ONE deer spawn egg for the deer family.

The one egg may resolve to:
- adult stag;
- adult doe;
- rarely a family/small herd setup with doe/fawn composition.

Do NOT expose separate clutter eggs just because separate internal entity types exist.

---

# 5. MOB DESIGN STANDARD

Every combat mob must define ALL of:

- gameplay role;
- attitude toward player;
- target acquisition;
- aggro conditions;
- disengage conditions;
- movement;
- navigation type;
- preferred combat range;
- HP;
- damage;
- armor;
- speed;
- follow range;
- attacks;
- attack startup/telegraph;
- active frames;
- recovery;
- cooldown;
- special states;
- anti-cheese;
- water/fire/sun/environment interactions where relevant;
- animation expectations;
- sound set;
- drops;
- rare drops;
- XP;
- spawn logic;
- encounter purpose.

No mob may remain:
`walk to player -> swing every N ticks`.

---

# 6. TELEGRAPH / ACTIVE / RECOVERY

Every significant attack is modeled as:

`TELEGRAPH -> ACTIVE -> RECOVERY -> COOLDOWN`

The player must be able to learn attacks.

Do not implement difficulty by:
- zero-warning damage;
- instant unavoidable stun;
- permanent spam;
- massive HP only.

---

# 7. FULL REWORK GROUP

These mobs require fundamental redesign or a major rewrite:

- Kikimora;
- Poludnitsa;
- Polevik;
- Bannik;
- Igosha;
- Leshy;
- Brown Bear;
- Wild Boar;
- Forest Wolf / wolf pack behavior.

Exact requirements:
`02_FULL_REWORK_MOBS_FINAL.md`

---

# 8. POLISH GROUP

These already have useful foundations and must NOT be rewritten from zero unless code is broken:

- Upyr;
- Nav;
- Kurgan Druzhinnik;
- Kurgan Voivode;
- Buried Volkhv;
- Restless Prince;
- Ovinnik;
- Volkolak;
- Fire Serpent;
- Podvey;
- Likho One-Eyed;
- Tugarin Zmey;
- Rusalka;
- Vodyanoy;
- Elder Vodyanoy.

Also:
- 0.9.8 bandits / Ataman / Solovey must keep their new design.
- Solovey remains Boss 250 HP.
- Ataman remains 150 HP.
- ordinary bandits remain in the 30–50 HP ladder defined in 0.9.8.

Use:
`03_EXISTING_MOBS_POLISH_MATRIX_FINAL.md`

---

# 9. NON-COMBAT NPC RULE

Do not force combat redesign onto:
- Baba Yaga;
- Domovoy;
- service/reputation NPCs.

Their navigation/animations may be bug-fixed, but their identity remains non-standard combat.

---

# 10. DROPS AND REWARDS

Combat must have a reason.

Every dangerous creature must have:
- thematic common drop;
- useful material/drop where appropriate;
- rare trophy or rare component where warranted;
- no meaningless "2 rotten flesh" placeholder.

Do not invent dozens of new items.

First audit existing items.
Only add a new reward when no existing item fills the role.

Detailed rules:
`06_MOB_LOOT_REWARDS_AND_SOUND_FINAL.md`

---

# 11. AUDIO

Every major redesigned creature needs:
- ambient;
- hurt;
- death;
- attack telegraph;
- attack impact/special;
- state transition sound where relevant.

Reusing vanilla sounds is allowed only when it actually fits.
Do not use one zombie sound set for every spirit.

---

# 12. PERFORMANCE

AI must not:
- scan huge entity boxes every tick;
- pathfind from scratch every tick;
- summon unlimited helpers;
- maintain orphan state after unload.

Use cooldowns and bounded scans.

---

# 13. MULTIPLAYER / SERVER

All gameplay state is server-authoritative.

Client:
- render;
- animation;
- sound request/display;
- UI/boss bars.

Do not make combat depend on client-only Xaero/Jade/etc.

---

# 14. REQUIRED REPORTS

Create:
- `docs/mobs/MOB_REGISTRY_AUDIT_0.9.9.md`
- `docs/mobs/MOB_BALANCE_0.9.9.md`
- `docs/mobs/WILDLIFE_SPAWN_AUDIT_0.9.9.md`
- `docs/worldgen/STRUCTURE_COVERAGE_AUDIT_0.9.9.md`
- `docs/MANUAL_QA_0.9.9.md`

---

# 15. NO AUTO-RUN CLIENT

Do not launch Minecraft automatically.

Allowed:
- build;
- data validation;
- automated tests;
- headless simulations;
- report generation.

The user performs manual gameplay QA.

---

# 16. DEFINITION OF DONE

0.9.9 is NOT done unless:

WORLDGEN:
- global structure coverage architecture is fixed;
- Kurgans pass;
- bandit structures pass;
- all remaining structure families are audited;
- Small/Medium/Large distance contracts pass;
- superflat placement passes;
- locate uses real placement data.

MOBS:
- natural wildlife spawning works;
- one Bear egg exists;
- one Deer family egg exists;
- mother-bear miniboss family encounter works;
- all FULL_REWORK mobs have real stateful AI;
- sounds are assigned;
- rewards are meaningful;
- old strong mobs are normalized, not pointlessly rewritten;
- tests/build pass.
