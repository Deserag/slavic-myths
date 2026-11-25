package org.slavicmyths.desert;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;
public final class SandKingContent {
 public static final DeferredHolder<EntityType<?>,EntityType<SandKingEntity>> BOSS=ModEntities.ENTITIES.register("sand_king",()->EntityType.Builder.of(SandKingEntity::new,MobCategory.MONSTER).sized(3.0F,2.6F).clientTrackingRange(32).updateInterval(1).build("slavicmyths:sand_king"));
 public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("sand_king_spawn_egg",()->new DeferredSpawnEggItem(BOSS,0x542c25,0xe5a333,new Item.Properties()));
 public static void init(IEventBus bus){SandKingSounds.init();bus.addListener(SandKingContent::attributes);bus.addListener(SandKingContent::creative);NeoForge.EVENT_BUS.addListener(SandKingQuicksand::tick);}
 private static void attributes(EntityAttributeCreationEvent e){e.put(BOSS.get(),SandKingEntity.attributes().build());}
 private static void creative(BuildCreativeModeTabContentsEvent e){if(e.getTabKey()==CreativeModeTabs.SPAWN_EGGS)e.accept(EGG.get());}
 private SandKingContent(){}
}
