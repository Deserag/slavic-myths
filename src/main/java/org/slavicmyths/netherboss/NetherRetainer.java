package org.slavicmyths.netherboss;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Bounded, persisted owner-bound summons. Never loads an owner chunk or grants rewards. */
public final class NetherRetainer extends Zombie {
 private UUID owner;
 private int remaining=1200;
 private long expires;
 public NetherRetainer(EntityType<? extends Zombie> type,Level level){super(type,level);xpReward=0;setPersistenceRequired();}
 public void configure(LivingEntity master,float hp,float damage,int lifetime) {
  owner=master.getUUID();remaining=Math.clamp(lifetime,1,1200);expires=level().getServer().overworld().getGameTime()+remaining;
  getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);setHealth(hp);
  for(var slot:EquipmentSlot.values())setDropChance(slot,0);
 }
 public UUID owner(){return owner;}
 @Override protected boolean convertsInWater(){return false;}
 @Override public boolean canChangeDimensions(Level from,Level to){return false;}
 @Override protected boolean isSunSensitive(){return false;}
 @Override protected void dropAllDeathLoot(ServerLevel world,net.minecraft.world.damagesource.DamageSource source){}
 @Override public boolean canPickUpLoot(){return false;}
 @Override public boolean canAttack(LivingEntity target){
  if(owner==null||!(level() instanceof ServerLevel world))return false;
  var master=world.getEntity(owner);
  if(!(master instanceof LivingEntity living)||!living.isAlive()||target==master||target instanceof NetherRetainer other&&java.util.Objects.equals(owner,other.owner()))return false;
  if(living instanceof net.minecraft.server.level.ServerPlayer player)return NetherTrophies.hostile(player,target);
  return master instanceof NetherSummonBoss boss&&target==boss.getTarget()&&super.canAttack(target);
 }
 @Override public void setTarget(LivingEntity target){super.setTarget(target==null||canAttack(target)?target:null);}
 @Override public void die(net.minecraft.world.damagesource.DamageSource source){
  if(level() instanceof ServerLevel world&&owner!=null&&world.getEntity(owner) instanceof NetherSummonBoss boss)boss.retainerDied(getUUID());
  super.die(source);
 }
 @Override public void tick(){
  super.tick();if(level().isClientSide)return;
  if(--remaining<=0||owner==null||level().getServer().overworld().getGameTime()>=expires){discard();return;}
  var master=((ServerLevel)level()).getEntity(owner);
  if(!(master instanceof LivingEntity living)||!living.isAlive()){discard();return;}
  if(tickCount%10==0){
   if(master instanceof NetherSummonBoss boss)setTarget(boss.getTarget());
   else if(master instanceof Player player){
    var target=player.getLastHurtByMob();
    if(target==null||!target.isAlive())target=player.getLastHurtMob();
    setTarget(target);
   }
   if(getTarget()==null&&distanceToSqr(master)>16)getNavigation().moveTo(master,1.1);
  }
 }
 @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(owner!=null)tag.putUUID("RiteOwner",owner);tag.putInt("RiteRemaining",remaining);tag.putLong("RiteExpires",expires);}
 @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);owner=tag.hasUUID("RiteOwner")?tag.getUUID("RiteOwner"):null;remaining=Math.clamp(tag.getInt("RiteRemaining"),0,1200);expires=tag.getLong("RiteExpires");}
}
