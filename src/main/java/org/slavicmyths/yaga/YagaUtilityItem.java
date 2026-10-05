package org.slavicmyths.yaga;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerLevel;
import org.slavicmyths.registry.ModItems;
public final class YagaUtilityItem extends Item{
 public enum Kind{THREAD,CLEANSE,SALVE,SIGHT,RESIST,VIGOR,RESTORE,HERBS}
 private final Kind kind;public YagaUtilityItem(Properties p,Kind k){super(p);kind=k;}
 @Override public void appendHoverText(ItemStack stack,net.minecraft.world.item.Item.TooltipContext w,List<Component> lines,net.minecraft.world.item.TooltipFlag flag){lines.add(Component.translatable(getDescriptionId()+".effect").withStyle(ChatFormatting.GRAY));}
 @Override public int getUseDuration(ItemStack s,net.minecraft.world.entity.LivingEntity user){return kind==Kind.THREAD||kind==Kind.HERBS?0:32;}@Override public UseAnim getUseAnimation(ItemStack s){return kind==Kind.SALVE?UseAnim.EAT:UseAnim.DRINK;}
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player player,InteractionHand hand){ItemStack s=player.getItemInHand(hand);if(kind==Kind.HERBS)return super.use(w,player,hand);if(kind!=Kind.THREAD)return start(w,player,hand);if(player.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(s);if(!w.isClientSide&&player instanceof ServerPlayer){ServerPlayer p=(ServerPlayer)player;ServerLevel overworld=p.server.getLevel(Level.OVERWORLD);if(!org.slavicmyths.navigation.NavigationManager.thread(p))return InteractionResultHolder.fail(s);p.getCooldowns().addCooldown(this,80);YagaServices.award(p,"follow_the_thread");w.playSound(null,p.blockPosition(),SoundEvents.WOOL_HIT,SoundSource.PLAYERS,.6F,1.1F);}return InteractionResultHolder.success(s);}
 private static InteractionResultHolder<ItemStack> start(Level w,Player p,InteractionHand h){p.startUsingItem(h);return InteractionResultHolder.consume(p.getItemInHand(h));}
 @Override public ItemStack finishUsingItem(ItemStack s,Level w,LivingEntity e){if(!w.isClientSide){switch(kind){case CLEANSE:for(net.minecraft.core.Holder<MobEffect> bad:java.util.List.of(MobEffects.POISON,MobEffects.WITHER,MobEffects.BLINDNESS,MobEffects.MOVEMENT_SLOWDOWN,MobEffects.WEAKNESS,MobEffects.HUNGER,MobEffects.CONFUSION))e.removeEffect(bad);break;case SALVE:if(e instanceof Player)FlightRecovery.apply((Player)e);break;case SIGHT:e.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,1200));break;case RESIST:e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,600));break;case VIGOR:e.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,1200));break;case RESTORE:e.addEffect(new MobEffectInstance(MobEffects.REGENERATION,200));break;default:break;}}
  boolean consume=!(e instanceof Player)||!((Player)e).getAbilities().instabuild;if(consume){s.shrink(1);if(kind!=Kind.SALVE){ItemStack bottle=new ItemStack(Items.GLASS_BOTTLE);if(s.isEmpty())return bottle;if(e instanceof Player){Player p=(Player)e;if(!p.getInventory().add(bottle))p.drop(bottle,false);}}}return s;
 }
}
