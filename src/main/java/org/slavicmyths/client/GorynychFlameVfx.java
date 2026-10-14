package org.slavicmyths.client;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slavicmyths.gorynych.FlameVfx;

/** Bounded local-only emitters; all particles expire at the impact's 40-tick deadline. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class GorynychFlameVfx {
    private record Emitter(Vec3 at,float radius,long born,net.minecraft.client.multiplayer.ClientLevel level){}
    private static final ArrayList<Emitter> EMITTERS=new ArrayList<>();
    @SubscribeEvent public static void received(FlameVfx.Received event){
        var mc=Minecraft.getInstance();var p=event.impact;
        if(mc.level==null||!Double.isFinite(p.at().x)||!Double.isFinite(p.at().y)||!Double.isFinite(p.at().z)||!Float.isFinite(p.radius()))return;
        EMITTERS.removeIf(e->e.at.distanceToSqr(p.at())<.5625);
        if(EMITTERS.size()>=FlameVfx.MAX_EMITTERS)EMITTERS.removeFirst();
        EMITTERS.add(new Emitter(p.at(),Math.min(3,Math.max(.5f,p.radius())),mc.level.getGameTime(),mc.level));
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();if(mc.level==null){EMITTERS.clear();return;}if(mc.isPaused())return;
        long now=mc.level.getGameTime();
        EMITTERS.removeIf(e->e.level!=mc.level||now-e.born>=FlameVfx.DURATION||now<e.born);
        for(var e:EMITTERS){int age=(int)(now-e.born);if(age%4!=0)continue;
            int count=age<24?6:3;
            for(int i=0;i<count;i++){
                double a=(i+mc.level.random.nextDouble())*Math.PI*2/count;
                double r=e.radius*.35*(.35+mc.level.random.nextDouble()*.65);
                double x=e.at.x+Math.cos(a)*r,y=e.at.y+.15+mc.level.random.nextDouble()*.25,z=e.at.z+Math.sin(a)*r;
                boolean water=mc.level.getFluidState(BlockPos.containing(x,y,z)).is(FluidTags.WATER);
                var particle=mc.particleEngine.createParticle(water?ParticleTypes.CLOUD:ParticleTypes.FLAME,x,y,z,0,.025,0);
                if(particle!=null)particle.setLifetime(Math.min(water?8:12,FlameVfx.DURATION-age));
            }
        }
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){EMITTERS.clear();}
    private GorynychFlameVfx(){}
}
