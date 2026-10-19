package org.slavicmyths.client;
import net.minecraft.client.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.morozko.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class FrostClient {
 public static final KeyMapping ICICLES=new KeyMapping("key.slavicmyths.frost_icicles",71,"key.categories.slavicmyths.frost"),WAVE=new KeyMapping("key.slavicmyths.frost_wave",86,"key.categories.slavicmyths.frost"),MIRROR=new KeyMapping("key.slavicmyths.frost_mirror",77,"key.categories.slavicmyths.frost");
 private static CompoundTag view=new CompoundTag();private static long received;private static boolean streamed;private static int heartbeat;
 private static void send(int a){PacketDistributor.sendToServer(new FrostNetwork.Action(a));}
 @SubscribeEvent public static void streamSound(FrostNetwork.StreamReceived e){Minecraft.getInstance().getSoundManager().play(new FrostStreamSound(e.entity));}
 @SubscribeEvent public static void receive(FrostNetwork.Received e){view=e.view.tag().copy();var mc=Minecraft.getInstance();received=mc.level==null?0:mc.level.getGameTime();}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();boolean staff=mc.player!=null&&mc.player.getMainHandItem().is(FrostContent.STAFF.get());while(ICICLES.consumeClick())if(staff&&mc.screen==null)send(1);while(WAVE.consumeClick())if(staff&&mc.screen==null)send(2);while(MIRROR.consumeClick())if(mc.player!=null&&mc.screen==null)send(3);boolean stream=mc.player!=null&&mc.player.isUsingItem()&&mc.player.getUseItem().is(FrostContent.STAFF.get())&&mc.screen==null&&mc.options.keyUse.isDown();if(stream&&++heartbeat%3==0)send(4);if(streamed&&!stream)send(5);streamed=stream;}
 @SubscribeEvent public static void click(InputEvent.InteractionKeyMappingTriggered e){var mc=Minecraft.getInstance();if(mc.player==null||!mc.player.getMainHandItem().is(FrostContent.STAFF.get())||!e.isAttack())return;var hit=mc.hitResult;if(hit!=null&&hit.getType()==net.minecraft.world.phys.HitResult.Type.BLOCK)return;e.setCanceled(true);e.setSwingHand(false);send(0);}
 @SubscribeEvent public static void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent e){if(e.getItemStack().is(FrostContent.STAFF.get()))e.getToolTip().add(FrostContent.text("bindings",ICICLES.getTranslatedKeyMessage(),WAVE.getTranslatedKeyMessage()));}
 @SubscribeEvent public static void hud(RenderGuiEvent.Post e){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.screen!=null||mc.options.hideGui)return;var g=e.getGuiGraphics();int w=g.guiWidth(),h=g.guiHeight(),x=12,y=h-85;double cold=view.getDouble("Cold");long now=view.getLong("Now")+Math.max(0,mc.level.getGameTime()-received);if(cold>0){g.fill(x,y,x+102,y+14,0xAA142F4F);g.fill(x+1,y+11,x+1+(int)cold,y+13,0xFFC0EDFF);g.drawString(mc.font,FrostContent.text("cold",(int)cold),x+3,y+1,0xD4EFFF,false);int tier=FrostRules.tier(cold);if(tier>0){int color=((12+tier*7)<<24)|0xAEE8FF;g.fill(0,0,w,3,color);g.fill(0,0,3,h-60,color);g.fill(w-3,0,w,h-60,color);}}
 if(mc.player.getMainHandItem().is(FrostContent.STAFF.get())){
  long until=view.getLong("StreamUntil"),ready=view.getLong("StreamReady");
  var stream=until>now?FrostContent.text("stream",String.format(java.util.Locale.ROOT,"%.1f",(until-now)/20f)):ready>now?FrostContent.text("stream_cooldown",seconds(ready,now)):FrostContent.text("stream_ready",FrostRules.STREAM_DURATION/20);
  g.drawString(mc.font,stream,x,y-40,0xBCE5FF,false);
  g.drawString(mc.font,FrostContent.text("icicles_status",ICICLES.getTranslatedKeyMessage(),cooldown(view.getLong("IcicleReady"),now)),x,y-27,0xBCE5FF,false);
  g.drawString(mc.font,FrostContent.text("wave_status",WAVE.getTranslatedKeyMessage(),cooldown(view.getLong("WaveReady"),now)),x,y-14,0xBCE5FF,false);
 }
 if(view.getBoolean("MirrorEquipped")){g.renderItem(new ItemStack(FrostContent.MIRROR.get()),x,y-66);g.drawString(mc.font,FrostContent.text("mirror_cd",seconds(view.getLong("MirrorReady"),now)),x+20,y-62,0xBCE5FF,false);}}
 private static net.minecraft.network.chat.Component cooldown(long ready,long now){return ready>now?FrostContent.text("ability_cooldown",seconds(ready,now)):FrostContent.text("ability_ready");}

 private static long seconds(long until,long now){return Math.max(0,(until-now+19)/20);}
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){view=new CompoundTag();received=0;streamed=false;}
 @EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
 public static final class Registration {
  @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(ICICLES);e.register(WAVE);e.register(MIRROR);}
  @SuppressWarnings("unchecked") @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(FrostContent.BOSS.get(),MorozkoRenderer::new);e.registerEntityRenderer(FrostContent.DOUBLE.get(),c->(EntityRenderer<IceDouble>)(Object)new MorozkoRenderer(c));e.registerEntityRenderer(FrostContent.MIRROR_ENTITY.get(),PlayerDoubleRenderer::new);e.registerEntityRenderer(FrostContent.SHARD.get(),FrostGeometryRenderer::new);e.registerEntityRenderer(FrostContent.HAZARD.get(),FrostGeometryRenderer::new);}
  @SubscribeEvent public static void models(ModelEvent.RegisterAdditional e){for(String name:new String[]{"morozko_staff","heart_of_eternal_cold"})for(String suffix:new String[]{"_icon","_held"})e.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(FrostContent.id("item/"+name+suffix)));}
  @SubscribeEvent public static void extension(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent e){e.registerItem(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions(){private final Quest185ItemRenderer renderer=new Quest185ItemRenderer();@Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){return renderer;}},FrostContent.STAFF.get(),FrostContent.HEART.get());}
 }
 private FrostClient(){}
}
