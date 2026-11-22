package org.slavicmyths.marine;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.slavicmyths.combat.CreatureProperties;
import org.slavicmyths.combat.RareBleedEffect;
/** Approved elemental rule: water deals +35% to fire. Earth resistance awaits a defined balance. */
@EventBusSubscriber(modid="slavicmyths")
public final class MarineWeaponRules {
 public static final float WATER_VS_FIRE=1.35F;
 public static final float WATER_VS_EARTH=1F;
 public static boolean weapon(ItemStack stack){return stack.is(PharaohkaTrident.ITEM.get())||(stack.is(MarineWeapons.HARPOON.get())||stack.is(MarineHarpoons.AMMO.get()))||stack.is(MarineWeapons.ANCHOR.get())||stack.is(MarineWeapons.STAFF.get())||stack.is(ZhelvaShield.ITEM.get());}
 public static final class AbilitySource extends net.minecraft.world.damagesource.DamageSource {
  final ItemStack weapon;
  public AbilitySource(ServerPlayer owner,ItemStack weapon){super(owner.damageSources().playerAttack(owner).typeHolder(),owner,owner);this.weapon=weapon.copyWithCount(1);}
 }
 public static ItemStack source(net.minecraft.world.damagesource.DamageSource source){
  if(source instanceof AbilitySource ability)return ability.weapon;
  if(source.getDirectEntity() instanceof ThrownTrident trident)return trident.getWeaponItem();
  if(source.getDirectEntity()==source.getEntity()&&source.getEntity() instanceof ServerPlayer player)return player.getMainHandItem();
  return ItemStack.EMPTY;
 }
 public static float multiplier(LivingEntity target){
  var traits=CreatureProperties.traits(target);
  if(traits.contains(CreatureProperties.Trait.EARTH_AFFINITY))return WATER_VS_EARTH;
  return traits.contains(CreatureProperties.Trait.FIRE_AFFINITY)?WATER_VS_FIRE:1F;
 }
 @SubscribeEvent public static void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event){
  if(!weapon(event.getItemStack()))return;
  if(event.getItemStack().is(MarineHarpoons.AMMO.get()))event.getToolTip().add(net.minecraft.network.chat.Component.translatable("item.slavicmyths.storm_harpoon_ammo.desc").withStyle(net.minecraft.ChatFormatting.AQUA));
  event.getToolTip().add(net.minecraft.network.chat.Component.translatable("item.slavicmyths.marine_fire_advantage").withStyle(net.minecraft.ChatFormatting.AQUA));
 }
 @SubscribeEvent public static void damage(LivingDamageEvent.Pre event){
  var stack=source(event.getSource());
  if(stack!=null&&weapon(stack)&&!event.getEntity().level().isClientSide)event.setNewDamage(event.getNewDamage()*multiplier(event.getEntity()));
 }
 @SubscribeEvent public static void bleeding(LivingDamageEvent.Post event){
  if(event.getNewDamage()<=0||!(event.getSource().getEntity() instanceof ServerPlayer owner))return;
  var stack=source(event.getSource());if(stack==null)return;
  if(weapon(stack))owner.setLastHurtMob(event.getEntity());
  if(stack.is(PharaohkaTrident.ITEM.get()))RareBleedEffect.applyNativeLevel(owner,event.getEntity(),2);
  else if((stack.is(MarineWeapons.HARPOON.get())||stack.is(MarineHarpoons.AMMO.get())))RareBleedEffect.applyNativeLevel(owner,event.getEntity(),1);
 }
}