package org.slavicmyths.industry.mixin;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.slavicmyths.industry.IndustryAccessories;
/** Adjust only the existing apply_bonus level for ore; never rolls a second loot table or rewrites the tool. */
@Mixin(ApplyBonusCount.class)
public abstract class OreFortuneMixin {
    @Redirect(method="run",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getItemEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/item/ItemStack;)I"))
    private int slavicmyths$fortune(Holder<Enchantment> enchantment,ItemStack tool,ItemStack output,LootContext context){return IndustryAccessories.effectiveFortune(enchantment,tool,context);}
}
