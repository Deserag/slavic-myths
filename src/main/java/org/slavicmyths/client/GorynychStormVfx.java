package org.slavicmyths.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slavicmyths.gorynych.StormVfx;

@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class GorynychStormVfx {
    private record Emitter(net.minecraft.client.multiplayer.ClientLevel level,net.minecraft.world.phys.Vec3 at,long born){}
    private static final java.util.ArrayList<Emitter> EMITTERS=new java.util.ArrayList<>();
    @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();if(mc.level==null){EMITTERS.clear();return;}if(mc.isPaused())return;
        long now=mc.level.getGameTime();
        EMITTERS.removeIf(e->e.level!=mc.level||now-e.born>=40||now<e.born);
        for(var e:EMITTERS){int age=(int)(now-e.born);if(age%4!=0)continue;
            for(int i=0;i<24;i++){double angle=i*Math.PI*2/24+age*.06;
                var p=e.at.add(Math.cos(angle)*3,.25,Math.sin(angle)*3);
                var particle=mc.particleEngine.createParticle(ParticleTypes.ELECTRIC_SPARK,p.x,p.y,p.z,0,.015,0);
                if(particle!=null)particle.setLifetime(Math.min(8,40-age));
            }
        }
    }
    @SubscribeEvent public static void logout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event){EMITTERS.clear();}
    @SubscribeEvent public static void received(StormVfx.Received event){
        var mc=Minecraft.getInstance();if(mc.level==null)return;var at=event.impact.at();
        if(!Double.isFinite(at.x)||!Double.isFinite(at.y)||!Double.isFinite(at.z))return;
        EMITTERS.removeIf(e->e.at.distanceToSqr(at)<.5625);
        if(EMITTERS.size()>=32)EMITTERS.removeFirst();
        EMITTERS.add(new Emitter(mc.level,at,mc.level.getGameTime()));
        for(var end:event.impact.ends())for(int i=0;i<=10;i++){
            var p=at.lerp(end,i/10.0).add((mc.level.random.nextDouble()-.5)*.08,(mc.level.random.nextDouble()-.5)*.08,(mc.level.random.nextDouble()-.5)*.08);
            var particle=mc.particleEngine.createParticle(ParticleTypes.ELECTRIC_SPARK,p.x,p.y,p.z,0,.005,0);if(particle!=null)particle.setLifetime(12);
        }
        for(int i=0;i<12;i++){var p=at.add((mc.level.random.nextDouble()-.5)*.8,mc.level.random.nextDouble()*.5,(mc.level.random.nextDouble()-.5)*.8);var particle=mc.particleEngine.createParticle(ParticleTypes.ELECTRIC_SPARK,p.x,p.y,p.z,0,.03,0);if(particle!=null)particle.setLifetime(12);}
    }
    private GorynychStormVfx(){}
}
