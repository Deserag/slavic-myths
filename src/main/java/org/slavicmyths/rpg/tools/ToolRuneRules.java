package org.slavicmyths.rpg.tools;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Bounded geometry and admission shared by runtime and offline checks. */
public final class ToolRuneRules {
 public static final List<String> IDS=List.of("miner_iii","excavator_iii","farmer_iii","lumberjack_iii","miner_iv","excavator_iv","farmer_iv","lumberjack_iv");
 public static final int BREAKS_PER_TICK=4,READS_PER_TICK=256,MAX_READS=8192,MAX_TREES=16,MAX_HEIGHT=32,TTL=160;
 public static boolean tool(String id){return IDS.contains(id);}
 public static String family(String id){return tool(id)?id.substring(0,id.lastIndexOf('_')):"";}
 public static int tier(String id){return tool(id)?id.endsWith("_iv")?4:3:0;}
 public static int tier(List<String> ids,String family){return ids.contains(family+"_iv")?4:ids.contains(family+"_iii")?3:0;}
 public static boolean conflict(List<String> installed,String id){return tool(id)&&installed.stream().anyMatch(old->!old.equals(id)&&family(old).equals(family(id)));}
 public static int radius(int tier){return tier==4?7:4;}
 public static int logs(int tier){return tier==4?96:48;}
 public static boolean neighbor(BlockPos original,BlockPos candidate,int tier){long dx=(long)candidate.getX()-original.getX(),dz=(long)candidate.getZ()-original.getZ();return Math.abs((long)candidate.getY()-original.getY())<=2&&dx*dx+dz*dz<=(long)radius(tier)*radius(tier);}
 public static boolean neighborTrees(List<BlockPos> original,List<BlockPos> candidate,int tier){return original.stream().anyMatch(a->candidate.stream().anyMatch(b->neighbor(a,b,tier)));}
 public static List<BlockPos> plane(BlockPos origin,Direction face,int tier){
  if(face==null||tier!=3&&tier!=4)return List.of(origin);
  var out=new ArrayList<BlockPos>();
  for(int depth=0;depth<(tier==4?2:1);depth++)for(int a=-1;a<=1;a++)for(int b=-1;b<=1;b++){
   var c=origin.relative(face.getOpposite(),depth);
   out.add(switch(face.getAxis()){case X->c.offset(0,a,b);case Y->c.offset(a,0,b);case Z->c.offset(a,b,0);});
  }return List.copyOf(out);
 }
 public static List<BlockPos> field(BlockPos origin,int tier){if(tier!=3&&tier!=4)return List.of(origin);int r=tier==4?2:1;var out=new ArrayList<BlockPos>();for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++)out.add(origin.offset(x,0,z));return List.copyOf(out);}
 /** Exact namespace and family; stripped material never becomes an automatic tree. */
 public static String logFamily(String id){int colon=id.indexOf(':');if(colon<1)return "";String name=id.substring(colon+1);if(name.startsWith("stripped_"))return "";String stem=name.endsWith("_log")?name.substring(0,name.length()-4):name.endsWith("_wood")?name.substring(0,name.length()-5):"";return stem.isEmpty()?"":id.substring(0,colon+1)+stem;}
 public static String leafFamily(String id){return id.endsWith("_leaves")?id.substring(0,id.length()-7):"";}
 private ToolRuneRules(){}
}
