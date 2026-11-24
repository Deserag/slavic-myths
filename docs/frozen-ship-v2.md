# Frozen ship V2 / Ледяной корабль

Approved voxel blueprint: 83 × 46 × 38, 16,022 non-air cells. The stored deflate stream uses x=-44..38, horizontal z=-23..22, y=-1..36, in y/z/x order. CRC32 cddb5d6e. No sails. The material palette uses vanilla wooden blocks and stable packed/blue ice; light ice is mapped to packed ice to avoid melting. Full-block fabric placeholders preserve the prototype volumes.

Command: `/slavicmyths dev generate frozen_ship` (64 blocks north, automatic surface height), or `/slavicmyths dev generate frozen_ship <pos>` (explicit ice-plane coordinate). Operator permission 2. Manual placement replaces existing terrain in the ship footprint, including built blocks; use a disposable test world. Biome restrictions are ignored only for this command.

Automatic encounters use the same winter grid and SavedData. The first ocean site is sought progressively on a 16-block grid by biome noise, including the full encounter cell, with conservative separation from previously occupied cells and recorded ships; the winter preset prioritizes 768,256. Only vanilla frozen_ocean/deep_frozen_ocean anchors qualify. The builder loads chunks through SiteChunks and uses a time/operation budget. A completed ship is saved before captain spawning; peaceful worlds retain a pending captain until difficulty permits spawning. Killing a captain does not schedule respawn.

Verification required in Minecraft 1.21.1: clean build, new winter test preset, normal worlds with both ocean types, command on land and in water, save/reload, defeat and revisit, interior stairs and boarding access, collision and terrain restoration.

## Rigging and advance generation

The original V2 blueprint is preserved. Four isolated rope cells are removed, 14 deck anchors and one missing vertical bow-chain link are added. The remaining diagonal rigging is connected iron bars rather than single-axis chain blocks. Saved ships receive an in-place rigging upgrade when the required chunks are loaded, without rebuilding the hull or respawning the captain. The first site is planned from the earliest server ticks; subsequent captain sites are sampled from biome noise up to 1024 blocks ahead, and new jobs do not start within 256 blocks of a non-spectating player. The old 160-block loaded-chunk trigger no longer places captain ships. Chunk loading is prefetched in batches with existing SiteChunks. Rapid teleportation may still reach a site before its bounded job completes.

## Acceptance — 2026-11-24

Owner approved the frozen ship V2, including the rigging correction and earlier generation, and requested that the result be recorded as accepted. Accepted implementation baseline: `432a3440939e111719b8276a2d2bb63de29803e3`. GitHub Actions Java 21 build and automated geometry/layout checks succeeded (run 45374205923). Preserve the approved design, two decks, sail-free masts, ice formations, captain arena, ocean-only automatic placement and operator generation command. No further redesign is requested. This approval does not independently certify every runtime scenario in the verification checklist above.
