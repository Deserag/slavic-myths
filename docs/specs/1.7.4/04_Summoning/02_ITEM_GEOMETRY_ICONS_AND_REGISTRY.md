# Item visuals, usage animation, sound and registry integration

Included real 64x64 sprites: `finist_call_feather.png`, `kartaus_black_seal.png`, `kartaus_spawn_egg.png` in `06_References/Technical/Item_Sprites`; preview sheet `06_References/Technical/Summoning_items_preview.png`. Real images and exact pixel colors are *not* source code, but enough for resource-pack assets. JSON generated item models must point directly to these files. Use nearest filter; maintain alpha with no pale border halo. Add local inventory tooltip and search categories. Copy binary OGG only from archive to configured resources; do not generate silent filenames.

Finist hero summon animation (28t): tick0 player raises feather ~10° on client, t6 gold small spiral (6–10 particles) at feather, t12 updraft ribbon, t15 Finist materializes at safe ground, t20 bird perches on arm, t28 calm. No invulnerability until fully spawned unless common item pattern already establishes. Failure: one light click sound only, no swirl/partial entity.

Kartaus seal animation (40t): t0 seal lifts, t8 black-red circle of 8–12 visible tiles (NOT terrain change); t15 3 dark geometric columns rise <2 blocks; t22 Kartaus silhouette and eye staff assemble; t30 boss bar, t40 first boss telegraph, summon guards not before tick40. 1 spawned boss only. Do not leave ritual decals in world after event; 20t fade. No massive initial worldgen. Sound levels moderate; last player action should clearly start quest.

Creative egg follows standard spawn egg usage, no lengthy ritual. Valid on land with >=3 block clearance; dimension/owner restrictions disabled in creative debug but drop/progression rewards disabled. Do not turn Item B into egg disguised as quest item.

Registry details: anchor IDs to current mod conventions and existing data generator. Confirm sound key, translation resources, models and registered entities exactly once. If class-loading circular dependencies exist, move only current new registration wiring, not the entire registry infrastructure. Prevent `DeferredRegister` duplicate registration on hot reload.
