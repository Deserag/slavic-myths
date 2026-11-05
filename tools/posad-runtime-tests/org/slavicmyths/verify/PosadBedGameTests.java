package org.slavicmyths.verify;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.posad.*;
@GameTestHolder("slavicmyths_posad") @PrefixGameTestTemplate(false)
public final class PosadBedGameTests {
 @GameTest(template="empty",timeoutTicks=100)
 public static void completeBedsEveryRotation(GameTestHelper t){
  int total=0;
  for(int turns=0;turns<4;turns++){
   var piece=new PosadPiece(t.absolutePos(new BlockPos(0,20,0)),turns);
   int feet=0,heads=0;
   for(int x=0;x<113;x++)for(int z=0;z<113;z++)for(int y:new int[]{1,7}){
    var s=piece.voxel(x,y,z);if(s==null||!s.is(PosadBeds.COT.get()))continue;
    var d=s.getValue(BedBlock.FACING);boolean foot=s.getValue(BedBlock.PART)==BedPart.FOOT;
    int dx=d.getStepX()*(foot?1:-1),dz=d.getStepZ()*(foot?1:-1);
    var other=piece.voxel(x+dx,y,z+dz);
    t.assertTrue(other!=null&&other.is(s.getBlock())&&other.getValue(BedBlock.PART)!=s.getValue(BedBlock.PART)&&other.getValue(BedBlock.FACING)==d,"Incomplete cot at "+x+","+y+","+z);
    var r=s.rotate(Rotation.values()[turns]);var o=other.rotate(Rotation.values()[turns]);
    t.assertTrue(r.getValue(BedBlock.FACING)==o.getValue(BedBlock.FACING),"Bed halves rotate differently");
    var floor=piece.voxel(x,y-1,z);t.assertTrue(floor!=null&&!floor.isAir(),"Unsupported cot at "+x+","+y+","+z);
    var above=piece.voxel(x,y+1,z);t.assertTrue(above==null||above.isAir(),"Blocked cot at "+x+","+y+","+z+": "+above);
    if(foot)feet++;else heads++;
   }
   t.assertTrue(feet==10&&heads==10,"Wrong bedroom bed count "+feet+"/"+heads);total+=feet;
  }
  System.out.println("POSAD_BEDS_PAIRED_ROTATIONS_PASS beds="+total);t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=100)
 public static void netherCotInteractionDoesNotExplode(GameTestHelper t){
  var w=t.getLevel().getServer().getLevel(net.minecraft.world.level.Level.NETHER);
  var at=new BlockPos(900,180,900);w.getChunk(at);w.getChunk(at.south());
  var foot=PosadBeds.COT.get().defaultBlockState().setValue(BedBlock.FACING,Direction.SOUTH).setValue(BedBlock.PART,BedPart.FOOT);
  w.setBlock(at.below(),Blocks.BLACKSTONE.defaultBlockState(),3);w.setBlock(at.south().below(),Blocks.BLACKSTONE.defaultBlockState(),3);
  w.setBlock(at,foot,2);w.setBlock(at.south(),foot.setValue(BedBlock.PART,BedPart.HEAD),2);
  var p=net.neoforged.neoforge.common.util.FakePlayerFactory.get(w,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"CotSafetyQA"));
  p.setPos(at.getX()+.5,at.getY()+1,at.getZ()+.5);
  var result=foot.useWithoutItem(w,p,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(at),Direction.UP,at,false));
  t.assertTrue(result.consumesAction(),"Nether cot interaction unhandled");
  t.assertTrue(w.getBlockState(at).is(PosadBeds.COT.get())&&w.getBlockState(at.south()).is(PosadBeds.COT.get()),"Nether bed exploded or lost half");
  t.assertTrue(w.getBlockState(at.below()).is(Blocks.BLACKSTONE),"Nether bed destroyed floor");
  w.setBlock(at,Blocks.AIR.defaultBlockState(),2);w.setBlock(at.south(),Blocks.AIR.defaultBlockState(),2);t.succeed();
 }

 @GameTest(template="empty",timeoutTicks=100)
 public static void netherFurnitureDoesNotBurn(GameTestHelper t){
  var w=t.getLevel();var at=t.absolutePos(BlockPos.ZERO);
  for(String wood:new String[]{"crimson","warped"})for(String kind:new String[]{"table","chair","stool","bench","shelf","wardrobe","bedside_cabinet","weapon_rack"}){
   var state=org.slavicmyths.furniture.Furniture.get(wood+"_"+kind).defaultBlockState();
   for(var side:Direction.values())t.assertTrue(state.getFlammability(w,at,side)==0&&state.getFireSpreadSpeed(w,at,side)==0,"Flammable Nether furniture "+wood+"_"+kind);
  }
  var old=org.slavicmyths.furniture.Furniture.get("pine_chair").defaultBlockState();
  t.assertTrue(old.getFlammability(w,at,Direction.UP)>0,"Unrelated pine furniture fire behavior changed");t.succeed();
 }

 @GameTest(template="empty",timeoutTicks=100)
 public static void throneSeatFourRotationsAndCleanup(GameTestHelper t){
  var w=t.getLevel();var seats=new java.util.ArrayList<net.minecraft.world.entity.Entity>();
  var player=PosadTestPlayers.create(w,"ThroneQA");
  for(int turns=0;turns<4;turns++){
   var at=t.absolutePos(new BlockPos(2+turns*3,20,2));w.getChunk(at);w.setChunkForced(at.getX()>>4,at.getZ()>>4,true);
   w.setBlock(at.below(),Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(),2);w.setBlock(at.above(),Blocks.AIR.defaultBlockState(),2);
   var state=org.slavicmyths.furniture.Furniture.get("posad_throne").defaultBlockState()
    .setValue(org.slavicmyths.furniture.FurnitureBlock.FACING,Direction.WEST).rotate(Rotation.values()[turns]);
   w.setBlock(at,state,2);player.setPos(at.getX()+.5,at.getY(),at.getZ()+.5);
   state.useWithoutItem(w,player,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(at),Direction.UP,at,false));
   t.assertTrue(player.getVehicle() instanceof org.slavicmyths.furniture.SeatEntity,"Throne cannot be used at rotation "+turns);
   t.assertTrue(state.getFlammability(w,at,Direction.UP)==0,"Throne is flammable");
   seats.add(player.getVehicle());player.stopRiding();
  }
  t.runAfterDelay(20,()->{for(var seat:seats)t.assertTrue(!seat.isAlive(),"Throne seat leaked after dismount");t.succeed();});
 }

}
