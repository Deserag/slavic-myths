package org.slavicmyths.textile;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import org.slavicmyths.registry.*;

public final class Textiles {
    public static final Map<String,DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
    public static final Map<String,DeferredHolder<Block,Block>> BLOCKS=new LinkedHashMap<>();
    public static final DeferredHolder<EntityType<?>,EntityType<Carcass>> CARCASS=ModEntities.ENTITIES.register("carcass",()->EntityType.Builder.of(Carcass::new,MobCategory.MISC).sized(1.2F,.45F).clientTrackingRange(8).build("slavicmyths:carcass"));
    public static final DeferredHolder<MobEffect,MobEffect> RESTED=ModEffects.EFFECTS.register("well_rested",()->new MobEffect(MobEffectCategory.BENEFICIAL,0xe3d5b3){}.addAttributeModifier(Attributes.MOVEMENT_SPEED,id("well_rested_speed"),.05,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL).addAttributeModifier(Attributes.BLOCK_BREAK_SPEED,id("well_rested_mining"),.10,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<TextileStation>> STATION=ModTiles.TILES.register("textile_station",()->BlockEntityType.Builder.of(TextileStation::new,BLOCKS.get("flax_breaker").get(),BLOCKS.get("spinning_wheel").get(),BLOCKS.get("loom_table").get()).build(null));
    public static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("slavicmyths",path);}
    public static Item item(String id){if(id.equals("flax_stalk"))return ModItems.FLAX_STALK.get();if(id.equals("linen_thread"))return ModItems.LINEN_THREAD.get();if(id.equals("linen_cloth"))return ModItems.LINEN_CLOTH.get();return ITEMS.get(id).get();}
    private static void register(String id,java.util.function.Supplier<Item> factory){ITEMS.put(id,ModItems.ITEMS.register(id,factory));}
    public static void init(IEventBus bus){
        for(String s:List.of("animal_fat","goat_hide","flax_fiber"))register(s,()->new Item(new Item.Properties()));
        register("butchering_knife",()->new SwordItem(Tiers.IRON,new Item.Properties().durability(250).attributes(SwordItem.createAttributes(Tiers.IRON,0,-2.2F))));
        for(String s:List.of("linen_shirt","linen_ports","sarafan","linen_headscarf","linen_apron","folk_vest","bast_shoes")){
            EquipmentSlot slot=s.equals("linen_ports")?EquipmentSlot.LEGS:s.equals("linen_headscarf")?EquipmentSlot.HEAD:s.equals("bast_shoes")?EquipmentSlot.FEET:EquipmentSlot.CHEST;
            register(s,()->new ClothingItem(slot));
        }
        register("woven_belt",BeltItem::new);
        BLOCKS.put("flax_breaker",ModBlocks.BLOCKS.register("flax_breaker",StationBlock.Breaker::new));
        BLOCKS.put("spinning_wheel",ModBlocks.BLOCKS.register("spinning_wheel",StationBlock.Wheel::new));
        BLOCKS.put("loom_table",ModBlocks.BLOCKS.register("loom_table",StationBlock.Loom::new));
        BLOCKS.put("tallow_candle",ModBlocks.BLOCKS.register("tallow_candle",()->new CandleBlock(Block.Properties.ofFullCopy(Blocks.CANDLE).lightLevel(CandleBlock.LIGHT_EMISSION))));
        BLOCKS.put("linen_bed",ModBlocks.BLOCKS.register("linen_bed",LinenBed::new));
        BLOCKS.forEach((s,b)->register(s,()->s.equals("linen_bed")?new BedItem(b.get(),new Item.Properties().stacksTo(1)):new BlockItem(b.get(),new Item.Properties())));
        BeltData.TYPES.register(bus);bus.addListener(BeltData::network);
    }
    private Textiles(){}
}
