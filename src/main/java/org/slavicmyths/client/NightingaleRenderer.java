package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.bandit.NightingaleEntity;
public final class NightingaleRenderer extends MobRenderer<NightingaleEntity,NightingaleModel> {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/nightingale.png");
    public NightingaleRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context manager){super(manager,new NightingaleModel(),.7F);}
    @Override public ResourceLocation getTextureLocation(NightingaleEntity entity){return TEXTURE;}
    @Override protected float getFlipDegrees(NightingaleEntity entity){return 0;} // model owns the collapse
}
