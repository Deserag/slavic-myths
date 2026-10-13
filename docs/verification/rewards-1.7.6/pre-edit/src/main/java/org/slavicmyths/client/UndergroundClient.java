package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.underground.*;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class UndergroundClient {
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(UndergroundMobs.PODZEMNIK.get(),c->new Renderer<>(c,0));e.registerEntityRenderer(UndergroundMobs.KLADOVIK.get(),c->new Renderer<>(c,2));e.registerEntityRenderer(UndergroundMobs.LARGE.get(),c->new Renderer<>(c,3));e.registerEntityRenderer(UndergroundMobs.STONE.get(),c->new ThrownItemRenderer<>(c,1.2F,false));e.registerEntityRenderer(UndergroundMobs.SPIKE.get(),SpikeRenderer::new);}
 public static final class Model<T extends EncounterMob> extends EntityModel<T> implements ArmedModel {
  final ModelPart root,body,head,right,left,rf,lf,legR,legL,jaw,held;final int kind;
  Model(int kind){this.kind=kind;root=mesh(kind);body=root.getChild("body");head=body.getChild("head");right=body.getChild("right_arm");left=body.getChild("left_arm");rf=right.getChild("forearm");lf=left.getChild("forearm");legR=root.getChild("right_leg");legL=root.getChild("left_leg");jaw=head.getChild("jaw");held=rf.getChild("held_stone");}
  static CubeListBuilder cube(int u,int v,float x,float y,float z,float w,float h,float d){return CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d);}
  static ModelPart mesh(int kind){boolean big=kind==3,keeper=kind==2,miner=kind==1;float torso=big?19:keeper?9:12,leg=big?14:keeper?5:12,width=big?15:keeper?8:miner?10:9,headW=big?11:keeper?9:8,headH=big?11:8,headD=big?9:7,arm=big?7:keeper?3:4,upper=big?10:keeper?5:8,lower=big?10:keeper?5:7;
   var mesh=new MeshDefinition();var r=mesh.getRoot();var body=r.addOrReplaceChild("body",cube(0,32,-width/2,-torso,-(big?4:3),width,torso,big?8:6),PartPose.offset(0,24-leg,0));
   var head=body.addOrReplaceChild("head",cube(0,0,-headW/2,-headH,-headD/2,headW,headH,headD),PartPose.offset(0,-torso,keeper?-2:-1));
   head.addOrReplaceChild("nose",cube(48,72,-1.5F,-headH+3,-headD/2-(keeper?4:2),3,3,keeper?5:3),PartPose.ZERO);
   head.addOrReplaceChild("brows",CubeListBuilder.create().texOffs(0,64).addBox(-headW/2-.5F,-headH+1,-headD/2-1,headW/2,2,2).texOffs(0,64).addBox(.5F,-headH+1,-headD/2-1,headW/2,2,2),PartPose.ZERO);
   var jaw=head.addOrReplaceChild("jaw",cube(0,64,-headW/2-.5F,-1,-headD/2-.6F,headW+1,3,headD+1),PartPose.ZERO);
   if(big)jaw.addOrReplaceChild("teeth",CubeListBuilder.create().texOffs(110,108).addBox(-4,-2,-headD/2-1.2F,1.5F,2,.8F).texOffs(110,108).addBox(2.5F,-2,-headD/2-1.2F,1.5F,2,.8F).texOffs(110,108).addBox(-1,-1.2F,-headD/2-1.2F,2,1,.8F),PartPose.ZERO);
   if(keeper)jaw.addOrReplaceChild("beard",CubeListBuilder.create().texOffs(108,112).addBox(-3,-1,-headD/2-1,2,3,1).texOffs(108,112).addBox(-1,-1,-headD/2-1,2,4,1).texOffs(108,112).addBox(1,-1,-headD/2-1,2,2.5F,1),PartPose.ZERO);
   if(keeper){body.addOrReplaceChild("coffer",CubeListBuilder.create().texOffs(64,64).addBox(-5,-12,3,10,12,7).texOffs(64,96).addBox(-5.5F,-13,3,11,2,7.5F).texOffs(96,64).addBox(-5.5F,-10,5,11,1,1).texOffs(96,64).addBox(-5.5F,-5,5,11,1,1),PartPose.ZERO);body.addOrReplaceChild("pendants",CubeListBuilder.create().texOffs(96,64).addBox(-4,-9,-3.5F,1,7,1).texOffs(96,64).addBox(3,-9,-3.5F,1,7,1).texOffs(96,64).addBox(-1,-5,-4,2,2,1),PartPose.ZERO);}
   if(big){body.addOrReplaceChild("shoulder_stones",CubeListBuilder.create().texOffs(64,0).addBox(-12,-torso-1,-3,8,6,8).texOffs(64,0).addBox(5,-torso-2,-4,8,7,7).texOffs(64,32).addBox(-9,-torso-5,-2,5,5,5).texOffs(64,32).addBox(8,-torso-6,-2,4,5,4),PartPose.ZERO);}
   if(miner){head.addOrReplaceChild("mining_cap",cube(64,96,-headW/2-.5F,-headH-1,-headD/2-.5F,headW+1,3,headD+1),PartPose.ZERO);head.addOrReplaceChild("goggles",CubeListBuilder.create().texOffs(100,92).addBox(-3,-headH+3,-headD/2-1,2,2,1).texOffs(100,92).addBox(1,-headH+3,-headD/2-1,2,2,1).texOffs(96,64).addBox(4,-headH+1,-1,2,3,3),PartPose.ZERO);body.addOrReplaceChild("straps",CubeListBuilder.create().texOffs(96,64).addBox(-3,-torso,-3.5F,1,torso,1).texOffs(96,64).addBox(2,-torso,-3.5F,1,torso,1),PartPose.ZERO);}
   for(int side:new int[]{-1,1}){var a=body.addOrReplaceChild(side<0?"right_arm":"left_arm",cube(0,64,-arm/2,0,-arm/2,arm,upper,arm),PartPose.offset(side*(width/2+arm/2),-torso+2,0));var f=a.addOrReplaceChild("forearm",cube(big?64:0,big?0:64,-arm/2-.5F,0,-arm/2-.5F,arm+1,lower,arm+1),PartPose.offset(0,upper,0));f.addOrReplaceChild("fist",cube(0,64,-arm/2-1,lower-2,-arm/2-1,arm+2,3,arm+2),PartPose.ZERO);f.addOrReplaceChild("held_stone",cube(64,0,-3,lower-2,-4,6,6,6),PartPose.ZERO);}
   for(int side:new int[]{-1,1}){var l=r.addOrReplaceChild(side<0?"right_leg":"left_leg",cube(0,96,-(big?3:2),0,-2,big?6:4,leg,big?6:4),PartPose.offset(side*(big?4:keeper?2.5F:2.7F),24-leg,0));l.addOrReplaceChild("foot",cube(0,64,-(big?3.5F:2.5F),leg-3,-4,big?7:5,3,big?8:6),PartPose.ZERO);}
   return LayerDefinition.create(mesh,128,128).bakeRoot();
  }
  public void setupAnim(T e,float swing,float amount,float age,float yaw,float pitch){root.getAllParts().forEach(ModelPart::resetPose);head.yRot=(float)Math.toRadians(Math.max(-65,Math.min(65,yaw)));head.xRot=(float)Math.toRadians(Math.max(-30,Math.min(30,pitch)));float walk=net.minecraft.util.Mth.cos(swing*.55F)*amount;legR.xRot=walk*.8F;legL.xRot=-walk*.8F;right.xRot=-walk*.5F;left.xRot=walk*.5F;body.xRot=(kind==2?.18F:kind==3?.12F:.09F)+(float)Math.sin(age*.045)*.018F;held.visible=false;lf.getChild("held_stone").visible=false;float t=e.age(0);var action=e.action();
   if(e.deathTime>0){body.zRot=Math.min(1.4F,e.deathTime*.07F);right.xRot=-.4F;left.xRot=-.4F;return;}
   if(e.hurtTime>0)body.zRot=(float)Math.sin(e.hurtTime*.5F)*.09F;
   switch(action){case ALERT,RAGE->{right.xRot=left.xRot=-1.4F;rf.xRot=lf.xRot=-.7F;jaw.xRot=.4F;body.xRot=-.08F;}
    case MELEE->{float p=Math.min(1,t/(kind==3?10:kind==2?7:6));right.xRot=p<.75F?-2*p:-1.5F+(p-.75F)*5;rf.xRot=-.45F;body.yRot=(float)Math.sin(t*.18)*.12F;}
    case MINER_HEAVY,HEAVY,TREMOR,SPIKES->{int impact=action==EncounterMob.Action.HEAVY?(e.raged()?32:38):action==EncounterMob.Action.MINER_HEAVY?20:action==EncounterMob.Action.TREMOR?24:26;float p=Math.min(1,t/Math.max(1,impact-3));right.xRot=-2.5F*p;left.xRot=action==EncounterMob.Action.MINER_HEAVY?-.35F:-2.5F*p;rf.xRot=lf.xRot=-.35F;body.xRot=.05F;if(t>=impact-3){float hit=Math.min(1,(t-impact+3)/3);right.xRot=-2.5F+hit*3.2F;left.xRot=action==EncounterMob.Action.MINER_HEAVY?-.35F:right.xRot;body.xRot=hit*.45F;}if(t>impact+8){right.xRot=left.xRot=.35F;}}
    case THROW->{held.visible=t<16||t>=20&&t<24;right.xRot=t<16?-1.8F*(t/16):t>=20&&t<24?-.8F-(t-20)/4:-.8F;rf.xRot=held.visible?-1.2F:0;body.yRot=held.visible?-.18F:.12F;}
    case BURROW->{float depth=t<20?Math.max(0,(t-13)/7):t<75?1:Math.max(0,1-(t-75)/7);root.y+=depth*25;right.xRot=left.xRot=-1.4F;rf.xRot=lf.xRot=-.7F;}
    case COIN_WAVE->{right.xRot=left.xRot=-.65F;right.yRot=-.8F;left.yRot=.8F;rf.xRot=lf.xRot=-1;body.xRot=t<20?.3F:-.15F;}
    case CHARGE,LUNGE->{body.xRot=t<18?.45F:.55F;right.xRot=left.xRot=.45F;legR.xRot=(float)Math.sin(age*1.4)*.8F;legL.xRot=-legR.xRot;}
    default->{}
   }
  }
  public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,int color){root.render(p,v,light,overlay,color);}
  public void translateToHand(HumanoidArm side,PoseStack p){root.translateAndRotate(p);body.translateAndRotate(p);var a=side==HumanoidArm.RIGHT?right:left;a.translateAndRotate(p);a.getChild("forearm").translateAndRotate(p);p.translate(0,kind==3?.55:.4,0);}
 }
 public static final class Renderer<T extends EncounterMob> extends MobRenderer<T,Model<T>> {
  private final Model<T> common,miner;private final int kind;
  Renderer(EntityRendererProvider.Context c,int kind){super(c,new Model<>(kind),kind==3?.8F:.4F);this.kind=kind;common=model;miner=kind==0?new Model<>(1):null;addLayer(new ItemInHandLayer<>(this,c.getItemInHandRenderer()));}
  public void render(T e,float yaw,float partial,PoseStack p,net.minecraft.client.renderer.MultiBufferSource buffers,int light){model=e instanceof Podzemnik m&&m.variant()==1?miner:common;super.render(e,yaw,partial,p,buffers,light);}
  protected void scale(T e,PoseStack p,float partial){float s=kind==0?(e instanceof Podzemnik m&&m.variant()==1?.88F:.825F):1;p.scale(s,s,s);}
  public ResourceLocation getTextureLocation(T e){return UndergroundMobs.id("textures/entity/underground/"+(kind==2?"kladovik":kind==3?"large_podzemnik":e instanceof Podzemnik m&&m.variant()==1?"podzemnik_miner":"podzemnik")+(e.raged()?"_rage":"")+".png");}
 }
 static final class SpikeRenderer extends EntityRenderer<StoneSpike>{private final ModelPart spike;SpikeRenderer(EntityRendererProvider.Context c){super(c);var m=new MeshDefinition();m.getRoot().addOrReplaceChild("spike",CubeListBuilder.create().texOffs(64,0).addBox(-4,-7,-4,8,7,8).texOffs(64,0).addBox(-3,-12,-3,6,5,6).texOffs(64,0).addBox(-1.5F,-17,-1.5F,3,5,3),PartPose.ZERO);spike=LayerDefinition.create(m,128,128).bakeRoot();}public ResourceLocation getTextureLocation(StoneSpike e){return UndergroundMobs.id("textures/entity/underground/large_podzemnik.png");}public void render(StoneSpike e,float yaw,float partial,PoseStack p,net.minecraft.client.renderer.MultiBufferSource b,int light){p.pushPose();p.scale(1,-1,1);float scale=Math.min(1,(e.tickCount+partial)/2);p.scale(scale,scale,scale);spike.render(p,b.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(e))),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);p.popPose();super.render(e,yaw,partial,p,b,light);}}
 @EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
 public static final class KnockdownVisual {
  @SubscribeEvent public static void pose(RenderPlayerEvent.Pre e){if(OminousAura.knocked(e.getEntity()))e.getPoseStack().mulPose(Axis.ZP.rotationDegrees(30));}
 }
}
