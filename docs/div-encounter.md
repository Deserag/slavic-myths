# Div / Див — fourth savanna boss

## Approved visual lock (2026-11-27)
Owner approved concept board variant 3 (four-limbed anatomical revision). The creature is primarily a giant bird, not a humanoid in feather clothing. Cubic player-model head, two pairs of amber glowing eyes (exactly four), no beak. Massive paired feathered wings, dark purple/black/burgundy plumage with bone-colored accents, large feathered tail. Exactly four clawed limbs: two walking bird legs and two grasping bird limbs attached to lateral chest below wing roots, visible independent joints. No limbs growing out of wing feathers. Preserve the approved silhouette and layered plumage. Reference image remains in the design conversation; this document records the text specification only.

## Combat direction — draft, pending owner approval
Proposed tier VIII, 1600 actual HP, armor 16, toughness 6, knockback resistance 0.9, follow range 64; hostile mythical beast. Size draft: height 8–10 blocks, wingspan 16–20. Reuse shared uncapped health mechanism (CreatureHealthBalance) as appropriate; verify real health, save/load and bossbar. Ground melee, ranged curses, flight and landing windows. Four eyes communicate curse charge. Proposed Doom Mark: maximum four stacks, visible individually, 12-second independent expirations, fourth triggers a delayed avoidable attack and consumes marks; no permanent unavoidable debuff. Proposed phases >70%, 35–70%, <=35% (1120 and 560 HP). No combat stats, attacks, rewards, spawn rules or code are approved by this document beyond the owner-approved visual design; implementation not started.

## Approved combat direction and first implementation (2026-11-27)
Owner approved model V1 and 13 Python attack animations. Tier VIII, 1600 HP. Ground and ranged attacks, four independent curse timers and flight attacks, single spawn egg. First Java port is unverified: build, runtime, exact animation timing and balance still need validation. Loot rewards not approved; empty loot table for now.

Follow-up adds collision-checked egg use, curse stack/health GameTests alongside the isolated savanna test suite, hostile bird patrol/prey selection and creature tags. The first combat port uses server-traced particle projectiles (not a registered visible feather model); landing, flight, animation transitions and all 13 skill timings still require in-game QA.

Wing motion interpolates the folded/extended Python feather endpoints; eye-only emissive texture remains flat on the player head. Both are unverified visually in the game.
