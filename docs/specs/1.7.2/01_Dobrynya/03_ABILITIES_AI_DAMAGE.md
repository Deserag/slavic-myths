# DOBRYNYA — actual ability rules, AI and combat numbers

## Core stats
Max HP 230; visual height 2.40 blocks; collision width ~.8 and height ~2.2. Walking speed baseline .27 blocks/tick with side-step bursts constrained to max 1.2 blocks; follow player soft range 5–9, warp only if far away in normal world using existing ally behavior, never while story encounter locked; melee spear range approx 3.6 blocks (line-of-sight, bounded by collision). Armor/toughness derived from existing bogatyr formula rather than direct player diamond duplication: target 25–30% less physical damage than unarmored equivalent, significantly less tanky than Ilya.

## AI priority order (evaluated server-side ~every 4 ticks)
1. If combat finished/dead/no eligible owner -> idle/follow/world policy.
2. If cursed into aurochs -> transformedAI only; no active spear/shield skills. If ritual anchor is destroyed -> transform back clip.
3. If HP <=34.5 (15% of 230), has not retreated in current encounter and is not transforming -> `RETREAT`; leave active chase/attack; no swapping another hero into this slot.
4. If partner imminent targeted by magical projectile and front cone accessible and `ward_guard` off cooldown -> intercept as a limited defensive action; do not force aggro switch.
5. If boss is casting ritual curse and ward-break conditions met -> break ward or telegraphed magic object.
6. If enemy melee tell begins in weapon range -> timed parry decision (probabilistic 55–65% success when off CD, reduce against undodgeable boss spells).
7. Attack based on distance: long thrust at 2.2–3.6; three-hit combo at <=2.6; circular sweep only if 2+ foes in arc and no allied body in hitpath.
8. Patrol follow/player aid. No constant taunt, no invincible lock-in of boss attention.

## Skills (values initial balancing baselines, reconcile with effective damage rules)
- **Heroic Parry**: CD 8s; telegraph 0.15s, successful window 0.20s; up to 70% reduction of a *single* physical strike into Dobrynya, counters with 13 raw HP attack against attacker, 0.6s stagger for ordinary mobs, max .35s on bosses, never blocks entire ritual without special counter flag.
- **Threefold Spear Dance**: CD 6s; 3 hits 9/10/15 raw HP against target (each physical); no crit multiplier by default, max 1 hit per target per sweep contact phase. Avoid 34 direct unconditional damage by respecting armor and miss/blocks.
- **Long Spear Thrust**: CD 5.5s; range 3.6b, 21 raw physical HP, +.45b knockback vs normal mob; boss knockback tiny (<=.08b); miss when target moved behind cover.
- **Circular Sweep**: CD 14s; arc 210°, max radius 3.25b, 15 raw each to at most 5 foes, slow 1.1s only normal mobs; cannot hit same target twice; entity-vs-wall blockage respected.
- **Shield Against Charms**: CD 12s; guard duration 0.80s, frontal arc ±55°; reflect one projectiled `reflectable=true` magic bolt and negate it for protected ally within 1.7b behind. Area flames, summoned zones and curse-on-trace cannot be reflected; reduce splash 40% if appropriately guarded. No general immunity.
- **Spellbreaker**: CD 17s; up to 3.2b cast break; when Marinka's shield has exposed counter seal after successful projectile parry, make ward `BROKEN` for 8.0s. Only once per 13s after ward restoration; cannot be spammed; ritual anchor curse break requires player action, unless manually paired with ally assist.
- **Unbroken Resolve**: passive 35% reduction to curse-duration ONLY, 12s immunity to reapplication of same control effect after successfully resisting it; no immunity to damage.
- **Story-battle Retreat**: at 15% HP (<=34.5) once per encounter; route 10–14 blocks to prevalidated safe cover, use 3.15s drink animation; recover to 46% max HP in discrete pulses (<=106 HP), not to full; cannot rejoin until 8s out of danger. If defeated while retreating mark `INCAPACITATED` for this encounter; never allow mid-fight replacing him. Outside story battles use existing ally recovery rules where appropriate.

## Narrative special interaction
- When Dobrynya parries a reflectable bolt, it can ricochet toward Marinka. A successful hit flags short `WARD_EXPOSED`, bright cyan/silver crack; player attacks during 8s broken ward window. This creates player agency, not auto-winning hero.
- On forced aurochs transform, Dobrynya retains player-friendly faction, can intermittently charge Marinka once per 8–13s but has reduced targeting and unpredictable (telegraphed) path. Player must destroy the hex anchor to liberate him; attach anchor to deterministic location reachable within arena. Do not allow player to use world-protection to trap him permanently.
- In fight start, Marinka says an original line about turning the warrior's footprints against him; short Dobrynya response. Do not quote copyrighted film dialogue.

## Failure prevention
- Spear must neither have twice the range in damage than the animation depicts nor hit through solid walls; preserve friendly-fire config. Never apply buffs to a second nearby summoned hero or player as a substitute for Dobrynya's required story slot. Shield visually aims toward threat even if attack miss; limited mitigation only in arc.
