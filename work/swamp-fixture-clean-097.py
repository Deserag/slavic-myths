from pathlib import Path
p=Path('tools/kurgan-tests/org/slavicmyths/swamp/SwampGameTests.java');s=p.read_text(encoding='utf-8');old='   for(int x=0;x<width;x++)for(int z=0;z<depth;z++){';new='''   world.getEntitiesOfClass(BolotnikEntity.class,new net.minecraft.world.phys.AABB(clip.minX(),clip.minY(),clip.minZ(),clip.maxX()+1,clip.maxY()+1,clip.maxZ()+1)).forEach(net.minecraft.world.entity.Entity::discard);
   for(int x=0;x<width;x++)for(int z=0;z<depth;z++)for(int y=-8;y<=clip.maxY()-origin.getY();y++)world.setBlock(origin.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);
'''+old;assert old in s;s=s.replace(old,new);p.write_text(s,encoding='utf-8')
