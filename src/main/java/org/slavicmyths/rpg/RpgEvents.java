package org.slavicmyths.rpg;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.*;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.*;
import net.minecraft.stats.Stats;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.entity.*;
import org.slavicmyths.registry.ModItems;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class RpgEvents {
 @SubscribeEvent public static void clone(PlayerEvent.Clone e){CompoundNBT old=e.getOriginal().getPersistentData().getCompound(PlayerEntity.PERSISTED_NBT_TAG);e.getPlayer().getPersistentData().put(PlayerEntity.PERSISTED_NBT_TAG,old.copy());}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getPlayer() instanceof ServerPlayerEntity){e.getPlayer().getPersistentData().remove("SlavicPendingBlock");e.getPlayer().getPersistentData().remove("SlavicShock");RpgNetwork.sync((ServerPlayerEntity)e.getPlayer());}}
 @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getPlayer() instanceof ServerPlayerEntity)RpgNetwork.sync((ServerPlayerEntity)e.getPlayer());}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getPlayer() instanceof ServerPlayerEntity)RpgNetwork.sync((ServerPlayerEntity)e.getPlayer());}
 @SubscribeEvent public static void knockback(LivingKnockBackEvent e){if(!(e.getEntityLiving() instanceof PlayerEntity)||e.getEntityLiving().level.isClientSide)return;PlayerEntity p=(PlayerEntity)e.getEntityLiving();if(PathData.data(p).getLong("Stance")>p.level.getGameTime())e.setStrength(e.getStrength()*.4F);else if(PathData.has(p,"drill")&&((!Runes.category(p.getMainHandItem()).isEmpty()&&!Runes.category(p.getMainHandItem()).equals("bow"))||p.getOffhandItem().getItem() instanceof ShieldItem))e.setStrength(e.getStrength()*.85F);}
 // Forge 36 has no ShieldBlockEvent. Confirm vanilla's actual blocked-damage stat
 // after hurt() completes, instead of granting retaliation for merely holding a shield.
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void attack(LivingAttackEvent e){
  if(!(e.getEntityLiving() instanceof ServerPlayerEntity)||e.getAmount()<=0)return;ServerPlayerEntity p=(ServerPlayerEntity)e.getEntityLiving();
  if(!p.isBlocking()||p.getPersistentData().contains("SlavicPendingBlock"))return;
  if(e.getSource().isBypassArmor()||p.isInvulnerableTo(e.getSource())||e.getSource().getSourcePosition()==null)return;
  if(e.getSource().getDirectEntity() instanceof AbstractArrowEntity&&((AbstractArrowEntity)e.getSource().getDirectEntity()).getPierceLevel()>0)return;
  net.minecraft.util.math.vector.Vector3d direction=p.position().subtract(e.getSource().getSourcePosition()).normalize();
  if(new net.minecraft.util.math.vector.Vector3d(direction.x,0,direction.z).dot(p.getLookAngle())>=0)return;
  CompoundNBT pending=new CompoundNBT();pending.putInt("Before",p.getStats().getValue(Stats.CUSTOM.get(Stats.DAMAGE_BLOCKED_BY_SHIELD)));pending.put("Shield",p.getUseItem().save(new CompoundNBT()));pending.putInt("Attacker",e.getSource().getEntity()==null?-1:e.getSource().getEntity().getId());p.getPersistentData().put("SlavicPendingBlock",pending);
 }
 @SubscribeEvent public static void blockConfirmation(TickEvent.PlayerTickEvent e){
  if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayerEntity))return;
  RuneEffects.finishShock((ServerPlayerEntity)e.player);
  if(!e.player.getPersistentData().contains("SlavicPendingBlock"))return;
  ServerPlayerEntity p=(ServerPlayerEntity)e.player;CompoundNBT n=p.getPersistentData().getCompound("SlavicPendingBlock");p.getPersistentData().remove("SlavicPendingBlock");
  if(p.getStats().getValue(Stats.CUSTOM.get(Stats.DAMAGE_BLOCKED_BY_SHIELD))<=n.getInt("Before"))return;Entity a=p.level.getEntity(n.getInt("Attacker"));RuneEffects.block(p,a instanceof LivingEntity?(LivingEntity)a:null,ItemStack.of(n.getCompound("Shield")));
 }
 @SubscribeEvent public static void arrow(EntityJoinWorldEvent e){
  if(e.getWorld().isClientSide||!(e.getEntity() instanceof AbstractArrowEntity))return;AbstractArrowEntity arrow=(AbstractArrowEntity)e.getEntity();if(arrow.getPersistentData().getBoolean("SlavicBowChecked"))return;
  arrow.getPersistentData().putBoolean("SlavicBowChecked",true);if(!(arrow.getOwner() instanceof PlayerEntity))return;PlayerEntity p=(PlayerEntity)arrow.getOwner();ItemStack bow=p.getUseItem();if(!(bow.getItem() instanceof BowItem))return;
  arrow.getPersistentData().put("SlavicBow",bow.save(new CompoundNBT()));
  double damage=arrow.getBaseDamage(),speed=1;
  if(arrow.isCritArrow()&&PathData.has(p,"aim"))damage*=1.1;
  if(arrow.isCritArrow()&&PathData.has(p,"precision")&&PathData.ready(p,"precision",400))damage*=1.2;
  if(Runes.has(bow,"midday")&&arrow.isCritArrow()&&RuneEffects.day(p))damage*=1.08;
  if(Runes.has(bow,"shadow")&&RuneEffects.dark(p))damage*=1.06;
  if(Runes.has(bow,"forest")&&RuneEffects.forest(p))speed+=.04;
  if(Runes.has(bow,"wind"))speed+=.08;arrow.setBaseDamage(damage);arrow.setDeltaMovement(arrow.getDeltaMovement().scale(speed));
 }
 @SubscribeEvent public static void hurt(LivingHurtEvent e){
  if(e.getEntityLiving().level.isClientSide||e.getAmount()<=0)return;
  if(!(e.getSource().getEntity() instanceof PlayerEntity))return;PlayerEntity p=(PlayerEntity)e.getSource().getEntity();LivingEntity target=e.getEntityLiving();
  boolean arrow=e.getSource().getDirectEntity() instanceof AbstractArrowEntity;
  boolean melee=e.getSource().getDirectEntity()==p&&"player".equals(e.getSource().getMsgId());if(!arrow&&!melee)return;
  ItemStack item=arrow?ItemStack.of(e.getSource().getDirectEntity().getPersistentData().getCompound("SlavicBow")):p.getMainHandItem();String category=Runes.category(item);if(category.isEmpty()||!Abilities.canAffect(p,target)||!arrow&&!p.canSee(target))return;
  net.minecraft.nbt.ListNBT encounters=target.getPersistentData().getList("SlavicAmbushers",8);
  boolean seen=false;for(int n=0;n<encounters.size();n++)if(encounters.getString(n).equals(p.getUUID().toString()))seen=true;
  boolean first=!arrow&&p.isCrouching()&&(!(target instanceof MobEntity)||((MobEntity)target).getTarget()!=p)&&!seen&&encounters.size()<16;
  if(!arrow){
   if(PathData.has(p,"heavy_hand")&&Runes.heavy(item))e.setAmount(e.getAmount()+.75F);
   if(first){if(PathData.has(p,"ambush"))e.setAmount(e.getAmount()+(category.equals("dagger")?2:1));if(PathData.has(p,"precision")&&category.equals("dagger")&&PathData.ready(p,"precision",400))e.setAmount(e.getAmount()+2);}
   if(!seen&&encounters.size()<16){encounters.add(net.minecraft.nbt.StringNBT.valueOf(p.getUUID().toString()));target.getPersistentData().put("SlavicAmbushers",encounters);}
   if(PathData.has(p,"hunter")&&target instanceof AnimalEntity)e.setAmount(e.getAmount()+1);
   if(PathData.data(p).getLong("Riposte")>p.level.getGameTime()){PathData.data(p).remove("Riposte");e.setAmount(e.getAmount()+1.5F);}
   if(Runes.heavy(item)&&PathData.data(p).getLong("Sweep")>p.level.getGameTime()){
    PathData.data(p).remove("Sweep");int n=0;for(LivingEntity x:p.level.getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(1.5),x->x!=target&&Abilities.canHit(p,x)&&p.distanceToSqr(x)<16)){x.hurt(net.minecraft.util.DamageSource.indirectMagic(p,p),2);if(++n>=3)break;}
   }
  }
  RuneEffects.hit(p,target,item,e,arrow,first);
 }
 @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntityLiving().level.isClientSide||!(e.getSource().getEntity() instanceof PlayerEntity))return;PlayerEntity p=(PlayerEntity)e.getSource().getEntity();if(e.getSource().getDirectEntity()==p&&Runes.has(p.getMainHandItem(),"life")&&PathData.ready(p,"rune_life",900))p.heal(2);}
 @SubscribeEvent public static void food(LivingEntityUseItemEvent.Finish e){if(!(e.getEntityLiving() instanceof PlayerEntity)||e.getEntityLiving().level.isClientSide)return;PlayerEntity p=(PlayerEntity)e.getEntityLiving();Item item=e.getItem().getItem();if(item!=ModItems.BERRY_MORS.get()&&item!=ModItems.FOREST_MIX.get())return;if(PathData.has(p,"herbs"))p.addEffect(new EffectInstance(Effects.REGENERATION,60,0));if(item==ModItems.BERRY_MORS.get()&&PathData.has(p,"healing_brew")&&PathData.ready(p,"brew",200))p.heal(2);}
 @SubscribeEvent public static void sense(PlayerInteractEvent.EntityInteract e){PlayerEntity p=e.getPlayer();Entity entity=e.getTarget();if(p.level.isClientSide||!PathData.has(p,"spirit_sense")||!(entity instanceof LandSpiritEntity||entity instanceof DomovoyEntity||entity instanceof LeshyEntity)||!PathData.ready(p,"sense",40))return;
  if(entity instanceof DomovoyEntity){int rep=((DomovoyEntity)entity).reputation(p);PathData.message(p,rep<0?"favor_low":rep>=40?"favor_high":"favor_medium");}else PathData.message(p,entity instanceof MobEntity&&((MobEntity)entity).getTarget()!=null?"hostile":entity instanceof LandSpiritEntity&&((LandSpiritEntity)entity).state()==1?"wary":"calm");}
 private RpgEvents(){}
}
