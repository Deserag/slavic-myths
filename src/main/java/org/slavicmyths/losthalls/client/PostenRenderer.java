package org.slavicmyths.losthalls.client;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.losthalls.PostenMob;
public final class PostenRenderer extends MobRenderer<PostenMob,PostenRenderer.Model> {
 public static final class Model extends HierarchicalModel<PostenMob>{public final HallsMesh mesh=new HallsMesh("posten");@Override public ModelPart root(){return mesh.root;}@Override public void setupAnim(PostenMob e,float walk,float amount,float age,float yaw,float pitch){float t=e.castTime(age-e.tickCount),blend=Math.min(1,t/12);blend=blend*blend*(3-2*blend);float mask=e.controller()&&e.cast()>0&&e.cast()<5?e.mask()-1+blend:e.mask();mesh.pose(walk,amount,age,e.cast()==0?0:t,mask,e.hurtTime,e.cast()==6?Math.min(80,(int)e.castTime(age-e.tickCount)):0);mesh.parts.get("head").yRot+=yaw*.0174533F;if(e.cast()==5)mesh.root.yScale=Math.max(.05F,(float)Math.abs(Math.cos(e.castTime(age-e.tickCount)*Math.PI/32)));}}
 public PostenRenderer(EntityRendererProvider.Context c){super(c,new Model(),.7F);}
 @Override public ResourceLocation getTextureLocation(PostenMob e){return HallsClient.texture("posten");}
 @Override protected float getFlipDegrees(PostenMob e){return 0;}
}
