package org.slavicmyths.verify;
import java.lang.reflect.*;
import java.util.*;
public final class GreyWolfModelOffline {
 private static Object field(Object o,String name)throws Exception{var f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 public static void main(String[] args)throws Exception{
  var model=new org.slavicmyths.client.GreyWolfModel();var parts=(Map<?,?>)field(model,"parts");int checks=0,cubes=0;
  for(String name:List.of("root","chest","pelvis","loin","neck","head","upperJaw","jaw","nose","earL","earR","earLTip","earRTip","fl","fr","bl","br","flLower","frLower","blLower","brLower","flFoot","frFoot","blFoot","brFoot","tail1","tail2","tail3")){if(!parts.containsKey(name))throw new AssertionError(name);checks++;}
  for(var part:parts.values())for(var cube:(List<?>)field(part,"cubes")){cubes++;for(var face:(Object[])field(cube,"polygons"))for(var vertex:(Object[])field(face,"vertices")){float u=(float)field(vertex,"u"),v=(float)field(vertex,"v");if(!Float.isFinite(u)||!Float.isFinite(v)||u<0||u>1||v<0||v>1)throw new AssertionError("UV outside atlas");checks++;}}
  System.out.println("GREY_WOLF_MODEL_OFFLINE_CHECKS="+checks+" parts="+parts.size()+" cubes="+cubes+"; no client launch");
 }
}
