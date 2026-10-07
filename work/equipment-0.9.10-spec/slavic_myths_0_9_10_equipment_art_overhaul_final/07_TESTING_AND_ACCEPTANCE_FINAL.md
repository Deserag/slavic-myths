# 07 — TESTING / ACCEPTANCE

No auto client launch.

# RESOURCE TESTS

For every changed item:
- texture exists;
- dimensions expected;
- model resolves;
- no missing parent/texture;
- no duplicate ID.

Create:
`docs/equipment/ASSET_DIMENSIONS_0.9.10.md`

# RECIPE TESTS

Verify:
- cores;
- smithing;
- templates;
- JEI;
- outputs;
- no conflicts.

# ABILITY TESTS

Thief Blade:
- cap 5;
- duration 5 sec;
- refresh;
- immunity.

Fire Blade:
- prime 5 sec;
- cooldown 12;
- wooden bonus.

Thunder Axe:
- cooldown 8;
- max 2 secondary;
- wet bonus.

Knyaz Sword:
- real shield block only;
- 3 sec window;
- consumed next hit.

Solovey Bow:
- full draw successful hit only;
- 5th hit proc;
- misses no count.

Harpoon:
- +2 once;
- 10 sec active cooldown.

# ARMOR TESTS

Full 4-piece only.
Remove one -> deactivate.

Knyaz:
threshold + cooldown.

Solovey:
no leap chaining.

Fire:
no real fire blocks.

Depth:
>=6 hit trigger + 20 sec cooldown.

# ENCHANT TESTS

Representative:
Sharpness/Smite/Unbreaking/Mending/Looting;
Protection variants;
Feather Falling/Respiration;
bow enchants.

Upgrading enchanted item must preserve valid enchantments.

# BALANCE REPORT

Compare Diamond / Netherite / Perunite / boss weapons:
damage;
DPS;
durability;
ability contribution.

Flag universal domination.

# MANUAL QA

Create `docs/MANUAL_QA_0.9.10_EQUIPMENT.md`.

Include visual and gameplay checks.

# FINAL CODEX RESPONSE

List exact:
- IDs changed;
- stats;
- abilities;
- materials;
- sets;
- recipes;
- enchant compatibility;
- test results;
- manual QA remaining.
