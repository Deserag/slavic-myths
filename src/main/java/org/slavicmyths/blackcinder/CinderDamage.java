package org.slavicmyths.blackcinder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.Entity;
public final class CinderDamage {
 public static final ResourceKey<DamageType> HEAT=ResourceKey.create(Registries.DAMAGE_TYPE,CinderWorldgen.id("cinder_heat"));
 public static DamageSource heat(Entity direct,Entity owner){return new DamageSource(direct.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(HEAT),direct,owner);}
 private CinderDamage(){}
}
