package org.slavicmyths.karachun;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class NightContent {
 public static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
 public static final DeferredHolder<EntityType<?>,EntityType<Karachun>> BOSS=ModEntities.ENTITIES.register("karachun",()->EntityType.Builder.of(Karachun::new,MobCategory.MONSTER).sized(1.5f,3.95f).clientTrackingRange(16).build("slavicmyths:karachun"));
 public static final DeferredHolder<EntityType<?>,EntityType<NightRaven>> RAVEN=ModEntities.ENTITIES.register("karachun_raven",()->EntityType.Builder.of(NightRaven::new,MobCategory.CREATURE).sized(.6f,.65f).noSave().clientTrackingRange(12).updateInterval(2).build("slavicmyths:karachun_raven"));
 public static final DeferredHolder<EntityType<?>,EntityType<NightBlade>> BLADE=ModEntities.ENTITIES.register("karachun_blade",()->EntityType.Builder.<NightBlade>of(NightBlade::new,MobCategory.MISC).sized(.7f,.7f).noSave().clientTrackingRange(12).updateInterval(1).build("slavicmyths:karachun_blade"));
 public static final DeferredHolder<Item,Item> TOTEM=ModItems.ITEMS.register("karachun_trial_totem",NightTotem::new),SCYTHE=ModItems.ITEMS.register("karachun_scythe",NightScythe::new),RUNE=ModItems.ITEMS.register("rune_of_waning",NightItem::new),SHARD=ModItems.ITEMS.register("shard_of_last_night",NightItem::new),WING=ModItems.ITEMS.register("black_wing_amulet",()->new org.slavicmyths.item.FolkAccessoryItem(new Item.Properties().rarity(Rarity.EPIC),"necklace"));
 public static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new LinkedHashMap<>();
 static {for(String s:List.of("ambient_low_wind","enter_trial","voice_whisper","scythe_windup","scythe_hit","dark_frost","long_night_cast","touch_hunger","touch_frost","touch_death","last_hour_mark","last_hour_impact","fear_phantom_spawn","fear_phantom_dispel","yield","raven_call","raven_flap","raven_dive","raven_hit","life_eater","last_breath","black_harvest","wing_activate","waning_stage_up"))SOUNDS.put(s,ModSounds.SOUNDS.register("karachun."+s,()->SoundEvent.createFixedRangeEvent(id("karachun."+s),32)));}
 public static void init(net.neoforged.bus.api.IEventBus b){b.addListener(NightNetwork::register);b.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->{e.put(BOSS.get(),Karachun.attributes().build());e.put(RAVEN.get(),NightRaven.attributes().build());});}
 public static net.minecraft.network.chat.MutableComponent text(String s,Object...a){return net.minecraft.network.chat.Component.translatable("night192."+s,a);}
 public static void sound(Entity e,String s){e.level().playSound(null,e.blockPosition(),SOUNDS.get(s).get(),net.minecraft.sounds.SoundSource.HOSTILE,s.contains("ambient")?.22f:.65f,.94f+e.getRandom().nextFloat()*.12f);}
 public static class NightItem extends Item {public NightItem(){super(new Properties().rarity(Rarity.RARE));}@Override public void appendHoverText(ItemStack s,TooltipContext c,List<net.minecraft.network.chat.Component> t,TooltipFlag f){t.add(net.minecraft.network.chat.Component.translatable(getDescriptionId()+".desc"));}}
 private NightContent(){}
}
