package org.slavicmyths.nochnitsa;
import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.common.world.*;
import org.slavicmyths.registry.*;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class NochnitsaContent {
    public static final DeferredHolder<EntityType<?>,EntityType<NochnitsaEntity>> ENTITY=ModEntities.ENTITIES.register("nochnitsa",()->EntityType.Builder.of(NochnitsaEntity::new,MobCategory.MONSTER).sized(.72F,2.15F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:nochnitsa"));
    public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("nochnitsa_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(ENTITY,0x343b50,0xdce5f6,new Item.Properties()));
    public static final TagKey<Biome> BIOMES=TagKey.create(Registries.BIOME,id("nochnitsa_surface"));
    public static final List<String> CUES=List.of("ambient","breath","step","claw_swing","hurt","scream_inhale","scream_release","scream_interrupt","target_spotted","sunrise_dissolve","death","robes_rustle");
    private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new LinkedHashMap<>();
    static{for(String name:CUES)SOUNDS.put(name,ModSounds.SOUNDS.register("nochnitsa."+name,()->SoundEvent.createVariableRangeEvent(id("nochnitsa."+name))));}
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> MODIFIERS=DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS,"slavicmyths");
    private static final DeferredHolder<MapCodec<? extends BiomeModifier>,MapCodec<SpawnModifier>> SPAWNS=MODIFIERS.register("nochnitsa_spawns",()->MapCodec.unit(SpawnModifier::new));
    public static ResourceLocation id(String p){return ResourceLocation.fromNamespaceAndPath("slavicmyths",p);}
    public static SoundEvent sound(String p){return SOUNDS.get(p).get();}
    public static void init(IEventBus bus){MODIFIERS.register(bus);}
    public static final class SpawnModifier implements BiomeModifier{
        public MapCodec<? extends BiomeModifier> codec(){return SPAWNS.get();}
        public void modify(net.minecraft.core.Holder<Biome> biome,Phase phase,ModifiableBiomeInfo.BiomeInfo.Builder builder){if(phase==Phase.ADD&&biome.is(BIOMES)&&NochnitsaConfig.spawnWeight>0)builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER,new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(ENTITY.get(),NochnitsaConfig.spawnWeight,1,1));}
    }
    @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY.get(),Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,50).add(Attributes.ARMOR,0).add(Attributes.ATTACK_DAMAGE,6).add(Attributes.MOVEMENT_SPEED,.26).add(Attributes.FOLLOW_RANGE,24).add(Attributes.KNOCKBACK_RESISTANCE,.05).build());}
    @SubscribeEvent public static void placements(RegisterSpawnPlacementsEvent e){e.register(ENTITY.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,reason,p,r)->{
        if(reason==MobSpawnType.SPAWN_EGG||reason==MobSpawnType.COMMAND)return true;
        if(w.getLevel().dimension()!=Level.OVERWORLD||!NochnitsaRules.night(w.getLevel().getDayTime())||!w.canSeeSky(p)||!w.getBiome(p).is(BIOMES)||!w.getLevel().getWorldBorder().isWithinBounds(p)||!w.getFluidState(p).isEmpty()||!Monster.checkMonsterSpawnRules(type,w,reason,p,r))return false;
        if(!w.getLevel().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING))return false;
        return w.getLevel().getEntitiesOfClass(NochnitsaEntity.class,new net.minecraft.world.phys.AABB(p).inflate(48),n->n.isAlive()&&n.distanceToSqr(p.getX()+.5,p.getY(),p.getZ()+.5)<=48*48).isEmpty();
    },RegisterSpawnPlacementsEvent.Operation.REPLACE);}
    private NochnitsaContent(){}
}
