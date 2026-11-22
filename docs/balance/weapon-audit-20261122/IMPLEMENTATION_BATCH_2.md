# Weapon progression balance — implementation batch 2 (2026-11-22)

Branch: `feature/weapon-skill-input-standard-20261122`.

This batch follows the first completed CI-validated perunite/Nether weapon tuning. It strengthens three underperforming special weapons rather than nerfing existing strong rewards.

| Weapon | Component | Before | After | Rationale |
|---|---|---:|---:|---|
| abyss_staff | Magic pulse against up to 4 targets | 4 | 8 | Low per-target damage for a special staff with a long cooldown |
| abyss_staff | Ability cooldown | 200 ticks (10 s) | 140 ticks (7 s) | More usable while still bounded by durability, range and target cap |
| storm_staff | Magic pulse base damage | 8 | 11 | Raise single-target staff impact against high-health enemies |
| pool_spear | Base melee attack damage | 9 | 11 | Improve usefulness outside water without removing its specialist identity |
| pool_spear | Water-only charged strike damage | 9 | 12 | Reward the water restriction, 16-tick charge and 120-tick cooldown |

Storm staff's existing modifiers (staff mastery and thunder rune) apply multiplicatively to the new base damage; its cooldown stays 32 ticks, or 28 with applicable rune.

No changes to enemy health, recipes, weapon input, equipment tiers, target selection, charge time, or damage sources. This is code-verified intended damage, not game-measured DPS; actual performance may vary with armor, modifiers and combat context.

Commits:
- 001cb2c404e408e3e6f8afdedb714e83319e922e
- c84a7ff565f15d7cbd9ab088b5e1de351a2d4e5e
- 6c68a924e1141c828ec0e2f31d8f3da00a9c025d

Validation: GitHub Actions clean build will run on push for this branch; in-game GameTests and multiplayer gameplay are not established by this report. Catalog.md remains historical before/after baseline.
