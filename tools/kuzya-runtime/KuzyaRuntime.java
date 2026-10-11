package org.slavicmyths.verify;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.kuzya.*;
import org.slavicmyths.yaga.*;
import java.util.*;

@GameTestHolder("slavicmyths_kuzya")
@PrefixGameTestTemplate(false)
public final class KuzyaRuntime {
 @GameTest(template="empty",timeoutTicks=2400)
 public static void household(GameTestHelper t)throws Exception {
  var w=t.getLevel();BlockPos site=t.absolutePos(new BlockPos(24,0,24));int floor=site.getY();
  for(int x=-12;x<=12;x++)for(int z=-18;z<=12;z++){var p=new BlockPos(site.getX()+x,floor,site.getZ()+z);w.setBlockAndUpdate(p,Blocks.GRASS_BLOCK.defaultBlockState());for(int y=1;y<=15;y++)w.setBlockAndUpdate(p.above(y),Blocks.AIR.defaultBlockState());}
  var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(w);player.moveTo(site.getX()+.5,floor+1,site.getZ()-20+.5,0,0);
  var source=player.createCommandSourceStack().withPermission(2);int generated=w.getServer().getCommands().getDispatcher().execute("slavicmyths dev yaga generate",source);t.assertTrue(generated==1,"registered generation command succeeds");
  var data=YagaData.get(w);var k=(KuzyaEntity)w.getEntity(data.kuzya);t.assertTrue(k!=null&&k.homeHutId.equals(data.hutId),"bound canonical resident");k.renovated=false;
  t.assertTrue(!k.hurt(w.damageSources().generic(),1000)&&k.getHealth()==20,"actual hurt suppressed");k.knockback(5,1,1);k.push(1,1,1);t.assertTrue(k.getDeltaMovement().lengthSqr()<.001,"no knockback/push");
  t.assertTrue(!k.canBeLeashed()&&!k.startRiding(k,true)&&!k.canUsePortal(false),"no leash/vehicle/portal");
  t.assertTrue(w.getEntitiesOfClass(KuzyaEntity.class,k.homeBounds()).size()==1,"one resident");t.assertTrue(!YagaHut.tryPlace(w,site).ok,"second generation rejected");
  var barrel=(net.minecraft.world.level.block.entity.BarrelBlockEntity)w.getBlockEntity(data.anchor.offset(-4,4,5));t.assertTrue(barrel!=null&&barrel.getLootTable()!=null,"real vanilla loot barrel");barrel.unpackLootTable(null);t.assertTrue(!barrel.isEmpty()&&barrel.getLootTable()==null,"loot resolves once");
  var cloned=KuzyaContent.ENTITY.get().create(w);cloned.bind(k.home,data.hutId);cloned.moveTo(k.position());t.assertTrue(!w.addFreshEntity(cloned),"canonical manager rejects clone");
  var tag=new net.minecraft.nbt.CompoundTag();k.addAdditionalSaveData(tag);t.assertTrue(tag.getInt("data_version")==2&&tag.hasUUID("HomeHutId"),"persistent residence schema");
  var travel=KuzyaEntity.class.getDeclaredMethod("travel",String.class,Vec3[].class);travel.setAccessible(true);BlockPos h=k.home;
  Vec3[] points={new Vec3(h.getX()+.5,h.getY()+4,h.getZ()+3.5),new Vec3(h.getX()+1.5,h.getY()+4.5,h.getZ()+3.5),new Vec3(h.getX()+2.5,h.getY()+5,h.getZ()+3.5),new Vec3(h.getX()+2.5,h.getY()+5.5,h.getZ()+4.5),new Vec3(h.getX()+3.5,h.getY()+6,h.getZ()+4.5)};
  var due=KuzyaEntity.class.getDeclaredField("nextActivity");due.setAccessible(true);long initialDue=due.getLong(k);Vec3 initialPosition=k.position();boolean[] active={false};
  for(int at=40;at<=180;at+=10)t.runAtTickTime(at,()->{if(!k.clip().equals("idle_stand"))active[0]=true;});
  t.assertTrue(initialDue<=w.getGameTime()+61,"first household activity within ten seconds");
  t.runAtTickTime(20,()->{player.moveTo(k.getX()+1,k.getY(),k.getZ());t.assertTrue(k.interact(player,net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(),"actual right-click handled");});
  t.runAtTickTime(120,()->{try{t.assertTrue(k.voice()>=0&&k.voice()<KuzyaAssets.VOICES.length&&KuzyaAssets.VOICES[k.voice()].startsWith("voice_original_"),"real interaction selects original clip");}catch(Exception e){throw new RuntimeException(e);}});
  t.runAtTickTime(180,()->{t.assertTrue(active[0],"autonomous activity starts without scripted travel: "+k.clip());System.out.println("KUZYA AUTONOMOUS PASS clip="+k.clip());});
  t.runAtTickTime(200,()->{try{k.renovated=true;travel.invoke(k,"stove",(Object)points);}catch(Exception e){throw new RuntimeException(e);}});
  t.runAtTickTime(250,()->{var savedRoute=new net.minecraft.nbt.CompoundTag();k.addAdditionalSaveData(savedRoute);var copy=KuzyaContent.ENTITY.get().create(w);copy.moveTo(k.position());copy.readAdditionalSaveData(savedRoute);var check=new net.minecraft.nbt.CompoundTag();copy.addAdditionalSaveData(check);t.assertTrue(check.getList("Route",10).equals(savedRoute.getList("Route",10))&&check.getInt("Waypoint")==savedRoute.getInt("Waypoint"),"in-progress route and waypoint survive save/load");});
  for(int time:new int[]{260,320,440,540})t.runAtTickTime(time,()->System.out.println("KUZYA ROUTE tick="+k.tickCount+" pos="+k.position()+" speed="+k.getSpeed()+" delta="+k.getDeltaMovement()+" path="+k.getNavigation().getPath()+" done="+k.getNavigation().isDone()));
  t.runAtTickTime(550,()->{t.assertTrue(k.getY()>=h.getY()+5.9,"physical stove navigation, y="+k.getY()+" clip="+k.clip()+" pos="+k.position());t.assertTrue(k.clip().equals("sleep_loop")||k.clip().equals("lie_idle"),"finished safe climb and lie/sleep sequence: "+k.clip());t.assertTrue(w.getEntitiesOfClass(KuzyaEntity.class,k.homeBounds()).size()==1,"no duplicate across 550 ticks");
   var saved=new net.minecraft.nbt.CompoundTag();k.addAdditionalSaveData(saved);
   var resumed=KuzyaContent.ENTITY.get().create(w);resumed.moveTo(k.position());resumed.readAdditionalSaveData(saved);
   t.assertTrue(resumed.clip().equals(k.clip()),"sleep pose survives NBT roundtrip");
   saved.putLong("SceneSavedAt",w.getGameTime()-12000);resumed.readAdditionalSaveData(saved);resumed.tick();
   t.assertTrue(resumed.clip().equals("wake_up"),"elapsed unloaded sleep ends without forced chunks");
   var old=new net.minecraft.nbt.CompoundTag();old.putInt("data_version",1);old.putLong("HomeOrigin",h.asLong());old.putUUID("HomeHutId",data.hutId);
   resumed.moveTo(initialPosition);resumed.readAdditionalSaveData(old);
   try{t.assertTrue(due.getLong(resumed)<=w.getGameTime(),"legacy resident activates immediately");}catch(Exception e){throw new RuntimeException(e);}
   old.putInt("data_version",2);old.putString("SceneClip","broken_clip");resumed.readAdditionalSaveData(old);t.assertTrue(resumed.clip().equals("idle_stand"),"invalid saved clip recovers safely");
   System.out.println("KUZYA SCENE PERSISTENCE PASS");
   try{var begin=KuzyaEntity.class.getDeclaredMethod("begin",String.class);begin.setAccessible(true);begin.invoke(k,"wake_up");}catch(Exception e){throw new RuntimeException(e);}
   k.renovated=false;});
  Set<Integer> workSpots=new HashSet<>();
  for(int at=850;at<=2250;at+=20)t.runAtTickTime(at,()->{try{var last=KuzyaEntity.class.getDeclaredField("lastChore");last.setAccessible(true);if(k.clip().equals("domestic_sweep"))workSpots.add(last.getInt(k));}catch(Exception e){throw new RuntimeException(e);}});
  t.runAtTickTime(2300,()->{t.assertTrue(k.getY()<h.getY()+5,"physical return from stove");t.assertTrue(workSpots.size()>=2,"autonomous chores reach multiple real spots: "+workSpots);System.out.println("KUZYA MULTI SPOT PASS "+workSpots);t.succeed();});
 }
}
