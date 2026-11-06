package org.slavicmyths.sisters;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;

/** Dedicated 3.3 content registration. No unapproved natural-spawn or worldgen rules. */
public final class SistersContent {
    public static final DeferredHolder<EntityType<?>,EntityType<LedeyaBoss>> LEDEYA = ModEntities.ENTITIES.register("ledeya", () -> EntityType.Builder.of(LedeyaBoss::new, MobCategory.MONSTER).sized(1.75F,4.65F).clientTrackingRange(24).updateInterval(2).build("slavicmyths:ledeya"));
    public static final DeferredHolder<EntityType<?>,EntityType<NeveyaBoss>> NEVEYA = ModEntities.ENTITIES.register("neveya", () -> EntityType.Builder.of(NeveyaBoss::new, MobCategory.MONSTER).sized(1.45F,7.25F).clientTrackingRange(32).updateInterval(2).build("slavicmyths:neveya"));
    public static final DeferredHolder<EntityType<?>,EntityType<NeveyaBolt>> NEVEYA_BOLT = ModEntities.ENTITIES.register("neveya_death_bolt", () -> EntityType.Builder.<NeveyaBolt>of(NeveyaBolt::new, MobCategory.MISC).sized(.42F,.42F).clientTrackingRange(20).updateInterval(1).build("slavicmyths:neveya_death_bolt"));
    public static final DeferredHolder<EntityType<?>,EntityType<LomeyaBoss>> LOMEYA = ModEntities.ENTITIES.register("lomeya", () -> EntityType.Builder.of(LomeyaBoss::new, MobCategory.MONSTER).sized(1.55F,6.75F).clientTrackingRange(24).updateInterval(2).build("slavicmyths:lomeya"));
    public static final DeferredHolder<EntityType<?>,EntityType<AgneyaBoss>> AGNEYA = ModEntities.ENTITIES.register("agneya", () -> EntityType.Builder.of(AgneyaBoss::new, MobCategory.MONSTER).sized(1.75F,5.10F).clientTrackingRange(24).updateInterval(2).build("slavicmyths:agneya"));
    public static final DeferredHolder<EntityType<?>,EntityType<SisterBoss>> GNETEYA = ModEntities.ENTITIES.register("gneteya", () -> EntityType.Builder.of(SisterBoss::new, MobCategory.MONSTER).sized(2.2F,5.5F).clientTrackingRange(24).updateInterval(2).build("slavicmyths:gneteya"));
    public static final DeferredHolder<EntityType<?>,EntityType<SisterBoss>> TRYASEYA = ModEntities.ENTITIES.register("tryaseya", () -> EntityType.Builder.of(SisterBoss::new, MobCategory.MONSTER).sized(1.15F,3.5F).clientTrackingRange(24).updateInterval(2).build("slavicmyths:tryaseya"));
    public static final DeferredHolder<EntityType<?>,EntityType<FeverSpirit>> SPIRIT = ModEntities.ENTITIES.register("tryaseya_fever_spirit", () -> EntityType.Builder.of(FeverSpirit::new, MobCategory.MONSTER).sized(.7F,1.1F).clientTrackingRange(12).updateInterval(2).build("slavicmyths:tryaseya_fever_spirit"));
    public static final DeferredHolder<EntityType<?>,EntityType<SisterBolt>> BOLT = ModEntities.ENTITIES.register("tryaseya_fever_bolt", () -> EntityType.Builder.<SisterBolt>of(SisterBolt::new, MobCategory.MISC).sized(.4F,.4F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:tryaseya_fever_bolt"));
    public static final DeferredHolder<EntityType<?>,EntityType<SisterBolt>> FROST_BOLT = ModEntities.ENTITIES.register("ledeya_frost_bolt", () -> EntityType.Builder.<SisterBolt>of(SisterBolt::new, MobCategory.MISC).sized(.4F,.4F).clientTrackingRange(24).updateInterval(1).build("slavicmyths:ledeya_frost_bolt"));
    public static final DeferredHolder<EntityType<?>,EntityType<BoneBarrier>> BARRIER = ModEntities.ENTITIES.register("gneteya_bone_barrier", () -> EntityType.Builder.<BoneBarrier>of(BoneBarrier::new, MobCategory.MISC).sized(.95F,2.8F).clientTrackingRange(16).updateInterval(2).build("slavicmyths:gneteya_bone_barrier"));
    public static final DeferredHolder<EntityType<?>,EntityType<SistersZone>> ZONE = ModEntities.ENTITIES.register("tryaseya_trembling_zone", () -> EntityType.Builder.<SistersZone>of(SistersZone::new, MobCategory.MISC).sized(.2F,.2F).clientTrackingRange(12).updateInterval(10).build("slavicmyths:tryaseya_trembling_zone"));
    public static final DeferredHolder<Item,Item> LEDEYA_EGG = ModItems.ITEMS.register("ledeya_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(LEDEYA,0xD7DFE8,0x2856A4,new Item.Properties()));
    public static final DeferredHolder<Item,Item> NEVEYA_EGG = ModItems.ITEMS.register("neveya_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(NEVEYA,0x141824,0x698AC6,new Item.Properties()));
    public static final DeferredHolder<Item,Item> LOMEYA_EGG = ModItems.ITEMS.register("lomeya_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(LOMEYA,0xCCC9C5,0x4C4251,new Item.Properties()));
    public static final DeferredHolder<Item,Item> AGNEYA_EGG = ModItems.ITEMS.register("agneya_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(AGNEYA,0x5A2430,0xE6AE3B,new Item.Properties()));
    public static final DeferredHolder<Item,Item> GNETEYA_EGG = ModItems.ITEMS.register("gneteya_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(GNETEYA,0x48232D,0xD4C7AA,new Item.Properties()));
    public static final DeferredHolder<Item,Item> TRYASEYA_EGG = ModItems.ITEMS.register("tryaseya_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(TRYASEYA,0x292336,0xA86AD8,new Item.Properties()));

    public static void init(IEventBus bus) {
        TryaseyaSounds.init(bus);
        bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) -> {
            event.put(LEDEYA.get(),LedeyaBoss.attributes().build());
            event.put(NEVEYA.get(),NeveyaBoss.attributes().build());
            event.put(LOMEYA.get(),LomeyaBoss.attributes().build());
            event.put(AGNEYA.get(),AgneyaBoss.attributes().build());
            event.put(GNETEYA.get(),SisterBoss.attributes(false).build());
            event.put(TRYASEYA.get(),SisterBoss.attributes(true).build());
            event.put(SPIRIT.get(),FeverSpirit.attributes().build());
        });
    }

    /** Integration hook for future approved arena; never invoked by individual Creative eggs. */
    public static boolean spawnPair(ServerLevel world,BlockPos anchor) {
        if (!world.hasChunkAt(anchor) || !world.hasChunkAt(anchor.offset(3,0,0))) return false;
        SisterBoss g = GNETEYA.get().create(world);
        SisterBoss t = TRYASEYA.get().create(world);
        if (g == null || t == null) return false;
        g.setPos(anchor.getX()+.5,anchor.getY()+1,anchor.getZ()+.5);
        t.setPos(anchor.getX()+3.5,anchor.getY()+1,anchor.getZ()+.5);
        if (!world.noCollision(g) || !world.noCollision(t)) return false;
        UUID encounter = UUID.randomUUID();
        g.link(encounter,t.getUUID());
        t.link(encounter,g.getUUID());
        if (!world.addFreshEntity(g)) return false;
        if (!world.addFreshEntity(t)) { g.discard(); return false; }
        return true;
    }
    private SistersContent() {}
}
