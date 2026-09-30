package org.slavicmyths.artifact;
import net.minecraft.item.*;
import net.minecraft.util.ActionResultType;
public final class SkatertItem extends BlockItem {
 public SkatertItem(Properties p){super(org.slavicmyths.registry.ModBlocks.SKATERT.get(),p.stacksTo(1));}
 public ActionResultType useOn(ItemUseContext c){if(!c.getLevel().isClientSide&&c.getItemInHand().getOrCreateTag().getLong("SlavicClothReady")>ArtifactEvents.now(c.getLevel())){if(c.getPlayer()!=null)c.getPlayer().displayClientMessage(new net.minecraft.util.text.TranslationTextComponent("artifact.slavicmyths.resting"),true);return ActionResultType.FAIL;}int before=c.getItemInHand().getCount();ActionResultType result=super.useOn(c);if(!c.getLevel().isClientSide&&result.consumesAction()&&c.getPlayer()!=null&&c.getPlayer().isCreative()&&c.getItemInHand().getCount()==before)c.getItemInHand().shrink(1);return result;}
}
