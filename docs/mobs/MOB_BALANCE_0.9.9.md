# Mob balance 0.9.9 — implementation and verification

COMPILES; release acceptance remains unfinished. Registry audit contains actual default attributes, before spawn equipment/modifiers; attack damage 2 on an unarmed bandit is not its final armed strike damage. Preserve registry IDs and existing stronger-mob kits.

| Existing entity | HP | Armor | Base damage | Implementation |
|---|---:|---:|---:|---|
| Kikimora | 36 | 2 | 5 | Ambush, two timed claws, retreat |
| Poludnitsa | 60 | 4 | 7 | Form transition, frontal sweep, warned heat |
| Polevik | 44 | 3 | 6 | Crop disturbance, warned dash/snare |
| Bannik | 80 | 5 | 7 | Steam tell, slap, water interruption |
| Igosha | 30 | 0 | 4 | Nuisance feint and retreat; offering behavior retained |
| Leshy | 100 | 8 | 8 | Forest disturbance, warned root, two short decoys, safe relocation |
| Bear | 50 | 3 | 7 | Warning and charge |
| Mother bear | 120 | 6 | 8 | Owned cub defense, combat-only boss bar, separate existing-material loot |
| Bear cub | 12 | 0 | 0 | No loot or XP |
| Wolf | 30 | 1 | 5 | Pack roles, hesitation, short lunge and retreat |
| Boar | 36 | 2 | 4 | Warned charge does 7 damage; collision stun |
| Stag / doe / fawn | 26 / 22 / 12 | 0 | 4 / 0 / 0 | Flee/regroup; close stag defense |
| Bandit fighter / archer / senior / heavy | 30 / 36 / 44 / 50 | equipment | equipment | Existing kit retained |
| Ataman / Nightingale | 150 / 250 | equipment | existing kit | Template Ataman health override corrected |

Headless evidence: six actual registered spirit attack cycles exercise telegraph/active/recovery/cooldown and NBT cancellation. Twelve existing strong-fighter goals run every server tick with exclusive MOVE/LOOK ownership. Ataman HP is checked through a real gate template marker, not just the attribute registry. Manual visual/audio/complete combat-kit QA is NOT STARTED. Dedicated special-effect, wall/miss and late-client tracking assertions remain incomplete. Full 0.9.8 bandit overhaul is not asserted as implemented in this baseline.
