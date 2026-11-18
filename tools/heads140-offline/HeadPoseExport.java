package org.slavicmyths.client;
import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import org.slavicmyths.trophy.TrophyHeads;
/** Actual production vertices/UV; never a world or client launch. */
public final class HeadPoseExport {
 public static void main(String[] args)throws Exception {
  var poses=new ArrayList<Object>();
  // Historical six-head regression; Magma Lord uses burning-islands-tests native poses.
  for(var kind:java.util.EnumSet.range(TrophyHeads.Kind.TUGARIN,TrophyHeads.Kind.PODZEMNIK))for(int time:new int[]{0,27,40,60}){
   var h=TrophyHeadsClient.create(kind);h.pose(time,.7F,.3F);
   var out=new TrophyHeadsClient.Capture();h.render(new PoseStack(),out,0,0);
   poses.add(Map.of("kind",kind.name(),"time",time,"texture",h.texture.toString(),"vertices",out.vertices,"bounds",out.bounds()));
  }
  var path=Path.of(System.getProperty("head.export","work/heads-before.json"));Files.createDirectories(path.getParent());
  Files.writeString(path,new com.google.gson.Gson().toJson(poses));
 }
}
