package org.slavicmyths.blackcinder;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class CinderContent {
 public enum Kind { COAL_CRAWLER,ASH_SPIRIT,PEKELNIK,SPARKLING,ZHARENITSA }
 public static final Map<Kind,DeferredHolder<EntityType<?>,EntityType<CinderMob>>> TYPES=new EnumMap<>(Kind.class);
 public static String id(Kind k){return k.name().toLowerCase(Locale.ROOT);}
 static {for(Kind k:Kind.values()){
  float w=switch(k){case COAL_CRAWLER->1.8F;case ASH_SPIRIT->.9F;case PEKELNIK->1.2F;case SPARKLING->.4F;case ZHARENITSA->1.8F;};
  float h=switch(k){case COAL_CRAWLER->1.1F;case ASH_SPIRIT->1.6F;case PEKELNIK->3F;case SPARKLING->.5F;case ZHARENITSA->4F;};
  var type=ModEntities.ENTITIES.register(id(k),()->EntityType.Builder.<CinderMob>of((t,l)->new CinderMob(t,l,k),k==Kind.SPARKLING?MobCategory.AMBIENT:MobCategory.MONSTER).sized(w,h).fireImmune().clientTrackingRange(12).updateInterval(3).build("slavicmyths:"+id(k)));
  TYPES.put(k,type);ModItems.ITEMS.register(id(k)+"_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(type,0x242122,k==Kind.ASH_SPIRIT?0x9b9993:0xe46b23,new Item.Properties()));
 }}
 public static final DeferredHolder<Item,Item> RUNE=ModItems.ITEMS.register("rune_ash_mantle",CinderMantle::new);
 public static void init(net.neoforged.bus.api.IEventBus bus){bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->TYPES.forEach((k,t)->e.put(t.get(),CinderMob.attributes(k).build())));}
 private CinderContent(){}
}
