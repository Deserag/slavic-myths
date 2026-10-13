package org.slavicmyths.client;
import org.slavicmyths.gorynych.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
public final class GorynychRenderer extends MobRenderer<Gorynych,GorynychModel> {
 public GorynychRenderer(EntityRendererProvider.Context context){super(context,new GorynychModel(),2.7f);addLayer(new RenderLayer<Gorynych,GorynychModel>(this){@Override public void render(com.mojang.blaze3d.vertex.PoseStack pose,MultiBufferSource out,int light,Gorynych e,float limb,float amount,float partial,float age,float yaw,float pitch){if(e.state()==GorynychRules.PURIFIED||e.cleanMask()==7&&e.age()>=80)return;getParentModel().renderToBuffer(pose,out.getBuffer(RenderType.eyes(GorynychContent.id("textures/entity/zmey_gorynych_corrupted_glow.png"))),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0xFFFFFFFF);}});}
 @Override public void render(Gorynych e,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack pose,MultiBufferSource buffers,int light){super.render(e,yaw,partial,pose,buffers,light);int age=e.heads().getInt("BodyAge");if(e.bodyAction()!=2||age<32||age>72)return;var out=buffers.getBuffer(RenderType.entitySolid(GorynychContent.id("textures/entity/gorynych_effect.png")));float rise=Math.min(1,(age-31+partial)/5f)*Math.min(1,(72-age-partial)/10f);for(long value:e.heads().getLongArray("Spikes")){var p=net.minecraft.core.BlockPos.of(value);for(int i=0;i<6;i++){double size=1.5-i*.22;GuardianClient.cube(pose,out,p.getX()+.5-e.getX()-size/2,p.getY()-e.getY()+i*.55*rise,p.getZ()+.5-e.getZ()-size/2,size,.65*rise,size,0xFF625B65);}}}
 @Override public net.minecraft.resources.ResourceLocation getTextureLocation(Gorynych e){return GorynychContent.id("textures/entity/zmey_gorynych_"+(e.state()==GorynychRules.PURIFIED||e.state()==GorynychRules.CLEANSING&&e.cleanMask()==7&&e.age()>=80?"purified":e.cleanMask()>0?"clean_"+e.cleanMask():"corrupted")+".png");}
 @Override protected float getFlipDegrees(Gorynych e){return 0;}
 @Override public boolean shouldRender(Gorynych e,net.minecraft.client.renderer.culling.Frustum frustum,double x,double y,double z){return frustum.isVisible(e.getBoundingBox().inflate(18,8,18));}
}
