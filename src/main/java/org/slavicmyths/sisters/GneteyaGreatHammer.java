package org.slavicmyths.sisters;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/** Level-seven material reward: slower two-handed impact, distinct from Lomeya's paired hammers. */
@EventBusSubscriber(modid="slavicmyths")
public final class GneteyaGreatHammer extends SwordItem {
 private static long ready(ServerPlayer p){return org.slavicmyths.rpg.PathData.data(p).getLong("GneteyaGreatHammerReady");}
 @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){
  if(selected&&entity instanceof ServerPlayer p&&ready(p)>level.getGameTime()&&!p.getCooldowns().isOnCooldown(this))
   p.getCooldowns().addCooldown(this,(int)Math.min(240,ready(p)-level.getGameTime()));
 }
 public GneteyaGreatHammer(){super(Tiers.NETHERITE,new Properties().durability(1800).rarity(Rarity.EPIC).attributes(SwordItem.createAttributes(Tiers.NETHERITE,14,-3.2F)));}
 @SubscribeEvent public static void attack(AttackEntityEvent event){
  if(event.getEntity() instanceof ServerPlayer p&&p.getMainHandItem().getItem() instanceof GneteyaGreatHammer&&!p.getOffhandItem().isEmpty()){
   event.setCanceled(true);p.displayClientMessage(Component.translatable("tooltip.slavicmyths.gneteya_great_hammer.two_handed"),true);
  }
 }
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
  var stack=player.getItemInHand(hand);
  if(hand!=InteractionHand.MAIN_HAND||!player.isShiftKeyDown())return InteractionResultHolder.pass(stack);
  if(!player.getOffhandItem().isEmpty()||player.getCooldowns().isOnCooldown(this)||player instanceof ServerPlayer p&&ready(p)>level.getGameTime())return InteractionResultHolder.fail(stack);
  player.startUsingItem(hand);return InteractionResultHolder.consume(stack);
 }
 @Override public int getUseDuration(ItemStack stack,LivingEntity owner){return 72000;}
 @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.SPEAR;}
 @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity entity,int remaining){
  if(!(entity instanceof ServerPlayer p)||!p.isAlive()||p.isSpectator()||!p.getMainHandItem().equals(stack)
    ||!p.getOffhandItem().isEmpty()||getUseDuration(stack,p)-remaining<20||p.getCooldowns().isOnCooldown(this)||ready(p)>level.getGameTime())return;
  p.getCooldowns().addCooldown(this,240);org.slavicmyths.rpg.PathData.data(p).putLong("GneteyaGreatHammerReady",level.getGameTime()+240);int count=0;
  for(var target:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(4),t->t!=p&&t.isAlive()&&p.distanceToSqr(t)<=16&&org.slavicmyths.rpg.Abilities.canHit(p,t)&&p.hasLineOfSight(t))){
   if(target.hurt(p.damageSources().playerAttack(p),26)){
    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,80,0),p);
    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,1),p);
    if(++count>=6)break;
   }
  }
  p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,p.getX(),p.getY()+.2,p.getZ(),40,2,.1,2,.05);
  stack.hurtAndBreak(3,p,EquipmentSlot.MAINHAND);p.swing(InteractionHand.MAIN_HAND,true);
 }
 @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
  lines.add(Component.translatable("tooltip.slavicmyths.gneteya_great_hammer.two_handed"));
  lines.add(Component.translatable("tooltip.slavicmyths.gneteya_great_hammer.skill"));
 }
}
