package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.poloz.*;
/** Render the authoritative 24-piece track, not a detached root-only model. */
public final class PolozRenderer extends EntityRenderer<GreatPoloz> {
 private final ModelPart head,body,scar;public PolozRenderer(EntityRendererProvider.Context c){super(c);head=head();body=body();scar=scar();shadowRadius=1.7f;}
 public static ModelPart head(){var m=new MeshDefinition();var r=m.getRoot();r.addOrReplaceChild("skull",CubeListBuilder.create().texOffs(0,0).addBox(-6,0,-8,12,10,16).texOffs(0,32).addBox(-5,2,-12,10,6,4),PartPose.ZERO);r.addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(0,42).addBox(-4.8f,0,-10,9.6f,2.4f,12),PartPose.offset(0,0,0));for(int sign:new int[]{-1,1}){r.addOrReplaceChild("eye"+sign,CubeListBuilder.create().texOffs(58,0).addBox(sign>0?5.9f:-6.1f,6,-5,.2f,1.4f,2),PartPose.ZERO);for(int i=0;i<3;i++)r.addOrReplaceChild("tooth"+sign+i,CubeListBuilder.create().texOffs(58,12).addBox(sign*4.2f-.5f,1,-8+i*3,1,2,1),PartPose.ZERO);r.addOrReplaceChild("crest"+sign,CubeListBuilder.create().texOffs(48,20).addBox(sign*3-1,9,3,2,3,5),PartPose.ZERO);}for(int sign:new int[]{-1,1}) {
  r.addOrReplaceChild("cheek"+sign,CubeListBuilder.create().texOffs(0,28).addBox(sign>0?5:-7,2,-5,2,7,10),PartPose.ZERO);
  for(int i=0;i<4;i++) {
   r.addOrReplaceChild("browplate"+sign+i,CubeListBuilder.create().texOffs(0,28).addBox(sign>0?3:-6,8+i*.2f,-9+i*3,3,2.4f,3.2f),PartPose.ZERO);
   r.addOrReplaceChild("fang"+sign+i,CubeListBuilder.create().texOffs(58,12).addBox(sign*4-.45f,-2,-10+i*3, .9f,4.5f,.9f),PartPose.ZERO);
   r.getChild("jaw").addOrReplaceChild("lowerfang"+sign+i,CubeListBuilder.create().texOffs(58,12).addBox(sign*3.8f-.35f,2,-9+i*3,.7f,2.8f,.7f),PartPose.ZERO);
  }
  for(int i=0;i<3;i++)r.addOrReplaceChild("horn"+sign+i,CubeListBuilder.create().texOffs(56,12).addBox(-.8f,0,-.8f,1.6f,4,1.6f).addBox(-.4f,4,-.4f,.8f,2,.8f),PartPose.offsetAndRotation(sign*4,9,-1+i*3,.35f,0,sign*-.5f));
 }return LayerDefinition.create(m,64,64).bakeRoot();}
 public static ModelPart body(){var m=new MeshDefinition();var r=m.getRoot();r.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,0).addBox(-8,0,-4,16,16,8),PartPose.ZERO);for(int i=0;i<3;i++){for(int s:new int[]{-1,1})r.addOrReplaceChild("plate"+s+i,CubeListBuilder.create().texOffs(0,28).addBox(s>0?7.7f:-8.7f,2+i*4,-3.8f,.9f,3.8f,7.6f),PartPose.ZERO);r.addOrReplaceChild("ridge"+i,CubeListBuilder.create().texOffs(36,28).addBox(-4+i*3,15.9f,-3.2f,2.6f,1,6.4f),PartPose.ZERO);}for(int i=0;i<3;i++){
  r.addOrReplaceChild("spine"+i,CubeListBuilder.create().texOffs(58,12).addBox(-.6f,0,-.6f,1.2f,3,1.2f).addBox(-.3f,3,-.3f,.6f,1.5f,.6f),PartPose.offsetAndRotation(-3+i*3,16,0,.3f,0,(i-1)*.3f));
  for(int sign:new int[]{-1,1})r.addOrReplaceChild("overlap"+sign+i,CubeListBuilder.create().texOffs(0,28).addBox(-2,0,-3.5f,4,1.2f,7),PartPose.offsetAndRotation(sign*6,11+i*1.5f,0,0,0,sign*-.45f));
 }return LayerDefinition.create(m,64,64).bakeRoot();}
 public static ModelPart scar(){var m=new MeshDefinition();m.getRoot().addOrReplaceChild("scar",CubeListBuilder.create().texOffs(48,48).addBox(-5,0,-1,3,4,2).addBox(-2,3,-1,3,4,2).addBox(1,6,-1,3,3,2),PartPose.ZERO);return LayerDefinition.create(m,64,64).bakeRoot();}
 @Override public ResourceLocation getTextureLocation(GreatPoloz e){return PolozContent.id("textures/entity/great_poloz_body.png");}
 private Vec3 at(PolozPart part,float alpha){return new Vec3(part.xo,part.yo,part.zo).lerp(part.position(),alpha);}
 @Override public boolean shouldRender(GreatPoloz e,net.minecraft.client.renderer.culling.Frustum f,double x,double y,double z){return true;}
 @Override public void render(GreatPoloz e,float yaw,float partial,PoseStack p,MultiBufferSource b,int light){if(e.parts[0].position().distanceToSqr(e.position())>10000)return;float alpha=e.trackAlpha(partial);Vec3 root=new Vec3(e.xo,e.yo,e.zo).lerp(e.position(),partial);
  p.pushPose();p.translate(0,e.headLift(partial)-Math.min(2,(e.deathTime+partial)/50f),0);p.mulPose(Axis.YP.rotationDegrees(180-yaw));p.mulPose(Axis.XP.rotationDegrees(e.hurtTime>0?e.hurtTime*.7f:0));p.mulPose(Axis.YP.rotationDegrees(e.action()==0?(float)Math.sin((e.tickCount+partial)*.025)*3:0));p.scale(4.667f,4*(1-Math.min(.4f,e.deathTime/250f)),4.5f);head.getChild("jaw").xRot=e.action()==3||e.action()==6?(float)(-.2-.12*Math.sin(e.actionAge(partial)*.12)):e.action()==1&&e.actionAge(partial)>25&&e.actionAge(partial)<60?-.32f:-.03f;head.render(p,b.getBuffer(RenderType.entityCutoutNoCull(PolozContent.id("textures/entity/great_poloz_head.png"))),light,OverlayTexture.NO_OVERLAY);if(e.deathTime<60)for(int side:new int[]{-1,1})head.getChild("eye"+side).render(p,b.getBuffer(RenderType.eyes(PolozContent.id("textures/entity/great_poloz_head.png"))),0xF000F0,OverlayTexture.NO_OVERLAY);p.popPose();
  // Texture switches above can end the previous BufferBuilder. Acquire the body buffer only after the head and eyes are finished.
  var buffer=b.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(e)));
  Vec3 previous=root;for(int i=0;i<24;i++){Vec3 at=at(e.parts[i],alpha);Vec3 dir=previous.subtract(at);p.pushPose();p.translate(at.x-root.x,at.y-root.y,at.z-root.z);p.mulPose(Axis.YP.rotation((float)Math.atan2(-dir.x,-dir.z)));float width=PolozRules.width(i);if(e.action()==4&&i>14){float age=e.actionAge(partial);p.translate(0,age<24?Math.sin(age*Math.PI/48)*1.5:Math.max(0,1.5-(age-24)*.3),0);}p.translate(0,e.deathTime>0?-Math.min(2,e.deathTime/50f):Math.sin((e.tickCount+partial)*.045-i*.3)*.04,0);p.scale(width,(i<20?3:1.4f)*(1-Math.min(.5f,e.deathTime/200f)),i<20?3:2);body.render(p,buffer,light,OverlayTexture.NO_OVERLAY);p.popPose();previous=at;}
  for(int i=24;i<27;i++){var part=e.parts[i];Vec3 at=at(part,alpha),center=at(e.parts[part.segment],alpha);p.pushPose();p.translate(at.x-root.x,at.y-root.y,at.z-root.z);p.mulPose(Axis.YP.rotation((float)Math.atan2(at.x-center.x,at.z-center.z)));if(i==25){p.translate(0,.15,-.45);p.mulPose(Axis.XP.rotationDegrees(90));}p.scale(1.6f,1.6f,1.6f);scar.render(p,buffer,e.deathTime>60?light:0xF000F0,OverlayTexture.NO_OVERLAY);p.popPose();}
  super.render(e,yaw,partial,p,b,light);
 }
 public static ModelPart glob(){var m=new MeshDefinition();m.getRoot().addOrReplaceChild("glob",CubeListBuilder.create().texOffs(0,0).addBox(-3,-3,-3,6,6,6),PartPose.ZERO);return LayerDefinition.create(m,32,32).bakeRoot();}
 public static final class GlobRenderer extends EntityRenderer<PolozGlob>{private final ModelPart glob;public GlobRenderer(EntityRendererProvider.Context c){super(c);glob=glob();}@Override public ResourceLocation getTextureLocation(PolozGlob e){return PolozContent.id("textures/entity/poloz_glob.png");}@Override public void render(PolozGlob e,float y,float t,PoseStack p,MultiBufferSource b,int l){glob.render(p,b.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(e))),0xF000F0,OverlayTexture.NO_OVERLAY);}}
}
