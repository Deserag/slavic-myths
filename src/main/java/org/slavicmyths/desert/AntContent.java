package org.slavicmyths.desert;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class AntContent {
 public static final DeferredHolder<EntityType<?>,EntityType<AntQueenEntity>> QUEEN=ModEntities.ENTITIES.register("ant_queen",()->EntityType.Builder.of(AntQueenEntity::new,MobCategory.MONSTER).sized(2.4F,2.8F).clientTrackingRange(32).updateInterval(1).build("slavicmyths:ant_queen"));
 public static final DeferredHolder<EntityType<?>,EntityType<AntMinionEntity>> WORKER=ModEntities.ENTITIES.register("desert_ant",()->EntityType.Builder.<AntMinionEntity>of((t,w)->new AntMinionEntity(t,w,false),MobCategory.MONSTER).sized(.72F,.60F).clientTrackingRange(10).updateInterval(2).build("slavicmyths:desert_ant"));
 public static final DeferredHolder<EntityType<?>,EntityType<AntMinionEntity>> FLYING=ModEntities.ENTITIES.register("flying_desert_ant",()->EntityType.Builder.<AntMinionEntity>of((t,w)->new AntMinionEntity(t,w,true),MobCategory.MONSTER).sized(.72F,.60F).clientTrackingRange(12).updateInterval(2).build("slavicmyths:flying_desert_ant"));
 public static final DeferredHolder<Item,Item> QUEEN_EGG=ModItems.ITEMS.register("ant_queen_spawn_egg",()->new DeferredSpawnEggItem(QUEEN,0x38252c,0xa53639,new Item.Properties()));
 public static final DeferredHolder<Item,Item> WORKER_EGG=ModItems.ITEMS.register("desert_ant_spawn_egg",()->new DeferredSpawnEggItem(WORKER,0x632b31,0xd7ad79,new Item.Properties()));
 public static final DeferredHolder<Item,Item> FLYING_EGG=ModItems.ITEMS.register("flying_desert_ant_spawn_egg",()->new DeferredSpawnEggItem(FLYING,0x632b31,0xc9d9d5,new Item.Properties()));
 public static void init(IEventBus bus){AntSounds.init();bus.addListener(AntContent::attributes);bus.addListener(AntContent::creative);}
 private static void attributes(EntityAttributeCreationEvent e){e.put(QUEEN.get(),AntQueenEntity.attributes().build());e.put(WORKER.get(),AntMinionEntity.attributes(false).build());e.put(FLYING.get(),AntMinionEntity.attributes(true).build());}
 private static void creative(BuildCreativeModeTabContentsEvent e){if(e.getTabKey()==CreativeModeTabs.SPAWN_EGGS){e.accept(QUEEN_EGG.get());e.accept(WORKER_EGG.get());e.accept(FLYING_EGG.get());}}
 private AntContent(){}
}
