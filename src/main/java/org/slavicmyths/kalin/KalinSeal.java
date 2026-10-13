package org.slavicmyths.kalin;
import java.util.List;import net.minecraft.network.chat.Component;import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.item.*;import net.minecraft.world.level.Level;
public final class KalinSeal extends Item {
 public KalinSeal(){super(new Properties().stacksTo(1));}
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){var s=p.getItemInHand(hand);if(p instanceof ServerPlayer owner){boolean ok=KalinEncounter.start(owner);p.getCooldowns().addCooldown(this,ok?100:20);if(ok)p.playSound(KalinContent.sound("battle_seal_use"),1,1);}return InteractionResultHolder.sidedSuccess(s,w.isClientSide);}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){lines.add(Component.translatable("tooltip.slavicmyths.kalin_battle_seal"));}
}
