package org.slavicmyths.desert;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.combat.CombatTargeting;
import static org.slavicmyths.desert.AntRules.*;
public final class AntMinionEntity extends AntEntity {
 private final boolean flying;private UUID owner,locked;private int slot,rest,anger,orphan,flee;private boolean dispersing,fired;
 public AntMinionEntity(EntityType<? extends AntMinionEntity> t,Level w,boolean flying){super(t,w);this.flying=flying;xpReward=0;}
 public static AttributeSupplier.Builder attributes(boolean f){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,f?FLYING_HP:WORKER_HP).add(Attributes.ATTACK_DAMAGE,f?3:4).add(Attributes.ARMOR,0).add(Attributes.MOVEMENT_SPEED,f?.38:.32).add(Attributes.FOLLOW_RANGE,f?32:24);}
 @Override public int kind(){return flying?FLYING:WORKER;}
 public UUID ownerId(){return owner;}
 public boolean dispersing(){return dispersing;}
 public void bind(UUID owner,int slot,UUID target){this.owner=owner;this.slot=slot;locked=target;action(flying?TAKEOFF:EMERGE);setNoGravity(flying);}
 public void enrage(){anger=160;}
 public void disperse(){dispersing=true;flee=0;locked=null;setTarget(null);action(IDLE);}
 private boolean legal(LivingEntity t){return t!=null&&!(t instanceof AntEntity)&&distanceToSqr(t)<48*48&&CombatTargeting.canAttack(this,t,CombatTargeting.Context.DIRECT);}
 @Override public void tick(){super.tick();if(!isAlive()||!(level() instanceof ServerLevel w))return;if(rest>0)rest--;if(anger>0)anger--;
  if(dispersing){if(++flee>=160){discard();return;}if(flee%15==1){double a=random.nextDouble()*Math.PI*2;Vec3 p=position().add(Math.cos(a)*6,flying?2:0,Math.sin(a)*6);if(flying)flyTo(p,.28);else getNavigation().moveTo(p.x,p.y,p.z,1.3);}return;}
  AntQueenEntity queen=owner!=null&&w.getEntity(owner) instanceof AntQueenEntity q?q:null;
  if(owner!=null){if(queen==null){getNavigation().stop();setDeltaMovement(Vec3.ZERO);if(++orphan>100)disperse();return;}orphan=0;if(!queen.isAlive()||distanceToSqr(queen)>48*48){disperse();return;}}
  int a=action();if(a==EMERGE||a==TAKEOFF||a==LAND){if(a==TAKEOFF){setNoGravity(true);setDeltaMovement(0,.09,0);}else if(a==LAND){setNoGravity(false);}else getNavigation().stop();if(age()>=duration(a))action(IDLE);return;}
  if(a==BITE||a==DIVE){attack(w,a);return;}
  LivingEntity t=queen==null?getTarget():queen.getTarget();if(queen==null&&!legal(t)){LivingEntity attacker=getLastHurtByMob();if(legal(attacker))t=attacker;else if(tickCount%10==0)t=w.getEntitiesOfClass(Player.class,getBoundingBox().inflate(16),p->legal(p)&&hasLineOfSight(p)).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);}
  if(!legal(t)){setTarget(null);getNavigation().stop();if(flying){setNoGravity(false);if(!onGround())action(LAND);}return;}setTarget(t);turn(t.position());double d=distanceToSqr(t);
  if(flying&&onGround()){action(TAKEOFF);return;}
  if(rest==0&&hasLineOfSight(t)&&(flying?d<12:d<2.1)){locked=t.getUUID();fired=false;action(flying?DIVE:BITE);getNavigation().stop();playSound(AntSounds.ALERT.get(),.25F,1.7F);return;}
  double angle=slot*2.399963229728653+getUUID().getLeastSignificantBits()%13*.1;double speed=anger>0?1.3:1;
  if(flying){setNoGravity(true);flyTo(t.position().add(Math.cos(angle)*2.5,1.7+slot%3*.4,Math.sin(angle)*2.5),.38*speed);}
  else if(tickCount%6==slot%6){Vec3 point=d>4?t.position().add(Math.cos(angle)*1.2,0,Math.sin(angle)*1.2):t.position();getNavigation().moveTo(point.x,point.y,point.z,speed);}
 }
 private void flyTo(Vec3 p,double speed){Vec3 d=p.subtract(position());if(d.lengthSqr()>.01)setDeltaMovement(d.normalize().scale(speed));else setDeltaMovement(Vec3.ZERO);}
 private void attack(ServerLevel w,int a){LivingEntity t=locked!=null&&w.getEntity(locked) instanceof LivingEntity e?e:null;int age=age(),hit=a==DIVE?10:6,len=a==DIVE?16:12;
  getNavigation().stop();if(a==DIVE){setNoGravity(true);if(age<6)setDeltaMovement(Vec3.ZERO);else if(legal(t)&&age<=hit)flyTo(t.getEyePosition(),.65);else setDeltaMovement(0,.20,0);}
  if(!fired&&age>=hit){fired=true;if(legal(t)&&distanceToSqr(t)<(a==DIVE?4:2.25)&&hasLineOfSight(t)&&t.hurt(damageSources().mobAttack(this),(float)getAttributeValue(Attributes.ATTACK_DAMAGE))){Vec3 d=t.position().subtract(position());t.knockback(a==DIVE?1.35:.15,-d.x,-d.z);playSound(AntSounds.BITE.get(),.35F,1.6F);}}
  if(age>=len){action(IDLE);locked=null;rest=anger>0?(flying?36:18):(flying?60:30);}
 }
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(owner!=null)n.putUUID("AntOwner",owner);n.putInt("AntSlot",slot);n.putBoolean("AntDispersing",dispersing);n.putInt("AntFlee",flee);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);owner=n.hasUUID("AntOwner")?n.getUUID("AntOwner"):null;slot=Math.max(0,n.getInt("AntSlot"));dispersing=n.getBoolean("AntDispersing");flee=n.getInt("AntFlee");action(IDLE);}
}
