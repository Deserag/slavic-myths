package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.bandit.NightingaleEntity;
public final class NightingaleRenderer extends MobRenderer<NightingaleEntity,NightingaleModel> {
    private static final ResourceLocation TEXTURE=new ResourceLocation("slavicmyths","textures/entity/nightingale.png");
    public NightingaleRenderer(EntityRendererManager manager){super(manager,new NightingaleModel(),.7F);}
    @Override public ResourceLocation getTextureLocation(NightingaleEntity entity){return TEXTURE;}
    @Override protected float getFlipDegrees(NightingaleEntity entity){return 0;} // model owns the collapse
}
