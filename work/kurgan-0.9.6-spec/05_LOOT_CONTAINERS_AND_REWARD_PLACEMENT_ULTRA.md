# 05 — LOOT / CONTAINERS / REWARD PLACEMENT — ULTRA DETAIL

Goal:
exploring optional rooms must matter.

This is not the final 0.9.13 global progression pass, but Kurgans must stop feeling empty now.

---

# 1. INVENTORY CURRENT LOOT FIRST

Before editing:
list all current Kurgan loot tables and Kurgan-specific items.

Create:
`docs/kurgan/KURGAN_LOOT_CURRENT_0.9.6.md`

Do not delete existing artifacts accidentally.

---

# 2. CONTAINER TYPES

Use existing actual container implementations first.

Supported conceptual types:
- chest;
- trapped chest;
- burial urn/container;
- sarcophagus inventory/reward;
- offering cache;
- secret stash.

If custom urn/sarcophagus already exists, reuse it.
If not, only add a new container when needed and documented.

---

# 3. CONTAINER DENSITY TARGETS

## Small Kurgan
2–5 meaningful reward/container points total.
Not necessarily all chests.

## Warrior
5–9 total.

## Great
10–18 total across the whole complex.
Spread across branches.

A container point can be:
major chest,
urn cluster,
sarcophagus reward,
hidden cache.

Do not place 18 full chests.

---

# 4. EMPTY ROOM RULE

A room may be intentionally without loot if it provides:
- route choice;
- atmosphere;
- combat;
- clue;
- secret;
- shortcut.

But no sequence of 5 optional rooms should all be empty.

---

# 5. ROOM-BASED LOOT

## Main Burial Chamber
Primary:
existing burial artifacts;
jewelry;
ancient currency;
restoration materials;
rare relic chance.

Avoid:
fresh bread as signature reward.

## Warrior Hall
Weapons/components;
armor-related materials;
coins;
restoration/metal items.

## Treasury
Highest conventional loot density.
Mix several item categories.

## Ritual Hall
Ritual ingredients;
charms/relics;
rare ceremonial item.

## Trap Gallery
Bait reward or hidden safe reward.
Better average value because of risk.

## Flooded Crypt
One or two small hidden caches.
Potential special damp/water-adjacent materials if already in mod.

## Ossuary
Lower value;
jewelry fragment;
coin;
bone-related resource.

## Offering Store
Coins;
small ceremonial objects;
ingredients;
materials.

## Secret Reliquary
Highest rarity per roll, low quantity.

---

# 6. LOOT TABLE STRUCTURE

Prefer separate tables:
- kurgan/common_cache
- kurgan/burial
- kurgan/warrior
- kurgan/ritual
- kurgan/treasury
- kurgan/secret
- kurgan/great_special

Use actual namespace/folder conventions for 1.21.1.

Do not duplicate giant JSON pools if shared sub-tables/functions can be used cleanly.

---

# 7. ROLLS

Exact values should be tuned from actual item pool, but initial target:

Common cache:
2–4 rolls.

Burial:
2–5 rolls.

Treasury:
4–7 rolls.

Secret:
2–4 rolls, higher rarity.

Great special:
3–6 rolls with one meaningful rare opportunity.

Do not guarantee every rare artifact in one run.

---

# 8. SIDE PATH REWARD POLICY

At least:
- Small: >=40% optional dead-end branches have reward/secret value.
- Warrior: >=55%.
- Great: >=60%.

Value can be loot OR meaningful shortcut/secret, not necessarily chest.

---

# 9. TRAPPED LOOT

Trapped reward chance:
Small: low.
Warrior: moderate.
Great: moderate, not every chest.

Trap must have visual clue.

---

# 10. CONTAINER PLACEMENT

Never:
- chest floating;
- chest blocked from opening;
- chest clipping wall;
- all chests centered identically.

Preferred:
- against alcove wall;
- on low plinth;
- in side niche;
- behind partial rubble;
- beside sarcophagus but not touching it randomly.

---

# 11. HARVESTABLE BUILDING MATERIAL AS SECONDARY REWARD

Good-looking Kurgan blocks are themselves a reward because players may mine them.

That is allowed.

But the dungeon still needs actual item/container rewards.

---

# 12. LATER PROGRESSION PASS

Do not attempt to solve every economy issue.
0.9.13 can later rebalance global progression.
For 0.9.6:
make Kurgan rewards clearly worthwhile and thematic.
