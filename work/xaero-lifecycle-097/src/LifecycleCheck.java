import java.util.*;import java.lang.reflect.*;import xaero.common.*;import xaero.common.minimap.waypoints.*;import xaero.hud.minimap.waypoint.*;import org.slavicmyths.compat.xaero.XaeroNavigationBridge;import org.slavicmyths.navigation.*;import net.minecraft.resources.ResourceLocation;
public class LifecycleCheck{
 static Object get(Object b,String name)throws Exception{var f=b.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(b);}static void set(Object b,String name,Object value)throws Exception{var f=b.getClass().getDeclaredField(name);f.setAccessible(true);f.set(b,value);}static void check(boolean p){if(!p)throw new AssertionError();}
 public static void main(String[] args)throws Exception{
  int cases=0;for(int mode=0;mode<5;mode++){
   var bridge=new XaeroNavigationBridge();var session=new XaeroMinimapSession();var manager=new WaypointsManager();session.manager=mode==0?null:manager;XaeroMinimapSession.current=mode==2?null:session;if(mode==3)XaeroMinimapSession.current=new XaeroMinimapSession();manager.throwOnUpdate=mode==4;
   var group=new WaypointSet();var user=new Waypoint(0,0,0,"User","U",WaypointColor.GRAY);var own=new Waypoint(1,1,1,"Mod","M",WaypointColor.GRAY);group.add(user);group.add(own);set(bridge,"session",session);set(bridge,"set",group);((Map)get(bridge,"owned")).put(UUID.randomUUID(),own);
   group.onRemove=()->{try{check(get(bridge,"session")==null&&get(bridge,"set")==null&&((Map)get(bridge,"owned")).isEmpty());}catch(Exception e){throw new RuntimeException(e);}};
   bridge.clear();check(group.points.equals(List.of(user)));check(manager.updates==((mode==1||mode==4)?1:0));check(get(bridge,"publishedState")==null);bridge.clear();check(group.points.equals(List.of(user)));cases++;
  }
  var b=new XaeroNavigationBridge();XaeroMinimapSession.current=null;check(!b.publish(new NavigationState(),ResourceLocation.fromNamespaceAndPath("minecraft","overworld")));cases++;
  XaeroMinimapSession.current=new XaeroMinimapSession();check(!b.publish(new NavigationState(),ResourceLocation.fromNamespaceAndPath("minecraft","overworld")));cases++;
  XaeroMinimapSession.current.manager=new WaypointsManager();XaeroMinimapSession.current.manager.world=null;check(!b.publish(new NavigationState(),ResourceLocation.fromNamespaceAndPath("minecraft","overworld")));cases++;
  check(!b.publish(null,ResourceLocation.fromNamespaceAndPath("minecraft","overworld")));cases++;check(!b.publish(new NavigationState(),null));cases++;
  System.out.println("XAERO_LIFECYCLE_PASS cases="+cases+" userWaypointPreserved=true internalStateClearedFirst=true");
 }}