from pathlib import Path
p=Path('tools/kurgan-tests/org/slavicmyths/swamp/SwampGameTests.java');s=p.read_text(encoding='utf-8');old='   var feature=org.slavicmyths.registry.ModFeatures.SWAMP_NATURE.get();';new='''   // ServerLevel chunks do not normally retain worldgen heightmaps; prime the fixture explicitly.
   for(int x=(p.getX()-12)>>4;x<=(p.getX()+12)>>4;x++)for(int z=(p.getZ()-12)>>4;z<=(p.getZ()+12)>>4;z++)net.minecraft.world.level.levelgen.Heightmap.primeHeightmaps(world.getChunk(x,z),java.util.EnumSet.of(net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG));
'''+old;assert old in s;s=s.replace(old,new);p.write_text(s,encoding='utf-8')
