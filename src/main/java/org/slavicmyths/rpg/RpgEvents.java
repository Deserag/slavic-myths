package org.slavicmyths.rpg;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.*;
import net.minecraft.stats.Stats;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;

import net.neoforged.bus.api.*;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.entity.*;
import org.slavicmyths.registry.ModItems;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class RpgEvents {
 @SubscribeEvent public static void clone(PlayerEvent.Clone e){CompoundTag old=e.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);e.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG,old.copy());}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer){e.getEntity().getPersistentData().remove("SlavicPendingBlock");e.getEntity().getPersistentData().remove("SlavicShock");RpgNetwork.sync((ServerPlayer)e.getEntity());}}
 @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer)RpgNetwork.sync((ServerPlayer)e.getEntity());}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer)RpgNetwork.sync((ServerPlayer)e.getEntity());}
 @SubscribeEvent public static void knockback(LivingKnockBackEvent e){if(!(e.getEntity() instanceof Player)||e.getEntity().level().isClientSide)return;Player p=(Player)e.getEntity();if(PathData.data(p).getLong("Stance")>p.level().getGameTime())e.setStrength(e.getStrength()*.4F);else if(PathData.has(p,"drill")&&((!Runes.category(p.getMainHandItem()).isEmpty()&&!Runes.category(p.getMainHandItem()).equals("bow"))||p.getOffhandItem().getItem() instanceof ShieldItem))e.setStrength(e.getStrength()*.85F);}
 // Capture target shield blocking, then confirm the actual blocked-damage statistic after hurt().
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void attack(LivingShieldBlockEvent e){
  if(!(e.getEntity() instanceof ServerPlayer)||!e.getBlocked()||e.getBlockedDamage()<=0)return;ServerPlayer p=(ServerPlayer)e.getEntity();
  if(!p.isBlocking()||p.getPersistentData().contains("SlavicPendingBlock"))return;
  if(e.getDamageSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)||p.isInvulnerableTo(e.getDamageSource())||e.getDamageSource().getSourcePosition()==null)return;
  if(e.getDamageSource().getDirectEntity() instanceof AbstractArrow&&((AbstractArrow)e.getDamageSource().getDirectEntity()).getPierceLevel()>0)return;
  net.minecraft.world.phys.Vec3 direction=p.position().subtract(e.getDamageSource().getSourcePosition()).normalize();
  if(new net.minecraft.world.phys.Vec3(direction.x,0,direction.z).dot(p.getLookAngle())>=0)return;
  CompoundTag pending=new CompoundTag();pending.putInt("Before",p.getStats().getValue(Stats.CUSTOM.get(Stats.DAMAGE_BLOCKED_BY_SHIELD)));pending.put("Shield",p.getUseItem().saveOptional(p.registryAccess()));pending.putInt("Attacker",e.getDamageSource().getEntity()==null?-1:e.getDamageSource().getEntity().getId());p.getPersistentData().put("SlavicPendingBlock",pending);
 }
 @SubscribeEvent public static void blockConfirmation(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){
  if(!(e.getEntity() instanceof ServerPlayer))return;
  RuneEffects.finishShock((ServerPlayer)e.getEntity());
  if(!e.getEntity().getPersistentData().contains("SlavicPendingBlock"))return;
  ServerPlayer p=(ServerPlayer)e.getEntity();CompoundTag n=p.getPersistentData().getCompound("SlavicPendingBlock");p.getPersistentData().remove("SlavicPendingBlock");
  if(p.getStats().getValue(Stats.CUSTOM.get(Stats.DAMAGE_BLOCKED_BY_SHIELD))<=n.getInt("Before"))return;Entity a=p.level().getEntity(n.getInt("Attacker"));RuneEffects.block(p,a instanceof LivingEntity?(LivingEntity)a:null,org.slavicmyths.item.ItemState.snapshot(p.registryAccess(),n.getCompound("Shield")));
 }
 @SubscribeEvent public static void arrow(EntityJoinLevelEvent e){
  if(e.getLevel().isClientSide||!(e.getEntity() instanceof AbstractArrow))return;AbstractArrow arrow=(AbstractArrow)e.getEntity();if(arrow.getPersistentData().getBoolean("SlavicBowChecked"))return;
  arrow.getPersistentData().putBoolean("SlavicBowChecked",true);if(!(arrow.getOwner() instanceof Player))return;Player p=(Player)arrow.getOwner();ItemStack bow=p.getUseItem();if(!(bow.getItem() instanceof BowItem))return;
  arrow.getPersistentData().put("SlavicBow",bow.saveOptional(p.registryAccess()));
  double damage=arrow.getBaseDamage(),speed=1;
  if(arrow.isCritArrow()&&PathData.has(p,"aim"))damage*=1.1;
  if(arrow.isCritArrow()&&PathData.has(p,"precision")&&PathData.ready(p,"precision",400))damage*=1.2;
  if(Runes.has(bow,"midday")&&arrow.isCritArrow()&&RuneEffects.day(p))damage*=RuneDefinition.n("midday","bow_multiplier");
  if(Runes.has(bow,"shadow")&&RuneEffects.dark(p))damage*=RuneDefinition.n("shadow","bow_multiplier");
  if(Runes.has(bow,"forest")&&RuneEffects.forest(p))speed+=RuneDefinition.n("forest","arrow_speed");
  if(Runes.has(bow,"wind"))speed+=RuneDefinition.n("wind","arrow_speed");arrow.setBaseDamage(damage);arrow.setDeltaMovement(arrow.getDeltaMovement().scale(speed));
 }
 @SubscribeEvent public static void hurt(LivingIncomingDamageEvent e){
  if(e.getEntity().level().isClientSide||e.getAmount()<=0)return;
  if(!(e.getSource().getEntity() instanceof Player))return;Player p=(Player)e.getSource().getEntity();LivingEntity target=e.getEntity();
  boolean arrow=e.getSource().getDirectEntity() instanceof AbstractArrow;
  boolean melee=e.getSource().getDirectEntity()==p&&"player".equals(e.getSource().getMsgId());if(!arrow&&!melee)return;
  ItemStack item=arrow?org.slavicmyths.item.ItemState.snapshot(p.registryAccess(),e.getSource().getDirectEntity().getPersistentData().getCompound("SlavicBow")):p.getMainHandItem();String category=Runes.category(item);if(category.isEmpty()||!Abilities.canAffect(p,target)||!arrow&&!p.hasLineOfSight(target))return;
  net.minecraft.nbt.ListTag encounters=target.getPersistentData().getList("SlavicAmbushers",8);
  boolean seen=false;for(int n=0;n<encounters.size();n++)if(encounters.getString(n).equals(p.getUUID().toString()))seen=true;
  boolean first=!arrow&&p.isCrouching()&&(!(target instanceof Mob)||((Mob)target).getTarget()!=p)&&!seen&&encounters.size()<16;
  if(!arrow){
   if(PathData.has(p,"heavy_hand")&&Runes.heavy(item))e.setAmount(e.getAmount()+.75F);
   if(first){if(PathData.has(p,"ambush"))e.setAmount(e.getAmount()+(category.equals("dagger")?2:1));if(PathData.has(p,"precision")&&category.equals("dagger")&&PathData.ready(p,"precision",400))e.setAmount(e.getAmount()+2);}
   if(!seen&&encounters.size()<16){encounters.add(net.minecraft.nbt.StringTag.valueOf(p.getUUID().toString()));target.getPersistentData().put("SlavicAmbushers",encounters);}
   if(PathData.has(p,"hunter")&&target instanceof Animal)e.setAmount(e.getAmount()+1);
   if(PathData.data(p).getLong("Riposte")>p.level().getGameTime()){PathData.data(p).remove("Riposte");e.setAmount(e.getAmount()+1.5F);}
   if(Runes.heavy(item)&&PathData.data(p).getLong("Sweep")>p.level().getGameTime()){
    PathData.data(p).remove("Sweep");int n=0;for(LivingEntity x:p.level().getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(1.5),x->x!=target&&Abilities.canHit(p,x)&&p.distanceToSqr(x)<16)){x.hurt(p.damageSources().indirectMagic(p,p),2);if(++n>=3)break;}
   }
  }
  RuneEffects.hit(p,target,item,e,arrow,first);
 }
 @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity().level().isClientSide||!(e.getSource().getEntity() instanceof Player))return;Player p=(Player)e.getSource().getEntity();if(e.getSource().getDirectEntity()==p&&Runes.has(p.getMainHandItem(),"life")&&PathData.ready(p,"rune_life",RuneDefinition.i("life","kill_cooldown")))p.heal(RuneDefinition.i("life","kill_healing"));}
 @SubscribeEvent public static void food(LivingEntityUseItemEvent.Finish e){if(!(e.getEntity() instanceof Player)||e.getEntity().level().isClientSide)return;Player p=(Player)e.getEntity();Item item=e.getItem().getItem();if(item!=ModItems.BERRY_MORS.get()&&item!=ModItems.FOREST_MIX.get())return;if(PathData.has(p,"herbs"))p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,60,0));if(item==ModItems.BERRY_MORS.get()&&PathData.has(p,"healing_brew")&&PathData.ready(p,"brew",200))p.heal(2);}
 @SubscribeEvent public static void sense(PlayerInteractEvent.EntityInteract e){Player p=e.getEntity();Entity entity=e.getTarget();if(p.level().isClientSide||!PathData.has(p,"spirit_sense")||!(entity instanceof LandSpiritEntity||entity instanceof DomovoyEntity||entity instanceof LeshyEntity)||!PathData.ready(p,"sense",40))return;
  if(entity instanceof DomovoyEntity){int rep=((DomovoyEntity)entity).reputation(p);PathData.message(p,rep<0?"favor_low":rep>=40?"favor_high":"favor_medium");}else PathData.message(p,entity instanceof Mob&&((Mob)entity).getTarget()!=null?"hostile":entity instanceof LandSpiritEntity&&((LandSpiritEntity)entity).state()==1?"wary":"calm");}
 private RpgEvents(){}
}
