package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.earth.*;
import java.util.*;

/** Native model units: 16 units/block, +Y down, front -Z, root at feet (y=24).
 * Independent local pivots and eased keyframes; all collision and hit cues remain server-side. */
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class EarthClient {
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(EarthContent.BOSS.get(),BossRenderer::new);e.registerEntityRenderer(EarthContent.SERVANT.get(),ServantRenderer::new);e.registerEntityRenderer(EarthContent.ACTOR.get(),ActorRenderer::new);}
    private static CubeListBuilder cube(int u,int v,float x,float y,float z,float w,float h,float d){return CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d);}
    private static final Map<PartDefinition,Vec3Position> pivots=new IdentityHashMap<>();private static final List<float[]> fitted=new ArrayList<>();
    private record Vec3Position(float x,float y,float z){}
    private static void resetGeometry(){pivots.clear();fitted.clear();}
    private static PartDefinition empty(PartDefinition parent,String name){var child=parent.addOrReplaceChild(name,CubeListBuilder.create(),PartPose.ZERO);pivots.put(child,pivots.getOrDefault(parent,new Vec3Position(0,0,0)));return child;}
    private static PartDefinition part(PartDefinition parent,String name,int u,int v,float x,float y,float z,float w,float h,float d,float px,float py,float pz){var origin=pivots.getOrDefault(parent,new Vec3Position(0,0,0));var at=new Vec3Position(origin.x+px,origin.y+py,origin.z+pz);float shell=0;float[] bounds;
        while(true){bounds=new float[]{at.x+x-shell,at.y+y-shell,at.z+z-shell,at.x+x+w+shell,at.y+y+h+shell,at.z+z+d+shell};boolean conflict=false;
            for(var old:fitted){for(int axis=0;axis<3;axis++){boolean plane=false;for(int a:new int[]{axis,axis+3})for(int b:new int[]{axis,axis+3})if(Math.abs(bounds[a]-old[b])<.0001F)plane=true;boolean overlap=true;for(int k=0;k<3;k++)if(k!=axis&&Math.min(bounds[k+3],old[k+3])-Math.max(bounds[k],old[k])<=.001F)overlap=false;if(plane&&overlap){conflict=true;break;}}if(conflict)break;}
            if(!conflict)break;shell+=.0413F;if(shell>.8F)throw new IllegalStateException("Earth coplanar mesh "+name);
        }fitted.add(bounds);var child=parent.addOrReplaceChild(name,CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d,new CubeDeformation(shell)),PartPose.offset(px,py,pz));pivots.put(child,at);return child;}
    private static float ease(float t){t=Math.clamp(t,0,1);return t*t*(3-2*t);}
    private static float ramp(float age,float start,float end){return EarthRules.ramp(age,start,end);}
    private static float pose(float t,float[] times,float[] values){return EarthRules.pose(t,times,values);}
    public static ModelPart bossMesh(){resetGeometry();var m=new MeshDefinition();var root=m.getRoot();var pelvis=part(root,"pelvis",144,160,-17,-5,-10,34,10,20,0,-14,0);var lower=part(pelvis,"lower",144,112,-14,-13,-9,28,14,18,0,-3,0);var torso=part(lower,"torso",0,0,-21,-35,-12,42,35,24,0,-1,0);
        var chest=part(torso,"chest",144,0,-16,-8,-2,32,16,5,0,-24,-13);for(int i=0;i<4;i++)part(torso,"plate_"+i,144,32,-12,-4,-2,24-i*2,6.8F,5,(i%2==0?-3:4),-30+i*7,-13-i%2-i*.12F);
        var core=part(chest,"core",224,224,-7,-7,-1,14,14,2,0,2,-3); // relief spiral is geometry, readable with particles disabled
        int[][] spiral={{-7,-7,14,2},{5,-5,2,12},{-7,5,12,2},{-7,-5,2,10},{-5,-3,8,2},{1,-1,2,6},{-5,3,6,2},{-5,-1,2,4},{-3,-1,4,1},{0,0,1,2},{-3,1,3,1},{-3,0,1,1},{-2,0,2,1}};
        for(int i=0;i<spiral.length;i++){var s=spiral[i];part(core,"spiral_"+i,224,240,s[0],s[1],-1.2F,s[2],s[3],1,0,0,0);}
        var neck=part(torso,"neck",176,112,-8,-5,-7,16,7,14,0,-35,0);var head=part(neck,"head",0,64,-11,-18,-9,22,18,18,0,-1,-1);part(head,"nose",144,64,-2,-8,-3,4,12,5,0,-9,-9);part(head,"brow_l",144,88,-9,-2,-2,10,4,5,-1,-12,-9);part(head,"brow_r",144,88,-1,-3,-2,10,4,4,1,-12,-9);var jaw=part(head,"jaw",80,64,-10,0,-8,20,5,16,0,-2,-1);
        part(head,"cheek_l",184,64,-4,-6,-2,6,10,5,-9,-4,-8);part(head,"cheek_r",184,64,-2,-5,-2,6,9,5,9,-5,-8);
        part(head,"eyes",232,244,-8,-1,-.5F,5,2,1,0,-9,-10.2F);part(head,"eye_r",232,244,3,-1,-.5F,5,2,1,0,-9,-10.2F);
        var crown=empty(head,"crown");part(crown,"rock_a",192,192,-4,-9,-3,6,10,6,-5,-18,0);part(crown,"rock_b",192,192,-3,-10,-3,5,10,5,3,-18,3);part(crown,"rock_c",192,192,-2,-5,-2,5,6,5,9,-18,-2);
        for(int side:new int[]{-1,1}){String tag=side<0?"r":"l";var shoulder=part(torso,"shoulder_"+tag,144,0,-8,-4,-9,16,13,18,side*23,-32,0);for(int i=0;i<4;i++)part(shoulder,"growth_"+i,192,192,-4,-3,-4,8,7+i%2,8,side*(i*2),-4-i*2,2-i*3+(side>0?2:0));
            var upper=part(shoulder,"upper",side<0?0:80,112,-6,0,-6,12,18,12,0,6,0);var elbow=part(upper,"elbow",176,112,-7,-3,-7,14,6,14,0,18,0);var fore=part(elbow,"forearm",side<0?0:80,138,-8,0,-8,16,17,16,0,0,0);var wrist=part(fore,"wrist",176,112,-6,0,-6,12,5,12,0,16,0);var hand=part(wrist,"hand",side<0?128:188,64,-9,0,-8,18,13,16,0,4,0);
            for(int i=0;i<3;i++)part(hand,"finger_"+i,192,192,-2.5F,0,-3,5,9,6,-6+i*6,11,-4);part(hand,"thumb",192,192,-3,0,-3,6,10,6,side*10,2,-1);
            part(fore,"forearm_mark",232,244,-3,-3,-.6F,6,7,1,0,7,-8.5F);
            var hip=part(pelvis,"hip_"+tag,side<0?0:80,176,-8,0,-8,16,20,16,side*10,0,0);var knee=part(hip,"knee",192,192,-8,-3,-10,16,6,10,0,20,0);var shin=part(knee,"shin",side<0?0:80,208,-7,0,-7,14,15,14,0,0,0);var ankle=part(shin,"ankle",176,112,-6,0,-6,12,4,12,0,15,0);var foot=part(ankle,"foot",128,208,-9,-2,-14,18,5,24,0,1,0);for(int i=0;i<3;i++)part(foot,"toe_"+i,192,192,-3,-2,-4,6,4,6,-6+i*6,1,-12);
        }
        var roots=empty(torso,"roots");for(int i=0;i<8;i++)part(roots,"root_"+i,224,192,-1,0,-1,2,7+i%4*3,2,-17+i*5,-20+i%3*4,13);
        for(int i=0;i<6;i++)part(torso,"armor_"+i,144,32,-6,-4,-2,12,10,4,i<2?(i==0?-11:11):i<4?(i==2?-29:29):(i==4?-24:24),i<2?-20:i<4?-30:-6,-16);
        return LayerDefinition.create(m,256,256).bakeRoot();
    }
    public static final class BossModel extends EntityModel<EarthSpirit> {
        private static final class Stride {double x,z;float phase,weight,age;boolean initialized;}private final Map<EarthSpirit,Stride> strides=new WeakHashMap<>();
        final ModelPart root,pelvis,torso,head,jaw,core;final Map<String,ModelPart> nodes=new HashMap<>();
        BossModel(){root=bossMesh();index(root);pelvis=nodes.get("pelvis");torso=nodes.get("torso");head=nodes.get("head");jaw=nodes.get("jaw");core=nodes.get("core");}
        private void index(ModelPart p){for(String name:List.of("pelvis","lower","torso","chest","core","neck","head","jaw","roots","crown","shoulder_r","shoulder_l","hip_r","hip_l","armor_0","armor_1","armor_2","armor_3","armor_4","armor_5"))if(p.hasChild(name)){var child=p.getChild(name);nodes.put(name,child);}p.getAllParts().filter(q->q!=p).forEach(q->{for(String n:List.of("pelvis","lower","torso","chest","core","neck","head","jaw","roots","crown","shoulder_r","shoulder_l","hip_r","hip_l","armor_0","armor_1","armor_2","armor_3","armor_4","armor_5"))if(q.hasChild(n))nodes.put(n,q.getChild(n));});}
        private void arm(String side,float shoulder,float elbow,float wrist,float grip){var s=nodes.get("shoulder_"+side);s.xRot=shoulder;s.getChild("upper").getChild("elbow").xRot=elbow;var hand=s.getChild("upper").getChild("elbow").getChild("forearm").getChild("wrist");hand.xRot=wrist;for(int i=0;i<3;i++)hand.getChild("hand").getChild("finger_"+i).xRot=grip*(.8F+i*.12F);hand.getChild("hand").getChild("thumb").zRot=(side.equals("r")?-1:1)*grip*.8F;}
        public void poseLegs(float walk){nodes.get("hip_r").xRot=walk*.65F;nodes.get("hip_l").xRot=-walk*.65F;pelvis.zRot=walk*.035F;nodes.get("hip_r").getChild("knee").xRot=Math.max(0,-walk)*.55F;nodes.get("hip_l").getChild("knee").xRot=Math.max(0,walk)*.55F;for(String side:List.of("r","l")){var hip=nodes.get("hip_"+side);var knee=hip.getChild("knee");var ankle=knee.getChild("shin").getChild("ankle");ankle.xRot=-hip.xRot*.35F-knee.xRot*.5F;}pelvis.y-=(float)Math.abs(walk)*.6F;}
        public void setupAnim(EarthSpirit e,float swing,float amount,float age,float yaw,float pitch){root.getAllParts().forEach(p->{p.resetPose();p.visible=true;});float t=e.age(age-e.tickCount);head.yRot=(float)Math.toRadians(Math.clamp(yaw,-18,18));head.xRot=(float)Math.toRadians(Math.clamp(pitch,-10,12));torso.y=(float)Math.sin(age*.045)*.55F;var stride=strides.computeIfAbsent(e,k->new Stride());float partial=age-e.tickCount;double x=net.minecraft.util.Mth.lerp(partial,e.xo,e.getX()),z=net.minecraft.util.Mth.lerp(partial,e.zo,e.getZ());double moved=stride.initialized?Math.hypot(x-stride.x,z-stride.z):0;float dt=stride.initialized?Math.max(0,age-stride.age):0;stride.initialized=true;stride.age=age;stride.x=x;stride.z=z;boolean walking=e.action()==EarthRules.Action.IDLE||e.action()==EarthRules.Action.APPROACH||e.action()==EarthRules.Action.RECOVER;stride.phase+=(float)Math.min(moved,1)*2.8F;if(!walking)stride.weight=0;else if(dt>0)stride.weight+=(Math.max(amount,(float)Math.min(1,moved/dt*12))-stride.weight)*Math.min(1,dt*.4F);float walk=(float)Math.sin(stride.phase)*stride.weight;poseLegs(walk);arm("r",-.08F-walk*.12F,-.12F,.08F,.2F);arm("l",-.08F+walk*.12F,-.12F,.08F,.2F);
            for(int i=0;i<8;i++)nodes.get("roots").getChild("root_"+i).xRot=(float)Math.sin(age*.04+i)*.08F;
            for(int i=0;i<6;i++){var a=nodes.get("armor_"+i);a.visible=e.armorTicks()>0||e.action()==EarthRules.Action.ARMOR&&t>=8;a.z-=e.action()==EarthRules.Action.ARMOR?ramp(t,8,20)*3:3;}
            switch(e.action()){
                case AWAKEN->{float p=ramp(t,20,72),legs=ramp(t,16,36);pelvis.y=24-38*legs;nodes.get("hip_r").yScale=nodes.get("hip_l").yScale=legs;nodes.get("lower").visible=t>=32;nodes.get("lower").yScale=ramp(t,32,60);head.visible=t>=56;head.xScale=head.yScale=head.zScale=ramp(t,56,72);core.visible=t>=72;for(String side:List.of("r","l")){var shoulder=nodes.get("shoulder_"+side);shoulder.visible=t>=42;shoulder.xScale=shoulder.yScale=shoulder.zScale=ramp(t,42,66);}nodes.get("hip_r").xRot=(1-p)*-.25F;nodes.get("hip_l").xRot=(1-p)*-.25F;torso.xRot=(1-p)*.3F;head.xRot=(1-ramp(t,64,80))*.6F;arm("r",-ramp(t,35,65),-.4F,0,.3F);arm("l",-ramp(t,40,70),-.4F,0,.3F);jaw.xRot=ramp(t,72,80)*.35F;}
                case PILLAR->{float[] times={0,7,17,27,39,46,62,76};torso.xRot=pose(t,times,new float[]{0,.12F,.2F,0,-.26F,.25F,.12F,0});torso.yRot=pose(t,times,new float[]{0,-.12F,-.15F,-.3F,-.4F,.3F,.15F,0});float right=pose(t,times,new float[]{0,.3F,.2F,-1.1F,-2.0F,-.8F,-.4F,0});arm("r",right,pose(t,times,new float[]{0,-.3F,-.8F,-1.25F,-1.4F,-.1F,0,0}),-.2F,1-ramp(t,45,51));arm("l",pose(t,times,new float[]{0,.2F,.2F,-.6F,-1.1F,-.3F,.2F,0}),-.6F,.12F,.4F);pelvis.y+=pose(t,times,new float[]{0,1,3,0,-1,2,1,0});}
                case VOLLEY->{float end=26+(e.volleyTotal()-1)*EarthRules.SHOT_INTERVAL+16;float p=ramp(t,0,17)*(1-ramp(t,end-16,end));float pulse=t>=26?(float)Math.sin(Math.min(6,(t-26)%EarthRules.SHOT_INTERVAL)*Math.PI/6)*.12F:0;arm("r",-1.3F*p-pulse,-.2F*p,-.18F*p,.05F);arm("l",-.25F*p,-.6F*p,0,.3F);torso.yRot=-.12F*p;}
                case MELEE->{float[] times={0,4,10,13,18,24};arm("r",pose(t,times,new float[]{0,-.6F,-2.0F,.7F,.3F,0}),pose(t,times,new float[]{0,-.2F,-.7F,-.1F,0,0}),0,ramp(t,0,10));torso.yRot=pose(t,times,new float[]{0,-.1F,-.3F,.25F,.1F,0});torso.xRot=ramp(t,10,13)*(1-ramp(t,13,24))*.25F;}
                case COUNTER,TREMOR->{boolean big=e.action()==EarthRules.Action.TREMOR;float impact=big?33:26,end=big?76:50;float[] times={0,6,big?24:17,impact-2,impact,impact+10,end};float s=pose(t,times,new float[]{0,-.2F,big?-2.7F:-1.8F,big?-2.7F:-1.8F,.9F,.6F,0});arm("r",s,-.45F,0,.9F);arm("l",s-.05F,-.5F,0,.9F);torso.xRot=pose(t,times,new float[]{0,-.1F,-.18F,-.18F,.4F,.25F,0});pelvis.y=pose(t,times,new float[]{-14,-12,-14,-14,-10,-11,-14});}
                case SUMMON->{float[] times={0,10,22,32,40,44};arm("r",pose(t,times,new float[]{0,-.9F,-.6F,.3F,.1F,0}),-.9F,.3F,.1F);arm("l",pose(t,times,new float[]{0,-.9F,-.6F,.3F,.1F,0}),-.9F,.3F,.1F);torso.xRot=pose(t,times,new float[]{0,-.1F,.2F,.2F,.1F,0});}
                case SPIKES->{float[] times={0,8,20,24,32,46};arm("r",pose(t,times,new float[]{0,-.5F,-1.8F,.7F,.3F,0}),-.4F,0,.05F);arm("l",-.6F,-1.2F,0,.9F);torso.xRot=pose(t,times,new float[]{0,.1F,-.1F,.3F,.2F,0});}
                case ARMOR->{arm("r",-.6F,-1.2F,0,.3F);arm("l",-.6F,-1.2F,0,.3F);head.xRot=.2F;}
                case PHASE->{float p=ramp(t,0,22)*(1-ramp(t,35,50));arm("r",-.7F*p,-.4F,0,.1F);arm("l",-.7F*p,-.4F,0,.1F);nodes.get("shoulder_r").zRot=-.45F*p;nodes.get("shoulder_l").zRot=.45F*p;jaw.xRot=.45F*p;torso.xRot=-.15F*p;}
                case DORMANT->{head.xRot=.3F;torso.xRot=.08F;}
                case DEATH->{float shake=(1-ramp(t,6,20))*.04F;torso.zRot=(float)Math.sin(t*2.3)*shake;head.yRot=(float)Math.sin(t*1.8)*shake;nodes.get("crown").visible=t<20;nodes.get("shoulder_r").visible=nodes.get("shoulder_l").visible=t<34;head.visible=t<48;torso.visible=t<62;nodes.get("lower").visible=t<72;nodes.get("hip_r").visible=nodes.get("hip_l").visible=t<82;pelvis.visible=t<86;nodes.get("roots").visible=t<58;arm("l",-.08F,-.12F,.08F,0);arm("r",-.08F,-.12F,.08F,0);}
                default->{}
            }if(e.hurtTime>0)torso.zRot+=(float)Math.sin(e.hurtTime*.6)*.025F;
        }
        public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,int color){var luminous=new ArrayList<ModelPart>();luminous.add(head.getChild("eyes"));luminous.add(head.getChild("eye_r"));for(int i=0;i<13;i++)luminous.add(core.getChild("spiral_"+i));luminous.forEach(n->n.skipDraw=true);root.render(p,v,light,OverlayTexture.NO_OVERLAY,color);root.getAllParts().forEach(n->n.skipDraw=true);luminous.forEach(n->n.skipDraw=false);root.render(p,v,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,color);root.getAllParts().forEach(n->n.skipDraw=false);}
    }
    public static final class BossRenderer extends MobRenderer<EarthSpirit,BossModel> {
        org.joml.Matrix4f frameBase=new org.joml.Matrix4f();
        BossRenderer(EntityRendererProvider.Context c){super(c,new BossModel(),1.5F);addLayer(new StonySkinLayer(this));addLayer(new HeldPillarLayer(this));}
        @Override protected void scale(EarthSpirit e,PoseStack p,float partial){float s=(float)(EarthRules.HEIGHT/6.6875);p.scale(s,s,s);}
        @Override protected float getFlipDegrees(EarthSpirit e){return 0;} // The segmented death timeline owns the fall.
        public ResourceLocation getTextureLocation(EarthSpirit e){return EarthContent.id("textures/entity/earth_spirit/phase_"+e.phase()+".png");}
        @Override public void render(EarthSpirit e,float yaw,float partial,PoseStack p,MultiBufferSource b,int light){frameBase.set(p.last().pose());super.render(e,yaw,partial,p,b,light);}
    }
    /** Real vanilla block materials on articulated stone clusters; no world blocks. */
    static final class StonySkinLayer extends RenderLayer<EarthSpirit,BossModel>{
        StonySkinLayer(BossRenderer r){super(r);}
        private void piece(PoseStack p,MultiBufferSource buffers,int light,List<String> path,double x,double y,double z,float size,net.minecraft.world.level.block.Block block){p.pushPose();try{var part=getParentModel().root;if(!part.visible)return;part.translateAndRotate(p);for(String name:path){part=part.getChild(name);if(!part.visible)return;part.translateAndRotate(p);}p.translate(x/16,y/16,z/16);p.scale(size,-size,size);p.translate(-.5,-.5,-.5);net.minecraft.client.Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(),p,buffers,light,OverlayTexture.NO_OVERLAY,net.neoforged.neoforge.client.model.data.ModelData.EMPTY,null);}finally{p.popPose();}}
        @Override public void render(PoseStack p,MultiBufferSource b,int light,EarthSpirit e,float swing,float amount,float partial,float age,float yaw,float pitch){var cobble=net.minecraft.world.level.block.Blocks.COBBLESTONE;var moss=net.minecraft.world.level.block.Blocks.MOSSY_COBBLESTONE;var andesite=net.minecraft.world.level.block.Blocks.ANDESITE;
            var torso=List.of("pelvis","lower","torso");for(int side:new int[]{-1,1}){piece(p,b,light,torso,side*13,-29,-14.3,.53F,side<0?cobble:moss);piece(p,b,light,torso,side*14,-17,-14.7,.50F,cobble);piece(p,b,light,torso,side*6,-7,-13.4,.48F,andesite);String tag=side<0?"r":"l";var shoulder=List.of("pelvis","lower","torso","shoulder_"+tag);piece(p,b,light,shoulder,side*3,-3,-7.4,.62F,moss);piece(p,b,light,shoulder,side*7,3,-5.2,.50F,cobble);var forearm=List.of("pelvis","lower","torso","shoulder_"+tag,"upper","elbow","forearm");piece(p,b,light,forearm,side*8,5,.4,.52F,cobble);piece(p,b,light,forearm,side*7,13,1.3,.48F,andesite);var shin=List.of("pelvis","hip_"+tag,"knee","shin");piece(p,b,light,shin,side*6,5,-1.2,.46F,cobble);piece(p,b,light,shin,side*5,11,-2.3,.43F,moss);}
            var crown=List.of("pelvis","lower","torso","neck","head","crown");piece(p,b,light,crown,-5,-21,.7,.40F,moss);piece(p,b,light,crown,3,-23,3.4,.37F,cobble);
        }
    }
    /** RenderLayer inherits the exact body transform; the held shaft follows all wrist pivots. */
    static final class HeldPillarLayer extends RenderLayer<EarthSpirit,BossModel>{
        final BossRenderer renderer;
        HeldPillarLayer(BossRenderer r){super(r);renderer=r;}
        public void render(PoseStack p,MultiBufferSource b,int light,EarthSpirit e,float swing,float amount,float partial,float age,float yaw,float pitch){
            float t=e.age(partial);boolean pillar=e.action()==EarthRules.Action.PILLAR&&t>=20&&t<46;
            boolean volley=e.action()==EarthRules.Action.VOLLEY&&e.pending()>0;
            if(!pillar&&!volley)return;
            p.pushPose();ModelPart part=getParentModel().root;part.translateAndRotate(p);
            for(String name:List.of("pelvis","lower","torso","shoulder_r","upper","elbow","forearm","wrist","hand")){part=part.getChild(name);part.translateAndRotate(p);}
            p.translate(0,7/16F,-4/16F);
            if(pillar){
                // Start at the real consumed pillar, then lift into the wrist, with no teleport.
                var at=e.pillarOrigin().getCenter().add(.5,2.5,.5).subtract(e.position());
                var start=renderer.frameBase.transformPosition(new org.joml.Vector3f((float)at.x,(float)at.y,(float)at.z));
                var hand=p.last().pose().transformPosition(new org.joml.Vector3f());
                var point=start.lerp(hand,ramp(t,20,27));
                var local=new org.joml.Matrix4f(p.last().pose()).invert().transformPosition(point);
                p.translate(local.x,local.y,local.z);
                ActorRenderer.COLUMN.render(p,b.getBuffer(RenderType.entityCutoutNoCull(EarthContent.id("textures/entity/earth_spirit/pillar.png"))),light,OverlayTexture.NO_OVERLAY);
            }else for(int i=0;i<e.pending();i++){
                double a=-1+i*.45;p.pushPose();p.translate(Math.cos(a)*1.2,Math.sin(a)*.7,-1.2);
                p.mulPose(Axis.YP.rotationDegrees((e.tickCount+partial)*4+i*30));ActorRenderer.rock(p,b,light,.6F,false,i%3);p.popPose();
            }
            p.popPose();
        }
    }
    public static ModelPart servantMesh(int variant){resetGeometry();var m=new MeshDefinition();var root=m.getRoot();var body=part(root,"body",0,16,-5,-10,-4,10,10,8,0,17,0);var head=part(body,"head",0,0,-4,-7,-4,8,7,8,0,-10,-1);part(head,"brow",32,32,-5,-2,-1,10,3,3,0,-5,-4);part(head,"eyes",56,56,-3,0,-.5F,2,1,1,0,-3,-4.5F);part(head,"eye_r",56,56,1,0,-.5F,2,1,1,0,-3,-4.5F);part(body,"mark",56,56,-1,-2,-.5F,2,4,1,0,-5,-4.5F);
        for(int side:new int[]{-1,1}){var arm=part(body,side<0?"r":"l",32,16,-2.5F,0,-2.5F,5,7,5,side*7,-10,0);part(arm,"fist",32,32,-3,0,-3,6,6,6,0,6,0);part(arm,"shoulder",32,0,-3,-2,-3,6,4+(variant==1&&side>0?2:0),6,0,0,0);var leg=part(root,side<0?"leg_r":"leg_l",0,40,-2,0,-2,4,6,4,side*3,18,0);part(leg,"foot",32,48,-2.5F,-1,-4,5,2,7,0,5,0);}return LayerDefinition.create(m,64,64).bakeRoot();}
    public static final class ServantModel extends EntityModel<EarthServant>{final ModelPart root,body;ServantModel(int variant){root=servantMesh(variant);body=root.getChild("body");}public void setupAnim(EarthServant e,float swing,float amount,float age,float yaw,float pitch){root.getAllParts().forEach(p->{p.resetPose();p.visible=true;});body.xRot=.12F;body.getChild("head").yRot=(float)Math.toRadians(Math.clamp(yaw,-40,40));float walk=(float)Math.sin(swing*.8)*amount;root.getChild("leg_r").xRot=walk*.6F;root.getChild("leg_l").xRot=-walk*.6F;body.getChild("r").xRot=-walk*.4F;body.getChild("l").xRot=walk*.4F;if(e.state()==0)root.y+=(1-ramp(e.age(age-e.tickCount),0,12))*22;if(e.state()==2)body.getChild("r").xRot=pose(e.age(age-e.tickCount),new float[]{0,4,9,12,21},new float[]{0,-.8F,-1.6F,.6F,0});if(e.state()==3||e.deathTime>0){float f=ramp(e.state()==3?e.age(age-e.tickCount):e.deathTime,0,16);body.xRot=f;root.y+=f*12;}}public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,int color){root.render(p,v,light,overlay,color);}}
    static final class ServantRenderer extends MobRenderer<EarthServant,ServantModel>{final ServantModel a,b;ServantRenderer(EntityRendererProvider.Context c){super(c,new ServantModel(0),.4F);a=model;b=new ServantModel(1);}public void render(EarthServant e,float yaw,float partial,PoseStack p,MultiBufferSource buf,int light){model=e.variant()==0?a:b;super.render(e,yaw,partial,p,buf,light);}public ResourceLocation getTextureLocation(EarthServant e){return EarthContent.id("textures/entity/earth_spirit/servant_"+e.variant()+".png");}protected void scale(EarthServant e,PoseStack p,float partial){p.scale(.896F,.896F,.896F);}}
    static final class ActorRenderer extends EntityRenderer<EarthActor>{
        static final ModelPart[] ROCKS={rockMesh(0),rockMesh(1),rockMesh(2)};static final ModelPart COLUMN=columnMesh();
        ActorRenderer(EntityRendererProvider.Context c){super(c);}
        static ModelPart rockMesh(int variant){resetGeometry();var m=new MeshDefinition();var r=m.getRoot();part(r,"main",0,0,-4,-4,-4,8,8,8,0,0,0);for(int i=0;i<4;i++)part(r,"chip_"+i,32,0,-2,-2,-2,4,4,4,(i%2==0?-3:3),i<2?-3:3,variant==1?3:-2);return LayerDefinition.create(m,64,64).bakeRoot();}
        static ModelPart columnMesh(){resetGeometry();var m=new MeshDefinition();var r=m.getRoot();for(int i=0;i<8;i++)part(r,"segment_"+i,0,0,-14,-5.5F,-14,28,11,28,(i%2)*.8F,-38.5F+i*11,0);return LayerDefinition.create(m,128,128).bakeRoot();}
        static void rock(PoseStack p,MultiBufferSource b,int light,float scale,boolean column,int variant){p.pushPose();p.scale(scale,-scale,scale);(column?COLUMN:ROCKS[Math.floorMod(variant,3)]).render(p,b.getBuffer(RenderType.entityCutoutNoCull(EarthContent.id("textures/entity/earth_spirit/"+(column?"pillar":"rock")+".png"))),light,OverlayTexture.NO_OVERLAY);p.popPose();}
        public ResourceLocation getTextureLocation(EarthActor e){return EarthContent.id("textures/entity/earth_spirit/rock.png");}
        public void render(EarthActor e,float yaw,float partial,PoseStack p,MultiBufferSource b,int light){float age=e.age(partial);switch(e.kind()){
            case EarthActor.COLLAPSE_BLOCK->{p.pushPose();float shrink=1-ramp(age,e.life()-16,e.life());float size=e.radius()*shrink;p.translate(0,size*.5,0);if(!e.onGround()){p.mulPose(Axis.YP.rotationDegrees(age*7+e.getId()*31));p.mulPose(Axis.XP.rotationDegrees(age*5));}p.scale(size,size,size);p.translate(-.5,-.5,-.5);net.minecraft.client.Minecraft.getInstance().getBlockRenderer().renderSingleBlock(net.minecraft.world.level.block.Block.stateById(e.variant()),p,b,light,OverlayTexture.NO_OVERLAY,net.neoforged.neoforge.client.model.data.ModelData.EMPTY,null);p.popPose();}
            case EarthActor.STONE,EarthActor.DEBRIS->{p.pushPose();p.mulPose(Axis.YP.rotationDegrees(age*11));p.mulPose(Axis.XP.rotationDegrees(age*6));rock(p,b,light,e.kind()==EarthActor.STONE?1.1F:e.radius()*2,false,e.variant());p.popPose();}
            case EarthActor.PILLAR->{p.pushPose();p.mulPose(Axis.YP.rotationDegrees(-e.getYRot()));p.mulPose(Axis.XP.rotationDegrees(e.kind()==EarthActor.PILLAR?90+e.getXRot():e.getXRot()));if(e.kind()==EarthActor.PILLAR)p.mulPose(Axis.YP.rotationDegrees(age*4));rock(p,b,light,1,true,e.variant());p.popPose();}
            case EarthActor.WARNING,EarthActor.COUNTER,EarthActor.TREMOR->{float travel=e.kind()==EarthActor.TREMOR?16:9;float radius=e.kind()==EarthActor.WARNING?e.radius():Math.min(e.radius(),1.5F+(e.radius()-1.5F)*age/travel);ring(p,b,radius,age<e.life()-3?0xffc48645:0xff815432);}
            case EarthActor.LINE_WARN,EarthActor.SPIKES->{p.pushPose();Vec3Compat.rotate(p,e.direction());for(int i=0;i<3;i++){p.pushPose();p.translate(0,0,2+i*4);if(e.kind()==EarthActor.LINE_WARN)rectangle(p,b,1,2,0xffc48645);else if(age>=i*2){p.translate(0,Math.sin(Math.min(1,(age-i*2)/10)*Math.PI)*.7,0);for(int j=0;j<3;j++){p.pushPose();p.translate((j-1)*.6,0,0);rock(p,b,light,1,false,j);p.popPose();}}p.popPose();}p.popPose();}
            case EarthActor.GATE->{p.pushPose();p.mulPose(Axis.YP.rotationDegrees(e.variant()*-90));float open=1-ramp(age,0,38);for(int side:new int[]{-1,1}){p.pushPose();p.translate(side*(1.25+open*2.5),3.5,0);p.scale(1.4F,7F/5.5F,.7F);rock(p,b,light,1,true,0);p.popPose();}p.popPose();}
            default->{}
        }super.render(e,yaw,partial,p,b,light);}
        static void ring(PoseStack p,MultiBufferSource b,float radius,int color){var v=b.getBuffer(RenderType.debugQuads());var matrix=p.last().pose();int steps=64;for(int i=0;i<steps;i++){double a=i*Math.PI*2/steps,c=(i+1)*Math.PI*2/steps;for(double[] point:new double[][]{{Math.cos(a)*(radius-.12),Math.sin(a)*(radius-.12)},{Math.cos(c)*(radius-.12),Math.sin(c)*(radius-.12)},{Math.cos(c)*(radius+.12),Math.sin(c)*(radius+.12)},{Math.cos(a)*(radius+.12),Math.sin(a)*(radius+.12)}})v.addVertex(matrix,(float)point[0],.045F,(float)point[1]).setColor(color);}}
        static void rectangle(PoseStack p,MultiBufferSource b,float halfWidth,float halfLength,int color){var v=b.getBuffer(RenderType.debugQuads());var m=p.last().pose();v.addVertex(m,-halfWidth,.045F,-halfLength).setColor(color);v.addVertex(m,halfWidth,.045F,-halfLength).setColor(color);v.addVertex(m,halfWidth,.045F,halfLength).setColor(color);v.addVertex(m,-halfWidth,.045F,halfLength).setColor(color);}
    }
    private static final class Vec3Compat{static void rotate(PoseStack p,net.minecraft.world.phys.Vec3 d){p.mulPose(Axis.YP.rotationDegrees((float)Math.toDegrees(Math.atan2(d.x,d.z))));}}
    private EarthClient(){}
}
