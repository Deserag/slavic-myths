package org.slavicmyths.navigation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Formats only public values already received by the client. No target lookup or synchronization. */
public final class SearchGuidance {
 private static MutableComponent text(String key,Object... args){return Component.translatable("navigation.search."+key,args);}
 private static MutableComponent line(MutableComponent result,Component next){return result.append("\n").append(next);}
 public static Component dimension(String id){return switch(id){case "minecraft:overworld"->text("overworld");case "minecraft:the_nether"->text("nether");case "minecraft:the_end"->text("end");default->Component.literal(id);};}
 public static Component hint(CompoundTag h,String current,double px,double pz,long now){
  if(h.getBoolean("Stale")||h.contains("Expires",99)&&now>h.getLong("Expires"))return text("expired");
  if(!h.contains("X",99)||!h.contains("Z",99)||!h.contains("Radius",99)||!h.contains("Dimension",8))return text("unknown");
  if(h.getDouble("Radius")!=h.getInt("Radius"))return text("unknown");
  return area(h.getDouble("X"),h.getDouble("Z"),h.getInt("Radius"),h.getString("Dimension"),current,px,pz,"known");
 }
 public static Component marker(SlavicMarker m,String current,double px,double pz,boolean live){
  if(m.area()!=null&&m.area().state()==SearchArea.State.COMPLETED)return text("completed");
  if(!live)return text("ended");
  if(m.area()!=null&&m.area().state()==SearchArea.State.EXPIRED)return text("expired");
  if(m.area()!=null){var a=m.area();String state=switch(a.state()){case COMPLETED->"completed";case TARGET_DISCOVERED->"found";default->"known";};return area(a.centerX(),a.centerZ(),a.radius(),m.dimension().toString(),current,px,pz,state);}
  // Exact point coordinates already became public through the existing discovery protocol.
  var result=text("dimension",dimension(m.dimension().toString()));line(result,text(m.kind()==SlavicMarker.Kind.PERMANENT_DISCOVERY||m.kind()==SlavicMarker.Kind.TEMPORARY_QUEST?"found":"point_known"));
  if(!m.dimension().toString().equals(current))line(result,text("other_dimension"));
  return line(result,text("point",SearchCoordinates.number(m.x()),SearchCoordinates.number(m.y()),SearchCoordinates.number(m.z())));
 }
 public static Component area(double x,double z,int radius,String dim,String current,double px,double pz,String state){
  var location=net.minecraft.resources.ResourceLocation.tryParse(dim);
  if(!SearchCoordinates.valid(x,z,radius)||location==null)return text("unknown");
  dim=location.toString();
  var result=text("dimension",dimension(dim));line(result,text(state));
  if(!dim.equals(current))line(result,text("other_dimension"));
  line(result,text("center",SearchCoordinates.number(x),SearchCoordinates.number(z)));line(result,text("radius",radius));
  line(result,text("bounds_x",SearchCoordinates.number(SearchCoordinates.lower(x,radius)),SearchCoordinates.number(SearchCoordinates.upper(x,radius))));
  line(result,text("bounds_z",SearchCoordinates.number(SearchCoordinates.lower(z,radius)),SearchCoordinates.number(SearchCoordinates.upper(z,radius))));
  line(result,text("circle"));line(result,text("bounds_note"));
  if(SearchCoordinates.clipped(x,z,radius))line(result,text("world_edge"));
  if(dim.equals(current)&&Double.isFinite(px)&&Double.isFinite(pz))line(result,SearchCoordinates.inside(x,z,radius,px,pz)?text("inside"):text("outside",SearchCoordinates.distance(x,z,radius,px,pz)));
  return result;
 }
 private SearchGuidance(){}
}
