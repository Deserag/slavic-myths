package org.slavicmyths.underground;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
public final class Kladovik extends RoomBoss {
 private Vec3 emerge,start;private boolean safeEmerge,doubleThrow;private int lost;private final List<BlockPos> spikePoints=new ArrayList<>();
 public Kladovik(EntityType<? extends Monster> t,Level l){super(t,l);}
 protected boolean neutral(){return true;}
 public void aiStep(){if(!level().isClientSide&&home!=null&&action()==Action.IDLE&&getTarget()==null&&tickCount%20==0){var t=level().getNearestPlayer(getX(),getY(),getZ(),7,p->p instanceof Player player&&!player.isCreative()&&!p.isSpectator()&&p.isAlive()&&Math.abs(p.getY()-home.getY())<1.5&&hasLineOfSight(p));if(t!=null){setTarget(t);begin(Action.ALERT,20);}}
  super.aiStep();if(!level().isClientSide){if(getTarget()==null||!hasLineOfSight(getTarget()))lost++;else lost=0;if(lost>=300){setTarget(null);if(home!=null)getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,.8);}if(getHealth()<=100&&!raged()){rage();getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.3);sound("rage",1);}}
 }
 protected void choose(){double d=distanceTo(getTarget());if(d<4&&ready(Action.COIN_WAVE))begin(Action.COIN_WAVE,25);else if(d>=2&&d<=9&&ready(Action.SPIKES)){spikePoints.clear();var dir=aimDirection(getTarget().position());for(int n=2;n<=6;n+=2){var p=BlockPos.containing(position().add(dir.scale(n)));if(safe(p)&&hasClearLine(position().add(0,.5,0),p.getCenter()))spikePoints.add(p);}if(!spikePoints.isEmpty())begin(Action.SPIKES,38);}
  else if(d>=3&&d<=12&&ready(Action.BURROW)){start=position();emerge=start;safeEmerge=false;for(int i=0;i<12;i++){double angle=i*Math.PI/6;var p=BlockPos.containing(getTarget().position().add(Math.cos(angle)*2.5,0,Math.sin(angle)*2.5));var path=getNavigation().createPath(p,0);if(safe(p)&&Math.abs(p.getY()-getY())<.6&&hasClearLine(position().add(0,.8,0),p.getCenter())&&path!=null&&path.canReach()){emerge=new Vec3(p.getX()+.5,p.getY(),p.getZ()+.5);safeEmerge=true;break;}}begin(Action.BURROW,90);}
  else if(d>=5&&d<=16&&ready(Action.THROW)){doubleThrow=raged()&&random.nextFloat()<.25;begin(Action.THROW,doubleThrow?38:28);}else if(d<2.5&&ready(Action.MELEE))begin(Action.MELEE,24);
 }
 private Vec3 aimDirection(Vec3 p){return new Vec3(p.x-getX(),0,p.z-getZ()).normalize();}
 private boolean hasClearLine(Vec3 a,Vec3 b){return level().clip(new ClipContext(a,b,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;}
 protected void step(){switch(action()){
  case MELEE->{if(elapsed==7)melee(6,2.5);if(elapsed==duration)finish(Action.MELEE,8,8);}
  case THROW->{if(elapsed==16||doubleThrow&&elapsed==24){throwStone();sound("stone",1);}if(elapsed==duration)finish(Action.THROW,100,10);}
  case BURROW->{if(elapsed==1)sound("burrow",1);if(elapsed<20&&elapsed%5==0)dust(position(),2);if(elapsed==20)noPhysics=true;if(elapsed>=20&&elapsed<60){if(safeEmerge){Vec3 p=start.lerp(emerge,Math.min(1,(elapsed-20)/30.0));setPos(p.x,p.y,p.z);}if(elapsed%5==0)dust(position(),2);}if(elapsed>=60&&elapsed<75&&elapsed%5==0)dust(position(),4);if(elapsed==75){noPhysics=false;sound("impact",1);}if(elapsed==80&&safeEmerge){for(var t:victims(position(),1.6)){if(t.hurt(damageSources().mobAttack(this),9))t.push(0,.28,0);}dust(position(),8);}if(elapsed==duration){noPhysics=false;finish(Action.BURROW,raged()?200:270,20);}}
  case SPIKES->{if(elapsed>=8&&elapsed<26&&elapsed%6==0)for(var p:spikePoints)dust(p.getCenter().add(0,-.5,0),2);if(elapsed==26){Set<UUID> hit=new HashSet<>();for(var p:spikePoints){var fx=UndergroundMobs.SPIKE.get().create(level());if(fx!=null){fx.owner=getUUID();fx.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,0,0);level().addFreshEntity(fx);}for(var t:victims(new Vec3(p.getX()+.5,p.getY(),p.getZ()+.5),.85))if(hit.add(t.getUUID())){t.hurt(damageSources().mobAttack(this),6);t.push(0,.2,0);t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,20,0));}}sound("impact",1);}if(elapsed==duration)finish(Action.SPIKES,250,16);}
  case COIN_WAVE->{if(elapsed==1){sound("coins",.8F);ring(4,20);}if(elapsed==20){for(var t:victims(position(),4)){t.hurt(damageSources().mobAttack(this),1);Vec3 v=t.position().subtract(position()).normalize();t.push(v.x*.8,.15,v.z*.8);t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,0));}dust(position(),24);}if(elapsed==duration)finish(Action.COIN_WAVE,raged()?240:320,16);}
  default->{}
 }}
 private void throwStone(){var t=getTarget();if(!validTarget(t)||!hasLineOfSight(t))return;var stone=new ThrownStone(level(),this);Vec3 from=position().add(getLookAngle().scale(.55)).add(0,getBbHeight()*.72,0);stone.setPos(from);Vec3 v=t.getEyePosition().subtract(from);stone.shoot(v.x,v.y+v.length()*.035,v.z,.9F,1);level().addFreshEntity(stone);}
 public boolean isPickable(){return action()!=Action.BURROW||age(0)<20||age(0)>=75;}
 public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource d){return action()==Action.BURROW&&age(0)>=20&&age(0)<75&&!d.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)||super.isInvulnerableTo(d);}
 protected void cancel(){super.cancel();if(start!=null&&safe(BlockPos.containing(start)))setPos(start);}
 public void die(net.minecraft.world.damagesource.DamageSource d){if(!level().isClientSide){for(var s:level().getEntitiesOfClass(ThrownStone.class,getBoundingBox().inflate(40),s->s.getOwner()==this))s.discard();for(var s:level().getEntitiesOfClass(StoneSpike.class,getBoundingBox().inflate(40),s->getUUID().equals(s.owner)))s.discard();}super.die(d);}
}
