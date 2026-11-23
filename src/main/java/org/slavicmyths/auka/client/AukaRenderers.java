package org.slavicmyths.auka.client;

import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.auka.AukaBoss;
import org.slavicmyths.auka.AukaContent;
import org.slavicmyths.auka.AukaMinion;

public final class AukaRenderers {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/auka.png");
    public static final class Boss extends MobRenderer<AukaBoss,AukaBossModel>{
        public Boss(EntityRendererProvider.Context context){super(context,new AukaBossModel(),1.1F);}
        @Override public ResourceLocation getTextureLocation(AukaBoss entity){return TEXTURE;}
    }
    public static final class Companion extends MobRenderer<AukaMinion,AukaMinionModel>{
        public Companion(EntityRendererProvider.Context context,boolean bear){super(context,new AukaMinionModel(bear),bear?.8F:.5F);}
        @Override public ResourceLocation getTextureLocation(AukaMinion entity){return TEXTURE;}
    }
    public static void register(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(AukaContent.AUKA.get(),Boss::new);
        event.registerEntityRenderer(AukaContent.AUKA_BEAR.get(),ctx->new Companion(ctx,true));
        event.registerEntityRenderer(AukaContent.AUKA_WOLF.get(),ctx->new Companion(ctx,false));
    }
    private AukaRenderers(){}
}
