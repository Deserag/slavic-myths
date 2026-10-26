package org.slavicmyths.quest185;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
public final class Quest185Item extends Item {
 private final String kind;
 public Quest185Item(String kind){super(new Properties().stacksTo(kind.equals("ward")?64:1).rarity(Rarity.UNCOMMON));this.kind=kind;}
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player player,InteractionHand hand){var stack=player.getItemInHand(hand);if(w.isClientSide){player.startUsingItem(hand);return InteractionResultHolder.consume(stack);}if(!(player instanceof ServerPlayer p))return InteractionResultHolder.pass(stack);var n=Quest185Story.data(p);long now=Quest185Story.now(p);
  if(kind.equals("ward")){if(p.hasEffect(Quest185Content.WARD_EFFECT)){p.displayClientMessage(Quest185Story.text("ward_active"),true);return InteractionResultHolder.fail(stack);}if(!p.addEffect(new MobEffectInstance(Quest185Content.WARD_EFFECT,24000,0,false,true,true)))return InteractionResultHolder.fail(stack);if(!p.isCreative()&&!stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getBoolean("GuardianWard196"))stack.shrink(1);Quest185Content.sound(p,"ward_on");}
  else if(kind.equals("tracker")){if(p.hasEffect(Quest185Content.TRACKER_EFFECT)){p.displayClientMessage(Quest185Story.text("tracker_active"),true);return InteractionResultHolder.fail(stack);}if(now<n.getLong("TrackerReady")){p.displayClientMessage(Quest185Story.text("cooldown",(n.getLong("TrackerReady")-now+19)/20),true);return InteractionResultHolder.fail(stack);}if(!p.addEffect(new MobEffectInstance(Quest185Content.TRACKER_EFFECT,3600,0,false,true,true)))return InteractionResultHolder.fail(stack);n.putLong("TrackerReady",now+12000);n.putLong("TrackerUntil",now+3600);Quest185Content.sound(p,"tracker_on");}
  else if(!WolfSupport.summon(p))return InteractionResultHolder.fail(stack);
  if(!kind.equals("ward"))p.startUsingItem(hand);p.swing(hand,true);return InteractionResultHolder.consume(stack);
 }
 @Override public int getUseDuration(ItemStack s,net.minecraft.world.entity.LivingEntity e){return kind.equals("wolf")?6:10;}
 @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.TOOT_HORN;}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){lines.add(Quest185Story.text(kind+"_hint"));}
}
