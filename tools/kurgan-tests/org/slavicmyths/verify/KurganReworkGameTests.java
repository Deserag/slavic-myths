package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.kurgan.*;

@GameTestHolder("slavicmyths")
@PrefixGameTestTemplate(false)
public final class KurganReworkGameTests {
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void placementAndInventory(GameTestHelper test){
  var world=test.getLevel();int totalContainers=0,totalPaths=0;
  for(int tier=0;tier<3;tier++){
   var plan=KurganPlan.create(tier,0);var origin=test.absolutePos(new BlockPos(512+tier*256,80,512));UUID id=UUID.randomUUID();
   var piece=new KurganDungeonPiece(plan,origin,id);var box=piece.getBoundingBox();
   // Dedicated disposable GameTest world; load only this accepted plan's bounded footprint.
   for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)world.getChunk(x,z);
   BurialRecords.get(world).register(new KurganInstance(id,origin,plan));
   piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(0),box,new ChunkPos(origin),origin);
   for(var r:plan.rooms)if(r.roomNode){BlockPos lamp=origin.offset(r.x-r.rx+2,r.y+r.height,r.z);test.assertTrue(world.getBlockState(lamp).is(Blocks.LANTERN)&&world.getBlockState(lamp).canSurvive(world,lamp),"Warm fixture has no ceiling support");}
   Map<BlockPos,RandomizableContainerBlockEntity> containers=new LinkedHashMap<>();
   for(var r:plan.rooms){var b=r.box();for(int x=b.x0;x<=b.x1;x++)for(int z=b.z0;z<=b.z1;z++){
    BlockPos p=origin.offset(x,r.y+1,z);if(world.getBlockEntity(p) instanceof RandomizableContainerBlockEntity c&&c.getPersistentData().hasUUID("SlavicKurganContainer")){
     containers.put(p,c);test.assertTrue(world.getBlockState(p.above()).isAir(),"Blocked container clearance "+p);test.assertTrue(!world.getBlockState(p.below()).isAir(),"Floating container "+p);c.setLootTable(null,0);c.setItem(0,new ItemStack(Items.DIAMOND,3));
     if(c instanceof BurialCoffinTile){var state=world.getBlockState(p);BlockPos head=p.relative(state.getValue(BurialCoffinBlock.FACING));test.assertTrue(world.getBlockState(head).is(org.slavicmyths.registry.ModBlocks.BURIAL_COFFIN.get()),"Missing coffin head");test.assertTrue(world.getBlockState(head.above()).isAir(),"Blocked coffin lid");}
    }
   }}
   test.assertTrue(containers.size()==KurganLayout.metrics(plan).containers(),"Reward point placement drift tier="+tier+" count="+containers.size());
   for(var link:plan.links)if(!link.secret)for(var step:link.steps){BlockPos p=origin.offset(step.x,step.y+1,step.z);if(plan.seal!=null&&plan.seal.contains(step.x,step.y+1,step.z))continue;
    test.assertTrue(world.getBlockState(p).getCollisionShape(world,p).isEmpty(),"Blocked corridor feet tier="+tier+" at "+p);test.assertTrue(world.getBlockState(p.above()).getCollisionShape(world,p.above()).isEmpty(),"Blocked corridor head at "+p);totalPaths++;
   }
   piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(0),box,new ChunkPos(origin),origin);
   for(var e:containers.entrySet()){test.assertTrue(world.getBlockEntity(e.getKey())==e.getValue(),"Repeated placement replaced container");test.assertTrue(e.getValue().getItem(0).is(Items.DIAMOND)&&e.getValue().getItem(0).getCount()==3,"Repeated placement erased inventory");}
   if(plan.seal!=null){var r=plan.rooms.get(plan.finalRoom);test.assertTrue(world.getBlockState(origin.offset(r.x,r.y-1,r.z)).is(KurganBlocks.get("sealed_kurgan_masonry")),"Unprotected tomb floor");test.assertTrue(world.getBlockState(origin.offset(r.x,r.y+r.height+2,r.z)).is(KurganBlocks.get("sealed_kurgan_masonry")),"Unprotected tomb roof");test.assertTrue(world.getBlockState(origin.offset(r.x,r.y+6,r.z-r.rz-1)).is(KurganBlocks.get("sealed_kurgan_masonry")),"Mineable bypass above seal");}
   totalContainers+=containers.size();
  }
  System.out.println("KURGAN_RUNTIME_PLACEMENT_PASS tiers=3 containers="+totalContainers+" pathSteps="+totalPaths+" inventoryRetries=3");test.succeed();
 }
 @GameTest(template="port_empty",timeoutTicks=100)
 public static void finiteTrap(GameTestHelper test){
  KurganPlan selected=null;KurganPlan.Room trap=null;for(long seed=0;seed<16&&trap==null;seed++){var p=KurganPlan.create(2,seed);for(var r:p.rooms)if(r.archetype==KurganPlan.Archetype.TRAP){selected=p;trap=r;break;}}
  test.assertTrue(trap!=null,"No trap sample");var plan=selected;var room=trap;var world=test.getLevel();var origin=test.absolutePos(new BlockPos(2304,80,2304));var b=room.box();var clip=new BoundingBox(origin.getX()+b.x0,origin.getY()+b.y0,origin.getZ()+b.z0,origin.getX()+b.x1,origin.getY()+b.y1,origin.getZ()+b.z1);var piece=new KurganDungeonPiece(plan,origin,UUID.randomUUID());
  for(int x=clip.minX()>>4;x<=clip.maxX()>>4;x++)for(int z=clip.minZ()>>4;z<=clip.maxZ()>>4;z++){world.setChunkForced(x,z,true);world.getChunk(x,z);}
  piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(0),clip,new ChunkPos(origin),origin);var p=origin.offset(room.x,room.y,room.z);
  test.assertTrue(world.getBlockEntity(p) instanceof DispenserBlockEntity,"Trap dispenser missing");var dispenser=(DispenserBlockEntity)world.getBlockEntity(p);test.assertTrue(dispenser.getItem(0).getCount()==8,"Initial ammo drift");
  world.setBlock(p.above(),Blocks.STONE_PRESSURE_PLATE.defaultBlockState().setValue(PressurePlateBlock.POWERED,true),3);world.neighborChanged(p,Blocks.STONE_PRESSURE_PLATE,p.above());
  test.runAfterDelay(12,()->{try{test.assertTrue(dispenser.getItem(0).getCount()==7,"Trap ammo after trigger "+dispenser.getItem(0).getCount());piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(0),clip,new ChunkPos(origin),origin);test.assertTrue(world.getBlockEntity(p)==dispenser&&dispenser.getItem(0).getCount()==7,"Trap retry replenishes ammo");System.out.println("KURGAN_RUNTIME_TRAP_PASS initialAmmo=8 afterShot=7 retriesDoNotRefill=true");test.succeed();}finally{for(int x=clip.minX()>>4;x<=clip.maxX()>>4;x++)for(int z=clip.minZ()>>4;z<=clip.maxZ()>>4;z++)world.setChunkForced(x,z,false);}});
 }
 @GameTest(template="port_empty",timeoutTicks=100)
 public static void variantsAndLoot(GameTestHelper test){
  int shapes=0;for(var e:KurganBlocks.BLOCKS.entrySet())if(e.getKey().endsWith("_stairs")||e.getKey().endsWith("_wall")||e.getKey().endsWith("_slab")&&e.getValue().get() instanceof SlabBlock){var block=e.getValue().get();test.assertTrue(block instanceof StairBlock||block instanceof SlabBlock||block instanceof WallBlock,"Decor masquerades as construction shape");test.assertTrue(BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath("slavicmyths",e.getKey())),"No block item");shapes++;}
  test.assertTrue(shapes==25,"Construction variants count "+shapes);
  long items=BuiltInRegistries.ITEM.keySet().stream().filter(i->i.getNamespace().equals("slavicmyths")).count(),blocks=BuiltInRegistries.BLOCK.keySet().stream().filter(i->i.getNamespace().equals("slavicmyths")).count();test.assertTrue(items==412&&blocks==179,"Registry totals drift items="+items+" blocks="+blocks);System.out.println("KURGAN_RUNTIME_REGISTRY_TOTALS items="+items+" blocks="+blocks);
  for(String name:List.of("common_cache","burial","warrior","ritual","treasury","secret","great_special"))test.assertTrue(test.getLevel().getServer().reloadableRegistries().getKeys(Registries.LOOT_TABLE).contains(ResourceLocation.fromNamespaceAndPath("slavicmyths","kurgan/"+name)),"Missing loot table "+name);
  System.out.println("KURGAN_RUNTIME_VARIANTS_PASS shapes=25 lootTables=7");test.succeed();
 }
}
