package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.firebird.*;

@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class FirebirdClient {
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(FirebirdContent.ENTITY.get(),Renderer::new);}
    public static final class Renderer extends MobRenderer<FirebirdEntity,Model>{
        Renderer(EntityRendererProvider.Context c){super(c,new Model(),1.1F);addLayer(new Glow(this));}
        @Override public ResourceLocation getTextureLocation(FirebirdEntity b){return FirebirdContent.id("textures/entity/firebird.png");}
        @Override public boolean shouldRender(FirebirdEntity b,Frustum f,double x,double y,double z){return b.shouldRender(x,y,z)&&f.isVisible(b.getBoundingBox().inflate(4));}
    }
    private static final class Glow extends RenderLayer<FirebirdEntity,Model>{
        Glow(RenderLayerParent<FirebirdEntity,Model> p){super(p);}
        @Override public void render(PoseStack p,MultiBufferSource buffers,int light,FirebirdEntity b,float walk,float amount,float partial,float age,float yaw,float pitch){getParentModel().renderToBuffer(p,buffers.getBuffer(RenderType.eyes(FirebirdContent.id("textures/entity/firebird_emissive.png"))),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0xffffffff);}
    }
    public static final class Model extends EntityModel<FirebirdEntity>{
        public final ModelPart root=FirebirdMesh.create();private final Map<String,ModelPart> parts=FirebirdMesh.index(root);
        private ModelPart n(String s){return parts.get(s);}private static float rad(double v){return (float)Math.toRadians(v);}
        private static float key(float u,float[] points){float[] phases={0,.2F,.42F,.56F,.78F,1};u=u-(float)Math.floor(u);for(int i=1;i<phases.length;i++)if(u<=phases[i]){float t=(u-phases[i-1])/(phases[i]-phases[i-1]);t=t*t*(3-2*t);return points[i-1]+(points[i]-points[i-1])*t;}return points[0];}
        @Override public void setupAnim(FirebirdEntity b,float walk,float amount,float age,float yaw,float pitch){root.getAllParts().forEach(ModelPart::resetPose);root.y=24;float action=b.age()+age-b.tickCount;float phase=age/(b.state()==7?27:35);boolean ground=b.state()==1;
            float spread=ground?0:1;if(b.state()==3)spread=Math.clamp(action/12,0,1);if(b.state()==8)spread=1-Math.clamp((action-14)/9,0,1);if(b.state()==0)spread=Math.clamp((float)b.getDeltaMovement().length()*6,0,1);
            float shoulder=key(phase,new float[]{-5,35,-12,-32,-13,-5});float mid=key(phase-.045F,new float[]{8,24,-9,-22,5,8});float tip=key(phase-.095F,new float[]{12,16,7,-15,15,12});
            if(b.state()==9){shoulder=8;mid=0;tip=(float)Math.sin(age*.2)*4;}
            for(int side:new int[]{-1,1}){String s=side<0?"left":"right";n(s+"_shoulder").zRot=rad(side*((shoulder-9)*spread+6*(1-spread)));n(s+"_shoulder").yRot=rad(-side*75*(1-spread));n(s+"_mid").zRot=rad(side*(mid+7)*spread);n(s+"_mid").yRot=rad(-side*55*(1-spread));n(s+"_tip").zRot=rad(side*(tip+10)*spread);n(s+"_tip").yRot=rad(side*30*(1-spread));n(s+"_leg").xRot=rad(-45*spread);n(s+"_ankle").xRot=rad(40*spread);for(int i=0;i<8;i++)n(s+"_feather"+i).zRot=rad(side*Math.sin(age*.15-i*.18)*3*spread);}
            n("head").yRot=rad(Math.clamp(yaw,-16,16));n("head").xRot=rad(4+Math.sin(age*.06)*2);n("body").xRot=rad(ground?0:5);n("neck_upper").xRot=rad(ground?Math.sin(age*.08)*2:-4);
            for(int i=0;i<7;i++){n("tail"+i).xRot+=rad(Math.sin(age*.13-i*.3)*4);n("tail_tip"+i).xRot+=rad(Math.sin(age*.13-i*.3-.4)*7);if(b.state()==4)n("tail"+i).yRot+=rad((i-3)*4*Math.sin(Math.min(1,action/14)*Math.PI));}
            for(int i=0;i<7;i++)n("crest"+i).xRot+=rad(Math.sin(age*.1+i)*3+(b.state()==5?-20:0));
            poseFeet(spread,age,ground);
            if(b.state()==6){float strike=(float)Math.sin(Math.clamp(action/17,0,1)*Math.PI);n("body").xRot+=rad(22*strike);for(String s:List.of("left","right")){n(s+"_leg").xRot=rad(-65*strike);n(s+"_ankle").xRot=rad(30*(1-strike));n(s+"_claws").xRot=rad(-20*strike);for(int j=0;j<3;j++){n(s+"_toe"+j).xRot=rad(-12*strike);n(s+"_talon"+j).xRot+=rad(45*strike);}}}
        }
        public void poseFeet(float spread,float age,boolean ground){for(int side:new int[]{-1,1}){String s=side<0?"left":"right";float wave=(float)Math.sin(age*.12+side*.7);n(s+"_leg").xRot=rad(-48*spread+wave*(ground?3:4));n(s+"_ankle").xRot=rad(72*spread-wave*3);n(s+"_claws").xRot=rad(-32*spread+wave*2);if(ground)n(s+"_leg").y-=Math.max(0,(float)Math.sin(age*.045+side*1.5))*.35F;for(int j=0;j<3;j++){n(s+"_toe"+j).xRot=rad((18+j*3)*spread);n(s+"_talon"+j).xRot+=rad(26*spread);n(s+"_toe"+j).yRot+=(j-1)*rad(ground?4:9);}}}
        @Override public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,int color){root.render(p,v,light,overlay,color);}
    }
}

