package org.slavicmyths.polozaudit.mixin;
import org.slavicmyths.polozaudit.PressureObserver;
import net.minecraft.client.particle.*;import net.minecraft.core.particles.*;import net.minecraft.core.registries.BuiltInRegistries;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=ParticleEngine.class,remap=false)
public abstract class ParticleMixin {
 @Inject(method="createParticle",at=@At("HEAD"))private void begin(ParticleOptions options,double x,double y,double z,double vx,double vy,double vz,CallbackInfoReturnable<Particle> ci){PressureObserver.type=BuiltInRegistries.PARTICLE_TYPE.getKey(options.getType()).toString();}
 @Inject(method="createParticle",at=@At("RETURN"))private void end(ParticleOptions options,double x,double y,double z,double vx,double vy,double vz,CallbackInfoReturnable<Particle> ci){PressureObserver.type=null;}
 @Inject(method="add",at=@At("HEAD"))private void added(Particle p,CallbackInfo ci){PressureObserver.born(p);}
}
