# SLAVIC MYTHS — 0.9.10 EQUIPMENT & ART OVERHAUL
## FINAL STRICT IMPLEMENTATION SPECIFICATION
### Minecraft 1.21.1 / NeoForge / Java 21
### Mod ID: `slavicmyths`

# 0. TASK STATUS

Codex is NOT the designer in this task. Codex is the implementer.

It MUST:
- audit the actual repository first;
- preserve existing registry IDs where possible;
- rework weak items instead of creating unnecessary duplicates;
- add only new items explicitly allowed here;
- implement exact ability logic, caps, durations and cooldowns;
- preserve vanilla enchant compatibility;
- prepare clean extension points for 0.9.11 runes/enchantments/classes.

It MUST NOT:
- simplify systems because implementation is difficult;
- silently change HP/stat/cooldown values;
- create extra boss drops without need;
- implement runes/classes here;
- hide unfinished art behind recolors.

# 1. VERSION PURPOSE

0.9.10 must fix:
1. weak weapon/tool/armor art;
2. repeated silhouettes;
3. short spear problem;
4. boxy helmets;
5. special weapons that are only stat sticks;
6. boss loot with weak equipment payoff;
7. mod gear that cannot use appropriate vanilla enchants;
8. lack of clean groundwork for 0.9.11.

# 2. SCOPE

IN:
- visual overhaul;
- stat rebalance;
- intrinsic weapon abilities;
- boss-material upgrade chains;
- four endgame armor paths;
- full-set bonuses;
- vanilla enchant compatibility;
- item/entity tags for future systems;
- recipe/smithing integration;
- visual QA.

OUT:
- rune UI/effects;
- class UI/tree;
- full custom enchantment system.

# 3. ASSET STANDARD

Replace old blanket 256x256 rule.

Targets:
- simple materials/food: 32x32;
- normal weapons/tools/accessories: 64x64;
- boss weapon/complex armor: 64x64 or 128x128;
- concept/source art can remain 256x256+.

Never upscale old low-res art and call it reworked.

# 4. REFERENCE

Use `references/ref_01_equipment_armor_concepts.png`.

Image is visual reference only. This text defines mechanics.

# 5. EQUIPMENT PHILOSOPHY

Every rare weapon must define:
- role;
- unique silhouette;
- intrinsic property;
- intended targets;
- vanilla enchant compatibility;
- future rune category.

# 6. INTRINSIC ABILITIES

Intrinsic abilities are not enchantments and not runes.
They persist on the item and must be server-authoritative where state exists.

See `02_WEAPONS_INTRINSIC_ABILITIES_FINAL.md`.

# 7. ARMOR

Implement/rework:
- Knyaz/Druzhina heavy;
- Solovey mobile;
- Fire Serpent;
- Depth/Elder Vodyanoy.

See `03_ARMOR_SETS_AND_SET_BONUSES_FINAL.md`.

# 8. UPGRADE MODEL

Use netherite-like philosophy:
high-tier base item + Slavic upgrade component -> upgraded equipment.

Prefer Smithing Table in 0.9.10.
Future equipment workbench may reuse same data/recipes.

# 9. VANILLA ENCHANTMENTS

Relevant Slavic equipment must accept equivalent vanilla enchantments via modern tags/data.
Avoid hardcoding many item IDs.

# 10. ENTITY TAGS

At minimum prepare:
- `slavicmyths:bleed_immune`
- `slavicmyths:wooden_entities`
- `slavicmyths:spirit_entities`
- `slavicmyths:aquatic_entities`
- `slavicmyths:silver_vulnerable`

Populate from ACTUAL registry.

# 11. AUDIT

Create `docs/equipment/EQUIPMENT_AUDIT_0.9.10.md`.

For every weapon/tool/armor/accessory:
- registry ID;
- stats;
- texture dimensions;
- visual status KEEP/POLISH/FULL_REDRAW;
- gameplay status KEEP/BUFF/REWORK;
- enchant compatibility;
- future rune category;
- action taken.

# 12. TOOLTIP / JEI

Every unique item must expose intrinsic ability clearly in RU/EN.
Recipes must appear in JEI.

# 13. NO CLIENT AUTO-LAUNCH

Build/data/resource tests only.

# 14. DONE WHEN

- weak equipment genuinely redrawn;
- dagger/sword/spear/axe silhouettes differ;
- spears are long;
- shields work if still broken;
- intrinsic abilities work;
- Bleed = 5 sec and max 5 stacks;
- Fire Strike respects `wooden_entities`;
- four armor paths work;
- boss upgrades use existing drops or tightly controlled new materials;
- compatible vanilla enchants work;
- no rune/class feature creep;
- tests and audit pass.
