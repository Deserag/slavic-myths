package org.slavicmyths.pesigolovtsy;
import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.biome.*;
import net.minecraft.tags.TagKey;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.common.world.*;
import org.slavicmyths.registry.*;
import org.slavicmyths.pesigolovtsy.DogheadRules.Role;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class DogheadContent {
 public static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("slavicmyths",path);}
 public static final DeferredHolder<EntityType<?>,EntityType<DogheadEntity>> ENTITY=ModEntities.ENTITIES.register("pesigolovets",()->EntityType.Builder.of(DogheadEntity::new,MobCategory.MONSTER).sized(.95F,2.3F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:pesigolovets"));
 public static final DeferredHolder<EntityType<?>,EntityType<DogheadArrow>> ARROW=ModEntities.ENTITIES.register("doghead_arrow",()->EntityType.Builder.<DogheadArrow>of(DogheadArrow::new,MobCategory.MISC).sized(.5F,.5F).clientTrackingRange(8).updateInterval(1).build("slavicmyths:doghead_arrow"));
 private static final List<DeferredHolder<Item,Item>> EGGS=new ArrayList<>();
 private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new LinkedHashMap<>();
 static{for(var role:Role.values()){EGGS.add(ModItems.ITEMS.register("pesigolovets_"+role.id()+"_spawn_egg",()->{var n=new CompoundTag();n.putString("id","slavicmyths:pesigolovets");n.putString("DogheadRole",role.name());n.putFloat("Health",role.hp);return new net.neoforged.neoforge.common.DeferredSpawnEggItem(ENTITY,role==Role.LEADER?0x2b2928:role==Role.SCOUT?0x514b49:0x796358,role==Role.SCOUT?0x3f322b:0x843b30,new Item.Properties().component(DataComponents.ENTITY_DATA,CustomData.of(n)));}));}
  for(String key:List.of("hunter.ambient","hunter.bark","hunter.thrust_whoosh","hunter.sweep_whoosh","scout.ambient","scout.spot_bark","scout.bow_draw","scout.bow_release","scout.knife_whoosh","leader.ambient","leader.howl_inhale","leader.howl_release","leader.axe_whoosh","leader.axe_impact","shared.hurt","shared.death","shared.claw_gear","shared.leather_step","shared.mud_step","shared.stone_step","shared.fur_cloth"))SOUNDS.put(key,ModSounds.SOUNDS.register("doghead."+key,()->SoundEvent.createVariableRangeEvent(id("doghead."+key))));
  for(var role:Role.values())for(String event:List.of("hurt","death")){String key=role.id()+"."+event;SOUNDS.put(key,ModSounds.SOUNDS.register("doghead."+key,()->SoundEvent.createVariableRangeEvent(id("doghead."+key))));}
 }
 public static SoundEvent sound(String key){return SOUNDS.get(key).get();}
 public static final TagKey<Biome> BIOMES=TagKey.create(Registries.BIOME,id("pesigolovtsy_wilderness"));
 public static boolean clearLargestBody(net.minecraft.server.level.ServerLevel w,net.minecraft.core.BlockPos p){return w.noCollision(null,new net.minecraft.world.phys.AABB(p.getX()+.025,p.getY(),p.getZ()+.025,p.getX()+.975,p.getY()+2.3,p.getZ()+.975));}
 private static final DeferredRegister<MapCodec<? extends BiomeModifier>> MODIFIERS=DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS,"slavicmyths");
 private static final DeferredHolder<MapCodec<? extends BiomeModifier>,MapCodec<SpawnModifier>> SPAWNS=MODIFIERS.register("doghead_spawns",()->MapCodec.unit(SpawnModifier::new));
 public static void init(IEventBus bus){MODIFIERS.register(bus);}
 public static final class SpawnModifier implements BiomeModifier{public MapCodec<? extends BiomeModifier> codec(){return SPAWNS.get();}public void modify(net.minecraft.core.Holder<Biome> biome,Phase phase,ModifiableBiomeInfo.BiomeInfo.Builder b){if(phase==Phase.ADD&&biome.is(BIOMES)&&DogheadConfig.weight>0)b.getMobSpawnSettings().addSpawn(MobCategory.MONSTER,new MobSpawnSettings.SpawnerData(ENTITY.get(),DogheadConfig.weight,3,6));}}
 @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY.get(),Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,40).add(Attributes.ARMOR,0).add(Attributes.ATTACK_DAMAGE,6).add(Attributes.MOVEMENT_SPEED,.26).add(Attributes.FOLLOW_RANGE,28).build());}
 @SubscribeEvent public static void placements(RegisterSpawnPlacementsEvent e){e.register(ENTITY.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,reason,p,r)->{
  if(reason==MobSpawnType.SPAWN_EGG||reason==MobSpawnType.COMMAND)return true;
  if(w.getLevel().dimension()!=Level.OVERWORLD||!w.getBiome(p).is(BIOMES)||w.getLevel().isCloseToVillage(p,2)||p.distToLowCornerSqr(w.getLevel().getSharedSpawnPos().getX(),p.getY(),w.getLevel().getSharedSpawnPos().getZ())<96*96||!w.getLevel().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING)||!w.getLevel().getWorldBorder().isWithinBounds(p)||!w.getFluidState(p).isEmpty()||p.getY()<w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,p.getX(),p.getZ())-2)return false;
  if(!clearLargestBody(w.getLevel(),p))return false;if(!DogheadRules.night(w.getLevel().getDayTime())&&r.nextInt(40)!=0)return false;
  return Monster.checkMonsterSpawnRules(type,w,reason,p,r);
 },RegisterSpawnPlacementsEvent.Operation.REPLACE);}
 private DogheadContent(){}
}
