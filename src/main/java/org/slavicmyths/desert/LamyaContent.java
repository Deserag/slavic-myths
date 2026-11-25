package org.slavicmyths.desert;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class LamyaContent {
 public static final DeferredHolder<EntityType<?>,EntityType<LamyaEntity>> BOSS=ModEntities.ENTITIES.register("lamya",()->EntityType.Builder.of(LamyaEntity::new,MobCategory.MONSTER).sized(2.4F,6.8F).clientTrackingRange(32).updateInterval(1).build("slavicmyths:lamya"));
 public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("lamya_spawn_egg",()->new DeferredSpawnEggItem(BOSS,0x483844,0xffb32b,new Item.Properties()));
 public static void init(IEventBus bus){LamyaSounds.init();bus.addListener(LamyaContent::attributes);bus.addListener(LamyaContent::creative);}
 private static void attributes(EntityAttributeCreationEvent e){e.put(BOSS.get(),LamyaEntity.attributes().build());}
 private static void creative(BuildCreativeModeTabContentsEvent e){if(e.getTabKey()==CreativeModeTabs.SPAWN_EGGS)e.accept(EGG.get());}
 private LamyaContent(){}
}
