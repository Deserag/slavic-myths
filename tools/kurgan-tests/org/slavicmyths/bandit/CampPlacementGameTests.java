package org.slavicmyths.bandit;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.item.*;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("slavicmyths") @PrefixGameTestTemplate(false)
public final class CampPlacementGameTests {
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void chunkBoundsAndLootRetries(GameTestHelper test){
  var world=test.getLevel();var campRecords=CampRecords.get(world);var strongholdRecords=StrongholdRecords.get(world);
  var probe=UUID.randomUUID();var before=strongholdRecords.save(new net.minecraft.nbt.CompoundTag(),world.registryAccess());
  var worker=java.util.concurrent.CompletableFuture.supplyAsync(()->CampRecords.get(world)==campRecords&&StrongholdRecords.get(world)==strongholdRecords&&!strongholdRecords.processed(probe,0)&&!strongholdRecords.nightingaleProcessed(probe));
  test.assertTrue(worker.join(),"Worldgen worker did not receive prepared records");
  test.assertTrue(before.equals(strongholdRecords.save(new net.minecraft.nbt.CompoundTag(),world.registryAccess())),"Worker read created or dirtied a missing camp record");
  var manager=world.getStructureManager();int containers=0,atamans=0;
  for(int fixture=0;fixture<3;fixture++){
   var origin=new BlockPos(28672+fixture*96,150,28672);
   TemplateStructurePiece piece=fixture==0?new CampPiece(manager,"storage_tent",origin,UUID.randomUUID(),5,2):new LargeCampPiece(manager,fixture==1?"warehouse_square":"gate",origin,UUID.randomUUID());
   var accepted=piece.getBoundingBox();
   for(int cx=accepted.minX()>>4;cx<=accepted.maxX()>>4;cx++)for(int cz=accepted.minZ()>>4;cz<=accepted.maxZ()>>4;cz++)world.getChunk(cx,cz);
   for(int x=accepted.minX();x<=accepted.maxX();x++)for(int z=accepted.minZ();z<=accepted.maxZ();z++)for(int y=accepted.minY();y<=accepted.maxY();y++)world.setBlock(new BlockPos(x,y,z),(y<origin.getY()?Blocks.STONE:Blocks.AIR).defaultBlockState(),2);
   Map<BlockPos,RandomizableContainerBlockEntity> rewards=new HashMap<>();
   for(int pass=0;pass<2;pass++){
    for(int cx=accepted.minX()>>4;cx<=accepted.maxX()>>4;cx++)for(int cz=accepted.minZ()>>4;cz<=accepted.maxZ()>>4;cz++){
     var clip=new BoundingBox(cx*16,world.getMinBuildHeight(),cz*16,cx*16+15,world.getMaxBuildHeight()-1,cz*16+15);
     piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),RandomSource.create(0),clip,new ChunkPos(cx,cz),origin);
     test.assertTrue(piece.getBoundingBox().equals(accepted),"Expanded camp bounds lost during chunk placement");
    }
    if(pass==0){
     for(int x=accepted.minX();x<=accepted.maxX();x++)for(int z=accepted.minZ();z<=accepted.maxZ();z++)for(int y=origin.getY();y<=accepted.maxY();y++){
      var pos=new BlockPos(x,y,z);if(world.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container&&container.getLootTable()!=null){container.unpackLootTable(null);container.setItem(0,new ItemStack(Items.DIAMOND,3));rewards.put(pos,container);containers++;}
     }
    }else for(var reward:rewards.entrySet())test.assertTrue(world.getBlockEntity(reward.getKey())==reward.getValue()&&reward.getValue().getItem(0).getCount()==3&&reward.getValue().getLootTable()==null,"Camp retry resets player inventory or loot table");
   }
   for(var mob:world.getEntitiesOfClass(BanditEntity.class,new net.minecraft.world.phys.AABB(accepted.minX(),accepted.minY(),accepted.minZ(),accepted.maxX()+1,accepted.maxY()+1,accepted.maxZ()+1)))if(mob.getType()==org.slavicmyths.registry.ModEntities.ATAMAN.get()){test.assertTrue(mob.getMaxHealth()==150,"Structure-spawned Ataman has obsolete health");atamans++;}
  }
  test.assertTrue(containers>=2,"Camp fixture did not exercise loot");test.assertTrue(atamans>0,"Gate fixture did not exercise Ataman spawning");
  System.out.println("CAMP_CHUNK_RETRY_PASS fixtures=3 containers="+containers+" atamans="+atamans+" expandedBoundsRetained=true");test.succeed();
 }
}
