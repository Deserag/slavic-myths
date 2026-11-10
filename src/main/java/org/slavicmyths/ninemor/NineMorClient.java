package org.slavicmyths.ninemor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
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
        event.registerEntityRenderer(NineMorContent.SKILL.get(), SkillRenderer::new);
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

    /** Visible, animated 3D reward meshes replace one-frame particle bursts. */
    private static final class SkillRenderer extends ThrownItemRenderer<NineMorSkillEffect> {
        private static final ItemStack SPEAR=new ItemStack(NineMorContent.SPEAR.get());
        private static final ItemStack AXE=new ItemStack(NineMorContent.AXE.get());
        private static final ItemStack SHIELD=new ItemStack(NineMorContent.SHIELD.get());
        private SkillRenderer(EntityRendererProvider.Context c){super(c);}
        private void mesh(PoseStack pose,MultiBufferSource buffers,int light,ItemStack stack,ResourceLocation id) {
            var client=Minecraft.getInstance();
            client.getItemRenderer().render(stack,ItemDisplayContext.NONE,false,pose,buffers,light,OverlayTexture.NO_OVERLAY,
                client.getModelManager().getModel(ModelResourceLocation.inventory(id)));
        }
        @Override public void render(NineMorSkillEffect fx,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
            float age=fx.tickCount+partial;
            pose.pushPose();
            switch(fx.mode()){
                case NineMorSkillEffect.SWEEP -> {
                    for(int i=0;i<4;i++){
                        float angle=age*.40F+i*(float)Math.PI*.5F;
                        pose.pushPose();
                        pose.translate(Math.cos(angle)*2.1D,.15D+Math.sin(age*.25F+i)*.18D,Math.sin(angle)*2.1D);
                        pose.mulPose(Axis.YP.rotation(angle));
                        pose.mulPose(Axis.ZP.rotationDegrees(62F));
                        pose.scale(.56F,.56F,.56F);
                        mesh(pose,buffers,light,SPEAR,NineMorContent.id("nine_mor_spear"));
                        pose.popPose();
                    }
                }
                case NineMorSkillEffect.RIFT -> {
                    pose.mulPose(Axis.YP.rotationDegrees(-fx.getYRot()));
                    for(int i=-2;i<=2;i++){
                        pose.pushPose();
                        pose.translate(i*.86D,.3D+Math.sin(age*.55F+i)*.17D,0D);
                        pose.mulPose(Axis.XP.rotationDegrees(58F));
                        pose.mulPose(Axis.YP.rotationDegrees(age*13F+i*12F));
                        pose.scale(.48F,.48F,.48F);
                        mesh(pose,buffers,light,AXE,NineMorContent.id("nine_mor_axe"));
                        pose.popPose();
                    }
                }
                case NineMorSkillEffect.EXECUTION -> {
                    float descent=age<10?0F:Math.min(1F,(age-10F)/7F);
                    pose.translate(0D,3.1D-descent*2.65D,0D);
                    pose.mulPose(Axis.YP.rotationDegrees(age*6F));
                    pose.mulPose(Axis.XP.rotationDegrees(-40F+descent*120F));
                    pose.scale(1.25F,1.25F,1.25F);
                    mesh(pose,buffers,light,AXE,NineMorContent.id("nine_mor_axe"));
                }
                case NineMorSkillEffect.DOME -> {
                    for(int i=0;i<15;i++){
                        float angle=age*.047F+i*2F*(float)Math.PI/15F;
                        int tier=i%3;
                        float radius=tier==0?2.7F:tier==1?2.25F:1.55F;
                        float height=tier==0?-.5F:tier==1?.75F:1.75F;
                        pose.pushPose();
                        pose.translate(Math.cos(angle)*radius,height,Math.sin(angle)*radius);
                        pose.mulPose(Axis.YP.rotationDegrees(90F-(float)Math.toDegrees(angle)));
                        pose.mulPose(Axis.XP.rotationDegrees(tier==2?-42F:12F));
                        pose.scale(.47F,.47F,.47F);
                        mesh(pose,buffers,light,SHIELD,NineMorContent.id("nine_mor_shield"));
                        pose.popPose();
                    }
                }
                default -> {pose.popPose();super.render(fx,yaw,partial,pose,buffers,light);return;}
            }
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
