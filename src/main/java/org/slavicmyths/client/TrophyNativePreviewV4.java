package org.slavicmyths.client;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Offline bounding-volume reference preview, not registered in Minecraft.
 * Raven: native skull cuboids centered on one anchor cell; beak points into the next cell.
 * Zmey: source head, jaw and horn coordinates from MorovoyFireZmeyMesh, uniformly scaled;
 * the neck branches are new, built to join the two heads without overlap.
 * Coordinates are 1/16-block units, X / Z are horizontal, Y is up. */
public final class TrophyNativePreviewV4 {
 public record Box(String part,double x,double y,double z,double w,double h,double d,String material) {}
 private final List<Box> boxes=new ArrayList<>();
 private void add(String part,double x,double y,double z,double w,double h,double d,String mat){
   if(x<-.001||y<-.001||z<-.001||x+w>32.001||y+h>16.001||z+d>32.001||w<=0||h<=0||d<=0)throw new IllegalArgumentException("Outside 2x2x1: "+part+" "+x+","+y+","+z+" dims "+w+","+h+","+d);
   boxes.add(new Box(part,x,y,z,w,h,d,mat));
 }
 // Match TrophyHeadsClient: X=8px on the anchor; positive Z is the forward cell.
 // Use native bounds Y[-17..13], Z[-47..17] at the same uniform .45 scale.
 private void ravenRaw(String part,double x,double my,double z,double w,double h,double d,String m){
   double s=.45;
   add(part,8+s*x,.48+s*(13-my-h),16-s*(z+d+15),w*s,h*s,d*s,m);
 }
 private void raven(){
   // Original MorovoyRavenModel skull pieces, including hooked beak. Never squash height.
   ravenRaw("skull",-11,-12,-15,22,20,26,"raven_black");
   ravenRaw("skull",-8,-12,-12,16,4,12,"raven_slate");
   ravenRaw("skull",-7,-7,8,14,10,9,"raven_black");
   ravenRaw("beak",-7,-1,-25,14,7,14,"beak");
   ravenRaw("beak",-5,0,-36,10,6,11,"beak");
   ravenRaw("beak",-3,2.5,-43,6,8,9,"beak");
   ravenRaw("beak",-1.8,7,-47,3.6,6,6,"beak");
   ravenRaw("skull",-6.9,1,-24,1,2,2,"raven_black");
   ravenRaw("skull",5.9,1,-24,1,2,2,"raven_black");
   ravenRaw("eyes",-11.7,-6.5,-11.5,2.2,4.2,5,"raven_eye");
   ravenRaw("eyes",9.5,-6.5,-11.5,2.2,4.2,5,"raven_eye");
   for(int i=0;i<3;i++)ravenRaw("feathers",-6+i*4.5,-15-(i%2)*2,0,3,6,12,"raven_slate");
   // Jaw from native skull child: PartPose.offset(0,4,-13)
   ravenRaw("jaw",-6,4,-25,12,3,12,"raven_slate");
   ravenRaw("jaw",-3,4,-34,6,3,10,"beak");
   ravenRaw("jaw",-2.5,3.5,-27,5,1,10,"raven_black");
 }
 private void zmeyRaw(String name,double center,double x,double my,double z,double w,double h,double d,String mat){
   double s=.84;
   add(name,center+s*x,7.30-s*(my+h),18.2+s*z,w*s,h*s,d*s,mat);
 }
 private void head(String name,double cx,boolean fire){
   // Each half uses the actual MorovoyFireZmeyMesh material family:
   // infected = SLATE/CHARCOAL/MOR_WEB/MOR_VEIN, burning = OBSIDIAN/FIRE_WEB/FIRE_CRACK.
   String hide=fire?"fire_obsidian":"mor_slate";
   String brow=fire?"fire_charcoal":"mor_charcoal";
   String snout=fire?"fire_web":"mor_web";
   String markings=fire?"fire_crack":"mor_vein";
   String crest=fire?"fire_crest":"mor_crest";
   String eye=fire?"fire_eye":"mor_eye";
   String horns=fire?"scorched_bone":"bone";
   zmeyRaw(name,cx,-4.6,-4.5,-10,9.2,7,11,hide);
   zmeyRaw(name,cx,-3.5,-.5,-15,7,3,8,snout);
   zmeyRaw(name,cx,-3.8,2,-14,7.6,2.5,13,brow);
   zmeyRaw(name,cx,-4.5,-5,-10,9,2,5,brow);
   for(int sign:new int[]{-1,1}){
      zmeyRaw(name,cx,sign<0?-4.7:3.4,-3,-8,1.3,1.8,2,eye);
      zmeyRaw(name,cx,sign*3.1-1,-10.2,-3.2,2,7,2.5,horns);
   }
   // Each crown has different form and material, not only the eye pigment.
   for(int i=0;i<4;i++){
      double h=fire?4.7+i*.72:3.5+i*.35;
      double bx=fire?(-1.9+.18*i):(-1.1-.10*i);
      zmeyRaw(name,cx,bx,-3.5-i*.42,-8+i*2.5,fire?2.5:1.9,h,2.5,crest);
   }
   for(int i=0;i<5;i++)zmeyRaw(name,cx,-3+i*1.4,0,-12.5,.8,2.4,1.2,horns);
   // Mor lineage: interlocking cold infection bars along brow, crest, lower cheek.
   // Fire lineage: short irregular glowing cracks on crown, cheeks and upper snout.
   for(int i=0;i<7;i++){
      double x=-3.95+i*1.28;
      if(fire){
         zmeyRaw(name,cx,x,-5.05+(i%3)*.22,-8.7, .52,1.12+(i%2)*.38,.73,markings);
         if(i%2==0)zmeyRaw(name,cx,x+.23,-1.55,-14.15,.62,.45,1.25,markings);
      }else{
         zmeyRaw(name,cx,x,-4.85+(i%2)*.18,-8.72,.38,.52,1.5,markings);
         if(i%2==0)zmeyRaw(name,cx,x-.10,-.90,-14.65,.42,.75,1.05,markings);
      }
   }
   for(int i=0;i<4;i++){
      double x=fire?-3.70+i*2.2:-3.65+i*2.1;
      zmeyRaw(name,cx,x,1.05,-11.7,.38,1.48,.45,markings);
   }
 }
 private void pair(){
   // Common branching collar occupies the second row of blocks, left and right heads separate.
   add("neck",13.7,3.45,24.6,4.6,5.2,5.9,"charcoal");
   add("neck",7.15,4.15,18.3,2.4,3.5,8.4,"mor_slate");
   add("neck",22.45,4.15,18.3,2.4,3.5,8.4,"fire_obsidian");
   for(int i=0;i<5;i++){
      add("neck",7.65,7.48,18.65+i*1.25,1.4,.33,.85,"mor_vein");
      add("neck",22.85,7.48,18.65+i*1.25,1.40,.33,.85,"fire_crack");
   }
   add("neck",8.2,4.30,24.3,7.85,3.5,3.3,"charcoal");
   add("neck",15.95,4.30,24.3,7.85,3.5,3.3,"charcoal");
   for(int k=0;k<4;k++)add("neck",13.6+k*1.25,8.05,24.6,1.01,1.52,1.3,"crest");
   head("mor_head",8.0,false);
   head("fire_head",24.0,true);
 }
 public static List<Box> build(boolean raven){TrophyNativePreviewV4 v=new TrophyNativePreviewV4();if(raven)v.raven();else v.pair();return List.copyOf(v.boxes);}
 public static void main(String[] args)throws Exception{
   if(args.length!=2)throw new IllegalArgumentException("Usage: raven|twin file.json");
   boolean raven=args[0].equals("raven");
   StringBuilder b=new StringBuilder("{\"model\":\""+(raven?"raven_native_unscaled_shape":"twin_zmey_native_heads")+"\",\"cuboids\":[");
   boolean first=true;
   for(Box v:build(raven)){
     if(!first)b.append(',');first=false;
     b.append(String.format(Locale.ROOT,"{\"part\":\"%s\",\"x\":%.5f,\"y\":%.5f,\"z\":%.5f,\"w\":%.5f,\"h\":%.5f,\"d\":%.5f,\"material\":\"%s\"}",v.part(),v.x(),v.y(),v.z(),v.w(),v.h(),v.d(),v.material()));
   }
   Files.writeString(Path.of(args[1]),b.append("]}").toString(),StandardCharsets.UTF_8);
   System.out.println(args[0]+" cuboids "+build(raven).size());
 }
}