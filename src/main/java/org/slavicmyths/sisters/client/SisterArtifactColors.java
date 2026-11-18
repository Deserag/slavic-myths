package org.slavicmyths.sisters.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slavicmyths.sisters.SisterArtifacts;

/** The real pixel icon is determined by the distinct installed sister powers. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class SisterArtifactColors {
    private static final ResourceLocation VISUAL=ResourceLocation.fromNamespaceAndPath("slavicmyths","sisters_visual");
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for(String id:SisterArtifacts.TYPES) {
                ItemProperties.register(SisterArtifacts.ITEMS.get(id).get(),VISUAL,(stack,level,entity,seed)->{
                    int mask=0;
                    for(String power:SisterArtifacts.powers(stack)) {
                        int index=SisterArtifacts.IDS.indexOf(power);
                        if(index>=0)mask|=1<<index;
                    }
                    return mask;
                });
            }
        });
    }
    private SisterArtifactColors(){}
}
