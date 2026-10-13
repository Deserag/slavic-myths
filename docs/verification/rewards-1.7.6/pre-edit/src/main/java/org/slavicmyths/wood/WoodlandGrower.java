package org.slavicmyths.wood;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.grower.TreeGrower;
/** The target grower resolves the existing custom tree geometry through a sapling configuration. */
public final class WoodlandGrower {
 public static TreeGrower create(String species){return new TreeGrower("slavicmyths:"+species,
  species.equals("pine")?Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE,ResourceLocation.fromNamespaceAndPath("slavicmyths","giant_pine_sapling"))):Optional.empty(),Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE,
   ResourceLocation.fromNamespaceAndPath("slavicmyths",species+"_sapling"))),Optional.empty());}
 private WoodlandGrower(){}
}
