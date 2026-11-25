package org.slavicmyths.desert;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class IndrikContent {
 public static final DeferredHolder<EntityType<?>,EntityType<IndrikEntity>> BOSS=ModEntities.ENTITIES.register("indrik_zmey",()->EntityType.Builder.of(IndrikEntity::new,MobCategory.MONSTER).sized(3.2F,3.8F).clientTrackingRange(32).updateInterval(1).build("slavicmyths:indrik_zmey"));
 public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("indrik_zmey_spawn_egg",()->new DeferredSpawnEggItem(BOSS,0x514b3e,0x42bdb5,new Item.Properties()));
 public static void init(IEventBus bus){bus.addListener(IndrikContent::attributes);bus.addListener(IndrikContent::creative);}
 private static void attributes(EntityAttributeCreationEvent e){e.put(BOSS.get(),IndrikEntity.attributes().build());}
 private static void creative(BuildCreativeModeTabContentsEvent e){if(e.getTabKey()==CreativeModeTabs.SPAWN_EGGS)e.accept(EGG.get());}
 private IndrikContent(){}
}
