package org.slavicmyths.registry;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slavicmyths.loot.FernFlowerModifier;

public final class ModLoot {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "slavicmyths");
    static { SERIALIZERS.register("fern_flower", () -> FernFlowerModifier.CODEC); }
    private ModLoot() { }
}
