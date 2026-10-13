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
import org.slavicmyths.bayun.*;

@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class BayunClient {
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(BayunContent.ENTITY.get(),Renderer::new);}
    public static final class Renderer extends MobRenderer<BayunEntity,Model> {
        public Renderer(EntityRendererProvider.Context c){super(c,new Model(),.62F);}
        @Override public ResourceLocation getTextureLocation(BayunEntity e){return BayunContent.id("textures/entity/kot_bayun/skin.png");}
        @Override protected float getFlipDegrees(BayunEntity e){return 0;}
    }
    public static final class Model extends EntityModel<BayunEntity> {
        public final ModelPart root=BayunMesh.create();public final Map<String,ModelPart> nodes=BayunMesh.index(root);
        private record Frame(String clip,float[][] previous,float[][] from){}
        private static final class Playback {String clip="";Map<String,float[]> last=Map.of(),from=Map.of();double x,z;float gait;boolean ready;}
        private final Map<BayunEntity,Playback> playbacks=new WeakHashMap<>();
        private ModelPart node(String name){return nodes.get(name);}
        public static Map<String,float[]> sample(String clip,float time){Map<String,float[]> out=new HashMap<>();if(clip.equals("sleep_curl_loop")||clip.equals("wake_up")){var rest=sample("sleep_curl_in",BayunAnimations.duration("sleep_curl_in"));float weight=clip.equals("sleep_curl_loop")?1:1-smooth(time/BayunAnimations.duration("wake_up"));rest.forEach((bone,values)->{float[] v=values.clone();for(int j=0;j<v.length;j++)v[j]*=weight;out.put(bone,v);});}int duration=BayunAnimations.duration(clip);float t=BayunAnimations.loop(clip)?Math.floorMod((long)(time*1000),duration*1000L)/1000F:Math.clamp(time,0,duration);
            for(var curve:BayunAnimations.curves(clip)){var keys=curve.keys();int right=1;while(right<keys.length-1&&keys[right][0]<t)right++;var a=keys[Math.max(0,right-1)];var b=keys[Math.min(right,keys.length-1)];float u=b[0]==a[0]?1:Math.clamp((t-a[0])/(b[0]-a[0]),0,1);u=u*u*(3-2*u);float[] f=new float[6];for(int j=0;j<6;j++)f[j]=Mth.lerp(u,a[j+1],b[j+1]);out.put(curve.bone(),f);}return out;
        }
        @Override public void setupAnim(BayunEntity e,float swing,float amount,float age,float yaw,float pitch){float partial=age-e.tickCount;var p=playbacks.computeIfAbsent(e,k->new Playback());double x=Mth.lerp(partial,e.xo,e.getX()),z=Mth.lerp(partial,e.zo,e.getZ());if(p.ready)p.gait+=(float)Math.min(.5,Math.hypot(x-p.x,z-p.z))*24/.8F;p.x=x;p.z=z;p.ready=true;String clip=e.clip();float tick=clip.equals("walk")?p.gait:e.age(partial);var frame=sample(clip,tick);if(!clip.equals(p.clip)){p.from=p.last;p.clip=clip;}float fade=Math.clamp(e.age(partial)/(clip.equals("pounce_kill")?2:6),0,1);Map<String,float[]> blended=new HashMap<>();Set<String> bones=new HashSet<>(frame.keySet());bones.addAll(p.from.keySet());for(String b:bones){float[] a=p.from.getOrDefault(b,new float[6]),v=frame.getOrDefault(b,new float[6]),f=new float[6];for(int j=0;j<6;j++)f[j]=Mth.lerp(fade,a[j],v[j]);blended.put(b,f);}p.last=blended;pose(clip,blended,tick,yaw,pitch);}
        public void pose(String clip,Map<String,float[]> frame,float age,float yaw,float pitch){root.getAllParts().forEach(ModelPart::resetPose);root.xScale=root.yScale=root.zScale=.88F;node("head").xScale=node("head").yScale=node("head").zScale=.25F;
            for(var item:frame.entrySet()){var n=node(item.getKey());if(n==null)throw new IllegalStateException("Unmapped Bayun bone "+item.getKey());float[] f=item.getValue();boolean dense=item.getKey().startsWith("ear_")||item.getKey().startsWith("eye_")||item.getKey().equals("jaw");float unit=dense?64:16;n.xRot=f[0]*Mth.DEG_TO_RAD;n.yRot=f[1]*Mth.DEG_TO_RAD;n.zRot=f[2]*Mth.DEG_TO_RAD;n.x+=f[3]*unit;n.y-=f[4]*unit;n.z+=f[5]*unit;}
            node("head").yRot+=Math.clamp(yaw,-35,35)*Mth.DEG_TO_RAD*.3F;node("head").xRot+=Math.clamp(pitch,-18,18)*Mth.DEG_TO_RAD*.3F;
            float breath=(float)Math.sin(age*Math.PI/40)*.008F;node("chest").y-=breath*16;
            boolean sleep=clip.equals("sleep_curl_loop");float curl=sleep?1:clip.equals("sleep_curl_in")?smooth(age/34):clip.equals("wake_up")?1-smooth(age/30):0;
            if(curl>0){node("spine").y+=curl*2;node("head").yRot+=curl*.35F;for(String side:List.of("L","R")){node("haunch_"+side).xRot-=curl*.5F;node("hindleg_"+side).xRot+=curl*1.1F;node("paw_F"+side).xRot-=curl*.55F;}for(int i=1;i<=5;i++)node("tail_0"+i).yRot-=curl*.48F;}
            if(clip.equals("idle_sit")||clip.equals("loaf_rest")){for(String side:List.of("L","R")){node("haunch_"+side).xRot-=.7F;node("hindleg_"+side).xRot+=1.1F;node("paw_B"+side).xRot-=.4F;}}
            float blink=sleep?1:age%120>=34&&age%120<39?1-Math.abs(age%120-36.5F)/2.5F:0;for(String side:List.of("L","R")){node("eyelid_"+side).yScale=Math.max(.001F,blink);node("ear_"+side).zRot=Math.clamp(node("ear_"+side).zRot,-.384F,.384F);node("jaw").xRot=Math.clamp(node("jaw").xRot,0,.436F);}
            boolean claws=clip.equals("pounce_kill")&&age>=8&&age<25||clip.equals("recover")&&age<18;node("claws_L").visible=node("claws_R").visible=claws;
            for(int i=1;i<=5;i++)node("tail_0"+i).zRot+=(float)Math.sin(age*.025F-i*.5F)*.018F;
            if(clip.equals("walk"))feet(age);
            if(!clip.equals("pounce_kill")||age<8||age>=25)groundPaws();
        }
        /** Evaluate four eight-corner paw boxes only, not the 254-cube fur mesh. */
        private void groundPaws(){float lowest=Float.NEGATIVE_INFINITY;for(String side:List.of("L","R"))for(boolean front:new boolean[]{true,false}){var stack=new PoseStack();root.translateAndRotate(stack);node("spine").translateAndRotate(stack);if(front)node("chest").translateAndRotate(stack);node((front?"shoulder_":"haunch_")+side).translateAndRotate(stack);node((front?"foreleg_":"hindleg_")+side).translateAndRotate(stack);node((front?"paw_F":"paw_B")+side).translateAndRotate(stack);float half=front?.13F:.14F,back=front?.13F:.14F,forward=front?.16F:.18F;for(float x:new float[]{-half,half})for(float y:new float[]{-.065F,.065F})for(float z:new float[]{-forward,back}){var point=stack.last().pose().transformPosition(new org.joml.Vector3f(x,-y,z));lowest=Math.max(lowest,point.y*16);}}if(Float.isFinite(lowest))root.y+=24-lowest;}
        private void feet(float time){for(String side:List.of("L","R"))for(boolean front:new boolean[]{true,false}){float phase=(time/24+(side.equals("L")?0:.5F)+(front?0:.5F))%1;if(phase<0)phase++;float stride=phase<.65F?.24F-.48F*phase/.65F:-.24F+.48F*smooth((phase-.65F)/.35F);float lift=phase<.65F?0:(float)Math.sin((phase-.65F)/.35F*Math.PI)*.07F;String upper=front?"shoulder_":"haunch_",lower=front?"foreleg_":"hindleg_",paw=front?"paw_F":"paw_B";float a=front?.39F:.34F,b=front?.36F:.30F,dy=a+b-lift;float bend=(float)Math.acos(Math.clamp((dy*dy+stride*stride-a*a-b*b)/(2*a*b),-1,1));float joint=(float)Math.atan2(stride,dy)-(float)Math.atan2(b*Math.sin(bend),a+b*Math.cos(bend));node(upper+side).xRot=joint;node(lower+side).xRot=bend;node(paw+side).xRot=-joint-bend;}}
        private static float smooth(float x){float u=Math.clamp(x,0,1);return u*u*(3-2*u);}
        @Override public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,int color){
            node("eye_L").skipDraw=node("eye_R").skipDraw=true;
            root.render(p,v,light,overlay,color);
            try{root.getAllParts().forEach(n->n.skipDraw=true);node("eye_L").skipDraw=node("eye_R").skipDraw=false;
                root.render(p,v,net.minecraft.client.renderer.LightTexture.FULL_BRIGHT,overlay,color);
            }finally{root.getAllParts().forEach(n->n.skipDraw=false);}
        }
    }
    private BayunClient(){}
}
