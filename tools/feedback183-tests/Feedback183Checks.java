package org.slavicmyths.client;
import java.util.*;
import java.nio.file.*;
import java.lang.reflect.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.finist.*;

/** Native baked model checks, not an in-game rendering claim. */
public final class Feedback183Checks {
    private static int checks;private static Field cubes,children,polygons;
    private static void check(boolean ok,String text){checks++;if(!ok)throw new AssertionError(text);}
    @SuppressWarnings("unchecked") private static void export(ModelPart part,PoseStack stack,String path,List<Map<String,Object>> out)throws Exception{
        if(!part.visible)return;stack.pushPose();part.translateAndRotate(stack);
        for(var cube:(List<?>)cubes.get(part)){var faces=new ArrayList<List<float[]>>();for(var face:(Object[])polygons.get(cube))if(face!=null){var f=face.getClass().getDeclaredField("vertices");f.setAccessible(true);var vertices=new ArrayList<float[]>();for(var v:(Object[])f.get(face)){var pos=v.getClass().getDeclaredField("pos");var u=v.getClass().getDeclaredField("u");var t=v.getClass().getDeclaredField("v");pos.setAccessible(true);u.setAccessible(true);t.setAccessible(true);var xyz=stack.last().pose().transformPosition(new org.joml.Vector3f((org.joml.Vector3f)pos.get(v)).div(16));float uv=u.getFloat(v),vv=t.getFloat(v);check(uv>=0&&uv<=1&&vv>=0&&vv<=1,"UV outside atlas: "+path);vertices.add(new float[]{xyz.x*16,xyz.y*16,xyz.z*16,uv,vv});}faces.add(vertices);}out.add(Map.of("part",path,"faces",faces));}
        for(var e:((Map<String,ModelPart>)children.get(part)).entrySet())export(e.getValue(),stack,path+"/"+e.getKey(),out);stack.popPose();
    }

 static float[] bounds(Object cube)throws Exception{float[] b={999,999,999,-999,-999,-999};for(Object face:(Object[])polygons.get(cube)){var f=face.getClass().getDeclaredField("vertices");f.setAccessible(true);for(Object vertex:(Object[])f.get(face)){var pf=vertex.getClass().getDeclaredField("pos");pf.setAccessible(true);var v=(org.joml.Vector3f)pf.get(vertex);for(int i=0;i<3;i++){b[i]=Math.min(b[i],v.get(i));b[i+3]=Math.max(b[i+3],v.get(i));}}}return b;}
 static boolean connected(ModelPart weapon)throws Exception{List<float[]> boxes=new ArrayList<>();for(Object cube:(List<?>)cubes.get(weapon))boxes.add(bounds(cube));Set<Integer> reached=new HashSet<>();reached.add(0);boolean change=true;while(change){change=false;for(int i=0;i<boxes.size();i++)if(!reached.contains(i))for(int j:new ArrayList<>(reached)){var a=boxes.get(i);var b=boxes.get(j);boolean overlap=true;for(int axis=0;axis<3;axis++)if(a[axis]>b[axis+3]+.02||b[axis]>a[axis+3]+.02)overlap=false;if(overlap){reached.add(i);change=true;break;}}}return reached.size()==boxes.size();}
 static boolean palmContains(ModelPart hand,ModelPart weapon)throws Exception{for(Object cube:(List<?>)cubes.get(hand)){float[] lo={999,999,999},hi={-999,-999,-999};for(Object face:(Object[])polygons.get(cube)){var f=face.getClass().getDeclaredField("vertices");f.setAccessible(true);for(Object vertex:(Object[])f.get(face)){var pos=vertex.getClass().getDeclaredField("pos");pos.setAccessible(true);var v=(org.joml.Vector3f)pos.get(vertex);for(int i=0;i<3;i++){lo[i]=Math.min(lo[i],v.get(i));hi[i]=Math.max(hi[i],v.get(i));}}}if(weapon.x>=lo[0]-.05&&weapon.x<=hi[0]+.05&&weapon.y>=lo[1]-.05&&weapon.y<=hi[1]+.05&&weapon.z>=lo[2]-.05&&weapon.z<=hi[2]+.05)return true;}return false;}
 @SuppressWarnings("unchecked") public static void main(String[] args)throws Exception{
 net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
 cubes=ModelPart.class.getDeclaredField("cubes");children=ModelPart.class.getDeclaredField("children");polygons=ModelPart.Cube.class.getDeclaredField("polygons");for(var f:List.of(cubes,children,polygons))f.setAccessible(true);
 var output=new LinkedHashMap<String,List<Map<String,Object>>>();
 for(String name:List.of("ilya","alyosha","dobrynya","finist")){
  Object model;ModelPart root;Map<String,ModelPart> nodes;
  if(name.equals("ilya")){var m=new BogatyrClient.Model<org.slavicmyths.bogatyr.CombatEntity>(IlyaMesh.create(),true);model=m;var f=m.getClass().getDeclaredField("nodes");f.setAccessible(true);nodes=(Map<String,ModelPart>)f.get(m);var rf=m.getClass().getDeclaredField("root");rf.setAccessible(true);root=(ModelPart)rf.get(m);}
  else if(name.equals("alyosha")){var m=new SkimModel<org.slavicmyths.skim.SkimActor>(AlyoshaMesh.create(),false);model=m;root=m.root;nodes=m.nodes;}
  else if(name.equals("dobrynya")){var m=new WitchModel<net.minecraft.world.entity.LivingEntity>(DobrynyaMesh.create(),0);model=m;root=m.root;nodes=m.nodes;}
  else {var m=new FinistModel<org.slavicmyths.finist.FalconActor>(FinistMesh.create(),false);model=m;root=m.root;nodes=m.nodes;}
  Class<? extends Enum> state=(Class<? extends Enum>)Class.forName(name.equals("ilya")?"org.slavicmyths.bogatyr.BogatyrRules$State":name.equals("alyosha")?"org.slavicmyths.skim.SkimAction":name.equals("dobrynya")?"org.slavicmyths.marinka.WitchAction":"org.slavicmyths.finist.FalconAction");
  String weapon=name.equals("ilya")?"mace":name.equals("alyosha")?"saber":name.equals("dobrynya")?"spear":"weapon";
  check(connected(nodes.get(weapon)),"Disconnected production weapon cube components: "+name);
  for(Object clip:state.getEnumConstants())for(int tick:new int[]{0,4,8,13,19,24,40,60}){
   if(name.equals("ilya"))model.getClass().getMethod("pose",state,float.class,float.class,float.class,float.class,float.class,float.class,boolean.class).invoke(model,clip,(float)tick,1F,.4F,(float)tick,0F,0F,false);
   else model.getClass().getMethod("pose",state,float.class,float.class,float.class,float.class,float.class,float.class).invoke(model,clip,(float)tick,1F,.4F,(float)tick,0F,0F);
   if(nodes.get(weapon).visible)check(palmContains(nodes.get("right_hand"),nodes.get(weapon)),"Weapon grip leaves palm: "+name+" "+clip+" "+tick);
   var mesh=new ArrayList<Map<String,Object>>();export(root,new PoseStack(),"root",mesh);output.put(name+"_"+clip+"_"+tick,mesh);if(clip.toString().equals("IDLE")&&tick==0)output.put(name+"_IDLE",mesh);
  }
 }
 Files.writeString(Path.of("work/feedback-1.8.3/meshes.json"),new com.google.gson.Gson().toJson(output));System.out.println("FEEDBACK183_NATIVE_PASS checks="+checks+" meshes="+output.size()+" NOT_GAME_TEST");
 }
}

