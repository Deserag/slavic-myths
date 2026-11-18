package org.slavicmyths.verify;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.registry.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
@GameTestHolder("slavicmyths_netherboss") @PrefixGameTestTemplate(false)
public final class NetherBossGameTests {
 @GameTest(template="empty",timeoutTicks=200)
 public static void attributesAndSave(GameTestHelper t) {
  var morova=ModEntities.MOROVA_DEVA.get().create(t.getLevel());
  var prince=ModEntities.TRIZNA_PRINCE.get().create(t.getLevel());
  t.assertTrue(morova.getMaxHealth()==1350,"Morova HP");
  t.assertTrue(prince.getMaxHealth()==1800,"Trizna HP");
  var saved=new CompoundTag();morova.addAdditionalSaveData(saved);
  saved.putInt("NetherBossAction",1);saved.putInt("NetherBossActionTicks",25);saved.putInt("NetherBossCooldown",100);saved.putBoolean("NetherBossResolved",false);
  morova.readAdditionalSaveData(saved);
  t.assertTrue(morova.action()==1&&morova.actionTicks()==25,"Lost attack state");
  var roundtrip=new CompoundTag();morova.addAdditionalSaveData(roundtrip);
  t.assertTrue(roundtrip.getInt("NetherBossCooldown")==100&&!roundtrip.getBoolean("NetherBossResolved"),"Lost cooldown/contact state");
  morova.discard();prince.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void plagueLevels(GameTestHelper t) {
  var mob=net.minecraft.world.entity.EntityType.ZOMBIE.create(t.getLevel());
  double speed=mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
  for(int phase=0;phase<3;phase++) {
   mob.removeEffect(ModEffects.MOROVA_PLAGUE);
   mob.addEffect(new MobEffectInstance(ModEffects.MOROVA_PLAGUE,200,phase));
   double ratio=mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)/speed;
   t.assertTrue(Math.abs(ratio-(phase==0?.90:phase==1?.85:.80))<.0001,"Wrong plague slow");
  }
  mob.removeEffect(ModEffects.MOROVA_PLAGUE);
  t.assertTrue(Math.abs(mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)-speed)<.0001,"Attribute residue");
  mob.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void rewardDistribution(GameTestHelper t){
  for(boolean morova:new boolean[]{true,false})for(int looting=0;looting<=3;looting++){
   int both=0,accessory=0;var random=net.minecraft.util.RandomSource.create(353354+looting);
   for(int i=0;i<10000;i++){
    var reward=org.slavicmyths.netherboss.NetherBossRewards.roll(morova,looting,random);
    t.assertTrue(reward.size()==3||reward.size()==4,"Reward count");
    t.assertTrue(reward.get(0).equals("legendary_soul"),"Shared soul");
    t.assertTrue(reward.stream().distinct().count()==reward.size(),"Duplicate reward");
    if(reward.size()==4)both++;if(reward.get(2).equals(morova?"morova_silence_pendant":"fallen_druzhina_horn"))accessory++;
   }
   t.assertTrue(Math.abs(both/10000.0-org.slavicmyths.netherboss.NetherBossRewards.secondChance(looting))<.025,"Second chance distribution");
   t.assertTrue(Math.abs(accessory/10000.0-.5)<.025,"Guaranteed pair fairness");
  }t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void runicCompatibility(GameTestHelper t){
  var sword=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
  var chest=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE);
  t.assertTrue(org.slavicmyths.rpg.Runes.compatible(sword,"morova_lingering"),"Weapon rune rejected");
  t.assertTrue(!org.slavicmyths.rpg.Runes.compatible(chest,"morova_lingering"),"Weapon rune on armor");
  t.assertTrue(org.slavicmyths.rpg.Runes.compatible(chest,"trizna_ember_ward"),"Chest rune rejected");
  t.assertTrue(!org.slavicmyths.rpg.Runes.compatible(sword,"trizna_ember_ward"),"Chest rune on weapon");
  org.slavicmyths.rpg.Runes.write(sword,1,java.util.List.of("morova_lingering"));
  t.assertTrue(org.slavicmyths.rpg.Runes.has(sword,"morova_lingering"),"Existing RuneState lost ID");t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void componentLoot(GameTestHelper t){
  var pairs=java.util.Map.of("nether_watchpost",java.util.List.of("broken_watch_horn","charred_signal_ribbon"),"wayfarer_camp",java.util.List.of("morova_thread","darkened_waymark"),"forgotten_shrine",java.util.List.of("faded_rite_seal","nameless_ashes"),"ancestor_shrine",java.util.List.of("ancestor_bone_charm","mourning_circlet"),"trizna_pyre",java.util.List.of("nameless_ashes","charred_signal_ribbon"));
  var world=t.getLevel();var params=new net.minecraft.world.level.storage.loot.LootParams.Builder(world)
   .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,net.minecraft.world.phys.Vec3.ZERO)
   .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
  for(var row:pairs.entrySet()){
   var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","chests/"+row.getKey()));
   var table=world.getServer().reloadableRegistries().getLootTable(key);
   for(int seed=0;seed<100;seed++){
    var loot=table.getRandomItems(params,seed);
    for(String item:row.getValue()){
     int count=loot.stream().filter(stack->stack.is(org.slavicmyths.netherboss.NetherBossItems.item(item))).mapToInt(net.minecraft.world.item.ItemStack::getCount).sum();
     t.assertTrue(count==1,"Missing/duplicate component "+row.getKey()+" "+item+" seed="+seed);
    }
   }
  }t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void recipes(GameTestHelper t){
  for(boolean morova:new boolean[]{true,false}){
   String[] components=morova?new String[]{"morova_thread","faded_rite_seal","ancestor_bone_charm","nameless_ashes"}:new String[]{"broken_watch_horn","darkened_waymark","mourning_circlet","charred_signal_ribbon"};
   net.minecraft.world.item.Item[] vanilla=morova?new net.minecraft.world.item.Item[]{net.minecraft.world.item.Items.SPIDER_EYE,net.minecraft.world.item.Items.GHAST_TEAR,net.minecraft.world.item.Items.SOUL_SAND,net.minecraft.world.item.Items.BONE}:new net.minecraft.world.item.Item[]{net.minecraft.world.item.Items.BLAZE_ROD,net.minecraft.world.item.Items.GHAST_TEAR,net.minecraft.world.item.Items.NETHERITE_SCRAP,net.minecraft.world.item.Items.BONE};
   var slots=java.util.List.of(new net.minecraft.world.item.ItemStack(org.slavicmyths.netherboss.NetherBossItems.item(components[0])),new net.minecraft.world.item.ItemStack(vanilla[0]),new net.minecraft.world.item.ItemStack(org.slavicmyths.netherboss.NetherBossItems.item(components[1])),new net.minecraft.world.item.ItemStack(vanilla[1]),net.minecraft.world.item.ItemStack.EMPTY,new net.minecraft.world.item.ItemStack(vanilla[2]),new net.minecraft.world.item.ItemStack(org.slavicmyths.netherboss.NetherBossItems.item(components[2])),new net.minecraft.world.item.ItemStack(vanilla[3]),new net.minecraft.world.item.ItemStack(org.slavicmyths.netherboss.NetherBossItems.item(components[3])));
   var input=net.minecraft.world.item.crafting.CraftingInput.of(3,3,slots);
   var recipe=t.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,t.getLevel());
   t.assertTrue(recipe.isPresent(),"Recipe missing");
   t.assertTrue(recipe.get().value().assemble(input,t.getLevel().registryAccess()).is(org.slavicmyths.netherboss.NetherBossItems.item(morova?"amulet_of_the_faded":"last_watch_horn")),"Wrong summoner");
  }t.succeed();
 }

 @GameTest(template="empty",timeoutTicks=1800)
 public static void explicitPosadCommandBuildsPhysicalShell(GameTestHelper t){
  var w=t.getLevel();var a=t.absolutePos(new net.minecraft.core.BlockPos(4096,0,4096)).atY(120);int y=a.getY();
  for(int x=0;x<=112;x++)for(int z=-12;z<=112;z++)for(int dy=-3;dy<=0;dy++)w.setBlock(a.offset(x,dy,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);
  var source=w.getServer().createCommandSourceStack().withLevel(w).withPosition(new net.minecraft.world.phys.Vec3(a.getX()+56,y+1,a.getZ()-12));
  t.assertTrue(org.slavicmyths.posad.PosadCommands.start(source)==1,"Posad command rejected");
  t.runAtTickTime(1600,()->{
   t.assertTrue(w.getBlockState(a).is(net.minecraft.world.level.block.Blocks.BLACKSTONE),"No physical city foundation");
   t.assertTrue(w.getBlockState(a.offset(56,0,56)).is(net.minecraft.world.level.block.Blocks.POLISHED_BLACKSTONE_BRICKS),"No central street");
   t.assertTrue(w.getBlockState(a.offset(56,3,110)).isAir(),"Gate passage blocked");t.succeed();
  });
 }

}
