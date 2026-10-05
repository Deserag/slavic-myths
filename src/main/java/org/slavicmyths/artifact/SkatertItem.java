package org.slavicmyths.artifact;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionResult;
public final class SkatertItem extends BlockItem {
 public SkatertItem(Properties p){super(org.slavicmyths.registry.ModBlocks.SKATERT.get(),p.stacksTo(1));}
 public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext c){if(!c.getLevel().isClientSide&&org.slavicmyths.item.ItemState.clothReady(c.getItemInHand())>ArtifactEvents.now(c.getLevel())){if(c.getPlayer()!=null)c.getPlayer().displayClientMessage(net.minecraft.network.chat.Component.translatable("artifact.slavicmyths.resting"),true);return InteractionResult.FAIL;}int before=c.getItemInHand().getCount();InteractionResult result=super.useOn(c);if(!c.getLevel().isClientSide&&result.consumesAction()&&c.getPlayer()!=null&&c.getPlayer().isCreative()&&c.getItemInHand().getCount()==before)c.getItemInHand().shrink(1);return result;}
}
