package org.slavicmyths.morozko;
import net.minecraft.world.entity.*;
import net.minecraft.server.level.ServerLevel;
public final class IceDouble extends Morozko {
 public IceDouble(EntityType<? extends IceDouble> t,net.minecraft.world.level.Level w){super(t,w);setHealth(FrostRules.DOUBLE_HP);}
 public void dissolve(){if(phase()!=6){entityData.set(PHASE,6);entityData.set(AGE,0);getNavigation().stop();FrostContent.sound(this,"clone_break");}}
 public Morozko boss(){return level() instanceof ServerLevel w&&owner!=null&&w.getEntity(owner) instanceof Morozko b&&!(b instanceof IceDouble)?b:null;}
 @Override public boolean fighting(){var b=boss();return b!=null&&b.fighting()&&phase()!=6&&tickCount>=33;}
 @Override public boolean candidate(LivingEntity e){var b=boss();return b!=null&&b.candidate(e);}
 @Override public void attacked(LivingEntity e){var b=boss();if(b!=null)b.attacked(e);}
 @Override public void credit(java.util.UUID p,float damage){var b=boss();if(b!=null)b.credit(p,damage);}
 @Override protected void doubleTick(){var b=boss();if(!isAlive())return;if(b==null||!b.fighting())dissolve();entityData.set(AGE,age()+1);if(phase()==6){if(age()>=12)discard();return;}if(tickCount<33){if(tickCount%4==0)FrostVisuals.burst((ServerLevel)level(),position().add(0,tickCount/12.0,0),5);return;}entityData.set(PHASE,1);if(action()!=0){castTick();return;}if(tickCount%10==0){setTarget(b.targets().stream().filter(this::hasLineOfSight).min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null));}var t=getTarget();if(t==null)return;getLookControl().setLookAt(t,10,10);long now=level().getGameTime();if(distanceToSqr(t)<16&&now>=ready[1]){action(1);ready[1]=now+70;}else if(distanceToSqr(t)<=400&&now>=ready[2]&&hasLineOfSight(t)){action(2);ready[2]=now+80;}else if(tickCount%10==0)getNavigation().moveTo(t,1);}
 @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource source,float amount){return fighting()&&super.hurt(source,amount);}
 @Override protected void tickDeath(){if(++deathTime>=11)discard();}
}
