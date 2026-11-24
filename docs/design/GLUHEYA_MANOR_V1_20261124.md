# Gluheya manor V1

Approved 39 × 30 × 27 block coordinate blueprint (6907 non-air voxels).
Shared winter palace site job, chunk tickets, encounter grid and saved records.
Natural forest encounters use the existing lookahead (320–2048 blocks);
the winter superflat template uses the same noise generator. A deterministic boss
UUID and saved waiting set prevent normal respawn after defeat.

Commands (operator level 2):
- /slavicmyths dev generate gluheya_manor — force a manor near the source, survey surface height.
- /slavicmyths dev generate gluheya_manor X Y Z — force at blueprint origin (front centre, foundation Y), regardless of biome or terrain. Destructive to terrain and blocks in the footprint. Leave the footprint during construction.
- /locate structure slavicmyths:gluheya_manor — closest recorded entrance, or queue a matching forest encounter when none exists.

Kitchen, laboratory, upstairs bedroom/study, separate stair, double-height arena,
40 lanterns and 8 sea lanterns. The blueprint preserves approved coordinates;
the palette translates furniture into valid Minecraft states. CI checks checksum,
room routes, stair headroom and lighting count. In-game lighting, block survival,
generation and boss navigation still require a runtime playtest.
