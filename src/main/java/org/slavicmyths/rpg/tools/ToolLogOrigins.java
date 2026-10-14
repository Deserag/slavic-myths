package org.slavicmyths.rpg.tools;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent placed/moved-log deny list; never evict a marker and silently turn a house into a tree. */
public final class ToolLogOrigins extends SavedData {
 public static final int MAX=65536;
 private final Set<Long> placed=new HashSet<>(),unsafeChunks=new HashSet<>();private boolean disabled;private CompoundTag root=new CompoundTag();
 public static ToolLogOrigins get(ServerLevel level){return level.getDataStorage().computeIfAbsent(new Factory<>(ToolLogOrigins::new,ToolLogOrigins::load),"slavicmyths_tool_log_origins");}
 public boolean blocked(BlockPos p){return disabled||placed.contains(p.asLong())||unsafeChunks.contains(net.minecraft.world.level.ChunkPos.asLong(p.getX()>>4,p.getZ()>>4));}
 public void mark(BlockPos p){if(disabled)return;if(placed.size()>=MAX&&!placed.contains(p.asLong())){disabled=true;setDirty();return;}if(placed.add(p.asLong()))setDirty();}
 public void denyChunk(int x,int z){if(disabled)return;if(unsafeChunks.size()>=4096){disabled=true;setDirty();return;}if(unsafeChunks.add(net.minecraft.world.level.ChunkPos.asLong(x,z)))setDirty();}
 public boolean disabled(){return disabled;}
 public static ToolLogOrigins load(CompoundTag n,HolderLookup.Provider lookup){var d=new ToolLogOrigins();d.root=n.copy();d.disabled=n.getBoolean("Disabled")||n.contains("Version")&&!n.contains("Version",3)||n.getInt("Version")<0||n.getInt("Version")>1||n.contains("Placed")&&!n.contains("Placed",12);var a=n.getLongArray("Placed");if(a.length>MAX)d.disabled=true;else for(long p:a)d.placed.add(p);var chunks=n.getLongArray("UnsafeChunks");if(chunks.length>4096||n.contains("UnsafeChunks")&&!n.contains("UnsafeChunks",12))d.disabled=true;else for(long c:chunks)d.unsafeChunks.add(c);return d;}
 @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider lookup){var out=root.copy();if(out.getInt("Version")>1)return out;out.putLongArray("UnsafeChunks",unsafeChunks.stream().mapToLong(Long::longValue).sorted().toArray());out.putInt("Version",1);out.putBoolean("Disabled",disabled);out.putLongArray("Placed",placed.stream().mapToLong(Long::longValue).sorted().toArray());return out;}
}
