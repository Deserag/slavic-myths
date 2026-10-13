package org.slavicmyths.industry;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
public class IndustryItem extends Item {
    private final String name;
    public IndustryItem(Properties properties,String name){super(properties);this.name=name;}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){lines.add(Component.translatable("industry.slavicmyths.hint."+name));}
}
