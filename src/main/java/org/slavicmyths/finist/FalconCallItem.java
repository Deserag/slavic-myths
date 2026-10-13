package org.slavicmyths.finist;
import net.minecraft.world.*;import net.minecraft.world.item.*;import net.minecraft.world.item.context.UseOnContext;import net.minecraft.world.entity.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.level.Level;import net.minecraft.server.level.ServerPlayer;import net.minecraft.network.chat.Component;import java.util.List;
public final class FalconCallItem extends Item {
 public final boolean seal;public FalconCallItem(boolean seal){super(new Properties().stacksTo(1));this.seal=seal;}
 @Override public int getUseDuration(ItemStack s,LivingEntity e){return seal?40:28;}
 @Override public UseAnim getUseAnimation(ItemStack s){return seal?UseAnim.BOW:UseAnim.TOOT_HORN;}
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){p.startUsingItem(hand);return InteractionResultHolder.consume(p.getItemInHand(hand));}
 @Override public InteractionResult useOn(UseOnContext c){if(c.getPlayer()!=null)c.getPlayer().startUsingItem(c.getHand());return InteractionResult.CONSUME;}
 @Override public void onUseTick(Level w,LivingEntity living,ItemStack s,int remaining){if(!(living instanceof ServerPlayer p)||remaining!=getUseDuration(s,p)-(seal?8:15))return;String key=seal?"KartausSealUntil174":"FinistFeatherUntil174";long now=w.getGameTime();if(now<p.getPersistentData().getLong(key))return;
 boolean ok=seal?KartausEncounter.start(p):org.slavicmyths.skim.HeroCalls.toggle(p,"finist");int cd=ok?40:8;p.getPersistentData().putLong(key,now+cd);p.getCooldowns().addCooldown(this,cd);p.playSound(FinistContent.sound(seal?"item.items.kartaus_seal":"item.items.finist_call"),ok?.7F:.2F,1);}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){lines.add(Component.translatable("tooltip.slavicmyths."+(seal?"kartaus_black_seal":"finist_call_feather")));}
}
