package org.slavicmyths.owl;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class WinterOwlContent {
 public static final DeferredHolder<EntityType<?>,EntityType<WinterOwl>> BOSS=ModEntities.ENTITIES.register("winter_owl",()->EntityType.Builder.of(WinterOwl::new,MobCategory.MONSTER).sized(2.6F,4.5F).clientTrackingRange(32).updateInterval(1).build("slavicmyths:winter_owl"));
 public static final DeferredHolder<EntityType<?>,EntityType<WinterOwl>> SPIRIT=ModEntities.ENTITIES.register("winter_owl_spirit",()->EntityType.Builder.of(WinterOwl::new,MobCategory.MONSTER).sized(.78F,1.35F).noSave().clientTrackingRange(16).updateInterval(2).build("slavicmyths:winter_owl_spirit"));
 public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("winter_owl_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(BOSS,0x777985,0x42C9ED,new Item.Properties()));
 public static void init(IEventBus bus){bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->{e.put(BOSS.get(),WinterOwl.attributes(false).build());e.put(SPIRIT.get(),WinterOwl.attributes(true).build());});}
 private WinterOwlContent(){}
}