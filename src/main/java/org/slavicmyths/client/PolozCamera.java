package org.slavicmyths.client;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.poloz.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class PolozCamera {
 private static java.lang.ref.WeakReference<GreatPoloz> nearby=new java.lang.ref.WeakReference<>(null);
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){var m=net.minecraft.client.Minecraft.getInstance();if(m.player==null||m.level==null){nearby.clear();return;}if(m.player.tickCount%20!=0)return;nearby=new java.lang.ref.WeakReference<>(m.level.getEntitiesOfClass(GreatPoloz.class,m.player.getBoundingBox().inflate(64),p->p.isAlive()&&p.nearestDistance(m.player.position())<24*24).stream().findFirst().orElse(null));}
 @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){var m=net.minecraft.client.Minecraft.getInstance();var p=nearby.get();if(m.player==null||m.screen!=null||p==null||!p.isAlive()||!PolozVisualConfig.SHAKE.get()||m.options.screenEffectScale().get()<=0)return;float age=p.actionAge((float)e.getPartialTick());float impact=p.action()==4?24:p.action()==2?100:-100;float elapsed=age-impact;if(elapsed<0||elapsed>8)return;float strength=(float)(.6*(1-elapsed/8)*m.options.screenEffectScale().get());e.setPitch(e.getPitch()+(float)Math.sin(elapsed*2.2)*strength);e.setRoll(e.getRoll()+(float)Math.cos(elapsed*2.2)*strength*.5f);}
}
