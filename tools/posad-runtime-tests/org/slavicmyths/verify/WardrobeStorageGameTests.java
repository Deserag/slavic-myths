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
import org.slavicmyths.furniture.CrateTile;
import org.slavicmyths.furniture.Furniture;
import org.slavicmyths.furniture.WardrobeBlock;
import org.slavicmyths.furniture.WardrobeTile;
@GameTestHolder("slavicmyths_posad")
@PrefixGameTestTemplate(false)
public final class WardrobeStorageGameTests {
 private static final String[] WOODS={"pine","linden","apple","rowan","willow","darkened","mor_oak","navya","blackwood"};
 @GameTest(template="empty",timeoutTicks=200)
 public static void existingWardrobesHaveSinglePersistentInventory(GameTestHelper t) {
  for(int i=0;i<WOODS.length;i++){
   BlockPos p=t.absolutePos(new BlockPos(i+1,2,1));
   var block=Furniture.get(WOODS[i]+"_wardrobe");
   var lower=block.defaultBlockState().setValue(WardrobeBlock.HALF,DoubleBlockHalf.LOWER);
   t.getLevel().setBlock(p,lower,3);
   t.getLevel().setBlock(p.above(),lower.setValue(WardrobeBlock.HALF,DoubleBlockHalf.UPPER),3);
   t.assertTrue(t.getLevel().getBlockEntity(p) instanceof WardrobeTile,"Missing lower tile "+WOODS[i]);
   t.assertTrue(t.getLevel().getBlockEntity(p.above())==null,"Upper half must not duplicate inventory "+WOODS[i]);
   WardrobeTile tile=(WardrobeTile)t.getLevel().getBlockEntity(p);
   t.assertTrue(tile.getContainerSize()==27,"Incorrect container size "+WOODS[i]);
   tile.setItem(0,new ItemStack(Items.DIAMOND,i+1));
   t.assertTrue(tile.getItem(0).getCount()==i+1,"Cannot store items "+WOODS[i]);
   t.getLevel().setBlock(p.above(),Blocks.AIR.defaultBlockState(),3);
   t.assertTrue(t.getLevel().getBlockState(p).isAir(),"Lower half did not remove "+WOODS[i]);
   t.assertTrue(t.getLevel().getBlockEntity(p)==null,"Removed wardrobe retained tile "+WOODS[i]);
  }
  t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void allBedsideCabinetsHaveRealInventory(GameTestHelper t) {
  for(int i=0;i<WOODS.length;i++){
   BlockPos p=t.absolutePos(new BlockPos(i+1,2,1));
   var block=Furniture.get(WOODS[i]+"_bedside_cabinet");
   t.getLevel().setBlock(p,block.defaultBlockState(),3);
   t.assertTrue(t.getLevel().getBlockEntity(p) instanceof CrateTile,"Missing cabinet inventory "+WOODS[i]);
   CrateTile tile=(CrateTile)t.getLevel().getBlockEntity(p);
   t.assertTrue(tile.getContainerSize()==27,"Incorrect cabinet size "+WOODS[i]);
   tile.setItem(0,new ItemStack(Items.DIAMOND,i+1));
   t.assertTrue(tile.getItem(0).getCount()==i+1,"Cabinet cannot store items "+WOODS[i]);
  }
  t.succeed();
 }
}
