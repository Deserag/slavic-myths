package org.slavicmyths.burningislands;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

/** Independent playable entities/items. Natural selection stays disconnected until ocean-mask proof. */
public final class IslandsContent {
 public enum Kind { HEAT_SALAMANDER,SLAG_CRAB,VENT_GOLEM,MAGMA_LORD }
 public static final ResourceKey<Biome> BIOME=ResourceKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","burning_islands"));
 public static final Map<Kind,DeferredHolder<EntityType<?>,EntityType<IslandsMob>>> TYPES=new EnumMap<>(Kind.class);
 public static final Map<String,DeferredHolder<Item,Item>> MATERIALS=new LinkedHashMap<>();
 public static String id(Kind k){return k.name().toLowerCase(Locale.ROOT);}
 static {
  for(Kind k:Kind.values()){
   float w=switch(k){case HEAT_SALAMANDER->1.5F;case SLAG_CRAB->2F;case VENT_GOLEM->1.8F;case MAGMA_LORD->2.2F;};
   float h=switch(k){case HEAT_SALAMANDER->.8F;case SLAG_CRAB->1F;case VENT_GOLEM->3F;case MAGMA_LORD->5.3F;};
   var type=ModEntities.ENTITIES.register(id(k),()->EntityType.Builder.<IslandsMob>of((t,l)->new IslandsMob(t,l,k),MobCategory.CREATURE).sized(w,h).fireImmune().clientTrackingRange(12).updateInterval(2).build("slavicmyths:"+id(k)));
   TYPES.put(k,type);
   ModItems.ITEMS.register(id(k)+"_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(type,0x29232c,0xea6d19,new Item.Properties()));
  }
  for(String id:List.of("magma_clot","magma_core","salamander_hide","crab_claw","magma_rune_fragment","magma_lord_heart"))
   MATERIALS.put(id,ModItems.ITEMS.register(id,()->new Item(new Item.Properties().fireResistant()){
    @Override public void appendHoverText(ItemStack stack,TooltipContext c,List<net.minecraft.network.chat.Component> lines,TooltipFlag f){if(id.equals("magma_lord_heart"))lines.add(net.minecraft.network.chat.Component.translatable("burningislands.heart.future"));}
   }));
  for(int tier=1;tier<=2;tier++){
   String id="rune_magma_"+tier;
   // RpgClient/RuneDefinition owns rune tooltips; do not append the same effect twice.
   MATERIALS.put(id,ModItems.ITEMS.register(id,()->new Item(new Item.Properties().stacksTo(16).fireResistant().rarity(Rarity.RARE))));
  }
 }
 public static Item item(String id){return MATERIALS.get(id).get();}
 public static void init(net.neoforged.bus.api.IEventBus bus){
  bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->TYPES.forEach((k,t)->e.put(t.get(),IslandsMob.attributes(k).build())));
  bus.addListener((net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent e)->{
   for(Kind k:new Kind[]{Kind.HEAT_SALAMANDER,Kind.SLAG_CRAB,Kind.VENT_GOLEM})e.register(TYPES.get(k).get(),SpawnPlacementTypes.ON_GROUND,net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
    (type,level,reason,pos,random)->level.getBiome(pos).is(BIOME)&&pos.getY()>level.getSeaLevel()&&level.getFluidState(pos).isEmpty()&&level.getFluidState(pos.below()).isEmpty()&&level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),net.minecraft.core.Direction.UP)&&Mob.checkMobSpawnRules(type,level,reason,pos,random),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
  });
 }
 private IslandsContent(){}
}
