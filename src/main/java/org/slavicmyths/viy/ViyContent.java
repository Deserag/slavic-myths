package org.slavicmyths.viy;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.sisters.ViyBoss;
import org.slavicmyths.sisters.ViyServant;

/** Isolated Viy boss/weak servant registration; no natural spawn or automatic rewards. */
public final class ViyContent {
    public static final DeferredHolder<EntityType<?>,EntityType<ViyBoss>> VIY=
        ModEntities.ENTITIES.register("viy",()->EntityType.Builder.of(ViyBoss::new,MobCategory.MONSTER)
            .sized(2.90F,4.0F).clientTrackingRange(32).updateInterval(2).build("slavicmyths:viy"));
    public static final DeferredHolder<EntityType<?>,EntityType<ViyServant>> VIY_UPYR=
        ModEntities.ENTITIES.register("viy_grave_upyr",()->EntityType.Builder.<ViyServant>of(
            (t,w)->new ViyServant(t,w,ViyServant.Kind.UPYR),MobCategory.MONSTER)
            .sized(ViyServant.Kind.UPYR.width,ViyServant.Kind.UPYR.height).clientTrackingRange(16).build("slavicmyths:viy_grave_upyr"));
    public static final DeferredHolder<EntityType<?>,EntityType<ViyServant>> VIY_NAV=
        ModEntities.ENTITIES.register("viy_faceless_nav",()->EntityType.Builder.<ViyServant>of(
            (t,w)->new ViyServant(t,w,ViyServant.Kind.NAV),MobCategory.MONSTER)
            .sized(ViyServant.Kind.NAV.width,ViyServant.Kind.NAV.height).clientTrackingRange(16).build("slavicmyths:viy_faceless_nav"));
    public static final DeferredHolder<EntityType<?>,EntityType<ViyServant>> VIY_KORNELAP=
        ModEntities.ENTITIES.register("viy_kornelap",()->EntityType.Builder.<ViyServant>of(
            (t,w)->new ViyServant(t,w,ViyServant.Kind.KORNELAP),MobCategory.MONSTER)
            .sized(ViyServant.Kind.KORNELAP.width,ViyServant.Kind.KORNELAP.height).clientTrackingRange(16).build("slavicmyths:viy_kornelap"));
    public static final DeferredHolder<Item,Item> VIY_EYE = ModItems.ITEMS.register("viy_eye", () -> new org.slavicmyths.item.FolkAccessoryItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).stacksTo(1), "head"));
    public static final DeferredHolder<Item,Item> VIY_RING = ModItems.ITEMS.register("viy_ring", () -> new org.slavicmyths.item.FolkAccessoryItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).stacksTo(1), "ring"));
    public static final DeferredHolder<Item,Item> VIY_SHARD = ModItems.ITEMS.register("viy_shard", () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).stacksTo(64)));
    public static final DeferredHolder<Item,Item> VIY_EGG=
        ModItems.ITEMS.register("viy_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(
            VIY,0x4B3F37,0xA27862,new Item.Properties()));
    public static void init(IEventBus bus){
        bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event)->{
            event.put(VIY.get(),ViyBoss.attributes().build());
            event.put(VIY_UPYR.get(),ViyServant.attributes(ViyServant.Kind.UPYR).build());
            event.put(VIY_NAV.get(),ViyServant.attributes(ViyServant.Kind.NAV).build());
            event.put(VIY_KORNELAP.get(),ViyServant.attributes(ViyServant.Kind.KORNELAP).build());
        });
    }
    private ViyContent(){}
}
