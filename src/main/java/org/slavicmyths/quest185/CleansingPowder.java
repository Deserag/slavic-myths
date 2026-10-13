package org.slavicmyths.quest185;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
public final class CleansingPowder extends Item {
 public CleansingPowder(){super(new Properties());}
 @Override public InteractionResult useOn(UseOnContext c){if(c.getLevel().isClientSide)return InteractionResult.SUCCESS;if(!(c.getPlayer() instanceof ServerPlayer p))return InteractionResult.PASS;if(!CleansingTasks.get(p.serverLevel()).start(p,c.getClickedPos(),c.getClickedFace(),c.getItemInHand()))return InteractionResult.FAIL;p.swing(c.getHand(),true);if(!c.getItemInHand().isEmpty())p.startUsingItem(c.getHand());return InteractionResult.CONSUME;}
 @Override public int getUseDuration(ItemStack s,net.minecraft.world.entity.LivingEntity e){return 6;}
 @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.BRUSH;}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){lines.add(Quest185Story.text("powder_hint"));}
}
