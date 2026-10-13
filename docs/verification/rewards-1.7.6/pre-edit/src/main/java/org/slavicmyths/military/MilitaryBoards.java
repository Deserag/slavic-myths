package org.slavicmyths.military;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
/** Board offers are region/day records, so destruction/reopen does not roll targets or rewards. */
public final class MilitaryBoards extends SavedData {
 private final Map<String,ListTag> boards=new HashMap<>();
 public static List<CompoundTag> offers(ServerPlayer p,BlockPos pos){var l=p.serverLevel();var s=l.getDataStorage().computeIfAbsent(new Factory<>(MilitaryBoards::new,(n,r)->{var state=new MilitaryBoards();for(Tag t:n.getList("Boards",10)){var q=(CompoundTag)t;state.boards.put(q.getString("Key"),q.getList("Offers",10));}return state;}),"slavicmyths_military_boards");long day=Math.floorDiv(l.getDayTime(),24000);String key=net.minecraft.world.level.ChunkPos.asLong(pos.getX()>>6,pos.getZ()>>6)+":"+day;var board=s.boards.get(key);if(board==null){board=new ListTag();for(var q:MilitaryQuests.generateOffers(p,pos))board.add(q.copy());s.boards.put(key,board);s.boards.keySet().removeIf(k->{long d=Long.parseLong(k.substring(k.lastIndexOf(':')+1));return d<day-2;});s.setDirty();}List<CompoundTag> result=new ArrayList<>();for(Tag t:board)result.add(((CompoundTag)t).copy());return result;}
 public CompoundTag save(CompoundTag n,HolderLookup.Provider r){ListTag all=new ListTag();boards.forEach((key,offers)->{CompoundTag q=new CompoundTag();q.putString("Key",key);q.put("Offers",offers.copy());all.add(q);});n.put("Boards",all);return n;}
}
