package org.slavicmyths.client;
import net.minecraft.client.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.wolf.WolfStoryNetwork;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class WolfStoryClient {
 public static final KeyMapping RING=new KeyMapping("key.slavicmyths.grey_pack",86,"key.categories.slavicmyths");
 @SubscribeEvent public static void view(WolfStoryNetwork.Viewed e){var mc=Minecraft.getInstance();if(e.view.data().isEmpty()){if(mc.screen instanceof WolfStoryScreen)mc.setScreen(null);return;}if(e.view.open())mc.setScreen(new WolfStoryScreen(e.view.data()));else if(mc.screen instanceof WolfStoryScreen s)s.update(e.view.data());}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){var mc=Minecraft.getInstance();while(RING.consumeClick())if(mc.player!=null&&mc.screen==null){if(ClassClient.conflict(RING)){mc.player.displayClientMessage(org.slavicmyths.military.GreyWolfStory.text("key_conflict"),true);continue;}net.neoforged.neoforge.network.PacketDistributor.sendToServer(new WolfStoryNetwork.Ring());}}
 @SubscribeEvent public static void mark(RenderLevelStageEvent e){
  if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES)return;
  var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.options.hideGui||!mc.player.getMainHandItem().is(org.slavicmyths.finist.FinistItems.BOW.get())&&!mc.player.getOffhandItem().is(org.slavicmyths.finist.FinistItems.BOW.get()))return;
  var d=RpgClient.data.getCompound("FinistMark");if(!d.hasUUID("Target")||!d.getString("Dimension").equals(mc.level.dimension().location().toString())||d.getLong("Until")<=mc.level.getGameTime())return;
  var target=mc.level.getEntity(d.getInt("Entity"));if(target==null||!target.getUUID().equals(d.getUUID("Target"))||!target.isAlive()||!mc.player.hasLineOfSight(target)||mc.player.distanceToSqr(target)>48*48)return;
  var at=target.getPosition(e.getPartialTick().getGameTimeDeltaPartialTick(false)).add(0,target.getBbHeight()+.35,0).subtract(e.getCamera().getPosition());
  var pose=e.getPoseStack();pose.pushPose();pose.translate(at.x,at.y,at.z);pose.mulPose(e.getCamera().rotation());pose.scale(.4F,.4F,.4F);
  var type=net.minecraft.client.renderer.RenderType.entityCutoutNoCull(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/item/finist_target_marker.png"));var buffers=mc.renderBuffers().bufferSource();var vertices=buffers.getBuffer(type);var matrix=pose.last();
  for(float[] v:new float[][]{{-.5F,-.5F,0,1},{.5F,-.5F,1,1},{.5F,.5F,1,0},{-.5F,.5F,0,0}})vertices.addVertex(matrix,v[0],v[1],0).setColor(255,255,255,255).setUv(v[2],v[3]).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(matrix,0,0,1);
  pose.popPose();buffers.endBatch(type);
 }
 private WolfStoryClient(){}
}
