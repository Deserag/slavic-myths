package org.slavicmyths.marine;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("slavicmyths_marine_armor") @PrefixGameTestTemplate(false)
public final class CropHarvestGameTests {
 @GameTest(template="empty",timeoutTicks=60)
 public static void allPlantableIngredientsGrowAndReplantWithSickle(GameTestHelper t){
  var p=net.neoforged.neoforge.common.util.FakePlayerFactory.get(t.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"CropHarvest"));
  p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
  var crops=org.slavicmyths.farming.Farming.crops();var seeds=org.slavicmyths.farming.Farming.seeds();
  var ground=t.absolutePos(new BlockPos(3,2,3));var pos=ground.above();var world=t.getLevel();
  p.moveTo(Vec3.atCenterOf(ground).add(2,1,0));
  for(int i=0;i<crops.size();i++){
   var crop=(CropBlock)crops.get(i);var id=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(crop);
   world.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
   world.setBlock(ground,(crop instanceof org.slavicmyths.farming.CrimsonPepperCrop?Blocks.CRIMSON_NYLIUM:Blocks.FARMLAND).defaultBlockState(),3);
   p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(seeds.get(i),2));
   p.getMainHandItem().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(ground),Direction.UP,ground,false)));
   t.assertTrue(world.getBlockState(pos).is(crop),id+" could not be planted");
   var meal=new ItemStack(Items.BONE_MEAL,32);
   for(int n=0;n<8&&!crop.isMaxAge(world.getBlockState(pos));n++)
    t.assertTrue(BoneMealItem.growCrop(meal,world,pos),id+" rejected bone meal");
   t.assertTrue(crop.isMaxAge(world.getBlockState(pos)),id+" did not mature");
   p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(org.slavicmyths.registry.ModItems.SICKLE.get()));
   var hit=new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
   var result=p.getMainHandItem().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,hit));
   t.assertTrue(result.consumesAction(),id+" rejected sickle harvest");
   var replanted=world.getBlockState(pos);
   t.assertTrue(replanted.is(crop)&&!crop.isMaxAge(replanted),id+" not replanted");
   t.assertTrue(p.getMainHandItem().getDamageValue()==1,id+" incorrect sickle wear");
   int wear=p.getMainHandItem().getDamageValue();
   p.getMainHandItem().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,hit));
   t.assertTrue(p.getMainHandItem().getDamageValue()==wear,id+" harvested an immature crop");
  }
  world.setBlock(pos,Blocks.AIR.defaultBlockState(),3);p.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=60)
 public static void netherIngredientsCanBePlantedFertilizedAndHarvested(GameTestHelper t){
  var p=net.neoforged.neoforge.common.util.FakePlayerFactory.get(t.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"NetherGardener"));p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
  var world=t.getLevel();var ground=t.absolutePos(new BlockPos(8,4,8));var pos=ground.above();p.moveTo(Vec3.atCenterOf(ground).add(3,1,0));
  var blocks=new net.minecraft.world.level.block.Block[]{org.slavicmyths.registry.ModBlocks.HEAT_BERRY_BUSH.get(),org.slavicmyths.registry.ModBlocks.SERNICA_CROP.get(),org.slavicmyths.registry.ModBlocks.COAL_PLUM_SAPLING.get()};
  var items=new Item[]{org.slavicmyths.registry.ModItems.HEAT_BERRY_CUTTING.get(),org.slavicmyths.registry.ModItems.SERNICA_SEEDS.get(),org.slavicmyths.registry.ModItems.COAL_PLUM_SAPLING.get()};
  for(int kind=0;kind<3;kind++){
   for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)for(int y=0;y<=8;y++)world.setBlock(pos.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);
   world.setBlock(ground,(kind==1?Blocks.SOUL_SOIL:Blocks.CRIMSON_NYLIUM).defaultBlockState(),3);
   p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(items[kind],2));p.getMainHandItem().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(ground),Direction.UP,ground,false)));
   t.assertTrue(world.getBlockState(pos).is(blocks[kind]),"Nether planting failed: "+kind);
   var meal=new ItemStack(Items.BONE_MEAL,20);
   if(kind==2){
    world.setBlock(pos.above(4),Blocks.STONE.defaultBlockState(),3);int count=meal.getCount();
    t.assertTrue(!BoneMealItem.growCrop(meal,world,pos)&&meal.getCount()==count,"Blocked tree consumed bone meal");
    t.assertTrue(world.getBlockState(pos.above(4)).is(Blocks.STONE),"Tree overwrote obstruction");world.setBlock(pos.above(4),Blocks.AIR.defaultBlockState(),3);
    t.assertTrue(BoneMealItem.growCrop(meal,world,pos),"Plum sapling rejects bone meal");
    t.assertTrue(world.getBlockState(pos).is(Blocks.CRIMSON_STEM),"Sapling did not grow into a tree");
    int height=0;while(world.getBlockState(pos.above(height)).is(Blocks.CRIMSON_STEM))height++;
    t.assertTrue(height>=5&&height<=7,"Tree height outside agreed bounds");
    var fruitPos=pos.offset(1,height-1,0);var leaf=(CropBlock)org.slavicmyths.registry.ModBlocks.COAL_PLUM_LEAVES.get();
    for(int n=0;n<3;n++)t.assertTrue(BoneMealItem.growCrop(meal,world,fruitPos),"Plum foliage did not ripen");
    p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(org.slavicmyths.registry.ModItems.SICKLE.get()));
    p.getMainHandItem().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(fruitPos),Direction.UP,fruitPos,false)));
    t.assertTrue(world.getBlockState(fruitPos).is(leaf)&&!leaf.isMaxAge(world.getBlockState(fruitPos)),"Plum branches were destroyed on harvest");
   }else{
    var crop=(org.slavicmyths.farming.NetherPlant)blocks[kind];
    for(int n=0;n<3;n++)t.assertTrue(BoneMealItem.growCrop(meal,world,pos),"Nether ingredient rejects bone meal: "+kind);
    int count=meal.getCount();t.assertTrue(!BoneMealItem.growCrop(meal,world,pos)&&meal.getCount()==count,"Mature crop consumed bone meal");
    t.assertTrue(!Block.getDrops(world.getBlockState(pos),world,pos,null).isEmpty(),"Mature crop has no yield");
    p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(org.slavicmyths.registry.ModItems.SICKLE.get()));
    p.getMainHandItem().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false)));
    t.assertTrue(world.getBlockState(pos).is(crop)&&crop.getAge(world.getBlockState(pos))==crop.harvestedAge(),"Nether ingredient was not replanted: "+kind);
   }
  }
  p.discard();t.succeed();
 }}