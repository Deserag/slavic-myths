package org.slavicmyths.netherboss;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.registry.*;

/** Server-owned attack geometry. Transient zones and waves never mutate terrain. */
public final class NetherBossCombat {
 public static final int BREATH=1,STRIKE=2,VEIL=3,PLAGUE_ZONES=4,SHADOWS=5,COPIES=6,TRIPLE_WAVE=7,SPLIT=8,DASH=9,DRUZHINA=10,FIRE=11,RING=12;
 private final NetherSummonBoss boss;
 private int sequence;
 private final List<Zone> zones=new ArrayList<>();
 private final List<Wave> waves=new ArrayList<>();
 private final Set<UUID> retainers=new HashSet<>();
 private record Zone(Vec3 center,int remaining){}
 private static final class Wave {
  Vec3 center;int age,delay;float damage;final Set<UUID> hit=new HashSet<>();
  Wave(Vec3 center,int delay,float damage){this.center=center;this.delay=delay;this.damage=damage;}
 }
 public NetherBossCombat(NetherSummonBoss boss){this.boss=boss;}
 public int choose(){
  int[] pool=boss.isMorova()?(boss.phase()==1?new int[]{BREATH,VEIL,PLAGUE_ZONES}:boss.phase()==2?new int[]{BREATH,VEIL,PLAGUE_ZONES,SHADOWS,COPIES}:new int[]{BREATH,VEIL,SHADOWS,TRIPLE_WAVE})
   :(boss.phase()==1?new int[]{STRIKE,SPLIT,DASH}:boss.phase()==2?new int[]{STRIKE,SPLIT,DRUZHINA,FIRE}:new int[]{STRIKE,DASH,DRUZHINA,FIRE,RING});
  return pool[Math.floorMod(sequence++,pool.length)];
 }
 private List<LivingEntity> targets(double radius){
  return boss.level().getEntitiesOfClass(LivingEntity.class,boss.getBoundingBox().inflate(radius,3,radius),e->e!=boss&&e.isAlive()
   &&!(e instanceof NetherRetainer r&&boss.getUUID().equals(r.owner()))&&!e.isAlliedTo(boss)
   &&(e==boss.getTarget()||e instanceof Player p&&!p.isCreative()&&!p.isSpectator()));
 }
 private boolean hit(LivingEntity target,float damage){return boss.hasLineOfSight(target)&&target.hurt(boss.damageSources().mobAttack(boss),damage);}
 public void resolve(int action,Vec3 aim){
  if(!(boss.level() instanceof ServerLevel world))return;
  boss.playSound(NetherBossSounds.cue(boss.isMorova(),switch(action){case PLAGUE_ZONES,FIRE->"zone";case SHADOWS,COPIES,DRUZHINA->"summon";case TRIPLE_WAVE,RING->"wave";default->"impact";}),1F,1F);
  switch(action){
   case BREATH -> {for(var t:targets(6))if(t.distanceToSqr(boss)<=36&&hit(t,8))t.addEffect(new MobEffectInstance(ModEffects.MOROVA_PLAGUE,200,boss.phase()-1),boss);}
   case STRIKE -> {for(var t:targets(4))if(t.distanceToSqr(boss)<=16)hit(t,16);}
   case VEIL -> {for(var t:targets(12)){
    Vec3 offset=t.position().subtract(boss.position());double distance=offset.horizontalDistance();
    if(distance<=12&&offset.normalize().dot(aim)>=.707&&hit(t,12))t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,100,0),boss);
   }}
   case PLAGUE_ZONES,FIRE -> {
    zones.clear();var target=boss.getTarget();Vec3 center=target==null?boss.position():target.position();
    for(int i=0;i<3;i++){double angle=i*Math.PI*2/3;zones.add(new Zone(center.add(Math.cos(angle)*2,0,Math.sin(angle)*2),100));}
   }
   case SHADOWS -> summon(2+boss.getRandom().nextInt(3),4,30,5);
   case DRUZHINA -> summon(2,4,50,7);
   case COPIES -> boss.showCopies();
   case TRIPLE_WAVE -> {for(int delay:new int[]{0,16,32})waves.add(new Wave(boss.position(),delay,10));}
   case RING -> waves.add(new Wave(boss.position(),0,18));
   case SPLIT -> {for(var t:targets(12)){
    Vec3 delta=t.position().subtract(boss.position());double along=delta.dot(aim);
    if(along>=0&&along<=12&&delta.subtract(aim.scale(along)).horizontalDistance()<=1)hit(t,22);
   }}
   case DASH -> {
    // Native collision resolution; no teleport, chunk request or movement through a wall.
    Vec3 start=boss.position();boss.move(MoverType.SELF,aim.scale(6));Vec3 travel=boss.position().subtract(start);double squared=travel.lengthSqr();
    for(var t:targets(8)){
     double fraction=squared==0?0:Math.clamp(t.position().subtract(start).dot(travel)/squared,0,1);
     if(t.position().distanceToSqr(start.add(travel.scale(fraction)))<=2.25)hit(t,14);
    }
   }
  }
 }
 private void summon(int number,int cap,float hp,float damage){
  ServerLevel world=(ServerLevel)boss.level();retainers.removeIf(id->world.getEntity(id) instanceof NetherRetainer e&&!e.isAlive());
  int wanted=Math.min(number,cap-retainers.size());if(wanted<2)return;
  var planned=NetherRetainer.prepare(boss,wanted,hp,damage);if(planned.size()<2)return;
  for(var minion:planned){minion.setTarget(boss.getTarget());if(world.addFreshEntity(minion))retainers.add(minion.getUUID());}

 }
 public void tick(){
  if(!(boss.level() instanceof ServerLevel world))return;
  for(int i=zones.size()-1;i>=0;i--){var z=zones.get(i);if(z.remaining<=0){zones.remove(i);continue;}
   if(boss.tickCount%5==0)world.sendParticles(boss.isMorova()?NetherSummonBoss.plagueParticle():ParticleTypes.FLAME,z.center.x,z.center.y+.1,z.center.z,8,1,.1,1,0);
   zones.set(i,new Zone(z.center,z.remaining-1));
  }
  if(boss.tickCount%20==0&&!zones.isEmpty())for(var t:targets(20)){
   boolean inside=zones.stream().anyMatch(z->Math.abs(t.getY()-z.center.y)<3&&t.position().subtract(z.center).horizontalDistance()<=2);
   if(inside&&boss.hasLineOfSight(t))t.hurt(boss.isMorova()?boss.damageSources().magic():boss.damageSources().inFire(),boss.isMorova()?3:4);
  }
  for(int i=waves.size()-1;i>=0;i--){var wave=waves.get(i);if(wave.delay-->0)continue;
   double radius=++wave.age*.4;if(radius>12){waves.remove(i);continue;}
   for(int p=0;p<32;p++){double a=p*Math.PI/16;world.sendParticles(boss.isMorova()?NetherSummonBoss.plagueParticle():ParticleTypes.FLAME,wave.center.x+Math.cos(a)*radius,wave.center.y+.2,wave.center.z+Math.sin(a)*radius,1,0,0,0,0);}
   for(var t:targets(16)){double r=t.position().subtract(wave.center).horizontalDistance();
    if(Math.abs(r-radius)<=.7&&Math.abs(t.getY()-wave.center.y)<=2&&!wave.hit.contains(t.getUUID())&&hit(t,wave.damage))wave.hit.add(t.getUUID());
   }
  }
 }
 public void release(UUID id){retainers.remove(id);}
 public void clear(){var ids=List.copyOf(retainers);retainers.clear();if(boss.level() instanceof ServerLevel world)for(var id:ids){var entity=world.getEntity(id);if(entity!=null)entity.discard();}zones.clear();waves.clear();}
 public void save(CompoundTag root){
  root.putInt("BossSequence",sequence);var list=new ListTag();
  for(var z:zones){var n=new CompoundTag();n.putDouble("X",z.center.x);n.putDouble("Y",z.center.y);n.putDouble("Z",z.center.z);n.putInt("Life",z.remaining);list.add(n);}root.put("BossZones",list);
  list=new ListTag();for(var id:retainers){var n=new CompoundTag();n.putUUID("Id",id);list.add(n);}root.put("BossRetainers",list);
  // Interrupted waves are discarded on reload, never replayed as fresh damage.
 }
 public void load(CompoundTag root){sequence=root.getInt("BossSequence");zones.clear();waves.clear();retainers.clear();
  for(var raw:root.getList("BossZones",10)){if(zones.size()>=3)break;var n=(CompoundTag)raw;zones.add(new Zone(new Vec3(n.getDouble("X"),n.getDouble("Y"),n.getDouble("Z")),Math.clamp(n.getInt("Life"),0,100)));}
  for(var raw:root.getList("BossRetainers",10)){if(retainers.size()>=4)break;var n=(CompoundTag)raw;if(n.hasUUID("Id"))retainers.add(n.getUUID("Id"));}
 }
}
