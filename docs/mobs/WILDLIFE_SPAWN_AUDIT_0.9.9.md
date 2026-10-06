# Wildlife spawn audit 0.9.9

COMPILES / headless spawn predicates and family transactions passed; full wildlife release acceptance incomplete.

Loaded biome modifications provide 68 eligible entries across bear, wolf, boar, stag, doe, pike, carp and crayfish. Ground placement accepts grass/podzol/coarse dirt/rooted dirt/moss and rejects lava. Fish/crayfish placement uses actual water and floor conditions at Y=-20, 62 and 120; obsolete absolute-height restrictions are removed. Aquatic IDs remain unchanged.

The creative tab contains one bear egg and one deer egg. Legacy cub/doe egg registry IDs remain for saved stacks and commands. Bear egg selects 90% ordinary / 10% mother with 1–2 cubs; deer egg selects 45% stag / 45% doe / 10% doe family with a 35% second-fawn chance. Server family placement preflights all children, rolls back failed insertions, persists parent IDs and is idempotent. Family logic is shared by normal block/fluid use and dispensers; ENTITY_DATA/spawner behavior retains vanilla handling. Actual user-use/dispenser consumption needs further headless coverage.

Natural mother selection is 6%. Natural deer SpawnGroupData assigns a shared herd UUID and consumes planned fawns from the configured 2–5-member group, avoiding extra children beyond the native pack. Mixed-sex natural herd generation remains IN PROGRESS; current native type-specific spawn groups are not claimed to satisfy every herd composition requirement. Wolf packs use bounded neighbor scans and front/flank/hesitation roles. Deer share fleeing directions and regroup; cub threats notify the owning mother, with unrelated bears excluded.

Runtime regression: two actual cubs have 12 HP / zero attack; mother 120 HP; a persisted mother cannot duplicate her family; one actual fawn has 12 HP and smaller collision dimensions. A fully blocked encounter leaves no partial family. The statistical fixture uses 1000 deterministic rolls and does not prove long-run natural spawn frequency or measured MSPT. Boss-bar lifecycle, actual natural herd sequences, crowded/water/cliff behavior and late client animations need additional verification.
