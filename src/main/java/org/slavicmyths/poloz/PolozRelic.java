package org.slavicmyths.poloz;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
/** Collection/crafting items explain their current acquisition and purpose. */
public final class PolozRelic extends Item {
 private final String key;
 public PolozRelic(String key,Properties properties){super(properties);this.key=key;}
 @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){lines.add(Component.translatable("poloz.relic."+key));}
}
