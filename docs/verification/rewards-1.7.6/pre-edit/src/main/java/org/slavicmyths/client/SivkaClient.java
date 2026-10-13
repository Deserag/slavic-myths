package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.sivka.*;

@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class SivkaClient {
    public static final KeyMapping DOWN=new KeyMapping("key.slavicmyths.sivka_descend",88,"key.categories.slavicmyths");
    private static int sequence;private static SivkaEntity hinted;
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(mc.player==null||!(mc.player.getVehicle() instanceof SivkaEntity h)){hinted=null;return;}
        if(hinted!=h){hinted=h;mc.player.displayClientMessage(net.minecraft.network.chat.Component.translatable("sivka.slavicmyths.controls"),true);}
        if(mc.player.tickCount%2!=0)return;boolean enabled=mc.screen==null&&mc.isWindowActive();
        PacketDistributor.sendToServer(new SivkaNetwork.Input(h.getId(),++sequence,enabled?(mc.options.keyUp.isDown()?1:0)-(mc.options.keyDown.isDown()?1:0):0,enabled?(mc.options.keyLeft.isDown()?1:0)-(mc.options.keyRight.isDown()?1:0):0,mc.player.getYRot(),enabled&&mc.options.keySprint.isDown(),enabled&&mc.options.keyJump.isDown(),enabled&&DOWN.isDown()));
    }
    @EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Setup {
        @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(DOWN);}
        @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(SivkaContent.ENTITY.get(),Renderer::new);}
    }
    public static final class Renderer extends MobRenderer<SivkaEntity,Model> {
        public Renderer(EntityRendererProvider.Context c){super(c,new Model(),1.2F);addLayer(new Glow(this));}
        @Override public ResourceLocation getTextureLocation(SivkaEntity h){return SivkaContent.id("textures/entity/sivka_burka.png");}
        @Override protected RenderType getRenderType(SivkaEntity h,boolean visible,boolean translucent,boolean glowing){return h.reaction()==6?RenderType.entityTranslucent(getTextureLocation(h)):super.getRenderType(h,visible,translucent,glowing);}
        @Override protected float getFlipDegrees(SivkaEntity h){return 0;}
    }
    private static final class Glow extends RenderLayer<SivkaEntity,Model> {
        Glow(RenderLayerParent<SivkaEntity,Model> p){super(p);}
        @Override public void render(PoseStack p,MultiBufferSource b,int light,SivkaEntity h,float walk,float amount,float partial,float age,float yaw,float pitch){if(h.flying()||h.reaction()==1||h.reaction()==3)getParentModel().renderToBuffer(p,b.getBuffer(RenderType.eyes(SivkaContent.id("textures/entity/sivka_burka_glow.png"))),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0xffffffff);}
    }
    public static final class Model extends EntityModel<SivkaEntity> {
        public final ModelPart root=SivkaMesh.create();public final Map<String,ModelPart> nodes=SivkaMesh.index(root);
        private static final class Motion {float locomotion,airBlend,previousAge=-1;}
        private final Map<SivkaEntity,Motion> motion=new WeakHashMap<>();private float alpha=1;
        ModelPart n(String id){return nodes.get(id);}static float rad(float degree){return degree*(float)Math.PI/180;}
        @Override public void setupAnim(SivkaEntity h,float walk,float amount,float age,float yaw,float pitch){root.getAllParts().forEach(ModelPart::resetPose);root.y=24;float partial=age-h.tickCount,u=h.gait()+partial/(h.flying()?14.4F:12.6F),speed=h.actualSpeed();var state=motion.computeIfAbsent(h,k->new Motion());float delta=state.previousAge<0?1:Math.clamp(age-state.previousAge,0,1);state.previousAge=age;state.locomotion+=((Math.clamp(speed/3,0,1))-state.locomotion)*.2F*delta;state.airBlend+=((h.flying()?1:0)-state.airBlend)*.3F*delta;float locomotion=state.locomotion,airBlend=state.airBlend;alpha=h.reaction()==6?1-Math.clamp((h.reactionAge()+partial-40)/8,0,1):1;
            boolean air=h.flying();float fast=Math.clamp((speed-6)/3,0,1);float[] offsets={0,.5F,.75F,.25F};
            for(int i=0;i<4;i++){float phase=u+(speed>3.2?i==0||i==3?0:.5F:offsets[i]);float swing=(float)Math.sin(phase*Math.PI*2)*rad(speed>3.2?25:18),knee=Math.max(0,(float)Math.sin(phase*Math.PI*2))*rad(speed>3.2?36:24);float key=SivkaPoses.sample(air,u,3+i),bend=SivkaPoses.sample(air,u,i<2?7:8);float blend=air?1:fast;
                n("leg"+i).xRot=(swing*(1-blend)+rad(key)*blend)*Math.max(locomotion,airBlend);n("knee"+i).xRot=(knee*(1-blend)+rad(bend)*blend)*Math.max(locomotion,airBlend);n("hoof"+i).xRot=-n("knee"+i).xRot*.35F;
            }
            float bodyPitch=rad(SivkaPoses.sample(air,u,1))*(air?1:fast*locomotion);n("body").xRot=-bodyPitch;n("body").zRot=rad(h.bank());float bob=air?SivkaPoses.sample(true,u,2):SivkaPoses.sample(false,u,2)*fast*locomotion;n("body").y-=bob*16;n("body").xScale=1+(float)Math.sin(age*.08)*.009F*(1-locomotion);
            n("neck").xRot=bodyPitch*.6F+rad((float)Math.sin(age*.12)*1.5F)*(1-locomotion);n("head").yRot=rad(Math.clamp(yaw,-20,20));n("head").xRot=rad(Math.clamp(pitch,-10,10))*.4F;
            n("body").xRot-=rad(h.lift()*50);float action=h.reactionAge()+partial;
            switch(h.reaction()){
                case 1->{n("head").xRot+=rad(9);n("jaw").xRot=rad(5)*(float)Math.abs(Math.sin(action*Math.PI/4));n("ear_-1").zRot=rad(7);}
                case 2->n("head").yRot+=rad(19)*(float)Math.sin(Math.clamp(action/18,0,1)*Math.PI);
                case 3->{n("head").xRot-=rad(6);n("leg0").xRot-=rad(12)*(float)Math.sin(action/26*Math.PI);}
                case 4,5->n("head").xRot+=rad(4)*(float)Math.sin(action*.2);
                case 6->n("head").yRot=rad(14)*(float)Math.sin(Math.min(1,action/25)*Math.PI);
                case 7->{n("neck").xRot+=rad(30);n("head").xRot+=rad(18);}
                case 8->{n("neck").xRot-=rad(9);n("ear_-1").xRot=n("ear_1").xRot=rad(-15);}
            }
            if(h.mode()==1){float t=Math.clamp((h.modeAge()+partial)/16,0,1);n("body").xRot-=rad(14)*(float)Math.sin(t*Math.PI);for(int i=0;i<4;i++)n("knee"+i).xRot+=rad(i<2?25:35)*(float)Math.sin(t*Math.PI);}
            if(h.mode()==3){float t=h.modeAge()+partial;for(int i=0;i<4;i++)n("knee"+i).xRot+=rad(18)*(float)Math.sin(Math.clamp((t-(i<2?0:3))/17,0,1)*Math.PI);}
            for(int i=0;i<12;i++){float wave=(float)Math.sin((u-i*.028)*Math.PI*2);n("mane"+i).xRot=rad(18+Math.min(22,speed*1.3F)+wave*(air?10:4));n("maneTip"+i).zRot=rad((i%2==0?-1:1)*(4+wave*4));}
            poseTail(age,speed);n("forelock").xRot=rad((float)Math.sin(u*Math.PI*2-.4)*5);
            boolean blink=(h.tickCount+Math.abs(h.getId()*17))%137<3;n("eye_-1").visible=n("eye_1").visible=!blink;
            if(h.reaction()==0){float twitch=(age+h.getId()*11)%163;if(twitch<10)n("ear_-1").zRot=rad(12)*(float)Math.sin(twitch/10*Math.PI);twitch=(age+h.getId()*7)%197;if(twitch<9)n("ear_1").zRot=-rad(10)*(float)Math.sin(twitch/9*Math.PI);}
        }
        public void poseTail(float age,float speed){n("tail").xRot=rad(Math.min(25,speed*1.5F));n("tail").yRot=rad((float)Math.sin(age*.032)*6);n("tailMid").xRot=rad((float)Math.sin(age*.10-.8)*4);for(int i=0;i<3;i++)n("tailTip"+i).xRot=0;}
        @Override public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,int color){int a=(int)(((color>>>24)&255)*alpha);root.render(p,v,light,overlay,(color&0xffffff)|(a<<24));}
    }
    private SivkaClient(){}
}
