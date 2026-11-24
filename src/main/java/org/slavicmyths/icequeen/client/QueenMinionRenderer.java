package org.slavicmyths.icequeen.client;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.slavicmyths.client.FolkModelGeometry;
import org.slavicmyths.auka.client.AukaAnimalModel;
import org.slavicmyths.auka.AukaBoss;
import org.slavicmyths.owl.client.WinterOwlModel;
import org.slavicmyths.wolverine.client.WolverineModel;
import org.slavicmyths.icequeen.*;
public final class QueenMinionRenderer extends MobRenderer<QueenMinion,EntityModel<QueenMinion>>{
 private final QueenContent.Kind kind;
 public QueenMinionRenderer(EntityRendererProvider.Context c,QueenContent.Kind k){super(c,k.copy()?new QueenModel<QueenMinion>():new ServantModel(k),.45F);kind=k;}
 @Override public ResourceLocation getTextureLocation(QueenMinion e){return switch(kind){case WOLF,BEAR->ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/auka.png");case OWL->ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/winter_owl.png");case WOLVERINE->ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/northern_wolverine.png");case GOLEM,LARGE_GOLEM->ResourceLocation.withDefaultNamespace("textures/block/packed_ice.png");default->QueenRenderer.WHITE;};}
 @Override protected void scale(QueenMinion e,PoseStack p,float partial){float s=switch(kind){case WOLF->.29F;case BEAR->.50F;case OWL->.19F;case WOLVERINE->.25F;case MELEE_COPY,RANGED_COPY->1.12F;case LARGE_GOLEM->1.5F;case GOLEM->.83F;default->1;};p.scale(s,s,s);}
 private static final class ServantModel extends EntityModel<QueenMinion>{
  final QueenContent.Kind kind;final AukaAnimalModel<QueenMinion> animal;final WinterOwlModel owl;final WolverineModel wolverine;
  final FolkModelGeometry geo=new FolkModelGeometry();final ModelPart root=geo.part(128,128),left=geo.part(128,128),right=geo.part(128,128),legL=geo.part(128,128),legR=geo.part(128,128);
  ServantModel(QueenContent.Kind k){kind=k;animal=k==QueenContent.Kind.WOLF||k==QueenContent.Kind.BEAR?new AukaAnimalModel<>(k==QueenContent.Kind.BEAR):null;owl=k==QueenContent.Kind.OWL?new WinterOwlModel():null;wolverine=k==QueenContent.Kind.WOLVERINE?new WolverineModel():null;
   if(animal==null&&owl==null&&wolverine==null){
    root.setPos(0,24,0);
    geo.box(root,-6,-18,-4,12,9,8);
    geo.box(root,-5,-20,-3.5F,10,3,7);
    geo.box(root,-4,-10,-3,8,3,6);
    geo.box(root,-3.5F,-26,-3.5F,7,7,7);
    geo.box(root,-2.5F,-27,-2.5F,5,2,5);
    geo.box(root,-3.5F,-24,-4,7,1.5F,.8F);
    geo.box(root,-4,-18,-4.6F,8,4,.8F);
    geo.box(root,-2,-15,-5,4,4,1.2F);
    geo.box(root,-4,-18,4,2,6,3);
    geo.box(root,2,-19,4,2,7,3);
    for(int side:new int[]{-1,1}){
      ModelPart arm=side==1?left:right,leg=side==1?legL:legR;
      arm.setPos(side*6,-19,0);geo.attach(root,arm);
      geo.box(arm,side==1?0:-5,-1,-3.5F,5,5,7);
      geo.box(arm,side==1?1:-4,-4,-2.5F,3,4,5);
      geo.box(arm,side==1?.5F:-4.5F,4,-2.5F,4,5,5);
      geo.box(arm,side==1?0:-5,8,-3,5,5,6);
      geo.box(arm,side==1?.5F:-4.5F,12,-2.5F,4,2,5);
      leg.setPos(side*3,-8,0);geo.attach(root,leg);
      geo.box(leg,-2.5F,0,-2.5F,5,6,5);
      geo.box(leg,-2.5F,5,-3.5F,5,3,6);
      geo.box(leg,-2,2,-3,4,3,1);
    }
   }
  }
  @Override public void setupAnim(QueenMinion e,float walk,float amount,float age,float yaw,float pitch){
   if(animal!=null)animal.pose(walk,amount,age,yaw,pitch,e.attackFrame()>0?AukaBoss.Move.WOLF_BITE:AukaBoss.Move.IDLE,e.attackFrame(),true);
   if(owl!=null)owl.poseQueenMinion(age,yaw,pitch);
   if(wolverine!=null)wolverine.poseQueenMinion(walk,amount,age,yaw,pitch,e.attackFrame());
   left.xRot=Mth.cos(walk*.65F)*amount*.55F;right.xRot=-left.xRot;legL.xRot=-left.xRot;legR.xRot=left.xRot;if(e.attackFrame()>0)right.xRot=-1.4F*Math.max(0,Math.min(1,e.attackFrame()/18F))*Math.max(0,Math.min(1,(35-e.attackFrame())/12F));
  }
  @Override public void renderToBuffer(PoseStack p,VertexConsumer out,int light,int overlay,int color){int tint=0xff81cfff;if(animal!=null)animal.renderToBuffer(p,out,light,overlay,tint);else if(owl!=null)owl.renderToBuffer(p,out,light,overlay,tint);else if(wolverine!=null)wolverine.renderToBuffer(p,out,light,overlay,tint);else root.render(p,out,light,overlay,0xffd4f3ff);}
 }
}
