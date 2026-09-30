package org.slavicmyths.water;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.slavicmyths.progression.Knowledge;
public final class RusalkaEntity extends WaterSpirit {
 private int watching;private long silentUntil;
 public RusalkaEntity(EntityType<? extends RusalkaEntity> t,World w){super(t,w);}
 public static AttributeModifierMap.MutableAttribute attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,28).add(Attributes.MOVEMENT_SPEED,.23).add(Attributes.ATTACK_DAMAGE,3).add(Attributes.FOLLOW_RANGE,20);}
 public boolean singing(){return state()==2||state()==4;}
 protected void think(){if(home==null)home=blockPosition();if(getTarget()!=null){state(3);return;}PlayerEntity p=nearby(20);if(p==null){watching=0;state(0);wander();return;}Knowledge.award(p,"meet_rusalka");getLookControl().setLookAt(p,15,15);if(level.getGameTime()<silentUntil){state(1);return;}watching+=20;if(watching<100){state(1);return;}state(2);boolean lured=false;
  for(PlayerEntity listener:level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(20),q->q.isAlive()&&!q.isCreative()&&!q.isSpectator()&&distanceToSqr(q)<=400)){RusalkaSong.expose(listener,this);if(listener.getPersistentData().getFloat("SlavicWaterCharm")>=50)lured=true;if(distanceToSqr(listener)<6.25){provoke(listener);silentUntil=level.getGameTime()+80;return;}}
  if(lured)state(4);
 }
 public boolean hurt(DamageSource source,float amount){boolean hit=super.hurt(source,amount);if(hit&&!level.isClientSide){silentUntil=level.getGameTime()+120;watching=0;state(3);}return hit;}
 public boolean doHurtTarget(Entity e){boolean hit=super.doHurtTarget(e);if(hit&&isInWater()&&level.getGameTime()>=nextPower){nextPower=level.getGameTime()+100;e.setDeltaMovement(e.getDeltaMovement().add(0,-.07,0));e.hurtMarked=true;}return hit;}
}
