package org.slavicmyths.guardians;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class GuardianContent {
 public static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
 public static final DeferredHolder<EntityType<?>,EntityType<FireGuardian>> FIRE=ModEntities.ENTITIES.register("extinguished_flame_guardian",()->EntityType.Builder.of(FireGuardian::new,MobCategory.MONSTER).sized(1.5f,3.6f).clientTrackingRange(12).updateInterval(2).build("slavicmyths:extinguished_flame_guardian"));
 public static final DeferredHolder<EntityType<?>,EntityType<SpringKeeper>> WATER=ModEntities.ENTITIES.register("defiled_spring_keeper",()->EntityType.Builder.of(SpringKeeper::new,MobCategory.MONSTER).sized(1f,3f).clientTrackingRange(12).updateInterval(2).build("slavicmyths:defiled_spring_keeper"));
 public static final DeferredHolder<EntityType<?>,EntityType<WaterEcho>> ECHO=ModEntities.ENTITIES.register("guardian_water_echo",()->EntityType.Builder.of(WaterEcho::new,MobCategory.MONSTER).sized(.65f,1.4f).noSave().clientTrackingRange(10).build("slavicmyths:guardian_water_echo"));
 public static final DeferredHolder<EntityType<?>,EntityType<WaterPrison>> PRISON=ModEntities.ENTITIES.register("guardian_water_prison",()->EntityType.Builder.<WaterPrison>of(WaterPrison::new,MobCategory.MISC).sized(2f,2.8f).noSave().clientTrackingRange(10).updateInterval(2).build("slavicmyths:guardian_water_prison"));
 public static final DeferredHolder<Item,Item> FIRE_FRAGMENT=ModItems.ITEMS.register("mor_fire_amulet_fragment",Fragment::new),WATER_FRAGMENT=ModItems.ITEMS.register("mor_water_amulet_fragment",Fragment::new);
 public static final DeferredHolder<Block,GuardianNodeBlock> PEDESTAL=ModBlocks.BLOCKS.register("guardian_relic_pedestal",()->new GuardianNodeBlock(false)),VALVE=ModBlocks.BLOCKS.register("spring_valve",()->new GuardianNodeBlock(true));
 public static final DeferredHolder<Item,Item> PEDESTAL_ITEM=ModItems.ITEMS.register("guardian_relic_pedestal",()->new BlockItem(PEDESTAL.get(),new Item.Properties())),VALVE_ITEM=ModItems.ITEMS.register("spring_valve",()->new BlockItem(VALVE.get(),new Item.Properties()));
 public static final net.neoforged.neoforge.registries.DeferredRegister<BlockEntityType<?>> BES=net.neoforged.neoforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE,"slavicmyths");
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<GuardianNode>> NODE=BES.register("guardian_node",()->BlockEntityType.Builder.of(GuardianNode::new,PEDESTAL.get(),VALVE.get()).build(null));
 public static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new LinkedHashMap<>();
 static {for(String s:List.of("fire_guardian_idle","fire_guardian_awaken","fire_guardian_step","fire_guardian_hurt","fire_guardian_swing","fire_pulse_charge","fire_pulse_burst","fire_lockdown_warning","fire_lockdown_ignite","fire_shield_break","fire_guardian_ground_impact","fire_guardian_purify","spring_keeper_idle","spring_keeper_emerge","spring_keeper_step","spring_keeper_hurt","spring_whip_windup","spring_whip_impact","spring_whirlpool_warning","spring_whirlpool_loop","spring_prison_form","spring_prison_break","spring_flood_rise","spring_heal_begin","spring_heal_interrupt","spring_valve_switch","spring_keeper_purify","amulet_fragment_claim"))SOUNDS.put(s,ModSounds.SOUNDS.register("guardians."+s,()->SoundEvent.createFixedRangeEvent(id("guardians."+s),24)));}
 public static void init(net.neoforged.bus.api.IEventBus bus){BES.register(bus);bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->{e.put(FIRE.get(),GuardianEntity.attributes(false).build());e.put(WATER.get(),GuardianEntity.attributes(true).build());e.put(ECHO.get(),WaterEcho.attributes().build());e.put(PRISON.get(),WaterPrison.attributes().build());});}
 public static net.minecraft.world.damagesource.DamageSource fireDamage(GuardianEntity e){var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,id("guardian_fire"));return new net.minecraft.world.damagesource.DamageSource(e.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(key),e,e);}
 public static net.minecraft.network.chat.MutableComponent text(String s,Object...args){return net.minecraft.network.chat.Component.translatable("guardians194."+s,args);}
 public static void sound(net.minecraft.world.level.Level w,net.minecraft.core.BlockPos p,String s){w.playSound(null,p,SOUNDS.get(s).get(),s.contains("idle")?SoundSource.AMBIENT:s.contains("valve")||s.contains("claim")?SoundSource.BLOCKS:SoundSource.HOSTILE,s.contains("idle")?.16f:.65f,1);}
 public static final class Fragment extends Item {public Fragment(){super(new Properties().stacksTo(1).rarity(Rarity.EPIC));}@Override public void appendHoverText(ItemStack s,TooltipContext c,List<net.minecraft.network.chat.Component> lines,TooltipFlag f){lines.add(net.minecraft.network.chat.Component.translatable(getDescriptionId()+".desc"));}}
 private GuardianContent(){}
}
