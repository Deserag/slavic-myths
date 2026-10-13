package org.slavicmyths.yaga;
import java.util.List;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
public final class YagaSpawnEgg extends net.neoforged.neoforge.common.DeferredSpawnEggItem {
 public YagaSpawnEgg(Item.Properties p){super(org.slavicmyths.registry.ModEntities.BABA_YAGA,0x493B33,0x8D493B,p);}
 @Override public void appendHoverText(ItemStack s,net.minecraft.world.item.Item.TooltipContext w,List<Component> lines,net.minecraft.world.item.TooltipFlag flag){super.appendHoverText(s,w,lines,flag);lines.add(Component.translatable(getDescriptionId()+".effect").withStyle(ChatFormatting.GRAY));}
}
