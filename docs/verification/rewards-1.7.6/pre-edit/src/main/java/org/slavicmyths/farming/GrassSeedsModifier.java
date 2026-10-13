package org.slavicmyths.farming;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/** Probability and table selection live in JSON; append exactly one uniformly chosen seed. */
public final class GrassSeedsModifier extends LootModifier {
    public static final MapCodec<GrassSeedsModifier> CODEC = RecordCodecBuilder.mapCodec(i -> codecStart(i).apply(i, GrassSeedsModifier::new));
    public GrassSeedsModifier(LootItemCondition[] conditions) { super(conditions); }
    @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if(context.getRandom().nextInt(3)==0)loot.add(new ItemStack(org.slavicmyths.brewing.Brewing.item("hops_cutting")));
        var seeds = Farming.seeds(); loot.add(new ItemStack(seeds.get(context.getRandom().nextInt(seeds.size())))); return loot;
    }
    @Override public MapCodec<? extends IGlobalLootModifier> codec() { return CODEC; }
}
