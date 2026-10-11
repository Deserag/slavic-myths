package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.nochnitsa.*;
import org.slavicmyths.nochnitsa.NochnitsaRules.State;

@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class NochnitsaClient {
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(NochnitsaContent.ENTITY.get(),Renderer::new);}
    public static final class Renderer extends MobRenderer<NochnitsaEntity,Model>{
        public Renderer(EntityRendererProvider.Context c){super(c,new Model(),.35F);}
        public ResourceLocation getTextureLocation(NochnitsaEntity e){return NochnitsaContent.id("textures/entity/nochnitsa.png");}
        protected float getFlipDegrees(NochnitsaEntity e){return 0;}
        protected RenderType getRenderType(NochnitsaEntity e,boolean visible,boolean translucent,boolean glowing){return e.state()==State.DAWN?RenderType.entityTranslucent(getTextureLocation(e)):super.getRenderType(e,visible,translucent,glowing);}
        @Override public void render(NochnitsaEntity e,float yaw,float partial,PoseStack p,MultiBufferSource b,int light){super.render(e,yaw,partial,p,b,light);if(net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(e.position())>48*48)return;float elapsed=(float)(e.level().getGameTime()-e.screamVisualStart)+partial;if(elapsed<0||elapsed>28)return;
            var vertices=b.getBuffer(RenderType.entityTranslucent(NochnitsaContent.id("textures/entity/nochnitsa_wave.png")));for(int ring=0;ring<3;ring++){float t=elapsed-ring*5;if(t<0||t>=18)continue;float radius=.4F+t/18*10,alpha=.35F*(1-t/18),width=.045F;for(int i=0;i<32;i++){double a=i*Math.PI*2/32,c=(i+1)*Math.PI*2/32;float[][] points={{(float)Math.cos(a)*(radius-width),(float)Math.sin(a)*(radius-width)},{(float)Math.cos(a)*(radius+width),(float)Math.sin(a)*(radius+width)},{(float)Math.cos(c)*(radius+width),(float)Math.sin(c)*(radius+width)},{(float)Math.cos(c)*(radius-width),(float)Math.sin(c)*(radius-width)}};for(int j=0;j<4;j++)vertices.addVertex(p.last().pose(),points[j][0],1.7F,points[j][1]).setColor(1F,1F,1F,alpha).setUv(j==0||j==3?0:1,j<2?0:1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(p.last(),0,1,0);}}
        }
    }
    public static final class Model extends EntityModel<NochnitsaEntity>{
        public final ModelPart root=NochnitsaMesh.create();public final Map<String,ModelPart> nodes=NochnitsaMesh.index(root);private float alpha=1;
        private static final class Stride{double x,z;float phase,weight,age;boolean ready;}
        private final Map<NochnitsaEntity,Stride> strides=new WeakHashMap<>();
        ModelPart n(String name){return nodes.get(name);}static float rad(double degrees){return (float)(degrees*Math.PI/180);}
        static float ramp(float t,float a,float b){float u=Math.clamp((t-a)/(b-a),0,1);return u*u*(3-2*u);}
        static float key(float t,float[] times,float[] values){if(t<=times[0])return values[0];for(int i=1;i<times.length;i++)if(t<=times[i])return values[i-1]+(values[i]-values[i-1])*ramp(t,times[i-1],times[i]);return values[values.length-1];}
        @Override public void setupAnim(NochnitsaEntity e,float swing,float amount,float age,float yaw,float pitch){float partial=age-e.tickCount;var s=strides.computeIfAbsent(e,k->new Stride());double x=Mth.lerp(partial,e.xo,e.getX()),z=Mth.lerp(partial,e.zo,e.getZ());float dt=s.ready?Math.max(0,age-s.age):0;double distance=s.ready?Math.min(.5,Math.hypot(x-s.x,z-s.z)):0;s.phase+=distance/.8F;if(dt>0)s.weight+=(Math.max(amount,(float)Math.min(1,distance/dt*10))-s.weight)*Math.min(1,dt*.35F);s.x=x;s.z=z;s.age=age;s.ready=true;pose(e.state(),e.age(partial),s.phase,s.weight,age,yaw,pitch);}
        /** Real runtime pose entry, also used by native model regression tests. Gait is distance/.8. */
        public void pose(State state,float t,float gait,float movement,float age,float yaw,float pitch){root.getAllParts().forEach(p->{p.resetPose();p.visible=true;});alpha=state==State.DAWN?1-Math.clamp(t/30,0,1):1;boolean moving=state==State.WANDER||state==State.PURSUE;float weight=moving?Math.clamp(movement,0,1):0;float wave=(float)Math.cos(gait*Math.PI*2),stride=wave*weight;boolean chase=state==State.PURSUE;
            n("spine_upper").xRot=rad(chase?32:24);n("spine_upper").xScale=1+(float)Math.sin(age*Math.PI*2/64)*.025F*(1-weight);n("head").yRot=rad(Math.clamp(yaw,-40,40)-4);n("head").xRot=(chase?-.45F:-.30F)+rad(Math.clamp(pitch,-20,20))*.5F;n("neck").xRot=-.087F;
            for(int side:new int[]{-1,1}){String tag=side<0?"r":"l";float sign=side*stride;n("thigh_"+tag).xRot=rad(sign*(chase?25:16));n("knee_"+tag).xRot=rad(Math.max(0,-sign)*(chase?28:15));n("ankle_"+tag).xRot=-n("thigh_"+tag).xRot*.25F-n("knee_"+tag).xRot*.4F;n("shoulder_"+tag).xRot=rad(-sign*(chase?18:8));n("elbow_"+tag).xRot=-.45F;n("wrist_"+tag).zRot=rad(sign*8);}
            n("pelvis").y-=(float)Math.abs(Math.sin(gait*Math.PI*2))*(chase?.2F:.13F)*weight;n("pelvis").zRot=stride*.035F;
            for(var entry:nodes.entrySet()){String name=entry.getKey();if(name.startsWith("hair_")&&!name.equals("hair_cap")){float lag=(float)Math.sin(age*.075+name.hashCode()*.01);entry.getValue().xRot+=rad(lag*(name.endsWith("_tip")?3:1.5)+(chase?-13*weight:0));}if(name.startsWith("skirt_")){entry.getValue().xRot+=rad((float)Math.sin(gait*Math.PI*2-.5+name.hashCode()%3)*weight*(chase?10:4)+(float)Math.sin(age*.05)*2);}}
            switch(state){
                case HEAD_SNAP->{n("head").yRot+=rad(key(t,new float[]{0,4,7,12,16},new float[]{0,18,-14,-7,0}));}
                case SPOT->{n("head").xRot-=rad(8*ramp(t,0,7));n("neck").xRot-=rad(3*ramp(t,0,7));}
                case CLAW_LEFT,CLAW_RIGHT->{String tag=state==State.CLAW_LEFT?"l":"r";float pull=key(t,new float[]{0,4,8,10,12,16},new float[]{0,-.65F,-1.6F,.6F,.35F,0});n("shoulder_"+tag).xRot=pull;n("shoulder_"+tag).yRot=key(t,new float[]{0,4,8,10,16},new float[]{0,-.2F,-.65F,.5F,0})*(tag.equals("l")?1:-1);n("elbow_"+tag).xRot=rad(35)*(ramp(t,0,4)-ramp(t,10,16));n("wrist_"+tag).xRot=-rad(24)*(ramp(t,4,8)-ramp(t,12,16));n("spine_upper").yRot=rad(12)*(tag.equals("l")?1:-1)*(ramp(t,2,8)-ramp(t,10,16));n("head").zRot=rad(5)*(tag.equals("l")?1:-1)*(ramp(t,4,6)-ramp(t,10,16));for(int i=1;i<=4;i++)n("claw_"+tag+"_"+i).zRot+=(i-2.5F)*rad(6)*(ramp(t,4,8)-ramp(t,12,16));}
                case WINDUP,RELEASE->{float charge=state==State.WINDUP?key(t,new float[]{0,6,12,20,26,30},new float[]{0,.31F,.64F,1,1,1}):1-ramp(t,3,12);n("spine_upper").xRot=rad(11-6*charge);n("head").xRot=-rad(28)*charge;n("jaw").xRot=.5F*charge;for(int side:new int[]{-1,1}){String tag=side<0?"r":"l";n("shoulder_"+tag).zRot=-side*rad(state==State.RELEASE?85:70)*charge;n("elbow_"+tag).xRot=rad(15)*charge;n("wrist_"+tag).xRot=-rad(18)*charge;n("shoulder_"+tag).y-=.4F*charge;for(int i=1;i<=4;i++){n("claw_"+tag+"_"+i).xRot=-.2F+rad(15)*charge;n("claw_"+tag+"_"+i).zRot+=(i-2.5F)*rad(7)*charge;}}for(var entry:nodes.entrySet())if(entry.getKey().startsWith("hair_")&&!entry.getKey().equals("hair_cap"))entry.getValue().xRot+=rad(12)*charge;}
                case HURT->{float kick=1-ramp(t,0,10);n("spine_upper").xRot-=rad(12)*kick;n("head").zRot=rad(8)*kick;n("pelvis").z+=.4F*kick;}
                case DAWN->{float recoil=ramp(t,0,12);n("head").xRot=-rad(17)*recoil;n("spine_upper").xRot-=rad(8)*recoil;n("shoulder_l").xRot=-.45F*recoil;n("shoulder_r").xRot=-.25F*recoil;}
                case DEATH->{float fall=ramp(t,0,22);n("knee_l").xRot=n("knee_r").xRot=rad(65)*fall;n("thigh_l").xRot=n("thigh_r").xRot=-rad(45)*fall;n("spine_upper").xRot+=rad(40)*fall;n("head").xRot+=rad(25)*fall;n("pelvis").y+=6*fall;n("pelvis").zRot=rad(14)*fall;n("eye_l").visible=n("eye_r").visible=t<20;}
                default->{}
            }
        }
        @Override public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,int color){int argb=(color&0xffffff)|((int)(((color>>>24)&255)*alpha)<<24);n("eye_l").skipDraw=n("eye_r").skipDraw=true;root.render(p,v,light,overlay,argb);root.getAllParts().forEach(n->n.skipDraw=true);n("eye_l").skipDraw=n("eye_r").skipDraw=false;root.render(p,v,LightTexture.FULL_BRIGHT,overlay,argb);root.getAllParts().forEach(n->n.skipDraw=false);}
    }
    private NochnitsaClient(){}
}
