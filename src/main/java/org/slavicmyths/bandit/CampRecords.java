package org.slavicmyths.bandit;
import java.util.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
/** Event-only record of fixed camp roster slots. No entity/world scanning or tick handler. */
public final class CampRecords extends SavedData {
    private final Map<UUID,int[]> camps=new HashMap<>();
    public CampRecords(){super();}
    public static synchronized CampRecords get(ServerLevel w){return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(CampRecords::new,(tag,registries)->{CampRecords data=new CampRecords();data.load(tag);return data;}),"slavicmyths_bandit_camps");}
    public synchronized boolean processed(UUID id,int slot){int[] r=camps.get(id);return r!=null&&((r[0]|r[1])&(1<<slot))!=0;}
    public synchronized void spawned(UUID id,int slot,int total){int[] r=camps.computeIfAbsent(id,k->new int[]{0,0,total});r[0]|=1<<slot;setDirty();}
    public synchronized void killed(BanditEntity bandit,Entity killer){int[] r=camps.computeIfAbsent(bandit.camp,k->new int[]{0,0,bandit.campTotal});if(bandit.campMember<0||bandit.campMember>=12)return;r[1]|=1<<bandit.campMember;setDirty();if(r[2]>=7&&r[1]==(1<<r[2])-1&&killer instanceof Player)org.slavicmyths.progression.Knowledge.award((Player)killer,"disperse_bandits");}
    public synchronized void load(CompoundTag tag){camps.clear();for(Tag entry:tag.getList("Camps",10)){CompoundTag c=(CompoundTag)entry;int total=c.getInt("Total");if(c.hasUUID("Id")&&total>=3&&total<=12)camps.put(c.getUUID("Id"),new int[]{c.getInt("Spawned"),c.getInt("Dead"),total});}}
    @Override public synchronized CompoundTag save(CompoundTag tag,net.minecraft.core.HolderLookup.Provider registries){ListTag list=new ListTag();camps.forEach((id,r)->{CompoundTag c=new CompoundTag();c.putUUID("Id",id);c.putInt("Spawned",r[0]);c.putInt("Dead",r[1]);c.putInt("Total",r[2]);list.add(c);});tag.put("Camps",list);return tag;}
}
