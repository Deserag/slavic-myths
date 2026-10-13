package org.slavicmyths.client;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.slavicmyths.earth.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class EarthCamera {
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!EarthVisualConfig.SHAKE.get())return;double amount=0,time=mc.level.getGameTime()+e.getPartialTick();for(var actor:mc.level.getEntitiesOfClass(EarthActor.class,mc.player.getBoundingBox().inflate(16))){if(actor.kind()==EarthActor.TREMOR&&actor.age(0)<9)amount=Math.max(amount,.4*(1-actor.age(0)/9.0));else if(actor.kind()==EarthActor.DEBRIS&&actor.age(0)<6)amount=Math.max(amount,.24*(1-actor.age(0)/6.0));}for(var boss:mc.level.getEntitiesOfClass(EarthSpirit.class,mc.player.getBoundingBox().inflate(16))){float age=boss.age(0);float start=boss.action()==EarthRules.Action.AWAKEN?80:22;if((boss.action()==EarthRules.Action.PHASE||boss.action()==EarthRules.Action.AWAKEN)&&age>=start&&age<start+8)amount=Math.max(amount,.1*(1-(age-start)/8));}var balance=mc.player.getEffect(EarthContent.IMBALANCE);if(balance!=null)e.setRoll(e.getRoll()+(float)(Math.sin((13-balance.getDuration()+e.getPartialTick())*Math.PI/13)*2));if(amount>0){e.setPitch(e.getPitch()+(float)(Math.sin(time*2.7)*amount));e.setRoll(e.getRoll()+(float)(Math.sin(time*3.1)*amount*.4));}}
    private EarthCamera(){}
}
