package org.slavicmyths.marine;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("slavicmyths_marine_armor") @PrefixGameTestTemplate(false)
public final class BossRetaliationGameTests {
 static Mob boss(GameTestHelper t,String id){return (Mob)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",id)).create(t.getLevel());}
 static java.lang.reflect.Method method(Class<?> type,String name,Class<?>... args)throws Exception{while(type!=null){try{var m=type.getDeclaredMethod(name,args);m.setAccessible(true);return m;}catch(NoSuchMethodException e){type=type.getSuperclass();}}throw new NoSuchMethodException(name);}
 @SuppressWarnings("unchecked") static void phase(Mob b)throws Exception{var f=b.getClass().getDeclaredField("PHASE");f.setAccessible(true);b.getEntityData().set((net.minecraft.network.syncher.EntityDataAccessor<Integer>)f.get(null),1);}
 @GameTest(template="empty",timeoutTicks=40)
 public static void remainingBossValidatorsAcceptOnlyRecentLegalAttackers(GameTestHelper t){
  String[][] cases={{"tryaseya","valid"},{"gneteya","valid"},{"ledeya","canShoot"},{"pharaohka","validTarget"},{"earth_spirit","valid"},{"skim_beast","valid"},{"kalin_tsar","enemy"},{"idolishche_poganoe","valid"},{"morozko","candidate"},{"karachun","candidate"}};
  try{for(var row:cases){
   var b=boss(t,row[0]);var cow=EntityType.COW.create(t.getLevel());
   // Arena defaults are centered at the origin for these unbound test entities.
   b.moveTo(0,2,0);cow.moveTo(0,2,2);
   if(b instanceof org.slavicmyths.morozko.Morozko f){f.session=java.util.UUID.randomUUID();f.anchor=b.position();phase(b);}
   if(b instanceof org.slavicmyths.karachun.Karachun n){n.session=java.util.UUID.randomUUID();n.anchor=b.position();phase(b);}
   b.setLastHurtByMob(cow);
   var valid=method(b.getClass(),row[1],LivingEntity.class);
   t.assertTrue((boolean)valid.invoke(b,cow),row[0]+" rejects recent mob attacker");
   cow.setHealth(0);t.assertTrue(!(boolean)valid.invoke(b,cow),row[0]+" accepts a dead attacker");
   cow.discard();b.discard();
  }}catch(Exception e){throw new RuntimeException(e);}t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40)
 public static void retaliationExpiresAndRejectsAllies(GameTestHelper t){
  var b=boss(t,"tsar_pike");var cow=EntityType.COW.create(t.getLevel());b.moveTo(0,2,0);cow.moveTo(0,2,2);
  b.setLastHurtByMob(cow);t.assertTrue(org.slavicmyths.combat.BossRetaliation.recent(b,cow),"Recent attacker missing");
  b.tickCount+=201;t.assertTrue(!org.slavicmyths.combat.BossRetaliation.recent(b,cow),"Retaliation never expired");
  var board=t.getLevel().getScoreboard();var team=board.addPlayerTeam("retaliation_"+b.getId());
  board.addPlayerToTeam(b.getScoreboardName(),team);board.addPlayerToTeam(cow.getScoreboardName(),team);
  b.setLastHurtByMob(cow);t.assertTrue(!org.slavicmyths.combat.BossRetaliation.recent(b,cow),"Friendly fire created a hostile ally");
  board.removePlayerTeam(team);b.discard();cow.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=40)
 public static void sistersAndEarthAttacksDamageMobAggressors(GameTestHelper t){
  try{for(String id:new String[]{"gneteya","tryaseya","earth_spirit"}){
   var b=boss(t,id);var cow=MarineNewWeaponGameTests.target(t);b.moveTo(cow.position().add(0,0,-2));b.setYRot(0);b.setLastHurtByMob(cow);b.setTarget(cow);
   if(b instanceof org.slavicmyths.sisters.SisterBoss s){
    // Both sisters' resolution paths must accept LivingEntity; SLAM is deterministic.
    var state=b.getClass().getDeclaredField("SLAM");state.setAccessible(true);
    method(b.getClass(),"begin",int.class,LivingEntity.class).invoke(b,state.getInt(null),cow);
    method(b.getClass(),"resolve",net.minecraft.server.level.ServerLevel.class,LivingEntity.class,org.slavicmyths.sisters.SisterBoss.class).invoke(b,t.getLevel(),cow,null);
   }else{
    // Exercise the shared damage path used by waves, spikes and thrown rocks.
    var actor=org.slavicmyths.earth.EarthContent.ACTOR.get().create(t.getLevel());
    method(actor.getClass(),"damage",net.minecraft.server.level.ServerLevel.class,org.slavicmyths.earth.EarthSpirit.class,LivingEntity.class,float.class,double.class,Vec3.class).invoke(actor,t.getLevel(),b,cow,10F,0D,b.position());actor.discard();
   }
   t.assertTrue(cow.getHealth()<100,id+" cannot damage mob attacker");cow.discard();b.discard();
  }}catch(Exception e){throw new RuntimeException(e);}t.succeed();
 } @GameTest(template="empty",timeoutTicks=40)
 public static void guardianBoundsAndSummonedSpiritRemainSafe(GameTestHelper t){
  try{
   var cow=MarineNewWeaponGameTests.target(t);var spirit=boss(t,"tryaseya_fever_spirit");spirit.moveTo(cow.position().add(0,0,-1));spirit.setTarget(cow);
   t.assertTrue(spirit.doHurtTarget(cow)&&cow.getHealth()<100,"Summoned spirit cannot damage its assigned mob target");
   spirit.discard();cow.discard();
   for(String id:new String[]{"extinguished_flame_guardian","defiled_spring_keeper"}){
    var b=boss(t,id);var attacker=EntityType.COW.create(t.getLevel());b.moveTo(0,2,0);attacker.moveTo(0,2,2);
    var layout=new net.minecraft.nbt.CompoundTag();layout.putIntArray("ArenaBounds",new int[]{-10,-5,-10,10,10,10});
    var field=org.slavicmyths.guardians.GuardianEntity.class.getDeclaredField("layout");field.setAccessible(true);field.set(b,layout);
    b.setLastHurtByMob(attacker);var guard=(org.slavicmyths.guardians.GuardianEntity)b;
    t.assertTrue(guard.candidate(attacker),id+" rejects attacker inside arena");
    attacker.setPos(30,2,0);t.assertTrue(!guard.candidate(attacker),id+" follows attacker outside arena");
    attacker.discard();b.discard();
   }
  }catch(Exception e){throw new RuntimeException(e);}t.succeed();
 }}