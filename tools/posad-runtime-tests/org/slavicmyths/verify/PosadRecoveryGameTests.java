package org.slavicmyths.verify;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.posad.PosadRecovery;
import org.slavicmyths.worldgen.ManualStructureRecords;
@GameTestHolder("slavicmyths_posad") @PrefixGameTestTemplate(false)
public final class PosadRecoveryGameTests {
 @GameTest(template="empty",timeoutTicks=100,batch="recovery")
 public static void onlyEmptyUnrewardedReservationCanBeReleased(GameTestHelper t){
  var w=t.getLevel().getServer().getLevel(net.minecraft.world.level.Level.NETHER);
  var at=new BlockPos(4096,192,4096);w.getChunk(at);
  for(var pos:BlockPos.betweenClosed(at,at.offset(15,15,15)))w.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
  var source=w.getServer().createCommandSourceStack().withLevel(w).withPermission(2).withSuppressedOutput();
  var records=ManualStructureRecords.get(w);var id=java.util.UUID.randomUUID();
  var box=new BoundingBox(at.getX(),at.getY(),at.getZ(),at.getX()+15,at.getY()+15,at.getZ()+15);
  records.add(id,"posad_building/incomplete_city_safe",0,at,box);
  t.assertTrue(PosadRecovery.releaseEmpty(source.withPermission(0),at)==0,"Non-OP released reservation");
  w.setBlock(at,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(),2);
  t.assertTrue(PosadRecovery.releaseEmpty(source,at)==0&&w.getBlockState(at).is(Blocks.POLISHED_BLACKSTONE_BRICKS),"Recovery removed remaining masonry");
  w.setBlock(at,Blocks.AIR.defaultBlockState(),2);
  records.replace(id,"posad_building/incomplete_city_reward_locked",0,at,box);
  t.assertTrue(PosadRecovery.releaseEmpty(source,at)==0,"Possible rewards were re-armed");
  records.replace(id,"posad_building/incomplete_city_safe",0,at,box);
  w.setBlock(at,Blocks.CHEST.defaultBlockState(),2);
  var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)w.getBlockEntity(at);
  chest.setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND));
  t.assertTrue(PosadRecovery.releaseEmpty(source,at)==0&&chest.getItem(0).is(net.minecraft.world.item.Items.DIAMOND),"Recovery touched player chest");
  w.removeBlockEntity(at);w.setBlock(at,Blocks.AIR.defaultBlockState(),2);
  var saved=records.save(new net.minecraft.nbt.CompoundTag(),w.registryAccess());
  t.assertTrue(ManualStructureRecords.load(saved).entries().stream().anyMatch(e->e.id().equals(id)),"Incomplete city protection lost on serialization");
  t.assertTrue(PosadRecovery.releaseEmpty(source,at)==1,"Proven-empty unrewarded reservation not released");
  t.assertTrue(records.entries().stream().noneMatch(e->e.id().equals(id)),"Released record remained");t.succeed();
 }
}
