package org.slavicmyths.mor;

import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;

/** Version-tolerant, namespaced schema. No vanilla entity data is copied or replaced. */
public record MorSnapshot(int state,int remaining,boolean isClone,boolean summoned,int age,UUID parent,
                          int[] cooldowns,List<UUID> children,Map<UUID,BlockPos> positions,
                          int skill,int castTick,UUID focus,Vec3 aim,Vec3 mark,Vec3 origin,int retry,boolean retried,
                          int createdCount,boolean diveHit,boolean hitSoundPlayed) {
    private static final String P="slavicmyths:mor_";
    public MorSnapshot {state=Math.clamp(state,0,2);remaining=state==1?Math.clamp(remaining,1,50):0;age=Math.clamp(age,0,1800);cooldowns=cooldowns.clone();children=List.copyOf(children.subList(0,Math.min(2,children.size())));positions=Map.copyOf(positions);skill=Math.clamp(skill,0,9);castTick=Math.clamp(castTick,0,42);retry=Math.clamp(retry,0,20);createdCount=Math.clamp(createdCount,0,2);}
    @Override public int[] cooldowns(){return cooldowns.clone();}
    public void write(CompoundTag n){n.putInt(P+"state",state);n.putInt(P+"remaining",remaining);n.putBoolean(P+"is_clone",isClone);n.putBoolean(P+"clones_summoned",summoned);n.putInt(P+"clone_age",age);if(parent!=null)n.putUUID(P+"parent_uuid",parent);n.putIntArray(P+"cooldowns",cooldowns);for(int i=0;i<children.size();i++){UUID id=children.get(i);n.putUUID(P+"child_"+i,id);if(positions.containsKey(id))n.putLong(P+"child_pos_"+i,positions.get(id).asLong());}n.putInt(P+"skill",skill);n.putInt(P+"cast_tick",castTick);if(focus!=null)n.putUUID(P+"focus",focus);vector(n,"aim",aim);vector(n,"mark",mark);vector(n,"origin",origin);n.putInt(P+"retry",retry);n.putBoolean(P+"retried",retried);n.putInt(P+"created_count",createdCount);n.putBoolean(P+"dive_hit",diveHit);n.putBoolean(P+"hit_sound",hitSoundPlayed);}
    public static MorSnapshot read(CompoundTag n){List<UUID> children=new ArrayList<>();Map<UUID,BlockPos> positions=new HashMap<>();for(int i=0;i<2;i++)if(n.hasUUID(P+"child_"+i)){UUID id=n.getUUID(P+"child_"+i);children.add(id);if(n.contains(P+"child_pos_"+i))positions.put(id,BlockPos.of(n.getLong(P+"child_pos_"+i)));}return new MorSnapshot(n.getInt(P+"state"),n.getInt(P+"remaining"),n.getBoolean(P+"is_clone"),n.getBoolean(P+"clones_summoned"),n.getInt(P+"clone_age"),uuid(n,"parent_uuid"),n.getIntArray(P+"cooldowns"),children,positions,n.getInt(P+"skill"),n.getInt(P+"cast_tick"),uuid(n,"focus"),vector(n,"aim"),vector(n,"mark"),vector(n,"origin"),n.getInt(P+"retry"),n.getBoolean(P+"retried"),n.getInt(P+"created_count"),n.getBoolean(P+"dive_hit"),n.getBoolean(P+"hit_sound"));}
    private static UUID uuid(CompoundTag n,String key){return n.hasUUID(P+key)?n.getUUID(P+key):null;}
    private static void vector(CompoundTag n,String key,Vec3 v){n.putDouble(P+key+"_x",v.x);n.putDouble(P+key+"_y",v.y);n.putDouble(P+key+"_z",v.z);}
    private static Vec3 vector(CompoundTag n,String key){double x=n.getDouble(P+key+"_x"),y=n.getDouble(P+key+"_y"),z=n.getDouble(P+key+"_z");return Double.isFinite(x)&&Double.isFinite(y)&&Double.isFinite(z)?new Vec3(x,y,z):Vec3.ZERO;}
}
