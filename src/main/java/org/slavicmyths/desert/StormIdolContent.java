package org.slavicmyths.desert;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class StormIdolContent {
 public static final DeferredHolder<EntityType<?>,EntityType<StormIdolEntity>> BOSS=ModEntities.ENTITIES.register("storm_idol",()->EntityType.Builder.of(StormIdolEntity::new,MobCategory.MONSTER).sized(1.8F,5.2F).clientTrackingRange(32).updateInterval(1).build("slavicmyths:storm_idol"));
 public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("storm_idol_spawn_egg",()->new DeferredSpawnEggItem(BOSS,0x474755,0x8ae8ff,new Item.Properties()));
 public static void init(IEventBus bus){StormIdolSounds.init();bus.addListener(StormIdolContent::attributes);bus.addListener(StormIdolContent::creative);}
 private static void attributes(EntityAttributeCreationEvent e){e.put(BOSS.get(),StormIdolEntity.attributes().build());}
 private static void creative(BuildCreativeModeTabContentsEvent e){if(e.getTabKey()==CreativeModeTabs.SPAWN_EGGS)e.accept(EGG.get());}
 private StormIdolContent(){}
}
