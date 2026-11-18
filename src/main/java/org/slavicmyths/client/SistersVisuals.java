package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.sisters.BoneBarrier;
import org.slavicmyths.sisters.FeverSpirit;
import org.slavicmyths.sisters.SisterBolt;
import org.slavicmyths.sisters.SistersZone;

/** Separate original fever sprite rig, real bone-block-looking segments and magic effects. */
public final class SistersVisuals {
    private static final ResourceLocation VIOLET=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/tryaseya_albedo.png");
    public static final class SpiritModel extends EntityModel<FeverSpirit> {
        private final ModelPart root,head,tail,arms;
        public SpiritModel() {
            FolkModelGeometry g=new FolkModelGeometry();
            root=g.part(256,256);
            head=g.part(256,256);head.setPos(0,-7,0);g.attach(root,head);
            g.box(head,128,0,-5,-6,-4,10,10,8);
            g.box(head,192,0,-4,-2,-5,8,3,1);
            for(int i=0;i<5;i++)g.box(head,192,0,-5+i*2,-7,-3,1.5F,4,4);
            tail=g.part(256,256);tail.setPos(0,2,0);g.attach(root,tail);
            g.box(tail,192,0,-6,-1,-5,12,9,10);
            for(int i=0;i<7;i++)g.box(tail,192,0,-4+i*1.3F,6,-3+(i%3),2.2F,7+(i%3)*2,2);
            arms=g.part(256,256);arms.setPos(0,-5,0);g.attach(root,arms);
            g.box(arms,192,0,-10,0,-3,5,12,5);
            g.box(arms,192,0,5,0,-3,5,12,5);
            g.box(arms,128,0,-9,10,-4,3,4,4);
            g.box(arms,128,0,6,10,-4,3,4,4);
        }
        @Override public void setupAnim(FeverSpirit spirit,float walk,float amount,float age,float yaw,float pitch) {
            head.yRot=yaw*(float)Math.PI/180F;head.xRot=pitch*(float)Math.PI/180F;
            tail.yRot=.15F*(float)Math.sin(age*.15);tail.zRot=.12F*(float)Math.sin(age*.12);
            arms.zRot=.1F*(float)Math.sin(age*.22);
        }
        @Override public void renderToBuffer(PoseStack pose,VertexConsumer vertex,int light,int overlay,int color) {root.render(pose,vertex,light,overlay,color);}
    }
    public static final class Spirit extends MobRenderer<FeverSpirit,SpiritModel> {
        public Spirit(EntityRendererProvider.Context context) {super(context,new SpiritModel(),.25F);}
        @Override public ResourceLocation getTextureLocation(FeverSpirit e) {return VIOLET;}
        @Override protected RenderType getRenderType(FeverSpirit e,boolean visible,boolean translucent,boolean glowing) {return RenderType.entityTranslucent(VIOLET);}
    }
    public static final class Wall extends EntityRenderer<BoneBarrier> {
        private final net.minecraft.client.renderer.block.BlockRenderDispatcher blocks;
        public Wall(EntityRendererProvider.Context context) {super(context);blocks=context.getBlockRenderDispatcher();}
        @Override public ResourceLocation getTextureLocation(BoneBarrier entity) {return TextureAtlas.LOCATION_BLOCKS;}
        @Override public void render(BoneBarrier entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
            pose.pushPose();
            pose.translate(-.48,0,-.48);
            for(int i=0;i<3;i++) {
                pose.pushPose();
                pose.translate(0,i*.91,0);
                blocks.renderSingleBlock(net.minecraft.world.level.block.Blocks.BONE_BLOCK.defaultBlockState(),pose,buffers,light,OverlayTexture.NO_OVERLAY);
                pose.popPose();
            }
            pose.popPose();
            super.render(entity,yaw,partial,pose,buffers,light);
        }
    }
    public static final class Zone extends EntityRenderer<SistersZone> {
        public Zone(EntityRendererProvider.Context context) {super(context);}
        @Override public ResourceLocation getTextureLocation(SistersZone entity) {return VIOLET;}
        @Override public void render(SistersZone entity,float yaw,float partial,PoseStack pose,MultiBufferSource out,int light) { }
    }
    private static final ResourceLocation FROST = ResourceLocation.fromNamespaceAndPath(
        "slavicmyths", "textures/entity/ledeya_frost_bolt.png");

    public static final class Bolt extends EntityRenderer<SisterBolt> {
        public Bolt(EntityRendererProvider.Context context) {super(context);}
        @Override public ResourceLocation getTextureLocation(SisterBolt entity) {
            return entity.getType() == org.slavicmyths.sisters.SistersContent.FROST_BOLT.get() ? FROST : VIOLET;
        }
        @Override public void render(SisterBolt bolt,float yaw,float partial,PoseStack pose,MultiBufferSource out,int light) {
            pose.pushPose();
            pose.mulPose(entityRenderDispatcher.cameraOrientation());
            pose.scale(.45F,.45F,.45F);
            VertexConsumer consumer=out.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(bolt)));
            PoseStack.Pose matrix=pose.last();
            vertex(consumer,matrix,-1,-1,.75F,.02F,light);
            vertex(consumer,matrix,1,-1,.98F,.02F,light);
            vertex(consumer,matrix,1,1,.98F,.25F,light);
            vertex(consumer,matrix,-1,1,.75F,.25F,light);
            pose.popPose();
            super.render(bolt,yaw,partial,pose,out,light);
        }
        private static void vertex(VertexConsumer v,PoseStack.Pose pose,float x,float y,float u,float t,int light) {
            v.addVertex(pose,x,y,0).setColor(255,255,255,255).setUv(u,t).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,1,0);
        }
    }
    private SistersVisuals() {}
}
