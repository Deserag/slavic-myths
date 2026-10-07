from pathlib import Path
p=Path('tools/kurgan-tests/org/slavicmyths/swamp/SwampGameTests.java');s=p.read_text(encoding='utf-8');index=s.rfind('\n}');s=s[:index]+'''
 private static void biome(net.minecraft.server.level.ServerLevel world,BlockPos center,net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> key){
  var registry=world.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);var holder=registry.getHolderOrThrow(key);
  for(int x=(center.getX()-12)>>4;x<=(center.getX()+12)>>4;x++)for(int z=(center.getZ()-12)>>4;z<=(center.getZ()+12)>>4;z++){
   var chunk=world.getChunk(x,z);var sections=chunk.getSections();
   for(int n=0;n<sections.length;n++)sections[n]=new net.minecraft.world.level.chunk.LevelChunkSection(sections[n].getStates(),new net.minecraft.world.level.chunk.PalettedContainer<>(registry.asHolderIdMap(),holder,net.minecraft.world.level.chunk.PalettedContainer.Strategy.SECTION_BIOMES));chunk.setUnsaved(true);
  }
 }
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void actualTreesAndSwampStats(GameTestHelper test){
  var world=test.getLevel();int placed=0;
  for(String kind:List.of("willow","oak","pine","dead"))for(boolean wet:new boolean[]{false,true}){
   var p=test.absolutePos(new BlockPos(8192+placed*64,80,8192));biome(world,p,net.minecraft.world.level.biome.Biomes.SWAMP);
   for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++){
    world.setBlock(p.offset(x,-1,z),Blocks.CLAY.defaultBlockState(),2);for(int y=0;y<25;y++)world.setBlock(p.offset(x,y,z),wet&&y==0?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
   }
   var feature=org.slavicmyths.registry.ModFeatures.SWAMP_NATURE.get();var context=new net.minecraft.world.level.levelgen.feature.FeaturePlaceContext<>(Optional.empty(),world,world.getChunkSource().getGenerator(),RandomSource.create(17),p,new SwampNatureFeature.Config(kind));
   boolean result=feature.place(context);test.assertTrue(result!= (wet&&kind.equals("pine")),"Actual tree preflight "+kind+" wet="+wet);
   if(result){test.assertTrue(world.getBlockState(p).is(net.minecraft.tags.BlockTags.LOGS),"Missing tree trunk "+kind);placed++;}
   // Outside vanilla swamp the feature must reject without placing any new tree.
   biome(world,p,net.minecraft.world.level.biome.Biomes.PLAINS);test.assertTrue(!feature.place(context),"Feature leaked outside vanilla swamp");
  }
  var p=test.absolutePos(new BlockPos(9216,80,8192));biome(world,p,net.minecraft.world.level.biome.Biomes.SWAMP);
  var r=org.slavicmyths.registry.ModEntities.RUSALKA.get().create(world);var v=org.slavicmyths.registry.ModEntities.VODYANOY.get().create(world);
  for(var spirit:List.of(r,v)){spirit.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,0,0);spirit.finalizeSpawn(world,world.getCurrentDifficultyAt(p),net.minecraft.world.entity.MobSpawnType.STRUCTURE,null);}
  test.assertTrue(r.getMaxHealth()==36&&v.getMaxHealth()==72,"Swamp HP not applied");biome(world,p,net.minecraft.world.level.biome.Biomes.PLAINS);
  var outside=org.slavicmyths.registry.ModEntities.RUSALKA.get().create(world);outside.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,0,0);outside.finalizeSpawn(world,world.getCurrentDifficultyAt(p),net.minecraft.world.entity.MobSpawnType.STRUCTURE,null);test.assertTrue(outside.getMaxHealth()==28,"Outside swamp balance changed");
  System.out.println("SWAMP_RUNTIME_NATURE_PASS placed="+placed+" outsideBiomeRejected=true pineWaterRejected=true swampHP=36,72");test.succeed();
 }
'''+s[index:];p.write_text(s,encoding='utf-8')
