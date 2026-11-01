package org.slavicmyths.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.sisters.SisterBoss;

/** Different material atlases and distinct actual geometry for each approved sister. */
public final class SistersRenderer extends MobRenderer<SisterBoss,SistersModel> {
    private static final ResourceLocation G=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/gneteya.png");
    private static final ResourceLocation T=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/tryaseya.png");
    private final boolean tryaseya;
    public SistersRenderer(EntityRendererProvider.Context ctx,boolean tryaseya) {
        super(ctx,new SistersModel(tryaseya),tryaseya?.55F:1.05F);
        this.tryaseya=tryaseya;
    }
    @Override public ResourceLocation getTextureLocation(SisterBoss boss) {return tryaseya?T:G;}
    @Override protected RenderType getRenderType(SisterBoss e,boolean visible,boolean translucent,boolean glowing) {return tryaseya?RenderType.entityTranslucent(T):super.getRenderType(e,visible,translucent,glowing);}
}
