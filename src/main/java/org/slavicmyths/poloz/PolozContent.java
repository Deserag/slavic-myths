package org.slavicmyths.poloz;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class PolozContent {
 public static ResourceLocation id(String n){return ResourceLocation.fromNamespaceAndPath("slavicmyths",n);}
 public static final DeferredHolder<EntityType<?>,EntityType<GreatPoloz>> BOSS=ModEntities.ENTITIES.register("great_poloz",()->EntityType.Builder.<GreatPoloz>of(GreatPoloz::new,MobCategory.MONSTER).sized(3.5F,3).fireImmune().clientTrackingRange(16).updateInterval(4).build("slavicmyths:great_poloz"));
 public static final DeferredHolder<EntityType<?>,EntityType<PolozGlob>> GLOB=ModEntities.ENTITIES.register("poloz_glob",()->EntityType.Builder.<PolozGlob>of(PolozGlob::new,MobCategory.MISC).sized(.4F,.4F).clientTrackingRange(8).updateInterval(4).build("slavicmyths:poloz_glob"));
 public static final DeferredHolder<EntityType<?>,EntityType<PolozStoneSpike>> SPIKE=ModEntities.ENTITIES.register("poloz_stone_spike",()->EntityType.Builder.<PolozStoneSpike>of(PolozStoneSpike::new,MobCategory.MISC).sized(.7F,2).clientTrackingRange(8).updateInterval(1).build("slavicmyths:poloz_stone_spike"));
 public static final DeferredHolder<Item,Item> DUST=ModItems.ITEMS.register("mor_dust",()->new PolozRelic("mor_dust",new Item.Properties())),EYE=ModItems.ITEMS.register("eye_of_mor",()->new PolozRelic("eye_of_mor",new Item.Properties().stacksTo(16))),HEART=ModItems.ITEMS.register("ashen_heart",()->new PolozRelic("ashen_heart",new Item.Properties().stacksTo(16))),CORE=ModItems.ITEMS.register("poloz_core",()->new PolozRelic("poloz_core",new Item.Properties().stacksTo(1))),SCALE=ModItems.ITEMS.register("poloz_scale",()->new PolozRelic("poloz_scale",new Item.Properties())),HORN=ModItems.ITEMS.register("mor_horn",()->new MorHorn(new Item.Properties().stacksTo(1)));
 public static final String[] SOUND_IDS={"mor_horn_call","mor_horn_charge","poloz_distant_reply","poloz_earth_rumble","poloz_ambient_rumble","poloz_surface_roar","poloz_move_crush","poloz_armor_deflect","poloz_weakspot_hit","poloz_tail_slam","poloz_mor_rain","poloz_burrow","poloz_emerge","poloz_phase2_cracks","poloz_phase3_roar","poloz_minion_call","poloz_death"};
 private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new HashMap<>();
 static {for(String s:SOUND_IDS)SOUNDS.put(s,ModSounds.SOUNDS.register(s,()->SoundEvent.createVariableRangeEvent(id(s))));}
 public static SoundEvent sound(String s){return SOUNDS.get(s).get();}
 public static void init() {PolozRewards.init();PolozTrophy.init();org.slavicmyths.mor.MorGrowth.init();}
}
