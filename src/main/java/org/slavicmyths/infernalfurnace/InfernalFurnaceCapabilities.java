package org.slavicmyths.infernalfurnace;
import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.neoforge.capabilities.*;import net.neoforged.neoforge.items.wrapper.*;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class InfernalFurnaceCapabilities {
 @SubscribeEvent public static void register(RegisterCapabilitiesEvent e){e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,InfernalFurnace.TILE.get(),(tile,side)->side==null?new InvWrapper(tile):new SidedInvWrapper(tile,side));}
 private InfernalFurnaceCapabilities(){}
}
