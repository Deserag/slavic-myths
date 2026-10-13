package org.slavicmyths.skim;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
public final class HeroCallItem extends Item {
 private final String id;public HeroCallItem(String id){super(new Properties().stacksTo(1));this.id=id;}
 private boolean horn(){return id.equals("beast_challenge_horn");}
 @Override public int getUseDuration(ItemStack s,net.minecraft.world.entity.LivingEntity e){return horn()?38:22;}
 @Override public UseAnim getUseAnimation(ItemStack s){return horn()||id.equals("alyosha_call_whistle")?UseAnim.TOOT_HORN:UseAnim.BOW;}
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){var s=p.getItemInHand(hand);if(p.isSpectator()||p.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(s);p.startUsingItem(hand);return InteractionResultHolder.consume(s);}
 @Override public InteractionResult useOn(UseOnContext c){var p=c.getPlayer();if(p==null)return InteractionResult.PASS;return use(c.getLevel(),p,c.getHand()).getResult();}
 @Override public void onUseTick(Level w,net.minecraft.world.entity.LivingEntity e,ItemStack s,int remaining){if(w.isClientSide||!(e instanceof ServerPlayer p)||remaining!=getUseDuration(s,e)-(horn()?20:12))return;var n=p.getPersistentData();long now=w.getGameTime();String k="CallCooldown_"+id;if(now<n.getLong(k))return;n.putLong(k,now+(horn()?300:100));p.getCooldowns().addCooldown(this,horn()?300:100);
 boolean ok=horn()?SkimValley.start(p):HeroCalls.toggle(p,id.equals("ilya_call_token")?"ilya_muromets":id.equals("dobrynya_call_emblem")?"dobrynya_nikitich":"alyosha_popovich");if(ok)p.playSound(SkimContent.sound(horn()?"items.beast_horn":id.equals("alyosha_call_whistle")?"items.alyosha_whistle":id.equals("ilya_call_token")?"items.ilya_token":"items.dobrynya_emblem"),.8F,1);
 }
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> out,TooltipFlag f){out.add(Component.translatable("tooltip.slavicmyths."+(horn()?"beast_horn":"hero_call")));}
}
