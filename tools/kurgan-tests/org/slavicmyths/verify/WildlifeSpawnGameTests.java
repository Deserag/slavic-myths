package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;

@GameTestHolder("slavicmyths") @PrefixGameTestTemplate(false)
public final class WildlifeSpawnGameTests {
 @GameTest(template="port_empty",timeoutTicks=100)
 public static void familyCreativeEggs(GameTestHelper test){
  var tab=org.slavicmyths.registry.ModItemGroup.TAB.get();
  tab.buildContents(new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters(test.getLevel().enabledFeatures(),true,test.getLevel().registryAccess()));
  var items=tab.getDisplayItems();
  test.assertTrue(items.stream().filter(stack->stack.is(ModItems.BROWN_BEAR_SPAWN_EGG.get())).count()==1,"Missing/duplicate bear family egg");
  test.assertTrue(items.stream().filter(stack->stack.is(ModItems.STAG_SPAWN_EGG.get())).count()==1,"Missing/duplicate deer family egg");
  test.assertTrue(items.stream().noneMatch(stack->stack.is(ModItems.BEAR_CUB_SPAWN_EGG.get())||stack.is(ModItems.DOE_SPAWN_EGG.get())),"Redundant legacy eggs still displayed");
  test.assertTrue(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(ModItems.BEAR_CUB_SPAWN_EGG.get()).toString().equals("slavicmyths:bear_cub_spawn_egg"),"Legacy cub ID removed");
  System.out.println("WILDLIFE_CREATIVE_EGGS_PASS oneBear=true oneDeer=true legacyIdsPreserved=true");test.succeed();
 }
 @GameTest(template="port_empty",timeoutTicks=100)
 public static void families(GameTestHelper test) {
  var random=RandomSource.create(990099);int bears=0,stags=0,does=0,families=0;
  for(int i=0;i<1000;i++){
   var bear=org.slavicmyths.entity.WildlifeFamilyPlan.bear(random,false);if(bear.babies()>0){bears++;test.assertTrue(bear.babies()<=2,"Too many cubs");}
   var deer=org.slavicmyths.entity.WildlifeFamilyPlan.deer(random);if(deer.babies()>0)families++;else if(deer.adult()==org.slavicmyths.entity.WildlifeFamilyPlan.Adult.STAG)stags++;else does++;
  }
  test.assertTrue(bears>=60&&bears<=140&&families>=60&&families<=140,"Family probability drift");
  test.assertTrue(stags>=380&&stags<=520&&does>=380&&does<=520,"Deer adult probability drift");
  var world=test.getLevel();var origin=test.absolutePos(new BlockPos(13000,160,13000));
  world.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.PORTAL,new net.minecraft.world.level.ChunkPos(origin),3,origin);
  world.getChunkAt(origin);
  for(int x=(origin.getX()-5)>>4;x<=(origin.getX()+5)>>4;x++)for(int z=(origin.getZ()-5)>>4;z<=(origin.getZ()+5)>>4;z++)world.getChunk(x,z);
  for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)for(int y=-1;y<=4;y++)world.setBlock(origin.offset(x,y,z),(y==-1?Blocks.GRASS_BLOCK:Blocks.AIR).defaultBlockState(),2);
  int bearRolls=bears,stagRolls=stags,doeRolls=does,familyRolls=families;
  // Fresh far-away chunks publish their entity-section visibility on the next server tick.
  test.runAfterDelay(20,()->{
  var mother=ModEntities.BROWN_BEAR.get().create(world);mother.moveTo(origin.getX()+.5,origin.getY(),origin.getZ()+.5,0,0);test.assertTrue(world.addFreshEntity(mother),"Mother insertion failed");
  boolean placed=mother.family(2);test.assertTrue(placed&&mother.isMother()&&mother.getMaxHealth()==120,"Mother stats/composition placed="+placed+" flag="+mother.isMother()+" hp="+mother.getMaxHealth());
  test.assertTrue(!mother.family(1),"Family retry duplicated cubs");
  var children=world.getEntitiesOfClass(org.slavicmyths.entity.WildlifeEntity.class,mother.getBoundingBox().inflate(6),a->mother.getUUID().equals(a.parentId()));
  test.assertTrue(children.size()==2&&children.stream().allMatch(a->a.getMaxHealth()==12&&a.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)==0),"Cub attributes/ownership count="+children.size()+" values="+children.stream().map(a->a.kind()+" hp="+a.getMaxHealth()+" damage="+a.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)).toList());
  var attacker=net.minecraft.world.entity.EntityType.PIG.create(world);attacker.moveTo(origin.getX()+3,origin.getY(),origin.getZ()+3,0,0);world.addFreshEntity(attacker);
  var unrelated=ModEntities.BROWN_BEAR.get().create(world);unrelated.moveTo(origin.getX()-3,origin.getY(),origin.getZ()-3,0,0);world.addFreshEntity(unrelated);
  test.assertTrue(children.getFirst().hurt(world.damageSources().mobAttack(attacker),1),"Cub threat did not damage cub");
  test.assertTrue(mother.getTarget()==attacker&&unrelated.getTarget()==null,"Cub threat enrages unrelated bear or ignores owning mother");
  int entityAge=mother.tickCount;mother.tickCount=999999;test.assertTrue(mother.actionProgress(0)<.1F,"Animation clock depends on entity tracking age");mother.tickCount=entityAge;
  try{var field=org.slavicmyths.entity.WildlifeEntity.class.getDeclaredField("motherBar");field.setAccessible(true);var bar=(net.minecraft.server.level.ServerBossEvent)field.get(mother);mother.aiStep();test.assertTrue(bar.isVisible(),"Mother combat bar absent");mother.setTarget(null);mother.aiStep();test.assertTrue(!bar.isVisible(),"Mother bar remains after combat");}catch(ReflectiveOperationException failure){throw new AssertionError(failure);}
  attacker.discard();unrelated.discard();
  var saved=new net.minecraft.nbt.CompoundTag();mother.saveWithoutId(saved);var restored=ModEntities.BROWN_BEAR.get().create(world);restored.load(saved);
  test.assertTrue(restored.isMother()&&restored.getMaxHealth()==120&&!restored.family(1),"Mother NBT reload / family idempotence");children.forEach(net.minecraft.world.entity.Entity::discard);mother.discard();
  var doe=ModEntities.DOE.get().create(world);doe.moveTo(origin.getX()+.5,origin.getY(),origin.getZ()+.5,0,0);world.addFreshEntity(doe);test.assertTrue(doe.family(1),"Doe family fails");
  var fawns=world.getEntitiesOfClass(org.slavicmyths.entity.WildlifeEntity.class,doe.getBoundingBox().inflate(6),a->doe.getUUID().equals(a.parentId()));
  test.assertTrue(fawns.size()==1&&fawns.getFirst().isFawn()&&fawns.getFirst().getMaxHealth()==12&&fawns.getFirst().getBbHeight()<doe.getBbHeight(),"Fawn scale/stats");fawns.forEach(net.minecraft.world.entity.Entity::discard);doe.discard();
  var blocked=ModEntities.BROWN_BEAR.get().create(world);blocked.moveTo(origin.getX()+.5,origin.getY(),origin.getZ()+.5,0,0);
  for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)for(int y=0;y<=2;y++)world.setBlock(origin.offset(x,y,z),Blocks.STONE.defaultBlockState(),2);
  test.assertTrue(!blocked.family(2)&&!blocked.isMother(),"Blocked encounter leaves partial family");
  world.getChunkSource().removeRegionTicket(net.minecraft.server.level.TicketType.PORTAL,new net.minecraft.world.level.ChunkPos(origin),3,origin);
  System.out.println("WILDLIFE_RUNTIME_FAMILY_PASS rolls=1000 bearFamilies="+bearRolls+" stag="+stagRolls+" doe="+doeRolls+" deerFamilies="+familyRolls+" motherHP=120 cubHP=12 rollback=true ownedDefense=true combatBarLifecycle=true animationUsesWorldClock=true");test.succeed();
  });
 }
 @GameTest(template="port_empty",timeoutTicks=100)
 public static void predicatesAndBiomes(GameTestHelper test) {
  var world=test.getLevel();world.setDayTime(6000);
  var p=test.absolutePos(new BlockPos(12000,160,12000));world.getChunkAt(p);
  for(int y=-1;y<=3;y++)world.setBlock(p.offset(0,y,0),Blocks.AIR.defaultBlockState(),2);
  int groundCases=0;
  for(var type:List.of(ModEntities.BROWN_BEAR.get(),ModEntities.FOREST_WOLF.get(),ModEntities.BOAR.get(),ModEntities.STAG.get(),ModEntities.DOE.get())) {
   test.assertTrue(SpawnPlacements.getPlacementType(type)==SpawnPlacementTypes.ON_GROUND,"Missing ground placement "+type);
   for(var ground:List.of(Blocks.GRASS_BLOCK,Blocks.PODZOL,Blocks.COARSE_DIRT,Blocks.ROOTED_DIRT,Blocks.MOSS_BLOCK)) {
    world.setBlock(p.below(),ground.defaultBlockState(),2);
    test.assertTrue(SpawnPlacements.checkSpawnRules(type,world,MobSpawnType.NATURAL,p,RandomSource.create(99)),"Rejects woodland floor "+type+" "+ground);groundCases++;
   }
   world.setBlock(p.below(),Blocks.LAVA.defaultBlockState(),2);
   test.assertTrue(!SpawnPlacements.checkSpawnRules(type,world,MobSpawnType.NATURAL,p,RandomSource.create(99)),"Accepts lava "+type);
  }
  int waterCases=0;
  for(var type:List.of(ModEntities.PIKE.get(),ModEntities.CARP.get(),ModEntities.CRAYFISH.get())) {
   test.assertTrue(SpawnPlacements.getPlacementType(type)==SpawnPlacementTypes.IN_WATER,"Missing water placement "+type);
   for(int height:List.of(-20,62,120)) {
    var at=new BlockPos(p.getX()+8,height,p.getZ());
    world.setBlock(at,Blocks.WATER.defaultBlockState(),2);world.setBlock(at.above(),Blocks.WATER.defaultBlockState(),2);
    world.setBlock(at.below(),(type==ModEntities.CRAYFISH.get()?Blocks.CLAY:Blocks.WATER).defaultBlockState(),2);
    test.assertTrue(SpawnPlacements.checkSpawnRules(type,world,MobSpawnType.NATURAL,at,RandomSource.create(99)),"Rejects valid river elevation "+type+" "+height);waterCases++;
    world.setBlock(at,Blocks.AIR.defaultBlockState(),2);
    test.assertTrue(!SpawnPlacements.checkSpawnRules(type,world,MobSpawnType.NATURAL,at,RandomSource.create(99)),"Water animal accepts air "+type);
   }
  }
  var biomes=world.registryAccess().registryOrThrow(Registries.BIOME);
  int eligible=0;
  for(var type:List.of(ModEntities.BROWN_BEAR.get(),ModEntities.FOREST_WOLF.get(),ModEntities.BOAR.get(),ModEntities.STAG.get(),ModEntities.DOE.get(),ModEntities.PIKE.get(),ModEntities.CARP.get(),ModEntities.CRAYFISH.get())) {
   int count=0;
   for(var biome:biomes)for(var spawn:biome.getMobSettings().getMobs(type.getCategory()).unwrap())if(spawn.type==type) {
    test.assertTrue(spawn.getWeight().asInt()>0&&spawn.minCount>0&&spawn.maxCount>=spawn.minCount,"Invalid actual spawn table "+type);count++;
   }
   test.assertTrue(count>0,"No eligible modified biomes "+type);eligible+=count;
  }
  System.out.println("WILDLIFE_RUNTIME_SPAWN_PASS woodlandFloors="+groundCases+" waterElevations="+waterCases+" biomeEntries="+eligible);test.succeed();
 }
}
