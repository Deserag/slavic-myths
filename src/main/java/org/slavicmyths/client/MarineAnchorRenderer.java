package org.slavicmyths.client;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.slavicmyths.marine.MarineAnchorVisuals;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class MarineAnchorRenderer {
 record Animation(MarineAnchorVisuals.Flight value,long started){}
 static final Map<Integer,Animation> ACTIVE=new HashMap<>();
 static final MarineWeaponModels MODELS=new MarineWeaponModels();
 static final ModelPart LINK=link();
 public static boolean localFlight(){var player=Minecraft.getInstance().player;return player!=null&&ACTIVE.containsKey(player.getId());}
 static ModelPart link(){var g=new FolkModelGeometry();var p=g.part(16,16);g.box(p,-1,-1.5F,-.35F,2,.5F,.7F);g.box(p,-1,1,-.35F,2,.5F,.7F);g.box(p,-1,-1,-.35F,.5F,2,.7F);g.box(p,.5F,-1,-.35F,.5F,2,.7F);return p;}
 @SubscribeEvent public static void flight(MarineAnchorVisuals.Received event){var mc=Minecraft.getInstance();if(mc.level!=null&&mc.level.dimension().location().equals(event.value.dimension()))ACTIVE.put(event.value.owner(),new Animation(event.value,mc.level.getGameTime()));}
 @SubscribeEvent public static void render(RenderLevelStageEvent event){
  if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES)return;var mc=Minecraft.getInstance();if(mc.level==null){ACTIVE.clear();return;}
  var camera=event.getCamera().getPosition();var pose=event.getPoseStack();var out=mc.renderBuffers().bufferSource();var iterator=ACTIVE.values().iterator();
  while(iterator.hasNext()){
   var animation=iterator.next();var owner=mc.level.getEntity(animation.value.owner());double elapsed=mc.level.getGameTime()-animation.started;
   if(owner==null||elapsed>14){iterator.remove();continue;}
   Vec3 origin=owner.getEyePosition().add(0,-.45,0).add(owner.getLookAngle().cross(new Vec3(0,1,0)).normalize().scale(.35));double phase=elapsed<=7?elapsed/7:(14-elapsed)/7;Vec3 head=origin.lerp(animation.value.end(),Math.clamp(phase,0,1));
   pose.pushPose();pose.translate(head.x-camera.x,head.y-camera.y,head.z-camera.z);pose.scale(.4F,-.4F,.4F);MODELS.render("anchor",pose,out,0xF000F0,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);pose.popPose();
   Vec3 end=head.add(0,.5,0),delta=end.subtract(origin);int count=Math.min(100,(int)(delta.length()/.12));if(count<1)continue;
   var rotation=new org.joml.Quaternionf().rotationTo(new org.joml.Vector3f(0,1,0),new org.joml.Vector3f((float)delta.x,(float)delta.y,(float)delta.z).normalize());
   for(int i=0;i<=count;i++){Vec3 point=origin.lerp(end,i/(double)count);pose.pushPose();pose.translate(point.x-camera.x,point.y-camera.y,point.z-camera.z);pose.mulPose(rotation);if(i%2==1)pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));LINK.render(pose,out.getBuffer(RenderType.entityCutoutNoCull(MarineWeaponModels.WHITE)),0xF000F0,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0xff43545e);pose.popPose();}
  }
 }
}