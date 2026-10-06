package org.slavicmyths.combat;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Registry-backed versions of existing named damage sources. */
public final class MythDamageSources {
    private static Holder<DamageType> type(Entity entity,String name){
        return entity.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(
            ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("slavicmyths",name)));
    }
    public static DamageSource caused(String name,Entity cause){return new DamageSource(type(cause,name),cause);}
    /** Periodic damage retains kill credit without masquerading as a new weapon hit. */
    public static DamageSource periodic(String name,Entity cause){return new DamageSource(type(cause,name),null,cause);}
    public static DamageSource unattributed(String name,Entity context){return new DamageSource(type(context,name));}
    // The original shielded slam explicitly had no source position.
    public static DamageSource slam(Entity cause){return new DamageSource(type(cause,"ovinnik_slam"),cause){
        @Override public Vec3 getSourcePosition(){return null;}
    };}
    public static DamageSource ember(Entity projectile,net.minecraft.world.entity.LivingEntity owner){return new DamageSource(type(projectile,"ember_projectile"),projectile,owner);}
    private MythDamageSources(){}
}
