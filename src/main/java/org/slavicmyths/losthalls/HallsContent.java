package org.slavicmyths.losthalls;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

public final class HallsContent {
 public enum Kind { SHEPTUN,OSKOLNIK,GULOKRYL,EKHOVIK,GRIBOSHMYG }
 public static final ResourceKey<Biome> BIOME=ResourceKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","lost_halls"));
 public static final Map<Kind,DeferredHolder<EntityType<?>,EntityType<HallsMob>>> TYPES=new EnumMap<>(Kind.class);
 public static final Map<String,DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
 public static String id(Kind k){return k.name().toLowerCase(Locale.ROOT);}
 static {
  for(Kind k:Kind.values()){
   var type=ModEntities.ENTITIES.register(id(k),()->EntityType.Builder.<HallsMob>of((t,l)->new HallsMob(t,l,k),k==Kind.SHEPTUN||k==Kind.OSKOLNIK?MobCategory.MONSTER:MobCategory.CREATURE).sized(k==Kind.EKHOVIK?1.65F:k==Kind.OSKOLNIK?1.2F:k==Kind.GRIBOSHMYG?.85F:.8F,k==Kind.EKHOVIK?1.6F:k==Kind.OSKOLNIK?2.8F:k==Kind.SHEPTUN?2.4F:k==Kind.GULOKRYL?.8F:.8F).fireImmune().clientTrackingRange(10).build("slavicmyths:"+id(k)));
   TYPES.put(k,type);ModItems.ITEMS.register(id(k)+"_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(type,0x252432,k==Kind.GRIBOSHMYG?0xaa315b:0x55b8cb,new Item.Properties()));
  }
  for(String id:List.of("lost_whisper_thread","lightvein_shard","echo_vial","spore_pouch","posten_heart"))ITEMS.put(id,ModItems.ITEMS.register(id,()->id.equals("spore_pouch")?new HallsUtility.SporeItem():new Item(new Item.Properties())));
  ITEMS.put("lost_halls_tuning_fork",ModItems.ITEMS.register("lost_halls_tuning_fork",HallsUtility.TuningFork::new));
 }
 public static Item item(String id){return ITEMS.get(id).get();}
 public static void init(net.neoforged.bus.api.IEventBus bus){
  HallsBlocks.init();
  bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->TYPES.forEach((k,t)->e.put(t.get(),HallsMob.attributes(k).build())));
  bus.addListener((net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent e)->TYPES.forEach((k,t)->e.register(t.get(),k==Kind.GULOKRYL?SpawnPlacementTypes.NO_RESTRICTIONS:SpawnPlacementTypes.ON_GROUND,net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,r,p,random)->w.getBiome(p).is(BIOME)&&HallsWorldgen.habitat(k,w.getLevel().getSeed(),p)&&p.getY()>32&&p.getY()<121&&w.isEmptyBlock(p)&&w.isEmptyBlock(p.above())&&w.getFluidState(p).isEmpty()&&(k!=Kind.SHEPTUN&&k!=Kind.OSKOLNIK||w.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL)&&Mob.checkMobSpawnRules(type,w,r,p,random),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE)));
 }
 private HallsContent(){}
}
