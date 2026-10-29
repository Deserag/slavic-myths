package org.slavicmyths.hellqa;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.netherstructures.*;
import org.slavicmyths.worldgen.StructureCandidates;

/** 2.1.2: decorative-only actual worldgen and source geometry, no client/owner acceptance. */
@GameTestHolder("slavicmyths_hellqa")
@PrefixGameTestTemplate(false)
public final class BridgeQaTests {
 private static void check(boolean pass,String detail){if(!pass)throw new GameTestAssertException(detail);}
 @GameTest(template="empty")
 public static void fourVariantsFiveLaneDeckNoRewards(GameTestHelper t){
  int profiles=0;
  for(int len:new int[]{9,17,23,38})for(int variant=0;variant<4;variant++)
   for(Rotation rot:Rotation.values()){
    int[] depth=new int[len*2];
    for(int n=0;n<len;n++)if(NetherBridgeTerrain.pier(n,len)){depth[n*2]=11;depth[n*2+1]=12;}
    int gap=variant==2?Math.min(5,Math.max(2,len/6)):variant==3?Math.min(5,Math.max(3,len/7)):0;
    int start=(len-gap)/2;
    var plan=new NetherBridgeTerrain.Plan(new BlockPos(8,60,8),rot,len,variant,start,start+gap,depth,1287L,7,10);
    var piece=new NetherBridgePiece(plan);
    check(piece.getBoundingBox().maxX()-piece.getBoundingBox().minX()>=6,"Unexpected narrow bounds");
    int spans=0,holes=0, supports=0;
    for(int n=0;n<len;n++){
     if(piece.span(n)){
      spans++;
      for(int lane=-2;lane<=2;lane++){
       var s=piece.voxel(lane,0,n);
       check(s!=null && s.isSolid(),"Missing road lane="+lane+" span="+n+" variant="+variant);
       check(piece.voxel(lane,1,n).isAir(),"Walkway obstructed above five-lane deck");
       check(piece.voxel(lane,2,n).isAir(),"Walkway blocked at head height");
      }
      for(int lane:new int[]{-3,3})if(NetherBridgeTerrain.pier(n,len)){
       check(piece.voxel(lane,-1,n)!=null,"Missing support connection");
       check(piece.voxel(lane,-5,n)!=null,"Floating basalt pier");
       supports++;
      }
     }else{holes++;
      for(int lane=-3;lane<=3;lane++)check(piece.voxel(lane,0,n)==null,"Collapsed gap still has floor");
     }
     for(int lane=-3;lane<=3;lane++)for(int dy=-24;dy<=2;dy++){
      var s=piece.voxel(lane,dy,n);
      if(s==null)continue;
      check(!s.hasBlockEntity()&&!s.is(Blocks.CHEST)&&!s.is(Blocks.BARREL)&&!s.is(Blocks.SPAWNER)&&!s.is(Blocks.LAVA),
          "Decorative bridge contains forbidden reward/block entity");
     }
    }
    check(spans>=3,"Unreadable ruins");
    if(variant==2||variant==3)check(holes>=2,"Missing major ruin gap");
    check(supports>=2,"No bank supports");
    profiles++;
   }
  check(profiles==64,"Wrong variant count "+profiles);
  System.out.println("HELLQA_PASS 2.1.2 64 geometry profiles / 4 variants / four rotations / 4 lengths, width5, no rewards, non-floating designed supports");
  t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=1200)
 public static void actualNativeNetherLavaBridge(GameTestHelper t){
  var world=t.getLevel().getServer().getLevel(Level.NETHER);
  var registered=world.registryAccess().registryOrThrow(Registries.STRUCTURE).get(NetherBridgeWorldgen.id("broken_nether_bridge"));
  check(registered instanceof NetherBridgeStructure,"Native bridge missing from registry");
  var structure=(NetherBridgeStructure)registered;
  var placement=StructureCandidates.placement(world,structure);
  check(placement!=null,"No native bridge structure_set");
  var generator=world.getChunkSource().getGenerator();
  int sampled=0,plans=0;
  for(int ring=0;ring<=6;ring++)for(int gx=-ring;gx<=ring;gx++)for(int gz=-ring;gz<=ring;gz++){
   if(Math.max(Math.abs(gx),Math.abs(gz))!=ring)continue;
   var cp=placement.getPotentialStructureChunk(world.getSeed(),gx*placement.spacing(),gz*placement.spacing());
   if(!placement.applyAdditionalChunkRestrictions(cp.x,cp.z,world.getSeed()))continue;
   sampled++;
   var c=new Structure.GenerationContext(world.registryAccess(),generator,generator.getBiomeSource(),
        world.getChunkSource().randomState(),world.getStructureManager(),world.getSeed(),cp,
        world,structure.biomes()::contains);
   var planned=structure.findGenerationPoint(c);
   if(planned.isEmpty())continue;
   plans++;
   var nativeChunk=world.getChunk(cp.x,cp.z);
   var start=nativeChunk.getAllStarts().get(structure);
   check(start!=null&&start.isValid(),"Found noise-only plan but NO actual native start at "+cp);
   var bridge=start.getPieces().stream().filter(NetherBridgePiece.class::isInstance)
         .map(NetherBridgePiece.class::cast).findFirst().orElseThrow();
   int road=0,lanes=0,lanterns=0,chests=0;
   var box=bridge.getBoundingBox();
   // Cross-chunk physical proof; check all chunks that intersect the piece, not just its start chunk.
   for(int cx=box.minX()>>4;cx<=box.maxX()>>4;cx++)
    for(int cz=box.minZ()>>4;cz<=box.maxZ()>>4;cz++)world.getChunk(cx,cz);
   for(int n=0;n<bridge.length();n++)if(bridge.span(n)){
    road++;
    for(int lane=-2;lane<=2;lane++){
     var pos=bridge.at(lane,0,n);
     var actual=world.getBlockState(pos);
     check(actual.is(Blocks.POLISHED_BLACKSTONE_BRICKS)||actual.is(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)||
           actual.is(Blocks.POLISHED_BLACKSTONE),"Native physical deck lane missing at "+pos+" got="+actual);
     check(world.isEmptyBlock(pos.above())&&world.isEmptyBlock(pos.above(2)),
        "Native bridge 5-lane headroom blocked at "+pos+" above="+world.getBlockState(pos.above())+" above2="+world.getBlockState(pos.above(2)));
     lanes++;
    }
   }
   for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)
     for(int y=box.minY();y<=box.maxY();y++){
      var s=world.getBlockState(new BlockPos(x,y,z));
      if(s.is(Blocks.CHEST)||s.is(Blocks.BARREL)||s.is(Blocks.SPAWNER))chests++;
      if(s.is(Blocks.LANTERN)||s.is(Blocks.SOUL_LANTERN))lanterns++;
     }
   check(chests==0&&road>=3&&lanes==road*5,"Bridge worldgen has prizes, missing road or wrong width");
   System.out.println("HELLQA_PASS 2.1.2 ACTUAL NATURAL NETHER BRIDGE seed="+world.getSeed()+
       " chunk="+cp+" biome="+world.getBiome(bridge.at(0,0,0)).unwrapKey().orElseThrow().location()+
       " length="+bridge.length()+" variant="+bridge.variant()+" roadSteps="+road+
       " width5Cells="+lanes+" bbox="+box+" sampledCandidates="+sampled+" acceptedPlans="+plans+
       " no loot; chunk-clipped natural generation, not manual place");
   t.succeed();return;
  }
  throw new GameTestAssertException("No accepted true natural Nether bridge in 13x13 placement-cell sample seed="+
        world.getSeed()+" candidates="+sampled+" acceptedPlans="+plans+"; investigate terrain planner, not only registry");
 }
}
