package org.slavicmyths.morovoy;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;

/** Isolated 2.3.2 content; does not replace Gorynych or the small fire serpent. */
public final class MorovoyFireZmeyContent {
 public static final DeferredHolder<EntityType<?>,EntityType<MorovoyFireZmey>> BOSS=ModEntities.ENTITIES.register("morovoy_fire_zmey",()->EntityType.Builder.of(MorovoyFireZmey::new,MobCategory.MONSTER).sized(2.7f,2.7f).eyeHeight(2.1f).fireImmune().clientTrackingRange(24).updateInterval(2).build("slavicmyths:morovoy_fire_zmey"));
 public static final DeferredHolder<EntityType<?>,EntityType<MorovoyZmeyBolt>> BOLT=ModEntities.ENTITIES.register("morovoy_zmey_bolt",()->EntityType.Builder.<MorovoyZmeyBolt>of(MorovoyZmeyBolt::new,MobCategory.MISC).sized(.52f,.52f).clientTrackingRange(14).updateInterval(1).build("slavicmyths:morovoy_zmey_bolt"));
 public static final DeferredHolder<EntityType<?>,EntityType<MorovoyZmeyField>> FIELD=ModEntities.ENTITIES.register("morovoy_zmey_field",()->EntityType.Builder.<MorovoyZmeyField>of(MorovoyZmeyField::new,MobCategory.MISC).sized(.4f,.4f).clientTrackingRange(10).updateInterval(5).build("slavicmyths:morovoy_zmey_field"));
 public static final DeferredHolder<Item,Item> BONE=ModItems.ITEMS.register("morovoy_fire_zmey_bone",()->new Item(new Item.Properties().rarity(Rarity.RARE)));
 public static final DeferredHolder<Item,Item> CORE=ModItems.ITEMS.register("morovoy_fire_zmey_core",()->new Item(new Item.Properties().rarity(Rarity.EPIC).stacksTo(16)));
 public static final DeferredHolder<Item,Item> SPAWN_EGG=ModItems.ITEMS.register("morovoy_fire_zmey_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(BOSS,0x571926,0x7635B8,new Item.Properties()));
 public static ResourceLocation id(String name){return ResourceLocation.fromNamespaceAndPath("slavicmyths",name);}
 public static void init(net.neoforged.bus.api.IEventBus bus){bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event)->event.put(BOSS.get(),MorovoyFireZmey.attributes().build()));}
 private MorovoyFireZmeyContent(){}
}
