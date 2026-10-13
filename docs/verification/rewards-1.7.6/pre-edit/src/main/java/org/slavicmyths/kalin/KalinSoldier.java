package org.slavicmyths.kalin;
import java.util.UUID;import net.minecraft.nbt.CompoundTag;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.entity.*;import net.minecraft.world.entity.monster.Monster;import net.minecraft.world.level.Level;
public final class KalinSoldier extends Monster {
 public UUID commander;
 public KalinSoldier(EntityType<? extends KalinSoldier> t,Level w){super(t,w);setPersistenceRequired();}
 public boolean elite(){return getType()==KalinContent.ELITE.get();}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new net.minecraft.world.entity.ai.goal.FloatGoal(this));goalSelector.addGoal(2,new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this,1,false));}
 @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w)||tickCount%10!=0)return;var e=commander==null?null:w.getEntity(commander);if(!(e instanceof KalinEntity k)||!k.valid()||k.surrendered()||!k.soldiers.contains(getUUID())||position().distanceToSqr(k.origin())>36*36){discard();return;}var t=k.partyTarget(this);if(getPersistentData().getLong("AlyoshaTauntUntil")>w.getGameTime()&&getTarget()!=null&&k.enemy(getTarget()))t=getTarget();if(getPersistentData().getLong("BogatyrTauntUntil")>w.getGameTime()&&getPersistentData().hasUUID("BogatyrTauntHero")&&w.getEntity(getPersistentData().getUUID("BogatyrTauntHero")) instanceof LivingEntity hero&&k.enemy(hero))t=hero;if(getPersistentData().getLong("FinistDistractedUntil")>w.getGameTime())t=null;setTarget(t);}
 @Override public boolean removeWhenFarAway(double d){return false;}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(commander!=null)n.putUUID("KalinCommander",commander);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);commander=n.hasUUID("KalinCommander")?n.getUUID("KalinCommander"):null;}
 @Override protected void dropAllDeathLoot(ServerLevel w,net.minecraft.world.damagesource.DamageSource s){}@Override protected void dropExperience(Entity e){}
}
