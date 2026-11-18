package org.slavicmyths.ninemor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.registry.ModSounds;

/** A separate nine-headed summon boss; never replaces either existing dragon. */
public final class NineMorContent {
    public static final DeferredHolder<EntityType<?>, EntityType<NineMorSerpent>> BOSS =
        ModEntities.ENTITIES.register("nine_mor_serpent", () ->
            EntityType.Builder.of(NineMorSerpent::new, MobCategory.MONSTER)
                .sized(18.0F, 13.8F).eyeHeight(11.8F).fireImmune()
                .clientTrackingRange(32).updateInterval(2).build("slavicmyths:nine_mor_serpent"));

    public static final DeferredHolder<EntityType<?>, EntityType<NineMorShot>> SHOT =
        ModEntities.ENTITIES.register("nine_mor_shot", () ->
            EntityType.Builder.<NineMorShot>of(NineMorShot::new, MobCategory.MISC)
                .sized(.42F, .42F).clientTrackingRange(18).updateInterval(1)
                .build("slavicmyths:nine_mor_shot"));

    /** No spawn egg and no crafting recipe until the ritual ingredients are designed. */
    public static final DeferredHolder<Item, Item> SEAL =
        ModItems.ITEMS.register("pechat_devyati_morov", () ->
            new NineMorSealItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1).fireResistant()));

    public static final DeferredHolder<SoundEvent, SoundEvent> SUMMON_SOUND =
        ModSounds.SOUNDS.register("nine_mor_serpent.summon", () ->
            SoundEvent.createFixedRangeEvent(id("nine_mor_serpent.summon"), 64));

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("slavicmyths", path);
    }

    public static void init(IEventBus bus) {
        bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) ->
            event.put(BOSS.get(), NineMorSerpent.attributes().build()));
    }

    private NineMorContent() {}
}
