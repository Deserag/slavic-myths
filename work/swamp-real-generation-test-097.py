from pathlib import Path
p=Path('tools/kurgan-tests/org/slavicmyths/swamp/SwampGameTests.java');s=p.read_text(encoding='utf-8');a=s.rfind('\n}');s=s[:a]+'''
 private static net.minecraft.world.level.chunk.ChunkGenerator terrain(net.minecraft.server.level.ServerLevel world,net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> biome,int water,int stone){
  var settings=new net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings(Optional.empty(),world.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getHolderOrThrow(biome),List.of());
  settings.getLayersInfo().add(new net.minecraft.world.level.levelgen.flat.FlatLayerInfo(1,Blocks.BEDROCK));settings.getLayersInfo().add(new net.minecraft.world.level.levelgen.flat.FlatLayerInfo(stone,Blocks.STONE));settings.getLayersInfo().add(new net.minecraft.world.level.levelgen.flat.FlatLayerInfo(1,Blocks.CLAY));if(water>0)settings.getLayersInfo().add(new net.minecraft.world.level.levelgen.flat.FlatLayerInfo(water,Blocks.WATER));settings.updateLayers();return new net.minecraft.world.level.levelgen.FlatLevelSource(settings);
 }
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void acceptedWorldgenStarts(GameTestHelper test){
  var w=test.getLevel();int accepted=0,attempts=0;var g=terrain(w,net.minecraft.world.level.biome.Biomes.SWAMP,4,122);
  for(String id:List.of("swamp_hut","abandoned_settlement","bog_causeway","flooded_shrine","fishing_camp","swamp_remnants","swamp_watchtower")){
   var structure=SwampStructures.get(w,id);boolean found=false;
   for(int n=0;n<24;n++){attempts++;var chunk=new ChunkPos(2048+n*17,2048+n*23);var start=structure.generate(w.registryAccess(),g,g.getBiomeSource(),w.getChunkSource().randomState(),w.getStructureManager(),n,chunk,0,w,h->h.is(net.minecraft.world.level.biome.Biomes.SWAMP));if(start.isValid()){test.assertTrue(start.getPieces().size()==1,"Partial or unexpected new start");found=true;accepted++;break;}}
   test.assertTrue(found,"No accepted actual worldgen start "+id);
  }
  var structure=SwampStructures.get(w,"fishing_camp");
  for(var bad:List.of(terrain(w,net.minecraft.world.level.biome.Biomes.SWAMP,12,122),terrain(w,net.minecraft.world.level.biome.Biomes.SWAMP,0,2),terrain(w,net.minecraft.world.level.biome.Biomes.PLAINS,4,122))){
   var start=structure.generate(w.registryAccess(),bad,bad.getBiomeSource(),w.getChunkSource().randomState(),w.getStructureManager(),0,new ChunkPos(4096,4096),0,w,h->h.is(net.minecraft.world.level.biome.Biomes.SWAMP));test.assertTrue(!start.isValid(),"Invalid deep/thin/outside-biome terrain accepted");
  }
  System.out.println("SWAMP_RUNTIME_WORLDGEN_PASS types="+accepted+" attempts="+attempts+" deepWaterThinFlatOutsideBiomeRejected=true");test.succeed();
 }
'''+s[a:];p.write_text(s,encoding='utf-8')
