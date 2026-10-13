package org.slavicmyths.morozko;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
public final class FrostDamage {
 public static DamageSource projectile(String name,Entity projectile,LivingEntity owner){return new DamageSource(projectile.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,FrostContent.id(name))),projectile,owner);}
 private FrostDamage(){}
}
