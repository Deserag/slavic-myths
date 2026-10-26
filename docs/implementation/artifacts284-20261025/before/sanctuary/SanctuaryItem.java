package org.slavicmyths.sanctuary;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
public final class SanctuaryItem extends Item {
 private final String kind;
 public SanctuaryItem(String kind){super(new Properties().stacksTo(kind.equals("flame_vial")||kind.equals("spring_tear")?16:64).rarity(kind.equals("purification_stone")?Rarity.RARE:Rarity.UNCOMMON));this.kind=kind;}
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){var s=p.getItemInHand(hand);
  if(kind.equals("flame_vial")){if(p.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(s);if(p instanceof ServerPlayer server){var vial=SanctuaryContent.VIAL.get().create(w);if(vial==null)return InteractionResultHolder.fail(s);vial.setOwner(p);vial.setPos(p.getEyePosition());vial.shootFromRotation(p,p.getXRot(),p.getYRot(),-10,1.1f,.5f);if(!w.addFreshEntity(vial))return InteractionResultHolder.fail(s);if(!p.isCreative())s.shrink(1);p.getCooldowns().addCooldown(this,20);SanctuaryContent.sound(w,p.blockPosition(),"vial_throw");}return InteractionResultHolder.sidedSuccess(s,w.isClientSide);}
  if(kind.equals("fading_flame_charm")){if(p.hasEffect(SanctuaryContent.GUARD))return InteractionResultHolder.fail(s);if(p instanceof ServerPlayer){s.shrink(1);p.addEffect(new MobEffectInstance(SanctuaryContent.GUARD,SanctuaryConfig.WARD_SECONDS.get()*20));SanctuaryContent.sound(w,p.blockPosition(),"fire_restore");}return InteractionResultHolder.sidedSuccess(s,w.isClientSide);}
  if(kind.equals("purified_water_bottle")||kind.equals("spring_tear")){if(p instanceof ServerPlayer server&&kind.equals("spring_tear")&&now(server)<org.slavicmyths.rpg.PathData.data(p).getLong("Sanctuary193TearReady")){p.displayClientMessage(SanctuaryContent.text("tear_wait"),true);return InteractionResultHolder.fail(s);}p.startUsingItem(hand);return InteractionResultHolder.consume(s);}
  return InteractionResultHolder.pass(s);
 }
 private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
 @Override public int getUseDuration(ItemStack s,LivingEntity e){return 32;}
 @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.DRINK;}
 @Override public ItemStack finishUsingItem(ItemStack stack,Level w,LivingEntity e){if(!(e instanceof ServerPlayer p))return stack;
  if(kind.equals("spring_tear")){var n=org.slavicmyths.rpg.PathData.data(p);if(now(p)<n.getLong("Sanctuary193TearReady"))return stack;n.putLong("Sanctuary193TearReady",now(p)+SanctuaryConfig.TEAR_COOLDOWN.get()*20);p.addEffect(new MobEffectInstance(SanctuaryContent.TEAR,160));p.getCooldowns().addCooldown(this,SanctuaryConfig.TEAR_COOLDOWN.get()*20);SanctuaryContent.sound(w,p.blockPosition(),"tear_use");}
  else if(kind.equals("purified_water_bottle")){for(var effect:List.of(MobEffects.MOVEMENT_SLOWDOWN,MobEffects.WEAKNESS,MobEffects.POISON)){var active=p.getEffect(effect);if(active==null||active.isInfiniteDuration()||protectedEffect(p,effect))continue;var shortened=new MobEffectInstance(effect,SanctuaryRules.shortened(active.getDuration()),active.getAmplifier(),active.isAmbient(),active.isVisible(),active.showIcon());p.removeEffect(effect);p.addEffect(shortened);}SanctuaryContent.sound(w,p.blockPosition(),"water_use");}
  else return stack;stack.shrink(1);if(kind.equals("purified_water_bottle")){var bottle=new ItemStack(Items.GLASS_BOTTLE);if(stack.isEmpty())return bottle;if(!p.getInventory().add(bottle))p.drop(bottle,false);}return stack;
 }
 private static boolean protectedEffect(ServerPlayer p,net.minecraft.core.Holder<MobEffect> effect){if(!effect.is(MobEffects.POISON)&&p.getPersistentData().contains(org.slavicmyths.morozko.FrostCold.KEY))return true;if(p.getPersistentData().getBoolean("Quest185Ground."+effect.value().getDescriptionId()))return true;var owned=p.getPersistentData().getCompound("Night192Effects");String id=net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.value()).toString();return owned.getAllKeys().stream().anyMatch(k->owned.getCompound(k).contains(id));}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<net.minecraft.network.chat.Component> t,TooltipFlag f){t.add(net.minecraft.network.chat.Component.translatable(getDescriptionId()+".desc"));}
}
