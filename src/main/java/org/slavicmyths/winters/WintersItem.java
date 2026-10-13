package org.slavicmyths.winters;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
public final class WintersItem extends Item {
 public WintersItem(){super(new Properties().stacksTo(1).rarity(Rarity.EPIC));}
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player player,InteractionHand hand){
  var stack=player.getItemInHand(hand);if(hand!=InteractionHand.MAIN_HAND)return InteractionResultHolder.pass(stack);
  if(player instanceof ServerPlayer p)WintersEvents.use(p,stack);
  return InteractionResultHolder.sidedSuccess(stack,w.isClientSide);
 }
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){
  lines.add(Component.translatable("winters.slavicmyths."+ (WintersRecipe.original(s)?"original":"copy")));
  lines.add(Component.translatable("winters.slavicmyths.controls"));
  lines.add(Component.translatable("winters.slavicmyths.passive"));
  lines.add(Component.translatable("winters.slavicmyths.mode_query"));
 }
}
