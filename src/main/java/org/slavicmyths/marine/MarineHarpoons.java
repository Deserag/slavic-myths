package org.slavicmyths.marine;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.rpg.Abilities;
import org.slavicmyths.combat.IndependentWeaponCooldowns;
@EventBusSubscriber(modid="slavicmyths")
public final class MarineHarpoons {
 public static final String HITS="MarineHarpoonHits",LAST="MarineHarpoonLast",TETHER="MarineHarpoonTether";
 public static final DeferredHolder<Item,Item> AMMO=MarineWeapons.HARPOON;
 public static void init(){}
 public static class Ammo extends TridentItem {
  public Ammo(){super(new Properties().stacksTo(32).rarity(Rarity.EPIC).attributes(TridentItem.createAttributes()));}
  @Override public boolean hasCraftingRemainingItem(ItemStack stack){return true;}
  @Override public ItemStack getCraftingRemainingItem(ItemStack stack){return stack.copyWithCount(1);}
  @Override public int getUseDuration(ItemStack stack,LivingEntity user){return 72000;}
  @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.SPEAR;}
  @Override public InteractionResultHolder<ItemStack> use(Level world,Player player,InteractionHand hand){   if(player.isShiftKeyDown()&&player instanceof ServerPlayer server){
    var state=player.getPersistentData();
    if(state.getInt(HITS)<4||server.serverLevel().getGameTime()<state.getLong("MarineHarpoonReady")){
     server.displayClientMessage(net.minecraft.network.chat.Component.translatable("item.slavicmyths.storm_harpoon_ammo.not_ready",state.getInt(HITS)),true);
     return InteractionResultHolder.fail(player.getItemInHand(hand));
    }
   }
   if(player instanceof ServerPlayer server){
    if(!IndependentWeaponCooldowns.ready(server,IndependentWeaponCooldowns.HARPOON,player.isShiftKeyDown()))return InteractionResultHolder.fail(player.getItemInHand(hand));
    server.getPersistentData().putBoolean("MarineHarpoonCastTether",player.isShiftKeyDown());
   }
   player.startUsingItem(hand);return InteractionResultHolder.consume(player.getItemInHand(hand));}
  @Override public void releaseUsing(ItemStack stack,Level world,LivingEntity user,int remaining){
   if(!(user instanceof ServerPlayer player)||getUseDuration(stack,user)-remaining<10)return;
   var data=player.getPersistentData();boolean alternate=data.getBoolean("MarineHarpoonCastTether");data.remove("MarineHarpoonCastTether");
   if(!IndependentWeaponCooldowns.ready(player,IndependentWeaponCooldowns.HARPOON,alternate))return;
   if(alternate&&(data.getInt(HITS)<4||!data.hasUUID(LAST)||player.serverLevel().getGameTime()<data.getLong("MarineHarpoonReady")))return;
   var shot=new ThrownTrident(world,player,stack.copyWithCount(1));shot.pickup=AbstractArrow.Pickup.DISALLOWED;
   if(alternate){
    shot.getPersistentData().putUUID(TETHER,data.getUUID(LAST));
   }
   shot.shootFromRotation(player,player.getXRot(),player.getYRot(),0,2.6F,1);
   if(world.addFreshEntity(shot)){IndependentWeaponCooldowns.start(player,stack,IndependentWeaponCooldowns.HARPOON,alternate,12);if(!player.isCreative())stack.shrink(1);}
  }
 }
 @SubscribeEvent public static void impact(ProjectileImpactEvent event){
  if(!(event.getProjectile() instanceof ThrownTrident shot)||!(shot.getOwner() instanceof ServerPlayer player)||shot.getWeaponItem()==null||!shot.getWeaponItem().is(AMMO.get()))return;
  var metadata=shot.getPersistentData();if(metadata.getBoolean("MarineImpactSpent"))return;metadata.putBoolean("MarineImpactSpent",true);var hit=event.getRayTraceResult();
  if(metadata.hasUUID(TETHER)){
   if(hit instanceof EntityHitResult impact&&impact.getEntity() instanceof LivingEntity victim&&Abilities.canHit(player,victim))victim.hurt(player.damageSources().trident(shot,player),victim.isInWaterOrBubble()?11:8);
   var target=player.serverLevel().getEntity(metadata.getUUID(TETHER));
   if(player.getPersistentData().getInt(HITS)>=4&&target instanceof LivingEntity living&&living.isAlive()&&Abilities.canHit(player,living)&&living.position().distanceToSqr(hit.getLocation())<=100&&player.hasLineOfSight(living)&&MarineWeaponAbilities.ready(player,"harpoonTether",500)){
    Vec3 at=hit.getLocation();var state=living.getPersistentData();state.putLong(TETHER+"Until",player.serverLevel().getGameTime()+140);state.putDouble(TETHER+"X",at.x);state.putDouble(TETHER+"Y",at.y);state.putDouble(TETHER+"Z",at.z);state.putUUID(TETHER+"Owner",player.getUUID());
    player.getPersistentData().putInt(HITS,0);player.getPersistentData().putLong("MarineHarpoonReady",player.serverLevel().getGameTime()+500);
   }
   shot.discard();event.setCanceled(true);return;
  }
  if(hit instanceof EntityHitResult result&&result.getEntity() instanceof LivingEntity target){
   if(Abilities.canHit(player,target)&&target.hurt(player.damageSources().trident(shot,player),target.isInWaterOrBubble()?11:8)){
    var state=player.getPersistentData();state.putInt(HITS,Math.min(4,state.getInt(HITS)+1));state.putUUID(LAST,target.getUUID());
    player.displayClientMessage(net.minecraft.network.chat.Component.translatable("item.slavicmyths.storm_harpoon_ammo.charge",state.getInt(HITS)),true);
   }
   shot.discard();event.setCanceled(true);
  }else if(hit instanceof BlockHitResult){shot.discard();event.setCanceled(true);}
 }
 @SubscribeEvent public static void tether(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event){
  if(!(event.getEntity() instanceof LivingEntity target)||!(target.level() instanceof net.minecraft.server.level.ServerLevel world))return;
  var data=target.getPersistentData();if(!data.hasUUID(TETHER+"Owner"))return;var actor=world.getEntity(data.getUUID(TETHER+"Owner"));
  if(world.getGameTime()>=data.getLong(TETHER+"Until")||!target.isAlive()||!(actor instanceof ServerPlayer owner)||!owner.isAlive()||!Abilities.canHit(owner,target)){data.remove(TETHER+"Owner");return;}
  Vec3 at=new Vec3(data.getDouble(TETHER+"X"),data.getDouble(TETHER+"Y"),data.getDouble(TETHER+"Z"));Vec3 delta=at.subtract(target.position());
  if(!org.slavicmyths.combat.CombatTargeting.controlResistant(target)){
   Vec3 velocity=target.getDeltaMovement();Vec3 predicted=target.position().add(velocity).subtract(at);
   double radius=predicted.horizontalDistance();
   if(radius>3){Vec3 boundary=new Vec3(at.x+predicted.x/radius*3,target.getY()+velocity.y,at.z+predicted.z/radius*3);Vec3 restrained=boundary.subtract(target.position());if(world.noCollision(target,target.getBoundingBox().move(restrained)))target.setDeltaMovement(restrained);}
  }else target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,10,1));
  if(world.getGameTime()%4==0)world.sendParticles(net.minecraft.core.particles.ParticleTypes.NAUTILUS,at.x,at.y+.1,at.z,3,.12,.12,.12,0);
  if(world.getGameTime()%4==0)for(int i=0;i<16;i++){Vec3 point=at.lerp(target.position().add(0,1,0),i/15D);world.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,point.x,point.y,point.z,1,0,0,0,0);}
 }
}
