package org.slavicmyths.industry;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class ChudPeople {
    public static final DeferredHolder<EntityType<?>,EntityType<ChudPerson>> RESIDENT=type("chud_resident"),ARTISAN=type("chud_artisan"),GUARD=type("chud_guard");
    private static DeferredHolder<EntityType<?>,EntityType<ChudPerson>> type(String name){return ModEntities.ENTITIES.register(name,()->EntityType.Builder.of(ChudPerson::new,MobCategory.CREATURE).sized(.6F,1.75F).clientTrackingRange(8).build("slavicmyths:"+name));}
    static {egg("chud_resident",RESIDENT,0x55705d);egg("chud_artisan",ARTISAN,0x8d643f);egg("chud_guard",GUARD,0x626b70);}
    private static void egg(String name,DeferredHolder<EntityType<?>,EntityType<ChudPerson>> type,int color){ModItems.ITEMS.register(name+"_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(type,color,0xb2a188,new Item.Properties()));}
    public static void init(IEventBus bus){}
    @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(RESIDENT.get(),ChudPerson.attributes(20).build());e.put(ARTISAN.get(),ChudPerson.attributes(24).build());e.put(GUARD.get(),ChudPerson.attributes(44).build());}
    private ChudPeople(){}
}
