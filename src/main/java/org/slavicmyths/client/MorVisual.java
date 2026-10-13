package org.slavicmyths.client;

import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.entity.LandSpiritEntity;

/** Three discrete texture stages; ordinary assets are never changed. */
public final class MorVisual {
    public static boolean shown(LandSpiritEntity e){return e.morState()==2||e.morState()==1&&e.morRemaining()<=34;}
    public static ResourceLocation texture(LandSpiritEntity e,String name){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/mor/"+name+(e.morState()==1&&e.morRemaining()>15?"_forming":"")+".png");}
    public static float transform(LandSpiritEntity e){return e.morState()==1?(50-e.morRemaining())/50F:0;}
    private MorVisual(){}
}
