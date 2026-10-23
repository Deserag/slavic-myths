package org.slavicmyths.client;
import java.util.*;
import java.nio.file.*;
import java.lang.reflect.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.finist.*;

/** Native baked model checks, not an in-game rendering claim. */
public final class FinistChecks {
    private static int checks;private static Field cubes,children,polygons;
    private static void check(boolean ok,String text){checks++;if(!ok)throw new AssertionError(text);}
    @SuppressWarnings("unchecked") private static void export(ModelPart part,PoseStack stack,String path,List<Map<String,Object>> out)throws Exception{
        if(!part.visible)return;stack.pushPose();part.translateAndRotate(stack);
        for(var cube:(List<?>)cubes.get(part)){var faces=new ArrayList<List<float[]>>();for(var face:(Object[])polygons.get(cube))if(face!=null){var f=face.getClass().getDeclaredField("vertices");f.setAccessible(true);var vertices=new ArrayList<float[]>();for(var v:(Object[])f.get(face)){var pos=v.getClass().getDeclaredField("pos");var u=v.getClass().getDeclaredField("u");var t=v.getClass().getDeclaredField("v");pos.setAccessible(true);u.setAccessible(true);t.setAccessible(true);var xyz=stack.last().pose().transformPosition(new org.joml.Vector3f((org.joml.Vector3f)pos.get(v)).div(16));float uv=u.getFloat(v),vv=t.getFloat(v);check(uv>=0&&uv<=1&&vv>=0&&vv<=1,"UV outside atlas: "+path);vertices.add(new float[]{xyz.x*16,xyz.y*16,xyz.z*16,uv,vv});}faces.add(vertices);}out.add(Map.of("part",path,"faces",faces));}
        for(var e:((Map<String,ModelPart>)children.get(part)).entrySet())export(e.getValue(),stack,path+"/"+e.getKey(),out);stack.popPose();
    }
    @SuppressWarnings("unchecked") public static void main(String[] args)throws Exception{
        net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
        cubes=ModelPart.class.getDeclaredField("cubes");children=ModelPart.class.getDeclaredField("children");polygons=ModelPart.Cube.class.getDeclaredField("polygons");for(var f:List.of(cubes,children,polygons))f.setAccessible(true);
        var output=new LinkedHashMap<String,List<Map<String,Object>>>();
        // Exercise the production steering method, including both sides of its
        // antiparallel threshold, rather than a separately copied formula.
        for(var from:List.of(new net.minecraft.world.phys.Vec3(1,0,0),new net.minecraft.world.phys.Vec3(0,1,0),new net.minecraft.world.phys.Vec3(1,2,3).normalize())){
            var tangent=(Math.abs(from.y)<.9?new net.minecraft.world.phys.Vec3(0,1,0):new net.minecraft.world.phys.Vec3(1,0,0));
            tangent=tangent.subtract(from.scale(tangent.dot(from))).normalize();
            for(double degrees:new double[]{0,1,6,45,85,90,179,179.99,179.999,180}){
                double radians=Math.toRadians(degrees);
                var desired=from.scale(Math.cos(radians)).add(tangent.scale(Math.sin(radians)));
                var actual=FinistArrow.steer(from.scale(2.5),desired);
                check(Math.abs(actual.length()-2.5)<1e-8,"Steering speed at "+degrees);
                double turned=Math.acos(Math.clamp(from.dot(actual.normalize()),-1,1));
                check(Math.abs(turned-Math.min(radians,Math.toRadians(6)))<1e-7,"Steering turn at "+degrees);
                if(degrees>6)check(actual.normalize().dot(desired)>from.dot(desired),"Steering progresses at "+degrees);
            }
        }
        for(int form=0;form<2;form++){
        // Production guidance preserves speed and stays bounded even for full-draw
        // arrows aimed away from a nearby target; native hit tests live in #223.
        for(double speed:new double[]{.2,.72,1.87,3.6})for(double distance:new double[]{1,8,24,48})for(double degrees:new double[]{0,10,45,90,135,179.999,180}){
            double radians=Math.toRadians(degrees);var velocity=new net.minecraft.world.phys.Vec3(0,0,speed);var desired=new net.minecraft.world.phys.Vec3(Math.sin(radians)*distance,0,Math.cos(radians)*distance);var actual=FinistArrow.guideVelocity(velocity,desired);
            check(Double.isFinite(actual.x)&&Double.isFinite(actual.y)&&Double.isFinite(actual.z),"Nonfinite adaptive guidance");check(Math.abs(actual.length()-speed)<1e-8,"Adaptive steering changed speed");double turned=Math.toDegrees(Math.acos(Math.clamp(actual.normalize().z,-1,1)));check(turned<=45.000001,"Adaptive steering exceeds45 degrees");if(degrees>0)check(actual.normalize().dot(desired.normalize())>velocity.normalize().dot(desired.normalize()),"Adaptive guidance did not progress");
        }
 var model=new FinistModel<FalconActor>(form==0?FinistMesh.create():KartausMesh.create(),form==1);String who=form==0?"finist":"kartaus";
 var clips=form==0?List.of(FalconAction.IDLE,FalconAction.ARRIVE,FalconAction.RECALL,FalconAction.SWORD,FalconAction.MARK,FalconAction.INSPIRE,FalconAction.DASH,FalconAction.MORPH,FalconAction.RETREAT,FalconAction.DRINK,FalconAction.VICTORY,FalconAction.HURT):List.of(FalconAction.IDLE,FalconAction.INTRO,FalconAction.SWORD,FalconAction.BANNER,FalconAction.GUARDS,FalconAction.WAVE,FalconAction.CHAINS,FalconAction.EYE,FalconAction.CURSE,FalconAction.STAGGER,FalconAction.RAGE,FalconAction.DEATH,FalconAction.RECOVER);
 for(var action:clips){for(int tick=0;tick<=100;tick++){model.pose(action,tick,tick,.5F,tick,0,0);for(var n:model.nodes.values())check(Float.isFinite(n.xRot)&&Float.isFinite(n.yRot)&&Float.isFinite(n.zRot)&&Float.isFinite(n.x)&&Float.isFinite(n.y)&&Float.isFinite(n.z)&&Float.isFinite(n.xScale)&&Float.isFinite(n.yScale),"Nonfinite "+who+action);}
 model.pose(action,action==FalconAction.IDLE?0:action==FalconAction.BANNER?16:16,0,0,0,0,0);var mesh=new ArrayList<Map<String,Object>>();export(model.root,new PoseStack(),"root",mesh);output.put(who+"_"+action,mesh);
 if(action==FalconAction.IDLE){double min=1000,max=-1000;for(var cube:mesh){String part=(String)cube.get("part");if(part.contains("weapon"))continue;for(var face:(List<List<float[]>>)cube.get("faces"))for(var v:face){min=Math.min(min,v[1]);max=Math.max(max,v[1]);}}check(Math.abs((24-min)/16-(form==0?2.25:2.7))<.02,"Height "+who+" "+(24-min)/16);check(Math.abs(max-24)<.001,"Feet "+who);}
 }

 }
 var bird=FalconMesh.create();var bm=new ArrayList<Map<String,Object>>();export(bird,new PoseStack(),"root",bm);output.put("falcon_IDLE",bm);
 Files.createDirectories(Path.of("work/finist"));Files.writeString(Path.of("work/finist/meshes.json"),new com.google.gson.Gson().toJson(output));System.out.println("FINIST OFFLINE PASS checks="+checks+" clips="+output.size()+" NOT_GAME_RENDERING");
 }
}
