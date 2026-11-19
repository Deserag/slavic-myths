package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.marine.PharaohkaContent;
import org.slavicmyths.marine.PharaohkaEntity;

public final class PharaohkaRenderer extends MobRenderer<PharaohkaEntity,PharaohkaModel> {
    private static final ResourceLocation WHITE =
        ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    public PharaohkaRenderer(EntityRendererProvider.Context context,boolean leader) {
        super(context,new PharaohkaModel(leader),leader?.8F:.4F);
    }
    @Override protected void setupRotations(PharaohkaEntity entity,com.mojang.blaze3d.vertex.PoseStack pose,
                                            float age,float bodyYaw,float partialTick,float scaleFactor) {
        super.setupRotations(entity,pose,age,bodyYaw,partialTick,scaleFactor);
        // Idle mermaids stand upright, but turn toward horizontal flight when travelling.
        // Rotate around the central torso rather than the feet, preserving the visual hitbox.
        if (entity.isInWater()) {
            float swimSpeed=(float)entity.getDeltaMovement().horizontalDistance();
            float lean=entity.action()==PharaohkaEntity.DASH?48F:
                       Math.min(34F,swimSpeed*105F);
            if(lean>1.5F) {
                float pivot=entity.getBbHeight()*.45F;
                pose.translate(0,pivot,0);
                pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-lean));
                pose.translate(0,-pivot,0);
            }
        }
    }
    @Override protected void scale(PharaohkaEntity entity,com.mojang.blaze3d.vertex.PoseStack pose,float partialTick) {
        // 63 model pixels from crest to tail fin -> ~3.8 blocks for the leader.
        // Align the lowest fin to the entity's feet, avoiding a buried tail.
        float scale=entity.leader()?.96F:.55F;
        pose.translate(0,(34F*scale-24F)/16F,0);
        pose.scale(scale,scale,scale);
    }
    @Override public ResourceLocation getTextureLocation(PharaohkaEntity entity) {return WHITE;}
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PharaohkaContent.PHARAOHKA.get(),ctx->new PharaohkaRenderer(ctx,false));
        event.registerEntityRenderer(PharaohkaContent.LEADER.get(),ctx->new PharaohkaRenderer(ctx,true));
    }
}