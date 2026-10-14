package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.combat.*;
import org.slavicmyths.poloz.*;
import org.slavicmyths.karachun.*;
import org.slavicmyths.gorynych.*;
@GameTestHolder("slavicmyths_creature_feedback") @PrefixGameTestTemplate(false)
public final class CreatureFeedbackTests {
    private static final class Connection extends net.minecraft.network.Connection {
        private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
        Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}@Override public io.netty.channel.Channel channel(){return channel;}
    }
    private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
        Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
        @Override public void send(net.minecraft.network.protocol.Packet<?> p){}@Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener l){}
    }
 private static ServerPlayer player(GameTestHelper t){var w=t.getLevel();var p=new ServerPlayer(w.getServer(),w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"CreatureCheck"),ClientInformation.createDefault());p.connection=new Listener(p);p.setPos(t.absoluteVec(new Vec3(16,3,16)));p.setNoGravity(true);return p;}
 private static Object field(Object e,String name){try{var f=e.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(e);}catch(Exception ex){throw new RuntimeException(ex);}}
 private static void call(Object e,String name){try{var m=e.getClass().getDeclaredMethod(name);m.setAccessible(true);m.invoke(e);}catch(Exception ex){throw new RuntimeException(ex);}}
 private static void bite(Gorynych d,ServerPlayer p){((int[])field(d,"headRest"))[0]=999;((int[])field(d,"headRest"))[1]=999;((int[])field(d,"headAction"))[2]=2;((int[])field(d,"headAge"))[2]=31;((LivingEntity[])field(d,"headTarget"))[2]=p;((float[])field(d,"pitch"))[2]=45;call(d,"positionHeads");}
 private static int checks;
 static void check(GameTestHelper t,boolean ok,String name){t.assertTrue(ok,name);System.out.println("CREATURE_FEEDBACK_PASS "+(++checks)+" "+name);}
 static Entity make(ServerLevel w,String name){return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("slavicmyths:"+name)).create(w);}
 @GameTest(template="empty",timeoutTicks=600) public static void mechanics(GameTestHelper t){
  var w=t.getLevel();w.getServer().setDifficulty(Difficulty.NORMAL,true);
  var origin=t.absoluteVec(new Vec3(12,3,12));
  var p=(GreatPoloz)make(w,"great_poloz");p.setPos(origin);p.setNoAi(true);w.addFreshEntity(p);p.initializeBody();
  check(t,p.getMaxHealth()==3000&&p.getHealth()==3000,"Poloz 3000 HP");
  var attacker=EntityType.ZOMBIE.create(w);attacker.setNoAi(true);attacker.setPos(origin);w.addFreshEntity(attacker);
  for(int phase=1;phase<=3;phase++){
   p.setPhaseForTest(phase);float before=p.getHealth();
   check(t,!p.hurt(attacker.damageSources().mobAttack(attacker),100)&&p.getHealth()==before,"root armor phase "+phase);
   var arrow=EntityType.ARROW.create(w);arrow.setPos(p.parts[24].getBoundingBox().getCenter());arrow.setDeltaMovement(.1,0,0);
   var source=attacker.damageSources().arrow(arrow,attacker);
   // Different owner per contact avoids the intentional six-tick per-attacker gate.
   var archer=EntityType.SKELETON.create(w);archer.setPos(origin);source=archer.damageSources().arrow(arrow,archer);
   check(t,p.hitPart(p.parts[24],source,100)&&p.getHealth()<before&&before-p.getHealth()<=6.01,"actual scar contact and ordinary mob cap phase "+phase);
   arrow.discard();archer.discard();
  }
  var player=player(t);player.setGameMode(GameType.CREATIVE);w.addNewPlayer(player);
  check(t,!p.validVictim(player)&&!CombatTargeting.canAttack(p,player),"Creative excluded from Poloz/common targeting");
  p.setTarget(player);check(t,p.getTarget()==null,"target event rejects Creative");
  float safeHealth=player.getHealth();player.hurt(p.damageSources().mobAttack(p),100);check(t,player.getHealth()==safeHealth,"Creative takes no delayed damage");
  var dragon=(Gorynych)make(w,"zmey_gorynych");check(t,!dragon.candidate(player),"Gorynych excludes Creative owner");
  var nightProbe=(Karachun)make(w,"karachun");nightProbe.owner=player.getUUID();check(t,!nightProbe.candidate(player),"Night excludes Creative trial owner");
  player.setGameMode(GameType.SPECTATOR);check(t,!p.validVictim(player)&&!CombatTargeting.canControl(p,player),"Spectator excluded from control");
  NightStatus.add(player,UUID.randomUUID(),30,90,true);check(t,!player.getPersistentData().contains(NightStatus.KEY),"Spectator receives no fear/control status");
  player.setGameMode(GameType.SURVIVAL);try{var f=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");f.setAccessible(true);f.setInt(player,0);}catch(Exception ex){throw new RuntimeException(ex);}
  var record=new CompoundTag();record.putLong("Origin",BlockPos.containing(origin).asLong());record.putBoolean("Standalone",true);record.putInt("State",GorynychRules.GROUND);record.putFloat("HP",2800);record.putInt(GorynychRules.BALANCE,GorynychRules.BALANCE_VERSION);dragon.setPos(origin);dragon.configure(UUID.randomUUID(),record);
  player.setPos(origin.add(0,0,10));bite(dragon,player);float beforeBite=player.getHealth();call(dragon,"headTick");check(t,field(dragon,"grabbed")==null&&player.getHealth()==beforeBite,"Gorynych cannot grab without head contact");
  bite(dragon,player);player.setPos(origin.add(GorynychPose.offset(2,0,0,43,dragon.tickCount,false,false,false,2,32)).add(0,-.2,0));call(dragon,"headTick");check(t,field(dragon,"grabbed")==player&&player.getHealth()<beforeBite,"contact bite damages before confirmed grab");
  player.setGameMode(GameType.CREATIVE);dragon.cancelTarget(player);check(t,field(dragon,"grabbed")==null,"Creative mode releases Gorynych grab");
  player.discard();dragon.discard();nightProbe.discard();
  var cow=EntityType.COW.create(w);cow.setPos(p.parts[8].position().add(3,0,0));w.addFreshEntity(cow);attacker.discard();
  check(t,p.validVictim(cow),"passive animal is Poloz candidate");
  for(int skill=1;skill<=5;skill++)check(t,PolozRules.damage(skill)>0,"attack budget skill "+skill);
  p.setPhaseForTest(1);float cowBefore=cow.getHealth();UUID volley=UUID.randomUUID();for(int i=0;i<20;i++){cow.invulnerableTime=0;p.rainDamage(cow,3,volley);}check(t,cowBefore-cow.getHealth()<=PolozRules.damage(3)+.01,"rain per-victim cap");
  var guard=(org.slavicmyths.industry.ChudPerson)make(w,"chud_guard");
  check(t,guard.getMainHandItem().is(Items.IRON_AXE),"guard constructor weapon");
  var n=new CompoundTag();guard.saveWithoutId(n);var restored=(org.slavicmyths.industry.ChudPerson)make(w,"chud_guard");restored.load(n);
  check(t,restored.getMainHandItem().is(Items.IRON_AXE),"guard weapon save/load");
  n.remove("HandItems");restored.load(n);check(t,restored.getMainHandItem().is(Items.IRON_AXE),"old empty guard repaired");
  var fearTarget=EntityType.COW.create(w);fearTarget.setNoAi(true);fearTarget.setPos(origin.add(0,2,0));w.addFreshEntity(fearTarget);UUID fear=UUID.randomUUID(),other=UUID.randomUUID();
  for(int i=0;i<90;i++)NightStatus.add(fearTarget,fear,0,1,false);
  check(t,NightStatus.value(fearTarget,"Fear")==90&&NightRules.phantoms(NightStatus.visualFear(fearTarget))==3,"fear builds to three phantoms");
  NightStatus.add(fearTarget,other,0,10,false);NightStatus.reduceFear(fearTarget,fear);check(t,NightStatus.value(fearTarget,"Fear")==84,"successful hit weakens fear");
  var row=fearTarget.getPersistentData().getCompound(NightStatus.KEY).getCompound(fear.toString());row.putDouble("Fear",65);NightStatus.add(fearTarget,fear,0,0,false);check(t,NightStatus.visualFear(fearTarget)==75,"fear demotion grace holds tier");
  row.putLong("FearFallAt",w.getGameTime()-41);NightStatus.add(fearTarget,fear,0,0,false);check(t,NightStatus.visualFear(fearTarget)==65,"fear tier demotes after grace");
  NightStatus.clear(fearTarget,fear);check(t,NightStatus.value(fearTarget,"Fear")==10,"source-local fear cleanup preserves second source");
  for(int h=0;h<3;h++)for(int a:new int[]{0,24,30,34,52})check(t,Double.isFinite(GorynychPose.offset(h,0,0,0,a,false,false,false,1,a).x),"head finite pose "+h+"/"+a);
  check(t,!GorynychPose.contact(2,24)&&GorynychPose.contact(2,32)&&!GorynychPose.contact(2,40),"bite contact restricted to lunge");
  var night=(Karachun)make(w,"karachun");night.session=UUID.randomUUID();night.owner=UUID.randomUUID();night.setPos(origin);NightLedger.get(w).session(night.session).putUUID("Owner",night.owner);w.addFreshEntity(night);
  var frost=(org.slavicmyths.morozko.Morozko)make(w,"morozko");frost.session=UUID.randomUUID();frost.owner=UUID.randomUUID();frost.setPos(origin);org.slavicmyths.morozko.FrostLedger.get(w).session(frost.session).putUUID("Owner",frost.owner);w.addFreshEntity(frost);
  var civilian=(Mob)make(w,"chud_resident");civilian.setPos(origin);w.addFreshEntity(civilian);
  w.getServer().setDifficulty(Difficulty.PEACEFUL,true);
  t.runAfterDelay(5,()->{
   check(t,p.isRemoved()&&night.isRemoved()&&frost.isRemoved(),"Peaceful removes Poloz/Night/Frost without death");
   check(t,NightLedger.get(w).session(night.session).getBoolean("Cancelled")&&!NightLedger.get(w).player(night.owner).getBoolean("Completed"),"Night cancelled without victory");
   check(t,org.slavicmyths.morozko.FrostLedger.get(w).session(frost.session).getBoolean("Cancelled"),"Frost cancelled safely");
   var hostile=(Mob)make(w,"cursed_volkolak");hostile.setPos(origin);check(t,!w.addFreshEntity(hostile),"Peaceful blocks new hostile spawn");
   check(t,!civilian.isRemoved(),"Peaceful preserves civilian");
   int rejected=0;for(var type:BuiltInRegistries.ENTITY_TYPE){if(!BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace().equals("slavicmyths"))continue;var e=type.create(w);if(e instanceof Mob mob&&CreatureSafety.hostile(mob)){mob.setPos(origin);check(t,!w.addFreshEntity(mob),"Peaceful rejects "+BuiltInRegistries.ENTITY_TYPE.getKey(type));rejected++;}else if(e!=null)e.discard();}check(t,rejected>30,"registered hostile matrix covered");
   civilian.discard();cow.discard();fearTarget.discard();p.discard();guard.discard();restored.discard();w.getServer().setDifficulty(Difficulty.NORMAL,true);t.succeed();
  });
 }
}
