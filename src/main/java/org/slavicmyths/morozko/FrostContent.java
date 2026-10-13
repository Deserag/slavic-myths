package org.slavicmyths.morozko;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class FrostContent {
 public static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
 public static final DeferredHolder<EntityType<?>,EntityType<Morozko>> BOSS=ModEntities.ENTITIES.register("morozko",()->EntityType.Builder.of(Morozko::new,MobCategory.MONSTER).sized(1.35f,3.15f).clientTrackingRange(16).build("slavicmyths:morozko"));
 public static final DeferredHolder<EntityType<?>,EntityType<IceDouble>> DOUBLE=ModEntities.ENTITIES.register("morozko_ice_double",()->EntityType.Builder.of(IceDouble::new,MobCategory.MONSTER).sized(1.35f,3.15f).clientTrackingRange(16).build("slavicmyths:morozko_ice_double"));
 public static final DeferredHolder<EntityType<?>,EntityType<PlayerDouble>> MIRROR_ENTITY=ModEntities.ENTITIES.register("player_ice_double",()->EntityType.Builder.of(PlayerDouble::new,MobCategory.CREATURE).sized(.6f,1.8f).noSave().clientTrackingRange(16).build("slavicmyths:player_ice_double"));
 public static final DeferredHolder<EntityType<?>,EntityType<FrostShard>> SHARD=ModEntities.ENTITIES.register("morozko_ice_bolt",()->EntityType.Builder.<FrostShard>of(FrostShard::new,MobCategory.MISC).sized(.25f,.4f).noSave().clientTrackingRange(16).updateInterval(1).build("slavicmyths:morozko_ice_bolt"));
 public static final DeferredHolder<EntityType<?>,EntityType<FrostHazard>> HAZARD=ModEntities.ENTITIES.register("morozko_icicle",()->EntityType.Builder.<FrostHazard>of(FrostHazard::new,MobCategory.MISC).sized(.65f,2.4f).noSave().clientTrackingRange(16).updateInterval(1).build("slavicmyths:morozko_icicle"));
 public static final DeferredHolder<Item,Item> TOTEM=ModItems.ITEMS.register("morozko_trial_totem",TrialTotem::new),MIRROR=ModItems.ITEMS.register("frost_mirror",()->new org.slavicmyths.item.FolkAccessoryItem(new Item.Properties().rarity(Rarity.EPIC),"necklace")),RESISTANCE=ModItems.ITEMS.register("frost_resistance_rune",()->new FrostItem()),RUNE=ModItems.ITEMS.register("frost_rune",()->new FrostItem()),HEART=ModItems.ITEMS.register("heart_of_eternal_cold",()->new FrostItem()),STAFF=ModItems.ITEMS.register("morozko_staff",FrostStaff::new);
 public static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new LinkedHashMap<>();
 static {for(String name:List.of("ambient","step","staff_hit","ice_bolt","blizzard_start","blizzard_loop","spike_telegraph","spike_break","breath","wave","clone_spawn","clone_break","frost_damage","yield","mirror_summon","mirror_break","staff_shoot","staff_stream","staff_icicles","staff_wave","trial_call"))SOUNDS.put(name,ModSounds.SOUNDS.register("morozko."+name,()->SoundEvent.createFixedRangeEvent(id("morozko."+name),name.contains("blizzard")||name.equals("yield")?32:24)));}
 public static void init(net.neoforged.bus.api.IEventBus bus){bus.addListener(FrostNetwork::register);bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->{e.put(BOSS.get(),Morozko.attributes().build());e.put(DOUBLE.get(),Morozko.attributes().add(Attributes.MAX_HEALTH,FrostRules.DOUBLE_HP).build());e.put(MIRROR_ENTITY.get(),net.minecraft.world.entity.PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH,FrostRules.MIRROR_HP).add(Attributes.MOVEMENT_SPEED,.3).add(Attributes.ATTACK_DAMAGE,1).build());});}
 public static void sound(Entity e,String s){e.level().playSound(null,e.blockPosition(),SOUNDS.get(s).get(),net.minecraft.sounds.SoundSource.HOSTILE,s.equals("ambient")?.25f:.6f,1);}
 public static net.minecraft.network.chat.MutableComponent text(String s,Object...args){return net.minecraft.network.chat.Component.translatable("frost191."+s,args);}
 static class FrostItem extends Item{FrostItem(){super(new Properties().rarity(Rarity.RARE));}@Override public void appendHoverText(ItemStack s,TooltipContext c,List<net.minecraft.network.chat.Component> l,TooltipFlag f){l.add(net.minecraft.network.chat.Component.translatable(getDescriptionId()+".desc"));}}
 private FrostContent(){}
}
