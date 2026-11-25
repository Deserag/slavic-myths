package org.slavicmyths.desert;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
public final class SandKingDamage {
 public static final ResourceKey<DamageType> RAPID=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("slavicmyths","sand_king_rapid"));
 public static DamageSource combo(SandKingEntity e){return new DamageSource(e.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(RAPID),e);}
 private SandKingDamage(){}
}
