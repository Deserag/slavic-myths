package org.slavicmyths.compat.xaero;

import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.navigation.*;
import org.slavicmyths.navigation.client.MapIntegrationBridge;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.waypoints.*;
import xaero.hud.minimap.waypoint.WaypointColor;

/** Isolated adapter for pinned Minimap 26.5.0. Uses public methods, no reflection/mixins.
 * Core persistence is the source of truth. Temporary display copies never serialize
 * into Xaero files; only our exact object references are modified or removed.
 */
public final class XaeroNavigationBridge implements MapIntegrationBridge {
    private static final String GROUP="Slavic Myths";
    private XaeroMinimapSession session;
    private WaypointWorld world;
    private WaypointSet set;
    private NavigationState publishedState;
    private final Map<UUID,Waypoint> owned=new HashMap<>();
    @Override public boolean publish(NavigationState state,ResourceLocation dimension){
        var current=XaeroMinimapSession.getCurrentSession();if(current==null){clear();return false;}
        var manager=current.getWaypointsManager();var next=manager.getCurrentWorld();
        if(next==null||next.getDimId()==null||!next.getDimId().location().equals(dimension)){clear();return false;}
        if(session!=current||world!=next){clear();session=current;world=next;}
        if(!state.preferences.contains(NavigationState.Preference.XAERO)){clear();return false;}
        if(publishedState==state&&set!=null)return state.tracked!=null&&owned.containsKey(state.tracked)&&world.getCurrentSet()==set;
        if(!world.getSets().containsKey(GROUP))world.addSet(GROUP);
        set=world.getSets().get(GROUP);Set<UUID> wanted=new HashSet<>();
        for(var marker:state.markers.values()){
            if(!marker.dimension().equals(dimension)||!state.visible(marker)||
               marker.area()!=null&&marker.area().state()==SearchArea.State.INSIDE_SEARCH_AREA)continue;
            wanted.add(marker.id());Waypoint point=owned.get(marker.id());
            String name=Component.translatable(marker.name()).getString();
            if(marker.area()!=null)name=Component.translatable("navigation.slavicmyths.area_name",name,marker.area().radius()).getString();
            if(marker.id().equals(state.tracked))name="▶ "+name;
            if(point==null){point=new Waypoint(marker.x(),marker.y(),marker.z(),name,marker.category().symbol,color(marker.category()));
                point.setTemporary(true);point.setThirdPartyOrigin(ResourceLocation.fromNamespaceAndPath("slavicmyths","navigation/"+marker.id()));
                owned.put(marker.id(),point);set.add(point);}
            point.setName(name);point.setX(marker.x());point.setY(marker.y());point.setZ(marker.z());
            point.setColor(color(marker.category()).ordinal());point.setSymbol(marker.category().symbol);
            point.setYIncluded(marker.area()==null);point.setVisibility(WaypointVisibilityType.GLOBAL);
        }
        for(UUID id:List.copyOf(owned.keySet()))if(!wanted.contains(id)){set.remove(owned.remove(id));}
        manager.updateWaypoints();
        publishedState=state;
        // Never switch the user's selected set or claim equivalence while our set is hidden.
        return state.tracked!=null&&owned.containsKey(state.tracked)&&world.getCurrentSet()==set;
    }
    private static WaypointColor color(MarkerCategory category){return switch(category){
        case YAGA->WaypointColor.DARK_PURPLE;case WAYSTONE->WaypointColor.AQUA;case KURGAN->WaypointColor.GRAY;
        case BANDIT->WaypointColor.DARK_RED;case BOSS->WaypointColor.RED;case QUEST->WaypointColor.GOLD;
        case SPECIAL_LOCATION->WaypointColor.GREEN;case EVENT->WaypointColor.YELLOW;};}
    @Override public void clear(){if(set!=null)for(var point:owned.values())set.remove(point);
        if(session!=null)session.getWaypointsManager().updateWaypoints();
        owned.clear();set=null;world=null;session=null;publishedState=null;}
}
