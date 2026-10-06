package org.slavicmyths.verify;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.worldgen.LandFoundationPiece;
@GameTestHolder("slavicmyths_terrain_placement") @PrefixGameTestTemplate(false)
public final class NativeLandPreparationGameTests {
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void canopyAndSlopeDoNotLeaveHollowFoundations(GameTestHelper test){
  var world=test.getLevel();int x0=65536,z0=65536,floor=world.getMinBuildHeight()+3,target=floor+6;
  var core=new BoundingBox(x0,floor,z0,x0+15,target+2,z0+15);var piece=new LandFoundationPiece(core,target,12,world.getMinBuildHeight(),world.getMaxBuildHeight()-1);var box=piece.getBoundingBox();
  for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++){world.getChunk(x>>4,z>>4);for(int y=world.getMinBuildHeight();y<=floor+24;y++)world.setBlock(new BlockPos(x,y,z),(y==world.getMinBuildHeight()?Blocks.BEDROCK:y<floor?Blocks.DIRT:y==floor?Blocks.GRASS_BLOCK:Blocks.AIR).defaultBlockState(),2);}
  for(int x=x0;x<x0+8;x++)for(int z=z0;z<z0+8;z++){int height=floor+1+(x-x0)+(z-z0)/2;for(int y=floor+1;y<=height;y++)world.setBlock(new BlockPos(x,y,z),Blocks.DIRT.defaultBlockState(),2);world.setBlock(new BlockPos(x,height+1,z),Blocks.SNOW.defaultBlockState(),2);}
  for(int y=floor+1;y<=floor+18;y++)world.setBlock(new BlockPos(x0+10,y,z0+10),Blocks.OAK_LOG.defaultBlockState(),2);
  for(int x=x0+8;x<=x0+12;x++)for(int z=z0+8;z<=z0+12;z++)world.setBlock(new BlockPos(x,floor+18,z),Blocks.OAK_LEAVES.defaultBlockState(),2);
  world.setBlock(new BlockPos(x0+14,floor+1,z0+14),Blocks.POPPY.defaultBlockState(),2);
  for(int cx=box.minX()>>4;cx<=box.maxX()>>4;cx++)for(int cz=box.minZ()>>4;cz<=box.maxZ()>>4;cz++)piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),RandomSource.create(0),new BoundingBox(cx*16,world.getMinBuildHeight(),cz*16,cx*16+15,world.getMaxBuildHeight()-1,cz*16+15),new ChunkPos(cx,cz),BlockPos.ZERO);
  for(int x=x0;x<=x0+15;x++)for(int z=z0;z<=z0+15;z++){for(int y=floor;y<=target;y++)test.assertTrue(!world.getBlockState(new BlockPos(x,y,z)).getCollisionShape(world,new BlockPos(x,y,z)).isEmpty(),"Hollow native foundation at "+new BlockPos(x,y,z));for(int y=target+1;y<=floor+20;y++)test.assertTrue(world.getBlockState(new BlockPos(x,y,z)).isAir(),"Tree/snow/flower remains above terrace at "+new BlockPos(x,y,z));test.assertTrue(world.getBlockState(new BlockPos(x,world.getMinBuildHeight(),z)).is(Blocks.BEDROCK),"Bedrock was changed");}
  System.out.println("NATIVE_LAND_PREPARATION_PASS columns=256 slope=true canopy=true snow=true flower=true foundationSolid=true bedrockPreserved=true");test.succeed();
 }
}
