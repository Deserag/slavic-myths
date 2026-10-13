package org.slavicmyths.client;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.*;
import com.mojang.blaze3d.vertex.*;
import org.slavicmyths.guardians.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class GuardianClient {
 private static final net.minecraft.resources.ResourceLocation WATER=GuardianContent.id("textures/entity/guardian_water_surface.png");
 @EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
 public static final class Registration {
  @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(GuardianContent.FIRE.get(),c->new GuardianRenderer<FireGuardian>(c,false));e.registerEntityRenderer(GuardianContent.WATER.get(),c->new GuardianRenderer<SpringKeeper>(c,true));e.registerEntityRenderer(GuardianContent.PRISON.get(),PrisonRenderer::new);e.registerEntityRenderer(GuardianContent.ECHO.get(),EchoRenderer::new);}
 }
 public static void cube(PoseStack pose,VertexConsumer out,double x,double y,double z,double w,double h,double d,int color){var m=pose.last();float x0=(float)x,y0=(float)y,z0=(float)z,x1=(float)(x+w),y1=(float)(y+h),z1=(float)(z+d);float[][][] faces={{{x0,y0,z0},{x0,y1,z0},{x1,y1,z0},{x1,y0,z0}},{{x1,y0,z1},{x1,y1,z1},{x0,y1,z1},{x0,y0,z1}},{{x0,y0,z1},{x0,y1,z1},{x0,y1,z0},{x0,y0,z0}},{{x1,y0,z0},{x1,y1,z0},{x1,y1,z1},{x1,y0,z1}},{{x0,y1,z0},{x0,y1,z1},{x1,y1,z1},{x1,y1,z0}},{{x0,y0,z1},{x0,y0,z0},{x1,y0,z0},{x1,y0,z1}}};float[][] normals={{0,0,-1},{0,0,1},{-1,0,0},{1,0,0},{0,1,0},{0,-1,0}};for(int f=0;f<6;f++)for(int i=0;i<4;i++){var v=faces[f][i];out.addVertex(m,v[0],v[1],v[2]).setColor(color).setUv(i==0||i==1?0:1,i==0||i==3?1:0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(m,normals[f][0],normals[f][1],normals[f][2]);}}
 public static final class PrisonRenderer extends EntityRenderer<WaterPrison> {
  public PrisonRenderer(EntityRendererProvider.Context c){super(c);}
  @Override public net.minecraft.resources.ResourceLocation getTextureLocation(WaterPrison p){return WATER;}
  @Override public void render(WaterPrison e,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){var out=buffers.getBuffer(RenderType.entityTranslucent(WATER));int color=e.hits()==0?0xAA72D9F2:e.hits()==1?0x8872D9F2:0x6672D9F2;for(int i=0;i<4;i++){double x=i%2==0?-1:.8,z=i<2?-1:.8;cube(p,out,x,0,z,.2,2.7,.2,color);}for(int y=0;y<3;y++){cube(p,out,-1,y*.9,-1,2,.14,.14,color);cube(p,out,-1,y*.9,.86,2,.14,.14,color);cube(p,out,-1,y*.9,-1,.14,.14,2,color);cube(p,out,.86,y*.9,-1,.14,.14,2,color);}super.render(e,yaw,partial,p,buffers,light);}
 }
 public static final class EchoRenderer extends EntityRenderer<WaterEcho> {
  public EchoRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.25f;}
  @Override public net.minecraft.resources.ResourceLocation getTextureLocation(WaterEcho e){return WATER;}
  @Override public void render(WaterEcho e,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){var out=buffers.getBuffer(RenderType.entityTranslucent(WATER));cube(p,out,-.3,.6,-.25,.6,.5,.5,0x9944BBDB);cube(p,out,-.22,1.1,-.2,.44,.35,.4,0xBBA7E8EC);for(int i=0;i<3;i++)cube(p,out,-.3+i*.2,.05+Math.sin((e.tickCount+partial)*.12+i)*.04,-.2,.15,.65,.4,0x8839B6DC);cube(p,out,-.21,1.28,-.23,.08,.05,.04,0xFFFFFFFF);cube(p,out,.13,1.28,-.23,.08,.05,.04,0xFFFFFFFF);super.render(e,yaw,partial,p,buffers,light);}
 }
 @SubscribeEvent public static void scene(RenderLevelStageEvent event){if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES)return;var mc=Minecraft.getInstance();if(mc.level==null)return;var camera=event.getCamera().getPosition();var pose=event.getPoseStack();var buffers=mc.renderBuffers().bufferSource();for(var actor:mc.level.entitiesForRendering())if(actor instanceof GuardianEntity e&&e.distanceToSqr(camera)<80*80){if(e instanceof FireGuardian fire&&e.state()==GuardianRules.LOCKDOWN){pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);var out=buffers.getBuffer(RenderType.entityTranslucent(WATER));int warning=GuardianRules.lockWarning(fire.lastEmbers()),length=warning+GuardianRules.LOCK_ACTIVE,wave=Math.min(2,Math.max(0,e.age()-1)/length),local=Math.max(0,e.age()-1)%length;var b=e.arena();for(int i=0;i<3;i++){double x=b.minX+i*b.getXsize()/3;int color=i==GuardianRules.safeStrip(e.pattern(),wave)?0x443DB981:local<warning?0x66661E29:0x889D3522;cube(pose,out,x,b.minY+.025,b.minZ,b.getXsize()/3,.03,b.getZsize(),color);}for(int i=0;i<12;i++){double a=i*Math.PI/6;cube(pose,out,e.getX()+Math.cos(a)*1.5-.1,e.getY()+.3,e.getZ()+Math.sin(a)*1.5-.1,.2,1.5,.2,0xAAFA6732);}pose.popPose();}
   if(e instanceof SpringKeeper keeper&&e.state()==GuardianRules.FLOOD){pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);var out=buffers.getBuffer(RenderType.entityTranslucent(WATER));var areas=keeper.floodRegions();for(int i=0;i<areas.length;i++){var b=areas[i];int age=e.age()-GuardianRules.floodDelay(i);double height=age<GuardianRules.FLOOD_WARNING?.04:Math.min(.8,(age-GuardianRules.FLOOD_WARNING)/25.);height=Math.min(height,Math.max(.04,(GuardianRules.FLOOD_WARNING+GuardianRules.FLOOD_ACTIVE-e.age())/30.));cube(pose,out,b.minX,b.minY,b.minZ,b.getXsize(),height,b.getZsize(),age<GuardianRules.FLOOD_WARNING?0x4439B6DC:0x7739B6DC);}pose.popPose();}
   if(e instanceof SpringKeeper keeper&&e.state()==GuardianRules.WHIRL){pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);var out=buffers.getBuffer(RenderType.entityTranslucent(WATER));var center=keeper.whirlCenter();for(int i=0;i<20;i++){double a=i*Math.PI/10+e.age()*.06;double x=center.x+Math.cos(a)*2.5,z=center.z+Math.sin(a)*2.5;if(e.arena().contains(new Vec3(x,center.y+.1,z)))cube(pose,out,x-.1,center.y+.03,z-.1,.2,e.age()<GuardianRules.WHIRL_WARNING?.08:.3,.2,0x8839B6DC);}pose.popPose();}}

 }
 private GuardianClient(){}
}
