package org.slavicmyths.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.slavicmyths.registry.ModItems;

/** Conditions remain in JSON; the existing drops are preserved. */
public final class FernFlowerModifier extends LootModifier {
    public static final MapCodec<FernFlowerModifier> CODEC = RecordCodecBuilder.mapCodec(
            instance -> codecStart(instance).apply(instance, FernFlowerModifier::new));
    public FernFlowerModifier(LootItemCondition[] conditions) { super(conditions); }
    @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        loot.add(new ItemStack(ModItems.FERN_FLOWER.get()));
        return loot;
    }
    @Override public MapCodec<? extends IGlobalLootModifier> codec() { return CODEC; }
}
