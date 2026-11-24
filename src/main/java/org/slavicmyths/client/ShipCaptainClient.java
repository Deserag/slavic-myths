package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.winterboss.*;

/** Procedural V2 cuboids. Authoring coordinates: x lateral, y depth (front negative), z height. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class ShipCaptainClient {
    private static final ResourceLocation TEX=ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    private static final int NAVY=0x25364e,DARK=0x182638,BLUE=0x3d526c,GOLD=0xb28a50,GOLD2=0xdfbd78,RED=0x743b49,RED2=0xa3545b,
        SKIN=0x8c9eaa,SKIN2=0xb1bcc4,HAIR=0xaab9c8,HAIR2=0xd0dce4,HAIR3=0x7a8da1,FUR=0xb5b4ac,FUR2=0xd5d3c8,
        LEATHER=0x48372e,BROWN=0x75543c,BOOT=0x242b36,METAL=0x8293a4,ICE=0x4aaed6,ICE2=0x91d8ed,ICE3=0xc0f0fa,GLOW=0x43e5ff,BLACK=0x101b27;
    private static final double AXE_SCALE=1.4;
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){
        e.registerEntityRenderer(ShipCaptainContent.BOSS.get(),CaptainRenderer::new);
        e.registerEntityRenderer(ShipCaptainContent.AXE.get(),AxeRenderer::new);
    }
    private interface Boxes {void box(double x,double y,double z,double w,double d,double h,int color);}
    private record Frame(Vec3 x,Vec3 y,Vec3 z,Vec3 origin){
        Vec3 map(Vec3 v){return origin.add(x.scale(v.x)).add(y.scale(v.y)).add(z.scale(v.z));}
        static Frame of(Vec3 axis,Vec3 hint,Vec3 origin){
            Vec3 z=axis.normalize(),y=hint.subtract(z.scale(hint.dot(z)));
            if(y.lengthSqr()<.0001)y=new Vec3(1,0,0).subtract(z.scale(z.x));
            y=y.normalize();Vec3 x=y.cross(z).normalize();return new Frame(x,z.cross(x),z,origin);
        }
        Boxes transform(Emitter emit){return (x,y,z,w,d,h,c)->emit.box(this,x,y,z,w,d,h,c);}
    }
    private static final Frame ID=new Frame(new Vec3(1,0,0),new Vec3(0,1,0),new Vec3(0,0,1),Vec3.ZERO);
    private static final Vec3 FRONT=new Vec3(0,-1,0);
    private static Vec3 v(double x,double y,double z){return new Vec3(x,y,z);}
    private static Vec3 mc(Vec3 a){return v(a.x,a.z,-a.y);}
    private static final int[][] FACES={{0,3,2,1},{4,5,6,7},{0,1,5,4},{3,7,6,2},{0,4,7,3},{1,2,6,5}};
    private record Emitter(PoseStack p,VertexConsumer out,int light,int overlay){
        void box(Frame f,double x,double y,double z,double w,double d,double h,int c){
            Vec3[] a={v(x,y,z),v(x+w,y,z),v(x+w,y+d,z),v(x,y+d,z),v(x,y,z+h),v(x+w,y,z+h),v(x+w,y+d,z+h),v(x,y+d,z+h)};
            for(int i=0;i<a.length;i++)a[i]=mc(f.map(a[i]));
            for(int[] face:FACES){
                Vec3 n=a[face[1]].subtract(a[face[0]]).cross(a[face[2]].subtract(a[face[0]])).normalize();
                for(int idx:face){Vec3 q=a[idx];out.addVertex(p.last(),(float)q.x,(float)q.y,(float)q.z).setColor(0xff000000|c).setUv(.5F,.5F).setOverlay(overlay).setLight(c==GLOW?15728880:light).setNormal(p.last(),(float)n.x,(float)n.y,(float)n.z);}
            }
        }
    }
    private static void legs(Boxes b,int s){
        double x=s*3.;
        b.box(x-2.15,-2.65,0,4.3,6.1,3.4,BOOT);b.box(x-2.2,-3.1,0,4.4,6.6,.85,DARK);
        b.box(x-1.95,-2.1,3.4,3.9,4.4,7.8,BOOT);b.box(x-2.15,-2.25,5.9,4.3,4.7,.8,BROWN);b.box(x-2.15,-2.25,9,4.3,4.7,1,GOLD);
        b.box(x-1.9,-2,11.2,3.8,4.3,9,DARK);b.box(x-1.9,-2.3,12.3,3.8,.7,2.7,METAL);
        b.box(x-1.35,-3.2,1.8,2.7,.5,1.1,GOLD);b.box(x-1.5,-2.5,7,3,.4,1.5,BLUE);
    }
    private static void torso(Boxes b){
        b.box(-5,-2.9,19.4,10,6,5.5,DARK);
        for(int i=0;i<7;i++){
            double z=10.5+i*1.7,w=7.8-i*.35,depth=3.9-i*.12;
            for(int s:new int[]{-1,1}){
                b.box(s==1?1.5:-w,-depth,z,w-1.5,.95,1.75,NAVY);
                b.box(s*w-(s==1?.9:0),-depth+.8,z,.9,depth*2-1.2,1.75,BLUE);
                b.box(s*1.5-(s<0?.55:0),-depth-.10,z,.55,.35,1.75,GOLD);
            }
            b.box(-w,-.1+depth,z,w*2,1,1.75,NAVY);
        }
        for(int s:new int[]{-1,1})for(int j=0;j<5;j++){
            double x=s*(2.1+j*1.15),z=9.1+j%3*.7;
            b.box(x-.55,-3.95,z,1.1,1.1,2.1,j%2==0?NAVY:BLUE);b.box(x-.48,-4.03,z-.65,.96,.6,1,j%2==0?ICE:ICE2);
        }
        for(int j=0;j<12;j++){double x=-7.2+j*1.3,z=8.6+j%4*.7;b.box(x,3.8,z,1.2,.9,2.6,j%3==0?BLUE:NAVY);b.box(x,4,z-.65,1.05,.6,1.1,j%3==0?ICE2:ICE);}
        b.box(-5.5,-3.1,22.4,11,6.2,5.2,NAVY);b.box(-6.3,-3.5,27.6,12.6,7,8.7,NAVY);b.box(-6.9,-3,33.5,13.8,6,2.8,BLUE);
        b.box(-2,-3.65,25.4,4,.8,9.8,DARK);
        for(int s:new int[]{-1,1}){
            b.box(s*2.2-(s<0?.65:0),-3.87,25,.65,.65,10.8,GOLD);
            for(int k=0;k<3;k++)b.box(s*3.9-(s<0?.8:0),-3.99,27+k*2.5,.8,.55,.72,GOLD2);
            b.box(s*4.7-(s<0?1.8:0),-3.6,32.4,1.8,.55,2.3,BLUE);
        }
        for(int j=0;j<4;j++)b.box(-1.45,-4,26+j*2.1,2.9,.6,.6,j%2==0?METAL:HAIR3);
        for(int j=0;j<10;j++){b.box(-5.3+j*1.02,-4.35,24+j*1.04,1.25,.65,1.55,LEATHER);b.box(-5.25+j*1.02,-4.72,24.25+j*1.04,.24,.16,.3,BROWN);}
        b.box(-5.8,-3.7,21.7,11.6,7.4,2.05,LEATHER);b.box(-2,-4.42,21.75,4,.72,2,GOLD);b.box(-1.18,-4.82,22.15,2.36,.4,1.15,DARK);
        for(int j=0;j<5;j++)b.box(1.7+j*.18,-4.25,13.2+j*1.7,1.9,.65,1.8,j%2==0?RED2:RED);
        for(int i=0;i<12;i++){double a=i*Math.PI/6;b.box(2.65*Math.cos(a)-.45,3.55,29.9+2.65*Math.sin(a)-.45,.9,.5,.9,GOLD);}
        for(int i=0;i<8;i++){double a=i*Math.PI/4;for(double r:new double[]{.7,1.5,3.2})b.box(r*Math.cos(a)-.25,3.6,29.9+r*Math.sin(a)-.25,.5,.5,.5,GOLD);}
        b.box(-.65,3.6,29.25,1.3,.55,1.3,GOLD2);
        for(int s:new int[]{-1,1})for(int i=0;i<6;i++){
            double x=s*(1.8+i*.96),z=35.5-i*.32;
            for(double y:new double[]{-3.3,-1.9,-.5,1,2.5})b.box(x-.55,y-.55,z,1.15,1.35,1.4+.3*Math.floorMod(i+(int)(y*2),3),Math.floorMod(i+(int)y,3)==0?FUR2:FUR);
            b.box(x-.48,-4,z-1-i%2*.65,1,.9,2,i%3==0?HAIR:FUR);b.box(x-.48,3.4,z-1-i%3*.55,1,.9,2,i%2==0?FUR:FUR2);
        }
        head(b);
        for(double[] a:new double[][]{{-6.9,-3.5,31.6},{6.2,2.8,29.7},{-6.6,3.6,16.6},{6.7,-3.3,14.7}})b.box(a[0],a[1],a[2],.8,.75,2.3,ICE2);
        for(int j=0;j<2;j++){double x=-5.2-j;b.box(x,-4.3,17.2-j*.5,.65,.6,4.4,BROWN);b.box(x-1.2,-4.55,20.1-j*.5,1.85,.85,1.75,METAL);b.box(x-1.5,-4.65,20.1-j*.5,.38,.9,1.75,ICE2);}
    }
    private static void head(Boxes b){
        b.box(-1.8,-1.7,36,3.6,3.4,2,SKIN);b.box(-4.15,-3.35,38,8.3,6.7,7.3,SKIN);b.box(-3.75,-3.75,38.9,7.5,1,5.2,SKIN2);
        for(int s:new int[]{-1,1}){
            double x=s*2.;
            b.box(x-1.3,-4.12,41.9,2.6,.5,1.55,BLACK);b.box(x-.85,-4.37,42.18,1.7,.35,.85,GLOW);b.box(x-.45,-4.57,42.33,.9,.18,.45,ICE3);
            b.box(x-1.6,-4.18,43.42,3.2,.65,.65,HAIR2);b.box(s*4.05-(s<0?.45:0),-1.1,40.2,.65,1.5,2,SKIN);
            for(int j=0;j<4;j++){
                x=s*(3.2+j*.45);b.box(x-.55,-3.3+j*.65,37.2+j%2*.8,1.1,1.3,7.2-j%2*.8,j%2==0?HAIR2:HAIR);
                x=s*(.7+j*.78);b.box(x-.44,-5.15+j*.14,39.2-j*.32,.9,1,1.25,HAIR2);
            }
        }
        b.box(-.75,-4.9,40.45,1.5,1.25,2.2,SKIN);b.box(-1.5,-4.2,39.3,3,.45,.65,BLACK);
        for(int j=0;j<8;j++)b.box(-3.85+j*.98,2.65,36.3+j%3*.5,1.04,1.45,8.5-j%3*.5,j%2==0?HAIR3:HAIR);
        for(int j=0;j<7;j++){double x=-3+j*.89,h=5.6-Math.abs(j-3)*.9,z=39.6-h;b.box(x,-4.5-.15*(j%2),z,.95,1.6,h,j%2==0?HAIR2:HAIR);b.box(x+.2,-5.1,z+.6,.45,.55,h-.8,j%2==0?HAIR3:HAIR2);}
        b.box(-6,-4.4,44.4,12,9,1.65,DARK);b.box(-6.5,-3.8,45.1,13,7.5,1.1,NAVY);b.box(-4.5,-2.9,45.7,9,5.8,3,NAVY);b.box(-3.2,-2.5,48.7,6.4,5,1,BLUE);
        for(int s:new int[]{-1,1}){
            for(int j=0;j<5;j++){double x=s*(1.15+j*1.06),h=4.4-j*.52;b.box(x-.57,-4.45,45.5,1.13,1.2,h,NAVY);b.box(x-.58,-4.6,45.5+h-.58,1.15,1.25,.6,GOLD);}
            for(int j=0;j<4;j++){double x=s*(2.1+j*1.05);b.box(x-.53,3.3,45.6,1.07,1.05,2.6-j*.3,NAVY);b.box(x-.53,3.4,47.65-j*.3,1.07,1,.55,GOLD);}
        }
        b.box(-.9,-4.95,47.2,1.8,.5,1.8,GOLD);b.box(-.4,-5.1,47.65,.8,.25,.8,DARK);
        for(double[] a:new double[][]{{-5.8,-3.8,2.7},{-4.9,-4.5,1.5},{5.3,-3.6,2.1},{4.6,3.5,1.9},{-5.4,3.4,1.2}}){double x=a[0],y=a[1],h=a[2];b.box(x,y,45.5-h,.65,.65,h,ICE2);b.box(x+.12,y-.05,45.5-h-.55,.35,.45,.75,ICE3);}
    }
    private static Vec3 limb(Emitter emit,int s,Vec3 upper,Vec3 fore){
        Vec3 shoulder=v(s*7.25,0,34.4),u=upper.normalize(),f=fore.normalize(),elbow=shoulder.add(u.scale(6.5)),hand=elbow.add(f.scale(6.1));
        Boxes b=Frame.of(u.scale(-1),FRONT,shoulder).transform(emit);
        b.box(-2.35,-2.15,-6.5,4.7,4.3,6.8,s==1?ICE:NAVY);
        b.box(-2.65,-2.45,-1.7,5.3,4.9,2.25,FUR);b.box(-2.3,-2.65,-2.65,4.6,1.15,1.4,FUR2);
        if(s==1){b.box(-2.55,-2.5,-5.8,1.3,1.1,3.5,ICE2);b.box(.45,-2.55,-6.1,1.45,1.25,4.1,ICE3);b.box(-2,1.45,-5.3,1.7,1.1,3.7,ICE2);}else b.box(-2.4,-2.2,-5.7,4.8,4.4,.85,BLUE);
        b=Frame.of(f.scale(-1),FRONT,elbow).transform(emit);
        b.box(-2,-1.95,-5.5,4,3.9,5.8,s==1?ICE:NAVY);
        b.box(-2.2,-2.15,-4.5,4.4,4.3,1.4,s==1?METAL:LEATHER);b.box(-2.25,-2.2,-4.5,4.5,4.4,.48,GOLD);
        b.box(-1.75,-1.65,-7,3.5,3.3,2.6,s==1?ICE2:SKIN);b.box(-1.5,-1.95,-6.7,3,.5,1.3,s==1?ICE3:SKIN2);
        if(s==1){b.box(-2.5,-2.4,-3.3,1.55,1.6,4.8,ICE2);b.box(.9,-2.3,-4.2,1.55,1.5,3.4,ICE3);b.box(-2.05,1.2,-2.9,1.5,1.2,4.2,ICE2);b.box(.45,1.5,-6.9,1.7,1.2,3.2,ICE);for(int j=0;j<3;j++)b.box(-1.5+j*1.03,-1.9,-7.55,.82,1.45,1.6-j%2*.3,j%2==0?ICE2:ICE3);}
        return hand;
    }
    private static void axe(Emitter emit,Vec3 at,Vec3 axis,Vec3 hint){
        Boxes original=Frame.of(axis,hint,at).transform(emit);
        Boxes b=(x,y,z,w,d,h,c)->original.box(x*AXE_SCALE,y*AXE_SCALE,(z+2.7)*AXE_SCALE,w*AXE_SCALE,d*AXE_SCALE,h*AXE_SCALE,c);
        b.box(-.55,-.55,-5.1,1.1,1.1,12.7,BROWN);
        for(double z:new double[]{-4.8,-2.8,-.8,1.2,4.9})b.box(-.64,-.64,z,1.28,1.28,.65,z<4?LEATHER:METAL);
        b.box(-1,-.9,4.8,3.3,1.8,3.5,METAL);b.box(-.8,-1,5.5,1.6,2,1.45,GOLD);
        double[][] blades={{1.8,2.2},{2.5,2.2},{2.8,2.8},{2.5,2.4},{1.8,2.15}};
        for(int j=0;j<5;j++){double x=blades[j][0],w=blades[j][1],z=3.8+j*1.15;b.box(x-.4,-.65,z,w,1.3,1.17,METAL);b.box(x+w-.6,-.78,z,.65,1.56,1.18,ICE2);b.box(x+w-.05,-.55,z,.35,1.1,1.12,ICE3);}
        b.box(-.85,-.85,-5.3,1.7,1.7,.7,METAL);
    }
    private static void held(Emitter e,Vec3 hand,Vec3 fore,Vec3 pref,int s){
        Vec3 f=fore.normalize(),a=pref.normalize();a=a.subtract(f.scale(a.dot(f)));
        if(a.lengthSqr()<.0625){a=v(s*.5,-1,0);a=a.subtract(f.scale(a.dot(f)));}
        a=a.normalize();Vec3 edge=v(0,0,-1);edge=edge.subtract(a.scale(edge.dot(a)));
        if(edge.lengthSqr()<.04){edge=v(s,0,0);edge=edge.subtract(a.scale(edge.dot(a)));}
        axe(e,hand,a,a.cross(edge.normalize()));
    }
    private static final double[][][] POSES={
        {{-.34,0,-1},{-.16,-.16,-1},{.34,0,-1},{.16,-.16,-1},{-.48,-1,0},{.48,-1,0}},
        {{-.40,-.35,-.65},{.4,-.9,-.1},{.25,0,-1},{.10,-.25,-1},{.72,-.6,.2},{.48,-1,0}},
        {{-.42,-.68,-.3},{.12,-.8,-.6},{.42,-.68,-.3},{-.12,-.8,-.6},{-.35,-.55,-.8},{.35,-.55,-.8}},
        {{-.35,-.45,.8},{.20,-1,.05},{.2,0,-1},{.1,-.15,-1},{-.48,-1,0},{.48,-1,0}},
        {{-1,0,.04},{-1,.10,.03},{1,0,.04},{1,-.10,.03},{-.65,-.75,.20},{.65,.75,.20}},
        {{-.2,0,-1},{-.1,-.2,-1},{.35,-.45,-.85},{-.2,-.6,-.75},{-.48,-1,0},{.48,-1,0}},
        {{-.8,-.4,.2},{-.3,-.85,.3},{.8,-.4,.2},{.3,-.85,.3},{-.48,-1,0},{.48,-1,0}},
        {{-.2,0,-1},{-.1,-.1,-1},{.25,-.95,.08},{-.12,-1,0},{-.48,-1,0},{.48,-1,0}},
        {{-.3,.6,-.6},{-.1,.8,-.3},{.45,-.7,-.5},{.2,-.9,.1},{-.2,-.6,.8},{.6,-.2,.7}}
    };
    private static final int[] HIT={0,13,18,14,10,25,20,18,14}, END={1,26,38,32,60,48,48,36,38};
    private static Vec3 poseVector(int action,int i,double weight){
        double[] a=POSES[0][i],b=POSES[action][i];return v(Mth.lerp(weight,a[0],b[0]),Mth.lerp(weight,a[1],b[1]),Mth.lerp(weight,a[2],b[2]));
    }
    private static double weight(int a,double t){
        if(a==0)return 0;
        if(a==4)return Math.clamp(Math.min(t/10,(60-t)/10),0,1);
        if(a==8)return Math.clamp(Math.min(t/14,(38-t)/14),0,1);
        return Math.clamp(t<=HIT[a]?t/HIT[a]:(END[a]-t)/(END[a]-HIT[a]),0,1);
    }
    public static final class CaptainRenderer extends EntityRenderer<ShipCaptain>{
        public CaptainRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.65F;}
        @Override public ResourceLocation getTextureLocation(ShipCaptain e){return TEX;}
        @Override public void render(ShipCaptain e,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){
            if(!e.isInvisible()){
                int a=Math.clamp(e.action(),0,8);double t=e.actionFrame()+partial,w=weight(a,t);
                float body=a==0?Mth.rotLerp(partial,e.yBodyRotO,e.yBodyRot):e.attackYaw();
                p.pushPose();p.mulPose(Axis.YP.rotationDegrees(-body));
                if(a==4)p.mulPose(Axis.YP.rotationDegrees((float)(Math.clamp((t-10)/40,0,1)*720)));
                p.scale(1F/16,1F/16,1F/16);
                Emitter em=new Emitter(p,buffers.getBuffer(RenderType.entityCutoutNoCull(TEX)),light,OverlayTexture.NO_OVERLAY);
                torso(ID.transform(em));
                for(int s:new int[]{-1,1}){
                    p.pushPose();p.translate(s*3,20,0);
                    float stride=(float)(Math.sin((e.tickCount+partial)*.45)*Math.min(.35,e.getDeltaMovement().horizontalDistance()*2)*s);
                    p.mulPose(Axis.XP.rotation(stride));p.translate(-s*3,-20,0);legs(ID.transform(em),s);p.popPose();
                }
                for(int s:new int[]{-1,1}){
                    int i=s<0?0:2;
                    Vec3 u=poseVector(a,i,w),f=poseVector(a,i+1,w),hand=limb(em,s,u,f);
                    boolean visible=(e.axes()&(s<0?1:2))!=0;
                    if(visible)held(em,hand,f,poseVector(a,s<0?4:5,w),s);
                }
                p.popPose();
            }
            super.render(e,yaw,partial,p,buffers,light);
        }
    }
    public static final class AxeRenderer extends EntityRenderer<ShipCaptainAxe>{
        public AxeRenderer(EntityRendererProvider.Context c){super(c);}
        @Override public ResourceLocation getTextureLocation(ShipCaptainAxe e){return TEX;}
        @Override public void render(ShipCaptainAxe e,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){
            p.pushPose();Vec3 vel=e.getDeltaMovement();
            if(vel.horizontalDistanceSqr()>.001)p.mulPose(Axis.YP.rotationDegrees((float)Math.toDegrees(Math.atan2(vel.x,vel.z))));
            p.mulPose(Axis.XP.rotationDegrees((e.tickCount+partial)*50));p.scale(1F/16,1F/16,1F/16);
            axe(new Emitter(p,buffers.getBuffer(RenderType.entityCutoutNoCull(TEX)),light,OverlayTexture.NO_OVERLAY),v(0,0,-3),v(0,0,1),FRONT);
            p.popPose();super.render(e,yaw,partial,p,buffers,light);
        }
    }
    private ShipCaptainClient(){}
}
