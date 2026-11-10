package org.slavicmyths.ninemor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.math.Axis;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Uses existing dark-stone texture atlas; accent crowns are tinted per attack type. */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class NineMorClient {
    private static final ResourceLocation SKIN =
        NineMorContent.id("textures/entity/nine_mor_white.png");

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NineMorContent.BOSS.get(), BossRenderer::new);
        event.registerEntityRenderer(NineMorContent.SHOT.get(), SummonedSpearRenderer::new);
    }

    /** Player-summoned spears render as full-size authored 3D items, never as flat firework sprites. */
    private static final class SummonedSpearRenderer extends ThrownItemRenderer<NineMorShot> {
        private static final ResourceLocation SPEAR_MODEL=NineMorContent.id("nine_mor_spear");
        private SummonedSpearRenderer(EntityRendererProvider.Context context){super(context);}
        @Override public void render(NineMorShot shot,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
            if(!shot.isSummonedSpear()){super.render(shot,yaw,partial,pose,buffers,light);return;}
            pose.pushPose();
            float age=shot.tickCount+partial;
            pose.translate(0D,.12D+(!shot.spearLaunched()?Math.sin(age*.22F)*.07D:0D),0D);
            Vec3 motion=shot.getDeltaMovement();
            if(shot.spearLaunched()&&motion.lengthSqr()>.001D){
                Vector3f direction=new Vector3f((float)motion.x,(float)motion.y,(float)motion.z).normalize();
                pose.mulPose(new Quaternionf().rotationTo(new Vector3f(0,1,0),direction));
            }else{
                pose.mulPose(Axis.YP.rotationDegrees(age*2.5F));
                pose.mulPose(Axis.XP.rotationDegrees(7F));
            }
            pose.scale(1.28F,1.28F,1.28F);
            var client=Minecraft.getInstance();
            client.getItemRenderer().render(new ItemStack(NineMorContent.SPEAR.get()),ItemDisplayContext.NONE,false,
                pose,buffers,light,OverlayTexture.NO_OVERLAY,
                client.getModelManager().getModel(ModelResourceLocation.inventory(SPEAR_MODEL)));
            pose.popPose();
        }
    }

    private static final class BossRenderer extends MobRenderer<NineMorSerpent, NineMorModel> {
        private static final float MODEL_SCALE = 4.0F;
        private BossRenderer(EntityRendererProvider.Context context) {
            super(context, new NineMorModel(), 0.9F);
            addLayer(new RenderLayer<NineMorSerpent, NineMorModel>(this) {
                @Override
                public void render(PoseStack pose, MultiBufferSource buffers, int packedLight,
                                   NineMorSerpent boss, float limbSwing, float limbSwingAmount,
                                   float partialTick, float age, float netHeadYaw, float headPitch) {
                    getParentModel().renderColoredParts(pose,
                        buffers.getBuffer(RenderType.entityCutoutNoCull(SKIN)),
                        packedLight, OverlayTexture.NO_OVERLAY);
                    getParentModel().renderEmissiveEyes(pose,
                        buffers.getBuffer(RenderType.eyes(SKIN)));
                }
            });
        }

        @Override public ResourceLocation getTextureLocation(NineMorSerpent boss) { return SKIN; }

        @Override protected void scale(NineMorSerpent boss, PoseStack pose, float partialTick) {
            pose.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        }

        @Override
        public boolean shouldRender(NineMorSerpent boss,
                                    net.minecraft.client.renderer.culling.Frustum frustum,
                                    double x, double y, double z) {
            return frustum.isVisible(boss.getBoundingBox().inflate(30D, 18D, 30D));
        }
    }

    private NineMorClient() {}
}
