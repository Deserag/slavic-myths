package org.slavicmyths.swamp;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("slavicmyths") @PrefixGameTestTemplate(false)
public final class SwampGameTests {
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void wetStructures(GameTestHelper test)throws Exception{
  var world=test.getLevel();var manager=world.getStructureManager();int count=0,loot=0,supports=0;
  var report=com.google.gson.JsonParser.parseString(Files.readString(Path.of(System.getProperty("slavicmyths.portRoot"),"docs/swamp/templates-0.9.7.json"))).getAsJsonObject();
  for(var entry:report.entrySet())for(boolean wet:new boolean[]{true,false}){
   var origin=test.absolutePos(new BlockPos(2048+count*48,80,2048));var piece=new SwampPiece(manager,entry.getKey(),origin,0);var clip=piece.getBoundingBox();
   for(int x=clip.minX()>>4;x<=clip.maxX()>>4;x++)for(int z=clip.minZ()>>4;z<=clip.maxZ()>>4;z++)world.getChunk(x,z);
   int width=entry.getValue().getAsJsonObject().getAsJsonArray("size").get(0).getAsInt(),depth=entry.getValue().getAsJsonObject().getAsJsonArray("size").get(2).getAsInt();
   world.getEntitiesOfClass(BolotnikEntity.class,new net.minecraft.world.phys.AABB(clip.minX(),clip.minY(),clip.minZ(),clip.maxX()+1,clip.maxY()+1,clip.maxZ()+1)).forEach(net.minecraft.world.entity.Entity::discard);
   for(int x=0;x<width;x++)for(int z=0;z<depth;z++)for(int y=-8;y<=clip.maxY()-origin.getY();y++)world.setBlock(origin.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);
   for(int x=0;x<width;x++)for(int z=0;z<depth;z++){
    world.setBlock(origin.offset(x,wet?-4:-1,z),Blocks.CLAY.defaultBlockState(),2);
    if(wet)for(int y=-3;y<=0;y++)world.setBlock(origin.offset(x,y,z),Blocks.WATER.defaultBlockState(),2);
   }
   piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),RandomSource.create(0),clip,new ChunkPos(origin),origin);
   test.assertTrue(piece.getBoundingBox().equals(clip),"Template placement discarded expanded foundation bounds "+entry.getKey());
   Map<BlockPos,RandomizableContainerBlockEntity> containers=new HashMap<>();
   for(int x=clip.minX();x<=clip.maxX();x++)for(int z=clip.minZ();z<=clip.maxZ();z++)for(int y=origin.getY();y<=clip.maxY();y++){
    var p=new BlockPos(x,y,z);var state=world.getBlockState(p);
    if(state.is(Blocks.SPRUCE_LOG)&&y==origin.getY()){
     for(int d=1;d<=(wet?4:1);d++)test.assertTrue(world.getBlockState(p.below(d)).isSolid(),"Floating support "+entry.getKey()+" at "+p.below(d));supports++;
    }
    if(state.is(Blocks.LANTERN)||state.is(Blocks.SOUL_LANTERN))test.assertTrue(state.canSurvive(world,p),"Unsupported lantern "+p);
    if(world.getBlockEntity(p) instanceof RandomizableContainerBlockEntity c&&c.getPersistentData().getBoolean("SlavicSwampLoot")){
     test.assertTrue(!world.getBlockState(p.below()).isAir(),"Floating reward "+entry.getKey()+" "+p);
     test.assertTrue(c.getLootTable()!=null,"No loot table "+entry.getKey());c.unpackLootTable(null);boolean nonempty=false;for(int slot=0;slot<c.getContainerSize();slot++)if(!c.getItem(slot).isEmpty())nonempty=true;test.assertTrue(nonempty,"Empty important reward "+entry.getKey());
     c.setItem(0,new ItemStack(Items.DIAMOND,3));containers.put(p,c);loot++;
    }
   }
   test.assertTrue(containers.size()==entry.getValue().getAsJsonObject().get("lootPoints").getAsInt(),"Loot point count "+entry.getKey());
   long mobs=world.getEntitiesOfClass(BolotnikEntity.class,new net.minecraft.world.phys.AABB(clip.minX(),clip.minY(),clip.minZ(),clip.maxX()+1,clip.maxY()+1,clip.maxZ()+1)).size();
   if(entry.getKey().contains("cluster"))test.assertTrue(mobs>=1,"Rare shrine has no guarded encounter");
   piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),RandomSource.create(0),clip,new ChunkPos(origin),origin);
   for(var e:containers.entrySet())test.assertTrue(world.getBlockEntity(e.getKey())==e.getValue()&&e.getValue().getItem(0).getCount()==3,"Structure retry reset loot");
   long after=world.getEntitiesOfClass(BolotnikEntity.class,new net.minecraft.world.phys.AABB(clip.minX(),clip.minY(),clip.minZ(),clip.maxX()+1,clip.maxY()+1,clip.maxZ()+1)).size();test.assertTrue(mobs==after,"Repeated encounter spawn");count++;
  }
  System.out.println("SWAMP_RUNTIME_STRUCTURES_PASS templates="+count+" meaningfulLoot="+loot+" supportedPosts="+supports+" inventoryRetries="+count);test.succeed();
 }
 @GameTest(template="port_empty",timeoutTicks=100)
 public static void geometryAndMobs(GameTestHelper test){
  int shapes=0;for(String kind:List.of("willow","oak","pine","dead")){Set<Integer> hashes=new HashSet<>();for(int seed=0;seed<100;seed++){
   var tree=new SwampTreeShape(kind,seed);hashes.add(Objects.hash(tree.logs,tree.leaves,tree.hanging));test.assertTrue(tree.logs.size()+tree.leaves.size()+tree.hanging.size()<=2400,"Unbounded tree");test.assertTrue(tree.leaves.values().stream().allMatch(d->d>=1&&d<=6),"Disconnected foliage");test.assertTrue(!kind.equals("dead")||tree.leaves.isEmpty(),"Dead tree foliage");shapes++;}test.assertTrue(hashes.size()>50,"Repeated tree geometry "+kind);}
  var mob=org.slavicmyths.registry.ModEntities.BOLOTNIK.get().create(test.getLevel());test.assertTrue(mob!=null&&mob.getMaxHealth()==48,"Bolotnik attributes");test.assertTrue(mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)==7,"Bog combat stats");test.assertTrue(mob.getNavigation()!=null,"Bog path navigation missing");
  var data=new net.minecraft.nbt.CompoundTag();mob.addAdditionalSaveData(data);mob.readAdditionalSaveData(data);test.assertTrue(mob.getMaxHealth()==48,"Attributes persistence");System.out.println("SWAMP_RUNTIME_GEOMETRY_PASS shapes="+shapes+" bolotnikHP=48");test.succeed();
 }
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
   // ServerLevel chunks do not normally retain worldgen heightmaps; prime the fixture explicitly.
   for(int x=(p.getX()-12)>>4;x<=(p.getX()+12)>>4;x++)for(int z=(p.getZ()-12)>>4;z<=(p.getZ()+12)>>4;z++)net.minecraft.world.level.levelgen.Heightmap.primeHeightmaps(world.getChunk(x,z),java.util.EnumSet.of(net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG));
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

 private static net.minecraft.world.level.chunk.ChunkGenerator terrain(net.minecraft.server.level.ServerLevel world,net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> biome,int water,int stone){
  var settings=new net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings(Optional.empty(),world.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getHolderOrThrow(biome),List.of());
  settings.getLayersInfo().add(new net.minecraft.world.level.levelgen.flat.FlatLayerInfo(1,Blocks.BEDROCK));settings.getLayersInfo().add(new net.minecraft.world.level.levelgen.flat.FlatLayerInfo(stone,Blocks.STONE));settings.getLayersInfo().add(new net.minecraft.world.level.levelgen.flat.FlatLayerInfo(1,Blocks.CLAY));if(water>0)settings.getLayersInfo().add(new net.minecraft.world.level.levelgen.flat.FlatLayerInfo(water,Blocks.WATER));settings.updateLayers();return new net.minecraft.world.level.levelgen.FlatLevelSource(settings);
 }
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void acceptedWorldgenStarts(GameTestHelper test){
  var w=test.getLevel();var biomes=w.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);var swamp=biomes.getHolderOrThrow(net.minecraft.world.level.biome.Biomes.SWAMP).value();var features=w.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.PLACED_FEATURE);
  for(String name:List.of("willow","oak","pine","dead","detail")){var expected=features.getHolderOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.PLACED_FEATURE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","swamp_"+name)));test.assertTrue(swamp.getGenerationSettings().features().stream().anyMatch(set->set.contains(expected)),"Swamp injection missing "+name);var plains=biomes.getHolderOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS).value();test.assertTrue(plains.getGenerationSettings().features().stream().noneMatch(set->set.contains(expected)),"Swamp feature injected into plains");}
  test.assertTrue(swamp.getMobSettings().getMobs(net.minecraft.world.entity.MobCategory.MONSTER).unwrap().stream().anyMatch(entry->entry.type==org.slavicmyths.registry.ModEntities.BOLOTNIK.get()),"Bolotnik not injected into swamp spawn settings");
  System.out.println("SWAMP_RUNTIME_BIOME_INJECTION_PASS features=5 bolotnikSpawn=true plainsUntouched=true");
  int accepted=0,attempts=0;var g=terrain(w,net.minecraft.world.level.biome.Biomes.SWAMP,4,122);
  for(String id:List.of("swamp_hut","abandoned_settlement","bog_causeway","flooded_shrine","fishing_camp","swamp_remnants","swamp_watchtower")){
   var structure=SwampStructures.get(w,id);boolean found=false;
   for(int n=0;n<24;n++){attempts++;var chunk=new ChunkPos(2048+n*17,2048+n*23);var start=structure.generate(w.registryAccess(),g,g.getBiomeSource(),w.getChunkSource().randomState(),w.getStructureManager(),n,chunk,0,w,h->h.is(net.minecraft.world.level.biome.Biomes.SWAMP));if(start.isValid()){test.assertTrue(start.getPieces().stream().filter(p->p instanceof SwampPiece).count()==1&&start.getPieces().size()<=2,"Partial or unexpected authored start / terrain foundation");found=true;accepted++;break;}}
   test.assertTrue(found,"No accepted actual worldgen start "+id);
  }
  var structure=SwampStructures.get(w,"fishing_camp");
  for(var bad:List.of(terrain(w,net.minecraft.world.level.biome.Biomes.SWAMP,12,122),terrain(w,net.minecraft.world.level.biome.Biomes.PLAINS,4,122))){
   var start=structure.generate(w.registryAccess(),bad,bad.getBiomeSource(),w.getChunkSource().randomState(),w.getStructureManager(),0,new ChunkPos(4096,4096),0,w,h->h.is(net.minecraft.world.level.biome.Biomes.SWAMP));test.assertTrue(!start.isValid(),"Invalid deep/outside-biome terrain accepted");
  }
  int thinAccepted=0;var thin=terrain(w,net.minecraft.world.level.biome.Biomes.SWAMP,0,2);
  for(String id:List.of("swamp_hut","abandoned_settlement","bog_causeway","flooded_shrine","fishing_camp","swamp_remnants","swamp_watchtower")){
   var site=SwampStructures.get(w,id).generate(w.registryAccess(),thin,thin.getBiomeSource(),w.getChunkSource().randomState(),w.getStructureManager(),17,new ChunkPos(8192,8192),0,w,h->h.is(net.minecraft.world.level.biome.Biomes.SWAMP));
   test.assertTrue(site.isValid(),"Clear thin swamp superflat rejected "+id);thinAccepted++;
  }
  System.out.println("SWAMP_RUNTIME_THIN_SUPERFLAT_PASS accepted="+thinAccepted);
  System.out.println("SWAMP_RUNTIME_WORLDGEN_PASS types="+accepted+" attempts="+attempts+" deepWaterOutsideBiomeRejected=true");test.succeed();
 }

}
