package org.slavicmyths.quest185;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;
/** Immunity belongs to this restored block, not future construction at its position. */
public final class CleansedLand {
 public static final DeferredHolder<AttachmentType<?>,AttachmentType<CompoundTag>> IMMUNITY=Quest185Origins.TYPES.register("cleansed_land_immunity",()->AttachmentType.builder(()->new CompoundTag()).serialize(CompoundTag.CODEC).build());
 public static void mark(ServerLevel w,BlockPos p,BlockState state){var c=w.getChunkAt(p);var n=c.getData(IMMUNITY);String key=Long.toString(p.asLong());if(n.size()>=16384&&!n.contains(key))return;var entry=new CompoundTag();entry.putLong("Until",w.getGameTime()+1800);entry.put("State",NbtUtils.writeBlockState(state));entry.putUUID("Generation",java.util.UUID.randomUUID());n.put(key,entry);c.setUnsaved(true);}
 public static boolean protectedAt(ServerLevel w,BlockPos p){return remaining(w,p)>0;}
 public static long remaining(ServerLevel w,BlockPos p){if(!w.hasChunkAt(p))return 0;var c=w.getChunkAt(p);var n=c.getData(IMMUNITY);String key=Long.toString(p.asLong());if(!n.contains(key))return 0;var e=n.getCompound(key);long left=e.getLong("Until")-w.getGameTime();var state=NbtUtils.readBlockState(w.holderLookup(net.minecraft.core.registries.Registries.BLOCK),e.getCompound("State"));if(left<=0||!state.equals(w.getBlockState(p))){n.remove(key);c.setUnsaved(true);return 0;}return left;}
 public static void remove(ServerLevel w,BlockPos p){if(!w.hasChunkAt(p))return;var c=w.getChunkAt(p);if(!c.hasData(IMMUNITY))return;var n=c.getData(IMMUNITY);if(n.contains(Long.toString(p.asLong()))){n.remove(Long.toString(p.asLong()));c.setUnsaved(true);}}
 public static void prune(net.minecraft.world.level.chunk.LevelChunk c){if(!c.hasData(IMMUNITY))return;var n=c.getData(IMMUNITY);long now=c.getLevel().getGameTime();for(var key:java.util.List.copyOf(n.getAllKeys()))if(n.getCompound(key).getLong("Until")<=now){n.remove(key);c.setUnsaved(true);}}
 private CleansedLand(){}
}
