# Frozen ship V2 / Ледяной корабль

Approved voxel blueprint: 83 × 46 × 38, 16,022 non-air cells. The stored deflate stream uses x=-44..38, horizontal z=-23..22, y=-1..36, in y/z/x order. CRC32 cddb5d6e. No sails. The material palette uses vanilla wooden blocks and stable packed/blue ice; light ice is mapped to packed ice to avoid melting. Full-block fabric placeholders preserve the prototype volumes.

Command: `/slavicmyths dev generate frozen_ship` (64 blocks north, automatic surface height), or `/slavicmyths dev generate frozen_ship <pos>` (explicit ice-plane coordinate). Operator permission 2. Manual placement replaces existing terrain in the ship footprint, including built blocks; use a disposable test world. Biome restrictions are ignored only for this command.

Automatic encounters use the same winter grid and SavedData. The first ocean site is sought progressively by biome noise; the winter preset prioritizes 768,256. Only vanilla frozen_ocean/deep_frozen_ocean anchors qualify. The builder loads chunks through SiteChunks and uses a time/operation budget. A completed ship is saved before captain spawning; peaceful worlds retain a pending captain until difficulty permits spawning. Killing a captain does not schedule respawn.

Verification required in Minecraft 1.21.1: clean build, new winter test preset, normal worlds with both ocean types, command on land and in water, save/reload, defeat and revisit, interior stairs and boarding access, collision and terrain restoration.
