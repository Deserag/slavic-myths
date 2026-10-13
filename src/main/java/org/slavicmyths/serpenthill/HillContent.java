package org.slavicmyths.serpenthill;
import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.item.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class HillContent {
 public static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
 public static final Map<String,DeferredHolder<Block,HillBlock>> BLOCKS=new LinkedHashMap<>();
 public static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new LinkedHashMap<>();
 static {for(String name:List.of("serpent_masonry","cracked_serpent_masonry","ruined_serpent_masonry","serpent_bas_relief","ancient_ritual_slab","defiled_ritual_slab","serpent_torch","ritual_brazier","defiled_ritual_brazier","ancient_hanging_lamp","mor_crystal")){int light=name.equals("serpent_bas_relief")?2:name.equals("serpent_torch")?12:name.equals("ritual_brazier")?15:name.equals("defiled_ritual_brazier")?10:name.equals("ancient_hanging_lamp")?12:name.equals("mor_crystal")?7:name.equals("defiled_ritual_slab")?4:0;var holder=ModBlocks.BLOCKS.register(name,()->new HillBlock(name,light));BLOCKS.put(name,holder);ModItems.ITEMS.register(name,()->new BlockItem(holder.get(),new Item.Properties()));}for(String sound:List.of("danger","crystal","brazier","deep_ambient","raised"))SOUNDS.put(sound,ModSounds.SOUNDS.register("serpenthill."+sound,()->SoundEvent.createFixedRangeEvent(id("serpenthill."+sound),24)));}
 public static final DeferredHolder<StructureType<?>,StructureType<SerpentHillStructure>> STRUCTURE=org.slavicmyths.swamp.SwampStructures.STRUCTURES.register("serpent_hill",()->()->SerpentHillStructure.CODEC);
 public static final DeferredHolder<net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType,net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType> PIECE=org.slavicmyths.swamp.SwampStructures.PIECES.register("serpent_hill_piece",()->(context,tag)->new SerpentHillPiece(tag));
 public static BlockStateCache palette=new BlockStateCache();
 public static void init(){}
 public static net.minecraft.network.chat.MutableComponent text(String s,Object... a){return net.minecraft.network.chat.Component.translatable("serpenthill195."+s,a);}
 public static final class BlockStateCache {
  private final Map<String,net.minecraft.world.level.block.state.BlockState> cache=new java.util.concurrent.ConcurrentHashMap<>();
  public net.minecraft.world.level.block.state.BlockState state(String key){return cache.computeIfAbsent(key,k->{String[] parts=k.split("@");String name=parts[0];var state=(name.startsWith("slavicmyths:")&&BLOCKS.containsKey(name.substring(12))?BLOCKS.get(name.substring(12)).get():net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(ResourceLocation.parse(name))).defaultBlockState();if(parts.length>1){var face=net.minecraft.core.Direction.valueOf(parts[1]);if(state.hasProperty(HillBlock.FACING))state=state.setValue(HillBlock.FACING,face);else if(state.hasProperty(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING))state=state.setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,face);}if(parts.length>2&&state.hasProperty(HillBlock.HANGING))state=state.setValue(HillBlock.HANGING,Boolean.parseBoolean(parts[2]));return state;});}
 }
 private HillContent(){}
}
