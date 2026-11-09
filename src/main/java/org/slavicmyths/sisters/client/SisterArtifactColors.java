package org.slavicmyths.sisters.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.slavicmyths.sisters.SisterArtifacts;

/** Each of the three real cuboid sockets tints independently from the actual ItemStack state. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class SisterArtifactColors {
    private static final int[] PALETTE={0xFF9B63D7,0xFFC5CCD6,0xFF6BCBF3,0xFFFF8B33,0xFF6D428A,0xFF85818D};
    @SubscribeEvent public static void register(RegisterColorHandlersEvent.Item event){
        event.register((stack,index)->{
            if(index<0||index>=3)return 0xFFFFFFFF;
            var powers=SisterArtifacts.powers(stack);
            if(index>=powers.size())return 0xFF272430;
            int color=SisterArtifacts.IDS.indexOf(powers.get(index));
            return color<0?0xFF272430:PALETTE[color];
        },SisterArtifacts.ITEMS.get("sisters_ring").get(),SisterArtifacts.ITEMS.get("sisters_necklace").get(),
          SisterArtifacts.ITEMS.get("sisters_belt").get(),SisterArtifacts.ITEMS.get("sisters_crown").get(),
          SisterArtifacts.ITEMS.get("sisters_brooch").get());
    }
    private SisterArtifactColors(){}
}
