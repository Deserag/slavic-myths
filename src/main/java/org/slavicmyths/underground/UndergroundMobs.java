package org.slavicmyths.underground;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slavicmyths.registry.*;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class UndergroundMobs {
 public static final DeferredHolder<EntityType<?>,EntityType<Podzemnik>> PODZEMNIK=ModEntities.ENTITIES.register("podzemnik",()->EntityType.Builder.of(Podzemnik::new,MobCategory.MONSTER).sized(.68F,1.68F).clientTrackingRange(10).build("slavicmyths:podzemnik"));
 public static final DeferredHolder<EntityType<?>,EntityType<Kladovik>> KLADOVIK=ModEntities.ENTITIES.register("kladovik",()->EntityType.Builder.of(Kladovik::new,MobCategory.MONSTER).sized(.82F,1.4F).clientTrackingRange(12).build("slavicmyths:kladovik"));
 public static final DeferredHolder<EntityType<?>,EntityType<LargePodzemnik>> LARGE=ModEntities.ENTITIES.register("large_podzemnik",()->EntityType.Builder.of(LargePodzemnik::new,MobCategory.MONSTER).sized(1.45F,2.8F).clientTrackingRange(14).build("slavicmyths:large_podzemnik"));
 public static final DeferredHolder<EntityType<?>,EntityType<ThrownStone>> STONE=ModEntities.ENTITIES.register("kladovik_stone",()->EntityType.Builder.<ThrownStone>of(ThrownStone::new,MobCategory.MISC).sized(.35F,.35F).clientTrackingRange(8).updateInterval(1).build("slavicmyths:kladovik_stone"));
 public static final DeferredHolder<EntityType<?>,EntityType<StoneSpike>> SPIKE=ModEntities.ENTITIES.register("kladovik_spike",()->EntityType.Builder.<StoneSpike>of(StoneSpike::new,MobCategory.MISC).sized(.5F,1.1F).clientTrackingRange(8).updateInterval(1).build("slavicmyths:kladovik_spike"));
 public static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(Registries.MOB_EFFECT,"slavicmyths");
 public static final DeferredHolder<MobEffect,MobEffect> KNOCKDOWN=EFFECTS.register("underground_knockdown",()->new MobEffect(MobEffectCategory.HARMFUL,0x615d57){}.addAttributeModifier(Attributes.MOVEMENT_SPEED,id("knockdown"),-.95,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
 public static final DeferredHolder<Item,Item> RUNE=ModItems.ITEMS.register("rune_ominous_aura",()->new org.slavicmyths.industry.IndustryItem(new Item.Properties(),"rune_ominous_aura"));
 static {egg("podzemnik",PODZEMNIK,0x665f53,0x302d29);egg("kladovik",KLADOVIK,0x402e2e,0xba933d);egg("large_podzemnik",LARGE,0x44454a,0xd87b3a);}
 private static void egg(String n,DeferredHolder<EntityType<?>,? extends EntityType<? extends Mob>> type,int a,int b){ModItems.ITEMS.register(n+"_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(type,a,b,new Item.Properties()));}
 public static ResourceLocation id(String n){return ResourceLocation.fromNamespaceAndPath("slavicmyths",n);}
 public static void init(IEventBus bus){EFFECTS.register(bus);org.slavicmyths.rpg.RuneDefinition.ALL.put("ominous_aura",new org.slavicmyths.rpg.RuneDefinition("ominous_aura",java.util.Map.of(),java.util.Set.of("sword","dagger","spear","heavy","bow","shield","staff","pickaxe")));}
 @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(PODZEMNIK.get(),EncounterMob.attributes(28,.25,5,20,.1).build());e.put(KLADOVIK.get(),EncounterMob.attributes(200,.26,6,32,.3).build());e.put(LARGE.get(),EncounterMob.attributes(300,.22,10,28,.75).build());}
 private UndergroundMobs(){}
}
