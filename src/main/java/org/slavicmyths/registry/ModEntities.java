package org.slavicmyths.registry;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.entity.DomovoyEntity;
import org.slavicmyths.entity.LeshyEntity;

@Mod.EventBusSubscriber(modid = SlavicMyths.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, SlavicMyths.MOD_ID);
    public static final RegistryObject<EntityType<DomovoyEntity>> DOMOVOY = ENTITIES.register("domovoy",
            () -> EntityType.Builder.of(DomovoyEntity::new, EntityClassification.CREATURE)
                    .sized(0.55F, 1.1F).clientTrackingRange(8).build("slavicmyths:domovoy"));
    public static final RegistryObject<EntityType<LeshyEntity>> LESHY = ENTITIES.register("leshy",
            () -> EntityType.Builder.of(LeshyEntity::new, EntityClassification.MONSTER)
                    .sized(0.8F, 2.8F).clientTrackingRange(8).build("slavicmyths:leshy"));

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(DOMOVOY.get(), DomovoyEntity.attributes().build());
        event.put(LESHY.get(), LeshyEntity.attributes().build());
    }
    private ModEntities() { }
}
