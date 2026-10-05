package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.navigation.*;

public final class NavigationHeadless {
    private static void require(boolean condition,String why){if(!condition)throw new AssertionError(why);}
    private static SlavicMarker permanent(String source){return new SlavicMarker(SlavicMarker.identity(source),source,MarkerCategory.KURGAN,
        SlavicMarker.Kind.PERMANENT_DISCOVERY,SlavicMarker.Lifetime.PERMANENT,"navigation.slavicmyths.structure.kurgan_great",
        ResourceLocation.parse("minecraft:overworld"),100,64,200,1000,0,null);}
    public static void main(String[] args){
        var s=new NavigationState();var a=permanent("structure:a");var b=permanent("structure:b");
        require(s.upsert(a)&&!s.upsert(a),"Repeated source duplicated");s.upsert(b);s.track(a.id());s.track(b.id());
        require(s.tracked.equals(b.id())&&s.markers.size()==2,"Tracking removed prior saved marker");
        s.categories.remove(MarkerCategory.KURGAN);require(!s.visible(a)&&s.visible(b),"Hidden category / tracked override");
        s.preferences.remove(NavigationState.Preference.ALWAYS_TRACKED);require(!s.visible(b),"Disabled tracked override ignored");
        var restored=NavigationState.read(s.save());require(restored.tracked.equals(b.id())&&!restored.visible(b),"Filter/tracking persistence");
        restored.categories.add(MarkerCategory.KURGAN);require(restored.visible(a),"Category toggle erased discoveries");
        var other=new NavigationState();require(other.markers.isEmpty()&&other.tracked==null,"Player isolation");
        CompoundTag foreign=a.save();foreign.putString("Owner","user");boolean rejected=false;try{SlavicMarker.read(foreign);}catch(IllegalArgumentException expected){rejected=true;}require(rejected,"Foreign marker adopted");
        var stats=NavigationStatistics.run(1000);
        require(stats.outside()==0&&stats.inner25()<100&&stats.outer50to85()>=550&&stats.outer50to85()<=800&&stats.edge85to95()>=100&&stats.edge85to95()<=250,"Center bias: "+stats);
        for(int i=0;i<1000;i++){
            UUID id=new UUID(i*7919L,i*31L);double x=i*11.5-8000,z=i*7.5-9000;
            var first=SearchArea.generate(x,z,SearchArea.Scale.MINI,id,456789,0,(cx,cz,r)->true);
            require(first.equals(SearchArea.generate(x,z,SearchArea.Scale.MINI,id,456789,0,(cx,cz,r)->true)),"Relog changed zone");
            require(first.contains(x,z)&&first.distance(x,z)>first.radius()*.25,"Target outside/centered");
            require(!first.equals(SearchArea.generate(x,z,SearchArea.Scale.MINI,id,456789,1,(cx,cz,r)->true)),"Replacement revision ignored");
        }
        int[] attempts={0};require(SearchArea.generate(500,500,SearchArea.Scale.MINI,new UUID(1,2),3,0,(x,z,r)->{attempts[0]++;return false;})==null&&attempts[0]==32,"Unbounded retry or exact fallback");
        var area=SearchArea.generate(12345,67890,SearchArea.Scale.MINI,new UUID(9,8),987654321,0,(x,z,r)->true);
        var marker=new SlavicMarker(new UUID(9,8),"quest:opaque:search",MarkerCategory.QUEST,SlavicMarker.Kind.SEARCH_AREA,
            SlavicMarker.Lifetime.UNTIL_QUEST_END,"entity.slavicmyths.ovinnik",ResourceLocation.parse("minecraft:overworld"),(int)area.centerX(),0,(int)area.centerZ(),0,0,area);
        var quest=new NavigationState();quest.upsert(marker);quest.track(marker.id());String publicNbt=quest.save().toString();
        for(String forbidden:List.of("12345","67890","987654321","Anchor","TargetX","TargetZ","Seed","Salt","TargetUUID"))require(!publicNbt.contains(forbidden),"Hidden information in public NBT: "+forbidden);
        require(SlavicMarker.read(marker.save()).equals(marker),"Geometry roundtrip");require(area.boundaryDistance(area.centerX(),area.centerZ())==0,"Center not inside");
        require(NavigationMath.arrow(0,0,0,0,10)==0&&NavigationMath.arrow(0,0,0,10,0)==6&&NavigationMath.arrow(0,0,-90,10,0)==0,"Yaw bearing");
        require(!NavigationMath.sameDimension(marker,"minecraft:the_nether"),"Cross-dimension arrow enabled");
        require(quest.end(marker.id(),true)&&quest.tracked==null&&quest.markers.isEmpty()&&quest.trackingEnded,"Cleanup / notification");
        var clean=NavigationState.read(quest.save());require(clean.markers.isEmpty()&&clean.history.size()==1,"Completed marker resurrected");
        CompoundTag malformed=s.save();malformed.getList("Markers",10).getCompound(0).putString("Category","not_a_category");
        require(NavigationState.read(malformed).markers.size()==1,"Malformed entry poisoned whole state");
        System.out.println("NAVIGATION_HEADLESS_PASS "+stats);
    }
}
