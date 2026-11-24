package org.slavicmyths.winterboss;
import net.minecraft.world.entity.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;
public final class ShipCaptainContent {
    public static final DeferredHolder<EntityType<?>,EntityType<ShipCaptain>> BOSS=ModEntities.ENTITIES.register("ship_captain",()->EntityType.Builder.of(ShipCaptain::new,MobCategory.MONSTER).sized(1.25F,3.125F).eyeHeight(2.68F).clientTrackingRange(20).updateInterval(1).build("slavicmyths:ship_captain"));
    public static final DeferredHolder<EntityType<?>,EntityType<ShipCaptainAxe>> AXE=ModEntities.ENTITIES.register("ship_captain_axe",()->EntityType.Builder.of(ShipCaptainAxe::new,MobCategory.MISC).sized(.5F,.5F).noSave().clientTrackingRange(16).updateInterval(1).build("slavicmyths:ship_captain_axe"));
    public static void init(IEventBus bus){bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->e.put(BOSS.get(),ShipCaptain.attributes().build()));}
    private ShipCaptainContent(){}
}
