package org.slavicmyths.farming;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/** Applied once to mature produce only, so seeds and the turnip bonus never multiply Fortune. */
public final class CappedFortune extends LootItemConditionalFunction {
    public static final MapCodec<CappedFortune> CODEC = RecordCodecBuilder.mapCodec(i -> commonFields(i).apply(i, CappedFortune::new));
    public CappedFortune(List<LootItemCondition> conditions) { super(conditions); }
    @Override public LootItemFunctionType<CappedFortune> getType() { return Farming.FORTUNE.get(); }
    @Override public Set<LootContextParam<?>> getReferencedContextParams() { return Set.of(LootContextParams.TOOL); }
    @Override protected ItemStack run(ItemStack stack, LootContext context) {
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool != null) {
            var fortune = context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
            int level = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(fortune, tool));
            if (level > 0) stack.grow(context.getRandom().nextInt(level + 1));
        }
        return stack;
    }
}
