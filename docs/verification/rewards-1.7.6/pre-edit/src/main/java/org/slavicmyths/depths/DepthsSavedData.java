package org.slavicmyths.depths;

import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Only compact instance/phase metadata is persistent; block blueprints are rebuilt deterministically. */
public final class DepthsSavedData extends SavedData {
    public static final class Entry {
        public DepthsPlan plan;
        public UUID player;
        public String status="PENDING",phase="BLOCKS",source="COMMAND";
        public int cursor,mobs,chests,mapId=-1;
        public boolean rewardDelivered;
        public Entry(DepthsPlan plan,UUID player,String source){this.plan=plan;this.player=player;this.source=source;}
        public CompoundTag save(){
            var n=new CompoundTag();n.putUUID("InstanceId",plan.id());n.putUUID("StartedBy",player);n.putString("Status",status);n.putString("Phase",phase);n.putString("Source",source);
            n.putString("Dimension","minecraft:overworld");n.putIntArray("Origin",point(plan.origin()));n.putIntArray("PlayerStart",point(plan.start()));n.putInt("Direction",plan.direction());
            n.putLong("GenerationSeed",plan.seed());n.putString("TopVariant",plan.skylight()?"SKYLIGHT":"CLOSED");n.putInt("SkyTop",plan.skyTop());
            var b=plan.bounds();n.putIntArray("Bounds",new int[]{b.x0(),b.y0(),b.z0(),b.x1(),b.y1(),b.z1()});n.putIntArray("BossAnchor",point(plan.bossAnchor()));
            var area=plan.bossArea();n.putIntArray("BossArea",new int[]{area.x0(),area.y0(),area.z0(),area.x1(),area.y1(),area.z1()});
            n.putInt("Cursor",cursor);n.putInt("MobBits",mobs);n.putInt("ChestBits",chests);n.putBoolean("MobsInitialized",mobs==4095);n.putBoolean("LootInitialized",chests==3);
            n.putInt("MapId",mapId);n.putBoolean("RewardDelivered",rewardDelivered);return n;
        }
        public static Entry load(CompoundTag n){
            var plan=new DepthsPlan(n.getUUID("InstanceId"),point(n.getIntArray("PlayerStart")),point(n.getIntArray("Origin")),n.getInt("Direction"),n.getLong("GenerationSeed"),n.getString("TopVariant").equals("SKYLIGHT"),n.getInt("SkyTop"));
            var e=new Entry(plan,n.getUUID("StartedBy"),n.getString("Source"));e.status=n.getString("Status");e.phase=n.getString("Phase");e.cursor=n.getInt("Cursor");
            e.mobs=n.getInt("MobBits");e.chests=n.getInt("ChestBits");e.mapId=n.contains("MapId")?n.getInt("MapId"):-1;e.rewardDelivered=n.getBoolean("RewardDelivered");return e;
        }
    }
    public final Map<UUID,Entry> entries=new LinkedHashMap<>();
    private static int[] point(DepthsPlan.Point p){return new int[]{p.x(),p.y(),p.z()};}
    private static DepthsPlan.Point point(int[] a){if(a.length!=3)throw new IllegalArgumentException("depths coordinate");return new DepthsPlan.Point(a[0],a[1],a[2]);}
    public static DepthsSavedData get(ServerLevel level){return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(DepthsSavedData::new,DepthsSavedData::load),"slavicmyths_ancient_depths");}
    private static DepthsSavedData load(CompoundTag n,HolderLookup.Provider registries){
        var data=new DepthsSavedData();for(var tag:n.getList("Instances",Tag.TAG_COMPOUND)){var e=Entry.load((CompoundTag)tag);data.entries.put(e.plan.id(),e);}return data;
    }
    @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider registries){n.putInt("Version",1);var list=new ListTag();entries.values().forEach(e->list.add(e.save()));n.put("Instances",list);return n;}
    public boolean overlaps(DepthsPlan.Box box){return entries.values().stream().anyMatch(e->e.plan.bounds().intersects(box,16));}
}
