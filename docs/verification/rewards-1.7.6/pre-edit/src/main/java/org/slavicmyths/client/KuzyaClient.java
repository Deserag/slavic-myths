package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.kuzya.*;

@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class KuzyaClient {
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(KuzyaContent.ENTITY.get(),Renderer::new);}
    public static final class Renderer extends MobRenderer<KuzyaEntity,Model>{
        public Renderer(EntityRendererProvider.Context c){super(c,new Model(),.18F);}
        @Override public ResourceLocation getTextureLocation(KuzyaEntity e){return KuzyaContent.id("textures/entity/kuzya.png");}
        @Override protected float getFlipDegrees(KuzyaEntity e){return 0;}
    }
    public static final class Model extends EntityModel<KuzyaEntity>{
        public final ModelPart root=KuzyaMesh.create();public final Map<String,ModelPart> nodes=KuzyaMesh.index(root);
        private static final class Playback{String clip="";float[] last=new float[15],from=new float[15];double x,z;float gait;boolean ready;}
        private final Map<KuzyaEntity,Playback> playback=new WeakHashMap<>();
        private ModelPart n(String name){return nodes.get(name);}
        private static float rad(float value){return value*Mth.DEG_TO_RAD;}
        public static float[] sample(String name,float seconds){int id=KuzyaAssets.index(name);var keys=KuzyaAssets.KEYS[id];float duration=KuzyaAssets.TICKS[id]/20F;float t=KuzyaAssets.LOOPS[id]?seconds%duration:Math.clamp(seconds,0,duration);float[] out=new float[15];int right=1;while(right<keys.length-1&&keys[right][0]<t)right++;var a=keys[right-1];var b=keys[right];float u=Math.clamp((t-a[0])/(b[0]-a[0]),0,1);u=u*u*(3-2*u);for(int i=1;i<16;i++)out[i-1]=Mth.lerp(u,a[i],b[i]);return out;}
        @Override public void setupAnim(KuzyaEntity e,float swing,float amount,float age,float yaw,float pitch){float partial=age-e.tickCount;var p=playback.computeIfAbsent(e,k->new Playback());String clip=e.clip();float t=e.clipAge(partial)/20F;double x=Mth.lerp(partial,e.xo,e.getX()),z=Mth.lerp(partial,e.zo,e.getZ());if(p.ready)p.gait+=Math.min(.5,Math.hypot(x-p.x,z-p.z))/.22F;p.x=x;p.z=z;p.ready=true;
            if(!clip.equals(p.clip)){p.from=p.last.clone();p.clip=clip;}float[] frame=sample(clip,clip.equals("walk")?p.gait*.9F:t);float fade=Math.clamp(t/.25F,0,1);fade=fade*fade*(3-2*fade);for(int i=0;i<frame.length;i++)frame[i]=Mth.lerp(fade,p.from[i],frame[i]);p.last=frame.clone();pose(clip,frame,e.voice(),e.voiceAge(partial),age,yaw,pitch,clip.equals("walk")?p.gait*.9F:t);
        }
        /** Applies authored curves to real joints; used by native offline fixture as well. */
        public void pose(String clip,float[] f,int voice,float voiceAge,float age,float yaw,float pitch,float t){root.getAllParts().forEach(ModelPart::resetPose);root.xScale=root.yScale=root.zScale=.25F;
            n("head").y+=4;n("head").xScale=n("head").zScale=1.05F;n("head").yScale=1.25F;
            for(String side:List.of("L","R"))n("eye_"+side).xScale=n("eye_"+side).yScale=n("eye_"+side).zScale=.5F;
            n("head").xRot=rad(f[0]+Math.clamp(pitch,-20,20)*.3F);n("head").yRot=rad(f[1]+Math.clamp(yaw,-35,35)*.3F);n("torso").xRot=rad(f[2]);n("torso").yRot=rad(f[3]);n("shoulder_R").xRot=-rad(f[4]);n("shoulder_L").xRot=-rad(f[5]);n("elbow_R").xRot=-rad(f[6]);n("elbow_L").xRot=-rad(f[7]);n("hip_R").xRot=-rad(f[8]);n("hip_L").xRot=-rad(f[9]);n("knee_R").xRot=rad(f[10]);n("knee_L").xRot=rad(f[11]);n("pelvis").y-=f[12]*4;
            for(String side:List.of("L","R")){n("foot_"+side).xRot=-(n("hip_"+side).xRot+n("knee_"+side).xRot)*.7F;n("pupil_"+side).x+=Math.clamp(yaw,-35,35)*.012F;n("brow_"+side).zRot=rad((clip.startsWith("grumble")?-10:2)*(side.equals("L")?1:-1));}
            float blink=Math.max(f[13],age%87<3?1-Math.abs(age%87-1.5F)/1.5F:0);n("eyelid_L").yScale=n("eyelid_R").yScale=Math.max(.001F,blink);float mouth=f[14];if(voice>=0&&voice<KuzyaAssets.ENVELOPES.length&&voiceAge>=0&&voiceAge<KuzyaAssets.VOICE_TICKS[voice]){var env=KuzyaAssets.ENVELOPES[voice];int a=Math.clamp((int)(voiceAge*2.5F),0,env.length-1);mouth=Math.max(mouth,env[a]);}n("jaw").y+=mouth*2.2F;n("jaw").xRot=mouth*.2F;
            boolean seated=clip.equals("idle_sit")||clip.startsWith("eat")||clip.equals("pick_food")||clip.equals("place_bowl");if(seated){n("pelvis").y+=1.15F*4;for(String side:List.of("L","R")){n("hip_"+side).xRot=-rad(73);n("knee_"+side).xRot=rad(68);n("foot_"+side).xRot=rad(5);}}
            boolean lie=clip.startsWith("sleep")||clip.equals("lie_idle");float lying=lie?1:clip.equals("lie_down")?smooth(t/1.15F):clip.equals("wake_up")?1-smooth(t/1.3F):0;n("pelvis").zRot=-Mth.HALF_PI*lying;n("pelvis").y=Mth.lerp(lying,n("pelvis").y,-4F*4);n("torso").xRot*=1-lying;n("head").zRot=rad(10)*lying;
            n("food_prop").visible=clip.equals("pick_food")?t>=.43:clip.startsWith("eat")||clip.equals("place_bowl")&&t<.4;n("bowl_prop").visible=clip.equals("carry_bowl")||clip.equals("place_bowl");n("broom_prop").visible=clip.equals("domestic_sweep");
            if(clip.equals("domestic_sweep")){float sweep=key(t,new float[]{0,.55F,1.2F,1.9F,2.1F},new float[]{0,-.35F,.35F,-.35F,0});n("torso").yRot+=sweep*.4F;n("hand_R").zRot=sweep;n("shoulder_L").xRot=-.45F;}
            if(clip.equals("shoo_guest")||clip.equals("grumble_large"))n("hand_R").yRot=key(t,new float[]{0,.5F,.7F,.9F,1.1F,1.5F,1.8F},new float[]{0,0,.22F,-.22F,.22F,0,0});
            if(clip.equals("walk"))plantFeet(t/.9F);
            for(int i=0;i<36;i++)n("hair_"+i).xRot+=(float)Math.sin(age*.055F-i*.35F)*rad(lie?1:3);n("torso").y-=(float)Math.sin(age*Math.PI/40)*.03F*4;
        }
        /** Two-bone planar IK: a stance sole is fixed in world space as distance phase advances. */
        private void plantFeet(float phase){n("pelvis").y+=.4F*4;for(String side:List.of("R","L")){float u=(phase+(side.equals("R")?0:.5F))%1;if(u<0)u++;boolean stance=u<.65F;float z=stance?-1.144F+3.52F*u:1.144F-2.288F*smooth((u-.65F)/.35F);float lift=stance?0:(float)Math.sin((u-.65F)/.35F*Math.PI)*.3F;float height=-(n("pelvis").y+n("hip_"+side).y)/4;float dy=height-.55F-lift;float bend=(float)Math.acos(Math.clamp((dy*dy+z*z-1.15F*1.15F-1.35F*1.35F)/(2*1.15F*1.35F),-1,1));float hip=(float)Math.atan2(z,dy)-(float)Math.atan2(1.35F*Math.sin(bend),1.15F+1.35F*Math.cos(bend));n("hip_"+side).xRot=hip;n("knee_"+side).xRot=bend;n("foot_"+side).z=0;n("foot_"+side).xRot=-hip-bend;}}
        static float smooth(float t){float u=Math.clamp(t,0,1);return u*u*(3-2*u);}
        static float key(float t,float[] times,float[] values){for(int i=1;i<times.length;i++)if(t<=times[i])return Mth.lerp(smooth((t-times[i-1])/(times[i]-times[i-1])),values[i-1],values[i]);return values[values.length-1];}
        @Override public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,int color){root.render(p,v,light,overlay,color);}
    }
    private KuzyaClient(){}
}
