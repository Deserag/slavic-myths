package org.slavicmyths.blackcinder;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
/** Reusable active defensive trophy rune; expiry is absolute saved game time. */
@EventBusSubscriber(modid="slavicmyths")
public final class CinderMantle extends Item {
 public CinderMantle(){super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));}
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
  var stack=player.getItemInHand(hand);if(player.getCooldowns().isOnCooldown(this)||player.getPersistentData().getLong("CinderMantleReady")>level.getGameTime())return InteractionResultHolder.fail(stack);
  if(level instanceof net.minecraft.server.level.ServerLevel server)server.sendParticles(net.minecraft.core.particles.ParticleTypes.ASH,player.getX(),player.getY()+1,player.getZ(),20,.7,.6,.7,.02);
  if(!level.isClientSide){player.getPersistentData().putLong("CinderMantleReady",level.getGameTime()+600);player.getPersistentData().putLong("CinderMantleUntil",level.getGameTime()+160);player.getPersistentData().putBoolean("CinderMantlePulse",true);player.getCooldowns().addCooldown(this,600);level.playSound(null,player.blockPosition(),CinderSounds.get("mantle"),net.minecraft.sounds.SoundSource.PLAYERS,.6F,1);}
  return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
 }
 @SubscribeEvent public static void damage(LivingIncomingDamageEvent event){
  var wearer=event.getEntity();if(wearer.level().isClientSide)return;var data=wearer.getPersistentData();
  if(data.getLong("CinderMantleUntil")<=wearer.level().getGameTime()&&data.getLong("CinderMantleReady")<=wearer.level().getGameTime()&&event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)){
   boolean installed=false;for(var stack:wearer.getArmorSlots())if(org.slavicmyths.rpg.Runes.has(stack,"ash_mantle")){installed=true;break;}
   if(installed){data.putLong("CinderMantleUntil",wearer.level().getGameTime()+160);data.putLong("CinderMantleReady",wearer.level().getGameTime()+600);data.putBoolean("CinderMantlePulse",true);wearer.playSound(CinderSounds.get("mantle"),.6F,1);}
  }
  if(data.getLong("CinderMantleUntil")<=wearer.level().getGameTime())return;
  if(event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE))event.setAmount(event.getAmount()*.6F);
  if(event.getAmount()>0 && data.getBoolean("CinderMantlePulse")){
   data.putBoolean("CinderMantlePulse",false);
   if(event.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity enemy && enemy!=wearer && wearer.hasLineOfSight(enemy) && wearer.distanceToSqr(enemy)<36){var d=enemy.position().subtract(wearer.position());enemy.knockback(.8,-d.x,-d.z);}
   if(wearer.level() instanceof net.minecraft.server.level.ServerLevel server)server.sendParticles(net.minecraft.core.particles.ParticleTypes.ASH,wearer.getX(),wearer.getY()+1,wearer.getZ(),16,.7,.5,.7,.04);
  }
 }
 @Override public void appendHoverText(ItemStack stack,TooltipContext context,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){lines.add(net.minecraft.network.chat.Component.translatable("cinder.mantle.desc"));}
}
