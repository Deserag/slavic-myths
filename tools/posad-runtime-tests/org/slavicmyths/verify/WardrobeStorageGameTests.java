package org.slavicmyths.verify;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.furniture.Furniture;
import org.slavicmyths.furniture.WardrobeBlock;
import org.slavicmyths.furniture.WardrobeTile;
@GameTestHolder("slavicmyths_posad")
@PrefixGameTestTemplate(false)
public final class WardrobeStorageGameTests {
 @GameTest(template="empty",timeoutTicks=200)
 public static void existingWardrobesHaveSinglePersistentInventory(GameTestHelper t) {
  String[] woods={"pine","linden","apple","rowan","willow","darkened","mor_oak","navya","blackwood"};
  for(int i=0;i<woods.length;i++){
   BlockPos p=t.absolutePos(new BlockPos(i+1,2,1));
   var block=Furniture.get(woods[i]+"_wardrobe");
   var lower=block.defaultBlockState().setValue(WardrobeBlock.HALF,DoubleBlockHalf.LOWER);
   t.getLevel().setBlock(p,lower,3);
   t.getLevel().setBlock(p.above(),lower.setValue(WardrobeBlock.HALF,DoubleBlockHalf.UPPER),3);
   t.assertTrue(t.getLevel().getBlockEntity(p) instanceof WardrobeTile,"Missing lower tile "+woods[i]);
   t.assertTrue(t.getLevel().getBlockEntity(p.above())==null,"Upper half must not duplicate inventory "+woods[i]);
   WardrobeTile tile=(WardrobeTile)t.getLevel().getBlockEntity(p);
   t.assertTrue(tile.getContainerSize()==27,"Incorrect container size "+woods[i]);
   tile.setItem(0,new ItemStack(Items.DIAMOND,i+1));
   t.assertTrue(tile.getItem(0).getCount()==i+1,"Cannot store items "+woods[i]);
   t.getLevel().setBlock(p.above(),Blocks.AIR.defaultBlockState(),3);
   t.assertTrue(t.getLevel().getBlockState(p).isAir(),"Lower half did not remove "+woods[i]);
   t.assertTrue(t.getLevel().getBlockEntity(p)==null,"Removed wardrobe retained tile "+woods[i]);
  }
  t.succeed();
 }
}
