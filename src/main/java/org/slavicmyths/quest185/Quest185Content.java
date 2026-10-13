package org.slavicmyths.quest185;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class Quest185Content {
 public static ResourceLocation id(String n){return ResourceLocation.fromNamespaceAndPath("slavicmyths",n);}
 public static final DeferredHolder<Item,Item> WARD=ModItems.ITEMS.register("mor_ward",()->new Quest185Item("ward")),TRACKER=ModItems.ITEMS.register("tracker_sense",()->new Quest185Item("tracker")),CALL=ModItems.ITEMS.register("wolf_call",()->new Quest185Item("wolf")),POWDER=ModItems.ITEMS.register("cleansing_powder",CleansingPowder::new);
 public static final DeferredHolder<MobEffect,MobEffect> WARD_EFFECT=ModEffects.EFFECTS.register("mor_ward",()->new MobEffect(MobEffectCategory.BENEFICIAL,0xCBAD75){}),TRACKER_EFFECT=ModEffects.EFFECTS.register("tracker_sense",()->new MobEffect(MobEffectCategory.BENEFICIAL,0x9C2839){});
 public static final DeferredHolder<EntityType<?>,EntityType<WolfSupport>> WOLF=ModEntities.ENTITIES.register("grey_wolf_support",()->EntityType.Builder.<WolfSupport>of(WolfSupport::new,MobCategory.CREATURE).sized(2.2F,3.5F).clientTrackingRange(16).build("slavicmyths:grey_wolf_support"));
 public static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES=net.neoforged.neoforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.PARTICLE_TYPE,"slavicmyths");
 public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>,net.minecraft.core.particles.SimpleParticleType> PURIFY=PARTICLES.register("purify_spark",()->new net.minecraft.core.particles.SimpleParticleType(false){}),GREEN=PARTICLES.register("purify_green_spark",()->new net.minecraft.core.particles.SimpleParticleType(false){}),LAND_WARD=PARTICLES.register("cleansed_land_ward",()->new net.minecraft.core.particles.SimpleParticleType(false){}),INSPECTION=PARTICLES.register("mor_inspection",()->new net.minecraft.core.particles.SimpleParticleType(false){});
 public static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new LinkedHashMap<>();
 static{for(String s:List.of("invitation","teleport_start","teleport_end","yaga_greeting","bayun_comment","wolf_comment","ritual_sample","quest_claim","wolf_call","wolf_arrive","wolf_howl","tracker_on","ward_on","purify"))SOUNDS.put(s,ModSounds.SOUNDS.register("quest185."+s,()->SoundEvent.createFixedRangeEvent(id("quest185."+s),s.equals("wolf_howl")?48:s.equals("wolf_call")?32:12)));}
 public static void sound(net.minecraft.server.level.ServerPlayer p,String s){p.playNotifySound(SOUNDS.get(s).get(),net.minecraft.sounds.SoundSource.PLAYERS,.5f,1);}
 public static void init(net.neoforged.bus.api.IEventBus bus){CleansedLand.IMMUNITY.getId();Quest185Origins.TYPES.register(bus);PARTICLES.register(bus);bus.addListener(Quest185Network::register);bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->e.put(WOLF.get(),org.slavicmyths.wolf.GreyWolf.attributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,50).add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,8).build()));}
 private Quest185Content(){}
}
