package org.slavicmyths.losthalls;
import java.util.*;
import net.minecraft.core.BlockPos;
import org.slavicmyths.losthalls.LostHallsRooms.*;
/** Pure geometry consumed by the actual chunk feature and finite route tests. */
public final class HallsGeometry {
 public record Column(int floor,int ceiling,boolean openBelow,Scene scene,boolean center,boolean pillar){}
 /** Six different physical families plus connected, sloped and reversible main passages. */
 public static Column column(LostHallsRegionTopology.Plan p,int x,int z){
  List<Node> nodes=new ArrayList<>(p.rooms());nodes.add(new Node(p.arenaId(),Scene.LARGE_HALL,p.arena(),1));Column best=null;
  for(Node n:nodes){double dx=x-n.anchor().getX(),dz=z-n.anchor().getZ();if(n.scene()==Scene.CORRIDOR)dx-=2*Math.sin(dz*.28);double rx=switch(n.scene()){case CORRIDOR->3.5;case BRIDGE->22;case JUNCTION->12;default->24;};if(n.id().equals(p.arenaId()))rx=30;double rz=n.scene()==Scene.CORRIDOR?16:n.scene()==Scene.BRIDGE?17:rx*.82;
   double q=dx*dx/(rx*rx)+dz*dz/(rz*rz);if(q<=1.12){boolean bridge=n.scene()==Scene.BRIDGE&&Math.abs(dz)>2;int floor=n.anchor().getY()-1;int height=switch(n.scene()){case CORRIDOR->7;case JUNCTION->10;case BRIDGE->18;default->18+(int)(8*(1-q));};boolean pillar=q>1||!n.id().equals(p.arenaId())&&n.scene()==Scene.LARGE_HALL&&Math.abs(Math.abs(dx)-12)<=1&&Math.abs(Math.abs(dz)-9)<=1;int tier=n.scene()==Scene.LUMINOUS_GROTTO&&q>.55?2:0;best=new Column(bridge?floor-14:floor+tier,floor+height,bridge,n.scene(),Math.abs(dx)<3&&Math.abs(dz)<3,pillar);}
  }
  Map<UUID,BlockPos> anchors=new HashMap<>();for(Node n:p.rooms())anchors.put(n.id(),n.anchor());anchors.put(p.arenaId(),p.arena());
  for(var link:p.links()){BlockPos a=anchors.get(link.from()),b=anchors.get(link.to());double vx=b.getX()-a.getX(),vz=b.getZ()-a.getZ();double t=Math.clamp(((x-a.getX())*vx+(z-a.getZ())*vz)/(vx*vx+vz*vz),0,1);double dx=x-a.getX()-t*vx,dz=z-a.getZ()-t*vz;if(dx*dx+dz*dz<=9){int floor=(int)Math.floor(a.getY()+t*(b.getY()-a.getY()))-1;best=new Column(floor,floor+7,false,Scene.CORRIDOR,true,false);}}
  return best;
 }
 private HallsGeometry(){}
}
