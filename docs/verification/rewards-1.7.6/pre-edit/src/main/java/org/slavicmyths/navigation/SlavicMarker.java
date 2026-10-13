package org.slavicmyths.navigation;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/** Same safe marker value on server/client; private encounter links live elsewhere. */
public record SlavicMarker(UUID id,String source,MarkerCategory category,Kind kind,Lifetime lifetime,
    String name,ResourceLocation dimension,int x,int y,int z,long createdAt,int revision,SearchArea area) {
    public enum Kind { PERMANENT_DISCOVERY,TEMPORARY_QUEST,TEMPORARY_EVENT,SEARCH_AREA,TRAVEL_POINT,TRACKED_ONLY }
    public enum Lifetime { PERMANENT,UNTIL_QUEST_END,UNTIL_EVENT_END,UNTIL_DISCOVERED }
    public SlavicMarker {
        if(id==null||source==null||source.isEmpty()||source.length()>192||name==null||name.length()>128||
           dimension==null||category==null||kind==null||lifetime==null||revision<0||
           Math.abs((long)x)>30_001_000||Math.abs((long)z)>30_001_000||y< -4096||y>4096||
           (kind==Kind.SEARCH_AREA)!=(area!=null))throw new IllegalArgumentException("Invalid marker");
    }
    public static UUID identity(String source){return UUID.nameUUIDFromBytes(("slavicmyths:"+source).getBytes(StandardCharsets.UTF_8));}
    public boolean permanent(){return lifetime==Lifetime.PERMANENT;}
    public SlavicMarker withArea(SearchArea next){return new SlavicMarker(id,source,category,kind,lifetime,name,dimension,x,y,z,createdAt,revision,next);}
    public CompoundTag save(){
        CompoundTag n=new CompoundTag();n.putString("Owner","slavicmyths");n.putUUID("Id",id);n.putString("Source",source);
        n.putString("Category",category.name());n.putString("Kind",kind.name());n.putString("Lifetime",lifetime.name());
        n.putString("Name",name);n.putString("Dimension",dimension.toString());n.putInt("X",x);n.putInt("Y",y);n.putInt("Z",z);
        n.putLong("CreatedAt",createdAt);n.putInt("Revision",revision);
        if(area!=null){CompoundTag a=new CompoundTag();a.putDouble("CenterX",area.centerX());a.putDouble("CenterZ",area.centerZ());
            a.putInt("Radius",area.radius());a.putString("State",area.state().name());n.put("Area",a);}
        return n;
    }
    public static SlavicMarker read(CompoundTag n){
        if(!n.getString("Owner").equals("slavicmyths"))throw new IllegalArgumentException("Foreign marker");
        SearchArea area=null;
        if(n.contains("Area",10)){CompoundTag a=n.getCompound("Area");area=new SearchArea(a.getDouble("CenterX"),a.getDouble("CenterZ"),a.getInt("Radius"),SearchArea.State.valueOf(a.getString("State")));}
        return new SlavicMarker(n.getUUID("Id"),n.getString("Source"),MarkerCategory.valueOf(n.getString("Category")),
            Kind.valueOf(n.getString("Kind")),Lifetime.valueOf(n.getString("Lifetime")),n.getString("Name"),
            ResourceLocation.parse(n.getString("Dimension")),n.getInt("X"),n.getInt("Y"),n.getInt("Z"),
            Math.max(0,n.getLong("CreatedAt")),n.getInt("Revision"),area);
    }
}
