package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.slavicmyths.swamp.BolotnikEntity;
/** Separate broad root-and-bark silhouette, not a recolored water spirit. */
public final class BolotnikRenderer extends MobRenderer<BolotnikEntity,BolotnikRenderer.Model>{
 public BolotnikRenderer(EntityRendererProvider.Context context){super(context,new Model(),.65F);}
 @Override public ResourceLocation getTextureLocation(BolotnikEntity e){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/bolotnik.png");}
 public static final class Model extends EntityModel<BolotnikEntity>{
  private final FolkModelGeometry geometry=new FolkModelGeometry();private final ModelPart body,head,left,right,legLeft,legRight;
  private ModelPart part(ModelPart parent,int u,int v,float x,float y,float z,float a,float b,float c){ModelPart p=geometry.part(256,256,u,v);geometry.box(p,x,y,z,a,b,c);if(parent!=null)geometry.attach(parent,p);return p;}
  public Model(){
   body=part(null,0,40,-7,-15,-5,14,15,10);body.setPos(0,17,0);
   head=part(body,80,0,-5,-22,-6,10,8,10);
   part(head,180,100,-4,-23,-2,3,3,5);part(head,180,100,2,-23,-1,3,4,4);
   left=part(body,20,110,0,-13,-4,5,17,7);left.setPos(7,0,0);
   right=part(body,20,110,-5,-13,-4,5,17,7);right.setPos(-7,0,0);
   legLeft=part(body,110,120,-3,0,-3,6,7,7);legLeft.setPos(4,0,0);
   legRight=part(body,110,120,-3,0,-3,6,7,7);legRight.setPos(-4,0,0);
   for(int side:new int[]{-1,1}){ModelPart roots=part(body,170,140,side<0?-9:6,-10,-4,3,11,4);roots.zRot=side*.25F;}
  }
  @Override public void setupAnim(BolotnikEntity e,float walk,float amount,float age,float yaw,float pitch){head.yRot=yaw*.017453F;head.xRot=pitch*.017453F;body.xRot=.08F;legLeft.xRot=Mth.cos(walk*.55F)*amount*.5F;legRight.xRot=-legLeft.xRot;left.xRot=e.getTarget()!=null?-.45F:legRight.xRot;right.xRot=e.getTarget()!=null?-.45F:legLeft.xRot;}
  @Override public void renderToBuffer(PoseStack pose,VertexConsumer vertices,int light,int overlay,int color){body.render(pose,vertices,light,overlay,color);}
 }
}
