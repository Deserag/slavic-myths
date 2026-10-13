package org.slavicmyths.quest185;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;
/** Chunk-scoped original states, bounded by the chunk's actual converted cells. */
public final class Quest185Origins {
 public static final DeferredRegister<AttachmentType<?>> TYPES=DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES,"slavicmyths");
 public static final DeferredHolder<AttachmentType<?>,AttachmentType<CompoundTag>> ORIGINS=TYPES.register("mor_original_states",()->AttachmentType.builder(()->new CompoundTag()).serialize(CompoundTag.CODEC).build());
 public static void remember(ServerLevel w,BlockPos pos,BlockState original,BlockState infected){if(!w.hasChunkAt(pos))return;var chunk=w.getChunkAt(pos);var n=chunk.getData(ORIGINS);String key=Long.toString(pos.asLong());if(!n.contains(key)&&n.size()>=16384)return;var entry=new CompoundTag();entry.put("Original",NbtUtils.writeBlockState(original));entry.put("Infected",NbtUtils.writeBlockState(infected));n.put(key,entry);chunk.setUnsaved(true);}
 public static BlockState original(ServerLevel w,BlockPos pos,BlockState infected){if(!w.hasChunkAt(pos))return null;var chunk=w.getChunkAt(pos);var n=chunk.getData(ORIGINS);var entry=n.getCompound(Long.toString(pos.asLong()));if(!entry.isEmpty()){var lookup=w.holderLookup(net.minecraft.core.registries.Registries.BLOCK);var expected=NbtUtils.readBlockState(lookup,entry.getCompound("Infected"));if(expected.getBlock()==infected.getBlock()||expected.is(org.slavicmyths.mor.MorBlocks.BLOCKS.get("grass_block").get())&&infected.is(org.slavicmyths.mor.MorBlocks.BLOCKS.get("dirt").get())){var original=NbtUtils.readBlockState(lookup,entry.getCompound("Original"));if(original.getBlock()!=net.minecraft.world.level.block.Blocks.AIR)return original;}}
  return org.slavicmyths.mor.MorBlocks.index(infected)>=0?org.slavicmyths.mor.MorBlocks.prototype(infected):null;
 }
 public static void remove(ServerLevel w,BlockPos pos){if(!w.hasChunkAt(pos))return;var chunk=w.getChunkAt(pos);if(!chunk.hasData(ORIGINS))return;var n=chunk.getData(ORIGINS);n.remove(Long.toString(pos.asLong()));chunk.setUnsaved(true);}
 private record GrassTransition(ServerLevel world,BlockPos pos){}
 private static final ThreadLocal<GrassTransition> GRASS=new ThreadLocal<>();
 public static boolean grassTransition(ServerLevel w,BlockPos pos){var t=GRASS.get();return t!=null&&t.world==w&&t.pos.equals(pos);}
 public static void grassTick(ServerLevel w,BlockPos pos,Runnable tick){var old=GRASS.get();GRASS.set(new GrassTransition(w,pos.immutable()));try{tick.run();}finally{if(old==null)GRASS.remove();else GRASS.set(old);}}
 private Quest185Origins(){}
}
