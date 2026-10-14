package org.slavicmyths.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slavicmyths.gorynych.StormVfx;

@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class GorynychStormVfx {
    @SubscribeEvent public static void received(StormVfx.Received event){
        var mc=Minecraft.getInstance();if(mc.level==null)return;var at=event.impact.at();
        for(var end:event.impact.ends())for(int i=0;i<=10;i++){
            var p=at.lerp(end,i/10.0).add((mc.level.random.nextDouble()-.5)*.08,(mc.level.random.nextDouble()-.5)*.08,(mc.level.random.nextDouble()-.5)*.08);
            var particle=mc.particleEngine.createParticle(ParticleTypes.ELECTRIC_SPARK,p.x,p.y,p.z,0,.005,0);if(particle!=null)particle.setLifetime(12);
        }
        for(int i=0;i<12;i++){var p=at.add((mc.level.random.nextDouble()-.5)*.8,mc.level.random.nextDouble()*.5,(mc.level.random.nextDouble()-.5)*.8);var particle=mc.particleEngine.createParticle(ParticleTypes.ELECTRIC_SPARK,p.x,p.y,p.z,0,.03,0);if(particle!=null)particle.setLifetime(12);}
    }
    private GorynychStormVfx(){}
}
