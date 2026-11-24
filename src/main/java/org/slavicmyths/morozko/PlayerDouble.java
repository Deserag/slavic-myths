package org.slavicmyths.morozko;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.server.level.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
public final class PlayerDouble extends PathfinderMob {
 private static final EntityDataAccessor<String> OWNER_NAME=SynchedEntityData.defineId(PlayerDouble.class,EntityDataSerializers.STRING);
 private static final EntityDataAccessor<Boolean> LEFT=SynchedEntityData.defineId(PlayerDouble.class,EntityDataSerializers.BOOLEAN);
 private static final EntityDataAccessor<Optional<UUID>> OWNER=SynchedEntityData.defineId(PlayerDouble.class,EntityDataSerializers.OPTIONAL_UUID);
 private int life,sequence;private float snapshotDamage;private int meleeInterval=13;private double rangedBase=2,rangedSpeed=2;private boolean rangedCritical;private final java.util.Set<java.util.UUID> arrows=new java.util.HashSet<>();private long lastMelee;private boolean restored; private boolean crown; private int sideSign=1; private LivingEntity pending; private boolean pendingRanged; private int pendingUntil; private float attackScale=1; private boolean critical;
 public PlayerDouble(EntityType<? extends PlayerDouble> t,net.minecraft.world.level.Level w){super(t,w);xpReward=0;setPersistenceRequired();}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(OWNER,Optional.empty());b.define(OWNER_NAME,"");b.define(LEFT,false);}
 @Override public net.minecraft.network.chat.Component getName(){return net.minecraft.network.chat.Component.translatable("entity.slavicmyths.player_ice_double",entityData.get(OWNER_NAME));}
 @Override public net.minecraft.world.entity.HumanoidArm getMainArm(){return entityData.get(LEFT)?net.minecraft.world.entity.HumanoidArm.LEFT:net.minecraft.world.entity.HumanoidArm.RIGHT;}
 public UUID owner(){return entityData.get(OWNER).orElse(null);}
 public ServerPlayer player(){return level() instanceof ServerLevel w&&owner()!=null?w.getServer().getPlayerList().getPlayer(owner()):null;}
 public void configure(ServerPlayer p){entityData.set(OWNER,Optional.of(p.getUUID()));entityData.set(OWNER_NAME,p.getName().getString());entityData.set(LEFT,p.getMainArm()==net.minecraft.world.entity.HumanoidArm.LEFT);for(var slot:EquipmentSlot.values()){var snapshot=p.getItemBySlot(slot).copy();boolean glint=snapshot.hasFoil();EnchantmentHelper.updateEnchantments(snapshot,m->m.removeIf(h->h.is(net.minecraft.world.item.enchantment.Enchantments.LOOTING)));if(glint&&!snapshot.hasFoil())snapshot.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE,true);setItemSlot(slot,snapshot);}snapshotDamage=(float)p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);meleeInterval=Math.max(1,(int)Math.ceil(20/p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED)));setHealth(FrostRules.MIRROR_HP);}
 @Override public void tick(){super.tick();if(level().isClientSide)return;var p=player();if(restored||p==null||!p.isAlive()||p.level()!=level()||++life>FrostRules.MIRROR_LIFE||(crown?org.slavicmyths.item.FolkAccessoryItem.equipped(p,org.slavicmyths.registry.ModItems.ICE_CROWN.get()).isEmpty():org.slavicmyths.item.FolkAccessoryItem.equipped(p,FrostContent.MIRROR.get()).isEmpty())){shatter();return;}if(life<13){if(life%3==0)FrostVisuals.burst((ServerLevel)level(),position().add(0,life/8.0,0),5);return;}if(pending!=null){if(life>pendingUntil||!allowedTarget(pending)||distanceToSqr(p)>32*32){pending=null;}else if(performPending()){pending=null;}else{getLookControl().setLookAt(pending,45,45);getNavigation().moveTo(pending,1.25);return;}}setShiftKeyDown(p.isShiftKeyDown());setSprinting(p.isSprinting());if(tickCount%4==0){var side=new Vec3(Math.cos(Math.toRadians(p.getYRot()))*1.8*sideSign,0,Math.sin(Math.toRadians(p.getYRot()))*1.8*sideSign);var at=FrostWorld.safe((ServerLevel)level(),p.position().add(side),.6f,1.8f,1);if(at!=null)getNavigation().moveTo(at.x,at.y,at.z,p.isSprinting()?1.3:1);if(distanceToSqr(p)>32*32){var recovery=FrostWorld.safe((ServerLevel)level(),p.position().add(side),.6f,1.8f,3);if(recovery==null){shatter();return;}FrostVisuals.burst((ServerLevel)level(),position(),8);teleportTo(recovery.x,recovery.y,recovery.z);}if(p.getDeltaMovement().y>.2&&onGround())getJumpControl().jump();getLookControl().setLookAt(p.getEyePosition().add(p.getLookAngle().scale(5)).x,p.getEyePosition().add(p.getLookAngle().scale(5)).y,p.getEyePosition().add(p.getLookAngle().scale(5)).z,25,25);}if(p.isUsingItem()&&(getItemInHand(p.getUsedItemHand()).getItem() instanceof BowItem||getItemInHand(p.getUsedItemHand()).getItem() instanceof CrossbowItem||getItemInHand(p.getUsedItemHand()).getItem() instanceof ShieldItem))startUsingItem(p.getUsedItemHand());else stopUsingItem();}
 public void crownSide(int sign){crown=true;sideSign=sign;}
 public boolean fromCrown(){return crown;}
 public boolean allowedTarget(LivingEntity t){var p=player();if(p==null||t==null||!org.slavicmyths.combat.CombatTargeting.canAttack(p,t,org.slavicmyths.combat.CombatTargeting.Context.PROJECTILE)||t==p||t==this||!t.isAlive()||t.isAlliedTo(p)||java.util.Objects.equals(FrostWorld.owner(t),owner()))return false;return !(t instanceof net.minecraft.world.entity.player.Player target)||p.canHarmPlayer(target);}
 public void mirrorHit(LivingEntity target,int attackSequence,boolean ranged){
  var p=player();if(p==null||attackSequence<=sequence||!allowedTarget(target))return;
  sequence=attackSequence;pending=target;pendingRanged=ranged;pendingUntil=life+60;
  attackScale=Math.clamp(p.getPersistentData().getFloat("FrostAttackScale"),0,1);critical=p.getPersistentData().getBoolean("FrostCritical");
 }
 private boolean performPending(){
  var target=pending;var p=player();if(p==null||target==null||!allowedTarget(target))return true;
  boolean ranged=pendingRanged;double reach=ranged?24:3.5;
  if(!hasLineOfSight(target)||FrostWorld.bboxDistance(getEyePosition(),target.getBoundingBox())>reach*reach)return false;
  long now=level().getGameTime();if(now-lastMelee<meleeInterval)return false;
  lastMelee=now;getNavigation().stop();getLookControl().setLookAt(target,180,180);
  setYRot((float)(Math.atan2(target.getZ()-getZ(),target.getX()-getX())*180/Math.PI)-90);
  swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);
  if(ranged){
   net.minecraft.world.entity.projectile.AbstractArrow arrow;
   if(getMainHandItem().getItem() instanceof org.slavicmyths.combat.ThrowingWeaponItem weapon)arrow=new org.slavicmyths.combat.WeaponProjectile(level(),this,getMainHandItem().copy(),weapon.knife,false);
   else if(getMainHandItem().getItem() instanceof TridentItem)arrow=new net.minecraft.world.entity.projectile.ThrownTrident(level(),this,getMainHandItem().copy());
   else if(getMainHandItem().getItem() instanceof BowItem||getMainHandItem().getItem() instanceof CrossbowItem)arrow=new net.minecraft.world.entity.projectile.Arrow(level(),this,new ItemStack(Items.ARROW),getMainHandItem());
   else return true;
   arrow.pickup=net.minecraft.world.entity.projectile.AbstractArrow.Pickup.DISALLOWED;arrow.setBaseDamage(rangedBase);arrow.setCritArrow(rangedCritical);
   var aim=target.getBoundingBox().getCenter().subtract(arrow.position());arrow.shoot(aim.x,aim.y+aim.horizontalDistance()*.04,aim.z,(float)rangedSpeed,0);
   if(level().addFreshEntity(arrow))arrows.add(arrow.getUUID());return true;
  }
  var source=org.slavicmyths.combat.MythDamageSources.caused("morozko_mirror",this);
  float damage=EnchantmentHelper.modifyDamage((ServerLevel)level(),getMainHandItem(),target,source,snapshotDamage*(.2f+attackScale*attackScale*.8f));
  if(critical&&attackScale>.9f)damage*=1.5f;
  if(target.hurt(source,damage)){EnchantmentHelper.doPostAttackEffectsWithItemSource((ServerLevel)level(),target,source,getMainHandItem());FrostEvents.proc(this,target);}
  return true;
 }
 public void ranged(net.minecraft.world.entity.projectile.AbstractArrow original){rangedBase=original.getBaseDamage();rangedSpeed=Math.max(.1,original.getDeltaMovement().length());rangedCritical=original.isCritArrow();}
 @Override public void remove(RemovalReason reason){if(level() instanceof ServerLevel w)for(var id:arrows){var arrow=w.getEntity(id);if(arrow!=null)arrow.discard();}super.remove(reason);}
 private void shatter(){if(!level().isClientSide){FrostContent.sound(this,"mirror_break");FrostVisuals.burst((ServerLevel)level(),position().add(0,1,0),15);discard();}}
 @Override public void die(DamageSource s){shatter();}
 @Override protected void dropAllDeathLoot(ServerLevel w,DamageSource s){}
 @Override public boolean isPushable(){return false;}
 @Override protected void doPush(Entity e){}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);restored=true;}
}
