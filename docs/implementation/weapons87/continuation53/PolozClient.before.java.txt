package org.slavicmyths.client;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.poloz.PolozContent;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class PolozClient {
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(org.slavicmyths.poloz.PolozTrophy.TILE.get(),PolozTrophyRenderer::new);e.registerEntityRenderer(PolozContent.SPIKE.get(),PolozSpikeRenderer::new);e.registerEntityRenderer(PolozContent.BOSS.get(),PolozRenderer::new);e.registerEntityRenderer(PolozContent.GLOB.get(),PolozRenderer.GlobRenderer::new);}
 @SubscribeEvent public static void extensions(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent e){e.registerItem(new PolozArmor(),org.slavicmyths.poloz.PolozRewards.ARMOR.values().stream().map(h->h.get()).toArray(net.minecraft.world.item.Item[]::new));}
 @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e){e.enqueueWork(()->{net.minecraft.client.renderer.item.ItemProperties.register(org.slavicmyths.poloz.PolozRewards.WHIP.get(),PolozContent.id("whip_pose"),(s,w,entity,seed)->entity==null?0:entity.getAttackAnim(0)>0?.3f+entity.getAttackAnim(0)*.6f:entity instanceof net.minecraft.world.entity.player.Player player&&player.getCooldowns().isOnCooldown(s.getItem())?1:.1f);net.minecraft.client.renderer.item.ItemProperties.register(PolozContent.HORN.get(),PolozContent.id("held"),(s,w,entity,seed)->entity==null?0:1);});}
}
