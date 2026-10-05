package org.slavicmyths.kurgan;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.core.BlockPos;

public final class KurganInstance {
    public final UUID id; public final BlockPos origin;public final KurganPlan plan;
    public final Map<Integer,KurganEncounterState> encounters=new HashMap<>();public boolean bossDefeated;
    public int disturbance,thresholds;public boolean sealOpened;public final Set<String> fired=new HashSet<>();
    public KurganInstance(UUID id,BlockPos origin,KurganPlan plan){this.id=id;this.origin=origin.immutable();this.plan=plan;}
    public boolean record(String source,int amount){if(!fired.add(source))return false;disturbance=Math.min(100,disturbance+Math.max(0,amount));return true;}
    public boolean contains(BlockPos p){return plan.bounds().contains(p.getX()-origin.getX(),p.getY()-origin.getY(),p.getZ()-origin.getZ());}
    public KurganPlan.Room room(BlockPos p){for(KurganPlan.Room r:plan.rooms)if(r.box().contains(p.getX()-origin.getX(),p.getY()-origin.getY(),p.getZ()-origin.getZ()))return r;return null;}
    public CompoundTag save(){CompoundTag n=new CompoundTag();n.putUUID("Id",id);n.putLong("Origin",origin.asLong());n.put("Plan",KurganPlanNbt.write(plan));n.putBoolean("BossDefeated",bossDefeated);CompoundTag ec=new CompoundTag();for(Map.Entry<Integer,KurganEncounterState> e:encounters.entrySet())ec.put(Integer.toString(e.getKey()),e.getValue().save());n.put("Encounters",ec);n.putInt("Disturbance",disturbance);n.putInt("Thresholds",thresholds);n.putBoolean("SealOpened",sealOpened);ListTag list=new ListTag();for(String s:fired)list.add(StringTag.valueOf(s));n.put("Fired",list);return n;}
    public static KurganInstance load(CompoundTag n){KurganInstance i=new KurganInstance(n.getUUID("Id"),BlockPos.of(n.getLong("Origin")),KurganPlanNbt.read(n.getCompound("Plan")));i.bossDefeated=n.getBoolean("BossDefeated");CompoundTag ec=n.getCompound("Encounters");for(String key:ec.getAllKeys())i.encounters.put(Integer.parseInt(key),KurganEncounterState.load(ec.getCompound(key)));i.disturbance=Math.max(0,Math.min(100,n.getInt("Disturbance")));i.thresholds=n.getInt("Thresholds");i.sealOpened=n.getBoolean("SealOpened");for(Tag s:n.getList("Fired",8))i.fired.add(s.getAsString());return i;}
}
