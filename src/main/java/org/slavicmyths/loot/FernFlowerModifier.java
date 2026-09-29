package org.slavicmyths.loot;

import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import org.slavicmyths.registry.ModItems;

/** Conditions live in JSON; original vanilla drops are preserved. */
public final class FernFlowerModifier extends LootModifier {
    public FernFlowerModifier(ILootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.add(new ItemStack(ModItems.FERN_FLOWER.get()));
        return generatedLoot;
    }

    public static final class Serializer extends GlobalLootModifierSerializer<FernFlowerModifier> {
        @Override
        public FernFlowerModifier read(ResourceLocation name, JsonObject json, ILootCondition[] conditions) {
            return new FernFlowerModifier(conditions);
        }

        @Override
        public JsonObject write(FernFlowerModifier modifier) {
            return makeConditions(modifier.conditions);
        }
    }
}
