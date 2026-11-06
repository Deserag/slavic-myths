package org.slavicmyths.verify;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import org.slavicmyths.registry.ModBlocks;
@GameTestHolder("slavicmyths_posad") @PrefixGameTestTemplate(false)
public final class PosadFoodGameTests {
 @GameTest(template="empty",timeoutTicks=100,batch="food")
 public static void bonemealStages(GameTestHelper t){
  var crops=java.util.List.of(ModBlocks.GARLIC_CROP.get(),ModBlocks.HORSERADISH_CROP.get(),ModBlocks.BLACK_RADISH_CROP.get(),ModBlocks.MUSTARD_CROP.get(),ModBlocks.ONION_CROP.get(),ModBlocks.CRIMSON_PEPPER_CROP.get());
  for(int i=0;i<crops.size();i++){
   var p=t.absolutePos(new net.minecraft.core.BlockPos(i*3+1,200,1));var w=t.getLevel();for(int dy=0;dy<=3;dy++)w.setBlockAndUpdate(p.above(dy),Blocks.AIR.defaultBlockState());w.setBlockAndUpdate(p.below(),(i==5?Blocks.CRIMSON_NYLIUM:Blocks.FARMLAND).defaultBlockState());var crop=(CropBlock)crops.get(i);
   for(int age=0;age<=3;age++){
    w.setBlockAndUpdate(p,crop.getStateForAge(age));var meal=new ItemStack(Items.BONE_MEAL,8);boolean used=BoneMealItem.growCrop(meal,w,p);
    t.assertTrue(age==3?!used&&meal.getCount()==8:used&&meal.getCount()==7&&crop.getAge(w.getBlockState(p))>age,"Bone meal failed crop="+i+" age="+age+" used="+used+" count="+meal.getCount()+" state="+w.getBlockState(p));
   }
  }t.succeed();
 }
}
