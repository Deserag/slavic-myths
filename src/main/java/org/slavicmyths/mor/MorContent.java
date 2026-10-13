package org.slavicmyths.mor;

import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

public final class MorContent {
    public static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES=net.neoforged.neoforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.PARTICLE_TYPE,"slavicmyths");
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>,net.minecraft.core.particles.SimpleParticleType> SPARK=PARTICLES.register("mor_spark",()->new net.minecraft.core.particles.SimpleParticleType(false)),ASH=PARTICLES.register("mor_ash",()->new net.minecraft.core.particles.SimpleParticleType(false));
    public static final DeferredHolder<EntityType<?>,EntityType<MorFlaskProjectile>> FLASK=ModEntities.ENTITIES.register("mor_flask_projectile",()->EntityType.Builder.<MorFlaskProjectile>of(MorFlaskProjectile::new,MobCategory.MISC).sized(.25F,.25F).clientTrackingRange(4).updateInterval(2).build("slavicmyths:mor_flask_projectile"));
    public static final DeferredHolder<EntityType<?>,EntityType<MorBolt>> BOLT=ModEntities.ENTITIES.register("mor_bolt",()->EntityType.Builder.<MorBolt>of(MorBolt::new,MobCategory.MISC).sized(.35F,.35F).clientTrackingRange(4).updateInterval(2).build("slavicmyths:mor_bolt"));
    public static final DeferredHolder<EntityType<?>,EntityType<MorZone>> ZONE=ModEntities.ENTITIES.register("mor_zone",()->EntityType.Builder.<MorZone>of(MorZone::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(8).updateInterval(20).build("slavicmyths:mor_zone"));
    public static void init(net.neoforged.bus.api.IEventBus bus){PARTICLES.register(bus);}
    private MorContent(){}
}
