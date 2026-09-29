package org.slavicmyths.registry;

import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.loot.FernFlowerModifier;

public final class ModLoot {
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, SlavicMyths.MOD_ID);

    static {
        SERIALIZERS.register("fern_flower", FernFlowerModifier.Serializer::new);
    }

    private ModLoot() { }
}
