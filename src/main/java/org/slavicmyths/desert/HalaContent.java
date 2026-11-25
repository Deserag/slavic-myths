package org.slavicmyths.desert;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class HalaContent {
 public static final DeferredHolder<EntityType<?>,EntityType<HalaEntity>> BOSS=ModEntities.ENTITIES.register("hala",()->EntityType.Builder.of(HalaEntity::new,MobCategory.MONSTER).sized(2.8F,6.4F).fireImmune().clientTrackingRange(32).updateInterval(1).build("slavicmyths:hala"));
 public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("hala_spawn_egg",()->new DeferredSpawnEggItem(BOSS,0x362832,0xdfa54b,new Item.Properties()));
 public static void init(IEventBus bus){HalaSounds.init();bus.addListener(HalaContent::attributes);bus.addListener(HalaContent::creative);}
 private static void attributes(EntityAttributeCreationEvent e){e.put(BOSS.get(),HalaEntity.attributes().build());}
 private static void creative(BuildCreativeModeTabContentsEvent e){if(e.getTabKey()==CreativeModeTabs.SPAWN_EGGS)e.accept(EGG.get());}
 private HalaContent(){}
}
