package org.slavicmyths.sanctuary;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
import org.slavicmyths.swamp.SwampStructures;
public final class SanctuaryContent {
 public static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}public static net.minecraft.network.chat.MutableComponent text(String s,Object...a){return net.minecraft.network.chat.Component.translatable("sanctuary193."+s,a);}
 public static final DeferredHolder<StructureType<?>,StructureType<SanctuaryStructure>> FIRE=SwampStructures.STRUCTURES.register("sanctuary_unfading_fire",()->()->SanctuaryStructure.codec(false)),SPRING=SwampStructures.STRUCTURES.register("abode_pure_spring",()->()->SanctuaryStructure.codec(true));
 public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE=SwampStructures.PIECES.register("sanctuary_piece",()->(c,n)->new SanctuaryPiece(c.structureTemplateManager(),n));
 public static final Map<String,DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();public static final Map<String,DeferredHolder<Block,? extends Block>> BLOCKS=new LinkedHashMap<>();
 static{for(String s:List.of("sacred_ember","purifying_ash","flame_vial","fading_flame_charm","ancient_rune_fragment","pure_spring_drop","purified_water_bottle","spring_tear","purification_stone"))ITEMS.put(s,ModItems.ITEMS.register(s,()->new SanctuaryItem(s)));
  for(String s:List.of("unfading_brazier","pure_spring_heart","tainted_spring_node","seal_fire_door","seal_spring_door","carved_fire_pillar","carved_spring_pillar","fire_idol_sun","fire_idol_tree","fire_idol_circle")){var block=ModBlocks.BLOCKS.<Block>register(s,()->Set.of("unfading_brazier","pure_spring_heart","tainted_spring_node").contains(s)?new SanctuaryBlock(s):new SanctuaryDecor(s));BLOCKS.put(s,block);ModItems.ITEMS.register(s,()->new BlockItem(block.get(),new Item.Properties()));}}
 public static final DeferredHolder<Block,Block> CACHE=ModBlocks.BLOCKS.register("sanctuary_ritual_cache",()->new SanctuaryChestBlock());
 public static final DeferredHolder<Item,Item> CACHE_ITEM=ModItems.ITEMS.register("sanctuary_ritual_cache",()->new BlockItem(CACHE.get(),new Item.Properties()));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SanctuaryHeart>> HEART=ModTiles.TILES.register("sanctuary_heart",()->BlockEntityType.Builder.of(SanctuaryHeart::new,block("unfading_brazier"),block("pure_spring_heart"),block("tainted_spring_node")).build(null));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SanctuaryChest>> CHEST=ModTiles.TILES.register("sanctuary_chest",()->BlockEntityType.Builder.of(SanctuaryChest::new,CACHE.get()).build(null));
 public static final DeferredHolder<MobEffect,MobEffect> GUARD=ModEffects.EFFECTS.register("sanctuary_flame_guard",()->new MobEffect(MobEffectCategory.BENEFICIAL,0xD28D42){}),TEAR=ModEffects.EFFECTS.register("spring_tear_healing",()->new MobEffect(MobEffectCategory.BENEFICIAL,0x78D7E8){@Override public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){return SanctuaryRules.healTick(duration);}@Override public boolean applyEffectTick(LivingEntity e,int amp){if(!e.level().isClientSide)e.heal(1);return true;}});
 public static final DeferredHolder<EntityType<?>,EntityType<FlameVial>> VIAL=ModEntities.ENTITIES.register("flame_vial",()->EntityType.Builder.<FlameVial>of(FlameVial::new,MobCategory.MISC).sized(.25f,.25f).noSave().clientTrackingRange(8).build("slavicmyths:flame_vial"));
 public static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new LinkedHashMap<>();
 static{for(String s:List.of("fire_ambient","fire_restore","fire_seal","fire_relic_hint","spring_ambient","spring_channel","spring_purify","spring_tainted","door_blocked","vial_throw","vial_hit","water_use","tear_use"))SOUNDS.put(s,ModSounds.SOUNDS.register("sanctuary193."+s,()->SoundEvent.createFixedRangeEvent(id("sanctuary193."+s),20)));}
 public static Block block(String s){return BLOCKS.get(s).get();}public static Item item(String s){return ITEMS.get(s).get();}
 public static void init(net.neoforged.bus.api.IEventBus bus){}
 public static void sound(net.minecraft.world.level.Level w,net.minecraft.core.BlockPos p,String s){w.playSound(null,p,SOUNDS.get(s).get(),net.minecraft.sounds.SoundSource.BLOCKS,.45f,1);}
 private SanctuaryContent(){}
}
