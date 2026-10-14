package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.*;
import org.slavicmyths.combat.CreatureHealthBalance;
import org.slavicmyths.gorynych.*;
import org.slavicmyths.guardians.*;
import org.slavicmyths.hunt.*;
import org.slavicmyths.morozko.*;
import org.slavicmyths.bandit.*;
import org.slavicmyths.volkolak.CursedVolkolak;

/** Native NBT/attributes/rules without a Minecraft server, client, or world. */
public final class CreatureHealthChecks {
 private static int checks;
 private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 private static void near(double a,double b,String message){check(Math.abs(a-b)<.001,message+" "+a+" != "+b);}
 private static CompoundTag entity(double max,float health,boolean mor){
  var n=new CompoundTag();n.putFloat("Health",health);var attribute=new AttributeInstance(Attributes.MAX_HEALTH,a->{});attribute.setBaseValue(max);
  if(mor)attribute.addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","mor_health"),1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
  var list=new ListTag();list.add(attribute.save());n.put("attributes",list);n.putString("Unrelated","preserved");return n;
 }
 private static void firebirdLifecycle()throws Exception{
  // Deliberately incomplete level stub: no constructor, world, server thread, ticks or registries.
  var field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);var unsafe=(sun.misc.Unsafe)field.get(null);
  var bird=(org.slavicmyths.firebird.FirebirdEntity)unsafe.allocateInstance(org.slavicmyths.firebird.FirebirdEntity.class);
  var level=unsafe.allocateInstance(net.minecraft.server.level.ServerLevel.class);var world=net.minecraft.world.entity.Entity.class.getDeclaredField("level");world.setAccessible(true);world.set(bird,level);
  var builder=new net.minecraft.network.syncher.SynchedEntityData.Builder(bird);
  for(var entry:Map.<String,Object>of("DATA_SHARED_FLAGS_ID",(byte)0,"DATA_AIR_SUPPLY_ID",300,"DATA_CUSTOM_NAME_VISIBLE",false,"DATA_CUSTOM_NAME",Optional.empty(),"DATA_SILENT",false,"DATA_NO_GRAVITY",false,"DATA_POSE",net.minecraft.world.entity.Pose.STANDING,"DATA_TICKS_FROZEN",0).entrySet()){
   var key=net.minecraft.world.entity.Entity.class.getDeclaredField(entry.getKey());key.setAccessible(true);builder.define((net.minecraft.network.syncher.EntityDataAccessor)key.get(null),entry.getValue());
  }
  var define=org.slavicmyths.firebird.FirebirdEntity.class.getDeclaredMethod("defineSynchedData",net.minecraft.network.syncher.SynchedEntityData.Builder.class);define.setAccessible(true);define.invoke(bird,builder);var data=net.minecraft.world.entity.Entity.class.getDeclaredField("entityData");data.setAccessible(true);data.set(bird,builder.build());
  var cue=org.slavicmyths.firebird.FirebirdEntity.class.getDeclaredField("hurtCue");cue.setAccessible(true);cue.setInt(bird,1); // Sound playback unavailable in this fixture.
  var source=new net.minecraft.world.damagesource.DamageSource(net.minecraft.core.Holder.direct(new net.minecraft.world.damagesource.DamageType("fixture",0)));
  for(float hp:new float[]{-1000,0,1,1500,3000,100000,Float.NaN}){bird.setHealth(hp);check(bird.getHealth()>0&&bird.getHealth()<=3000&&bird.isAlive(),"native firebird setHealth guard");}
  bird.setHealth(1500);check(!bird.hurt(source,100000)&&bird.getHealth()==1500,"native environmental overkill blocked");bird.die(source);bird.kill();check(bird.isAlive()&&!bird.isRemoved(),"native die/kill do not kill/discard");near(bird.getMaxHealth(),3000,"native bird ceiling retained");
 }
 public static void main(String[] args)throws Exception{
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  // Register NeoForge's real attribute suppliers in this offline JVM only (no mod/game launch).
  var registry=(net.minecraft.core.MappedRegistry<Attribute>)net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE;registry.unfreeze();
  var register=net.neoforged.neoforge.common.NeoForgeMod.class.getDeclaredField("ATTRIBUTES");register.setAccessible(true);
  var entries=net.neoforged.neoforge.registries.DeferredRegister.class.getDeclaredField("entries");entries.setAccessible(true);
  for(var entry:((Map<?,?>)entries.get(register.get(null))).entrySet()){
   var holder=(net.neoforged.neoforge.registries.DeferredHolder<?,?>)entry.getKey();
   net.minecraft.core.Registry.register(registry,holder.getId(),(Attribute)((java.util.function.Supplier<?>)entry.getValue()).get());
  }
  for(double[] pair:new double[][]{{150,100},{350,400},{250,350},{320,350},{400,700},{500,900},{450,800},{1000,1400}}){
   for(double fraction:new double[]{0,.01,.05,.25,.5,.625,.8,1}){
    var original=entity(pair[0],(float)(pair[0]*fraction),false);var saved=original.copy();var migrated=CreatureHealthBalance.prepare(original,pair[1],pair[0],pair[1]>1024);
    near(migrated.getFloat("Health"),pair[1]*fraction,"old health proportion");near(CreatureHealthBalance.savedBase(migrated,0),pair[1],"new base");check(original.equals(saved),"source NBT is immutable");check(migrated.getString("Unrelated").equals("preserved"),"unrelated NBT retained");
    check(CreatureHealthBalance.prepare(migrated,pair[1],pair[0],pair[1]>1024).equals(migrated),"second load is idempotent");
   }
  }
  for(double fraction:new double[]{0,.05,.4,1}){
   var n=entity(320,(float)(640*fraction),true);var m=CreatureHealthBalance.prepare(n,350,320,false);near(m.getFloat("Health"),700*fraction,"mor wounded HP scales once");
   var attrs=m.getList("attributes",10);check(attrs.getCompound(0).getList("modifiers",10).size()==1,"one permanent mor modifier");
   var a=new AttributeInstance(Attributes.MAX_HEALTH,x->{});a.load(attrs.getCompound(0));near(a.getValue(),700,"actual native total mor max");
   check(CreatureHealthBalance.prepare(m,350,320,false).equals(m),"mor second load no healing/doubling");
  }
  var raw=new AttributeInstance(Attributes.MAX_HEALTH,a->{});raw.setBaseValue(1400);near(raw.getValue(),1024,"installed vanilla cap");near(CreatureHealthBalance.uncappedMaximum(raw),1400,"local Morozko ceiling");
  var oldFrost=entity(1000,1024,false);var oldAttribute=oldFrost.getList("attributes",10).getCompound(0);var oldMods=new ListTag();oldMods.add(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","old_max_bonus"),100,AttributeModifier.Operation.ADD_VALUE).save());oldAttribute.put("modifiers",oldMods);near(CreatureHealthBalance.prepare(oldFrost,1400,1000,true).getFloat("Health"),1500,"legacy Morozko modifiers used capped old maximum");
  raw.addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","test_health_add"),100,AttributeModifier.Operation.ADD_VALUE));raw.addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","test_health_base"),.1,AttributeModifier.Operation.ADD_MULTIPLIED_BASE));raw.addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","test_health_total"),.2,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));near(CreatureHealthBalance.uncappedMaximum(raw),1980,"uncapped native operation ordering");
  near(BanditEntity.attributes(BanditEntity.ATAMAN).build().getBaseValue(Attributes.MAX_HEALTH),100,"new ataman");near(WorldBoss.attributes(BossKind.TUGARIN).build().getBaseValue(Attributes.MAX_HEALTH),400,"new Tugarin");near(WorldBoss.attributes(BossKind.LIKHO).build().getBaseValue(Attributes.MAX_HEALTH),350,"new ordinary Likho");near(NightingaleEntity.attributes().build().getBaseValue(Attributes.MAX_HEALTH),350,"new Nightingale");near(CursedVolkolak.attributes().build().getBaseValue(Attributes.MAX_HEALTH),700,"new cursed Volkolak");
  near(700*.625,437.5,"cursed phase2");near(700*.25,175,"cursed phase3");near(FrostRules.BOSS_HP,1400,"Morozko default");near(FrostRules.DOUBLE_HP,50,"ice double unchanged");
  near(GuardianEntity.attributes(false).build().getBaseValue(Attributes.MAX_HEALTH),900,"new fire");near(GuardianEntity.attributes(true).build().getBaseValue(Attributes.MAX_HEALTH),800,"new spring");near(GuardianRules.threshold(false),45,"fire floor");near(GuardianRules.threshold(true),40,"water floor");near(GuardianRules.HEAL_AMOUNT,5,"healing unchanged");near(GuardianRules.ECHO_HP,25,"echo unchanged");
  for(boolean water:new boolean[]{false,true})for(String stage:List.of("READY","ACTIVE","PURIFICATION","CLEARED")){
   var row=new CompoundTag();row.putBoolean("Spring",water);row.putString("Stage",stage);row.putFloat("Health",water?225:250);row.putString("ClaimReceipt","kept");
   check(GuardianRules.migrateRecord(row),"first guardian record load");near(row.getFloat("Health"),stage.equals("CLEARED")?(water?800:900):stage.equals("PURIFICATION")?(water?40:45):(water?400:450),"guardian ledger health/stage");var copy=row.copy();check(!GuardianRules.migrateRecord(row)&&row.equals(copy),"guardian ledger idempotence");check(row.getString("ClaimReceipt").equals("kept")&&row.getString("Stage").equals(stage),"guardian stage/claim retained");
  }
  near(GorynychRules.HP,2800,"dragon logical HP");near(GorynychRules.TRANSFORM_HP,1500,"absolute transform");near(GorynychRules.FLOOR,75,"absolute exhaustion");near(Gorynych.attributes().build().getBaseValue(Attributes.MAX_HEALTH),1024,"dragon service cap retained");
  for(int state=0;state<=6;state++)for(int clean=0;clean<8;clean++)for(float hp:new float[]{3600,1800,1000,180}){
   var row=new CompoundTag();row.putInt("State",state);row.putInt("Clean",clean);row.putFloat("HP",hp);row.putBoolean("Wings",state>=3);row.putString("RewardReceipt","kept");
   check(GorynychRules.migrate(row,"HP","State"),"dragon first migration");near(row.getFloat("HP"),state==4||state==5?75:state==6?2800:Math.max(75,hp/3600*2800),"dragon scale with nonlethal states");check(row.getInt("State")==state&&row.getInt("Clean")==clean&&row.getString("RewardReceipt").equals("kept"),"dragon reached state/cleansing/reward retained");var copy=row.copy();check(!GorynychRules.migrate(row,"HP","State")&&copy.equals(row),"dragon second migration");
   var entity=new CompoundTag();entity.putInt("GorynychState",state);entity.putFloat("GorynychHP",hp);GorynychRules.migrate(entity,"GorynychHP","GorynychState");near(entity.getFloat("GorynychHP"),row.getFloat("HP"),"entity/ledger use identical policy");
  }
  var ledger=new GorynychLedger();var id=UUID.randomUUID();var hill=new CompoundTag();hill.putLong("Origin",42);ledger.ensure(id,hill);near(ledger.row(id).getFloat("HP"),2800,"new ledger HP");check(!GorynychRules.migrate(ledger.row(id),"HP","State"),"new encounters already marked");
  var legacy=ledger.save(new CompoundTag(),null);var old=legacy.getCompound("Encounters").getCompound(id.toString());old.remove(GorynychRules.BALANCE);old.putFloat("HP",1800);old.putInt("State",3);old.putInt("Clean",5);var loaded=GorynychLedger.load(legacy,null);near(loaded.row(id).getFloat("HP"),1400,"SavedData migration");check(loaded.row(id).getInt("State")==3&&loaded.row(id).getInt("Clean")==5,"SavedData state retained");var again=GorynychLedger.load(loaded.save(new CompoundTag(),null),null);check(again.row(id).equals(loaded.row(id)),"SavedData roundtrip idempotence");
  for(float hp:new float[]{2800,1500,76,75})for(float damage:new float[]{0,1,100,100000})check(GorynychRules.remaining(hp,damage)>=75,"overkill nonlethal floor");
  near(CreatureHealthBalance.proportional(0,400,700),0,"dead not resurrected");near(CreatureHealthBalance.proportional(Float.NaN,400,700),0,"invalid HP not resurrected");
  firebirdLifecycle();
  var night=org.slavicmyths.karachun.NightConfig.class.getDeclaredField("VALUES");night.setAccessible(true);var values=(Map<?,?>)night.get(null);near(((net.neoforged.neoforge.common.ModConfigSpec.DoubleValue)values.get("health")).getDefault(),1600,"Karachun config default");
  System.out.println("CREATURE_HEALTH_OFFLINE_PASS checks="+checks+"; native NBT/attributes/rules + limited firebird lifecycle stub; game/server/client launches=0; not a real relog or AI/audio test");
 }
}
