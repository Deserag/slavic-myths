package org.slavicmyths.flight;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
/** Data-driven definition keeps the saved enchantment ID and original flight-level policy. */
public final class Tailwind {
 public static final ResourceKey<Enchantment> TAILWIND=ResourceKey.create(Registries.ENCHANTMENT,ResourceLocation.fromNamespaceAndPath("slavicmyths","tailwind"));
 public static int level(HolderLookup.Provider registries,ItemStack stack) {
  return EnchantmentHelper.getItemEnchantmentLevel(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(TAILWIND),stack);
 }
 private Tailwind() { }
}
