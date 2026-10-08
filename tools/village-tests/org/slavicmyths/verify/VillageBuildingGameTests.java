package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.village.*;

@GameTestHolder("slavicmyths_buildings") @PrefixGameTestTemplate(false)
public final class VillageBuildingGameTests {
 @GameTest(template="empty",timeoutTicks=100)
 public static void sackMenuAndMigration(GameTestHelper t){
  var l=t.getLevel();var pos=t.absolutePos(new BlockPos(3,2,3));var block=org.slavicmyths.storage.Household.BLOCKS.get(org.slavicmyths.storage.Household.Kind.SACK).get();l.setBlock(pos,block.defaultBlockState(),3);var tile=(org.slavicmyths.storage.StorageTile)l.getBlockEntity(pos);
  t.assertTrue(tile.getContainerSize()==5&&tile.getMaxStackSize()==64,"Sack is not five stacks");tile.setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WHEAT_SEEDS,63));t.assertTrue(l.getBlockState(pos).getValue(org.slavicmyths.storage.StorageBlock.FILL)==0,"Partial stack raised fill");tile.setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WHEAT_SEEDS,64));t.assertTrue(l.getBlockState(pos).getValue(org.slavicmyths.storage.StorageBlock.FILL)==1,"Full stack did not raise fill");
  var player=t.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);player.moveTo(pos.getX(),pos.getY(),pos.getZ(),0,0);var menu=new org.slavicmyths.storage.SackMenu(1,player.getInventory(),pos,tile);t.assertTrue(menu.slots.size()==41&&!menu.getSlot(0).mayPlace(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD)),"Sack accepts tools / wrong slot count");
  player.getInventory().setItem(9,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CARROT,64));menu.quickMoveStack(player,5);t.assertTrue(tile.getItem(1).is(net.minecraft.world.item.Items.CARROT)&&tile.getItem(1).getCount()==64&&player.getInventory().getItem(9).isEmpty(),"Whole stack quick move failed");
  var saved=tile.saveWithoutMetadata(l.registryAccess());var loaded=new org.slavicmyths.storage.StorageTile(pos,l.getBlockState(pos));loaded.loadWithComponents(saved,l.registryAccess());t.assertTrue(loaded.getItem(0).getCount()==64&&loaded.getItem(1).getCount()==64,"Sack saved contents lost");for(int i=0;i<5;i++)tile.setItem(i,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WHEAT,64));t.assertTrue(l.getBlockState(pos).getValue(org.slavicmyths.storage.StorageBlock.FILL)==5,"Full sack not level five");var legacy=net.minecraft.core.NonNullList.withSize(4,net.minecraft.world.item.ItemStack.EMPTY);for(int j=0;j<4;j++)legacy.set(j,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WHEAT_SEEDS,99));var oldTag=new CompoundTag();net.minecraft.world.ContainerHelper.saveAllItems(oldTag,legacy,l.registryAccess());var migrated=new org.slavicmyths.storage.StorageTile(pos,l.getBlockState(pos));migrated.loadWithComponents(oldTag,l.registryAccess());int stored=0;for(int j=0;j<5;j++)stored+=migrated.getItem(j).getCount();int spill=0;for(var raw:migrated.saveWithoutMetadata(l.registryAccess()).getList("SackOverflow",10))spill+=net.minecraft.world.item.ItemStack.parseOptional(l.registryAccess(),(CompoundTag)raw).getCount();t.assertTrue(stored==320&&spill==76&&stored+spill==396,"Legacy sack migration deleted items");var stableTag=new CompoundTag();var stable=net.minecraft.core.NonNullList.withSize(5,net.minecraft.world.item.ItemStack.EMPTY);stable.set(3,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WHEAT,12));net.minecraft.world.ContainerHelper.saveAllItems(stableTag,stable,l.registryAccess());migrated.loadWithComponents(stableTag,l.registryAccess());t.assertTrue(migrated.getItem(3).getCount()==12&&migrated.getItem(0).isEmpty(),"Sack reload moved GUI slots");System.out.println("SACK_MENU_PASS slots=5 stackLimit=64 partialLevel=0 fullLevel=5 mixedProduce=true quickMove=64 saveRoundtrip=true legacy396Retained=true stableSlots=true toolsRejected=true");t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=100)
 public static void footingMaterial(GameTestHelper t){var l=t.getLevel();var base=t.absolutePos(new BlockPos(3,3,3));for(int i=1;i<=5;i++)l.setBlock(base.below(i),Blocks.AIR.defaultBlockState(),2);l.setBlock(base.below(5),Blocks.STONE.defaultBlockState(),2);var cobble=new StructureTemplate.StructureBlockInfo(base,Blocks.COBBLESTONE.defaultBlockState(),null);var log=new StructureTemplate.StructureBlockInfo(base.above(2),Blocks.OAK_LOG.defaultBlockState(),null);var processed=org.slavicmyths.village.VillageFoundation.INSTANCE.finalizeProcessing(l,base,base,List.of(log,cobble),List.of(log,cobble),new StructurePlaceSettings());for(int i=1;i<=4;i++){final var pos=base.below(i);t.assertTrue(processed.stream().anyMatch(info->info.pos().equals(pos)&&info.state().is(Blocks.COBBLESTONE)),"Floating footing / wrong support material "+pos);}t.assertTrue(processed.stream().noneMatch(info->info.pos().equals(base)&&info.state().is(Blocks.OAK_LOG)),"Log overwrote authored cobble base");System.out.println("FOOTING_PASS fourAirBlocksFilled=true sameBaseMaterial=true authoredBlocksRetained=true");t.succeed();}
 @BeforeBatch(batch="building_showcase")
 public static void layeredSuperflat(net.minecraft.server.level.ServerLevel level){
  var generator=(net.minecraft.world.level.levelgen.FlatLevelSource)level.getChunkSource().getGenerator();var settings=generator.settings();
  settings.getLayersInfo().clear();settings.getLayersInfo().add(new net.minecraft.world.level.levelgen.flat.FlatLayerInfo(1,Blocks.BEDROCK));settings.getLayersInfo().add(new net.minecraft.world.level.levelgen.flat.FlatLayerInfo(6,Blocks.DIRT));settings.getLayersInfo().add(new net.minecraft.world.level.levelgen.flat.FlatLayerInfo(1,Blocks.GRASS_BLOCK));settings.updateLayers();
  level.setDayTime(2000);level.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DAYLIGHT).set(false,level.getServer());
 }

 @GameTest(template="empty",timeoutTicks=100)
 public static void resourcesAndPlanning(GameTestHelper t)throws Exception{
  var l=t.getLevel();int states=0,plans=0,barracks=0;
  var manager=l.getServer().getResourceManager();
  for(var e:manager.listResources("structure/village",r->r.getNamespace().equals("slavicmyths")&&r.getPath().endsWith(".nbt")).entrySet()){
   CompoundTag data;try(var in=e.getValue().open()){data=NbtIo.readCompressed(in,NbtAccounter.unlimitedHeap());}
   for(var raw:data.getList("palette",10)){
    var state=(CompoundTag)raw;var id=ResourceLocation.parse(state.getString("Name"));t.assertTrue(BuiltInRegistries.BLOCK.containsKey(id),"Unknown block "+id+" in "+e.getKey());
    var block=BuiltInRegistries.BLOCK.get(id);var props=state.getCompound("Properties");
    for(var name:props.getAllKeys()){var property=block.getStateDefinition().getProperty(name);t.assertTrue(property!=null&&property.getValue(props.getString(name)).isPresent(),"Invalid property "+id+" "+name+"="+props.getString(name)+" in "+e.getKey());}
    states++;
   }
   for(var raw:data.getList("blocks",10)){var tag=((CompoundTag)raw).getCompound("nbt");if(tag.contains("LootTable")){var key=ResourceLocation.parse(tag.getString("LootTable"));t.assertTrue(l.getServer().reloadableRegistries().getLootTable(net.minecraft.resources.ResourceKey.create(Registries.LOOT_TABLE,key))!=net.minecraft.world.level.storage.loot.LootTable.EMPTY,"Missing loot "+key);}}
  }
  for(String family:List.of("plains","taiga","snowy","savanna","desert")){
   var structure=l.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.withDefaultNamespace("village_"+family));var generator=l.getChunkSource().getGenerator();int valid=0,buildings=0;
   for(int i=0;i<8;i++){
    var start=structure.generate(l.registryAccess(),generator,generator.getBiomeSource(),l.getChunkSource().randomState(),l.getStructureManager(),l.getSeed(),new net.minecraft.world.level.ChunkPos(2000+i*40,3000+i*31),0,l,b->true);
    if(!start.isValid())continue;valid++;plans++;
    var pieces=start.getPieces().stream().filter(p->p instanceof PoolElementStructurePiece).map(p->(PoolElementStructurePiece)p).toList();
    t.assertTrue(pieces.getFirst().getElement().toString().contains("slavicmyths:village/"),"Unreplaced center "+family);
    int houses=(int)pieces.stream().filter(p->p.getElement().toString().contains("/residential/")||p.getElement().toString().contains("/profession/")||p.getElement().toString().contains("/utility/")).count();buildings+=houses;System.out.println("NATIVE_PLAN family="+family+" pieces="+pieces.size()+" houses="+houses);
    long n=pieces.stream().filter(p->p.getElement().toString().contains("druzhinnik_barracks")||p.getElement().toString().contains("military/barracks")).count();t.assertTrue(n<=1,"Duplicate barracks "+family);barracks+=n;
    t.assertTrue(pieces.stream().noneMatch(p->p.getElement().toString().matches(".*minecraft:village/[^/]+/(zombie/)?houses/.*")),"Vanilla house selected "+family);
   }
   t.assertTrue(valid>0&&buildings>0,"No valid houses in native plans "+family+" plans="+valid+" houses="+buildings);
  }
  System.out.println("BUILDINGS_RESOURCES_PASS rawStates="+states+" nativePlans="+plans+" barracks="+barracks+" fiveFamilies=true");t.succeed();
 }
 @GameTest(template="empty",batch="building_showcase",timeoutTicks=800)
 public static void nativeClaimAndEntry(GameTestHelper t)throws Exception{
  var l=t.getLevel();int x=202000,z=200000,ground=-57;Set<net.minecraft.world.level.ChunkPos>held=new HashSet<>();
  for(int cx=(x-4)>>4;cx<=(x+176)>>4;cx++)for(int cz=(z-4)>>4;cz<=(z+176)>>4;cz++){held.add(new net.minecraft.world.level.ChunkPos(cx,cz));l.setChunkForced(cx,cz,true);l.getChunk(cx,cz);}
  var source=l.getServer().createCommandSourceStack().withLevel(l).withPosition(new net.minecraft.world.phys.Vec3(x-8,ground+1,z-8));t.assertTrue(VillageShowcase.showcase(source,"temperate")==34,"Claim showcase failed");
  record Case(Villager v,VillageShowcase.Entry e,BlockPos job){}List<Case>cases=new ArrayList<>();int i=0;
  for(var e:VillageShowcase.catalog(l)){
   if(!e.palette().equals("temperate"))continue;int slot=VillageShowcase.layoutSlot(i++);var origin=new BlockPos(x+(slot%6)*28,ground-e.surface(),z+(slot/6)*28);
   if(e.job()==null)continue;
   var area=new net.minecraft.world.phys.AABB(origin.getCenter(),origin.offset(22,26,22).getCenter());String key=e.job().contains(":")?e.job():"minecraft:"+e.job();
   var v=l.getEntitiesOfClass(Villager.class,area,a->BuiltInRegistries.VILLAGER_PROFESSION.getKey(a.getVillagerData().getProfession()).toString().equals(key)).stream().findFirst().orElseThrow();
   var p=origin.offset(e.entrance()).above().north(2);v.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,180,0);v.setVillagerXp(1);v.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE);v.refreshBrain(l);cases.add(new Case(v,e,origin.offset(e.workstation())));
  }
  Set<java.util.UUID> visited=new HashSet<>();
  t.onEachTick(()->{for(var c:cases){var memory=c.v().getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE);if(memory.isPresent()&&c.v().distanceToSqr(memory.get().pos().getCenter())<=16)visited.add(c.v().getUUID());}});
  t.runAfterDelay(600,()->{
   List<String>failures=new ArrayList<>();int claimed=0,reached=0;
   try{
    for(var c:cases){var job=c.v().getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE);
     boolean owns=job.isPresent()&&job.get().dimension().equals(l.dimension())&&l.getBlockState(job.get().pos()).getBlock()==l.getBlockState(c.job()).getBlock();if(owns)claimed++;else failures.add(c.e().id()+" claim="+job);
     if(owns&&visited.contains(c.v().getUUID()))reached++;else failures.add(c.e().id()+" entry/arrival position="+c.v().position()+" activity="+c.v().getBrain().getActiveNonCoreActivity()+" walk="+c.v().getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET)+" failure="+VillageWork.state(c.v()).failure);
    }
    t.assertTrue(failures.isEmpty(),"Native claims/entry: "+failures);System.out.println("BUILDINGS_CLAIM_ENTRY_PASS workers="+cases.size()+" nativeClaimed="+claimed+" actualArrival="+reached+" startOutside=true noJobMemoryShortcut=true");t.succeed();
   }finally{for(var chunk:held)l.setChunkForced(chunk.x,chunk.z,false);}
  });
 }
 @GameTest(template="empty",batch="bounds_abort",timeoutTicks=100)
 public static void prevalidationAbort(GameTestHelper t){
  var l=t.getLevel();var p=new BlockPos(3008,l.getMaxBuildHeight()-2,3008);l.setBlock(p,Blocks.GRASS_BLOCK.defaultBlockState(),2);
  var witness=p.offset(2,0,2);l.setBlock(witness,Blocks.GOLD_BLOCK.defaultBlockState(),2);
  var source=l.getServer().createCommandSourceStack().withLevel(l).withPosition(new net.minecraft.world.phys.Vec3(3000,0,3000));
  t.assertTrue(VillageShowcase.showcase(source,"temperate")==0,"Invalid height accepted");t.assertTrue(l.getBlockState(witness).is(Blocks.GOLD_BLOCK),"Prevalidation changed the world");System.out.println("BUILDINGS_ABORT_PASS worldHeightBounds=true noPlacementBeforeValidation=true");t.succeed();
 }
 @GameTest(template="empty",batch="building_showcase",timeoutTicks=300)
 public static void negativeShowcaseAndPaths(GameTestHelper t)throws Exception{
  var l=t.getLevel();t.assertTrue(l.getChunkSource().getGenerator() instanceof net.minecraft.world.level.levelgen.FlatLevelSource,"Not a real superflat generator");var entries=VillageShowcase.catalog(l);List<Runnable>checks=new ArrayList<>();Set<net.minecraft.world.level.ChunkPos> held=new HashSet<>();int x=200000,z=200000,ground=-57;
  for(String palette:List.of("temperate","cold","warm")){
   for(int cx=(x-4)>>4;cx<=(x+176)>>4;cx++)for(int cz=(z-4)>>4;cz<=(z+176)>>4;cz++){held.add(new net.minecraft.world.level.ChunkPos(cx,cz));l.setChunkForced(cx,cz,true);l.getChunk(cx,cz);}
   t.assertTrue(l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z)-1==ground,"Generated superflat surface is not -57");t.assertTrue(l.getBlockState(new BlockPos(x,-64,z)).is(Blocks.BEDROCK),"Missing real generated bedrock layer");
   var source=l.getServer().createCommandSourceStack().withLevel(l).withPosition(new net.minecraft.world.phys.Vec3(x-8,ground+1,z-8));
   int n=l.getServer().getCommands().getDispatcher().execute("sm test village_showcase"+(palette.equals("temperate")?"":" "+palette),source);t.assertTrue(n==34,"Showcase failed "+palette+": "+n);
   int i=0;
   for(var e:entries){if(!e.palette().equals(palette))continue;int slot=VillageShowcase.layoutSlot(i++);var origin=new BlockPos(x+(slot%6)*28,ground-e.surface(),z+(slot/6)*28);
    for(int bx=0;bx<22;bx++)for(int bz=0;bz<22;bz++)t.assertTrue(l.getBlockState(new BlockPos(origin.getX()+bx,-64,origin.getZ()+bz)).is(Blocks.BEDROCK),"Showcase damaged bedrock "+e.id());
    var v=EntityType.VILLAGER.create(l);var outside=origin.offset(e.entrance()).above().north(2);v.moveTo(outside.getX()+.5,outside.getY(),outside.getZ()+.5,0,0);v.setNoAi(true);((net.minecraft.world.entity.ai.navigation.GroundPathNavigation)v.getNavigation()).setCanOpenDoors(true);l.addFreshEntity(v);
    final var raw=l.getStructureManager().get(e.id()).orElseThrow();CompoundTag data=raw.save(new CompoundTag());List<BlockPos>beds=new ArrayList<>();
    for(var row:data.getList("blocks",10)){var block=(CompoundTag)row;var position=block.getList("pos",3);var p=origin.offset(position.getInt(0),position.getInt(1),position.getInt(2));var state=l.getBlockState(p);
     var paletteTag=(CompoundTag)data.getList("palette",10).get(block.getInt("state"));
     if(paletteTag.getString("Name").endsWith("_bed")&&paletteTag.getCompound("Properties").getString("part").equals("head")){t.assertTrue(state.getBlock() instanceof BedBlock,"Bed lost after placement "+e.id()+" "+p);beds.add(p);}
    }
    checks.add(()->{
     v.setOnGround(true);
     for(var bed:beds){var path=v.getNavigation().createPath(bed,1);t.assertTrue(path!=null&&path.canReach(),"Bed path blocked "+e.id()+" target="+bed+" start="+v.position()+" path="+path+" end="+(path==null?null:path.getEndNode()));}
     if(e.workstation()!=null){var job=origin.offset(e.workstation());var path=v.getNavigation().createPath(job,1);t.assertTrue(path!=null&&path.canReach(),"Work path blocked "+e.id()+" job="+job+" start="+v.position());t.assertTrue(net.minecraft.world.entity.ai.village.poi.PoiTypes.forState(l.getBlockState(job)).isPresent(),"Missing job POI "+e.id());}
     v.discard();
    });
   }
   x+=224;
  }
  var singleSource=l.getServer().createCommandSourceStack().withLevel(l).withPosition(new net.minecraft.world.phys.Vec3(204000,-56,200000));t.assertTrue(l.getServer().getCommands().getDispatcher().execute("sm place village slavicmyths:village/temperate/residential/seni_01",singleSource)==1,"Individual template command failed");
  t.runAfterDelay(20,()->{List<String> failures=new ArrayList<>();try{for(var check:checks){try{check.run();}catch(Exception ex){failures.add(ex.getMessage());System.out.println("BUILDING_PATH_FAILURE "+ex.getMessage());}}}finally{for(var chunk:held)l.setChunkForced(chunk.x,chunk.z,false);}t.assertTrue(failures.isEmpty(),"Blocked paths: "+failures);System.out.println("BUILDINGS_SHOWCASE_PASS palettes=3 placed=102 surface=-57 nativePathChecks="+checks.size()+" directCommand=true");t.succeed();});
 }
}


